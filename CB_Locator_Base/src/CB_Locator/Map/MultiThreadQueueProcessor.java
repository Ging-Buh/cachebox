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

import java.util.SortedMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Energy;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Lists.CB_List;

/**
 * @author ging-buh
 * @author Longri
 */
class MultiThreadQueueProcessor extends Thread {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(MultiThreadQueueProcessor.class);
    static int instanceCount = 0;
    static CB_List<Descriptor> inLoadDesc = new CB_List<Descriptor>();
    static final Lock inLoadDescLock = new ReentrantLock();

    final int ThreadId;
    public boolean queueProcessorLifeCycle = false;

    private final QueueData queueData;
    private boolean isAlive;

    MultiThreadQueueProcessor(QueueData queueData, int threadID) {
	//log.debug("Create MultiThreadQueueProcessor[" + threadID + "]");
	ThreadId = threadID;
	this.queueData = queueData;
    }

    @Override
    public void run() {
	try {
	    do {
		this.isAlive = true;
		queueProcessorLifeCycle = !queueProcessorLifeCycle;
		Descriptor desc = null;
		if (!Energy.DisplayOff() /* && MapView.this.isVisible() */
			&& ((queueData.queuedTiles.size() > 0) || (queueData.queuedOverlayTiles.size() > 0))) {

		    try {
			boolean calcOverlay = false;
			queueData.queuedTilesLock.lock();

			if (queueData.CurrentOverlayLayer != null)
			    queueData.queuedOverlayTilesLock.lock();
			try {
			    Descriptor nearestDesc = null;
			    double nearestDist = Double.MAX_VALUE;
			    int nearestZoom = 0;
			    SortedMap<Long, Descriptor> tmpQueuedTiles = queueData.queuedTiles;
			    calcOverlay = false;
			    if (queueData.CurrentOverlayLayer != null && queueData.queuedTiles.size() == 0) {
				tmpQueuedTiles = queueData.queuedOverlayTiles;
				calcOverlay = true; // es wird gerade ein Overlay Tile geladen
			    }

			    for (Descriptor tmpDesc : tmpQueuedTiles.values()) {
				// zugeh�rige MapView aus dem Data vom Descriptor holen
				MapViewBase mapView = null;
				if ((tmpDesc.Data != null) && (tmpDesc.Data instanceof MapViewBase))
				    mapView = (MapViewBase) tmpDesc.Data;
				if (mapView == null)
				    continue;

				long posFactor = MapTileLoader.getMapTilePosFactor(tmpDesc.Zoom);

				double dist = Math.sqrt(Math.pow((double) tmpDesc.X * posFactor * 256 + 128 * posFactor - mapView.screenCenterW.x, 2) + Math.pow((double) tmpDesc.Y * posFactor * 256 + 128 * posFactor + mapView.screenCenterW.y, 2));

				if (Math.abs(mapView.aktZoom - nearestZoom) > Math.abs(mapView.aktZoom - tmpDesc.Zoom)) {
				    // der Zoomfaktor des bisher besten
				    // Tiles ist weiter entfernt vom
				    // aktuellen Zoom als der vom tmpDesc ->
				    // tmpDesc verwenden
				    nearestDist = dist;
				    nearestDesc = tmpDesc;
				    nearestZoom = tmpDesc.Zoom;
				}

				if (dist < nearestDist) {
				    if (Math.abs(mapView.aktZoom - nearestZoom) < Math.abs(mapView.aktZoom - tmpDesc.Zoom)) {
					// zuerst die Tiles, die dem
					// aktuellen Zoom Faktor am n�chsten
					// sind.
					continue;
				    }
				    nearestDist = dist;
				    nearestDesc = tmpDesc;
				    nearestZoom = tmpDesc.Zoom;
				}
			    }
			    desc = nearestDesc;

			} finally {
			    queueData.queuedTilesLock.unlock();
			    if (queueData.CurrentOverlayLayer != null)
				queueData.queuedOverlayTilesLock.unlock();
			}

			if (desc != null) {
			    inLoadDescLock.lock();
			    if (inLoadDesc.contains(desc)) {
				continue;// Other thread is loading this Desc. Skip!
			    }
			    inLoadDescLock.unlock();

			    if (calcOverlay && queueData.CurrentOverlayLayer != null)
				LoadOverlayTile(desc);
			    else if (queueData.CurrentLayer != null) {
				inLoadDescLock.lock();
				if (inLoadDesc.contains(desc)) {
				    inLoadDescLock.unlock();
				    continue;// Other thread is loading this Desc. Skip!
				}
				inLoadDesc.add(desc);
				inLoadDescLock.unlock();
			    }

			    // log.debug("LoadTile on[" + ThreadId + "]");
			    LoadTile(desc);
			    // log.debug("finish LoadTile on[" + ThreadId + "]");
			    inLoadDescLock.lock();
			    inLoadDesc.remove(desc);
			    inLoadDescLock.unlock();

			} else {
			    // nothing to do, so we can sleep
			    Thread.sleep(100);
			}
		    } catch (Exception ex1) {
			log.error("MapViewGL.queueProcessor.doInBackground()", "1", ex1);
			Thread.sleep(200);
		    }

		} else {
		    Thread.sleep(50);
		}
		this.isAlive = true;
	    } while (true);
	} catch (Exception ex3) {
	    log.error("MapViewGL.queueProcessor.doInBackground()", "3", ex3);

	    try {
		Thread.sleep(200);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }

	    this.isAlive = false;
	} finally {
	    // damit im Falle einer Exception der Thread neu gestartet wird
	    // queueProcessor = null;
	}
	return;
    }

