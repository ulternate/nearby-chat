package com.danielcswain.nearbychat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.danielcswain.nearbychat.Messages.MessageAdapter;
import com.danielcswain.nearbychat.Messages.MessageObject;

import java.util.ArrayList;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    ArrayList<MessageObject> mMessageObjects;
    EditText mTextField;
    RecyclerView.Adapter mMessageRecyclerAdapter;
    RecyclerView mMessagesRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get the calling intent
        Intent intent = getIntent();

        // Get reference to the topic text view and set the activity title using the calling intent
        setTitle(intent.getStringExtra("channelTitle"));
        TextView channelTopic = (TextView) findViewById(R.id.chat_channel_topic);
        channelTopic.setText(intent.getStringExtra("channelTopic"));

        // Get the messages
        mMessageObjects = new ArrayList<>();
        mMessageObjects.add(new MessageObject("ulternate", "This is a message from me", true));
        mMessageObjects.add(new MessageObject("kenneth", "This is a message from someone else", false));
        mMessageObjects.add(new MessageObject("ulternate", "This is another message from me", true));
        mMessageObjects.add(new MessageObject("ulternate", "This is a really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really long message", true));

        // Get a reference to the RecyclerView and set the recycler view to have fixed layout size
        // (as the layout is already full screen)
        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messages_list);
        mMessagesRecyclerView.setHasFixedSize(true);

        // Using a stock linear layout manager for the RecyclerView
        mLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(mLayoutManager);

        // Set up the RecyclerView Adapter with the temporary data set and assign it to the RecyclerView
        mMessageRecyclerAdapter = new MessageAdapter(mMessageObjects);
        mMessagesRecyclerView.setAdapter(mMessageRecyclerAdapter);

        // Get the message send views
        mTextField = (EditText) findViewById(R.id.text_entry_field);
        ImageButton submitButton = (ImageButton) findViewById(R.id.message_send_button);

        // Send a new message to the chat when the submit button is clicked
        // TODO use Nearby API to actually send a message
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the edit text field's text
                String message = mTextField.getText().toString();
                if (!message.isEmpty()){
                    // Randomly choose if the message is from the user or mimic being received
                    Random rand = new Random();
                    Boolean bool = rand.nextBoolean();
                    if (bool){
                        mMessageObjects.add(new MessageObject("ulternate", message, bool));
                    } else {
                        mMessageObjects.add(new MessageObject("kenneth", message, bool));
                    }
                    mMessageRecyclerAdapter.notifyDataSetChanged();
                    hideSoftKeyboard(ChatActivity.this, view);
                    mTextField.setText("");
                    mTextField.clearFocus();
                }
            }
        });
    }

    public static void hideSoftKeyboard(Activity activity, View view){
        InputMethodManager mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
