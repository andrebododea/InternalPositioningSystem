package com.example.s1350924.es_assignment_2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double mLat;
    private double mLong;

    private Marker myLocaysh;
    private Marker fleemingJenkinMarker;

    private static final String TAG = "ANDRE BIG TAG MANGGG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        // Refresh values for the GPS coordinates
        globalPosition();

        // Add a marker at current location and move the camera
        LatLng myLocation = new LatLng(mLat, mLong);

        myLocaysh = mMap.addMarker(new MarkerOptions().position(myLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .alpha(0.4f)
                .title("Your Location")
        );

        myLocaysh.showInfoWindow();

        // Set the map focus
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        // Set the zoom onto the current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLong), 15.0f));


        LatLng fleemingJenkin = new LatLng(55.922413, -3.172393);
        fleemingJenkinMarker = mMap.addMarker(new MarkerOptions().position(fleemingJenkin).title("Marker at coordinates " +
                mLat + ", " + mLong)
                .title("Fleeming Jenkin Building, KB")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        fleemingJenkinMarker.showInfoWindow();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(fleemingJenkinMarker)) { // if marker source is clicked
                    Intent myIntent = new Intent(MapsActivity.this, DrawActivity.class);
                    MapsActivity.this.startActivity(myIntent);
                }

                if (marker.equals(myLocaysh)) { // if marker source is clicked
                    myLocaysh.showInfoWindow();
                }

                return true;
            }
        });
    }

    public boolean onMarkerClick(final Marker marker) {

        if (marker.equals(fleemingJenkinMarker)) {

        }
        return true;
    }


    /*
     * Calculate values of GPS coordinates, and updates the global variables mLat and mLong.
     * mLat is the current latitude position, and mLong is the current longitude position
     * and whenever we enter the program, in onCreate(), we call globalPosition() in order
     * to update these with the last known position
     *
     * This provides a "ballpark" GPS location of where we are, and then we can go on to fine-tune
     * this location as we move around indoors by means of wifi triangulation
     */
    public void globalPosition() {
        // Initialise a location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if have the necessary permissions (Fine location for GPS)
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // try with network provider
            Location networkLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // If it worked, use it
            if (networkLocation != null) {
                mLat = networkLocation.getLatitude();
                mLong = networkLocation.getLongitude();
                // networkLocation.getAltitude();
            }
            // Otherwise, use the GPS location via GPS provider
            else {
                // Instantiate a CurrentLocationListener
                CurrentLocationListener locationListener = new CurrentLocationListener();

                // Passes the current location to the CurrentLocationListener function
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                mLat = locationListener.getCurrentLatitude();
                mLong = locationListener.getCurrentLatitude();
            }
        }
    }
}

