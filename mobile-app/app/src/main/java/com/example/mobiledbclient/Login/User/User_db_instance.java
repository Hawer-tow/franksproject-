package com.example.mobiledbclient.Login.User;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 2, exportSchema = false) // ✅ Increased version to 2
public abstract class User_db_instance extends RoomDatabase {

    private static User_db_instance instance;

    public abstract User_queries userQueries();

    public static synchronized User_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            User_db_instance.class, "user_database")
                    .fallbackToDestructiveMigration() // ✅ Wipe and recreate on schema change
                    .build();
        }
        return instance;
    }
}
