package com.example.android.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;
import com.example.android.inventory.data.InventoryContract.ProductEntry;
import com.example.android.inventory.data.InventoryDbHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    final int REQUEST_CODE_GALLERY = 999;

    private ImageView mProductImage;
    private Button mProductBtnImage;
    private EditText mProductTextName;
    private EditText mProductTextAmount;
    private EditText mProductTextValue;
    private EditText mProductTextDesc;
    private Spinner mSupplierSpinner;

    private int mSupplier;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductImage = findViewById(R.id.product_view_image);
        mProductBtnImage = findViewById(R.id.product_btn_image);
        mProductTextName = findViewById(R.id.product_text_name);
        mProductTextAmount = findViewById(R.id.product_text_amount);
        mProductTextValue = findViewById(R.id.product_text_value);
        mProductTextDesc = findViewById(R.id.product_text_desc);
        mSupplierSpinner = findViewById(R.id.product_spinner_supplier);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductImage.setOnTouchListener(mTouchListener);
        mProductTextName.setOnTouchListener(mTouchListener);
        mProductTextAmount.setOnTouchListener(mTouchListener);
        mProductTextValue.setOnTouchListener(mTouchListener);
        mProductTextDesc.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);

        mProductBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        EditorActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the product.
     */
    private void setupSpinner() {

        List<String> supliersDb = getAllSuppliers();

        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, supliersDb);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    mSupplier = getIdSupplier(selection);
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = 1;
            }
        });
    }

    public List<String> getAllSuppliers() {
        List<String> supliers = new ArrayList<String>();

        InventoryDbHelper mDbHelper = new InventoryDbHelper(getBaseContext());

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + InventoryContract.SuppliersEntry.TABLE_NAME, null);

        if(cursor.moveToFirst()) {
            do {
                supliers.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        cursor.close();
        mDbHelper.close();

        return supliers;
    }

    public int getIdSupplier(String supplierName) {
        int id = 0;
        InventoryDbHelper mDbHelper = new InventoryDbHelper(getBaseContext());

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                InventoryContract.SuppliersEntry.TABLE_NAME,
                new String[] { InventoryContract.SuppliersEntry._ID },
                InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME + " = ?",
                new String[] { supplierName },
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        cursor.close();
        mDbHelper.close();

        return id;
    }

    public String getNameSupplier(int supplierId) {
        String name = "";
        InventoryDbHelper mDbHelper = new InventoryDbHelper(getBaseContext());

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                InventoryContract.SuppliersEntry.TABLE_NAME,
                new String[] { InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME },
                InventoryContract.SuppliersEntry._ID + " = ?",
                new String[] { String.valueOf(supplierId) },
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            do {
                name = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        cursor.close();
        mDbHelper.close();

        return name;
    }

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mProductTextName.getText().toString().trim();
        String amountString = mProductTextAmount.getText().toString().trim();
        String valueString = mProductTextValue.getText().toString().trim();
        String descString = mProductTextDesc.getText().toString().trim();
        Boolean invalid = false;

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.

            if(mProductImage.getDrawable() == null) {
                Toast.makeText(this, getString(R.string.editor_warning_imagem_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }

            if(TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.editor_warning_name_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }

            if(TextUtils.isEmpty(amountString)) {
                Toast.makeText(this, getString(R.string.editor_warning_amount_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }

            if(TextUtils.isEmpty(valueString)) {
                Toast.makeText(this, getString(R.string.editor_warning_value_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }

        } else {
            if(TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.editor_warning_name_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }

            if(TextUtils.isEmpty(amountString)) {
                Toast.makeText(this, getString(R.string.editor_warning_amount_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }

            if(TextUtils.isEmpty(valueString)) {
                Toast.makeText(this, getString(R.string.editor_warning_value_null),
                        Toast.LENGTH_SHORT).show();
                invalid = true;
            }
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        if (mCurrentProductUri != null) {
            if(mProductImage.getDrawable() != null) {
                values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, Utils.imageViewToByte(mProductImage));
            }
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, Utils.imageViewToByte(mProductImage));
        }
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID, mSupplier);
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descString);

        // If the amount is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int amount = 0;
        if (!TextUtils.isEmpty(amountString)) {
            amount = Integer.parseInt(amountString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_AMOUNT, amount);

        // If the value is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        float value = 0;
        if (!TextUtils.isEmpty(valueString)) {
            value = Float.parseFloat(valueString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_VALUE, value);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            if(invalid == false) {
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                    // Exit activity
                    finish();
                }
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            if(invalid == false) {
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                    // Exit activity
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_AMOUNT,
                ProductEntry.COLUMN_PRODUCT_VALUE,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int amountColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AMOUNT);
            int valueColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_VALUE);
            int descColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);

            // Extract out the value from the Cursor for the given column index
            int supplier = cursor.getInt(supplierColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int amount = cursor.getInt(amountColumnIndex);
            float value = cursor.getFloat(valueColumnIndex);
            String desc = cursor.getString(descColumnIndex);

            // Update the views on the screen with the values from the database
            mProductTextName.setText(name);
            mProductTextAmount.setText(Integer.toString(amount));
            mProductTextValue.setText(Float.toString(value));
            mProductTextDesc.setText(desc);

            List<String> supliersDb = getAllSuppliers();

            // Create adapter for spinner. The list options are from the String array it will use
            // the spinner will use the default layout
            ArrayAdapter supplierSpinnerAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, supliersDb);

            // Apply the adapter to the spinner
            mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

            String supplierName = getNameSupplier(supplier);
            int spinnerPositon = supplierSpinnerAdapter.getPosition(supplierName);
            mSupplierSpinner.setSelection(spinnerPositon);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductTextName.setText("");
        mProductTextAmount.setText("");
        mProductTextValue.setText("");
        mProductTextDesc.setText("");
        mSupplierSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mProductImage.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
