package com.hurst.digitalpantry.ui.main.Pantry;

import android.content.Context;
import android.util.Log;
import android.widget.Adapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hurst.digitalpantry.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PantryViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    RequestQueue rQueue;
    MutableLiveData<List<PantryItem>> mPantryList;
    final String loadPantry_url = Config.WEB_SERVER_BASE + "/pantry";

    PantryViewModel(RequestQueue rQueue) {
        this.rQueue = rQueue;
    }

    public MutableLiveData<List<PantryItem>> getmPantryList() {
        if(mPantryList == null) {
            mPantryList = new MutableLiveData<>();
            mPantryList.setValue(new ArrayList<PantryItem>());
        }

        return mPantryList;
    }

    public void loadPantry() {

        try {
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                loadPantry_url,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Query for all pantry items", "received success response... parsing data");
                        JSONArray data = response.optJSONArray("data");
                        mPantryList.getValue().clear();
                        /*
                         * FOR EACH JSONObject :
                         *  1. Create a new PantryItem
                         *
                         * Construct a list of PantryItems
                         */
                        for(int i=0; i < data.length(); i++) {
                            try {
                                JSONObject itemData = data.getJSONObject(i);
                                PantryItem pantryItem = new PantryItem(itemData.getString("name"), itemData.getString("barcode"), itemData.getInt("quantity"));
                                mPantryList.getValue().add(pantryItem);
                            } catch(JSONException e) {
                                Log.e("Query for all pantry items", e.toString());
                            }
                        }
                        mPantryList.setValue(mPantryList.getValue());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        /**
                         * TODO: Handle individual errors for more informative feedback to the user.
                         */

                        Log.e("Query for all pantry items", error.toString());
                    }
                }
            );
            rQueue.add(request);
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }


}
