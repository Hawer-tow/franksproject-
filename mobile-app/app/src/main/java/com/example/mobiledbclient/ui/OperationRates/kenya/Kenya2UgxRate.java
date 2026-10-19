package com.example.mobiledbclient.ui.OperationRates.kenya;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Kenya2Ugxrates")
public class Kenya2UgxRate {
    @PrimaryKey
    @NonNull
    public String kenya2Ugxratedate;

    public double kenya2Ugxrate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
