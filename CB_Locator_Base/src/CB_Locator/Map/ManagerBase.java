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

import CB_Locator.LocatorSettings;
import CB_Locator.Map.Layer.LayerType;
import CB_Locator.Map.Layer.MapType;
import CB_UI_Base.graphics.extendedInterfaces.ext_Bitmap;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.HSV_Color;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.http.Webb;
import CB_Utils.http.WebbUtils;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.datastore.MultiMapDataStore.DataPolicy;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.*;
import org.mapsforge.map.rendertheme.rule.CB_RenderThemeHandler;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * @author ging-buh
 * @author Longri
 */
public abstract class ManagerBase {

    public static final String INTERNAL_THEME_DEFAULT = "Default";
    public static final String INTERNAL_THEME_OSMARENDER = "OsmaRender";
    public static final String INTERNAL_THEME_CAR = "Car";
    public static final float DEFAULT_TEXT_SCALE = 1;
    private static final String log = "ManagerBase";
    public static ManagerBase manager;
    static int PROCESSOR_COUNT; // == nr of threads for getting tiles (mapsforge)
    private final TileCache firstLevelTileCache = new InMemoryTileCache(128); //mapsforge
    private final Layer[] userMaps = new Layer[2];
    public float textScale = 1;
    protected ArrayList<PackBase> mapPacks = new ArrayList<>(); // loadLocalPixmap differs in Android and Java
    private DisplayModel displayModel;
    private MultiMapDataStore[] mapDatabase = null;
    private ArrayList<Layer> layers = new ArrayList<>();
    private ArrayList<TmsMap> tmsMaps = new ArrayList<>();
    private boolean mayAddLayer = false; // add only during startup (why?)
    private String mapsforgeThemesStyle = "";
    private String mapsforgeTheme = "";
    private boolean alreadySet = false;
    private DatabaseRenderer[] databaseRenderers = null;
    private RenderThemeFuture renderThemeFuture;

    public ManagerBase() {
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        Log.info(log, "Number of processors: " + PROCESSOR_COUNT);

        if (LocatorSettings.CurrentMapLayer != null)
            LocatorSettings.CurrentMapLayer.addSettingChangedListener(() -> {
                Layer layer = getOrAddLayer(LocatorSettings.CurrentMapLayer.getValue(), "", "");
                if (layer.isMapsForge())
                    initMapDatabase(layer);
            });

        LocatorSettings.UserMap1.addSettingChangedListener(() -> {
            try {
                if (userMaps[0] != null) {
                    layers.remove(userMaps[0]);
                }
                String url = LocatorSettings.UserMap1.getValue();
                userMaps[0] = getUserMap(url, "UserMap1");
                layers.add(userMaps[0]);
            } catch (Exception e) {
                Log.err(log, "Initial UserMap1", e);
            }
        });

        LocatorSettings.UserMap2.addSettingChangedListener(() -> {
            try {
                if (userMaps[1] != null) {
                    layers.remove(userMaps[1]);
                }
                String url = LocatorSettings.UserMap2.getValue();
                userMaps[1] = getUserMap(url, "UserMap2");
                layers.add(userMaps[1]);
            } catch (Exception e) {
                Log.err(log, "Initial UserMap2", e);
            }
        });

        manager = this;
    }

    /**
     * for night modus
     * <p>
     * The matrix is stored in a single array, and its treated as follows: [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] <br>
     * <br>
     * When applied to a color [r, g, b, a], the resulting color is computed as (after clamping) <br>
     * R' = a*R + b*G + c*B + d*A + e;<br>
     * G' = f*R + g*G + h*B + i*A + j;<br>
     * B' = k*R + l*G + m*B + n*A + o;<br>
     * A' = p*R + q*G + r*B + s*A + t;<br>
     *
     * @param matrix
     * @return
     */
    public static ImageData getImageDataWithColormatrixManipulation(float[] matrix, ImageData imgData) {

        int[] data = imgData.PixelColorArray;
        for (int i = 0; i < data.length; i++) {
            data[i] = HSV_Color.colorMatrixManipulation(data[i], matrix);
        }
        return imgData;
    }

