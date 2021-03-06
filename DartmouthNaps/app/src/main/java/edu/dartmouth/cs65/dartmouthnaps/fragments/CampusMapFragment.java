package edu.dartmouth.cs65.dartmouthnaps.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import edu.dartmouth.cs65.dartmouthnaps.R;
import edu.dartmouth.cs65.dartmouthnaps.activities.MainActivity;
import edu.dartmouth.cs65.dartmouthnaps.activities.NewReviewActivity;
import edu.dartmouth.cs65.dartmouthnaps.activities.SignupActivity;
import edu.dartmouth.cs65.dartmouthnaps.models.LatLng;
import edu.dartmouth.cs65.dartmouthnaps.models.Review;
import edu.dartmouth.cs65.dartmouthnaps.services.LocationService;
import edu.dartmouth.cs65.dartmouthnaps.tasks.AddPlacesToMapAT;
import edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;
import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

/*Written by the Dartmouth Naps Team*/
public class CampusMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "DartmouthNaps: CampusMapFragment"; //used for debugging
    private static final boolean DEBUG = true; //indicates if debugging

    private static final String TAG_CURRENT_LOCATION = "current location"; //current location tag
    private static final float ZOOM = 17; //map zoom level
    private static final String KEY_PERMISSIONS_GRANTED = "permissions granted";

    private static LSConnection mLSConnection; //LocationService connection

    public static LatLng sCurrentLocation = null;
    public static GoogleMap mGoogleMap;

    private CMFListener mCMFListener; //listener for the CampusMapFragment
    private Bitmap mCurrentLocationMarkerBitmap = null;
    private Marker mCurrentLocationMarker = null;
    private ReviewCardsContainerFragment reviewCardsContainerFragment;
    private Messenger mRecvMessenger;
    private Messenger mLSSendMessenger;
    private ImageButton imageButton;
    private Button addReviewButton;
    private boolean hidden;
    private Button mAddReviewBtn;
    private boolean mPermissionsGranted;
    private boolean mBindLSCalled;

    public CampusMapFragment() {
        // Required empty public constructor
    }

    public static CampusMapFragment newInstance(
            boolean permissionsGranted) {
        CampusMapFragment campusMapFragment = new CampusMapFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_PERMISSIONS_GRANTED, permissionsGranted);

        campusMapFragment.setArguments(args);
        return campusMapFragment;
    }

    @Override
    public void onAttach(Context context) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onAttach() called");

        super.onAttach(context);
        if (context instanceof CMFListener) {
            mCMFListener = (CMFListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onCreate() called");

        Context context = getContext();
        VectorDrawableCompat reviewMarkerVDC;
        Bundle args;
        int reviewMarkerWidth;
        int reviewMarkerHeight;

        super.onCreate(savedInstanceState);
        args = getArguments();

        if (args != null) {
            mPermissionsGranted = args.getBoolean(KEY_PERMISSIONS_GRANTED, false);
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

        mRecvMessenger = new Messenger(new CMFHandler());

        if (mLSConnection == null) mLSConnection = new LSConnection();

        mBindLSCalled = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout;
        SupportMapFragment mapFragment;

        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onCreateView() called");

        if (mCMFListener == null) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "mCMFListener currently null");
        }

        layout = inflater.inflate(R.layout.fragment_campus_map, container, false);
        (mapFragment = new SupportMapFragment()).getMapAsync(this);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.support_map_fragment_frame_layout, mapFragment).commit();

        if (mPermissionsGranted) {
            if (!LocationService.sIsRunning) mCMFListener.startAndBindLS(mLSConnection);
            else if (!LocationService.sIsBound) {
                if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "calling bindLS() from onCreateView()");
                mLSConnection = new LSConnection();
                mCMFListener.bindLS(mLSConnection);
                mBindLSCalled = true;
            }
        }

        mAddReviewBtn = layout.findViewById(R.id.add_review);

        reviewCardsContainerFragment = (ReviewCardsContainerFragment) getChildFragmentManager().findFragmentById(R.id.review_cards_container_fragment);
        imageButton = layout.findViewById(R.id.open_drawer);
        addReviewButton = layout.findViewById(R.id.add_review);

        if(hidden) {
            imageButton.setVisibility(View.GONE);
            addReviewButton.setText("Sign Up to Review");
            addReviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SignupActivity.class);
                    startActivity(intent);
                }
            });
        }

        return layout;
    }

    @Override
    public void onStart() {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onStart() called");

        super.onStart();

        // Bind if it should be done
        if (!LocationService.sIsBound && LocationService.sIsRunning && !mBindLSCalled) {
            if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "calling bindLS() from onStart()");
            mLSConnection = new LSConnection();
            mCMFListener.bindLS(mLSConnection);
        }
    }

    @Override
    public void onStop() {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onStop() called");

        // Unbind if it should be done
        if (LocationService.sIsBound) {
            mCMFListener.unbindLS(mLSConnection);
            mLSSendMessenger = null;
        }

        super.onStop();
    }

    @Override
    public void onDetach() {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onDetach() called");

        super.onDetach();
        mCMFListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "onMapReady() called");

        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnPolygonClickListener(this);
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setIndoorEnabled(false); // Indoor would be nice, but the only building in Hanover
        // with it that I can find is the Howe library, which isn't a Dartmouth building
        mGoogleMap.setMapStyle(new MapStyleOptions(CAMPUS_MAP_STYLE_JSON));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(PLACE_COORDINATES_AVG[1]).toGoogleLatLng(), ZOOM));
        new AddPlacesToMapAT().execute(mGoogleMap);
        MainActivity.sFirebaseDataSource.setGoogleMap(mGoogleMap);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        Object tag = polygon.getTag();

        Log.d(TAG, "tag: " + (tag == null ? "[null]" : tag.toString()));
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
            case R.id.open_drawer:
                MainActivity.drawer.openDrawer(GravityCompat.START);
                break;
        }
    }

    public void onRequestPermissionsResult(boolean result) {
        mPermissionsGranted = result;

        if (mPermissionsGranted) {
            if (!LocationService.sIsRunning) mCMFListener.startAndBindLS(mLSConnection);
            else if (!LocationService.sIsBound && !mBindLSCalled) {
                if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "calling bindLS() from onRequestPermissionsResult()");
                mLSConnection = new LSConnection();
                mCMFListener.bindLS(mLSConnection);
            }
        }
    }

    public void reviewPrompt(LatLng location) {
        if (mAddReviewBtn != null) {
            sCurrentLocation = location;
            onClick(mAddReviewBtn);
        }
    }

    public void showStarredReview(Review review) {
        reviewCardsContainerFragment.showStarredReview(review);
    }

    private void handleLocation(LatLng location) {
        sCurrentLocation = location;

        reviewCardsContainerFragment.calculateConveniences(sCurrentLocation.toLocation());

        if (mGoogleMap != null && mCurrentLocationMarkerBitmap != null) {
            if (mCurrentLocationMarker != null) mCurrentLocationMarker.remove();

            mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mCurrentLocationMarkerBitmap))
                    .position(sCurrentLocation.toGoogleLatLng())
                    .zIndex(1.0f));
            mCurrentLocationMarker.setTag(TAG_CURRENT_LOCATION);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    sCurrentLocation.toGoogleLatLng(), ZOOM));
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

    public interface CMFListener {
        void startAndBindLS(ServiceConnection serviceConnection);

        void bindLS(ServiceConnection serviceConnection);

        void unbindLS(ServiceConnection serviceConnection);
    }

    private class CMFHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SEND_LOCATION:
                    if (DEBUG_GLOBAL && DEBUG) Log.d(TAG, "MSG_WHAT_SEND_LOCATION received");

                    handleLocation((LatLng)msg.obj);
                    break;
            }
        }
    }

    private class LSConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Message msg;

            mBindLSCalled = false;

            // Initialize mLSSendMessenger to communicate with the Service
            mLSSendMessenger = new Messenger(service);

            // Try to send the Service a Messenger reference
            try {
                msg = Message.obtain(null, MSG_WHAT_SEND_MESSENGER);
                msg.replyTo = mRecvMessenger;
                mLSSendMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public void setHideButton(boolean bool) {
        hidden = bool;
    }


}