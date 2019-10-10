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
package CB_Core;

import CB_Core.Api.LiveMapQue;
import CB_Core.Types.Cache;
import CB_UI_Base.Energy;

import java.util.ArrayList;

/**
 * @author Longri
 */
public class CacheListChangedEventList {
    public static ArrayList<CacheListChangedEventListener> list = new ArrayList<>();
    private static Thread threadCall;

    public static void Add(CacheListChangedEventListener event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void Remove(CacheListChangedEventListener event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static void Call() {
        if (Energy.isDisplayOff())
            return;

        synchronized (Database.Data.cacheList) {
            Cache cache = Database.Data.cacheList.getCacheByGcCodeFromCacheList("CBPark");

            if (cache != null)
                Database.Data.cacheList.remove(cache);

            // add Parking Cache
            if (CB_Core_Settings.ParkingLatitude.getValue() != 0) {
                cache = new Cache(CB_Core_Settings.ParkingLatitude.getValue(), CB_Core_Settings.ParkingLongitude.getValue(), "My Parking area", CacheTypes.MyParking, "CBPark");
                Database.Data.cacheList.add(0, cache);
            }

            // add all Live Caches
            for (int i = 0; i < LiveMapQue.LiveCaches.getSize(); i++) {
                if (FilterInstances.isLastFilterSet()) {
                    Cache ca = LiveMapQue.LiveCaches.get(i);
                    if (ca == null)
                        continue;
                    if (!Database.Data.cacheList.contains(ca)) {
                        if (FilterInstances.getLastFilter().passed(ca)) {
                            ca.setLive(true);
                            Database.Data.cacheList.add(ca);
                        }
                    }
                } else {
                    Cache ca = LiveMapQue.LiveCaches.get(i);
                    if (ca == null)
                        continue;
                    if (!Database.Data.cacheList.contains(ca)) {
                        ca.setLive(true);
                        Database.Data.cacheList.add(ca);
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

        if (threadCall == null)
            threadCall = new Thread(() -> {
                synchronized (list) {
                    for (CacheListChangedEventListener event : list) {
                        if (event == null)
                            continue;
                        event.CacheListChangedEvent();
                    }
                }

            });

        threadCall.start();
    }

}
