package CB_Locator;

import java.io.Serializable;

import CB_Utils.Converter.UTMConvert;
import CB_Utils.Lists.CB_List;

/**
 * @author Longri
 */
public class CoordinateGPS extends Coordinate implements Serializable
{

	private static final long serialVersionUID = 1235642315487L;

	/**
	 * @uml.property name="elevation"
	 */
	private double Elevation = 0;

	/**
	 * Die Genauigkeit dieser Coordinate! Wird beim Messen benutzt
	 * 
	 * @uml.property name="accuracy"
	 */
	private int Accuracy = -1;

	// Cache the inputs and outputs of computeDistanceAndBearing
	// so calls to distanceTo() and bearingTo() can share work
	// private final double mLat1 = 0.0;
	// private final double mLon1 = 0.0;
	// private final double mLat2 = 0.0;
	// private final double mLon2 = 0.0;
	// private final float mInitialBearing = 0.0f;
	// private float mDistance = 0.0f;

	public CoordinateGPS()
	{
		Valid = false;
	}

	public CoordinateGPS(double latitude, double longitude)
	{
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setElevation(0);
		Valid = true;
	}

	public CoordinateGPS(double latitude, double longitude, int accuracy)
	{
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setElevation(0);
		this.Accuracy = accuracy;
		Valid = true;
	}

	public CoordinateGPS(CoordinateGPS parent)
	{
		this.setLatitude(parent.getLatitude());
		this.setLongitude(parent.getLongitude());
		this.setElevation(parent.getElevation());
		this.Accuracy = parent.getAccuracy();
		this.Valid = parent.Valid;
	}

	public boolean hasAccuracy()
	{
		if (Accuracy == -1) return false;
		return true;
	}

	/**
	 * @return
	 * @uml.property name="accuracy"
	 */
	public int getAccuracy()
	{
		return Accuracy;
	}

	// Parse Coordinates from String
	public CoordinateGPS(String text)
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

	/**
	 * @return
	 * @uml.property name="elevation"
	 */
	public double getElevation()
	{
		return Elevation;
	}

	/**
	 * @param elevation
	 * @uml.property name="elevation"
	 */
	public void setElevation(double elevation)
	{
		Elevation = elevation;
	}

	public void setAccuracy(float accuracy)
	{
		Accuracy = (int) accuracy;
	}

}