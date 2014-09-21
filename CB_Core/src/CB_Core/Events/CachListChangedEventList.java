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
package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.CoreSettingsForward;
import CB_Core.FilterProperties;
import CB_Core.Api.LiveMapQue;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Cache;

/**
 * @author Longri
 */
public class CachListChangedEventList
{
	public static ArrayList<CacheListChangedEventListner> list = new ArrayList<CacheListChangedEventListner>();

	public static void Add(CacheListChangedEventListner event)
	{
		synchronized (list)
		{
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(CacheListChangedEventListner event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	private static Thread threadCall;

	public static void Call()
	{
		if (CoreSettingsForward.DisplayOff) return;

		synchronized (Database.Data.Query)
		{
			Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");

			if (cache != null) Database.Data.Query.remove(cache);

			// add Parking Cache
			if (CB_Core_Settings.ParkingLatitude.getValue() != 0)
			{
				cache = new Cache(CB_Core_Settings.ParkingLatitude.getValue(), CB_Core_Settings.ParkingLongitude.getValue(), "My Parking area", CacheTypes.MyParking, "CBPark");
				Database.Data.Query.add(0, cache);
			}

			// add all Live Caches
			for (int i = 0; i < LiveMapQue.LiveCaches.getSize(); i++)
			{
				if (FilterProperties.isFilterSet())
				{
					Cache ca = LiveMapQue.LiveCaches.get(i);
					if (!Database.Data.Query.contains(ca))
					{
						if (ca.correspondToFilter(FilterProperties.LastFilter)) continue;
						ca.setLive(true);
						Database.Data.Query.add(ca);
					}
				}
				else
				{
					Cache ca = LiveMapQue.LiveCaches.get(i);
					if (!Database.Data.Query.contains(ca))
					{
						ca.setLive(true);
						Database.Data.Query.add(ca);
					}
				}

			}
		}

		if (threadCall != null)
		{
			if (threadCall.getState() != Thread.State.TERMINATED) return;
			else
				threadCall = null;
		}

		if (threadCall == null) threadCall = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					for (CacheListChangedEventListner event : list)
					{
						if (event == null) continue;
						event.CacheListChangedEvent();
					}
				}

			}
		});

		threadCall.start();
	}

}
