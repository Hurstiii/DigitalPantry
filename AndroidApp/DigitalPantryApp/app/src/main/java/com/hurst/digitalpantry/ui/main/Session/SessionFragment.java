package com.hurst.digitalpantry.ui.main.Session;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hurst.digitalpantry.Config;
import com.hurst.digitalpantry.MainActivity;
import com.hurst.digitalpantry.R;
import com.hurst.digitalpantry.CreateNewProductPrompt;
import com.hurst.digitalpantry.ui.main.Pantry.PantryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SessionFragment extends Fragment {

    public static final int CREATE_NEW_PRODUCT_PROMPT_RC = 00;

    RecyclerView recyclerView;
    private SessionViewModel mViewModel;
    IntentIntegrator intentInt = null;

    public static SessionFragment newInstance() {
        return new SessionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.session_fragment, container, false);
        intentInt = new IntentIntegrator(this);
        FloatingActionButton add_fab = view.findViewById(R.id.session_add_fab);
        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Session adding new items", "Pressed the 'add' floating action button : initiating scanner");
                intentInt.initiateScan();
            }
        });

        FloatingActionButton confirm_fab = view.findViewById(R.id.session_confirm_fab);
        confirm_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Confirming session", "Pressed the 'confirm' floating action button : moving session inventory to main pantry");
                /*
                 * Need to move the items from the ViewModel list to the main pantry
                 * by submitting them to the database in the pantry table.
                 */
                List<PantryItem> session_inventory = new ArrayList<>(mViewModel.mSessionList.getValue()); // copy the current list : to be sent to the database.
                mViewModel.getmSessionList().getValue().clear(); // clear the session to show user instantly that the session has ended.
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scheduleLayoutAnimation();
                JSONArray body = new JSONArray();
                for(PantryItem item : session_inventory) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("barcode", item.getBarcode());
                        object.put("quantity", item.getQuantity());
                        body.put(object);
                        Log.d("Confirming session", "added item with barcode=" + item.getBarcode());
                    } catch (JSONException e) {
                        Log.e("Confirming session", "Error with item " + item.getBarcode() + "\n" + e.getMessage());
                        e.printStackTrace();
                    }
                }
                final String mRequestBody = body.toString();
                String URL = Config.WEB_SERVER_BASE + "/pantry";
                StringRequest request = new StringRequest(
                        Request.Method.POST,
                        URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Confirming session", response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Confirming session", error.toString());
                            }
                        }
                ) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.statusCode);
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };

                ((MainActivity)getActivity()).requestQueue.add(request);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(SessionViewModel.class);

        mViewModel.getmSessionList().observe(getViewLifecycleOwner(), (new Observer<List<PantryItem>>() {
            @Override
            public void onChanged(List<PantryItem> pantryItems) {
                /**
                 * update the local record with the one from the view model
                 * notify the adapter that the local model has changed and it should get the new one
                 */
                try {
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }));

        MySessionRecyclerViewAdapter mAdapter = new MySessionRecyclerViewAdapter(getContext(), mViewModel.getmSessionList().getValue());
        recyclerView = getView().findViewById(R.id.session_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeSessionItemCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // add instructions text
        String text = "This page allows you to add items to a session and then add all items from that session into your pantry. " +
                "Firstly, use the 'add' action button in the bottom right to add item to the session. " +
                "Then once you have scanned everything and would like to add the session to your pantry press the 'tick' action button." +
                "\n\nAdditionally swiping left and right on an item will decrease and increase its quantity respectively.";

        TextView instructions = getActivity().findViewById(R.id.mainActivity_instructions);
        instructions.setText(text);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Scanner result
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
                        Log.e("Scan Result", "scanResult == null");
                        return;
                    }

                    final String barcode = scanResult.getContents();
                    if (barcode == null || barcode.equals("")) {
                        return;
                    }

                    /*
                     * Valid barcode
                     * Query the web server for details of the product with that barcode
                     */
                    String URL = Config.WEB_SERVER_BASE + "/products/" + barcode;
                    Log.d("Query for product", URL);
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.GET,
                            URL, // endpoint
                            new JSONObject(),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("Query for product", response.toString());

                                    JSONArray data = response.optJSONArray("data");
                                    if(data.length() >= 1) {
                                        // Retrieve the product name and show it on screen (Toast) as feedback to the user.
                                        JSONObject product = data.optJSONObject(0);
                                        String barcode = product.optString("barcode");
                                        String name = product.optString("name");
                                        Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                                        Log.d("Query for product", "Found " + name);

                                        /**
                                         * Add to mSessionList
                                         */
                                        PantryItem item = new PantryItem(name, barcode, 1);
                                        addItemToSession(item);
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
                                        Intent newProductPrompt = new Intent(getContext(), CreateNewProductPrompt.class);
                                        newProductPrompt.putExtra("barcode", barcode);
                                        startActivityForResult(newProductPrompt, CREATE_NEW_PRODUCT_PROMPT_RC);
                                    }
                                    Toast.makeText(getContext(), "Request failed, \nTimed out", Toast.LENGTH_LONG).show();
                                    Log.e("Query for product", error.toString());
                                }
                            }
                    );

                    // add constructed request to a queue to be sent off
                    ((MainActivity)getActivity()).requestQueue.add(request);
            } else if (resultCode == RESULT_CANCELED) {
                    Log.d("Scan Result", "scan cancelled");
                }
                break;

            // New product details prompt result
            case CREATE_NEW_PRODUCT_PROMPT_RC:
                if (resultCode == RESULT_OK) {
                    Log.d("Create New Product Prompt", "Result OK received by Scanner Fragment");
                    // get the name from the result
                    Bundle result = data.getExtras();
                    final String name = result.getString("name");
                    final String barcode = result.getString("barcode");
                    Toast.makeText(this.getContext(), name, Toast.LENGTH_SHORT).show();
                    // POST the new product record to the web server
                    try {
                        JSONObject newProduct = new JSONObject();
                        newProduct.put("barcode", barcode);
                        newProduct.put("format", "N/A");
                        newProduct.put("name", name);
                        JSONArray payload = new JSONArray();
                        payload.put(newProduct);
                        final String mRequestBody = payload.toString();

                        String newProductEndpoint = Config.WEB_SERVER_BASE + "/products";
                        StringRequest request = new StringRequest(
                                Request.Method.POST,
                                newProductEndpoint,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("Create New Product Prompt", response);

                                        /**
                                         * Add to mSessionList
                                         */
                                        PantryItem item = new PantryItem(name, barcode, 1);
                                        addItemToSession(item);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getContext(), "Request failed, \nBad request : failed to add the new product to the database", Toast.LENGTH_LONG).show();
                                        Log.e("Create New Product Prompt", error.toString());
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
                                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                                } catch (UnsupportedEncodingException uee) {
                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                                    return null;
                                }
                            }

                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                String responseString = "";
                                if (response != null) {
                                    responseString = String.valueOf(response.statusCode);
                                }
                                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                            }
                        };
                        ((MainActivity)getActivity()).requestQueue.add(request);
                    } catch(JSONException e) {
                        Log.e("Create New Product Prompt", "Malformed json, couldn't create the JSONObject");
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    Log.d("Create New Product Prompt", "Activity cancelled");
                }
                break;
        }
    }

    private void addItemToSession(PantryItem item) {
        ListIterator<PantryItem> i = mViewModel.getmSessionList().getValue().listIterator();

        /**
         * Check if list is empty
         * Can just add the item if list is empty
         */
        if(mViewModel.getmSessionList().getValue().isEmpty()) {
            mViewModel.getmSessionList().getValue().add(item);
        } else {
            // check for existence
            while (i.hasNext()) {
                PantryItem existing = i.next();

                /**
                 * Check the current position in the list for a match
                 * Increase the quantity of the existing (and matching) item in the list
                 */
                if (existing.getBarcode().equals(item.getBarcode())) {
                    existing.increaseQuantity();
                    break;
                }

                /**
                 * Last item in list was checked and none matched
                 * Therefore insert as new item
                 */
                if (!i.hasNext()) {
                    mViewModel.getmSessionList().getValue().add(item);
                }
            }
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
}