    public Layer getOrAddLayer(String[] Name, String friendlyName, String url) {
        if (Name[0] == "OSM" || Name[0] == "")
            Name[0] = "Mapnik";

        for (Layer layer : layers) {
            if (layer.Name.equalsIgnoreCase(Name[0])) {

                // add aditional
                // todo : this adding is only necessary when setting from Config. Otherwise the adds are done directly to the layers additionalMapsforgeLayer.
                // therefore checked on additionalMapsforgeLayer.add for duplicates. A bit
                if (Name.length > 1) {
                    for (int i = 1; i < Name.length; i++) {
                        for (Layer la : layers) {
                            if (la.Name.equalsIgnoreCase(Name[i])) {
                                layer.addAdditionalMap(la);
                            }
                        }
                    }
                }

                return layer;
            }
        }

        if (mayAddLayer) {
            Layer newLayer = new Layer(MapType.ONLINE, LayerType.normal, Layer.StorageType.PNG, Name[0], Name[0], url);
            layers.add(newLayer);
            return newLayer;
        } else {
            if (layers != null && layers.size() > 0) {
                Layer firstLayer = layers.get(0);
                LocatorSettings.CurrentMapLayer.setValue(firstLayer.getNames());
                return firstLayer; // ist wahrscheinlich Mapnik und sollte immer tun
            }
            return null;
        }
    }

    /**
     * Load Tile from URL and save to MapTile-Cache
     *
     * @param layer Layer
     * @param tile  Descriptor
     * @return boolean
     */
    boolean cacheTile(Layer layer, Descriptor tile) {

        if (layer == null) {
            Log.err(log, "layer = null");
            return false;
        }

        // get mapPack from layer and check, if tile is covered (can be generated) from mapPack then simply return
        for (PackBase pack : mapPacks)
            if (pack.layer == layer)
                if (pack.contains(tile) != null)
                    return true;

        // Download from url into cache
        String filename = layer.GetLocalFilename(tile);
        String url = layer.GetUrl(tile);
        if (!url.startsWith("http")) {
            return false;
        }

        // Falls Kachel schon geladen wurde, kann sie übersprungen werden
        synchronized (this) {
            if (FileIO.fileExists(filename))
                return true;
        }

        // Kachel laden
        synchronized (this) {
            // Verzeichnis anlegen
            if (!FileIO.createDirectory(filename))
                return false;
        }

        try {
            Log.info(log, "Caching " + url + " to " + filename);
            FileOutputStream stream = new FileOutputStream(filename, false);
            InputStream fromUrl;
            // 15 sec
            int CONECTION_TIME_OUT = 15000;
            fromUrl = Webb.create()
                    .get(url)
                    .connectTimeout(CONECTION_TIME_OUT)
                    .ensureSuccess()
                    .asStream()
                    .getBody();
            WebbUtils.copyStream(fromUrl, stream);
            fromUrl.close();
            stream.close();
            return true;
        } catch (Exception ex) {
            Log.err(log, "Download from url (into cache) " + url + " : " + ex.toString());
            return false;
        }
    }

    private void loadMapPack(String file) {
        try {
            PackBase pack = getMapPack(file);
            layers.add(pack.layer);
            mapPacks.add(pack);
            Collections.sort(mapPacks);
        } catch (Exception ignored) {
        }
    }

    protected abstract PackBase getMapPack(String file) throws Exception;

    private void loadMapsforge(String pathAndName) {
        try {
            MapFile mapFile = new MapFile(pathAndName);
            MapFileInfo mapInfo = mapFile.getMapFileInfo();
            MapType mapType = MapType.MAPSFORGE;
            if (mapInfo.comment != null && mapInfo.comment.contains("FZK")) {
                mapType = MapType.FREIZEITKARTE;
            }
            String Name = FileIO.getFileNameWithoutExtension(pathAndName);
            Layer layer = new Layer(mapType, LayerType.normal, Layer.StorageType.PNG, Name, Name, pathAndName);
            layer.languages = mapFile.getMapLanguages();
            layers.add(layer);
        } catch (Exception e) {
            Log.err(log, "Load mapsforge for " + pathAndName + ":\n" + e.toString());
        }
    }

    private void loadTMS(String string) {
        try {
            TmsMap tmsMap = new TmsMap(string);
            if ((tmsMap.name == null) || (tmsMap.url == null)) {
                return;
            }
            layers.add(new TmsLayer(LayerType.normal, tmsMap));
        } catch (Exception ignored) {
        }
    }

    private void loadBSH(String string) {
        try {
            layers.add(new BshLayer(LayerType.normal, string));
        } catch (Exception ignored) {
        }
    }

