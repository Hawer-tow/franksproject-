package com.example.mobiledbclient.ui.Kisenyi.openandclosings;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface OpenAndClose_queries {
    @Insert
    void insert(OpenAndCloseEntity entity);

    @Update
    void update(OpenAndCloseEntity entity);

    @Delete
    void delete(OpenAndCloseEntity entity);

    @Query("SELECT * FROM open_and_close")
    List<OpenAndCloseEntity> getAll();

    @Query("SELECT * FROM open_and_close WHERE date = :date LIMIT 1")
    OpenAndCloseEntity findByDate(String date);

    @Query("SELECT * FROM open_and_close WHERE synced = 0")
    List<OpenAndCloseEntity> getUnsynced();

    @Query("DELETE FROM open_and_close WHERE date = :date")
    void deleteByDate(String date);
    @Query("DELETE FROM open_and_close")
    void clearAll();
    @Query("SELECT * FROM open_and_close WHERE date(date) >= date('now', '-' || :days || ' days')")
    List<OpenAndCloseEntity> getInRange(int days);

    @Query("SELECT * FROM open_and_close WHERE strftime('%Y-%m', date) = strftime('%Y-%m', 'now')")
    List<OpenAndCloseEntity> getMonthly();
}


