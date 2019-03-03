package edu.dartmouth.cs65.dartmouthnaps.listeners;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import edu.dartmouth.cs65.dartmouthnaps.activities.MainForFragmentActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class ReviewsChildEventListener implements ChildEventListener {
    private static final String TAG = TAG_GLOBAL + ": ReviewsChildEventListener";
    private static final boolean DEBUG = true;

    private GoogleMap mGoogleMap;

    public ReviewsChildEventListener(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Review review;

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildAdded() called");

        review = dataSnapshot.getValue(Review.class);
        MainForFragmentActivity.sFirebaseDataSource.addReview(review);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Review review;

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onChildChanged() called");

        review = dataSnapshot.getValue(Review.class);
        MainForFragmentActivity.sFirebaseDataSource.removeReview(review);
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
