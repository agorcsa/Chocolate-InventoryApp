package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChocolateDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ChocolateDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "chocolate.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ChocolateDbHelper}.
     *
     * @param context of the app
     */
    public ChocolateDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the chocolate table
        String SQL_CREATE_CHOCOLATE_TABLE = "CREATE TABLE " + ChocolateContract.ChocolateEntry.TABLE_NAME + " ("
                + ChocolateContract.ChocolateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PICTURE + " TEXT, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME + " TEXT NOT NULL, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE + " REAL NOT NULL, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_PHONE + " TEXT NOT NULL, "
                + ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_EMAIL + " TEXT);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_CHOCOLATE_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}


