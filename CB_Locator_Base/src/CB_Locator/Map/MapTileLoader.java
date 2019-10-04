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
    private final QueueData queueData;
    private int threadIndex;
    private MultiThreadQueueProcessor[] queueProcessor;
    private Thread[] queueProcessorAliveCheck;
    private int maxNumTiles;
    private boolean isThreadPrioSet;
    private boolean allThreadsAreRunning;

    MapTileLoader() {
        queueData = new QueueData();
        threadIndex = 0;
        maxNumTiles = 0;
        isThreadPrioSet = false;
        allThreadsAreRunning = false;
        queueProcessor = new MultiThreadQueueProcessor[ManagerBase.PROCESSOR_COUNT];
        queueProcessorAliveCheck = new Thread[ManagerBase.PROCESSOR_COUNT];
        initialize(Thread.NORM_PRIORITY); // first initialize only one thread(MultiThreadQueueProcessor)
    }

    private void initialize(int ThreadPriority) {
        if (threadIndex < ManagerBase.PROCESSOR_COUNT) {
            queueProcessor[threadIndex] = new MultiThreadQueueProcessor(queueData, threadIndex);
            queueProcessor[threadIndex].setPriority(ThreadPriority);
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

    public int loadedTilesSize() {
        return queueData.loadedTiles.size();
    }

    public void loadTiles(MapViewBase mapView, Descriptor upperLeftTile, Descriptor lowerRightTile, int aktZoom) {

        if (!allThreadsAreRunning) {
            if (threadIndex < ManagerBase.PROCESSOR_COUNT && threadIndex > 0) {
                queueProcessor[threadIndex - 1].setPriority(Thread.MIN_PRIORITY);
                initialize(Thread.NORM_PRIORITY);
            } else if (threadIndex >= ManagerBase.PROCESSOR_COUNT && !isThreadPrioSet) {
                for (int i = 0; i < ManagerBase.PROCESSOR_COUNT; i++) {
                    queueProcessor[i].setPriority(Thread.MIN_PRIORITY);
                    isThreadPrioSet = true;
                    allThreadsAreRunning = true;
                }
            }
        }

        if (ManagerBase.manager == null)
            return; // Kann nichts laden, wenn der Manager Null ist!

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
        for (int i = 0; i < ManagerBase.PROCESSOR_COUNT; i++) {
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

    public void increaseLoadedTilesAge() {
        queueData.loadedTilesLock.lock();
        queueData.loadedTiles.increaseLoadedTilesAge();
        queueData.loadedTilesLock.unlock();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.loadedOverlayTiles.increaseLoadedTilesAge();
            queueData.loadedOverlayTilesLock.unlock();
        }
    }

    public TileGL getLoadedTile(Descriptor desc) {
        return queueData.loadedTiles.get(desc.getHashCode());
    }

    public boolean markToDraw(Descriptor desc) {
        return queueData.loadedTiles.markToDraw(desc.getHashCode());
    }

    public int getDrawingSize() {
        return queueData.loadedTiles.DrawingSize();
    }

    public TileGL getDrawingTile(int i) {
        return queueData.loadedTiles.getDrawingTile(i);
    }

    public void clearDrawingTiles() {
        queueData.loadedTiles.clearDrawingList();
    }

    public void sort() {
        queueData.loadedTiles.sort();
        if (queueData.currentOverlayLayer != null) {
            queueData.loadedOverlayTiles.sort();
        }
    }

    public boolean markToDrawOverlay(Descriptor desc) {
        return queueData.loadedOverlayTiles.markToDraw(desc.getHashCode());
    }

    public int getDrawingSizeOverlay() {
        return queueData.loadedOverlayTiles.DrawingSize();
    }

    public TileGL getDrawingTileOverlay(int i) {
        return queueData.loadedOverlayTiles.getDrawingTile(i);
    }

    public void clearDrawingTilesOverlay() {
        queueData.loadedOverlayTiles.clearDrawingList();
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

    // #######################################################################################################
    // Static

    public void setCurrentOverlayLayer(Layer layer) {
        queueData.currentOverlayLayer = layer;
    }

    public int getCacheSize() {
        return queueData.loadedTiles.getCapacity();
    }

    public void reloadTile(Descriptor desc) {
        // queue only if no Tile on Work
        if (queueData.queuedTiles.size() != 0)
            return;
        queueData.loadedTilesLock.lock();
        queueData.queuedTilesLock.lock();
        if (!queueData.loadedTiles.containsKey(desc.getHashCode())) {
            if (!queueData.queuedTiles.containsKey(desc.getHashCode())) {
                if (!queueData.queuedTiles.containsKey(desc.getHashCode()))
                    queueData.queuedTiles.put(desc.getHashCode(), desc);
            }
        }
        queueData.queuedTilesLock.unlock();
        queueData.loadedTilesLock.unlock();
    }

}
