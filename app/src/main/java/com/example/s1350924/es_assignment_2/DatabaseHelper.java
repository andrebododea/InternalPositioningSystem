/*
 *     Andre Bododea
 *     s1350924
 *     The University of Edinburgh
 *
 *
 *
 *
 * This class extends SQLiteOpenHelper and will be used to handle everything database related
 *
 * This includes creating the database, submitting entries to the database, and computing database
 * operations in order to find the nearest points to the user's current location.
 *
 * The database is of the form:
 *
 *          CoordTable

----------------------------------------------------------------------------------------------------
   PointID    |    X_coord    |   Y_coord    |       BSSID            |      Strength (out of 20)
----------------------------------------------------------------------------------------------------
     1             4.7            6.9            00:08:C7:1B:8C:02                79
----------------------------------------------------------------------------------------------------
     2              7              24            20:29:B1:1A:5A:03                67
----------------------------------------------------------------------------------------------------
     3             1.1             5.7           00:08:C7:1B:8C:02                36
----------------------------------------------------------------------------------------------------
     1             4.7             6.9           12:08:C7:1C:8B:02                25
----------------------------------------------------------------------------------------------------
     3             1.1             5.7           20:29:B1:1A:5A:03                88
----------------------------------------------------------------------------------------------------

 *
 * PointIDs do repeat themselves, as each point will have multiple BSSID results, each with their
 * corresponding strengths
 *
 * This is accounted for via SQL commands later on in the code when accessing these points.
 *
 * Important methods include:
 *
 * insertDataForSomePoint() - takes x,y coordinates, BSSID array, and signal strength array. This
 *                            method handles the insertion of new rows into the database
 *
 * returnNearestNeighbour() - takes the same as insertDataForSomePoint(), but returns the nearest x,y
 *                            coordinate set for the nearest recorded datapoint
 *
 *
 */


package com.example.s1350924.es_assignment_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;



