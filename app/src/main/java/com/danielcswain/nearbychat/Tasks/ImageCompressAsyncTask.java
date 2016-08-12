package com.danielcswain.nearbychat.Tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.danielcswain.nearbychat.ChatActivity;
import com.danielcswain.nearbychat.MainActivity;
import com.danielcswain.nearbychat.Messages.MessageObject;
import com.danielcswain.nearbychat.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by ulternate on 12/08/2016.
 *
 * AsyncTask to compress the selected Image and return the message as a base64 encoded string
 * ready to be sent as a Nearby Message
 */
public class ImageCompressAsyncTask extends AsyncTask<String, Void, String> {

    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";

    private static final int MAX_IMAGE_SIZE = 70000;
    private String mUsername;
    private String mAvatarColour;
    private boolean mFromUser;

    /**
     * The AsyncTask to compress the image and return an encoded Base64 string representation to be able
     * to send the image
     * @param strings the parameters needed to send the message and compress the image
     *                in order they are: filePath, sender's username, sender's avatar colour and whether from the user or not.
     * @return the encoded string
     */
    @Override
    protected String doInBackground(String... strings) {
        if (strings.length == 4) {
            int streamLength = MAX_IMAGE_SIZE;
            int compressionQuality = 55;

            // Get the message info from the strings
            String mFilePath = strings[0];
            mUsername = strings[1];
            mAvatarColour = strings[2];
            mFromUser = !strings[3].equals(FALSE);

            // Decode a bitmap from the file path string
            Bitmap bitmap = BitmapFactory.decodeFile(mFilePath);
            String bitmapString = "";

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Keep compressing the image until it is smaller than the MAX_IMAGE_SIZE
            while (streamLength >= MAX_IMAGE_SIZE && compressionQuality > 5) {
                // To avoid an out of memory error, flush and refresh the output stream
                try {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Reduce the compression quality
                compressionQuality -= 5;
                Log.d("compressionQuality", "cQ = " + compressionQuality);
                // Compress the image and update the streamLength
                bitmap.compress(Bitmap.CompressFormat.WEBP, compressionQuality, byteArrayOutputStream);
                streamLength = byteArrayOutputStream.size();
                Log.d("streamLength", "streamLength = " + streamLength);
            }

            // Only set the bitmapString if the streamLength is less than the max message size
            if (streamLength < MAX_IMAGE_SIZE) {
                bitmapString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
            }

            // Recycle the bitmap as we're finished with it to free up memory
            bitmap.recycle();

            return bitmapString;
        } else {
            return null;
        }
    }

    /**
     * Show the image on the chat board once it's been compressed
     * @param s the encoded compressed image
     */
    @Override
    protected void onPostExecute(String s) {
        if (!s.isEmpty()) {
            // The compression completed and returned a non-empty string, publish on the chat board
            ChatActivity.publishMessage(new MessageObject(mUsername, s, MessageObject.MESSAGE_CONTENT_IMAGE, mAvatarColour, mFromUser));
        } else {
            // The compression couldn't make the image small enough, it was too large to begin with. Notify the user
            ChatActivity.showSnackbar(MainActivity.mainContext.getString(R.string.error_image_to_large));
        }
    }
}
