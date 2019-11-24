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
import de.droidcachebox.AbstractGlobal;
import de.droidcachebox.Energy;
import de.droidcachebox.OnResumeListeners;
import de.droidcachebox.WaypointListChangedEventList;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.core.LiveMapQue;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites.*;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.MapInfoPanel.CoordType;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.graphics.KineticZoom;
import de.droidcachebox.gdx.graphics.PolygonDrawable;
import de.droidcachebox.gdx.math.*;
import de.droidcachebox.gdx.views.MapViewCacheList.MapViewCacheListUpdateData;
import de.droidcachebox.gdx.views.MapViewCacheList.WaypointRenderInfo;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.map.MapScale;
import de.droidcachebox.locator.map.*;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.PointL;
import de.droidcachebox.utils.log.Log;

import java.util.*;

import static de.droidcachebox.core.GroundspeakAPI.updateGeoCache;
import static de.droidcachebox.gdx.Sprites.*;

public class MapView extends MapViewBase implements de.droidcachebox.SelectedCacheChangedEventListener, PositionChangedEvent {
    private static final String log = "MapView";
    private CB_RectF TargetArrow = new CB_RectF();
    private SortedMap<Integer, Integer> DistanceZoomLevel;
    private MapMode Mode;
    private LiveButton liveButton;
    private MultiToggleButton togBtn;
    private InfoBubble infoBubble;
    private CancelWaitDialog wd = null;
    private int zoomCross = 16;
    private boolean showRating;
    private boolean showDT;
    private boolean showTitles;
    private boolean hideMyFinds;
    private boolean showDirectLine;
    private boolean showAllWaypoints;
    private GL_Paint paint;
    private Cache lastSelectedCache = null;
    private Waypoint lastSelectedWaypoint = null;
    private GlyphLayout layout = null;
    private CB_RectF TargetArrowScreenRec;
    private MapViewCacheList mapCacheList;
    private int lastCompassMapZoom = -1;
    private MapInfoPanel info;
    private PointL lastScreenCenter;

    public MapView(CB_RectF cb_RectF, MapMode Mode) {
        super(cb_RectF, Mode.name());
        lastScreenCenter = new PointL(0, 0);
        this.Mode = Mode;
        Log.info(log, "creating Mapview for " + Mode + " map");
        mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);

        if (Mode != MapMode.Normal) {
            setOnDoubleClickListener((v, x, y, pointer, button) -> {
                if (this.Mode == MapMode.Track)
                    setMapState(MapState.FREE);
                else setMapState(MapState.GPS);
                // Center own position!
                setCenter(Locator.getInstance().getMyPosition());
                return true;
            });
        }

        de.droidcachebox.Config.MapsforgeDayTheme.addSettingChangedListener(themeChangedEventHandler);
        de.droidcachebox.Config.MapsforgeNightTheme.addSettingChangedListener(themeChangedEventHandler);
        de.droidcachebox.Config.MapsforgeCarDayTheme.addSettingChangedListener(themeChangedEventHandler);
        de.droidcachebox.Config.MapsforgeCarNightTheme.addSettingChangedListener(themeChangedEventHandler);

        registerSkinChangedEvent();

        setBackground(ListBack);

        // calculate max Map Tile cache
        try {
            int aTile = 256 * 256;
            maxTilesPerScreen = (int) ((getWidth() * getHeight()) / aTile + 0.5);
            maxNumTiles = (int) (maxTilesPerScreen * 6);// 6 times as much as necessary

        } catch (Exception e) {
            maxNumTiles = 60;
        }
        maxNumTiles = Math.min(maxNumTiles, 60);
        maxNumTiles = Math.max(maxNumTiles, 20);
        // maxNumTiles between 20 and 60
        mapTileLoader = new MapTileLoader(maxNumTiles);

        mapScale = new MapScale(new CB_RectF(GL_UISizes.margin, GL_UISizes.margin, getHalfWidth(), GL_UISizes.ZoomBtn.getHalfWidth() / 4), "mapScale", this, de.droidcachebox.Config.ImperialUnits.getValue());

        if (Mode == MapMode.Normal) {
            addChild(mapScale);
        } else {
            mapScale.setInvisible();
        }

        // initial Zoom Buttons
        zoomBtn = new ZoomButtons(GL_UISizes.ZoomBtn, this, "ZoomButtons");

        zoomBtn.setX(getWidth() - (zoomBtn.getWidth() + UiSizes.getInstance().getMargin()));

