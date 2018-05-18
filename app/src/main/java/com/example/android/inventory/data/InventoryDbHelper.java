package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    private static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " + InventoryContract.ProductEntry.TABLE_NAME + " (" +
                    InventoryContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID + " INTEGER NOT NULL, " +
                    InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB, " +
                    InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    InventoryContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + " INTEGER DEFAULT 0, " +
                    InventoryContract.ProductEntry.COLUMN_PRODUCT_VALUE + " REAL DEFAULT 0, " +
                    InventoryContract.ProductEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT);";

    private static final String SQL_CREATE_SALES_TABLE =
            "CREATE TABLE " + InventoryContract.SalesEntry.TABLE_NAME + " ("
                    + InventoryContract.SalesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + InventoryContract.SalesEntry.COLUMN_SALES_PRODUCT_ID + " INTEGER NOT NULL, "
                    + InventoryContract.SalesEntry.COLUMN_SALES_AMOUNT + " INTEGER DEFAULT 0, "
                    + InventoryContract.SalesEntry.COLUMN_SUPP_DATE + " TEXT);";

    private static final String SQL_CREATE_SUPPLIERS_TABLE =
            "CREATE TABLE " + InventoryContract.SuppliersEntry.TABLE_NAME + " ("
            + InventoryContract.SuppliersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
            + InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
            + InventoryContract.SuppliersEntry.COLUMN_SUPPLIER_TEL + " TEXT);";

    private static final String SQL_INSET_SUPPLIER_UNKNOWN =
            "INSERT INTO " + InventoryContract.SuppliersEntry.TABLE_NAME + " VALUES ("
            + "1, 'Sem fornecedor', 'email@semforncedor.com', '');";

    private static final String SQL_DELETE_PRODUCTS =
            "DROP TABLE IF EXISTS " + InventoryContract.ProductEntry.TABLE_NAME;

    private static final String SQL_DELETE_SALES =
            "DROP TABLE IF EXISTS " + InventoryContract.SalesEntry.TABLE_NAME;

    private static final String SQL_DELETE_SUPPLIERS =
            "DROP TABLE IF EXISTS " + InventoryContract.SuppliersEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
        db.execSQL(SQL_CREATE_SALES_TABLE);
        db.execSQL(SQL_CREATE_SUPPLIERS_TABLE);
        db.execSQL(SQL_INSET_SUPPLIER_UNKNOWN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PRODUCTS);
        db.execSQL(SQL_DELETE_SALES);
        db.execSQL(SQL_DELETE_SUPPLIERS);
        onCreate(db);
    }
}
