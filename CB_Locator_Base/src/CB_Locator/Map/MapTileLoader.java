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

import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapTileLoader {
    private static final String log = "MapTileLoader";
    static int PROCESSOR_COUNT; // == nr of threads for getting tiles
    private final QueueData queueData;
    private int threadIndex;
    private static CopyOnWriteArrayList<MultiThreadQueueProcessor> queueProcessors;
    private int maxNumTiles;

    MapTileLoader() {
        queueData = new QueueData();
        threadIndex = 0;
        maxNumTiles = 0;
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        Log.trace(log, "Number of processors: " + PROCESSOR_COUNT);
        queueProcessors = new CopyOnWriteArrayList<>();
        Thread queueProcessorAliveCheck = new Thread(() -> {
            do {
                Log.trace(log, "queueProcessors alive checking");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
                for (Iterator<MultiThreadQueueProcessor> iterator = queueProcessors.iterator(); iterator.hasNext(); ) {
                    MultiThreadQueueProcessor threadToCheck = iterator.next();
                    boolean isHanging = (System.currentTimeMillis() - threadToCheck.startTime > 30000) && threadToCheck.isWorking; // or less or more??
                    if (!threadToCheck.isAlive() || isHanging) {
                        try {
                        Log.trace(log, "Starting a new thread with index: " + threadToCheck.threadIndex);
                        queueProcessors.remove(threadToCheck);
                        MultiThreadQueueProcessor.inLoadDescLock.lock();
                        MultiThreadQueueProcessor.inLoadDesc.remove(threadToCheck.actualDescriptor);
                        MultiThreadQueueProcessor.inLoadDescLock.unlock();
                        MultiThreadQueueProcessor newThread = new MultiThreadQueueProcessor(queueData, threadToCheck.threadIndex);
                        queueProcessors.add(newThread);
                        newThread.setPriority(Thread.MIN_PRIORITY);
                        newThread.start();}
                        catch (Exception ex) {
                            Log.err(log, "Started a new thread with index: " + threadToCheck.threadIndex);
                        }
                    }
                }
            } while (true);
        });
        startQueueProzessors();
        queueProcessorAliveCheck.start();
    }

    private void startQueueProzessors() {
        for (threadIndex = 0; threadIndex < PROCESSOR_COUNT; threadIndex++) {
            MultiThreadQueueProcessor thread = new MultiThreadQueueProcessor(queueData, threadIndex);
            queueProcessors.add(thread);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    int queuedTilesSize() {
        return queueData.wantedTiles.size();
    }

    int getNumberOfLoadedTiles() {
        return queueData.loadedTiles.getNumberOfLoadedTiles();
    }

    public void loadTiles(MapViewBase mapView, Descriptor upperLeftTile, Descriptor lowerRightTile, int aktZoom) {

        clearOrderQueue(); // perhaps new orders
        queueData.loadedTilesLock.lock();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
        }

        CB_List<Descriptor> wantedTiles = new CB_List<>();
        for (int i = upperLeftTile.getX(); i <= lowerRightTile.getX(); i++) {
            for (int j = upperLeftTile.getY(); j <= lowerRightTile.getY(); j++) {
                // attention this new Descriptor can not be compared with previous one in  corresponding lists
                Descriptor descriptor = new Descriptor(i, j, aktZoom);
                descriptor.Data = mapView;
                wantedTiles.add(descriptor);
            }
        }

        for (Descriptor descriptor : wantedTiles) {
            if (queueData.loadedTiles.containsKey(descriptor.getHashCode())) {
                Log.trace(log, "loaded: " + descriptor + " Age: " + queueData.loadedTiles.get(descriptor.getHashCode()).age);
                if (queueData.wantedTiles.containsKey(descriptor.getHashCode())) {
                    // should never happen! did only add descriptors, that are not in loaded tiles, which are locked
                    Log.err(log, descriptor + " already loaded. Should not be in queuedTiles");
                    queueData.wantedTiles.remove(descriptor.getHashCode());
                }
            } else {
                if (!queueData.wantedTiles.containsKey(descriptor.getHashCode())) {
                    Log.trace(log, "wanted: " + descriptor);
                    queueData.wantedTiles.put(descriptor.getHashCode(), descriptor);
                } else {
                    Log.err(log, "already in wanted Tiles" + descriptor);
                }
            }

            if (queueData.currentOverlayLayer != null) {
                if (queueData.loadedOverlayTiles.containsKey(descriptor.getHashCode())) {
                    continue;
                }
                if (queueData.wantedOverlayTiles.containsKey(descriptor.getHashCode()))
                    continue;
                if (!queueData.wantedOverlayTiles.containsKey(descriptor.getHashCode()))
                    queueData.wantedOverlayTiles.put(descriptor.getHashCode(), descriptor);
            }
        }

        try {
            queueData.queuedTilesLock.unlock();
            queueData.loadedTilesLock.unlock();
            if (queueData.currentOverlayLayer != null) {
                queueData.queuedOverlayTilesLock.unlock();
                queueData.loadedOverlayTilesLock.unlock();
            }
        } catch (Exception ignored) {
        }

        for (MultiThreadQueueProcessor thread : queueProcessors) {
            thread.interrupt();
        }

    }

    private void releaseOrderQueue() {
        queueData.queuedTilesLock.unlock();
        if (queueData.currentOverlayLayer != null) {
            queueData.queuedOverlayTilesLock.unlock();
        }
    }

    private void clearOrderQueue() {
        queueData.queuedTilesLock.lock();
        if (queueData.currentOverlayLayer != null) {
            queueData.queuedOverlayTilesLock.lock();
        }
        // clear Queue, to remove not yet loaded (previously needed) tiles
        // don't use clear because  of the mapview.
        // Only remove descriptors for this mapview (map,compass,track, ?)
        ArrayList<Descriptor> toDeleteOfThisMapView = new ArrayList<>();
        for (Descriptor desc : queueData.wantedTiles.values()) {
            // if (desc.Data == mapView) {
            toDeleteOfThisMapView.add(desc);
            // }
        }

        for (Descriptor desc : toDeleteOfThisMapView) {
            queueData.wantedTiles.remove(desc.getHashCode());
        }

        if (queueData.currentOverlayLayer != null) {
            toDeleteOfThisMapView.clear();
            for (Descriptor desc : queueData.wantedOverlayTiles.values()) {
                // if (desc.Data == mapView) {
                toDeleteOfThisMapView.add(desc);
                // }
            }
            for (Descriptor desc : toDeleteOfThisMapView) {
                queueData.wantedOverlayTiles.remove(desc.getHashCode());
            }
        }
    }

    public void setMaxNumTiles(int maxNumTiles2) {
        if (maxNumTiles2 > maxNumTiles)
            maxNumTiles = maxNumTiles2;
        queueData.setLoadedTilesCacheCapacity(maxNumTiles);
    }

    void clearLoadedTiles() {
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

    public int getTilesToDrawCounter() {
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

    public void sortByAge() {
        queueData.loadedTiles.sortByAge();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTiles.sortByAge();
        }
    }

    public Layer getCurrentLayer() {
        return queueData.currentLayer;
    }

    public boolean setCurrentLayer(Layer layer) {
        if (layer != queueData.currentLayer) {
            clearLoadedTiles();
            queueData.currentLayer = layer;
            return true;
        }
        return false;
    }

    public void changeCurrentLayer(Layer layer) {
        clearLoadedTiles();
    }

    public Layer getCurrentOverlayLayer() {
        return queueData.currentOverlayLayer;
    }

    public void setCurrentOverlayLayer(Layer layer) {
        queueData.currentOverlayLayer = layer;
    }

    public int getCacheSize() {
        return queueData.loadedTiles.getCapacity();
    }

    void reloadTile(Descriptor desc) {
        // called only, if not in loaded tiles
        queueData.queuedTilesLock.lock();
        if (!queueData.wantedTiles.containsKey(desc.getHashCode())) {
            if (!queueData.wantedTiles.containsKey(desc.getHashCode()))
                queueData.wantedTiles.put(desc.getHashCode(), desc);
        }
        queueData.queuedTilesLock.unlock();
    }
}
