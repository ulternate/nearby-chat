package com.danielcswain.nearbychat.Channels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielcswain.nearbychat.R;

import java.util.ArrayList;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom ArrayAdapter for the channel list in main activity, it lists all nearby chat channels
 */
public class ChannelListAdapter extends ArrayAdapter<ChannelObject> {

    /**
     * Constructor for the ChannelListAdapter
     * @param context the application/activity context
     * @param channelObjects an ArrayList of channelObjects used to initialise the arrayList with
     *                       data
     */
    public ChannelListAdapter(Context context, ArrayList<ChannelObject> channelObjects){
        super(context, 0, channelObjects);
    }

    /**
     * Inflate the list item view for each item in the list
     * @param position the position in the ArrayList/ListView
     * @param convertView the view that is to be returned to the ListView in that position
     * @param parent the parent view group
     * @return the resulting listView item with the data relating to the object at that position in
     *          the ArrayAdapter's arrayList
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChannelObject channelObject = getItem(position);

        // Inflate the channel_list_item view if the convertView at that position doesn't exist
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.channel_list_item, parent, false);
        }

        // Get references to the channelTitle, Topic and Privacy view items
        TextView channelTitleTV = (TextView) convertView.findViewById(R.id.channel_title);
        TextView channelTopicTV = (TextView) convertView.findViewById(R.id.channel_topic);
        ImageView channelPrivateIV = (ImageView) convertView.findViewById(R.id.channel_private_image);

        // Set the Channel title and topic based upon the ChannelObject at this position in the List
        channelTitleTV.setText(channelObject.getChannelTitle());
        channelTopicTV.setText(channelObject.getChannelTopic());

        // If the channel is private, show the lock icon to signify that it requires a password
        if (channelObject.getChannelPrivate()){
            channelPrivateIV.setVisibility(View.VISIBLE);
        } else {
            channelPrivateIV.setVisibility(View.GONE);
        }

        return convertView;
    }
}
