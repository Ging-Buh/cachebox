package de.cachebox_test;

import CB_Locator.Events.GPS_FallBackEvent;
import CB_Locator.Events.GPS_FallBackEventList;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;

public class NotifyService extends Service implements GPS_FallBackEvent {

    public enum NotificationType {
	NO_GPS, GPS, KILLED
    }

    int mStartMode; // indicates how to behave if the service is killed
    boolean mAllowRebind; // indicates whether onRebind should be used

    /**
     * notificationID allows you to update the notification later on.
     */
    final static int myID = 1234;
    final NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    // Binder wird von der ServiceConnection verwendet
    private final IBinder mBinder = new LocalBinder();

    public NotifyService() {
	super();
	this.mBuilder = new NotificationCompat.Builder(this);

    }

    boolean intentSeted = false;

    private void showNotification(NotificationType type) {

	if (this.mNotificationManager == null)
	    this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	if (!intentSeted && main.mainActivity != null) {
	    try {
		Intent mainIntent = new Intent(this, main.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(main.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(mainIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		intentSeted = true;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	int iconId = R.drawable.cb_icon_0;

	switch (type) {
	case GPS:
	    iconId = R.drawable.cb_icon_1;
	    break;
	case KILLED:
	    iconId = R.drawable.cb_killed;
	    break;
	case NO_GPS:
	    iconId = R.drawable.cb_icon_0;
	    break;
	default:
	    iconId = R.drawable.cb_icon_0;
	    break;

	}
	Bitmap bm = BitmapFactory.decodeResource(getResources(), iconId);

	mBuilder.setSmallIcon(iconId);
	mBuilder.setLargeIcon(bm);
	mBuilder.setContentTitle("Cachebox");

	mBuilder.setAutoCancel(false);

	Notification notify = mBuilder.build();
	notify.flags |= Notification.FLAG_NO_CLEAR;

	int smallIconId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
	if (smallIconId != 0) {
	    notify.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
	}

	mNotificationManager.notify(myID, notify);
    }

    /**
     * Usere verschachtelte Klasse
     */
    class LocalBinder extends Binder {

	/**
	 * Damit erhalten wir Zugriff auf unseren Service. Gibt unsere Instanz des NotifyService zurück.
	 */
	NotifyService getService() {
	    return NotifyService.this;
	}
    }

    @Override
    public void onCreate() {
	// The service is being created
	super.onCreate();
	GPS_FallBackEventList.Add(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// The service is starting, due to a call to startService()
	return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
	// A client is binding to the service with bindService()
	showNotification(NotificationType.NO_GPS);
	return mBinder;
    }

    public static boolean finish = false;

    @Override
    public boolean onUnbind(Intent intent) {
	// All clients have unbound with unbindService()
	if (finish) {
	    // CB is closing from User
	    mNotificationManager.cancel(myID);
	    stopForeground(true);
	} else {
	    // CB is killing
	    Log.d("CACHEBOX", "Service => ACB is killed");
	    showNotification(NotificationType.KILLED);
	}
	return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
	// A client is binding to the service with bindService(),
	// after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
	// The service is no longer used and is being destroyed
	if (!finish) {
	    Log.d("CACHEBOX", "Service => ACB is killed");
	    showNotification(NotificationType.KILLED);
	}
    }

    @Override
    public void FallBackToNetworkProvider() {
	showNotification(NotificationType.NO_GPS);
    }

    @Override
    public void Fix() {
	showNotification(NotificationType.GPS);
    }

}
