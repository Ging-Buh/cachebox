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

package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.*;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.core.LiveMapQue;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.MapInfoPanel.CoordType;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.graphics.*;
import de.droidcachebox.gdx.math.*;
import de.droidcachebox.gdx.views.MapViewCacheList.MapViewCacheListUpdateData;
import de.droidcachebox.gdx.views.MapViewCacheList.WayPointRenderInfo;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.map.MapScale;
import de.droidcachebox.locator.map.*;
import de.droidcachebox.menu.menuBtn2.ShowSpoiler;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.PointL;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.updateGeoCache;
import static de.droidcachebox.gdx.Sprites.*;
import static de.droidcachebox.utils.Config_Core.displayDensity;

public class MapView extends MapViewBase implements CacheSelectionChangedListeners.CacheSelectionChangedListener, PositionChangedEvent {
    private static final String sKlasse = "MapView";
    private CB_RectF targetArrow = new CB_RectF();
    private TreeMap<Integer, Integer> distanceZoomLevel;
    private final MapMode mapMode;
    private final LiveButton liveButton;
    private MultiToggleButton btnMapState;
    private InfoBubble infoBubble;
    private CancelWaitDialog wd = null;
    private int zoomCross = 16;
    private boolean showRating;
    private boolean showDT;
    private boolean showTitles;
    private boolean hideMyFinds;
    private boolean showDirectLine, showAllWaypoints, showDistanceCircle;
    private Cache lastSelectedCache = null;
    private Waypoint lastSelectedWaypoint = null;
    private GlyphLayout geocacheOrWayPointName = null;
    private CB_RectF targetArrowScreenRec;
    private final MapViewCacheList mapCacheList;
    private int lastCompassMapZoom = -1;
    private MapInfoPanel mapInfoPanel;
    private final PointL lastScreenCenter;
    private final GL_Paint distanceCirclePaint;
    private final GL_Paint directLinePaint;
    private PolygonDrawable directLine;

    public MapView(CB_RectF cb_RectF, MapMode mapMode) {
        super(cb_RectF, mapMode.name());
        lastScreenCenter = new PointL(0, 0);
        this.mapMode = mapMode;
        Log.info(sKlasse, "creating Mapview for " + mapMode + " map");
        mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);

        if (mapMode != MapMode.Normal) {
            setOnDoubleClickListener((v, x, y, pointer, button) -> {
                if (this.mapMode == MapMode.Track)
                    setMapState(MapState.FREE);
                else setMapState(MapState.GPS);
                // Center own position!
                setCenter(Locator.getInstance().getMyPosition());
                return true;
            });
        }

        Config.mapsForgeDayTheme.addSettingChangedListener(themeChangedEventHandler);
        Config.mapsForgeNightTheme.addSettingChangedListener(themeChangedEventHandler);
        Config.mapsForgeCarDayTheme.addSettingChangedListener(themeChangedEventHandler);
        Config.mapsForgeCarNightTheme.addSettingChangedListener(themeChangedEventHandler);

        registerSkinChangedEvent();

        setBackground(ListBack);

        mapScale = new MapScale(new CB_RectF(GL_UISizes.margin, GL_UISizes.margin, getHalfWidth(), GL_UISizes.zoomBtn.getHalfWidth() / 4), "mapScale", this, Config.ImperialUnits.getValue());
        if (mapMode == MapMode.Normal) {
            addChild(mapScale);
        } else {
            mapScale.setInvisible();
        }

        zoomBtn = new ZoomButtons(GL_UISizes.zoomBtn, this, "ZoomButtons");
        zoomBtn.setX(getWidth() - (zoomBtn.getWidth() + UiSizes.getInstance().getMargin()));
        zoomBtn.setOnClickListenerDown((v, x, y, pointer, button) -> {
            zoomScale.resetFadeOut();
            inputState = InputState.Idle;
            lastDynamicZoom = zoomBtn.getZoom();
            kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
            GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
            renderOnce("zoomBtn.setOnClickListenerDown");
            calcPixelsPerMeter();
            return true;
        });
        zoomBtn.setOnClickListenerUp((v, x, y, pointer, button) -> {
            setZoomScale(zoomBtn.getZoom());
            zoomScale.resetFadeOut();
            inputState = InputState.Idle;
            lastDynamicZoom = zoomBtn.getZoom();
            kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
            GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
            renderOnce("zoomBtn.setOnClickListenerUp");
            calcPixelsPerMeter();
            return true;
        });
        if (mapMode == MapMode.Compass) {
            zoomBtn.setInvisible();
        } else {
            addChild(zoomBtn);
            zoomBtn.setMinimumFadeValue(0.25f);
        }

