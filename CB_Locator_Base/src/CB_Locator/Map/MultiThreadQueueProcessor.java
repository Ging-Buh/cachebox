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

import CB_Utils.Log.Log;
import com.badlogic.gdx.utils.Array;

/**
 * insert sleeping with wakeup from maptileloader loadTiles(...
 * Logging with threadID
 */
class MultiThreadQueueProcessor extends Thread {
    private static int threadIndex = -1;
    private final MapTiles mapTiles;
    private final Array<OrderData> orders;
    boolean isWorking, canTakeOrder;
    long startTime;
    private String log = "MapTileQueueThread";
    private OrderData newOrder;

    MultiThreadQueueProcessor(MapTiles mapTiles) {
        threadIndex++;
        log = log + "[" + threadIndex + "]";
        this.mapTiles = mapTiles;
        isWorking = false;
        startTime = System.currentTimeMillis();
        orders = new Array<>(true, mapTiles.getCapacity());
        canTakeOrder = true;
    }

    /**
     * used by MapTileLoader for ordering a new Tile (so a Texture to draw)
     */
    void addOrder(Descriptor descriptor, boolean forOverlay, MapViewBase mapView) {
        synchronized (orders) {
            orders.add(new OrderData(descriptor, forOverlay, mapView));
        }
        // Log.info(log, "put Order: " + descriptor + " Distance: " + (Integer) descriptor.Data ;
    }

    private boolean getNextOrder() {
        synchronized (orders) {
            if (orders.size > 0) {
                newOrder = orders.get(0);
                orders.removeIndex(0);
                return true;
            }
            return false;
        }
    }

    @Override
    public void run() {
        try {
            do {
                canTakeOrder = false;
                if (getNextOrder()) {
                    startTime = System.currentTimeMillis();
                    isWorking = true;
                    // Log.info(log, "got Order: " + newOrder.descriptor + " Distance: " + (Integer) newOrder.descriptor.Data + " for " + newOrder.orderGroup);
                    newOrder.descriptor.Data = newOrder.mapView;
                    if (newOrder.forOverlay) {
                        mapTiles.loadOverlayTile(newOrder.descriptor);
                    } else {
                        mapTiles.loadTile(newOrder.descriptor);
                    }
                    isWorking = false;
                } else {
                    try {
                        // Log.info(log, "Wait for Order");
                        canTakeOrder = true;
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

    private static class OrderData {
        Descriptor descriptor;
        boolean forOverlay;
        MapViewBase mapView;

        OrderData(Descriptor actualDescriptor, boolean forOverlay, MapViewBase mapView) {
            this.descriptor = actualDescriptor;
            this.forOverlay = forOverlay;
            this.mapView = mapView;
        }
    }
}
