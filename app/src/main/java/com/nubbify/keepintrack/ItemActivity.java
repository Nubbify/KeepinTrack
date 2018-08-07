package com.nubbify.keepintrack;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nubbify.keepintrack.data.ItemContract.ItemEntry;

import java.util.Locale;

public class ItemActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    private TextView mNameText;

    private TextView mQuantityText;

    private TextView mPriceText;

    private Button mEditNameButton;

    private Button mEditQuantityButton;

    private Button mEditPriceButton;

    private Button mDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        mNameText = findViewById(R.id.tv_item_name);
        mQuantityText = findViewById(R.id.tv_item_quantity);
        mPriceText = findViewById(R.id.tv_item_price);
        mEditNameButton = findViewById(R.id.btn_update_name);
        mEditQuantityButton = findViewById(R.id.btn_update_quantity);
        mEditPriceButton = findViewById(R.id.btn_update_price);
        mDeleteButton = findViewById(R.id.btn_delete_item);

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);

        mEditQuantityButton.setOnClickListener(createEditAlertClickListener());
        mEditPriceButton.setOnClickListener(createEditAlertClickListener());
        mEditNameButton.setOnClickListener(createEditAlertClickListener());

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_PICTURE
        };

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int pictureColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE);

            String itemName = data.getString(nameColumnIndex);
            String itemQuantity = String.valueOf(data.getInt(quantityColumnIndex));
            String itemPrice = getString(R.string.currency_symbol, String.format(Locale.US,"%.2f", data.getDouble(priceColumnIndex)));

            mNameText.setText(itemName);
            mQuantityText.setText(itemQuantity);
            mPriceText.setText(itemPrice);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private View.OnClickListener createEditAlertClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //root is null here since we can't attach it to an alertdialog that doesn't exist yet
                View mEditItem = LayoutInflater.from(ItemActivity.this).inflate(R.layout.alert_edit_item, null);
                final EditText mItemEditText = mEditItem.findViewById(R.id.et_edit_item);

                AlertDialog.Builder editAlert = new AlertDialog.Builder(ItemActivity.this);
                editAlert.setView(mEditItem);
                editAlert.setNegativeButton(getString(R.string.cancel), null);

                if (v == mEditNameButton) {
                    editAlert.setTitle(getString(R.string.edit_name_alert_title));
                    mItemEditText.setHint(getString(R.string.edit_name_alert_hint));
                    mItemEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editAlert.setPositiveButton(getString(R.string.edit_name_alert_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String editText = mItemEditText.getText().toString().trim();
                            if (editText.equals("")) {
                                Toast.makeText(ItemActivity.this, R.string.error_fill_edit_fields, Toast.LENGTH_LONG).show();
                                return;
                            }
                            try {
                                editItemName(editText);
                            } catch (Exception e) {
                                Toast.makeText(ItemActivity.this, getString(R.string.error_edit_item) + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (v == mEditPriceButton) {
                    editAlert.setTitle(getString(R.string.edit_price_alert_title));
                    mItemEditText.setHint(getString(R.string.edit_price_alert_hint));
                    mItemEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editAlert.setPositiveButton(getString(R.string.edit_price_alert_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String editText = mItemEditText.getText().toString().trim();
                            if (editText.equals("")) {
                                Toast.makeText(ItemActivity.this, R.string.error_fill_edit_fields, Toast.LENGTH_LONG).show();
                                return;
                            }
                            try {
                                editItemPrice(editText);
                            } catch (Exception e) {
                                Toast.makeText(ItemActivity.this, getString(R.string.error_edit_item) + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (v == mEditQuantityButton) {
                    editAlert.setTitle(getString(R.string.edit_quantity_alert_title));
                    mItemEditText.setHint(getString(R.string.edit_quantity_alert_hint));
                    mItemEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editAlert.setPositiveButton(getString(R.string.edit_quantity_alert_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String editText = mItemEditText.getText().toString().trim();
                            if (editText.equals("")) {
                                Toast.makeText(ItemActivity.this, R.string.error_fill_edit_fields, Toast.LENGTH_LONG).show();
                                return;
                            }
                            try {
                                editItemQuantity(editText);
                            } catch (Exception e) {
                                Toast.makeText(ItemActivity.this, getString(R.string.error_edit_item) + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Listener got a view parameter it shouldn't be linked to");
                }
                editAlert.show();
            }
        };
    }

    private void editItemName (String editText){
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, editText);
        if (getContentResolver().update(mCurrentItemUri, values, null, null) == 0) {
            Toast.makeText(this, getString(R.string.edit_item_failed), Toast.LENGTH_LONG).show();
        } else {
            getLoaderManager().restartLoader(EXISTING_ITEM_LOADER, null, this);
            Toast.makeText(this, getString(R.string.edit_item_success), Toast.LENGTH_LONG).show();
        }
    }

    private void editItemQuantity (String editText){
        ContentValues values = new ContentValues();
        int quantityInt = Integer.parseInt(editText);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityInt);
        if (getContentResolver().update(mCurrentItemUri, values, null, null) == 0) {
            Toast.makeText(this, getString(R.string.edit_item_failed), Toast.LENGTH_LONG).show();
        } else {
            getLoaderManager().restartLoader(EXISTING_ITEM_LOADER, null, this);
            Toast.makeText(this, getString(R.string.edit_item_success), Toast.LENGTH_LONG).show();
        }
    }

    private void editItemPrice (String editText){
        ContentValues values = new ContentValues();
        double priceDouble = Double.parseDouble(editText);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceDouble);
        if (getContentResolver().update(mCurrentItemUri, values, null, null) == 0) {
            Toast.makeText(this, getString(R.string.edit_item_failed), Toast.LENGTH_LONG).show();
        } else {
            getLoaderManager().restartLoader(EXISTING_ITEM_LOADER, null, this);
            Toast.makeText(this, getString(R.string.edit_item_success), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete this?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getContentResolver().delete(mCurrentItemUri, null, null) == 1) {
                            finish();
                        } else {
                            Toast.makeText(ItemActivity.this, R.string.delete_item_failed, Toast.LENGTH_LONG);
                        }
                    }
                });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

}
