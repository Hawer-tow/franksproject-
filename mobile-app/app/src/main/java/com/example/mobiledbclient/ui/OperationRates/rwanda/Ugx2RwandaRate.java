package com.example.mobiledbclient.ui.OperationRates.rwanda;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ugx2rwandarates")
public class Ugx2RwandaRate {
    @PrimaryKey
    @NonNull
    public String ugx2rwandaratedate;

    public double ugx2rwandarate;
    public double min_transactionvalue;
    public double max_transactionValue;
}
