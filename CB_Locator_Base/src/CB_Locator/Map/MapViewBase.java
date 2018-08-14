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
package CB_Locator.Map;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Locator.Locator;
import CB_Locator.LocatorSettings;
import CB_UI_Base.Events.invalidateTextureEvent;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.*;
import CB_UI_Base.GL_UI.Controls.ZoomButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.utils.KineticPan;
import CB_UI_Base.GL_UI.utils.KineticZoom;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Math.Point;
import CB_Utils.Math.PointD;
import CB_Utils.Math.PointL;
import CB_Utils.MathUtils;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Timer;

/**
 * @author ging-buh
 * @author Longri
 * @author arbor95
 */
public abstract class MapViewBase extends CB_View_Base implements PositionChangedEvent, invalidateTextureEvent {
    public static final boolean debug = false;
    private static final String log = "MapViewBase";
    public static int INITIAL_SETTINGS = 1;
    public static int INITIAL_THEME = 2;
    public static int INITIAL_ALL = 7;
    public static int INITIAL_WITH_OUT_ZOOM = 8;
    public static int INITIAL_SETTINGS_WITH_OUT_ZOOM = 9;

    // ######################################
    public static MapTileLoader mapTileLoader = new MapTileLoader();
    public static int INITIAL_NEW_SETTINGS = 3;
    protected final int ZoomTime = 1000;
    protected final Vector2 loVector = new Vector2();
    protected final Vector2 ruVector = new Vector2();
    protected final Descriptor lo = new Descriptor();
    protected final Descriptor ru = new Descriptor();
    private final Point lastMovement = new Point(0, 0);
    private final HashMap<Integer, Point> fingerDown = new LinkedHashMap<Integer, Point>();
    public PointL screenCenterW = new PointL(0, 0);
    public CoordinateGPS center = new CoordinateGPS(48.0, 12.0);
    public float ySpeedVersatz = 200;
    public float pixelsPerMeter = 0;
    protected ZoomButtons zoomBtn;
    protected ZoomScale zoomScale;
    protected MapScale mapScale;
    protected float arrowHeading = 0;
    protected Vector2 myPointOnScreen;
    protected boolean showAccuracyCircle;
    protected int aktZoom;
    protected KineticZoom kineticZoom = null;
    protected float maxTilesPerScreen = 0;
    protected PointL screenCenterT = new PointL(0, 0);
    protected int mapIntWidth;
    protected int mapIntHeight;
    protected int drawingWidth;
    protected int drawingHeight;
    protected boolean positionInitialized = false;
    protected OrthographicCamera camera;
    protected boolean CarMode = false;
    protected boolean NorthOriented = true;
    protected float iconFactor = 1.5f;
    protected boolean showMapCenterCross;
    protected PolygonDrawable CrossLines = null;
    protected IChanged themeChangedEventHandler = new IChanged() {

        @Override
        public void isChanged() {
            MapViewBase.this.invalidateTexture();
        }
    };
    protected AccuracyDrawable accuracyDrawable = null;
    protected String mapsForgeThemePath;
    protected float scale;
    protected int outScreenDraw = 0;
    protected float lastDynamicZoom = -1;
    protected InputState inputState = InputState.Idle;
    // protected LoadedSortedTiles tilesToDraw = new LoadedSortedTiles((short) 10);
    long posx = 8745;
    long posy = 5685;
    long pos20y = 363904;
    long size20 = 256;
    long startTime;
    Timer myTimer;
    boolean useNewInput = true;
    boolean NightMode = false;
    int debugcount = 0;
    String str = "";
    private MapState mapState = MapState.FREE;
    // protected boolean alignToCompass = false;
    private float mapHeading = 0;
    private KineticPan kineticPan = null;

    public MapViewBase(String Name) {
        super(Name);
    }

    public MapViewBase(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
    }

    public MapViewBase(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name) {
        super(X, Y, Width, Height, Parent, Name);
    }

    public MapViewBase(CB_RectF rec, String Name) {
        super(rec, Name);
        invalidateTextureEventList.Add(this);
    }

    public MapViewBase(CB_RectF rec, GL_View_Base Parent, String Name) {
        super(rec, Parent, Name);
    }

    public MapViewBase(SizeF size, String Name) {
        super(size, Name);
    }

    public boolean GetNightMode() {
        return this.NightMode;
    }

