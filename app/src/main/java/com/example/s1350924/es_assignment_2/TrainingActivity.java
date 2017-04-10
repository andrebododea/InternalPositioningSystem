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
import android.graphics.Paint;
import android.graphics.Path;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.s1350924.es_assignment_2.R.id.fab_help;
import static com.example.s1350924.es_assignment_2.R.id.fab_next;


/*
 * This Activity is used to train the dataset. The user follows the dot along the path that they
 * drew in the previous activity, DrawActivity, at the speed of the animation - they must attempt to
 * keep up the same pace as the dot in order for scans to be as accurate as possible.
 *
 * As they follow the animation,  we use a WifiManager to access all current BSSIDs and signal strengths.
 * These are then passed to DatabaseHelper, which enters these points into the database. These will
 * be used later in the TrackingActivity to track the user as they move around the map.
 */



public class TrainingActivity extends Activity {

    static int currentAnimationIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */
        final Context mycontext = (Context)this;

        raiseExplanationDialogueBox(mycontext);

        FloatingActionButton fabnext = (FloatingActionButton) findViewById(fab_next);
        fabnext.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
        fabnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TrainingActivity.this, TrackingActivity.class);

                // Start new activity with this new intent
                TrainingActivity.this.startActivity(myIntent);

            }
        });

        // Help users can click if they forgot how to train
        FloatingActionButton question_button = (FloatingActionButton) findViewById(fab_help);
        question_button.setImageResource(R.drawable.ic_help_black_48dp);
        question_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseExplanationDialogueBox(mycontext);
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
                                Toast.LENGTH_SHORT).show();

                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    // Does all the drawing
    public static class animateRoute extends View {

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
        private ArrayList<Float> xCoords;
        private ArrayList<Float> yCoords;
        int numberOfInvalidations;

        // Three contructor overloads are required for views inflated from XML.
        // The first takes a Context argument
        // the second take a Context and an AttributeSet
        // the last one takes a Context, an AttributeSet, and an integer

        // First constructor
        public animateRoute(Context context) {
            super(context);
            init(context);
        }

        // The second constructor
        public animateRoute(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        // Third constructor
        public animateRoute(Context context, AttributeSet attrs, int lastarg) {
            super(context, attrs, lastarg);
            init(context);
        }


        // Initializes the class when constructor is called
        // Gets called from both constructors
        private void init(Context context) {

            // Get context and then the activity from that context
            this.context = context;
            Activity activity = (Activity) context;

            currentAnimationIndex = 0;


            // Get byte array that contains the blueprint bitmap from the intent
            // This was passed in from the DrawActivity activity
            byte[] byteArray = activity.getIntent().getByteArrayExtra("image");

            // Convert byte array to bitmap
            fleemingJenkin = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            // Get the coordinates passed in from the last activity
            xCoords = ( ArrayList<Float>) activity.getIntent().getSerializableExtra("Xpoints");
            yCoords = ( ArrayList<Float>) activity.getIntent().getSerializableExtra("Ypoints");

            // Path is stroked, red, 5dpi in diameter,
            // and the points of the path will be joined and rounded.
            paint.setAntiAlias(true);
            paint.setStrokeWidth(15f);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);


            // Test the table and print all entries.
            // WARNING - IF TABLE IS > 1000 ENTRIES THIS WILL CRASH THE APP
            /*
            DatabaseHelper db = new DatabaseHelper(context);
            String dbstring = db.getTableAsString();
            System.out.println(dbstring);
            */

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

            numberOfInvalidations = 0;
        }


        // When the screen is tapped, the animation begins
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            // Create handler for delay to draw out the animation and make it slower
            // and thus easier to follow as the walker

            // Prevent us going over the size of the actual number of input data points
            // Otherwise we could have many duplicate map points
            if(numberOfInvalidations <= xCoords.size()) {
                // Animate the green circle to move over the path
                delayIndexIncrease();
            }

            return true;
        }

        // Animate the green circle to move over the path of points
        // Waits a few hundred ms until moving to the next point, this makes it slow enough for
        // a human walker to keep pace
        private void delayIndexIncrease(){
            int numberOfIterations = xCoords.size()-1;
            int millisecondsPerFrame = 800; // 800 is walking pace
            int totalMilliseconds = millisecondsPerFrame * numberOfIterations;
            /*
             * TIMER
             * THIS IS WHERE THE DATABASE FUNCTIONS ARE CALLED
             * THIS IS WHERE THE DOT IS REDRAWN FOR ANIMATION
             * PROBABLY THE MOST IMPORTANT PART OF THE ACTIVITY
             */
            new CountDownTimer(totalMilliseconds, millisecondsPerFrame) {
                public void onTick(long millisUntilFinished) {
                    if(currentAnimationIndex == xCoords.size()-1){
                        Toast.makeText(context, "Touch the arrow to begin tracking yourself!",
                                Toast.LENGTH_SHORT).show();
                        cancel();
                    }else {
                        // Re-draw the onDraw() method, this moves the green dot to the next point
                        invalidate();

                        wifiScanAndDatabaseEntry();

                        // Increment the index of points on the training path
                        currentAnimationIndex++;
                    }
                }
                public void onFinish() {}

            }.start();
        }


        public void wifiScanAndDatabaseEntry(){
            WifiManager wifiManager  = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList = wifiManager.getScanResults();

            ArrayList<String> networkAddresses = new ArrayList<String>();
            ArrayList<Integer> signalStrengths = new ArrayList<Integer>();

           // Add the BSSIDs and signal strengths to the lists
            for (ScanResult scanResult : wifiList) {

                // The MAC address of the wireless access point (BSSID)
                // This is unique to each network access point
                networkAddresses.add(scanResult.BSSID);
          //      System.out.println("Network address: "+ scanResult.BSSID);

                // Network's signal level

          //    int level = WifiManager.calculateSignalLevel(scanResult.level, 50);
                signalStrengths.add(Math.abs(scanResult.level));
           //     System.out.println("Level is " + level + " out of 50");
            }

            // Get current coordinates
            float xc = xCoords.get(currentAnimationIndex);
            float yc = yCoords.get(currentAnimationIndex);

            // Pass the scan data to the DatabaseHelper in order to add into the database
            DatabaseHelper db = new DatabaseHelper(context);
            db.insertDataForSomePoint(xc, yc,networkAddresses, signalStrengths);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(fleemingJenkin, 0, 0, null);
            canvas.drawPath(path, paint);
            float xc = xCoords.get(currentAnimationIndex);
            float yc = yCoords.get(currentAnimationIndex);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GREEN);
            canvas.drawCircle(xc, yc, 30, paint );
            numberOfInvalidations++;
        }
    }
}
