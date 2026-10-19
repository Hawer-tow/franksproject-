package com.example.mobiledbclient.ui.Customers.allcustomers;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface customers_queries {

    @Insert
    void insert(Customers customer);

    @Update
    void update(Customers customer);

    @Delete
    void delete(Customers customer);

    @Query("SELECT * FROM customers")
    List<Customers> getAllCustomers();

    @Query("SELECT * FROM customers WHERE customer_ID = :id LIMIT 1")
    Customers getCustomerById(int id);

    @Query("DELETE FROM customers")
    void clearAll();
}
