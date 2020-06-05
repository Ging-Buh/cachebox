/*
 * Copyright (C) 2009 - 2010 getcachebox.net
 *
 * Copyright (C) 2011 - 2014 team-cachebox.de
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

import com.badlogic.gdx.utils.Array;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.LocatorSettings;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.PointD;

/**
 * has x,y,zoom for defining a tile
 * also a hashCode for quick identification (calculated in getter on the fly)
 */
public class Descriptor implements Comparable<Descriptor> {
    private static String tileCacheFolder;
    private static final IChanged tileCacheFolderSettingChanged = new IChanged() {
        @Override
        public void handleChange() {
            tileCacheFolder = LocatorSettings.tileCacheFolder.getValue();
            if (LocatorSettings.tileCacheFolderLocal.getValue().length() > 0)
                tileCacheFolder = LocatorSettings.tileCacheFolderLocal.getValue();
        }
    };
    private static int maxZoom = 25;
    private static int[] tileOffset = new int[maxZoom];
    private static int[] tilesPerLine = new int[maxZoom];
    private static int[] tilesPerColumn = new int[maxZoom];

    static {

        tileOffset[0] = 0;

        for (int i = 0; i < maxZoom - 1; i++) {
            tilesPerLine[i] = (int) (2 * Math.pow(2, i));
            tilesPerColumn[i] = (int) Math.pow(2, i);
            tileOffset[i + 1] = tileOffset[i] + (tilesPerLine[i] * tilesPerColumn[i]);
        }

        tileCacheFolder = LocatorSettings.tileCacheFolder.getValue();
        if (LocatorSettings.tileCacheFolderLocal.getValue().length() > 0)
            tileCacheFolder = LocatorSettings.tileCacheFolderLocal.getValue();

        LocatorSettings.tileCacheFolderLocal.addSettingChangedListener(tileCacheFolderSettingChanged);
        LocatorSettings.tileCacheFolder.addSettingChangedListener(tileCacheFolderSettingChanged);
    }

    private Object data = null;
    private int x;
    private int y;
    private int zoom;
    private long hashCode;

    /**
     * Erzeugt einen neuen Descriptor mit den übergebenen Parametern
     *
     * @param x    X-Koordinate der Kachel
     * @param y    Y-Koordinate der Kachel
     * @param zoom Zoom-Stufe
     */
    public Descriptor(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        hashCode = 0;
    }

    /**
     * Constructor for given coordinate and zoom-level
     *
     * @param coordinate  ?
     * @param zoom ?
     */
    public Descriptor(Coordinate coordinate, int zoom) {
        x = (int) longitudeToTileX(zoom, coordinate.getLongitude());
        y = (int) latitudeToTileY(zoom, coordinate.getLatitude());
        this.zoom = zoom;
        hashCode = 0;
    }

    public Descriptor() {
        x = 0;
        y = 0;
        zoom = 0;
        hashCode = 0;
    }

    public Descriptor(long hashCode, int zoom) {
        // x = hashCode - (tileOffset[zoom]) - (long) (tilesPerLine[zoom]) * y;
    }

    /**
     * Projiziert die übergebene Koordinate in den Tile Space
     *
     * @param latitude       Breitengrad
     * @param longitude      Längengrad
     * @param projectionZoom zoom
     * @return PointD
     */
    public static PointD projectCoordinate(double latitude, double longitude, int projectionZoom) {
        return new PointD(longitudeToTileX(projectionZoom, longitude), latitudeToTileY(projectionZoom, latitude));
    }

    /**
     * Berechnet aus dem übergebenen Längengrad die X-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
     *
     * @param zoom      Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
     * @param longitude Longitude
     * @return double
     */
    public static double longitudeToTileX(double zoom, double longitude) {
        return (longitude + 180.0) / 360.0 * Math.pow(2, zoom);

    }

    /**
     * Berechnet aus dem übergebenen Breitengrad die Y-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
     *
     * @param zoom     Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
     * @param latitude Latitude
     * @return double
     */
    public static double latitudeToTileY(double zoom, double latitude) {
        double latRad = latitude * MathUtils.DEG_RAD;
        return (1 - Math.log(Math.tan(latRad) + (1.0 / Math.cos(latRad))) / Math.PI) / 2 * Math.pow(2, zoom);
    }

    /*
    public static double latitudeToTileY(byte zoom, double latitude, int tileSize) {
        double sinLatitude = Math.sin(latitude * (Math.PI / 180));
        long mapSize = tileSize << zoom;
        // FIX ME improve this formula so that it works correctly without the clipping
        double pixelY = (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) * mapSize;
        return Math.min(Math.max(0, pixelY), mapSize);
    }
     */

    /**
     * Berechnet aus der übergebenen OSM-X-Koordinate den entsprechenden Längengrad
     */
    public static double tileXToLongitude(double zoom, double x) {
        return -180.0 + (360.0 * x) / Math.pow(2, zoom);
    }

