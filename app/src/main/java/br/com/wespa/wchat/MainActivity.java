package br.com.wespa.wchat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.wespa.wchat.comm.local.EventHelper;
import br.com.wespa.wchat.services.MqttClient;

public class MainActivity extends AppCompatActivity {

    private EventHelper events;
    private long startedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startedAt = System.currentTimeMillis();
        events = new EventHelper(getApplicationContext(), this)
                .map(MqttClient.OUT_EVENT_CONNECTION_FAIL, "onEventConnectionFail")
                .map(MqttClient.OUT_EVENT_CONNECTION_SUCCESS, "onEventConnectionSuccess")
                .register();

        if (MqttClient.isConnected()) {
            finish();
            startActivity(new Intent(this, ChatActivity.class));
        }
        else {
            setContentView(R.layout.activity_main);
            startService(new Intent(this, MqttClient.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        events.unregister();
    }

    private void onEventConnectionSuccess(Bundle bundle) {
        scheduleOpen(ChatActivity.class);
    }

    private void onEventConnectionFail(Bundle bundle) {
        scheduleOpen(LoginActivity.class);
    }

    private <T> void scheduleOpen(final Class<T> activityClass) {
        long time = 1000 - (System.currentTimeMillis() - startedAt);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(MainActivity.this, activityClass));
            }
        };

        new Handler().postDelayed(runnable, time);
    }

}
