package CB_Core;

import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector;
import CB_Core.Log.Logger;
import CB_Core.Map.RouteOverlay;
import CB_Core.TranslationEngine.LangStrings;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.utils.Clipboard;

public class GlobalCore
{

	public static final int CurrentRevision = 1069;
	public static final String CurrentVersion = "0.5.";
	public static final String VersionPrefix = "Test";

	public static final String br = System.getProperty("line.separator");
	public static final String AboutMsg = "Team Cachebox (2011-2012)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009,"
			+ br + "Groundspeak Inc. Used with permission";
	public static final String splashMsg = AboutMsg + br + br + br + "POWERED BY:";

	// / <summary>
	// / Letzte bekannte Position
	// / </summary>
	public static Coordinate LastValidPosition = new Coordinate();
	public static Coordinate LastPosition = new Coordinate();
	// public static Coordinate Marker = new Coordinate();
	public static boolean ResortAtWork = false;
	public static final int LatestDatabaseChange = 1022;
	public static final int LatestDatabaseFieldNoteChange = 1003;
	public static final int LatestDatabaseSettingsChange = 1002;
	public static double displayDensity = 1;
	public static Plattform platform = Plattform.undef;

	public static CB_Core.Locator.Locator Locator = null;

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

	public static boolean useSmallSkin = false;

	public static LangStrings Translations = new LangStrings();

	private static Cache selectedCache = null;
	public static boolean autoResort;

	public static FilterProperties LastFilter = null;

	public static void SelectedCache(Cache cache)
	{
		SelectedWaypoint(cache, null);
	}

	public static Cache SelectedCache()
	{
		return selectedCache;
	}

	private static Cache nearestCache = null;

	public static Cache NearestCache()
	{
		return nearestCache;
	}

	private static Waypoint selectedWaypoint = null;

	public static void SelectedWaypoint(Cache cache, Waypoint waypoint)
	{
		SelectedWaypoint(cache, waypoint, true);
	}

	/**
	 * if changeAutoResort == false -> do not change state of autoResort Flag
	 * 
	 * @param cache
	 * @param waypoint
	 * @param changeAutoResort
	 */
	public static void SelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort)
	{
		selectedCache = cache;
		selectedWaypoint = waypoint;
		SelectedCacheEventList.Call(selectedCache, waypoint);

		if (changeAutoResort)
		{
			// switch off auto select
			GlobalCore.autoResort = false;
			Config.settings.AutoResort.setValue(GlobalCore.autoResort);
		}
	}

	public static void NearestCache(Cache nearest)
	{
		nearestCache = nearest;
	}

	public static Waypoint SelectedWaypoint()
	{
		return selectedWaypoint;
	}

	public static CB_Core.Types.Categories Categories = null;

	// / <summary>
	// / SDBM-Hash algorithm for storing hash values into the database. This is
	// neccessary to be compatible to the CacheBox@Home project. Because the
	// / standard .net Hash algorithm differs from compact edition to the normal
	// edition.
	// / </summary>
	// / <param name="str"></param>
	// / <returns></returns>
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
			Logger.General("global.APIisOnline() -Invalid AccessToken");
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
			Logger.General("global.APIisOnline() -Invalid Joker");
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

}
