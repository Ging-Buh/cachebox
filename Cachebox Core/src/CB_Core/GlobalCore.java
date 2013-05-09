package CB_Core;

import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.DisplayType;
import CB_Core.Log.Logger;
import CB_Core.Map.RouteOverlay;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;

import com.badlogic.gdx.utils.Clipboard;

public class GlobalCore
{

	public static final int CurrentRevision = 1628;
	public static final String CurrentVersion = "0.6.";
	public static final String VersionPrefix = "Test";

	public static final String br = System.getProperty("line.separator");
	public static final String fs = System.getProperty("file.separator");
	// public static final String ps = System.getProperty("path.separator");
	public static final String AboutMsg = "Team Cachebox (2011-2013)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009,"
			+ br + "Groundspeak Inc. Used with permission";
	public static final String splashMsg = AboutMsg + br + br + br + "POWERED BY:";

	public static boolean restartAfterKill = false;
	public static String restartCache;
	public static String restartWaypoint;

	public static boolean ResortAtWork = false;
	public static final int LatestDatabaseChange = 1024;
	public static final int LatestDatabaseFieldNoteChange = 1005;
	public static final int LatestDatabaseSettingsChange = 1002;
	public static double displayDensity = 1;
	public static Plattform platform = Plattform.undef;

	// ######### theme Path ###############
	public static String PathDefault;
	public static String PathCustom;
	public static String PathDefaultNight;
	public static String PathCustomNight;
	// ######################################

	public static RouteOverlay.Track AktuelleRoute = null;
	public static int aktuelleRouteCount = 0;
	public static long TrackDistance;

	public static boolean switchToCompassCompleted = false;

	public static GlobalLocationReceiver receiver;

	private static Clipboard defaultClipBoard;

	public static Clipboard getDefaultClipboard()
	{
		if (defaultClipBoard == null)
		{
			return null;
		}
		else
		{
			return defaultClipBoard;
		}
	}

	public static void setDefaultClipboard(Clipboard clipBoard)
	{
		defaultClipBoard = clipBoard;
	}

	/**
	 * Wird im Splash gesetzt und ist True, wenn es sich um ein Tablet handelt!
	 */
	public static boolean isTab = false;

	public static boolean forceTab = false;

	public static boolean forcePhone = false;

	public static boolean useSmallSkin = false;

	public static DisplayType displayType = DisplayType.Normal;

	public static boolean posibleTabletLayout;

	private static Cache selectedCache = null;
	private static boolean autoResort;

	public static FilterProperties LastFilter = null;

	public static void setSelectedCache(Cache cache)
	{
		setSelectedWaypoint(cache, null);
	}

	public static Cache getSelectedCache()
	{
		return selectedCache;
	}

	private static Cache nearestCache = null;

	public static Cache NearestCache()
	{
		return nearestCache;
	}

	private static Waypoint selectedWaypoint = null;

	public static void setSelectedWaypoint(Cache cache, Waypoint waypoint)
	{
		setSelectedWaypoint(cache, waypoint, true);
	}

	/**
	 * if changeAutoResort == false -> do not change state of autoResort Flag
	 * 
	 * @param cache
	 * @param waypoint
	 * @param changeAutoResort
	 */
	public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort)
	{
		selectedCache = cache;
		selectedWaypoint = waypoint;
		SelectedCacheEventList.Call(selectedCache, waypoint);

		if (changeAutoResort)
		{
			// switch off auto select
			GlobalCore.setAutoResort(false);
		}
	}

	public static void NearestCache(Cache nearest)
	{
		nearestCache = nearest;
	}

	public static Waypoint getSelectedWaypoint()
	{
		return selectedWaypoint;
	}

	public static CB_Core.Types.Categories Categories = null;

	/**
	 * SDBM-Hash algorithm for storing hash values into the database. This is neccessary to be compatible to the CacheBox@Home project.
	 * Because the standard .net Hash algorithm differs from compact edition to the normal edition.
	 * 
	 * @param str
	 * @return
	 */
	public static long sdbm(String str)
	{
		if (str == null || str.equals("")) return 0;

		long hash = 0;
		// set mask to 2^32!!!???!!!
		long mask = 42949672;
		mask = mask * 100 + 95;

		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			hash = (c + (hash << 6) + (hash << 16) - hash) & mask;
		}

		return hash;
	}

	static String FormatDM(double coord, String positiveDirection, String negativeDirection)
	{
		int deg = (int) coord;
		double frac = coord - deg;
		double min = frac * 60;

		String result = Math.abs(deg) + "\u00B0  " + String.format("%.3f", Math.abs(min));

		result += " ";

		if (coord < 0) result += negativeDirection;
		else
			result += positiveDirection;

		return result;
	}

	public static String FormatLatitudeDM(double latitude)
	{
		return FormatDM(latitude, "N", "S");
	}

	public static String FormatLongitudeDM(double longitude)
	{
		return FormatDM(longitude, "E", "W");
	}

	public static String Rot13(String message)
	{
		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lookup = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM";

		String result = "";

		for (int i = 0; i < message.length(); i++)
		{
			String curChar = message.substring(i, i + 1);
			int idx = alphabet.indexOf(curChar);

			if (idx < 0) result += curChar;
			else
				result += lookup.substring(idx, idx + 1);
		}

		return result;

	}

	/**
	 * APIisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein API Access Token vorhanden ist.
	 */
	public static boolean APIisOnline()
	{
		if (Config.GetAccessToken().length() == 0)
		{
			Logger.General("GlobalCore.APIisOnline() - no GC - API AccessToken");
			return false;
		}
		if (platformConector.isOnline())
		{
			return true;
		}
		return false;
	}

	/**
	 * JokerisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein Passwort für gcJoker.de vorhanden ist.
	 */
	public static boolean JokerisOnline()
	{
		if (Config.settings.GcJoker.getValue().length() == 0)
		{
			Logger.General("GlobalCore.JokerisOnline() - no Joker Password");
			return false;
		}
		if (platformConector.isOnline())
		{
			return true;
		}
		return false;
	}

	public static String getVersionString()
	{
		final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  "
				+ (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
		return ret;
	}

	public static Coordinate getSelectedCoord()
	{
		Coordinate ret = null;

		if (selectedWaypoint != null)
		{
			ret = selectedWaypoint.Pos;
		}
		else if (selectedCache != null)
		{
			ret = selectedCache.Pos;
		}

		return ret;
	}

	public static boolean getAutoResort()
	{
		return autoResort;
	}

	public static void setAutoResort(boolean value)
	{
		GlobalCore.autoResort = value;
	}

	private static boolean isTestVersionCheked = false;
	private static boolean isTestVersion = false;

	public static boolean isTestVersion()
	{
		if (isTestVersionCheked) return isTestVersion;
		isTestVersion = VersionPrefix.contains("Test");
		isTestVersionCheked = true;
		return isTestVersion;
	}
}
