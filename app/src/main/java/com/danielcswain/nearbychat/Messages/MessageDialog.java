package com.danielcswain.nearbychat.Messages;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.danielcswain.nearbychat.ChatActivity;
import com.danielcswain.nearbychat.MainActivity;
import com.danielcswain.nearbychat.R;

/**
 * Created by ulternate on 11/08/2016.
 *
 * A custom Dialog to notify the user that the messages they send are ephemeral and will disappear
 * when they leave the chat. This dialog shows each time the user enters the chat unless the user
 * has clicked don't show again
 */
public class MessageDialog extends DialogFragment {

    /**
     * Create the dialog and set the positive and negative actions
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_messages_ephemeral_body)
                .setPositiveButton(R.string.dialog_messages_ephemeral_positive_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing the count is iterated in onDismiss
                    }
                })
                .setNegativeButton(R.string.dialog_messages_ephemeral_negative_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Set the dialog to not show again
                        MainActivity.sharedPreferences.edit().putBoolean(ChatActivity.DIALOG_DISMISSED_KEY, true).apply();
                    }
                });
        return builder.create();
    }

    /**
     * Iterate the dialog displayed counter by 1 when dismissed by either of the buttons
     * Store the count value in the shared preferences
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        int counter = MainActivity.sharedPreferences.getInt(ChatActivity.DIALOG_COUNTER_KEY, 1) + 1;
        MainActivity.sharedPreferences.edit().putInt(ChatActivity.DIALOG_COUNTER_KEY, counter).apply();
    }
}
