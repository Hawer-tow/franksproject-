package com.example.mobiledbclient.ui.OperationRates.tanzania;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ugx2tanzaniarates")
public class Ugx2TanzaniaRate {
    @PrimaryKey
    @NonNull
    public String ugx2tanzaniaratedate;

    public double ugx2tanzaniarate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