        zoomBtn.setOnClickListenerDown((v, x, y, pointer, button) -> {
            // bei einer Zoom Animation in negativer Richtung muss der setDiffCameraZoom gesetzt werden!
            // zoomScale.setDiffCameraZoom(-1.9f, true);
            // zoomScale.setZoom(zoomBtn.getZoom());
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

        if (Mode == MapMode.Compass) {
            zoomBtn.setInvisible();
        } else {
            addChild(zoomBtn);
            zoomBtn.setMinimumFadeValue(0.25f);
        }

        setClickHandler((v, x, y, pointer, button) -> {
            WaypointRenderInfo minWpi = null;

            if (TargetArrow != null && TargetArrow.contains(x, y)) {
                if (de.droidcachebox.GlobalCore.isSetSelectedCache()) {
                    if (de.droidcachebox.GlobalCore.getSelectedWaypoint() != null) {
                        Coordinate tmp = de.droidcachebox.GlobalCore.getSelectedWaypoint().Pos;
                        setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
                    } else {
                        Coordinate tmp = de.droidcachebox.GlobalCore.getSelectedCache().coordinate;
                        setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
                    }

                    // switch map state to WP
                    togBtn.setState(2);
                }
                return false;
            }

            synchronized (mapCacheList.list) {
                double minDist = Double.MAX_VALUE;
                for (int i = 0, n = mapCacheList.list.size(); i < n; i++) {
                    WaypointRenderInfo wpi = mapCacheList.list.get(i);
                    Vector2 screen = worldToScreen(new Vector2(Math.round(wpi.MapX), Math.round(wpi.MapY)));
                    double aktDist = Math.sqrt(Math.pow(screen.x - x, 2) + Math.pow(screen.y - y, 2));
                    if (aktDist < minDist) {
                        minDist = aktDist;
                        minWpi = wpi;
                    }
                }
                // empty mapCacheList
                if (minWpi == null || minWpi.Cache == null) {
                    return true;
                }
                // always hide the bubble
                if (infoBubble.isVisible()) {
                    infoBubble.setInvisible();
                }
                // check for showing the bubble
                if (minDist < 40) {
                    if (minWpi.Waypoint != null) {
                        if (de.droidcachebox.GlobalCore.getSelectedCache() != minWpi.Cache) {
                            // show bubble at the location of the waypoint!!!
                            infoBubble.setCache(minWpi.Cache, minWpi.Waypoint);
                            infoBubble.setVisible();
                        } else {
                            // if only waypoint changes, the bubble will not be shown. (why not ?)
                            // so we must do the selection here
                            de.droidcachebox.GlobalCore.setSelectedWaypoint(minWpi.Cache, minWpi.Waypoint);
                            MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
                            data.hideMyFinds = hideMyFinds;
                            data.showAllWaypoints = showAllWaypoints;
                            data.showAtOriginalPosition = showAtOriginalPosition;
                            mapCacheList.update(data);
                        }
                    } else {
                        // show bubble
                        infoBubble.setCache(minWpi.Cache, null);
                        infoBubble.setVisible();
                    }
                    inputState = InputState.Idle;
                }
            }
            return false;
        });

        float InfoHeight = 0;
        if (Mode == MapMode.Normal) {
            info = (MapInfoPanel) addChild(new MapInfoPanel(GL_UISizes.Info, "InfoPanel", this));
            InfoHeight = info.getHeight();
        }

        CB_RectF ZoomScaleRec = new CB_RectF();
        ZoomScaleRec.setSize((float) (44.6666667 * GL_UISizes.DPI), getHeight() - InfoHeight - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());
        ZoomScaleRec.setPos(new Vector2(GL_UISizes.margin, zoomBtn.getMaxY() + GL_UISizes.margin));

        zoomScale = new ZoomScale(ZoomScaleRec, "zoomScale", 2, 21, 12);
        if (Mode == MapMode.Normal)
            addChild(zoomScale);

        mapIntWidth = (int) getWidth();
        mapIntHeight = (int) getHeight();
        midVector2 = new Vector2((float) mapIntWidth / 2f, (float) mapIntHeight / 2f);

        drawingWidth = mapIntWidth;
        drawingHeight = mapIntHeight;


        if (mapTileLoader.getCurrentLayer() == null) {
            mapTileLoader.setCurrentLayer(LayerManager.getInstance().getLayer(de.droidcachebox.Config.currentMapLayer.getValue()), isCarMode);
        }
        String[] currentOverlayLayerName = new String[]{de.droidcachebox.Config.CurrentMapOverlayLayerName.getValue()};
        if (mapTileLoader.getCurrentOverlayLayer() == null && currentOverlayLayerName[0].length() > 0)
            mapTileLoader.setCurrentOverlayLayer(LayerManager.getInstance().getOverlayLayer(currentOverlayLayerName));
        initializeMap();

        // initial Zoom Scale
        // zoomScale = new GL_ZoomScale(6, 20, 13);

        // from create

        if (de.droidcachebox.Config.MapViewDPIFaktor.getValue() == 1) {
            de.droidcachebox.Config.MapViewDPIFaktor.setValue(AbstractGlobal.displayDensity);
            de.droidcachebox.Config.AcceptChanges();
        }
        iconFactor = de.droidcachebox.Config.MapViewDPIFaktor.getValue();

        liveButton = new LiveButton();
        liveButton.setState(de.droidcachebox.Config.LiveMapEnabeld.getDefaultValue());
        de.droidcachebox.Config.DisableLiveMap.addSettingChangedListener(this::requestLayout);

        togBtn = new MultiToggleButton(GL_UISizes.Toggle, "toggle");

        togBtn.addState("Free", new HSV_Color(Color.GRAY));
        togBtn.addState("GPS", new HSV_Color(Color.GREEN));
        togBtn.addState("WP", new HSV_Color(Color.MAGENTA));
        togBtn.addState("Lock", new HSV_Color(Color.RED));
        togBtn.addState("Car", new HSV_Color(Color.YELLOW));
        togBtn.setLastStateWithLongClick(true);

        MapState last = MapState.values()[de.droidcachebox.Config.LastMapToggleBtnState.getValue()];
        togBtn.setState(last.ordinal());

        togBtn.setOnStateChangedListener((v, State) -> setMapState(MapState.values()[State]));
        togBtn.registerSkinChangedEvent();

        switch (Mode) {
            case Compass:
                setMapState(MapState.GPS);
                break;
            case Normal:
                setMapState(last);
                break;
            case Track:
                setMapState(MapState.FREE);
                break;
        }

        if (Mode == MapMode.Normal) {
            switch (de.droidcachebox.Config.LastMapToggleBtnState.getValue()) {
                case 0:
                    info.setCoordType(CoordType.Map);
                    break;
                case 1:
                case 3:
                case 4:
                    info.setCoordType(CoordType.GPS);
                    break;
                case 2:
                    info.setCoordType(CoordType.Cache);
                    break;
            }
        }

        if (Mode == MapMode.Normal) {
            addChild(togBtn);
            if (de.droidcachebox.Config.DisableLiveMap.getValue()) {
                liveButton.setState(false);
            }
            addChild(liveButton);
        }

        infoBubble = new InfoBubble(GL_UISizes.Bubble, "infoBubble");
        infoBubble.setInvisible();
        infoBubble.setClickHandler((v, x, y, pointer, button) -> {
            if (infoBubble.saveButtonClicked(x, y)) {
                wd = CancelWaitDialog.ShowWait(Translation.get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), () -> {

                }, new ICancelRunnable() {

                    @Override
                    public void run() {

                        String GCCode = infoBubble.getCache().getGcCode();
                        ArrayList<GroundspeakAPI.GeoCacheRelated> geoCacheRelateds = updateGeoCache(infoBubble.getCache());
                        if (geoCacheRelateds.size() > 0) {
                            try {
                                WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds, null);
                            } catch (InterruptedException ex) {
                                Log.err(log, "WriteIntoDB.CachesAndLogsAndImagesIntoDB", ex);
                            }
                        } else {
                            String msg = "No Cache loaded: \n remaining Full:" + GroundspeakAPI.fetchMyUserInfos().remaining + "\n remaining Lite:" + GroundspeakAPI.fetchMyUserInfos().remainingLite;
                            GL.that.Toast(msg);
                        }

                        // Reload result from DB
                        synchronized (Database.Data.cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(de.droidcachebox.Config.GcLogin.getValue());
                            Database.Data.cacheList = CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, de.droidcachebox.Config.ShowAllWaypoints.getValue());
                        }
                        CacheListChangedListeners.getInstance().cacheListChanged();

                        Cache selCache = Database.Data.cacheList.getCacheByGcCodeFromCacheList(GCCode);
                        de.droidcachebox.GlobalCore.setSelectedCache(selCache);
                        infoBubble.setCache(selCache, null, true);
                        wd.close();
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
                    de.droidcachebox.GlobalCore.setSelectedWaypoint(infoBubble.getCache(), waypoint);
                } else {
                    de.droidcachebox.GlobalCore.setSelectedWaypoint(infoBubble.getCache(), infoBubble.getWaypoint());
                }
            }

            infoBubble.setInvisible();
            return true;
        });
        if (Mode == MapMode.Normal)
            addChild(infoBubble);

        resize(getWidth(), getHeight());

        try {
            center = new CoordinateGPS(de.droidcachebox.Config.MapInitLatitude.getValue(), de.droidcachebox.Config.MapInitLongitude.getValue());
        } catch (Exception ex) {
            Log.err(log, "MapView/CoordinateGPS", ex);
        }

        // Info aktualisieren
        if (Mode == MapMode.Normal)
            info.setCoord(center);

        zoomBtn.setZoom(de.droidcachebox.Config.lastZoomLevel.getValue());
        calcPixelsPerMeter();
        mapScale.zoomChanged();

        if ((center.getLatitude() == -1000) && (center.getLongitude() == -1000)) {
            // not initialized
            center = new CoordinateGPS(48, 12);
        }

        // Initial SettingsChanged Events
        SetNightMode(de.droidcachebox.Config.nightMode.getValue());
        de.droidcachebox.Config.nightMode.addSettingChangedListener(() -> SetNightMode(de.droidcachebox.Config.nightMode.getValue()));

        SetNorthOriented(de.droidcachebox.Config.MapNorthOriented.getValue());
        de.droidcachebox.Config.MapNorthOriented.addSettingChangedListener(() -> {
            SetNorthOriented(de.droidcachebox.Config.MapNorthOriented.getValue());
            this.positionChanged();
        });
        // to force generation of tiles in loadTiles();
        OnResumeListeners.getInstance().addListener(this::onResume);
    }

