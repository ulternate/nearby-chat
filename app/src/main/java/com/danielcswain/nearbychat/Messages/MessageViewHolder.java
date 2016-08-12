package com.danielcswain.nearbychat.Messages;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielcswain.nearbychat.R;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom view holder extending the RecyclerView.ViewHolder class to support the custom message layout
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView usernameTV;
    public TextView messageBodyTV;
    public ImageView userAvatarIV;
    public ImageView messageBodyIV;

    /**
     * The ViewHolder constructor that holds the views for the RecyclerView Adapter
     * @param itemView a view representing a single layout item in the RecyclerView (i.e. a message row)
     */
    public MessageViewHolder(View itemView) {
        super(itemView);

        this.usernameTV = (TextView) itemView.findViewById(R.id.username);
        this.messageBodyTV = (TextView) itemView.findViewById(R.id.message_body);
        this.userAvatarIV = (ImageView) itemView.findViewById(R.id.user_avatar);
        this.messageBodyIV = (ImageView) itemView.findViewById(R.id.message_body_image);
    }
}
