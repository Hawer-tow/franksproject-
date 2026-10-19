package com.example.mobiledbclient.ui.OperationRates.tanzania;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Ugx2TanzaniaRate_queries {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ugx2TanzaniaRate rate);

    @Update
    void update(Ugx2TanzaniaRate rate);

    @Query("SELECT * FROM Ugx2tanzaniarates WHERE ugx2tanzaniaratedate = :date LIMIT 1")
    Ugx2TanzaniaRate getRateByDate(String date);

    @Query("SELECT * FROM Ugx2tanzaniarates ORDER BY ugx2tanzaniaratedate DESC")
    List<Ugx2TanzaniaRate> getAllRates();
}
