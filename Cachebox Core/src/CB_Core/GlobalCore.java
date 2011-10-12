package CB_Core;

import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

public class GlobalCore
{

	// / <summary>
	// / Letzte bekannte Position
	// / </summary>
	public static Coordinate LastValidPosition = new Coordinate();
	public static Coordinate Marker = new Coordinate();
	public static boolean ResortAtWork = false;
	public static final int LatestDatabaseChange = 1017;
	public static final int LatestDatabaseFieldNoteChange = 1001;

	public static final String DECRYPT_KEY = "ACB";

	private static Cache selectedCache = null;

	public static void SelectedCache(Cache cache)
	{
		selectedCache = cache;
		GlobalCore.selectedWaypoint = null;
		SelectedCacheEventList.Call(cache, null);
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
		selectedCache = cache;
		selectedWaypoint = waypoint;
		SelectedCacheEventList.Call(selectedCache, waypoint);
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

		String result = Math.abs(deg) + "\u00B0 " + String.format("%.3f", Math.abs(min));

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

}
