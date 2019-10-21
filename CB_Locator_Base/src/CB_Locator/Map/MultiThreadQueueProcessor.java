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

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Log.Log;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * insert sleeping with wakeup from maptileloader loadTiles(...
 * Logging with threadID
 */
class MultiThreadQueueProcessor extends Thread {
    private static int newOrderGroup;
    private final QueueData queueData;
    int threadIndex;
    long startTime;
    boolean isWorking;
    private String log = "MapTileQueueThread";
    final private Array<OrderData> orders;
    private OrderData newOrder;
    private int actualOrderGroup;

    MultiThreadQueueProcessor(QueueData queueData, int threadIndex) {
        log = log + "[" + threadIndex + "]";
        this.threadIndex = threadIndex;
        this.queueData = queueData;
        isWorking = false;
        startTime = System.currentTimeMillis();
        actualOrderGroup = -1;
        newOrderGroup = -1;
        orders = new Array<>(true, queueData.getCapacity());
    }

    /**
     * used by MapTileLoader for ordering a new Tile (so a Texture to draw)
     */
    void addOrder(Descriptor descriptor, boolean forOverlay, int orderGroup, MapViewBase mapView) {
        newOrderGroup = orderGroup;
        removeOldOrders();
        orders.add(new OrderData(descriptor, forOverlay, orderGroup, mapView));
        // Log.info(log, "put Order: " + descriptor + " Distance: " + (Integer) descriptor.Data + " for " + orderGroup);
    }

    private boolean getNextOrder() {
        removeOldOrders();
        if (orders.size > 0) {
            newOrder = orders.get(0);
            orders.removeIndex(0);
            actualOrderGroup = newOrderGroup;
            return true;
        }
        return false;
    }

    private void removeOldOrders() {
        synchronized (orders) {
            if (orders.size > 0) {
                if (actualOrderGroup != newOrderGroup) {
                    while (orders.size > 0 && orders.get(0).orderGroup != newOrderGroup) {
                        for (Iterator<OrderData> iterator = orders.iterator(); iterator.hasNext(); ) {
                            OrderData od = iterator.next();
                            if (od.orderGroup != newOrderGroup) {
                                // Log.info(log, "remove " + od.descriptor + " of " + od.orderGroup);
                                orders.removeValue(od, true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            do {
                if (getNextOrder()) {
                    startTime = System.currentTimeMillis();
                    isWorking = true;
                    // Log.info(log, "got Order: " + newOrder.descriptor + " Distance: " + (Integer) newOrder.descriptor.Data + " for " + newOrder.orderGroup);
                    newOrder.descriptor.Data = newOrder.mapView;
                    if (newOrder.forOverlay) {
                        loadOverlayTile(newOrder.descriptor);
                    } else {
                        loadTile(newOrder.descriptor);
                    }
                    isWorking = false;
                } else {
                    try {
                        // Log.info(log, "Wait for Order");
                        Thread.sleep(100000);
                    } catch (InterruptedException ignored) {
                    }
                }
            } while (true);
        } catch (Exception ex3) {
            Log.err(log, log, ex3);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
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

    private static class OrderData {
        Descriptor descriptor;
        boolean forOverlay;
        int orderGroup;
        MapViewBase mapView;

        OrderData(Descriptor actualDescriptor, boolean forOverlay, int orderGroup, MapViewBase mapView) {
            this.descriptor = actualDescriptor;
            this.forOverlay = forOverlay;
            this.orderGroup = orderGroup;
            this.mapView = mapView;
        }
    }
}
