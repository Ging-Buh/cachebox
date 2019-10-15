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
 * insert sleeping with wakeup from maptileloader loadTiles(...
 * Logging with threadID
 */
class MultiThreadQueueProcessor extends Thread {
    static final Lock inLoadDescLock = new ReentrantLock();
    static final CB_List<Descriptor> inLoadDesc = new CB_List<>();
    final int threadIndex;
    private final QueueData queueData;
    public Descriptor actualDescriptor;
    long startTime;
    boolean isWorking;
    private String log = "MapTileQueueThread";

    MultiThreadQueueProcessor(QueueData queueData, int threadID) {
        log = log + "[" + threadID + "]";
        this.threadIndex = threadID;
        this.queueData = queueData;
        isWorking = false;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            do {
                actualDescriptor = null;
                if (!Energy.isDisplayOff() && ((queueData.wantedTiles.size() > 0) || (queueData.wantedOverlayTiles.size() > 0))) {
                    try {
                        boolean calcOverlay = false;
                        queueData.queuedTilesLock.lock();
                        if (queueData.currentOverlayLayer != null) {
                            if (queueData.wantedTiles.size() == 0)
                                calcOverlay = true;
                            queueData.queuedOverlayTilesLock.lock();
                        }
                        try {
                            actualDescriptor = calcNextAndRemove((queueData.wantedTiles.size() > 0) ? queueData.wantedTiles : queueData.wantedOverlayTiles);
                        } finally {
                            queueData.queuedTilesLock.unlock();
                            if (queueData.currentOverlayLayer != null)
                                queueData.queuedOverlayTilesLock.unlock();
                        }

                        if (actualDescriptor != null) {
                            inLoadDescLock.lock();
                            if (inLoadDesc.contains(actualDescriptor)) {
                                // Other thread is loading this Desc. Skip!
                                inLoadDescLock.unlock();
                                continue;
                            }
                            inLoadDescLock.unlock();

                            if (calcOverlay && queueData.currentOverlayLayer != null) {
                                startTime = System.currentTimeMillis();
                                isWorking = true;
                                loadOverlayTile(actualDescriptor);
                                isWorking = false;
                            }
                            else if (queueData.currentLayer != null) {
                                inLoadDescLock.lock();
                                if (inLoadDesc.contains(actualDescriptor)) {
                                    inLoadDescLock.unlock();
                                    continue;// Other thread is loading this Desc. Skip!
                                }
                                inLoadDesc.add(actualDescriptor);
                                inLoadDescLock.unlock();
                            }

                            startTime = System.currentTimeMillis();
                            isWorking = true;
                            loadTile(actualDescriptor);
                            isWorking = false;
                            // long lasts = (System.currentTimeMillis() - startTime);

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {
                                // Log.trace(log, "returned from powernapping");
                            }
                        } else {
                            // no next descriptor calculated, so we can sleep
                            try {
                                Log.err(log, "Descriptor = null");
                                Thread.sleep(10000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    } catch (Exception ex1) {
                        Log.err(log, "getting Descriptor: " + actualDescriptor + " : " + ex1.toString());
                    }
                } else {
                    try {
                        Log.trace(log, "Do a long sleep");
                        do {
                            Thread.sleep(100000);
                        }
                        while (Energy.isDisplayOff() || ((queueData.wantedTiles.size() <= 0) && (queueData.wantedOverlayTiles.size() <= 0)));
                    } catch (InterruptedException i) {
                        Log.trace(log, "returned from sleeping");
                    }
                }
            } while (true);
        } catch (Exception ex3) {
            Log.err(log, "try over all " + ex3.toString(), ex3);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private Descriptor calcNextAndRemove(SortedMap<Long, Descriptor> tmpQueuedTiles) {
        if (tmpQueuedTiles == null) return null;
        Descriptor nearestDesc = null;
        double nearestDist = Double.MAX_VALUE;
        int nearestZoom = 0;
        for (Descriptor tmpDesc : tmpQueuedTiles.values()) {
            MapViewBase mapView;
            if (tmpDesc.Data instanceof MapViewBase)
                mapView = (MapViewBase) tmpDesc.Data;
            else
                continue;

            long posFactor = mapView.getMapTilePosFactor(tmpDesc.zoom);

            double dist = Math.sqrt(Math.pow((double) tmpDesc.X * posFactor * 256 + 128 * posFactor - mapView.screenCenterWorld.getX(), 2) + Math.pow((double) tmpDesc.Y * posFactor * 256 + 128 * posFactor + mapView.screenCenterWorld.getY(), 2));

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

    private void loadTile(final Descriptor descriptor) {
        TileGL tile;
        try {
            tile = queueData.currentLayer.getTileGL(descriptor);
        }
        catch (Exception ex) {
            Log.err(log, "loadTile", ex);
            tile = null;
        }
        inLoadDescLock.lock();
        inLoadDesc.remove(descriptor);
        inLoadDescLock.unlock();

        if (tile != null) {
            addLoadedTileWithLock(descriptor, tile);
            // Redraw Map after a new Tile was loaded or generated
            GL.that.renderOnce();
        } else {
            new Thread(() -> {
                // download in separate thread
                if (queueData.currentLayer.cacheTile(descriptor)) {
                    addLoadedTileWithLock(descriptor, queueData.currentLayer.getTileGL(descriptor));
                    // Redraw Map after a new Tile was loaded or generated
                    GL.that.renderOnce();
                }
            }).start();
        }
    }

    private void loadOverlayTile(Descriptor desc) {
        if (queueData.currentOverlayLayer == null)
            return;

        TileGL tile = queueData.currentOverlayLayer.getTileGL(desc);

        if (tile != null) {
            addLoadedOverlayTileWithLock(desc, tile);
            // Redraw Map after a new Tile was loaded or generated
            GL.that.renderOnce();
        } else {
            new Thread(() -> {
                // download in separate thread
                queueData.currentOverlayLayer.cacheTile(desc);
            }).start();
        }
    }

    private void addLoadedTileWithLock(Descriptor desc, TileGL tile) {
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

    private void addLoadedOverlayTileWithLock(Descriptor desc, TileGL tile) {
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
