package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.RatingFragment;

public class ReviewActivity extends AppCompatActivity {

    TextView reviewTitle;

    RatingFragment noiseFragment;
    RatingFragment comfortFragment;
    RatingFragment lightFragment;
    RatingFragment convenienceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Bundle extras = getIntent().getExtras();

        reviewTitle = findViewById(R.id.review_title);

        reviewTitle.setText(extras.getString("title"));

        noiseFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.noise_fragment);
        comfortFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.comfort_fragment);
        lightFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.light_fragment);
        convenienceFragment = (RatingFragment) getSupportFragmentManager().findFragmentById(R.id.convenience_fragment);

        noiseFragment.setRating(extras.getInt("noise"));
        comfortFragment.setRating(extras.getInt("comfort"));
        lightFragment.setRating(extras.getInt("light"));
//        convenienceFragment.setRating(extras.getInt("noise"));
    }
}
