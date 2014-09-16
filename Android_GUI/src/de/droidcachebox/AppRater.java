package de.droidcachebox;

import CB_Translation_Base.TranslationEngine.Translation;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater
{
	private final static String APP_TITLE = "Cachebox";
	private final static String APP_PACKAGE_NAME = "de.droidcachebox";

	private final static int DAYS_UNTIL_PROMPT = 10;// 3;
	private final static int LAUNCHES_UNTIL_PROMPT = 15;// 7;

	public static void app_launched(final Context mContext)
	{
		SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
		if (prefs.getBoolean("dontshowagain", false))
		{
			return;
		}

		final SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0)
		{
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT)
		{
			if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000))
			{
				main.mainActivity.runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						showRateDialog(mContext, editor);
					}
				});

			}
		}

		editor.commit();
	}

	public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor)
	{
		Dialog dialog = new Dialog(mContext);

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		String message = Translation.Get("Rate_Message", APP_TITLE);
		String title = Translation.Get("Rate_Title", APP_TITLE);
		String now = Translation.Get("Rate_now");
		String later = Translation.Get("Rate_later");
		String never = Translation.Get("Rate_never");

		builder.setMessage(message).setTitle(title).setIcon(mContext.getApplicationInfo().icon).setCancelable(false)
				.setPositiveButton(now, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						editor.putBoolean("dontshowagain", true);
						editor.commit();
						try
						{
							mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						dialog.dismiss();
					}
				}).setNeutralButton(later, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();

					}
				}).setNegativeButton(never, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (editor != null)
						{
							editor.putBoolean("dontshowagain", true);
							editor.commit();
						}
						dialog.dismiss();

					}
				});
		dialog = builder.create();

		dialog.show();
	}
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
