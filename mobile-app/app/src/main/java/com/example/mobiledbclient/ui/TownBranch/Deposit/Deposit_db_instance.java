package com.example.mobiledbclient.ui.TownBranch.Deposit;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DepositEntity.class}, version = 1)
public abstract class Deposit_db_instance extends RoomDatabase {
    private static Deposit_db_instance instance;

    public abstract Deposit_queries deposit_Queries();

    public static synchronized Deposit_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            Deposit_db_instance.class, "deposit_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
