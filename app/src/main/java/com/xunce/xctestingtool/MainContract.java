package com.xunce.xctestingtool;

/**
 * Created by yangxu on 2017/8/7.
 */

public interface MainContract {
    interface View {
        void setPresenter(Presenter presenter);
        void gotoScanner();
        void setIMEI(String IMEI);
        void setAccText(String text);
        void setFenceText(String text);
        void setBackSeatText(String text);
        void showToast(String text);
        void setVolText(String text);
        void setGSMText(String text);
    }

    interface Presenter{
        void subscribe();
        void setIMEI(String IMEI);
        void unSubscribe();
        void gotoScanner();
        void playAlarm();
        void openAcc();
        void closeAcc();
        void setFence();
        void closeFence();
        void openBackSeat();
        void getAllInfo();
        void closeDevice();
    }

}
