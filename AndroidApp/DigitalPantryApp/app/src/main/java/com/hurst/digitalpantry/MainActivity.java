package com.hurst.digitalpantry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.christophesmet.android.views.maskableframelayout.MaskableFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hurst.digitalpantry.ui.main.Pantry.PantryFragment;
import com.hurst.digitalpantry.ui.main.Pantry.PantryItem;
import com.hurst.digitalpantry.ui.main.Products.ProductFragment;
import com.hurst.digitalpantry.ui.main.Products.ProductListListener;
import com.hurst.digitalpantry.ui.main.Session.SessionFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ProductFragment.OnProductListFragmentInteractionListener {

    static final int EDIT_PRODUCT_ITEM_RC = 001;

    BottomNavigationView navigation;
    public RequestQueue requestQueue;
    ProductListListener pfCallbackListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, PantryFragment.newInstance())
                    .commitNow();
        }

        navigation = findViewById(R.id.bottomNavigationView);
        navigation.setSelectedItemId(R.id.navigation_pantry);
        navigation.setOnNavigationItemSelectedListener(this);

        requestQueue = Volley.newRequestQueue(this);

        final FloatingActionButton open = findViewById(R.id.mainActivity_instructions_open_fab);
        final CardView instructions_container = findViewById(R.id.mainActivity_instructions_container);
        final ImageButton close = findViewById(R.id.mainActivity_instructions_close_button);

        final View instructions_frame = findViewById(R.id.instructions_frame);

        open.setVisibility(View.GONE);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change visibility of card view container to 'gone'
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // get the center for the clipping circle
                    int cx = instructions_frame.getWidth() / 2;
                    int cy = instructions_frame.getHeight() / 2;

                    // get the initial radius for the clipping circle
                    float initialRadius = (float) Math.hypot(cx, cy);

                    // create the animation (the final radius is zero)
                    Animator anim = ViewAnimationUtils.createCircularReveal(instructions_frame, cx, cy, initialRadius, 0f);

                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            instructions_frame.setVisibility(View.GONE);
                            open.setVisibility(View.VISIBLE);
                        }
                    });

                    // start the animation
                    anim.start();
                } else {
                    // set the view to visible without a circular reveal animation below Lollipop
                    instructions_frame.setVisibility(View.GONE);
                    open.setVisibility(View.VISIBLE);
                }
            }
        });
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change visibility of card view container to 'visible'
                open.setVisibility(View.GONE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // get the center for the clipping circle
                    int cx = instructions_frame.getWidth() / 2;
                    int cy = instructions_frame.getHeight() / 2;

                    // get the final radius for the clipping circle
                    float finalRadius = (float) Math.hypot(cx, cy);

                    // create the animator for this view (the start radius is zero)
                    Animator anim = ViewAnimationUtils.createCircularReveal(instructions_frame, cx, cy, 0f, finalRadius);

                    // make the view visible and start the animation
                    instructions_frame.setVisibility(View.VISIBLE);
                    anim.start();
                } else {
                    // set the view to invisible without a circular reveal animation below Lollipop
                    instructions_frame.setVisibility(View.GONE);
                }
            }
        });
    }

    public void registerPfCallbackListener(ProductListListener pfCallbackListener) {
        this.pfCallbackListener = pfCallbackListener;
    }

    public void navigateToPantry() {
        navigation.setSelectedItemId(R.id.navigation_pantry);
    }

    public void navigateToScanner() {
        navigation.setSelectedItemId(R.id.navigation_session);
    }

    private boolean loadFragment(Fragment fragment) {
        if(fragment == null) {
            return false;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;

        switch(menuItem.getItemId()) {
            case R.id.navigation_session:
                fragment = SessionFragment.newInstance();
                break;
            case R.id.navigation_pantry:
                fragment = PantryFragment.newInstance();
                break;
            case R.id.navigation_products:
                fragment = ProductFragment.newInstance(1);
                break;
        }

        return loadFragment(fragment);
    }

    @Override
    public void onListFragmentInteraction(PantryItem item) {
        Log.d("Product list interaction", "Pressed item : " + item.getName());
    }

    @Override
    public void onProductEditClicked(PantryItem item) {
        Log.d("Product list interaction", "Edit of item selected : " + item.getName());

        /*
         * Edit button of an item pressed
         * Need to start a new {@link com.hurst.digitalpantry.CreateNewProductPrompt} activity to edit the details (name) of the item
         */
        Intent newProductPrompt = new Intent(this, CreateNewProductPrompt.class);
        newProductPrompt.putExtra("barcode", item.getBarcode());
        newProductPrompt.putExtra("name", item.getName());
        startActivityForResult(newProductPrompt, EDIT_PRODUCT_ITEM_RC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case EDIT_PRODUCT_ITEM_RC:
                if (resultCode == RESULT_OK) {
                    /*
                     * Try to extract the new product information from the intent
                     * Then push it to the web server
                     * Then reload the products fragment?
                     */
                    Log.d("Editing products information", "Received a RESULT_OK from the CreateNewProductPrompt activity");
                    try {
                        String name = data.getStringExtra("name");
                        String barcode = data.getStringExtra("barcode");

                        JSONObject newProduct = new JSONObject();
                        newProduct.put("barcode", barcode);
                        newProduct.put("format", "N/A");
                        newProduct.put("name", name);
                        final String mRequestBody = newProduct.toString();

                        String newProductEndpoint = Config.WEB_SERVER_BASE + "/products/" + barcode;
                        StringRequest request = new StringRequest(
                                Request.Method.PUT,
                                newProductEndpoint,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("Edit a products information", response);
                                        pfCallbackListener.onListChanged();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(MainActivity.this, "Request failed, \nBad request : failed to add the update product information", Toast.LENGTH_LONG).show();
                                        Log.e("Edit a products information", error.toString());
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
                        requestQueue.add(request);
                    } catch(NullPointerException e) {
                        Log.e("Editing products information", e.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
