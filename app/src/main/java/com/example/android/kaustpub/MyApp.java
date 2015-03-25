package com.example.android.kaustpub;

import android.app.Application;
import android.content.Context;

/**
 * Created by Osama on 3/23/2015.
 */
public class MyApp extends Application{
    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApp.context;
    }
}
