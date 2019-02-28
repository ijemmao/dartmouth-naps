package edu.dartmouth.cs65.dartmouthnaps.tasks;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceConstants.*;

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

        for (int p = 0; p < mPlaceCount; p++) {
            coordinates = PLACE_COORDINATES[p];
            latLngs = new LatLng[coordinates.length];

            for (int c = 0; c < coordinates.length; c++) {
                latLngs[c] = new LatLng(coordinates[c][0], coordinates[c][1]);
            }

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

        for (int p = 0; p < mPlaceCount; p++) {
            polygon = mGoogleMap.addPolygon(mPolygonOptionsArray[p]);
            polygon.setTag(PLACE_NAMES[p]);
        }
    }


}