    public boolean Alive() {
	return this.isAlive;
    }

    // #######################################################################
    // private

    private void LoadTile(Descriptor desc) {

	TileGL tile = null;
	if (ManagerBase.Manager != null) {
	    tile = ManagerBase.Manager.LoadLocalPixmap(queueData.CurrentLayer, desc, ThreadId);
	}

	if (tile != null) {
	    addLoadedTile(desc, tile);
	    // Redraw Map after a new Tile was loaded or generated
	    GL.that.renderOnce();
	} else {
	    if (ManagerBase.Manager.CacheTile(queueData.CurrentLayer, desc)) {
		tile = ManagerBase.Manager.LoadLocalPixmap(queueData.CurrentLayer, desc, ThreadId);
		addLoadedTile(desc, tile);
		// Redraw Map after a new Tile was loaded or generated
		GL.that.renderOnce();
	    }
	    // to avoid endless trys
	    if (tile != null)
		RemoveFromQueuedTiles(desc);
	}

    }

    private void LoadOverlayTile(Descriptor desc) {
	if (queueData.CurrentOverlayLayer == null)
	    return;

	TileGL tile = null;
	if (ManagerBase.Manager != null) {
	    // Load Overlay never inverted !!!
	    tile = ManagerBase.Manager.LoadLocalPixmap(queueData.CurrentOverlayLayer, desc, ThreadId);
	}

	if (tile != null) {
	    addLoadedOverlayTile(desc, tile);
	    // Redraw Map after a new Tile was loaded or generated
	    GL.that.renderOnce();
	} else {
	    ManagerBase.Manager.CacheTile(queueData.CurrentOverlayLayer, desc);
	    // to avoid endless trys
	    RemoveFromQueuedTiles(desc);
	}
    }

    private void RemoveFromQueuedTiles(Descriptor desc) {
	queueData.queuedTilesLock.lock();
	try {
	    if (queueData.queuedTiles.containsKey(desc.GetHashCode())) {
		queueData.queuedTiles.remove(desc.GetHashCode());
	    }
	} finally {
	    queueData.queuedTilesLock.unlock();
	}
    }

    private void addLoadedTile(Descriptor desc, TileGL tile) {
	queueData.loadedTilesLock.lock();
	try {
	    if (queueData.loadedTiles.containsKey(desc.GetHashCode())) {
		tile.dispose(); // das war dann umsonnst
	    } else {
		queueData.loadedTiles.add(desc.GetHashCode(), tile);
	    }

	} finally {
	    queueData.loadedTilesLock.unlock();
	}

	queueData.queuedTilesLock.lock();
	try {
	    if (queueData.queuedTiles.containsKey(desc.GetHashCode())) {
		queueData.queuedTiles.remove(desc.GetHashCode());
	    }
	} finally {
	    queueData.queuedTilesLock.unlock();
	}

    }

    private void addLoadedOverlayTile(Descriptor desc, TileGL tile) {
	queueData.loadedOverlayTilesLock.lock();
	try {
	    if (queueData.loadedOverlayTiles.containsKey(desc.GetHashCode())) {

	    } else {
		queueData.loadedOverlayTiles.add(desc.GetHashCode(), tile);
	    }

	} finally {
	    queueData.loadedOverlayTilesLock.unlock();
	}

	queueData.queuedOverlayTilesLock.lock();
	try {
	    if (queueData.queuedOverlayTiles.containsKey(desc.GetHashCode())) {
		queueData.queuedOverlayTiles.remove(desc.GetHashCode());
	    }
	} finally {
	    queueData.queuedOverlayTilesLock.unlock();
	}

    }

}
