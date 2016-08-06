package com.danielcswain.nearbychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Random;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHARED_PREFS_FILE = "NearbyChatPreferences";

    private EditText mUsernameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the viw and toolbar
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the buttons and fields from the view
        mUsernameField = (EditText) findViewById(R.id.username_field);
        ImageButton mGenerateUsernameButton = (ImageButton) findViewById(R.id.button_username_generate);
        ImageButton mPickAvatarColourButton = (ImageButton) findViewById(R.id.button_avatar_colour_picker);
        Button mEnterChatButton = (Button) findViewById(R.id.button_enter_chat);

        // Generate a random username when the mGenerateUsernameButton is clicked
        mGenerateUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsernameField.setText(generateUsername());
                mUsernameField.setSelection(mUsernameField.getText().length());
            }
        });

        // Launch a colour picker and select the avatar colour when mPickAvatarColourButton is clicked
        mPickAvatarColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        // Enter the chat room when mEnterChatButton is clicked
        mEnterChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO send generated username and colour when this is clicked, and check for empty fields
                // and save the values in shared prefs
                Intent enterChatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(enterChatIntent);
            }
        });
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
     * Generate a random username drawing from an array of moods and adjectives and animal names.
     * @return a String of the form "Mood/Adjective Animal"
     */
    private String generateUsername(){
        String[] moodsAndAdjectivesArray = getApplicationContext().getResources().getStringArray(R.array.moods_and_adjectives);
        String[] animalsArray = getApplicationContext().getResources().getStringArray(R.array.animals);
        return moodsAndAdjectivesArray[new Random().nextInt(moodsAndAdjectivesArray.length)] + " " +
                animalsArray[new Random().nextInt(animalsArray.length)];
    }
}
