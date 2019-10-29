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
package CB_UI.GL_UI.Views;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheListChangedEventListener;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Map.Descriptor;
import CB_UI.GlobalCore;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.MoveableList;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.util.concurrent.atomic.AtomicInteger;

import static CB_UI_Base.GL_UI.Sprites.*;

/**
 * @author ging-buh
 * @author Longri
 */
public class MapViewCacheList implements CacheListChangedEventListener {
    private static final String log = "MapViewCacheList";
    public final CB_List<WaypointRenderInfo> list = new CB_List<>();
    private final int maxZoomLevel;
    /**
     * State 0: warten auf neuen Update Befehl <br>
     * State 1: Berechnen <br>
     * State 2: Berechnung in Gang <br>
     * State 3: Berechnung fertig - warten auf abholen <br>
     * State 4: QueueProcessor abgebrochen
     */
    private final AtomicInteger state = new AtomicInteger(0);
    private final MoveableList<WaypointRenderInfo> tmplist = new MoveableList<>();
    public int anz = 0;
    private MapViewCacheListUpdateData savedQuery = null;
    private MapViewCacheListUpdateData lastUpdateData = null;
    private QueueProcessor queueProcessor = null;
    private Vector2 point1;
    private Vector2 point2;
    private int zoom = 15;
    private WaypointRenderInfo selectedWP;
    private boolean hideMyFinds = false;
    private boolean showAllWaypoints = false;
    private boolean showAtOriginalPosition = false;
    private Vector2 lastPoint1;
    private Vector2 lastPoint2;
    private int lastzoom;

    public MapViewCacheList(int maxZoomLevel) {
        super();
        this.maxZoomLevel = maxZoomLevel;

        startQueueProcessor();

        // register as CacheListChangedEventListener
        CacheListChangedEventList.Add(this);

    }

    private void startQueueProcessor() {

        try {
            // Log.debug(log, "MapCacheList.QueueProcessor Create");
            queueProcessor = new QueueProcessor();
            queueProcessor.setPriority(Thread.MIN_PRIORITY);
        } catch (Exception ex) {
            Log.err(log, "MapCacheList.QueueProcessor", "onCreate", ex);
        }

        // Log.debug(log, "MapCacheList.QueueProcessor Start");
        queueProcessor.start();

        state.set(0);
    }

