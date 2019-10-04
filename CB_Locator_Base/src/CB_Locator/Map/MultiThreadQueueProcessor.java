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

import CB_UI_Base.Energy;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;

import java.util.SortedMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ging-buh
 * @author Longri
 */
class MultiThreadQueueProcessor extends Thread {
    private static final Lock inLoadDescLock = new ReentrantLock();
    private static final CB_List<Descriptor> inLoadDesc = new CB_List<>();
    private final int threadId;
    private final QueueData queueData;
    private String log = "MapTileQueueThread";

    MultiThreadQueueProcessor(QueueData queueData, int threadID) {
        log = log + "[" + threadID + "]";
        this.threadId = threadID;
        this.queueData = queueData;
    }

    @Override
    public void run() {
        try {
            do {
                Descriptor descriptor = null;
                if (!Energy.DisplayOff() && ((queueData.queuedTiles.size() > 0) || (queueData.queuedOverlayTiles.size() > 0))) {
                    try {
                        boolean calcOverlay = false;
                        queueData.queuedTilesLock.lock();
                        if (queueData.currentOverlayLayer != null) {
                            if (queueData.queuedTiles.size() == 0)
                                calcOverlay = true;
                            queueData.queuedOverlayTilesLock.lock();
                        }
                        try {
                            descriptor = calcNextAndRemove((queueData.queuedTiles.size() > 0) ? queueData.queuedTiles : queueData.queuedOverlayTiles);
                        } finally {
                            queueData.queuedTilesLock.unlock();
                            if (queueData.currentOverlayLayer != null)
                                queueData.queuedOverlayTilesLock.unlock();
                        }

                        if (descriptor != null) {
                            inLoadDescLock.lock();
                            if (inLoadDesc.contains(descriptor)) {
                                // Other thread is loading this Desc. Skip!
                                continue;
                            }
                            inLoadDescLock.unlock();

                            if (calcOverlay && queueData.currentOverlayLayer != null)
                                loadOverlayTile(descriptor);
                            else if (queueData.currentLayer != null) {
                                inLoadDescLock.lock();
                                if (inLoadDesc.contains(descriptor)) {
                                    inLoadDescLock.unlock();
                                    continue;// Other thread is loading this Desc. Skip!
                                }
                                inLoadDesc.add(descriptor);
                                inLoadDescLock.unlock();
                            }

                            // long startTime = System.currentTimeMillis();
                            loadTile(descriptor);
                            // long lasts = (System.currentTimeMillis() - startTime);

                            inLoadDescLock.lock();
                            inLoadDesc.remove(descriptor);
                            inLoadDescLock.unlock();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {
                            }
                        } else {
                            // nothing to do, so we can sleep
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    } catch (Exception ex1) {
                        Log.err(log, "getting Descriptor: " + descriptor + " : " + ex1.toString());
                    }
                } else {
                    try {
                        do {
                            Thread.sleep(100000);
                        }
                        while (Energy.DisplayOff() || ((queueData.queuedTiles.size() <= 0) && (queueData.queuedOverlayTiles.size() <= 0)));
                    } catch (InterruptedException i) {
                        Log.info(log, "returned from sleeping");
                    }
                }
            } while (true);
        } catch (Exception ex3) {
            Log.err(log, "try over all " + ex3.toString(), ex3);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        } finally {
            // damit im Falle einer Exception der Thread neu gestartet wird
            // queueProcessor = null;
        }
    }

    private Descriptor calcNextAndRemove(SortedMap<Long, Descriptor> tmpQueuedTiles) {
        if (tmpQueuedTiles == null) return null;
        Descriptor nearestDesc = null;
        double nearestDist = Double.MAX_VALUE;
        int nearestZoom = 0;
        for (Descriptor tmpDesc : tmpQueuedTiles.values()) {
            MapViewBase mapView = null;
            if ((tmpDesc.Data != null) && (tmpDesc.Data instanceof MapViewBase))
                mapView = (MapViewBase) tmpDesc.Data;
            if (mapView == null)
                continue;

            long posFactor = mapView.getMapTilePosFactor(tmpDesc.zoom);

            double dist = Math.sqrt(Math.pow((double) tmpDesc.X * posFactor * 256 + 128 * posFactor - mapView.screenCenterW.getX(), 2) + Math.pow((double) tmpDesc.Y * posFactor * 256 + 128 * posFactor + mapView.screenCenterW.getY(), 2));

            if (Math.abs(mapView.aktZoom - nearestZoom) > Math.abs(mapView.aktZoom - tmpDesc.zoom)) {
                // der Zoomfaktor des bisher besten Tiles ist weiter entfernt vom aktuellen Zoom als der vom tmpDesc -> tmpDesc verwenden
                nearestDist = dist;
                nearestDesc = tmpDesc;
                nearestZoom = tmpDesc.zoom;
            }

            if (dist < nearestDist) {
                if (Math.abs(mapView.aktZoom - nearestZoom) < Math.abs(mapView.aktZoom - tmpDesc.zoom)) {
                    // zuerst die Tiles, die dem aktuellen Zoom Faktor am n�chsten sind.
                    continue;
                }
                nearestDist = dist;
                nearestDesc = tmpDesc;
                nearestZoom = tmpDesc.zoom;
            }
        }
        if (nearestDesc != null) {
            // if we don't remove here, the desc can be picked by another thread
            tmpQueuedTiles.remove(nearestDesc.getHashCode());
        }
        return nearestDesc;
    }

    private void loadTile(Descriptor desc) {

        TileGL tile = null;
        if (ManagerBase.manager != null) {
            tile = ManagerBase.manager.getTileGL(queueData.currentLayer, desc, threadId);
            if (tile != null) {
                addLoadedTile(desc, tile);
                // Redraw Map after a new Tile was loaded or generated
                GL.that.renderOnce();
            } else {
                if (ManagerBase.manager.cacheTile(queueData.currentLayer, desc)) {
                    tile = ManagerBase.manager.getTileGL(queueData.currentLayer, desc, threadId);
                    addLoadedTile(desc, tile);
                    // Redraw Map after a new Tile was loaded or generated
                    GL.that.renderOnce();
                }
            }
        }
    }

    private void loadOverlayTile(Descriptor desc) {
        if (queueData.currentOverlayLayer == null)
            return;

        TileGL tile = null;
        if (ManagerBase.manager != null) {
            // Load Overlay never inverted !!!
            tile = ManagerBase.manager.getTileGL(queueData.currentOverlayLayer, desc, threadId);
        }

        if (tile != null) {
            addLoadedOverlayTile(desc, tile);
            // Redraw Map after a new Tile was loaded or generated
            GL.that.renderOnce();
        } else {
            ManagerBase.manager.cacheTile(queueData.currentOverlayLayer, desc);
        }
    }

    private void addLoadedTile(Descriptor desc, TileGL tile) {
        queueData.loadedTilesLock.lock();
        try {
            if (queueData.loadedTiles.containsKey(desc.getHashCode())) {
                tile.dispose(); // das war dann umsonst
            } else {
                queueData.loadedTiles.add(desc.getHashCode(), tile);
            }

        } finally {
            queueData.loadedTilesLock.unlock();
        }
    }

    private void addLoadedOverlayTile(Descriptor desc, TileGL tile) {
        queueData.loadedOverlayTilesLock.lock();
        try {
            if (!queueData.loadedOverlayTiles.containsKey(desc.getHashCode())) {
                queueData.loadedOverlayTiles.add(desc.getHashCode(), tile);
            }
        } finally {
            queueData.loadedOverlayTilesLock.unlock();
        }
    }
}
