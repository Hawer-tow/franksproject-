package com.example.mobiledbclient.ui.TownBranch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TownBranchViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TownBranchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}