package edu.dartmouth.cs65.dartmouthnaps.models;

import android.location.Location;
import android.support.annotation.Nullable;

import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class LatLng {
    public double latitude;
    public double longitude;

    public LatLng() {
        // So that the review is placed in the Dartmouth area, a LatLng is set to Baker-Berry
        // Library by default
        latitude = PLACE_COORDINATES_AVG[1][LAT];
        longitude = PLACE_COORDINATES_AVG[1][LNG];
    }

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public com.google.android.gms.maps.model.LatLng toGoogleLatLng() {
        return new com.google.android.gms.maps.model.LatLng(latitude, longitude);
    }

    public Location toLocation() {
        Location location = new Location("");
        location.setLatitude(this.latitude);
        location.setLongitude(this.longitude);
        return location;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof LatLng)) return false;
        if (this.latitude != ((LatLng)obj).latitude) return false;
        if (this.longitude != ((LatLng)obj).longitude) return false;
        return true;
    }
}