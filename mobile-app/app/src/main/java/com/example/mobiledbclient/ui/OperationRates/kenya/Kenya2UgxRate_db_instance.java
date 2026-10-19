package com.example.mobiledbclient.ui.OperationRates.kenya;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Kenya2UgxRate.class}, version = 1)
public abstract class Kenya2UgxRate_db_instance extends RoomDatabase {
    public abstract Kenya2UgxRate_queries Kenya2UgxRate_queries();

    private static volatile Kenya2UgxRate_db_instance INSTANCE;

    public static Kenya2UgxRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Kenya2UgxRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Kenya2UgxRate_db_instance.class, "kenya2ugx_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
