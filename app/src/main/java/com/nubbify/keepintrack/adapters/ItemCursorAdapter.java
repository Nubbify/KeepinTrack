package com.nubbify.keepintrack.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.nubbify.keepintrack.R;
import com.nubbify.keepintrack.data.ItemContract.ItemEntry;

import java.util.Locale;

public class ItemCursorAdapter extends CursorAdapter {

    ItemCursorAdapter (Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.tv_item_name);
        TextView quantityTextView = view.findViewById(R.id.tv_item_quantity);
        TextView priceTextView = view.findViewById(R.id.tv_item_price);
        Button saleButton = view.findViewById(R.id.btn_item_sale);

        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);

        String itemName = cursor.getString(nameColumnIndex);
        String itemQuantity = Integer.toString(cursor.getInt(quantityColumnIndex));
        String itemPrice = String.format(Locale.US,"%.2f", cursor.getDouble(priceColumnIndex));

        nameTextView.setText(itemName);
        quantityTextView.setText(itemQuantity);
        priceTextView.setText(itemPrice);
    }
}
