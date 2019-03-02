package edu.dartmouth.cs65.dartmouthnaps;


import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RatingFragment extends Fragment implements View.OnClickListener {

    TextView header;
    CharSequence headerTitle;
    Button firstRating;
    Button secondRating;
    Button thirdRating;
    Button fourthRating;
    Button fifthRating;

    boolean disabled;

    Button[] ratingsList;

    int rating;


    public RatingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.RatingFragment);

        // Unique title and disabled switch
        headerTitle = a.getString(R.styleable.RatingFragment_title);
        disabled = a.getBoolean(R.styleable.RatingFragment_disabled, false );

        a.recycle();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        header = view.findViewById(R.id.header);
        header.setText(headerTitle);

        firstRating = view.findViewById(R.id.first_rating);
        secondRating = view.findViewById(R.id.second_rating);
        thirdRating = view.findViewById(R.id.third_rating);
        fourthRating = view.findViewById(R.id.fourth_rating);
        fifthRating = view.findViewById(R.id.fifth_rating);

        firstRating.setOnClickListener(this);
        secondRating.setOnClickListener(this);
        thirdRating.setOnClickListener(this);
        fourthRating.setOnClickListener(this);
        fifthRating.setOnClickListener(this);

        // Disabled when presenting the data
        if (disabled) {
            firstRating.setEnabled(false);
            secondRating.setEnabled(false);
            thirdRating.setEnabled(false);
            fourthRating.setEnabled(false);
            fifthRating.setEnabled(false);
        }



        ratingsList = new Button[]{firstRating, secondRating, thirdRating, fourthRating, fifthRating};

        rating = 0;

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.first_rating:
                ratingsList[0].setBackground(getResources().getDrawable(R.drawable.red, null));
                setColor(1, getResources().getDrawable(R.drawable.red, null));
                rating = 1;
                break;
            case R.id.second_rating:
                setColor(2, getResources().getDrawable(R.drawable.red, null));
                rating = 2;
                break;
            case R.id.third_rating:
                setColor(3, getResources().getDrawable(R.drawable.yellow, null));
                rating = 3;
                break;
            case R.id.fourth_rating:
                setColor(4, getResources().getDrawable(R.drawable.blue, null));
                rating = 4;
                break;
            case R.id.fifth_rating:
                setColor(5, getResources().getDrawable(R.drawable.blue, null));
                rating = 5;
                break;
        }
    }

    private void setColor(int rating, Drawable drawable) {
        Drawable gray = getResources().getDrawable(R.drawable.gray, null);
        for (int i = 0; i < rating; i++) {
            ratingsList[i].setBackground(drawable);
        }
        for (int i = rating; i < 5; i++) {
            ratingsList[i].setBackground(gray);
        }
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
        if (rating == 1 || rating == 2) {
            setColor(rating, getResources().getDrawable(R.drawable.red, null));
        } else if (rating == 3) {
            setColor(rating, getResources().getDrawable(R.drawable.yellow, null));
        } else {
            setColor(rating, getResources().getDrawable(R.drawable.blue, null));
        }
    }

}
