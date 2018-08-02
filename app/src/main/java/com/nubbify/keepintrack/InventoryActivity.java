package com.nubbify.keepintrack;

import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CursorAdapter;

import com.nubbify.keepintrack.adapters.ItemRecyclerAdapter;
import com.nubbify.keepintrack.data.ItemContract.ItemEntry;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private CursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mRecyclerView = findViewById(R.id.rv_inventory);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

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

    private void displayDatabaseInfo() {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_PICTURE };

        Cursor cursor = getContentResolver().query(
                ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        RecyclerView itemListView = findViewById(R.id.rv_inventory);
        ItemRecyclerAdapter adapter = new ItemRecyclerAdapter(this, cursor);
        itemListView.setAdapter(adapter);

        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }

    private void showAddItemDialog(Context c) {
        //root here is null because we can't attach the inflated layout to a dialog that doesn't
        //exist yet.
        View mItemInfo = LayoutInflater.from(c).inflate(R.layout.add_item_alert, null);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Add a new Item")
                .setView(mItemInfo)
                .setPositiveButton(R.string.button_add_item, null)
                .setNegativeButton(R.string.cancel, null)
                .show();

    }
}
