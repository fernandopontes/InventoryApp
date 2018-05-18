package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    public InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "inventory_products";
    public static final String PATH_SALES = "inventory_sales";
    public static final String PATH_SUPPLIERS = "inventory_suppliers";

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * O tipo MIME do {@link #CONTENT_URI} para uma lista de PRODUCTs.
         */

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * O tipo MIME do {@link #CONTENT_URI} para um único PRODUCT.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public final static String TABLE_NAME = "products";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_SUPPLIER_ID = "supplier_id";
        public final static String COLUMN_PRODUCT_IMAGE = "image";
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRODUCT_AMOUNT = "amount";
        public final static String COLUMN_PRODUCT_DESCRIPTION = "description";
        public final static String COLUMN_PRODUCT_VALUE = "value";
    }

    public static final class SalesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SALES);

        /**
         * O tipo MIME do {@link #CONTENT_URI} para uma lista de SALES.
         */

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SALES;

        /**
         * O tipo MIME do {@link #CONTENT_URI} para um único SALE.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SALES;

        public final static String TABLE_NAME = "sales";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SALES_PRODUCT_ID = "product_id";
        public final static String COLUMN_SALES_AMOUNT = "amount";
        public final static String COLUMN_SUPP_DATE = "date";
    }

    public static final class SuppliersEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLIERS);

        /**
         * O tipo MIME do {@link #CONTENT_URI} para uma lista de SALES.
         */

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIERS;

        /**
         * O tipo MIME do {@link #CONTENT_URI} para um único SALE.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIERS;

        public final static String TABLE_NAME = "suppliers";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SUPPLIER_NAME = "name";
        public final static String COLUMN_SUPPLIER_EMAIL = "email";
        public final static String COLUMN_SUPPLIER_TEL = "telephone";
    }
}
