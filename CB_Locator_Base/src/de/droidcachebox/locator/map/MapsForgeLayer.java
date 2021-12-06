package de.droidcachebox.locator.map;

import static de.droidcachebox.locator.LocatorMethods.getImageFromData;
import static de.droidcachebox.locator.LocatorMethods.getImageFromFile;
import static de.droidcachebox.locator.LocatorMethods.getImagePixel;
import static de.droidcachebox.locator.LocatorMethods.getMapsForgeGraphicFactory;
import static de.droidcachebox.settings.AllSettings.mapViewTextFaktor;
import static de.droidcachebox.settings.AllSettings.mapsForgeCarDayStyle;
import static de.droidcachebox.settings.AllSettings.mapsForgeCarDayTheme;
import static de.droidcachebox.settings.AllSettings.mapsForgeCarNightStyle;
import static de.droidcachebox.settings.AllSettings.mapsForgeCarNightTheme;
import static de.droidcachebox.settings.AllSettings.mapsForgeDayStyle;
import static de.droidcachebox.settings.AllSettings.mapsForgeDayTheme;
import static de.droidcachebox.settings.AllSettings.mapsForgeNightStyle;
import static de.droidcachebox.settings.AllSettings.mapsForgeNightTheme;
import static de.droidcachebox.settings.AllSettings.mapsForgeSaveZoomLevel;
import static de.droidcachebox.settings.AllSettings.nightMode;
import static de.droidcachebox.settings.AllSettings.preferredMapLanguage;

import com.badlogic.gdx.graphics.Pixmap;

import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;
import org.mapsforge.map.rendertheme.rule.RenderThemeHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

/**
 * MapsForge (Offline): getting tiles from a file in mapsforge format (one of these can be taken from Freizeitkarte)
 * you have to use theme for correct rendering
 */
public class MapsForgeLayer extends Layer {
    public static final String INTERNAL_THEME_DEFAULT = "Default";
    public static final String INTERNAL_THEME_OSMARENDER = "OsmaRender";
    public static final String INTERNAL_THEME_CAR = "Car";
    private static final float DEFAULT_TEXT_SCALE = 1;
    private static final String sClass = "MapsForgeLayer";
    public static DisplayModel displayModel;
    private static MultiMapDataStore[] multiMapDataStores;
    private static DatabaseRenderer[] databaseRenderers;
    private static RenderThemeFuture renderThemeFuture;
    private static int PROCESSOR_COUNT;
    private static boolean mustInitialize = true;
    private final ArrayList<MapsForgeLayer> additionalMapsForgeLayers;
    private final TileCache firstLevelTileCache; // perhaps static?
    private MapFile mapFile;
    private float textScale;
    private final String pathAndName;
    private String mapsforgeThemesStyle;
    private String mapsforgeThemeName;
    private boolean isSetRenderTheme;
    private int mDataStoreNumber;

