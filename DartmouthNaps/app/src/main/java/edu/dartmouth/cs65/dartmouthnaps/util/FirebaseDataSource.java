package edu.dartmouth.cs65.dartmouthnaps.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.listeners.ReviewsChildEventListener;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class FirebaseDataSource {
    private static final String TAG = "DartmouthNaps: FirebaseDataSource";
    private static final boolean DEBUG = true;
    private DatabaseReference mReviewsFDBR;
    private DatabaseReference mUserReviewsFDBR;
    private GoogleMap mGoogleMap;
    private Map<String, Review> mReviews;
    private Map<String, Marker> mReviewMarkers;
    private Bitmap mReviewMarkerBitmap = null;

    public FirebaseDataSource(Context context) {
        VectorDrawableCompat reviewMarkerVDC;
        int reviewMarkerWidth;
        int reviewMarkerHeight;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReviewsFDBR = dbRef.child("reviews");

        if (firebaseUser != null) mUserReviewsFDBR = dbRef
                .child("users")
                .child(firebaseUser.getUid())
                .child("reviews");
        else mUserReviewsFDBR = null;

        try {
            MapsInitializer.initialize(context);
            reviewMarkerVDC = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_marker_bed, context.getTheme());
            reviewMarkerWidth = reviewMarkerVDC.getIntrinsicWidth();
            reviewMarkerHeight = reviewMarkerVDC.getIntrinsicHeight();
            reviewMarkerVDC.setBounds(0, 0, reviewMarkerWidth, reviewMarkerHeight);
            mReviewMarkerBitmap = Bitmap.createBitmap(reviewMarkerWidth, reviewMarkerHeight, Bitmap.Config.ARGB_8888);
            reviewMarkerVDC.draw(new Canvas(mReviewMarkerBitmap));
//            mReviewMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_marker_bed);
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
            mReviewsFDBR.addChildEventListener(new ReviewsChildEventListener(googleMap));
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
    }

    public void createReview(Review review) {
        String key = mReviewsFDBR.push().getKey();

        if (key != null) {
            mReviewsFDBR.child(key).setValue(review);
            mUserReviewsFDBR.child(key);
        } else if (DEBUG_GLOBAL && DEBUG)
            Log.d(TAG, "mReviewsFDBR.push().getKey() returned null in createReview()");
    }
}
