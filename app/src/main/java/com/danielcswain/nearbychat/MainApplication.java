package com.danielcswain.nearbychat;

import android.app.Application;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ulternate on 5/08/2016.
 */
public class MainApplication extends Application {

    public static boolean firstStart = true;
    public boolean wasInBackground;

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    public MainApplication(){

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void setFirstStart(boolean bool){
        firstStart = bool;
    }

    public void startActivityTransitionTimer(){
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            @Override
            public void run() {
                MainApplication.this.wasInBackground = true;
                // Disconnect from the NearbyAPI client here
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer(){
        if (this.mActivityTransitionTimerTask != null){
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null){
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }


}
