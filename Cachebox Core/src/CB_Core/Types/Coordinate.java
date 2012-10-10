package CB_Core.Types;

import java.io.Serializable;
import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.Converter.Formatter;
import CB_Core.Converter.UTMConvert;
import CB_Core.Log.Logger;

public class Coordinate implements Serializable
{

	private static final long serialVersionUID = 1235642315487L;

	public boolean Valid;
	public double Latitude = 0;
	public double Longitude = 0;
	public double Elevation = 0;

	/**
	 * Die Genauigkeit dieser Coordinate! Wird beim Messen benutzt
	 */
	public int Accuracy = -1;

	// Cache the inputs and outputs of computeDistanceAndBearing
	// so calls to distanceTo() and bearingTo() can share work
	private double mLat1 = 0.0;
	private double mLon1 = 0.0;
	private double mLat2 = 0.0;
	private double mLon2 = 0.0;
	// private float mDistance = 0.0f;
	private float mInitialBearing = 0.0f;

	// Scratchpad
	private float[] mResults = new float[2];

	public Coordinate()
	{
		Valid = false;
	}

	public Coordinate(double latitude, double longitude)
	{
		this.Latitude = latitude;
		this.Longitude = longitude;
		this.Elevation = 0;
		Valid = true;
	}

	public Coordinate(double latitude, double longitude, int accuracy)
	{
		this.Latitude = latitude;
		this.Longitude = longitude;
		this.Elevation = 0;
		Accuracy = accuracy;
		Valid = true;
	}

	public Coordinate(Coordinate parent)
	{
		this.Latitude = parent.Latitude;
		this.Longitude = parent.Longitude;
		this.Elevation = parent.Elevation;
		this.Valid = parent.Valid;
	}

	public double getLatitude()
	{
		return this.Latitude;
	}

	public double getLongitude()
	{
		return this.Longitude;
	}

	public boolean hasAccuracy()
	{
		if (Accuracy == -1) return false;
		return true;
	}

	public int getAccuracy()
	{
		return Accuracy;
	}

