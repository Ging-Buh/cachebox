package CB_Locator;

import java.io.Serializable;

import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Converter.UTMConvert;
import CB_Utils.Lists.CB_List;

public class Coordinate implements Serializable
{
	private static final long serialVersionUID = 4288564255686705L;

	static final String br = System.getProperty("line.separator");

	protected boolean Valid;
	/**
	 * @uml.property name="latitude"
	 */
	private double Latitude = 0;
	/**
	 * @uml.property name="longitude"
	 */
	private double Longitude = 0;
	private static final float[] mResults = new float[2];

	public static Coordinate Project(Coordinate coord, double Direction, double Distance)
	{
		return Project(coord.getLatitude(), coord.getLongitude(), Direction, Distance);
	}

	public boolean isValid()
	{
		return Valid;
	}

	public boolean isZero()
	{
		if (!isValid()) return false;
		return ((Latitude == 0) && (Longitude == 0));
	}

	/**
	 * Gibt einen Formatierten String dieser Koordinate wieder
	 * 
	 * @return
	 */
	public String FormatCoordinate()
	{
		if (Valid) return Formatter.FormatLatitudeDM(getLatitude()) + " / " + Formatter.FormatLongitudeDM(getLongitude());
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
		if (Valid) return Formatter.FormatLatitudeDM(getLatitude()) + br + Formatter.FormatLongitudeDM(getLongitude());
		else
			return "not Valid";
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
		double a = (Latitude >= 0) ? (90 - Latitude) * MathUtils.DEG_RAD : Latitude * MathUtils.DEG_RAD;

		double q = (360 - Direction) * MathUtils.DEG_RAD;
		double b = Math.acos(Math.cos(q) * Math.sin(a) * Math.sin(c) + Math.cos(a) * Math.cos(c));

		result.setLatitude(90 - (b * MathUtils.RAD_DEG));
		if (result.getLatitude() > 90) result.setLatitude(result.getLatitude() - 180);

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

		result.setLongitude(Longitude - g * MathUtils.RAD_DEG);

		result.Valid = true;
		return result;
	}

	public static double Bearing(CalculationType type, Coordinate coord1, Coordinate coord2)
	{
		return Bearing(type, coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude());
	}

	public static double Bearing(CalculationType type, double froLatitude, double fromLongitude, double toLatitude, double toLongitude)
	{
		Coordinate loc = new Coordinate();
		loc.setLatitude(froLatitude);
		loc.setLongitude(fromLongitude);

		Coordinate loc2 = new Coordinate("");
		loc2.setLatitude(toLatitude);
		loc2.setLongitude(toLongitude);

		return loc.bearingTo(loc2, type);

	}

	public static Coordinate Intersection(Coordinate coord1, Coordinate coord2, Coordinate coord3, Coordinate coord4)
	{
		Coordinate result = null;

		double[] x = new double[4];
		double[] y = new double[4];
		x[0] = coord1.getLongitude();
		y[0] = coord1.getLatitude();
		x[1] = coord2.getLongitude();
		y[1] = coord2.getLatitude();
		x[2] = coord3.getLongitude();
		y[2] = coord3.getLatitude();
		x[3] = coord4.getLongitude();
		y[3] = coord4.getLatitude();

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

	/**
	 * Returns the approximate initial bearing in degrees East of true North when traveling along the shortest path between this location
	 * and the given location. The shortest path is defined using the WGS84 ellipsoid. Locations that are (nearly) antipodal may produce
	 * meaningless results.
	 * 
	 * @param dest
	 *            the destination location
	 * @return the initial bearing in degrees
	 */
	public float bearingTo(Coordinate dest, CalculationType type)
	{
		synchronized (mResults)
		{
			// See if we already have the result
			// if (getLatitude() != mLat1 || getLongitude() != mLon1 || dest.getLatitude() != mLat2 || dest.getLongitude() != mLon2)
			// {
			// MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), dest.getLatitude(), dest.getLongitude(), mResults);
			// mLat1 = getLatitude();
			// mLon1 = getLongitude();
			// mLat2 = dest.getLatitude();
			// mLon2 = dest.getLongitude();
			// mInitialBearing = mResults[1];
			// }
			// return mInitialBearing;

			synchronized (mResults)
			{
				MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), dest.getLatitude(), dest.getLongitude(), mResults);
				return mResults[1];
			}
		}
	}

	/**
	 * Returns the distance to to refer Coordinate
	 * 
	 * @param coord
	 * @return
	 */
	public float Distance(Coordinate coord, CalculationType type)
	{
		// float[] dist = new float[1];
		MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), coord.getLatitude(), coord.getLongitude(), mResults);
		return mResults[0];
	}

	/**
	 * Returns the distance to to last valid Position
	 * 
	 * @return
	 */
	public float Distance(CalculationType type)
	{
		float[] dist = new float[1];
		MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), Locator.getLatitude(), Locator.getLongitude(), dist);
		return dist[0];
	}

	public boolean equals(Coordinate coord)
	{
		if (this.getLatitude() != coord.getLatitude()) return false;
		if (this.getLongitude() != coord.getLongitude()) return false;

		return true;
	}

	public static Coordinate Crossbearing(CalculationType type, Coordinate coord1, double direction1, Coordinate coord2, double direction2)
	{
		float[] dist = new float[4];
		MathUtils.computeDistanceAndBearing(type, coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude(),
				dist);
		double distance = dist[0];
		Coordinate coord3 = Project(coord1, direction1, distance);
		Coordinate coord4 = Project(coord2, direction2, distance);

		return Intersection(coord1, coord3, coord2, coord4);
	}

	public Coordinate()
	{
		super();
	}

	public Coordinate(Coordinate parent)
	{
		this.setLatitude(parent.getLatitude());
		this.setLongitude(parent.getLongitude());
		this.Valid = parent.Valid;
	}

	public Coordinate(double latitude, double longitude)
	{
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		Valid = true;

	}

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
					this.setLatitude(ddlat);
					this.setLongitude(ddlon);
					return;
				}
				catch (Exception ex)
				{

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
		CB_List<String> llat = new CB_List<String>(clat.length);
		CB_List<String> llon = new CB_List<String>(clon.length);
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
			Valid = false;
			return;
		}
		this.setLatitude(lat);
		this.setLongitude(lon);
		if (dlat == 'S') this.setLatitude(-this.getLatitude());
		if (dlon == 'W') this.setLongitude(-this.getLongitude());
		this.Valid = true;
		if (this.getLatitude() > 180.00001) this.Valid = false;
		if (this.getLatitude() < -180.00001) this.Valid = false;
		if (this.getLongitude() > 180.00001) this.Valid = false;
		if (this.getLongitude() < -180.00001) this.Valid = false;
	}

	public Coordinate copy()
	{
		return new Coordinate(this);
	}

	/**
	 * @return
	 * @uml.property name="latitude"
	 */
	public double getLatitude()
	{
		return Latitude;
	}

	/**
	 * @param latitude
	 * @uml.property name="latitude"
	 */
	public void setLatitude(double latitude)
	{
		Latitude = latitude;
		Valid = true;// FIXME check if relay valid
	}

	/**
	 * @return
	 * @uml.property name="longitude"
	 */
	public double getLongitude()
	{
		return Longitude;
	}

	/**
	 * @param longitude
	 * @uml.property name="longitude"
	 */
	public void setLongitude(double longitude)
	{
		Longitude = longitude;
		Valid = true; // FIXME check if relay valid
	}

	public void setValid(boolean b)
	{
		Valid = b;
	}

	@Override
	public String toString()
	{
		return FormatCoordinate();
	}

}