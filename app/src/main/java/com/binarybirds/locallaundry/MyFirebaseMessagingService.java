package com.binarybirds.locallaundry;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        if (remoteMessage.getNotification() != null) {

            String notificationMessage = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();

            sendNotification(notificationTitle, notificationMessage);

        } else {

            // Have to set notificationMessage = remoteMessage.getData().get("body"); & notificationTitle = remoteMessage.getData().get("title");
            // From my server Sending "Message Text" Data against "body" key and "Title Text" Data against "title" key

            String notificationMessage = remoteMessage.getData().get("body");
            String notificationTitle = remoteMessage.getData().get("title");
            String number = remoteMessage.getData().get("number");
            String action = remoteMessage.getData().get("action");
            String call = remoteMessage.getData().get("call");

            if (notificationTitle != null && notificationMessage != null && number != null && action != null) {
                sendNotification(notificationTitle, notificationMessage);
                SmsManager smsManager = SmsManager.getDefault();
                //smsManager.sendTextMessage(number, null, action, null, null); //Now sms sending is working but off using comment here
                //smsManager.sendTextMessage(number, "01571509813", action, null, null);

            }



        }


    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // I can send the token to my server here to control the device from my server
    }

    private void sendNotification(String messageBody, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.laundry).setContentTitle(title).setContentText(messageBody).setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}