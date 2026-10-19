package com.example.mobiledbclient.ui.OperationRates.congo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Ugx2CongoRate_queries {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ugx2CongoRate rate);

    @Update
    void update(Ugx2CongoRate rate);

    @Query("SELECT * FROM Ugx2congorates WHERE Ugx2congoratedate = :date LIMIT 1")
    Ugx2CongoRate getRateByDate(String date);

    @Query("SELECT * FROM Ugx2congorates ORDER BY Ugx2congoratedate DESC")
    List<Ugx2CongoRate> getAllRates();
}
