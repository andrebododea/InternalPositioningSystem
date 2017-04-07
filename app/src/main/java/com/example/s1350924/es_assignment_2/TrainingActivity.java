package com.example.s1350924.es_assignment_2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.s1350924.es_assignment_2.R.id.fab_pause;

public class TrainingActivity extends Activity {

    ImageView stickman;

    ArrayList<Float> animationXCoords;
    ArrayList<Float> animationYCoords;
    static int currentAnimationIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */


        // Get the coordinates passed in from the last activity
        animationXCoords = ( ArrayList<Float>) getIntent().getSerializableExtra("Xpoints");
        animationYCoords = ( ArrayList<Float>) getIntent().getSerializableExtra("Ypoints");


        // Set the stickman ImageView to the first location of the path
        stickman = (ImageView) findViewById(R.id.stickman);
        if(animationXCoords != null && animationYCoords != null) {
            stickman.setX(animationXCoords.get(0));
            stickman.setY(animationYCoords.get(0));
        }

        FloatingActionButton fab_play_pause = (FloatingActionButton) findViewById(fab_pause);
        fab_play_pause.setImageResource(android.R.drawable.ic_media_pause);



        fab_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Should pause the training", Snackbar.LENGTH_LONG)
                        .setAction("Pause", null).show();
/*
                currentAnimationIndex = 1;
                currentlyAnimating = true;
                beginAnimationOfRoute();
                */
            }
        });
    }
/*
    public void beginAnimationOfRoute(){

        if(currentlyAnimating) {
            for (int i = currentAnimationIndex; i < animationXCoords.size()-1; i++) {
                currentAnimationIndex++;
                TranslateAnimation animation = new TranslateAnimation(animationXCoords.get(i), animationXCoords.get(i+1),
                        animationYCoords.get(i), animationYCoords.get(i+1));
                animation.setDuration(1000);
                animation.setFillAfter(true);
                stickman.startAnimation(animation);
            }
        }
    }
*/

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

            System.out.println("Number of invalidations is : "+numberOfInvalidations);
            System.out.println("Number of data points is : "+xCoords.size());

            return true;
        }

        // Animate the green circle to move over the path of points
        // Waits a few hundred ms until moving to the next point, this makes it slow enough for
        // a human walker to keep pace
        private void delayIndexIncrease(){
            int numberOfIterations = xCoords.size()-1;
            int millisecondsPerFrame = 300; // 800 is walking pace
            int totalMilliseconds = millisecondsPerFrame * numberOfIterations;
            new CountDownTimer(totalMilliseconds, millisecondsPerFrame) {
                public void onTick(long millisUntilFinished) {
                    invalidate();
                    if(currentAnimationIndex == xCoords.size()-1){
                        cancel();
                    }else {
                        currentAnimationIndex++;
                    }
                }
                public void onFinish() {}
            }.start();


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
