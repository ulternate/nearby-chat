package com.danielcswain.nearbychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.Random;

public class MainActivity extends AppCompatActivity{

    private static final String COLOUR_PICKER_TAG = "Avatar Colour Picker";
    private static final String SHARED_PREFS_FILE = "NearbyChatPreferences";
    public static final String USERNAME_KEY = "username";
    public static final String AVATAR_COLOUR_KEY = "avatar_colour";
    private SharedPreferences mSharedPreferences;

    private EditText mUsernameField;
    private ImageButton mGenerateUsernameButton;
    private ImageButton mPickAvatarColourButton;
    private Button mEnterChatButton;
    private static Integer sCurrentAvatarColour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the viw and toolbar
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the buttons and fields from the view
        mUsernameField = (EditText) findViewById(R.id.username_field);
        mGenerateUsernameButton = (ImageButton) findViewById(R.id.button_username_generate);
        mPickAvatarColourButton = (ImageButton) findViewById(R.id.button_avatar_colour_picker);
        mEnterChatButton = (Button) findViewById(R.id.button_enter_chat);

        // Get any saved username and avatar colour from the shared preference file
        mSharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String username = mSharedPreferences.getString(USERNAME_KEY, "");
        String avatarColour = mSharedPreferences.getString(AVATAR_COLOUR_KEY, "");

        // Set the username field if the user had saved one previously
        if (!username.isEmpty() && !username.equals("")){
            mUsernameField.setText(username);
        }

        // Set sCurrentAvatarColour to the default colour (currently md_pink_500
        sCurrentAvatarColour = ContextCompat.getColor(getApplicationContext(), R.color.md_pink_500);

        // Change the sCurrentAvatarColour to the user's previous value if one exists
        if (!avatarColour.isEmpty() && !avatarColour.equals("")){
            try{
                // Try and parse the colour into a colour int
                sCurrentAvatarColour = Color.parseColor(avatarColour);
                // Set the background colour of the mPickAvatarColourButton to the users preference
                GradientDrawable buttonBackgroundShape = (GradientDrawable) mPickAvatarColourButton.getBackground();
                buttonBackgroundShape.setColor(sCurrentAvatarColour);
            } catch (IllegalArgumentException e){
                // Otherwise use the default md_pink_500 colour from the resources
                sCurrentAvatarColour = ContextCompat.getColor(getApplicationContext(), R.color.md_pink_500);
            }
        }

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
                launchColourPickerDialog(sCurrentAvatarColour);
            }
        });

        // Enter the chat room when mEnterChatButton is clicked
        mEnterChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the username and sCurrentAvatarColour
                String username = mUsernameField.getText().toString();
                String avatarColour = Integer.toHexString(sCurrentAvatarColour);

                // If the username is empty prompt the user and don't enter the chat
                if (username.isEmpty() || username.equals("")){
                    Snackbar.make(mEnterChatButton, getString(R.string.error_empty_username), Snackbar.LENGTH_SHORT).show();
                } else {
                    // Ensure the avatarColour string starts with # prior to sending and saving
                    if (!avatarColour.startsWith("#")){
                        avatarColour = "#" + avatarColour;
                    }

                    // Save the username and sCurrentAvatarColour in the shared preferences
                    storeUsernameAndAvatarColour(username, avatarColour);

                    // Enter the chat with the username and avatarColour sent to the ChatActivity
                    Intent enterChatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                    enterChatIntent.putExtra(USERNAME_KEY, username);
                    enterChatIntent.putExtra(AVATAR_COLOUR_KEY, avatarColour);
                    startActivity(enterChatIntent);
                }
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

    /**
     * Launch the colour picker dialog using the Spectrum Colour picker
     * https://github.com/the-blue-alliance/spectrum
     */
    private void launchColourPickerDialog(Integer currentColour){
        new SpectrumDialog.Builder(getApplicationContext())
                .setColors(R.array.avatar_colours)
                .setSelectedColor(currentColour)
                .setDismissOnColorSelected(true)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            // Change the button colour to the selected colour.
                            GradientDrawable buttonBackgroundShape = (GradientDrawable) mPickAvatarColourButton.getBackground();
                            buttonBackgroundShape.setColor(color);
                            // Update the sCurrentAvatarColour
                            sCurrentAvatarColour = color;
                        }
                    }
                }).build().show(getSupportFragmentManager(), COLOUR_PICKER_TAG);
    }

    private void storeUsernameAndAvatarColour(String username, String avatarColour){
        // Store the values in the SharedPreferences file
        mSharedPreferences.edit().putString(USERNAME_KEY, username).apply();
        mSharedPreferences.edit().putString(AVATAR_COLOUR_KEY, avatarColour).apply();
    }
}
