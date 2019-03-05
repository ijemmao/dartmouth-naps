package edu.dartmouth.cs65.dartmouthnaps.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

    private static final int UNIT_TO_MILLI = 1000;  // Conversion factor to go from unit to milli
    private static final int MIN_TO_SEC = 60;       // Conversion factor to go from min to s
    private static final int THRESHOLD = 1;         // Threshold for the review prompt (in min)
    private static final int THRESHOLD_IN_MILLIS =  // Threshold for the review prompt (in ms)
            THRESHOLD * MIN_TO_SEC * UNIT_TO_MILLI;
    private static final int PERIOD = 5;            // Period to check the location (in s)
    private static final int PERIOD_IN_MILLIS =     // Period to check the location (in ms)
            PERIOD * UNIT_TO_MILLI;

    private static Thread sCheckLocationPeriodicallyThread; // Thread for checking notifications
    private static boolean sLocationMonitorPosted = false;  // boolean for whether the location
                                                            // monitor notification is posted
    private static boolean sReviewPrompted = false;         // boolean for whether a review has been
                                                            // prompted

    public static boolean sIsBound = false;             // boolean for whether this Service is bound
    public static boolean sIsRunning = false;           // boolean for whether this Service is
                                                        // running

    private NotificationManager mNotificationManager;   // NotificationManager to call to cancel the
                                                        // notification when appropriate
    private FusedLocationProviderClient mFLCP;          // FusedLocationProviderClient to request
                                                        // location updates from
    private LSLocationCallback mLSLC;                   // LSLocationCallback to be called upon a
                                                        // location update
    private Messenger mRecvMessenger;   // Messenger for receiving messages
    private Messenger mSendMessenger;   // Messenger for sending messages
    private LatLng mCurrentLocation;    // LatLng for the current location
    private int mPrevPlaceIndex;        // int for the index of the most recent place
    private long mPrevTime;             // long for the time of the most recent location check

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onCreate() called");

        mFLCP = null;
        mLSLC = new LSLocationCallback();
        mRecvMessenger = new Messenger(new LSHandler());
        mSendMessenger = null;
        mPrevPlaceIndex = -1;
        sCheckLocationPeriodicallyThread = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel notificationChannel;

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onStartCommand() called");

        sIsRunning = true;

        if (!sLocationMonitorPosted) {
            // Initialize the NotificationManager
            mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            // Create the NotificationChannel for the location monitor notification
            notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_IDS[NOTIFICATION_ID_LOCATION_MONITOR - 1],
                    NOTIFICATION_CHANNEL_NAMES[NOTIFICATION_ID_LOCATION_MONITOR - 1],
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);

            // Create the NotificationChannel for the review prompt notification
            notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_IDS[NOTIFICATION_ID_REVIEW_PROMPT - 1],
                    NOTIFICATION_CHANNEL_NAMES[NOTIFICATION_ID_REVIEW_PROMPT - 1],
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(notificationChannel);

            // Post the location monitor notification
            postNotification(NOTIFICATION_ID_LOCATION_MONITOR);
            sLocationMonitorPosted = true;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onBind() called");

        sIsBound = true;

        // If the thread to check the location periodically isn't null, interrupt it and set it null
        if (sCheckLocationPeriodicallyThread != null) {
            sCheckLocationPeriodicallyThread.interrupt();
            sCheckLocationPeriodicallyThread = null;
        }

        return mRecvMessenger.getBinder();
    }

    @Override
    public void onRebind (Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onRebind() called");

        sIsBound = true;

        // If the thread to check the location periodically isn't null, interrupt it and set it null
        if (sCheckLocationPeriodicallyThread != null) {
            sCheckLocationPeriodicallyThread.interrupt();
            sCheckLocationPeriodicallyThread = null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onUnbind() called");

        mSendMessenger = null;          // mSendMessenger is no longer valid
        sIsBound = false;               // This Service is no longer bound
        checkLocationPeriodically();    // Start checking the location periodically

        return true;
    }

    @Override
    public void onTaskRemoved (Intent rootIntent) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onTaskRemoved() called");

        //Stop this Service when the Task is removed (when the user clears the app from recent apps)
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onDestroy() called");

        super.onDestroy();

        // Upon being destroyed,
        //  * get rid of all notifications
        //  * stop listening for location updates with the LocationServiceLocationCallback
        //  * interrupt (which stops) the Thread to check the location periodically
        //  * set sIsRunning to false
        mNotificationManager.cancelAll();
        mFLCP.removeLocationUpdates(mLSLC);
        mFLCP = null;

        if (sCheckLocationPeriodicallyThread != null) {
            sCheckLocationPeriodicallyThread.interrupt();
            sCheckLocationPeriodicallyThread = null;
        }

        sIsRunning = false;
    }

    /**************** postNotification() ****************
     * Posts a Notification with the given id (which also defines its type)
     * @param id    int for the id of the Notification to post (needs to be one of the values
     *              "NOTIFICATION_ID_" in Globals
     */
    private void postNotification(int id) {
        Intent          intent;         // Intent for the Notification
        String          title;          // String for the title of the Notification
        String          text;           // String for the text of the Notification
        Notification    notification;   // Notification to post

        if (DEBUG_GLOBAL && DEBUG && mNotificationManager == null) {
            Log.d(TAG, "Warning: posting notification " +
                    " while mNotificationManager is null");
        }

        // The Notification should take the user to the MainActivity, and title and text are
        // probably just values from their respective array (the Notification id 0 can't be used,
        // so they start at 1)
        intent = new Intent(this, MainActivity.class);
        title = NOTIFICATION_TITLES[id - 1];
        text = NOTIFICATION_TEXTS[id - 1];

        // If the intended Notification is a review prompt, add the location data and set the
        // KEY_REVIEW_PROMPTED boolean true to launch a NewReviewActivity when it's clicked
        if (id == NOTIFICATION_ID_REVIEW_PROMPT) {
            intent.putExtra(KEY_REVIEW_PROMPTED, true);
            intent.putExtra(KEY_LATITUDE, mCurrentLocation.latitude);
            intent.putExtra(KEY_LATITUDE, mCurrentLocation.longitude);

            // The title should also include the name of the place the user has been in
            title = title.substring(0, 44) + PLACE_NAMES[mPrevPlaceIndex] + title.substring(44);
        }

        // Build the Notification
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

        // Adjust the Notification's flags as necessary and then post it (either through
        // startForeground() or notify()
        switch (id) {
            case NOTIFICATION_ID_LOCATION_MONITOR:
                notification.flags |=
                        Notification.FLAG_NO_CLEAR |
                        Notification.FLAG_ONGOING_EVENT;
                startForeground(id, notification);
                break;
            case NOTIFICATION_ID_REVIEW_PROMPT:
                notification.flags |=
                        Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(id, notification);
                break;
        }
    }

    /**************** requestLocationUpdates() ****************
     * Requests location updates through the FusedLocationProviderClient
     */
    private void requestLocationUpdates() {
        LocationRequest locationRequest;

        // If mFLCP isn't null, its currently receiving location updates
        if (mFLCP != null) {
            return;
        }

        // Initialize mFLCP and the LocationRequest
        mFLCP = new FusedLocationProviderClient(getApplicationContext());
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(5 * UNIT_TO_MILLI)
                .setInterval(10 * UNIT_TO_MILLI)
                .setSmallestDisplacement(5);

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

    /**************** sendMessage() ****************
     * Sends a Message with the given what and Object
     * @param what  int for the what variable of the Message (should be one of the values
     *              "MSG_WHAT_" in Globals
     * @param obj   Object for the obj of the Message
     */
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

    /**************** checkLocationsPeriodically() ****************
     * Starts checking the Location at constant intervals of PERIOD
     */
    private void checkLocationPeriodically() {
        sCheckLocationPeriodicallyThread = new CheckLocationPeriodicallyThread(
                new CheckLocationPeriodicallyRunnable());
        sCheckLocationPeriodicallyThread.start();
    }

    /**************** LSHandler ****************
     * Handles message for the LocationService
     */
    private class LSHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SEND_MESSENGER:
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "MSG_WHAT_SEND_MESSENGER received");
                    mSendMessenger = msg.replyTo;
                    requestLocationUpdates();
                    break;
            }
        }
    }

    /**************** LSLocationCallback ****************
     * A LocationCallback for the LocationService
     */
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

            // If this Service is bound, send the location to the user
            if (sIsBound) {
                sendMessage(MSG_WHAT_SEND_LOCATION, mCurrentLocation);
            }
        }
    }

    /**************** CheckLocationPeriodicallyRunnable ****************
     * Runnable for checking the Location at constants intervals of PERIOD
     */
    private class CheckLocationPeriodicallyRunnable implements Runnable {
        private static final String TAG = TAG_GLOBAL + ": CheckLocationPeriodicallyRunnable";
        private static final boolean DEBUG = true;

        @Override
        public void run() {
            try {
                mFLCP.getLastLocation()
                        .addOnSuccessListener(new CheckLocationPeriodicallyOnSuccessListener());
            } catch (SecurityException e) {
                if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "Error trying to get last GPS location");
                e.printStackTrace();
            }
        }
    }

    /**************** CheckLocationPeriodicallyOnSuccessListener ****************
     * OnSuccessListener for CheckLocationPeriodicallyRunnable
     */
    private class CheckLocationPeriodicallyOnSuccessListener implements OnSuccessListener<Location> {
        private static final String TAG = TAG_GLOBAL + ": CheckLocationPeriodicallyOnSuccessListener";
        private static final boolean DEBUG = true;

        @Override
        public void onSuccess(android.location.Location location) {
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
                    // If the place index has changed, reset the time and index of the previous
                    // datum, and reset sReviewPrompted so that another Review can be shown for this
                    // new spot
                    mPrevPlaceIndex = currPlaceIndex;
                    mPrevTime = currTime;
                    sReviewPrompted = false;
                } else if (mPrevPlaceIndex != -1 &&
                        currTime - mPrevTime >= THRESHOLD_IN_MILLIS &&
                        !sReviewPrompted) {
                    // Otherwise if the place index isn't -1 (outside of all places) and the time
                    // difference has exceeded the threshold and a review hasn't been prompted,
                    // push a review prompt Notification
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "stayed in " +
                            PLACE_NAMES[mPrevPlaceIndex] + " for " + THRESHOLD + " min");

                    postNotification(NOTIFICATION_ID_REVIEW_PROMPT);
                    mPrevTime = currTime;
                    sReviewPrompted = true;
                }
            }
        }
    }

    /**************** CheckLocationPeriodicallyThread ****************
     * Thread for checking the Location at constants intervals of PERIOD
     */
    private static class CheckLocationPeriodicallyThread extends Thread {
        private static final String TAG = TAG_GLOBAL + ": CheckLocationPeriodicallyThread";
        private static final boolean DEBUG = true;

        private Runnable mRunnable;
        private boolean mInterrupted = false;

        CheckLocationPeriodicallyThread(final Runnable runnable) {
            mRunnable = runnable;
        }

        @Override
        public void run() {
            while (!mInterrupted) {
                // Alternate between checking the Location and sleeping for PERIOD
                mRunnable.run();

                try {
                    Thread.sleep(PERIOD_IN_MILLIS);
                } catch (InterruptedException e) {
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "Error trying to sleep");
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void interrupt() {
            mInterrupted = true;
        }
    }
}
