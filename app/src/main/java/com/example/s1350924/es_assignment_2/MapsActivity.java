/*
 *     Andre Bododea
 *     s1350924
 *     The University of Edinburgh
 *
 *
 * This activity makes use of the Google Maps API (v2).
 *
 * It is used to display a world map, overlayed with our desired floorplans. It also includes
 * a feature to access our current location, and a zoom in/zoom out button as well as pinch-to-zoom
 * capabilites.
 *
 * The user can click on any orange marker in order to access the training sequence for that given floor plan.
 * Alternatively the user can click the button "INSIDE VIEW" to go immeditely to the tracking screen
 * and avoid training the map if they have already done so or simply wish to use the tracking feature right now.
 */



package com.example.s1350924.es_assignment_2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.s1350924.es_assignment_2.R.id.map;



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
                .findFragmentById(map);

        mapFragment.getMapAsync(this);

        // Set the button
        Button button = (Button) findViewById(R.id.trackbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MapsActivity.this, TrackingActivity.class);

                // Start new activity with this new intent
                MapsActivity.this.startActivity(myIntent);
            }
        });

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

        // Set the wee blue dot to get current location
        try{
            mMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e) { System.out.println("You haven't added permissions."); }

        // Enable Zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Set level support for indoor view of buildings
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        // Enable the google maps compass
        mMap.getUiSettings().setCompassEnabled(true);

        /*
         * Add in the fleeming jenkin building floor plan to the map.
         * Scales a floor plan image to size and then overlays over the building on the map.
         */

        // Set coordinates for the building
       // LatLng fleemingJenkin = new LatLng(55.922428, -3.172451);
        LatLng fleemingJenkin = new LatLng(55.922434, -3.172458);

        // Add the floor plan overlayed onto the floor plan, using the scaled bitmap
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.fleeming_jenkin_ground_floor))
                .position(fleemingJenkin,1171f/15, 634f/20);


        // Add an overlay to the map, retaining a handle to the GroundOverlay object.
        // This is the final floor plan embedded into the map
        GroundOverlay imageOverlay = mMap.addGroundOverlay(newarkMap);
        // Rotate the image 237.5 degrees relative to true north
        imageOverlay.setBearing(237.5f);

        /*
         *   Add in the marker for the fleeming jenkin onto the map
         *   It will be orange and clickable
         */

        // Add in an orange marker for the fleeming jenkin building
        LatLng fjMarkerPos = new LatLng(55.922738, -3.172604);
        fleemingJenkinMarker = mMap.addMarker(new MarkerOptions().position(fjMarkerPos).title("Marker at coordinates " +
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
                return true;
            }
        });
    }

    public boolean onMarkerClick(final Marker marker) {

        if (marker.equals(fleemingJenkinMarker)) {
            Intent myIntent = new Intent(MapsActivity.this, DrawActivity.class);
            MapsActivity.this.startActivity(myIntent);
        }
        return true;
    }

    public void onLocationChanged(Location location){

        // Refresh values for the GPS coordinates
        globalPosition();

        // Add a marker at current location and move the camera
        LatLng myLocation = new LatLng(mLat, mLong);

        // Set the map focus
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        // Set the zoom onto the current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLong), 15.0f));

    }


    /*
     * Calculate values of current GPS coordinates, and updates the global variables mLat and mLong.
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

