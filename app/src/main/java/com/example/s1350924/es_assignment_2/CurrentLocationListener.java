/*
 *     Andre Bododea
 *     s1350924
 *     The University of Edinburgh
 *
 *
 * Custom LocationListener class. It has getter methods to return the latitude and longitude
 *
 * This will be used to pinpoint the zoom on the current location. Useful for preparing the google
 * map for viewers so that they don't need to spend 10-15 seconds zooming into the map and adjusting
 * to encompass their current location.
 *
 */


package com.example.s1350924.es_assignment_2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


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

    /*
     * These
     */

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
