package com.example.mobiledbclient.ui.OperationRates.tanzania;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Ugx2TanzaniaRate.class}, version = 1)
public abstract class Ugx2TanzaniaRate_db_instance extends RoomDatabase {
    public abstract Ugx2TanzaniaRate_queries Ugx2TanzaniaRateDao();

    private static volatile Ugx2TanzaniaRate_db_instance INSTANCE;

    public static Ugx2TanzaniaRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Ugx2TanzaniaRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Ugx2TanzaniaRate_db_instance.class, "Ugx2TanzaniaRate_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
