package com.danielcswain.nearbychat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ArrayList<Message> mMessages;
    EditText mTextField;
    RecyclerView.Adapter mMessageRecyclerAdapter;
    RecyclerView mMessagesRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the messages
        mMessages = new ArrayList<>();
        mMessages.add(new Message("ulternate", "This is a message from me", true));
        mMessages.add(new Message("kenneth", "This is a message from someone else", false));
        mMessages.add(new Message("ulternate", "This is another message from me", true));
        mMessages.add(new Message("ulternate", "This is a really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really, really long message", true));

        // Get a reference to the RecyclerView and set the recycler view to have fixed layout size
        // (as the layout is already full screen)
        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messages_list);
        mMessagesRecyclerView.setHasFixedSize(true);

        // Using a stock linear layout manager for the RecyclerView
        mLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(mLayoutManager);

        // Set up the RecyclerView Adapter with the temporary data set and assign it to the RecyclerView
        mMessageRecyclerAdapter = new MessageAdapter(mMessages);
        mMessagesRecyclerView.setAdapter(mMessageRecyclerAdapter);

        // Get the message send views
        mTextField = (EditText) findViewById(R.id.text_entry_field);
        ImageButton submitButton = (ImageButton) findViewById(R.id.message_send_button);

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
                        mMessages.add(new Message("ulternate", message, bool));
                    } else {
                        mMessages.add(new Message("kenneth", message, bool));
                    }
                    mMessageRecyclerAdapter.notifyDataSetChanged();
                    hideSoftKeyboard(MainActivity.this, view);
                    mTextField.setText("");
                    mTextField.clearFocus();
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

    public static void hideSoftKeyboard(Activity activity, View view){
        InputMethodManager mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
