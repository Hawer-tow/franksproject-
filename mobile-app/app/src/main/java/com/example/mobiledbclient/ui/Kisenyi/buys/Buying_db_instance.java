package com.example.mobiledbclient.ui.Kisenyi.buys;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {BuyingEntity.class}, version = 1)
public abstract class Buying_db_instance extends RoomDatabase {
    private static Buying_db_instance instance;

    public abstract Buying_queries buying_Queries();

    public static synchronized Buying_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            Buying_db_instance.class, "buying_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
