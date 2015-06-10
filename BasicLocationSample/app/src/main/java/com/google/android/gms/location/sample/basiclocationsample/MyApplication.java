package com.google.android.gms.location.sample.basiclocationsample;

import android.app.Application;
import android.content.Context;

/**
 * Created by weibin on 2015/6/8.
 */
public class MyApplication extends Application{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
