package edu.dartmouth.cs65.dartmouthnaps.models;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class MapPlace {
    public static MapPlace MP_BAKER_BERRY;
    public static MapPlace MP_SANBORN;
    public static MapPlace MP_CARPENTER;

    private PolygonOptions mPolygonOptions;
    private LatLng mAvgLatLng;
    private String mTag;
    private double[][] mCoordinates;

    public MapPlace(String tag, double[][] coordinates, int colorIdx) {
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
        mTag = tag;
        mCoordinates = coordinates;
    }

    public PolygonOptions getPolygonOptions() {
        return mPolygonOptions;
    }

    public LatLng getAvgLatLng() {
        return mAvgLatLng;
    }

    public String getTag() {
        return mTag;
    }

    public double[][] getCoordinates() {
        return mCoordinates;
    }

    public static void createConstants() {
        double[][] coordinates;

        MP_BAKER_BERRY = new MapPlace(MAP_PLACE_TAGS[0], new double[][]{
                {43.704881, -72.288981},
                {43.704875, -72.289151},
                {43.705067, -72.289156},
                {43.705070, -72.289204},
                {43.705178, -72.289205},
                {43.705178, -72.289158},
                {43.705387, -72.289166},
                {43.705388, -72.289029},
                {43.705752, -72.289034},
                {43.705888, -72.288857},
                {43.705799, -72.288246},
                {43.705680, -72.288276},
                {43.705691, -72.288405},
                {43.705617, -72.288401},
                {43.705616, -72.288429},
                {43.705395, -72.288425},
                {43.705395, -72.288280},
                {43.705212, -72.288279},
                {43.705210, -72.288258},
                {43.705094, -72.288252},
                {43.705096, -72.288271},
                {43.704897, -72.288267},
                {43.704894, -72.288436},
                {43.705088, -72.288443},
                {43.705084, -72.288675},
                {43.705063, -72.288677},
                {43.705063, -72.288782},
                {43.705077, -72.288783},
                {43.705075, -72.288984}}, 0);
        MP_SANBORN = new MapPlace(MAP_PLACE_TAGS[1], new double[][]{
                {43.704797, -72.289024},
                {43.704700, -72.289022},
                {43.704699, -72.289426},
                {43.704877, -72.289426},
                {43.704880, -72.289249},
                {43.704794, -72.289249}}, 1);
        MP_CARPENTER = new MapPlace(MAP_PLACE_TAGS[2], new double[][]{
                {43.705388, -72.289029},
                {43.705381, -72.289416},
                {43.705660, -72.289421},
                {43.705662, -72.289251},
                {43.705573, -72.289250},
                {43.705577, -72.289031}}, 1);
    }

    public static List<MapPlace> getConstants() {
        return new ArrayList<>(Arrays.asList(
                MP_BAKER_BERRY,
                MP_SANBORN,
                MP_CARPENTER));
    }
}
