package com.example.android.inventoryappstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class GameProvider extends ContentProvider {

    /** URI matcher code for the content URI for the games table */
    private static final int GAMES = 100;

    /** URI matcher code for the content URI for a single game in the games table */
    private static final int GAME_ID = 101;

    private GameInventoryDbHelper mDbHelper;

    /** Tag for the log messages */
    public static final String LOG_TAG = GameProvider.class.getSimpleName();

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // types of uri to be used in the app
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(GameInventoryContract.CONTENT_AUTHORITY, GameInventoryContract.PATH_GAMES, GAMES );
        sUriMatcher.addURI(GameInventoryContract.CONTENT_AUTHORITY, GameInventoryContract.PATH_GAMES + "/#", GAME_ID );
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new GameInventoryDbHelper(getContext());
        return true;
    }

    // returns the type of uri on which operation is performed
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case GAMES:
                return GameInventoryContract.GameInventoryEntry.CONTENT_LIST_TYPE;
            case GAME_ID:
                return GameInventoryContract.GameInventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     * @return Cursor object depending on provided uri.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case GAMES:
                cursor = database.query(GameInventoryContract.GameInventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case GAME_ID:
                selection = GameInventoryContract.GameInventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(GameInventoryContract.GameInventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     * @return Uri appended with the id of row.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case GAMES:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // helper method to insert data into the database
    private Uri insertBook(Uri uri, ContentValues values) {

        String name = values.getAsString(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Game requires a name");
        }

        Integer price = values.getAsInteger(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE);
        if(price == null || price < 0){
            throw new IllegalArgumentException("Game requires a valid price.");
        }

        Integer quantity = values.getAsInteger(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY);
        if(quantity < 0){
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        String supplierName = values.getAsString(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME);
        if(supplierName == null){
            throw new IllegalArgumentException("Game requires a supplier name");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long newRowId = database.insert(GameInventoryContract.GameInventoryEntry.TABLE_NAME, null, values);

        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Perform the delete operation for the given URI. Use the given projection, selection, selection arguments.
     * @return rowsDeleted - number of rows deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case GAMES:
                rowsDeleted = database.delete(GameInventoryContract.GameInventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0)
                    getContext().getContentResolver().notifyChange(uri, null);
                return rowsDeleted;

            case GAME_ID:
                selection = GameInventoryContract.GameInventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(GameInventoryContract.GameInventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0)
                    getContext().getContentResolver().notifyChange(uri, null);
                return rowsDeleted;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Update the existing data into the provider with the given ContentValues.
     * @return rowsUpdated - number of rows updated
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case GAMES:
                return updateGame(uri, contentValues, selection, selectionArgs);

            case GAME_ID:
                selection = GameInventoryContract.GameInventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateGame(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // helper method to update the data
    private int updateGame(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(values.containsKey(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME)){
            String name = values.getAsString(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Game requires a name");
            }
        }

        if(values.containsKey(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE)){
            Integer price = values.getAsInteger(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE);
            if(price == null || price < 0){
                throw new IllegalArgumentException("Game requires a valid price.");
            }
        }

        if(values.containsKey(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY)){
            Integer quantity = values.getAsInteger(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY);
            if(quantity < 0){
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
        }

        if(values.containsKey(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME)){
            String supplierName = values.getAsString(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME);
            if(supplierName == null){
                throw new IllegalArgumentException("Book requires a supplier name");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(GameInventoryContract.GameInventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}

