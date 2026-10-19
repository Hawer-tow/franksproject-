package com.example.mobiledbclient.ui.OperationRates.congo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Congo2UgxRate_queries {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Congo2UgxRate rate);

    @Update
    void update(Congo2UgxRate rate);

    @Query("SELECT * FROM congo2Ugxrates WHERE congo2Ugxratedate = :date LIMIT 1")
    Congo2UgxRate getRateByDate(String date);

    @Query("SELECT * FROM congo2Ugxrates ORDER BY congo2Ugxratedate DESC")
    List<Congo2UgxRate> getAllRates();
}