    @Override
    protected void renderSynchronOverlay(Batch batch) {
        batch.setProjectionMatrix(myParentInfo.Matrix());

        // calculate icon size
        int iconSize = 0; // 8x8
        if ((aktZoom >= 13) && (aktZoom <= 14))
            iconSize = 1; // 13x13
        else if (aktZoom > 14)
            iconSize = 2; // default Images

        if (Mode != MapMode.Compass)
            de.droidcachebox.RouteOverlay.RenderRoute(batch, this);
        renderWPs(GL_UISizes.WPSizes[iconSize], GL_UISizes.UnderlaySizes[iconSize], batch);
        renderPositionMarker(batch);
        RenderTargetArrow(batch);
    }

    @Override
    protected void renderNonSynchronOverlay(Batch batch) {
        renderUI(batch);
    }

    private void renderUI(Batch batch) {
        batch.setProjectionMatrix(myParentInfo.Matrix());

        if (showMapCenterCross) {
            if (getMapState() == MapState.FREE) {
                if (CrossLines == null) {
                    float crossSize = Math.min(mapIntHeight / 3.0f, mapIntWidth / 3.0f) / 2;
                    float strokeWidth = 2 * UiSizes.getInstance().getScale();

                    GeometryList geomList = new GeometryList();
                    Line l1 = new Line(mapIntWidth / 2.0f - crossSize, mapIntHeight / 2.0f, mapIntWidth / 2.0f + crossSize, mapIntHeight / 2.0f);
                    Line l2 = new Line(mapIntWidth / 2.0f, mapIntHeight / 2.0f - crossSize, mapIntWidth / 2.0f, mapIntHeight / 2.0f + crossSize);
                    Quadrangle q1 = new Quadrangle(l1, strokeWidth);
                    Quadrangle q2 = new Quadrangle(l2, strokeWidth);

                    geomList.add(q1);
                    geomList.add(q2);

                    GL_Paint paint = new GL_Paint();
                    paint.setGLColor(COLOR.getCrossColor());
                    CrossLines = new PolygonDrawable(geomList.getVertices(), geomList.getTriangles(), paint, mapIntWidth, mapIntHeight);

                    geomList.dispose();
                    l1.dispose();
                    l2.dispose();
                    q1.dispose();
                    q2.dispose();

                }

                CrossLines.draw(batch, 0, 0, mapIntWidth, mapIntHeight, 0);
            }
        }
    }

