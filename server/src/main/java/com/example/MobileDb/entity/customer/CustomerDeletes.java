package com.example.MobileDb.entity.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "customer_deletes")
public class CustomerDeletes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int delete_id;

    private int customer_id;
    private String customer_name;
    private String customer_phone;
    private String customer_email;
    private String customer_primary_country;
    private String customer_secondary_country;
    private String customer_major_transactions;
    private String customer_description;
    private Timestamp deleted_at;
    private String deleted_by;

    public CustomerDeletes() {}

    public CustomerDeletes(Customers customer, String deletedBy) {
        this.customer_id = customer.getCustomer_ID();
        this.customer_name = customer.getCustomer_name();
        this.customer_phone = customer.getCustomer_Phone_No();
        this.customer_email = customer.getCustomer_email();
        this.customer_primary_country = customer.getCustomer_primary_country();
        this.customer_secondary_country = customer.getCustomer_secondary_country();
        this.customer_major_transactions = customer.getCustomer_major_transactions();
        this.customer_description = customer.getCustomer_description();
        this.deleted_at = new Timestamp(System.currentTimeMillis());
        this.deleted_by = deletedBy;
    }

    // getters and setters...
}
