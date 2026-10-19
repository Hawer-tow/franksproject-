package com.example.mobiledbclient.ui.MajorTransactions.MpesaSales;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobiledbclient.ui.Kisenyi.sales.Selling;

import java.util.ArrayList;
import java.util.List;

public class MpesaTransactionsViewModel extends ViewModel {

    private final MutableLiveData<List<Selling>> transactions = new MutableLiveData<>(new ArrayList<>());

    // Fragment 1 (Paybill → Phone)
    private final MutableLiveData<String> paybillNumber = new MutableLiveData<>();
    private final MutableLiveData<String> recipientPhone1 = new MutableLiveData<>();
    private final MutableLiveData<String> amount1 = new MutableLiveData<>();
    private final MutableLiveData<String> rate1 = new MutableLiveData<>();
    private final MutableLiveData<String> description1 = new MutableLiveData<>();

    // Fragment 2 (Sender Paybill → Recipient Paybill, optional Recipient Phone)
    private final MutableLiveData<String> senderPaybill = new MutableLiveData<>();
    private final MutableLiveData<String> recipientPaybill = new MutableLiveData<>();
    private final MutableLiveData<String> recipientPhone = new MutableLiveData<>();
    private final MutableLiveData<String> amount = new MutableLiveData<>();
    private final MutableLiveData<String> rate = new MutableLiveData<>();
    private final MutableLiveData<String> description = new MutableLiveData<>();

    public LiveData<List<Selling>> getTransactions() { return transactions; }

    public void addTransaction(Selling s) {
        List<Selling> current = transactions.getValue();
        current.add(s);
        transactions.setValue(current);
    }

    // Fragment 1 getters/setters
    public LiveData<String> getPaybillNumber() { return paybillNumber; }
    public void setPaybillNumber(String val) { paybillNumber.setValue(val); }

    public LiveData<String> getRecipientPhone1() { return recipientPhone1; }
    public void setRecipientPhone1(String val) { recipientPhone1.setValue(val); }

    public LiveData<String> getAmount1() { return amount1; }
    public void setAmount1(String val) { amount1.setValue(val); }

    public LiveData<String> getRate1() { return rate1; }
    public void setRate1(String val) { rate1.setValue(val); }

    public LiveData<String> getDescription1() { return description1; }
    public void setDescription1(String val) { description1.setValue(val); }

    // Fragment 2 getters/setters
    public LiveData<String> getSenderPaybill() { return senderPaybill; }
    public void setSenderPaybill(String val) { senderPaybill.setValue(val); }

    public LiveData<String> getRecipientPaybill() { return recipientPaybill; }
    public void setRecipientPaybill(String val) { recipientPaybill.setValue(val); }

    public LiveData<String> getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String val) { recipientPhone.setValue(val); }

    public LiveData<String> getAmount() { return amount; }
    public void setAmount(String val) { amount.setValue(val); }

    public LiveData<String> getRate() { return rate; }
    public void setRate(String val) { rate.setValue(val); }

    public LiveData<String> getDescription() { return description; }
    public void setDescription(String val) { description.setValue(val); }

    // Transaction trigger stub (to be implemented with repository/backend call)
    public void sendTransaction(String senderPaybill,
                                String recipientPaybill,
                                String recipientPhone,
                                String amount,
                                String rate,
                                String description,
                                TransactionCallback callback) {
        // TODO: Implement backend call to your server here
        // For now, simulate success
        if (callback != null) {
            callback.onComplete(true);
        }
    }

    public interface TransactionCallback {
        void onComplete(boolean success);
    }
}
