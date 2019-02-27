package edu.dartmouth.cs65.dartmouthnaps;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.models.MapPlace;
import edu.dartmouth.cs65.dartmouthnaps.tasks.CreateMapPlaceConstantsAT;

public class CampusMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolygonClickListener {
    private static final String TAG = "DartmouthNaps: CampusMapActivity";
    private static final LatLng LAT_LNG_DARTMOUTH = new LatLng(43.7044406,-72.2886935);
    private static final float ZOOM = 17;

    private GoogleMap mMap;
    private List<MapPlace> mMapPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        mMapPlaces = null;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap = googleMap;
        mMap.setOnPolygonClickListener(this);
        mMap.setBuildingsEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LAT_LNG_DARTMOUTH, ZOOM));
        new CreateMapPlaceConstantsAT().execute(mMap);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
//        if (mMapPlaces == null) mMapPlaces = MapPlace.getConstants();

        Object tag = polygon.getTag();

        Log.d(TAG, "tag: " + (tag == null ? "[null]" : tag.toString()));
    }
}
