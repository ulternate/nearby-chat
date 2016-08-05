package com.danielcswain.nearbychat.Channels;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom object representing a single chat channel
 */
public class ChannelObject {

    private static final Gson gson = new Gson();
    private final String channelTitle;
    private final String channelTopic;
    private final Boolean channelPrivate;
    private final Boolean channelIsUsers;

    /**
     * Constructor to create a chat channel object
     * @param channelTitle the chat channel's title
     * @param channelTopic the chat channel's topic of discussion
     * @param channelPrivate whether the chat channel is private or not
     */
    public ChannelObject(String channelTitle, String channelTopic, Boolean channelPrivate, Boolean channelIsUsers){
        this.channelTitle = channelTitle;
        this.channelTopic = channelTopic;
        this.channelPrivate = channelPrivate;
        this.channelIsUsers = channelIsUsers;
    }

    public static Message newNearbyMessage(String channelTitle, String channelTopic, Boolean channelIsUsers){
        ChannelObject channelObject = new ChannelObject(channelTitle, channelTopic, false, channelIsUsers);
        return new Message(gson.toJson(channelObject).getBytes(Charset.forName("UTF-8")), "Channel");
    }

    public static ChannelObject fromNearbyMessage(Message message){
        String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                ChannelObject.class);
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

    public Boolean getChannelIsUsers() {
        return channelIsUsers;
    }
}
