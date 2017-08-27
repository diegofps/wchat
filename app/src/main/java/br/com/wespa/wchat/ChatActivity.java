package br.com.wespa.wchat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

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

public class ChatActivity extends AppCompatActivity {

    private EditText inputMsg;
    private Button btSend;
    private EventHelper events;
    private ListView msgList;
    private MessageAdapter mAdapter;
    private RealmResults messages;
    private Realm realm;
    private Settings mSettings;

    public static boolean isVisible;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Context context = getApplicationContext();
        mSettings = new Settings(context);
        events = new EventHelper(context, this).register();

        inputMsg = (EditText) findViewById(R.id.inputMsg);
        btSend = (Button) findViewById(R.id.btSend);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSend();
            }
        });

        mAdapter = new MessageAdapter();
        msgList = (ListView) findViewById(R.id.messageList);
        msgList.setAdapter(mAdapter);

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

        messages = realm.where(Message.class).findAllAsync();
        messages.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Message>>() {
            @Override
            public void onChange(RealmResults<Message> messages, OrderedCollectionChangeSet changeSet) {
                onMessageListChanged(messages, changeSet);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    private void onMessageListChanged(RealmResults<Message> elements, OrderedCollectionChangeSet changes) {
        if (changes == null) {
            mAdapter.clear();
            for (Message msg : elements)
                mAdapter.add(msg);
        } else {
            for (int i : changes.getInsertions())
                mAdapter.add(elements.get(i));
        }

        mAdapter.notifyDataSetChanged();
        msgList.setSelection(msgList.getCount() - 1);
    }

    private void onClickSend() {
        final String msgContent = inputMsg.getText().toString();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number id = realm.where(Message.class).max("id");

                Message msg = new Message();
                msg.setId(id == null ? 1 : id.intValue() + 1);
                msg.setUuid(UUID.randomUUID().toString());
                msg.setContent(msgContent);
                msg.setSender(mSettings.getUsername());
                msg.setCreatedAt(new Date());
                msg.setSent(false);
                realm.insert(msg);
            }
        });

        inputMsg.setText("");
    }

    public class MessageAdapter extends BaseAdapter {

        private final ArrayList<Message> mMsgs;

        public MessageAdapter() {
            mMsgs = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mMsgs.size();
        }

        @Override
        public Object getItem(int position) {
            return mMsgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(mMsgs.get(position).toString());

            return view;
        }

        public void add(Message msg) {
            mMsgs.add(msg);
        }

        public void clear() {
            mMsgs.clear();
        }

    }

}
