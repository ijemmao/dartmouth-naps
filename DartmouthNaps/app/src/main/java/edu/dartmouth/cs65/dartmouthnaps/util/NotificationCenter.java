package edu.dartmouth.cs65.dartmouthnaps.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;

import static android.content.Context.NOTIFICATION_SERVICE;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class NotificationCenter {
    private static final String TAG = TAG_GLOBAL + ": NotificationCenter";
    private static final boolean DEBUG = true;

    private NotificationManager mNotificationManager;
    private Service mService;
    private LatLng mLocation;
    private String mReviewKey;
    private NCCallback mNCCallback;
    private int mPlaceIndex;
    private int mID;

    public NotificationCenter(Context context) {
        NotificationChannel notificationChannel;

        if (context instanceof NCCallback) mNCCallback = (NCCallback)context;

        // Initialize the NotificationManager
        mNotificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);

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

        // Create the NotificationChannel for the starred review notification
        notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_IDS[NOTIFICATION_ID_STARRED_REVIEW - 1],
                NOTIFICATION_CHANNEL_NAMES[NOTIFICATION_ID_STARRED_REVIEW - 1],
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(notificationChannel);
    }

    /***************** postNotification() ****************
     * Posts a Notification with the ID of the current value of mID (set previously before calling
     * sendContext())
     */
    private void postNotification(Context context) {
        Intent          intent;         // Intent for the Notification
        String          title;          // String for the title of the Notification
        String          text;           // String for the text of the Notification
        Notification    notification;   // Notification to post

        // The Notification should take the user to the MainActivity, and title and text are
        // probably just values from their respective array (the Notification mID 0 can't be used,
        // so they start at 1)
        intent = new Intent(context, MainActivity.class);
        title = NOTIFICATION_TITLES[mID - 1];
        text = NOTIFICATION_TEXTS[mID - 1];


        // If the intended Notification is a review prompt, add the location data and set the
        // KEY_REVIEW_PROMPT boolean true to launch a NewReviewActivity when it's clicked
        if (mID == NOTIFICATION_ID_REVIEW_PROMPT) {
            intent.putExtra(KEY_REVIEW_PROMPT, true);
            intent.putExtra(KEY_LATITUDE, mLocation.latitude);
            intent.putExtra(KEY_LATITUDE, mLocation.longitude);

            // The title should also include the name of the place the user has been in
            title = title.substring(0, 44) + PLACE_NAMES[mPlaceIndex] + title.substring(44);
        }

        switch (mID) {
            case NOTIFICATION_ID_REVIEW_PROMPT:
                // If the intended Notification is a review prompt, add the location data and set
                // KEY_REVIEW_PROMPT boolean true to launch a NewReviewActivity when it's clicked
                intent.putExtra(KEY_REVIEW_PROMPT, true);
                intent.putExtra(KEY_LATITUDE, mLocation.latitude);
                intent.putExtra(KEY_LATITUDE, mLocation.longitude);

                // The title should also include the name of the place the user has been in
                title = title.substring(0, 44) + PLACE_NAMES[mPlaceIndex] + title.substring(44);
                break;
            case NOTIFICATION_ID_STARRED_REVIEW:
                // If the intended Notification is for a starred review, add the string key that
                // will be used by the FirebaseDataSource to store it and set KEY_STARRED_REVIEW to
                // true to eventually open up that specific ReviewActivity
                intent.putExtra(KEY_STARRED_REVIEW, true);
                intent.putExtra(KEY_REVIEW_KEY, mReviewKey);

                // The title should also include the name of the place the review was posted in
                title = title.substring(0, 23) + PLACE_NAMES[mPlaceIndex] + title.substring(23);
                break;
        }

        // Build the Notification
        notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_IDS[mID - 1])
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        mID,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        // Adjust the Notification's flags as necessary and then post it (either through
        // startForeground() or notify()
        switch (mID) {
            case NOTIFICATION_ID_LOCATION_MONITOR:
                notification.flags |=
                        Notification.FLAG_NO_CLEAR |
                                Notification.FLAG_ONGOING_EVENT;
                mService.startForeground(mID, notification);
                break;
            case NOTIFICATION_ID_REVIEW_PROMPT:
                notification.flags |=
                        Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(mID, notification);
                break;
            case NOTIFICATION_ID_STARRED_REVIEW:
                notification.flags |=
                        Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(mID, notification);
                break;
        }
    }

    /**************** postLocationMonitorNotification() ****************
     * Posts a location monitor Notification through the given Service
     * @param service   Service to post the Notification with (to keep the app from being killed by
     *                  Android, this Notification is posted using startForeground() called from
     *                  service)
     */
    public void postLocationMonitorNotification(Service service) {
        mService = service;
        mID = NOTIFICATION_ID_LOCATION_MONITOR;
        mNCCallback.sendContext();
    }

    /**************** postReviewPromptNotification() ****************
     * Posts a review prompt Notification that will take the user to a NewReviewActivity for the
     * given location, which has been evaluated to be at the given placeIndex
     * @param location      LatLng for location to be associated with the new Review
     * @param placeIndex    int for the place index of the new Review (it is assumed that this index
     *                      was generated through calling PlaceUtil.getPlaceIndex(location))
     */
    public void postReviewPromptNotification(LatLng location, int placeIndex) {
        mLocation = location;
        mPlaceIndex = placeIndex;
        mID = NOTIFICATION_ID_REVIEW_PROMPT;
        mNCCallback.sendContext();
    }

    /**************** postStarredReviewNotification() ****************
     * Posts a Notification for a review being made on a starred location
     * @param reviewKey     String for the key (the timestamp) used to access the Review from the
     *                      Map in FirebaseDataSource
     * @param placeIndex    int for the index of the place of the review
     */
    public void postStarredReviewNotification(String reviewKey, int placeIndex) {
        mReviewKey = reviewKey;
        mPlaceIndex = placeIndex;
        mID = NOTIFICATION_ID_STARRED_REVIEW;
        mNCCallback.sendContext();
    }

    /**************** cancelAll() ****************
     * Calls cancelAll() on the private NotificationManager
     */
    public void cancelAll() {
        mNotificationManager.cancelAll();
    }

    /**************** receiveContext() ****************
     * Receives the Context requested by calling sendContext(), and immediately posts the awaiting
     * Notification with this Context
     * @param context   Context to receive
     */
    public void receiveContext(Context context) {
        postNotification(context);
    }

    /**************** NCCallback ****************
     * Callback interface for NotificationCenter
     */
    public interface NCCallback {
        /**************** sendContext() ****************
         * A request for a Context to post a Notification
         */
        void sendContext();
    }
}
