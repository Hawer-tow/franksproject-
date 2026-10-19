package com.example.mobiledbclient.ui.OperationRates.rwanda;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rwanda2Ugxrates")
public class Rwanda2UgxRate {
    @PrimaryKey
    @NonNull
    public String rwanda2Ugxratedate;

    public double rwanda2Ugxrate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
