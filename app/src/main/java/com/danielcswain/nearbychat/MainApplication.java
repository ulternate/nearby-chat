package com.danielcswain.nearbychat;

import android.app.Application;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ulternate on 5/08/2016.
 */
public class MainApplication extends Application {

    public static boolean firstStart = true;
    public static boolean wasInBackground;

    private static Timer sActivityTransitionTimer;
    private static TimerTask sActivityTransitionTimerTask;
    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    public MainApplication(){

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void setFirstStart(boolean bool){
        firstStart = bool;
    }


    /**
     * Create and start a timer and timer task that will execute after a short delay if not stopped.
     * If this tasks executes (it wasn't stopped) then the GoogleApiClient will be disconnected
     * after any channel/messages were unpublished and the phone has unsubscribed from any Nearby channels/messages.
     *
     * This timer task is called in the activities onStop method and canceled in the onStart call.
     */
    public static void startActivityTransitionTimer(){
        sActivityTransitionTimer = new Timer();
        sActivityTransitionTimerTask = new TimerTask() {
            @Override
            public void run() {
                //Set was in background to true
                wasInBackground = true;
                // Unpublish and unsubscribe from the nearby channels.
                MainActivity.unpublishNearbyChannel();
                MainActivity.unsubscribeFromNearbyChannels();
                // Disconnect from the NearbyAPI client as the app has now entered the background.
                if (MainActivity.sGoogleApiClient.isConnected()){
                    MainActivity.sGoogleApiClient.disconnect();
                }
            }
        };
        // Schedule the timer task to run after the MAX_ACTIVITY_TRANSITION_TIME_MS delay has passed.
        sActivityTransitionTimer.schedule(sActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    /**
     * Stop the activity timer
     */
    public static void stopActivityTransitionTimer(){
        if (sActivityTransitionTimerTask != null){
            sActivityTransitionTimerTask.cancel();
        }
        if (sActivityTransitionTimer != null){
            sActivityTransitionTimer.cancel();
        }
        wasInBackground = false;
    }


}
