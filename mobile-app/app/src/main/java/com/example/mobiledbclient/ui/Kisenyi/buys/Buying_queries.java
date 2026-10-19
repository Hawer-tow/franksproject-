package com.example.mobiledbclient.ui.Kisenyi.buys;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Buying_queries {
    @Insert
    void insert(BuyingEntity buying);

    @Update
    void update(BuyingEntity buying);

    @Query("DELETE FROM buying WHERE localId = :id")
    void deleteById(int id);

    @Query("SELECT * FROM buying")
    List<BuyingEntity> getAll();

    @Query("SELECT * FROM buying WHERE synced = 0")
    List<BuyingEntity> getUnsynced();

    @Query("SELECT * FROM buying WHERE serverId = :serverId LIMIT 1")
    BuyingEntity findByServerId(int serverId);

    @Query("SELECT * FROM buying WHERE date(buyingDate) >= date('now', '-' || :days || ' days')")
    List<BuyingEntity> getBuyingInRange(int days);

    @Query("SELECT * FROM buying WHERE strftime('%Y-%m', buyingDate) = strftime('%Y-%m', 'now')")
    List<BuyingEntity> getBuyingMonthly();

    @Query("DELETE FROM buying")
    void clearAll();

}

