package com.example.CovidCare;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.Manifest;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    private static final int VIDEO_CAPTURE = 1;
    private Uri fileUri;
    private int window = 9;
    long startExecTime;
    private TextView heartRateTextView;
    private TextView breathRateTextView;

    private boolean uploadSignClicked = false;
    private boolean ongoingHeartRateProcess = false;
    private boolean ongoingBreathRateProcess = false;

    Camera camera;
    FrameLayout frameLayout;
    CameraPreview showCamera;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    private String rootPath = Environment.getExternalStorageDirectory().getPath();

    private UserDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signs_screen);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Button recordButton = (Button) findViewById(R.id.record_video_button);
        Button measureHeartRateButton = (Button) findViewById(R.id.measure_heartrate_button);
        Button measureBreathRateButton = (Button) findViewById(R.id.measure_breath_button);
        Button uploadSymptomsButton = (Button) findViewById(R.id.upload_symptoms_button);
        Button uploadSignsButton = (Button) findViewById(R.id.upload_signs_button);
        Button viewDatabaseButton = (Button) findViewById(R.id.view_database_button);

        heartRateTextView = (TextView) findViewById(R.id.heart_rate_text_view);
        breathRateTextView = (TextView) findViewById(R.id.breath_rate_text_view);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try{
                    db = UserDatabase.getInstance(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        if(!hasCamera()){
            recordButton.setEnabled(false);
        }

        handlePermissions(MainActivity.this);


        camera = Camera.open();
        showCamera = new CameraPreview(this, camera);
        frameLayout = findViewById(R.id.frame_layout);
        frameLayout.addView(showCamera);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ongoingHeartRateProcess == true) {
                    Toast.makeText(MainActivity.this, "Please wait for process to complete before recording a new video!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (prepareVideoRecorder())
                    {
                        mediaRecorder.start();
                        System.out.println("Recording!");
                        Toast.makeText(getApplicationContext(), "Recording Started!", Toast.LENGTH_SHORT).show();


                        Log.i("Camera", "Stop");

                    } else
                    {
                        releaseMediaRecorder();
                    }
                }
            }
        });

        measureHeartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File videoFile = new File(rootPath + "/heart_rate_video.mp4");
                fileUri = Uri.fromFile(videoFile);

                if(ongoingHeartRateProcess == true) {
                    Toast.makeText(MainActivity.this, "Please wait for process to complete before starting a new one!",
                            Toast.LENGTH_SHORT).show();
                } else if (videoFile.exists()) {
                    ongoingHeartRateProcess = true;
                    heartRateTextView.setText("Calculating heart rate...");

                    startExecTime = System.currentTimeMillis();
                    System.gc();
                    Intent heartIntent = new Intent(MainActivity.this, CovidHeartRate.class);
                    startService(heartIntent);

                } else {
                    Toast.makeText(MainActivity.this, "Please record heart reate video first!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        measureBreathRateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(ongoingBreathRateProcess == true) {
                    Toast.makeText(MainActivity.this, "Please wait for process to complete before starting a new one!",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Place the phone on your chest for 45s", Toast.LENGTH_LONG).show();
                    ongoingBreathRateProcess = true;
                    breathRateTextView.setText("Sensing...");
                    Intent accelIntent = new Intent(MainActivity.this, CovidAccelerometer.class);
                    startService(accelIntent);
                }
            }
        });

        uploadSymptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CovidSymptoms.class);
                intent.putExtra("uploadSignsClicked", uploadSignClicked);
                startActivity(intent);
            }
        });

        uploadSignsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadSignClicked = true;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UserData data = new UserData();
                        data.heartRate = Float.parseFloat(heartRateTextView.getText().toString());
                        data.breathingRate = Float.parseFloat(breathRateTextView.getText().toString());
                        data.timestamp = new Date(System.currentTimeMillis());
                        db.userInfoDao().insert(data);
                    }
                });
                thread.start();

                Toast.makeText(MainActivity.this, "Signs uploaded!", Toast.LENGTH_SHORT).show();
            }

        });

        viewDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GetDatabase.class);
                //intent.putExtra("viewDatabaseClicked", uploadSignsClicked);
                startActivity(intent);
            }
        });

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();
                BreathingRateDetector runnable = new BreathingRateDetector(b.getIntegerArrayList("accelValuesX"));

                Thread thread = new Thread(runnable);
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                breathRateTextView.setText(runnable.breathingRate + "");

                Toast.makeText(MainActivity.this, "Respiratory rate calculated!", Toast.LENGTH_SHORT).show();
                ongoingBreathRateProcess = false;
                b.clear();
                System.gc();

            }
        }, new IntentFilter("broadcastingAccelData"));


        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();
                float heartRate = 0;
                int fail = 0;
                for (int i = 0; i < window; i++) {

                    ArrayList<Integer> heartData = null;
                    heartData = b.getIntegerArrayList("heartData "+i);

                    ArrayList<Integer> denoisedRedness = denoise(heartData, 5);

                    float zeroCrossings = peakFinding(denoisedRedness);
                    heartRate += zeroCrossings/2;
                    Log.i("log", "heart rate for " + i + "= " + zeroCrossings/2);
                }

                heartRate = (heartRate*12)/ window;
                Log.i("log", "Final heart rate = " + heartRate);
                heartRateTextView.setText(heartRate + "");
                ongoingHeartRateProcess = false;
                Toast.makeText(MainActivity.this, "Heart rate calculated!", Toast.LENGTH_SHORT).show();
                System.gc();
                b.clear();

            }
        }, new IntentFilter("broadcastingHeartData"));

    }

    @Override
    protected void onResume() {
        super.onResume();

        camera = Camera.open();
        showCamera = new CameraPreview(this, camera);
        frameLayout = findViewById(R.id.frame_layout);
        frameLayout.addView(showCamera);
    }
    private boolean prepareVideoRecorder() {

        camera = Camera.open();
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        mediaRecorder = new MediaRecorder();

        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

        File mediaFile = new File(rootPath + "/heart_rate_video.mp4");

        mediaRecorder.setMaxDuration(45000);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    Toast.makeText(getApplicationContext(), "Recording complete!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mediaRecorder.setOutputFile(mediaFile.toString());

        mediaRecorder.setPreviewDisplay(showCamera.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("Error", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("Error", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        uploadSignClicked = false;
    }

    public class BreathingRateDetector implements Runnable{

        public float breathingRate;
        ArrayList<Integer> accelValuesX;

        BreathingRateDetector(ArrayList<Integer> accelValuesX){
            this.accelValuesX = accelValuesX;
        }

        @Override
        public void run() {

            ArrayList<Integer> accelValuesXDenoised = denoise(accelValuesX, 10);

            int  zeroCrossings = peakFinding(accelValuesXDenoised);
            breathingRate = (zeroCrossings*60)/90;
            Log.i("log", "Respiratory rate = " + breathingRate);
        }

    }

    public ArrayList<Integer> denoise(ArrayList<Integer> data, int filter){

        ArrayList<Integer> movingAvgArr = new ArrayList<>();
        int movingAvg = 0;

        for(int i=0; i< data.size(); i++){
            movingAvg += data.get(i);
            if(i+1 < filter) {
                continue;
            }
            movingAvgArr.add((movingAvg)/filter);
            movingAvg -= data.get(i+1 - filter);
        }

        return movingAvgArr;

    }

    public int peakFinding(ArrayList<Integer> data) {

        int diff, prev, slope = 0, zeroCrossings = 0;
        int j = 0;
        prev = data.get(0);

        while(slope == 0 && j + 1 < data.size()){
            diff = data.get(j + 1) - data.get(j);
            if(diff != 0){
                slope = diff/abs(diff);
            }
            j++;
        }

        for(int i = 1; i<data.size(); i++) {

            diff = data.get(i) - prev;
            prev = data.get(i);

            if(diff == 0) continue;

            int currSlope = diff/abs(diff);

            if(currSlope == -1* slope){
                slope *= -1;
                zeroCrossings++;
            }
        }

        return zeroCrossings;
    }


    public static void handlePermissions(Activity activity) {

        int storagePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int REQUEST_EXTERNAL_STORAGE = 1;

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA

        };

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i("log", "Read/Write Permissions needed!");
        }

        ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS,
                REQUEST_EXTERNAL_STORAGE
        );

        Log.i("log", "Permissions Granted!");

    }

    private boolean hasCamera() {

        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        } else {
            return false;
        }
    }

    

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean deleteFile = false;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {

                MediaMetadataRetriever videoRetriever = new MediaMetadataRetriever();
                FileInputStream input = null;
                try {
                    input = new FileInputStream(fileUri.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    videoRetriever.setDataSource(input.getFD());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String timeString = videoRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long time = Long.parseLong(timeString)/1000;

                if(time<45) {

                    Toast.makeText(this,
                            "Please record video for a minimum of 45 seconds! ", Toast.LENGTH_SHORT).show();
                    deleteFile = true;
                } else{
                    Toast.makeText(this, "Video saved to:\n" +
                            data.getData(), Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled!",
                        Toast.LENGTH_SHORT).show();
                deleteFile = true;
            } else {
                Toast.makeText(this, "Failed to record video!",
                        Toast.LENGTH_SHORT).show();
            }

            if(deleteFile) {
                File fdelete = new File(fileUri.getPath());

                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("Recording has been deleted");
                    }
                }
            }
            fileUri = null;
        }
    }
}
