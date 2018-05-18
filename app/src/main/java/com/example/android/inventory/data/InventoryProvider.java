package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final int SALES = 200;
    private static final int SALES_ID = 201;
    private static final int SUPPLIERS = 300;
    private static final int SUPPLIERS_ID = 301;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SALES, SALES);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SALES + "/#", SALES_ID);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SUPPLIERS, SUPPLIERS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SUPPLIERS + "/#", SUPPLIERS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIERS:
                cursor = database.query(InventoryContract.SuppliersEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIERS_ID:
                selection = InventoryContract.SuppliersEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryContract.SuppliersEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
             default:
                 throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.ProductEntry.CONTENT_ITEM_TYPE;
            case SALES:
                return InventoryContract.SalesEntry.CONTENT_LIST_TYPE;
            case SALES_ID:
                return InventoryContract.SalesEntry.CONTENT_ITEM_TYPE;
            case SUPPLIERS:
                return InventoryContract.SuppliersEntry.CONTENT_LIST_TYPE;
            case SUPPLIERS_ID:
                return InventoryContract.SuppliersEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            case SALES:
                return insertSale(uri, values);
            case SUPPLIERS:
                return insertSupplier(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        Integer supplier = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID);
        if (supplier == null) {
            throw new IllegalArgumentException("Product requires a supplier");
        }

        String image = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Product requires a image");
        }

        String name = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if(name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer amount = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT);
        if (amount != null && amount < 0) {
            throw new IllegalArgumentException("Product requires valid amount");
        }

        Float value = values.getAsFloat(InventoryContract.ProductEntry.COLUMN_PRODUCT_VALUE);
        if (value != null && value < 0) {
            throw new IllegalArgumentException("Product requires valid value");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.ProductEntry.TABLE_NAME, null, values);
        if(id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertSale(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.SalesEntry.TABLE_NAME, null, values);
        if(id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertSupplier(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME);
        if(name == null) {
            throw new IllegalArgumentException("Supplier requires a name");
        }

        String email = values.getAsString(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL);
        if(email == null) {
            throw new IllegalArgumentException("Supplier requires a email");
        }

        String phone = values.getAsString(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_TEL);
        if(phone == null) {
            throw new IllegalArgumentException("Supplier requires a phone");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.SuppliersEntry.TABLE_NAME, null, values);
        if(id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.SuppliersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIERS_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.SuppliersEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryContract.SuppliersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            case SUPPLIERS:
                return updateSupplier(uri, values, selection, selectionArgs);
            case SUPPLIERS_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.SuppliersEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSupplier(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID)) {
            Integer supplier = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID);
            if (supplier == null) {
                throw new IllegalArgumentException("Product requires a supplier");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT)) {
            Integer amount = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT);
            if (amount != null && amount < 0) {
                throw new IllegalArgumentException("Product requires valid amount");
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_VALUE)) {
            Float value = values.getAsFloat(InventoryContract.ProductEntry.COLUMN_PRODUCT_VALUE);
            if (value != null && value < 0) {
                throw new IllegalArgumentException("Product requires valid value");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    private int updateSupplier(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME)) {
            String name = values.getAsString(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Supplier requires a name");
            }
        }

        if (values.containsKey(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL)) {
            String email = values.getAsString(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Supplier requires a email");
            }
        }

        if (values.containsKey(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL)) {
            String phone = values.getAsString(InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL);
            if (phone == null) {
                throw new IllegalArgumentException("Supplier requires a phone");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.SuppliersEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }
}
