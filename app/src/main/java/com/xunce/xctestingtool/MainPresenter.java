package com.xunce.xctestingtool;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangxu on 2017/8/7.
 */

public class MainPresenter  implements MainContract.Presenter{
    private Integer type;
    private MainContract.View mainView;
    private String  IMEI = "865067024210547";
    private String url = "http://product.xiaoantech.com/v1/device";
    private String typeUrl = "https://api.xiaoantech.com/v2/";
    private String verisonUrl = "http://api.xiaoan110.com:8083/v1/imeiData/";
    protected   MainPresenter(MainContract.View mainView){
        this.mainView = mainView;
        mainView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unSubscribe() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setCheckType(Integer type) {
        this.type = type;
    }

    @Override
    public void gotoScanner() {
        mainView.gotoScanner();
    }

    @Override
    public void playAlarm() {
        JSONObject param = new JSONObject();
        try {
            param.put("idx",5);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(14, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if(isSuc(result)){
                    //TODO:
                }
            }
        });
    }

    @Override
    public void openAcc() {
        JSONObject param = new JSONObject();
        try {
            param.put("acc",1);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(33, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setAccText("电门状态：开");
                }
            }
        });
    }

    @Override
    public void closeAcc() {
        JSONObject param = new JSONObject();
        try {
            param.put("acc",0);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(33, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setAccText("电门状态：关");
                }
            }
        });
    }

    @Override
    public void setFence() {
        JSONObject param = new JSONObject();
        try {
            param.put("defend",1);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(4, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setFenceText("防御状态：开");
                }
            }
        });
    }

    @Override
    public void wirteCmd(String cmd) {
        if (mainView != null) {
            ((MainActivity) mainView).wirteCmd(cmd);
        }
    }

    @Override
    public void closeFence() {
        JSONObject param = new JSONObject();
        try {
            param.put("defend",0);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(4, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setFenceText("防御状态：关");
                }
            }
        });
    }

    @Override
    public void openBackWheel() {
        JSONObject param = new JSONObject();
        try {
            param.put("sw",0);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(28, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setBackWheel("后轮锁：开");
                }
            }
        });
    }

    @Override
    public void closeBackWheel() {
        JSONObject param = new JSONObject();
        try {
            param.put("sw",1);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmdAndParam(28, param, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setBackWheel("后轮锁：关");
                }
            }
        });
    }

    @Override
    public void openBackSeat() {
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmd(29,this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.setBackSeatText("后座锁：开");
                }
            }
        });
    }


    private void getDeviceVersion(){
        HttpRequest.httpGetWithUrl(verisonUrl+IMEI, null, new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.has("version")){
                        mainView.setDeviceVersion("deviceVersion:"+Integer.toHexString(jsonObject.getInt("version")));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void getAllInfo() {
        getDeviceVersion();
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmd(34,this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if(isSuc(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject resultObject =jsonObject.getJSONObject("result");
                        if(resultObject.has("defend")){
                            if(resultObject.getInt("defend")==1){
                                mainView.setFenceText("防御状态：开");
                            }else {
                                mainView.setFenceText("防御状态：关");
                            }
                        }
                        if (resultObject.has("wheelLock")){
                            if (resultObject.getInt("wheelLock")==1){
                                mainView.setBackWheel("后轮锁：开");
                            }else {
                                mainView.setBackWheel("后轮锁：关");
                            }
                        }
                        if(resultObject.has("acc")){
                            if (resultObject.getInt("acc")==0){
                                mainView.setAccText("电门状态：开");
                            }else {
                                mainView.setAccText("电门状态：关");
                            }
                        }
                        if(resultObject.has("seatLock")){
                            if (resultObject.getInt("seatLock")==1){
                                mainView.setBackSeatText("后座锁：开");
                            }else {
                                mainView.setBackSeatText("后座锁：关");
                            }
                        }
                        if (resultObject.has("voltage")){
                            mainView.setVolText("电压："+resultObject.getInt("voltage")+"v");
                        }
                        if(resultObject.has("gsm")){
                            mainView.setGSMText("GSM："+resultObject.getInt("gsm"));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void closeDevice() {
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmd(25,this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.showToast("关闭成功");
                }
            }
        });
    }

    @Override
    public void setIMEI(String IMEI) {
        if(IMEI.length()!=15){
            return;
        }
        this.IMEI = IMEI;
        mainView.setIMEI(this.IMEI);
        checkType();
        getAllInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIMEIEvent(IMEIEvent event){
        this.IMEI = event.getIMEI();
        mainView.setIMEI(this.IMEI);
        checkType();
        getAllInfo();
    }

    @Override
    public void checkType() {
        JSONObject object = new JSONObject();
        try{
            object.put("imei",IMEI);
        }catch (JSONException e){
            e.printStackTrace();
        }

        HttpRequest.postHttpCmdResult(typeUrl+"qcVerifyImei", object.toString(), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                Log.d("test",result);
                try{
                    JSONObject object = new JSONObject(result);
                    if(object.getInt("code") == 0){
                        Integer type = object.getInt("type");
                         if(type == 1) {
                            mainView.showToast("此设备为芒果");
                        }else  if(type == 2){
                            mainView.showToast("此设备为小蜜");
                        }
                    }else if(object.getInt("code") == 1){
                        mainView.showToast("此设备为新设备");

                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void update() {
        HttpRequest.postHttpCmdResult(url, HttpRequest.getStringWithCmd(35, this.IMEI), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                if (isSuc(result)){
                    mainView.showToast("升级成功");
                }
            }
        });
    }

    @Override
    public void checkSuccess() {
        JSONObject object = new JSONObject();
        try{
            object.put("imei",IMEI);
            object.put("type",type);
        }catch (JSONException e){
            e.printStackTrace();
        }
        HttpRequest.postHttpCmdResult(typeUrl+"qcAddImei", object.toString(), new HttpCallback() {
            @Override
            public void httpCallBack(String result) {
                isSuc(result);
            }
        });
    }

    private boolean isSuc(String result){
        try {
            JSONObject jsonObject = new JSONObject(result);
            if(jsonObject.has("code")&&jsonObject.getInt("code")==0){
                mainView.showToast("设置成功");
                return true;
            }else {
                mainView.showToast("查询失败");
                return false;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }
}
