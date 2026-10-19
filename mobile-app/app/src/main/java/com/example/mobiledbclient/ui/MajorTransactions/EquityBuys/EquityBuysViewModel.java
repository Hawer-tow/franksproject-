package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EquityBuysViewModel extends ViewModel {

    // Phone card fields
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<String> phoneAmount = new MutableLiveData<>();
    private final MutableLiveData<String> phoneRate = new MutableLiveData<>();
    private final MutableLiveData<String> phoneTelco = new MutableLiveData<>(); // NEW FIELD

    // Account card fields
    private final MutableLiveData<String> accountNumber = new MutableLiveData<>();
    private final MutableLiveData<String> accountAmount = new MutableLiveData<>();
    private final MutableLiveData<String> accountRate = new MutableLiveData<>();

    // Getters/Setters for phone fields
    public LiveData<String> getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String val) { phoneNumber.setValue(val); }

    public LiveData<String> getPhoneAmount() { return phoneAmount; }
    public void setPhoneAmount(String val) { phoneAmount.setValue(val); }

    public LiveData<String> getPhoneRate() { return phoneRate; }
    public void setPhoneRate(String val) { phoneRate.setValue(val); }

    public LiveData<String> getPhoneTelco() { return phoneTelco; }   // NEW GETTER
    public void setPhoneTelco(String val) { phoneTelco.setValue(val); } // NEW SETTER

    // Getters/Setters for account fields
    public LiveData<String> getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String val) { accountNumber.setValue(val); }

    public LiveData<String> getAccountAmount() { return accountAmount; }
    public void setAccountAmount(String val) { accountAmount.setValue(val); }

    public LiveData<String> getAccountRate() { return accountRate; }
    public void setAccountRate(String val) { accountRate.setValue(val); }
}