	// Parse Coordinates from String
	public Coordinate(String text)
	{
		text = text.toUpperCase();
		text = text.replace(",", ".");
		Valid = false;

		// UTM versuche
		String[] utm = text.trim().split(" ");
		if (utm.length == 3)
		{
			{
				String zone = utm[0];
				String seasting = utm[1];
				String snording = utm[2];
				try
				{
					snording = snording.replace(",", ".");
					seasting = seasting.replace(",", ".");
					double nording = Double.valueOf(snording);
					double easting = Double.valueOf(seasting);
					UTMConvert convert = new UTMConvert();
					double ddlat = 0;
					double ddlon = 0;
					convert.iUTM2LatLon(nording, easting, zone);
					ddlat = convert.dLat;
					ddlon = convert.dLon;
					// Ergebnis runden, da sonst Koordinaten wie 47° 60' herauskommen!
					ddlat = Math.rint(ddlat * 1000000) / 1000000;
					ddlon = Math.rint(ddlon * 1000000) / 1000000;
					this.Valid = true;
					this.Latitude = ddlat;
					this.Longitude = ddlon;
					return;
				}
				catch (Exception ex)
				{
					Logger.Error("Coordinate(String text)", "UTM Versuche", ex);
				}
			}
		}

		text = text.replace("'", "");
		text = text.replace("\\U0022", "");
		text = text.replace("\"", "");
		text = text.replace("\r", "");
		text = text.replace("\n", "");
		text = text.replace("/", "");
		// NumberFormatInfo ni = new NumberFormatInfo();
		// text = text.Replace(".", Global.DecimalSeparator);
		text = text.replace(",", ".");
		double lat = 0;
		double lon = 0;
		int ilat = text.indexOf('N');
		if (ilat < 0) ilat = text.indexOf('S');
		int ilon = text.indexOf('E');
		if (ilon < 0) ilon = text.indexOf('W');
		if (ilat < 0) return;
		if (ilon < 0) return;
		if (ilat > ilon) return;
		char dlat = text.charAt(ilat);
		char dlon = text.charAt(ilon);
		String slat = "";
		String slon = "";
		if (ilat < 2)
		{
			slat = text.substring(ilat + 1, ilon).trim().replace("\u00B0", " ");
			slon = text.substring(ilon + 1, text.length()).trim().replace("\u00B0", " ");
		}
		else
		{
			slat = text.substring(0, ilat).trim().replace("\u00B0", " ");
			slon = text.substring(ilat + 1, text.length() - 1).trim().replace("\u00B0", " ");
		}

		String[] clat = slat.split(" ");
		String[] clon = slon.split(" ");
		ArrayList<String> llat = new ArrayList<String>();
		ArrayList<String> llon = new ArrayList<String>();
		for (String ss : clat)
		{
			if (!ss.equals(""))
			{
				llat.add(ss);
			}

		}
		for (String ss : clon)
		{
			if (!ss.equals(""))
			{
				llon.add(ss);
			}

		}

		try
		{
			if ((llat.size() == 1) && (llon.size() == 1))
			{
				// Decimal
				lat = Double.valueOf(llat.get(0));
				lon = Double.valueOf(llon.get(0));
			}
			else if ((llat.size() == 2) && (llon.size() == 2))
			{
				// Decimal Minute
				lat = Integer.valueOf(llat.get(0));
				lat += Double.valueOf(llat.get(1)) / 60;
				lon = Integer.valueOf(llon.get(0));
				lon += Double.valueOf(llon.get(1)) / 60;
			}
			else if ((llat.size() == 3) && (llon.size() == 3))
			{
				// Decimal - Minute - Second
				lat = Integer.valueOf(llat.get(0));
				lat += Double.valueOf(llat.get(1)) / 60;
				lat += Double.valueOf(llat.get(2)) / 3600;
				lon = Integer.valueOf(llon.get(0));
				lon += Double.valueOf(llon.get(1)) / 60;
				lon += Double.valueOf(llon.get(2)) / 3600;
			}
		}
		catch (Exception exc)
		{
			Logger.Error("Coordinate(String text)", "Valid Coord", exc);
			Valid = false;
			return;
		}
		this.Latitude = lat;
		this.Longitude = lon;
		if (dlat == 'S') this.Latitude = -this.Latitude;
		if (dlon == 'W') this.Longitude = -this.Longitude;
		this.Valid = true;
		if (this.Latitude > 180.00001) this.Valid = false;
		if (this.Latitude < -180.00001) this.Valid = false;
		if (this.Longitude > 180.00001) this.Valid = false;
		if (this.Longitude < -180.00001) this.Valid = false;
	}

	/**
	 * Gibt einen Formatierten String dieser Koordinate wieder
	 * 
	 * @return
	 */
	public String FormatCoordinate()
	{
		if (Valid) return Formatter.FormatLatitudeDM(Latitude) + " / " + Formatter.FormatLongitudeDM(Longitude);
		else
			return "not Valid";
	}

	/**
	 * Gibt einen Formatierten String dieser Koordinate in zwei Zeilen wieder
	 * 
	 * @return
	 */
	public String FormatCoordinateLineBreake()
	{
		if (Valid) return Formatter.FormatLatitudeDM(Latitude) + GlobalCore.br + Formatter.FormatLongitudeDM(Longitude);
		else
			return "not Valid";
	}

	// / <summary>
	// / Projiziert die übergebene Koordinate
	// / </summary>
	// / <param name="Latitude">Breitengrad</param>
	// / <param name="Longitude">Längengrad</param>
	// / <param name="Direction">Richtung</param>
	// / <param name="Distance">Distanz</param>
	// / <returns>Die projizierte Koordinate</returns>
	public static Coordinate Project(Coordinate coord, double Direction, double Distance)
	{
		return Project(coord.Latitude, coord.Longitude, Direction, Distance);
	}

