package com.hurst.digitalpantry.ui.main.Session;

import com.hurst.digitalpantry.ui.main.Pantry.PantryItem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SessionViewModel extends ViewModel {

    MutableLiveData<List<PantryItem>> mSessionList;

    public LiveData<List<PantryItem>> getmSessionList() {
        if(mSessionList == null) {
            mSessionList = new MutableLiveData<>();
            mSessionList.setValue(new ArrayList<PantryItem>());
            // load any data that might want to be added from the beginning
        }
        return mSessionList;
    }
}
