package com.danielcswain.nearbychat;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.danielcswain.nearbychat.Channels.ChannelListAdapter;
import com.danielcswain.nearbychat.Channels.ChannelObject;
import com.danielcswain.nearbychat.Dialogs.NewChatDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String NEW_CHAT_DIALOG_TAG = "New Chat Dialog";
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static ChannelListAdapter mChannelListAdapter;
    private ArrayList<ChannelObject> channelObjects;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the channel list view and layout title text view
        TextView channelListTitle = (TextView) findViewById(R.id.channel_list_title);
        ListView channelListView = (ListView) findViewById(R.id.channel_list);

        // Initiate the channel ArrayList and ListAdapter
        channelObjects = new ArrayList<>();
        mChannelListAdapter = new ChannelListAdapter(this, channelObjects);

        // Add a fake channel to the ArrayList
//        channelObjects.add(new ChannelObject("Group Chat", "A place for group discussion on the Group Project", true));

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart called");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //TODO
        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
//        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
    }

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
}
