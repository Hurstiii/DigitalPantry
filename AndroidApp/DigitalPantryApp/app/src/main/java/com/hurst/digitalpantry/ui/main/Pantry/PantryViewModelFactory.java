package com.hurst.digitalpantry.ui.main.Pantry;

import com.android.volley.RequestQueue;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class PantryViewModelFactory implements ViewModelProvider.Factory {

    private RequestQueue rQueue;

    public PantryViewModelFactory(RequestQueue rQ) {
        rQueue = rQ;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PantryViewModel(rQueue);
    }
}
