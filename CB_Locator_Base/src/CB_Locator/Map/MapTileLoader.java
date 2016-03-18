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

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.OrthographicCamera;

import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;

/**
 * @author ging-buh
 * @author Longri
 */
public class MapTileLoader {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(MapTileLoader.class);
	public static final int MAX_MAP_ZOOM = 22;
	private final QueueData queueData = new QueueData();
	private MultiThreadQueueProcessor[] queueProcessor = null;
	private Thread[] queueProcessorAliveCheck = null;
	CB_List<Long> neadedTiles = new CB_List<Long>();
	private int maxNumTiles = 0;
	private boolean ThreadPrioSetted = false;
	private boolean CombleadInitial = false;
	private long lastLoadHash = 0;

	public MapTileLoader() {
		super();

		if (queueProcessor == null) {

			queueProcessor = new MultiThreadQueueProcessor[ManagerBase.PROCESSOR_COUNT];
			queueProcessorAliveCheck = new Thread[ManagerBase.PROCESSOR_COUNT];

			initial(Thread.NORM_PRIORITY); // first initial one thread(MultiThreadQueueProcessor)

		}
	}

	int InitialCount = 0;

	private void initial(int ThreadPriority) {
		if (InitialCount < ManagerBase.PROCESSOR_COUNT) {
			queueProcessor[InitialCount] = new MultiThreadQueueProcessor(queueData, InitialCount);
			queueProcessor[InitialCount].setPriority(ThreadPriority);
			queueProcessor[InitialCount].start();

			startAliveCheck(InitialCount);

			InitialCount++;
		}
	}

	private void startAliveCheck(final int index) {

		queueProcessorAliveCheck[index] = new Thread(new Runnable() {
			@Override
			public void run() {
				do {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (!queueProcessor[index].Alive()) {
						Log.debug(log, "MapTileLoader Restart queueProcessor[" + index + "]");
						queueProcessor[index] = new MultiThreadQueueProcessor(queueData, index);
						queueProcessor[index].setPriority(Thread.MIN_PRIORITY);
						queueProcessor[index].start();
					}
				} while (true);
			}
		});

		queueProcessorAliveCheck[index].setPriority(Thread.MIN_PRIORITY);
		queueProcessorAliveCheck[index].start();
	}

	public int QueuedTilesSize() {
		return queueData.queuedTiles.size();
	}

	public int LoadedTilesSize() {
		return queueData.loadedTiles.size();
	}

