package com.example.toshiba.ardiuno_bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button btnScan;
    ListView devicelist;
    TextView txv;
    public static final String EXTRA_ADDRESS = "address";

    private BluetoothAdapter mBluetoothAdapter = null;
    ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();
    ProgressDialog mProgressDlg;
    ArrayList list = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = (Button) findViewById(R.id.button);

        devicelist = (ListView) findViewById(R.id.listView);


        txv=(TextView)findViewById(R.id.textView);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
            } else {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startDiscovery();
            }
        });

        //创建ProgressDialog对象
        mProgressDlg 		= new ProgressDialog(this);
        // 设置ProgressDialog 标题
        mProgressDlg.setTitle("提示");
        // 设置ProgressDialog 提示信息
        mProgressDlg.setMessage("Scanning...");
        // 设置进度条风格，风格为圆形，旋转的
        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 标题图标
        //mProgressDlg.setIcon(R.drawable.a);
        // 设置ProgressDialog 的进度条是否不明确
        mProgressDlg.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        mProgressDlg.setCancelable(false);
        //设置ProgressDialog 的一个Button
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                mBluetoothAdapter.cancelDiscovery();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mProgressDlg.show();
                mDeviceList = new ArrayList<BluetoothDevice>();

            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //mDeviceList.add(device);
                list.add(device.getName() + "\n" + device.getAddress());
                Toast.makeText(getApplicationContext(), "Found device " + device.getName(), Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // 让ProgressDialog显示
                mProgressDlg.dismiss();
                show();
                //Intent newIntent = new Intent(MainActivity.this, MainActivity.class);
                //newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                mBluetoothAdapter.cancelDiscovery();
                //startActivity(newIntent);
            }
        }
    };
    private void show(){
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3){
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            txv.setText(address);
            Intent i = new Intent(MainActivity.this, ledControl.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };
}
