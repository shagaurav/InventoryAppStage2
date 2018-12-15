package com.example.android.inventoryappstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryappstage2.data.GameInventoryContract;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    GameCursorAdapter gameCursorAdapter;

    /**
     * Identifier for the game data loader
     */
    private static final int GAME_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // button to add a new game
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the game data
        ListView gameListView = findViewById(R.id.list);

        // Find and set empty view on the ListView.
        gameListView.setEmptyView(findViewById(R.id.empty_view));

        // There is no game data yet (until the loader finishes) so pass in null for the Cursor.
        gameCursorAdapter = new GameCursorAdapter(this, null);
        gameListView.setAdapter( gameCursorAdapter );

        // Setup the item click listener
        gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(GameInventoryContract.GameInventoryEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        // triggers the cursor loader
        getSupportLoaderManager().initLoader( GAME_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()){
            case R.id.action_data:
                insertGame();
                return true;

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllGames();
        }
        return super.onOptionsItemSelected(item);
    }

    // inserts the dummy data into the database
    private void insertGame(){
        ContentValues values = new ContentValues();
        values.put(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME, "God of War");
        values.put(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE, 3999);
        values.put(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY, 2);
        values.put(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME, "Sony");
        values.put(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER, "0123456789");

        getContentResolver().insert(GameInventoryContract.GameInventoryEntry.CONTENT_URI, values);
    }

    // deletes all data from the database
    private void deleteAllGames() {
        getContentResolver().delete(GameInventoryContract.GameInventoryEntry.CONTENT_URI, null, null);
    }

    // creates and returns the loader for relevant datas to be displayed on CatalogActivity
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                GameInventoryContract.GameInventoryEntry._ID,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY
        };

        return new CursorLoader(this,
                GameInventoryContract.GameInventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    // swaps the data received by cursor loader with cursor adapter
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Update {@link gameCursorAdapter} with this new cursor containing updated game data
        gameCursorAdapter.swapCursor(data);
    }

    // resets the loader
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        gameCursorAdapter.swapCursor(null);
    }
}
