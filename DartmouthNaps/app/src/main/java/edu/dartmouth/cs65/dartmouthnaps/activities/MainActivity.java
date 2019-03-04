package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.View;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.MyReviewsFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private String uID = "";

    private MyReviewsFragment myReviewsFragment;
//    private TempNavFragment tempNavFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();


        if(savedInstanceState == null) {
            if (user == null) { //if noone is logged in, go to login activity
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
            } else { //fetch the data from firebase and load it onto local database, if right afterlogging in
                uID = user.getUid();
            }
        }

        Log.d("tag2", "UID: " + uID);

//        if(!uID.equals("")) {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.text_open, R.string.text_close); //to toggle open and close the navigation drawer
            drawer.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
            actionBarDrawerToggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this); //sets up the listener for navigation item changes

        getSupportActionBar().hide();

//        } else {
//            setContentView(R.layout.temp_no_login_main_activity);
//        }

        myReviewsFragment = new MyReviewsFragment();

        navigationView.setCheckedItem(R.id.nav_reviews);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, myReviewsFragment).commit();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (id == R.id.nav_reviews) {
            Log.d("tag2", "here");
            fragmentManager.beginTransaction().replace(R.id.content_main, myReviewsFragment).commit();
        } else if(id == R.id.nav_temp) {
            Log.d("tag2", "here2");
//            fragmentManager.beginTransaction().replace(R.id.content_main, tempNavFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void onLogoutClicked(View view) {
        Toast.makeText(getApplicationContext(), "LOGOUT HERE", Toast.LENGTH_SHORT).show();
    }
}
