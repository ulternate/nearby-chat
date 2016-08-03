package com.danielcswain.nearbychat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom Adapter extending the RecyclerView.Adapter class. This represents a single chat window and uses
 * the MessageViewHolder class to hold the individual message views which get their contents from the
 * mMessages ArrayList
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private ArrayList<Message> mMessages;

    /**
     * Constructor for the MessageAdapter
     * @param messages an ArrayList of Message objects
     */
    public MessageAdapter(ArrayList<Message> messages){
        this.mMessages = messages;
    }

    /**
     * Get the ViewType of the item. This is used to determine whether to inflate the message_item_received or
     * message_item_sent layouts in onCreateViewHolder.
     *
     * @param position the position of the ItemView object in the mMessages arrayList / RecyclerView.Adapter
     * @return 0 for a message that was sent the user and 1 if it was a received message.
     */
    @Override
    public int getItemViewType(int position) {
        Message message = mMessages.get(position);
        if (message.getFromUser()){
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     * Uses the same MessageViewHolder class for both sent and received messages
     *
     * @param parent the parent view group
     * @param viewType the type of view to be represented in the ViewHolder. This is determined by getItemViewType
     *                 and the message type (either sent from the user or a received message)
     * @return a new MessageViewHolder with the given ViewType inflated
     */
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if (viewType == 0) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_sent, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_received, parent, false);
        }

        return new MessageViewHolder(v);
    }

    /**
     * Displays the message data for the given position in the RecyclerView. Uses the MessageViewHolder to
     * get the correct references to the View Items
     * @param holder the MessageViewHolder representing a single RecyclerView item containing all the
     *               views for that item (i.e. the message body, username and avatar)
     * @param position the position of the item in the RecyclerView.Adapters data set.
     */
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        // Get the message from the ArrayList at the position specified
        Message message = mMessages.get(position);

        // Set the TextViews from the ViewHolder with the appropriate data from the message.
        holder.usernameTV.setText(message.getUsername());
        holder.messageBodyTV.setText(message.getMessageBody());

        // If not the first message then check if the previous message was from the same user.
        // if so, then don't display the user's avatar or username
        if (position > 0){
            String prevUsername = mMessages.get(position - 1).getUsername();
            if (Objects.equals(prevUsername, message.getUsername())){
                holder.userAvatarIV.setVisibility(View.GONE);
                holder.usernameTV.setVisibility(View.GONE);
            }
        }
    }

    /**
     * @return the total number of items in the data set held by the Adapter
     */
    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
