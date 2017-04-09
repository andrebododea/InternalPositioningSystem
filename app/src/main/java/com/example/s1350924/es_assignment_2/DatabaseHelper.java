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
             * The ID of each score in the ArrayList in the database will be held in pointIdInDatabase.
             *
             * Once every score has been entered into the ArrayList, the highest one will be chosen
             * and the x and y coordinates of that point will be extracted from the database,
             * stored into the xyArr declared above, and returned.
             *
             * This point will be deemed the closest point to the user, and shown on the map as the
             * user's nearest trained location.
             */
        ArrayList<Integer> knnScores = new ArrayList<Integer>();
        ArrayList<Integer> pointIdInDatabase = new ArrayList<Integer>();

        // Open the database
        SQLiteDatabase mDatabase = this.getWritableDatabase();

        // The cursor that wil be used to access the database query results
        Cursor res;

        // Store
        ArrayList<Integer> distDifferences = new ArrayList<Integer>();
        ArrayList<Integer> numberOfMatchingBSSIDs = new ArrayList<Integer>();

        // Get the lowest point ID in the database
        res = mDatabase.rawQuery("SELECT MIN(PointID) FROM pointTable",null);
        res.moveToFirst();
        int lowestID =res.getInt(0);

        // Get the highest point ID in the database
        res = mDatabase.rawQuery("SELECT MAX(PointID) FROM pointTable",null);
        res.moveToFirst();
        int highestID =res.getInt(0);

        // Initialise the values to 0 for both arrays.
        // Must do this as values in the arrays will be incremented, therefore we start at 0
        for(int i = 0; i <= highestID; i++){
            distDifferences.add(i, 0);
            numberOfMatchingBSSIDs.add(i,0);
        }



        // Iterate through all the BSSIDs that we got as input to this method for our current point
        for(int i = 0; i <  BSSID_arr.size(); i++){
            String currBSSID = BSSID_arr.get(i);
            int currStrength = signalStrength_arr.get(i);

            // Get all the signal strengths in the database that match this BSSID
            Cursor id = mDatabase.rawQuery("SELECT PointID FROM pointTable WHERE BSSID=\'" + currBSSID+"\' ",null );

            // Get the first ID point from the cursor
            if(id.moveToFirst()){

                // Iterate through all the resulting IDs, and get the matching SignalStrength for that BSSID
                do{
                    int idStr = id.getInt(0);
                    Cursor strengths = mDatabase.rawQuery("SELECT SignalStrength FROM pointTable WHERE BSSID= \'"
                            + currBSSID+"\' AND PointID=\'"+idStr+"\'",null );
                    strengths.moveToFirst();
                    int dbSigStrength = strengths.getInt(0);

                    int strengthDifference = Math.abs(dbSigStrength -  currStrength);

                    // If there are not yet values added into the difference array

                    // Add the new strength difference to the existing strength difference for that
                    // point. This will be averaged after all differences are counted, using
                    // the corresponding value from numberOfMatchingBSSIDs
                    int newVal = distDifferences.get(idStr) +strengthDifference;
                    distDifferences.add(idStr,newVal);

                    // Increment the number of matching BSSIDs.
                    // Only difference scores with a high enough number of matched BSSIDs will be considered
                    // This is to prevent the situation where one or two scans match very well, however there
                    // is a better match somewhere
                    newVal = numberOfMatchingBSSIDs.get(idStr)+1;
                    numberOfMatchingBSSIDs.add(idStr,newVal);

                } while(id.moveToNext());
            }

        }


        /*
         * Use the two arrays:
         * Total differences for each ID point - distDifferences
         * Total number of matches for each ID point - numberOfMatchingBSSIDs
         *
         * First ensure the points where numberOfMatchingBSSIDs is at least at the threshold value
         * This is to ensure that we have enough matching sample BSSIDs for a candidate point.
         *
         * If this condition is met, then take the average distance difference.
         * Store the
         */

        int matchThreshold = 10;
        int closestPointID = -1;
        double lowestAvgDistance = -1.0;

        // Iterate through all matched data from the database above
        for(int i = 0; i < numberOfMatchingBSSIDs.size(); i++){
            int numOfMatches = numberOfMatchingBSSIDs.get(i);
            // Ensure that we exceed the match threshold number
            if(numOfMatches >= matchThreshold){
                // Compute the avgDist
                double avgDist = distDifferences.get(i)/numOfMatches;

                // If we do not yet have a value, simply add this one in
                if(lowestAvgDistance == -1.0){
                    lowestAvgDistance = avgDist;
                    closestPointID = i;
                }else{
                    // Otherwise, only replace the lowestAvgDistance with the current avgDist if
                    // avgDist is lower
                    if(avgDist < lowestAvgDistance){
                        lowestAvgDistance = avgDist;
                        closestPointID = i;
                    }
                }
            }
        }


        // Now that we have found the closestPointID, we go into the database one final
        // time, and extract the x-y coordinates for that point.
        // Place them into xyArr, x coordinate at index 0 and y coordinate at index 1, and return the array
        //
        // The special case where we found no matches, gives us -1, -1 for the x-y coordinates
        // This will translate to a message to users that says "Tracking currently unavailable,
        // you are outwith the trained range."



        // Case where no points were found
        if(closestPointID == -1){
            xyArr[0] = -1;
            xyArr[1] = -1;
        }else{
            // Search the database for the correct xCoord
            res = mDatabase.rawQuery("SELECT xCoord FROM pointTable WHERE PointID=\'" + closestPointID+"\'",null );
            // Get the xCoord from the cursor
            res.moveToFirst();
            xyArr[0] = res.getInt(0);

            // Search the database for the correct yCoord
            res = mDatabase.rawQuery("SELECT yCoord FROM pointTable WHERE PointID=\'" + closestPointID+"\'",null );
            // Get the yCoord from the cursor
            res.moveToFirst();
            xyArr[1] = res.getInt(0);
        }

        // Close the database
        mDatabase.close();

        // Return the array containing x coordinate and y coordinate of the nearest recorded point
        return xyArr;
    }


    public ArrayList<Float> getAllXCoords(){
        ArrayList<Float> xCoords = new ArrayList<Float>();
        return xCoords;
    }


    public ArrayList<Float> getAllYCoords(){
        ArrayList<Float> yCoords = new ArrayList<Float>();
        return yCoords;
    }
}


