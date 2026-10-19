package com.example.mobiledbclient.ui.Customers.allcustomers;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customers {

    @PrimaryKey(autoGenerate = true)
    private int customer_ID;

    private String customer_passwordID;
    private String customer_name;
    private String customer_Phone_No;
    private String customer_email;
    private String customer_primary_country;
    private String customer_secondary_country;
    private String customer_major_transactions;
    private String customer_description;

    // --- Getters and Setters ---

    public int getCustomer_ID() {
        return customer_ID;
    }

    public void setCustomer_ID(int customer_ID) {
        this.customer_ID = customer_ID;
    }

    public String getCustomer_passwordID() {
        return customer_passwordID;
    }

    public void setCustomer_passwordID(String customer_passwordID) {
        this.customer_passwordID = customer_passwordID;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_Phone_No() {
        return customer_Phone_No;
    }

    public void setCustomer_Phone_No(String customer_Phone_No) {
        this.customer_Phone_No = customer_Phone_No;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public String getCustomer_primary_country() {
        return customer_primary_country;
    }

    public void setCustomer_primary_country(String customer_primary_country) {
        this.customer_primary_country = customer_primary_country;
    }

    public String getCustomer_secondary_country() {
        return customer_secondary_country;
    }

    public void setCustomer_secondary_country(String customer_secondary_country) {
        this.customer_secondary_country = customer_secondary_country;
    }

    public String getCustomer_major_transactions() {
        return customer_major_transactions;
    }

    public void setCustomer_major_transactions(String customer_major_transactions) {
        this.customer_major_transactions = customer_major_transactions;
    }

    public String getCustomer_description() {
        return customer_description;
    }

    public void setCustomer_description(String customer_description) {
        this.customer_description = customer_description;
    }
}
