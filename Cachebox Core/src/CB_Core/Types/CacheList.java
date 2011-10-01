package CB_Core.Types;

import java.util.ArrayList;
import java.util.Collections;

import CB_Core.GlobalCore;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public class CacheList extends ArrayList<Cache>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayList<MysterySolution> MysterySolutions;

	public Cache GetCacheByGcCode(String GcCode)
	{
		for (Cache cache : this)
		{
			if (cache.GcCode.equalsIgnoreCase(GcCode)) return cache;
		}
		return null;
	}

	public Cache GetCacheById(long cacheId)
	{
		for (Cache cache : this)
		{
			if (cache.Id == cacheId) return cache;
		}
		return null;
	}

	public void Resort()
	{
		GlobalCore.ResortAtWork = true;
		// Alle Distanzen aktualisieren
		for (Cache cache : this)
		{
			cache.Distance(true);
		}

		Collections.sort(this);

		// Nächsten Cache auswählen
		if (this.size() > 0)
		{
			Cache nextCache = this.get(0);
			// Wenn der nachste Cache ein Mystery mit Final Waypoint ist
			// -> gleich den Final Waypoint auswahlen!!!
			// When the next Cache is a mystery with final waypoint
			// -> activate the final waypoint!!!
			Waypoint waypoint = nextCache.GetFinalWaypoint();
			GlobalCore.SelectedWaypoint(nextCache, waypoint);
			// Global.SelectedCache = Geocaching.Cache.Query[0];
			GlobalCore.NearestCache(nextCache);
		}

		CB_Core.Events.CachListChangedEventList.Call();

		// Cursor.Current = Cursors.Default;
		GlobalCore.ResortAtWork = false;
	}

}
