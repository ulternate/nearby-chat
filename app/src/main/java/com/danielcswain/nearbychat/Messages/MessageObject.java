package com.danielcswain.nearbychat.Messages;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom Object representing a single message object
 */
public class MessageObject {

    private static final Gson sGson = new Gson();
    private static final String MESSAGE_NAMESPACE = "NearbyChat";
    private static final String MESSAGE_TYPE = "Message";

    private String mUsername;
    private String mMessageBody;
    private String mAvatarColour;
    private Boolean mFromUser;

    public MessageObject(String username, String messageBody, String avatarColour, Boolean fromUser){
        this.mUsername = username;
        this.mMessageBody = messageBody;
        this.mAvatarColour = avatarColour;
        this.mFromUser = fromUser;
    }

    public static Message newNearbyMessage(MessageObject messageObject){
        return new Message(sGson.toJson(messageObject).getBytes(Charset.forName("UTF-8")), MESSAGE_NAMESPACE, MESSAGE_TYPE);
    }

    public static MessageObject fromNearbyMessage(Message message){
        String nearbyMessageString = new String(message.getContent()).trim();
        return sGson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                MessageObject.class);
    }

    @Override
    public boolean equals(Object obj) {
        boolean match = false;
        if (obj != null && obj instanceof MessageObject){
            if (Objects.equals(((MessageObject) obj).mUsername, this.mUsername) &&
                    Objects.equals(((MessageObject) obj).mMessageBody, this.mMessageBody) &&
                    Objects.equals(((MessageObject) obj).mAvatarColour, this.mAvatarColour) &&
                    Objects.equals(((MessageObject) obj).mFromUser, this.mFromUser)){
                match = true;
            }
        }
        return match;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public Boolean getFromUser() {
        return mFromUser;
    }

    public String getAvatarColour() {
        return mAvatarColour;
    }
}
