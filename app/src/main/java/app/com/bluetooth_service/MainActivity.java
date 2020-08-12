package app.com.bluetooth_service;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 456;

    private BluetoothAdapter bluetoothAdapter = null;



    private TextView lblConnectedDevice;
    private Button connect;
    private Button ON;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start = findViewById( R.id.button1);

        Button stop = findViewById( R.id.button2 );
        stop.setOnClickListener( buttonsListener );
        start.setOnClickListener( buttonsListener );




        ON = findViewById( R.id.ON );



        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( ! bluetoothAdapter.isEnabled() ) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }



        ON.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("service_message");
                if (ON.getText().equals("ON")) {

                intent.putExtra("message_key", "P");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                ON.setText("OFF");
            }else{
                    intent.putExtra("message_key", "L");
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    ON.setText("ON");
                }
            }
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( ! bluetoothAdapter.isEnabled() ) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    private View.OnClickListener buttonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            switch( view.getId() ) {
                case R.id.button1: startService(new Intent(MainActivity.this, MyService.class)); break;
                case R.id.button2:  stopService(new Intent(MainActivity.this, MyService.class)); break;


            }


        }
    };









}

