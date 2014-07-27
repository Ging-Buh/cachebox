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

package CB_Core.Api;

import java.util.ArrayList;

import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Map.Descriptor;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Logger;

/**
 * @author Longri
 */
public class LiveMapQue implements PositionChangedEvent
{
	public static final byte DEFAULT_ZOOM = 14;
	public static final int MAX_REQUEST_CACHE_COUNT = 50;
	public static final int MAX_REQUEST_CACHE_RADIUS = 1062; // Descriptor Zoom 14 are 1500m * 1500m
	private static final ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
	private static final ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

	public static final CB_List<Cache> LiveCaches = new CB_List<Cache>();
	public static boolean DownloadIsActive = false;

	static CB_List<Descriptor> quedDescList = new CB_List<Descriptor>();

	public static CB_List<QueStateChanged> eventList = new CB_List<LiveMapQue.QueStateChanged>();

	public void AddStateChangedListner(QueStateChanged listner)
	{
		eventList.add(listner);
	}

	public interface QueStateChanged
	{
		public void stateChanged();
	}

	@Override
	public void PositionChanged()
	{
		Coordinate local = Locator.getCoordinate(ProviderType.any);
		quePosition(local);
	}

	@Override
	public void OrientationChanged()
	{
		// Nothing to do;

	}

	@Override
	public void SpeedChanged()
	{
		// Nothing to do;
	}

	@Override
	public String getReceiverName()
	{
		return "LiveMapQue";
	}

	@Override
	public Priority getPriority()
	{
		return Priority.Low;
	}

	static public boolean quePosition(Coordinate coord)
	{
		// no request for invalid Coords
		if (coord == null || !coord.isValid()) return false;

		final Descriptor desc = new Descriptor(coord, DEFAULT_ZOOM);

		if (quedDescList.contains(desc)) return false; // all ready for this descriptor
		quedDescList.add(desc);
		// Add request Time Limits (Stapel verarbeitung!?)

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				for (int i = 0; i < eventList.size(); i++)
					eventList.get(i).stateChanged();
				DownloadIsActive = true;
				Coordinate requestCoordinate = desc.getCenterCoordinate();
				SearchLiveMap requestSearch = new SearchLiveMap(MAX_REQUEST_CACHE_COUNT, requestCoordinate, MAX_REQUEST_CACHE_RADIUS);

				ArrayList<Cache> apiCaches = new ArrayList<Cache>();

				CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
				t.SearchForGeocachesJSON(requestSearch, apiCaches, apiLogs, apiImages, 0);

				for (Cache ca : apiCaches)
				{
					if (LiveCaches.contains(ca))
					{
						Logger.DEBUG("Live Map:Cache Doppelt geladen => " + ca.toString());
					}
					else
					{
						ca.setLive(true);
						LiveCaches.add(ca);
					}
				}

				CachListChangedEventList.Call();
				DownloadIsActive = false;
				for (int i = 0; i < eventList.size(); i++)
					eventList.get(i).stateChanged();
			}
		});

		thread.start();
		return true;
	}
}
