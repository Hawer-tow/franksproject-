package com.example.mobiledbclient.ui.OperationRates.rwanda;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Rwanda2UgxRate.class}, version = 1)
public abstract class Rwanda2UgxRate_db_instance extends RoomDatabase {
    public abstract Rwanda2UgxRate_queries Rwanda2UgxRateDao();

    private static volatile Rwanda2UgxRate_db_instance INSTANCE;

    public static Rwanda2UgxRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Rwanda2UgxRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Rwanda2UgxRate_db_instance.class, "Rwanda2UgxRate_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
