package com.example.mobiledbclient.ui.OperationRates.tanzania;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Tanzania2UgxRate_queries {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Tanzania2UgxRate rate);

    @Update
    void update(Tanzania2UgxRate rate);

    @Query("SELECT * FROM tanzania2Ugxrates WHERE tanzania2Ugxratedate = :date LIMIT 1")
    Tanzania2UgxRate getRateByDate(String date);

    @Query("SELECT * FROM tanzania2Ugxrates ORDER BY tanzania2Ugxratedate DESC")
    List<Tanzania2UgxRate> getAllRates();
}
