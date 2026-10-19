package com.example.mobiledbclient.ui.OperationRates.kenya;



import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ugx2Kenyarates")
public class Ugx2KenyaRate {
    @PrimaryKey
    @NonNull
    public String ugx2kenyaratedate;

    public double ugx2kenyarate;
    public double min_transactionvalue;
    public double max_transactionValue;
}



