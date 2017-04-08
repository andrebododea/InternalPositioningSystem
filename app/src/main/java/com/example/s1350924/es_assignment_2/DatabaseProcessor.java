package com.example.s1350924.es_assignment_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DatabaseProcessor {

    private SQLiteDatabase mDatabase;
    private DatabaseHelper mSQLHelper;
    private Context mContext;

    public DatabaseProcessor(Context context) {
        mContext = context;
        mSQLHelper = DatabaseHelper.getInstance(mContext);
    }

    private void open() {
        mDatabase = mSQLHelper.getWritableDatabase();
    }

    private void close() {
        mDatabase.close();
    }

    public void createTable(){

    }


    // When we have recorded scans for some point and want to put that data into the database
    public void insertDataForSomePoint(double x, double y,
                                       ArrayList<String> BSSID_arr,
                                       ArrayList<Integer> signalStrength_arr) {

        ContentValues values = new ContentValues();

        // Cursor object will allow us to access the return values from our database
        // Use it as an interface with SQL queries
        Cursor res;

        open();

        res = mDatabase.rawQuery("SELECT * FROM pointTable", null);


        // Check to see if these coordinates already have an ID
        res = mDatabase.rawQuery("SELECT PointID FROM pointTable where xCoord=" + x + " AND yCoord=" + y, null);

        int pointIdValue;

        // If the point has already been recorded, do not do anything.
        // Do not want to overwrite data.
        if (res != null && !(res.moveToFirst()))  {
            // If these coordinates are new to the database:
            // Get the highest PointID so far. The PointID of the next point
            // in the table will be this value +1
            res = mDatabase.rawQuery("SELECT MAX(PointID) FROM pointTable", null);


            // This is the special case where the database is empty
            // Therefore initialise the first entry with a value of 0
            if(res == null && !(res.moveToFirst())){
                pointIdValue = 0;
            } else{
                // If the database is not empty, make the new index equal
                // to the highest index +1
                String maxStr = res.getString(0);
                int maxValue = Integer.parseInt(maxStr);
                pointIdValue = maxValue + 1;
            }



                /*
                 * Now that we have chosen an ID value, we can create the new database entry for the point
                 */

            // Access the database


            // Inserts as many rows as we have BSSID's for the given point
            // In TLG this will be between 20-40 entries
            for(int i = 0; i < BSSID_arr.size(); i++){
                // Put the ID value
                values.put("PointID", pointIdValue);
                // Put the x and y coordinate values
                values.put("xCoord", x);
                values.put("yCoord", y);
                // Put the MAC address (the BSSID)
                values.put("BSSID", BSSID_arr.get(i));
                // Put the signal strength value (out of 20) of that BSSID
                values.put("SignalStrength", signalStrength_arr.get(i));

                // Insert the new row, with all the data we just put
                mDatabase.insert("pointTable", null, values);
            }
        }
        close();
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

        open();

        // Return all the values
        Cursor res = mDatabase.rawQuery("select ",null);

        // While there are still more values in res
        // We will loop through all fields returned by the query above
        while(res.moveToNext()){

        }

        close();

        //
        return xyArr;

    }
}