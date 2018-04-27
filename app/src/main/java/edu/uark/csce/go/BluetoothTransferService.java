package edu.uark.csce.go;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by toren on 4/6/18.
 */

public class BluetoothTransferService {
    private Handler handler;

    public BluetoothTransferService(BluetoothSocket _socket, String toSend){
        handler = new Handler();
        ConnectThread client = new ConnectThread(_socket);
        client.write(toSend.getBytes());
    }

    public BluetoothTransferService(BluetoothSocket _socket){
        ConnectThread client = new ConnectThread(_socket);
        client.run();
    }

    private interface MessageAction {
        public static final int MESSAGE_READ  = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    private class ConnectThread extends Thread{
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] buffer;

        public ConnectThread(BluetoothSocket _socket){
            socket = _socket;
            InputStream tmpIn = null;
            OutputStream tmpOut= null;

            try{
                tmpIn = socket.getInputStream();
            }catch(IOException e){
                Log.e("BT_T_SERVICE", "Could not get Input stream from socket");
            }
            try{
                tmpOut = socket.getOutputStream();
            }catch(IOException e){
                Log.e("BT_T_SERVICE", "Could not get Output stream from socket");
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run(){
            buffer = new byte[1024];
            int numBytes;

            Log.e("BT_T_SERVICE", "Reading from bluetooth");
            while(true){
                try{
                    numBytes = inputStream.read(buffer);

                    Message readMsg = handler.obtainMessage(MessageAction.MESSAGE_READ, numBytes, -1, buffer);
                    readMsg.sendToTarget();
                }catch(IOException e){
                    Log.d("BT_T_SERVICE", "Input stream was disconnected", e);
                    break;
                }

            }
        }

        public void write(byte[] bytes){
            try{
                outputStream.write(bytes);

                Message writtenMsg = handler.obtainMessage(MessageAction.MESSAGE_WRITE, -1, -1, buffer);
                writtenMsg.sendToTarget();
                Log.i("BT_T_SERVICE", "Message Sent");
            }catch(IOException e){
                Log.e("BT_T_SERVICE", "Could not send message");

                Message writeErrorMsg = handler.obtainMessage(MessageAction.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send message");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel(){
            try{
                socket.close();
            }catch(IOException e){
                Log.e("BT_T_SERVICE", "Could not close socket", e);
            }
        }
    }
}