public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;

    String TAG = "DatabaseHelper";

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
    // This will allow the user to insert a scan for a particular point as some row
    public void insertDataForSomePoint(double x, double y,
                                       ArrayList<String> BSSID_arr,
                                       ArrayList<Integer> signalStrength_arr) {

        System.out.println("BSSID ARR is of size: "+BSSID_arr.size()+"\n");
        System.out.println("signal strength ARR is of size: "+signalStrength_arr.size()+"\n");

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
        if (numberOfColsReturned==1 || numberOfColsReturned==0 ) {

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


        // Close cursor to prevent memory leaks
        res.close();
    }



    /*
     * The function finds the nearest neighbours of the point passed in
     * Returns the x and y coordinates of the
     * It allows us to access all points in the database and find the nearest one to us based on BSSID scans.
     */
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

        System.out.println("HIGHEST ID IS: "+ highestID);

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

            // Get the IDs of all the signal strengths in the database that match this BSSID

            // Get rid of reducedTable just in case something interrupted the loop and stopped the table from being deleted
            mDatabase.execSQL("DROP TABLE IF EXISTS reducedTable;");
            // Create a table with all the relevant BSSIDs to shrink the data set
            mDatabase.execSQL("CREATE TABLE reducedTable AS SELECT * FROM pointTable WHERE BSSID='" + currBSSID+"';");

            Cursor id = mDatabase.rawQuery("SELECT DISTINCT PointID FROM reducedTable WHERE BSSID='" + currBSSID+"'",null );
            System.out.println("Entries from id query is: "+id.getCount());

            // Get the first ID point from the cursor
            if(id.moveToFirst() && id.getCount() > 0){

                // Iterate through all the resulting IDs, and get the matching SignalStrength for that BSSID
                do{
                    int idStr = id.getInt(0);
                   // System.out.println("The matched IDs are: "+idStr+"\n");

                    // Use reduced table to quicken the operation
                    Cursor strengths = mDatabase.rawQuery("SELECT SignalStrength FROM reducedTable WHERE PointID="+idStr +" AND BSSID= '"
                            + currBSSID+"'",null );

                    strengths.moveToFirst();

                    int dbSigStrength = strengths.getInt(0);

                    int strengthDifference = Math.abs(Math.abs(dbSigStrength) -  Math.abs(currStrength));
               //     System.out.println("strength diff : "+Math.abs(dbSigStrength)+" - "+ Math.abs(currStrength)+" = "+strengthDifference+"\n");

                    // If there are not yet values added into the difference array

                    // Add the new strength difference to the existing strength difference for that
                    // point. This will be averaged after all differences are counted, using
                    // the corresponding value from numberOfMatchingBSSIDs
                    int newVal = distDifferences.get(idStr) +strengthDifference;

                    distDifferences.set(idStr,newVal);

                    // Increment the number of matching BSSIDs.
                    // Only difference scores with a high enough number of matched BSSIDs will be considered
                    // This is to prevent the situation where one or two scans match very well, however there
                    // is a better match somewhere
                    newVal = numberOfMatchingBSSIDs.get(idStr)+1;
                    numberOfMatchingBSSIDs.set(idStr,newVal);

                    // Close the cursor to prevent memory leaks
                    strengths.close();


                } while(id.moveToNext());
            }
            // Delete the reduced table
            mDatabase.execSQL("DROP TABLE reducedTable;");

            // Close the cursor to prevent memory leaks
            id.close();
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

        // int matchThreshold = 1;
        int matchThreshold = 1;
        int closestPointID = -1;
        double lowestAvgDistance = -1.0;

        // Iterate through all matched data from the database above
        for(int k = 1; k < numberOfMatchingBSSIDs.size(); k++){
            int numOfMatches = numberOfMatchingBSSIDs.get(k);
            // Ensure that we exceed the match threshold number
            if(numOfMatches >= matchThreshold){

                // Compute the avgDist
                double avgDist = distDifferences.get(k)/numOfMatches;

             //  System.out.println("AVG Dist of this point is: " + avgDist + ", with "+ numOfMatches +" matches and pointId=" + k+", and lowest recorded avg Distance is: "+lowestAvgDistance+ " at point "+closestPointID);

                if(avgDist != 0.0) {
                    // If we do not yet have a value, simply add this one in
                    if (lowestAvgDistance == -1.0) {
                        lowestAvgDistance = avgDist;
                        closestPointID = k;
                    } else {
                        // Otherwise, only replace the lowestAvgDistance with the current avgDist if
                        // avgDist is lower
                        if (avgDist <= lowestAvgDistance) {
                            lowestAvgDistance = avgDist;
                            closestPointID = k;
                        }
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



        // Cover the case where no points were found
        if(closestPointID == -1){
            xyArr[0] = -1;
            xyArr[1] = -1;
        }else{
            // Search the database for the correct xCoord
            res = mDatabase.rawQuery("SELECT xCoord FROM pointTable WHERE PointID=" + closestPointID,null );
            // Get the xCoord from the cursor
            res.moveToFirst();
            xyArr[0] = res.getInt(0);

            // Search the database for the correct yCoord
            res = mDatabase.rawQuery("SELECT yCoord FROM pointTable WHERE PointID=" + closestPointID,null );
            // Get the yCoord from the cursor
            res.moveToFirst();
            xyArr[1] = res.getInt(0);
        }

        // This is for debugging
        // System.out.println("PointID of chosen point is: "+ closestPointID);


        // Close the database
        mDatabase.close();

        // Close the cursor to prevent memory leaks
        res.close();



       //  System.out.println("The chosen point is at x= " + xyArr[0] + ", y= "+ xyArr[1]);

        // Return the array containing x coordinate and y coordinate of the nearest recorded point
        return xyArr;
    }


    public ArrayList<Float> getAllXCoords(){
        ArrayList<Float> xCoords = new ArrayList<Float>();

        // Open the database
        SQLiteDatabase mDatabase = this.getWritableDatabase();

        Cursor res = mDatabase.rawQuery("SELECT xCoord FROM(SELECT DISTINCT xCoord, yCoord FROM pointTable)",null);

        if(res.moveToFirst() && res.getCount() > 0) {
            do {
                float coord = res.getFloat(0);
                xCoords.add(coord);
            } while (res.moveToNext());
        }
        // Close the database
        mDatabase.close();

        // Close cursor to prevent memory leaks
        res.close();

        return xCoords;
    }


    public ArrayList<Float> getAllYCoords(){
        ArrayList<Float> yCoords = new ArrayList<Float>();

        // Open the database
        SQLiteDatabase mDatabase = this.getWritableDatabase();

        Cursor res = mDatabase.rawQuery("SELECT yCoord FROM(SELECT DISTINCT xCoord, yCoord FROM pointTable)",null);

        if(res.moveToFirst() && res.getCount() > 0) {
            do {
                float coord = res.getFloat(0);
                yCoords.add(coord);
            } while (res.moveToNext());
        }
        // Close the database
        mDatabase.close();

        // Close cursor to prevent memory leaks
        res.close();

        return yCoords;
    }

    // For debugging/printing the SQL table
    public String getTableAsString() {
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = "pointTable";
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }


}




