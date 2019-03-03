package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import java.util.Locale;
import java.util.Vector;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainForFragmentActivity;
import edu.dartmouth.cs65.dartmouthnaps.tasks.AddPlacesToMapAT;
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.CAMPUS_MAP_STYLE_JSON;

public class CampusMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "DartmouthNaps: CampusMapFragment";
    private static final LatLng LAT_LNG_DARTMOUTH = new LatLng(43.7044406,-72.2886935);
    private static final float ZOOM = 17;
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PERMISSIONS_GRANTED = "permissions granted";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    private OnFragmentInteractionListener mListener;

    private CMFListener mCMFListener;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Looper mLooper;
    private boolean mPermissionsGranted;
    private Bitmap mReviewMarkerBitmap = null;
    private Marker mCurrentLocationMarker = null;

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

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment CampusMapFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static CampusMapFragment newInstance(String param1, String param2) {
//        CampusMapFragment fragment = new CampusMapFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getContext();
        VectorDrawableCompat reviewMarkerVDC;
        int reviewMarkerWidth;
        int reviewMarkerHeight;

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPermissionsGranted = getArguments().getBoolean(PERMISSIONS_GRANTED, false);
            Log.d(TAG, "mPermissionsGranted: " + mPermissionsGranted);
        }

        try {
            MapsInitializer.initialize(context);
            reviewMarkerVDC = VectorDrawableCompat.create(getResources(), R.drawable.ic_marker_bed, context.getTheme());
            reviewMarkerWidth = reviewMarkerVDC.getIntrinsicWidth();
            reviewMarkerHeight = reviewMarkerVDC.getIntrinsicHeight();
            reviewMarkerVDC.setBounds(0, 0, reviewMarkerWidth, reviewMarkerHeight);
            mReviewMarkerBitmap = Bitmap.createBitmap(reviewMarkerWidth, reviewMarkerHeight, Bitmap.Config.ARGB_8888);
            reviewMarkerVDC.draw(new Canvas(mReviewMarkerBitmap));
//            mReviewMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_marker_bed);
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

        if (mCMFListener == null) Log.d(TAG, "mCMFListener currently null");
        else mCMFListener.callInitializeFusedLocationProviderClient();
        layout = inflater.inflate(R.layout.fragment_campus_map, container, false);
        (mapFragment = new SupportMapFragment()).getMapAsync(this);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.support_map_fragment_frame_layout, mapFragment).commit();

        if (mPermissionsGranted) requestLocationUpdates();

        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean mapStyleSet;

        mGoogleMap = googleMap;
        mGoogleMap.setOnPolygonClickListener(this);
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setIndoorEnabled(false); // Indoor would be nice, but the only building in Hanover
        // with it that I can find is the Howe library, which isn't a Dartmouth building
        mapStyleSet = mGoogleMap.setMapStyle(new MapStyleOptions(CAMPUS_MAP_STYLE_JSON));
        Log.d(TAG, "mapStyleSet: " + (mapStyleSet ? "true" : "false"));
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
        return false;
    }

    public interface CMFListener {
        void callInitializeFusedLocationProviderClient();
    }

    public void onRequestPermissionsResult(boolean result) {
        mPermissionsGranted = result;
        Log.d(TAG, "mPermissionsGranted: " + mPermissionsGranted);

        if (mPermissionsGranted) requestLocationUpdates();
    }

    public void initializeFusedLocationProviderClient(Activity activity, Looper looper) {
        mFusedLocationProviderClient = new FusedLocationProviderClient(activity);
        mLooper = looper;
    }

    private void requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates() called");
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
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    Location location = result.getLastLocation();

                    if (mGoogleMap != null && mReviewMarkerBitmap != null) {
                        if (mCurrentLocationMarker != null) mCurrentLocationMarker.remove();

                        mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(mReviewMarkerBitmap))
                                .position(locationToLatLng(location)));
                    }

                    int placeIndex = PlaceUtil.getPlaceIndex(new double[]
                            {location.getLatitude(), location.getLongitude()});
                    String additional = placeIndex == -1 ? " is outside all places" : " is inside " + PlaceUtil.PLACE_NAMES[placeIndex];
                    Log.d(TAG, "(" + location.getLatitude() + ", " + location.getLongitude() + ")" + additional);
                }
            }, mLooper);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException caught in requestLocationUpdates; mPermissionsGranted: " + mPermissionsGranted);
            e.printStackTrace();
        }
    }

    private static void logCirclePoints() {
        String str = "";
        for (int i = 0; i <= 8; i++) {
            str += String.format(Locale.getDefault(), "%.3f,%.3f ",
                    15 + 3 * Math.cos(Math.PI * i / 16),
                    13.5 - 3 * Math.sin(Math.PI * i / 16));
        }
        Log.d(TAG, "M" + str);
    }

    private static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
