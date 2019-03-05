package edu.dartmouth.cs65.dartmouthnaps.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class LocationService extends Service {
    private static final String TAG = TAG_GLOBAL + ": LocationService";
    private static final boolean DEBUG = true;

    private static final int UNIT_TO_MILLI = 1000;
    private static final int MIN_TO_SEC = 60;
    private static final int THRESHOLD = 1;
    private static final int THRESHOLD_IN_MILLIS = THRESHOLD * MIN_TO_SEC * UNIT_TO_MILLI;

    private static Thread sLocationThread;
    private static boolean sNotificationRunning = false;
    private static boolean sReviewPrompted = false;

    public static boolean sIsBound = false;
    public static boolean sIsRunning = false;

    private NotificationManager mNotificationManager;   // NotificationManager to call to cancel the
                                                        // notification when appropriate
    private FusedLocationProviderClient mFLCP;
    private LSLocationCallback mLSLC;
    private Messenger mRecvMessenger;
    private Messenger mSendMessenger;
    private LatLng mCurrentLocation;
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
        sLocationThread = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel notificationChannel;

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onStartCommand() called");

        sIsRunning = true;

        if (!sNotificationRunning) {
            mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            // Upon creation, we want to start the Notification with the PendingIntent to bring the app
            // to the forefront again
            notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_IDS[0],
                    NOTIFICATION_CHANNEL_NAMES[0],
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);
            notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_IDS[1],
                    NOTIFICATION_CHANNEL_NAMES[1],
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(notificationChannel);
            postNotification(NOTIFICATION_LOCATION_MONITOR);
            sNotificationRunning = true;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onBind() called");
        sIsBound = true;

        if (sLocationThread != null) {
            sLocationThread.interrupt();
            sLocationThread = null;
        }

        return mRecvMessenger.getBinder();
    }

    @Override
    public void onRebind (Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onRebind() called");
        sIsBound = true;

        if (sLocationThread != null) {
            sLocationThread.interrupt();
            sLocationThread = null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onUnbind() called");
        mSendMessenger = null;
        sIsBound = false;
        checkLocationPeriodically();

        return true;
    }

    @Override
    public void onTaskRemoved (Intent rootIntent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onTaskRemoved() called");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onDestroy() called");
        super.onDestroy();
        // Upon being destroyed, get rid of all notifications and set sIsRunning to false
        mNotificationManager.cancelAll();
        mFLCP.removeLocationUpdates(mLSLC);

        if (sLocationThread != null) {
            sLocationThread.interrupt();
            sLocationThread = null;
        }

        sIsRunning = false;
    }

    private void postNotification(int id) {
        Intent intent;
        String title;
        String text;
        Notification notification;
        int flags;

        if (DEBUG_GLOBAL && DEBUG && mNotificationManager == null) {
            Log.d(TAG, "Warning: posting notification " +
                    " while mNotificationManager is null");
        }

        intent = new Intent(this, MainActivity.class);
        title = NOTIFICATION_TITLES[id - 1];
        text = NOTIFICATION_TEXTS[id - 1];

        if (id == NOTIFICATION_REVIEW_PROMPT) {
            intent.putExtra(KEY_REVIEW_PROMPTED, true);
            intent.putExtra(KEY_LATITUDE, mCurrentLocation.latitude);
            intent.putExtra(KEY_LATITUDE, mCurrentLocation.longitude);
            title = title.substring(0, 44) + PLACE_NAMES[mPrevPlaceIndex] + title.substring(44);
        }

        notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_IDS[id - 1])
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(PendingIntent.getActivity(
                        this,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        switch (id) {
            case NOTIFICATION_LOCATION_MONITOR:
                notification.flags |=
                        Notification.FLAG_NO_CLEAR |
                        Notification.FLAG_ONGOING_EVENT;
                startForeground(id, notification);
                break;
            case NOTIFICATION_REVIEW_PROMPT:
                notification.flags |=
                        Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(id, notification);
                break;
        }
    }

    private void requestLocationUpdates(boolean fast) {
        LocationRequest locationRequest;

        if (mFLCP != null) {
            mFLCP.removeLocationUpdates(mLSLC);
        }

        mFLCP = new FusedLocationProviderClient(getApplicationContext());
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(fast ? 5 * UNIT_TO_MILLI : UNIT_TO_MILLI)
                .setInterval(fast ? 10 * UNIT_TO_MILLI : 2 * UNIT_TO_MILLI)
                .setSmallestDisplacement(fast ? 5 : 0);

        try {
            mFLCP.requestLocationUpdates(
                    locationRequest,
                    mLSLC,
                    getMainLooper());
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

    private void checkLocationPeriodically() {
        sLocationThread = performOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mFLCP.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // GPS location can be null if GPS is switched off
                                    if (location != null) {
                                        mCurrentLocation = new LatLng(location);
                                        long currTime;
                                        int currPlaceIndex;

                                        currPlaceIndex = PlaceUtil.getPlaceIndex(mCurrentLocation.toDoubleArr());
                                        currTime = Calendar.getInstance().getTimeInMillis();

                                        if (DEBUG_GLOBAL && DEBUG) {
                                            String additional = currPlaceIndex == -1 ?
                                                    " is outside all places" :
                                                    " is inside " + PLACE_NAMES[currPlaceIndex];
                                            Log.d(TAG, "checkLocationPeriodically(): (" +
                                                    mCurrentLocation.latitude + ", " +
                                                    mCurrentLocation.longitude + ")" + additional);
                                        }

                                        if (mPrevPlaceIndex != currPlaceIndex) {
                                            mPrevPlaceIndex = currPlaceIndex;
                                            mPrevTime = currTime;
                                            sReviewPrompted = false;
                                        } else if (mPrevPlaceIndex != -1 &&
                                                currTime - mPrevTime >= THRESHOLD_IN_MILLIS &&
                                                !sReviewPrompted) {
                                            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "stayed in " +
                                                    PLACE_NAMES[mPrevPlaceIndex] + " for " + THRESHOLD + " min");

                                            postNotification(NOTIFICATION_REVIEW_PROMPT);
                                            mPrevTime = currTime;
                                            sReviewPrompted = true;
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "getLastLocation().addOnFailureListener() called");
                                    e.printStackTrace();
                                }
                            });
                } catch (SecurityException e) {
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "Error trying to get last GPS location");
                    e.printStackTrace();
                }
            }
        });
    }

    private class LSHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SEND_MESSENGER:
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "MSG_WHAT_SEND_MESSENGER received");
                    mSendMessenger = msg.replyTo;
                    requestLocationUpdates(true);
                    break;
            }
        }
    }

    private class LSLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult result) {
            long currTime;
            int currPlaceIndex;

            mCurrentLocation = new LatLng(result.getLastLocation());
            currPlaceIndex = getPlaceIndex(mCurrentLocation.toDoubleArr());

            if (DEBUG_GLOBAL && DEBUG) {
                String additional = currPlaceIndex == -1 ?
                        " is outside all places" :
                        " is inside " + PLACE_NAMES[currPlaceIndex];
                Log.d(TAG, "onLocationResult(): (" +
                        mCurrentLocation.latitude + ", " +
                        mCurrentLocation.longitude + ")" + additional);
            }

            Log.d(TAG, "onLocationResult(): sIsBound == " + sIsBound);

            if (sIsBound) {
                sendMessage(MSG_WHAT_SEND_LOCATION, mCurrentLocation);
            } else {
                currTime = Calendar.getInstance().getTimeInMillis();

                if (mPrevPlaceIndex != currPlaceIndex) {
                    mPrevPlaceIndex = currPlaceIndex;
                    mPrevTime = currTime;
                } else if (mPrevPlaceIndex != -1 &&
                        currTime - mPrevTime >= THRESHOLD_IN_MILLIS) {
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "stayed in " +
                            PLACE_NAMES[mPrevPlaceIndex] + " for " + THRESHOLD + " min");

                    mPrevTime = currTime;
                }
            }
        }
    }

    private static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            volatile boolean pleaseStopNow = false;

            @Override
            public void run() {
                while (!pleaseStopNow) {
                    runnable.run();

                    try {
                        Thread.sleep(5 * UNIT_TO_MILLI);
                    } catch (InterruptedException e) {
                        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "Error trying to sleep");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void interrupt() {
                pleaseStopNow = true;
            }
        };
        t.start();
        return t;
    }
}
