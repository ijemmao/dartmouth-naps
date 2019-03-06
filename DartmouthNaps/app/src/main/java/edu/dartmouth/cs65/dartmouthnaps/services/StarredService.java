package edu.dartmouth.cs65.dartmouthnaps.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StarredService extends Service {
    public StarredService() {}

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {

    }
}