    private void RenderTargetArrow(Batch batch) {

        if (de.droidcachebox.GlobalCore.getSelectedCache() == null)
            return;

        Coordinate coord = (de.droidcachebox.GlobalCore.getSelectedWaypoint() != null) ? de.droidcachebox.GlobalCore.getSelectedWaypoint().Pos : de.droidcachebox.GlobalCore.getSelectedCache().coordinate;

        if (coord == null) {
            return;
        }
        float x = (float) (256.0 * Descriptor.LongitudeToTileX(MAX_MAP_ZOOM, coord.getLongitude()));
        float y = (float) (-256.0 * Descriptor.LatitudeToTileY(MAX_MAP_ZOOM, coord.getLatitude()));

        float halfHeight = mapIntHeight / 2.0f - ySpeedVersatz;
        float halfWidth = mapIntWidth / 2.0f;

        // create ScreenRec
        try {
            if (TargetArrowScreenRec == null) {
                TargetArrowScreenRec = new CB_RectF(0, 0, mapIntWidth, mapIntHeight);
                if (Mode != MapMode.Compass) {
                    TargetArrowScreenRec.ScaleCenter(0.9f);

                    if (Mode == MapMode.Normal) {
                        TargetArrowScreenRec.setHeight(TargetArrowScreenRec.getHeight() - (TargetArrowScreenRec.getHeight() - info.getY()) - zoomBtn.getHeight());
                        TargetArrowScreenRec.setY(zoomBtn.getMaxY());
                    }
                }
            }

            Vector2 ScreenCenter = new Vector2(halfWidth, halfHeight);

            Vector2 screen = worldToScreen(new Vector2(x, y));
            Vector2 target = new Vector2(screen.x, screen.y);

            Vector2 newTarget = TargetArrowScreenRec.getIntersection(ScreenCenter, target);

            // Rotation berechnen
            if (newTarget != null) {

                float direction = get_angle(ScreenCenter.x, ScreenCenter.y, newTarget.x, newTarget.y);
                direction = 180 - direction;

                // draw sprite
                Sprite arrow = Arrows.get(4);
                arrow.setRotation(direction);

                float boundsX = newTarget.x - GL_UISizes.TargetArrow.halfWidth;
                float boundsY = newTarget.y - GL_UISizes.TargetArrow.height;

                arrow.setBounds(boundsX, boundsY, GL_UISizes.TargetArrow.width, GL_UISizes.TargetArrow.height);

                arrow.setOrigin(GL_UISizes.TargetArrow.halfWidth, GL_UISizes.TargetArrow.height);
                arrow.draw(batch);

                // get real bounding box of TargetArrow
                float[] t = arrow.getVertices();
                float maxX = Math.max(Math.max(t[0], t[5]), Math.max(t[10], t[15]));
                float minX = Math.min(Math.min(t[0], t[5]), Math.min(t[10], t[15]));
                float maxY = Math.max(Math.max(t[1], t[6]), Math.max(t[11], t[16]));
                float minY = Math.min(Math.min(t[1], t[6]), Math.min(t[11], t[16]));
                TargetArrow.set(minX, minY, maxX - minX, maxY - minY);
            } else {
                TargetArrow.set(0, 0, 0, 0);
            }
        } catch (Exception e) {
            TargetArrow.set(0, 0, 0, 0);
        }
    }

    private void renderWPs(SizeF wpUnderlay, SizeF wpSize, Batch batch) {

        if (mapCacheList.list != null) {
            synchronized (mapCacheList.list) {

                for (int i = 0, n = mapCacheList.list.size(); i < n; i++) {
                    WaypointRenderInfo wpi = mapCacheList.list.get(i);
                    if (wpi.Selected) {
                        // wenn der Wp selectiert ist, dann immer in der größten Darstellung
                        renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
                    } else if (isCarMode) {
                        // wenn CarMode dann immer in der größten Darstellung
                        renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
                    } else {
                        renderWPI(batch, wpUnderlay, wpSize, wpi);
                    }
                }
            }
        }
        outScreenDraw = 0;
    }

