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

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.LocatorSettings;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.PointD;

/**
 * has x,y,zoom for defining a tile
 * also a hashCode for quick identification (calculated in getter on the fly)
 */
public class Descriptor implements Comparable<Descriptor> {
    public static String TileCacheFolder;
    private static final IChanged TileCacheFolderSettingChanged = new IChanged() {
        @Override
        public void handleChange() {
            TileCacheFolder = LocatorSettings.TileCacheFolder.getValue();
            if (LocatorSettings.TileCacheFolderLocal.getValue().length() > 0)
                TileCacheFolder = LocatorSettings.TileCacheFolderLocal.getValue();
        }
    };
    private static int maxZoom = 25;
    private static int[] tileOffset = new int[maxZoom];
    private static int[] TilesPerLine = new int[maxZoom];
    private static int[] tilesPerColumn = new int[maxZoom];

    static {

        tileOffset[0] = 0;

        for (int i = 0; i < maxZoom - 1; i++) {
            TilesPerLine[i] = (int) (2 * Math.pow(2, i));
            tilesPerColumn[i] = (int) Math.pow(2, i);
            tileOffset[i + 1] = tileOffset[i] + (TilesPerLine[i] * tilesPerColumn[i]);
        }

        TileCacheFolder = LocatorSettings.TileCacheFolder.getValue();
        if (LocatorSettings.TileCacheFolderLocal.getValue().length() > 0)
            TileCacheFolder = LocatorSettings.TileCacheFolderLocal.getValue();

        LocatorSettings.TileCacheFolderLocal.addSettingChangedListener(TileCacheFolderSettingChanged);
        LocatorSettings.TileCacheFolder.addSettingChangedListener(TileCacheFolderSettingChanged);
    }

    public Object Data = null;
    public int X;
    public int Y;
    public int zoom;
    private long hashCode = 0;

    /**
     * Erzeugt einen neuen Descriptor mit den übergebenen Parametern
     *
     * @param x    X-Koordinate der Kachel
     * @param y    Y-Koordinate der Kachel
     * @param zoom Zoom-Stufe
     */
    public Descriptor(int x, int y, int zoom) {
        this.X = x;
        this.Y = y;
        this.zoom = zoom;
        hashCode = 0;
    }

    /**
     * Constructor for given coordinate and zoom-level
     *
     * @param coord
     * @param zoom
     */
    public Descriptor(Coordinate coord, int zoom) {
        this.X = (int) LongitudeToTileX(zoom, coord.getLongitude());
        this.Y = (int) LatitudeToTileY(zoom, coord.getLatitude());
        this.zoom = zoom;
        hashCode = 0;
    }

    /**
     * Copy-Konstruktor
     *
     * @param original Zu klonende Instanz
     */
    public Descriptor(Descriptor original) {
        this.X = original.X;
        this.Y = original.Y;
        this.zoom = original.zoom;
        hashCode = 0;
    }

    public Descriptor() {
        this.X = 0;
        this.Y = 0;
        this.zoom = 0;
        hashCode = 0;
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
        return new PointD(LongitudeToTileX(projectionZoom, longitude), LatitudeToTileY(projectionZoom, latitude));
    }

    /**
     * Berechnet aus dem übergebenen Längengrad die X-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
     *
     * @param zoom      Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
     * @param longitude Longitude
     * @return double
     */
    public static double LongitudeToTileX(double zoom, double longitude) {
        return (longitude + 180.0) / 360.0 * Math.pow(2, zoom);

    }

    public static double LongitudeToTileX(byte zoom, double longitude) {
        return LongitudeToTileX(zoom, longitude, 1);
    }

    public static double LongitudeToTileX(byte zoom, double longitude, int TileSize) {

        long mapSize = TileSize << zoom;
        return (longitude + 180) / 360 * mapSize;
    }

    /**
     * Berechnet aus dem übergebenen Breitengrad die Y-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
     *
     * @param zoom     Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
     * @param latitude Latitude
     * @return double
     */
    public static double LatitudeToTileY(double zoom, double latitude) {
        double latRad = latitude * MathUtils.DEG_RAD;

        return (1 - Math.log(Math.tan(latRad) + (1.0 / Math.cos(latRad))) / Math.PI) / 2 * Math.pow(2, zoom);
    }

    public static double LatitudeToTileY(byte zoom, double latitude) {
        return LatitudeToTileY(zoom, latitude, 1);
    }

    public static double LatitudeToTileY(byte zoom, double latitude, int TileSize) {
        double sinLatitude = Math.sin(latitude * (Math.PI / 180));
        long mapSize = TileSize << zoom;
        // FIXME improve this formula so that it works correctly without the clipping
        double pixelY = (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) * mapSize;
        return Math.min(Math.max(0, pixelY), mapSize);

    }

    /**
     * Berechnet aus der übergebenen OSM-X-Koordinate den entsprechenden Längengrad
     */
    public static double TileXToLongitude(double zoom, double x) {
        return -180.0 + (360.0 * x) / Math.pow(2, zoom);
    }

