package CB_Core.Types;

import java.util.Collections;

import CB_Core.GlobalCore;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
import CB_Core.Util.MoveableList;
import CB_Locator.Coordinate;
import CB_Locator.Locator;

public class CacheList extends MoveableList<Cache>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		boolean LocatorValid = Locator.Valid();
		// Alle Distanzen aktualisieren
		if (LocatorValid)
		{
			for (Cache cache : this)
			{
				cache.Distance(true);
			}
		}
		else
		{
			// sort after Distance from selected Cache
			Coordinate fromPos = GlobalCore.getSelectedCoord();
			// avoid "illegal waypoint"
			if (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)
			{
				fromPos = GlobalCore.getSelectedCache().Pos;
			}
			if (fromPos == null)
			{
				GlobalCore.ResortAtWork = false;
				return;
			}
			for (Cache cache : this)
			{
				cache.Distance(true, fromPos);
			}
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
			if (waypoint == null)
			{
				// wenn ein Cache keinen Final Waypoint hat dann wird überprüft, ob dieser einen Startpunkt definiert hat
				// Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
				// selektiert
				waypoint = nextCache.GetStartWaypoint();
			}

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

	public void checkSelectedCacheValid()
	{
		// Prüfen, ob der SelectedCache noch in der cacheList drin ist.
		if ((size() > 0) && (GlobalCore.getSelectedCache() != null) && (GetCacheById(GlobalCore.getSelectedCache().Id) == null))
		{
			// der SelectedCache ist nicht mehr in der cacheList drin -> einen beliebigen aus der CacheList auswählen
			Logger.DEBUG("Change SelectedCache from " + GlobalCore.getSelectedCache().GcCode + "to" + get(0).GcCode);
			GlobalCore.setSelectedCache(get(0));
		}
		// Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
		if ((GlobalCore.getSelectedCache() == null) && (size() > 0))
		{
			GlobalCore.setSelectedCache(get(0));
			Logger.DEBUG("Set SelectedCache to " + get(0).GcCode + " first in List.");
		}
	}

}
