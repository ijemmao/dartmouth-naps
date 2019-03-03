package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainForFragmentActivity;
import edu.dartmouth.cs65.dartmouthnaps.activities.NewReviewActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;
import edu.dartmouth.cs65.dartmouthnaps.tasks.AddPlacesToMapAT;
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.CAMPUS_MAP_STYLE_JSON;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.DEBUG_GLOBAL;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.KEY_LATITUDE;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.KEY_LONGITUDE;

public class CampusMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "DartmouthNaps: CampusMapFragment";
    private static final boolean DEBUG = true;

    private static final String TAG_CURRENT_LOCATION = "current location";
    private static final LatLng LAT_LNG_DARTMOUTH = new LatLng(43.7044406,-72.2886935);
    private static final float ZOOM = 17;
    private static final String PERMISSIONS_GRANTED = "permissions granted";

    private CMFListener mCMFListener;
    public static GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Looper mLooper;
    private boolean mPermissionsGranted;
    private Bitmap mCurrentLocationMarkerBitmap = null;
    private Marker mCurrentLocationMarker = null;
    public static Location sCurrentLocation = null;

    private ReviewCardsContainerFragment reviewCardsContainerFragment;

    public CampusMapFragment() {
        // Required empty public constructor
    }

    public static CampusMapFragment newInstance(boolean permissionsGranted) {
        CampusMapFragment campusMapFragment = new CampusMapFragment();
        Bundle args = new Bundle();
        args.putBoolean(PERMISSIONS_GRANTED, permissionsGranted);
        campusMapFragment.setArguments(args);
        return campusMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getContext();
        VectorDrawableCompat reviewMarkerVDC;
        int reviewMarkerWidth;
        int reviewMarkerHeight;

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPermissionsGranted = getArguments().getBoolean(PERMISSIONS_GRANTED, false);
        }

        try {
            MapsInitializer.initialize(context);
            reviewMarkerVDC = VectorDrawableCompat.create(getResources(), R.drawable.ic_marker_shoes, context.getTheme());
            reviewMarkerWidth = reviewMarkerVDC.getIntrinsicWidth();
            reviewMarkerHeight = reviewMarkerVDC.getIntrinsicHeight();
            reviewMarkerVDC.setBounds(0, 0, reviewMarkerWidth, reviewMarkerHeight);
            mCurrentLocationMarkerBitmap = Bitmap.createBitmap(reviewMarkerWidth, reviewMarkerHeight, Bitmap.Config.ARGB_8888);
            reviewMarkerVDC.draw(new Canvas(mCurrentLocationMarkerBitmap));
        } catch (Exception e) {
            Log.d(TAG, "Caught exception trying to create bitmap");
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View layout;
        SupportMapFragment mapFragment;

        if (mCMFListener == null) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "mCMFListener currently null");
        } else mCMFListener.callInitializeFusedLocationProviderClient();
        layout = inflater.inflate(R.layout.fragment_campus_map, container, false);
        (mapFragment = new SupportMapFragment()).getMapAsync(this);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.support_map_fragment_frame_layout, mapFragment).commit();

        if (mPermissionsGranted) requestLocationUpdates();

        reviewCardsContainerFragment = (ReviewCardsContainerFragment) getChildFragmentManager().findFragmentById(R.id.review_cards_container_fragment);

        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnPolygonClickListener(this);
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setIndoorEnabled(false); // Indoor would be nice, but the only building in Hanover
        // with it that I can find is the Howe library, which isn't a Dartmouth building
        mGoogleMap.setMapStyle(new MapStyleOptions(CAMPUS_MAP_STYLE_JSON));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LAT_LNG_DARTMOUTH, ZOOM));
        new AddPlacesToMapAT().execute(mGoogleMap);
        MainForFragmentActivity.sFirebaseDataSource.addReviewsChildEventListener(mGoogleMap);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        Object tag = polygon.getTag();

        Log.d(TAG, "tag: " + (tag == null ? "[null]" : tag.toString()));
    }

//      // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CMFListener) {
            mCMFListener = (CMFListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCMFListener = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();
        marker.getPosition();

        for (int i = 0; i < ReviewCardsContainerFragment.reviews.size(); i++) {
            if (marker.getPosition().latitude == ReviewCardsContainerFragment.reviews.get(i).getLocation().latitude
                    && marker.getPosition().longitude == ReviewCardsContainerFragment.reviews.get(i).getLocation().longitude) {
                reviewCardsContainerFragment.mPager.setCurrentItem(i);
                break;
            }
        }

        return tag != null && tag.toString().equals(TAG_CURRENT_LOCATION);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_review:
                Intent intent = new Intent(getContext(), NewReviewActivity.class);

                if (sCurrentLocation != null) {
                    intent.putExtra(KEY_LATITUDE, sCurrentLocation.getLatitude());
                    intent.putExtra(KEY_LONGITUDE, sCurrentLocation.getLongitude());
                }

                startActivity(intent);
                break;
        }
    }

    public interface CMFListener {
        void callInitializeFusedLocationProviderClient();
    }

    public void onRequestPermissionsResult(boolean result) {
        mPermissionsGranted = result;

        if (mPermissionsGranted) requestLocationUpdates();
    }

    public void initializeFusedLocationProviderClient(Activity activity, Looper looper) {
        mFusedLocationProviderClient = new FusedLocationProviderClient(activity);
        mLooper = looper;
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest;

        if (mFusedLocationProviderClient == null) {
            Log.d(TAG, "mFusedLocationProviderClient is currently null");
            return;
        }

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(5000);
        locationRequest.setInterval(10000);
        locationRequest.setSmallestDisplacement(5);

        try {
            mFusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    new CurrentLocationCallback(),
                    mLooper);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException caught in requestLocationUpdates; mPermissionsGranted: " + mPermissionsGranted);
            e.printStackTrace();
        }
    }

    private static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private class CurrentLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult result) {
            sCurrentLocation = result.getLastLocation();


            if (mGoogleMap != null && mCurrentLocationMarkerBitmap != null) {
                if (mCurrentLocationMarker != null) mCurrentLocationMarker.remove();

                mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(mCurrentLocationMarkerBitmap))
                        .position(locationToLatLng(sCurrentLocation)));
                mCurrentLocationMarker.setTag(TAG_CURRENT_LOCATION);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        locationToLatLng(sCurrentLocation), ZOOM));
            }

            if (DEBUG_GLOBAL && DEBUG) {
                int placeIndex = PlaceUtil.getPlaceIndex(new double[]
                        {sCurrentLocation.getLatitude(), sCurrentLocation.getLongitude()});
                String additional = placeIndex == -1 ?
                        " is outside all places" :
                        " is inside " + PlaceUtil.PLACE_NAMES[placeIndex];
                Log.d(TAG, "(" +
                        sCurrentLocation.getLatitude() + ", " +
                        sCurrentLocation.getLongitude() + ")" + additional);
            }
        }
    }
}
