package com.xunce.xctestingtool;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.xunce.bluetoothgatt.BluetoothHolder;
import com.xunce.bluetoothgatt.BluetoothLeService;
import com.xunce.xctestingtool.databinding.ActivityMainBinding;
import com.xunce.xctestingtool.utils.KeyBoardUtils;

public class MainActivity extends AppCompatActivity implements  MainContract.View{
    private static final int  Manguo = 1;
    private static final int  Xiaomi = 2;
    private ActivityMainBinding mBinding;
    private MainContract.Presenter mPresenter;
    private boolean mConnected = false;
    private BluetoothHolder mBluetoothHolder;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("BLE", "connected !!!!");
                mConnected = true;
                if (mBinding != null) {
                    mBinding.cmd1.setVisibility(View.VISIBLE);
                    mBinding.cmd2.setVisibility(View.VISIBLE);
                    mBinding.cmd3.setVisibility(View.VISIBLE);
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d("BLE", "disconnected !!!!");
                if (mBinding != null) {
                    mBinding.cmd1.setVisibility(View.INVISIBLE);
                    mBinding.cmd2.setVisibility(View.INVISIBLE);
                    mBinding.cmd3.setVisibility(View.INVISIBLE);
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.d("BLE", "discovered !!!!");

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d("BLE", "read data : " + data);
                if (mBinding != null) {
                    String appendRet = mBinding.txtResult.getText().toString();
                    mBinding.txtResult.setText(appendRet + data);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothHolder = new BluetoothHolder();
        mBluetoothHolder.init(this);
        bindData();
        initView();
    }

    private void bindData(){
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothHolder != null) {
            mBluetoothHolder.registerCallBack();
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothHolder != null) {
            mBluetoothHolder.unregisterCallBack();
        }
    }

    private void showChooseDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("请选择质检产品");
        builder.setTitle("产品选择");
        builder.setPositiveButton("小蜜", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.setCheckType(Xiaomi);
            }
        });
        builder.setNegativeButton("芒果", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.setCheckType(Manguo);
            }
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unSubscribe();
        if (mBluetoothHolder != null) {
            mBluetoothHolder.quit();
        }
    }


    public void initView(){
        mPresenter = new MainPresenter(this);
        mBinding.setPresenter(mPresenter);
        mPresenter.subscribe();
        mBinding.version.setText("version:"+BuildConfig.VERSION_NAME);
        showChooseDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true;
        }
        getMenuInflater().inflate(R.menu.main, menu);
        if (mBluetoothHolder != null && !mBluetoothHolder.isScaning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                if (mBluetoothHolder != null) {
                    mBluetoothHolder.scanDeviceAndConnect(mBinding.editImei.getText().toString(), true);
                }
                break;
            case R.id.menu_stop:
                if (mBluetoothHolder != null) {
                    mBluetoothHolder.scanDeviceAndConnect(mBinding.editImei.getText().toString(), false);
                }
                break;
        }
        return true;
    }

    public void wirteCmd(final String cmd) {
        if (mBluetoothHolder == null) {
            return;
        }
        if (mBinding != null) {
            mBinding.txtResult.setText("");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBluetoothHolder.writeCmd(cmd);
            }
        }).start();
    }

    public void setPresenter(MainContract.Presenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void setIMEI(String IMEI) {
        KeyBoardUtils.hideSoftKeyboard(this);
        mBinding.txtIMEI.setText("IMEI:"+IMEI);
        if (mBluetoothHolder != null) {
            mBluetoothHolder.scanDeviceAndConnect(IMEI, true);
        }
    }

    @Override
    public void setAccText(final String text) {
        mBinding.txtAcc.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtAcc.setText(text);
            }
        });
    }

    @Override
    public void setFenceText(final String text) {
        mBinding.txtFence.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtFence.setText(text);
            }
        });
    }

    @Override
    public void setBackWheel(final String text) {
        mBinding.txtBackWheel.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtBackWheel.setText(text);
            }
        });
    }

    @Override
    public void setBackSeatText(final String text) {
        mBinding.txtBackseat.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtBackseat.setText(text);
            }
        });
    }

    @Override
    public void setVolText(final String text) {
        mBinding.txtVol.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtVol.setText(text);
            }
        });
    }

    @Override
    public void setGSMText(final String text) {
        mBinding.txtGSM.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtGSM.setText(text);
            }
        });
    }

    @Override
    public void setDeviceVersion(final String text) {
        mBinding.txtVersion.post(new Runnable() {
            @Override
            public void run() {
                mBinding.txtVersion.setText(text);
            }
        });
    }

    @Override
    public void showToast(final String text) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void gotoScanner() {
        Intent intent = new Intent(MainActivity.this,ScannerActivity.class);
        startActivity(intent);
    }
}
