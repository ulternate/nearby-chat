package com.danielcswain.nearbychat.Channels;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom object representing a single chat channel
 */
public class ChannelObject {

    private static final Gson sGson = new Gson();
    private final String mChannelTitle;
    private final String mChannelTopic;
    private final Boolean mChannelPrivate;
    private final Boolean mChannelIsUsers;
    private String mChannelUuid;
    private String mChannelToken;
    public static final String channelNamespace = "CHANNEL";
    public static final String channelType = "CHANNEL";

    /**
     * Constructor to create a chat channel object
     * @param channelTitle the chat channel's title
     * @param channelTopic the chat channel's topic of discussion
     * @param channelPrivate whether the chat channel is private or not
     */
    public ChannelObject(String channelTitle, String channelTopic, Boolean channelPrivate, Boolean channelIsUsers){
        this.mChannelTitle = channelTitle;
        this.mChannelTopic = channelTopic;
        this.mChannelPrivate = channelPrivate;
        this.mChannelIsUsers = channelIsUsers;
        //TODO tokenise user's password when channel created.
        this.mChannelToken = "";
        this.mChannelUuid = UUID.randomUUID().toString();
    }

    public static Message newNearbyMessage(ChannelObject channelObject){
        return new Message(sGson.toJson(channelObject).getBytes(Charset.forName("UTF-8")), channelNamespace, channelType);
    }

    public static Message newNearbyMessage(String channelTitle, String channelTopic, Boolean channelIsUsers){
        ChannelObject channelObject = new ChannelObject(channelTitle, channelTopic, false, channelIsUsers);
        return new Message(sGson.toJson(channelObject).getBytes(Charset.forName("UTF-8")), channelNamespace, channelType);
    }

    public static ChannelObject fromNearbyMessage(Message message){
        String nearbyMessageString = new String(message.getContent()).trim();
        return sGson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                ChannelObject.class);
    }

    @Override
    public boolean equals(Object obj) {
        boolean match = false;
        if (obj != null && obj instanceof ChannelObject){
            if (Objects.equals(((ChannelObject) obj).mChannelTitle, this.mChannelTitle) &&
                    Objects.equals(((ChannelObject) obj).mChannelTopic, this.mChannelTopic) &&
                    Objects.equals(((ChannelObject) obj).mChannelPrivate, this.mChannelPrivate) &&
                    Objects.equals(((ChannelObject) obj).mChannelIsUsers, this.mChannelIsUsers)){
                match = true;
            }
        }
        return match;
    }

    public String getmChannelTitle() {
        return mChannelTitle;
    }

    public String getmChannelTopic() {
        return mChannelTopic;
    }

    public Boolean getmChannelPrivate() {
        return mChannelPrivate;
    }

    public Boolean getmChannelIsUsers() {
        return mChannelIsUsers;
    }

    public String getmChannelToken() {
        return mChannelToken;
    }

    public String getmChannelUuid() {
        return mChannelUuid;
    }
}