	public void loadTiles(MapViewBase mapView, Descriptor lo, Descriptor ru, int aktZoom) {

		long hash = lo.GetHashCode() * ru.GetHashCode();
		if (lastLoadHash == hash)
			return; // we have loaded!
		lastLoadHash = hash;

		// Initial Threads?
		if (!CombleadInitial) {
			if (InitialCount < ManagerBase.PROCESSOR_COUNT && InitialCount > 0 && queueData.loadedTiles.size() > 1) {
				initial(Thread.NORM_PRIORITY);
			} else if (InitialCount >= ManagerBase.PROCESSOR_COUNT && !ThreadPrioSetted) {
				for (int i = 0; i < ManagerBase.PROCESSOR_COUNT; i++) {
					queueProcessor[i].setPriority(Thread.MIN_PRIORITY);
					ThreadPrioSetted = true;
					CombleadInitial = true;
				}
			}
		}

		// {// DEBUG
		//
		// String tre = String.valueOf(((queueProcessor == null) ? 0 : queueProcessor.length));
		// String text = "Threads:" + tre + " | MaxCache:" + maxNumTiles + " " + " loaded:" + queueData.loadedTiles.size() + " life:"
		// + TileGL_Bmp.LifeCount;
		// GL.MaptileLoaderDebugString = text;
		// }

		if (ManagerBase.Manager == null)
			return; // Kann nichts laden, wenn der Manager Null ist!

		// alle notwendigen Tiles zum Laden einstellen in die Queue

		queueData.loadedTilesLock.lock();
		queueData.queuedTilesLock.lock();
		if (queueData.CurrentOverlayLayer != null) {
			queueData.loadedOverlayTilesLock.lock();
			queueData.queuedOverlayTilesLock.lock();
		}
		// Queue jedesmal l�schen, damit die Tiles, die eigentlich
		// mal
		// gebraucht wurden aber trotzdem noch nicht geladen sind
		// auch nicht mehr geladen werden
		// dabei aber die MapView ber�cksichtigen, die die queuedTiles angefordert hat
		// queuedTiles.clear();
		ArrayList<Descriptor> toDelete = new ArrayList<Descriptor>();
		for (Descriptor desc : queueData.queuedTiles.values()) {
			if (desc.Data == mapView) {
				toDelete.add(desc);
			}
		}
		for (Descriptor desc : toDelete) {
			queueData.queuedTiles.remove(desc.GetHashCode());
		}
		if (queueData.CurrentOverlayLayer != null) {
			toDelete.clear();
			for (Descriptor desc : queueData.queuedOverlayTiles.values()) {
				if (desc.Data == mapView) {
					toDelete.add(desc);
				}
			}
			for (Descriptor desc : toDelete) {
				queueData.queuedOverlayTiles.remove(desc.GetHashCode());
			}

		}

		CB_List<Descriptor> trueZommDescList = new CB_List<Descriptor>();
		// // CB_List<Descriptor> biggerZommDescList = new CB_List<Descriptor>();
		//
		for (int i = lo.getX(); i <= ru.getX(); i++) {
			for (int j = lo.getY(); j <= ru.getY(); j++) {
				Descriptor desc = new Descriptor(i, j, aktZoom, lo.NightMode);

				// speichern, zu welche MapView diese Descriptor angefordert hat
				desc.Data = mapView;

				trueZommDescList.add(desc);
				neadedTiles.add(desc.GetHashCode());

			}
		}

		// then true zoom level
		for (int i = 0, n = trueZommDescList.size(); i < n; i++) {
			Descriptor desc = trueZommDescList.get(i);
			if (!queueData.loadedTiles.containsKey(desc.GetHashCode())) {
				if (!queueData.queuedTiles.containsKey(desc.GetHashCode())) {
					queueTile(desc, queueData.queuedTiles, queueData.queuedTilesLock);
				}
			} else if (queueData.queuedTiles.containsKey(desc.GetHashCode())) {
				queueData.queuedTiles.remove(desc.GetHashCode());
			}

			if (queueData.CurrentOverlayLayer != null) {
				if (queueData.loadedOverlayTiles.containsKey(desc.GetHashCode())) {
					continue;
				}
				if (queueData.queuedOverlayTiles.containsKey(desc.GetHashCode()))
					continue;
				queueTile(desc, queueData.queuedOverlayTiles, queueData.queuedOverlayTilesLock);
			}
		}

		try {
			queueData.queuedTilesLock.unlock();
			queueData.loadedTilesLock.unlock();
			if (queueData.CurrentOverlayLayer != null) {
				queueData.queuedOverlayTilesLock.unlock();
				queueData.loadedOverlayTilesLock.unlock();
			}
		} catch (Exception e) {
		}
		neadedTiles.truncate(0);
	}

	int numLoadedTiles() {
		return queueData.loadedTiles.size();
	}

	public void setMaxNumTiles(int maxNumTiles2) {
		if (maxNumTiles2 > maxNumTiles)
			maxNumTiles = maxNumTiles2;
		queueData.setLoadedTilesCacheCapacity(maxNumTiles);
	}

	private void queueTile(Descriptor desc, SortedMap<Long, Descriptor> queuedTiles, Lock queuedTilesLock) {
		queuedTilesLock.lock();
		try {
			if (queuedTiles.containsKey(desc.GetHashCode()))
				return;

			queuedTiles.put(desc.GetHashCode(), desc);
		} finally {
			queuedTilesLock.unlock();
		}

	}

