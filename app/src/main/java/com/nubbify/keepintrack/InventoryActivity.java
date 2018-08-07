package com.nubbify.keepintrack;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nubbify.keepintrack.adapters.ItemRecyclerAdapter;
import com.nubbify.keepintrack.data.ItemContract.ItemEntry;

public class InventoryActivity extends AppCompatActivity {

    private Cursor cursor;
    private RecyclerView itemRecyclerView;
    private ItemRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        itemRecyclerView = findViewById(R.id.rv_inventory);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        itemRecyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = findViewById(R.id.fab_add_inventory);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemDialog(InventoryActivity.this);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }

    private void displayDatabaseInfo() {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE};

        cursor = getContentResolver().query(
                ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        adapter = new ItemRecyclerAdapter(this, cursor);
        itemRecyclerView.setAdapter(adapter);
    }

    private void showAddItemDialog(Context c) {
        //root here is null because we can't attach the inflated layout to a dialog that doesn't
        //exist yet.
        View mItemInfo = LayoutInflater.from(c).inflate(R.layout.alert_add_item, null);
        final EditText mItemName = mItemInfo.findViewById(R.id.et_add_item_name);
        final EditText mItemQuantity = mItemInfo.findViewById(R.id.et_add_item_quantity);
        final EditText mItemPrice = mItemInfo.findViewById(R.id.et_add_item_price);

        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle(getString(R.string.add_item_dialog_title))
                .setView(mItemInfo)
                .setPositiveButton(getString(R.string.button_add_item), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = mItemName.getText().toString().trim();
                        String quantity = mItemQuantity.getText().toString().trim();
                        String price = mItemPrice.getText().toString().trim();
                        if (name.equals("") || quantity.equals("") || price.equals("")){
                            Toast.makeText(InventoryActivity.this, R.string.error_fill_item_fields, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            saveItem(name, quantity, price);
                        } catch (Exception e) {
                            Toast.makeText(InventoryActivity.this, getString(R.string.error_add_item) + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();

    }

    private void saveItem(String name, String quantity, String price) {
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, name);
        int quantityInt = Integer.parseInt(quantity);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityInt);
        double priceDouble = Double.parseDouble(price);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceDouble);

        Uri mItemUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

        if (mItemUri == null) {
            Toast.makeText(this, getString(R.string.add_item_insert_failed), Toast.LENGTH_LONG).show();
        } else {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
            //We close the cursor here because we open a new one in displayDatabaseInfo.
            displayDatabaseInfo();
            Toast.makeText(this, getString(R.string.add_item_insert_success), Toast.LENGTH_LONG).show();
        }
    }
}
