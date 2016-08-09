package com.danielcswain.nearbychat.Users;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.danielcswain.nearbychat.R;

/**
 * Created by ulternate on 9/08/2016.
 *
 * Custom view holder extending the RecyclerView.ViewHolder class to support the custom user layout
 * to show the current users
 */
public class UserViewHolder extends RecyclerView.ViewHolder {

    public ImageView userAvatar;

    /**
     * The ViewHolder constructor that holds the views for the RecyclerView Adapter
     * @param itemView a view representing a single layout item in the RecyclerView (i.e. a user icon)
     */
    public UserViewHolder(View itemView) {
        super(itemView);

        this.userAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
    }
}
