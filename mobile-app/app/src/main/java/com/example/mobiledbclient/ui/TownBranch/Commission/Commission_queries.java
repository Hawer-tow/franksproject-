package com.example.mobiledbclient.ui.TownBranch.Commission;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface Commission_queries {
    @Insert
    void insert(CommissionEntity entity);

    @Update
    void update(CommissionEntity entity);

    @Delete
    void delete(CommissionEntity entity);

    @Query("SELECT * FROM commission")
    List<CommissionEntity> getAll();

    @Query("SELECT * FROM commission WHERE cid = :cid LIMIT 1")
    CommissionEntity findByCid(String cid);

    @Query("SELECT * FROM commission WHERE synced = 0")
    List<CommissionEntity> getUnsynced();

    @Query("DELETE FROM commission WHERE cid = :cid")
    void deleteByCid(String cid);
}
