package edu.dartmouth.cs65.dartmouthnaps.fragments;


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

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewCardsContainerFragment extends Fragment {

    private ArrayList<Review> reviews;

    private ViewPager mPager;
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
            cardFragment.setArguments(extras);

            return cardFragment;
        }

        @Override
        public int getCount() {
            return reviews.size();
        }
    }

    ValueEventListener reviewsListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            System.out.println(dataSnapshot);

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Review review = snapshot.getValue(Review.class);
                reviews.add(review);
            }

            mPager = (ViewPager) getActivity().findViewById(R.id.pager);
            pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
            mPager.setAdapter(pagerAdapter);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

}