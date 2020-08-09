package com.hurst.digitalpantry.ui.main.Session;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hurst.digitalpantry.R;
import com.hurst.digitalpantry.SwipableItem;
import com.hurst.digitalpantry.ui.main.Pantry.PantryItem;
import com.hurst.digitalpantry.ui.main.Products.ProductFragment;

import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PantryItem} and makes a call to the
 * specified {@link ProductFragment.OnProductListFragmentInteractionListener}.
 * Replace the implementation with code for your data type.
 */
public class MySessionRecyclerViewAdapter extends RecyclerView.Adapter<MySessionRecyclerViewAdapter.ViewHolder> implements SwipableItem {

    private final List<PantryItem> mValues;
    private final Context context;

    public MySessionRecyclerViewAdapter(Context context, List<PantryItem> items) {
        mValues = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mBarcode.setText(mValues.get(position).getBarcode());
        holder.mName.setText(mValues.get(position).getName());
        holder.mQuantity.setText(String.valueOf(mValues.get(position).getQuantity()));

        holder.mBackground.setLongClickable(true);
        holder.mBackground.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("Long Press Session Item", "Long press registered on " + position);
                mValues.remove(position);
                notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onSwipe(int position, int direction) {
        if(direction == ItemTouchHelper.LEFT) {
            mValues.get(position).decreaseQuantity();
            notifyDataSetChanged();
        } else if(direction == ItemTouchHelper.RIGHT) {
            mValues.get(position).increaseQuantity();
            notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final CardView mBackground;
        final TextView mBarcode;
        final TextView mName;
        final TextView mQuantity;
        PantryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBackground = view.findViewById(R.id.session_item_background);
            mBarcode = (TextView) view.findViewById(R.id.session_barcode);
            mName = (TextView) view.findViewById(R.id.session_name);
            mQuantity = (TextView) view.findViewById(R.id.session_item_quantity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}
