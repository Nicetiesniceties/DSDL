package com.example.chenhongsheng.ble2;

import android.bluetooth.BluetoothGattCallback;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Set;
import java.util.ArrayList;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCallback;
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


public class MainActivity extends AppCompatActivity {
    private Button Open,Connect,Detect,Send;
    private EditText Et;
    private TextView tv;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattCharacteristic mWriteCharacteristic = null;
    private BluetoothGattCharacteristic mReadCharacteristric = null;
    private BluetoothGatt mBluetoothGatt = null;
    final String address = new String("B8:27:EB:DA:3C:DF");

    private Context mContext,context;
    private int mState = 0;
    // 設備連接狀態
    private final int CONNECTED = 0x01;
    private final int DISCONNECTED = 0x02;
    private final int CONNECTTING = 0x03;

    private final String TAG = "BLE";

    // 讀寫相關的Service、Characteristic的UUID
    public static final UUID TRANSFER_SERVICE_READ = UUID.fromString("34567817-2432-5678-1235-3c1d5ab44e17");
    public static final UUID TRANSFER_SERVICE_WRITE = UUID.fromString("34567817-2432-5678-1235-3c1d5ab44e18");
    public static final UUID TRANSFER_CHARACTERISTIC_READ = UUID.fromString("23487654-5678-1235-2432-3c1d5ab44e94");
    public static final UUID TRANSFER_CHARACTERISTIC_WRITE = UUID.fromString("23487654-5678-1235-2432-3c1d5ab44e93");


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG,"The state changes,status ");
            Log.d(TAG, "status = " + status + "newState" + newState );

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    mState = CONNECTED;
                    Log.i(TAG, "Connected to GATT server.");
                    //bluetooth is connected so discover services
                    Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    mState = DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                }


            }else if(status == 133){
                //Log.d(TAG, "status = "+ status +" newState =" + newState);
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                Log.d(TAG, "Trying to create a new connection again.");
                mState = CONNECTTING;
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);


            }else{
                //Bluetooth is disconnected
                mState = DISCONNECTED;
                Log.d(TAG, "change to disconnect");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG,"The service changes");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService btGattWriteService = mBluetoothGatt
                        .getService(TRANSFER_SERVICE_WRITE);
                BluetoothGattService btGattReadService = mBluetoothGatt
                        .getService(TRANSFER_SERVICE_READ);
                if (btGattWriteService != null) {
                    mWriteCharacteristic = btGattWriteService
                            .getCharacteristic(TRANSFER_CHARACTERISTIC_WRITE);
                }
                if (btGattReadService != null) {
                    mReadCharacteristric = btGattReadService
                            .getCharacteristic(TRANSFER_CHARACTERISTIC_READ);
                    if (mReadCharacteristric != null) {
                        mBluetoothGatt.readCharacteristic(mReadCharacteristric);
                    }
                }
            }


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG,"The read char is available");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacterisricValue(characteristic);

                // 訂閱遠端設備的characteristic，
                // 當此characteristic發生改變時當回調mBtGattCallback中的onCharacteristicChanged方法
                mBluetoothGatt.setCharacteristicNotification(mReadCharacteristric,
                        true);
                BluetoothGattDescriptor descriptor = mReadCharacteristric
                        .getDescriptor(UUID
                                .fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor != null) {
                    byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    descriptor.setValue(val);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG,"The char changes");

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            super.onCharacteristicWrite(gatt, characteristic, status);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    Log.d(TAG, "write data success");
                    break;// 寫入成功
                case BluetoothGatt.GATT_FAILURE:
                    Log.d(TAG, "write data failed");
                    break;// 寫入失敗
                case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                    Log.d(TAG, "write not permitted");
                    break;// 沒有寫入的權限
            }

        }
    };

    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothSocket mBluetoothSocket;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    /*public class DeviceScanActivity extend ListActivity{

    }
    */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mContext = context;
        Et = (EditText) findViewById(R.id.Et);
        Detect = (Button)findViewById(R.id.button0);
        Open = (Button)findViewById(R.id.button1);
        Connect =(Button)findViewById(R.id.button2);
        Send = (Button)findViewById(R.id.button3);
        tv = (TextView)findViewById(R.id.textview1);
    }
    public void Detect(View view){//偵測裝置是否支援BLE
        if (!getPackageManager().hasSystemFeature
                (PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "此硬體不支援BLE", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, "硬體支援BLE", Toast.LENGTH_SHORT).show();
        }

    }
    public void Open(View view){//開啟藍芽
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }
    public void Connect(View view){

        mBluetoothGatt = null;
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
        }
        Log.d(TAG, "Trying to create a new connection.");
        Log.d(TAG, "BluetoothDeviceAddress is :" + address);
        mState = CONNECTTING;

        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);

    }
    public void Send(View view){
        String string = Et.getText().toString();
        byte[] byte_to_write = string.getBytes();
        sendData(byte_to_write);
    }
    private void readCharacterisricValue(
            BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        StringBuffer buffer = new StringBuffer("0x");
        int i;
        for (byte b : data) {
            i = b & 0xff;
            buffer.append(Integer.toHexString(i));
        }
        String readmsg = buffer.toString();
        tv.setText(readmsg);
    }
    private void sendData(byte[] data) {
        if (data != null && data.length > 0 && data.length < 21) {
            if (mWriteCharacteristic.setValue(data)
                    && mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
                Log.d(TAG, "send data OK");
            }
        }
    }

}
