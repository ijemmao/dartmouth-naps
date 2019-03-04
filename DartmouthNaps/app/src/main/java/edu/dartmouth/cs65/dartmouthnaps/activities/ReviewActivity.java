package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.RatingFragment;

public class ReviewActivity extends AppCompatActivity {

    TextView reviewTitle;
    ImageView reviewImage;

    String image;

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

        reviewImage = findViewById(R.id.review_image);

        image = extras.getString("image");
        noiseFragment.setRating(extras.getInt("noise"));
        comfortFragment.setRating(extras.getInt("comfort"));
        lightFragment.setRating(extras.getInt("light"));
        convenienceFragment.setRating(extras.getInt("convenience"));

        try {
            FileInputStream is = new FileInputStream (new File(image));

            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();

            reviewImage.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
