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
package CB_Locator.Map;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.concurrent.locks.Lock;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Logger;

import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * @author ging-buh
 * @author Longri
 */
public class MapTileLoader
{
	public static final int MAX_MAP_ZOOM = 22;
	private final QueueData queueData = new QueueData();
	private Thread[] queueProcessor = null;
	private Thread[] queueProcessorAliveCheck = null;
	CB_List<Long> neadedTiles = new CB_List<Long>();
	private int maxNumTiles = 0;
	private boolean doubleCache;
	private int doubleCacheCount = 0;

	public MapTileLoader()
	{
		super();

		int processors = Runtime.getRuntime().availableProcessors();

		if (queueProcessor == null)
		{

			queueProcessor = new Thread[processors];
			queueProcessorAliveCheck = new Thread[processors];

			for (int i = 0; i < processors; i++)
			{
				queueProcessor[i] = new MultiThreadQueueProcessor(queueData);
				queueProcessor[i].setPriority(Thread.MIN_PRIORITY);
				queueProcessor[i].start();

				startAliveCheck(i);
			}

		}
	}

	private void startAliveCheck(final int index)
	{

		queueProcessorAliveCheck[index] = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				do
				{

					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					if (!queueProcessor[index].isAlive())
					{
						Logger.DEBUG("MapTileLoader Restart queueProcessor");
						queueProcessor[index] = new MultiThreadQueueProcessor(queueData);
						queueProcessor[index].setPriority(Thread.MIN_PRIORITY);
						queueProcessor[index].start();
					}
				}
				while (true);
			}
		});

		queueProcessorAliveCheck[index].setPriority(Thread.MIN_PRIORITY);
		queueProcessorAliveCheck[index].start();
	}

	public int QueuedTilesSize()
	{
		return queueData.queuedTiles.size();
	}

	public int LoadedTilesSize()
	{
		return queueData.loadedTiles.size();
	}

	public void loadTiles(MapViewBase mapView, Descriptor lo, Descriptor ru, int aktZoom)
	{

		{// DEBUG

			String tre = String.valueOf(((queueProcessor == null) ? 0 : queueProcessor.length));
			String text = "Threads:" + tre + " | MaxCache:" + maxNumTiles + " " + (doubleCache ? "D" : "") + " loaded:"
					+ queueData.loadedTiles.size() + " life:" + TileGL_Bmp.LifeCount;
			GL.MaptileLoaderDebugString = text;
		}

		if (ManagerBase.Manager == null) return; // Kann nichts laden, wenn der Manager Null ist!

		deleteUnusedTiles(mapView, queueData.loadedTiles, queueData.loadedTilesLock);
		if (queueData.CurrentOverlayLayer != null)
		{
			deleteUnusedTiles(mapView, queueData.loadedOverlayTiles, queueData.loadedOverlayTilesLock);
		}
		// alle notwendigen Tiles zum Laden einstellen in die Queue

		queueData.loadedTilesLock.lock();
		queueData.queuedTilesLock.lock();
		if (queueData.CurrentOverlayLayer != null)
		{
			queueData.loadedOverlayTilesLock.lock();
			queueData.queuedOverlayTilesLock.lock();
		}
		// Queue jedesmal löschen, damit die Tiles, die eigentlich
		// mal
		// gebraucht wurden aber trotzdem noch nicht geladen sind
		// auch nicht mehr geladen werden
		// dabei aber die MapView berücksichtigen, die die queuedTiles angefordert hat
		// queuedTiles.clear();
		ArrayList<Descriptor> toDelete = new ArrayList<Descriptor>();
		for (Descriptor desc : queueData.queuedTiles.values())
		{
			if (desc.Data == mapView)
			{
				toDelete.add(desc);
			}
		}
		for (Descriptor desc : toDelete)
		{
			queueData.queuedTiles.remove(desc.GetHashCode());
		}
		if (queueData.CurrentOverlayLayer != null)
		{
			toDelete.clear();
			for (Descriptor desc : queueData.queuedOverlayTiles.values())
			{
				if (desc.Data == mapView)
				{
					toDelete.add(desc);
				}
			}
			for (Descriptor desc : toDelete)
			{
				queueData.queuedOverlayTiles.remove(desc.GetHashCode());
			}

		}

		try
		{

			for (int i = lo.X; i <= ru.X; i++)
			{
				for (int j = lo.Y; j <= ru.Y; j++)
				{
					Descriptor desc = new Descriptor(i, j, aktZoom, lo.NightMode);
					// speichern, zu welche MapView diesen Descriptor angefordert hat
					desc.Data = mapView;

					neadedTiles.add(desc.GetHashCode());

					try
					{
						if (!queueData.loadedTiles.containsKey(desc.GetHashCode()))
						{
							if (!queueData.queuedTiles.containsKey(desc.GetHashCode()))
							{
								queueTile(desc, queueData.queuedTiles, queueData.queuedTilesLock);
							}
						}
						if (queueData.CurrentOverlayLayer != null)
						{
							if (queueData.loadedOverlayTiles.containsKey(desc.GetHashCode()))
							{
								continue;
							}
							if (queueData.queuedOverlayTiles.containsKey(desc.GetHashCode())) continue;
							queueTile(desc, queueData.queuedOverlayTiles, queueData.queuedOverlayTilesLock);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		finally
		{

		}

		if (!doubleCache && LoadedTilesSize() > neadedTiles.size() * 1.5)
		{

			CB_List<Long> delList = queueData.loadedTiles.allKeysAreNot(neadedTiles);

			for (long hash : delList)
			{
				queueData.loadedTiles.get(hash).dispose();
				queueData.loadedTiles.remove(hash);
			}

			if (queueData.CurrentOverlayLayer != null)
			{
				for (long hash : delList)
				{
					queueData.loadedOverlayTiles.get(hash).dispose();
					queueData.loadedOverlayTiles.remove(hash);
				}
			}
		}

		queueData.queuedTilesLock.unlock();
		queueData.loadedTilesLock.unlock();
		if (queueData.CurrentOverlayLayer != null)
		{
			queueData.queuedOverlayTilesLock.unlock();
			queueData.loadedOverlayTilesLock.unlock();
		}
		neadedTiles.truncate(0);
	}

	/**
	 * Double the value for maxNumTiles, for 50 render call's
	 */
	public void doubleCache()
	{
		doubleCache = true;
		doubleCacheCount = 50;
	}

	private void deleteUnusedTiles(MapViewBase mapView, LoadetSortedTiles loadedTiles, Lock loadedTilesLock)
	{
		// Ist Auslagerung überhaupt nötig?
		int doubleCacheValue = doubleCache ? maxNumTiles + maxNumTiles : maxNumTiles;

		if (doubleCache)
		{
			doubleCacheCount--;
			if (doubleCacheCount <= 0) doubleCache = false;
		}

		if (numLoadedTiles() <= doubleCacheValue) return;

		loadedTilesLock.lock();
		loadedTiles.removeDestroyedTiles();
		loadedTiles.sort();
		loadedTiles.removeAndDestroy(doubleCacheValue);
		loadedTilesLock.unlock();

	}

	int numLoadedTiles()
	{
		return queueData.loadedTiles.size();
	}

	public void setMaxNumTiles(int maxNumTiles2)
	{
		if (maxNumTiles2 > maxNumTiles) maxNumTiles = maxNumTiles2;
	}

	private void queueTile(Descriptor desc, SortedMap<Long, Descriptor> queuedTiles, Lock queuedTilesLock)
	{
		queuedTilesLock.lock();
		try
		{
			if (queuedTiles.containsKey(desc.GetHashCode())) return;

			queuedTiles.put(desc.GetHashCode(), desc);
		}
		finally
		{
			queuedTilesLock.unlock();
		}

	}

	public void clearLoadedTiles()
	{
		queueData.loadedTilesLock.lock();
		queueData.loadedTiles.clear();
		queueData.loadedTilesLock.unlock();

		if (queueData.CurrentOverlayLayer != null)
		{
			queueData.loadedOverlayTilesLock.lock();
			queueData.loadedOverlayTiles.clear();
			queueData.loadedOverlayTilesLock.unlock();

		}
	}

	public void increaseLoadedTilesAge()
	{
		// das Alter aller Tiles um 1 erhöhen
		for (TileGL tile : queueData.loadedTiles.getValues())
		{
			tile.Age++;
		}
		if (queueData.CurrentOverlayLayer != null)
		{
			for (TileGL tile : queueData.loadedOverlayTiles.getValues())
			{
				tile.Age++;
			}
		}
	}

	public TileGL getLoadedTile(Descriptor desc)
	{
		return queueData.loadedTiles.get(desc.GetHashCode());
	}

	public TileGL getLoadedOverlayTile(Descriptor desc)
	{
		// Overlay Tiles liefern
		if (queueData.CurrentOverlayLayer == null)
		{
			return null;
		}
		return queueData.loadedOverlayTiles.get(desc.GetHashCode());
	}

	// #######################################################################################################
	// Static

	public static float convertCameraZommToFloat(OrthographicCamera cam)
	{
		if (cam.zoom <= 0) return 0f;

		float result = 0.0f;
		result = MAX_MAP_ZOOM - (float) (Math.log(cam.zoom) / Math.log(2.0));
		return result;
	}

	public static long getMapTilePosFactor(float zoom)
	{
		long result = 1;
		result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
		return result;
	}

	public void setLayer(Layer layer)
	{
		queueData.CurrentLayer = layer;
	}

	public void setOverlayLayer(Layer layer)
	{
		queueData.CurrentOverlayLayer = layer;
	}

	public Layer getCurrentLayer()
	{
		return queueData.CurrentLayer;
	}

	public Layer getCurrentOverlayLayer()
	{
		return queueData.CurrentOverlayLayer;
	}
}
