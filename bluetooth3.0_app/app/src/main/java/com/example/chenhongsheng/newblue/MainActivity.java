package com.example.chenhongsheng.ble;
import java.util.Set;
import java.util.ArrayList;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import android.widget.AdapterView;
import android.support.v4.app.ActivityCompat;
import static android.Manifest.permission.*;
import android.Manifest;
import android.bluetooth.BluetoothSocket;
import java.lang.Thread;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.NoSuchMethodException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.Parcelable;
import android.os.Message;
import java.util.UUID;

public class MainActivity extends Activity {
    private Button Open,Connect,Send;
    private EditText Et;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private static final int REQUEST_ENABLE_BT = 2;
    private TextView tv;
    private BluetoothSocket BT_socket;
    private Message msg;
    private final String TAG = "BLE";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //處理接收到的訊息
            String tem = (String) msg.obj;
            tv.setText(tem);
        }
    };
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private InputStream inputstream;
    private OutputStream outputstream;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter(); //初始Adapter
        Open = (Button)findViewById(R.id.button1);
        Connect = (Button)findViewById(R.id.button2);
        Et = (EditText) findViewById(R.id.Et);
        Send = (Button) findViewById(R.id.Send);
        tv = (TextView) findViewById(R.id.textview1);
            //看看手機硬體是否支援BLE
        if (!getPackageManager().hasSystemFeature
                (PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "此硬體不支援BLE", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, "硬體支援BLE", Toast.LENGTH_SHORT).show();
        }
   ;
    }

    public void Send(View view){
        Toast.makeText(getApplicationContext(),"傳送中", Toast.LENGTH_SHORT).show();
        if(BT_socket!= null ){
            try{
                String string = Et.getText().toString();
                byte[] byte_to_write = string.getBytes();
                outputstream.flush();
                outputstream.write(byte_to_write);
            }catch(IOException e){
                Toast.makeText(MainActivity.this,"訊息傳送失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(MainActivity.this,"傳送成功", Toast.LENGTH_SHORT).show();
        tv.setText("訊息已傳送");
    }
    public void Open(View view){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "請開啟藍芽裝置", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    public void Connect(View view){
        final String address = new String("B8:27:EB:DA:3C:DF");
        //final String address = new String("78:67:D7:44:A8:41");
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},0);
        BluetoothSocket BTsocket_tmp;
        BluetoothAdapter btAdapter = mBluetoothAdapter.getDefaultAdapter();
        BluetoothDevice tmp_device = null;
        tmp_device = mBluetoothAdapter.getRemoteDevice(address);
        //檢查device是否連上pi藍芽
        if(tmp_device == null){
            Toast.makeText(getApplicationContext(),"No connection",Toast.LENGTH_SHORT).show();
            return;
        }else{
            Toast.makeText(getApplicationContext(),"connection",Toast.LENGTH_SHORT).show();
        }

            //嘗試用UUID對pi建立socket
        try{
            BTsocket_tmp = tmp_device.createRfcommSocketToServiceRecord(MY_UUID);
        }catch (IOException e) {
            Toast.makeText(MainActivity.this,"socket建立失敗", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(),"connection too",Toast.LENGTH_SHORT).show();
        BT_socket = BTsocket_tmp;
            //
        try{
            BT_socket = (BluetoothSocket) tmp_device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(tmp_device, 1);

        }catch(Exception e) {
            Toast.makeText(MainActivity.this,"又失敗了",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(),"connection 3",Toast.LENGTH_SHORT).show();
        try{
            BT_socket.connect();
        }catch(IOException e) {
                // 對socket連線失敗
            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            inputstream = BT_socket.getInputStream();
            outputstream = BT_socket.getOutputStream();
        }catch(IOException e) {
            Toast.makeText(MainActivity.this,"IOStream建立失敗",Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this,"連線成功啦",Toast.LENGTH_SHORT).show();

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){

                byte[] buffer = new byte[1024];
                int readmsg;

                while(true){
                    try{
                        readmsg = inputstream.available();
                        if(readmsg != 0){
                            inputstream.read(buffer, 0, readmsg);
                            String read_mess = new String(buffer, "UTF-8");
                            Message msg = Message.obtain();
                            msg.obj = read_mess;
                            msg.setTarget(mHandler);
                            msg.sendToTarget();
                        }
                    }
                    catch(IOException e){
                        break;
                    }
                }
            }
        });
        thread.start();
    }



}