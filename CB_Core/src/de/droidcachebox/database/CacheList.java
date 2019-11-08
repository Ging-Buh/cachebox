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
        for (int i = 0, n = this.size(); i < n; i++) {
            Cache cache = this.get(i);
            if (cache.getGcCode().equalsIgnoreCase(GcCode))
                return cache;
        }
        return null;
    }

    public Cache getCacheByIdFromCacheList(long cacheId) {
        for (int i = 0, n = this.size(); i < n; i++) {
            Cache cache = this.get(i);
            if (cache.Id == cacheId)
                return cache;
        }
        return null;
    }

    /**
     * @param selectedCoord
     *            GlobalCore.getSelectedCoord()
     * @param selected
     *            new CacheWithWp(GlobalCore.getSelectedCache(),GlobalCore.getSelectedWP())
     * @param userName
     *            Config.settings.GcLogin.getValue()
     * @param ParkingLatitude
     *            Config.settings.ParkingLatitude.getValue()
     * @param ParkingLongitude
     *            Config.settings.ParkingLongitude.getValue()
     * @return CacheWithWP [null posible] set To<br>
     *         GlobalCore.setSelectedWaypoint(nextCache, waypoint, false);<br>
     *         GlobalCore.NearestCache(nextCache);
     */

    /**
     * @param selectedCoord
     * @param selected
     * @return
     */
    public CacheWithWP resort(Coordinate selectedCoord, CacheWithWP selected) {

        CacheWithWP retValue = null;

        this.ResortAtWork = true;
        boolean isLocatorValid = Locator.getInstance().Valid();
        // Alle Distanzen aktualisieren
        if (isLocatorValid) {
            for (int i = 0, n = this.size(); i < n; i++) {
                Cache cache = this.get(i);
                cache.Distance(CalculationType.FAST, true);
            }
        } else {
            // sort after Distance from selected Cache
            Coordinate fromPos = selectedCoord;
            // avoid "illegal waypoint"
            if (fromPos == null || (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)) {
                fromPos = selected.getCache().Pos;
            }
            if (fromPos == null) {
                this.ResortAtWork = false;
                return retValue;
            }
            for (int i = 0, n = this.size(); i < n; i++) {
                Cache cache = this.get(i);
                cache.Distance(CalculationType.FAST, true, fromPos);
            }
        }

        this.sort();

        // Nächsten Cache auswählen
        if (this.size() > 0) {
            Cache nextCache = this.get(0); // or null ...
            for (int i = 0; i < this.size(); i++) {
                nextCache = this.get(i);
                if (!nextCache.isArchived()) {
                    if (nextCache.isAvailable()) {
                        if (!nextCache.isFound()) {
                            // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht)
                            if (!nextCache.ImTheOwner()) {
                                if ((nextCache.getType() == CacheTypes.Event) || (nextCache.getType() == CacheTypes.MegaEvent) || (nextCache.getType() == CacheTypes.CITO) || (nextCache.getType() == CacheTypes.Giga)) {
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
                                    if (nextCache.getType() == CacheTypes.Mystery) {
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
        Cache park = this.getCacheByGcCodeFromCacheList("CBPark");
        if (park != null) {
            this.MoveItemFirst(this.indexOf(park));
        }

        // Cursor.Current = Cursors.Default;
        this.ResortAtWork = false;
        return retValue;
    }

    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.<br>
     * All Cache objects are disposed
     */
    @Override
    public void clear() {
        for (int i = 0, n = this.size(); i < n; i++) {
            Cache cache = this.get(i);
            if (!cache.isLive())
                cache.dispose(); // don't dispose LiveCaches
            cache = null;
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
        for (int i = 0, n = this.size(); i < n; i++) {
            list.add(this.get(i).getGcCode());
        }
        return list;
    }

    @Override
    public int add(Cache ca) {
        if (ca == null)
            return -1;

        int index = -1;
        for (int i = 0, n = this.size(); i < n; i++) {

            Cache cache = get(i);
            if (cache.Id == ca.Id) {
                index = i;
            }
        }

        if (index > -1) {
            // Replace LiveCache with Cache
            if (get(index).isLive()) {
                if (!ca.isLive()) {
                    this.replace(ca, index);
                    return index;
                }
            }

        }

        return super.add(ca);
    }

}