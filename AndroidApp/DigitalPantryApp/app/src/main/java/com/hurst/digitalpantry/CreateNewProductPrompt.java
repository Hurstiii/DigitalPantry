package com.hurst.digitalpantry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hurst.digitalpantry.R;

public class CreateNewProductPrompt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_product_prompt);

        if(getIntent().hasExtra("name")) {
            EditText nameInput = findViewById(R.id.new_product_name_input);
            nameInput.setText(getIntent().getStringExtra("name"));
        }
    }

    public void onConfirm(View view) {
        EditText name_input = findViewById(R.id.new_product_name_input);
        String name = name_input.getText().toString();
        if(name.equals("")) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent result = new Intent();
        Bundle data = new Bundle();
        if(getIntent().hasExtra("barcode")) {
            data.putString("barcode", getIntent().getStringExtra("barcode"));
        }
        data.putString("name", name);
        result.putExtras(data);
        setResult(RESULT_OK, result);
        finish();
    }
}
