package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ILEANA on 6/23/2017.
 */

public final class ChocolateContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CHOCOLATE = "chocolate";

    public static abstract class ChocolateEntry implements BaseColumns {
        public static final String TABLE_NAME = "Chocolates";
        public static final String _ID = "_id";
        public static final String COLUMN_CHOCOLATE_NAME = "name";
        public static final String COLUMN_CHOCOLATE_PICTURE = "picture";
        public static final String COLUMN_CHOCOLATE_PRICE = "price";
        public static final String COLUMN_CHOCOLATE_QUANTITY = "available_quantity";
        public static final String COLUMN_CHOCOLATE_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_CHOCOLATE_SUPPLIER_PHONE = "supplier_phone";
        public static final String COLUMN_CHOCOLATE_SUPPLIER_EMAIL = "supplier_email";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CHOCOLATE);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of chocolates.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHOCOLATE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single chocolates.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHOCOLATE;

    }
}
