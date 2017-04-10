/*
 *     Andre Bododea
 *     s1350924
 *     The University of Edinburgh
 *
 *
 * This class is where everything starts off.
 * Has some instructions for users.
 * Has a button that takes us directly to the Google Maps view (MapsActivity)
 *
 *
 */


package com.example.s1350924.es_assignment_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;



public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Internal Positioning System app");



        // First, request permissions
        requestPermissionsBox();

        final Activity activity = this;

        Context mycontext = (Context)activity;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(activity, MapsActivity.class);

                // Add extras to the Intent to pass to the next activity
                /*
                myIntent.putExtra("image",byteArray);

                // Add the two arrays with points
                myIntent.putExtra("Xpoints",xCoords);
                myIntent.putExtra("Ypoints",yCoords);
                */

                // Start new activity with this new intent
                activity.startActivity(myIntent);
            }
        });
    }




    /*
     * Checks permissions and allows the user to enable permissions
     * If permissions are not enabled by default, this can cause the app to crash or exhibit
     * undefined behaviour. Therefore we check permissions and ask the user to enable them as soon
     * as the onCreate() function is called. This prevents any of this unwanted behaviour.
     */
    public void requestPermissionsBox() {
        // Both variables for requesting permissions
        final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=20;
        final int MY_PERMISSIONS_REQUEST_FINE_LOCATION=30;

        // Here, MapsActivity is the current activity
        if (ContextCompat.checkSelfPermission(StartActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request the permission.
            ActivityCompat.requestPermissions(StartActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant.
            // The callback method gets the result of the request.
        }
    }
}
