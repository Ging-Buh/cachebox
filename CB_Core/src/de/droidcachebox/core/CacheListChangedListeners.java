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

import de.droidcachebox.Energy;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.GeoCacheType;

import java.util.concurrent.CopyOnWriteArrayList;

public class CacheListChangedListeners extends CopyOnWriteArrayList<CacheListChangedListeners.CacheListChangedListener> {
    private static CacheListChangedListeners cacheListChangedListeners;
    private static Thread threadCall;

    public static CacheListChangedListeners getInstance() {
        if (cacheListChangedListeners == null)
            cacheListChangedListeners = new CacheListChangedListeners();
        return cacheListChangedListeners;
    }

    public boolean addListener(CacheListChangedListener listener) {
        if (!contains(listener))
            return super.add(listener);
        else
            return false;
    }

    public void removeListener(CacheListChangedListener listener) {
        super.remove(listener);
    }

    public void cacheListChanged() {
        if (Energy.isDisplayOff())
            return;

        synchronized (Database.Data.cacheList) {

            // remove Parking Cache
            Cache cache = Database.Data.cacheList.getCacheByGcCodeFromCacheList("CBPark");
            if (cache != null)
                Database.Data.cacheList.remove(cache);
            // add Parking Cache from saved Config (ParkingLatitude, ParkingLongitude)
            if (CB_Core_Settings.ParkingLatitude.getValue() != 0) {
                cache = new Cache(CB_Core_Settings.ParkingLatitude.getValue(), CB_Core_Settings.ParkingLongitude.getValue(), "My Parking area", GeoCacheType.MyParking, "CBPark");
                Database.Data.cacheList.add(0, cache);
            }

            // add all Live Caches
            for (int i = 0; i < LiveMapQue.LiveCaches.getSize(); i++) {
                Cache geoCache = LiveMapQue.LiveCaches.get(i);
                if (geoCache == null)
                    continue;
                if (FilterInstances.isLastFilterSet()) {
                    if (!Database.Data.cacheList.contains(geoCache)) {
                        if (FilterInstances.getLastFilter().passed(geoCache)) {
                            geoCache.setLive(true);
                            Database.Data.cacheList.add(geoCache);
                        }
                    }
                } else {
                    if (!Database.Data.cacheList.contains(geoCache)) {
                        geoCache.setLive(true);
                        Database.Data.cacheList.add(geoCache);
                    }
                }

            }
        }

        if (threadCall != null) {
            if (threadCall.getState() != Thread.State.TERMINATED)
                return;
            else
                threadCall = null;
        }

        threadCall = new Thread(() -> {
            for (CacheListChangedListener listener : this) {
                if (listener == null)
                    continue;
                listener.cacheListChanged();
            }
        });

        threadCall.start();
    }

    public interface CacheListChangedListener {
        void cacheListChanged();
    }
}
