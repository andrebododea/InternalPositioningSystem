package com.example.s1350924.es_assignment_2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double mLat;
    private double mLong;

    private static final String TAG = "ANDRE BIG TAG MANGGG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // First, request permissions
        requestPermissionsBox();
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(mLat, mLong);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker at coordinates " +
                mLat + ", " + mLong));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
    public void globalPosition(){
        // Initialise a location manager
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

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
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request the permission.
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant.
            // The callback method gets the result of the request.
        }
    }
}
