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

public class MapTileLoader {
    private static final String log = "MapTileLoader";
    static int PROCESSOR_COUNT; // == nr of threads for getting tiles
    private final QueueData queueData;
    private int threadIndex;
    private MultiThreadQueueProcessor[] queueProcessor;
    private Thread[] queueProcessorAliveCheck;
    private int maxNumTiles;
    private boolean isThreadPrioSet;
    private boolean allThreadsAreRunning;
    private int lastLoadingChangedCounter;

    MapTileLoader() {
        queueData = new QueueData();
        threadIndex = 0;
        maxNumTiles = 0;
        isThreadPrioSet = false;
        allThreadsAreRunning = false;
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        Log.info(log, "Number of processors: " + PROCESSOR_COUNT);
        queueProcessor = new MultiThreadQueueProcessor[PROCESSOR_COUNT];
        queueProcessorAliveCheck = new Thread[PROCESSOR_COUNT];
        initialize(); // first initialize only one thread(MultiThreadQueueProcessor)
        lastLoadingChangedCounter = -1;
    }

    private void initialize() {
        if (threadIndex < PROCESSOR_COUNT) {
            queueProcessor[threadIndex] = new MultiThreadQueueProcessor(queueData, threadIndex);
            queueProcessor[threadIndex].setPriority(Thread.NORM_PRIORITY);
            queueProcessor[threadIndex].start();
            startAliveCheck(threadIndex);
            threadIndex++;
        }
    }

    private void startAliveCheck(final int index) {

        queueProcessorAliveCheck[index] = new Thread(() -> {
            do {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }

                if (!queueProcessor[index].isAlive()) {
                    queueProcessor[index] = new MultiThreadQueueProcessor(queueData, index);
                    queueProcessor[index].setPriority(Thread.MIN_PRIORITY);
                    queueProcessor[index].start();
                }
            } while (true);
        });

        queueProcessorAliveCheck[index].setPriority(Thread.MIN_PRIORITY);
        queueProcessorAliveCheck[index].start();
    }

    int queuedTilesSize() {
        return queueData.queuedTiles.size();
    }

    int getNumberOfLoadedTiles() {
        return queueData.loadedTiles.getNumberOfLoadedTiles();
    }

    public void loadTiles(MapViewBase mapView, Descriptor upperLeftTile, Descriptor lowerRightTile, int aktZoom) {

        if (!allThreadsAreRunning) {
            if (threadIndex < PROCESSOR_COUNT && threadIndex > 0) {
                queueProcessor[threadIndex - 1].setPriority(Thread.MIN_PRIORITY);
                initialize();
            } else if (threadIndex >= PROCESSOR_COUNT && !isThreadPrioSet) {
                for (int i = 0; i < PROCESSOR_COUNT; i++) {
                    queueProcessor[i].setPriority(Thread.MIN_PRIORITY);
                    isThreadPrioSet = true;
                    allThreadsAreRunning = true;
                }
            }
        }

        queueData.loadedTilesLock.lock();
        queueData.queuedTilesLock.lock();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.queuedOverlayTilesLock.lock();
        }
        // clear Queue, to remove not yet loaded (previously needed) tiles
        // don't use clear because  of the mapview.
        // Only remove descriptors for this mapview (map,compass,track, ?)
        ArrayList<Descriptor> toDelete = new ArrayList<>();
        for (Descriptor desc : queueData.queuedTiles.values()) {
            if (desc.Data == mapView) {
                toDelete.add(desc);
            }
        }

        for (Descriptor desc : toDelete) {
            queueData.queuedTiles.remove(desc.getHashCode());
        }

        if (queueData.currentOverlayLayer != null) {
            toDelete.clear();
            for (Descriptor desc : queueData.queuedOverlayTiles.values()) {
                if (desc.Data == mapView) {
                    toDelete.add(desc);
                }
            }
            for (Descriptor desc : toDelete) {
                queueData.queuedOverlayTiles.remove(desc.getHashCode());
            }
        }

        CB_List<Descriptor> wantedTiles = new CB_List<>();
        for (int i = upperLeftTile.getX(); i <= lowerRightTile.getX(); i++) {
            for (int j = upperLeftTile.getY(); j <= lowerRightTile.getY(); j++) {
                Descriptor descriptor = new Descriptor(i, j, aktZoom);
                descriptor.Data = mapView;
                wantedTiles.add(descriptor);
            }
        }

        for (int i = 0, n = wantedTiles.size(); i < n; i++) {
            Descriptor desc = wantedTiles.get(i);
            if (!queueData.loadedTiles.containsKey(desc.getHashCode())) {
                if (!queueData.queuedTiles.containsKey(desc.getHashCode())) {
                    queueData.queuedTiles.put(desc.getHashCode(), desc);
                }
            } else if (queueData.queuedTiles.containsKey(desc.getHashCode())) {
                // should never happen! did only add descriptors, that are not in loaded tiles, which are locked
                Log.err(log, desc + " already loaded. Should not be in queuedTiles");
                queueData.queuedTiles.remove(desc.getHashCode());
            }

            if (queueData.currentOverlayLayer != null) {
                if (queueData.loadedOverlayTiles.containsKey(desc.getHashCode())) {
                    continue;
                }
                if (queueData.queuedOverlayTiles.containsKey(desc.getHashCode()))
                    continue;
                if (!queueData.queuedOverlayTiles.containsKey(desc.getHashCode()))
                    queueData.queuedOverlayTiles.put(desc.getHashCode(), desc);
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
        for (int i = 0; i < PROCESSOR_COUNT; i++) {
            if (queueProcessor[i] != null)
                queueProcessor[i].interrupt();
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

    boolean markTilesToDraw(Descriptor desc) {
        return queueData.loadedTiles.markTilesToDraw(desc.getHashCode());
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

    public void setCurrentLayer(Layer layer) {
        queueData.currentLayer = layer;
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
        if (!queueData.queuedTiles.containsKey(desc.getHashCode())) {
            if (!queueData.queuedTiles.containsKey(desc.getHashCode()))
                queueData.queuedTiles.put(desc.getHashCode(), desc);
        }
        queueData.queuedTilesLock.unlock();
    }

    public boolean isLoadingChanged() {
        if (queueData.loadedTiles.changeCounter == lastLoadingChangedCounter) {
            return false;
        }
        else {
            lastLoadingChangedCounter = queueData.loadedTiles.changeCounter;
            return true;
        }
    }
}
