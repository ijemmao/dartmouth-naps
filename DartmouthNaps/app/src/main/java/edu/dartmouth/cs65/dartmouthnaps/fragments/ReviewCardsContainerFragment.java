package edu.dartmouth.cs65.dartmouthnaps.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainForFragmentActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

import java.util.concurrent.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewCardsContainerFragment extends Fragment {

    public static ArrayList<Review> reviews;
    private static ArrayList<Review> tempReviews;


    public ViewPager mPager;
    private PagerAdapter pagerAdapter;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private StorageReference storageReference;

    ExecutorService executorService;
    private static DataSnapshot dataSnapshot;

    public ReviewCardsContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        storageReference = FirebaseStorage.getInstance().getReference();

        dbReference.addValueEventListener(reviewsListener);

        executorService = Executors.newSingleThreadExecutor();

        return view;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            final ReviewCardFragment cardFragment = new ReviewCardFragment();
            Review review = reviews.get(position);
            final Bundle extras = new Bundle();

            // Writing images of reviews to phone to avoid passing over byte arrays
            try {
                String imageFileName = "bitmap";

                File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(imageFileName, ".jpg", storageDir);

                imageFileName = image.getAbsolutePath();

                Bitmap bmp = BitmapFactory.decodeByteArray(review.getImage(), 0, review.getImage().length);
                FileOutputStream stream = new FileOutputStream(new File(imageFileName));
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                //Cleanup
                stream.close();
                bmp.recycle();

                extras.putString("title", review.getTitle());
                extras.putString("image", imageFileName);
                extras.putInt("noise", review.getNoise());
                extras.putInt("comfort", review.getComfort());
                extras.putInt("light", review.getLight());
                extras.putInt("convenience", review.getConvenience());
                cardFragment.setArguments(extras);
            } catch (Exception e) {
                System.out.println("REVIEW: " + review.getImage().length);
                e.printStackTrace();
            }

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
        pagerAdapter.notifyDataSetChanged();
    }

    // AsyncTask to load all the images together from Firebase
    public class ImageLoadTask extends AsyncTask<Void, Void, Void> {

        @Override protected Void doInBackground(Void... params) {

            tempReviews = new ArrayList<>();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                final Review review = snapshot.getValue(Review.class);

                try {
                    Future reviewFuture = runFuture(review);
                    byte[] reviewImage = (byte[]) reviewFuture.get();
                    review.setImage(reviewImage);
                } catch (Exception e) {

                }
                tempReviews.add(review);
            }

            return null;
        }

        // Once all images are loaded, the screen will be updated with all the reviews
        @Override protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            reviews = tempReviews;
            if (pagerAdapter != null) {
                pagerAdapter.notifyDataSetChanged();
            }

            Location currentLocation = CampusMapFragment.sCurrentLocation;

            // Sorts the reviews when a new item is added to the database
            if (currentLocation != null) {
                reviews = MainActivity.sFirebaseDataSource.getReviewsNear(reviews, new edu.dartmouth.cs65.dartmouthnaps.models.LatLng(
                        currentLocation.getLatitude(), currentLocation.getLongitude()));
            }

            try {
                mPager = getActivity().findViewById(R.id.pager);
                pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
                mPager.setAdapter(pagerAdapter);
                pagerAdapter.notifyDataSetChanged();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Future (similar to Promise) which is an async object that will be completed later
    public Future runFuture(final Review review) {
        Future<byte[]> future = executorService.submit(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {

                StorageReference imageRef = storageReference.child("images/" + review.getAuthor() + "-" + review.getImageName() + ".jpg");
                final long ONE_MEGABYTE = 1024 * 1024;
                Task<byte[]> result = imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                            review.setImage(bytes);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                while (!result.isComplete()) {}

                return result.getResult();
            }
        });
        return future;
    }


    // ValueEventListener that listens for changes from Firebase
    // Once an event is heard, the listener will call the AsyncTask
    ValueEventListener reviewsListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            ReviewCardsContainerFragment.dataSnapshot = dataSnapshot;
            new ImageLoadTask().execute();

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


}
