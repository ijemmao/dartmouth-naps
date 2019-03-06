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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;
import edu.dartmouth.cs65.dartmouthnaps.models.User;
import edu.dartmouth.cs65.dartmouthnaps.services.LocationService;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class FirebaseDataSource {
    private static final String TAG = TAG_GLOBAL + ": FirebaseDataSource";
    private static final boolean DEBUG = true;
    private DatabaseReference mReviewsFDBR;
    private DatabaseReference mUserReviewsFDBR;
    private DatabaseReference mUserStarredFDBR;
    private GoogleMap mGoogleMap;
    private Map<String, Review> mReviews;
    private Map<String, Marker> mReviewMarkers;
    private boolean[] mStarred;
    private Bitmap mReviewMarkerBitmap = null;
    private String mUID;
    private int mReviewsCount;

    public FirebaseDataSource(Context context) {
        VectorDrawableCompat reviewMarkerVDC;
        DatabaseReference userFDBR;
        int reviewMarkerWidth;
        int reviewMarkerHeight;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReviewsFDBR = dbRef.child("reviews");
        mReviewsCount = -1;
        mReviewsFDBR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mReviewsCount = (int)dataSnapshot.getChildrenCount();

                if (mGoogleMap != null)
                    mReviewsFDBR.addChildEventListener(new ReviewsChildEventListener());
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        if (firebaseUser != null) {
            mUID = firebaseUser.getUid();
            userFDBR = dbRef
                    .child("users")
                    .child(mUID);
            mUserReviewsFDBR = userFDBR
                    .child("reviews");
            mUserStarredFDBR = userFDBR
                    .child("starred");
            userFDBR.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    mStarred = user == null ? null : user.toBooleanArr();

                    if (mStarred != null) mUserStarredFDBR.addChildEventListener(
                            new StarredChildEventListener());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        } else {
            mUserReviewsFDBR = null;
        }

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

        mReviews = new HashMap<>();
        mReviewMarkers = new HashMap<>();
        mGoogleMap = null;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (mReviewsFDBR != null && mReviewsCount != -1) {
            mReviewsFDBR.addChildEventListener(new ReviewsChildEventListener());
        } else if (DEBUG_GLOBAL && DEBUG)
            Log.d(TAG, "setGoogleMap() called while mReviewsFDBR is null");
    }

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

    public void removeReview(Review review) {
        Marker marker;
        mReviews.remove(review.getTimestamp());
        marker = mReviewMarkers.get(review.getTimestamp());

        if (marker != null) {
            marker.remove();
            mReviewMarkers.remove(review.getTimestamp());
        }
    }

    public void createReview(Review review) {
        String key;

        key = mUID + "-" + review.getTimestamp().substring(0, 19);
        mReviewsFDBR.child(key).setValue(review);
        mUserReviewsFDBR.child(key).setValue("");
    }

    private class ReviewsChildEventListener implements ChildEventListener {
        private static final String TAG = TAG_GLOBAL + ": ReviewsChildEventListenerRename";
        private static final boolean DEBUG = true;

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            int index;

            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildAdded() called");

            Review review = dataSnapshot.getValue(Review.class);
            if (mReviewsCount > 0) mReviewsCount--;
            else if (review != null && mStarred != null){
                index = getPlaceIndex(review.getLocation().toDoubleArr());

                if (DEBUG_GLOBAL && DEBUG) {
                    if (index == -1) Log.d(TAG, "Location of review added isn't valid");
                    else {
                        Log.d(TAG, "Review happened in " +
                                PLACE_NAMES[index] + ", which the user " +
                                (mStarred[index] ? "has" : "hasn't") + " starred");
                    }
                }

                if (index != -1) LocationService.sNotificationCenter.postStarredReviewNotification(
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

    private class StarredChildEventListener implements ChildEventListener {
        private static final String TAG = TAG_GLOBAL + ": StarredChildEventListener";
        private static final boolean DEBUG = true;

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildChanged() called");

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
    }

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
}
