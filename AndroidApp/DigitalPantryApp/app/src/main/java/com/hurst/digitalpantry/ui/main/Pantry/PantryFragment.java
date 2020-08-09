package com.hurst.digitalpantry.ui.main.Pantry;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hurst.digitalpantry.Config;
import com.hurst.digitalpantry.CreateNewProductPrompt;
import com.hurst.digitalpantry.MainActivity;
import com.hurst.digitalpantry.R;
import com.hurst.digitalpantry.ui.main.Session.MySessionRecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PantryFragment extends Fragment implements AdapterToPantryFragmentCallback {

    private PantryViewModel mViewModel;
    private RecyclerView recyclerView;
    IntentIntegrator intentInt = null;

    public static PantryFragment newInstance() {
        return new PantryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pantry_fragment, container, false);

        intentInt = new IntentIntegrator(this);
        FloatingActionButton scanOutFab = view.findViewById(R.id.pantry_scanout_item_fab);
        scanOutFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Scanning items out", "ScanOut Button pressed : initiating scanner");
                intentInt.initiateScan();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getView().findViewById(R.id.pantry_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mViewModel = ViewModelProviders.of(this,
                new PantryViewModelFactory(((MainActivity)this.getActivity()).requestQueue)
        ).get(PantryViewModel.class);

        mViewModel.getmPantryList().observe(getViewLifecycleOwner(), new Observer<List<PantryItem>>() {
            @Override
            public void onChanged(List<PantryItem> pantryItems) {
                try {
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        mViewModel.loadPantry();
        MyPantryRecyclerViewAdapter adapter = new MyPantryRecyclerViewAdapter(getContext(), this, mViewModel.getmPantryList().getValue());
        recyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipePantryItemCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // add instructions text
        String text = "This page shows all the items currently in your pantry and their quantities. " +
                "To take items out, either press the 'minus' action button in the bottom right and " +
                "scan items out, or swipe items left in the list.";

        TextView instructions = getActivity().findViewById(R.id.mainActivity_instructions);
        instructions.setText(text);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (resultCode == RESULT_OK) {
                    // start scanning activity for chain scanning
                    intentInt.initiateScan();

                    /*
                     * No information in the scan result
                     * Return and record error
                     */
                    if(scanResult == null) {
                        Log.e("Scanning out items : Scan Result", "scanResult == null");
                        return;
                    }

                    final String barcode = scanResult.getContents();
                    reduceItemQuantity(barcode);

                } else if (resultCode == RESULT_CANCELED) {
                    Log.d("Scanning out items : Scan Result", "scan cancelled");
                    mViewModel.loadPantry();
                }
                break;

        }
    }

    public void reduceItemQuantity(final String barcode) {
        if (barcode == null || barcode.equals("")) {
            Log.d("Scanning out items : Scan Result", "no valid barcode");
            return;
        }

        /*
         * Valid barcode
         * Query the web server for details of the product with that barcode
         */
        String URL = Config.WEB_SERVER_BASE + "/pantry/" + barcode;
        Log.d("Scanning out item : Query pantry", URL);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL, // endpoint
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Scanning out item : Query pantry", response.toString());

                        JSONArray data = response.optJSONArray("data");
                        if(data.length() >= 1) {
                            // Retrieve the product name and show it on screen (Toast) as feedback to the user.
                            JSONObject item = data.optJSONObject(0);
                            String barcode = item.optString("barcode");
                            String name = item.optString("name");
                            int quantity = item.optInt("quantity");
                            Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                            Log.d("Scanning out item : Query pantry", "Found " + name);

                            /**
                             * Make PUT request to reduce quantity of item by 1
                             */
                            String update_url = Config.WEB_SERVER_BASE + "/pantry/" + barcode;
                            Log.d("Scanning out item : Update quantity", update_url);

                            try {
                                item.put("quantity", quantity-1);
                                final String body = item.toString(); // update request body

                                StringRequest update_request = new StringRequest(
                                        Request.Method.PUT,
                                        update_url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Log.d("Scanning out item : Update quantity", response);
                                                mViewModel.loadPantry();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e("Query for product", error.toString());
                                            }
                                        }
                                ) {
                                    @Override
                                    public String getBodyContentType() {
                                        return "application/json; charset=utf-8";
                                    }

                                    @Override
                                    public byte[] getBody() {
                                        try {
                                            return body == null ? null : body.getBytes("utf-8");
                                        } catch (UnsupportedEncodingException uee) {
                                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", body, "utf-8");
                                            return null;
                                        }
                                    }
                                };

                                ((MainActivity)getActivity()).requestQueue.add(update_request);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // if the error is a 404 Not Found response, prompt user to add the product to the database.
                        if(error.networkResponse.statusCode == 404) {
                            /*
                             * No record of barcode in database
                             * Prompt user to add it as a new product by launching a new
                             * activity for a response.
                             */
                            Log.d("Query for product", "No record of barcode " + barcode);
                            Toast.makeText(getContext(), "Item not found in the pantry", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Request failed, \nTimed out", Toast.LENGTH_LONG).show();
                            Log.e("Query for product", error.toString());
                        }
                    }
                }
        );

        // add constructed request to a queue to be sent off
        ((MainActivity)getActivity()).requestQueue.add(request);
    }

    @Override
    public void onReduceItemQuantity(int position) {
        reduceItemQuantity(mViewModel.getmPantryList().getValue().get(position).getBarcode());
    }
}
