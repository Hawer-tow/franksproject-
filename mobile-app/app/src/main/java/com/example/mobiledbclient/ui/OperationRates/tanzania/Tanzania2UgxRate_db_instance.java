package com.example.mobiledbclient.ui.OperationRates.tanzania;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Tanzania2UgxRate.class}, version = 1)
public abstract class Tanzania2UgxRate_db_instance extends RoomDatabase {
    public abstract Tanzania2UgxRate_queries Tanzania2UgxRateDao();

    private static volatile Tanzania2UgxRate_db_instance INSTANCE;

    public static Tanzania2UgxRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Tanzania2UgxRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Tanzania2UgxRate_db_instance.class, "Tanzania2UgxRate_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
