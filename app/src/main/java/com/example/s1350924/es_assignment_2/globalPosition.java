package com.example.s1350924.es_assignment_2;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;


public class globalPosition extends Activity {

    private Location currentLocation;

    double mLat;
    double mLong;

    // When activity is resumed, listen to sensor
    protected void onResume(){
        super.onResume();
    }

    // When activity is paused, stop listening to the sensor
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public double getLatitude(){
        return mLat;

    }

    public double getLongitude(){
        return mLong;
    }
}
