package com.example.mobiledbclient.ui.OperationRates.congo;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ugx2congorates")
public class Ugx2CongoRate {
    @PrimaryKey
    @NonNull
    public String ugx2congoratedate;

    public double ugx2congorate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
