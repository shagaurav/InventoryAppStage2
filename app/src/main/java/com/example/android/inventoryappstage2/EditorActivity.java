package com.example.android.inventoryappstage2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.GameInventoryContract;

/**
 * Allows user to create a new game or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // EditText field to enter the games's name
    private EditText gameNameEditText;

    // EditText field to enter the game's price
    private EditText gamePriceEditText;

    // EditText field to enter the game's quantity
    private EditText gameQuantityEditText;

    // EditText field to enter the game's supplier name
    private EditText supplierNameEditText;

    // EditText field to enter the game's supplier contact
    private EditText supplierContactEditText;

    private FloatingActionButton fabPhoneButton;

    private boolean mGameHasChanged = false;

    // Increment Button
    private Button addQuantityButton;

    // Decrement button
    private Button subtractQuantityButton;

    private final static String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int CURRENT_GAME_LOADER = 0;
    private Uri mCurrentGameUri;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_editor );

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new game or editing an existing one.
        mCurrentGameUri = getIntent().getData();

        // Find all relevant views that we will need to read user input from
        gameNameEditText = findViewById( R.id.product_name );
        gamePriceEditText = findViewById( R.id.product_price );
        gameQuantityEditText = findViewById( R.id.product_quantity );
        supplierNameEditText = findViewById( R.id.product_supplier_name );
        supplierContactEditText = findViewById( R.id.supplier_contact );
        fabPhoneButton = findViewById( R.id.fab_phone );
        addQuantityButton = findViewById( R.id.increase_button );
        subtractQuantityButton = findViewById( R.id.decrease_button );

        // sets up the UI according to uri value
        if (mCurrentGameUri == null) {
            setTitle( R.string.add_a_game );
            invalidateOptionsMenu();
            fabPhoneButton.setVisibility( View.GONE );
            addQuantityButton.setVisibility( View.GONE );
            subtractQuantityButton.setVisibility( View.GONE );
        } else {
            setTitle( R.string.edit_games );
            getSupportLoaderManager().initLoader( CURRENT_GAME_LOADER, null, this );
        }


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        gameNameEditText.setOnTouchListener( mTouchListener );
        gamePriceEditText.setOnTouchListener( mTouchListener );
        gameQuantityEditText.setOnTouchListener( mTouchListener );
        supplierNameEditText.setOnTouchListener( mTouchListener );
        supplierContactEditText.setOnTouchListener( mTouchListener );

        // button to increase the quantity by one
        addQuantityButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt( gameQuantityEditText.getText().toString().trim() );
                quantity++;
                ContentValues values = new ContentValues();
                values.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY, String.valueOf( quantity ) );
                getContentResolver().update( mCurrentGameUri, values, null, null );
                gameQuantityEditText.setText( String.valueOf( quantity ) );
                Toast.makeText( EditorActivity.this, R.string.quantity_updated, Toast.LENGTH_SHORT ).show();
            }
        } );

        // button to decrease the quantity by one
        subtractQuantityButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt( gameQuantityEditText.getText().toString().trim() );
                if (quantity > 0) {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY, String.valueOf( quantity ) );
                    getContentResolver().update( mCurrentGameUri, values, null, null );
                    gameQuantityEditText.setText( String.valueOf( quantity ) );
                    Toast.makeText( EditorActivity.this, R.string.quantity_updated, Toast.LENGTH_SHORT ).show();
                } else
                    Toast.makeText( EditorActivity.this, R.string.all_units_deleted_msg,
                            Toast.LENGTH_SHORT ).show();
            }
        } );

        // button to make phone call to the supplier
        fabPhoneButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneIntent = new Intent( Intent.ACTION_DIAL );
                String phoneUri = "tel:" + supplierContactEditText.getText().toString().trim();
                phoneIntent.setData( Uri.parse( phoneUri ) );
                startActivity( phoneIntent );
            }
        } );
    }


    // Supplier of the game
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the gameHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGameHasChanged = true;
            return false;
        }
    };

    //Get user input from editor and save new game info into database.
    private void saveGame() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String gameString = gameNameEditText.getText().toString().trim();
        String gamePriceString = gamePriceEditText.getText().toString().trim();
        String gameQuantityString = gameQuantityEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierContactString = supplierContactEditText.getText().toString().trim();

        // closes the editor activity if no data is provided and save button is clicked
        if(mCurrentGameUri == null &&
                TextUtils.isEmpty(gameString) && TextUtils.isEmpty(gamePriceString)
                && TextUtils.isEmpty(gameQuantityString) && TextUtils.isEmpty(supplierNameString)
                && TextUtils.isEmpty(supplierContactString)){return;}

        // closes the editor activity if partial data is provided and save button is clicked
        if(mCurrentGameUri == null &&
                TextUtils.isEmpty(gameString) || TextUtils.isEmpty(gamePriceString)
                || TextUtils.isEmpty(gameQuantityString) || TextUtils.isEmpty(supplierNameString)
                || TextUtils.isEmpty(supplierContactString)){
            Toast.makeText(this, R.string.partial_input_message, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        ContentValues contentValues = new ContentValues();
        contentValues.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME, gameString );
        contentValues.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE, gamePriceString );
        contentValues.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY, gameQuantityString );
        contentValues.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME, supplierNameString );
        contentValues.put( GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER, supplierContactString );

        // inserts data into the database and returns the row id
        if(mCurrentGameUri == null){
            Uri newUri = getContentResolver().insert(GameInventoryContract.GameInventoryEntry.CONTENT_URI, contentValues);
            if(newUri == null)
                Toast.makeText(this, getString(R.string.error_saving_games),
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.game_saved),
                        Toast.LENGTH_SHORT).show();
        } else {
            int rowsAffected = getContentResolver().update(mCurrentGameUri, contentValues,
                    null, null);
            if(rowsAffected == 0)
                Toast.makeText(this, getString(R.string.error_saving_game),
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.game_updated),
                        Toast.LENGTH_SHORT).show();
        }
    }

    // inflates overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate( R.menu.editor_menu, menu );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // User clicked on a menu option in the app bar overflow menu
        super.onPrepareOptionsMenu( menu );
        if (mCurrentGameUri == null) {
            MenuItem menuItem = menu.findItem( R.id.delete );
            menuItem.setVisible( false );
        }
        return true;
    }

    // handles overflow menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.done:
                // Save pet to database
                saveGame();
                finish();
                // Exit activity
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete:
                //Allow user to confirm for deleting the entry
                showDeleteConfirmationDialog();
                // Exit activity
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the game hasn't changed, continue with navigating up to parent activity
                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask( EditorActivity.this );
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask( EditorActivity.this );
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog( discardButtonClickListener );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    // shows dialog box for unsaved changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.unsaved_changes );
        builder.setPositiveButton( R.string.discard, discardButtonClickListener );
        builder.setNegativeButton( R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null)
                    dialog.dismiss();
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the entry hasn't changed, continue with handling back button press
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog( discardButtonClickListener );
    }

    // deletes the game
    private void deleteGame() {
        // Deletes the words that match the selection criteria
        if (mCurrentGameUri != null) {
            int rowsDeleted = getContentResolver().delete( mCurrentGameUri,
                    null, null );

            if (rowsDeleted == 0)
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText( this, R.string.delete_error_message,
                        Toast.LENGTH_SHORT ).show();
            else
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText( this, R.string.delete_message,
                        Toast.LENGTH_SHORT ).show();
        }
        finish();
    }

    // shows dialog box before deleting a game
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.delete_msg );
        builder.setPositiveButton( R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteGame();
            }
        } );
        builder.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        } );

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // creates and returns the cursor loader for loading the data from the database
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Since the editor shows all games attributes, define a projection that contains
        // all columns from the games table
        String[] projection = {
                GameInventoryContract.GameInventoryEntry._ID,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME,
                GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this,
                mCurrentGameUri,
                projection,
                null,
                null,
                null);
    }

    // populates the input fields with data
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1)
            return;

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            int nameIndex = data.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_NAME);
            int priceIndex = data.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_PRICE);
            int quantityIndex = data.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_QUANTITY);
            int supplierNameIndex = data.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME);
            int supplierPhoneIndex = data.getColumnIndex(GameInventoryContract.GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER);

            // Update the views on the screen with the values from the database
            gameNameEditText.setText( data.getString( nameIndex ) );
            gamePriceEditText.setText( data.getString( priceIndex ) );
            gameQuantityEditText.setText( data.getString( quantityIndex ) );
            supplierNameEditText.setText( data.getString( supplierNameIndex ) );
            supplierContactEditText.setText( data.getString( supplierPhoneIndex ) );
        }
    }

    // resets the input fields
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        gameNameEditText.setText( "" );
        gamePriceEditText.setText( "" );
        gameQuantityEditText.setText( "" );
        supplierNameEditText.setText( "" );
        supplierContactEditText.setText( "" );
    }
}