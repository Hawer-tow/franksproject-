package com.example.mobiledbclient.ui.Kisenyi.sales;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {SellingEntity.class},
        version = 3, // bumped again to reflect schema evolution (saleType, rules)
        exportSchema = true // optional: keeps schema history for migrations
)
public abstract class Selling_db_instance extends RoomDatabase {
    private static Selling_db_instance instance;

    public abstract Selling_queries selling_Queries();

    public static synchronized Selling_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Selling_db_instance.class,
                            "selling_db"
                    )
                    .fallbackToDestructiveMigration() // rebuilds DB if schema mismatch
                    .allowMainThreadQueries() // for simplicity; consider moving to background threads
                    .build();
        }
        return instance;
    }
}
