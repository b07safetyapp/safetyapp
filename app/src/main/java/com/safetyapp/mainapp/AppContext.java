package com.safetyapp.mainapp;

import android.app.Application;
import android.content.Context;

public class AppContext extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
