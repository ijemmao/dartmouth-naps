package edu.dartmouth.cs65.dartmouthnaps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.models.Location;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout linearLayout = findViewById(R.id.color_parent);
        List<TextView> textViews = new ArrayList<>();
        List<Location> Location = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            textViews.add((TextView)linearLayout.getChildAt(i));
            Location.add(new Location());
            Map<String, Integer> ratings = Location.get(i).getRatings();
            for (int j = 0; j < 21; j++) {
                ratings.put("" + j, j <= i ? 5 : 0);
            }
            textViews.get(i).setBackgroundColor(Location.get(i).ratingColor());
        }
    }
}
