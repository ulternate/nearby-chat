package com.danielcswain.nearbychat;

import android.app.Application;

/**
 * Created by ulternate on 5/08/2016.
 */
public class MainApplication extends Application {

    public static boolean firstStart;
;

    public MainApplication(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        firstStart = true;
    }

    public static void setFirstStart(boolean bool){
        firstStart = bool;
    }

}
