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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.example.s1350924.es_assignment_2.R.id.fab_help;


/*
 * This activity allows the user to draw a path with their finger. The user will then need to walk
 * this path in order to train the dataset.
 *
 * In order to draw the path we extend the View object. We use a bitmap of our floorplan as our background
 * and use a Path object to draw our path. This path connects the points drawn by the user's finger on
 * the phone screen. We use a path because it's smoother than just drawing the points, which can be unevenly
 * spread.
 *
 * We also use dialogue boxes and Toasts to provide instruction to the user as they go along. These are
 * helpful at prompting the user as they use the app, and even just in case they forget how to use it.
 *
 * If the "question mark" button is pressed, we bring up the dialogue box with instructions.
 *
 * If the "forward arrow" button is pressed, we bring up the next activity, TrainingActivity.
 */

public class DrawActivity extends Activity {

    boolean proceedWithDrawing;
    Path finalPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_starting);
        setContentView(R.layout.activity_draw);

        final Context mycontext = (Context)this;

        raiseExplanationDialogueBox(mycontext);

        // Help: users can click if they forgot how to train and need instructions
        FloatingActionButton question_button = (FloatingActionButton) findViewById(fab_help);
        question_button.setImageResource(R.drawable.ic_help_black_48dp);
        question_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseExplanationDialogueBox(mycontext);
            }
        });
    }


    private void raiseExplanationDialogueBox(Context context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle("Training The Database");
        // set dialog message
        alertDialogBuilder
                .setMessage("This phase will present you with a floor plan of your current building. "
                        +"In order to train the app so that it can track your location in this building, "
                        +"you will first need to draw a \"training route\" on the map and then walk the route.\n\n"
                        +"In this phase you will be drawing your route. Please choose a route that is possible: "
                        +"e.g. not walking through walls or other obstacles. Draw slowly and as accurately as possible. \n\n"
                        + "Don't worry about making a mistake, you will be able to re-draw your route as many times "
                        + "as you need until you get it right. And remember to start at a point near you so that you "
                        + "don't need to walk to the other side of the building to start the walking phase!")
                .setCancelable(false)
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }


    // This class will take care of all the drawing
    public static class drawRoute extends View {
        Context context;
        /*
         * Array lists that will hold the x and y coordinates
         * These array lists will be stored alongside RSS values when training
         * so that we will be able to track user location on the floor plan when tracking
         */
        ArrayList<Float> xCoords = new ArrayList<Float>();
        ArrayList<Float> yCoords = new ArrayList<Float>();

        Bitmap floor_plan_bitmap;

        private Paint paint = new Paint();
        private Path path = new Path();

        // Three contructor overloads are required for views inflated from XML.
        // The first takes a Context argument
        // the second take a Context and an AttributeSet
        // the last one takes a Context, an AttributeSet, and an integer

        // First constructor
        public drawRoute(Context context) {
            super(context);
            init(context);
        }

        // The second constructor
        public drawRoute(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        // Third constructor
        public drawRoute(Context context, AttributeSet attrs, int lastarg) {
            super(context, attrs, lastarg);
            init(context);
        }


        // Initializes the class when constructor is called
        // Gets called from both constructors
        private void init(Context context) {

            this.context = context;

            // Sets floor plan image to a Bitmap so that we can draw over it via Paint
            Bitmap immutable_bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.fleeming_jenkin_ground_floor);

            immutable_bitmap = scaleBitMapToScreenSize(immutable_bitmap);

            // Gets rid of the "immutable bitmap passed to Canvas contructor" error
            // This is the immutable bitmap
            floor_plan_bitmap = immutable_bitmap.copy(Bitmap.Config.ARGB_8888, true);


            // Path is stroked, red, 5dpi in diameter,
            // and the points of the path will be joined and rounded.
            paint.setAntiAlias(true);
            paint.setStrokeWidth(15f);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);



        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(floor_plan_bitmap, 0, 0, null);
            // canvas.setBitmap(floor_plan_bitmap);
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Get the coordinates of the touch event
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                // When a finger touches down on the screen
                case MotionEvent.ACTION_DOWN:
                    // Add the coordinates to array lists
                    xCoords.add(eventX);
                    yCoords.add(eventY);
                    // Set a new starting point
                    path.moveTo(eventX, eventY);
                    return true;
                // When a finger moves around on the screen
                case MotionEvent.ACTION_MOVE:
                    xCoords.add(eventX);
                    yCoords.add(eventY);
                    // Connect the points
                    path.lineTo(eventX, eventY);
                    break;

                case MotionEvent.ACTION_UP:
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    // set title
                    alertDialogBuilder.setTitle("Would You Like To Accept This Path?");
                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Press yes to move on to the walking phase. \n\n" +
                                    "Press no if you would like to redraw your walking path.")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    // if this button is clicked, go on to the next activity
                                    Activity activity = (Activity) context;

                                    //Convert bitmap to byte array
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    floor_plan_bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    // Add bitmap byte array to intent
                                    Intent myIntent = new Intent(activity, TrainingActivity.class);
                                    myIntent.putExtra("image",byteArray);

                                    // Add the two arrays with points
                                    myIntent.putExtra("Xpoints",xCoords);
                                    myIntent.putExtra("Ypoints",yCoords);

                                    // Start new activity with this new intent
                                    activity.startActivity(myIntent);

                                    // Clear the old path
                                    path.reset();

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    path.reset();
                                    Toast toast = Toast.makeText(context,
                                            "Try again! Once you start drawing the old path will disappear."
                                            , Toast.LENGTH_SHORT);
                                    toast.show();

                                    xCoords = new ArrayList<Float>();
                                    yCoords = new ArrayList<Float>();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                    return false;

                default:
                    return false;
            }

            // Makes our view repaint and call onDraw
            invalidate();
            return true;
        }


        /*
         * This method takes in an unscaled bitmap, and scales it to the right size to fit on the screen
         * correctly.
         *
         * This bitmap will be scaled now and then passed on through the rest of the Activities
         * in order to be able to use if in the future when drawing on Views.
         */
        private Bitmap scaleBitMapToScreenSize(Bitmap unscaledBitmap) {

            // This is the scaled bitmap, will be returned
            Bitmap s_Bitmap = null;

            // Use try/catch to get rid of StackTrace error uncaught warning
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getMetrics(metrics);

                int orig_width = unscaledBitmap.getWidth();
                System.out.println("Unscaled width is: " + orig_width);
                int orig_height = unscaledBitmap.getHeight();
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
                s_Bitmap = Bitmap.createBitmap(unscaledBitmap, 0, 0, orig_width, orig_height, matrix, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return s_Bitmap;
        }

    }

}