    private void addWaypoints(Cache cache, int iconSize) {
        if (cache.waypoints == null)
            return;
        for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
            addWaypoint(cache, cache.waypoints.get(i), iconSize);
        }
    }

    private void addWaypoint(Cache cache, Waypoint wp, int iconSize) {
        // im Bild ?
        double MapX = 256.0 * Descriptor.LongitudeToTileX(maxZoomLevel, wp.Pos.getLongitude());
        double MapY = -256.0 * Descriptor.LatitudeToTileY(maxZoomLevel, wp.Pos.getLatitude());
        if (isVisible(MapX, MapY) || (GlobalCore.getSelectedWaypoint() == wp)) {
            WaypointRenderInfo wpi = new WaypointRenderInfo();
            wpi.MapX = (float) MapX;
            wpi.MapY = (float) MapY;

            wpi.Icon = getWaypointIcon(wp);
            wpi.Cache = cache;
            wpi.Waypoint = wp;
            wpi.UnderlayIcon = getUnderlayIcon(wpi.Cache, wpi.Waypoint, iconSize);
            wpi.Selected = (GlobalCore.getSelectedWaypoint() == wp);
            if (wpi.Selected)
                selectedWP = wpi;
            tmplist.add(wpi);
        }
    }

    private boolean isVisible(double x, double y) {
        return ((x >= point1.x) && (x < point2.x) && (Math.abs(y) > Math.abs(point1.y)) && (Math.abs(y) < Math.abs(point2.y)));
    }

    private Sprite getWaypointIcon(Waypoint waypoint) {
        if ((waypoint.Type == CacheTypes.MultiStage) && (waypoint.IsStart))
            return getSprite("mapMultiStageStartP"); //
        else
            return getSprite("map" + waypoint.Type.name());
    }

    private Sprite getCacheIcon(Cache cache, int iconSize) {
        if (iconSize > 0) {
            return getMapIcon(cache);
        } else {
            if (GlobalCore.isSetSelectedCache()) {
                if (cache.Id == GlobalCore.getSelectedCache().Id) {
                    // the selected cache always has a big icon
                    return getMapIcon(cache);
                }
            }
            return getSmallMapIcon(cache);
        }
    }

    private Sprite getMapIcon(Cache cache) {
        if (cache.ImTheOwner())
            return getSprite("star");
        else if (cache.isFound())
            return getSprite("mapFound");
        else if (showAtOriginalPosition && !cache.hasCorrectedCoordinates())
            // todo for corrected coordinates there is no original position saved at the moment
            return getSprite("map" + cache.getType().name());
        else if (cache.hasCorrectedCoordiantesOrHasCorrectedFinal()) {
            if (cache.getType() == CacheTypes.Mystery)
                return getSprite("mapMysterySolved");
            else if (cache.getType() == CacheTypes.Multi)
                return getSprite("mapMultiSolved");
            else if (cache.getType() == CacheTypes.Wherigo)
                return getSprite("mapWherigoSolved");
            else if (cache.getType() == CacheTypes.Letterbox)
                return getSprite("mapLetterboxSolved");
            else if (cache.getType() == CacheTypes.Traditional)
                return getSprite("mapTraditionalSolved");
            else if (cache.getType() == CacheTypes.Virtual)
                return getSprite("mapVirtualSolved");
            else if (cache.getType() == CacheTypes.Camera)
                return getSprite("mapCameraSolved");
            else if (cache.getType() == CacheTypes.Earth)
                return getSprite("mapEarthSolved");
            return getSprite("mapMysterySolved");
        }
        else if ((cache.getType() == CacheTypes.Multi) && cache.getStartWaypoint() != null)
            return getSprite("mapMultiStartP"); // Multi mit Startpunkt
        else if ((cache.getType() == CacheTypes.Mystery) && cache.getStartWaypoint() != null)
            return getSprite("mapMysteryStartP"); // Mystery ohne Final aber mit Startpunkt
        else
            return getSprite("map" + cache.getType().name());

    }

    private Sprite getSmallMapIcon(Cache cache) {
        String icon = "small1"; // Tradi, Ape, Letterbox
        String solved = "";

        if (cache.isFound())
            icon = "small6";
        else if (cache.ImTheOwner())
            icon = "small7";
        else {
            if (cache.getCorrectedFinal() != null || cache.getStartWaypoint() != null)
                solved = "Solved";
            switch (cache.getType()) {
                case Multi:
                    icon = "small2";
                    break;
                case Event:
                case MegaEvent:
                case Giga:
                case CITO:
                    icon = "small3";
                    break;
                case Virtual:
                case Camera:
                case Earth:
                    icon = "small4";
                    break;
                case Mystery:
                case Wherigo:
                    icon = "small5";
                    break;
                case MyParking:
                    return getSprite("map" + cache.getType().name());
                case Munzee:
                    return getSprite("map" + cache.getType().name());
                default:
                    break;
            }
        }

        if (cache.isArchived() || !cache.isAvailable())
            icon = icon + "no";

        return getSprite(icon + solved);

    }

    private Sprite getUnderlayIcon(Cache cache, Waypoint waypoint, int iconSize) {
        boolean selectedCache = false;
        if (GlobalCore.isSetSelectedCache()) {
            selectedCache = cache.Id == GlobalCore.getSelectedCache().Id;
        }

        if ((iconSize == 0) && (!selectedCache)) {
            return null;
        } else {
            if (waypoint == null) {
                if ((cache == null) || selectedCache) {
                    if (cache.isLive()) {// set color for underlayIcon to blue if this a LiveCache

                        return getMapOverlay(IconName.liveSelected);
                    }
                    return getMapOverlay(IconName.shaddowrectselected);
                } else {
                    if (cache.isLive()) {// set color for underlayIcon to blue if this a LiveCache

                        return getMapOverlay(IconName.live);
                    }

                    return getMapOverlay(IconName.shaddowrect);
                }
            } else {
                if (waypoint == GlobalCore.getSelectedWaypoint()) {
                    return getMapOverlay(IconName.shaddowrectselected);
                } else {
                    return getMapOverlay(IconName.shaddowrect);
                }
            }
        }
    }

    public void update(MapViewCacheListUpdateData data) {
        lastUpdateData = data;
        this.showAllWaypoints = data.showAllWaypoints;
        this.hideMyFinds = data.hideMyFinds;
        showAtOriginalPosition = data.showAtOriginalPosition;

        if (data.point1 == null || data.point2 == null)
            return;

        if (state.get() == 4) {
            // der QueueProcessor wurde gestoppt und muss neu gestartet werden
            startQueueProcessor();
        }

        if (state.get() != 0) {
            // Speichere Update anfrage und führe sie aus, wenn der QueueProcessor wieder bereit ist!
            savedQuery = data;
            return;
        }

        if ((data.zoom == lastzoom) && (!data.doNotCheck)) {
            // wenn LastPoint == 0 muss eine neue Liste Berechnet werden!
            if (lastPoint1 != null && lastPoint2 != null) {
                // Prüfen, ob überhaupt eine neue Liste berechnet werden muß
                if ((data.point1.x >= lastPoint1.x) && (data.point2.x <= lastPoint2.x) && (data.point1.y >= lastPoint1.y) && (data.point2.y <= lastPoint2.y))
                    return;
            }

        }

        // Bereich erweitern, damit von vorne herein gleiche mehr Caches geladen werden und diese Liste nicht so oft berechnet werden muss
        Vector2 size = new Vector2(data.point2.x - data.point1.x, data.point2.y - data.point1.y);
        data.point1.x -= size.x;
        data.point2.x += size.x;
        data.point1.y -= size.y;
        data.point2.y += size.y;

        this.lastzoom = data.zoom;
        lastPoint1 = data.point1;
        lastPoint2 = data.point2;

        this.zoom = data.zoom;
        this.point1 = data.point1;
        this.point2 = data.point2;
        state.set(1);
    }

    public boolean hasNewResult() {
        return state.get() == 3;
    }

    @Override
    public void CacheListChangedEvent() {
        if (lastUpdateData != null) {
            lastUpdateData.doNotCheck = true;
            update(lastUpdateData);
        }
    }

    public static class MapViewCacheListUpdateData {
        public Vector2 point1;
        public Vector2 point2;
        public int zoom;
        public boolean doNotCheck;
        public boolean hideMyFinds = false;
        public boolean showAllWaypoints = false;
        public boolean showAtOriginalPosition = false;

        public MapViewCacheListUpdateData(Vector2 point1, Vector2 point2, int zoom, boolean doNotCheck) {
            this.point1 = point1;
            this.point2 = point2;
            this.zoom = zoom;
            this.doNotCheck = doNotCheck;
        }

        public MapViewCacheListUpdateData(MapViewCacheListUpdateData data) {
            this.point1 = data.point1;
            this.point2 = data.point2;
            this.zoom = data.zoom;
            this.doNotCheck = data.doNotCheck;
        }
    }

    public static class WaypointRenderInfo {
        public float MapX;
        public float MapY;
        public Cache Cache;
        public Waypoint Waypoint;
        public boolean Selected;
        public Sprite Icon;
        public Sprite UnderlayIcon;
        public Sprite OverlayIcon;
    }

    private class QueueProcessor extends Thread {

        @Override
        public void run() {
            // boolean queueEmpty = false;
            try {
                do {
                    if (state.compareAndSet(1, 2)) {
                        int iconSize = 0; // 8x8
                        if ((zoom >= 13) && (zoom <= 14))
                            iconSize = 1; // 13x13
                        else if (zoom > 14)
                            iconSize = 2; // default Images

                        tmplist.clear();
                        selectedWP = null;
                        synchronized (Database.Data.cacheList) {
                            for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                                Cache cache = Database.Data.cacheList.get(i);
                                // handle show option "
                                if (hideMyFinds && cache.isFound()) continue;
                                boolean selectedCache = false;
                                if (GlobalCore.isSetSelectedCache()) {
                                    selectedCache = GlobalCore.getSelectedCache().Id == cache.Id;
                                }
                                boolean showWaypoints = showAllWaypoints || selectedCache;
                                double MapX = 256.0 * Descriptor.LongitudeToTileX(maxZoomLevel, cache.Longitude());
                                double MapY = -256.0 * Descriptor.LatitudeToTileY(maxZoomLevel, cache.Latitude());
                                Waypoint finalWaypoint = null;
                                Waypoint startWaypoint;
                                if (showWaypoints) {
                                    addWaypoints(cache, iconSize);
                                }
                                if (!showAtOriginalPosition) {
                                    // show Cache at corrected final
                                    if (!cache.hasCorrectedCoordinates()) {
                                        finalWaypoint = cache.getCorrectedFinal();
                                        if (finalWaypoint != null) {
                                            // show cache at Final coords
                                            MapX = 256.0 * Descriptor.LongitudeToTileX(maxZoomLevel, finalWaypoint.Pos.getLongitude());
                                            MapY = -256.0 * Descriptor.LatitudeToTileY(maxZoomLevel, finalWaypoint.Pos.getLatitude());
                                        }
                                    }
                                    // or show Cache at Startwaypoint (only for Multi and Mysterie)
                                    if ((cache.getType() == CacheTypes.Multi) || (cache.getType() == CacheTypes.Mystery)) {
                                        // todo ? really show corrected coord if a startWP is defined?
                                        if (!cache.hasCorrectedCoordinates() && (finalWaypoint == null)) {
                                            startWaypoint = cache.getStartWaypoint();
                                            if (startWaypoint != null) {
                                                MapX = 256 * Descriptor.LongitudeToTileX(maxZoomLevel, startWaypoint.Pos.getLongitude());
                                                MapY = -256 * Descriptor.LatitudeToTileY(maxZoomLevel, startWaypoint.Pos.getLatitude());
                                            }
                                        }
                                    }
                                }
                                // Cache Icon Overlay, ...
                                if (isVisible(MapX, MapY) || selectedCache) {
                                    WaypointRenderInfo wpi = new WaypointRenderInfo();
                                    wpi.MapX = (float) MapX;
                                    wpi.MapY = (float) MapY;
                                    if (cache.isArchived() || !cache.isAvailable())
                                        wpi.OverlayIcon = getMapOverlay(IconName.deact);
                                    wpi.UnderlayIcon = getUnderlayIcon(cache, null, iconSize);
                                    wpi.Icon = getCacheIcon(cache, iconSize);
                                    wpi.Cache = cache;
                                    wpi.Waypoint = null; // = fwp; ist null, ausser bei Mystery-Final // null -> Beschriftung Name vom Cache
                                    wpi.Selected = selectedCache;
                                    if (wpi.Selected && selectedWP == null)
                                        selectedWP = wpi;// select nur wenn kein WP selectiert ist (draw last)
                                    tmplist.add(wpi);
                                }
                            }
                        }

                        synchronized (list) {

                            // move selected WPI to last
                            int index = tmplist.indexOf(selectedWP);
                            if (index >= 0 && index <= tmplist.size())
                                tmplist.MoveItemLast(index);

                            list.clear();

                            for (int i = 0, n = tmplist.size(); i < n; i++) {
                                list.add(tmplist.get(i));
                            }
                            tmplist.clear();
                        }
                        Thread.sleep(50);
                        state.set(0);
                        anz++;
                        if (savedQuery != null) {
                            // es steht noch eine Anfrage an!
                            // Diese jetzt ausführen!
                            MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(savedQuery);
                            data.hideMyFinds = hideMyFinds;
                            data.showAllWaypoints = showAllWaypoints;
                            data.showAtOriginalPosition = showAtOriginalPosition;
                            savedQuery = null;
                            update(data);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } while (true);
            } catch (Exception ex3) {
                Log.err(log, "MapCacheList.QueueProcessor.doInBackground()", "3", ex3);
            } finally {
                // wenn der Thread beendet wurde, muss er neu gestartet werden!
                state.set(4);
            }
            return;
        }
    }
}
