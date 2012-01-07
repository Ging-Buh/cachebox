/* 
 * Copyright (C) 2009 - 2010 getcachebox.net
 * 
 * Copyright (C) 2011 team-cachebox.de
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

package CB_Core.Map;

public class Descriptor implements Comparable<Descriptor>
{

	public static class PointD
	{
		/**
		 * X
		 */
		public double X;

		/**
		 * Y
		 */
		public double Y;

		/**
		 * Standardkonstruktor
		 */
		public PointD(double x, double y)
		{
			this.X = x;
			this.Y = y;
		}
	};

	public static int[] TilesPerLine = null;
	public static int[] TilesPerColumn = null;
	static int[] tileOffset = null;

	public static void Init()
	{
		int maxZoom = 25;

		TilesPerLine = new int[maxZoom];
		TilesPerColumn = new int[maxZoom];
		tileOffset = new int[maxZoom];

		tileOffset[0] = 0;

		for (int i = 0; i < maxZoom - 1; i++)
		{
			TilesPerLine[i] = (int) (2 * Math.pow(2, i));
			TilesPerColumn[i] = (int) Math.pow(2, i);
			tileOffset[i + 1] = tileOffset[i] + (int) (TilesPerLine[i] * TilesPerColumn[i]);
		}
	}

	/**
	 * X-Koordinate der Kachel
	 */
	public int X;

	/**
	 * Y-Koordinate der Kachel
	 */
	public int Y;

	/**
	 * Zoom-Stufe der Kachel
	 */
	public int Zoom;

	/**
	 * Erzeugt einen neuen Deskriptor mit den übergebenen Parametern
	 * 
	 * @param x
	 *            X-Koordinate der Kachel
	 * @param y
	 *            Y-Koordinate der Kachel
	 * @param zoom
	 *            Zoom-Stufe
	 */
	public Descriptor(int x, int y, int zoom)
	{
		X = x;
		Y = y;
		Zoom = zoom;
	}

	/**
	 * Copy-Konstruktor
	 * 
	 * @param original
	 *            Zu klonende Instanz
	 */
	public Descriptor(Descriptor original)
	{
		this.X = original.X;
		this.Y = original.Y;
		this.Zoom = original.Zoom;
	}

	/**
	 * Erzeugt einen neuen Deskriptor mit anderer Zoom-Stufe
	 */
	public Descriptor AdjustZoom(int newZoomLevel)
	{
		int zoomDiff = newZoomLevel - Zoom;
		int pow = (int) Math.pow(2, Math.abs(zoomDiff));

		return new Descriptor(X * pow, Y * pow, newZoomLevel);
	}

	/**
	 * Projeziert die übergebene Koordinate in den Tile Space
	 * 
	 * @param latitude
	 *            Breitengrad
	 * @param longitude
	 *            Längengrad
	 * @param projectionZoom
	 *            zoom
	 * @return PointD
	 */
	public static PointD projectCoordinate(double latitude, double longitude, int projectionZoom)
	{
		PointD result = new PointD(LongitudeToTileX(projectionZoom, longitude), LatitudeToTileY(projectionZoom, latitude));

		return result;
	}

	/**
	 * Berechnet aus dem übergebenen Längengrad die X-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
	 * 
	 * @param zoom
	 *            Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
	 * @param longitude
	 *            Longitude
	 * @return double
	 */
	public static double LongitudeToTileX(int zoom, double longitude)
	{
		return (longitude + 180.0) / 360.0 * Math.pow(2, zoom);
	}

	/**
	 * Berechnet aus dem übergebenen Breitengrad die Y-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
	 * 
	 * @param zoom
	 *            Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
	 * @param latitude
	 *            Latitude
	 * @return double
	 */
	public static double LatitudeToTileY(int zoom, double latitude)
	{
		double latRad = latitude * Math.PI / 180.0;

		return (1 - Math.log(Math.tan(latRad) + (1.0 / Math.cos(latRad))) / Math.PI) / 2 * Math.pow(2, zoom);
	}

	/**
	 * Berechnet aus der übergebenen OSM-X-Koordinate den entsprechenden Längengrad
	 */
	public static double TileXToLongitude(int zoom, double x)
	{

		return -180.0 + (360.0 * x) / Math.pow(2, zoom);
	}

	/**
	 * Berechnet aus der übergebenen OSM-Y-Koordinate den entsprechenden Breitengrad
	 */
	public static double TileYToLatitude(int zoom, double y)
	{
		double xNom = Math.exp(2 * Math.PI) - Math.exp(4 * Math.PI * Math.pow(2, -zoom) * y);
		double xDen = Math.exp(2 * Math.PI) + Math.exp(4 * Math.PI * Math.pow(2, -zoom) * y);

		double yNom = 2 * Math.exp(-Math.PI * (-1 + Math.pow(2, 1 - zoom) * y));
		double yDen = Math.exp(-2 * Math.PI * (-1 + Math.pow(2, 1 - zoom) * y)) + 1;

		return Math.atan2(xNom / xDen, yNom / yDen) * 180.0 / Math.PI;
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
	public PointD ToWorld(int xOffset, int yOffset, int desiredZoom)
	{
		double adjust = Math.pow(2, (desiredZoom - Zoom));
		return new PointD((X + xOffset) * adjust * 256, (Y + yOffset) * adjust * 256);
	}

	public static PointD ToWorld(double X, double Y, int zoom, int desiredZoom)
	{
		double adjust = Math.pow(2, (desiredZoom - zoom));
		return new PointD(X * adjust * 256, Y * adjust * 256);
	}

	public static PointD FromWorld(double X, double Y, int zoom, int desiredZoom)
	{
		double adjust = Math.pow(2, (desiredZoom - zoom));
		return new PointD(X / (adjust * 256), Y / (adjust * 256));
	}

	public long GetHashCode()
	{
		return ((long) (tileOffset[Zoom]) + (long) (TilesPerLine[Zoom]) * Y + X);
	}

	public String ToString()
	{
		return "X = " + X + ", Y = " + Y + ", Zoom = " + Zoom;
	}

	@Override
	public int compareTo(Descriptor another)
	{
		long hashcode = this.GetHashCode();
		long objHashcode = another.GetHashCode();

		if (hashcode == objHashcode) return 0;

		if (hashcode < objHashcode) return -1;

		return 1;
	}

}