    public void initMapPacks() {
        layers.clear();

        mayAddLayer = true;
        layers.add(new Layer(MapType.ONLINE, LayerType.normal, Layer.StorageType.PNG, "Mapnik", "Mapnik", "http://c.tile.openstreetmap.org/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.normal, Layer.StorageType.PNG, "OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png"));
        // layers.add(new Layer(MapType.ONLINE, Type.normal, "Esri", "", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"));
        // layers.add(new Layer(MapType.ONLINE, Type.normal, "Google Hybrid", "", "http://mt0.google.com/vt/lyrs=y@142&x={x}&y={y}&z={z}"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "hillshading", "hillshading", "http://a.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "hiking", "hiking", "http://tile.waymarkedtrails.org/hillshading/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "public_transport", "public_transport", "http://tile.memomaps.de/tilegen/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "railway", "railway", "http://a.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png")); // Eisenbahn
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "cycling", "cycling", "http://a.www.toolserver.org/tiles/bicycle_network/{z}/{x}/{y}.png")); // Radwege Alternative
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "mtb", "mtb", "http://tile.waymarkedtrails.org/mtb/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "riding", "riding", "http://tile.waymarkedtrails.org/riding/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "skating", "skating", "http://tile.waymarkedtrails.org/skating/{z}/{x}/{y}.png"));
        layers.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "slopemap", "slopemap", "http://tile.waymarkedtrails.org/slopemap/{z}/{x}/{y}.png"));

        try {
            String url = LocatorSettings.UserMap1.getValue();
            if (url.length() == 0) {
                url = LocatorSettings.UserMap1.getDefaultValue();
            }
            userMaps[0] = getUserMap(url, "UserMap1");
            layers.add(userMaps[0]);
        } catch (Exception e) {
            Log.err(log, "Initial UserMap1", e);
        }

        try {
            String url = LocatorSettings.UserMap2.getValue();
            if (url.length() > 0) {
                userMaps[1] = getUserMap(url, "UserMap2");
                layers.add(userMaps[1]);
            }
        } catch (Exception e) {
            Log.err(log, "Initial UserMap2", e);
        }

        Array<String> alreadyAdded = new Array<>(); // avoid same file in different directories
        Log.debug(log, "dirOwnMaps = " + LocatorSettings.MapPackFolderLocal.getValue());
        addToLayers(LocatorSettings.MapPackFolderLocal.getValue(), alreadyAdded);
        Log.debug(log, "dirGlobalMaps = " + LocatorSettings.MapPackFolder.getValue());
        addToLayers(LocatorSettings.MapPackFolder.getValue(), alreadyAdded);
        mayAddLayer = false;
    }

    private void addToLayers(String directoryName, Array<String> alreadyAdded) {
        String[] fileNames = FileFactory.createFile(directoryName).list();
        if (fileNames != null && fileNames.length > 0) {
            for (String fileName : fileNames) {
                if (!alreadyAdded.contains(fileName, false)) {
                    String lowerCaseFileName = fileName.toLowerCase();
                    if (lowerCaseFileName.endsWith("pack")) {
                        loadMapPack(directoryName + "/" + fileName);
                    } else if (lowerCaseFileName.endsWith("map")) {
                        loadMapsforge(directoryName + "/" + fileName);
                    } else if (lowerCaseFileName.endsWith("xml")) {
                        loadTMS(directoryName + "/" + fileName);
                    } else if (lowerCaseFileName.endsWith("bsh")) {
                        loadBSH(directoryName + "/" + fileName);
                    }
                    alreadyAdded.add(fileName);
                }
            }
        }
    }

    private Layer getUserMap(String url, String name) {
        try {
            Log.info(log, "getUserMap by url=" + url + " Name=" + name);
            Layer.StorageType storageType = Layer.StorageType.PNG;
            if (url.contains("{name:")) {
                //replace name
                int pos = url.indexOf("{name:");
                int endPos = url.indexOf("}", pos);
                String nameTag = url.substring(pos, endPos + 1);
                url = url.replace(nameTag, "");
                name = nameTag.replace("{name:", "").replace("}", "");
            }
            if (url.toLowerCase().contains("{jpg}")) {
                storageType = Layer.StorageType.JPG;
                url = url.replace("{JPG}", "").replace("{jpg}", "").trim();
            } else if (url.toLowerCase().contains("{png}")) {
                url = url.replace("{PNG}", "").replace("{png}", "").trim();
            }
            return new Layer(MapType.ONLINE, LayerType.normal, storageType, name, "", url);
        } catch (Exception e) {
            Log.err(log, "Err while getUserMap: url=" + url + " Name=" + name + " Err=" + e.getLocalizedMessage());
            return new Layer(MapType.ONLINE, LayerType.normal, Layer.StorageType.PNG, name, "", url);
        }
    }

    public void setRenderTheme(String theme, String themestyle) {
        if (alreadySet)
            if (theme.equals(mapsforgeTheme))
                if (themestyle.equals(mapsforgeThemesStyle))
                    return;
        mapsforgeThemesStyle = themestyle;
        mapsforgeTheme = theme;
        XmlRenderTheme renderTheme;
        if (mapsforgeTheme.length() == 0) {
            Log.info(log, "Use RenderTheme CB_InternalRenderTheme.DEFAULT");
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        } else if (mapsforgeTheme.equals(INTERNAL_THEME_OSMARENDER)) {
            Log.info(log, "Use CB_InternalRenderTheme OSMARENDER");
            renderTheme = CB_InternalRenderTheme.OSMARENDER;
        } else if (mapsforgeTheme.equals(INTERNAL_THEME_CAR)) {
            Log.info(log, "Use CB_InternalRenderTheme CAR");
            renderTheme = CB_InternalRenderTheme.CAR;
        } else if (mapsforgeTheme.equals(INTERNAL_THEME_DEFAULT)) {
            Log.info(log, "Use CB_InternalRenderTheme DEFAULT");
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        } else {
            Log.info(log, "Use RenderTheme " + mapsforgeTheme + " with " + mapsforgeThemesStyle);
            try {
                File file = FileFactory.createFile(mapsforgeTheme);
                if (file.exists()) {
                    java.io.File themeFile = new java.io.File(file.getAbsolutePath());
                    renderTheme = new ExternalRenderTheme(themeFile, new Xml_RenderThemeMenuCallback());
                } else {
                    Log.err(log, mapsforgeTheme + " not found!");
                    renderTheme = CB_InternalRenderTheme.DEFAULT;
                }
            } catch (Exception e) {
                Log.err(log, "Load RenderTheme", "Error loading RenderTheme!", e);
                renderTheme = CB_InternalRenderTheme.DEFAULT;
            }
        }

        try {
            CB_RenderThemeHandler.getRenderTheme(getGraphicFactory(displayModel.getScaleFactor()), displayModel, renderTheme);
        } catch (Exception e) {
            Log.err(log, "Error in checking RenderTheme " + mapsforgeTheme, e);
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        }

        if (databaseRenderers == null) {
            databaseRenderers = new DatabaseRenderer[PROCESSOR_COUNT];
        } else {
            for (int i = 0; i < PROCESSOR_COUNT; i++) {
                databaseRenderers[i] = null;
            }
        }

        renderThemeFuture = new RenderThemeFuture(this.getGraphicFactory(displayModel.getScaleFactor()), renderTheme, this.displayModel);
        new Thread(this.renderThemeFuture).start();

        alreadySet = true;
    }

    protected TileGL getMapsforgeTileGL_Bmp(Layer layer, Descriptor desc, int ThreadIndex) {
        // check initialization
        if ((mapDatabase == null)) {
            initMapDatabase(layer);
        }
        // TileBasedLabelStore labelStore = null;
        if (databaseRenderers[ThreadIndex] == null) {
            databaseRenderers[ThreadIndex] = new DatabaseRenderer(mapDatabase[ThreadIndex], getGraphicFactory(displayModel.getScaleFactor()), firstLevelTileCache, null, true, true);
        }
        if (databaseRenderers[ThreadIndex] == null)
            return null;
        // create bitmap from tile-definition
        try {
            Tile tile = new Tile(desc.getX(), desc.getY(), (byte) desc.getZoom(), 256);
            RendererJob rendererJob = new RendererJob(tile, mapDatabase[ThreadIndex], renderThemeFuture, displayModel, textScale, false, false);
            TileBitmap bitmap = databaseRenderers[ThreadIndex].executeJob(rendererJob);
            /*
              // direct Buffer swap
              If the goal is to convert an Android Bitmap to a libgdx Texture, you don't need to use Pixmap at all. You can do it directly with
              the help of simple OpenGL and Android GLUtils. Try the followings; it is 100x faster than your solution. I assume that you are
              not in the rendering thread (you should not most likely). If you are, you don't need to call postRunnable().
              Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Format.RGBA8888);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                    bitmap.recycle(); // now you have the texture to do whatever you want
                }
              });
            */
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(baos);
                byte[] b = baos.toByteArray();
                TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileGL.TileState.Present, Pixmap.Format.RGB565);
                ((ext_Bitmap) bitmap).recycle();
                return bmpTile;
            } catch (Exception e) {
                Log.err(log, "convert mapsfore tile to bmpTile: " + e.toString(), e);
                return null;
            }
        } catch (Exception e) {
            Log.err(log, "get mapsfore tile: " + e.toString(), e);
            return null;
        }
    }

    public abstract GraphicFactory getGraphicFactory(float ScaleFactor);

    private void initMapDatabase(Layer layer) {

        try {
            MapFile mapforgeMapFile = getMapFile(layer);
            ArrayList<MapFile> additionalMapFiles = null;
            if (layer.hasAdditionalMaps()) {
                additionalMapFiles = new ArrayList<>();
                for (Layer addLayer : layer.getAdditionalMaps()) {
                    additionalMapFiles.add(getMapFile(addLayer));
                }
            }

            if (mapDatabase == null)
                mapDatabase = new MultiMapDataStore[PROCESSOR_COUNT];

            for (int i = 0; i < PROCESSOR_COUNT; i++) {
                if (mapDatabase[i] == null) {
                    mapDatabase[i] = new MultiMapDataStore(DataPolicy.DEDUPLICATE); // or DataPolicy.RETURN_FIRST
                } else {
                    mapDatabase[i].clearMapDataStore();
                }

                mapDatabase[i].addMapDataStore(mapforgeMapFile, false, false);
                if (layer.hasAdditionalMaps()) {
                    assert additionalMapFiles != null;
                    for (MapFile mf : additionalMapFiles) {
                        mapDatabase[i].addMapDataStore(mf, false, false);
                    }
                }
            }

            Log.debug(log, "Open MapsForge Map: " + layer.Name);
        } catch (Exception e) {
            Log.err(log, "ERROR with Open MapsForge Map: " + layer.Name, e);
        }
    }

    private MapFile getMapFile(Layer layer) {
        MapFile mapforgeMapFile = null;
        File mapFile = FileFactory.createFile(layer.Url);
        java.io.File file = new java.io.File(mapFile.getAbsolutePath());
        // todo discuss alternate preferred language per layer (saved in layer at selection) ,....
        if (layer.languages == null) {
            mapforgeMapFile = new MapFile(file);
        } else {
            String preferredLanguage = LocatorSettings.PreferredMapLanguage.getValue();
            if (preferredLanguage.length() > 0) {
                for (String la : layer.languages) {
                    if (la.equals(preferredLanguage)) {
                        mapforgeMapFile = new MapFile(file, preferredLanguage);
                        break;
                    }
                }
            }
            if (mapforgeMapFile == null) {
                if (layer.languages.length > 0)
                    mapforgeMapFile = new MapFile(file, layer.languages[0]);
                else
                    mapforgeMapFile = new MapFile(file);
            }
        }
        return mapforgeMapFile;
    }

    protected abstract TileGL getTileGL(Layer layer, Descriptor desc, int ThreadIndex);

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    public DisplayModel getDisplayModel() {
        return displayModel;
    }

    public void setDisplayModel(DisplayModel displayModel) {
        this.displayModel = displayModel;
    }

    public class ImageData {
        public int[] PixelColorArray;
        public int width;
        public int height;
    }

    private class Xml_RenderThemeMenuCallback implements XmlRenderThemeMenuCallback {
        @Override
        public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
            String ConfigStyle = mapsforgeThemesStyle;
            int StyleEnds = mapsforgeThemesStyle.indexOf("\t");
            String Style;
            if (StyleEnds > -1) {
                Style = mapsforgeThemesStyle.substring(0, StyleEnds);
            } else {
                Style = mapsforgeThemesStyle;
            }
            XmlRenderThemeStyleLayer selectedLayer = style.getLayer(Style);

            // now change the categories for this style
            if (selectedLayer == null) {
                return null;
            }
            Set<String> result = selectedLayer.getCategories();
            // add the categories from overlays that are enabled
            for (XmlRenderThemeStyleLayer overlay : selectedLayer.getOverlays()) {
                boolean overlayEnabled = overlay.isEnabled();
                int posInConfig = ConfigStyle.indexOf(overlay.getId());
                if (posInConfig > -1) {
                    overlayEnabled = ConfigStyle.substring(posInConfig - 1, posInConfig).equals("+");
                }
                if (overlayEnabled) {
                    result.addAll(overlay.getCategories());
                }
            }
            return result;
        }
    }
}
