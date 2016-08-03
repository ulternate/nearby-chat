package com.danielcswain.nearbychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by ulternate on 3/08/2016.
 */
public class MessageListAdapter extends ArrayAdapter<Message> {

    public MessageListAdapter(Context context, ArrayList<Message> messages){
        super(context, 0, messages);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Message message = getItem(position);

        if (convertView == null){
            if (message.getFromUser()) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item_sent, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item_received, parent, false);
            }
        }

        TextView usernameTV = (TextView) convertView.findViewById(R.id.username);
        ImageView userAvatarIV = (ImageView) convertView.findViewById(R.id.user_avatar);
        TextView messageBodyTV = (TextView) convertView.findViewById(R.id.message_body);

        usernameTV.setText(message.getUsername());
        messageBodyTV.setText(message.getMessageBody());

        if (position > 0){
            Message prevMessage = getItem(position - 1);
            if (Objects.equals(prevMessage.getUsername(), message.getUsername())){
                userAvatarIV.setVisibility(View.GONE);
                usernameTV.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
