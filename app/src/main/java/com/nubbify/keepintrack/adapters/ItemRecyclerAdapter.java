package com.nubbify.keepintrack.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ViewHolder> {

    private ItemCursorAdapter mItemCursorAdapter;
    private Context mContext;

    public ItemRecyclerAdapter(Context context, Cursor c) {
        mContext = context;
        mItemCursorAdapter = new ItemCursorAdapter(context, c, 0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mItemCursorAdapter.newView(mContext, mItemCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mItemCursorAdapter.getCursor().moveToPosition(position);
        mItemCursorAdapter.bindView(holder.itemView, mContext, mItemCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mItemCursorAdapter.getCount();
    }
}
