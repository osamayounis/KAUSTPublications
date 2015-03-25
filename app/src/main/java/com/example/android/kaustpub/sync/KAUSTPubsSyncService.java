package com.example.android.kaustpub.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.android.kaustpub.MyApp;

public class KAUSTPubsSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static KAUSTPubsSyncAdapter sKAUSTPubsSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sKAUSTPubsSyncAdapter == null) {
                sKAUSTPubsSyncAdapter = new KAUSTPubsSyncAdapter(MyApp.getAppContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sKAUSTPubsSyncAdapter.getSyncAdapterBinder();
    }
}