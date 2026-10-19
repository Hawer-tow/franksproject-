package com.example.mobiledbclient.ui.TownBranch.Commission;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CommissionEntity.class}, version = 1)
public abstract class Commission_db_instance extends RoomDatabase {
    private static Commission_db_instance instance;

    public abstract Commission_queries commission_Queries();

    public static synchronized Commission_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            Commission_db_instance.class, "commission_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
