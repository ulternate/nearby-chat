package com.danielcswain.nearbychat;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.danielcswain.nearbychat.Messages.MessageAdapter;
import com.danielcswain.nearbychat.Messages.MessageDialog;
import com.danielcswain.nearbychat.Messages.MessageObject;
import com.danielcswain.nearbychat.Users.UserAdapter;
import com.danielcswain.nearbychat.Users.UserObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.util.ArrayList;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

/**
 * Activity using the GoogleApiClient and the Nearby Api to send custom messages to nearby devices.
 */
public class ChatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 1002;
    private static final String TAG = ChatActivity.class.getSimpleName();
    public static final String DIALOG_COUNTER_KEY = "dialog_counter";
    public static final String DIALOG_DISMISSED_KEY = "dialog_dismissed";
    private static final String DIALOG_FRAGMENT_TAG = "Dialog Fragment";

    private static GoogleApiClient mGoogleApiClient;
    private static Message mPubMessage;
    private static MessageListener mMessageListener;

    private String mUsername;
    private String mAvatarColour;
    private static UserObject sCurrentUser;

    private ArrayList<MessageObject> mMessageObjects;
    private static ArrayList<UserObject> mUserObjects;
    private ImageView mEmojiButton;
    private EmojIconActions mEmojiActions;
    private EmojiconEditText mTextField;
    private ImageButton mSubmitButton;
    private RecyclerView.Adapter mMessageRecyclerAdapter;
    private static RecyclerView.Adapter mUserRecyclerAdapter;

    public static RelativeLayout mRootContainer;

    public static ToolTipsManager toolTipsManager;

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
        mRootContainer = (RelativeLayout) findViewById(R.id.root_view);

        // Initiate the Tooltips manager
        toolTipsManager = new ToolTipsManager();

        // Set up the users views and adapters
        setUpUsersViews();

        // Set up the message views and adapters
        setUpMessagesViews();

        // Set up the message send views and buttons
        setUpMessageSendViews();

        // Build the GoogleApiClient
        buildGoogleApiClient();

        // Build the Nearby MessageListener
        buildMessageListener();

        // Check if the MessageDialog needs to be shown
        checkAndShowMessageDialog();
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
        // Remove the user's from the userRecyclerViewAdapter
        mUserRecyclerAdapter.notifyItemRangeRemoved(0, mUserObjects.size());
        mUserObjects.clear();
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
     * Set up the views, array lists and adapters for the current user's area
     */
    private void setUpUsersViews(){
        // Get a reference to the RecyclerView
        RecyclerView mUsersRecyclerView = (RecyclerView) findViewById(R.id.users_list);

        // Using a stock linear layout manager for the RecyclerView
        RecyclerView.LayoutManager mUsersLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mUsersRecyclerView.setLayoutManager(mUsersLayoutManager);

        // Set up the RecyclerView Adapter
        mUserObjects = new ArrayList<>();
        mUserRecyclerAdapter = new UserAdapter(mUserObjects);
        mUsersRecyclerView.setAdapter(mUserRecyclerAdapter);
    }

    /**
     * Set up the views, array lists and adapters for the message area
     */
    private void setUpMessagesViews(){
        // Get a reference to the RecyclerView and set the recycler view to have fixed layout size
        // (as the layout is already full screen)
        RecyclerView mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messages_list);
        mMessagesRecyclerView.setHasFixedSize(true);

        // Using a stock linear layout manager for the RecyclerView
        RecyclerView.LayoutManager mMessagesLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(mMessagesLayoutManager);

        // Set up the RecyclerView Adapter
        mMessageObjects = new ArrayList<>();
        mMessageRecyclerAdapter = new MessageAdapter(mMessageObjects);
        mMessagesRecyclerView.setAdapter(mMessageRecyclerAdapter);
    }

    /**
     * Set up the views, buttons and onClickListeners for the message text entry/send area
     */
    private void setUpMessageSendViews() {
        // Get the message send views
        mEmojiButton = (ImageView) findViewById(R.id.text_emoji_button);
        mTextField = (EmojiconEditText) findViewById(R.id.text_entry_field);
        mSubmitButton = (ImageButton) findViewById(R.id.message_send_button);

        // Configure the rotation animation to signify a message is being sent
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(500);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);

        // Use SuperNova-Emoji to show a Whatsapp style Emoji replacement keyboard when the mEmojiButton
        // is clicked. Using the system default emoji rather than any styled (i.e. Apple Style or FB)
        mEmojiActions = new EmojIconActions(this, mRootContainer, mTextField, mEmojiButton);
        mEmojiActions.setUseSystemEmoji(true);
        mEmojiActions.ShowEmojIcon();

        // Send a new message to the chat when the submit button is clicked
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the edit text field's text
                String messageBody = mTextField.getText().toString();

                if (!messageBody.isEmpty() && !messageBody.matches("^(\\s+)$")){
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
                    // The messageBody is empty or contains only whitespace characters
                    showSnackbar(getString(R.string.error_empty_message));
                }
            }
        });
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
     * Build the Nearby MessageListener attached in subscribe that handles the messages as they are received.
     */
    private void buildMessageListener(){
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                if (message.getType().equals(MessageObject.MESSAGE_TYPE)) {
                    // If the message has the "Message" type then display the message in the chat
                    // Set the fromUser to false as it is a received message
                    displayMessageInChat(message, false);
                } else if(message.getType().equals(UserObject.MESSAGE_TYPE)){
                    // If the message has the "Greeting" type then display the userObject
                    addUserToUsersContainer(UserObject.fromNearbyMessage(message));
                }
            }

            @Override
            public void onLost(Message message) {
                if (message.getType().equals(MessageObject.MESSAGE_TYPE)) {
                    // If the message has the "Message" type then remove it if it was lost
                    removeMessageOnLost(message);
                } else if (message.getType().equals(UserObject.MESSAGE_TYPE)){
                    // If the message has the "Greeting" type then remove it if it was lost
                    removeUserFromUsersContainer(UserObject.fromNearbyMessage(message));
                }
            }
        };
    }

    /**
     * Check if the user hasn't permanently dismissed the Message Info dialog, if not then show it
     */
    private void checkAndShowMessageDialog() {
        // Get the amount of times the dialog has been dismissed and if it has been tagged as being permanently dismissed.
        int counter = MainActivity.sharedPreferences.getInt(DIALOG_COUNTER_KEY, 1);
        boolean dismissed = MainActivity.sharedPreferences.getBoolean(DIALOG_DISMISSED_KEY, false);

        // Only show the dialog if the user hasn't permanently dismissed it or the counter is less than 5.
        if (counter < 5 && !dismissed){
            DialogFragment mDialog = new MessageDialog();
            mDialog.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
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
                            // Add to channel as the message was published successfully (set fromUser to true)
                            displayMessageInChat(mPubMessage, true);
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
    private void displayMessageInChat(Message message, boolean fromUser){
        // Get the messageObject from the received Nearby Message
        MessageObject receivedMessage = MessageObject.fromNearbyMessage(message);
        // Set the fromUser to the passed boolean
        receivedMessage.setFromUser(fromUser);
        // Add to the RecyclerView if it doesn't already exist (i.e. the user has left and come back quickly
        // without stopping the activity)
        if (!mMessageObjects.contains(receivedMessage)) {
            mMessageObjects.add(receivedMessage);
            mMessageRecyclerAdapter.notifyItemInserted(mMessageObjects.size() - 1);
        }
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
        mMessageRecyclerAdapter.notifyItemRemoved(mMessageObjects.indexOf(lostMessage));
        mMessageObjects.remove(lostMessage);
    }

    /**
     * Remove the userIcon referring to the user from the usersContainer
     * @param userObject the user being removed
     */
    private void removeUserFromUsersContainer(UserObject userObject){
        mUserRecyclerAdapter.notifyItemRemoved(mUserObjects.indexOf(userObject));
        mUserObjects.remove(userObject);
    }

    /**
     * Add a userIcon to the User container to show who's in the chat currently
     * @param userObject the UserObject representing the user that has just joined the chat
     */
    private void addUserToUsersContainer(UserObject userObject){
        mUserObjects.add(userObject);
        mUserRecyclerAdapter.notifyItemInserted(mUserObjects.size() - 1);

        // Set the users_list background colour to the messageUsersList colour now that items are in the view
        RecyclerView usersList = (RecyclerView) findViewById(R.id.users_list);
        usersList.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.messageUsersField));
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
        if (mRootContainer != null){
            Snackbar.make(mRootContainer, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
