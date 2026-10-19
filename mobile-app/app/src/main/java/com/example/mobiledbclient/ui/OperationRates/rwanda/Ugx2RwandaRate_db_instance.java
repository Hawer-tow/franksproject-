package com.example.mobiledbclient.ui.OperationRates.rwanda;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Ugx2RwandaRate.class}, version = 1)
public abstract class Ugx2RwandaRate_db_instance extends RoomDatabase {
    public abstract Ugx2RwandaRate_queries Ugx2RwandaRateDao();

    private static volatile Ugx2RwandaRate_db_instance INSTANCE;

    public static Ugx2RwandaRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Ugx2RwandaRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Ugx2RwandaRate_db_instance.class, "Ugx2RwandaRate_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
