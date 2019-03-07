package edu.dartmouth.cs65.dartmouthnaps.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.MyReviewsFragment;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;
import edu.dartmouth.cs65.dartmouthnaps.services.LocationService;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class FirebaseDataSource {
    private static final String TAG = TAG_GLOBAL + ": FirebaseDataSource";
    private static final boolean DEBUG = true;

    public static boolean sReviewChildEventListenerAdded = false;

    private DatabaseReference mReviewsFDBR;     // DatabaseReference to the reviews node
    private DatabaseReference mUserReviewsFDBR; // DatabaseReference to the reviews node under user
    private GoogleMap mGoogleMap;               // GoogleMap to add Markers to
    private Map<String, Review> mReviews;       // Map of Strings to Reviews to keep track of
                                                // Reviews by their timestamp
    private Map<String, Marker> mReviewMarkers; // Map of Strings to Markers to keep track of
                                                // Markers by the timestamp of their Review
    private boolean[] mStarred;                 // boolean array for whether the user has starred
                                                // the place at that index
    private Bitmap mReviewMarkerBitmap = null;  // Bitmap for the Review Marker
    private String mUID;                        // String for the user ID
    private int mReviewsCount;                  // int for the number of Reviews to expect in
                                                // onChildAdded() before a Review is new data

    public FirebaseDataSource(Context context) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "FirebaseDataSource() called");
        VectorDrawableCompat reviewMarkerVDC;   // VectorDrawableCompat for the Review Marker
        DatabaseReference fdbr;                 // DatabaseReference for the firebase
        DatabaseReference userFDBR;             // DatabaseReference for the user node
        int reviewMarkerWidth;                  // int for the width of the Review Marker
        int reviewMarkerHeight;                 // int for the height of the Review Marker

        // Initialize the DatabaseReferences and set mReviewsCount to -1, using this value as an
        // indicator that onDataChange() hasn't been called
        fdbr = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReviewsFDBR = fdbr.child("reviews");
        mReviewsCount = -1;

        // Add ValueEventListener to count the number of reviews
        mReviewsFDBR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mReviewsCount = (int)dataSnapshot.getChildrenCount();

                // Since the ReviewsChildEventListener will try to interact with mGoogleMap, we
                // can't add it unless mGoogleMap has already been set by setGoogleMap()
                if (mGoogleMap != null && !sReviewChildEventListenerAdded) {
                    mReviewsFDBR.addChildEventListener(new ReviewsChildEventListener());
                    sReviewChildEventListenerAdded = true;
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // If the user reference isn't null, set the user ID, mUserReviewsFDBR, initialize mStarred,
        // and add the StarredChildEventListener
        if (firebaseUser != null) {
            mUID = firebaseUser.getUid();
            userFDBR = fdbr
                    .child("users")
                    .child(mUID);
            mUserReviewsFDBR = userFDBR
                    .child("reviews");
            mStarred = new boolean[PLACE_COUNT];
            userFDBR.child("starred").addChildEventListener(new StarredChildEventListener());
        } else {
            mUserReviewsFDBR = null;
        }

        // Try making the Marker Bitmap
        try {
            MapsInitializer.initialize(context);
            reviewMarkerVDC = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_marker_bed, context.getTheme());
            reviewMarkerWidth = reviewMarkerVDC.getIntrinsicWidth();
            reviewMarkerHeight = reviewMarkerVDC.getIntrinsicHeight();
            reviewMarkerVDC.setBounds(0, 0, reviewMarkerWidth, reviewMarkerHeight);
            mReviewMarkerBitmap = Bitmap.createBitmap(reviewMarkerWidth, reviewMarkerHeight, Bitmap.Config.ARGB_8888);
            reviewMarkerVDC.draw(new Canvas(mReviewMarkerBitmap));
        } catch (Exception e) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "Caught exception trying to create bitmap");
            e.printStackTrace();
        }

        // Initialize mReviews, mReviewMarkers, and mGoogleMap
        mReviews = new HashMap<>();
        mReviewMarkers = new HashMap<>();
        mGoogleMap = null;
    }

    /**************** setGoogleMap() ****************
     * Sets mGoogleMap, and adds the ReviewsChildEventListener if possible
     * @param googleMap GoogleMap to set mGoogleMap to
     */
    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (mReviewsFDBR != null && mReviewsCount != -1 && !sReviewChildEventListenerAdded) {
            mReviewsFDBR.addChildEventListener(new ReviewsChildEventListener());
            sReviewChildEventListenerAdded = true;
        } else if (DEBUG_GLOBAL && DEBUG)
            Log.d(TAG, "setGoogleMap() called while mReviewsFDBR is null");
    }

    /**************** addReview() ****************
     * Adds the Review to mReviews, and adds its Marker to mGoogleMap (via MarkerOptions) and
     * mReviewMarkers. This should only be called by ReviewsChildEventListener.onChildAdded()
     * @param review    Review to add
     */
    public void addReview(Review review) {
        Marker marker;

        mReviews.put(review.getTimestamp(), review);

        if (mReviewMarkerBitmap != null) {
            marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(review.getLocation().toGoogleLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(mReviewMarkerBitmap)));
            marker.setTag(review.getTimestamp());
            mReviewMarkers.put(review.getTimestamp(), marker);
        } else if (DEBUG_GLOBAL && DEBUG)
            Log.d(TAG, "addReview() called while mReviewMarkerBitmap is null");
    }

    /**************** removeReviews() ****************
     * Removes the Review from mReviews, and removes its marker from mGoogleMap and mReviewMarkers.
     * This should only be called by ReviewsChildEventListener.onChildRemoved()
     * @param review    Review to remove
     */
    public void removeReview(Review review) {
        Marker marker;
        mReviews.remove(review.getTimestamp());
        marker = mReviewMarkers.get(review.getTimestamp());

        if (marker != null) {
            marker.remove();
            mReviewMarkers.remove(review.getTimestamp());
        }
    }

    /**************** createReview() ****************
     * Creates a Review in the Firebase DB (the full Review is put under mReviewsFDBR, and the key
     * is put under mUserReviewsFDBR
     * @param review    Review to create in the Firebase DB
     */
    public void createReview(Review review) {
        String key;

        key = mUID + "-" + review.getTimestamp().substring(0, review.getTimestamp().length() - 7);
        mReviewsFDBR.child(key).setValue(review);
        mUserReviewsFDBR.child(key).setValue("");
    }

    /**************** deleteReview() ****************
     * Deletes a Review in the Firebase DB (both just the key and the full Review from
     * mUserReviewsFDBR and mReviewsFDBR respectively
     * @param review    Review to delete in the Firebase DB
     */
    public void deleteReview(Review review) {
        String key;

        if (mReviewsFDBR == null || mUserReviewsFDBR == null) return;

        key = mUID + "-" + review.getTimestamp().substring(0, review.getTimestamp().length() - 7);

        mReviewsFDBR.child(key).removeValue();
        mUserReviewsFDBR.child(key).removeValue();
    }

    /**************** getReview() ****************
     * Gets a Review from mReviews
     * @param reviewKey String for the key to the Review in mReviews
     * @return          Review associated with the given Review key (or null if not found)
     */
    public Review getReview(String reviewKey) {
        if (mReviews == null) return null;

        return mReviews.get(reviewKey);
    }

    /**************** getUserReviews() ****************
     * Gets all of the user's Reviews from the DB and passes them to MyReviewsFragment
     */
    public void getUserReviews() {
        final ArrayList<Review> userReviews = new ArrayList<>();
        mReviewsFDBR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot child : dataSnapshot.getChildren()) {
                    Review review = child.getValue(Review.class);
                    if (review.getAuthor().equals(mUID)) {
                        userReviews.add(review);
                    }
                }
                MyReviewsFragment.updateReviews(userReviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**************** ReviewsChildEventListener ****************
     * ChildEventListener for listening to the addition and deletion of Reviews
     */
    private class ReviewsChildEventListener implements ChildEventListener {
        private static final String TAG = TAG_GLOBAL + ": ReviewsChildEventListener";
        private static final boolean DEBUG = true;

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            int index;

            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildAdded() called; mReviewsCount: " + mReviewsCount);

            Review review = dataSnapshot.getValue(Review.class);

            // If mReviewsCount is greater than 0, this is a pre-existing Review and a starred
            // Review Notification shouldn't be shown
            if (mReviewsCount > 0) mReviewsCount--;
            else if (review != null && mStarred != null){
                // Otherwise, if review was fetched from the DataSnapshot properly and mStarred
                // isn't null, check if the Review is from a starred place
                index = getPlaceIndex(review.getLocation().toDoubleArr());

                if (DEBUG_GLOBAL && DEBUG) {
                    if (index == -1) Log.d(TAG, "Location of review added isn't valid");
                    else {
                        Log.d(TAG, "Review happened in " +
                                PLACE_NAMES[index] + ", which the user " +
                                (mStarred[index] ? "has" : "hasn't") + " starred");
                    }
                }

                if (index != -1 && mStarred[index] && !review.getAuthor().equals(mUID)) LocationService.sNotificationCenter.postStarredReviewNotification(
                        review.getTimestamp(), index);
            } else if (DEBUG_GLOBAL && DEBUG) {
                if (review == null) Log.d(TAG, "review is null");
                if (mStarred == null) Log.d(TAG, "mStarred is null");
            }

            addReview(review);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildChanged() called");
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildRemoved() called");

            removeReview(dataSnapshot.getValue(Review.class));
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildMoved() called");
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onCancelled() called");
        }
    }

    /**************** StarredChildEventListener ****************
     * Responds to changes in the starred array in the DB
     */
    private class StarredChildEventListener implements ChildEventListener {
        private static final String TAG = TAG_GLOBAL + ": StarredChildEventListener";
        private static final boolean DEBUG = true;

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildAdded() called");
            setSingleStarred(dataSnapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildChanged() called");
            setSingleStarred(dataSnapshot);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildRemoved() called");
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildMoved() called");
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onCancelled() called");
        }

        /**************** setSingleStarred() ****************
         * Sets a single element of mStarred using the index and value specified in dataSnapshot
         * @param dataSnapshot  DataSnapshot to get the index and value from
         */
        private void setSingleStarred(@NonNull DataSnapshot dataSnapshot) {
            String key;
            Object value;
            int index;
            boolean starred;

            key = dataSnapshot.getKey();
            index = key == null ? -1 : Integer.parseInt(dataSnapshot.getKey());
            value = dataSnapshot.getValue();
            starred = (value != null && Boolean.parseBoolean(value.toString()));

            if (index >= 0 && index < PLACE_COUNT) mStarred[index] = starred;
        }
    }

    /**************** getReviewsNear() ****************
     * Gets the Reviews near the given LatLng
     * @param reviews   ArrayList of Reviews to sort by distance from the location
     * @param location  LatLng to measure the distance from
     * @return          The same ArrayList passed in, reviews
     */
    public ArrayList<Review> getReviewsNear(ArrayList<Review> reviews, final LatLng location) {

        reviews.sort(new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                double microDistance1 = microDistanceBetween(
                        o1.getLocation().toDoubleArr(),
                        location.toDoubleArr());
                double microDistance2 = microDistanceBetween(
                        o2.getLocation().toDoubleArr(),
                        location.toDoubleArr());
                if (microDistance1 == microDistance2) return 0;
                return microDistance1 < microDistance2 ? -1 : 1;
            }
        });

        return reviews;
    }

    /**************** logStarred() ****************
     * Logs the values of mStarred (for debugging)
     */
    private void logStarred() {
        if (!(DEBUG_GLOBAL && DEBUG)) return;

        String logStr = "mStarred: ";

        if (mStarred == null) logStr += "[null]";
        else {
            for (int i = 0; i < PLACE_COUNT; i++) {
                logStr += String.format(Locale.getDefault(),
                        "\n\t%02d\t%s",
                        i,
                        mStarred[i] ? "true" : "false");
            }
        }

        Log.d(TAG, logStr);
    }
}