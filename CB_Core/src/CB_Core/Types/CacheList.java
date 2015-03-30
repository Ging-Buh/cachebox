/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Core.Types;

import java.util.ArrayList;

import CB_Core.Enums.CacheTypes;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.MoveableList;

public class CacheList extends MoveableList<Cache>
{

	private static final long serialVersionUID = -932434844601790958L;

	public boolean ResortAtWork = false;

	public Cache GetCacheByGcCode(String GcCode)
	{
		for (int i = 0, n = this.size(); i < n; i++)
		{
			Cache cache = this.get(i);
			if (cache.getGcCode().equalsIgnoreCase(GcCode)) return cache;
		}
		return null;
	}

	public Cache GetCacheById(long cacheId)
	{
		for (int i = 0, n = this.size(); i < n; i++)
		{
			Cache cache = this.get(i);
			if (cache.Id == cacheId) return cache;
		}
		return null;
	}

	/**
	 * @param selectedCoord
	 *            GlobalCore.getSelectedCoord()
	 * @param selected
	 *            new CacheWithWp(GlobalCore.getSelectedCache(),GlobalCore.getSelectedWP())
	 * @param userName
	 *            Config.settings.GcLogin.getValue()
	 * @param ParkingLatitude
	 *            Config.settings.ParkingLatitude.getValue()
	 * @param ParkingLongitude
	 *            Config.settings.ParkingLongitude.getValue()
	 * @param DisplayOff
	 *            Energy.DisplayOff()
	 * @return CacheWithWP [null posible] set To<br>
	 *         GlobalCore.setSelectedWaypoint(nextCache, waypoint, false);<br>
	 *         GlobalCore.NearestCache(nextCache);
	 */
	public CacheWithWP Resort(Coordinate selectedCoord, CacheWithWP selected)
	{

		CacheWithWP retValue = null;

		this.ResortAtWork = true;
		boolean LocatorValid = Locator.Valid();
		// Alle Distanzen aktualisieren
		if (LocatorValid)
		{
			for (int i = 0, n = this.size(); i < n; i++)
			{
				Cache cache = this.get(i);
				cache.Distance(CalculationType.FAST, true);
			}
		}
		else
		{
			// sort after Distance from selected Cache
			Coordinate fromPos = selectedCoord;
			// avoid "illegal waypoint"
			if (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)
			{
				fromPos = selected.getCache().Pos;
			}
			if (fromPos == null)
			{
				this.ResortAtWork = false;
				return retValue;
			}
			for (int i = 0, n = this.size(); i < n; i++)
			{
				Cache cache = this.get(i);
				cache.Distance(CalculationType.FAST, true, fromPos);
			}
		}

		this.sort();

		// N�chsten Cache ausw�hlen
		if (this.size() > 0)
		{
			Cache nextCache = this.get(0); // or null ...
			for (int i = 0; i < this.size(); i++)
			{
				nextCache = this.get(i);
				if (!nextCache.isArchived())
				{
					if (nextCache.isAvailable())
					{
						if (!nextCache.isFound()) // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht) , aber
													// found
													// kann nicht r�ckg�ngig gemacht werden.
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
				// wenn ein Cache keinen Final Waypoint hat dann wird �berpr�ft, ob dieser einen Startpunkt definiert hat
				// Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
				// selektiert
				waypoint = nextCache.GetStartWaypoint();
			}

			retValue = new CacheWithWP(nextCache, waypoint);
		}

		CB_Core.Events.CachListChangedEventList.Call();

		// vorhandenen Parkplatz Cache nach oben schieben
		Cache park = this.GetCacheByGcCode("CBPark");
		if (park != null)
		{
			this.MoveItemFirst(this.indexOf(park));
		}

		// Cursor.Current = Cursors.Default;
		this.ResortAtWork = false;
		return retValue;
	}

	/**
	 * Removes all of the elements from this list. The list will be empty after this call returns.<br>
	 * All Cache objects are disposed
	 */
	@Override
	public void clear()
	{
		for (int i = 0, n = this.size(); i < n; i++)
		{
			Cache cache = this.get(i);
			if (!cache.isLive()) cache.dispose(); // don't dispose LiveCaches
			cache = null;
		}

		super.clear();
	}

	public void dispose()
	{
		clear();
		super.dispose();
	}

	public ArrayList<String> getGcCodes()
	{
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0, n = this.size(); i < n; i++)
		{
			list.add(this.get(i).getGcCode());
		}
		return list;
	}

	@Override
	public int add(Cache ca)
	{
		if (ca == null) return -1;

		int index = -1;
		for (int i = 0, n = this.size(); i < n; i++)
		{

			Cache cache = get(i);
			if (cache.Id == ca.Id)
			{
				index = i;
			}
		}

		if (index > -1)
		{
			// Replace LiveCache with Cache
			if (get(index).isLive())
			{
				if (!ca.isLive())
				{
					this.replace(ca, index);
					return index;
				}
			}

		}

		return super.add(ca);
	}

}
