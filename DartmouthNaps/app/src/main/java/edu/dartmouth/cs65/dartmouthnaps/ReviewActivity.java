package edu.dartmouth.cs65.dartmouthnaps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ReviewActivity extends AppCompatActivity {

    TextView reviewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Bundle extras = getIntent().getExtras();

        reviewTitle = findViewById(R.id.review_title);

        reviewTitle.setText(extras.getString("title"));
    }
}
