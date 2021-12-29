package com.example.CovidCare;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class UserData {
    @PrimaryKey (autoGenerate = true)
    public int id;
    public Date timestamp;
    public float heartRate;
    public float breathingRate;
    public float fever;
    public float cough;
    public float tiredness;
    public float shortnessOfBreath;
    public float muscleAches;
    public float chills;
    public float soreThroat;
    public float runningNose;
    public float headache;
    public float chestPain;

    public UserData() {
        fever = 0;
        cough = 0;
        tiredness = 0;
        shortnessOfBreath = 0;
        muscleAches = 0;
        chills = 0;
        soreThroat = 0;
        runningNose = 0;
        headache = 0;
        chestPain = 0;
    }
}
