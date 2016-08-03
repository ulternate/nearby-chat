package com.danielcswain.nearbychat.Channels;

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
