package com.example.cameraxapp.app;

import android.app.Application;


public class MyApp extends Application {
    public  static MyApp app = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static MyApp getInstance(){
        return app;
    }
}
