package com.nubbify.keepintrack.data;

import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nubbify.keepintrack.data.ItemContract.ItemEntry;

public class ItemProvider extends ContentProvider{
    /** Log tag for debugging */
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private static final int CREATE_CHECK = 1001;
    private static final int UPDATE_CHECK = 1002;

    /** URI matcher code for the content URI to the item table */
    private static final int ITEMS = 100;

    /** URI matcher code for the content URI to a single item in the item table */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher matches the content URI we're given to the corresponding code.
     * Default value should be NO_MATCH for an invalid URI.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //When ItemProvider is first called, we set up all the possible URI matches
    static {
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /** Database helper object */
    private ItemDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    /**
     * Returns a cursor pointing to the database at the specified Uri with parameters provided.
     * @param uri Database uri
     * @param projection The columns to return for each row
     * @param selection Selection criteria
     * @param selectionArgs Arguments for selection criteria
     * @param sortOrder Specified sort order for returned rows
     * @return A cursor pointing to the database specified.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Uri does not match with any valid Uris");
        }

        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for this uri.");
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) throws IllegalArgumentException {
        try {
            checkValues(values, CREATE_CHECK);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Value check failed: " + e.getMessage());
            return null;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion isn't supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            checkValues(values, UPDATE_CHECK);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Value check failed: " + e.getMessage());
        }

        if (values.size() == 0)
            return 0;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private void checkValues (ContentValues values, int type) throws IllegalArgumentException {
        switch (type){
            //When creating an item, we want all values to be valid.
            case CREATE_CHECK: {
                String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Item requires a name");
                }

                int quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
                if (!ItemEntry.isValidQuantity(quantity)) {
                    throw new IllegalArgumentException("Can't have negative amounts of an item");
                }

                double price = values.getAsDouble(ItemEntry.COLUMN_ITEM_PRICE);
                if (!ItemEntry.isValidPrice(price)) {
                    throw new IllegalArgumentException("Can't have a negative price");
                }
                return;
            }
            //When updating, we don't require all content values to be filled. Thus we only check
            //the content values that are present.
            case UPDATE_CHECK: {
                if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
                    String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
                    if (name == null) {
                        throw new IllegalArgumentException("Item requires a name");
                    }
                }

                if (values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
                    int quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
                    if (!ItemEntry.isValidQuantity(quantity)) {
                        throw new IllegalArgumentException("Can't have negative amounts of an item");
                    }
                }

                if (values.containsKey(ItemEntry.COLUMN_ITEM_PRICE)) {
                    double price = values.getAsDouble(ItemEntry.COLUMN_ITEM_PRICE);
                    if (!ItemEntry.isValidPrice(price)) {
                        throw new IllegalArgumentException("Can't have a negative price");
                    }
                }
                return;
            }

            default:
                throw new IllegalArgumentException("Type for checkValues is invalid");
        }
    }
}
