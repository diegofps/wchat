package br.com.wespa.wchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.com.wespa.wchat.comm.local.EventHelper;
import br.com.wespa.wchat.services.MqttClient;

/**
 * Created by dsouza on 26/08/17.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText name;
    private EditText pass;
    private Button btAuth;
    private EventHelper bcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText) findViewById(R.id.fdName);
        pass = (EditText) findViewById(R.id.fdPass);

        bcast = new EventHelper(getApplicationContext(), this)
                .map(MqttClient.OUT_EVENT_CONNECTION_ALREADY_CONNECTED, "onEventConnectionSuccess")
                .map(MqttClient.OUT_EVENT_CONNECTION_SUCCESS, "onEventConnectionSuccess")
                .map(MqttClient.OUT_EVENT_CONNECTION_FAIL, "onEventConnectionFail")
                .register();

        btAuth = (Button) findViewById(R.id.btAuth);
        btAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bcast.newEvent(MqttClient.IN_EVENT_CONNECT)
                        .add(MqttClient._EVENT_CONNECT_USERNAME, name.getText().toString())
                        .add(MqttClient._EVENT_CONNECT_PASSWORD, pass.getText().toString())
                        .send();
            }
        });

        startService(new Intent(this, MqttClient.class));
    }

    private void onEventConnectionSuccess(Bundle bundle) {
        startActivity(new Intent(this, ChatActivity.class));
    }

    private void onEventConnectionFail(Bundle bundle) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_LONG).show();
    }

}
