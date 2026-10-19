package com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MpesaBuysViewModel extends ViewModel {

    // Phone card fields
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<String> phoneAmount = new MutableLiveData<>();
    private final MutableLiveData<String> phoneRate = new MutableLiveData<>();

    public LiveData<String> getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String val) { phoneNumber.setValue(val); }

    public LiveData<String> getPhoneAmount() { return phoneAmount; }
    public void setPhoneAmount(String val) { phoneAmount.setValue(val); }

    public LiveData<String> getPhoneRate() { return phoneRate; }
    public void setPhoneRate(String val) { phoneRate.setValue(val); }
}
