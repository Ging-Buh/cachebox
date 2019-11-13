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
package de.droidcachebox.locator.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.ZoomButtons;
import de.droidcachebox.gdx.graphics.KineticPan;
import de.droidcachebox.gdx.graphics.KineticZoom;
import de.droidcachebox.gdx.graphics.PolygonDrawable;
import de.droidcachebox.gdx.main.MainViewBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.invalidateTextureEvent;
import de.droidcachebox.invalidateTextureEventList;
import de.droidcachebox.locator.*;
import de.droidcachebox.utils.*;
import de.droidcachebox.utils.log.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class MapViewBase extends CB_View_Base implements PositionChangedEvent, invalidateTextureEvent {
    public static final int MAX_MAP_ZOOM = 22;
    private static final String log = "MapViewBase";
    public static MapTileLoader mapTileLoader;
    public static int INITIAL_SETTINGS = 1;
    public static int INITIAL_THEME = 2;
    public static int INITIAL_WP_LIST = 4;
    public static int INITIAL_ALL = 7;
    public static int INITIAL_SETTINGS_WITH_OUT_ZOOM = 9;
    public static int INITIAL_NEW_SETTINGS = 3;
    public final PointL screenCenterWorld = new PointL(0, 0);
    protected final int ZoomTime = 1000;
    protected final Vector2 loVector = new Vector2();
    protected final Vector2 ruVector = new Vector2();
    protected final PointL screenCenterT = new PointL(0, 0);
    private final Point lastMovement = new Point(0, 0);
    private final HashMap<Integer, Point> fingerDown = new LinkedHashMap<>();
    public CoordinateGPS center = new CoordinateGPS(48.0, 12.0);
    public float ySpeedVersatz = 200;
    public float pixelsPerMeter = 0;
    protected Vector2 midVector2;
    protected ZoomButtons zoomBtn;
    protected ZoomScale zoomScale;
    protected MapScale mapScale;
    protected float arrowHeading = 0;
    protected Vector2 myPointOnScreen;
    protected boolean showAccuracyCircle;
    protected int aktZoom;
    protected KineticZoom kineticZoom = null;
    protected float maxTilesPerScreen = 0;
    protected int mapIntWidth;
    protected int mapIntHeight;
    protected int drawingWidth;
    protected int drawingHeight;
    protected boolean positionInitialized = false;
    protected OrthographicCamera camera;
    protected boolean isCarMode = false;
    protected boolean NorthOriented = true;
    protected float iconFactor = 1.5f;
    protected boolean showMapCenterCross;
    protected boolean showAtOriginalPosition;
    protected PolygonDrawable CrossLines = null;
    protected IChanged themeChangedEventHandler = () -> MapViewBase.this.invalidateTexture();
    protected float scale;
    protected int outScreenDraw = 0;
    protected float lastDynamicZoom = -1;
    protected InputState inputState = InputState.Idle;
    protected boolean isShown, isCreated;
    protected int maxNumTiles;
    Descriptor lastDescriptorOrdered;
    private AccuracyDrawable accuracyDrawable = null;
    private boolean NightMode = false;
    private MapState mapState = MapState.FREE;
    // protected boolean alignToCompass = false;
    private float mapHeading = 0;
    private KineticPan kineticPan = null;

    public MapViewBase(CB_RectF rec, String Name) {
        super(rec, Name);
        isCreated = false;
        isShown = false;
        lastDescriptorOrdered = new Descriptor(0, 0, 10);
        invalidateTextureEventList.Add(this);
    }

    protected void SetNightMode(boolean NightMode) {
        this.NightMode = NightMode;
    }

    protected void SetNorthOriented(boolean NorthOriented) {
        this.NorthOriented = NorthOriented;
    }

    @Override
    public void onShow() {
        PositionChangedListeners.addListener(this);
        positionChanged();

        isCarMode = (getMapState() == MapState.CAR);
        if (!isCarMode) {
            drawingWidth = mapIntWidth;
            drawingHeight = mapIntHeight;
        }

        if (camera == null) {
            // onResized(this);
            Log.trace(log, "why is the camera zero");
        } else {
            setVisible();
            setActZoom();
            calcPixelsPerMeter(); // uses camera
        }
        isShown = true;
        GL.that.renderOnce();
    }

    @Override
    public void onHide() {
        PositionChangedListeners.removeListener(this);
        setInvisible();
        onStop();// save last zoom and position
    }

    @Override
    public void dispose() {
        // remove eventHandler
        PositionChangedListeners.removeListener(this);
        invalidateTextureEventList.Remove(this);
        super.dispose();
    }

    @Override
    public void onResized(CB_RectF rec) {
        if (rec.getWidth() <= 0 || rec.getHeight() <= 0)
            return;

        // do not need to do anything, if nothing changed
        if (rec.getWidth() == this.mapIntWidth && rec.getHeight() == this.mapIntHeight) {
            // except Camera == null!
            if (camera != null)
                return;
        }

        this.mapIntWidth = (int) rec.getWidth();
        this.mapIntHeight = (int) rec.getHeight();
        midVector2 = new Vector2((float) mapIntWidth / 2f, (float) mapIntHeight / 2f);
        this.drawingWidth = (int) rec.getWidth();
        this.drawingHeight = (int) rec.getHeight();

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.zoom = getMapTilePosFactor(zoomBtn.getZoom());

        camera.position.set(0, 0, 0);

        requestLayout();

    }

    @Override
    public void onStop() {
        LocatorSettings.MapInitLatitude.setValue(center.getLatitude());
        LocatorSettings.MapInitLongitude.setValue(center.getLongitude());
        LocatorSettings.lastZoomLevel.setValue(zoomBtn.getZoom());
        super.onStop();
    }

    public Layer getCurrentLayer() {
        return mapTileLoader.getCurrentLayer();
    }

    public void setCurrentLayer(Layer newLayer) {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.clearAdditionalMaps();
        if (newLayer == null) {
            LocatorSettings.currentMapLayer.setValue(new String[0]);
        } else {
            LocatorSettings.currentMapLayer.setValue(newLayer.getAllLayerNames());
        }
        if (mapTileLoader.setCurrentLayer(newLayer, isCarMode)) {
            renderOnce("setCurrentLayer");
        }
    }

    public void addAdditionalLayer(Layer layer) {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.addAdditionalMap(layer);
        LocatorSettings.currentMapLayer.setValue(currentLayer.getAllLayerNames());
        mapTileLoader.modifyCurrentLayer(isCarMode);
        renderOnce("addAdditionalLayer");
    }

    public void clearAdditionalLayers() {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.clearAdditionalMaps();
        LocatorSettings.currentMapLayer.setValue(currentLayer.getAllLayerNames());
        mapTileLoader.modifyCurrentLayer(isCarMode);
        renderOnce("clearAdditionalLayers");
    }

    public void removeAdditionalLayer() {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.clearAdditionalMaps();
        LocatorSettings.currentMapLayer.setValue(currentLayer.getAllLayerNames());
        mapTileLoader.modifyCurrentLayer(isCarMode);
        renderOnce("removeAdditionalLayer");
    }

    public void setCurrentOverlayLayer(Layer newLayer) {
        if (newLayer == null) {
            LocatorSettings.CurrentMapOverlayLayerName.setValue("");
        } else {
            LocatorSettings.CurrentMapOverlayLayerName.setValue(newLayer.getName());
        }
        mapTileLoader.setCurrentOverlayLayer(newLayer);
    }

    @Override
    protected void render(Batch batch) {
        try {
            if (LocatorSettings.MoveMapCenterWithSpeed.getValue() && isCarMode && Locator.getInstance().hasSpeed()) {

                double maxSpeed = LocatorSettings.MoveMapCenterMaxSpeed.getValue();

                double percent = Locator.getInstance().SpeedOverGround() / maxSpeed;

                float diff = (float) ((this.getHeight()) / 3 * percent);
                if (diff > this.getHeight() / 3)
                    diff = this.getHeight() / 3;

                ySpeedVersatz = diff;

            } else
                ySpeedVersatz = 0;

            boolean reduceFps = ((kineticZoom != null) || ((kineticPan != null) && (kineticPan.getStarted())));
            if (kineticZoom != null) {
                camera.zoom = kineticZoom.getAktZoom();
                setActZoom();

                if (kineticZoom.getFertig()) {
                    setZoomScale(zoomBtn.getZoom());
                    GL.that.removeRenderView(this);
                    kineticZoom = null;
                } else
                    reduceFps = false;

                calcPixelsPerMeter();
                if (mapScale != null)
                    mapScale.ZoomChanged();
                if (zoomScale != null)
                    zoomScale.setZoom(convertCameraZoomToFloat(camera));

            }

            if ((kineticPan != null) && (kineticPan.getStarted())) {
                long faktor = getMapTilePosFactor(aktZoom);
                Point pan = kineticPan.getAktPan();
                screenCenterT.set(screenCenterT.getX() + pan.x * faktor, screenCenterT.getY() + pan.y * faktor);
                calcCenter();

                if (kineticPan.getFertig()) {
                    kineticPan = null;
                } else
                    reduceFps = false;
            }

            if (reduceFps) {
                GL.that.removeRenderView(this);
            }
            camera.update();
            renderMapTiles(batch);
            renderSynchronOverlay(batch);
            renderNonSynchronOverlay(batch);
        } catch (Exception ex) {
            Log.err(log, "render", ex);
        }
    }

    protected abstract void renderSynchronOverlay(Batch batch);

    protected abstract void renderNonSynchronOverlay(Batch batch);

    private void renderMapTiles(Batch batch) {

        batch.disableBlending();

        float faktor = camera.zoom;
        float dx = this.thisWorldRec.getCenterPosX() - MainViewBase.mainView.getCenterPosX();
        float dy = this.thisWorldRec.getCenterPosY() - MainViewBase.mainView.getCenterPosY();
        dy -= ySpeedVersatz;
        camera.position.set(0, 0, 0);
        float dxr = dx;
        float dyr = dy;
        if (!this.NorthOriented || isCarMode) {
            camera.up.x = 0;
            camera.up.y = 1;
            camera.up.z = 0;
            camera.rotate(-mapHeading, 0, 0, 1);
            double angle = mapHeading * MathUtils.DEG_RAD;
            dxr = (float) (Math.cos(angle) * dx + Math.sin(angle) * dy);
            dyr = (float) (-Math.sin(angle) * dx + Math.cos(angle) * dy);
        } else {
            camera.up.x = 0;
            camera.up.y = 1;
            camera.up.z = 0;
        }
        camera.translate(-dxr * faktor, -dyr * faktor, 0);
        camera.update();
        Matrix4 mat = camera.combined;

        batch.setProjectionMatrix(mat);

        mapTileLoader.increaseAge();
        if (screenCenterWorld.isNull()) {
            synchronized (screenCenterT) {
                screenCenterWorld.set(screenCenterT);
            }
        }
        if (!screenCenterWorld.isNull()) {
            int halfMapIntWidth = mapIntWidth / 2;
            int halfMapIntHeight = mapIntHeight / 2;
            int halfDrawingtWidth = drawingWidth / 2;
            int halfDrawingHeight = drawingHeight / 2;
            loVector.set(halfMapIntWidth - halfDrawingtWidth, halfMapIntHeight - halfDrawingHeight - ySpeedVersatz);
            ruVector.set(halfMapIntWidth + halfDrawingtWidth, halfMapIntHeight + halfDrawingHeight + ySpeedVersatz);
            Descriptor lowerTile = screenToDescriptor(loVector, aktZoom);
            Descriptor upperTile = screenToDescriptor(ruVector, aktZoom);
            // int tilesWantedToDrawCount = (ru.getX() - lo.getX() + 1) * (ru.getY() - lo.getY() + 1);
            // if (mapTileLoader.isLoadingChanged() || tilesWantedToDrawCount > mapTileLoader.getTilesToDrawCounter())
            {
                // determine again the tiles to draw from loaded tiles (mostly the same as in last render step)
                // Log.info(log, "determine tiles to draw");
                mapTileLoader.resetTilesToDrawCounter();
                if (mapTileLoader.getCurrentOverlayLayer() != null) {
                    mapTileLoader.resetOverlayTilesToDrawCounter();
                }
                boolean tileOrdered = false;
                int midX = (lowerTile.getX() + upperTile.getX()) / 2;
                int midY = (lowerTile.getY() + upperTile.getY()) / 2;
                for (int i = lowerTile.getX(); i <= upperTile.getX(); i++) {
                    for (int j = lowerTile.getY(); j <= upperTile.getY(); j++) {
                        Descriptor desc = new Descriptor(i, j, aktZoom);
                        boolean canDraw;
                        canDraw = mapTileLoader.markTileToDraw(desc.getHashCode()) >= 0;
                        boolean canDrawOverlay = false;
                        if (mapTileLoader.getCurrentOverlayLayer() != null) {
                            canDrawOverlay = mapTileLoader.markOverlayTileToDraw(desc.getHashCode()) >= 0;
                        }

                        if (!canDraw) {
                            if (!tileOrdered && lastDescriptorOrdered.getHashCode() != desc.getHashCode()) {
                                // Log.info(log, "order: " + desc + " Distance: " + Math.max(Math.abs(i - midX), Math.abs(j - midY)));
                                lastDescriptorOrdered = desc;
                                directLoadTiles(lowerTile, upperTile, aktZoom);
                            }
                            tileOrdered = true;
                            desc.Data = this;
                            // at moment there is no suitable tile for this zoom, first try a bigger one, else try from smaller ones
                            if (!renderBiggerTiles(batch, i, j, aktZoom)) {
                                renderSmallerTiles(batch, i, j, aktZoom);
                            }
                        }

                        if (mapTileLoader.getCurrentOverlayLayer() != null) {
                            if (!canDrawOverlay) {
                                if (!renderBiggerOverlayTiles(batch, i, j, aktZoom))
                                    renderSmallerOverlayTiles(batch, i, j, aktZoom);
                            }
                        }
                    }
                }

                mapTileLoader.sortByAge();
            }
        }

        CB_List<TileGL_RotateDrawables> rotateList = new CB_List<>();

        synchronized (screenCenterWorld) {
            // Log.info(log, "Number of tiles to Draw: " + mapTileLoader.getTilesToDrawCounter());
            for (int i = mapTileLoader.getTilesToDrawCounter() - 1; i > -1; i--) {
                TileGL tile = mapTileLoader.getDrawingTile(i);
                if (tile != null) {
                    long posFactor = getscaledMapTilePosFactor(tile);
                    long xPos = tile.descriptor.getX() * posFactor * tile.getWidth() - screenCenterWorld.getX();
                    long yPos = -(tile.descriptor.getY() + 1) * posFactor * tile.getHeight() - screenCenterWorld.getY();
                    float xSize = tile.getWidth() * posFactor;
                    float ySize = tile.getHeight() * posFactor;
                    boolean addToRotateList = tile.descriptor.getZoom() == aktZoom;
                    tile.draw(batch, xPos, yPos, xSize, ySize, (addToRotateList ? rotateList : null));
                }
            }
            batch.enableBlending();
            // todo sort rotate List first the Symbols then the Text! sort Text with same Font!
            // Don't change the Texture (improve the Performance)
            for (int i = 0, n = rotateList.size(); i < n; i++) {
                TileGL_RotateDrawables drw = rotateList.get(i);
                if (drw != null)
                    drw.draw(batch, -mapHeading);
            }
            rotateList.truncate(0);
            rotateList = null;
        }

        if (mapTileLoader.getCurrentOverlayLayer() != null) {
            synchronized (screenCenterWorld) {
                for (int i = mapTileLoader.getOverlayTilesToDrawCounter() - 1; i > -1; i--) {
                    TileGL tile = mapTileLoader.getOverlayDrawingTile(i);
                    if (tile != null) {
                        long posFactor = getscaledMapTilePosFactor(tile);
                        long xPos = tile.descriptor.getX() * posFactor * tile.getWidth() - screenCenterWorld.getX();
                        long yPos = -(tile.descriptor.getY() + 1) * posFactor * tile.getHeight() - screenCenterWorld.getY();
                        float xSize = tile.getWidth() * posFactor;
                        float ySize = tile.getHeight() * posFactor;
                        tile.draw(batch, xPos, yPos, xSize, ySize, rotateList);
                    }
                }
            }
        }
    }

    private long getscaledMapTilePosFactor(TileGL tile) {
        // for scaling up this tile for drawing (each tile has 256 pixel, but you can't recognize anything on a large screen)
        // on the other hand upscaling makes it more blurred (pixilated), but changing pixels per tile needs a complete redesign of this app part
        if (tile == null || tile.descriptor == null) return 1;
        return (long) (Math.pow(2.0, MAX_MAP_ZOOM - tile.descriptor.getZoom()) / tile.getScaleFactor());
    }

    /*
    protected void renderDebugInfo(Batch batch) {

        CB_RectF r = this.thisWorldRec;

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        BitmapFont font = Fonts.getNormal();

        font.setColor(Color.BLACK);

        Matrix4 def = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        def.translate(r.getX(), r.getY(), 1);
        batch.setProjectionMatrix(def);

        // str = debugString;
        font.draw(batch, str, 20, 120);

        str = "fps: " + Gdx.graphics.getFramesPerSecond();
        font.draw(batch, str, 20, 100);

        str = String.valueOf(aktZoom) + " - camzoom: " + Math.round(camera.zoom * 100) / 100;
        font.draw(batch, str, 20, 80);

        str = "lTiles: " + mapTileLoader.getNumberOfLoadedTiles() + " - qTiles: " + mapTileLoader.queuedTilesSize();
        font.draw(batch, str, 20, 60);

        str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
        font.draw(batch, str, 20, 20);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    }
     */

    // FIXME make point and vPoint final and setValues!
    protected void renderPositionMarker(Batch batch) {
        PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MAX_MAP_ZOOM, Locator.getInstance().getLongitude()), Descriptor.LatitudeToTileY(MAX_MAP_ZOOM, Locator.getInstance().getLatitude()), MAX_MAP_ZOOM,
                MAX_MAP_ZOOM);

        Vector2 vPoint = new Vector2((float) point.X, -(float) point.Y);

        myPointOnScreen = worldToScreen(vPoint);

        myPointOnScreen.y -= ySpeedVersatz;

        if (showAccuracyCircle) {

            if (accuracyDrawable == null) {
                accuracyDrawable = new AccuracyDrawable(this.mapIntWidth, this.mapIntWidth);
            }

            float radius = (pixelsPerMeter * Locator.getInstance().getMyPosition().getAccuracy());

            if (radius > GL_UISizes.PosMarkerSize / 2) {
                accuracyDrawable.draw(batch, myPointOnScreen.x, myPointOnScreen.y, radius);
            }
        }

        boolean lastUsedCompass = Locator.getInstance().UseMagneticCompass();
        boolean Transparency = LocatorSettings.PositionMarkerTransparent.getValue();

        int arrowId = 0;
        if (lastUsedCompass) {
            arrowId = Transparency ? 1 : 0;
        } else {
            arrowId = Transparency ? 3 : 2;
        }

        if (isCarMode)
            arrowId = 15;

        Sprite arrow = Sprites.Arrows.get(arrowId);
        arrow.setRotation(-arrowHeading);
        arrow.setBounds(myPointOnScreen.x - GL_UISizes.halfPosMarkerSize, myPointOnScreen.y - GL_UISizes.halfPosMarkerSize, GL_UISizes.PosMarkerSize, GL_UISizes.PosMarkerSize);
        arrow.setOrigin(GL_UISizes.halfPosMarkerSize, GL_UISizes.halfPosMarkerSize);
        arrow.draw(batch);

    }

    protected float get_angle(float x1, float y1, float x2, float y2) {
        float opp;
        float adj;
        float ang1;

        // calculate vector differences
        opp = y1 - y2;
        adj = x1 - x2;

        if (x1 == x2 && y1 == y2)
            return (-1);

        // trig function to calculate angle
        if (adj == 0) // to catch vertical co-ord to prevent division by 0
        {
            if (opp >= 0) {
                return (0);
            } else {
                return (180);
            }
        } else {
            ang1 = (float) ((Math.atan(opp / adj)) * MathUtils.RAD_DEG);
            // the angle calculated will range from +90 degrees to -90 degrees
            // so the angle needs to be adjusted if point x1 is less or greater then x2
            if (x1 >= x2) {
                ang1 = 90 - ang1;
            } else {
                ang1 = 270 - ang1;
            }
        }
        return (ang1);
    }

    private boolean renderBiggerTiles(Batch batch, int i, int j, int zoom2) {
        // cause no tile for the actual zoom exists -> kleinere
        // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
        // von dem gefundenen Tile mu� dann nur ein Ausschnitt gezeichnet werden
        int ii = i / 2;
        int jj = j / 2;
        int zoomzoom = zoom2 - 1;

        Descriptor desc = new Descriptor(ii, jj, zoomzoom);
        boolean canDraw = mapTileLoader.markTileToDraw(desc.getHashCode()) >= 0;

        if (canDraw) {
            // das Alter der benutzten Tiles nicht auf 0 setzen, da dies
            // eigentlich nicht das richtige Tile ist!!!
            // tile.age = 0;

            return true;
        } else if ((zoomzoom >= aktZoom - 3) && (zoomzoom >= zoomBtn.getMinZoom())) {
            // f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
            // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
            // sind...
            // daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
            // gezeichnet werden (4 oder 16 oder 64...)
            // dieser Aufruf kann auch rekursiv sein...
            renderBiggerTiles(batch, ii, jj, zoomzoom);
        }
        return false;
    }

    private boolean renderBiggerOverlayTiles(Batch batch, int i, int j, int zoom2) {
        // f�r den aktuellen Zoom ist kein Tile vorhanden -> kleinere
        // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
        // von dem gefundenen Tile mu� dann nur ein Ausschnitt gezeichnet werden
        int ii = i / 2;
        int jj = j / 2;
        int zoomzoom = zoom2 - 1;

        Descriptor desc = new Descriptor(ii, jj, zoomzoom);
        boolean canDraw = mapTileLoader.markOverlayTileToDraw(desc.getHashCode()) >= 0;

        if (canDraw) {
            return true;
        } else if ((zoomzoom >= aktZoom - 3) && (zoomzoom >= zoomBtn.getMinZoom())) {
            // f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
            // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
            // sind...
            // daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
            // gezeichnet werden (4 oder 16 oder 64...)
            // dieser Aufruf kann auch rekursiv sein...
            renderBiggerOverlayTiles(batch, ii, jj, zoomzoom);
        }
        return false;
    }

    // private static String debugString = "";

    private void renderSmallerTiles(Batch batch, int i, int j, int zoom2) {
        // f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
        // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
        // daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles gezeichnet
        // werden (4 oder 16 oder 64...)
        int i1 = i * 2;
        int i2 = i * 2 + 1;
        int j1 = j * 2;
        int j2 = j * 2 + 1;
        int zoomzoom = zoom2 + 1;
        for (int ii = i1; ii <= i2; ii++) {
            for (int jj = j1; jj <= j2; jj++) {
                Descriptor desc = new Descriptor(ii, jj, zoomzoom);
                boolean canDraw = mapTileLoader.markTileToDraw(desc.getHashCode()) >= 0;
                if (canDraw) {
                } else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MAX_MAP_ZOOM)) {
                    // f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
                    // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
                    // sind...
                    // daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
                    // gezeichnet werden (4 oder 16 oder 64...)
                    // dieser Aufruf kann auch rekursiv sein...
                    renderSmallerTiles(batch, ii, jj, zoomzoom);
                }
            }
        }
    }

    private void renderSmallerOverlayTiles(Batch batch, int i, int j, int zoom2) {
        // f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
        // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
        // daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles gezeichnet
        // werden (4 oder 16 oder 64...)
        int i1 = i * 2;
        int i2 = i * 2 + 1;
        int j1 = j * 2;
        int j2 = j * 2 + 1;
        int zoomzoom = zoom2 + 1;
        for (int ii = i1; ii <= i2; ii++) {
            for (int jj = j1; jj <= j2; jj++) {
                Descriptor desc = new Descriptor(ii, jj, zoomzoom);
                boolean canDraw = mapTileLoader.markOverlayTileToDraw(desc.getHashCode()) >= 0;
                if (canDraw) {

                } else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MAX_MAP_ZOOM)) {
                    // f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
                    // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
                    // sind...
                    // daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
                    // gezeichnet werden (4 oder 16 oder 64...)
                    // dieser Aufruf kann auch rekursiv sein...
                    renderSmallerOverlayTiles(batch, ii, jj, zoomzoom);
                }
            }
        }
    }

    protected abstract void updateCacheList(boolean force);

    protected abstract void directLoadTiles(Descriptor upperLeftTile, Descriptor lowerRightTile, int aktZoom);

    public void initializeMap() {
        zoomBtn.setZoom(LocatorSettings.lastZoomLevel.getValue());
        // Bestimmung der ersten Position auf der Karte
        if (!positionInitialized) {
            double lat = LocatorSettings.MapInitLatitude.getValue();
            double lon = LocatorSettings.MapInitLongitude.getValue();

            // Initialisierungskoordinaten bekannt und k�nnen �bernommen werden
            if (lat != -1000 && lon != -1000) {
                setCenter(new CoordinateGPS(lat, lon));
                positionInitialized = true;
                // setLockPosition(0);
            } else {
                // GPS-Position bekannt?
                if (Locator.getInstance().Valid()) {
                    setCenter(Locator.getInstance().getMyPosition());
                    positionInitialized = true;
                } else {
                    setInitialLocation();
                }
            }
        }

        setNewSettings(INITIAL_ALL);

    }

    /**
     * Set the Initial location now. If you wont.
     */
    protected abstract void setInitialLocation();

    public abstract void setNewSettings(int InitialFlags);

    private void setScreenCenter(Vector2 newCenter) {
        synchronized (screenCenterT) {
            screenCenterT.set((long) newCenter.x, (long) (-newCenter.y));
        }
        if (isShown) isCreated = true;
        renderOnce("setScreenCenter");
    }

    public void setCenter(CoordinateGPS value) {
        synchronized (screenCenterWorld) {
            if (center == null)
                center = new CoordinateGPS(48.0, 12.0);
            positionInitialized = true;
            center = value;
            PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MAX_MAP_ZOOM, center.getLongitude()), Descriptor.LatitudeToTileY(MAX_MAP_ZOOM, center.getLatitude()), MAX_MAP_ZOOM,
                    MAX_MAP_ZOOM);
            setScreenCenter(new Vector2((float) point.X, (float) point.Y));
        }
    }

    /**
     * liefert die World-Koordinate in Pixel relativ zur Map in der höchsten Auflösung
     */
    protected Vector2 screenToWorld(Vector2 point) {
        if (camera != null) {
            try {
                synchronized (screenCenterWorld) {
                    float x = screenCenterWorld.getX() + (point.x - (float) mapIntWidth / 2f) * camera.zoom;
                    float y = -screenCenterWorld.getY() + (point.y - (float) mapIntHeight / 2f) * camera.zoom;
                    return new Vector2(x, y);
                }
            } catch (Exception ex) {
                Log.err(log, "screenToWorld", ex);
            }
        }
        return new Vector2(0, 0);
    }

    public Vector2 worldToScreen(Vector2 point) {
        Vector2 result = new Vector2(0, 0);
        if (camera != null) {
            try {
                result.x = ((long) point.x - screenCenterWorld.getX()) / camera.zoom + (float) mapIntWidth / 2;
                result.y = -(-(long) point.y + screenCenterWorld.getY()) / camera.zoom + (float) mapIntHeight / 2;
                result.add(-(float) mapIntWidth / 2, -(float) mapIntHeight / 2);
                result.rotate(mapHeading);
                result.add((float) mapIntWidth / 2, (float) mapIntHeight / 2);
            } catch (Exception ex) {
                Log.err(log, "worldToScreen", ex);
            }
        }
        return result;
    }

    protected Descriptor screenToDescriptor(Vector2 point, int zoom) {
        // World-Koordinaten in Pixel
        Vector2 world = screenToWorld(point);
        for (int i = MAX_MAP_ZOOM; i > zoom; i--) {
            world.x /= 2;
            world.y /= 2;
        }
        world.x /= 256;
        world.y /= 256;
        int x = (int) world.x;
        int y = (int) world.y;
        return new Descriptor(x, y, zoom);
    }

    @Override
    public void positionChanged() {
        if (isCarMode) {
            // im CarMode keine Netzwerk Koordinaten zulassen
            if (!Locator.getInstance().isGPSprovided())
                return;
        }
        if (getCenterGps())
            setCenter(Locator.getInstance().getMyPosition());

        renderOnce("PositionChanged");
    }

    @Override
    public void orientationChanged() {

        float heading = Locator.getInstance().getHeading();

        // im CarMode keine Richtungs �nderungen unter 20kmh
        if (isCarMode && Locator.getInstance().SpeedOverGround() < 20)
            heading = this.mapHeading;

        if (!this.NorthOriented || isCarMode) {
            this.mapHeading = heading;
            this.arrowHeading = 0;

            // da die Map gedreht in die offScreenBmp gezeichnet werden soll,
            // muss der Bereich, der gezeichnet werden soll gr��er sein, wenn
            // gedreht wird.
            if (heading >= 180)
                heading -= 180;
            if (heading > 90)
                heading = 180 - heading;
            double alpha = heading / 180 * Math.PI;
            double mapHeightCalcBase = mapIntHeight + (ySpeedVersatz * 1.7);
            double mapWidthCalcBase = mapIntWidth + (ySpeedVersatz * 1.7);
            double beta = Math.atan(mapWidthCalcBase / mapHeightCalcBase);
            double gammaW = Math.PI / 2 - alpha - beta;
            // halbe L�nge der Diagonalen
            double diagonal = Math.sqrt(Math.pow(mapWidthCalcBase, 2) + Math.pow(mapHeightCalcBase, 2)) / 2;
            drawingWidth = (int) (Math.cos(gammaW) * diagonal * 2);

            double gammaH = alpha - beta;
            drawingHeight = (int) (Math.cos(gammaH) * diagonal * 2);
        } else {
            this.mapHeading = 0;
            this.arrowHeading = heading;
            drawingWidth = mapIntWidth;
            drawingHeight = mapIntHeight;
        }

        renderOnce("OrientationChanged");
    }

    public void SetAlignToCompass(boolean value) {
        if (!value) {
            drawingWidth = mapIntWidth;
            drawingHeight = mapIntHeight;
        }
        this.NorthOriented = !value;
    }

    /**
     * Returns True, if MapState <br>
     * MapState.GPS<br>
     * MapState.LOCK<br>
     * MapState.CAR<br>
     *
     * @return
     */
    public boolean getCenterGps() {
        return mapState != MapState.FREE && mapState != MapState.WP;
    }

    public boolean GetAlignToCompass() {
        return !this.NorthOriented;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN) {
            lastTouchPos = new Vector2(x, y);
            return true;
        }

        y = mapIntHeight - y;
        // debugString = "touchDown " + x + " - " + y;
        if (inputState == InputState.Idle) {
            fingerDown.clear();
            inputState = InputState.IdleDown;
            fingerDown.put(pointer, new Point(x, y));
        } else {
            fingerDown.put(pointer, new Point(x, y));
            if (fingerDown.size() == 2)
                inputState = InputState.Zoom;
        }

        return true;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN) {
            // Mouse wheel scrolling => Zoom in/out

            if (lastDynamicZoom == -1)
                lastDynamicZoom = zoomBtn.getZoom();

            float div = lastTouchPos.x - x;

            float zoomValue = div / 100f;

            int maxZoom = LocatorSettings.OsmMaxLevel.getValue();
            int minZoom = LocatorSettings.OsmMinLevel.getValue();
            float dynZoom = (lastDynamicZoom - zoomValue);

            if (dynZoom > maxZoom)
                dynZoom = maxZoom;
            if (dynZoom < minZoom)
                dynZoom = minZoom;

            if (lastDynamicZoom != dynZoom) {

                // Log.debug(log, "Mouse Zoom:" + div + "/" + zoomValue + "/" + dynZoom);

                lastDynamicZoom = dynZoom;
                zoomBtn.setZoom((int) lastDynamicZoom);
                inputState = InputState.Idle;

                kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

                // kineticZoom = new KineticZoom(camera.zoom, lastDynamicZoom, System.currentTimeMillis(), System.currentTimeMillis() +
                // 1000);

                GL.that.addRenderView(MapViewBase.this, GL.FRAME_RATE_ACTION);
                renderOnce("onTouchDragged lastDynamicZoom != dynZoom");
                calcPixelsPerMeter();
            }

            return true;
        }

        try {
            y = mapIntHeight - y;
            // debugString = "touchDragged: " + x + " - " + y;
            // debugString = "touchDragged " + inputState.toString();
            if (inputState == InputState.IdleDown) {
                // es wurde 1x gedr�ckt -> testen, ob ein gewisser Minimum Bereich verschoben wurde
                Point p = fingerDown.get(pointer);
                if (p != null) {
                    // if ((Math.abs(p.x - x) > 10) || (Math.abs(p.y - y) > 10)) // this check is not necessary because this is already
                    // checked in GL.java
                    {
                        inputState = InputState.Pan;
                        // GL_Listener.glListener.addRenderView(this, frameRateAction);
                        renderOnce("inputState = InputState.Pan");
                        // xxx startTimer(frameRateAction);
                        // xxx ((GLSurfaceView) MapViewGL.ViewGl).requestRender();
                    }
                    return false;
                }
            }
            if (inputState == InputState.Button) {
                // wenn ein Button gedr�ckt war -> beim Verschieben nichts machen!!!
                return false;
            }

            if ((inputState == InputState.Pan) && (fingerDown.size() == 1)) {

                if (getMapState() == MapState.CAR || getMapState() == MapState.LOCK) {
                    // f�r verschieben gesperrt!
                    return false;
                } else {
                    // auf GPS oder WP ausgerichtet und wird jetzt auf Free gestellt
                    setMapStateFree();
                }

                // Fadein ZoomButtons!
                zoomBtn.resetFadeOut();

                // GL_Listener.glListener.addRenderView(this, frameRateAction);
                renderOnce("onTouchDragged inputState == InputState.Pan");
                // debugString = "";
                long faktor = getMapTilePosFactor(aktZoom);
                // debugString += faktor;
                LinkedHashMap<Integer, Point> fff = (LinkedHashMap) fingerDown;
                if (fff.values().size() == 0) return false;
                Point lastPoint = (Point) fingerDown.values().toArray()[0];
                // debugString += " - " + (lastPoint.x - x) * faktor + " - " + (y - lastPoint.y) * faktor;

                // camera.position.add((lastPoint.x - x) * faktor, (y - lastPoint.y) * faktor, 0);
                // screenCenterW.x = camera.position.x;
                // screenCenterW.y = camera.position.y;
                synchronized (screenCenterT) {
                    double angle = mapHeading * MathUtils.DEG_RAD;
                    int dx = (lastPoint.x - x);
                    int dy = (y - lastPoint.y);
                    int dxr = (int) (Math.cos(angle) * dx + Math.sin(angle) * dy);
                    int dyr = (int) (-Math.sin(angle) * dx + Math.cos(angle) * dy);
                    // debugString = dx + " - " + dy + " - " + dxr + " - " + dyr;

                    // Pan stufenlos anpassen an den aktuell g�ltigen Zoomfaktor
                    float tmpZoom = camera.zoom;
                    float ffaktor = 1.5f;
                    // ffaktor = ffaktor - iconFactor + 1;
                    ffaktor = ffaktor / iconFactor;
                    while (tmpZoom > ffaktor) {
                        tmpZoom /= 2;
                    }

                    screenCenterT.set(screenCenterT.getX() + (long) (dxr * faktor * tmpZoom), screenCenterT.getY() + (long) (dyr * faktor * tmpZoom));
                }
                calcCenter();

                // if (kineticPan == null) kineticPan = new KineticPan();
                // kineticPan.setLast(System.currentTimeMillis(), x, y);

                lastPoint.x = x;
                lastPoint.y = y;
            } else if ((inputState == InputState.Zoom) && (fingerDown.size() == 2)) {
                Point p1 = (Point) fingerDown.values().toArray()[0];
                Point p2 = (Point) fingerDown.values().toArray()[1];
                float originalDistance = (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

                if (fingerDown.containsKey(pointer)) {
                    // neue Werte setzen
                    fingerDown.get(pointer).x = x;
                    fingerDown.get(pointer).y = y;
                    p1 = (Point) fingerDown.values().toArray()[0];
                    p2 = (Point) fingerDown.values().toArray()[1];
                }
                float currentDistance = (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
                float ratio = originalDistance / currentDistance;
                camera.zoom = camera.zoom * ratio;

                if (camera.zoom < getMapTilePosFactor(zoomBtn.getMaxZoom())) {
                    camera.zoom = getMapTilePosFactor(zoomBtn.getMaxZoom());
                }
                if (camera.zoom > getMapTilePosFactor(zoomBtn.getMinZoom())) {
                    camera.zoom = getMapTilePosFactor(zoomBtn.getMinZoom());
                }

                lastDynamicZoom = camera.zoom;
                setActZoom();

                calcPixelsPerMeter();
                mapScale.ZoomChanged();
                zoomBtn.setZoom(aktZoom);

                if (!isCarMode && zoomScale != null) {
                    zoomScale.setZoom(convertCameraZoomToFloat(camera));
                    zoomScale.resetFadeOut();
                }

                return false;
            }

            // debugString = "State: " + inputState;
            return true;
        } catch (Exception ex) {
            Log.err(log, "MapView", "-onTouchDragged Error", ex);
        }
        return false;
    }

    private void setActZoom() {
        int zoom = MAX_MAP_ZOOM;
        float tmpZoom = camera.zoom;
        float faktor = 1.5f;
        faktor = faktor / iconFactor;
        while (tmpZoom > faktor) {
            tmpZoom /= 2;
            zoom--;
        }
        aktZoom = zoom;
    }

    protected void setMapStateFree() {
        setMapState(MapState.FREE);
    }

    protected void setZoomScale(int zoom) {
        if (!isCarMode && zoomScale != null)
            zoomScale.setZoom(zoom);
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {

        if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN) {
            return true;
        }

        y = mapIntHeight - y;
        // debugString = "touchUp: " + x + " - " + y;
        // debugString = "touchUp " + inputState.toString();
        if (inputState == InputState.IdleDown) {
            // es wurde gedr�ckt, aber nich verschoben
            fingerDown.remove(pointer);
            inputState = InputState.Idle;
            // -> Buttons testen

            // auf Button Clicks nur reagieren, wenn aktuell noch kein Finger gedr�ckt ist!!!
            if (kineticPan != null)
                // bei FingerKlick (wenn Idle) sofort das kinetische Scrollen stoppen
                kineticPan = null;

            inputState = InputState.Idle;
            return false;
        }

        fingerDown.remove(pointer);
        if (fingerDown.size() == 1)
            inputState = InputState.Pan;
        else if (fingerDown.size() == 0) {
            inputState = InputState.Idle;
            // wieder langsam rendern
            renderOnce("onTouchUp");

            if ((kineticZoom == null) && (kineticPan == null))
                GL.that.removeRenderView(this);

            if (kineticPan != null)
                kineticPan.start();
        }

        // debugString = "State: " + inputState;

        return true;
    }

    protected void calcCenter() {
        // berechnet anhand des ScreenCenterW die Center-Coordinaten
        PointD point = Descriptor.FromWorld(screenCenterWorld.getX(), screenCenterWorld.getY(), MAX_MAP_ZOOM, MAX_MAP_ZOOM);

        center = new CoordinateGPS(Descriptor.TileYToLatitude(MAX_MAP_ZOOM, -point.Y), Descriptor.TileXToLongitude(MAX_MAP_ZOOM, point.X));
    }

    protected void calcPixelsPerMeter() {

        float calcZoom = convertCameraZoomToFloat(camera);

        Coordinate dummy = CoordinateGPS.Project(center.getLatitude(), center.getLongitude(), 90, 1000);
        double l1 = Descriptor.LongitudeToTileX(calcZoom, center.getLongitude());
        double l2 = Descriptor.LongitudeToTileX(calcZoom, dummy.getLongitude());
        double diff = Math.abs(l2 - l1);
        pixelsPerMeter = (float) ((diff * 256) / 1000);

    }

    protected float getPixelsPerMeter(int ZoomLevel) {
        Coordinate dummy = CoordinateGPS.Project(center.getLatitude(), center.getLongitude(), 90, 1000);
        double l1 = Descriptor.LongitudeToTileX(ZoomLevel, center.getLongitude());
        double l2 = Descriptor.LongitudeToTileX(ZoomLevel, dummy.getLongitude());
        double diff = Math.abs(l2 - l1);
        return (float) ((diff * 256) / 1000);
    }

    public abstract void requestLayout();

    public int getAktZoom() {
        return aktZoom;
    }

    @Override
    public String getReceiverName() {
        return "Core.MapView";
    }

    @Override
    protected void skinIsChanged() {
        setBackground(Sprites.ListBack);
        invalidateTexture();
    }

    @Override
    public void invalidateTexture() {
        // Log.info(log, "invalidateTexture");
        setNewSettings(INITIAL_THEME);
        mapScale.ZoomChanged();

        GL.that.RunOnGLWithThreadCheck(() -> {
            if (CrossLines != null)
                CrossLines.dispose();
            CrossLines = null;
        });

    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    public MapState getMapState() {
        return mapState;
    }

    public void setMapState(MapState state) {
        if (mapState == state)
            return;

        mapState = state;

        boolean wasCarMode = isCarMode;

        if (mapState == MapState.CAR) {
            if (wasCarMode)
                return; // Brauchen wir nicht noch einmal machen!

            // Car mode
            isCarMode = true;
            invalidateTexture();
        } else if (mapState == MapState.WP) {
            MapStateChangedToWP();
        } else if (mapState == MapState.LOCK || mapState == MapState.GPS) {
            setCenter(Locator.getInstance().getMyPosition());
        }

        if (mapState != MapState.CAR) {
            if (!wasCarMode)
                return; // brauchen wir nicht noch einmal machen

            isCarMode = false;
            invalidateTexture();
        }

    }

    public abstract void MapStateChangedToWP();

    public void SetZoom(int newZoom) {

        if (zoomBtn != null) {
            if (zoomBtn.getZoom() != newZoom) {
                zoomBtn.setZoom(newZoom);
            }
        }

        setZoomScale(newZoom);
        if (zoomScale != null)
            zoomScale.resetFadeOut();
        inputState = InputState.Idle;

        lastDynamicZoom = newZoom;

        kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(newZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
        GL.that.addRenderView(MapViewBase.this, GL.FRAME_RATE_ACTION);
        renderOnce("SetZoom");
        calcPixelsPerMeter();
    }

    protected void renderOnce(String debugInfo) {
        try {
            synchronized (screenCenterT) {
                screenCenterWorld.set(screenCenterT);
            }
            Descriptor midTile = screenToDescriptor(midVector2, aktZoom);
            if (lastDescriptorOrdered.getHashCode() != midTile.getHashCode()) {
                lastDescriptorOrdered = midTile;
                // Log.info(log, debugInfo + " order: " + midTile);
                directLoadTiles(midTile, midTile, aktZoom);
                updateCacheList(debugInfo.contains("oom"));
            }
            GL.that.renderOnce();
        } catch (Exception ex) {
            Log.err(log, "debugInfo", ex);
        }
    }

    private float convertCameraZoomToFloat(OrthographicCamera cam) {
        if (cam == null || cam.zoom <= 0)
            return 0f;

        return MAX_MAP_ZOOM - (float) (Math.log(cam.zoom) / Math.log(2.0));
    }

    protected long getMapTilePosFactor(float zoom) {
        return (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
    }

    public boolean isCarMode() {
        return isCarMode;
    }

    public enum MapState {
        FREE, GPS, WP, LOCK, CAR
    }

    public enum InputState {
        Idle, IdleDown, Button, Pan, Zoom, PanAutomatic, ZoomAutomatic
    }

}