package edu.dartmouth.cs65.dartmouthnaps.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.util.Calendar;

import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class LocationService extends Service {
    private static final String TAG = TAG_GLOBAL + ": LocationService";
    private static final boolean DEBUG = true;

    private static final int UNIT_TO_MILLI = 1000;
    private static final int MIN_TO_SEC = 60;
    private static final int THRESHOLD = 20;
    private static final int THRESHOLD_IN_MILLIS = THRESHOLD * MIN_TO_SEC * UNIT_TO_MILLI;

    private static boolean sNotificationRunning = false;

    public static boolean sIsBound = false;
    public static boolean sIsRunning = false;

    private NotificationManager mNotificationManager;   // NotificationManager to call to cancel the
                                                        // notification when appropriate
    private FusedLocationProviderClient mFLCP;
    private LSLocationCallback mLSLC;
    private Looper mLooper;
    private Messenger mRecvMessenger;
    private Messenger mSendMessenger;
    private int mPrevPlaceIndex;
    private long mPrevTime;

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onCreate() called");

        mFLCP = null;
        mLSLC = new LSLocationCallback();
        mRecvMessenger = new Messenger(new LSHandler());
        mSendMessenger = null;
        mPrevPlaceIndex = -1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onStartCommand() called");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onBind() called");
        sIsBound = true;

        return mRecvMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onUnbind() called");
        mSendMessenger = null;
        sIsBound = false;
        requestLocationUpdates(null, false);

        return false;
    }

    @Override
    public void onDestroy() {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onDestroy() called");
    }

    private void requestLocationUpdates(Context context, boolean fast) {
        LocationRequest locationRequest;

        if (mFLCP != null) {
            mFLCP.removeLocationUpdates(mLSLC);
        }

        if (context != null) {
            mFLCP = new FusedLocationProviderClient(context);
        }

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(fast ? 5 * UNIT_TO_MILLI : MIN_TO_SEC * UNIT_TO_MILLI)
                .setInterval(fast ? 10 * UNIT_TO_MILLI : 2 * MIN_TO_SEC * UNIT_TO_MILLI)
                .setSmallestDisplacement(5);

        try {
            mFLCP.requestLocationUpdates(
                    locationRequest,
                    mLSLC,
                    mLooper);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException caught in requestLocationUpdates");
            e.printStackTrace();
        }
    }

    private void sendMessage(int what, Object obj) {
        if (mSendMessenger != null) {
            try {
                Message msg = Message.obtain();
                msg.what = what;
                msg.obj = obj;
                mSendMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class LSHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SEND_MESSENGER:
                    mSendMessenger = msg.replyTo;
                    break;
                case MSG_WHAT_SEND_LOOPER:
                    mLooper = (Looper)msg.obj;
                    break;
                case MSG_WHAT_SEND_CONTEXT:
                    requestLocationUpdates((Context)msg.obj, true);
                    break;
            }
        }
    }

    private class LSLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult result) {
            LatLng location;
            long currTime;
            int currPlaceIndex;

            location = new LatLng(result.getLastLocation());
            currPlaceIndex = PlaceUtil.getPlaceIndex(location.toDoubleArr());

            if (DEBUG_GLOBAL && DEBUG) {
                String additional = currPlaceIndex == -1 ?
                        " is outside all places" :
                        " is inside " + PlaceUtil.PLACE_NAMES[currPlaceIndex];
                Log.d(TAG, "(" +
                        location.latitude + ", " +
                        location.longitude + ")" + additional);
            }

            if (sIsBound) {
                sendMessage(MSG_WHAT_SEND_LOCATION, location);
            } else {
                currTime = Calendar.getInstance().getTimeInMillis();

                if (mPrevPlaceIndex != currPlaceIndex) {
                    mPrevPlaceIndex = currPlaceIndex;
                    mPrevTime = currTime;
                } else if (mPrevPlaceIndex != -1 &&
                        currTime - mPrevTime >= THRESHOLD_IN_MILLIS) {
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "stayed in " +
                            PlaceUtil.PLACE_NAMES[mPrevPlaceIndex] + " for " + THRESHOLD + " min");

                    mPrevTime = currTime;
                }
            }
        }
    }
}
