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
import CB_Utils.Log.Log;

import java.util.SortedMap;

/**
 * insert sleeping with wakeup from maptileloader loadTiles(...
 * Logging with threadID
 */
class MultiThreadQueueProcessor extends Thread {
    private final QueueData queueData;
    int threadIndex;
    long startTime;
    boolean isWorking;
    private String log = "MapTileQueueThread";

    MultiThreadQueueProcessor(QueueData queueData, int threadIndex) {
        log = log + "[" + threadIndex + "]";
        this.queueData = queueData;
        isWorking = false;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            do {
                Descriptor actualDescriptor;
                if (Energy.isDisplayOff()) {
                    try {
                        Log.info(log, "No Energy: I sleep");
                        do {
                            Thread.sleep(100000); // or longer
                        }
                        while (Energy.isDisplayOff());
                    } catch (InterruptedException i) {
                        Log.info(log, "tanked Energy");
                    }
                } else {
                    if (queueData.wantedTiles.size() > 0) {
                        startTime = System.currentTimeMillis();
                        isWorking = true;
                        try {
                            queueData.wantedTilesLock.lock();
                            actualDescriptor = calcNextAndRemove(queueData.wantedTiles);
                            queueData.wantedTilesLock.unlock();
                            if (actualDescriptor != null) {
                                loadTile(actualDescriptor);
                            } else {
                                Log.err(log, "calcNextAndRemove for wantedTile");
                            }
                        } catch (Exception ex) {
                            Log.err(log, "calcNextAndRemove: " + ex, ex);
                            queueData.wantedTilesLock.unlock();
                        }
                        isWorking = false;
                    } else if (queueData.wantedOverlayTiles.size() > 0) {
                        startTime = System.currentTimeMillis();
                        isWorking = true;
                        try {
                            queueData.wantedOverlayTilesLock.lock();
                            actualDescriptor = calcNextAndRemove(queueData.wantedOverlayTiles);
                            queueData.wantedOverlayTilesLock.unlock();
                            if (actualDescriptor != null) {
                                loadOverlayTile(actualDescriptor);
                            } else {
                                Log.err(log, "calcNextAndRemove for wantedOverlayTile");
                            }
                        } catch (Exception ex) {
                            Log.err(log, "calcNextAndRemove: " + ex, ex);
                            queueData.wantedOverlayTilesLock.unlock();
                        }
                        isWorking = false;
                    } else {
                        // nothing to do
                        // Log.info(log, "empty MapTileQueue: sleeping deep");
                        try {
                            Thread.sleep(100000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    try {
                        Thread.sleep(100); // Let others work
                    } catch (InterruptedException ignored) {
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
        if (tmpQueuedTiles == null) {
            Log.err(log, "queue not initialized: no way");
            return null;
        }
        Descriptor nearestDesc = null;
        double nearestDist = Double.MAX_VALUE;
        int nearestZoom = 0;
        for (Descriptor tmpDesc : tmpQueuedTiles.values()) {
            MapViewBase mapView;
            if (tmpDesc.Data instanceof MapViewBase)
                mapView = (MapViewBase) tmpDesc.Data;
            else {
                Log.err(log, "not for a map? for what shall i calc");
                continue;
            }

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

        if (nearestDesc == null) {
            if (tmpQueuedTiles.values().size() > 0) {
                // simply take the first from tmpQueuedTiles
                nearestDesc = tmpQueuedTiles.values().toArray(new Descriptor[0])[0];
            }
        }
        if (nearestDesc == null) {
            Log.err(log, "could not determine the first mapTile from number of tiles: " + tmpQueuedTiles.values().size());
        } else {
            // if we don't remove here, the desc can be picked by another thread
            try {
                tmpQueuedTiles.remove(nearestDesc.getHashCode());
            } catch (Exception ex) {
                Log.err(log, "tmpQueuedTiles.remove(nearestDesc.getHashCode())", ex);
            }
        }
        return nearestDesc;
    }

    private void loadTile(final Descriptor descriptor) {
        TileGL tile;
        try {
            tile = queueData.currentLayer.getTileGL(descriptor);
        } catch (Exception ex) {
            Log.err(log, "loadTile", ex);
            tile = null;
        }

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

    private void loadOverlayTile(final Descriptor descriptor) {
        if (queueData.currentOverlayLayer == null)
            return;

        TileGL tile = queueData.currentOverlayLayer.getTileGL(descriptor);

        if (tile != null) {
            addLoadedOverlayTileWithLock(descriptor, tile);
            // Redraw Map after a new Tile was loaded or generated
            GL.that.renderOnce();
        } else {
            new Thread(() -> {
                // download in separate thread
                queueData.currentOverlayLayer.cacheTile(descriptor);
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
