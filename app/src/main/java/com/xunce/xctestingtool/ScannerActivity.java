package com.xunce.xctestingtool;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.xunce.xctestingtool.utils.StringDecodeUtil;

import org.greenrobot.eventbus.EventBus;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * Created by yangxu on 2017/8/7.
 */

public class ScannerActivity extends AppCompatActivity  implements QRCodeView.Delegate{
    private ZXingView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mQRCodeView = (ZXingView)findViewById(R.id.zxingview);
        checkPermission();
        mQRCodeView.startCamera();
        mQRCodeView.startSpot();
        mQRCodeView.setResultHandler(this);
    }


    @Override
    public void onScanQRCodeSuccess(String result) {
        String IMEI = StringDecodeUtil.getIMEIFromMango(result);
        if (IMEI == null){
            IMEI = StringDecodeUtil.getIMEIFromSim808(result);
        }
        if(IMEI!=null && !IMEI.isEmpty()){
            EventBus.getDefault().post(new IMEIEvent(IMEI));
            this.finish();
        }else{
            vibrate();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("IMEI号错误");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mQRCodeView.startCamera();
                    mQRCodeView.startSpot();
                }
            }).create().show();
        }
    }
    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e("e", "打开相机出错");
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void checkPermission() {
        int permisson = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (permisson != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }



}
