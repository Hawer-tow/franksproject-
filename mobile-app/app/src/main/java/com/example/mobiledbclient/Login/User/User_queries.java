package com.example.mobiledbclient.Login.User;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface User_queries {

    // Insert a single user, replace if conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    // Insert a list of users at once
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    // Get a user by username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    // Get all users
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    // Clear all users (used before syncing from server)
    @Query("DELETE FROM users")
    void clearUsers();

    // Delete a single user
    @Delete
    void deleteUser(User user);

    @Query("DELETE FROM users")
    void clearAll();
}
