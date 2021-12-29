package com.example.CovidCare;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDataDao {

    @Query("select * from  UserData")
    List<UserData> getAllData();

    @Query("SELECT * FROM UserData where timestamp=(SELECT MAX(timestamp) FROM UserData)")
    public UserData getLatestData();

    @Insert
    public long insert(UserData userData);

    @Update
    public int update(UserData userData);
}
