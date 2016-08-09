package com.danielcswain.nearbychat.Users;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.danielcswain.nearbychat.ChatActivity;
import com.danielcswain.nearbychat.MainActivity;
import com.danielcswain.nearbychat.R;
import com.tomergoldst.tooltips.ToolTip;

import java.util.ArrayList;


/**
 * Created by ulternate on 9/08/2016.
 *
 * Custom Adapter extending the RecyclerView.Adapter class. This represents a the current user's row and
 * uses the UserViewHolder class to hold the individual userIcon views which get their contents from the
 * mUserObjects array list of current users
 */
public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private ArrayList<UserObject> mUserObjects;
    private final long DELAY = 2000;

    /**
     * Constructor for the UserAdapter
     * @param userObjects the array list of user objects
     */
    public UserAdapter(ArrayList<UserObject> userObjects){
        this.mUserObjects = userObjects;
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     * Uses the same UserViewHolder class for the userIcon's or avatars of the current users in the chat
     *
     * @param parent the parent view group
     * @param viewType the type of view to be represented in the ViewHolder.
     * @return a new UserViewHolder with the given ViewType inflated
     */
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_user_item, parent, false);
        return new UserViewHolder(v);
    }

    /**
     * Displays the user icon for the given position in the RecyclerView. Uses the UserViewHolder to
     * get the correct references to the View Items
     * @param holder the UserViewHolder representing a single RecyclerView item containing all the
     *               views for that item (the user's avatar))
     * @param position the position of the item in the RecyclerView.Adapters data set.
     */
    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        // Get the userObject from the ArrayList at that position
        final UserObject userObject = mUserObjects.get(position);

        // Set the colour of the avatar to match the user's preference as stored by the userObject
        holder.userAvatar.setColorFilter(Color.parseColor(userObject.getAvatarColour()));

        // Set an onClickListener to the avatar to show the user's username on click
        holder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ChatActivity.toolTipsManager != null) {
                    // There's a ToolTips manager so build and show the toolTip on click of the userAvatar
                    // First, dismiss any current ToolTips
                    ChatActivity.toolTipsManager.findAndDismiss(holder.toolTipTarget);
                    buildAndShowToolTip(holder, userObject);
                } else {
                    // Show a Toast if there's no ToolTips Manager
                    Toast.makeText(MainActivity.mainContext, userObject.getUsername(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildAndShowToolTip(final UserViewHolder holder, UserObject userObject){
        // Build the ToolTip containing the username
        ToolTip.Builder builder = new ToolTip.Builder(MainActivity.mainContext,
                holder.toolTipTarget,
                ChatActivity.mRootContainer,
                userObject.getUsername(),
                ToolTip.POSITION_BELOW);
        // Set the alignment of the ToolTip
        builder.setAlign(ToolTip.ALIGN_LEFT);
        // Set the background colour and text to match the user's avatar
        builder.setBackgroundColor(Color.parseColor(userObject.getAvatarColour()));
        builder.setTextColor(ContextCompat.getColor(MainActivity.mainContext, R.color.md_white));
        // Show the ToolTip
        ChatActivity.toolTipsManager.show(builder.build());
        // Dismiss the ToolTip after a short delay
        Handler dismissToolTipHandler = new Handler(Looper.getMainLooper());
        Runnable dismissToolTipRunnable = new Runnable() {
            @Override
            public void run() {
                ChatActivity.toolTipsManager.findAndDismiss(holder.toolTipTarget);
            }
        };
        dismissToolTipHandler.postDelayed(dismissToolTipRunnable, DELAY);
    }

    /**
     * @return the total number of items in the data set held by the Adapter
     */
    @Override
    public int getItemCount() {
        if (mUserObjects != null) {
            return mUserObjects.size();
        } else {
            return 0;
        }
    }
}
