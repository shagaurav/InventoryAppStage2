package com.example.android.inventoryappstage2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class GameInventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private GameInventoryContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_GAMES = "games";

    /**
     * Inner class that defines constant values for the games database table.
     */
    public static final class GameInventoryEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAMES );
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_GAMES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_GAMES;

        /**
         * Name of database table for games
         */
        public static final String TABLE_NAME = "games";

        /**
         * Unique ID number for the game (only for use in the database table).
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the Game.
         */
        public static final String COLUMN_GAME_NAME = "Product_Name";

        /**
         * Price of the Game.
         */
        public static final String COLUMN_GAME_PRICE = "Price";

        /**
         * Quantity of the Game.
         */
        public static final String COLUMN_GAME_QUANTITY = "Quantity";

        /**
         * Game supplier name of the Game.
         */
        public static final String COLUMN_GAME_SUPPLIER_NAME = "Supplier_Name";

        /**
         * Game supplier phone number of the Game.
         */
        public static final String COLUMN_GAME_SUPPLIER_PHONE_NUMBER = "Supplier_Phone_Number";
    }
}
