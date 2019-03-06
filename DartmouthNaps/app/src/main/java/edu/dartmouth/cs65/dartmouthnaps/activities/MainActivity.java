package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.Manifest;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.View;
import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.CampusMapFragment;
import edu.dartmouth.cs65.dartmouthnaps.fragments.MyReviewsFragment;
import edu.dartmouth.cs65.dartmouthnaps.fragments.StarredLocationsFragment;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.services.LocationService;
import edu.dartmouth.cs65.dartmouthnaps.util.FirebaseDataSource;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

/*Written by the Dartmouth Naps Team*/
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CampusMapFragment.CMFListener {
    private static final String TAG = TAG_GLOBAL + ": MainActivity"; //tag for debugging
    private static final boolean DEBUG = true; //bool for if we are debugging

    private static final int REQ_ACCESS_FINE_LOCATION = 0; //value to request location permissions
    public static FirebaseDataSource sFirebaseDataSource; //data source for the firebase
    public static DrawerLayout drawer; //navigation drawer

    //firebase information
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private String uID = "";

    //fragments
    private MyReviewsFragment myReviewsFragment;
    private CampusMapFragment mCampusMapFragment;
    private StarredLocationsFragment starredLocationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        boolean permissionsGranted;

        //gets firebase instance and user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        if(savedInstanceState == null) { //handles orientation changes
            if (user == null) { //if noone is logged in, go to signup activity
                intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
            } else { //fetch the data from firebase and load it onto local database, if right afterlogging in
                uID = user.getUid();
            }
        }

        //creates and formats the navigation drawer
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.text_open, R.string.text_close); //to toggle open and close the navigation drawer
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); //sets up the listener for navigation item changes
        getSupportActionBar().hide(); //hides teh action bar

        //creates fragments and adds to navigation drawer
        myReviewsFragment = new MyReviewsFragment();
        starredLocationsFragment = new StarredLocationsFragment();
        sFirebaseDataSource = new FirebaseDataSource(getApplicationContext());
        permissionsGranted = checkPermissions();
        mCampusMapFragment = CampusMapFragment.newInstance(permissionsGranted);
        navigationView.setCheckedItem(R.id.nav_reviews);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mCampusMapFragment).commit(); //sets default fragment to be displayed
        }

        if(uID.equals("")) { //if noone is logged in, prevent drawer opening
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mCampusMapFragment.setHideButton(true);
        } else { //if logged in, set email text
            View header = navigationView.getHeaderView(0);
            TextView headerEmail = (TextView) header.findViewById(R.id.header_email);
            headerEmail.setText(user.getEmail());
        }

        //request permissions if not granted already
        if (!permissionsGranted) requestPermissions();
    }

    //if main activity is active on notification click, uses this instead of onCreate
    @Override
    protected void onNewIntent (Intent intent) {
        MenuItem navMapMI = findViewById(R.id.nav_map);
        if (navMapMI != null) onNavigationItemSelected(navMapMI);

        if (intent.getBooleanExtra(KEY_REVIEW_PROMPT, false)
                && mCampusMapFragment != null) {
            mCampusMapFragment.reviewPrompt(new LatLng(
                    intent.getDoubleExtra(KEY_LATITUDE, PLACE_COORDINATES_AVG[1][LAT]),
                    intent.getDoubleExtra(KEY_LONGITUDE, PLACE_COORDINATES_AVG[1][LNG])));
        } else if (intent.getBooleanExtra(KEY_STARRED_REVIEW, false)) {
            mCampusMapFragment.showStarredReview(sFirebaseDataSource.getReview(intent.getStringExtra(KEY_REVIEW_KEY)));
        }
    }

    //handles the navigation item selection, and inflates the appropriate fragments
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onNavigationItemSelected() called");
        int id = menuItem.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (id == R.id.nav_reviews) {
            fragmentManager.beginTransaction().replace(R.id.content_main, myReviewsFragment).commit();
        } else if(id == R.id.nav_map) {
            fragmentManager.beginTransaction().replace(R.id.content_main, mCampusMapFragment).commit();
        } else if(id == R.id.nav_starred_locations) {
            fragmentManager.beginTransaction().replace(R.id.content_main, starredLocationsFragment).commit();
        }

        drawer.closeDrawer(GravityCompat.START); //closes drawer after selection

        return true;
    }

    //if logout is clicked log user out and restart the main activity
    public void onLogoutClicked(View view) {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class); //starts the login page
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //request permissions for location
    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_ACCESS_FINE_LOCATION);
    }

    //checks location permissions
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //one permission is granted
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mCampusMapFragment.onRequestPermissionsResult(requestCode == REQ_ACCESS_FINE_LOCATION &&
                permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    //starts and binds the Location Service, which listens for location changes
    @Override
    public void startAndBindLS(ServiceConnection serviceConnection) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "startAndBindLS() called");
        getApplicationContext().startService(getLSIntent());
        bindLS(serviceConnection);
    }

    //only binds the Location Service
    @Override
    public void bindLS(ServiceConnection serviceConnection) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "bindLS() called");
        getApplicationContext().bindService(getLSIntent(), serviceConnection, 0);
    }

    //unbinds the Location Service
    @Override
    public void unbindLS(ServiceConnection serviceConnection) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "unbindLS() called");
        try {
            getApplicationContext().unbindService(serviceConnection);
        } catch (IllegalArgumentException e){

        }
    }

    //when either the add review or the open drawer button is clicked
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_review:
                mCampusMapFragment.onClick(v);
                break;
            case R.id.open_drawer:
                mCampusMapFragment.onClick(v);
                break;
        }
    }

    //gets the Location Service intent, which is a new intent created from this method
    private Intent getLSIntent() {
        return new Intent(getApplicationContext(), LocationService.class);
    }
}
