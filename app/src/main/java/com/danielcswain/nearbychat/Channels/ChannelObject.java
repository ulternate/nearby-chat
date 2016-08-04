package com.danielcswain.nearbychat.Channels;

import com.google.android.gms.nearby.messages.Message;

import java.nio.charset.Charset;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom object representing a single chat channel
 */
public class ChannelObject {

    private String channelTitle;
    private String channelTopic;
    private Boolean channelPrivate;

    /**
     * Constructor to create a chat channel object
     * @param channelTitle the chat channel's title
     * @param channelTopic the chat channel's topic of discussion
     * @param channelPrivate whether the chat channel is private or not
     */
    public ChannelObject(String channelTitle, String channelTopic, Boolean channelPrivate){
        this.channelTitle = channelTitle;
        this.channelTopic = channelTopic;
        this.channelPrivate = channelPrivate;
    }

    public Message newNearbyMessage(String channelTitle, String channelTopic){
        ChannelObject channelObject = new ChannelObject(channelTitle, channelTopic, false);
        return new Message(channelObject.getChannelTitle().getBytes(Charset.forName("UTF-8")), channelTopic);
    }

    public static ChannelObject fromNearbyMessage(Message message){
        String nearbyMessageString = new String(message.getContent()).trim();
        String nearbyMessageType = message.getType().trim();
        return new ChannelObject(nearbyMessageString, nearbyMessageType, false);
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getChannelTopic() {
        return channelTopic;
    }

    public Boolean getChannelPrivate() {
        return channelPrivate;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public void setChannelTopic(String channelTopic) {
        this.channelTopic = channelTopic;
    }

    public void setChannelPrivate(Boolean channelPrivate) {
        this.channelPrivate = channelPrivate;
    }
}
