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

import com.badlogic.gdx.utils.Array;
import de.droidcachebox.utils.log.Log;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * at moment MapTileLoader is singleton ( meaning: there is only one instance ) for (all) the three MapView Modes (normal, compass, track)
 * So the loadTiles can be called from different threads.
 * These are handled and synced by is isWorking and finishYourself
 * Every call to loadTiles empties the orders, because there is possibly a new area to cover with mapTiles. Tiles already in generation will not be affected.
 */
public class MapTileLoader {
    private static final String log = "MapTileLoader";
    public static AtomicBoolean finishYourself, isWorking;
    private static MapTileLoader mapTileLoader;
    private static int PROCESSOR_COUNT; // == nr of threads for getting tiles ?
    private static CopyOnWriteArrayList<MultiThreadQueueProcessor> queueProcessors;
    private final Array<MultiThreadQueueProcessor.OrderData> orders;
    private final MapTiles mapTiles; // the tiles from the layer: to show
    private Comparator<Descriptor> byDistanceFromCenter;
    private Thread queueProcessorAliveCheck;
    private Array<Long> mapTilesInGeneration, overlayTilesInGeneration;
    private boolean queueProcessorsAreStarted;

    public MapTileLoader() {
        int capacity = 60;
        /*
        // calculate max Map Tile cache (only if not singleton)
        try {
            int aTile = 256 * 256;
            maxTilesPerScreen = (int) ((getWidth() * getHeight()) / aTile + 0.5);
            capacity = (int) (maxTilesPerScreen * 6);// 6 times as much as necessary

        } catch (Exception e) {
            capacity = 60;
        }
        capacity = Math.min(capacity, 60);
        capacity = Math.max(capacity, 20);
        // capacity between 20 and 60
         */
        mapTiles = new MapTiles(capacity); // this is for all visible maps together
        mapTilesInGeneration = new Array<>();
        overlayTilesInGeneration = new Array<>();
        finishYourself = new AtomicBoolean();
        finishYourself.set(false);
        isWorking = new AtomicBoolean();
        isWorking.set(false);
        orders = new Array<>(true, mapTiles.getCapacity());
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        Log.info(log, "Number of processors: " + PROCESSOR_COUNT);
        queueProcessors = new CopyOnWriteArrayList<>();
        queueProcessorsAreStarted = false;
        byDistanceFromCenter = (o1, o2) -> Integer.compare((Integer) o1.getData(), (Integer) o2.getData());

        queueProcessorAliveCheck = new Thread(() -> {
            do {
                // Log.info(log, "queueProcessors alive checking");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
                for (MultiThreadQueueProcessor threadToCheck : queueProcessors) {
                    boolean abortAndNew = false;
                    if (!threadToCheck.isAlive()) {
                        // is down (?Exception)
                        abortAndNew = true;
                        Log.info(log, "thread is down.");
                    }
                    if ((System.currentTimeMillis() - threadToCheck.startTime > 60000) && threadToCheck.isWorking) {
                        // is hanging (loading maptile for more than one minute) loading maptile
                        abortAndNew = true;
                        Log.info(log, "thread is hanging.");
                    }
                    if (abortAndNew) {
                        try {
                            stopQueueProzessor(threadToCheck);
                            MultiThreadQueueProcessor newThread = new MultiThreadQueueProcessor(mapTiles);
                            queueProcessors.add(newThread);
                            newThread.setPriority(Thread.MIN_PRIORITY);
                            newThread.start();
                        } catch (Exception ex) {
                            Log.err(log, "Start a new thread", ex);
                        }
                    }
                }
            } while (true);
        });
    }

    public static MapTileLoader getInstance() {
        if (mapTileLoader == null) mapTileLoader = new MapTileLoader();
        return mapTileLoader;
    }

