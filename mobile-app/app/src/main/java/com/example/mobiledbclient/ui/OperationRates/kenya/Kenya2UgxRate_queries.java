package com.example.mobiledbclient.ui.OperationRates.kenya;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Kenya2UgxRate_queries {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Kenya2UgxRate rate);

    @Update
    void update(Kenya2UgxRate rate);

    @Query("SELECT * FROM Kenya2Ugxrates WHERE kenya2Ugxratedate = :date LIMIT 1")
    Kenya2UgxRate getRateByDate(String date);

    @Query("SELECT * FROM Kenya2Ugxrates ORDER BY kenya2Ugxratedate DESC")
    List<Kenya2UgxRate> getAllRates();
}
