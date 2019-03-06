package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

public class MyReviewsFragment extends Fragment {

    private ListView listView; //The list view
    private MyReviewsAdapter adapter; //The adapter for the list view, using a custom adapter defined below
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uID;
    private DatabaseReference dbReference;
    private StorageReference storageReference;
    private ChildEventListener listener;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_reviews_fragment, container, false);

        listView = (ListView) view.findViewById(R.id.my_reviews_list_view);
        List<Review> reviews = new ArrayList<Review>();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = user.getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("users").child(uID).child("reviews");
        storageReference = FirebaseStorage.getInstance().getReference();

        uID = user.getUid();
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("tag2", dataSnapshot.toString());

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //unused
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //check if something was removed, and delete it from database and refresh adapter
//                ExerciseEntry entry = dataSnapshot.getValue(ExerciseEntry.class);
//                exerciseEntryDataSource.deleteEntry(entry);
//                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //unused
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //unused
            }
        };

        dbReference.addChildEventListener(listener); //sets the listener in the correct leve

        adapter = new MyReviewsAdapter(getActivity(), android.R.layout.simple_list_item_1, reviews);
        listView.setAdapter(adapter);

        return view;
    }


}

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

        Review review = reviews.get(position);

        TextView title = (TextView)view.findViewById(R.id.my_entry_title);
        TextView timestamp = (TextView)view.findViewById(R.id.timestamp);

        title.setText(review.getTitle());
        timestamp.setText(review.getTimestamp());


        ImageButton editButton = (ImageButton)view.findViewById(R.id.edit_review_button);
        ImageButton deleteButton = (ImageButton)view.findViewById(R.id.delete_review_button);

        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //GO TO EDIT ACTIVITY
            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reviews.remove(position); //or some other task
                notifyDataSetChanged();
            }
        });

        return view;
    }



}
