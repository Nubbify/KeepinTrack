package com.nubbify.keepintrack.data;

import android.content.ClipData;
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

import com.nubbify.keepintrack.data.ItemContract.ItemEntry;

public class ItemProvider extends ContentProvider{

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

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
        return null;
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

    private Uri insertItem(Uri uri, ContentValues values) {
        //Here we check our inputs to make sure they're valid.
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



        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
