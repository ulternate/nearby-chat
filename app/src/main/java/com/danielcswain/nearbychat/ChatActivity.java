package com.danielcswain.nearbychat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.danielcswain.nearbychat.Messages.MessageAdapter;
import com.danielcswain.nearbychat.Messages.MessageObject;
import com.danielcswain.nearbychat.Users.UserObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.ArrayList;

/**
 * Activity using the GoogleApiClient and the Nearby Api to send custom messages to nearby devices.
 */
public class ChatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 1002;
    private static final String TAG = ChatActivity.class.getSimpleName();

    private static GoogleApiClient mGoogleApiClient;
    private static Message mPubMessage;
    private static MessageListener mMessageListener;

    private String mUsername;
    private String mAvatarColour;
    private static UserObject sCurrentUser;

    private ArrayList<MessageObject> mMessageObjects;
    private EditText mTextField;
    private ImageButton mSubmitButton;
    private static LinearLayout mUsersContainer;
    private RecyclerView.Adapter mMessageRecyclerAdapter;

    private View mSnackbarContainer;

    // Animation used to rotate the send button about it's center.
    private Animation mRotateAnimation = new RotateAnimation(0.0f, 360.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get the calling intent
        Intent intent = getIntent();

        // Get the username and avatarColour for the user from the calling intent and create a UserObject
        mUsername = intent.getStringExtra(MainActivity.USERNAME_KEY);
        mAvatarColour = intent.getStringExtra(MainActivity.AVATAR_COLOUR_KEY);
        sCurrentUser = new UserObject(mUsername, mAvatarColour);

        // Get the View for the snackbar
        mSnackbarContainer = findViewById(R.id.text_entry_container);

        // Get the view for the current users container
        mUsersContainer = (LinearLayout) findViewById(R.id.chat_users_container);
        mUsersContainer.removeAllViews();

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
        mSubmitButton = (ImageButton) findViewById(R.id.message_send_button);

        // Configure the rotation animation to signify a message is being sent
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(500);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);

        // Send a new message to the chat when the submit button is clicked
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the edit text field's text
                String messageBody = mTextField.getText().toString();
                if (!messageBody.isEmpty()){
                    // Change the mSubmitbutton drawable to the loop/sync icon and animate it.
                    mSubmitButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_loop_light));
                    mSubmitButton.startAnimation(mRotateAnimation);

                    // Publish the message (it will be added to the chat when published successfully and the animation will be stopped)
                    publishMessage(new MessageObject(mUsername, messageBody, mAvatarColour, true));

                    // Hide the keyboard and reset the message text field
                    hideSoftKeyboard(ChatActivity.this, view);
                    mTextField.setText("");
                    mTextField.clearFocus();
                } else {
                    showSnackbar(getString(R.string.error_empty_message));
                }
            }
        });

        // Build the GoogleApiClient
        buildGoogleApiClient();

        // Build the Nearby MessageListener
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                if (message.getType().equals(MessageObject.MESSAGE_TYPE)) {
                    // If the message has the "Message" type then display the message in the chat
                    displayMessageOnReceived(message);
                } else if(message.getType().equals(UserObject.MESSAGE_TYPE)){
                    // If the message has the "Greeting" type then display
                    displayGreetingOnReceived(message);
                }
            }

            @Override
            public void onLost(Message message) {
                if (message.getType().equals(MessageObject.MESSAGE_TYPE)) {
                    // If the message has the "Message" type then remove it if it was lost
                    removeMessageOnLost(message);
                } else if (message.getType().equals(UserObject.MESSAGE_TYPE)){
                    // If the message has the "Greeting" type then remove it if it was lost
                    removeGreetingOnLost(message);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the GoogleApiClient if disconnected
        if (!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        // Unpublish, unsubscribe and disconnect from the GoogleApiClient if connected.
        if (mPubMessage != null) {
            unpublish();
        }
        unsubscribe();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        // Remove the user's from the usersContainer
        mUsersContainer.removeAllViews();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the GoogleApiClient couldn't connect it will prompt the user for permission to use Nearby
        // using the following request code. Handle this request and connect to the GoogleApiClient if successful
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

    /**
     * Override the default Configuration change handling, this is to stop the activity being recreated on an orientation
     * change
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Build the GoogleApiClient to use the Nearby Messages Api
     */
    private void buildGoogleApiClient() {
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
        // Say hi, i.e. send a message so other's can see how many users are in the chat
        if (!sCurrentUser.getUsername().isEmpty() && !sCurrentUser.getUsername().isEmpty()){
            publishHelloMessage(sCurrentUser);
        }
    }

    /**
     * Subscribe to the chat and receive nearby messages using the mMessageListener
     */
    private void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            // Subscribed successfully, notify the user.
                            showSnackbar(getApplicationContext().getString(R.string.nearby_subscription_success));
                        } else {
                            // Unsuccessfully subscribed, notify the user.
                            showSnackbar(getApplicationContext().getString(R.string.nearby_subscription_failed));
                        }
                    }
                });
    }

    /**
     * Publish the MessageObject to nearby devices.
     * @param messageObject the messageObject to be published
     */
    private void publishMessage(final MessageObject messageObject) {
        mPubMessage = MessageObject.newNearbyMessage(messageObject);
        // Publish the message and display in the chat on the device if the publish action was successful
        Nearby.Messages.publish(mGoogleApiClient, mPubMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()){
                            // Add to channel as the message was published successfully
                            mMessageObjects.add(messageObject);
                            mMessageRecyclerAdapter.notifyItemInserted(mMessageObjects.size() - 1);
                        } else {
                            // Show a snackbar with a publish failed message
                            showSnackbar(getString(R.string.nearby_publish_message_failed));
                        }
                        // Stop the animation regardless of successful publishing or not
                        mRotateAnimation.cancel();
                        mRotateAnimation.reset();
                        // Reset the message button's drawable to the send resource
                        mSubmitButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_send_light));
                    }
                });
    }

    /**
     * Publish a HelloMessage containing the UserObject of the user joining the chat to
     * enable a count of the current active users in the chat and show the avatar in the usersContainer.
     * @param userObject the user joining the chat
     */
    private void publishHelloMessage(final UserObject userObject){
        mPubMessage = UserObject.newNearbyMessage(userObject);
        // Publish the message to the channel and display in the user's bar the user avatar with the avatar colour
        Nearby.Messages.publish(mGoogleApiClient, mPubMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()){
                            // Show the user avatar in the chat user window with the user's colour.
                            addUserToUsersContainer(userObject);
                        } else {
                            // Log the error
                            Log.e(TAG, "Couldn't send the user greeting to the channel: " + status);
                        }
                    }
                });
    }

    /**
     * Unsubscribe and stop listening for nearby messages.
     */
    private void unsubscribe() {
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    /**
     * Unpublish the message from the chat
     */
    private void unpublish() {
        Nearby.Messages.unpublish(mGoogleApiClient, mPubMessage);
    }

    /**
     * Display the received message in the chat feed
     * @param message the message received
     */
    private void displayMessageOnReceived(Message message){
        // Get the messageObject from the received Nearby Message
        MessageObject receivedMessage = MessageObject.fromNearbyMessage(message);
        // Set the fromUser to false as it wasn't from the currentUser
        receivedMessage.setFromUser(false);
        // Add to the RecyclerView
        mMessageObjects.add(receivedMessage);
        mMessageRecyclerAdapter.notifyItemInserted(mMessageObjects.size() - 1);
    }

    /**
     * Display the received greeting in the user's present section
     * @param message the message received, containing the username and avatar colour
     */
    private void displayGreetingOnReceived(Message message){
        // Get the userObject from the received Nearby Message
        UserObject receivedUser = UserObject.fromNearbyMessage(message);
        // Show the userObject in the usersContainer
        addUserToUsersContainer(receivedUser);
    }

    /**
     * Remove a lost message from the message feed
     * @param message the message lost
     */
    private void removeMessageOnLost(Message message){
        // Called when a message is no longer detectable nearby.
        MessageObject lostMessage = MessageObject.fromNearbyMessage(message);
        // Set the fromUser to false as it wasn't from the currentUser
        lostMessage.setFromUser(false);
        // Remove from the RecyclerView
        mMessageObjects.remove(lostMessage);
        mMessageRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Remove a lost greeting from the user's present section
     * @param message the message lost
     */
    private void removeGreetingOnLost(Message message){
        UserObject userObject = UserObject.fromNearbyMessage(message);
        removeUserFromUsersContainer(userObject);
    }

    /**
     * Remove the userIcon referring to the user from the usersContainer
     * @param userObject the user being removed
     */
    private void removeUserFromUsersContainer(UserObject userObject){
        // Loop through the userIcons in the UsersContainer to find the user that was lost
        for (int i = 0; i < mUsersContainer.getChildCount(); i++) {
            // Remove the corresponding userIcon if it exists with the same username as the content description and avatarColour
            ImageView userIcon = (ImageView) mUsersContainer.getChildAt(i);
            if (userIcon.getContentDescription().equals(userObject.getUsername())){
                mUsersContainer.removeView(userIcon);
            }
        }
    }

    /**
     * Add a userIcon to the User container to show who's in the chat currently
     * @param userObject the UserObject representing the user that has just joined the chat
     */
    private void addUserToUsersContainer(UserObject userObject){
        // Create an ImageView to add to the LinearLayout
        ImageView userIcon = new ImageView(getApplicationContext());

        // Set the drawable for the ImageView
        userIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_avatar_circle_dark));

        // Use LayoutParams to set the height and width of the image.
        int iconDimens = (int) getResources().getDimension(R.dimen.button_message_send_32dp);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconDimens, iconDimens);

        // Set the colour of the avatar
        userIcon.setColorFilter(Color.parseColor(userObject.getAvatarColour()));

        // Add onClickListener to image to show the username of the user being clicked on
        final String username = userObject.getUsername();
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatActivity.this, username, Toast.LENGTH_SHORT).show();
            }
        });

        // Set the content description label (used to identify the userIcon for removal and accessibility)
        userIcon.setContentDescription(userObject.getUsername());

        // Add the new userIcon to the view with the layout params
        mUsersContainer.addView(userIcon, layoutParams);

        // Now there's a userIcon in the Users container, set the parent view background color
        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) mUsersContainer.getParent();
        horizontalScrollView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.messageUsersField));
    }

    /**
     * Hide the software keyboard from the view
     */
    private static void hideSoftKeyboard(Activity activity, View view){
        InputMethodManager mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    /**
     * Show a snackbar with a given message
     */
    private void showSnackbar(String message){
        if (mSnackbarContainer != null){
            Snackbar.make(mSnackbarContainer, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
