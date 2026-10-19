package com.example.mobiledbclient.ui.TownBranch.Withdraw;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface Withdraw_queries {
    @Insert
    void insert(WithdrawEntity entity);

    @Update
    void update(WithdrawEntity entity);

    @Delete
    void delete(WithdrawEntity entity);

    @Query("SELECT * FROM withdraw")
    List<WithdrawEntity> getAll();

    @Query("SELECT * FROM withdraw WHERE tid = :tid LIMIT 1")
    WithdrawEntity findByTid(String tid);

    @Query("SELECT * FROM withdraw WHERE synced = 0")
    List<WithdrawEntity> getUnsynced();

    @Query("DELETE FROM withdraw WHERE tid = :tid")
    void deleteByTid(String tid);
}
