package com.danielcswain.nearbychat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String NEW_CHAT_DIALOG_TAG = NewChatDialogFragment.class.getSimpleName();
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHARED_PREFS_FILE = "NearbyChatPreferences";
    private static final String SHARED_PREFS_CHANNEL_KEY = "channels";

    public static ChannelListAdapter channelListAdapter;
    public static ArrayList<ChannelObject> channelObjects;
    public static Message pubMessage;
    private static View sContainer;
    private static MessageListener sMessageListener;
    private static SharedPreferences sSharedPreferences;
    private static Context sContext;
    private static final Gson sGson = new Gson();

    protected static MainApplication sApplication;
    protected static GoogleApiClient sGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("lifecycle", "main activity onCreate called");
        // Get the application context and the application
        sContext = getApplicationContext();
        sApplication = (MainApplication) getApplication();

        // Get the root view for showing a snackbar
        sContainer = findViewById(R.id.channel_list);

        // Get the channel list view and layout title text view
        TextView channelListTitle = (TextView) findViewById(R.id.channel_list_title);
        ListView channelListView = (ListView) findViewById(R.id.channel_list);

        // Initiate the channel ArrayList and ListAdapter
        channelObjects = new ArrayList<>();
        channelListAdapter = new ChannelListAdapter(this, channelObjects);

        // Set the channel list title text and the connect the listAdapter to the ListView
        channelListTitle.setText(R.string.channel_list_title_no_channels);
        channelListView.setAdapter(channelListAdapter);

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
        sGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Message Listener used for the subscription to the NearbyAPI to get the nearby chat channels
        sMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Add the nearby channel to the channel list
                channelListAdapter.add(ChannelObject.fromNearbyMessage(message));
            }

            @Override
            public void onLost(final Message message) {
                // Remove the nearby channel from the channel list when it is no longer nearby
                channelListAdapter.remove(ChannelObject.fromNearbyMessage(message));
            }
        };

        // Get the shared preferences used for temporarily storing the chat channel information
        sSharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        // The following is to only run on the initial launch of the application
        if (MainApplication.firstStart){
            // Clear the sharedPreferences as no channels will be published until user specifies (no chats persist on close)
            sSharedPreferences.edit().clear().apply();

            // Set MainApplication.firstStart to false to stop this block repeating when onCreate is called again without an application relaunch
            MainApplication.setFirstStart(false);
        }
    }

    /**
     * Connect to the GoogleApiClient when onStart is called
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("lifecycle", "main activity onStart called");
        sGoogleApiClient.connect();
    }

    /**
     * When the activity is leaving view and onPause is called, store the user channels and start
     * an activity transition timer which will disconnect from the GoogleApiClient and unsubscribe/unpublish
     * when executed. This timer will be cancelled in the onResume methods of other activities and
     * should only be called when the app is closed or enters the background.
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Store the channels started by the user
        storeUsersChannelsInSharedPreferences(channelObjects);
        // Start the MainApplication Activity Transition timer which disconnects from the GoogleApiClient
        // if the app has entered the background.
        MainApplication.startActivityTransitionTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Stop the MainApplication Activity Transition timer if it hasn't executed, this keeps a connection
        // to the GoogleApiClient if transitioning between activities.
        MainApplication.stopActivityTransitionTimer();
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
        subscribeToNearbyChannels();
        // Get the Set of channel JSONObject strings from the shared preferences (channels the user had published in
        // this current session).
        Set<String> channels = sSharedPreferences.getStringSet(SHARED_PREFS_CHANNEL_KEY, new HashSet<String>());
        // If the user had published a chat channel in this session then re-publish it and add it back to the chat channel list
        for (String channel: channels){
            ChannelObject channelObject = sGson.fromJson(channel, ChannelObject.class);
            publishMessage(ChannelObject.newNearbyMessage(channelObject));
        }
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
                sGoogleApiClient.connect();
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
        Nearby.Messages.publish(sGoogleApiClient, message)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            // Add the chat channel to the channel list if it isn't there already
                            pubMessage = message;
                            ChannelObject publishedChannel = ChannelObject.fromNearbyMessage(message);
                            if(channelObjects.isEmpty() || !channelObjects.contains(publishedChannel)){
                                channelObjects.add(publishedChannel);
                                channelListAdapter.notifyDataSetChanged();
                                showSnackbar(sContext.getString(R.string.nearby_publish_channel_success));
                            }
                        } else {
                            showSnackbar(sContext.getString(R.string.nearby_publish_channel_failed_status) + status);
                        }
                    }
                });
    }

    /**
     * Subscribe to receive nearby chat channels.
     * Show a snackbar to notify of success or failure
     */
    private static void subscribeToNearbyChannels(){
        Nearby.Messages.subscribe(sGoogleApiClient, sMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            showSnackbar(sContext.getString(R.string.nearby_subscription_success));
                        } else {
                            showSnackbar(sContext.getString(R.string.nearby_subscription_failed_status) + status);
                        }
                    }
                });
    }

    public static void unpublishNearbyChannel() {
        Log.i(TAG, "Unpublishing message: " + ChannelObject.fromNearbyMessage(pubMessage).getChannelTitle());
        Nearby.Messages.unpublish(sGoogleApiClient, pubMessage);
    }

    public static void unsubscribeFromNearbyChannels(){
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(sGoogleApiClient, sMessageListener);
    }

    private static void showSnackbar(String message){
        if (sContainer != null){
            Snackbar.make(sContainer, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Store the chat channels the user has opened in this instance of the application in SharedPreferences
     * @param channelObjects the list of channel objects the user is nearby (both published and subscribed to)
     */
    private void storeUsersChannelsInSharedPreferences(ArrayList<ChannelObject> channelObjects){
        if (!channelObjects.isEmpty()){
            // Create a String set for the ChannelObjects as Sets of JSONObjects
            Set<String> channels = new HashSet<>();
            for(ChannelObject channelObject : channelObjects){
                // Only store the channels the user created/owns as the other channels may not be active
                // when the app is relaunched.
                if (channelObject.getChannelIsUsers()) {
                    String channelJSON = sGson.toJson(channelObject);
                    channels.add(channelJSON);
                }
            }
            // Save to sSharedPreferences
            sSharedPreferences.edit().putStringSet(SHARED_PREFS_CHANNEL_KEY, channels).apply();
        }
    }
}
