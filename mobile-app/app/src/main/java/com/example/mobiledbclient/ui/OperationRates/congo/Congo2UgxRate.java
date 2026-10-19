package com.example.mobiledbclient.ui.OperationRates.congo;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "congo2Ugxrates")
public class Congo2UgxRate {
    @PrimaryKey
    @NonNull
    public String congo2Ugxratedate;

    public double congo2Ugxrate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
