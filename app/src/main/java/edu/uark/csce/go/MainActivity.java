package edu.uark.csce.go;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        closed_lock = (ImageView)findViewById(R.id.closed_lock);
        open_lock = (ImageView)findViewById(R.id.open_lock);

        deviceAddress = "64:6E:69:E1:90:A6";

        final ConnectThread connectThread = new ConnectThread(bluetoothAdapter.getRemoteDevice(deviceAddress));
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

        public ConnectThread(BluetoothDevice _device){
            BluetoothSocket tmp = null;
            device = _device;

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
            try{
                outputStream = socket.getOutputStream();
                outputStream.write(command.getBytes());
                Log.i("BLUETOOTH", "Message Sent");
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