        setClickHandler((v, x, y, pointer, button) -> {
            WayPointRenderInfo minWpi = null;
            if (targetArrow != null && targetArrow.contains(x, y)) {
                if (GlobalCore.isSetSelectedCache()) {
                    if (GlobalCore.getSelectedWayPoint() != null) {
                        Coordinate tmp = GlobalCore.getSelectedWayPoint().getCoordinate();
                        setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
                    } else {
                        Coordinate tmp = GlobalCore.getSelectedCache().getCoordinate();
                        setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
                    }
                    btnMapState.setState(MapState.WP.ordinal());
                }
                return false;
            }

            synchronized (mapCacheList.wayPointsRenderInformation) {
                double minDist = Double.MAX_VALUE;
                for (int i = 0, n = mapCacheList.wayPointsRenderInformation.size(); i < n; i++) {
                    WayPointRenderInfo wpi = mapCacheList.wayPointsRenderInformation.get(i);
                    Vector2 screen = worldToScreen(new Vector2(Math.round(wpi.mapX), Math.round(wpi.mapY)));
                    double aktDist = Math.sqrt(Math.pow(screen.x - x, 2) + Math.pow(screen.y - y, 2));
                    if (aktDist < minDist) {
                        minDist = aktDist;
                        minWpi = wpi;
                    }
                }
                // empty mapCacheList
                if (minWpi == null || minWpi.cache == null) {
                    return true;
                }
                // always hide the bubble
                if (infoBubble.isVisible()) {
                    infoBubble.setInvisible();
                }
                // check for showing the bubble
                if (minDist < 40) {
                    if (minWpi.waypoint != null) {
                        if (GlobalCore.getSelectedCache() != minWpi.cache) {
                            // show bubble at the location of the waypoint!!!
                            infoBubble.setCache(minWpi.cache, minWpi.waypoint);
                            infoBubble.setVisible();
                        } else {
                            // if only waypoint changes, the bubble will not be shown. (why not ?)
                            // so we must do the selection here
                            GlobalCore.setSelectedWaypoint(minWpi.cache, minWpi.waypoint);
                            MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(getMapIntWidth(), getMapIntHeight())), currentZoom, true);
                            data.hideMyFinds = hideMyFinds;
                            data.showAllWaypoints = showAllWaypoints;
                            data.showAtOriginalPosition = showAtOriginalPosition;
                            mapCacheList.update(data);
                        }
                    } else {
                        // show bubble
                        infoBubble.setCache(minWpi.cache, null);
                        infoBubble.setVisible();
                    }
                    inputState = InputState.Idle;
                }
            }
            return false;
        });

        float infoHeight = 0;
        if (mapMode == MapMode.Normal) {
            mapInfoPanel = (MapInfoPanel) addChild(new MapInfoPanel(GL_UISizes.info, "InfoPanel", this));
            infoHeight = mapInfoPanel.getHeight();
        }

        CB_RectF ZoomScaleRec = new CB_RectF();
        ZoomScaleRec.setSize((float) (44.6666667 * GL_UISizes.dpi), getHeight() - infoHeight - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());
        ZoomScaleRec.setPos(GL_UISizes.margin, zoomBtn.getMaxY() + GL_UISizes.margin);

        zoomScale = new ZoomScale(ZoomScaleRec, "zoomScale", 2, 21, 12);
        if (mapMode == MapMode.Normal)
            addChild(zoomScale);

        setMapIntWidth((int) getWidth());
        setMapIntHeight((int) getHeight());
        midVector2 = new Vector2((float) getMapIntWidth() / 2f, (float) getMapIntHeight() / 2f);

        if (MapTileLoader.getInstance().getCurrentLayer() == null) {
            MapTileLoader.getInstance().setCurrentLayer(LayerManager.getInstance().getLayer(Config.currentMapLayer.getValue()), isCarMode);
        }
        String[] currentOverlayLayerName = new String[]{Config.CurrentMapOverlayLayerName.getValue()};
        if (MapTileLoader.getInstance().getCurrentOverlayLayer() == null && currentOverlayLayerName[0].length() > 0)
            MapTileLoader.getInstance().setCurrentOverlayLayer(LayerManager.getInstance().getOverlayLayer(currentOverlayLayerName));
        initializeMap();

        // initial Zoom Scale
        // zoomScale = new GL_ZoomScale(6, 20, 13);

        // from create

        if (Config.mapViewDPIFaktor.getValue() == 1) {
            Config.mapViewDPIFaktor.setValue(displayDensity);
            Config.AcceptChanges();
        }
        iconFactor = Config.mapViewDPIFaktor.getValue();

        liveButton = new LiveButton();
        liveButton.setActivated(Config.liveMapEnabled.getDefaultValue());
        Config.disableLiveMap.addSettingChangedListener(this::requestLayout);

        btnMapState = new MultiToggleButton(GL_UISizes.toggle, "toggle");

        btnMapState.addState("Free", new HSV_Color(Color.GRAY));
        btnMapState.addState("GPS", new HSV_Color(Color.GREEN));
        btnMapState.addState("WP", new HSV_Color(Color.MAGENTA));
        btnMapState.addState("Lock", new HSV_Color(Color.RED));
        btnMapState.addState("Car", new HSV_Color(Color.YELLOW));
        btnMapState.setLastStateWithLongClick(true);
        MapState lastUsedMapState = MapState.values()[Config.lastMapToggleBtnState.getValue()];
        btnMapState.setState(lastUsedMapState.ordinal());
        btnMapState.setOnStateChangedListener((v, state) -> setMapState(MapState.values()[state]));
        btnMapState.registerSkinChangedEvent();

        switch (mapMode) {
            case Compass:
                setMapState(MapState.GPS);
                break;
            case Normal:
                setMapState(lastUsedMapState);
                switch (Config.lastMapToggleBtnState.getValue()) {
                    case 0:
                        mapInfoPanel.setCoordType(CoordType.Map);
                        break;
                    case 1:
                    case 3:
                    case 4:
                        mapInfoPanel.setCoordType(CoordType.GPS);
                        break;
                    case 2:
                        mapInfoPanel.setCoordType(CoordType.Cache);
                        break;
                }
                addChild(btnMapState);

                if (Config.disableLiveMap.getValue()) {
                    liveButton.setActivated(false);
                }
                addChild(liveButton);

                break;
            case Track:
                setMapState(MapState.FREE);
                break;
        }

        infoBubble = new InfoBubble(GL_UISizes.bubble, "infoBubble");
        infoBubble.setInvisible();
        infoBubble.setClickHandler((v, x, y, pointer, button) -> {
            if (infoBubble.saveButtonClicked(x, y)) {
                wd = CancelWaitDialog.ShowWait(Translation.get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), () -> {

                }, new ICancelRunnable() {

                    @Override
                    public void run() {
                        Cache bubblesCache = infoBubble.getCache();
                        if (bubblesCache.getGeoCacheDetail() == null)
                            bubblesCache.loadDetail();
                        String GCCode = bubblesCache.getGeoCacheCode();
                        ArrayList<GroundspeakAPI.GeoCacheRelated> geoCacheRelateds = updateGeoCache(bubblesCache);
                        if (geoCacheRelateds.size() > 0) {
                            try {
                                WriteIntoDB.writeCachesAndLogsAndImagesIntoDB(geoCacheRelateds, null);
                            } catch (InterruptedException ex) {
                                Log.err(sKlasse, "WriteIntoDB.writeCachesAndLogsAndImagesIntoDB", ex);
                            }
                        } else {
                            if (GroundspeakAPI.APIError != OK) {
                                GL.that.RunOnGL(() -> MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("ReloadCacheAPI"), MessageBoxButton.OK, MessageBoxIcon.Information, null));
                            }
                        }

                        // Reload result from DB
                        synchronized (Database.Data.cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                            Database.Data.cacheList = CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Config.showAllWaypoints.getValue());
                        }

                        Cache selCache = Database.Data.cacheList.getCacheByGcCodeFromCacheList(GCCode);
                        GlobalCore.setSelectedCache(selCache);
                        infoBubble.setCache(selCache, null, true);
                        CacheListChangedListeners.getInstance().cacheListChanged();
                        wd.close();

                        ShowSpoiler.getInstance().ImportSpoiler(false).setReadyListener(() -> {
                            // do after import
                            if (GlobalCore.isSetSelectedCache()) {
                                GlobalCore.getSelectedCache().loadSpoilerRessources();
                            }
                        });

                    }

                    @Override
                    public boolean doCancel() {
                        return false;
                    }
                });
            } else {
                if (infoBubble.getWaypoint() == null) {
                    // Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
                    Waypoint waypoint = infoBubble.getCache().getCorrectedFinal();
                    // wenn ein Cache keine Final hat, aber einen StartWaypointm, dann wird dieser gleich selektiert
                    if (waypoint == null)
                        waypoint = infoBubble.getCache().getStartWaypoint();
                    GlobalCore.setSelectedWaypoint(infoBubble.getCache(), waypoint);
                } else {
                    GlobalCore.setSelectedWaypoint(infoBubble.getCache(), infoBubble.getWaypoint());
                }
            }

            infoBubble.setInvisible();
            return true;
        });
        if (mapMode == MapMode.Normal)
            addChild(infoBubble);

        resize(getWidth(), getHeight());

        try {
            center = new CoordinateGPS(Config.mapInitLatitude.getValue(), Config.mapInitLongitude.getValue());
        } catch (Exception ex) {
            Log.err(sKlasse, "MapView/CoordinateGPS", ex);
        }

        // Info aktualisieren
        if (mapMode == MapMode.Normal)
            mapInfoPanel.setCoord(center);

        zoomBtn.setZoom(Config.lastZoomLevel.getValue());
        calcPixelsPerMeter();
        mapScale.zoomChanged();

        if ((center.getLatitude() == -1000) && (center.getLongitude() == -1000)) {
            // not initialized
            center = new CoordinateGPS(48, 12);
        }

        // Initial SettingsChanged Events
        setNightMode();
        Config.nightMode.addSettingChangedListener(this::setNightMode);

        isNorthOriented = Config.isMapNorthOriented.getValue();
        Config.isMapNorthOriented.addSettingChangedListener(() -> {
            isNorthOriented = Config.isMapNorthOriented.getValue();
            positionChanged();
        });
        // to force generation of tiles in loadTiles();
        OnResumeListeners.getInstance().addListener(this::onResume);

        directLinePaint = new GL_Paint();
        directLinePaint.setColor(Color.RED);
        directLine = null;

        distanceCirclePaint = new GL_Paint();
        distanceCirclePaint.setColor(Color.GOLDENROD);
        distanceCirclePaint.setStrokeWidth(2 * UiSizes.getInstance().getScale());
        distanceCirclePaint.setStyle(GL_Paint.GL_Style.STROKE);
        distanceCircle = null;
    }

    @Override
    protected void renderSynchronOverlay(Batch batch) {
        batch.setProjectionMatrix(myParentInfo.Matrix());

        // calculate icon size
        int iconSize = 0; // 8x8
        if ((currentZoom >= 13) && (currentZoom <= 14))
            iconSize = 1; // 13x13
        else if (currentZoom >= 15)
            iconSize = 2; // default Images

        if (mapMode != MapMode.Compass)
            TrackList.getInstance().renderTracks(batch, this);
        renderWayPoints(GL_UISizes.wayPointSizes[iconSize], GL_UISizes.underlaySizes[iconSize], batch);
        renderPositionMarker(batch);
        renderTargetArrow(batch);
        if (mapState != MapState.GPS && mapState != MapState.CAR) renderDistanceToCenter(batch);
    }

    @Override
    protected void renderNonSynchronOverlay(Batch batch) {
        renderUI(batch);
    }

    private void renderUI(Batch batch) {
        batch.setProjectionMatrix(myParentInfo.Matrix());

        if (showMapCenterCross) {
            if (mapState == MapState.FREE) {
                if (CrossLines == null) {
                    float crossSize = Math.min(getMapIntHeight() / 3.0f, getMapIntWidth() / 3.0f) / 2;
                    float strokeWidth = 2 * UiSizes.getInstance().getScale();

                    GeometryList geomList = new GeometryList();
                    Line l1 = new Line(getMapIntWidth() / 2.0f - crossSize, getMapIntHeight() / 2.0f, getMapIntWidth() / 2.0f + crossSize, getMapIntHeight() / 2.0f);
                    Line l2 = new Line(getMapIntWidth() / 2.0f, getMapIntHeight() / 2.0f - crossSize, getMapIntWidth() / 2.0f, getMapIntHeight() / 2.0f + crossSize);
                    Quadrangle q1 = new Quadrangle(l1, strokeWidth);
                    Quadrangle q2 = new Quadrangle(l2, strokeWidth);

                    geomList.add(q1);
                    geomList.add(q2);

                    GL_Paint paint = new GL_Paint();
                    paint.setColor(COLOR.getCrossColor());
                    CrossLines = new PolygonDrawable(geomList.getVertices(), geomList.getTriangles(), paint, getMapIntWidth(), getMapIntHeight());

                    geomList.dispose();
                    l1.dispose();
                    l2.dispose();
                    q1.dispose();
                    q2.dispose();

                }

                CrossLines.draw(batch, 0, 0, getMapIntWidth(), getMapIntHeight(), 0);
            }
        }
    }

    private void renderTargetArrow(Batch batch) {

        if (GlobalCore.getSelectedCache() == null)
            return;

        Coordinate coord = (GlobalCore.getSelectedWayPoint() != null) ? GlobalCore.getSelectedWayPoint().getCoordinate() : GlobalCore.getSelectedCache().getCoordinate();

        if (coord == null) {
            return;
        }
        float x = (float) (256.0 * Descriptor.longitudeToTileX(MAX_MAP_ZOOM, coord.getLongitude()));
        float y = (float) (-256.0 * Descriptor.latitudeToTileY(MAX_MAP_ZOOM, coord.getLatitude()));

        float halfHeight = getMapIntHeight() / 2.0f - ySpeedVersatz;
        float halfWidth = getMapIntWidth() / 2.0f;

        // create ScreenRec
        try {
            if (targetArrowScreenRec == null) {
                targetArrowScreenRec = new CB_RectF(0, 0, getMapIntWidth(), getMapIntHeight());
                if (mapMode != MapMode.Compass) {
                    targetArrowScreenRec.scaleCenter(0.9f);

                    if (mapMode == MapMode.Normal) {
                        targetArrowScreenRec.setHeight(targetArrowScreenRec.getHeight() - (targetArrowScreenRec.getHeight() - mapInfoPanel.getY()) - zoomBtn.getHeight());
                        targetArrowScreenRec.setY(zoomBtn.getMaxY());
                    }
                }
            }

            Vector2 ScreenCenter = new Vector2(halfWidth, halfHeight);

            Vector2 screen = worldToScreen(new Vector2(x, y));
            Vector2 target = new Vector2(screen.x, screen.y);

            Vector2 newTarget = targetArrowScreenRec.getIntersection(ScreenCenter, target);

            // Rotation berechnen
            if (newTarget != null) {

                float direction = get_angle(ScreenCenter.x, ScreenCenter.y, newTarget.x, newTarget.y);
                direction = 180 - direction;

                // draw sprite
                Sprite arrow = Arrows.get(4); // 4 = target-arrow
                arrow.setRotation(direction);

                float boundsX = newTarget.x - GL_UISizes.targetArrow.getHalfWidth();
                float boundsY = newTarget.y - GL_UISizes.targetArrow.getHeight();

                arrow.setBounds(boundsX, boundsY, GL_UISizes.targetArrow.getWidth(), GL_UISizes.targetArrow.getHeight());

                arrow.setOrigin(GL_UISizes.targetArrow.getHalfWidth(), GL_UISizes.targetArrow.getHeight());
                arrow.draw(batch);

                // get real bounding box of TargetArrow
                float[] t = arrow.getVertices();
                float maxX = Math.max(Math.max(t[0], t[5]), Math.max(t[10], t[15]));
                float minX = Math.min(Math.min(t[0], t[5]), Math.min(t[10], t[15]));
                float maxY = Math.max(Math.max(t[1], t[6]), Math.max(t[11], t[16]));
                float minY = Math.min(Math.min(t[1], t[6]), Math.min(t[11], t[16]));
                targetArrow.set(minX, minY, maxX - minX, maxY - minY);
            } else {
                targetArrow.set(0, 0, 0, 0);
            }
        } catch (Exception e) {
            targetArrow.set(0, 0, 0, 0);
        }
    }

    private void renderWayPoints(SizeF wpUnderlay, SizeF wpSize, Batch batch) {
        if (mapCacheList.wayPointsRenderInformation != null) {
            synchronized (mapCacheList.wayPointsRenderInformation) {
                for (WayPointRenderInfo wayPointRenderInfo : mapCacheList.wayPointsRenderInformation) {
                    if (wayPointRenderInfo.selected || isCarMode) {
                        // use largest presentation
                        renderWayPointInformation(batch, GL_UISizes.wayPointSizes[2], GL_UISizes.underlaySizes[2], wayPointRenderInfo);
                    } else {
                        renderWayPointInformation(batch, wpUnderlay, wpSize, wayPointRenderInfo);
                    }
                }
            }
        }
        outScreenDraw = 0;
    }

    public void renderWayPointInformation(Batch batch, SizeF wpUnderlay, SizeF wpSize, WayPointRenderInfo wayPointRenderInfo) {
        Vector2 screen = worldToScreen(new Vector2(wayPointRenderInfo.mapX, wayPointRenderInfo.mapY));

        screen.y = screen.y - ySpeedVersatz;
        if (myPointOnScreen != null && showDirectLine && (wayPointRenderInfo.selected) && (wayPointRenderInfo.waypoint == GlobalCore.getSelectedWayPoint())) {
            if (directLine == null)
                directLine = new PolygonDrawable(directLinePaint, getMapIntWidth(), getMapIntHeight());
            // FIXME render only if visible on screen (intersect the screen rec)
            directLine.setVerticesAndTriangles(new Quadrangle(myPointOnScreen.x, myPointOnScreen.y, screen.x, screen.y, 3 * UiSizes.getInstance().getScale()));
            directLine.draw(batch, 0, 0, getMapIntWidth(), getMapIntHeight(), 0);
        }

        // Don't render if outside of screen !!
        if ((screen.x < 0 - wpSize.getWidth() || screen.x > getWidth() + wpSize.getHeight()) || (screen.y < 0 - wpSize.getHeight() || screen.y > getHeight() + wpSize.getHeight())) {
            if (wayPointRenderInfo.cache != null && (wayPointRenderInfo.cache.generatedId == infoBubble.getCacheId()) && infoBubble.isVisible()) {
                // check if wp selected
                if (wayPointRenderInfo.waypoint != null && wayPointRenderInfo.waypoint.equals(infoBubble.getWaypoint()) || wayPointRenderInfo.waypoint == null && infoBubble.getWaypoint() == null)
                    infoBubble.setInvisible();
            }
            return;
        }

        if (showDistanceCircle) {
            if (currentZoom >= 15) {
                if (wayPointRenderInfo.showDistanceCircle()) {
                    if (distanceCircle == null)
                        distanceCircle = new CircleDrawable(0, 0, pixelsPerMeter * 161, distanceCirclePaint, getMapIntWidth(), getMapIntHeight());
                    distanceCircle.setPosition(screen.x, screen.y, pixelsPerMeter * 161);
                    distanceCircle.draw(batch, 0, 0, getWidth(), getHeight(), 0);
                }
            }
        }


        boolean drawAsWaypoint = wayPointRenderInfo.waypoint != null;
        float nameYMovement = 0;
        // Beschriftung
        if (wayPointRenderInfo.cache != null && showTitles && (currentZoom >= 15)) {
            try {
                String name;
                float wpOffset = 0;
                if (drawAsWaypoint) {
                    name = wayPointRenderInfo.waypoint.getTitleForGui();
                    if (wayPointRenderInfo.cache.getCoordinate().equals(wayPointRenderInfo.waypoint.getCoordinate()))
                        wpOffset = wpUnderlay.getHeight();
                } else name = wayPointRenderInfo.cache.getGeoCacheName();

                if (geocacheOrWayPointName == null)
                    geocacheOrWayPointName = new GlyphLayout(Fonts.getNormal(), name);
                else
                    geocacheOrWayPointName.setText(Fonts.getNormal(), name);

                float halfWidth = geocacheOrWayPointName.width / 2;
                Fonts.getNormal().draw(batch, geocacheOrWayPointName, screen.x - halfWidth, screen.y - wpUnderlay.getHalfHeight() - wpOffset - nameYMovement);
            } catch (Exception ignored) {
            }
        }

        if ((currentZoom >= zoomCross) && (wayPointRenderInfo.selected) && (wayPointRenderInfo.waypoint == GlobalCore.getSelectedWayPoint())) {
            // Draw Cross and move screen vector
            Sprite cross = getMapOverlay(IconName.cross);
            cross.setBounds(screen.x - wpUnderlay.getHalfWidth(), screen.y - wpUnderlay.getHalfHeight(), wpUnderlay.getWidth(), wpUnderlay.getHeight());
            cross.draw(batch);

            screen.add(-wpUnderlay.getWidth(), wpUnderlay.getHeight());
            nameYMovement = wpUnderlay.getHeight();
        }

        if (wayPointRenderInfo.underlayIcon != null) {
            wayPointRenderInfo.underlayIcon.setBounds(screen.x - wpUnderlay.getHalfWidth(), screen.y - wpUnderlay.getHalfHeight(), wpUnderlay.getWidth(), wpUnderlay.getHeight());
            wayPointRenderInfo.underlayIcon.draw(batch);
        }
        if (wayPointRenderInfo.icon != null) {
            wayPointRenderInfo.icon.setBounds(screen.x - wpSize.getHalfWidth(), screen.y - wpSize.getHalfHeight(), wpSize.getWidth(), wpSize.getHeight());
            wayPointRenderInfo.icon.draw(batch);
        }

        // draw Favorite symbol
        if (wayPointRenderInfo.cache != null && wayPointRenderInfo.cache.isFavorite()) {
            batch.draw(getSprite(IconName.favorit.name()), screen.x + (wpSize.getHalfWidth() / 2), screen.y + (wpSize.getHalfHeight() / 2), wpSize.getWidth(), wpSize.getHeight());
        }

        if (wayPointRenderInfo.overlayIcon != null) {
            wayPointRenderInfo.overlayIcon.setBounds(screen.x - wpUnderlay.getHalfWidth(), screen.y - wpUnderlay.getHalfHeight(), wpUnderlay.getWidth(), wpUnderlay.getHeight());
            wayPointRenderInfo.overlayIcon.draw(batch);
        }

        // Rating des Caches darstellen
        if (wayPointRenderInfo.cache != null && showRating && (!drawAsWaypoint) && (wayPointRenderInfo.cache.gcVoteRating > 0) && (currentZoom >= 15)) {
            Sprite rating = MapStars.get((int) Math.min(wayPointRenderInfo.cache.gcVoteRating * 2, 5 * 2));
            rating.setBounds(screen.x - wpUnderlay.getHalfWidth(), screen.y - wpUnderlay.getHalfHeight() - wpUnderlay.getHeight48(), wpUnderlay.getWidth(), wpUnderlay.getHeight48());
            rating.setOrigin(wpUnderlay.getWidth() / 2, wpUnderlay.getHeight48() / 2);
            rating.setRotation(0);
            rating.draw(batch);
            nameYMovement += wpUnderlay.getHeight48();
        }

        // Show D/T-Rating
        if (wayPointRenderInfo.cache != null && showDT && (!drawAsWaypoint) && (currentZoom >= 15)) {
            Sprite difficulty = MapStars.get((int) Math.min(wayPointRenderInfo.cache.getDifficulty() * 2, 5 * 2));
            difficulty.setBounds(screen.x - wpUnderlay.getWidth() - GL_UISizes.infoShadowHeight, screen.y - (wpUnderlay.getHeight48() / 2), wpUnderlay.getWidth(), wpUnderlay.getHeight48());
            difficulty.setOrigin(wpUnderlay.getWidth() / 2, wpUnderlay.getHeight48() / 2);
            difficulty.setRotation(90);
            difficulty.draw(batch);

            Sprite terrain = MapStars.get((int) Math.min(wayPointRenderInfo.cache.getTerrain() * 2, 5 * 2));
            terrain.setBounds(screen.x + GL_UISizes.infoShadowHeight, screen.y - (wpUnderlay.getHeight48() / 2), wpUnderlay.getWidth(), wpUnderlay.getHeight48());
            terrain.setOrigin(wpUnderlay.getWidth() / 2, wpUnderlay.getHeight48() / 2);
            terrain.setRotation(90);
            terrain.draw(batch);
        }

        if (wayPointRenderInfo.cache != null && (wayPointRenderInfo.cache.generatedId == infoBubble.getCacheId()) && infoBubble.isVisible()) {
            if (infoBubble.getWaypoint() == wayPointRenderInfo.waypoint) {
                infoBubble.setPos(screen.x - infoBubble.getHalfWidth(), screen.y);
            }
        }

    }

    @Override
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        if (cache == null)
            return;
        try {
            // remember, we are dealing with objects
            if ((cache == lastSelectedCache) && (waypoint == lastSelectedWaypoint)) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        lastSelectedCache = cache;
        lastSelectedWaypoint = waypoint;

        MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(getMapIntWidth(), getMapIntHeight())), currentZoom, true);
        data.hideMyFinds = hideMyFinds;
        data.showAllWaypoints = showAllWaypoints;
        data.showAtOriginalPosition = showAtOriginalPosition;
        mapCacheList.update(data);

        if (mapStateIsNotFreeOrWp()) {
            positionChanged();
            return;
        }

        positionInitialized = true;

        if (mapState != MapState.WP)
            setMapState(MapState.FREE);
        try {
            CoordinateGPS target = (waypoint != null) ? new CoordinateGPS(waypoint.getLatitude(), waypoint.getLongitude()) : new CoordinateGPS(cache.getCoordinate().getLatitude(), cache.getCoordinate().getLongitude());
            setCenter(target);
        } catch (Exception ignored) {
        }

        GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);

        // für 2sec rendern lassen, bis Änderungen der WPI-list neu berechnet wurden
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                GL.that.removeRenderView(MapView.this);
            }
        }, 2000);

        positionChanged();
    }

    @Override
    public void speedChanged() {
        if (mapInfoPanel != null) {
            mapInfoPanel.setSpeed(Locator.getInstance().SpeedString());

            if (mapState == MapState.CAR && Config.dynamicZoom.getValue()) {
                // calculate dynamic Zoom

                double maxSpeed = Config.MoveMapCenterMaxSpeed.getValue();
                int maxZoom = Config.dynamicZoomLevelMax.getValue();
                int minZoom = Config.dynamicZoomLevelMin.getValue();

                double percent = Locator.getInstance().speedOverGround() / maxSpeed;

                float dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
                if (dynZoom > maxZoom)
                    dynZoom = maxZoom;
                if (dynZoom < minZoom)
                    dynZoom = minZoom;

                if (lastDynamicZoom != dynZoom) {
                    lastDynamicZoom = dynZoom;
                    zoomBtn.setZoom((int) lastDynamicZoom);
                    inputState = InputState.Idle;

                    kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

                    GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
                    renderOnce("SpeedChanged");
                    calcPixelsPerMeter();
                }
            }
        }

    }

    // Create new Waypoint at screen center
    public void createWaypointAtCenter() {
        String newGcCode;
        try {
            newGcCode = Database.Data.createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
        } catch (Exception e) {
            return;
        }
        Coordinate coord = center;
        if ((coord == null) || (!coord.isValid()))
            coord = Locator.getInstance().getMyPosition();
        if ((coord == null) || (!coord.isValid()))
            return;
        //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
        Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);

        EditWaypoint EdWp = new EditWaypoint(newWP, waypoint -> {
            if (waypoint != null) {

                GlobalCore.getSelectedCache().getWayPoints().add(waypoint);
                GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
                if (waypoint.isStartWaypoint) {
                    // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                    // definiert
                    // ist!!!
                    WaypointDAO wpd = new WaypointDAO();
                    wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), waypoint);
                }
                WaypointDAO waypointDAO = new WaypointDAO();
                waypointDAO.WriteToDatabase(waypoint);

                // informiere WaypointListView über Änderung
                WaypointListChangedEventList.Call(GlobalCore.getSelectedCache());
                GL.that.renderOnce();
            }
        }, true, false);
        EdWp.show();

    }

    @Override
    public void dispose() {
        CacheSelectionChangedListeners.getInstance().remove(this);
        super.dispose();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        targetArrowScreenRec = null;
        distanceCircle = null;
        directLine = null;
    }

    @Override
    public void onHide() {
        // Log.debug(sKlasse, "Map gets invisible");
        CacheSelectionChangedListeners.getInstance().remove(this);
        super.onHide();
    }

    @Override
    protected void updateCacheList(boolean force) {
        // force is for zoom
        if (lastScreenCenter.equals(screenCenterWorld) && !force) {
            return;
        }
        lastScreenCenter.set(screenCenterWorld);
        MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(getMapIntWidth(), getMapIntHeight())), currentZoom, false);
        data.hideMyFinds = hideMyFinds;
        data.showAllWaypoints = showAllWaypoints;
        data.showAtOriginalPosition = showAtOriginalPosition;
        mapCacheList.update(data);
    }

    protected void directLoadTiles(Descriptor lowerTile, Descriptor upperTile, int aktZoom) {
        // Log.debug(sKlasse, "directLoadTiles");
        if (Energy.isDisplayOff()) return;
        if (isCreated) {

            MapTileLoader.finishYourself.set(true);
            while (MapTileLoader.isWorking.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            MapTileLoader.finishYourself.set(false);

            MapTileLoader.isWorking.set(true);
            MapTileLoader.getInstance().loadTiles(this, lowerTile, upperTile, aktZoom);
            MapTileLoader.isWorking.set(false);


            if (isCarMode && CB_UI_Settings.liveMapEnabled.getValue()) {
                LiveMapQue.getInstance().setCenterDescriptor(center);
                // LiveMap queue complete screen
                lowerTile.setData(center);
                LiveMapQue.getInstance().queScreen(lowerTile, upperTile);
            }

        }
    }

    @Override
    public void setCenter(CoordinateGPS value) {
        if (mapMode == MapMode.Normal)
            mapInfoPanel.setCoord(value);
        super.setCenter(value);
    }

    @Override
    public void orientationChanged() {
        super.orientationChanged();
        if (mapInfoPanel != null) {
            try {
                Coordinate position = Locator.getInstance().getMyPosition();
                if (GlobalCore.isSetSelectedCache()) {
                    Coordinate dest = (GlobalCore.getSelectedWayPoint() != null) ? GlobalCore.getSelectedWayPoint().getCoordinate() : GlobalCore.getSelectedCache().getCoordinate();
                    if (dest == null)
                        return;
                    float heading = Locator.getInstance().getHeading();

                    float[] result = new float[2];
                    MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);
                    float bearing = result[1];
                    float relativeBearing = bearing - heading;
                    mapInfoPanel.setBearing(relativeBearing, heading);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void initializeMap() {
        // Log.debug(sKlasse, "initializeMap");
        zoomCross = Config.ZoomCross.getValue();
        super.initializeMap();
    }

    @Override
    protected void setZoomScale(int zoom) {
        if (mapMode == MapMode.Normal)
            zoomScale.setZoom(zoom);
        if (mapMode == MapMode.Normal)
            mapScale.zoomChanged();
    }

    @Override
    protected void calcCenter() {
        super.calcCenter();
        if (mapMode == MapMode.Normal) {
            mapInfoPanel.setCoord(center);
        }
    }

    @Override
    public void requestLayout() {
        // Log.debug(sKlasse, "requestLayout");

        if (isDisposed()) return;

        float margin = GL_UISizes.margin;

        float infoHeight = 0;
        if (mapMode == MapMode.Normal) {
            mapInfoPanel.setPos(margin, getMapIntHeight() - margin - mapInfoPanel.getHeight());
            mapInfoPanel.setVisible(Config.showInfo.getValue());
            infoHeight = mapInfoPanel.getHeight();
        }
        btnMapState.setPos(getMapIntWidth() - margin - btnMapState.getWidth(), getMapIntHeight() - margin - btnMapState.getHeight());

        if (Config.disableLiveMap.getValue()) {
            liveButton.setInvisible();
        } else {
            liveButton.setVisible();
        }

        liveButton.setRec(btnMapState);
        liveButton.setY(btnMapState.getY() - margin - liveButton.getHeight());

        zoomScale.setSize((float) (44.6666667 * GL_UISizes.dpi), getHeight() - infoHeight - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());

        GL.that.renderOnce();
    }

    public void setBtnMapStateToFree() {
        btnMapState.setState(MapState.FREE.ordinal());
    }

    @Override
    public void setMapState(MapState state) {
        if (mapState == state)
            return;
        // Log.debug(sKlasse, "setMapState :" + state);

        Config.lastMapToggleBtnState.setValue(state.ordinal());
        Config.AcceptChanges();

        boolean wasCarMode = isCarMode;

        if (mapMode == MapMode.Normal) {
            mapInfoPanel.setCoordType(CoordType.Map);
            switch (state) {
                case CAR:
                    if (!wasCarMode) {
                        mapInfoPanel.setCoordType(CoordType.GPS);
                    }
                    break;
                case WP:
                    mapInfoPanel.setCoordType(CoordType.Cache);
                    break;
                case LOCK:
                case GPS:
                    mapInfoPanel.setCoordType(CoordType.GPS);
                    break;
            }
        }

        super.setMapState(state);

        if (btnMapState.getState() != state.ordinal()) {
            btnMapState.setState(state.ordinal());
        }
    }

    @Override
    public void positionChanged() {
        // Log.debug(sKlasse, "positionChanged");
        if (isCarMode) {
            // im CarMode keine Netzwerk Koordinaten zulassen
            if (!Locator.getInstance().isGPSprovided())
                return;
        }

        super.positionChanged();

        if (mapInfoPanel != null) {
            if (center != null) {
                mapInfoPanel.setCoord(center);
            }

            if (GlobalCore.getSelectedCoordinate() != null) {
                mapInfoPanel.setDistance(GlobalCore.getSelectedCoordinate().distance(CalculationType.ACCURATE));
            }
            orientationChanged();
        }

        if (mapMode == MapMode.Compass) {
            // Berechne den Zoom so, dass eigene Position und WP auf der Map zu sehen sind.
            // if ((GlobalCore.Marker != null) && (GlobalCore.Marker.isValid)) position = GlobalCore.Marker;
            Coordinate position = Locator.getInstance().getMyPosition();

            float distance = -1;
            if (GlobalCore.isSetSelectedCache() && position.isValid()) {
                try {
                    if (GlobalCore.getSelectedWayPoint() == null)
                        distance = position.distance(GlobalCore.getSelectedCache().getCoordinate(), CalculationType.ACCURATE);
                    else
                        distance = position.distance(GlobalCore.getSelectedWayPoint().getCoordinate(), CalculationType.ACCURATE);
                } catch (Exception e) {
                    distance = 10;
                }
            }
            int setZoomTo = zoomBtn.getMinZoom();

            if (distanceZoomLevel != null) {
                for (int i = zoomBtn.getMaxZoom(); i > zoomBtn.getMinZoom(); i--) {
                    if (distance < distanceZoomLevel.get(i)) {
                        setZoomTo = i;
                        break;
                    }
                }
            }

            if (camera != null) {
                if (setZoomTo != lastCompassMapZoom) {
                    lastCompassMapZoom = setZoomTo;
                    zoomBtn.setZoom(setZoomTo);
                    inputState = InputState.Idle;

                    kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(setZoomTo), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

                    GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
                    renderOnce("PositionChanged");
                    calcPixelsPerMeter();
                }
            }
        }
    }

    @Override
    protected void skinIsChanged() {
        // Log.debug(sKlasse, "skinIsChanged");
        super.skinIsChanged();
        MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(getMapIntWidth(), getMapIntHeight())), currentZoom, true);
        data.hideMyFinds = hideMyFinds;
        data.showAllWaypoints = showAllWaypoints;
        data.showAtOriginalPosition = showAtOriginalPosition;
        mapCacheList.update(data);
        if (infoBubble.isVisible()) {
            infoBubble.setCache(infoBubble.getCache(), infoBubble.getWaypoint(), true);
        }
    }

    @Override
    public void onStop() {
        if (mapMode == MapMode.Normal) // save last zoom and position only from Map, not from CompassMap
        {
            super.onStop();
        }
    }

    @Override
    public void onShow() {
        // Log.debug(sKlasse, "onShow");
        super.onShow();
        CacheSelectionChangedListeners.getInstance().addListener(this);
        isNorthOriented = mapMode == MapMode.Normal ? Config.isMapNorthOriented.getValue() : false;
        handleCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWayPoint());
    }

    /**
     * setNewSettings
     */
    @Override
    public void setNewSettings(int InitialFlags) {
        // Log.debug(sKlasse, "setNewSettings");
        if ((InitialFlags & INITIAL_SETTINGS) != 0) {
            showRating = mapMode == MapMode.Compass ? false : Config.showRating.getValue();
            showDT = mapMode == MapMode.Compass ? false : Config.showDifficultyTerrain.getValue();
            showTitles = mapMode == MapMode.Compass ? false : Config.showTitles.getValue();
            hideMyFinds = Config.hideMyFinds.getValue();
            showDirectLine = mapMode == MapMode.Compass ? false : Config.showDirectLine.getValue();
            showAllWaypoints = mapMode == MapMode.Compass ? false : Config.showAllWaypoints.getValue();
            showAccuracyCircle = mapMode == MapMode.Compass ? false : Config.showAccuracyCircle.getValue();
            showMapCenterCross = mapMode == MapMode.Compass ? false : Config.showMapCenterCross.getValue();
            showAtOriginalPosition = mapMode == MapMode.Compass ? false : Config.showAtOriginalPosition.getValue();
            showDistanceCircle = mapMode == MapMode.Compass ? false : Config.showDistanceCircle.getValue();
            showDistanceToCenter = mapMode == MapMode.Compass ? false : Config.showDistanceToCenter.getValue();

            if (mapMode == MapMode.Track) {
                showMapCenterCross = true;
                setMapState(MapState.FREE);
            }

            if (mapInfoPanel != null)
                mapInfoPanel.setVisible(mapMode == MapMode.Compass ? false : Config.showInfo.getValue());

            if (InitialFlags == INITIAL_ALL) {

                if (Config.mapViewDPIFaktor.getValue() == 1) {
                    Config.mapViewDPIFaktor.setValue(displayDensity);
                    Config.AcceptChanges();
                }
                iconFactor = Config.mapViewDPIFaktor.getValue();

                int setMaxZoom = mapMode == MapMode.Compass ? Config.CompassMapMaxZommLevel.getValue() : Config.OsmMaxLevel.getValue();
                int setMinZoom = mapMode == MapMode.Compass ? Config.CompassMapMinZoomLevel.getValue() : Config.OsmMinLevel.getValue();

                zoomBtn.setMaxZoom(setMaxZoom);
                zoomBtn.setMinZoom(setMinZoom);
                zoomBtn.setZoom(Config.lastZoomLevel.getValue());

                zoomScale.setMaxZoom(setMaxZoom);
                zoomScale.setMinZoom(setMinZoom);

                if (mapMode == MapMode.Compass) {
                    // Berechne die darstellbare Entfernung für jedes ZoomLevel
                    distanceZoomLevel = new TreeMap<>();

                    int possiblePixel = (int) getHalfHeight();

                    for (int i = setMaxZoom; i > setMinZoom; i--) {
                        float PixelForZoomLevel = getPixelsPerMeter(i);
                        distanceZoomLevel.put(i, (int) (possiblePixel / PixelForZoomLevel));
                    }
                }

            }
        }

        if ((InitialFlags & INITIAL_THEME) != 0) {
            if (MapTileLoader.getInstance().getCurrentLayer() != null) {
                if (MapTileLoader.getInstance().getCurrentLayer().isMapsForge()) {
                    Log.info(sKlasse, "modify layer " + MapTileLoader.getInstance().getCurrentLayer().getName() + " for mapview " + mapMode);
                    MapTileLoader.getInstance().modifyCurrentLayer(isCarMode);
                    lastDescriptorOrdered = new Descriptor(0, 0, 10);
                    renderOnce("INITIAL_THEME");
                }
            }
            if (MapTileLoader.getInstance().getCurrentOverlayLayer() != null) {
                if (MapTileLoader.getInstance().getCurrentOverlayLayer().isMapsForge()) {
                    lastDescriptorOrdered = new Descriptor(0, 0, 10);
                    // until now there are only Online Overlays
                    // ((MapsForgeLayer) MapTileLoader.getInstance().getCurrentOverlayLayer()).initTheme(isCarMode);
                }
            }
        }

        if ((InitialFlags & INITIAL_WP_LIST) != 0) {
            if (mapCacheList != null) {
                MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(getMapIntWidth(), getMapIntHeight())), currentZoom, true);
                hideMyFinds = Config.hideMyFinds.getValue();
                data.hideMyFinds = hideMyFinds;
                showAllWaypoints = mapMode == MapMode.Compass ? false : Config.showAllWaypoints.getValue();
                data.showAllWaypoints = showAllWaypoints;
                showAtOriginalPosition = mapMode == MapMode.Compass ? false : Config.showAtOriginalPosition.getValue();
                data.showAtOriginalPosition = showAtOriginalPosition;
                mapCacheList.update(data);
            }
        }
    }

    @Override
    protected void setInitialLocation() {
        // Log.debug(sKlasse, "setInitialLocation");
        try {
            if (Database.Data != null) {
                if (Database.Data.cacheList != null) {
                    synchronized (Database.Data.cacheList) {
                        if (Database.Data.cacheList.size() > 0) {
                            // Koordinaten des ersten Caches der Datenbank
                            // nehmen
                            setCenter(new CoordinateGPS(Database.Data.cacheList.get(0).getCoordinate().getLatitude(), Database.Data.cacheList.get(0).getCoordinate().getLongitude()));
                            positionInitialized = true;
                            // setLockPosition(0);
                        } else {
                            // Wenns auch den nicht gibt...)
                            setCenter(new CoordinateGPS(48.0, 12.0));
                        }
                    }
                } else {
                    // Wenn cacheList == null
                    setCenter(new CoordinateGPS(48.0, 12.0));
                }
            } else {
                // Wenn Data == null
                setCenter(new CoordinateGPS(48.0, 12.0));
            }
        } catch (Exception e) {
            setCenter(new CoordinateGPS(48.0, 12.0));
        }
    }

    @Override
    public void mapStateChangedToWP() {
        // Log.debug(sKlasse, "mapStateChangedToWP");
        if (GlobalCore.isSetSelectedCache()) {
            if (GlobalCore.getSelectedWayPoint() != null) {
                Coordinate tmp = GlobalCore.getSelectedWayPoint().getCoordinate();
                setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
            } else {
                Coordinate tmp = GlobalCore.getSelectedCache().getCoordinate();
                setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
            }
        }
    }

    @Override
    public void setAlignToCompass(boolean value) {
        // Log.debug(sKlasse, "setAlignToCompass");
        super.setAlignToCompass(value);
        Config.isMapNorthOriented.setValue(!value);
    }

    private void onResume() {
        // Log.debug(sKlasse, "onResume");
        MapView.this.renderOnce("OnResumeListeners");
    }

    public enum MapMode {
        Normal, Compass, Track
    }

}