    /**
     * Berechnet aus der übergebenen OSM-Y-Koordinate den entsprechenden Breitengrad
     */
    public static double tileYToLatitude(double zoom, double y) {
        double exp = Math.exp(4 * Math.PI * Math.pow(2, -zoom) * y);
        double xNom = Math.exp(2 * Math.PI) - exp;
        double xDen = Math.exp(2 * Math.PI) + exp;

        double v = -1 + Math.pow(2, 1 - zoom) * y;
        double yNom = 2 * Math.exp(-Math.PI * v);
        double yDen = Math.exp(-2 * Math.PI * v) + 1;

        return Math.atan2(xNom / xDen, yNom / yDen) * MathUtils.RAD_DEG;
    }

    public static PointD toWorld(double X, double Y, int zoom, int desiredZoom) {
        double adjust = Math.pow(2, (desiredZoom - zoom));
        return new PointD(X * adjust * 256, Y * adjust * 256);
    }

    public static PointD fromWorld(double X, double Y, int zoom, int desiredZoom) {
        double adjust = Math.pow(2, (desiredZoom - zoom));
        return new PointD(X / (adjust * 256), Y / (adjust * 256));
    }

    public static String getTileCacheFolder() {
        return tileCacheFolder;
    }

    /**
     * Erzeugt einen neuen Deskriptor mit anderer Zoom-Stufe
     */
    public Array<Descriptor> adjustZoom(int newZoomLevel) {
        int zoomDiff = newZoomLevel - getZoom();
        int pow = (int) Math.pow(2, Math.abs(zoomDiff));

        Array<Descriptor> ret = new Array<>();

        if (zoomDiff > 0) {

            Descriptor def = new Descriptor(getX() * pow, getY() * pow, newZoomLevel);

            int count = pow / 2;

            for (int i = 0; i <= count; i++) {
                for (int j = 0; j <= count; j++) {
                    ret.add(new Descriptor(def.getX() + i, def.getY() + j, newZoomLevel));
                }
            }

        } else {
            ret.add(new Descriptor(getX() / pow, getY() / pow, newZoomLevel));
        }

        return ret;

    }

    /*
     * Berechnet die Pixel-Koordinaten auf dem Bildschirm. Es wird auf die Kachelecke oben links noch ein Offset addiert. Will man also die
     * Koordinaten der Ecke unten links haben, übergibt man xOffset=0,yOffset=1
     *
     * @param xOffset ?
     * @param yOffset ?
     * @param desiredZoom ?
     * @return PointD
    public PointD toWorld(int xOffset, int yOffset, int desiredZoom) {
        double adjust = Math.pow(2, (desiredZoom - getZoom()));
        return new PointD((getX() + xOffset) * adjust * 256, (getY() + yOffset) * adjust * 256);
    }
     */

    public long getHashCode() {
        if (hashCode != 0)
            return hashCode;
        hashCode = ((tileOffset[zoom]) + (long) (tilesPerLine[zoom]) * y + x);
        return hashCode;
    }

    public String toString() {
        return "X = " + x + ", Y = " + y + ", Zoom = " + zoom;
    }

    @Override
    public int compareTo(Descriptor another) {
        return Long.compare(getHashCode(), another.getHashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Descriptor) {
            return getHashCode() == ((Descriptor) obj).getHashCode();
        }
        return false;
    }

    public void dispose() {
        data = null;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        hashCode = 0; // Hash must new calculated
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        hashCode = 0; // Hash must new calculated
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        hashCode = 0; // Hash must new calculated
    }

    public void set(int x2, int y2, int zoom2) {
        x = x2;
        y = y2;
        zoom = zoom2;
        hashCode = 0;

    }

    public void set(Descriptor descriptor) {
        x = descriptor.x;
        y = descriptor.y;
        zoom = descriptor.zoom;
        hashCode = 0;
    }

    /**
     * Return the center coordinate of this Descriptor
     *
     * @return ?
     */
    public Coordinate getCenterCoordinate() {
        double lon = tileXToLongitude(zoom, x);
        double lat = tileYToLatitude(zoom, y);

        double lon1 = tileXToLongitude(zoom, x + 1);
        double lat1 = tileYToLatitude(zoom, y + 1);

        double divLon = (lon1 - lon) / 2;
        double divLat = (lat1 - lat) / 2;

        return new Coordinate(lat + divLat, lon + divLon);
    }

    /**
     * Returns the local Cache Path for the given Name and this Descriptor!<br>
     * .\cachebox\repository\cache\ {NAME} \ {Zoom} \ {X} \ {Y}
     *
     * @param Name ?
     * @return ?
     */
    public String getLocalCachePath(String Name) {
        return tileCacheFolder + "/" + Name + "/" + zoom + "/" + x + "/" + y;
    }

    /**
     * Returns the distance to the given Descriptor!
     *
     * @param desc ?
     * @return ?
     */
    public int getDistance(Descriptor desc) {
        int xDistance = Math.abs(desc.x - x);
        int yDistance = Math.abs(desc.y - y);
        return Math.max(xDistance, yDistance);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
