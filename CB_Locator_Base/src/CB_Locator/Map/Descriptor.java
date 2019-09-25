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

package CB_Locator.Map;

import CB_Locator.Coordinate;
import CB_Locator.LocatorSettings;
import CB_Utils.Lists.CB_List;
import CB_Utils.Math.PointD;
import CB_Utils.MathUtils;
import CB_Utils.Util.IChanged;

/**
 * @author hwinkelmann
 * @author ersthelfer
 * @author ging-buh
 * @author Longri
 */
public class Descriptor implements Comparable<Descriptor> {
    public final static double PI_180 = (Math.PI / 180);
    public final static double PI_4 = (Math.PI * 4);
    public static String TileCacheFolder;
    private static final IChanged TileCacheFolderSettingChanged = new IChanged() {
        @Override
        public void handleChange() {
            TileCacheFolder = LocatorSettings.TileCacheFolder.getValue();
            if (LocatorSettings.TileCacheFolderLocal.getValue().length() > 0)
                TileCacheFolder = LocatorSettings.TileCacheFolderLocal.getValue();
        }
    };
    static int[] tileOffset;
    private static int[] TilesPerLine;
    private static int[] TilesPerColumn;

    static {
        int maxZoom = 25;

        TilesPerLine = new int[maxZoom];
        TilesPerColumn = new int[maxZoom];
        tileOffset = new int[maxZoom];

        tileOffset[0] = 0;

        for (int i = 0; i < maxZoom - 1; i++) {
            TilesPerLine[i] = (int) (2 * Math.pow(2, i));
            TilesPerColumn[i] = (int) Math.pow(2, i);
            tileOffset[i + 1] = tileOffset[i] + (TilesPerLine[i] * TilesPerColumn[i]);
        }

        TileCacheFolder = LocatorSettings.TileCacheFolder.getValue();
        if (LocatorSettings.TileCacheFolderLocal.getValue().length() > 0)
            TileCacheFolder = LocatorSettings.TileCacheFolderLocal.getValue();

        LocatorSettings.TileCacheFolderLocal.addSettingChangedListener(TileCacheFolderSettingChanged);
        LocatorSettings.TileCacheFolder.addSettingChangedListener(TileCacheFolderSettingChanged);
    }

    // zur Übergabe beliebiger Daten
    public Object Data = null;
    public boolean NightMode = false;
    /**
     * X-Koordinate der Kachel
     */
    protected int X;
    /**
     * Y-Koordinate der Kachel
     */
    protected int Y;
    /**
     * Zoom-Stufe der Kachel
     */
    protected int Zoom; // TODO muss noch auf max begrenzt Werden
    private long BuffertHash = 0;

    /**
     * Erzeugt einen neuen Deskriptor mit den übergebenen Parametern
     *
     * @param x    X-Koordinate der Kachel
     * @param y    Y-Koordinate der Kachel
     * @param zoom Zoom-Stufe
     */
    public Descriptor(int x, int y, int zoom, boolean NightMode) {
        this.X = x;
        this.Y = y;
        this.Zoom = zoom;
        this.NightMode = NightMode;
        BuffertHash = 0;
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
        this.Zoom = zoom;
        this.NightMode = false;
        BuffertHash = 0;
    }

    /**
     * Copy-Konstruktor
     *
     * @param original Zu klonende Instanz
     */
    public Descriptor(Descriptor original) {
        this.X = original.X;
        this.Y = original.Y;
        this.Zoom = original.Zoom;
        this.NightMode = original.NightMode;
        BuffertHash = 0;
    }

    public Descriptor() {
        this.X = 0;
        this.Y = 0;
        this.Zoom = 0;
        this.NightMode = false;
        BuffertHash = 0;
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

            Descriptor def = new Descriptor(getX() * pow, getY() * pow, newZoomLevel, this.NightMode);

            int count = pow / 2;

            for (int i = 0; i <= count; i++) {
                for (int j = 0; j <= count; j++) {
                    ret.add(new Descriptor(def.getX() + i, def.getY() + j, newZoomLevel, this.NightMode));
                }
            }

        } else {
            ret.add(new Descriptor(getX() / pow, getY() / pow, newZoomLevel, this.NightMode));
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

    public long GetHashCode() {
        if (BuffertHash != 0)
            return BuffertHash;
        BuffertHash = ((tileOffset[Zoom]) + (long) (TilesPerLine[Zoom]) * Y + X);
        return BuffertHash;
    }

    public String toString() {
        return "X = " + X + ", Y = " + Y + ", Zoom = " + Zoom;
    }

    @Override
    public int compareTo(Descriptor another) {
        Long hashcode = this.GetHashCode();
        Long objHashcode = another.GetHashCode();

        if (hashcode.longValue() == objHashcode.longValue())
            return 0;

        if (hashcode.longValue() < objHashcode.longValue())
            return -1;

        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Descriptor) {
            Descriptor desc = (Descriptor) obj;
            if (this.GetHashCode() == desc.GetHashCode())
                return true;
        }
        return false;
    }

    public void dispose() {
        Data = null;
    }

    public int getZoom() {
        return Zoom;
    }

    public void setZoom(int zoom) {
        Zoom = zoom;
        BuffertHash = 0; // Hash must new calculated
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
        BuffertHash = 0; // Hash must new calculated
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
        BuffertHash = 0; // Hash must new calculated
    }

    public void set(int x2, int y2, int zoom2, boolean nightMode2) {
        this.X = x2;
        this.Y = y2;
        this.Zoom = zoom2;
        this.NightMode = nightMode2;
        BuffertHash = 0; // Hash must new calculated

    }

    public void set(Descriptor descripter) {
        this.X = descripter.X;
        this.Y = descripter.Y;
        this.Zoom = descripter.Zoom;
        this.NightMode = descripter.NightMode;
        BuffertHash = 0; // Hash must new calculated
    }

    /**
     * Return the center coordinate of this Descriptor
     *
     * @return
     */
    public Coordinate getCenterCoordinate() {
        double lon = TileXToLongitude(Zoom, X);
        double lat = TileYToLatitude(Zoom, Y);

        double lon1 = TileXToLongitude(Zoom, X + 1);
        double lat1 = TileYToLatitude(Zoom, Y + 1);

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
        return TileCacheFolder + "/" + Name + "/" + Zoom + "/" + X + "/" + Y;
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
