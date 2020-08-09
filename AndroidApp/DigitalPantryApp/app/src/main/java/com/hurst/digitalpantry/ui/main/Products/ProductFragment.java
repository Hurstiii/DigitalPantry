package com.hurst.digitalpantry.ui.main.Products;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hurst.digitalpantry.Config;
import com.hurst.digitalpantry.MainActivity;
import com.hurst.digitalpantry.R;
import com.hurst.digitalpantry.ui.main.Pantry.PantryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnProductListFragmentInteractionListener}
 * interface.
 */
public class ProductFragment extends Fragment implements ProductListListener {

    // Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // Customize parameters
    private int mColumnCount = 2;
    private OnProductListFragmentInteractionListener mListener;
    RequestQueue requestQueue;
    List<PantryItem> mProductsList = new ArrayList<PantryItem>();
    RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProductFragment() {
    }

    // Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProductFragment newInstance(int columnCount) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_fragment, container, false);

        refreshProductList();

        recyclerView = view.findViewById(R.id.product_recycler_view);

        // Set the adapter
        if (recyclerView != null) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyProductRecyclerViewAdapter(mProductsList, mListener));
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // add instructions text
        String text = "This page shows a list of known products that the scanner " +
                "will recognise without further user input. To add more known products simple go to " +
                "the session page, scan products and give them a name. Note: you don't have to " +
                "add the items to the pantry for the system to remember them. " +
                "Each product can be edited by pressing the pencil next to them.";

        TextView instructions = getActivity().findViewById(R.id.mainActivity_instructions);
        instructions.setText(text);
    }

    private void refreshProductList() {
        mProductsList.clear();

        /**
         * Query the web server for the product table data
         */
        requestQueue = ((MainActivity) this.getActivity()).requestQueue;
        String URL = Config.WEB_SERVER_BASE + "/products";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Query for all products", response.toString());

                        JSONArray data = response.optJSONArray("data");
                        /**
                         * FOR EACH JSONObject :
                         *  1. Create a new PantryItem
                         *
                         * Construct a list of ProductItems
                         */
                        for(int i=0; i < data.length(); i++) {
                            try {
                                JSONObject productData = data.getJSONObject(i);
                                PantryItem productItem = new PantryItem(productData.getString("name"), productData.getString("barcode"), -1);
                                mProductsList.add(productItem);
                            } catch(JSONException e) {
                                Log.e("Query for all products", e.toString());
                            }
                        }

                        if(recyclerView != null && recyclerView.getAdapter() != null) {
                            recyclerView.getAdapter().notifyDataSetChanged();
                            recyclerView.scheduleLayoutAnimation();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        /**
                         * TODO: Handle individual errors for more informative feedback to the user.
                         */

                        Log.e("Query for all products", error.toString());
                    }
                }
        );

        // add constructed request to a queue to be sent off
        requestQueue.add(request);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProductListFragmentInteractionListener) {
            mListener = (OnProductListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnProductListFragmentInteractionListener");
        }

        if (context instanceof MainActivity) {
            ((MainActivity) context).registerPfCallbackListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListChanged() {
        refreshProductList();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnProductListFragmentInteractionListener {
        void onListFragmentInteraction(PantryItem item);

        void onProductEditClicked(PantryItem item);
    }
}
