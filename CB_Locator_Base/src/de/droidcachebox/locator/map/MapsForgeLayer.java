package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.Pixmap;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.locator.LocatorSettings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;
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
import org.mapsforge.map.rendertheme.*;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;
import org.mapsforge.map.rendertheme.rule.RenderThemeHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static de.droidcachebox.locator.LocatorBasePlatFormMethods.*;

/**
 * MapsForge (Offline): getting tiles from a file in mapsforge format (one of these can be taken from Freizeitkarte)
 * you have to use theme for correct rendering
 */
public class MapsForgeLayer extends Layer {
    public static final String INTERNAL_THEME_DEFAULT = "Default";
    public static final String INTERNAL_THEME_OSMARENDER = "OsmaRender";
    public static final String INTERNAL_THEME_CAR = "Car";
    private static final float DEFAULT_TEXT_SCALE = 1;
    private static final String log = "MapsForgeLayer";
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
    private String pathAndName;
    private String mapsforgeThemesStyle;
    private String mapsforgeTheme;
    private boolean isSetRenderTheme;
    private int mDataStoreNumber;

    MapsForgeLayer(String pathAndName) {
        this.pathAndName = pathAndName;
        setMapFile(); // create mapFile from pathAndName
        MapFileInfo mapInfo = mapFile.getMapFileInfo();
        mapType = Layer.MapType.MAPSFORGE;
        if (mapInfo.comment != null && mapInfo.comment.contains("FZK")) {
            mapType = Layer.MapType.FREIZEITKARTE;
        }
        layerUsage = LayerUsage.normal;
        name = FileIO.getFileNameWithoutExtension(pathAndName);
        friendlyName = name;
        url = "";
        storageType = Layer.StorageType.PNG;
        data = null;
        languages = mapFile.getMapLanguages();

        mDataStoreNumber = -1;
        firstLevelTileCache = new InMemoryTileCache(128);
        textScale = 1;

        additionalMapsForgeLayers = new ArrayList<>();

        mapsforgeThemesStyle = "";
        mapsforgeTheme = "";
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

    @Override
    public void addAdditionalMap(Layer layer) {
        MapsForgeLayer additionalMapsForgeLayer = (MapsForgeLayer) layer;
        if (!additionalMapsForgeLayers.contains(additionalMapsForgeLayer)) {
            additionalMapsForgeLayers.add(additionalMapsForgeLayer);
            for (MultiMapDataStore mmds : multiMapDataStores) {
                mmds.addMapDataStore(additionalMapsForgeLayer.mapFile, false, false);
            }
        }
    }

    @Override
    public void clearAdditionalMaps() {
        for (MapsForgeLayer additionalMapsForgeLayer : additionalMapsForgeLayers) {
            additionalMapsForgeLayer.mapFile.close();
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
        ret[0] = getName();
        int idx = 1;
        for (Layer additionalLayer : additionalMapsForgeLayers) {
            ret[idx] = additionalLayer.getName();
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

    public void prepareLayer(boolean isCarMode) {
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
            Log.info(log, "prepareLayer " + getName() + additional);
        } catch (Exception e) {
            Log.err(log, "ERROR with Open MapsForge Map: " + getName(), e);
        }
        initTheme(isCarMode);
    }

    @Override
    TileGL getTileGL(Descriptor desc) {
        // hint: if returns null, cacheTileToFile(Descriptor descriptor) is called, but we already cache the tile to file here.
        String cachedTileFilename = getLocalFilename(desc); // to do perhaps extend name with language, theme, style
        if (desc.getZoom() <= LocatorSettings.mapsForgeSaveZoomLevel.getValue()) {
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
                        if (CB_UI_Base_Settings.nightMode.getValue()) {
                            bytes = getImageFromData(getImageDataWithColorMatrixManipulation(getImagePixel(bytes)));
                        }
                        return new TileGL_Bmp(desc, bytes, TileGL.TileState.Present, Pixmap.Format.RGB565);
                    } else {
                        FileFactory.createFile(cachedTileFilename).delete();
                    }
                }

            } catch (Exception ex) {
                Log.err(log, "getTileGL", ex);
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
                Log.err(log, "MF step 2: " + desc);
                return null;
            } else {
                TileGL_Bmp mfTile = new TileGL_Bmp(desc, bitmap, TileGL.TileState.Present, Pixmap.Format.RGB565);
                if (desc.getZoom() <= LocatorSettings.mapsForgeSaveZoomLevel.getValue()) {
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
                            Log.err(log,"bad write to disk ", ex);;
                            try {
                                if (outAbstractFile != null) outAbstractFile.delete();
                            } catch (IOException e) {
                                Log.err(log,"delete File after bad write to disk ", e);;
                            }
                        }
                    }
                }
                return mfTile;
            }
        } catch (Exception ex) {
            Log.err(log, "get mapsfore tile: ", ex);
            return null;
        }
    }

    private void setMapFile() {
        java.io.File file = new java.io.File(FileFactory.createFile(pathAndName).getAbsolutePath());
        if (getLanguages() == null) {
            mapFile = new MapFile(file);
        } else {
            String preferredLanguage = LocatorSettings.preferredMapLanguage.getValue();
            if (preferredLanguage.length() > 0) {
                for (String la : getLanguages()) {
                    if (la.equals(preferredLanguage)) {
                        mapFile = new MapFile(file, preferredLanguage);
                        break;
                    }
                }
            }
            if (mapFile == null) {
                if (getLanguages().length > 0)
                    mapFile = new MapFile(file, getLanguages()[0]);
                else
                    mapFile = new MapFile(file);
            }
        }
    }

    boolean cacheTileToFile(Descriptor descriptor) {
        // don't want to cache for mapsforge or cache after generation
        return false;
    }

    private void setRenderTheme(String theme, String themestyle) {
        if (isSetRenderTheme)
            if (theme.equals(mapsforgeTheme))
                if (themestyle.equals(mapsforgeThemesStyle))
                    return;
        mapsforgeThemesStyle = themestyle;
        mapsforgeTheme = theme;
        XmlRenderTheme renderTheme;
        if (mapsforgeTheme.length() == 0) {
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        } else if (mapsforgeTheme.equals(INTERNAL_THEME_OSMARENDER)) {
            renderTheme = CB_InternalRenderTheme.OSMARENDER;
        } else if (mapsforgeTheme.equals(INTERNAL_THEME_CAR)) {
            renderTheme = CB_InternalRenderTheme.CAR;
        } else if (mapsforgeTheme.equals(INTERNAL_THEME_DEFAULT)) {
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        } else {
            try {
                AbstractFile abstractFile = FileFactory.createFile(mapsforgeTheme);
                if (abstractFile.exists()) {
                    java.io.File themeFile = new java.io.File(abstractFile.getAbsolutePath());
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
            RenderThemeHandler.getRenderTheme(getMapsForgeGraphicFactory(), displayModel, renderTheme);
        } catch (Exception e) {
            Log.err(log, "Error in checking RenderTheme " + mapsforgeTheme, e);
            renderTheme = CB_InternalRenderTheme.DEFAULT;
        }

        // renderThemeFuture = new RenderThemeFuture(getGraphicFactory(displayModel.getScaleFactor()), renderTheme, displayModel);
        renderThemeFuture = new RenderThemeFuture(getMapsForgeGraphicFactory(), renderTheme, displayModel);

        new Thread(renderThemeFuture).start();

        isSetRenderTheme = true;
    }

    private void initTheme(boolean carMode) {
        String themeStyle;
        String theme;
        String path;
        if (carMode) {
            textScale = DEFAULT_TEXT_SCALE * 1.35f;
            if (CB_UI_Base_Settings.nightMode.getValue()) {
                themeStyle = LocatorSettings.mapsForgeCarNightStyle.getValue();
                path = LocatorSettings.mapsForgeCarNightTheme.getValue();
            } else {
                themeStyle = LocatorSettings.mapsForgeCarDayStyle.getValue();
                path = LocatorSettings.mapsForgeCarDayTheme.getValue();
            }
        } else {
            textScale = DEFAULT_TEXT_SCALE * CB_UI_Base_Settings.mapViewTextFaktor.getValue();
            if (CB_UI_Base_Settings.nightMode.getValue()) {
                themeStyle = LocatorSettings.mapsForgeNightStyle.getValue();
                path = LocatorSettings.mapsForgeNightTheme.getValue();
            } else {
                themeStyle = LocatorSettings.mapsForgeDayStyle.getValue();
                path = LocatorSettings.mapsForgeDayTheme.getValue();
            }
        }
        if (path.length() > 0) {
            if (path.equals(INTERNAL_THEME_CAR) || path.equals(INTERNAL_THEME_DEFAULT) || path.equals(INTERNAL_THEME_OSMARENDER)) {
                theme = path;
            } else if (FileIO.fileExists(path) && FileIO.getFileExtension(path).contains("xml")) {
                theme = path;
            } else
                theme = "";
        } else
            theme = "";
        setRenderTheme(theme, themeStyle);
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
