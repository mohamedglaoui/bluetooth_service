package app.com.bluetooth_service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.IBinder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    int notificationId = 2;
    String channelId = "channel-01";
    String channelName = "Channel Name";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    private static final int REQUEST_ENABLE_BT = 456;

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothClient bluetoothClient = null;
    private List<BluetoothDevice> knownDevices = null;

    BroadcastReceiver mreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message_key");
            Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();
             bluetoothClient.writeString(message);
        }};
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        LocalBroadcastManager.getInstance(getApplicationContext()).
                registerReceiver(mreceiver,new IntentFilter("service_message"));


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        LocalBroadcastManager.getInstance(getApplicationContext()).
                unregisterReceiver(mreceiver);
            bluetoothClient.close();
        Toast.makeText( getApplicationContext(), "HC-05 disconnected", Toast.LENGTH_LONG ).show();

    }


    @Override
    public void onCreate() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("98:D3:36:00:BD:03");
        bluetoothClient = new BluetoothClient( device );



        bluetoothClient.start();

    }







    private class BluetoothClient extends Thread {
        final Intent intent=new Intent(getApplicationContext(), MyService.class);
        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean isAlive = true;
        public  int i=0;

        public BluetoothClient( BluetoothDevice device ) {
            try {
                bluetoothDevice = device;
                bluetoothSocket = device.createRfcommSocketToServiceRecord( device.getUuids()[0].getUuid() );
                bluetoothSocket.connect();

                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();


                Toast.makeText( getApplicationContext(), device.getName() + " connected", Toast.LENGTH_LONG ).show();
            } catch ( IOException exception ) {
                Log.e( "DEBUG", "Cannot establish connection", exception );
                Toast.makeText( getApplicationContext(), device.getName() + " Cannot establish connection", Toast.LENGTH_LONG ).show();
            }
        }


        // Inutile dans le code actuel. Mais cela permettrait de recevoir
        // des informations du véhicule dans une future version.
        @Override
        public void run() {
            int i=0;
            //   Toast.makeText( MainActivity.this, "no", Toast.LENGTH_LONG ).show();
            byte[] buffer = new byte[2048];  // buffer store for the stream
            int bytes;
            while (true) {

                try {


                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();


                    if(inputStream.available()>0){
                        bytes = inputStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                        String  title = "ble is:";
                        String  body = readMessage;

                        showNotification(getApplicationContext(), title,  body, intent);
                       }
                    else{
                        Thread.sleep(100);
                    }



                } catch (Exception exception) {
                    Log.e("DEBUG", "Cannot read data", exception);
                    //   close();

                }
            }
        }



        // Termine la connexion en cours et tue le thread
        public void close() {
            try {
                bluetoothSocket.close();

                isAlive = false;
            } catch (IOException e) {
                Log.e( "DEBUG", "Cannot close socket", e );
            }
        }
        public void writeChar(char code) {

            try {

                outputStream.write( code );

                outputStream.flush();

            } catch (IOException e) {
                Log.e( "DEBUG", "Cannot write message", e );
            }
        }
        public void writeString(String text) {

            try {
                outputStream.write(text.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                /* .setSmallIcon(R.mipmap.ic_launcher)
                 .setContentTitle(title)
                 .setContentText(body);*/
                .setSmallIcon(R.drawable.icon)     // drawable for API 26
                .setAutoCancel(true)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setContentText(body)
                .setVibrate(new long[] { 0, 500, 110, 500, 110, 450, 110, 200, 110,
                        170, 40, 450, 110, 200, 110, 170, 40, 500 } )
                .setLights(Color.RED, 3000, 3000);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        //startForeground(notificationId, mBuilder.build());
        //   stopForeground(false);
        notificationManager.notify(notificationId, mBuilder.build());
    }

}
