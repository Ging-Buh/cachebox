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
import java.util.SortedMap;
import java.util.concurrent.locks.Lock;

/**
 * @author ging-buh
 * @author Longri
 */
public class MapTileLoader {
    private static final String log = "MapTileLoader";
    private final QueueData queueData;
    private CB_List<Long> neadedTiles;
    private int queueProcessorIndex;
    private MultiThreadQueueProcessor[] queueProcessor;
    private Thread[] queueProcessorAliveCheck;
    private int maxNumTiles;
    private boolean isThreadPrioSet;
    private boolean CombleadInitial;
    private long lastLoadHash;

    MapTileLoader() {
        super();
        queueData = new QueueData();
        neadedTiles = new CB_List<>();
        queueProcessorIndex = 0;
        maxNumTiles = 0;
        isThreadPrioSet = false;
        CombleadInitial = false;
        lastLoadHash = 0;
        queueProcessor = new MultiThreadQueueProcessor[ManagerBase.PROCESSOR_COUNT];
        queueProcessorAliveCheck = new Thread[ManagerBase.PROCESSOR_COUNT];
        initialize(Thread.NORM_PRIORITY); // first initialize only one thread(MultiThreadQueueProcessor)
    }

    private void initialize(int ThreadPriority) {
        if (queueProcessorIndex < ManagerBase.PROCESSOR_COUNT) {
            Log.info(log, "Start queueProcessor(thread) number " + queueProcessorIndex);
            queueProcessor[queueProcessorIndex] = new MultiThreadQueueProcessor(queueData, queueProcessorIndex);
            queueProcessor[queueProcessorIndex].setPriority(ThreadPriority);
            queueProcessor[queueProcessorIndex].start();
            startAliveCheck(queueProcessorIndex);
            queueProcessorIndex++;
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
                    Log.err(log, "MapTileLoader Restart queueProcessor[" + index + "]");
                    queueProcessor[index] = new MultiThreadQueueProcessor(queueData, index);
                    queueProcessor[index].setPriority(Thread.NORM_PRIORITY);
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

    public void loadTiles(MapViewBase mapView, Descriptor lo, Descriptor ru, int aktZoom) {

        long hash = lo.GetHashCode() * ru.GetHashCode();
        if (lastLoadHash == hash)
            return; // we have loaded!
        lastLoadHash = hash;

        // Initial Threads?
        if (!CombleadInitial) {
            if (queueProcessorIndex < ManagerBase.PROCESSOR_COUNT && queueProcessorIndex > 0 && queueData.loadedTiles.size() > 1) {
                initialize(Thread.NORM_PRIORITY);
            } else if (queueProcessorIndex >= ManagerBase.PROCESSOR_COUNT && !isThreadPrioSet) {
                for (int i = 0; i < ManagerBase.PROCESSOR_COUNT; i++) {
                    queueProcessor[i].setPriority(Thread.NORM_PRIORITY);
                    isThreadPrioSet = true;
                    CombleadInitial = true;
                }
            }
        }

        // {// DEBUG
        //
        // String tre = String.valueOf(((queueProcessor == null) ? 0 : queueProcessor.length));
        // String text = "Threads:" + tre + " | MaxCache:" + maxNumTiles + " " + " loaded:" + queueData.loadedTiles.size() + " life:"
        // + TileGL_Bmp.LifeCount;
        // GL.MaptileLoaderDebugString = text;
        // }

        if (ManagerBase.manager == null)
            return; // Kann nichts laden, wenn der Manager Null ist!

        // alle notwendigen Tiles zum Laden einstellen in die Queue

        queueData.loadedTilesLock.lock();
        queueData.queuedTilesLock.lock();
        if (queueData.CurrentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.queuedOverlayTilesLock.lock();
        }
        // Queue jedesmal l�schen, damit die Tiles, die eigentlich
        // mal
        // gebraucht wurden aber trotzdem noch nicht geladen sind
        // auch nicht mehr geladen werden
        // dabei aber die MapView ber�cksichtigen, die die queuedTiles angefordert hat
        // queuedTiles.clear();
        ArrayList<Descriptor> toDelete = new ArrayList<>();
        for (Descriptor desc : queueData.queuedTiles.values()) {
            if (desc.Data == mapView) {
                toDelete.add(desc);
            }
        }
        for (Descriptor desc : toDelete) {
            queueData.queuedTiles.remove(desc.GetHashCode());
        }
        if (queueData.CurrentOverlayLayer != null) {
            toDelete.clear();
            for (Descriptor desc : queueData.queuedOverlayTiles.values()) {
                if (desc.Data == mapView) {
                    toDelete.add(desc);
                }
            }
            for (Descriptor desc : toDelete) {
                queueData.queuedOverlayTiles.remove(desc.GetHashCode());
            }
        }

        CB_List<Descriptor> trueZoomDescList = new CB_List<>();
        // // CB_List<Descriptor> biggerZoomDescList = new CB_List<Descriptor>();
        //
        for (int i = lo.getX(); i <= ru.getX(); i++) {
            for (int j = lo.getY(); j <= ru.getY(); j++) {
                Descriptor descriptor = new Descriptor(i, j, aktZoom, lo.NightMode);

                // remember which mapView ordered this descriptor
                descriptor.Data = mapView;

                trueZoomDescList.add(descriptor);
                neadedTiles.add(descriptor.GetHashCode());

            }
        }

        // then true zoom level
        for (int i = 0, n = trueZoomDescList.size(); i < n; i++) {
            Descriptor desc = trueZoomDescList.get(i);
            if (!queueData.loadedTiles.containsKey(desc.GetHashCode())) {
                if (!queueData.queuedTiles.containsKey(desc.GetHashCode())) {
                    queueTile(desc, queueData.queuedTiles, queueData.queuedTilesLock);
                }
            } else if (queueData.queuedTiles.containsKey(desc.GetHashCode())) {
                queueData.queuedTiles.remove(desc.GetHashCode());
            }

            if (queueData.CurrentOverlayLayer != null) {
                if (queueData.loadedOverlayTiles.containsKey(desc.GetHashCode())) {
                    continue;
                }
                if (queueData.queuedOverlayTiles.containsKey(desc.GetHashCode()))
                    continue;
                queueTile(desc, queueData.queuedOverlayTiles, queueData.queuedOverlayTilesLock);
            }
        }

        try {
            queueData.queuedTilesLock.unlock();
            queueData.loadedTilesLock.unlock();
            if (queueData.CurrentOverlayLayer != null) {
                queueData.queuedOverlayTilesLock.unlock();
                queueData.loadedOverlayTilesLock.unlock();
            }
        } catch (Exception e) {
        }
        neadedTiles.truncate(0);
    }

    int numLoadedTiles() {
        return queueData.loadedTiles.size();
    }

    public void setMaxNumTiles(int maxNumTiles2) {
        if (maxNumTiles2 > maxNumTiles)
            maxNumTiles = maxNumTiles2;
        queueData.setLoadedTilesCacheCapacity(maxNumTiles);
    }

    private void queueTile(Descriptor desc, SortedMap<Long, Descriptor> queuedTiles, Lock queuedTilesLock) {
        queuedTilesLock.lock();
        try {
            if (queuedTiles.containsKey(desc.GetHashCode()))
                return;

            queuedTiles.put(desc.GetHashCode(), desc);
        } finally {
            queuedTilesLock.unlock();
        }

    }

    public void clearLoadedTiles() {
        lastLoadHash = 0;
        queueData.loadedTilesLock.lock();
        queueData.loadedTiles.clear();
        queueData.loadedTilesLock.unlock();

        if (queueData.CurrentOverlayLayer != null) {
            queueData.loadedOverlayTilesLock.lock();
            queueData.loadedOverlayTiles.clear();
            queueData.loadedOverlayTilesLock.unlock();

        }
    }

    public void increaseLoadedTilesAge() {
        // das Alter aller Tiles um 1 erh�hen
        queueData.loadedTiles.increaseLoadedTilesAge();

        if (queueData.CurrentOverlayLayer != null) {
            queueData.loadedOverlayTiles.increaseLoadedTilesAge();
        }
    }

    public TileGL getLoadedTile(Descriptor desc) {
        return queueData.loadedTiles.get(desc.GetHashCode());
    }

    public boolean markToDraw(Descriptor desc) {
        return queueData.loadedTiles.markToDraw(desc.GetHashCode());
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
        if (queueData.CurrentOverlayLayer != null) {
            queueData.loadedOverlayTiles.sort();
        }
    }

    public TileGL getLoadedOverlayTile(Descriptor desc) {
        // Overlay Tiles liefern
        if (queueData.CurrentOverlayLayer == null) {
            return null;
        }
        return queueData.loadedOverlayTiles.get(desc.GetHashCode());
    }

    public boolean markToDrawOverlay(Descriptor desc) {
        return queueData.loadedOverlayTiles.markToDraw(desc.GetHashCode());
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
        return queueData.CurrentLayer;
    }

    public void setCurrentLayer(Layer layer) {
        queueData.CurrentLayer = layer;
    }

    public Layer getCurrentOverlayLayer() {
        return queueData.CurrentOverlayLayer;
    }

    // #######################################################################################################
    // Static

    public void setCurrentOverlayLayer(Layer layer) {
        queueData.CurrentOverlayLayer = layer;
    }

    public int getCacheSize() {
        return queueData.loadedTiles.getCapacity();
    }

    public void reloadTile(MapViewBase mapViewBase, Descriptor desc, int aktZoom) {
        // queue only if no Tile on Work
        if (queueData.queuedTiles.size() != 0)
            return;

        queueData.loadedTilesLock.lock();
        queueData.queuedTilesLock.lock();

        if (!queueData.loadedTiles.containsKey(desc.GetHashCode())) {
            if (!queueData.queuedTiles.containsKey(desc.GetHashCode())) {
                queueTile(desc, queueData.queuedTiles, queueData.queuedTilesLock);
            }
        }
        queueData.loadedTilesLock.unlock();
        queueData.queuedTilesLock.unlock();
    }

}
