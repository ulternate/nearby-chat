package com.danielcswain.nearbychat.Users;

import java.util.UUID;

/**
 * Created by ulternate on 6/08/2016.
 */
public class UserObject {

    private String mUsername;
    private String mColourHex;
    private String mUuid;

    public UserObject(String mUsername, String mColourHex){
        this.mUsername = mUsername;
        this.mColourHex = mColourHex;
        this.mUuid = UUID.randomUUID().toString();
    }

    public String getColour() {
        return mColourHex;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getUuid() {
        return mUuid;
    }
}
