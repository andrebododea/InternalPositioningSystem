package com.example.s1350924.es_assignment_2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * create custom DatabaseHelper class that extends SQLiteOpenHelper
 */

// Help from: http://stackoverflow.com/questions/6905524/using-singleton-design-pattern-for-sqlitedatabase
// Help also from: http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;

    private static final String DATABASE_NAME = "appDatabase.db";
    private static final String TABLE_NAME = "pointTable";
    private static final String COL_1 = "PointID";
    private static final String COL_2 = "xCoord";
    private static final String COL_3 = "yCoord";
    private static final String COL_4 = "BSSID";
    private static final String COL_5 = "SignalStrength";

    private static final int DATABASE_VERSION = 1;

    private Context mCtx;
    SQLiteDatabase db;


    public static DatabaseHelper getInstance(Context ctx) {
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.mCtx = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Command creates table with the three columns: COL_1, COL_2, COL_3
        db.execSQL("CREATE TABLE " + TABLE_NAME +" (" +COL_1+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +COL_2+", "+ COL_3+", "+COL_4+", " + COL_5+ " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // In case of upgrade of the table, simply delete the table
        db.execSQL("DROP TABLE IF EXISTS  " + TABLE_NAME);
        onCreate(db);
    }

}


