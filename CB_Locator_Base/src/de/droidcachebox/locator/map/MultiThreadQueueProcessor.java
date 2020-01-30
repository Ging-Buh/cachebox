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
package de.droidcachebox.locator.map;

import de.droidcachebox.utils.log.Log;

/**
 * insert sleeping with wakeup from maptileloader loadTiles(...
 * Logging with threadID
 */
class MultiThreadQueueProcessor extends Thread {
    private static int threadIndex = -1;
    private final MapTiles mapTiles;
    boolean isWorking, canTakeOrder, doStop;
    long startTime;
    private String log = "MapTileQueueThread";

    MultiThreadQueueProcessor(MapTiles mapTiles) {
        threadIndex++;
        log = log + "[" + threadIndex + "]";
        this.mapTiles = mapTiles;
        isWorking = false;
        startTime = System.currentTimeMillis();
        canTakeOrder = true;
        Log.info(log, "Starting a new thread with index: " + threadIndex);
        doStop = false;
    }

    @Override
    public void run() {
        try {
            do {
                canTakeOrder = false;
                OrderData newOrder = MapTileLoader.getInstance().getNextOrder(this);
                if (newOrder != null) {
                    startTime = System.currentTimeMillis();
                    isWorking = true;
                    // Log.info(log, "got Order: " + newOrder.descriptor + " Distance: " + (Integer) newOrder.descriptor.Data + " for " + newOrder.orderGroup);
                    newOrder.descriptor.Data = newOrder.mapView;
                    if (newOrder.forOverlay) {
                        if (!doStop) mapTiles.loadOverlayTile(newOrder.descriptor);
                    } else {
                        if (!doStop) mapTiles.loadTile(newOrder.descriptor);
                    }
                    isWorking = false;
                } else {
                    try {
                        canTakeOrder = true;
                        Thread.sleep(100000);
                    } catch (InterruptedException ignored) {
                    }
                }
            } while (!doStop);
            Log.info(log, "stopping");
        } catch (Exception ex3) {
            Log.err(log, log, ex3);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void doStop() {
        doStop = true;
        mapTiles.setIsReady();
    }

    static class OrderData {
        Descriptor descriptor;
        boolean forOverlay;
        MapViewBase mapView;

        OrderData(Descriptor actualDescriptor, boolean forOverlay, MapViewBase mapView) {
            descriptor = actualDescriptor;
            this.forOverlay = forOverlay;
            this.mapView = mapView;
        }
    }
}
