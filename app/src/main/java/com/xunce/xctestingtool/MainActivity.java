package com.xunce.xctestingtool;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xunce.xctestingtool.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity implements  MainContract.View{

    private ActivityMainBinding mBinding;
    private MainContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindData();
        initView();
    }

    private void bindData(){
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unSubscribe();
    }


    public void initView(){
        mPresenter = new MainPresenter(this);
        mBinding.setPresenter(mPresenter);
        mPresenter.subscribe();
    }

    public void setPresenter(MainContract.Presenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void setIMEI(String IMEI) {
        mBinding.txtIMEI.setText("IMEI:"+IMEI);
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
