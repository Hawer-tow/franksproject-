package com.example.mobiledbclient.ui.OperationRates.rwanda;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Ugx2RwandaRate_queries {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ugx2RwandaRate rate);

    @Update
    void update(Ugx2RwandaRate rate);

    @Query("SELECT * FROM Ugx2rwandarates WHERE ugx2rwandaratedate = :date LIMIT 1")
    Ugx2RwandaRate getRateByDate(String date);

    @Query("SELECT * FROM Ugx2rwandarates ORDER BY ugx2rwandaratedate DESC")
    List<Ugx2RwandaRate> getAllRates();
}
