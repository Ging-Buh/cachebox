package CB_Core.Types;

import java.util.ArrayList;
import java.util.Collections;

import CB_Core.GlobalCore;
import CB_Core.Enums.CacheTypes;

public class CacheList extends MoveableList<Cache>
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
		if (GlobalCore.LastValidPosition == null) return;

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
			Cache nextCache = this.get(0); // or null ...
			for (int i = 0; i < this.size(); i++)
			{
				nextCache = this.get(i);
				if (!nextCache.Archived)
				{
					if (nextCache.Available)
					{
						if (!nextCache.Found) // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht) , aber found
												// kann nicht rückgängig gemacht werden.
						{
							if (!nextCache.ImTheOwner())
							{
								if (nextCache.Type != CacheTypes.Mystery)
								{
									break;
								}
								else
								{
									if (nextCache.CorrectedCoordiantesOrMysterySolved())
									{
										break;
									}
								}
							}
						}
					}
				}
			}
			// Wenn der nachste Cache ein Mystery mit Final Waypoint ist
			// -> gleich den Final Waypoint auswahlen!!!
			// When the next Cache is a mystery with final waypoint
			// -> activate the final waypoint!!!
			Waypoint waypoint = nextCache.GetFinalWaypoint();

			// do not Change AutoResort Flag when selecting a Cache in the Resort function
			GlobalCore.setSelectedWaypoint(nextCache, waypoint, false);
			GlobalCore.NearestCache(nextCache);
		}

		CB_Core.Events.CachListChangedEventList.Call();

		// vorhandenen Parkplatz Cache nach oben schieben
		Cache park = this.GetCacheByGcCode("CBPark");
		if (park != null)
		{
			this.MoveItemFirst(this.indexOf(park));
		}

		// Cursor.Current = Cursors.Default;
		GlobalCore.ResortAtWork = false;
	}

}
