package com.example.cdha.myapplication;

import android.app.Application;
import android.content.Context;

import com.inuker.bluetooth.library.BluetoothContext;

public class MyApplication extends Application {
    private static MyApplication instance;
    private Context context;
    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BluetoothContext.set(this);

    }
}
