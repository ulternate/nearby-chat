package com.danielcswain.nearbychat.Users;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created by ulternate on 8/08/2016.
 *
 * Custom Object representing a single user object
 */
public class UserObject {

    public static final String MESSAGE_TYPE = "Greeting";

    private static final Gson sGson = new Gson();
    private String mUsername;
    private String mAvatarColour;

    /**
     * Constructor for initiating a single UserObject
     * @param username the username of the user
     * @param avatarColour the colour of the user's avatar
     */
    public UserObject(String username, String avatarColour){
        this.mUsername = username;
        this.mAvatarColour = avatarColour;
    }

    /**
     * Create a Nearby Message using the UserObject
     * @param userObject the UserObject being sent
     * @return a Message with the UserObject as the package (converted to a byte[])
     */
    public static Message newNearbyMessage(UserObject userObject){
        return new Message(sGson.toJson(userObject).getBytes(Charset.forName("UTF-8")), MESSAGE_TYPE);
    }

    /**
     * Retrieve a userObject from the Nearby Message's content
     * @param message the Nearby Message containing the userObject as a byte[]
     * @return a UserObject using Gson to convert from JSON to an instance of the UserObject class
     */
    public static UserObject fromNearbyMessage(Message message){
        String nearbyMessageString = new String(message.getContent()).trim();
        return sGson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                UserObject.class);
    }

    public String getUsername() {
        return mUsername;
    }

    public String getAvatarColour() {
        return mAvatarColour;
    }
}
