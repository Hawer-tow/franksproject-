package com.example.mobiledbclient.ui.OperationRates.tanzania;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tanzania2Ugxrates")
public class Tanzania2UgxRate {
    @PrimaryKey
    @NonNull
    public String tanzania2Ugxratedate;

    public double tanzania2Ugxrate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