    /**
     todo modify to input-stream + Android 11 Access
     */
    public MapsForgeLayer(String pathAndName) {
        this.pathAndName = pathAndName;
        layerUsage = LayerUsage.normal;
        if (pathAndName.startsWith("content")) {
            int lastIndex = pathAndName.lastIndexOf("%2F");
            if (lastIndex == -1) {
                name = "";
            }
            else {
                name = this.pathAndName.substring(pathAndName.lastIndexOf("%2F")+3, pathAndName.length() - 4);
            }
        }
        else {
            name = FileIO.getFileNameWithoutExtension(pathAndName);
        }
        friendlyName = name;
        url = "";
        storageType = StorageType.PNG;
        data = null;
        mapType = MapType.MAPSFORGE;

        mDataStoreNumber = -1;
        firstLevelTileCache = new InMemoryTileCache(128);
        textScale = 1;

        additionalMapsForgeLayers = new ArrayList<>();

        mapsforgeThemesStyle = "";
        mapsforgeThemeName = "";
        isSetRenderTheme = false;

        getMapsForgeGraphicFactory(); // else overwrites the device scale faktor
        float restrictedScaleFactor = 1f;
        DisplayModel.setDeviceScaleFactor(restrictedScaleFactor);
        displayModel = new DisplayModel();

        if (mustInitialize) {
            // initialize these static things only once
            mustInitialize = false;
            PROCESSOR_COUNT = 1; //Runtime.getRuntime().availableProcessors();
            multiMapDataStores = new MultiMapDataStore[PROCESSOR_COUNT];
            databaseRenderers = new DatabaseRenderer[PROCESSOR_COUNT];
            for (int i = 0; i < PROCESSOR_COUNT; i++)
                multiMapDataStores[i] = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE); // or DataPolicy.RETURN_FIRST
        }

    }

    public MapFile getMapFile() {
        if (mapFile == null) prepareMapFile();
        return mapFile;
    }

    @Override
    public void addAdditionalMap(Layer layer) {
        MapsForgeLayer additionalMapsForgeLayer = (MapsForgeLayer) layer;
        if (!additionalMapsForgeLayers.contains(additionalMapsForgeLayer)) {
            additionalMapsForgeLayers.add(additionalMapsForgeLayer);
            for (MultiMapDataStore mmds : multiMapDataStores) {
                mmds.addMapDataStore(additionalMapsForgeLayer.getMapFile(), false, false);
            }
        }
    }

    @Override
    public void clearAdditionalMaps() {
        for (MapsForgeLayer additionalMapsForgeLayer : additionalMapsForgeLayers) {
            additionalMapsForgeLayer.getMapFile().close();
        }
        for (MultiMapDataStore mmds : multiMapDataStores) {
            mmds = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE);
            mmds.addMapDataStore(mapFile, false, false);
        }
        additionalMapsForgeLayers.clear();
    }

    @Override
    public String[] getAllLayerNames() {
        String[] ret = new String[additionalMapsForgeLayers.size() + 1];
        ret[0] = pathAndName;
        int idx = 1;
        for (MapsForgeLayer additionalLayer : additionalMapsForgeLayers) {
            ret[idx] = additionalLayer.pathAndName;
            idx++;
        }
        return ret;
    }

    @Override
    public Layer[] getAllLayers() {
        Layer[] ret = new Layer[additionalMapsForgeLayers.size() + 1];
        ret[0] = this;
        int idx = 1;
        for (MapsForgeLayer additionalLayer : additionalMapsForgeLayers) {
            ret[idx] = additionalLayer;
            idx++;
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Layer [");
        sb.append(this.getName());
        sb.append("] additional Layer:");

        if (additionalMapsForgeLayers == null || additionalMapsForgeLayers.isEmpty()) {
            sb.append("--");
        } else {
            for (Layer addLayer : additionalMapsForgeLayers) {
                sb.append(addLayer.getName()).append(", ");
            }
        }
        return sb.toString();
    }

    public boolean prepareLayer(boolean isCarMode) {
        if (mapFile == null) {
            prepareMapFile();
        }

        if (mapFile != null) {
            MapFileInfo mapInfo = mapFile.getMapFileInfo();
            if (mapInfo.comment != null && mapInfo.comment.contains("FZK")) {
                mapType = Layer.MapType.FREIZEITKARTE;
            }

            try {
                for (int i = 0; i < PROCESSOR_COUNT; i++) {
                    // Log.info(log, "multiMapDataStores[" + i + "].addMapDataStore: " + getName() + ": " + mapFile.getMapFileInfo().comment);
                    multiMapDataStores[i] = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE); //was  multiMapDataStores[i].clearMapDataStore();
                    multiMapDataStores[i].addMapDataStore(mapFile, false, false);
                    for (MapsForgeLayer mapsforgeLayer : additionalMapsForgeLayers) {
                        multiMapDataStores[i].addMapDataStore(mapsforgeLayer.mapFile, false, false);
                    }
                    // last parameter of new DatabaseRenderer HillsRenderConfig hillsRenderConfig = null; // new HillsRenderConfig(....);
                    databaseRenderers[i] = new DatabaseRenderer(multiMapDataStores[i], getMapsForgeGraphicFactory(), firstLevelTileCache, null, true, true, null);
                }
                String additional = mapFile.getMapFileInfo().comment == null ? "" : " : " + mapFile.getMapFileInfo().comment;
                Log.info(sClass, "prepareLayer " + getName() + additional);
            } catch (Exception e) {
                Log.err(sClass, "ERROR with Open MapsForge Map: " + getName(), e);
            }
            initTheme(isCarMode);
            return true;
        }
        return false;
    }

    @Override
    TileGL getTileGL(Descriptor desc) {
        // hint: if returns null, cacheTileToFile(Descriptor descriptor) is called, but we already cache the tile to file here.
        String cachedTileFilename = getLocalFilename(desc); // to do perhaps extend name with language, theme, style
        if (desc.getZoom() <= mapsForgeSaveZoomLevel.getValue()) {
            try {
                long lastModified = 0;
                if (FileIO.fileExists(cachedTileFilename)) {
                    AbstractFile info = FileFactory.createFile(cachedTileFilename);
                    lastModified = info.lastModified(); // must compare to date of .map-file (if replaced with newer one)
                    if (lastModified < mapFile.getDataTimestamp(null)) lastModified = 0; // there is a newer mapfile
                }

                if (lastModified != 0) {
                    if (FileIO.fileExistsNotEmpty(cachedTileFilename)) {
                        byte[] bytes = getImageFromFile(cachedTileFilename);
                        if (nightMode.getValue()) {
                            bytes = getImageFromData(getImageDataWithColorMatrixManipulation(getImagePixel(bytes)));
                        }
                        return new TileGL_Bmp(desc, bytes, TileGL.TileState.Present, Pixmap.Format.RGB565);
                    } else {
                        FileFactory.createFile(cachedTileFilename).delete();
                    }
                }

            } catch (Exception ex) {
                Log.err(sClass, "getTileGL", ex);
            }
        }
        // create bitmap from tile-definition
        try {
            // Log.info(log, "MF step 1: " + desc);
            Tile tile = new Tile(desc.getX(), desc.getY(), (byte) desc.getZoom(), 256);
            mDataStoreNumber = (mDataStoreNumber + 1) % PROCESSOR_COUNT;
            RendererJob rendererJob = new RendererJob(tile, multiMapDataStores[mDataStoreNumber], renderThemeFuture, displayModel, textScale, false, false);
            TileBitmap bitmap = databaseRenderers[mDataStoreNumber].executeJob(rendererJob);
            if (bitmap == null) {
                Log.err(sClass, "MF step 2: " + desc);
                return null;
            } else {
                TileGL_Bmp mfTile = new TileGL_Bmp(desc, bitmap, TileGL.TileState.Present, Pixmap.Format.RGB565);
                if (desc.getZoom() <= mapsForgeSaveZoomLevel.getValue()) {
                    // cache to file here
                    byte[]  bytesOfMfTile = mfTile.getBytes();
                    if (bytesOfMfTile != null) {
                        AbstractFile outAbstractFile = null;
                        try {
                            outAbstractFile = FileFactory.createFile(cachedTileFilename);
                            outAbstractFile.delete();
                            outAbstractFile.getParentFile().mkdirs();
                            outAbstractFile.createNewFile();
                            FileOutputStream stream = outAbstractFile.getFileOutputStream();
                            stream.write(bytesOfMfTile);
                            stream.close(); // There is no more need for this line since you had created the instance of "stream" inside the try. And this will automatically close the OutputStream
                            // Log.debug(log, "cached " + outAbstractFile.getName());
                            outAbstractFile.setLastModified(mapFile.getDataTimestamp(null));
                        }
                        catch (Exception ex) {
                            Log.err(sClass,"bad write to disk ", ex);
                            try {
                                if (outAbstractFile != null) outAbstractFile.delete();
                            } catch (IOException e) {
                                Log.err(sClass,"delete File after bad write to disk ", e);
                            }
                        }
                    }
                }
                return mfTile;
            }
        } catch (Exception ex) {
            Log.err(sClass, "get mapsfore tile: ", ex);
            return null;
        }
    }

    private void prepareMapFile() {
        AbstractFile file = FileFactory.createFile(pathAndName);
        if (file.exists() && file.isFile()) {
            try {
                mapFile = new MapFile(file.getFileInputStream());
                languages = mapFile.getMapLanguages();
                if (languages != null) {
                    String preferredLanguage = preferredMapLanguage.getValue();
                    if (preferredLanguage.length() > 0) {
                        for (String la : languages) {
                            if (la.equals(preferredLanguage)) {
                                mapFile = new MapFile(file.getFileInputStream(), preferredLanguage);
                                break;
                            }
                        }
                    }
                }
            }
            catch (Exception ex) {
                Log.err(sClass, pathAndName, ex);
                mapFile = null;
            }
        }
    }

    boolean cacheTileToFile(Descriptor descriptor) {
        // don't want to cache for mapsforge or cache after generation
        return false;
    }

    private void setRenderTheme(String themeName, String themeStyleName) {
        if (isSetRenderTheme)
            if (themeName.equals(mapsforgeThemeName))
                if (themeStyleName.equals(mapsforgeThemesStyle))
                    return;
        mapsforgeThemesStyle = themeStyleName;
        mapsforgeThemeName = themeName;
        XmlRenderTheme renderTheme;
        if (mapsforgeThemeName.length() == 0) {
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        } else if (mapsforgeThemeName.equals(INTERNAL_THEME_OSMARENDER)) {
            renderTheme = CB_InternalRenderTheme.OSMARENDER;
        } else if (mapsforgeThemeName.equals(INTERNAL_THEME_CAR)) {
            renderTheme = CB_InternalRenderTheme.CAR;
        } else if (mapsforgeThemeName.equals(INTERNAL_THEME_DEFAULT)) {
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        } else {
            try {
                AbstractFile abstractFile = FileFactory.createFile(mapsforgeThemeName);
                if (abstractFile.exists()) {
                    java.io.File themeFile = new java.io.File(abstractFile.getAbsolutePath());
                    renderTheme = new ExternalRenderTheme(themeFile, new Xml_RenderThemeMenuCallback());
                } else {
                    Log.err(sClass, mapsforgeThemeName + " not found!");
                    renderTheme = CB_InternalRenderTheme.DEFAULT;
                }
            } catch (Exception e) {
                Log.err(sClass, "Load RenderTheme", "Error loading RenderTheme!", e);
                renderTheme = CB_InternalRenderTheme.DEFAULT;
            }
        }

        try {
            RenderThemeHandler.getRenderTheme(getMapsForgeGraphicFactory(), displayModel, renderTheme);
        } catch (Exception e) {
            Log.err(sClass, "Error in checking RenderTheme " + mapsforgeThemeName, e);
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        }

        // renderThemeFuture = new RenderThemeFuture(getGraphicFactory(displayModel.getScaleFactor()), renderTheme, displayModel);
        renderThemeFuture = new RenderThemeFuture(getMapsForgeGraphicFactory(), renderTheme, displayModel);

        new Thread(renderThemeFuture).start();

        isSetRenderTheme = true;
    }

    private void initTheme(boolean carMode) {
        String themeStyleName;
        String themeName;
        String path;
        if (carMode) {
            textScale = DEFAULT_TEXT_SCALE * 1.35f;
            if (nightMode.getValue()) {
                themeStyleName = mapsForgeCarNightStyle.getValue();
                path = mapsForgeCarNightTheme.getValue();
            } else {
                themeStyleName = mapsForgeCarDayStyle.getValue();
                path = mapsForgeCarDayTheme.getValue();
            }
        } else {
            textScale = DEFAULT_TEXT_SCALE * mapViewTextFaktor.getValue();
            if (nightMode.getValue()) {
                themeStyleName = mapsForgeNightStyle.getValue();
                path = mapsForgeNightTheme.getValue();
            } else {
                themeStyleName = mapsForgeDayStyle.getValue();
                path = mapsForgeDayTheme.getValue();
            }
        }
        if (path.length() > 0) {
            if (path.equals(INTERNAL_THEME_CAR) || path.equals(INTERNAL_THEME_DEFAULT) || path.equals(INTERNAL_THEME_OSMARENDER)) {
                themeName = path;
            } else if (FileIO.fileExists(path) && FileIO.getFileExtension(path).contains("xml")) {
                themeName = path;
            } else
                themeName = "";
        } else
            themeName = "";
        setRenderTheme(themeName, themeStyleName);
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
                    overlayEnabled = ConfigStyle.charAt(posInConfig - 1) == '+';
                }
                if (overlayEnabled) {
                    result.addAll(overlay.getCategories());
                }
            }
            return result;
        }
    }

}