    /**
     * Berechnet aus der übergebenen OSM-Y-Koordinate den entsprechenden Breitengrad
     */
    public static double TileYToLatitude(double zoom, double y) {
        double xNom = Math.exp(2 * Math.PI) - Math.exp(4 * Math.PI * Math.pow(2, -zoom) * y);
        double xDen = Math.exp(2 * Math.PI) + Math.exp(4 * Math.PI * Math.pow(2, -zoom) * y);

        double yNom = 2 * Math.exp(-Math.PI * (-1 + Math.pow(2, 1 - zoom) * y));
        double yDen = Math.exp(-2 * Math.PI * (-1 + Math.pow(2, 1 - zoom) * y)) + 1;

        return Math.atan2(xNom / xDen, yNom / yDen) * MathUtils.RAD_DEG;
    }

    public static PointD ToWorld(double X, double Y, int zoom, int desiredZoom) {
        double adjust = Math.pow(2, (desiredZoom - zoom));
        return new PointD(X * adjust * 256, Y * adjust * 256);
    }

    public static PointD FromWorld(double X, double Y, int zoom, int desiredZoom) {
        double adjust = Math.pow(2, (desiredZoom - zoom));
        return new PointD(X / (adjust * 256), Y / (adjust * 256));
    }

    /**
     * Erzeugt einen neuen Deskriptor mit anderer Zoom-Stufe
     */
    public CB_List<Descriptor> AdjustZoom(int newZoomLevel) {
        int zoomDiff = newZoomLevel - getZoom();
        int pow = (int) Math.pow(2, Math.abs(zoomDiff));

        CB_List<Descriptor> ret = new CB_List<Descriptor>();

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

    /**
     * Berechnet die Pixel-Koordinaten auf dem Bildschirm. Es wird auf die Kachelecke oben links noch ein Offset addiert. Will man also die
     * Koordinaten der Ecke unten links haben, übergibt man xOffset=0,yOffset=1
     *
     * @param xOffset
     * @param yOffset
     * @param desiredZoom
     * @return PointD
     */
    public PointD ToWorld(int xOffset, int yOffset, int desiredZoom) {
        double adjust = Math.pow(2, (desiredZoom - getZoom()));
        return new PointD((getX() + xOffset) * adjust * 256, (getY() + yOffset) * adjust * 256);
    }

    public long getHashCode() {
        if (hashCode != 0)
            return hashCode;
        hashCode = ((tileOffset[zoom]) + (long) (TilesPerLine[zoom]) * Y + X);
        return hashCode;
    }

    public String toString() {
        return "X = " + X + ", Y = " + Y + ", Zoom = " + zoom;
    }

    @Override
    public int compareTo(Descriptor another) {
        Long hashcode = this.getHashCode();
        Long objHashcode = another.getHashCode();

        if (hashcode.longValue() == objHashcode.longValue())
            return 0;

        if (hashcode.longValue() < objHashcode.longValue())
            return -1;

        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Descriptor) {
            if (getHashCode() == ((Descriptor) obj).getHashCode())
                return true;
        }
        return false;
    }

    public void dispose() {
        Data = null;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        hashCode = 0; // Hash must new calculated
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
        hashCode = 0; // Hash must new calculated
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
        hashCode = 0; // Hash must new calculated
    }

    public void set(int x2, int y2, int zoom2) {
        this.X = x2;
        this.Y = y2;
        this.zoom = zoom2;
        hashCode = 0;

    }

    public void set(Descriptor descripter) {
        this.X = descripter.X;
        this.Y = descripter.Y;
        this.zoom = descripter.zoom;
        hashCode = 0;
    }

    /**
     * Return the center coordinate of this Descriptor
     *
     * @return
     */
    public Coordinate getCenterCoordinate() {
        double lon = TileXToLongitude(zoom, X);
        double lat = TileYToLatitude(zoom, Y);

        double lon1 = TileXToLongitude(zoom, X + 1);
        double lat1 = TileYToLatitude(zoom, Y + 1);

        double divLon = (lon1 - lon) / 2;
        double divLat = (lat1 - lat) / 2;

        return new Coordinate(lat + divLat, lon + divLon);
    }

    /**
     * Returns the local Cache Path for the given Name and this Descriptor!<br>
     * .\cachebox\repository\cache\ {NAME} \ {Zoom} \ {X} \ {Y}
     *
     * @param Name
     * @return
     */
    public String getLocalCachePath(String Name) {
        return TileCacheFolder + "/" + Name + "/" + zoom + "/" + X + "/" + Y;
    }

    /**
     * Returns the distance to the given Descriptor!
     *
     * @param desc
     * @return
     */
    public int getDistance(Descriptor desc) {
        int xDistance = Math.abs(desc.X - this.X);
        int yDistance = Math.abs(desc.Y - this.Y);
        return Math.max(xDistance, yDistance);
    }
}
