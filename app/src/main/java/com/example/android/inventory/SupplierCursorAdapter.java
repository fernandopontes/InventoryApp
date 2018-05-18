package com.example.android.inventory;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract;

public class SupplierCursorAdapter extends CursorAdapter {

    public SupplierCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_supplier_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.supplier_name);
        TextView emailTextView = view.findViewById(R.id.supplier_email);
        TextView phoneTextView = view.findViewById(R.id.supplier_phone);

        Button editBtn = view.findViewById(R.id.supplier_btn_edit);

        int idColumnIndex = cursor.getColumnIndex(InventoryContract.SuppliersEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME);
        int emailColumnIndex = cursor.getColumnIndex(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL);
        int phoneColumnIndex = cursor.getColumnIndex(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_TEL);

        final int prodId = cursor.getInt(idColumnIndex);
        String supplierName = cursor.getString(nameColumnIndex);
        String supplierEmail = cursor.getString(emailColumnIndex);
        String supplierPhone = cursor.getString(phoneColumnIndex);

        Resources res = context.getResources();

        nameTextView.setText(supplierName);
        emailTextView.setText(supplierEmail);
        phoneTextView.setText(supplierPhone);

        if(prodId == 1) {
            editBtn.setVisibility(View.GONE);
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(context, EditorSupplierActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.products/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(InventoryContract.SuppliersEntry.CONTENT_URI, prodId);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                context.startActivity(intent);
            }
        });
    }
}
