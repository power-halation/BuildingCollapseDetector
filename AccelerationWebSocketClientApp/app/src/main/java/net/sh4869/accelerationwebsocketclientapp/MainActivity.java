package net.sh4869.accelerationwebsocketclientapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.EditText;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private WebSocketClient mClient;
    SensorManager manager;
    int rate = SensorManager.SENSOR_DELAY_GAME;
    float[] gravity = new float[3];
    Queue<float[]> gravitys = new ArrayDeque<float[]>(100);
    float accgra = 0;
    boolean sendCollsp = true;
    int count = 0;
    Timer reconnectTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        reconnectTimer = new Timer();
        reconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(mClient != null) {
                    if (mClient.getReadyState() == WebSocket.READYSTATE.CLOSED || mClient.getReadyState() == WebSocket.READYSTATE.CLOSING) {
                        connectWebSocket(null);
                    }
                }
            }
        },0,10000);
    }

    public void connectWebSocket(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String uri = editText.getText().toString();
        if(uri != ""){
            try {
                mClient = new WebSocketClient(new URI(uri)) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        Log.d("WEBSOCKET", "Open");
                        mClient.send("Android");
                    }

                    @Override
                    public void onMessage(String message) {

                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {

                    }

                    @Override
                    public void onError(Exception ex) {

                    }
                };
                mClient.connect();
            }catch (URISyntaxException e){

            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), rate);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), rate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);

    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
        }

        String text = String.valueOf(gravity[0]) + " " + String.valueOf(gravity[1])  + " " + String.valueOf(gravity[2]);
        if(Math.abs(gravity[0]) > 10 && Math.abs(gravity[0]) > 10){
            if(sendCollsp) {
                try {
                    if (mClient != null) {
                        mClient.send("collapsed");
                    }
                } catch (WebsocketNotConnectedException e) {

                } catch (NotYetConnectedException e) {

                }
                count = 0;
                sendCollsp = false;
            }
        }
        count++;
        if(count > 1000) sendCollsp = true;
        Log.d("SENSOR",text + " | count : " + String.valueOf(count));
        /*
        try {
            if(mClient != null) {
                mClient.send(text);
            }
        } catch (WebsocketNotConnectedException e){

        } catch (NotYetConnectedException e){

        }
        */
    }
}
