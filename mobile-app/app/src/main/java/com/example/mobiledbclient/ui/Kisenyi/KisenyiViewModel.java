package com.example.mobiledbclient.ui.Kisenyi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class KisenyiViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public KisenyiViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}