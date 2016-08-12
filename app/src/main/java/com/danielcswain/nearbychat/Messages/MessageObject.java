package com.danielcswain.nearbychat.Messages;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom Object representing a single message object
 */
public class MessageObject {

    public static final String MESSAGE_TYPE = "Message";
    public static final String MESSAGE_CONTENT_TEXT = "text";
    public static final String MESSAGE_CONTENT_IMAGE = "image";

    private static final Gson sGson = new Gson();
    private String mUsername;
    private String mMessageBody;
    private String mAvatarColour;
    private boolean mFromUser;
    private UUID mMessageUuid;
    private String mMessageContent;

    /**
     * Constructor for initialising a single MessageObject.
     * @param username the username of the user sending the message
     * @param messageBody the message text
     * @param messageContent the type of content being sent in the message body (image or text)
     * @param avatarColour the colour of the user's avatar
     * @param fromUser boolean used to determine which layout to use, either the message_item_received or sent.
     */
    public MessageObject(String username, String messageBody, String messageContent, String avatarColour, boolean fromUser){
        this.mUsername = username;
        this.mMessageBody = messageBody;
        this.mMessageContent = messageContent;
        this.mAvatarColour = avatarColour;
        this.mFromUser = fromUser;
        this.mMessageUuid = UUID.randomUUID();
    }

    /**
     * Create a Nearby Message using the messageObject
     * @param messageObject the messageObject being sent
     * @return a Message with the messageObject as the package (converted to a byte[])
     */
    public static Message newNearbyMessage(MessageObject messageObject){
        return new Message(sGson.toJson(messageObject).getBytes(Charset.forName("UTF-8")), MESSAGE_TYPE);
    }

    /**
     * Retrieve a messageObject from the Nearby Message's content
     * @param message the Nearby Message containing the messageObject as a byte[]
     * @return a MessageObject using Gson to convert from JSON to an instance of the MessageObject class
     */
    public static MessageObject fromNearbyMessage(Message message){
        String nearbyMessageString = new String(message.getContent()).trim();
        return sGson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                MessageObject.class);
    }

    /**
     * Custom implementation to test for equality through message content, not if they refer to the same
     * object in memory. Used by ListAdapter.contains method.
     */
    @Override
    public boolean equals(Object obj) {
        boolean match = false;
        if (obj != null && obj instanceof MessageObject){
            if (Objects.equals(((MessageObject) obj).mUsername, this.mUsername) &&
                    Objects.equals(((MessageObject) obj).mMessageBody, this.mMessageBody) &&
                    Objects.equals(((MessageObject) obj).mMessageContent, this.mMessageContent) &&
                    Objects.equals(((MessageObject) obj).mAvatarColour, this.mAvatarColour) &&
                    Objects.equals(((MessageObject) obj).mFromUser, this.mFromUser) &&
                    Objects.equals(((MessageObject) obj).mMessageUuid, this.mMessageUuid)){
                match = true;
            }
        }
        return match;
    }

    /**
     * Get and Set methods for the MessageObject properties
     */
    public String getUsername() {
        return mUsername;
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public boolean getFromUser() {
        return mFromUser;
    }

    public String getAvatarColour() {
        return mAvatarColour;
    }

    public UUID getMessageUuid() {
        return mMessageUuid;
    }

    public String getMessageContent(){
        return mMessageContent;
    }

    public void setFromUser(boolean mFromUser) {
        this.mFromUser = mFromUser;
    }
}
