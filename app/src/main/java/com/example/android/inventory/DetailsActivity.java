package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;
import com.example.android.inventory.data.InventoryContract.ProductEntry;
import com.example.android.inventory.data.InventoryDbHelper;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** Adapter for the ListView */
    InventoryCursorAdapter mCursorAdapter;

    Button mProdBtnAdd;
    Button mProdBtnRem;
    Button mBtnEditProduct;
    Button mBtnBuySupplier;
    Button mBtnRemoveProduct;
    EditText amoutEditView;
    TextView amountTextView;
    int prodId = 0;
    String prodName;
    String prodAmount;
    int prodSupplierId = 0;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Otherwise this is an existing product, so change app bar to say "Edit Pet"
        setTitle(getString(R.string.activity_title_details_product));

        context = getBaseContext();
        amountTextView = findViewById(R.id.product_amount);
        amoutEditView = findViewById(R.id.details_product_text_amount);
        mProdBtnAdd = findViewById(R.id.details_product_btn_amount_add);
        mProdBtnRem = findViewById(R.id.details_product_btn_amount_remove);
        mBtnEditProduct = findViewById(R.id.btn_edit_product);
        mBtnBuySupplier = findViewById(R.id.btn_buy_from_supplier);
        mBtnRemoveProduct = findViewById(R.id.btn_remove_product);

        // Initialize a loader to read the product data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mProdBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int prodAmountS = Integer.valueOf(prodAmount);

                addStockProduct("add", prodAmountS, prodId, amoutEditView, amountTextView, context);

            }
        });

        mProdBtnRem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int prodAmountS = Integer.valueOf(prodAmount);

                if(prodAmountS > 0) {
                    addStockProduct("rem", prodAmountS, prodId, amoutEditView, amountTextView, context);
                } else {
                    Toast.makeText(context, context.getString(R.string.sell_update_amount_zeroed),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        mBtnEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, prodId);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });


        mBtnBuySupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailSupplier(prodSupplierId, prodName, context);
            }
        });

        mBtnRemoveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    public void addStockProduct(String action, int amount, int id, EditText amoutEditView, TextView amountTextView, Context context) {
        Uri currentPetUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, id);

        String amountEdString = amoutEditView.getText().toString().trim();

        if ( ! TextUtils.isEmpty(amountEdString)) {

            int amountEd = Integer.parseInt(amountEdString);
            int amountDb = 0;

            if(action == "add")
            {
                amountDb = amount + amountEd;
            } else {

                if(amountEd > amount) {
                    amountDb = amount;
                    Toast.makeText(context, context.getString(R.string.stock_smaller_amount_provided),
                            Toast.LENGTH_SHORT).show();
                } else {
                    amountDb = amount - amountEd;
                }
            }

            ContentValues values = new ContentValues();
            values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT, amountDb);

            int rowsAffected = context.getContentResolver().update(currentPetUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(context, context.getString(R.string.stock_update_amount_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Resources res = context.getResources();
                amountTextView.setText(String.format(res.getString(R.string.text_amount), String.valueOf(amountDb)));
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(context, context.getString(R.string.stock_update_amount_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.activity_amount_zeroed_product),
                    Toast.LENGTH_SHORT).show();
        }

    }

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

    public void sendEmailSupplier(int idSupplier, String prodName, Context context) {
        String emailSupplier = "";

        InventoryDbHelper mDbHelper = new InventoryDbHelper(getBaseContext());

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                InventoryContract.SuppliersEntry.TABLE_NAME,
                new String[] { InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL },
                InventoryContract.SuppliersEntry._ID + " = ?",
                new String[] { String.valueOf(idSupplier) },
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            do {
                emailSupplier = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        Resources res = this.getResources();

        String mailto = "mailto:" + emailSupplier;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailSupplier);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(res.getString(R.string.email_title_new_order), prodName));
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(res.getString(R.string.email_body_new_order), prodName));

        try {
            startActivity(Intent.createChooser(emailIntent, res.getString(R.string.email_title_send)));
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "Error email: " + e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
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
            ImageView imageView = findViewById(R.id.product_img);
            TextView nameTextView = findViewById(R.id.product_name);
            amountTextView = findViewById(R.id.product_amount);
            TextView valueTextView = findViewById(R.id.product_value);
            TextView descTextView = findViewById(R.id.product_desc);

            // Find the columns of product attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry._ID);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int amountColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AMOUNT);
            int valueColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_VALUE);
            int descColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);

            // Extract out the value from the Cursor for the given column index
            prodId = cursor.getInt(idColumnIndex);
            byte[] prodImage = cursor.getBlob(imageColumnIndex);
            prodSupplierId = cursor.getInt(supplierColumnIndex);
            prodName = cursor.getString(nameColumnIndex);
            prodAmount = cursor.getString(amountColumnIndex);
            String prodValue = cursor.getString(valueColumnIndex);
            String prodDesc = cursor.getString(descColumnIndex);

            Resources res = this.getResources();

            Bitmap bitmap = BitmapFactory.decodeByteArray(prodImage, 0, prodImage.length);
            imageView.setImageBitmap(bitmap);
            nameTextView.setText(prodName);
            amountTextView.setText(String.format(res.getString(R.string.text_amount), prodAmount));
            valueTextView.setText(String.format(res.getString(R.string.text_value), Utils.convertStringToReal(prodValue)));
            descTextView.setText(prodDesc);
            amoutEditView.setText("1");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        //mCursorAdapter.swapCursor(null);
    }
}
