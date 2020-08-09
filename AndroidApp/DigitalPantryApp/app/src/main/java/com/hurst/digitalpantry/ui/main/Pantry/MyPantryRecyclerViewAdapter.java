package com.hurst.digitalpantry.ui.main.Pantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hurst.digitalpantry.R;
import com.hurst.digitalpantry.SwipableItem;
import com.hurst.digitalpantry.ui.main.Products.ProductFragment.OnProductListFragmentInteractionListener;

import java.util.List;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PantryItem} and makes a call to the
 * specified {@link OnProductListFragmentInteractionListener}.
 * Replace the implementation with code for your data type.
 */
public class MyPantryRecyclerViewAdapter extends RecyclerView.Adapter<MyPantryRecyclerViewAdapter.ViewHolder> implements SwipableItem {

    private Context mContext;
    private final List<PantryItem> mValues;
    private AdapterToPantryFragmentCallback callback;

    public MyPantryRecyclerViewAdapter(Context context, AdapterToPantryFragmentCallback callback, List<PantryItem> items) {
        mContext = context;
        this.callback = callback;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pantry_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mName.setText(mValues.get(position).getName());
        holder.mQuantity.setText(String.valueOf(mValues.get(position).getQuantity()));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onSwipe(int position, int direction) {
        if(direction == ItemTouchHelper.LEFT) {
            callback.onReduceItemQuantity(position);
            notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return mContext;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mName;
        final TextView mQuantity;

        PantryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.pantry_name);
            mQuantity = (TextView) view.findViewById(R.id.pantry_item_quantity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}
