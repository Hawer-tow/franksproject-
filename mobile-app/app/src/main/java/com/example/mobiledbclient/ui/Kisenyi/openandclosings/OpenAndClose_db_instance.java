package com.example.mobiledbclient.ui.Kisenyi.openandclosings;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {OpenAndCloseEntity.class}, version = 1)
public abstract class OpenAndClose_db_instance extends RoomDatabase {
    private static OpenAndClose_db_instance instance;

    public abstract OpenAndClose_queries openAndClose_Queries();

    public static synchronized OpenAndClose_db_instance getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            OpenAndClose_db_instance.class, "open_and_close_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
