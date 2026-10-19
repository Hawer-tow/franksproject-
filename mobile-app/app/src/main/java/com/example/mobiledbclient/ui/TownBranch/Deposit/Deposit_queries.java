package com.example.mobiledbclient.ui.TownBranch.Deposit;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface Deposit_queries {
    @Insert
    void insert(DepositEntity entity);

    @Update
    void update(DepositEntity entity);

    @Delete
    void delete(DepositEntity entity);

    @Query("SELECT * FROM deposit")
    List<DepositEntity> getAll();

    @Query("SELECT * FROM deposit WHERE tid = :tid LIMIT 1")
    DepositEntity findByTid(String tid);

    @Query("SELECT * FROM deposit WHERE synced = 0")
    List<DepositEntity> getUnsynced();

    @Query("DELETE FROM deposit WHERE tid = :tid")
    void deleteByTid(String tid);
}
