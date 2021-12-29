package com.example.CovidCare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class CovidAccelerometer extends Service implements SensorEventListener {

    private Sensor senseAcc;
    private SensorManager accManager;

    private ArrayList<Integer> accValuesX = new ArrayList<>();
    private ArrayList<Integer> accValuesY = new ArrayList<>();
    private ArrayList<Integer> accValuesZ = new ArrayList<>();

    @Override
    public void onCreate(){
        Log.i("log", "Accel Service started");
        accManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAcc = accManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accManager.registerListener(this, senseAcc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accValuesX.clear();
        accValuesY.clear();
        accValuesZ.clear();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor genericSensor = sensorEvent.sensor;
        if (genericSensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            accValuesX.add((int)(sensorEvent.values[0] * 100));
            accValuesY.add((int)(sensorEvent.values[1] * 100));
            accValuesZ.add((int)(sensorEvent.values[2] * 100));

            if(accValuesX.size() >= 300){
                stopSelf();
            }
        }
    }

    @Override
    public void onDestroy(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                accManager.unregisterListener(CovidAccelerometer.this);
                Log.i("service", "Service stopping");

                Intent intent = new Intent("broadcastingAccelData");
                Bundle b = new Bundle();
                b.putIntegerArrayList("accelValuesX", accValuesX);
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(CovidAccelerometer.this).sendBroadcast(intent);
            }
        });
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
