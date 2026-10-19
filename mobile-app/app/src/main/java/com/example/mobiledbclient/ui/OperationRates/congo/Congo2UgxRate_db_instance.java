package com.example.mobiledbclient.ui.OperationRates.congo;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Congo2UgxRate.class}, version = 1)
public abstract class Congo2UgxRate_db_instance extends RoomDatabase {
    public abstract Congo2UgxRate_queries Congo2UgxRateDao();

    private static volatile Congo2UgxRate_db_instance INSTANCE;

    public static Congo2UgxRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Congo2UgxRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Congo2UgxRate_db_instance.class, "Congo2UgxRate_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
