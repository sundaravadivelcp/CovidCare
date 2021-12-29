package com.example.CovidCare;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {UserData.class}, version = 1)
@TypeConverters(TimeStampConverters.class)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDataDao userInfoDao();
    private static UserDatabase dbInstance;
    public static synchronized UserDatabase getInstance(Context context){
        if(dbInstance == null){
            dbInstance = Room
                    .databaseBuilder(context.getApplicationContext(), UserDatabase.class, "sundar")
                    .allowMainThreadQueries().build();
        }
        return dbInstance;
    }
}
