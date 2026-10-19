package com.example.mobiledbclient.ui.OperationRates.congo;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Ugx2CongoRate.class}, version = 1)
public abstract class Ugx2CongoRate_db_instance extends RoomDatabase {
    public abstract Ugx2CongoRate_queries Ugx2CongoRateDao();

    private static volatile Ugx2CongoRate_db_instance INSTANCE;

    public static Ugx2CongoRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Ugx2CongoRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Ugx2CongoRate_db_instance.class, "Ugx2CongoRate_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
