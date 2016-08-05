package com.danielcswain.nearbychat;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.danielcswain.nearbychat.Channels.ChannelListAdapter;
import com.danielcswain.nearbychat.Channels.ChannelObject;
import com.danielcswain.nearbychat.Dialogs.NewChatDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleApiClient mGoogleApiClient;
    private static final String NEW_CHAT_DIALOG_TAG = NewChatDialogFragment.class.getSimpleName();
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String NEARBY_PUBLISH_CHANNEL_SUCCESS = String.valueOf(R.string.nearby_publish_channel_success);
    private static final String NEARBY_PUBLISH_CHANNEL_FAILED_STATUS = String.valueOf(R.string.nearby_publish_channel_failed_status);
    private static final String NEARBY_SUBSCRIPTION_CHANNEL_SUCCESS = String.valueOf(R.string.nearby_subscription_success);
    private static final String NEARBY_SUBSCRIPTION_CHANNEL_FAILED_STATUS = String.valueOf(R.string.nearby_subscription_failed_status);

    public static ChannelListAdapter mChannelListAdapter;
    private ArrayList<ChannelObject> channelObjects;
    public static Message mPubMessage;
    private static View mContainer;
    private MessageListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the root view for showing a snackbar
        mContainer = findViewById(R.id.channel_list).getRootView();

        // Get the channel list view and layout title text view
        TextView channelListTitle = (TextView) findViewById(R.id.channel_list_title);
        ListView channelListView = (ListView) findViewById(R.id.channel_list);

        // Initiate the channel ArrayList and ListAdapter
        channelObjects = new ArrayList<>();
        mChannelListAdapter = new ChannelListAdapter(this, channelObjects);

        // Set the channel list title text and the connect the listAdapter to the ListView
        channelListTitle.setText(R.string.channel_list_title_no_channels);
        channelListView.setAdapter(mChannelListAdapter);

        // Set the listView onListItemClickListener
        channelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO make this point to the correct Channel using the nearby API
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("channelTitle", channelObjects.get(i).getChannelTitle());
                intent.putExtra("channelTopic", channelObjects.get(i).getChannelTopic());
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.FragmentManager fm = getFragmentManager();
                NewChatDialogFragment newChatDialogFragment = new NewChatDialogFragment();
                newChatDialogFragment.show(fm, NEW_CHAT_DIALOG_TAG);
            }
        });

        // Build the GoogleApiClient to use the Nearby.Message API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Message Listener used for the subscription to the NearbyAPI to get the nearby chat channels
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Add the nearby channel to the channel list
                mChannelListAdapter.add(ChannelObject.fromNearbyMessage(message));
            }

            @Override
            public void onLost(final Message message) {
                // Remove the nearby channel from the channel list when it is no longer nearby
                mChannelListAdapter.remove(ChannelObject.fromNearbyMessage(message));
            }
        };
    }

    /**
     * Connect to the GoogleApiClient when onStart is called
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * When the activity is no longer in view and onStop is called, unpublish and unsubscribe and
     * disconnect from the GoogleApiClient if it is connected.
     */
    @Override
    public void onStop() {
        unpublish();
        unsubscribe();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Subscribe to any Nearby devices when connected via the GoogleApiClient
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
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

    /**
     * Publish a Nearby Message and if successful add the chat channel to the list adapter
     * Show a snackbar to notify of success or failure
     */
    public static void publishMessage(final Message message){
        Nearby.Messages.publish(mGoogleApiClient, message)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            // Add the chat channel to the channel list
                            mChannelListAdapter.add(ChannelObject.fromNearbyMessage(message));
                            showSnackbar(NEARBY_PUBLISH_CHANNEL_SUCCESS);
                        } else {
                            showSnackbar(NEARBY_PUBLISH_CHANNEL_FAILED_STATUS + status);
                        }
                    }
                });
    }

    /**
     * Subscribe to receive nearby chat channels.
     * Show a snackbar to notify of success or failure
     */
    private void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            showSnackbar(NEARBY_SUBSCRIPTION_CHANNEL_SUCCESS);
                        } else {
                            showSnackbar(NEARBY_SUBSCRIPTION_CHANNEL_FAILED_STATUS + status);
                        }
                    }
                });
    }

    /**
     * Unpublish the most recent published channel message
     */
    //TODO update this to unpublish a specific channel message, add a method to unpublish all channel messages
    private void unpublish() {
        Log.i(TAG, "Unpublishing.");
        Nearby.Messages.unpublish(mGoogleApiClient, mPubMessage);
    }

    private void unsubscribe(){
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    private static void showSnackbar(String message){
        if (mContainer != null){
            Snackbar.make(mContainer, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
