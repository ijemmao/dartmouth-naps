package edu.dartmouth.cs65.dartmouthnaps.fragments;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainForFragmentActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewCardsContainerFragment extends Fragment {

    public static ArrayList<Review> reviews;

    public ViewPager mPager;
    private PagerAdapter pagerAdapter;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private StorageReference imageReference;

    public ReviewCardsContainerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_cards_container, container, false);

        reviews = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        imageReference = FirebaseStorage.getInstance().getReference();

        dbReference.addValueEventListener(reviewsListener);

        return view;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final ReviewCardFragment cardFragment = new ReviewCardFragment();
            Review review = reviews.get(position);
            final Bundle extras = new Bundle();
            extras.putString("title", review.getTitle());
            extras.putInt("noise", review.getNoise());
            extras.putInt("comfort", review.getComfort());
            extras.putInt("light", review.getLight());
            extras.putInt("convenience", review.getConvenience());
            cardFragment.setArguments(extras);

            return cardFragment;
        }

        @Override
        public int getCount() {
            return reviews.size();
        }
    }

    // Calculates the convenience of each review card
    private int calculateConvenience(float distance) {
        if (distance <= 80) {
            // Five bars
            return 5;
        } else if (distance <= 160) {
            // Four bars
            return 4;
        } else if (distance <= 320) {
            // Three bars
            return 3;
        } else if (distance <= 440) {
            // Two bars
            return 2;
        }
        // One bar
        return 1;
    }

    public void calculateConveniences(Location currentLocation) {
        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            review.setConvenience(calculateConvenience(currentLocation.distanceTo(review.getLocation().toLocation())));
        }
        mPager = getActivity().findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
    }

    ValueEventListener reviewsListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            reviews = new ArrayList<>();
            Location currentLocation = CampusMapFragment.sCurrentLocation;

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Review review = snapshot.getValue(Review.class);
                reviews.add(review);
            }

            // Sorts the reviews when a new item is added to the database
            if (currentLocation != null) {
                reviews = MainForFragmentActivity.sFirebaseDataSource.getReviewsNear(reviews, new edu.dartmouth.cs65.dartmouthnaps.models.LatLng(
                        currentLocation.getLatitude(), currentLocation.getLongitude()));
            }

            mPager = getActivity().findViewById(R.id.pager);
            pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                // Focus on marker when swiping through cards with smooth animation
                @Override
                public void onPageSelected(int i) {
                    Review review = reviews.get(i);
                    CampusMapFragment.mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(review.getLocation().latitude, review.getLocation().longitude)));
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
            mPager.setAdapter(pagerAdapter);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

}
