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

public class MapTileLoader {
    private static final String log = "MapTileLoader";
    public static AtomicBoolean finishYourself, isWorking;
    private static int PROCESSOR_COUNT; // == nr of threads for getting tiles
    private static int nextQueueProcessor;
    private static CopyOnWriteArrayList<MultiThreadQueueProcessor> queueProcessors;
    private final QueueData queueData;
    private int threadIndex;
    private int maxNumTiles;
    private Comparator<Descriptor> byDistanceFromCenter;
    private int orderGroup;
    private Thread queueProcessorAliveCheck;

    MapTileLoader() {
        queueData = new QueueData();
        threadIndex = 0;
        maxNumTiles = 0;
        finishYourself = new AtomicBoolean();
        finishYourself.set(false);
        isWorking = new AtomicBoolean();
        isWorking.set(false);
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        Log.info(log, "Number of processors: " + PROCESSOR_COUNT);
        queueProcessors = new CopyOnWriteArrayList<>();
        nextQueueProcessor = 0;
        orderGroup = -1;
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
                            MultiThreadQueueProcessor newThread = new MultiThreadQueueProcessor(queueData, threadToCheck.threadIndex);
                            queueProcessors.add(newThread);
                            newThread.setPriority(Thread.MIN_PRIORITY);
                            newThread.start();
                        } catch (Exception ex) {
                            Log.err(log, "Started a new thread with index: " + threadToCheck.threadIndex);
                        }
                    }
                }
            } while (true);
        });
    }

    private void startQueueProzessors() {
        for (threadIndex = 0; threadIndex < PROCESSOR_COUNT; threadIndex++) {
            MultiThreadQueueProcessor thread = new MultiThreadQueueProcessor(queueData, threadIndex);
            queueProcessors.add(thread);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        queueProcessorAliveCheck.start();
    }

    /*
    int getNumberOfLoadedTiles() {
        return queueData.loadedTiles.getNumberOfLoadedTiles();
    }
     */

    public void loadTiles(MapViewBase mapView, Descriptor upperLeftTile, Descriptor lowerRightTile, int aktZoom) {
        if (queueProcessors.size() ==  0) startQueueProzessors();
        orderGroup++;
        if (orderGroup == Integer.MAX_VALUE) orderGroup = -1;
        // take care of possibly different threads calling this (removed calling from render thread (GL Thread).  )
        // using a static boolean finishYourself that should finish this order and a new order with list of wantedtiles is supplied

        // make a copy of the loaded tiles (worst case an alrady generated Tile will be created again)
        Array<Long> loadedTiles = new Array<>();
        queueData.loadedTilesLock.lock();
        for (long hash : queueData.loadedTiles.getHashList()) {
            if (hash != 0) loadedTiles.add(hash);
        }
        queueData.loadedTilesLock.unlock();

        // make a copy of the loaded overlaytiles
        Array<Long> loadedOverlayTiles = new Array<>();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            for (long hash : queueData.loadedOverlayTiles.getHashList()) {
                if (hash != 0) loadedOverlayTiles.add(hash);
            }
            queueData.loadedOverlayTilesLock.unlock();
        }


        // calc nearest to middle and not yet handled
        // get tiles from middle of rectangle to border and not yet created
        // preparation
        int midX = (lowerRightTile.getX() + upperLeftTile.getX()) / 2;
        int midY = (lowerRightTile.getY() + upperLeftTile.getY()) / 2;
        // Log.info(log, "Center: " + new Descriptor(midX, midY, aktZoom));

        Array<Descriptor> wantedTiles = new Array<>();
        Array<Descriptor> wantedOverlayTiles = new Array<>();

        for (int i = upperLeftTile.getX(); i <= lowerRightTile.getX(); i++) {
            for (int j = upperLeftTile.getY(); j <= lowerRightTile.getY(); j++) {
                Descriptor descriptor = new Descriptor(i, j, aktZoom);
                descriptor.Data = Math.abs(i - midX) + Math.abs(j - midY);
                if (!loadedTiles.contains(descriptor.getHashCode(), false)) {
                    wantedTiles.add(descriptor);
                }
                if (queueData.currentOverlayLayer != null) {
                    if (!loadedOverlayTiles.contains(descriptor.getHashCode(), false)) {
                        wantedOverlayTiles.add(descriptor);
                    }
                }
            }
        }
        wantedTiles.sort(byDistanceFromCenter);
        if (queueData.currentOverlayLayer != null) {
            wantedOverlayTiles.sort(byDistanceFromCenter);
        }

        for (Descriptor descriptor : wantedTiles) {
            if (finishYourself.get()) {
                return;
            }
            queueProcessors.get(nextQueueProcessor).addOrder(descriptor, false, orderGroup, mapView);
            nextQueueProcessor = (nextQueueProcessor + 1) % PROCESSOR_COUNT;
        }

        for (Descriptor descriptor : wantedOverlayTiles) {
            if (finishYourself.get()) {
                return;
            }
            queueProcessors.get(nextQueueProcessor).addOrder(descriptor, true, orderGroup, mapView);
            nextQueueProcessor = (nextQueueProcessor + 1) % PROCESSOR_COUNT;
        }

        for (MultiThreadQueueProcessor thread : queueProcessors) {
            thread.interrupt();
        }

    }

    public void setMaxNumTiles(int maxNumTiles2) {
        if (maxNumTiles2 > maxNumTiles)
            maxNumTiles = maxNumTiles2;
        queueData.setLoadedTilesCacheCapacity(maxNumTiles);
    }

    private void clearLoadedTiles() {
        queueData.loadedTilesLock.lock();
        queueData.loadedTiles.clear();
        queueData.loadedTilesLock.unlock();

        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.loadedOverlayTiles.clear();
            queueData.loadedOverlayTilesLock.unlock();
        }
    }

    void clearLoadedOverlayTiles() {
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.loadedOverlayTiles.clear();
            queueData.loadedOverlayTilesLock.unlock();
        }
    }

    void increaseLoadedTilesAge() {
        queueData.loadedTilesLock.lock();
        queueData.loadedTiles.increaseLoadedTilesAge();
        queueData.loadedTilesLock.unlock();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.loadedOverlayTiles.increaseLoadedTilesAge();
            queueData.loadedOverlayTilesLock.unlock();
        }
    }

    boolean markTilesToDraw(Descriptor descriptor) {
        return queueData.loadedTiles.markTilesToDraw(descriptor.getHashCode());
    }

    int getTilesToDrawCounter() {
        return queueData.loadedTiles.getTilesToDrawCounter();
    }

    TileGL getDrawingTile(int i) {
        return queueData.loadedTiles.getDrawingTile(i);
    }

    void resetTilesToDrawCounter() {
        queueData.loadedTiles.resetTilesToDrawCounter();
    }

    boolean markOverlayTilesToDraw(Descriptor desc) {
        return queueData.loadedOverlayTiles.markTilesToDraw(desc.getHashCode());
    }

    int getOverlayTilesToDrawCounter() {
        return queueData.loadedOverlayTiles.getTilesToDrawCounter();
    }

    TileGL getOverlayDrawingTile(int i) {
        return queueData.loadedOverlayTiles.getDrawingTile(i);
    }

    void resetOverlayTilesToDrawCounter() {
        queueData.loadedOverlayTiles.resetTilesToDrawCounter();
    }

    void sortByAge() {
        queueData.loadedTiles.sortByAge();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTiles.sortByAge();
        }
    }

    public Layer getCurrentLayer() {
        return queueData.currentLayer;
    }

    public boolean setCurrentLayer(Layer layer, boolean isCarMode) {
        if (layer != queueData.currentLayer) {
            clearLoadedTiles();
            Log.info(log, "set layer to " + layer.name);
            layer.prepareLayer(isCarMode);
            queueData.currentLayer = layer;
            return true;
        }
        return false;
    }

    public void modifyCurrentLayer(boolean isCarMode) {
        clearLoadedTiles();
        queueData.currentLayer.prepareLayer(isCarMode);
    }

    public Layer getCurrentOverlayLayer() {
        return queueData.currentOverlayLayer;
    }

    public void setCurrentOverlayLayer(Layer layer) {
        queueData.currentOverlayLayer = layer;
    }

}
