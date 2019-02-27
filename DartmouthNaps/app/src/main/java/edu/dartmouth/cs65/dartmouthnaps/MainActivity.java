package edu.dartmouth.cs65.dartmouthnaps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.dartmouth.cs65.dartmouthnaps.models.Place;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        if(user == null) { //if noone is logged in, go to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else { //fetch the data from firebase and load it onto local database, if right afterlogging in
            uID = user.getUid();
            TextView welcomeUser = (TextView) findViewById(R.id.text_welcome_user);
            welcomeUser.setText("Welcome User " + uID);
        }

        LinearLayout linearLayout = findViewById(R.id.color_parent);
        List<TextView> textViews = new ArrayList<>();
        List<Place> Place = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            textViews.add((TextView)linearLayout.getChildAt(i));
            Place.add(new Place());
            Map<String, Integer> ratings = Place.get(i).getRatings();
            for (int j = 0; j < 21; j++) {
                ratings.put("" + j, j <= i ? 5 : 0);
            }
            textViews.get(i).setBackgroundColor(Place.get(i).ratingColor());
        }
    }
}
