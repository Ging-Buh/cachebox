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

import static de.droidcachebox.settings.AllSettings.ParkingLatitude;
import static de.droidcachebox.settings.AllSettings.ParkingLongitude;

import com.badlogic.gdx.utils.Array;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.Energy;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.utils.log.Log;

public class CacheListChangedListeners extends CopyOnWriteArrayList<CacheListChangedListeners.CacheListChangedListener> {
    private static CacheListChangedListeners cacheListChangedListeners;
    private static Thread threadCall;
    private static AtomicBoolean threadActive;

    public static CacheListChangedListeners getInstance() {
        if (cacheListChangedListeners == null) {
            cacheListChangedListeners = new CacheListChangedListeners();
            threadActive = new AtomicBoolean(false);
        }
        return cacheListChangedListeners;
    }

    public boolean addListener(CacheListChangedListener listener) {
        if (!contains(listener)) {
            super.add(listener);
            Log.info("CacheListChangedListeners", "add (" + this.size() + ") " + listener.toString());
            return true;
        }
        else
            return false;
    }

    public void removeListener(CacheListChangedListener listener) {
        super.remove(listener);
        Log.info("CacheListChangedListeners", "removed (" + this.size() + ") " + listener.toString());
    }

    public void fire(String caller) {
        Log.info("CacheListChangedListeners", "fired by " + caller);
        if (Energy.isDisplayOff())
            return;

        if (this.size() > 0) {

            if (threadCall != null) {
                // Log.info("CacheListChangedListeners", "still running (boolean): " + threadActive.get());
                if (threadCall.getState() != Thread.State.TERMINATED) {
                    // Log.info("CacheListChangedListeners", "still running");
                    return;
                }
                else {
                    threadCall = null;
                }
            }

            synchronized (CBDB.cacheList) {

                // remove Parking Cache
                Cache cache = CBDB.cacheList.getCacheByGcCodeFromCacheList("CBPark");
                if (cache != null)
                    CBDB.cacheList.remove(cache);
                // add Parking Cache from saved Config (ParkingLatitude, ParkingLongitude)
                if (ParkingLatitude.getValue() != 0) {
                    cache = new Cache(ParkingLatitude.getValue(), ParkingLongitude.getValue(), "My Parking area", GeoCacheType.MyParking, "CBPark");
                    CBDB.cacheList.add(0, cache);
                }

                synchronized (LiveMapQue.getInstance().getAllCacheLists()) {
                    for (Array<Cache> geoCacheList : LiveMapQue.getInstance().getAllCacheLists()) {
                        for (Cache geoCache : geoCacheList) {
                            if (geoCache != null) {
                                if (FilterInstances.isLastFilterSet()) {
                                    if (!CBDB.cacheList.contains(geoCache)) {
                                        if (FilterInstances.getLastFilter().passed(geoCache)) {
                                            geoCache.setLive(true);
                                            CBDB.cacheList.add(geoCache);
                                        }
                                    }
                                } else {
                                    if (!CBDB.cacheList.contains(geoCache)) {
                                        geoCache.setLive(true);
                                        CBDB.cacheList.add(geoCache);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            threadCall = new Thread(() -> {
                for (CacheListChangedListener listener : this) {
                    if (listener != null) {
                        // Log.info("handle changed list of geocaches in ", listener.toString());
                        listener.cacheListChanged();
                    }
                }
                threadActive.set(false);
            });
            threadActive.set(true);
            threadCall.start();
        }
    }

    public interface CacheListChangedListener {
        void cacheListChanged();
    }
}
