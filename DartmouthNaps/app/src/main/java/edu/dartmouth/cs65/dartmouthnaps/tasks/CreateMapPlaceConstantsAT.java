package edu.dartmouth.cs65.dartmouthnaps.tasks;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polygon;

import java.util.List;

import edu.dartmouth.cs65.dartmouthnaps.models.MapPlace;

public class CreateMapPlaceConstantsAT extends AsyncTask<GoogleMap, Void, Void> {
    private GoogleMap mGoogleMap;

    @Override
    protected Void doInBackground(GoogleMap... params) {
        mGoogleMap = params[0];
        MapPlace.createConstants();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        List<MapPlace> mapPlaces = MapPlace.getConstants();
        Polygon polygon;

        for (MapPlace mapPlace : mapPlaces) {
            polygon = mGoogleMap.addPolygon(mapPlace.getPolygonOptions());
            polygon.setTag(mapPlace.getTag());
//            polygon.setClickable(true);
        }
    }
}
