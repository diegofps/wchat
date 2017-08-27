package br.com.wespa.wchat.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import br.com.wespa.wchat.ChatActivity;
import br.com.wespa.wchat.R;
import br.com.wespa.wchat.comm.cast.Merlin;
import br.com.wespa.wchat.comm.dto.MessageDTO;
import br.com.wespa.wchat.comm.local.EventHelper;
import br.com.wespa.wchat.persistency.models.Message;
import br.com.wespa.wchat.persistency.preferences.Settings;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by dsouza on 24/08/17.
 */

public class MqttClient extends Service {

    public static final String IN_EVENT_CONNECT = "br.com.wespa.wchat.MqttClient.IN_EVENT_CONNECT";
    public static final String _EVENT_CONNECT_USERNAME = "username";
    public static final String _EVENT_CONNECT_PASSWORD = "password";


    public static final String IN_EVENT_DISCONNECT =
            "br.com.wespa.wchat.MqttClient.IN_EVENT_DISCONNECT";

    public static final String OUT_EVENT_CONNECTION_FAIL =
            "br.com.wespa.wchat.MqttClient.OUT_EVENT_CONNECTION_FAIL";

    public static final String OUT_EVENT_CONNECTION_SUCCESS =
            "br.com.wespa.wchat.MqttClient.OUT_EVENT_CONNECTION_SUCCESS";

    public static final String OUT_EVENT_CONNECTION_ALREADY_CONNECTED =
            "br.com.wespa.wchat.MqttClient.OUT_EVENT_CONNECTION_ALREADY_CONNECTED";

    private static final String SERVER_URI = "tcp://m12.cloudmqtt.com:13925";
    private static final String TOPIC_GENERAL = "general";


    private static MqttAndroidClient mMqttClient;
    private static EventHelper events;
    private Settings mSettings;
    private Realm realm;
    private RealmResults<Message> messages;
    private Merlin merlin;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        Log.w("MqttClient", "Service started");

        merlin = new Merlin();

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

        if (events == null) {
            events = new EventHelper(getApplicationContext(), this)
                    .map(IN_EVENT_CONNECT, "onEventConnect");
        }

        events.register();
        mSettings = new Settings(context);

        createClient(context);
        connect(mSettings.getUsername(), mSettings.getPassword());

        messages = realm.where(Message.class).equalTo("isSent", false).findAllAsync();
        messages.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Message>>() {
            @Override
            public void onChange(RealmResults<Message> messages, OrderedCollectionChangeSet changeSet) {
                onNewMessage(messages, changeSet);
            }
        });
    }

    private void onNewMessage(RealmResults<Message> messages, OrderedCollectionChangeSet changes) {
        if (changes != null)
            for (int i : changes.getInsertions())
                sendMessage(messages.get(i));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        events.unregister();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY_COMPATIBILITY;
    }

    public void onEventConnect(Bundle data) {
        if (mMqttClient.isConnected()) {
            Log.w("MQTTClient", "Already connected");
            events.newEvent(OUT_EVENT_CONNECTION_ALREADY_CONNECTED).send();
            return;
        }

        String username = data.getString(_EVENT_CONNECT_USERNAME, null);
        String password = data.getString(_EVENT_CONNECT_PASSWORD, null);
        connect(username, password);
    }

    public static boolean isConnected() {
        return mMqttClient != null && mMqttClient.isConnected();
    }

    private void createClient(Context context) {
        if (mMqttClient != null)
            return;

        mMqttClient = new MqttAndroidClient(context, SERVER_URI, mSettings.getUUID());
        mMqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Mqtt", "Connected: " + b + " - " + s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.w("Mqtt", "Connection lost");
            }

            @Override
            public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {
                String raw = mqttMessage.toString();
                Log.w("RAW Message", raw);

                final MessageDTO dto = merlin.fromJson(raw, MessageDTO.class);
                Message msg = realm.where(Message.class).equalTo("uuid", dto.uuid).findFirst();

                if (msg != null)
                    return;

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Number id = realm.where(Message.class).max("id");

                        Message msg = new Message();
                        msg.setId(id == null ? 1 : id.intValue() + 1);
                        msg.setTopic(topic);
                        msg.setSender(dto.sender);
                        msg.setContent(dto.content);
                        msg.setUuid(dto.uuid);
                        msg.setSent(true);
                        realm.insertOrUpdate(msg);
                    }
                });

                if (!ChatActivity.isVisible)
                    showNewMessageNotification(dto.sender);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.w("Mqtt", "Delivery complete");
                try {
                    int id = iMqttDeliveryToken.getMessage().getId();
                    final Message msg = realm.where(Message.class).equalTo("id", id).findFirst();

                    if (msg == null)
                        return;

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            msg.setSent(true);
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showNewMessageNotification(String name) {
        Intent intent = new Intent(MqttClient.this, ChatActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                MqttClient.this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MqttClient.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("WChat")
                .setContentText("New message from " + name)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }

    public void connect(final String username, final String password) {
        if (username == null || password == null) {
            Log.w("MQTTClient", "Connection failed");
            events.newEvent(OUT_EVENT_CONNECTION_FAIL).send();
            return;
        }

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setCleanSession(false);
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());

        try {
            mMqttClient.connect(connectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectOptions = new DisconnectedBufferOptions();
                    disconnectOptions.setBufferEnabled(true);
                    disconnectOptions.setBufferSize(100);
                    disconnectOptions.setPersistBuffer(false);
                    disconnectOptions.setDeleteOldestMessages(false);
                    mMqttClient.setBufferOpts(disconnectOptions);

                    subscribeToTopic("general");
                    mSettings.saveCredentials(username, password);
                    events.newEvent(OUT_EVENT_CONNECTION_SUCCESS).send();
                    sendPendingMessages();

                    Log.w("MQTTClient", "Connection Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + SERVER_URI + exception.toString());
                    events.newEvent(OUT_EVENT_CONNECTION_FAIL).send();
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
            events.newEvent(OUT_EVENT_CONNECTION_FAIL).send();
        }

        Log.w("MQTTClient", "Connection failed");
    }

    private void sendPendingMessages() {
        for (Message msg : realm.where(Message.class).equalTo("isSent", false).findAll()) {
            sendMessage(msg);
        }
    }

    private synchronized void sendMessage(final Message msg) {
        if (mMqttClient == null)
            return;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    Log.w("Client", "Sending msg: " + msg.getContent());

                    if (msg.getTopic() == null)
                        msg.setTopic(TOPIC_GENERAL);

                    MessageDTO dto = merlin.Cast(msg, MessageDTO.class);
                    MqttMessage pkg = new MqttMessage(merlin.toJson(dto).getBytes());
                    pkg.setId(msg.getId());
                    pkg.setQos(2);
                    pkg.setRetained(true);
                    mMqttClient.publish(msg.getTopic(), pkg);
                    realm.insertOrUpdate(msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                    Log.i("MQTTClientService", "Message not sent");
                }
            }
        });
    }

    private void subscribeToTopic(final String topic) {
        try {
            mMqttClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt", "Subscribed to " + topic + "!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscription to " + topic + " failed!");
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception while subscribing to " + topic);
            ex.printStackTrace();
        }
    }

    public class MqttBinder extends Binder {
        public MqttClient getServiceInstance() {
            return MqttClient.this;
        }
    }
}
