# nearby-chat
Chat application using Google's Nearby Messages API

Chat with people nearby using a combination of bluetooth and ultrasonic audio.
Notes:
- All messages are visible to anyone who is in the chat room.
- Once you leave a chat your messages are unpublished and will dissapear (like Snapchat).
- You may hear your phone click with older phones (that's the ultrasonic audio broadcasting to the chat).
- If you take a bit of time when picking an image to send your old messages may get unpublished as you technically leave the chat window.
- Messages may have their order jumbled or changed randomly if you leave and return from the chat quickly.

## App store beta link

You can grab the latest beta apk from the Google Play Store at the following link: 

https://play.google.com/apps/testing/com.danielcswain.nearbychat

## Building in Android Studio

1. Import project into Android Studio.
2. Follow the steps at https://developers.google.com/nearby/messages/android/get-started to get your own api key.
3. Create a `gradle.properties` file in your root directory to store this API key.
4. Store the API key in the above file with the `NEARBY_API_KEY_DEBUG` or `NEARBY_API_KEY_RELEASE` variable name depending on your release flavour.
  (note, apks built with different keys won't be able to communicate with each other).
5. The api key is retreived during the build step for your release flavour, see `build.gradle` for more info (https://github.com/ulternate/nearby-chat/blob/master/app/build.gradle)
6. Don't store this key in your own repo ;)
