package com.example.toshiba.ardiuno_bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import java.util.logging.LogRecord;

public class ledControl extends AppCompatActivity {

    Button btnOn, btnOff, btnDis,btnRec;
    SeekBar brightness;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    InputStream mmIntputStream;
    Thread workerThread;
    Handler h;
    final int RECIEVE_MESSAGE = 1;		// Status  for Handler
    private StringBuilder sb = new StringBuilder();
    TextView txv2;

    volatile boolean stopWorker;
    byte[] readBuffer;
    int readBufferPosition;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);
        //view of the ledControl layout
        setContentView(R.layout.activity_led_control);
        //call the widgtes
        btnOn = (Button)findViewById(R.id.btnOn);
        btnOff = (Button)findViewById(R.id.btnOff);
        btnDis = (Button)findViewById(R.id.btnDis);
        btnRec = (Button)findViewById(R.id.btnRec);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        txv2=(TextView)findViewById(R.id.textView2);

        new ConnectBT().execute(); //Call the class to connect
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //close connection
            }
        });
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginReceiveDatas();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    try {
                        btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    } catch (IOException e) {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;
        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");
        }
        @Override
        protected Void doInBackground(Void... devices){
            try{
                if (btSocket == null || !isBtConnected){
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e){
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if (!ConnectSuccess){
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
    private void Disconnect(){
        if (btSocket!=null){
            try{
                btSocket.close();
            }
            catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout
    }


    private void turnOffLed(){
        if (btSocket!=null){
            try{
                btSocket.getOutputStream().write("B".toString().getBytes());
            }
            catch (IOException e){
                msg("Error");
            }
        }
    }
    private void turnOnLed(){
        if (btSocket!=null){
            try{
                btSocket.getOutputStream().write("A".toString().getBytes());
            }
            catch (IOException e){
                msg("Error");
            }
        }
    }
    private void beginReceiveDatas(){
        try{
            btSocket.getOutputStream().write("C".toString().getBytes());
        }
        catch (IOException e){
            msg("Error");
        }

        //final Handler handler=new Handler();
        final byte delimiter=10;
        stopWorker=false;
        readBufferPosition=0;
        readBuffer=new byte[1024];
        try {
            mmIntputStream=btSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        workerThread=new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted()&&!stopWorker){
                    try {
                        int bytesAvailable=mmIntputStream.available();
                        if(bytesAvailable>0){
                            byte[] packetBytes=new byte[bytesAvailable];

                            String str1="";
                            mmIntputStream.read(packetBytes);
                            String str2=new String(packetBytes,"UTF-8");
                            str1=str1+packetBytes[0];
                            Log.e("in",  str2);
                            for(int i=0;i<bytesAvailable;i++){
                                byte b=packetBytes[i];
                                if(b==delimiter){
                                    byte[] encodeBytes=new byte[readBufferPosition];
                                    System.arraycopy(readBuffer,0,encodeBytes,0,encodeBytes.length);
                                    final String data=new String(encodeBytes,"US-ASCII");
                                    readBufferPosition=0;

                                }
                                else {
                                    readBuffer[readBufferPosition++]=b;
                                }
                            }


                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


}
