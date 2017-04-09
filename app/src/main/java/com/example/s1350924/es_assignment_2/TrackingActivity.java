package com.example.s1350924.es_assignment_2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.s1350924.es_assignment_2.TrainingActivity.currentAnimationIndex;

public class TrackingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
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

        int numberOfInvalidations;

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


            // Get byte array that contains the blueprint bitmap from the intent
            // This was passed in from the DrawActivity activity
            byte[] byteArray = activity.getIntent().getByteArrayExtra("image");

            // Convert byte array to bitmap
            fleemingJenkin = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        }


        // When the screen is tapped, the animation begins
        @Override
        public boolean onTouchEvent(MotionEvent event) {


            return true;
        }




        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(fleemingJenkin, 0, 0, null);
            canvas.drawPath(path, paint);
            float xc;
            float yc;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GREEN);
            canvas.drawCircle(xc, yc, 30, paint );
            numberOfInvalidations++;
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



    }



}