    public void renderWPI(Batch batch, SizeF WpUnderlay, SizeF WpSize, WaypointRenderInfo wpi) {
        Vector2 screen = worldToScreen(new Vector2(wpi.MapX, wpi.MapY));

        screen.y -= ySpeedVersatz;
        // FIXME create a LineDrawable class for create one times and set the Coordinates with calculated Triangles
        if (myPointOnScreen != null && showDirectLine && (wpi.Selected) && (wpi.Waypoint == de.droidcachebox.GlobalCore.getSelectedWaypoint())) {
            // FIXME render only if visible on screen (intersect the screen rec)
            Quadrangle line = new Quadrangle(myPointOnScreen.x, myPointOnScreen.y, screen.x, screen.y, 3 * UiSizes.getInstance().getScale());
            if (paint == null) {
                paint = new GL_Paint();
                paint.setGLColor(Color.RED);
            }
            PolygonDrawable po = new PolygonDrawable(line.getVertices(), line.getTriangles(), paint, mapIntWidth, mapIntHeight);
            po.draw(batch, 0, 0, mapIntWidth, mapIntHeight, 0);
            po.dispose();
        }
        // Don't render if outside of screen !!
        if ((screen.x < 0 - WpSize.width || screen.x > getWidth() + WpSize.height) || (screen.y < 0 - WpSize.height || screen.y > getHeight() + WpSize.height)) {
            if (wpi.Cache != null && (wpi.Cache.Id == infoBubble.getCacheId()) && infoBubble.isVisible()) {
                // check if wp selected
                if (wpi.Waypoint != null && wpi.Waypoint.equals(infoBubble.getWaypoint()) || wpi.Waypoint == null && infoBubble.getWaypoint() == null)
                    infoBubble.setInvisible();
            }
            return;
        }

        float NameYMovement = 0;

        if ((aktZoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == de.droidcachebox.GlobalCore.getSelectedWaypoint())) {
            // Draw Cross and move screen vector
            Sprite cross = getMapOverlay(IconName.cross);
            cross.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
            cross.draw(batch);

            screen.add(-WpUnderlay.width, WpUnderlay.height);
            NameYMovement = WpUnderlay.height;
        }

        if (wpi.UnderlayIcon != null) {
            wpi.UnderlayIcon.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
            wpi.UnderlayIcon.draw(batch);
        }
        if (wpi.Icon != null) {
            wpi.Icon.setBounds(screen.x - WpSize.halfWidth, screen.y - WpSize.halfHeight, WpSize.width, WpSize.height);
            wpi.Icon.draw(batch);
        }

        // draw Favorite symbol
        if (wpi.Cache != null && wpi.Cache.isFavorite()) {
            batch.draw(getSprite(IconName.favorit.name()), screen.x + (WpSize.halfWidth / 2), screen.y + (WpSize.halfHeight / 2), WpSize.width, WpSize.height);
        }

        if (wpi.OverlayIcon != null) {
            wpi.OverlayIcon.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
            wpi.OverlayIcon.draw(batch);
        }

        boolean drawAsWaypoint = wpi.Waypoint != null;

        // Rating des Caches darstellen
        if (wpi.Cache != null && showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (aktZoom >= 15)) {
            Sprite rating = MapStars.get((int) Math.min(wpi.Cache.Rating * 2, 5 * 2));
            rating.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight - WpUnderlay.Height4_8, WpUnderlay.width, WpUnderlay.Height4_8);
            rating.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
            rating.setRotation(0);
            rating.draw(batch);
            NameYMovement += WpUnderlay.Height4_8;
        }

        // Beschriftung
        if (wpi.Cache != null && showTitles && (aktZoom >= 15)) {
            try {
                String Name = drawAsWaypoint ? wpi.Waypoint.getTitleForGui() : wpi.Cache.getName();

                if (layout == null)
                    layout = new GlyphLayout(Fonts.getNormal(), Name);
                else
                    layout.setText(Fonts.getNormal(), Name);

                float halfWidth = layout.width / 2;
                Fonts.getNormal().draw(batch, layout, screen.x - halfWidth, screen.y - WpUnderlay.halfHeight - NameYMovement);
            } catch (Exception ignored) {
            }
        }

        // Show D/T-Rating
        if (wpi.Cache != null && showDT && (!drawAsWaypoint) && (aktZoom >= 15)) {
            Sprite difficulty = MapStars.get((int) Math.min(wpi.Cache.getDifficulty() * 2, 5 * 2));
            difficulty.setBounds(screen.x - WpUnderlay.width - GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2), WpUnderlay.width, WpUnderlay.Height4_8);
            difficulty.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
            difficulty.setRotation(90);
            difficulty.draw(batch);