    public void SetNightMode(boolean NightMode) {
        this.NightMode = NightMode;
    }

    public boolean GetNorthOriented() {
        return this.NorthOriented;
    }

    public void SetNorthOriented(boolean NorthOriented) {
        this.NorthOriented = NorthOriented;
    }

    @Override
    public void onShow() {
        PositionChangedEventList.Add(this);
        PositionChanged();

        CarMode = (getMapState() == MapState.CAR);
        if (!CarMode) {
            drawingWidth = mapIntWidth;
            drawingHeight = mapIntHeight;
        }

        setVisible();

        int zoom = MapTileLoader.MAX_MAP_ZOOM;
        float tmpZoom = camera.zoom;
        float faktor = 1.5f;
        faktor = faktor / iconFactor;
        while (tmpZoom > faktor) {
            tmpZoom /= 2;
            zoom--;
        }
        aktZoom = zoom;

        calcPixelsPerMeter();

    }

    @Override
    public void onHide() {
        PositionChangedEventList.Remove(this);
        setInvisible();
        onStop();// save last zoom and position
    }

    @Override
    public void dispose() {
        // remove eventHandler
        PositionChangedEventList.Remove(this);
        invalidateTextureEventList.Remove(this);
        super.dispose();
    }

    @Override
    public void onResized(CB_RectF rec) {
        if (rec.getWidth() <= 0 || rec.getHeight() <= 0)
            return;

        // wenn sich die Gr��e nicht ge�ndert hat, brauchen wir nicht zu machen!
        if (rec.getWidth() == this.mapIntWidth && rec.getHeight() == this.mapIntHeight) {
            // Ausser wenn Camera == null!
            if (camera != null)
                return;
        }
        this.mapIntWidth = (int) rec.getWidth();
        this.mapIntHeight = (int) rec.getHeight(); // Gdx.graphics.getHeight();
        this.drawingWidth = (int) rec.getWidth();
        this.drawingHeight = (int) rec.getHeight();

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        aktZoom = zoomBtn.getZoom();

        camera.zoom = MapTileLoader.getMapTilePosFactor(aktZoom);

        camera.position.set(0, 0, 0);

        // Log.debug(log, "MapView Size Changed MaxY=" + this.getMaxY());

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
            LocatorSettings.CurrentMapLayer.setValue(new String[0]);
        } else {
            LocatorSettings.CurrentMapLayer.setValue(newLayer.getNames());
        }
        mapTileLoader.setCurrentLayer(newLayer);
        mapTileLoader.clearLoadedTiles();
    }

    public void addAdditionalLayer(Layer layer) {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.addAdditionalMap(layer);
        LocatorSettings.CurrentMapLayer.setValue(currentLayer.getNames());
        mapTileLoader.setCurrentLayer(currentLayer);
        mapTileLoader.clearLoadedTiles();
    }

    public void clearAdditionalLayers() {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.clearAdditionalMaps();
        LocatorSettings.CurrentMapLayer.setValue(currentLayer.getNames());
        mapTileLoader.setCurrentLayer(currentLayer);
        mapTileLoader.clearLoadedTiles();
    }

    public void removeAdditionalLayer(Layer layer) {
        Layer currentLayer = mapTileLoader.getCurrentLayer();
        currentLayer.clearAdditionalMaps();
        LocatorSettings.CurrentMapLayer.setValue(currentLayer.getNames());
        mapTileLoader.setCurrentLayer(currentLayer);
        mapTileLoader.clearLoadedTiles();
    }

    public void SetCurrentOverlayLayer(Layer newLayer) {
        if (newLayer == null) {
            LocatorSettings.CurrentMapOverlayLayer.setValue("");
        } else {
            LocatorSettings.CurrentMapOverlayLayer.setValue(newLayer.Name);
        }
        mapTileLoader.setCurrentOverlayLayer(newLayer);
        mapTileLoader.clearLoadedTiles();
    }

    @Override
    protected void render(Batch batch) {

        if (LocatorSettings.MoveMapCenterWithSpeed.getValue() && CarMode && Locator.hasSpeed()) {

            double maxSpeed = LocatorSettings.MoveMapCenterMaxSpeed.getValue();

            double percent = Locator.SpeedOverGround() / maxSpeed;

            float diff = (float) ((this.getHeight()) / 3 * percent);
            if (diff > this.getHeight() / 3)
                diff = this.getHeight() / 3;

            ySpeedVersatz = diff;

        } else
            ySpeedVersatz = 0;

        boolean reduceFps = ((kineticZoom != null) || ((kineticPan != null) && (kineticPan.getStarted())));
        if (kineticZoom != null) {
            camera.zoom = kineticZoom.getAktZoom();
            // float tmpZoom = mapTileLoader.convertCameraZommToFloat(camera);
            // aktZoom = (int) tmpZoom;

            int zoom = MapTileLoader.MAX_MAP_ZOOM;
            float tmpZoom = camera.zoom;
            float faktor = 1.5f;
            // faktor = faktor - iconFactor + 1;
            faktor = faktor / iconFactor;
            while (tmpZoom > faktor) {
                tmpZoom /= 2;
                zoom--;
            }
            aktZoom = zoom;

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
                zoomScale.setZoom(MapTileLoader.convertCameraZommToFloat(camera));

        }

        if ((kineticPan != null) && (kineticPan.getStarted())) {
            long faktor = MapTileLoader.getMapTilePosFactor(aktZoom);
            Point pan = kineticPan.getAktPan();
            // debugString = pan.x + " - " + pan.y;
            // camera.position.add(pan.x * faktor, pan.y * faktor, 0);
            // screenCenterW.x = camera.position.x;
            // screenCenterW.y = camera.position.y;
            screenCenterT.x += pan.x * faktor;
            screenCenterT.y += pan.y * faktor;
            calcCenter();

            if (kineticPan.getFertig()) {
                kineticPan = null;
            } else
                reduceFps = false;
        }

        if (reduceFps) {
            GL.that.removeRenderView(this);
        }

        synchronized (screenCenterT) {
            screenCenterW.x = screenCenterT.x;
            screenCenterW.y = screenCenterT.y;
        }
        loadTiles();
        /*
         * if (alignToCompass) { camera.up.x = 0; camera.up.y = 1; camera.up.z = 0; camera.rotate(-mapHeading, 0, 0, 1); } else {
         * camera.up.x = 0; camera.up.y = 1; camera.up.z = 0; }
         */
        camera.update();

        // synchronized (screenCenterW)
        // {
        renderMapTiles(batch);
        renderSyncronOverlay(batch);
        // }
        // renderDebugInfo(batch);

        renderNonSyncronOverlay(batch);

    }

    protected abstract void renderSyncronOverlay(Batch batch);

    protected abstract void renderNonSyncronOverlay(Batch batch);

    private void renderMapTiles(Batch batch) {
        batch.disableBlending();

        float faktor = camera.zoom;
        float dx = this.thisWorldRec.getCenterPosX() - MainViewBase.mainView.getCenterPosX();
        float dy = this.thisWorldRec.getCenterPosY() - MainViewBase.mainView.getCenterPosY();

        dy -= ySpeedVersatz;

        camera.position.set(0, 0, 0);
        float dxr = dx;
        float dyr = dy;

        if (!this.NorthOriented || CarMode) {
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

        try {
            // das Alter aller Tiles um 1 erh�hen
            mapTileLoader.increaseLoadedTilesAge();
        } catch (Exception e) {
            // LogCat announces a java.util.ConcurrentModificationException
        }
        // for (int tmpzoom = zoom; tmpzoom <= zoom; tmpzoom++)
        {
            int tmpzoom = aktZoom;

            int halfMapIntWidth = mapIntWidth / 2;
            int halfMapIntHeight = mapIntHeight / 2;

            int halfDrawingtWidth = drawingWidth / 2;
            int halfDrawingHeight = drawingHeight / 2;

            loVector.set(halfMapIntWidth - halfDrawingtWidth, halfMapIntHeight - halfDrawingHeight - ySpeedVersatz);
            ruVector.set(halfMapIntWidth + halfDrawingtWidth, halfMapIntHeight + halfDrawingHeight + ySpeedVersatz);
            lo.set(screenToDescriptor(loVector, aktZoom, lo));
            ru.set(screenToDescriptor(ruVector, aktZoom, ru));

            for (int i = lo.getX(); i <= ru.getX(); i++) {
                for (int j = lo.getY(); j <= ru.getY(); j++) {
                    Descriptor desc = new Descriptor(i, j, tmpzoom, this.NightMode);
                    boolean canDraw = mapTileLoader.markToDraw(desc);
                    boolean canDrawOverlay = false;
                    if (mapTileLoader.getCurrentOverlayLayer() != null) {
                        canDrawOverlay = mapTileLoader.markToDrawOverlay(desc);
                    }

                    if (!canDraw && tmpzoom == aktZoom) {

                        // create this Tile new
                        desc.Data = this;
                        mapTileLoader.reloadTile(this, desc, aktZoom);

                        // f�r den aktuellen Zoom ist kein Tile vorhanden ->
                        // kleinere Zoomfaktoren durchsuchen
                        if (!renderBiggerTiles(batch, i, j, aktZoom)) {
                            // gr��ere Zoomfaktoren noch durchsuchen, ob davon Tiles
                            // vorhanden sind...
                            // daf�r m�ssen aber pro fehlendem Tile mehrere kleine
                            // Tiles gezeichnet werden (4 oder 16 oder 64...)
                            // dieser Aufruf kann auch rekursiv sein...
                            renderSmallerTiles(batch, i, j, aktZoom);
                        }
                    }

                    if (mapTileLoader.getCurrentOverlayLayer() != null) {
                        if (!canDrawOverlay && tmpzoom == aktZoom) {
                            if (!renderBiggerOverlayTiles(batch, i, j, aktZoom))
                                renderSmallerOverlayTiles(batch, i, j, aktZoom);
                        }
                    }
                }
            }
        }

        // FIXME Change to Sorted List, close Texture changing!!
        /*
         * Sort First Symbols then Text!
         *
         * Sort Symbols with Texture, close Texture changing!
         *
         * Sort Text with TextType and Size, close Texture changing!
         */
        CB_List<TileGL_RotateDrawables> rotateList = new CB_List<TileGL_RotateDrawables>();

        mapTileLoader.sort();

        synchronized (screenCenterW) {
            for (int i = mapTileLoader.getDrawingSize() - 1; i > -1; i--) {
                TileGL tile = mapTileLoader.getDrawingTile(i);
                if (tile == null)
                    continue;

                // Faktor, mit der dieses MapTile vergr��ert gezeichnet
                // werden mu�
                long posFactor = getscaledMapTilePosFactor(tile);

                long xPos = tile.Descriptor.getX() * posFactor * tile.getWidth() - screenCenterW.x;
                long yPos = -(tile.Descriptor.getY() + 1) * posFactor * tile.getHeight() - screenCenterW.y;
                float xSize = tile.getWidth() * posFactor;
                float ySize = tile.getHeight() * posFactor;

                // Draw Names and Symbols only from Tile with right zoom factor
                boolean addToRotateList = tile.Descriptor.getZoom() == aktZoom;

                tile.draw(batch, xPos, yPos, xSize, ySize, addToRotateList ? rotateList : null);

            }
            batch.enableBlending();

            // FIXME sort rotate List first the Symbols then the Text! sort Text with same Font!
            // Don't change the Texture (improve the Performance)

            for (int i = 0, n = rotateList.size(); i < n; i++) {
                TileGL_RotateDrawables drw = rotateList.get(i);
                if (drw != null)
                    drw.draw(batch, -mapHeading);
            }
            rotateList.truncate(0);
            rotateList = null;

        }
        mapTileLoader.clearDrawingTiles();

        if (mapTileLoader.getCurrentOverlayLayer() != null) {
            synchronized (screenCenterW) {
                for (int i = mapTileLoader.getDrawingSizeOverlay() - 1; i > -1; i--) {
                    TileGL tile = mapTileLoader.getDrawingTileOverlay(i);
                    if (tile == null)
                        continue;

                    // Faktor, mit der dieses MapTile vergr��ert gezeichnet
                    // werden mu�
                    long posFactor = getscaledMapTilePosFactor(tile);

                    long xPos = tile.Descriptor.getX() * posFactor * tile.getWidth() - screenCenterW.x;
                    long yPos = -(tile.Descriptor.getY() + 1) * posFactor * tile.getHeight() - screenCenterW.y;
                    float xSize = tile.getWidth() * posFactor;
                    float ySize = tile.getHeight() * posFactor;
                    tile.draw(batch, xPos, yPos, xSize, ySize, rotateList);

                }
            }
            mapTileLoader.clearDrawingTilesOverlay();
        }

    }

    private long getscaledMapTilePosFactor(TileGL tile) {
        if (tile == null || tile.Descriptor == null)
            return 1;
        long result = 1;
        result = (long) (Math.pow(2.0, MapTileLoader.MAX_MAP_ZOOM - tile.Descriptor.getZoom()) / tile.getScaleFactor());
        return result;
    }

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

        str = "lTiles: " + mapTileLoader.LoadedTilesSize() + " - qTiles: " + mapTileLoader.QueuedTilesSize();
        font.draw(batch, str, 20, 60);

        str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
        font.draw(batch, str, 20, 20);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    }

    // FIXME make point and vPoint final and setValues!
    protected void renderPositionMarker(Batch batch) {
        PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, Locator.getLongitude()), Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, Locator.getLatitude()), MapTileLoader.MAX_MAP_ZOOM,
                MapTileLoader.MAX_MAP_ZOOM);

        Vector2 vPoint = new Vector2((float) point.X, -(float) point.Y);

        myPointOnScreen = worldToScreen(vPoint);

        myPointOnScreen.y -= ySpeedVersatz;

        if (showAccuracyCircle) {

            if (accuracyDrawable == null) {
                accuracyDrawable = new AccuracyDrawable(this.mapIntWidth, this.mapIntWidth);
            }

            float radius = (pixelsPerMeter * Locator.getCoordinate().getAccuracy());

            if (radius > GL_UISizes.PosMarkerSize / 2) {
                accuracyDrawable.draw(batch, myPointOnScreen.x, myPointOnScreen.y, radius);
            }
        }

        boolean lastUsedCompass = Locator.UseMagneticCompass();
        boolean Transparency = LocatorSettings.PositionMarkerTransparent.getValue();

        int arrowId = 0;
        if (lastUsedCompass) {
            arrowId = Transparency ? 1 : 0;
        } else {
            arrowId = Transparency ? 3 : 2;
        }

        if (CarMode)
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
        // f�r den aktuellen Zoom ist kein Tile vorhanden -> kleinere
        // Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
        // von dem gefundenen Tile mu� dann nur ein Ausschnitt gezeichnet werden
        int ii = i / 2;
        int jj = j / 2;
        int zoomzoom = zoom2 - 1;

        Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
        boolean canDraw = mapTileLoader.markToDraw(desc);

        if (canDraw) {
            // das Alter der benutzten Tiles nicht auf 0 setzen, da dies
            // eigentlich nicht das richtige Tile ist!!!
            // tile.Age = 0;

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

        Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
        boolean canDraw = mapTileLoader.markToDrawOverlay(desc);

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
                Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
                boolean canDraw = mapTileLoader.markToDraw(desc);
                if (canDraw) {

                } else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MapTileLoader.MAX_MAP_ZOOM)) {
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
                Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
                boolean canDraw = mapTileLoader.markToDrawOverlay(desc);
                if (canDraw) {

                } else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MapTileLoader.MAX_MAP_ZOOM)) {
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

    protected void loadTiles() {
        int halfMapIntWidth = mapIntWidth / 2;
        int halfMapIntHeight = mapIntHeight / 2;

        int extensionTop = (int) ((halfMapIntHeight - ySpeedVersatz) * 1.5);
        int extensionBottom = (int) ((halfMapIntHeight + ySpeedVersatz) * 1.5);
        int extensionLeft = (int) (halfMapIntWidth * 1.5);
        int extensionRight = (int) (halfMapIntWidth * 1.5);

        loVector.set(halfMapIntWidth - drawingWidth / 2 - extensionLeft, halfMapIntHeight - drawingHeight / 2 - extensionTop);
        ruVector.set(halfMapIntWidth + drawingWidth / 2 + extensionRight, halfMapIntHeight + drawingHeight / 2 + extensionBottom);
        lo.set(screenToDescriptor(loVector, aktZoom, lo));
        ru.set(screenToDescriptor(ruVector, aktZoom, ru));

        // check count of Tiles
        boolean CacheisToSmall = true;
        int cacheSize = mapTileLoader.getCacheSize();
        do {
            int x = ru.X - lo.X + 1;
            int y = ru.Y - lo.Y + 1;
            int count = x * y;
            if (count <= cacheSize) {
                CacheisToSmall = false;
            } else {
                lo.X++;
                lo.Y++;
                ru.X--;
                ru.Y--;
            }

        } while (CacheisToSmall);

        mapTileLoader.loadTiles(this, lo, ru, aktZoom);

    }

    public void InitializeMap() {
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
                if (Locator.Valid()) {
                    setCenter(Locator.getCoordinate());
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

    protected void setTheme(String Path) {
        if (Path.length() > 0) {
            if (Path.equals(ManagerBase.INTERNAL_THEME_CAR) || Path.equals(ManagerBase.INTERNAL_THEME_DEFAULT) || Path.equals(ManagerBase.INTERNAL_THEME_OSMARENDER)) {
                mapsForgeThemePath = Path;
            } else if (FileIO.FileExists(Path) && FileIO.GetFileExtension(Path).contains("xml")) {
                mapsForgeThemePath = Path;
            } else
                mapsForgeThemePath = "";
        } else
            mapsForgeThemePath = "";
    }

    private void setScreenCenter(Vector2 newCenter) {
        synchronized (screenCenterT) {
            screenCenterT.x = (long) newCenter.x;
            screenCenterT.y = (long) (-newCenter.y);
        }
        // if (camera != null) camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);
        GL.that.renderOnce();
    }

    public void setCenter(CoordinateGPS value) {
        synchronized (screenCenterW) {

            if (center == null)
                center = new CoordinateGPS(48.0, 12.0);
            positionInitialized = true;
            center = value;
            PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, center.getLongitude()), Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, center.getLatitude()), MapTileLoader.MAX_MAP_ZOOM,
                    MapTileLoader.MAX_MAP_ZOOM);
            setScreenCenter(new Vector2((float) point.X, (float) point.Y));
        }
    }

    /**
     * liefert die World-Koordinate in Pixel relativ zur Map in der höchsten Auflösung
     */
    protected Vector2 screenToWorld(Vector2 point) {
        // Vector2 result = new Vector2(0, 0);
        try {
            synchronized (screenCenterW) {
                point.x = screenCenterW.x + ((long) point.x - mapIntWidth / 2) * camera.zoom;
                point.y = -screenCenterW.y + ((long) point.y - mapIntHeight / 2) * camera.zoom;
            }
        } catch (Exception e) {
            // wenn hier ein Fehler auftritt, dann geben wir einen Vector 0,0 zur�ck!
            point.x = 0;
            point.y = 0;
        }
        return point;
    }

    public Vector2 worldToScreen(Vector2 point) {

        Vector2 result = new Vector2(0, 0);
        result.x = ((long) point.x - screenCenterW.x) / camera.zoom + (float) mapIntWidth / 2;
        result.y = -(-(long) point.y + screenCenterW.y) / camera.zoom + (float) mapIntHeight / 2;
        result.add(-(float) mapIntWidth / 2, -(float) mapIntHeight / 2);
        result.rotate(mapHeading);
        result.add((float) mapIntWidth / 2, (float) mapIntHeight / 2);
        return result;

    }

    protected Descriptor screenToDescriptor(Vector2 point, int zoom, Descriptor destDescriptor) {
        // World-Koordinaten in Pixel
        Vector2 world = screenToWorld(point);
        for (int i = MapTileLoader.MAX_MAP_ZOOM; i > zoom; i--) {
            world.x /= 2;
            world.y /= 2;
        }
        world.x /= 256;
        world.y /= 256;
        int x = (int) world.x;
        int y = (int) world.y;
        destDescriptor.set(x, y, zoom, NightMode);
        // Descriptor result = new Descriptor(x, y, zoom, NightMode);
        return destDescriptor;
    }

    @Override
    public void PositionChanged() {
        if (CarMode) {
            // im CarMode keine Netzwerk Koordinaten zulassen
            if (!Locator.isGPSprovided())
                return;
        }
        if (getCenterGps())
            setCenter(Locator.getCoordinate());

        GL.that.renderOnce();
    }

    @Override
    public void OrientationChanged() {

        float heading = Locator.getHeading();

        // im CarMode keine Richtungs �nderungen unter 20kmh
        if (CarMode && Locator.SpeedOverGround() < 20)
            heading = this.mapHeading;

        if (!this.NorthOriented || CarMode) {
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

        GL.that.renderOnce();
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

                kineticZoom = new KineticZoom(camera.zoom, MapTileLoader.getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

                // kineticZoom = new KineticZoom(camera.zoom, lastDynamicZoom, System.currentTimeMillis(), System.currentTimeMillis() +
                // 1000);

                GL.that.addRenderView(MapViewBase.this, GL.FRAME_RATE_ACTION);
                GL.that.renderOnce();
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
                        GL.that.renderOnce();
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
                    SetMapStateFree();
                }

                // Fadein ZoomButtons!
                zoomBtn.resetFadeOut();

                // GL_Listener.glListener.addRenderView(this, frameRateAction);
                GL.that.renderOnce();
                // debugString = "";
                long faktor = MapTileLoader.getMapTilePosFactor(aktZoom);
                // debugString += faktor;
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

                    screenCenterT.x += (long) (dxr * faktor * tmpZoom);
                    screenCenterT.y += (long) (dyr * faktor * tmpZoom);
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

                if (camera.zoom < MapTileLoader.getMapTilePosFactor(zoomBtn.getMaxZoom())) {
                    camera.zoom = MapTileLoader.getMapTilePosFactor(zoomBtn.getMaxZoom());
                }
                if (camera.zoom > MapTileLoader.getMapTilePosFactor(zoomBtn.getMinZoom())) {
                    camera.zoom = MapTileLoader.getMapTilePosFactor(zoomBtn.getMinZoom());
                }

                lastDynamicZoom = camera.zoom;

                int zoom = MapTileLoader.MAX_MAP_ZOOM;
                float tmpZoom = camera.zoom;
                float faktor = 1.5f;
                // faktor = faktor - iconFactor + 1;
                faktor = faktor / iconFactor;
                while (tmpZoom > faktor) {
                    tmpZoom /= 2;
                    zoom--;
                }
                aktZoom = zoom;

                calcPixelsPerMeter();
                mapScale.ZoomChanged();
                zoomBtn.setZoom(aktZoom);

                if (!CarMode && zoomScale != null) {
                    zoomScale.setZoom(MapTileLoader.convertCameraZommToFloat(camera));
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

    protected void SetMapStateFree() {
        setMapState(MapState.FREE);
    }

    protected void setZoomScale(int zoom) {
        if (!CarMode && zoomScale != null)
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
            GL.that.renderOnce();

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
        PointD point = Descriptor.FromWorld(screenCenterW.x, screenCenterW.y, MapTileLoader.MAX_MAP_ZOOM, MapTileLoader.MAX_MAP_ZOOM);

        center = new CoordinateGPS(Descriptor.TileYToLatitude(MapTileLoader.MAX_MAP_ZOOM, -point.Y), Descriptor.TileXToLongitude(MapTileLoader.MAX_MAP_ZOOM, point.X));
    }

    protected void calcPixelsPerMeter() {

        float calcZoom = MapTileLoader.convertCameraZommToFloat(camera);

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
    protected void SkinIsChanged() {
        setBackground(Sprites.ListBack);
        invalidateTexture();
    }

    @Override
    public void invalidateTexture() {
        setNewSettings(INITIAL_THEME);
        mapTileLoader.clearLoadedTiles();
        mapScale.ZoomChanged();

        GL.that.RunOnGLWithThreadCheck(new IRunOnGL() {
            @Override
            public void run() {
                if (CrossLines != null)
                    CrossLines.dispose();
                CrossLines = null;
            }
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

        boolean wasCarMode = CarMode;

        if (mapState == MapState.CAR) {
            if (wasCarMode)
                return; // Brauchen wir nicht noch einmal machen!

            // Car mode
            CarMode = true;
            invalidateTexture();
        } else if (mapState == MapState.WP) {
            MapStateChangedToWP();
        } else if (mapState == MapState.LOCK || mapState == MapState.GPS) {
            setCenter(Locator.getCoordinate());
        }

        if (mapState != MapState.CAR) {
            if (!wasCarMode)
                return; // brauchen wir nicht noch einmal machen

            CarMode = false;
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

        kineticZoom = new KineticZoom(camera.zoom, MapTileLoader.getMapTilePosFactor(newZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
        GL.that.addRenderView(MapViewBase.this, GL.FRAME_RATE_ACTION);
        GL.that.renderOnce();
        calcPixelsPerMeter();
    }

    public boolean isCarMode() {
        return CarMode;
    }

    public enum MapState {
        FREE, GPS, WP, LOCK, CAR
    }

    public enum InputState {
        Idle, IdleDown, Button, Pan, Zoom, PanAutomatic, ZoomAutomatic
    }

}