package com.example.mobiledbclient.ui.OperationRates.kenya;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Ugx2KenyaRate_queries {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ugx2KenyaRate rate);

    @Update
    void update(Ugx2KenyaRate rate);

    @Query("SELECT * FROM Ugx2Kenyarates WHERE ugx2kenyaratedate = :date LIMIT 1")
    Ugx2KenyaRate getRateByDate(String date);

    @Query("SELECT * FROM Ugx2Kenyarates ORDER BY ugx2kenyaratedate DESC")
    List<Ugx2KenyaRate> getAllRates();
}
