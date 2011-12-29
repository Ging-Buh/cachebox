


package de.CB_GC_Joker_PlugIn;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import de.CB_PlugIn.IPlugIn;


public class PluginService extends Service {
	static final String LOG_TAG = "ResPluginService1";

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {

		Log.d(LOG_TAG, "Bind");

		return binder;
	}

	
	
	private final IPlugIn.Stub binder = new IPlugIn.Stub() {

		@Override
		public boolean call(String TelephoneNumber)
		{
			// Telefonnummer wählen
			try
			{
				// TelephoneNumber = "0..."; // Telefonnummer zum testen
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				callIntent.setData(Uri.parse("tel:" + TelephoneNumber));
				startActivity(callIntent);
//				TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//				listener = new ListenToPhoneState();
//				tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
				
				return true;
			}
			catch (ActivityNotFoundException e)
			{				
				Log.e("DroidCachebox", "Call failed", e);
				return false;
			}
		}
	
	};
}
