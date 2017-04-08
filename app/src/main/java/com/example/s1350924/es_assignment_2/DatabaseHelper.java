package com.example.s1350924.es_assignment_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * create custom DatabaseHelper class that extends SQLiteOpenHelper
 */

// Help from: http://stackoverflow.com/questions/6905524/using-singleton-design-pattern-for-sqlitedatabase
// Help also from: http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;

    private static final String DATABASE_NAME = "appDatab ase.db";
    private static final String TABLE_NAME = "pointTable";
    private static final String COL_1 = "PointID";
    private static final String COL_2 = "xCoord";
    private static final String COL_3 = "yCoord";
    private static final String COL_4 = "BSSID";
    private static final String COL_5 = "SignalStrength";

    private static final int DATABASE_VERSION = 1;


    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Command creates table with the three columns: COL_1, COL_2, COL_3
        db.execSQL("CREATE TABLE " + TABLE_NAME +" (" +COL_1+", "
                +COL_2+", "+ COL_3+", "+COL_4+", " + COL_5+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // In case of upgrade of the table, simply delete the table
        db.execSQL("DROP TABLE IF EXISTS  " + TABLE_NAME);
        onCreate(db);
    }

    // When we have recorded scans for some point and want to put that data into the database
    public void insertDataForSomePoint(double x, double y,
                                       ArrayList<String> BSSID_arr,
                                       ArrayList<Integer> signalStrength_arr) {

        // Open the database
        SQLiteDatabase mDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Cursor object will allow us to access the return values from our database
        // Use it as an interface with SQL queries
        Cursor res;


        // Check to see if these coordinates already have an ID
        res = mDatabase.rawQuery("SELECT PointID FROM pointTable where xCoord=" + x + " AND yCoord=" + y, null);

        int pointIdValue;

        // If the point has already been recorded, do not do anything.
        // Do not want to overwrite data.
        int numberOfColsReturned = res.getColumnCount();
        System.out.println("Number of columns returned from first query: " +numberOfColsReturned);
        if (numberOfColsReturned==1 ) {

                // If these coordinates are new to the database:
                // Get the highest PointID so far. The PointID of the next point
                // in the table will be this value +1
                res = mDatabase.rawQuery("SELECT MAX(PointID) FROM pointTable", null);

                res.moveToFirst();
                // If the database is not empty, make the new index equal
                // to the highest index +1
                int maxValue = res.getInt(0);
                System.out.println("Max value of ID in the table is: " + maxValue);
                pointIdValue = maxValue + 1;


                /*
                 * Now that we have chosen an ID value, we can create the new database entry for the point
                 */

            // Access the database


            // Inserts as many rows as we have BSSID's for the given point
            // In TLG this will be between 20-40 entries
            for (int i = 0; i < BSSID_arr.size(); i++) {
                // Put the ID value
                values.put("PointID", pointIdValue);
                // Put the x and y coordinate values
                values.put("xCoord", x);
                values.put("yCoord", y);
                // Put the MAC address (the BSSID)
                values.put("BSSID", BSSID_arr.get(i));
                // Put the signal strength value (out of 20) of that BSSID
                values.put("SignalStrength", signalStrength_arr.get(i));

                if (i == 0)
                    System.out.println("PointID: " + pointIdValue + ", coords: " + x + ", " + y
                            + "; BSSID: " + BSSID_arr.get(i) + ", Signal Strength: " + signalStrength_arr.get(i));

                // Insert the new row, with all the data we just put
                mDatabase.insert("pointTable", null, values);
            }
        }

        // Close the database
        mDatabase.close();
    }

    // The function finds the nearest neighbours of the point passed in
    // Returns the x and y coordinates of the

    // CODE FOR ACCESSING THE VALUES WE NEED IN THE DATABASE
    public float[] returnNearestNeighbour(float x, float y,
                                          ArrayList<String> BSSID_arr,
                                          ArrayList<Integer>  signalStrength_arr) {

        // The first value will be x coordinate, the second value will be y coordinte
        // This array will be returned
        float[] xyArr = new float[2];

            /*
             * knnScores will store the k nearest neighbour scores of each set of x-y coordinates.
             * The index of each score in the ArrayList will match the PointID in the database.
             *
             * Once every score has been entered into the ArrayList, the highest one will be chosen
             * and the x and y coordinates of that point will be extracted from the database,
             * stored into the xyArr declared above, and returned.
             *
             * This point will be deemed the closest point to the user, and shown on the map as the
             * user's nearest trained location.
             */
        ArrayList<Integer> knnScores = new ArrayList<Integer>();

        // Open the database
        SQLiteDatabase mDatabase = this.getWritableDatabase();

        // Return all the values
        Cursor res = mDatabase.rawQuery("select ",null);

        // While there are still more values in res
        // We will loop through all fields returned by the query above
        while(res.moveToNext()){

        }

        // Close the database
        mDatabase.close();

        // Return the array containing x coordinate and y coordinate of the nearest recorded point
        return xyArr;

    }




}


