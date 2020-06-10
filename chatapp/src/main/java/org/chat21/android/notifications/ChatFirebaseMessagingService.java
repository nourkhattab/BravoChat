package org.chat21.android.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import org.chat21.android.utils.StringUtils;

import chat21.android.demo.R;

import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;
import static org.chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
import static org.chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;

/**
 * Created by stefanodp91 on 06/02/18.
 */

public class ChatFirebaseMessagingService extends FirebaseMessagingService {

    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(DEBUG_NOTIFICATION, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(DEBUG_NOTIFICATION, "Message data payload: " + remoteMessage.getData());

            String sender = remoteMessage.getData().get("sender");
            String senderFullName = remoteMessage.getData().get("sender_fullname");
            String channelType = remoteMessage.getData().get("channel_type");
            String text = remoteMessage.getData().get("text");
            String timestamp = remoteMessage.getData().get("timestamp");
            String recipientFullName = remoteMessage.getData().get("recipient_fullname");
            String recipient = remoteMessage.getData().get("recipient");

            if(text.contains("https://firebasestorage"))
                text = "Photo";

            String currentOpenConversationId = ChatManager.getInstance()
                    .getConversationsHandler()
                    .getCurrentOpenConversationId();

            if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {

                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(sender)) {
                    sendDirectNotification(sender, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendDirectNotification(sender, senderFullName, text, channelType);
                    }
                }
            } else if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(recipient)) {
                    sendGroupNotification(recipient, recipientFullName, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendGroupNotification(recipient, recipientFullName, senderFullName, text, channelType);
                    }
                }
            } else {
                // default case
                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(sender)) {
                    sendDirectNotification(sender, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendDirectNotification(sender, senderFullName, text, channelType);
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Toast.makeText(this, "foreground notification", Toast.LENGTH_LONG ).show();
            Log.d(DEBUG_NOTIFICATION, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            example DIRECT:
//            Message Notification Body: Foreground
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Create and show a direct notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendDirectNotification(String sender, String senderFullName, String text, String channel) {

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_notification_small)
                        .setContentTitle(senderFullName)
                        .setSound(defaultSoundUri)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo fix
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d(DEBUG_NOTIFICATION, "Message Notification " + "Oreo fix");

            NotificationChannel mChannel = new NotificationChannel(
                    channel, channel, NotificationManager.IMPORTANCE_HIGH);

            mChannel.setVibrationPattern(new long[]{300, 300, 300});
            if (defaultSoundUri != null) {
                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mChannel.setSound(defaultSoundUri, att);
            }
            notificationManager.createNotificationChannel(mChannel);

        }

        int notificationId = (int) new Date().getTime();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Create and show a group notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendGroupNotification(String sender, String senderFullName, String recipientFullName, String text, String channel) {

        String title = recipientFullName + " @" + senderFullName;

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_notification_small)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo fix
        String channelId = channel;
        String channelName = channel;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int notificationId = (int) new Date().getTime();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}