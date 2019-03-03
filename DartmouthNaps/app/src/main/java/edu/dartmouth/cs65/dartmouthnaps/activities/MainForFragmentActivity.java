package edu.dartmouth.cs65.dartmouthnaps.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.fragments.CampusMapFragment;
import edu.dartmouth.cs65.dartmouthnaps.util.FirebaseDataSource;

public class MainForFragmentActivity extends AppCompatActivity implements CampusMapFragment.CMFListener {
    private static final int REQ_ACCESS_FINE_LOCATION = 0;

    public static FirebaseDataSource sFirebaseDataSource;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private String uID;
    private CampusMapFragment mCampusMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean permissionsGranted;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_for_fragment);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        if(user == null) { //if none is logged in, go to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else { //fetch the data from firebase and load it onto local database, if right after logging in
            uID = user.getUid();
        }

        sFirebaseDataSource = new FirebaseDataSource(getApplicationContext());
        permissionsGranted = checkPermissions();
        mCampusMapFragment = CampusMapFragment.newInstance(permissionsGranted);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.campus_map_fragment_frame_layout, mCampusMapFragment)
                .commit();

        if (!permissionsGranted) requestPermissions();


    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_ACCESS_FINE_LOCATION);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mCampusMapFragment.onRequestPermissionsResult(requestCode == REQ_ACCESS_FINE_LOCATION &&
                permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void callInitializeFusedLocationProviderClient() {
        mCampusMapFragment.initializeFusedLocationProviderClient(this, getMainLooper());
    }
}
