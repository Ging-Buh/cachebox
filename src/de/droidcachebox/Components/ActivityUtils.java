package de.droidcachebox.Components;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import android.app.Activity;
import android.content.Intent;

public class ActivityUtils
{
	private static int sTheme = 1;

	public final static int THEME_DEFAULT = 0;
	public final static int THEME_DAY = 1;
	public final static int THEME_NIGHT = 2;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme)
	{
		sTheme = theme;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity)
	{
		switch (sTheme)
		{
		default:
		case THEME_DEFAULT:
			break;
		case THEME_DAY:
			activity.setTheme(R.style.Theme_day);
			break;
		case THEME_NIGHT:
			activity.setTheme(R.style.Theme_night);
			break;
		
		}
		
		Global.initTheme(activity);	
		
	}
}
