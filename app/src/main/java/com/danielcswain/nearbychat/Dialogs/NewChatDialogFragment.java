package com.danielcswain.nearbychat.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.danielcswain.nearbychat.R;

/**
 * Created by ulternate on 3/08/2016.
 *
 * Custom dialog fragment used to set the nearby chat channel title, topic and whethere it is
 * private (which needs a password).
 */
public class NewChatDialogFragment extends DialogFragment {

    private EditText mChannelNameET;
    private EditText mChannelTopicET;
    private EditText mChannelPasswordET;
    private SwitchCompat mPrivateSwitch;

    /**
     * Initiate the custom dialog using the custom layout
     * @param savedInstanceState
     * @return the dialog object created by AlertDialog.Builder using the custom layout
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate the custom view for the NewChatDialogFragment and set the toggle listener
        final View view = inflater.inflate(R.layout.new_chat_dialog, null);

        // Get references to the input fields
        mChannelNameET = (EditText) view.findViewById(R.id.dialog_channel_name);
        mChannelTopicET = (EditText) view.findViewById(R.id.dialog_channel_topic);
        mChannelPasswordET = (EditText) view.findViewById(R.id.dialog_channel_password);

        // Show the password field if the toggle is checked
        mPrivateSwitch = (SwitchCompat) view.findViewById(R.id.dialog_private_switch);
        mPrivateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mChannelPasswordET.setVisibility(View.VISIBLE);
                } else {
                    mChannelPasswordET.setVisibility(View.GONE);
                }
            }
        });

        // Inflate and set the layout for the NewChatDialogFragment
        builder.setView(view)
                // Set the positive button action
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // No actions here as we're overriding the onStart method to control when the dialog
                        // is dismissed. If the logic was here then the dialog is always dismissed.
                    }
                })
                // Set the negative button action
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NewChatDialogFragment.this.getDialog().dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get the dialog
        AlertDialog dialog = (AlertDialog) getDialog();

        // Handle the positive button click if we got the reference to the dialog.
        if (dialog != null){
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String channelName = mChannelNameET.getText().toString();
                    String channelTopic = mChannelTopicET.getText().toString();

                    if (!channelName.isEmpty() && !channelTopic.isEmpty()){
                        Toast.makeText(getActivity(), "Channel " + channelName + " created, with the following topic: " + channelTopic, Toast.LENGTH_SHORT).show();
                        //TODO create channel and check password
                        NewChatDialogFragment.this.getDialog().dismiss();
                    } else {
                        Toast.makeText(getActivity(), R.string.dialog_channel_empty_fields_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
