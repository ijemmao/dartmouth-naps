package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;

public class MyReviewsFragment extends Fragment {

    private ListView listView; //The list view
    public MyReviewsAdapter adapter; //The adapter for the list view, using a custom adapter defined below


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_reviews_fragment, container, false);

        listView = (ListView) view.findViewById(R.id.my_reviews_list_view);
        List<Review> reviews = new ArrayList<Review>();

        Review newReview = new Review("Bryan",3,4,3,"Kemeny 007","img","January 1, 2019");
        Review newReview2 = new Review("Bryan",3,4,3,"Top of the Hop","img","February 29, 2019");

        reviews.add(newReview);
        reviews.add(newReview2);
        //replace this hard coded reviews with getting from firebase and adding to adapter

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

    public void tempOnClick(View view) {

    }

}
