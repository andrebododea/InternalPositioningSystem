package com.example.s1350924.es_assignment_2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


// Implement location listener.
public class CurrentLocationListener implements LocationListener {

    public String coordinates;
    private static final String TAG = "ANDRE BIG TAG MANGGG";

    public double latitude;
    public double longitude;

    public double getCurrentLatitude(){
        return latitude;
    }

    public double getCurrentLongitude(){
        return longitude;
    }


    // This function is called whenever a location is changed
    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            coordinates = "Latitude = " + location.getLatitude() + " Longitude = " + location.getLongitude();

            Log.v(TAG, coordinates);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
