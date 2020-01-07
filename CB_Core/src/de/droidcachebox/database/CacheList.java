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

    public boolean ResortAtWork = false;

    public Cache getCacheByGcCodeFromCacheList(String GcCode) {
        for (int i = 0, n = size(); i < n; i++) {
            Cache cache = get(i);
            if (cache.getGcCode().equalsIgnoreCase(GcCode))
                return cache;
        }
        return null;
    }

    public Cache getCacheByIdFromCacheList(long cacheId) {
        for (int i = 0, n = size(); i < n; i++) {
            Cache cache = get(i);
            if (cache.Id == cacheId)
                return cache;
        }
        return null;
    }

    public CacheWithWP resort(Coordinate selectedCoord, CacheWithWP selected) {

        CacheWithWP retValue = null;

        ResortAtWork = true;
        boolean isLocatorValid = Locator.getInstance().isValid();
        // Alle Distanzen aktualisieren
        if (isLocatorValid) {
            for (int i = 0, n = size(); i < n; i++) {
                get(i).recalculateAndGetDistance(CalculationType.FAST, true, Locator.getInstance().getMyPosition());
            }
        } else {
            // sort after distance from selected Cache
            Coordinate fromPos = selectedCoord;
            // avoid "illegal waypoint"
            if (fromPos == null || (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)) {
                fromPos = selected.getCache().getCoordinate();
            }
            if (fromPos == null) {
                ResortAtWork = false;
                return retValue;
            }
            for (int i = 0, n = size(); i < n; i++) {
                get(i).recalculateAndGetDistance(CalculationType.FAST, true, fromPos);
            }
        }

        sort();

        // Nächsten Cache auswählen
        if (size() > 0) {
            Cache nextCache = get(0); // or null ...
            for (int i = 0; i < size(); i++) {
                nextCache = get(i);
                if (!nextCache.isArchived()) {
                    if (nextCache.isAvailable()) {
                        if (!nextCache.isFound()) {
                            // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht)
                            if (!nextCache.ImTheOwner()) {
                                if ((nextCache.getType() == GeoCacheType.Event) || (nextCache.getType() == GeoCacheType.MegaEvent) || (nextCache.getType() == GeoCacheType.CITO) || (nextCache.getType() == GeoCacheType.Giga)) {
                                    Calendar dateHidden = GregorianCalendar.getInstance();
                                    Calendar today = GregorianCalendar.getInstance();
                                    try {
                                        dateHidden.setTime(nextCache.getDateHidden());
                                        if (("" + today.get(Calendar.DAY_OF_MONTH) + today.get(Calendar.MONTH) + today.get(Calendar.YEAR))
                                                .equals("" + dateHidden.get(Calendar.DAY_OF_MONTH) + dateHidden.get(Calendar.MONTH) + dateHidden.get(Calendar.YEAR))) {
                                            break;
                                        }
                                    } catch (Exception ex) {
                                        Log.err("CacheList", nextCache.getGcCode() + " Hidden:" + nextCache.getDateHidden());
                                    }
                                } else {
                                    if (nextCache.getType() == GeoCacheType.Mystery) {
                                        if (nextCache.hasCorrectedCoordiantesOrHasCorrectedFinal()) {
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
            // Wenn der nachste Cache ein Mystery mit Final Waypoint ist
            // -> gleich den Final Waypoint auswahlen!!!
            // When the next Cache is a mystery with final waypoint
            // -> activate the final waypoint!!!
            Waypoint waypoint = nextCache.getCorrectedFinal();
            if (waypoint == null) {
                // wenn ein Cache keinen Final Waypoint hat dann wird überprüft, ob dieser einen Startpunkt definiert hat
                // Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
                // selektiert
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
        ResortAtWork = false;
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
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0, n = size(); i < n; i++) {
            list.add(get(i).getGcCode());
        }
        return list;
    }

    @Override
    public int add(Cache ca) {
        if (ca == null)
            return -1;

        int index = -1;
        for (int i = 0, n = size(); i < n; i++) {
            if (get(i).Id == ca.Id) {
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
