package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

/*Written by the Dartmouth Naps Team*/
public class MyReviewsFragment extends Fragment {

    public static ListView listView; //The list view
    public static MyReviewsAdapter adapter; //The adapter for the list view, using a custom adapter defined below

    //stores firebase information
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uID;
    private DatabaseReference dbReference;

    public static List<Review> reviews; //list of my reviews
    private static Context context; //context of this fragment


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_reviews_fragment, container, false);

        listView = view.findViewById(R.id.my_reviews_list_view);
        reviews = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = user.getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("users").child(uID).child("reviews");

        uID = user.getUid();

        context = getActivity();
        adapter = new MyReviewsAdapter(getActivity(), android.R.layout.simple_list_item_1, reviews);

        MainActivity.sFirebaseDataSource.getUserReviews(); //gets the user's reviews from the firebase datasource
        listView.setAdapter(adapter); //sets the adapter

        //sets the onclick function of the open drawer button
        ImageButton button = (ImageButton)view.findViewById(R.id.open_drawer_review);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.drawer.openDrawer(GravityCompat.START);
            }
        });

        return view;
    }

    //updates the reviews when a review is deleted or added
    public static void updateReviews(ArrayList<Review> userReviews) {
        reviews = userReviews;
        adapter.notifyDataSetChanged();
        adapter = new MyReviewsAdapter(context, android.R.layout.simple_list_item_1, reviews);
        listView.setAdapter(adapter);
    }
}

//Adapter for the list view that holds all of user's reviews
class MyReviewsAdapter extends ArrayAdapter<Review> {

    private Context context;
    private int resource;
    private List<Review> reviews;

    //Constructor
    public MyReviewsAdapter(Context context, int resource, List<Review> reviews) {
        super(context, resource, reviews);

        this.context = context;
        this.resource = resource;
        this.reviews = reviews;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.my_reviews_list_view,parent,false);
        }

        Review review = reviews.get(position); //gets the review

        TextView title = view.findViewById(R.id.my_entry_title); //gets the title
        TextView timestamp = view.findViewById(R.id.timestamp); //gets the timestamp

        title.setText(review.getTitle()); //sets title
        timestamp.setText(review.getFormattedTimestamp()); //sets timestamp

        //gets and initializes onclick of the delete review button
        ImageButton deleteButton = view.findViewById(R.id.delete_review_button);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MainActivity.sFirebaseDataSource.deleteReview(reviews.get(position));
                reviews.remove(position); //or some other task
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
