package com.example.s1350924.es_assignment_2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.x;
import static android.R.attr.y;
import static com.example.s1350924.es_assignment_2.R.id.map_fab;

public class TrackingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // Map button
        FloatingActionButton mapFab = (FloatingActionButton) findViewById(map_fab);
        mapFab.setImageResource(android.R.drawable.ic_dialog_map);

        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(TrackingActivity.this, MapsActivity.class);

                // Start new activity with this new intent
                TrackingActivity.this.startActivity(myIntent);
            }
        });


    }




    // Give instructions to the user in a dialogue box
    // This is accessible the whole time the user is training via the question mark button
    private void raiseExplanationDialogueBox(final Context context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle("Training The Database");
        // set dialog message
        alertDialogBuilder
                .setMessage("Walk to the place in the building where the green dot is shown on the floor plan." +
                        "This is the starting location you have chosen on your training route. \n\n" +
                        "Once you are standing in this spot, touch anywhere on the screen to " +
                        "begin the training. The dot will start moving along the route, and you" +
                        " must do your best to keep pace with it as it moves along.\n\n" +
                        "The closer you keep pace with the dot, the more accurately the app will be able to" +
                        " track you as you move around the building once you have finished the training." )
                .setCancelable(false)
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(context, "Touch the screen anywhere to start training.",
                                Toast.LENGTH_LONG).show();

                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    // Does all the drawing
    public static class locationTracker extends View {

        Context context;
        /*
         * Array lists that will hold the x and y coordinates
         * These array lists will be stored alongside RSS values when training
         * so that we will be able to track user location on the floor plan when tracking
         */
        Handler mHandler;

        boolean drawingInProgress;

        private Paint paint = new Paint();
        private Path path = new Path();

        private Bitmap fleemingJenkin;


        // Three contructor overloads are required for views inflated from XML.
        // The first takes a Context argument
        // the second take a Context and an AttributeSet
        // the last one takes a Context, an AttributeSet, and an integer

        // First constructor
        public locationTracker(Context context) {
            super(context);
            init(context);
        }

        // The second constructor
        public locationTracker(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        // Third constructor
        public locationTracker(Context context, AttributeSet attrs, int lastarg) {
            super(context, attrs, lastarg);
            init(context);
        }


        // Initializes the class when constructor is called
        // Gets called from both constructors
        private void init(Context context) {

            // Get context and then the activity from that context
            this.context = context;
            Activity activity = (Activity) context;

            fleemingJenkin = scaleBitMapToScreenSize();

        }


        // When the screen is tapped, the animation begins
        @Override
        public boolean onTouchEvent(MotionEvent event) {


            return true;
        }




        @Override
        protected void onDraw(Canvas canvas) {

            // Test toast
            Toast.makeText(context, "Calling onDraw",Toast.LENGTH_SHORT).show();

            canvas.drawBitmap(fleemingJenkin, 0, 0, null);
            canvas.drawPath(path, paint);

            // Get nearest
            float[] xyArr =  nearestNeighbourCaller();
            float xc = xyArr[0];
            float yc = xyArr[1];
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GREEN);
            canvas.drawCircle(xc, yc, 30, paint );
        }


        // Method for getting Wifi Scan data for the current location, then interfacing with the
        // database via DatabaseHelper in order to find the nearest recorded point in the database.
        private float[] nearestNeighbourCaller(){
            // Get wifi scans of the current location
            WifiManager wifiManager  = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList = wifiManager.getScanResults();

            ArrayList<String> networkAddresses = new ArrayList<String>();
            ArrayList<Integer> signalStrengths = new ArrayList<Integer>();

            // Add the BSSIDs and corresponding signal strengths to the lists
            for (ScanResult scanResult : wifiList) {

                // The MAC address of the wireless access point (BSSID)
                // This is unique to each network access point
                networkAddresses.add(scanResult.BSSID);
                //      System.out.println("Network address: "+ scanResult.BSSID);

                // Network's signal level
                int level = WifiManager.calculateSignalLevel(scanResult.level, 20);
                signalStrengths.add(level);
                //     System.out.println("Level is " + level + " out of 50");
            }

            // Create a new DatabaseHelper object
            DatabaseHelper db = new DatabaseHelper(context);

            // Find the nearest point via the returnNearestNeighbour method within the DatabaseHelper
            float[] xyCoords = db.returnNearestNeighbour(x,y,networkAddresses,signalStrengths);
            return xyCoords;
        }


        private void drawAllRecordedPoints(){

            Activity activity = (Activity) context;

            // Initialise a database helper
            DatabaseHelper db = new DatabaseHelper(context);

            // Get all drawn data points in the database
            ArrayList<Float> xCoords = db.getAllXCoords();
            ArrayList<Float> yCoords = db.getAllYCoords();

            // Path is stroked, BLUE, 15dpi in diameter,
            // and the points of the path will be joined and rounded.
            paint.setAntiAlias(true);
            paint.setStrokeWidth(15f);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);

            // Start path
            if(xCoords.size() > 0 && yCoords.size()>0) {
                path.moveTo(xCoords.get(0), yCoords.get(0));
                for (int i = 1; i < xCoords.size(); i++) {
                    // Draw the next segment of the line
                    path.lineTo(xCoords.get(i), yCoords.get(i));
                }
            }else{
                Toast.makeText(activity, "No path data found. Please go back to the draw phase and try again.",
                        Toast.LENGTH_LONG).show();
            }
        }

        private void clearPath(){
            // Clear the old path
            path.reset();
        }




        private Bitmap scaleBitMapToScreenSize() {

            // Sets floor plan image to a Bitmap so that we can draw over it via Paint
            Bitmap immutable_bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.fleeming_jenkin_ground_floor);



            // This is the scaled bitmap, will be returned
            Bitmap s_Bitmap = null;

            // Use try/catch to get rid of StackTrace error uncaught warning
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getMetrics(metrics);

                int orig_width = immutable_bitmap.getWidth();
                System.out.println("Unscaled width is: " + orig_width);
                int orig_height = immutable_bitmap.getHeight();
                System.out.println("Unscaled height is: " + orig_height);

                float scaled_width = metrics.scaledDensity;
                System.out.println("Scaled width is: " + scaled_width);
                float scaled_height = metrics.scaledDensity;
                System.out.println("Scaled height is: " + scaled_height);

                // create a matrix for the manipulation
                Matrix matrix = new Matrix();
                // resize the bit map
                scaled_width = scaled_width * 0.15f;
                scaled_height = scaled_height * 0.15f;
                // Scale the matrix down
                matrix.postScale(scaled_width, scaled_height);
                // Rotate the matrix 90 degrees
                matrix.postRotate(90);

                // recreate the new Bitmap
                s_Bitmap = Bitmap.createBitmap(immutable_bitmap, 0, 0, orig_width, orig_height, matrix, true);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Gets rid of the "immutable bitmap passed to Canvas contructor" error
            // This is the immutable bitmap
            Bitmap floor_plan_bitmap = s_Bitmap.copy(Bitmap.Config.ARGB_8888, true);
            return floor_plan_bitmap;
        }



    }



}
