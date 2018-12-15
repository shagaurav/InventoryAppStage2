package com.example.android.inventoryappstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryappstage2.data.GameInventoryContract.GameInventoryEntry;

/**
 * Database helper for app.
 */
public class GameInventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = GameInventoryDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "games.db";

    /**
     * Database version.
     */
    private static final int DATABASE_VERSION = 1;

    // Constructs a new instance
    public GameInventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the games table
        String CREATE_GAMES_TABLE_QUERY = "CREATE TABLE " + GameInventoryEntry.TABLE_NAME + " ("
                + GameInventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GameInventoryEntry.COLUMN_GAME_NAME + " TEXT NOT NULL, "
                + GameInventoryEntry.COLUMN_GAME_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + GameInventoryEntry.COLUMN_GAME_QUANTITY + " INTEGER DEFAULT 0, "
                + GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME + " TEXT, "
                + GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER + " Text);";

        // creates table
        db.execSQL(CREATE_GAMES_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
