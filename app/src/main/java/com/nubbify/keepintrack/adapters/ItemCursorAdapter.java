package com.nubbify.keepintrack.adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nubbify.keepintrack.R;
import com.nubbify.keepintrack.data.ItemContract.ItemEntry;
import com.nubbify.keepintrack.utils.ValueContainer;

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
    public void bindView(final View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.tv_item_name);
        final TextView quantityTextView = view.findViewById(R.id.tv_item_quantity);
        TextView priceTextView = view.findViewById(R.id.tv_item_price);
        Button saleButton = view.findViewById(R.id.btn_item_sale);

        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        final int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);

        final String itemName = cursor.getString(nameColumnIndex);
        final String itemPrice = context.getString(R.string.currency_symbol) + String.format(Locale.US,"%.2f", cursor.getDouble(priceColumnIndex));
        final String itemQuantity = Integer.toString(cursor.getInt(quantityColumnIndex)) + context.getString(R.string.item_count_remaining);

        nameTextView.setText(itemName);
        quantityTextView.setText(itemQuantity);
        priceTextView.setText(itemPrice);


        final int id = cursor.getInt(idColumnIndex);
        //We put the quantity in a ValueContainer so we can access it and modify it from the
        //saleButton's onClickListener (as we wouldn't be able to modify it if it was just an int).
        final ValueContainer<Integer> quantity = new ValueContainer<>(cursor.getInt(quantityColumnIndex));
        //On the press of the sale button, lower the quantity by one.
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri itemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                ContentValues values = new ContentValues();
                if (quantity.getValue() > 0)
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity.getValue() - 1);
                else
                    return;

                if (view.getContext().getContentResolver()
                        .update(itemUri, values, null, null) == 1) {
                    quantity.setValue(quantity.getValue()-1);
                    quantityTextView.setText(Integer.toString(quantity.getValue())
                            + v.getContext().getString(R.string.item_count_remaining));
                }

            }
        });
    }
}
