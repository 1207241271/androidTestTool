package com.xunce.bluetoothgatt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xunce.xctestingtool.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by xunce on 2017/9/11.
 */

public class BluetoothHolder {
    private static final String TAG = BluetoothHolder.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeService mBluetoothLeService;

    private boolean mScanning;
    private Handler mHandler;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    //byte[] WriteBytes = null;
    byte[] WriteBytes = new byte[20];
    private String mDeviceAddress;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public void init(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }
        mContext = context;
        mHandler = new Handler();
    }

    public void registerCallBack() {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    public void unregisterCallBack() {

    }


    public void scanDeviceAndConnect(final String imei, final boolean enable) {
        if (mContext == null) {
            throw new RuntimeException("must call init first!");
        }
        if (mBluetoothAdapter == null) {
            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (!mBluetoothAdapter.isEnabled() && enable) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        if (TextUtils.isEmpty(imei) || imei.length() != 15) {
            Toast.makeText(mContext, "无效的imei", Toast.LENGTH_SHORT).show();
            return;
        }
        final SerialPortScanCallBack scanCallback = new SerialPortScanCallBack(this, imei);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(scanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(scanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(scanCallback);
        }
    }

    public boolean isScaning() {
        return mScanning;
    }


    private static class SerialPortScanCallBack implements BluetoothAdapter.LeScanCallback {
        private String mAimImei;
        private WeakReference<BluetoothHolder> holderRef;
        public SerialPortScanCallBack(BluetoothHolder holder, String imei) {
            holderRef = new WeakReference<BluetoothHolder>(holder);
            mAimImei = imei;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "device scaned : " + (device.getName()));
            if (mAimImei.equals(device.getName())) {
                if (holderRef == null || holderRef.get() == null) {
                    return;
                }
                BluetoothHolder holder = holderRef.get();
                holder.mDeviceAddress = device.getAddress();
                Intent gattServiceIntent = new Intent(holder.mContext, BluetoothLeService.class);
                holder.mContext.bindService(gattServiceIntent, holder.mServiceConnection, Context.BIND_AUTO_CREATE);

                if (holder.mScanning) {
                    holder.mBluetoothAdapter.stopLeScan(this);
                    holder.mScanning = false;
                }
            }
        }
    }

    public void writeCmd(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return;
        }
        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCmd(cmd);
        }
    }

    public void quit() {
        if (mContext != null) {
            mContext.unbindService(mServiceConnection);
        }
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();
        }
    }

}
