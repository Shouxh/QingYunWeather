package com.shouxh.weatherMain;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.PropertyResourceBundle;

public class UpdateService extends Service {
    private NetworkReceiver networkReceiver;
    private MainActivity mActivity;

    public UpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        networkReceiver=new NetworkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver,filter);

    }



    @Override
    public void onDestroy() {
        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new UpdateBinder();
    }


    class NetworkReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null&&networkInfo.isAvailable()){
                if(mActivity!=null) {
                    mActivity.refreshCityInfor();
                }
            }

        }
    }
    class UpdateBinder extends Binder {

        public void setMainActivity(MainActivity mainActivity){
            mActivity=mainActivity;
            if (mActivity==null) {
                mActivity=MainActivity.getMain();
            }
        }
    }

}
