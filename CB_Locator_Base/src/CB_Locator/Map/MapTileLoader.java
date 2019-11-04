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

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * at moment there is only one (static) MapTileLoader for (all) the three MapView Modes (normal, compass, track)
 */
public class MapTileLoader {
    private static final String log = "MapTileLoader";
    public static AtomicBoolean finishYourself, isWorking;
    private static int PROCESSOR_COUNT; // == nr of threads for getting tiles ?
    private static int nextQueueProcessor;
    private static CopyOnWriteArrayList<MultiThreadQueueProcessor> queueProcessors;
    private final MapTiles mapTiles; // the tiles from the layer: to show
    private Comparator<Descriptor> byDistanceFromCenter;
    private int orderAge;
    private Thread queueProcessorAliveCheck;
    private Array<Long> alreadyOrdered, alreadyOrderedOverlays;

    public MapTileLoader(int capacity) {
        mapTiles = new MapTiles(capacity);
        alreadyOrdered = new Array<>();
        alreadyOrderedOverlays = new Array<>();
        finishYourself = new AtomicBoolean();
        finishYourself.set(false);
        isWorking = new AtomicBoolean();
        isWorking.set(false);
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        Log.info(log, "Number of processors: " + PROCESSOR_COUNT);
        queueProcessors = new CopyOnWriteArrayList<>();
        nextQueueProcessor = 0;
        orderAge = -1;
        byDistanceFromCenter = (o1, o2) -> Integer.compare((Integer) o1.Data, (Integer) o2.Data);

        queueProcessorAliveCheck = new Thread(() -> {
            do {
                // Log.info(log, "queueProcessors alive checking");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
                for (Iterator<MultiThreadQueueProcessor> iterator = queueProcessors.iterator(); iterator.hasNext(); ) {
                    MultiThreadQueueProcessor threadToCheck = iterator.next();
                    boolean isHanging = (System.currentTimeMillis() - threadToCheck.startTime > 30000) && threadToCheck.isWorking; // or less or more??
                    if (!threadToCheck.isAlive() || isHanging) {
                        try {
                            //Log.info(log, "Starting a new thread with index: " + threadToCheck.threadIndex);
                            queueProcessors.remove(threadToCheck);
                            MultiThreadQueueProcessor newThread = new MultiThreadQueueProcessor(mapTiles);
                            queueProcessors.add(newThread);
                            newThread.setPriority(Thread.MIN_PRIORITY);
                            newThread.start();
                        } catch (Exception ex) {
                            Log.err(log, "Started a new thread with index: ");
                        }
                    }
                }
            } while (true);
        });
    }

    private void startQueueProzessors() {
        int threadIndex = 0;
        while (threadIndex < PROCESSOR_COUNT) {
            MultiThreadQueueProcessor thread = new MultiThreadQueueProcessor(mapTiles);
            queueProcessors.add(thread);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
            threadIndex++;
        }
        queueProcessorAliveCheck.start();
    }

