package com.hurst.digitalpantry.ui.main.Products;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hurst.digitalpantry.R;
import com.hurst.digitalpantry.ui.main.Pantry.PantryItem;
import com.hurst.digitalpantry.ui.main.Products.ProductFragment.OnProductListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PantryItem} and makes a call to the
 * specified {@link OnProductListFragmentInteractionListener}.
 * Replace the implementation with code for your data type.
 */
public class MyProductRecyclerViewAdapter extends RecyclerView.Adapter<MyProductRecyclerViewAdapter.ViewHolder> {

    private final List<PantryItem> mValues;
    private final OnProductListFragmentInteractionListener mListener;

    public MyProductRecyclerViewAdapter(List<PantryItem> items, OnProductListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mBarcode.setText(mValues.get(position).getBarcode());
        holder.mName.setText(mValues.get(position).getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onProductEditClicked(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mBarcode;
        public final TextView mName;
        public final Button mEditButton;
        public PantryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBarcode = (TextView) view.findViewById(R.id.product_barcode);
            mName = (TextView) view.findViewById(R.id.product_name);
            mEditButton = (Button) view.findViewById(R.id.product_edit);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}
