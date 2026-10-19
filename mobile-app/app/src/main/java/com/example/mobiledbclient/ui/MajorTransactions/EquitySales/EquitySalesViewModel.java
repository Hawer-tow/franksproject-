package com.example.mobiledbclient.ui.MajorTransactions.EquitySales;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EquitySalesViewModel extends ViewModel {

    // --- Phone transfer fields ---
    private final MutableLiveData<String> phoneAccount = new MutableLiveData<>();
    private final MutableLiveData<String> phoneTelco = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<String> phoneAmount = new MutableLiveData<>();
    private final MutableLiveData<String> phoneRate = new MutableLiveData<>();

    // --- Account-to-Account transfer fields ---
    private final MutableLiveData<String> accountSender = new MutableLiveData<>();
    private final MutableLiveData<String> accountReceiver = new MutableLiveData<>();
    private final MutableLiveData<String> accountAmount = new MutableLiveData<>();
    private final MutableLiveData<String> accountRate = new MutableLiveData<>();

    // --- Getters/Setters for Phone transfer ---
    public LiveData<String> getPhoneAccount() { return phoneAccount; }
    public void setPhoneAccount(String val) { phoneAccount.setValue(val); }

    public LiveData<String> getPhoneTelco() { return phoneTelco; }
    public void setPhoneTelco(String val) { phoneTelco.setValue(val); }

    public LiveData<String> getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String val) { phoneNumber.setValue(val); }

    public LiveData<String> getPhoneAmount() { return phoneAmount; }
    public void setPhoneAmount(String val) { phoneAmount.setValue(val); }

    public LiveData<String> getPhoneRate() { return phoneRate; }
    public void setPhoneRate(String val) { phoneRate.setValue(val); }

    // --- Getters/Setters for Account transfer ---
    public LiveData<String> getAccountSender() { return accountSender; }
    public void setAccountSender(String val) { accountSender.setValue(val); }

    public LiveData<String> getAccountReceiver() { return accountReceiver; }
    public void setAccountReceiver(String val) { accountReceiver.setValue(val); }

    public LiveData<String> getAccountAmount() { return accountAmount; }
    public void setAccountAmount(String val) { accountAmount.setValue(val); }

    public LiveData<String> getAccountRate() { return accountRate; }
    public void setAccountRate(String val) { accountRate.setValue(val); }
}