    private void startQueueProzessors() {
        for (int i = 0; i < PROCESSOR_COUNT; i++) {
            MultiThreadQueueProcessor thread = new MultiThreadQueueProcessor(mapTiles);
            queueProcessors.add(thread);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        queueProcessorsAreStarted = true;
        queueProcessorAliveCheck.start();
    }

    public void stopQueueProzessors() {
        for (MultiThreadQueueProcessor mtp : queueProcessors) {
            mtp.doStop();
            mtp.interrupt();
        }
        queueProcessors.clear();
    }

    private void stopQueueProzessor(MultiThreadQueueProcessor threadToStop) {
        threadToStop.doStop();
        threadToStop.interrupt();
        queueProcessors.remove(threadToStop);
    }

    public void loadTiles(MapViewBase mapView, Descriptor lowerTile, Descriptor upperTile, int aktZoom) {
        synchronized (orders) {
            orders.clear();
            if (!queueProcessorsAreStarted || queueProcessors.size() == 0) startQueueProzessors();

            // make a copy of the loaded tiles (worst case an already generated Tile will be created again)
            Array<Long> loadedTiles = mapTiles.getTilesHashCopy();
            Log.trace(log, "Num loadedTiles: " + loadedTiles.size);
            for (Long loaded : loadedTiles) {
                mapTilesInGeneration.removeValue(loaded, false);
            }

            // make a copy of the loaded overlaytiles
            Array<Long> loadedOverlayTiles = mapTiles.getOverlayTilesHashCopy();
            Log.trace(log, "Num loadedOverlayTiles: " + loadedOverlayTiles.size);
            for (Long loaded : loadedOverlayTiles) {
                overlayTilesInGeneration.removeValue(loaded, false);
            }

            // calc nearest to middle and not yet handled
            // get tiles from middle of rectangle to border and not yet created
            // preparation
            int midX = (upperTile.getX() + lowerTile.getX()) / 2;
            int midY = (upperTile.getY() + lowerTile.getY()) / 2;

            Array<Descriptor> wantedTiles = new Array<>();

            for (int i = lowerTile.getX(); i <= upperTile.getX(); i++) {
                for (int j = lowerTile.getY(); j <= upperTile.getY(); j++) {
                    Descriptor descriptor = new Descriptor(i, j, aktZoom);
                    descriptor.setData(Math.max(Math.abs(i - midX), Math.abs(j - midY)));
                    wantedTiles.add(descriptor);
                }
            }
            if (wantedTiles.size == 0)
                return;
            wantedTiles.sort(byDistanceFromCenter);

            // Log.debug(log, "Num wanted: " + wantedTiles.size);
            for (Descriptor descriptor : wantedTiles) {
                if (finishYourself.get()) {
                    Log.info(log, "MapTileLoader finishMyself during tile ordering");
                    return;
                }
                if (!loadedTiles.contains(descriptor.getHashCode(), false) && !mapTilesInGeneration.contains(descriptor.getHashCode(), false)) {
                    orders.add(new MultiThreadQueueProcessor.OrderData(descriptor, false, mapView));
                }
                else {
                    Log.trace(log, "Descriptor in loadedTiles: " + !loadedTiles.contains(descriptor.getHashCode(), false));
                    Log.trace(log, "Descriptor in Generation : " + !mapTilesInGeneration.contains(descriptor.getHashCode(), false));
                }
                if (finishYourself.get()) {
                    Log.info(log, "MapTileLoader finishMyself after mapTiles ordered");
                    return;
                }
                if (mapTiles.getCurrentOverlayLayer() != null) {
                    if (!loadedOverlayTiles.contains(descriptor.getHashCode(), false) && !overlayTilesInGeneration.contains(descriptor.getHashCode(), false)) {
                        orders.add(new MultiThreadQueueProcessor.OrderData(descriptor, false, mapView));
                    }
                }
            }
            // wakeup of possibly sleeping MultiThreadQueueProcessor threads
            for (MultiThreadQueueProcessor thread : queueProcessors) {
                if (thread.canTakeOrder) thread.interrupt();
            }

            // Log.debug(log, "MapTileLoader completed with " + orders.size + " tile orders.");
        }
    }

    /**
     * this is for one of the MultiThreadQueueProcessor threads to generate a new tile
     */
    MultiThreadQueueProcessor.OrderData getNextOrder(MultiThreadQueueProcessor forThread) {
        synchronized (orders) {
            if (orders.size > 0) {
                MultiThreadQueueProcessor.OrderData newOrder = orders.get(0);
                orders.removeIndex(0);
                if (newOrder.forOverlay)
                    overlayTilesInGeneration.add(newOrder.descriptor.getHashCode());
                else
                    mapTilesInGeneration.add(newOrder.descriptor.getHashCode());
                Log.trace(log, "mapTilesInGeneration : " + mapTilesInGeneration.size);
                return newOrder;
            }
            return null;
        }
    }

    int markTileToDraw(long hash) {
        return mapTiles.markTileToDraw(hash);
    }

    int getTilesToDrawCounter() {
        return mapTiles.getTilesToDrawCounter();
    }

    TileGL getDrawingTile(int i) {
        return mapTiles.getDrawingTile(i);
    }

    void resetTilesToDrawCounter() {
        mapTiles.resetTilesToDrawCounter();
    }

    int markOverlayTileToDraw(long hash) {
        return mapTiles.markOverlayTileToDraw(hash);
    }

    int getOverlayTilesToDrawCounter() {
        return mapTiles.getOverlayTilesToDrawCounter();
    }

    TileGL getOverlayDrawingTile(int i) {
        return mapTiles.getOverlayDrawingTile(i);
    }

    void resetOverlayTilesToDrawCounter() {
        mapTiles.resetOverlayTilesToDrawCounter();
    }

    void increaseAge() {
        mapTiles.increaseAge();
    }

    void sortByAge() {
        mapTiles.sortByAge();
    }

    public Layer getCurrentLayer() {
        return mapTiles.getCurrentLayer();
    }

    public boolean setCurrentLayer(Layer layer, boolean isCarMode) {
        if (layer != mapTiles.getCurrentLayer()) {
            mapTilesInGeneration.clear();
            mapTiles.clearTiles();
            Log.info(log, "set layer to " + layer.name);
            layer.prepareLayer(isCarMode);
            mapTiles.setCurrentLayer(layer);
            return true;
        }
        return false;
    }

    public void modifyCurrentLayer(boolean isCarMode) {
        mapTilesInGeneration.clear();
        mapTiles.clearTiles();
        mapTiles.getCurrentLayer().prepareLayer(isCarMode);
    }

    public Layer getCurrentOverlayLayer() {
        return mapTiles.getCurrentOverlayLayer();
    }

    public void setCurrentOverlayLayer(Layer layer) {
        overlayTilesInGeneration.clear();
        mapTiles.setCurrentOverlayLayer(layer);
        mapTiles.clearOverlayTiles();
    }

}