    public void loadTiles(MapViewBase mapView, Descriptor lowerTile, Descriptor upperTile, int aktZoom) {
        if (queueProcessors.size() == 0) startQueueProzessors();
        orderAge++;
        if (orderAge == Integer.MAX_VALUE) orderAge = 0;
        // take care of possibly different threads calling this (removed calling from render thread (GL Thread).  )
        // using a static boolean finishYourself that should finish this order and a new order with list of wantedtiles is supplied

        // make a copy of the loaded tiles (worst case an already generated Tile will be created again)
        Array<Long> loadedTiles = mapTiles.getTilesHashCopy();
        for (Long loaded : loadedTiles) {
            alreadyOrdered.removeValue(loaded, false);
        }

        // make a copy of the loaded overlaytiles
        Array<Long> loadedOverlayTiles = mapTiles.getOverlayTilesHashCopy();
        for (Long loaded : loadedOverlayTiles) {
            alreadyOrderedOverlays.removeValue(loaded, false);
        }

        // calc nearest to middle and not yet handled
        // get tiles from middle of rectangle to border and not yet created
        // preparation
        int midX = (upperTile.getX() + lowerTile.getX()) / 2;
        int midY = (upperTile.getY() + lowerTile.getY()) / 2;
        // Log.trace(log, "Center: " + new Descriptor(midX, midY, aktZoom));

        Array<Descriptor> wantedTiles = new Array<>();

        for (int i = lowerTile.getX(); i <= upperTile.getX(); i++) {
            for (int j = lowerTile.getY(); j <= upperTile.getY(); j++) {
                Descriptor descriptor = new Descriptor(i, j, aktZoom);
                descriptor.Data = Math.max(Math.abs(i - midX), Math.abs(j - midY));
                wantedTiles.add(descriptor);
            }
        }
        if (wantedTiles.size == 0)
            return;
        wantedTiles.sort(byDistanceFromCenter);

        int orderCount = 0; // don't order more than want to be cached !!! todo over all orders till now
        int firstDistance = 0;
        boolean isSetFirstDistance = false;
        for (Descriptor descriptor : wantedTiles) {
            if (finishYourself.get()) {
                return;
            }
            if (!loadedTiles.contains(descriptor.getHashCode(), false) && !alreadyOrdered.contains(descriptor.getHashCode(), false)) {
                MultiThreadQueueProcessor thread;
                int previousQueueProcessor = nextQueueProcessor;
                do {
                    thread = queueProcessors.get(nextQueueProcessor);
                    nextQueueProcessor = (nextQueueProcessor + 1) % PROCESSOR_COUNT;
                    if (nextQueueProcessor == previousQueueProcessor) {
                        try {
                            Thread.sleep(1000);
                        }
                        catch (Exception ignored) {
                        }
                    }
                }
                while (!thread.canTakeOrder);
                if (!isSetFirstDistance) {
                    firstDistance = (int) descriptor.Data;
                    isSetFirstDistance = true;
                }
                if (((int) descriptor.Data) - firstDistance > 1) {
                    // first create the nearest tiles
                    // if a tile is missing on rendering the load will be ordered there (again)
                    Log.info(log, "ordered: " + orderCount + " Distance: " + ((int) descriptor.Data - 1));
                    return;
                }
                Log.info(log, "order: " + descriptor + " Distance: " + firstDistance + " run: " + orderAge + " on thread: " + nextQueueProcessor);
                alreadyOrdered.add(descriptor.getHashCode());
                thread.addOrder(descriptor, false, orderAge, mapView);
                thread.interrupt();
                orderCount++;
                if (orderCount == mapTiles.getCapacity())
                    break;
            }
            if (finishYourself.get()) {
                return;
            }
            if (mapTiles.currentOverlayLayer != null) {
                if (!loadedOverlayTiles.contains(descriptor.getHashCode(), false) && !alreadyOrderedOverlays.contains(descriptor.getHashCode(), false)) {
                    MultiThreadQueueProcessor thread;
                    int previousQueueProcessor = nextQueueProcessor;
                    do {
                        thread = queueProcessors.get(nextQueueProcessor);
                        nextQueueProcessor = (nextQueueProcessor + 1) % PROCESSOR_COUNT;
                        if (nextQueueProcessor == previousQueueProcessor) {
                            try {
                                Thread.sleep(1000);
                            }
                            catch (Exception ignored) {
                            }
                        }
                    }
                    while (!thread.canTakeOrder);
                    alreadyOrderedOverlays.add(descriptor.getHashCode());
                    thread.addOrder(descriptor, true, orderAge, mapView);
                    thread.interrupt();
                    nextQueueProcessor = (nextQueueProcessor + 1) % PROCESSOR_COUNT;
                    orderCount++;
                    if (orderCount == mapTiles.getCapacity())
                        break;
                }
            }
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
        return mapTiles.currentLayer;
    }

    public boolean setCurrentLayer(Layer layer, boolean isCarMode) {
        if (layer != mapTiles.currentLayer) {
            mapTiles.clearTiles();
            Log.info(log, "set layer to " + layer.name);
            layer.prepareLayer(isCarMode);
            mapTiles.currentLayer = layer;
            return true;
        }
        return false;
    }

    public void modifyCurrentLayer(boolean isCarMode) {
        mapTiles.clearTiles();
        mapTiles.currentLayer.prepareLayer(isCarMode);
    }

    public Layer getCurrentOverlayLayer() {
        return mapTiles.currentOverlayLayer;
    }

    public void setCurrentOverlayLayer(Layer layer) {
        mapTiles.currentOverlayLayer = layer;
        mapTiles.clearOverlayTiles();
    }

    private static class OrderData {
        Descriptor descriptor;
        boolean forOverlay;
        int orderAge;
        MapViewBase mapView;

        OrderData(Descriptor actualDescriptor, boolean forOverlay, int orderAge, MapViewBase mapView) {
            this.descriptor = actualDescriptor;
            this.forOverlay = forOverlay;
            this.orderAge = orderAge;
            this.mapView = mapView;
        }
    }

}
