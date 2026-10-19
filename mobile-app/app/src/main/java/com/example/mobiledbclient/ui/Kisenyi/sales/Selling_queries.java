package com.example.mobiledbclient.ui.Kisenyi.sales;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Selling_queries {
    @Insert
    void insert(SellingEntity selling);

    @Update
    void update(SellingEntity selling);

    @Query("DELETE FROM selling WHERE localId = :id")
    void deleteById(int id);

    @Query("SELECT * FROM selling")
    List<SellingEntity> getAll();

    @Query("SELECT * FROM selling WHERE synced = 0")
    List<SellingEntity> getUnsynced();

    @Query("SELECT * FROM selling WHERE serverId = :serverId LIMIT 1")
    SellingEntity findByServerId(int serverId);

    @Query("DELETE FROM selling")
    void clearAll();

    @Query("SELECT * FROM selling WHERE date(substr(sellingDate,1,10)) >= date('now', '-' || :days || ' days')")
    List<SellingEntity> getSellingInRange(int days);

    @Query("SELECT * FROM selling WHERE strftime('%Y-%m', substr(sellingDate,1,10)) = strftime('%Y-%m', 'now')")
    List<SellingEntity> getSellingMonthly();

    // --- New queries for saleType filtering ---
    @Query("SELECT * FROM selling WHERE saleType = :type")
    List<SellingEntity> getSellingByType(String type);

    @Query("SELECT * FROM selling WHERE saleType = :type AND strftime('%Y-%m', substr(sellingDate,1,10)) = strftime('%Y-%m', 'now')")
    List<SellingEntity> getSellingMonthlyByType(String type);

    @Query("SELECT * FROM selling WHERE saleType LIKE 'USER_INITIATED%'")
    List<SellingEntity> getUserInitiated();

    @Query("SELECT * FROM selling WHERE saleType IN ('BANK','MPESA')")
    List<SellingEntity> getSystemGenerated();
}
