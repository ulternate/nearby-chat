package com.danielcswain.nearbychat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.danielcswain.nearbychat.Messages.MessageAdapter;
import com.danielcswain.nearbychat.Messages.MessageObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 1002;
    private static final String TAG = ChatActivity.class.getSimpleName();

    private static GoogleApiClient mGoogleApiClient;
    private static Message mPubMessage;
    private static MessageListener mMessageListener;

    private String mUsername;
    private String mAvatarColour;

    private ArrayList<MessageObject> mMessageObjects;
    private EditText mTextField;
    private RecyclerView.Adapter mMessageRecyclerAdapter;

    private View mSnackbarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get the calling intent
        Intent intent = getIntent();

        // Get the username and avatarColour for the user from the calling intent and create a UserObject
        mUsername = intent.getStringExtra(MainActivity.USERNAME_KEY);
        mAvatarColour = intent.getStringExtra(MainActivity.AVATAR_COLOUR_KEY);

        // Get the View for the snackbar
        mSnackbarContainer = findViewById(R.id.text_entry_container);

        // Get a reference to the RecyclerView and set the recycler view to have fixed layout size
        // (as the layout is already full screen)
        RecyclerView mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messages_list);
        mMessagesRecyclerView.setHasFixedSize(true);

        // Using a stock linear layout manager for the RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(mLayoutManager);

        // Set up the RecyclerView Adapter with the temporary data set and assign it to the RecyclerView
        mMessageObjects = new ArrayList<>();
        mMessageRecyclerAdapter = new MessageAdapter(mMessageObjects);
        mMessagesRecyclerView.setAdapter(mMessageRecyclerAdapter);

        // Get the message send views
        mTextField = (EditText) findViewById(R.id.text_entry_field);
        ImageButton submitButton = (ImageButton) findViewById(R.id.message_send_button);

        // Send a new message to the chat when the submit button is clicked
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the edit text field's text
                String messageBody = mTextField.getText().toString();
                if (!messageBody.isEmpty()){
                    // Publish the message (it will be added to the chat when published successfully)
                    publishMessage(new MessageObject(mUsername, messageBody, mAvatarColour, true));

                    // Hide the keyboard and reset the message text field
                    hideSoftKeyboard(ChatActivity.this, view);
                    mTextField.setText("");
                    mTextField.clearFocus();
                }
            }
        });

        // Build the GoogleApiClient
        buildGoogleApiClient();

        // Build the Nearby MessageListener
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                // Called when a new message is found.
                Log.d("message Found", "message found: " + MessageObject.fromNearbyMessage(message).getMessageBody());
                MessageObject receivedMessage = MessageObject.fromNearbyMessage(message);
                // Set the fromUser to false as it wasn't from the currentUser
                receivedMessage.setFromUser(false);
                // Add to the RecyclerView
                mMessageObjects.add(receivedMessage);
                mMessageRecyclerAdapter.notifyItemInserted(mMessageObjects.size() - 1);
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                MessageObject lostMessage = MessageObject.fromNearbyMessage(message);
                // Set the fromUser to false as it wasn't from the currentUser
                lostMessage.setFromUser(false);
                // Remove from the RecyclerView
                mMessageObjects.remove(lostMessage);
                mMessageRecyclerAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()){
            Log.d("disconnected", "wasn't connected");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        unpublish();
        unsubscribe();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void buildGoogleApiClient() {
        Log.d("building", "buildign client");
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Prompt the user to approve the Nearby Api connection if the connection failed
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("connectionFailed", "connection Failed");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "GoogleApiClient connection failed");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Subscribe to the chat
        subscribe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                Log.e(TAG, "GoogleApiClient connection failed. Unable to resolve.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            showSnackbar(getApplicationContext().getString(R.string.nearby_subscription_success));
                        } else {
                            showSnackbar(getApplicationContext().getString(R.string.nearby_subscription_failed_status) + status);
                        }
                    }
                });
    }

    private void publishMessage(final MessageObject messageObject) {
        Log.i(TAG, "Publishing message: " + messageObject.getMessageBody());
        mPubMessage = MessageObject.newNearbyMessage(messageObject);

        Nearby.Messages.publish(mGoogleApiClient, mPubMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()){
                            // Add to channel
                            mMessageObjects.add(messageObject);
                            mMessageRecyclerAdapter.notifyItemInserted(mMessageObjects.size() - 1);
                        } else {
                            showSnackbar(getString(R.string.nearby_publish_message_failed_status) + status);
                        }
                    }
                });
    }

    private void unsubscribe() {
        Log.d(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    private void unpublish() {
        Log.d(TAG, "Unpublishing.");
        Nearby.Messages.unpublish(mGoogleApiClient, mPubMessage);
    }

    public static void hideSoftKeyboard(Activity activity, View view){
        InputMethodManager mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private void showSnackbar(String message){
        if (mSnackbarContainer != null){
            Snackbar.make(mSnackbarContainer, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
