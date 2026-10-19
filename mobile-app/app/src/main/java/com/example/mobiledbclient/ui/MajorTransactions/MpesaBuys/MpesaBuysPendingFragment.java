package com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mobiledbclient.R;

public class MpesaBuysPendingFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mpesa_buys_pending_card, container, false);
    }
}
