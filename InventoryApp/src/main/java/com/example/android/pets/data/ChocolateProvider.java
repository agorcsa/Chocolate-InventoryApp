package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


/**
 * Created by ILEANA on 7/11/2017.
 */

public class ChocolateProvider extends ContentProvider {
    public static final int CHOCOLATES = 100;
    public static final int CHOCOLATE_ID = 101;
    public static final String LOG_TAG = ChocolateProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ChocolateContract.CONTENT_AUTHORITY, ChocolateContract.PATH_CHOCOLATE, CHOCOLATES);
        sUriMatcher.addURI(ChocolateContract.CONTENT_AUTHORITY, ChocolateContract.PATH_CHOCOLATE + "/#", CHOCOLATE_ID);
    }

    private ChocolateDbHelper mDbHelper;
    private Cursor cursor;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new ChocolateDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor = null;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CHOCOLATES:
                // For the CHOCOLATES code, query the chocolates table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the chocolates table.
                cursor = database.query(ChocolateContract.ChocolateEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CHOCOLATE_ID:
                selection = ChocolateContract.ChocolateEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the chocolate table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ChocolateContract.ChocolateEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHOCOLATES:
                return insertChocolate(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a chocolate into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertChocolate(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Chocolate requires a name");
        }
        String picture = values.getAsString(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PICTURE);
        if (picture == null) {
            throw new IllegalArgumentException("Chocolate requires a picture");
        }
        Integer quantity = values.getAsInteger(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Float price = values.getAsFloat(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Chocolate requires a valid price");
        }
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new chocolate with the given values
        long id = database.insert(ChocolateContract.ChocolateEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHOCOLATES:
                return updateChocolate(uri, contentValues, selection, selectionArgs);
            case CHOCOLATE_ID:
                selection = ChocolateContract.ChocolateEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateChocolate(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateChocolate(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME)) {
            String name = values.getAsString(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Chocolate requires a name");
            }
        }
        if (values.containsKey(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY)) {
            Integer quantity = values.getAsInteger(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY);
            if (quantity == null && quantity < 0) {
                throw new IllegalArgumentException("Quantity needs to be higher than 0");
            }
        }
        if (values.containsKey(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            Float price = values.getAsFloat(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Price must be higher than 0");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ChocolateContract.ChocolateEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;
        // Get writable database
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHOCOLATES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ChocolateContract.ChocolateEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHOCOLATE_ID:
                // Delete a single row given by the ID in the URI
                selection = ChocolateContract.ChocolateEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ChocolateContract.ChocolateEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                // Return the number of rows deleted
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHOCOLATES:
                return ChocolateContract.ChocolateEntry.CONTENT_LIST_TYPE;
            case CHOCOLATE_ID:
                return ChocolateContract.ChocolateEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

