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
package de.droidcachebox.database;

import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.MoveableList;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
            Cache cache = get(i);
            if (cache.generatedId == cacheId)
                return cache;
        }
        return null;
    }

    public CacheWithWP resort(Coordinate selectedCoord, CacheWithWP selected) {

        CacheWithWP retValue = null;

        resortAtWork = true;
        // Alle Distanzen aktualisieren
        if (Locator.getInstance().isValid()) {
            for (int i = 0, n = size(); i < n; i++) {
                get(i).recalculateAndGetDistance(CalculationType.FAST, true, Locator.getInstance().getMyPosition());
            }
        } else {
            // sort after distance from selected Cache
            Coordinate fromPos = selectedCoord;
            // avoid "illegal waypoint"
            if (fromPos == null || (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)) {
                if (selected == null)
                    fromPos = null;
                else
                    fromPos = selected.getCache().getCoordinate();
            }
            if (fromPos == null) {
                resortAtWork = false;
                return null;
            }
            for (int i = 0, n = size(); i < n; i++) {
                get(i).recalculateAndGetDistance(CalculationType.FAST, true, fromPos);
            }
        }

        sort();

        // Nächsten Cache auswählen
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

        CacheListChangedListeners.getInstance().cacheListChanged();

        // vorhandenen Parkplatz Cache nach oben schieben
        Cache park = getCacheByGcCodeFromCacheList("CBPark");
        if (park != null) {
            moveItemFirst(indexOf(park));
        }

        // Cursor.Current = Cursors.Default;
        resortAtWork = false;
        return retValue;
    }

    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.<br>
     * All Cache objects are disposed
     */
    @Override
    public void clear() {
        for (int i = 0, n = size(); i < n; i++) {
            Cache cache = get(i);
            if (!cache.isLive())
                cache.dispose();
        }
        super.clear();
    }

    @Override
    public void dispose() {
        clear();
        super.dispose();
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
