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
package de.droidcachebox.dataclasses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.MoveableList;
import de.droidcachebox.utils.log.Log;

public class CacheList extends MoveableList<Cache> {

    private static final long serialVersionUID = -932434844601790958L;

    public boolean resortAtWork = false;

    public Cache getCacheByGcCodeFromCacheList(String GcCode) {
        for (int i = 0, n = size(); i < n; i++) {
            Cache cache = get(i);
            if (cache.getGeoCacheCode().equalsIgnoreCase(GcCode))
                return cache;
        }
        return null;
    }

    public Cache getCacheByIdFromCacheList(long cacheId) {
        for (int i = 0, n = size(); i < n; i++) {
            if (get(i).generatedId == cacheId)
                return get(i);
        }
        return null;
    }

    public CacheWithWP resort(Coordinate fromPos) {

        resortAtWork = true;
        // check fromPos
        if (fromPos == null || (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)) {
            Log.debug("sort CacheList", "no sort: reference pos is zero.");
            resortAtWork = false;
            return null;
        }
        Log.debug("sort CacheList", "" + fromPos);
        // Alle Distanzen aktualisieren
        for (int i = 0, n = size(); i < n; i++) {
            get(i).recalculateAndGetDistance(CalculationType.FAST, true, fromPos);
        }
        // sortieren
        sort();
        // Nächsten Cache auswählen
        CacheWithWP retValue = null;
        if (size() > 0) {
            Cache nextCache = null;
            for (int i = 0; i < size(); i++) {
                nextCache = get(i);
                if (!nextCache.isArchived()) {
                    if (nextCache.isAvailable()) {
                        if (!nextCache.isFound()) {
                            // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht)
                            if (!nextCache.iAmTheOwner()) {
                                if (nextCache.isEvent()) {
                                    Calendar dateHidden = GregorianCalendar.getInstance();
                                    Calendar today = GregorianCalendar.getInstance();
                                    try {
                                        dateHidden.setTime(nextCache.getDateHidden());
                                        if (("" + today.get(Calendar.DAY_OF_MONTH) + today.get(Calendar.MONTH) + today.get(Calendar.YEAR))
                                                .equals("" + dateHidden.get(Calendar.DAY_OF_MONTH) + dateHidden.get(Calendar.MONTH) + dateHidden.get(Calendar.YEAR))) {
                                            break;
                                        }
                                    } catch (Exception ex) {
                                        Log.err("CacheList", nextCache.getGeoCacheCode() + " Hidden:" + nextCache.getDateHidden());
                                    }
                                } else {
                                    if (nextCache.getGeoCacheType() == GeoCacheType.Mystery) {
                                        if (nextCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (nextCache == null) {
                nextCache = get(0);
            }
            // When the next Cache has a final waypoint -> activate the final waypoint!!!
            Waypoint waypoint = nextCache.getCorrectedFinal();
            if (waypoint == null) {
                // When the next Cache has a start waypoint -> activate the start waypoint!!!
                waypoint = nextCache.getStartWaypoint();
            }
            retValue = new CacheWithWP(nextCache, waypoint);
        }

        // vorhandenen Parkplatz Cache nach oben schieben
        Cache park = getCacheByGcCodeFromCacheList("CBPark");
        if (park != null) {
            moveItemFirst(indexOf(park));
        }

        CacheListChangedListeners.getInstance().fire("CacheList resort");

        // Cursor.Current = Cursors.Default;
        resortAtWork = false;
        return retValue;
    }

    public ArrayList<String> getGcCodes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0, n = size(); i < n; i++) {
            list.add(get(i).getGeoCacheCode());
        }
        return list;
    }

    @Override
    public int add(Cache ca) {
        if (ca == null)
            return -1;

        int index = -1;
        for (int i = 0, n = size(); i < n; i++) {
            if (get(i).generatedId == ca.generatedId) {
                index = i;
            }
        }

        if (index > -1) {
            // Replace LiveCache with Cache
            if (get(index).isLive()) {
                if (!ca.isLive()) {
                    replace(ca, index);
                    return index;
                }
            }

        }

        return super.add(ca);
    }

}
