package edu.uark.csce.go;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView tv;

    ImageView closed_lock, open_lock;


    private String deviceAddress;

    BluetoothAdapter bluetoothAdapter;
    ConnectThread connectThread;

    Handler handler;

    private interface MessageConstants{
        public static final int READ = 0;
        public static final int WRITE = 1;
        public static final int TOAST = 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        closed_lock = (ImageView)findViewById(R.id.closed_lock);
        open_lock = (ImageView)findViewById(R.id.open_lock);

        deviceAddress = "B8:27:EB:23:5B:83";
        boolean piIsPaired = false;

        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

        for(BluetoothDevice device : devices){
            if(device.getAddress().equals(deviceAddress)){
                piIsPaired = true;
                break;
            }
        }

        handler = new Handler();

        connectThread = new ConnectThread();
        connectThread.run();


        open_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closed_lock.setVisibility(closed_lock.VISIBLE);
                open_lock.setVisibility(open_lock.GONE);
            }
        });

        closed_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closed_lock.setVisibility(closed_lock.GONE);
                open_lock.setVisibility(open_lock.VISIBLE);
                connectThread.sendCommand();
            }
        });
    }


    private class ConnectThread extends Thread{
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        byte[] buffer = new byte[1024];

        public ConnectThread(){

            BluetoothSocket tmp = null;
            device = bluetoothAdapter.getRemoteDevice(deviceAddress);

            try{
                tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());

            }catch (IOException e){
                Log.e("BLUETOOTH", "Could Not Create Socket");
            }

            socket = tmp;
        }

        public void run(){
            try{
                socket.connect();
                if(socket.isConnected()){
                    Log.i("BLUETOOTH", "Socket Connected");
                }
            }catch(IOException e){
                Log.e("BLUETOOTH", "Socket could not connect");
                return;
            }
        }

        public void sendCommand(){
            OutputStream outputStream;
            String command = "1234";
            byte[] bytes = command.getBytes();
            try{
                outputStream = socket.getOutputStream();
                outputStream.write(bytes);
                //Log.i("BLUETOOTH", "Message Sent");
                Log.i("BLUETOOTH", "OStream Opened Successfully");
            }catch(IOException e){
                Log.e("BLUETOOTH", "Could not open output stream");
            }
        }

        public void cancel(){
            try{
                socket.close();
                if(!socket.isConnected()){
                    Log.i("BLUETOOTH", "Socket Disconnected");
                }
            }catch(IOException e){
                Log.e("BLUETOOTH", "could not close socket");
            }
        }

    }
}


