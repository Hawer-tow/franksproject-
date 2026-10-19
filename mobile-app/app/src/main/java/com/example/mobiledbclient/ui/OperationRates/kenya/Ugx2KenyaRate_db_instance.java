package com.example.mobiledbclient.ui.OperationRates.kenya;



import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Ugx2KenyaRate.class}, version = 1)
public abstract class Ugx2KenyaRate_db_instance extends RoomDatabase {
    public abstract Ugx2KenyaRate_queries ugx2KenyaRateDao();

    private static volatile Ugx2KenyaRate_db_instance INSTANCE;

    public static Ugx2KenyaRate_db_instance getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Ugx2KenyaRate_db_instance.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Ugx2KenyaRate_db_instance.class, "ugx2kenya_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

