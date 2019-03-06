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
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.fragments.MyReviewsFragment;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class FirebaseDataSource {
    private static final String TAG = TAG_GLOBAL + ": FirebaseDataSource";
    private static final boolean DEBUG = true;
    private DatabaseReference mReviewsFDBR;
    private DatabaseReference mUserReviewsFDBR;
    private GoogleMap mGoogleMap;
    private Map<String, Review> mReviews;
    private Map<String, Marker> mReviewMarkers;
    private Bitmap mReviewMarkerBitmap = null;
    private String mUID;

    public FirebaseDataSource(Context context) {
        VectorDrawableCompat reviewMarkerVDC;
        int reviewMarkerWidth;
        int reviewMarkerHeight;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReviewsFDBR = dbRef.child("reviews");

        if (firebaseUser != null) {
            mUID = firebaseUser.getUid();
            mUserReviewsFDBR = dbRef
                    .child("users")
                    .child(mUID)
                    .child("reviews");
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
    }

    public void addReviewsChildEventListener(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (mReviewsFDBR != null) {
            mReviewsFDBR.addChildEventListener(new ReviewsChildEventListener());
        } else if (DEBUG_GLOBAL && DEBUG)
            Log.d(TAG, "addReviewsChildEventListener() called while mReviewsFDBR is null");
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
        String key = mUID + "-" + review.getTimestamp().substring(0, review.getTimestamp().length() - 7);
        mReviewsFDBR.child(key).removeValue();
        mUserReviewsFDBR.child(key).removeValue();
    }

    public void createReview(Review review) {
        String key;

        key = mUID + "-" + review.getTimestamp().substring(0, review.getTimestamp().length() - 7);
        mReviewsFDBR.child(key).setValue(review);
        mUserReviewsFDBR.child(key).setValue("");
    }

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

    private class ReviewsChildEventListener implements ChildEventListener {
        private static final String TAG = TAG_GLOBAL + ": ReviewsChildEventListenerRename";
        private static final boolean DEBUG = false;

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildAdded() called");

            addReview(dataSnapshot.getValue(Review.class));
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildChanged() called");

            removeReview(dataSnapshot.getValue(Review.class));
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
