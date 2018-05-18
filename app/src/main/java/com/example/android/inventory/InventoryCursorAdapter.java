package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        ImageView imageView = view.findViewById(R.id.product_img);
        TextView nameTextView = view.findViewById(R.id.product_name);
        final TextView amountTextView = view.findViewById(R.id.product_amount);
        TextView valueTextView = view.findViewById(R.id.product_value);
        Button sellBtn = view.findViewById(R.id.product_btn_sell);
        Button detailsBtn = view.findViewById(R.id.product_btn_details);

        int idColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int amountColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT);
        int valueColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_VALUE);

        final int prodId = cursor.getInt(idColumnIndex);
        byte[] prodImage = cursor.getBlob(imageColumnIndex);
        String prodName = cursor.getString(nameColumnIndex);
        final String prodAmount = cursor.getString(amountColumnIndex);
        String prodValue = cursor.getString(valueColumnIndex);

        Resources res = context.getResources();

        Bitmap bitmap = BitmapFactory.decodeByteArray(prodImage, 0, prodImage.length);
        imageView.setImageBitmap(bitmap);
        nameTextView.setText(prodName);
        amountTextView.setText(String.format(res.getString(R.string.text_amount), prodAmount));
        valueTextView.setText(String.format(res.getString(R.string.text_value), Utils.convertStringToReal(prodValue)));

        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int prodAmountS = Integer.valueOf(prodAmount);

                if(prodAmountS > 0) {
                    sellProduct(prodAmountS, prodId, amountTextView, context);
                } else {
                    Toast.makeText(context, context.getString(R.string.sell_update_amount_zeroed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup the item click listener
        detailsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(context, DetailsActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.products/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, prodId);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                context.startActivity(intent);
            }
        });
    }

    public void sellProduct(int amount, int id, TextView amountTextView, Context context) {

        Uri currentPetUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, id);

        int amountDb = amount - 1;
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT, amountDb);

        int rowsAffected = context.getContentResolver().update(currentPetUri, values, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(context, context.getString(R.string.sell_update_amount_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Register sell
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            ContentValues valuesSell = new ContentValues();
            valuesSell.put(InventoryContract.SalesEntry.COLUMN_SALES_PRODUCT_ID, id);
            valuesSell.put(InventoryContract.SalesEntry.COLUMN_SALES_AMOUNT, 1);
            valuesSell.put(InventoryContract.SalesEntry.COLUMN_SUPP_DATE, String.valueOf(dateFormat.format(new Date())));

            context.getContentResolver().insert(InventoryContract.SalesEntry.CONTENT_URI, valuesSell);

            Resources res = context.getResources();
            amountTextView.setText(String.format(res.getString(R.string.text_amount), String.valueOf(amountDb)));
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(context, context.getString(R.string.sell_update_amount_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
