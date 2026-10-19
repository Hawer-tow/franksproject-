package com.example.mobiledbclient.ui.Kisenyi.expenses;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {ExpenseEntity.class}, version = 1)

public abstract class expense_db_instance extends RoomDatabase {
    public abstract expense_queries expense_Queries();

    private static expense_db_instance INSTANCE;

    public static synchronized expense_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            expense_db_instance.class, "expense_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // for simplicity; use background threads in production
                    .build();
        }
        return INSTANCE;
    }
}