	public static Coordinate Project(double Latitude, double Longitude, double Direction, double Distance)
	{
		// nach http://www.zwanziger.de/gc_tools_projwp.html
		Coordinate result = new Coordinate();

		// Bearing auf [0..360] begrenzen
		while (Direction > 360)
			Direction -= 360;

		while (Direction < 0)
			Direction += 360;

		double c = Distance / 6378137.0;
		/*
		 * if (UnitFormatter.ImperialUnits) c = c / 0.9144f;
		 */
		double a = (Latitude >= 0) ? (90 - Latitude) * Math.PI / 180 : Latitude * Math.PI / 180;

		double q = (360 - Direction) * Math.PI / 180.0;
		double b = Math.acos(Math.cos(q) * Math.sin(a) * Math.sin(c) + Math.cos(a) * Math.cos(c));

		result.Latitude = 90 - (b * 180 / Math.PI);
		if (result.Latitude > 90) result.Latitude -= 180;

		double g = 0;
		try
		{
			g = ((a + b) == 0) ? 0 : Math.acos((Math.cos(c) - Math.cos(a) * Math.cos(b)) / (Math.sin(a) * Math.sin(b)));
			if (Double.isNaN(g)) g = 0;
		}
		catch (Exception ex)
		{
			g = 0;
		}

		if (Direction <= 180) g = -g;

		result.Longitude = Longitude - g * 180 / Math.PI;

		result.Valid = true;
		return result;
	}

	public static double Bearing(Coordinate coord1, Coordinate coord2)
	{
		return Bearing(coord1.Latitude, coord1.Longitude, coord2.Latitude, coord2.Longitude);
	}

	// / <summary>
	// / Berechnet den Kurs von from nach to auf einer Kugel
	// / </summary>
	// / <param name="froLatitude"></param>
	// / <param name="fromLongitude"></param>
	// / <param name="toLatitude"></param>
	// / <param name="toLongitude"></param>
	// / <returns></returns>
	public static double Bearing(double froLatitude, double fromLongitude, double toLatitude, double toLongitude)
	{
		Coordinate loc = new Coordinate();
		loc.Latitude = froLatitude;
		loc.Longitude = fromLongitude;

		Coordinate loc2 = new Coordinate("");
		loc2.Latitude = toLatitude;
		loc2.Longitude = toLongitude;

		return loc.bearingTo(loc2);

	}

	/**
	 * Returns the approximate initial bearing in degrees East of true North when traveling along the shortest path between this location
	 * and the given location. The shortest path is defined using the WGS84 ellipsoid. Locations that are (nearly) antipodal may produce
	 * meaningless results.
	 * 
	 * @param dest
	 *            the destination location
	 * @return the initial bearing in degrees
	 */
	public float bearingTo(Coordinate dest)
	{
		synchronized (mResults)
		{
			// See if we already have the result
			if (Latitude != mLat1 || Longitude != mLon1 || dest.Latitude != mLat2 || dest.Longitude != mLon2)
			{
				computeDistanceAndBearing(Latitude, Longitude, dest.Latitude, dest.Longitude, mResults);
				mLat1 = Latitude;
				mLon1 = Longitude;
				mLat2 = dest.Latitude;
				mLon2 = dest.Longitude;
				mInitialBearing = mResults[1];
			}
			return mInitialBearing;
		}
	}

	public float Distance(Coordinate coord)
	{
		float[] dist = new float[4];
		distanceBetween(Latitude, Longitude, coord.Latitude, coord.Longitude, dist);
		return dist[0];
	}

