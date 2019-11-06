package de.droidcachebox.activities;

import CB_Translation_Base.TranslationEngine.Translation;
import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import de.droidcachebox.R;

public class CBForeground extends IntentService {
    public CBForeground() {
        super("CBForeground");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        createAndShowForegroundNotification(this, 14711);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            try {
                Thread.sleep(360000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
        }
    }

    private int lastShownNotificationId;
    private void createAndShowForegroundNotification(Service yourService, int notificationId) {

        final NotificationCompat.Builder builder = getNotificationBuilder(yourService,
                "de.droidcachebox.CBForeground", // Channel id
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        builder.setOngoing(true)
                .setSmallIcon(R.drawable.cb) // .notification_icon
                .setContentTitle("Cachebox") // .setContentTitle(yourService.getString(R.string.title))
                .setContentText(Translation.get("welcome")); // .setContentText(yourService.getString(R.string.content));

        Notification notification = builder.build();

        yourService.startForeground(notificationId, notification);

        if (notificationId != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager nm = (NotificationManager) yourService.getSystemService(Activity.NOTIFICATION_SERVICE);
            nm.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = notificationId;
    }

    public NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = context.getString(R.string.app_name); // R.string.notifications_channel_description
        final NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);
            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nm.createNotificationChannel(nChannel);
            }
        }
    }

}
