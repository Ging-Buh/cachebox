package CB_Locator.Map;

import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.HSV_Color;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.http.Download;
import CB_Utils.http.Webb;
import CB_Utils.http.WebbUtils;
import com.badlogic.gdx.graphics.Pixmap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static CB_UI_Base.Events.PlatformUIBase.*;

public class Layer {
    private static final String log = "Layer";
    protected String name = "";
    protected String url;
    protected Object data;
    MapType mapType;
    StorageType storageType;
    String friendlyName = "";
    String[] languages;
    LayerUsage mLayerUsage;

    public Layer() {
    }

    public Layer(MapType mapType, LayerUsage LayerUsage, StorageType storageType, String name, String friendlyName, String url) {
        this.mapType = mapType;
        this.name = name;
        this.friendlyName = friendlyName;
        this.url = url;
        this.mLayerUsage = LayerUsage;
        this.storageType = storageType;
        data = null;
    }

    boolean downloadTile(Descriptor desc) {
        return Download.download(getUrl(desc), getLocalFilename(desc));
    }

    public String getUrl(Descriptor desc) {
        if (desc == null)
            return null;
        String lUrl = url;
        if (lUrl.contains("{z}")) {
            int max = 0;
            if (lUrl.contains("{1}")) {
                max = 1;
            }
            if (lUrl.contains("{2}")) {
                max = 2;
            }
            if (lUrl.contains("{3}")) {
                max = 3;
            }
            // int randomNum = ThreadLocalRandom.current().nextInt(0, max + 1);
            int randomNum = (int) (Math.random() * max);
            lUrl = lUrl.replace("{" + max + "}", "" + randomNum);
            return lUrl.replace("{x}", "" + desc.getX()).replace("{y}", "" + desc.getY()).replace("{z}", "" + desc.getZoom());
        } else
            return url + desc.getZoom() + "/" + desc.getX() + "/" + desc.getY() + this.storageType.extension; // now obsolete
    }

    String getLocalFilename(Descriptor desc) {
        if (desc == null)
            return null;
        return desc.getLocalCachePath(name) + this.storageType.extension;
    }

    public boolean isOverlay() {
        return mLayerUsage == LayerUsage.overlay;
    }

    public boolean isMapsForge() {
        return this.mapType == MapType.FREIZEITKARTE || this.mapType == MapType.MAPSFORGE;
    }

    public MapType getMapType() {
        return this.mapType;
    }

    public void addAdditionalMap(Layer layer) {
        throw new RuntimeException("Can't add this Layer");
    }

    public void clearAdditionalMaps() {
    }

    public boolean hasAdditionalMaps() {
        return false;
    }

    public String[] getAllLayerNames() {
        String[] ret = new String[1];
        ret[0] = name;
        return ret;
    }

    @Override
    public String toString() {
        return "Layer [" + name + "]";
    }

    public String getName() {
        return name;
    }

    public String[] getLanguages() {
        return languages;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    TileGL getTileGL(Descriptor desc, int ThreadIndex) {
        Pixmap.Format format = isOverlay() ? Pixmap.Format.RGBA4444 : Pixmap.Format.RGB565;
        try {
            String cachedTileFilename = getLocalFilename(desc);
            long cachedTileAge = 0;
            if (FileIO.fileExists(cachedTileFilename)) {
                File info = FileFactory.createFile(cachedTileFilename);
                cachedTileAge = info.lastModified();
            }

            if (getMapType() == MapType.MapPack) {
                MapPackLayer mapPack = (MapPackLayer) this;
                if (mapPack.maxAge >= cachedTileAge) {
                    BoundingBox bbox = mapPack.contains(desc);
                    if (bbox != null) {
                        byte[] b = mapPack.LoadFromBoundingBoxByteArray(bbox, desc);
                        if (CB_UI_Base_Settings.nightMode.getValue()) {
                            b = getImageFromData(getImageDataWithColorMatrixManipulation(getImagePixel(b)));
                        }
                        return new TileGL_Bmp(desc, b, TileGL.TileState.Present, format);
                    }
                }
            }

            if (cachedTileAge != 0) {
                if (FileIO.fileExistsNotEmpty(cachedTileFilename)) {
                    byte[] b = getImageFromFile(cachedTileFilename);
                    if (CB_UI_Base_Settings.nightMode.getValue()) {
                        b = getImageFromData(getImageDataWithColorMatrixManipulation(getImagePixel(b)));
                    }
                    return new TileGL_Bmp(desc, b, TileGL.TileState.Present, format);
                }
                else {
                    FileFactory.createFile(cachedTileFilename).delete();
                }
            }

        } catch (Exception ex) {
            Log.err(log, "getTileGL", ex);
        }
        return null;
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
     * @return ImageData
     */
    private ImageData getImageDataWithColorMatrixManipulation(ImageData imgData) {

        int[] data = imgData.PixelColorArray;
        for (int i = 0; i < data.length; i++) {
            data[i] = HSV_Color.colorMatrixManipulation(data[i], HSV_Color.NIGHT_COLOR_MATRIX);
        }
        return imgData;
    }


    /**
     * Load Tile from URL and save to MapTile-Cache
     *
     * @param descriptor Descriptor
     * @return boolean
     */
    boolean cacheTile(Descriptor descriptor) {

        if (isMapsForge()) return false;

        // get mapPack from layer and check, if tile is covered (can be generated) from mapPack then simply return true
        if (getMapType() == MapType.MapPack) {
            MapPackLayer pack = (MapPackLayer) data;
            if (pack.contains(descriptor) != null)
                return true;
        }

        // (the online layers, and MapPack contains a url) Download from url into cache (also from url for mapPack, if tile not inside)
        String filename = getLocalFilename(descriptor);
        String url = getUrl(descriptor);
        if (!url.startsWith("http")) {
            return false;
        }

        // Falls Kachel schon geladen wurde, kann sie übersprungen werden
        synchronized (this) {
            if (FileIO.fileExistsNotEmpty(filename))
                return true;
            else {
                try {
                    FileFactory.createFile(filename).delete();
                    if (!FileIO.createDirectory(filename))
                        return false;
                    // todo redirect from http to https and vice-versa (auto does not work in these cases)
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
                    Log.err(log, "Download from url (into cache) " + url, ex);
                    try {
                        FileFactory.createFile(filename).delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            }
        }
    }

    public enum MapType {
        ONLINE, MAPSFORGE, FREIZEITKARTE, MapPack
    }

    public enum LayerUsage {
        normal, overlay
    }

    public enum StorageType {
        PNG(".png"), JPG(".jpg");
        private final String extension;

        StorageType(String extension) {
            this.extension = extension;
        }
    }

}
