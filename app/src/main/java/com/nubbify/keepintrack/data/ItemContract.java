package com.nubbify.keepintrack.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DecimalFormat;

public final class ItemContract {

    //Prevent instantiation of the contract class.
    private ItemContract() {}

    /**
     * The package name for the app is used here as the name
     * for the entire content provider.
     */
    public static final String CONTENT_AUTHORITY = "com.nubbify.keepintrack";

    /**
     * Use CONTENT_AUTHORITY to create the core of all URIs used to
     * connect to our content provider
     */
    public static final Uri BASE_CONTENT_URL = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path for looking at item data.
     * As an example, content://com.nubbify.keepintrack/[PATH_ITEMS] is a valid
     * path to the item database.
     */
    public static final String PATH_ITEMS = "items";

    /**
     * Inner class that defines constant values for the database.
     * Each entry represents one item.
     */
    public static final class ItemEntry implements BaseColumns {

        /** The content URI to access item data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URL, PATH_ITEMS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY
                        + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY
                        + "/" + PATH_ITEMS;

        /** Name of database table for items */
        public final static String TABLE_NAME = "items";

        /**
         * Unique ID number for the item (only for use in database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         * Type: TEXT
         * Constraints: Cannot be empty.
         */
        public final static String COLUMN_ITEM_NAME = "name";

        /**
         * Quantity of the item available for sale.
         * Type: INTEGER
         * Constraints: Quantity >= 0
         * Default value: 0
         */
        public final static String COLUMN_ITEM_QUANTITY = "quantity";

        /**
         * Price of the item.
         * TYPE: DECIMAL(9,2)
         * Constraints: Quantity >= 0, Rounded to two decimal places.
         * Default value: 0.00
         */
        public final static String COLUMN_ITEM_PRICE = "price";

        /**
         * A picture of the item.
         * TYPE: BLOB
         */
        public final static String COLUMN_ITEM_PICTURE = "picture";

        /**
         * Checks that the quantity of an item is not negative.
         * @param quantity
         * @return true if positive or 0, false if negative
         */
        public static boolean isValidQuantity (int quantity) {
            if (quantity < 0)
                return false;
            else
                return true;
        }

        /**
         * Checks that the price of an item is not negative
         * @param price
         * @return true if positive or 0, false if negative
         */
        public static boolean isValidPrice (double price) {
            if (price < 0)
                return false;
            else
                return true;
        }

        /**
         * Formats the price inputted into a string going up to two decimal places.
         * @param price
         * @return a string that ends in the format "#.##" (regex /[0-9]+\.[0-9][0-9]/)
         */
        public static String formatPrice (double price) {
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(price);
        }
    }
}