            Sprite terrain = MapStars.get((int) Math.min(wpi.Cache.getTerrain() * 2, 5 * 2));
            terrain.setBounds(screen.x + GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2), WpUnderlay.width, WpUnderlay.Height4_8);
            terrain.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
            terrain.setRotation(90);
            terrain.draw(batch);

        }

        if (wpi.Cache != null && (wpi.Cache.Id == infoBubble.getCacheId()) && infoBubble.isVisible()) {
            if (infoBubble.getWaypoint() == wpi.Waypoint) {
                Vector2 pos = new Vector2(screen.x - infoBubble.getHalfWidth(), screen.y);
                infoBubble.setPos(pos);
            }
        }
    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
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
        /*
         * if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint }); return; }
         */

        // mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);
        MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
        data.hideMyFinds = hideMyFinds;
        data.showAllWaypoints = showAllWaypoints;
        data.showAtOriginalPosition = showAtOriginalPosition;
        mapCacheList.update(data);

        if (getCenterGps()) {
            this.positionChanged();
            return;
        }

        positionInitialized = true;

        if (getMapState() != MapState.WP)
            setMapState(MapState.FREE);

        try {
            CoordinateGPS target = (waypoint != null) ? new CoordinateGPS(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude()) : new CoordinateGPS(cache.coordinate.getLatitude(), cache.coordinate.getLongitude());
            setCenter(target);
        } catch (Exception ignored) {
        }

        GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);

        // für 2sec rendern lassen, bis Änderungen der WPI-list neu berechnet wurden
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                GL.that.removeRenderView(MapView.this);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 2000);
        this.positionChanged();
    }

    @Override
    public void speedChanged() {
        if (info != null) {
            info.setSpeed(Locator.getInstance().SpeedString());

            if (getMapState() == MapState.CAR && de.droidcachebox.Config.dynamicZoom.getValue()) {
                // calculate dynamic Zoom

                double maxSpeed = de.droidcachebox.Config.MoveMapCenterMaxSpeed.getValue();
                int maxZoom = de.droidcachebox.Config.dynamicZoomLevelMax.getValue();
                int minZoom = de.droidcachebox.Config.dynamicZoomLevelMin.getValue();

                double percent = Locator.getInstance().SpeedOverGround() / maxSpeed;

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
            newGcCode = Database.Data.createFreeGcCode(de.droidcachebox.GlobalCore.getSelectedCache().getGcCode());
        } catch (Exception e) {
            return;
        }
        Coordinate coord = center;
        if ((coord == null) || (!coord.isValid()))
            coord = Locator.getInstance().getMyPosition();
        if ((coord == null) || (!coord.isValid()))
            return;
        //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
        Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), de.droidcachebox.GlobalCore.getSelectedCache().Id, "", newGcCode);

        EditWaypoint EdWp = new EditWaypoint(newWP, waypoint -> {
            if (waypoint != null) {

                de.droidcachebox.GlobalCore.getSelectedCache().waypoints.add(waypoint);
                de.droidcachebox.GlobalCore.setSelectedWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), waypoint);
                if (waypoint.IsStart) {
                    // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                    // definiert
                    // ist!!!
                    WaypointDAO wpd = new WaypointDAO();
                    wpd.ResetStartWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), waypoint);
                }
                WaypointDAO waypointDAO = new WaypointDAO();
                waypointDAO.WriteToDatabase(waypoint);

                // informiere WaypointListView über Änderung
                WaypointListChangedEventList.Call(de.droidcachebox.GlobalCore.getSelectedCache());
                GL.that.renderOnce();
            }
        }, true, false);
        EdWp.show();

    }

    @Override
    public void dispose() {
        de.droidcachebox.SelectedCacheChangedEventListeners.getInstance().remove(this);
        super.dispose();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        TargetArrowScreenRec = null;
    }

    @Override
    public void onHide() {
        Log.info(log, "Map gets invisible");
        de.droidcachebox.SelectedCacheChangedEventListeners.getInstance().remove(this);
        super.onHide();
    }

    @Override
    protected void updateCacheList(boolean force) {
        // force is for zoom
        if (lastScreenCenter.equals(screenCenterWorld) && !force) {
            return;
        }
        lastScreenCenter.set(screenCenterWorld);
        MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, false);
        data.hideMyFinds = hideMyFinds;
        data.showAllWaypoints = showAllWaypoints;
        data.showAtOriginalPosition = showAtOriginalPosition;
        mapCacheList.update(data);
    }

    protected void directLoadTiles(Descriptor lowerTile, Descriptor upperTile, int aktZoom) {
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
            mapTileLoader.loadTiles(this, lowerTile, upperTile, aktZoom);
            MapTileLoader.isWorking.set(false);


            if (isCarMode && de.droidcachebox.CB_UI_Settings.LiveMapEnabeld.getValue()) {
                LiveMapQue.setCenterDescriptor(center);
                // LiveMap queue complete screen
                lowerTile.Data = center;
                LiveMapQue.queScreen(lowerTile, upperTile);
            }

        }
    }

    @Override
    public void setCenter(CoordinateGPS value) {
        if (Mode == MapMode.Normal)
            info.setCoord(value);
        super.setCenter(value);
    }

    @Override
    public void orientationChanged() {
        super.orientationChanged();
        if (info != null) {
            try {
                Coordinate position = Locator.getInstance().getMyPosition();

                if (de.droidcachebox.GlobalCore.isSetSelectedCache()) {
                    Coordinate dest = (de.droidcachebox.GlobalCore.getSelectedWaypoint() != null) ? de.droidcachebox.GlobalCore.getSelectedWaypoint().Pos : de.droidcachebox.GlobalCore.getSelectedCache().coordinate;

                    if (dest == null)
                        return;

                    float heading = Locator.getInstance().getHeading();

                    float[] result = new float[2];

                    MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);

                    float bearing = result[1];

                    float relativeBearing = bearing - heading;
                    info.setBearing(relativeBearing, heading);
                }
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void initializeMap() {
        zoomCross = de.droidcachebox.Config.ZoomCross.getValue();
        super.initializeMap();
    }

    @Override
    protected void setZoomScale(int zoom) {
        if (Mode == MapMode.Normal)
            zoomScale.setZoom(zoom);
        if (Mode == MapMode.Normal)
            mapScale.zoomChanged();
    }

    @Override
    protected void calcCenter() {
        super.calcCenter();
        if (Mode == MapMode.Normal) {
            info.setCoord(center);
        }
    }

    @Override
    public void requestLayout() {

        if (isDisposed()) return;

        float margin = GL_UISizes.margin;

        float infoHeight = 0;
        if (Mode == MapMode.Normal) {
            info.setPos(new Vector2(margin, mapIntHeight - margin - info.getHeight()));
            info.setVisible(de.droidcachebox.Config.MapShowInfo.getValue());
            infoHeight = info.getHeight();
        }
        togBtn.setPos(new Vector2(mapIntWidth - margin - togBtn.getWidth(), mapIntHeight - margin - togBtn.getHeight()));

        if (de.droidcachebox.Config.DisableLiveMap.getValue()) {
            liveButton.setInvisible();
        } else {
            liveButton.setVisible();
        }

        liveButton.setRec(togBtn);
        liveButton.setY(togBtn.getY() - margin - liveButton.getHeight());

        zoomScale.setSize((float) (44.6666667 * GL_UISizes.DPI), getHeight() - infoHeight - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());

        GL.that.renderOnce();
    }

    @Override
    public void setMapStateFree() {
        // setMapState(MapState.FREE);
        // Go over ToggelButton
        togBtn.setState(0);
    }

    @Override
    public void setMapState(MapState state) {
        if (super.getMapState() == state)
            return;

        de.droidcachebox.Config.LastMapToggleBtnState.setValue(state.ordinal());
        de.droidcachebox.Config.AcceptChanges();

        boolean wasCarMode = isCarMode;

        if (Mode == MapMode.Normal) {
            info.setCoordType(CoordType.Map);
            if (state == MapState.CAR) {
                if (!wasCarMode) {
                    info.setCoordType(CoordType.GPS);
                }
            } else if (state == MapState.WP) {
                info.setCoordType(CoordType.Cache);
            } else if (state == MapState.LOCK) {
                info.setCoordType(CoordType.GPS);
            } else if (state == MapState.GPS) {
                info.setCoordType(CoordType.GPS);
            }
        }

        super.setMapState(state);
    }

    @Override
    public void positionChanged() {

        if (isCarMode) {
            // im CarMode keine Netzwerk Koordinaten zulassen
            if (!Locator.getInstance().isGPSprovided())
                return;
        }

        super.positionChanged();

        if (info != null) {
            if (center != null) {
                info.setCoord(center);
            }

            if (de.droidcachebox.GlobalCore.getSelectedCoord() != null) {
                info.setDistance(de.droidcachebox.GlobalCore.getSelectedCoord().Distance(CalculationType.ACCURATE));
            }
            this.orientationChanged();
        }

        if (Mode == MapMode.Compass) {
            // Berechne den Zoom so, dass eigene Position und WP auf der Map zu sehen sind.
            // if ((GlobalCore.Marker != null) && (GlobalCore.Marker.isValid)) position = GlobalCore.Marker;
            Coordinate position = Locator.getInstance().getMyPosition();

            float distance = -1;
            if (de.droidcachebox.GlobalCore.isSetSelectedCache() && position.isValid()) {
                try {
                    if (de.droidcachebox.GlobalCore.getSelectedWaypoint() == null)
                        distance = position.Distance(de.droidcachebox.GlobalCore.getSelectedCache().coordinate, CalculationType.ACCURATE);
                    else
                        distance = position.Distance(de.droidcachebox.GlobalCore.getSelectedWaypoint().Pos, CalculationType.ACCURATE);
                } catch (Exception e) {
                    distance = 10;
                }
            }
            int setZoomTo = zoomBtn.getMinZoom();

            if (DistanceZoomLevel != null) {
                for (int i = zoomBtn.getMaxZoom(); i > zoomBtn.getMinZoom(); i--) {
                    if (distance < DistanceZoomLevel.get(i)) {
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
        super.skinIsChanged();
        MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
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
        if (Mode == MapMode.Normal) // save last zoom and position only from Map, not from CompassMap
        {
            super.onStop();
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        de.droidcachebox.SelectedCacheChangedEventListeners.getInstance().add(this);
        NorthOriented = Mode == MapMode.Normal ? de.droidcachebox.Config.MapNorthOriented.getValue() : false;
        selectedCacheChanged(de.droidcachebox.GlobalCore.getSelectedCache(), de.droidcachebox.GlobalCore.getSelectedWaypoint());
    }

    /**
     * setNewSettings
     */
    @Override
    public void setNewSettings(int InitialFlags) {
        if ((InitialFlags & INITIAL_SETTINGS) != 0) {
            showRating = Mode == MapMode.Compass ? false : de.droidcachebox.Config.MapShowRating.getValue();
            showDT = Mode == MapMode.Compass ? false : de.droidcachebox.Config.MapShowDT.getValue();
            showTitles = Mode == MapMode.Compass ? false : de.droidcachebox.Config.MapShowTitles.getValue();
            hideMyFinds = de.droidcachebox.Config.MapHideMyFinds.getValue();
            showDirectLine = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowDirektLine.getValue();
            showAllWaypoints = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowAllWaypoints.getValue();
            showAccuracyCircle = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowAccuracyCircle.getValue();
            showMapCenterCross = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowMapCenterCross.getValue();
            showAtOriginalPosition = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowAtOriginalPosition.getValue();

            if (Mode == MapMode.Track) {
                showMapCenterCross = true;
                setMapState(MapState.FREE);
            }

            if (info != null)
                info.setVisible(Mode == MapMode.Compass ? false : de.droidcachebox.Config.MapShowInfo.getValue());

            if (InitialFlags == INITIAL_ALL) {

                if (de.droidcachebox.Config.MapViewDPIFaktor.getValue() == 1) {
                    de.droidcachebox.Config.MapViewDPIFaktor.setValue(AbstractGlobal.displayDensity);
                    de.droidcachebox.Config.AcceptChanges();
                }
                iconFactor = de.droidcachebox.Config.MapViewDPIFaktor.getValue();

                int setMaxZoom = Mode == MapMode.Compass ? de.droidcachebox.Config.CompassMapMaxZommLevel.getValue() : de.droidcachebox.Config.OsmMaxLevel.getValue();
                int setMinZoom = Mode == MapMode.Compass ? de.droidcachebox.Config.CompassMapMinZoomLevel.getValue() : de.droidcachebox.Config.OsmMinLevel.getValue();

                zoomBtn.setMaxZoom(setMaxZoom);
                zoomBtn.setMinZoom(setMinZoom);
                zoomBtn.setZoom(de.droidcachebox.Config.lastZoomLevel.getValue());

                zoomScale.setMaxZoom(setMaxZoom);
                zoomScale.setMinZoom(setMinZoom);

                if (Mode == MapMode.Compass) {
                    // Berechne die darstellbare Entfernung für jedes ZoomLevel
                    DistanceZoomLevel = new TreeMap<>();

                    int possiblePixel = (int) getHalfHeight();

                    for (int i = setMaxZoom; i > setMinZoom; i--) {
                        float PixelForZoomLevel = getPixelsPerMeter(i);
                        DistanceZoomLevel.put(i, (int) (possiblePixel / PixelForZoomLevel));
                    }
                }

            }
        }

        if ((InitialFlags & INITIAL_THEME) != 0) {
            if (mapTileLoader.getCurrentLayer() != null) {
                if (mapTileLoader.getCurrentLayer().isMapsForge()) {
                    Log.info(log, "modify layer " + mapTileLoader.getCurrentLayer().getName() + " for mapview " + Mode);
                    mapTileLoader.modifyCurrentLayer(isCarMode);
                    renderOnce("INITIAL_THEME");
                }
            }
            if (mapTileLoader.getCurrentOverlayLayer() != null) {
                if (mapTileLoader.getCurrentOverlayLayer().isMapsForge()) {
                    // until now there are only Online Overlays
                    // ((MapsForgeLayer) mapTileLoader.getCurrentOverlayLayer()).initTheme(isCarMode);
                }
            }
        }

        if ((InitialFlags & INITIAL_WP_LIST) != 0) {
            if (mapCacheList != null) {
                MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
                hideMyFinds = de.droidcachebox.Config.MapHideMyFinds.getValue();
                data.hideMyFinds = hideMyFinds;
                showAllWaypoints = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowAllWaypoints.getValue();
                data.showAllWaypoints = showAllWaypoints;
                showAtOriginalPosition = Mode == MapMode.Compass ? false : de.droidcachebox.Config.ShowAtOriginalPosition.getValue();
                data.showAtOriginalPosition = showAtOriginalPosition;
                mapCacheList.update(data);
            }
        }
    }

    @Override
    protected void setInitialLocation() {
        try {
            if (Database.Data != null) {

                if (Database.Data.cacheList != null) {
                    synchronized (Database.Data.cacheList) {
                        if (Database.Data.cacheList.size() > 0) {
                            // Koordinaten des ersten Caches der Datenbank
                            // nehmen
                            setCenter(new CoordinateGPS(Database.Data.cacheList.get(0).coordinate.getLatitude(), Database.Data.cacheList.get(0).coordinate.getLongitude()));
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
    public void MapStateChangedToWP() {
        if (de.droidcachebox.GlobalCore.isSetSelectedCache()) {
            if (de.droidcachebox.GlobalCore.getSelectedWaypoint() != null) {
                Coordinate tmp = de.droidcachebox.GlobalCore.getSelectedWaypoint().Pos;
                setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
            } else {
                Coordinate tmp = de.droidcachebox.GlobalCore.getSelectedCache().coordinate;
                setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
            }
        }
    }

    @Override
    public void SetAlignToCompass(boolean value) {
        super.SetAlignToCompass(value);
        de.droidcachebox.Config.MapNorthOriented.setValue(!value);
    }

    private void onResume() {
        MapView.this.renderOnce("OnResumeListeners");
    }

    public enum MapMode {
        Normal, Compass, Track
    }

}
