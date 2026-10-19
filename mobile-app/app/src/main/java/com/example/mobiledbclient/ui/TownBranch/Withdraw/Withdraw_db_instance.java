package com.example.mobiledbclient.ui.TownBranch.Withdraw;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WithdrawEntity.class}, version = 1)
public abstract class Withdraw_db_instance extends RoomDatabase {
    private static Withdraw_db_instance instance;

    public abstract Withdraw_queries withdraw_Queries();

    public static synchronized Withdraw_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            Withdraw_db_instance.class, "withdraw_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
