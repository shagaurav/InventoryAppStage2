package com.example.android.inventoryappstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.GameInventoryContract;
import com.example.android.inventoryappstage2.data.GameInventoryContract.GameInventoryEntry;

public class GameCursorAdapter extends CursorAdapter {

    private final static String LOG_TAG = GameCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link GameCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public GameCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the game data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current game can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView productNameTextView = view.findViewById(R.id.product_name);
        TextView productPriceTextView = view.findViewById(R.id.price);
        TextView productQuantityTextView = view.findViewById(R.id.quantity);
        Button saleButton = view.findViewById(R.id.sale_button);

        // Find the columns of game attributes that we're interested in
        int gameName = cursor.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME);
        int gamePrice = cursor.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE);
        int gameQuantity = cursor.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY);
        final long ID = cursor.getColumnIndex(GameInventoryContract.GameInventoryEntry._ID);

        // sets the data to the views
        productNameTextView.setText(cursor.getString(gameName));
        productPriceTextView.setText(cursor.getString(gamePrice));
        productQuantityTextView.setText(cursor.getString(gameQuantity));

        /*
         * Each list view item will have a "Sale" button
         * This "Sale" button has OnClickListener which will decrease the product quantity by one at a time.
         * Update is only carried out if quantity is greater than 0(i.e MIMINUM quantity is 0).
         */
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View parentRow = (View) view.getParent();
                ListView listView = (ListView) parentRow.getParent();
                //position is the position of button which starts from 0
                int position = listView.getPositionForView(parentRow);

                int keyColumnIndex = cursor.getColumnIndex(GameInventoryContract.GameInventoryEntry._ID);
                try {
                    cursor.moveToFirst();
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, "attempt failed to re-open an already-closed cursor object");
                    return;
                }

                long key = 0;

                //Gets the row ID or PRIMARY_KEY of database into key Int variable.
                for (int i = 1; i <= position + 1; i++) {
                    key = cursor.getLong(keyColumnIndex);
                    cursor.moveToNext();
                }

                //Moving the cursor to the position where the button was clicked.
                cursor.moveToPosition(position);
                int quantityColumnIndex = cursor.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY);

                //currentQuantity add
                String currentQuantity = cursor.getString(quantityColumnIndex);
                final int currentQuantityInt = Integer.parseInt(currentQuantity);

                //Quantity reduced by 1 and stored in newQuantityInt
                int newQuantityInt = currentQuantityInt - 1;
                Log.v(LOG_TAG, "Current gameQuantity is: " + currentQuantityInt + " and changing to: " + newQuantityInt);

                if (newQuantityInt >= 0) {
                    //if newQuantity is > 0 then decrease gameQuantity by 1
                    ContentValues values = new ContentValues();
                    values.put(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY, newQuantityInt);
                    Uri currentProductUri = ContentUris.withAppendedId(GameInventoryContract.GameInventoryEntry.CONTENT_URI, key);
                    context.getContentResolver().update(currentProductUri, values, null, null);
                } else
                    Toast.makeText(context, R.string.all_units_deleted_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
