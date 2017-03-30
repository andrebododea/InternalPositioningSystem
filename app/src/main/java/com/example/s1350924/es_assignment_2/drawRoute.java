package com.example.s1350924.es_assignment_2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by andrebododea on 3/30/17.
 *
 *
 */

public class drawRoute extends View {
    /*
     * Array lists that will hold the x and y coordinates
     * These array lists will be stored alongside RSS values when training
     * so that we will be able to track user location on the floor plan when tracking
     */
    ArrayList<Float> xCoords =new ArrayList<Float>();
    ArrayList<Float> yCoords =new ArrayList<Float>();

    Canvas mCanvas;
    boolean initial_draw_floorplan;
    Bitmap floor_plan_bitmap;

    private Paint paint = new Paint();
    private Path path = new Path();

    // Using the tutorial http://creative-punch.net/2014/03/make-basic-single-touch-drawing-app-android/
    public drawRoute(Context context, AttributeSet attrs) {
        super(context, attrs);

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

        canvas.drawBitmap(floor_plan_bitmap,0,0,null);
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
                // ENTER THE ALERT DIALOGUE : https://developer.android.com/guide/topics/ui/dialogs.html
                // Two buttons: ACCEPT or START AGAIN
                // if START AGAIN is chosen, wipe the two array lists and start again.
                //acceptDrawing mydrawing = acceptDrawing.newInstance();
               // mydrawing.context.getSupportFragmentManager(), "dialog");

                return false;

            default:
                return false;
        }

        // Makes our view repaint and call onDraw
        invalidate();
        return true;
    }


    private Bitmap scaleBitMapToScreenSize(Bitmap unscaledBitmap){

        // This is the scaled bitmap, will be returned
        Bitmap s_Bitmap=null;

        // Use try/catch to get rid of StackTrace error uncaught warning
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);

            int orig_width = unscaledBitmap.getWidth();
            System.out.println("Unscaled width is: "+ orig_width);
            int orig_height = unscaledBitmap.getHeight();
            System.out.println("Unscaled height is: "+ orig_height);

            float scaled_width = metrics.scaledDensity;
            System.out.println("Scaled width is: "+ scaled_width);
            float scaled_height = metrics.scaledDensity;
            System.out.println("Scaled height is: "+ scaled_height);

            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            scaled_width = scaled_width *0.15f;
            scaled_height = scaled_height *0.15f;
            // Scale the matrix down
            matrix.postScale(scaled_width, scaled_height);
            // Rotate the matrix 90 degrees
            matrix.postRotate(90);

            // recreate the new Bitmap
            s_Bitmap = Bitmap.createBitmap(unscaledBitmap, 0, 0, orig_width, orig_height, matrix, true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return s_Bitmap;
    }

}
