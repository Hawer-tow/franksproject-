package com.example.mobiledbclient.ui.Customers.allcustomers;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Customers.class}, version = 1, exportSchema = false)
public abstract class customers_db_instance extends RoomDatabase {

    private static volatile customers_db_instance INSTANCE;

    public abstract customers_queries customers_Queries();

    public static customers_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (customers_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    customers_db_instance.class, "customers_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