	public void clearLoadedTiles() {
		lastLoadHash = 0;
		queueData.loadedTilesLock.lock();
		queueData.loadedTiles.clear();
		queueData.loadedTilesLock.unlock();

		if (queueData.CurrentOverlayLayer != null) {
			queueData.loadedOverlayTilesLock.lock();
			queueData.loadedOverlayTiles.clear();
			queueData.loadedOverlayTilesLock.unlock();

		}
	}

	public void increaseLoadedTilesAge() {
		// das Alter aller Tiles um 1 erh�hen
		queueData.loadedTiles.increaseLoadedTilesAge();

		if (queueData.CurrentOverlayLayer != null) {
			queueData.loadedOverlayTiles.increaseLoadedTilesAge();
		}
	}

	public TileGL getLoadedTile(Descriptor desc) {
		return queueData.loadedTiles.get(desc.GetHashCode());
	}

	public boolean markToDraw(Descriptor desc) {
		return queueData.loadedTiles.markToDraw(desc.GetHashCode());
	}

	public int getDrawingSize() {
		return queueData.loadedTiles.DrawingSize();
	}

	public TileGL getDrawingTile(int i) {
		return queueData.loadedTiles.getDrawingTile(i);
	}

	public void clearDrawingTiles() {
		queueData.loadedTiles.clearDrawingList();
	}

	public void sort() {
		queueData.loadedTiles.sort();
		if (queueData.CurrentOverlayLayer != null) {
			queueData.loadedOverlayTiles.sort();
		}
	}

	public TileGL getLoadedOverlayTile(Descriptor desc) {
		// Overlay Tiles liefern
		if (queueData.CurrentOverlayLayer == null) {
			return null;
		}
		return queueData.loadedOverlayTiles.get(desc.GetHashCode());
	}

	public boolean markToDrawOverlay(Descriptor desc) {
		return queueData.loadedOverlayTiles.markToDraw(desc.GetHashCode());
	}

	public int getDrawingSizeOverlay() {
		return queueData.loadedOverlayTiles.DrawingSize();
	}

	public TileGL getDrawingTileOverlay(int i) {
		return queueData.loadedOverlayTiles.getDrawingTile(i);
	}

	public void clearDrawingTilesOverlay() {
		queueData.loadedOverlayTiles.clearDrawingList();
	}

	public void setLayer(Layer layer) {
		queueData.CurrentLayer = layer;
	}

	public void setOverlayLayer(Layer layer) {
		queueData.CurrentOverlayLayer = layer;
	}

	public Layer getCurrentLayer() {
		return queueData.CurrentLayer;
	}

	public Layer getCurrentOverlayLayer() {
		return queueData.CurrentOverlayLayer;
	}

	public int getCacheSize() {
		return queueData.loadedTiles.getCapacity();
	}

	// #######################################################################################################
	// Static

	public static float convertCameraZommToFloat(OrthographicCamera cam) {
		if (cam == null || cam.zoom <= 0)
			return 0f;

		float result = 0.0f;
		result = MAX_MAP_ZOOM - (float) (Math.log(cam.zoom) / Math.log(2.0));
		return result;
	}

	public static long getMapTilePosFactor(float zoom) {
		long result = 1;
		result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
		return result;
	}

	public void reloadTile(MapViewBase mapViewBase, Descriptor desc, int aktZoom) {
		// queue only if no Tile on Work
		if (queueData.queuedTiles.size() != 0)
			return;

		queueData.loadedTilesLock.lock();
		queueData.queuedTilesLock.lock();

		if (!queueData.loadedTiles.containsKey(desc.GetHashCode())) {
			if (!queueData.queuedTiles.containsKey(desc.GetHashCode())) {
				queueTile(desc, queueData.queuedTiles, queueData.queuedTilesLock);
			}
		}
		queueData.loadedTilesLock.unlock();
		queueData.queuedTilesLock.unlock();
	}

}
