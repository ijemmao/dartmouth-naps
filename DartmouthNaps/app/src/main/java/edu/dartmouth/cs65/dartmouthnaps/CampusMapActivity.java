package edu.dartmouth.cs65.dartmouthnaps;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.dartmouth.cs65.dartmouthnaps.tasks.AddPlacesToMapAT;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class CampusMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolygonClickListener {
    private static final String TAG = "DartmouthNaps: CampusMapActivity";
    private static final LatLng LAT_LNG_DARTMOUTH = new LatLng(43.7044406,-72.2886935);
    private static final float ZOOM = 17;

    private GoogleMap mMap;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private String uID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        if(user == null) { //if no one is logged in, go to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else { //fetch the data from firebase and load it onto local database, if right afterlogging in
            uID = user.getUid();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) mapFragment.getMapAsync(this);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean mapStyleSet;

        mMap = googleMap;
        mMap.setOnPolygonClickListener(this);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false); // Indoor would be nice, but the only building in Hanover
        // with it that I can find is the Howe library, which isn't a Dartmouth building
        mapStyleSet = mMap.setMapStyle(new MapStyleOptions(CAMPUS_MAP_STYLE_JSON));
        Log.d(TAG, "mapStyleSet: " + (mapStyleSet ? "true" : "false"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LAT_LNG_DARTMOUTH, ZOOM));
        new AddPlacesToMapAT().execute(mMap);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        Object tag = polygon.getTag();
        Intent intent = new Intent(this,PlaceActivity.class);
        startActivity(intent);

        Log.d(TAG, "tag: " + (tag == null ? "[null]" : tag.toString()));
    }
}