	private static void computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2, float[] results)
	{
		// Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
		// using the "Inverse Formula" (section 4)

		int MAXITERS = 20;
		// Convert lat/long to radians
		lat1 *= Math.PI / 180.0;
		lat2 *= Math.PI / 180.0;
		lon1 *= Math.PI / 180.0;
		lon2 *= Math.PI / 180.0;

		double a = 6378137.0; // WGS84 major axis
		double b = 6356752.3142; // WGS84 semi-major axis
		double f = (a - b) / a;
		double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

		double L = lon2 - lon1;
		double A = 0.0;
		double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
		double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

		double cosU1 = Math.cos(U1);
		double cosU2 = Math.cos(U2);
		double sinU1 = Math.sin(U1);
		double sinU2 = Math.sin(U2);
		double cosU1cosU2 = cosU1 * cosU2;
		double sinU1sinU2 = sinU1 * sinU2;

		double sigma = 0.0;
		double deltaSigma = 0.0;
		double cosSqAlpha = 0.0;
		double cos2SM = 0.0;
		double cosSigma = 0.0;
		double sinSigma = 0.0;
		double cosLambda = 0.0;
		double sinLambda = 0.0;

		double lambda = L; // initial guess
		for (int iter = 0; iter < MAXITERS; iter++)
		{
			double lambdaOrig = lambda;
			cosLambda = Math.cos(lambda);
			sinLambda = Math.sin(lambda);
			double t1 = cosU2 * sinLambda;
			double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
			double sinSqSigma = t1 * t1 + t2 * t2; // (14)
			sinSigma = Math.sqrt(sinSqSigma);
			cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
			sigma = Math.atan2(sinSigma, cosSigma); // (16)
			double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda / sinSigma; // (17)
			cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
			cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

			double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
			A = 1 + (uSquared / 16384.0) * // (3)
					(4096.0 + uSquared * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
			double B = (uSquared / 1024.0) * // (4)
					(256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
			double C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
			double cos2SMSq = cos2SM * cos2SM;
			deltaSigma = B
					* sinSigma
					* // (6)
					(cos2SM + (B / 4.0)
							* (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0) * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma)
									* (-3.0 + 4.0 * cos2SMSq)));

			lambda = L + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SM + C * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

			double delta = (lambda - lambdaOrig) / lambda;
			if (Math.abs(delta) < 1.0e-12)
			{
				break;
			}
		}

		float distance = (float) (b * A * (sigma - deltaSigma));
		results[0] = distance;
		if (results.length > 1)
		{
			float initialBearing = (float) Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
			initialBearing *= 180.0 / Math.PI;
			results[1] = initialBearing;
			if (results.length > 2)
			{
				float finalBearing = (float) Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
				finalBearing *= 180.0 / Math.PI;
				results[2] = finalBearing;
			}
		}
	}

	/**
	 * Computes the approximate distance in meters between two locations, and optionally the initial and final bearings of the shortest path
	 * between them. Distance and bearing are defined using the WGS84 ellipsoid.
	 * <p>
	 * The computed distance is stored in results[0]. If results has length 2 or greater, the initial bearing is stored in results[1]. If
	 * results has length 3 or greater, the final bearing is stored in results[2].
	 * 
	 * @param startLatitude
	 *            the starting latitude
	 * @param startLongitude
	 *            the starting longitude
	 * @param endLatitude
	 *            the ending latitude
	 * @param endLongitude
	 *            the ending longitude
	 * @param results
	 *            an array of floats to hold the results
	 * @throws IllegalArgumentException
	 *             if results is null or has length < 1
	 */
	public static void distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results)
	{
		if (results == null || results.length < 1)
		{
			throw new IllegalArgumentException("results is null or has length < 1");
		}
		computeDistanceAndBearing(startLatitude, startLongitude, endLatitude, endLongitude, results);
	}

	public boolean equals(Coordinate coord)
	{
		if (this.Latitude != coord.Latitude) return false;
		if (this.Longitude != coord.Longitude) return false;

		return true;
	}

	public static Coordinate Intersection(Coordinate coord1, Coordinate coord2, Coordinate coord3, Coordinate coord4)
	{
		Coordinate result = null;

		double[] x = new double[4];
		double[] y = new double[4];
		x[0] = coord1.Longitude;
		y[0] = coord1.Latitude;
		x[1] = coord2.Longitude;
		y[1] = coord2.Latitude;
		x[2] = coord3.Longitude;
		y[2] = coord3.Latitude;
		x[3] = coord4.Longitude;
		y[3] = coord4.Latitude;

		// Steigungen
		double steig1 = (y[1] - y[0]) / (x[1] - x[0]);
		double steig2 = (y[3] - y[2]) / (x[3] - x[2]);
		// Nullwerte
		double null1 = y[0] - x[0] * steig1;
		double null2 = y[2] - x[2] * steig2;
		// Schnittpunkt
		double X = (null2 - null1) / (steig1 - steig2);
		double Y = steig1 * X + null1;
		// Konvertieren in Lat-Lon

		result = new Coordinate(Y, X);
		return result;
	}

	public static Coordinate Crossbearing(Coordinate coord1, double direction1, Coordinate coord2, double direction2)
	{
		float[] dist = new float[4];
		distanceBetween(coord1.Latitude, coord1.Longitude, coord2.Latitude, coord2.Longitude, dist);
		double distance = dist[0];
		Coordinate coord3 = Project(coord1, direction1, distance);
		Coordinate coord4 = Project(coord2, direction2, distance);

		return Intersection(coord1, coord3, coord2, coord4);
	}

	public Coordinate copy()
	{
		return new Coordinate(this);
	}
}