package edu.dartmouth.cs65.dartmouthnaps.tasks;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class AddPlacesToMapAT extends AsyncTask<GoogleMap, Void, Void> {
    private static final String POLYGON_FILL_COLOR = "#98a5be";
    private static final String POLYGON_STROKE_COLOR = "#1d2c4d";
    private GoogleMap mGoogleMap;
    private PolygonOptions[] mPolygonOptionsArray;
    private int mPlaceCount;

    @Override
    protected Void doInBackground(GoogleMap... params) {
        mGoogleMap = params[0];
        mPlaceCount = PLACE_NAMES.length;
        mPolygonOptionsArray = new PolygonOptions[mPlaceCount];
        double[][] coordinates;
        LatLng[] latLngs;

        // For each place in the PLACE_COORDINATES array, create a Polygon
        for (int p = 0; p < mPlaceCount; p++) {
            coordinates = PLACE_COORDINATES[p];
            latLngs = new LatLng[coordinates.length];

            // For each coordinate in the Polygon, create the corresponding (Google) LatLng
            for (int c = 0; c < coordinates.length; c++) {
                latLngs[c] = new LatLng(coordinates[c][0], coordinates[c][1]);
            }

            // Set each cell in the PolygonOptionsArray to add to the GoogleMap on post execute
            mPolygonOptionsArray[p] = new PolygonOptions()
                    .add(latLngs)
                    .clickable(true)
                    .fillColor(Color.parseColor(POLYGON_FILL_COLOR))
                    .strokeColor(Color.parseColor(POLYGON_STROKE_COLOR))
                    .strokeWidth(5);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Polygon polygon;

        // Add all the polygons to the map through the PolygonOptions array
        for (int p = 0; p < mPlaceCount; p++) {
            polygon = mGoogleMap.addPolygon(mPolygonOptionsArray[p]);
            polygon.setTag(PLACE_NAMES[p]);
        }
    }


}
