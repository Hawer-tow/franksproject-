package com.example.mobiledbclient.ui.Kisenyi.expenses;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface expense_queries {
    @Query("SELECT * FROM expenses WHERE synced = 0 OR markedForDelete = 1")
    List<ExpenseEntity> getUnsynced();

    @Insert
    void insert(ExpenseEntity expense);

    @Update
    void update(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("SELECT * FROM expenses WHERE serverId = :serverId LIMIT 1")
    ExpenseEntity findByServerId(Integer serverId);

    @Query("SELECT * FROM expenses")
    List<ExpenseEntity> getAll();

    @Query("SELECT * FROM expenses WHERE date(date) >= date('now', '-' || :days || ' days')")
    List<ExpenseEntity> getExpensesInRange(int days);


    @Query("SELECT * FROM expenses WHERE date BETWEEN date('now', 'start of month') AND date('now')")
    List<ExpenseEntity> getExpensesMonthly();

    @Query("SELECT * FROM expenses")
    List<ExpenseEntity> getAllExpenses();

    @Query("DELETE FROM expenses")
    void clearAll();

}
