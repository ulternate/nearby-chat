package com.danielcswain.nearbychat.Messages;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom Object representing a single message object
 */
public class MessageObject {

    private String username;
    private String messageBody;
    private Boolean fromUser;

    public MessageObject(String username, String messageBody, Boolean fromUser){
        this.username = username;
        this.messageBody = messageBody;
        this.fromUser = fromUser;
    }

    public String getUsername() {
        return username;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public Boolean getFromUser() {
        return fromUser;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setFromUser(Boolean fromUser) {
        this.fromUser = fromUser;
    }
}
