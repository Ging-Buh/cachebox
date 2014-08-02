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
import CB_Utils.Util.iChanged;

/**
 * @author Longri
 */
public class LiveMapQue implements PositionChangedEvent
{
	public static final byte DEFAULT_ZOOM_14 = 14;
	public static final int MAX_REQUEST_CACHE_RADIUS_14 = 1060;

	public static final byte DEFAULT_ZOOM_13 = 13;
	public static final int MAX_REQUEST_CACHE_RADIUS_13 = 2120;

	public static byte Used_Zoom;
	public static int Used_max_request_radius;

	public enum Live_Radius
	{
		Zoom_13, Zoom_14
	}

	public static Live_Radius radius = CB_Core.Settings.CB_Core_Settings.Live_Radius.getEnumValue();

	static
	{
		CB_Core.Settings.CB_Core_Settings.Live_Radius.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				radius = CB_Core.Settings.CB_Core_Settings.Live_Radius.getEnumValue();

				switch (radius)
				{
				case Zoom_13:
					Used_Zoom = DEFAULT_ZOOM_13;
					Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_13;
					break;
				case Zoom_14:
					Used_Zoom = DEFAULT_ZOOM_14;
					Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
					break;
				default:
					Used_Zoom = DEFAULT_ZOOM_14;
					Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
					break;

				}
			}

		});

		radius = CB_Core.Settings.CB_Core_Settings.Live_Radius.getEnumValue();

		switch (radius)
		{
		case Zoom_13:
			Used_Zoom = DEFAULT_ZOOM_13;
			Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_13;
			break;
		case Zoom_14:
			Used_Zoom = DEFAULT_ZOOM_14;
			Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
			break;
		default:
			Used_Zoom = DEFAULT_ZOOM_14;
			Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
			break;

		}
	}

	public static final int MAX_REQUEST_CACHE_COUNT = 200;
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

		// no request if disabled
		if (CB_Core.Settings.CB_Core_Settings.DisableLiveMap.getValue()) return false;

		final Descriptor desc = new Descriptor(coord, Used_Zoom);

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
				SearchLiveMap requestSearch = new SearchLiveMap(MAX_REQUEST_CACHE_COUNT, requestCoordinate, Used_max_request_radius);

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
