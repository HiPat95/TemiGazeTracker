package com.example.temicam;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnDetectionDataChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.listeners.OnUserInteractionChangedListener;
import com.robotemi.sdk.model.DetectionData;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        OnDetectionStateChangedListener,
        OnDetectionDataChangedListener,
        OnUserInteractionChangedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static Robot mRobot;

    private double angular_dist = 0;

    private double angular_tilt_dist = 0;
    private int time = 0;

    ImageView track1,track2,track3, track4, track5;
    ImageView trackSX1,trackSX2,trackSX3, trackSX4, trackSX5;
    ImageView closing;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize robot instance
        mRobot = Robot.getInstance();
        track1 = findViewById(R.id.track1);
        track2 = findViewById(R.id.track2);
        track3 = findViewById(R.id.track3);
        track4 = findViewById(R.id.track4);
        track5 = findViewById(R.id.track5);
        trackSX1 = findViewById(R.id.trackSX1);
        trackSX2 = findViewById(R.id.trackSX2);
        trackSX3 = findViewById(R.id.trackSX3);
        trackSX4 = findViewById(R.id.trackSX4);
        trackSX5 = findViewById(R.id.trackSX5);
        closing = findViewById(R.id.closing);
        track1.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add robot event listeners
        mRobot.addOnRobotReadyListener(this);
        mRobot.addOnDetectionStateChangedListener(this);
        mRobot.addOnDetectionDataChangedListener(this);
        mRobot.addOnUserInteractionChangedListener(this);


    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        mRobot.removeOnRobotReadyListener(this);
        mRobot.removeOnDetectionStateChangedListener(this);
        mRobot.removeOnDetectionDataChangedListener(this);
        mRobot.removeOnUserInteractionChangedListener(this);



    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            mRobot.hideTopBar(); // hide temi's top action bar when skill is active

            /*
             * |----------------------|------------------|---------------------------------------------|
             * | setDetectionModeOn() | setTrackUserOn() | Result                                      |
             * |----------------------|------------------|---------------------------------------------|
             * |         true         |       true       | Detection mode is on and tracking is on     |
             * |         true         |       false      | Detection mode is on and tracking is off    |
             * |         false        |       true       | Detection mode is **on** and tracking is on |
             * |         false        |       false      | Detection mode is off and tracking is off   |
             * |----------------------|------------------|---------------------------------------------|
             */
            Log.i(TAG, "Set detection mode: ON");
            mRobot.setDetectionModeOn(true, 2.0f); // Set detection mode on; set detection distance to be 2.0 m

           // Log.i(TAG, "Set track user: ON");
           mRobot.setTrackUserOn(false); // Set tracking mode on
            // Note: When exiting the application, track user will still be enabled unless manually disabled
        }
    }

    @Override
    public void onDetectionStateChanged(int state) {
        final TextView textView = findViewById(R.id.detectionState);
        Log.d(TAG, "State value: "+state);
        switch (state) {
            case OnDetectionStateChangedListener.IDLE:
                // No active detection and/or 10 seconds have passed since the last detection was lost
                Log.i(TAG, "OnDetectionStateChanged: IDLE");
                //textView.setText("OnDetectionStateChanged: IDLE");
                break;
            case OnDetectionStateChangedListener.LOST:
                // When human-target is lost
                Log.i(TAG, "OnDetectionStateChanged: LOST");
                //textView.setText("OnDetectionStateChanged: LOST");
                break;
            case OnDetectionStateChangedListener.DETECTED:
                // Human is detected
                Log.i(TAG, "OnDetectionStateChanged: DETECTED");
                //textView.setText("OnDetectionStateChanged: DETECTED");
                break;
            default:
                // This should not happen
                Log.i(TAG, "OnDetectionStateChanged: UNKNOWN");
                //textView.setText("OnDetectionStateChanged: UNKNOWN");
                break;
        }
    }
    @Override
    public void onDetectionDataChanged(@NotNull DetectionData detectionData) {
        if (detectionData.isDetected()) {
            final TextView textView = findViewById(R.id.detectionData);
            //textView.setText("Distance: " + detectionData.getDistance() + " m");
            Log.i(TAG, "Distance: " + detectionData.getDistance() + " m");

            angular_tilt_dist = detectionData.getDistance();
            //textView.setText(textView.getText()+ "   Angle: " + detectionData.getAngle() + " m");
            angular_dist = detectionData.getAngle();
            Log.i(TAG, "Angle: " + angular_dist);
            gaze_tracker(angular_dist, angular_tilt_dist);
           // user_tracker(angular_dist, angular_tilt_dist);
        }/*else {
            Log.i(TAG, "Detection Lost");
            doSomething();
        }*/
    }


   /* public void doSomething(){
        runOnUiThread(() -> {
            //Animation
        });
    }*/

    //Provide to track the user only through body movements
    public void user_tracker( double angular_dist, double angular_tilt_dist){
        double r = 0.7;
        int angle = (int) ((angular_dist / r) * (180 / 3.14));
        double m = -50 / r;
        int tilt_angle = (int) (m * angular_tilt_dist + 60);

        runOnUiThread(() -> {

            mRobot.turnBy(angle);
            mRobot.tiltAngle(tilt_angle);
        });
    }

    //Provide to track the user through gaze and body movements
    public void gaze_tracker(double angular_dist, double angular_tilt_dist) {

        //Conversion between angular distance and angle on the horizontal plane
        double r = 0.7;
        int angle = (int) ((angular_dist / r) * (180 / 3.14));

        //Conversion between distance and angle on the vertical plane(tilt movement)
        double m = -50 / r;
        int tilt_angle = (int) (m * angular_tilt_dist + 60);

        View[] trackSX = {trackSX1, trackSX2, trackSX3, trackSX4, trackSX5};
        View[] track = {track1, track2, track3, track4, track5, closing};


        runOnUiThread(() -> {

            for (View view : trackSX) {
                view.setVisibility(View.GONE);
            }
            for (View view : track) {
                view.setVisibility(View.GONE);
            }

            if (angle < -25) {
                trackSX[4].setVisibility(View.VISIBLE);
                mRobot.turnBy(angle);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));

                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= -25 && angle < -20) {
                trackSX[4].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= -20 && angle < -15) {
                trackSX[3].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= -15 && angle < -10) {
                trackSX[2].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= -10 && angle < -5) {
                trackSX[1].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>5){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= -5 && angle < 0) {
                trackSX[0].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= 0 && angle < 5) {
                track[0].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= 5 && angle < 10) {
                track[1].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= 10 && angle < 15) {
                track[2].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= 15 && angle < 20) {
                track[3].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= 20 && angle < 25) {
                track[4].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            } else if (angle >= 25) {
                track[4].setVisibility(View.VISIBLE);
                time += 1;
                mRobot.tiltAngle(Math.max(tilt_angle, 25));
                mRobot.turnBy(angle);
                if(time>8){
                    track[5].setVisibility(View.VISIBLE);
                    time = 0;
                }
            }

        });
    }

    @Override
    public void onUserInteraction(boolean isInteracting) {
        final TextView textView = findViewById(R.id.userInteraction);

        if (isInteracting) {
            // User is interacting with the robot:
            // - User is detected
            // - User is interacting by touch, voice, or in telepresence-mode
            // - Robot is moving
            Log.i(TAG, "OnUserInteraction: TRUE");
            //textView.setText("OnUserInteraction: TRUE");
        } else {
            // User is not interacting with the robot
            Log.i(TAG, "OnUserInteraction: FALSE");
            //textView.setText("OnUserInteraction: FALSE");
        }
    }
}