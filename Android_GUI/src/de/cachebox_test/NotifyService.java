package de.cachebox_test;

import CB_Locator.Events.GPS_FallBackEvent;
import CB_Locator.Events.GPS_FallBackEventList;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NotifyService extends Service implements GPS_FallBackEvent
{

	int mStartMode; // indicates how to behave if the service is killed
	boolean mAllowRebind; // indicates whether onRebind should be used

	final static int myID = 1234;

	// Binder wird von der ServiceConnection verwendet
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Usere verschachtelte Klasse
	 */
	class LocalBinder extends Binder
	{

		/**
		 * Damit erhalten wir Zugriff auf unseren Service. Gibt unsere Instanz des NotifyService zurück.
		 */
		NotifyService getService()
		{
			return NotifyService.this;
		}
	}

	@Override
	public void onCreate()
	{
		// The service is being created
		super.onCreate();
		GPS_FallBackEventList.Add(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// The service is starting, due to a call to startService()
		return mStartMode;
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBinder onBind(Intent intent)
	{
		// A client is binding to the service with bindService()

		Intent mainIntent = new Intent(this, main.class);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

		// This constructor is deprecated. Use Notification.Builder instead
		Notification notice = new Notification(R.drawable.cb_icon_0, "Cachebox", System.currentTimeMillis());

		// This method is deprecated. Use Notification.Builder instead.
		notice.setLatestEventInfo(this, "Cachebox", "is running", pendIntent);

		// notice.flags |= Notification.FLAG_NO_CLEAR;

		// TODO no Clear wieder einschalten => runNotification.flags |= Notification.FLAG_NO_CLEAR;

		startForeground(myID, notice);

		return mBinder;
	}

	public static boolean finish = false;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onUnbind(Intent intent)
	{
		// All clients have unbound with unbindService()

		if (finish)
		{
			// CB is closing from User
			stopForeground(true);
		}
		else
		{
			// CB is killing
			Log.d("CACHEBOX", "Service => ACB is killed");
			Intent mainIntent = new Intent(this, main.class);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

			// This constructor is deprecated. Use Notification.Builder instead
			Notification notice = new Notification(R.drawable.cb_killed, "Cachebox", System.currentTimeMillis());

			// This method is deprecated. Use Notification.Builder instead.
			notice.setLatestEventInfo(this, "Cachebox", "was killing", pendIntent);

			// notice.flags |= Notification.FLAG_NO_CLEAR;

			// TODO no Clear wieder einschalten => runNotification.flags |= Notification.FLAG_NO_CLEAR;

			startForeground(myID, notice);

		}

		return mAllowRebind;
	}

	@Override
	public void onRebind(Intent intent)
	{
		// A client is binding to the service with bindService(),
		// after onUnbind() has already been called
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy()
	{
		// The service is no longer used and is being destroyed

		if (!finish)
		{
			Log.d("CACHEBOX", "Service => ACB is killed");
			Intent mainIntent = new Intent(this, main.class);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

			// This constructor is deprecated. Use Notification.Builder instead
			Notification notice = new Notification(R.drawable.cb_killed, "Cachebox", System.currentTimeMillis());

			// This method is deprecated. Use Notification.Builder instead.
			notice.setLatestEventInfo(this, "Cachebox", "was killing", pendIntent);

		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void FallBackToNetworkProvider()
	{
		Intent mainIntent = new Intent(this, main.class);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

		// This constructor is deprecated. Use Notification.Builder instead
		Notification notice = new Notification(R.drawable.cb_icon_0, "Cachebox", System.currentTimeMillis());

		// This method is deprecated. Use Notification.Builder instead.
		notice.setLatestEventInfo(this, "Cachebox", "is running", pendIntent);

		// notice.flags |= Notification.FLAG_NO_CLEAR;

		// TODO no Clear wieder einschalten => runNotification.flags |= Notification.FLAG_NO_CLEAR;

		startForeground(myID, notice);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Fix()
	{
		Intent mainIntent = new Intent(this, main.class);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

		// This constructor is deprecated. Use Notification.Builder instead
		Notification notice = new Notification(R.drawable.cb_icon_1, "Cachebox", System.currentTimeMillis());

		// This method is deprecated. Use Notification.Builder instead.
		notice.setLatestEventInfo(this, "Cachebox", "is running", pendIntent);

		// notice.flags |= Notification.FLAG_NO_CLEAR;

		// TODO no Clear wieder einschalten => runNotification.flags |= Notification.FLAG_NO_CLEAR;

		startForeground(myID, notice);
	}

}
