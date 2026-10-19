package com.example.mobiledbclient.ui.OperationRates.rwanda;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Rwanda2UgxRate_queries {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Rwanda2UgxRate rate);

    @Update
    void update(Rwanda2UgxRate rate);

    @Query("SELECT * FROM rwanda2Ugxrates WHERE rwanda2Ugxratedate = :date LIMIT 1")
    Rwanda2UgxRate getRateByDate(String date);

    @Query("SELECT * FROM rwanda2Ugxrates ORDER BY rwanda2Ugxratedate DESC")
    List<Rwanda2UgxRate> getAllRates();
}
