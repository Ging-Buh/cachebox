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

package de.droidcachebox.core;

import static de.droidcachebox.settings.AllSettings.liveRadius;
import static de.droidcachebox.settings.AllSettings.tileCacheFolder;
import static de.droidcachebox.settings.AllSettings.tileCacheFolderLocal;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.database.CBDB;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.LoopThread;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class LiveMapQue {
    private static final String sClass = "LiveMapQue";
    private static final String LIVE_CACHE_NAME = "Live_Request";
    private static final String LIVE_CACHE_EXTENSION = ".txt";
    private static final byte DEFAULT_ZOOM_14 = 14;
    // private static final int MAX_REQUEST_CACHE_RADIUS_14 = 1060;
    private static final byte DEFAULT_ZOOM_13 = 13;
    // private static final int MAX_REQUEST_CACHE_RADIUS_13 = 2120;
    private static final int MAX_REQUEST_CACHE_COUNT = 50; //
    private static LiveMapQue liveMapQue;

    private final Array<Descriptor> descriptorStack;
    private final AtomicBoolean downloadIsActive;
    private Descriptor lastLo, lastRu;
    private Byte count = 0;
    private CacheListLive cacheListLive;
    private AllSettings.Live_Radius radius;
    private byte usedZoom;
    private GpxFilename gpxFilename;
    private final LoopThread loopThread = new LoopThread(2000) {

        protected boolean cancelLoop() {
            boolean cancel = descriptorStack.isEmpty();
            if (cancel) {
                new Thread(() -> {
                    CacheListChangedListeners.getInstance().fire(sClass + " cancelLoop");
                }).start();
                Log.debug(sClass, "cancel loop thread");
            }
            return cancel;
        }

        protected void loop() {
            if (downloadIsActive.get()) return; // only one download at a time
            // Log.debug(sClass, "download for one descriptor per loop");
            GL.that.postAsync(() -> {

                Descriptor descriptor;
                do {
                    // ? only use, if on screen (ShowMap.getInstance().normalMapView.center / descriptor)
                    // ? perhaps dont' use lastIn but sort on distance
                    if (descriptorStack.notEmpty()) descriptor = descriptorStack.pop();
                    else descriptor = null;
                } while (descriptor != null && cacheListLive.contains(descriptor));

                if (descriptor != null) {
                    Log.debug(sClass, "Download caches from " + descriptor);

                    downloadIsActive.set(true);
                    int request_radius = usedZoom == DEFAULT_ZOOM_14 ? 1300 : 2600;
                    /*
                    double lon1 = DEG_RAD * tileXToLongitude(descriptor.getZoom(), descriptor.getX());
                    double lat1 = DEG_RAD * tileYToLatitude(descriptor.getZoom(), descriptor.getY());
                    double lon2 = DEG_RAD * tileXToLongitude(descriptor.getZoom(), descriptor.getX() + 1);
                    double lat2 = DEG_RAD * tileYToLatitude(descriptor.getZoom(), descriptor.getY() + 1);
                    request_radius = (int) (WGS84_MAJOR_AXIS * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos((lon2 - lon1))) / 2 + 0.5); // round
                    */

                    GroundspeakAPI.Query q = new GroundspeakAPI.Query()
                            .setMaxToFetch(MAX_REQUEST_CACHE_COUNT)
                            .setDescriptor(descriptor)
                            .searchInCircle(descriptor.getCenterCoordinate(), request_radius);
                    if (AllSettings.liveExcludeFounds.getValue()) q.excludeFinds();
                    if (AllSettings.liveExcludeOwn.getValue()) q.excludeOwn();
                    q.resultWithLiteFields();
                    // todo change to Array<Cache> to make copy from arraylist to array unnecessary
                    ArrayList<GroundspeakAPI.GeoCacheRelated> apiCaches = null;
                    if (FileIO.fileExistsMaxAge(getLocalCachePath(descriptor), AllSettings.liveCacheTime.getEnumValue().getLifetime())) {
                        apiCaches = loadDescLiveFromCache(q);
                    }
                    if (apiCaches == null) {
                        if (gpxFilename == null) {
                            Category category = CoreData.categories.getCategory("API-Import");
                            gpxFilename = category.addGpxFilename("API-Import");
                        }
                        apiCaches = GroundspeakAPI.searchGeoCaches(q);
                    }

                    Array<Cache> tmp = new Array<>();
                    for (GroundspeakAPI.GeoCacheRelated c : apiCaches)
                        tmp.add(c.cache); // todo remove (is only to copy from Arraylist to Array)
                    final Array<Cache> geoCachesToRemove = cacheListLive.addAndReduce(descriptor, tmp);
                    if (geoCachesToRemove == null) {
                        Log.err(sClass, "descriptor already in cachelistLive: should not happen here!");
                    } else {
                        if (geoCachesToRemove.size > 0) {
                            new Thread(() -> {
                                synchronized (CBDB.cacheList) {
                                    // todo reuse removeAll
                                    for (Cache geoCache : geoCachesToRemove) {
                                        CBDB.cacheList.remove(geoCache);
                                    }
                                }
                            }).start();
                        }
                        if (cacheListLive.getNoOfGeoCachesForDescriptor(descriptor) > 0 || geoCachesToRemove.size > 0) {
                            CacheListChangedListeners.getInstance().fire(sClass + "loop");
                        }
                    }
                    downloadIsActive.set(false);
                } else {
                    Log.debug(sClass, "no descriptor for download");
                }
            });
        }
    };

    private LiveMapQue() {
        descriptorStack = new Array<>();
        downloadIsActive = new AtomicBoolean(false);

        liveRadius.addSettingChangedListener(() -> {
            radius = liveRadius.getEnumValue();
            if (radius == AllSettings.Live_Radius.Zoom_13) {
                usedZoom = DEFAULT_ZOOM_13;
            } else {
                usedZoom = DEFAULT_ZOOM_14;
            }
        });

        radius = liveRadius.getEnumValue();

        if (radius == AllSettings.Live_Radius.Zoom_13) {
            usedZoom = DEFAULT_ZOOM_13;
        } else {
            usedZoom = DEFAULT_ZOOM_14;
        }

        cacheListLive = new CacheListLive(AllSettings.liveMaxCount.getValue(), usedZoom);
        AllSettings.liveMaxCount.addSettingChangedListener(() -> cacheListLive = new CacheListLive(AllSettings.liveMaxCount.getValue(), usedZoom));

        Log.debug(sClass, "A new LiveMapQue");

    }

    public static LiveMapQue getInstance() {
        if (liveMapQue == null) liveMapQue = new LiveMapQue();
        return liveMapQue;
    }

    private ArrayList<GroundspeakAPI.GeoCacheRelated> loadDescLiveFromCache(GroundspeakAPI.Query query) {
        String path = getLocalCachePath(query.getDescriptor());
        String result;
        FileHandle fh = new FileHandle(path);
        try {
            BufferedReader br = fh.reader(1000);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            result = sb.toString();
            br.close();
            if (result.length() > 0) {
                JSONArray json = (JSONArray) new JSONTokener(result).nextValue();
                if (gpxFilename == null) {
                    Category category = CoreData.categories.getCategory("API-Import");
                    gpxFilename = category.addGpxFilename("API-Import");
                }
                return GroundspeakAPI.getGeoCacheRelateds(json, query.getFields(), null);
            }
        } catch (Exception e) {
            Log.err("LiveMapQue", "loadDescLiveFromCache", e);
        }
        return null;
    }

    public String getLocalCachePath(Descriptor desc) {
        if (desc == null)
            return "";
        String sTileCacheFolder = tileCacheFolder.getValue();
        if (tileCacheFolderLocal.getValue().length() > 0)
            sTileCacheFolder = tileCacheFolderLocal.getValue();
        return sTileCacheFolder + "/" + LIVE_CACHE_NAME + "/" + desc.getZoom() + "/" + desc.getX() + "/" + desc.getY() + LIVE_CACHE_EXTENSION;
    }

    public void quePosition(Coordinate coord) {
        if (!AllSettings.disableLiveMap.getValue()) {
            if (coord != null && coord.isValid()) {
                final Descriptor descriptor = new Descriptor(coord, usedZoom);
                if (cacheListLive.contains(descriptor)) {
                    Log.trace(sClass, "Live caches for " + descriptor + " already there.");
                } else {
                    if (!GroundspeakAPI.isDownloadLimitExceeded()) {
                        if (!descriptorStack.contains(descriptor, false)) {
                            descriptorStack.add(descriptor);
                            Log.debug(sClass, "Add " + descriptor + " to download stack. (" + descriptorStack.size + ")");
                        }
                        loopThread.start();
                    }
                }
            }
        }
    }

    public void queScreen(Descriptor lo, Descriptor ru) {
        // get geocaches for complete screen, only called if in car-mode
        if (GroundspeakAPI.isDownloadLimitExceeded())
            return;

        // check last request don't Double!
        if (lastLo != null && lastRu != null) {
            // all Descriptor are into the last request?
            if (lastLo.getX() == lo.getX() && lastRu.getX() == ru.getX() &&
                    lastLo.getY() == lo.getY() && lastRu.getY() == ru.getY()) {
                // Still run every 15th!
                if (count++ < 15)
                    return;
                count = 0;
            }

        }

        lastLo = lo;
        lastRu = ru;

        Array<Descriptor> descList = new Array<>();
        for (int i = lo.getX(); i <= ru.getX(); i++) {
            for (int j = lo.getY(); j <= ru.getY(); j++) {
                Descriptor desc = new Descriptor(i, j, lo.getZoom());

                Array<Descriptor> descAddList = adjustZoom(desc);

                for (int k = 0; k < descAddList.size; k++) {
                    if (!descList.contains(descAddList.get(k), false))
                        descList.add(descAddList.get(k));
                }
            }
        }

        // remove descriptors that are already in cacheListLive
        Set<Long> alreadyThere = cacheListLive.getDescriptorsHashCodes();
        for (Descriptor descriptor : descList) {
            if (alreadyThere.contains(descriptor.getHashCode())) {
                descList.removeValue(descriptor, false);
                Log.debug(sClass, "removed " + descriptor);
            }
        }

        synchronized (descriptorStack) {
            descriptorStack.clear();
            descriptorStack.addAll(descList);
            if ((lo.getData() != null) && (lo.getData() instanceof Coordinate)) {
                Coordinate center = (Coordinate) lo.getData();
                if (center != null) {
                    final Descriptor mapCenterDesc = new Descriptor(center, lo.getZoom());
                    descriptorStack.sort((item1, item2) -> Integer.compare(item1.getDistance(mapCenterDesc), item2.getDistance(mapCenterDesc)));
                }
            }
        }

        loopThread.start();

    }

    private Array<Descriptor> adjustZoom(Descriptor descriptor) {
        int zoomDiff = usedZoom - descriptor.getZoom();
        int pow = (int) Math.pow(2, Math.abs(zoomDiff));
        Array<Descriptor> ret = new Array<>();
        if (zoomDiff > 0) {
            Descriptor def = new Descriptor(descriptor.getX() * pow, descriptor.getY() * pow, usedZoom);
            int count = pow / 2;
            for (int i = 0; i <= count; i++) {
                for (int j = 0; j <= count; j++) {
                    ret.add(new Descriptor(def.getX() + i, def.getY() + j, usedZoom));
                }
            }
        } else {
            ret.add(new Descriptor(descriptor.getX() / pow, descriptor.getY() / pow, usedZoom));
        }
        return ret;
    }

    public void setCenterDescriptor(CoordinateGPS center) {
        cacheListLive.setCenterDescriptor(new Descriptor(center, usedZoom));
    }

    public boolean getDownloadIsActive() {
        return !descriptorStack.isEmpty();
    }

    public Collection<Array<Cache>> getAllCacheLists() {
        return cacheListLive.getAllCacheLists();
    }

    public void clearDescriptorStack() {
        descriptorStack.clear();
    }
}
