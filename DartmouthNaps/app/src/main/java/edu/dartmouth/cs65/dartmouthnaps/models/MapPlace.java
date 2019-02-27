package edu.dartmouth.cs65.dartmouthnaps.models;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class MapPlace {
    private PolygonOptions mPolygonOptions;
    private LatLng mAvgLatLng;
    private double[][] mCoordinates;

    public MapPlace(double[][] coordinates, int colorIdx) {
        LatLng[] latLngs = new LatLng[coordinates.length];
        double latSum = 0;
        double lngSum = 0;

        for (int i = 0; i < coordinates.length; i++) {
            latLngs[i] = new LatLng(coordinates[i][0], coordinates[i][1]);
            latSum += coordinates[i][0];
            lngSum += coordinates[i][1];
        }

        mPolygonOptions = new PolygonOptions()
                .add(latLngs)
                .clickable(true)
                .fillColor(Color.parseColor(colorIdx < 0 || colorIdx >= POLYGON_COLORS.length ?
                        POLYGON_COLORS[0] : POLYGON_COLORS[colorIdx]));
        mAvgLatLng = new LatLng(latSum / coordinates.length, lngSum / coordinates.length);
        mCoordinates = coordinates;
    }

    public PolygonOptions getPolygonOptions() {
        return mPolygonOptions;
    }

    public LatLng getAvgLatLng() {
        return mAvgLatLng;
    }

    public double[][] getCoordinates() {
        return mCoordinates;
    }
}
