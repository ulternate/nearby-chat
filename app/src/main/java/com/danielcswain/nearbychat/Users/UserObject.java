package com.danielcswain.nearbychat.Users;

import java.util.UUID;

/**
 * Created by ulternate on 6/08/2016.
 */
public class UserObject {

    private String mUsername;
    private int mColour;
    private String mUuid;

    public UserObject(String mUsername, int mColour){
        this.mUsername = mUsername;
        this.mColour = mColour;
        this.mUuid = UUID.randomUUID().toString();
    }

    public int getColour() {
        return mColour;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getUuid() {
        return mUuid;
    }
}
