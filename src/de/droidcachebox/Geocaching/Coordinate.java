package de.droidcachebox.Geocaching;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import de.droidcachebox.Global;

import android.location.Location;

public class Coordinate implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1235642315487L;
	
	public boolean Valid;
	public double Latitude = 0;
	public double Longitude = 0;
	public double Elevation = 0;
	
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

    public Coordinate(Coordinate parent)
    {
      this.Latitude = parent.Latitude;
      this.Longitude = parent.Longitude;
      this.Elevation = parent.Elevation;
      this.Valid = parent.Valid;
    }
        
    // Parse Coordinates from String
    public Coordinate(String text)
    {
      text = text.toUpperCase();
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
            double nording = Double.valueOf(snording);
            double easting = Double.valueOf(seasting);
//            UTM.Convert convert = new UTM.Convert();
            double ddlat = 0;
            double ddlon = 0;
//            convert.iUTM2LatLon(nording, easting, zone, ref ddlat, ref ddlon);
            // Ergebnis runden, da sonst Koordinaten wie 47° 60' herauskommen!
//            ddlat = Math.round(ddlat, 6);
//            ddlon = Math.round(ddlon, 6);
            this.Valid = true;
            this.Latitude = ddlat;
            this.Longitude = ddlon;
            return;
          } catch(Exception ex)
          {
          }
        }
      }


      text = text.replace("'", "");
      text = text.replace("\"", "");
      text = text.replace("\r", "");
      text = text.replace("\n", "");
      text = text.replace("/", ""); 
//      NumberFormatInfo ni = new NumberFormatInfo();
//      text = text.Replace(".", Global.DecimalSeparator);
      text = text.replace(",", ".");
      double lat = 0;
      double lon = 0;
      int ilat = text.indexOf('N');
      if (ilat < 0)
    	  ilat = text.indexOf('S');
      int ilon = text.indexOf('E');
      if (ilon < 0)
    	  ilon = text.indexOf('W');
      if (ilat < 0) return;
      if (ilon < 0) return;
      if (ilat > ilon) return;
      char dlat = text.charAt(ilat);
      char dlon = text.charAt(ilon);
      String slat = "";
      String slon = "";
      if (ilat < 2)
      {
        slat = text.substring(ilat + 1, ilon).trim().replace("°", "");
        slon = text.substring(ilon + 1, text.length()).trim().replace("°", "");
      }
      else
      {
        slat = text.substring(0, ilat).trim().replace("°", "");
        slon = text.substring(ilat+1, ilon - ilat - 1).trim().replace("°", "");
      }

      String[] clat = slat.split(" ");
      String[] clon = slon.split(" ");
      ArrayList<String> llat = new ArrayList<String>();
      ArrayList<String> llon = new ArrayList<String>();
      for (String ss : clat)
      {
        if (ss != "")
          llat.add(ss);
      }
      for (String ss : clon)
      {
        if (ss != "")
          llon.add(ss);
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
      this.Latitude = lat;
      this.Longitude = lon;
      if (dlat == 'S')
        this.Latitude = -this.Latitude;
      if (dlon == 'W')
        this.Longitude = -this.Longitude;
      this.Valid = true;
      if (this.Latitude > 180.00001)
        this.Valid = false;
      if (this.Latitude < -180.00001)
        this.Valid = false;
      if (this.Longitude > 180.00001)
        this.Valid = false;
      if (this.Longitude < -180.00001)
        this.Valid = false;
    }

    public String FormatCoordinate()
    {
      if (Valid)
        return Global.FormatLatitudeDM(Latitude) + " / " + Global.FormatLongitudeDM(Longitude);
      else
        return "not Valid";
    }

    /// <summary>
    /// Projiziert die übergebene Koordinate
    /// </summary>
    /// <param name="Latitude">Breitengrad</param>
    /// <param name="Longitude">Längengrad</param>
    /// <param name="Direction">Richtung</param>
    /// <param name="Distance">Distanz</param>
    /// <returns>Die projizierte Koordinate</returns>
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
      if (UnitFormatter.ImperialUnits)
        c = c / 0.9144f;
*/
      double a = (Latitude >= 0) ? (90 - Latitude) * Math.PI / 180 : Latitude * Math.PI / 180;

      double q = (360 - Direction) * Math.PI / 180.0;
      double b = Math.acos(Math.cos(q) * Math.sin(a) * Math.sin(c) + Math.cos(a) * Math.cos(c));

      result.Latitude = 90 - (b * 180 / Math.PI);
      if (result.Latitude > 90)
        result.Latitude -= 180;

      double g = 0;
      try
      {
    	  g = ((a + b) == 0) ? 0 : Math.acos((Math.cos(c) - Math.cos(a) * Math.cos(b)) / (Math.sin(a) * Math.sin(b)));
      } catch (Exception ex)
      {
    	  g = 0;
      }

      if (Direction <= 180)
        g = -g;

      result.Longitude = Longitude - g * 180 / Math.PI;

      result.Valid = true;
      return result;
    }

    public static double Bearing(Coordinate coord1, Coordinate coord2)
    {
        return Bearing(coord1.Latitude, coord1.Longitude, coord2.Latitude, coord2.Longitude);
    }
    /// <summary>
    /// Berechnet den Kurs von from nach to auf einer Kugel
    /// </summary>
    /// <param name="fromLatitude"></param>
    /// <param name="fromLongitude"></param>
    /// <param name="toLatitude"></param>
    /// <param name="toLongitude"></param>
    /// <returns></returns>
    public static double Bearing(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude)
    {
    	Location loc = new Location("");
    	loc.setLatitude(fromLatitude);
    	loc.setLongitude(fromLongitude);
    	
    	Location loc2 = new Location("");
    	loc2.setLatitude(toLatitude);
    	loc2.setLongitude(toLongitude);

    	return loc.bearingTo(loc2);
    	
/*    	
    	if (fromLatitude == toLatitude && fromLongitude == toLongitude)
            return 0;

        double latFromRad = fromLatitude * Math.PI / 180.0;
        double latToRad = toLatitude * Math.PI / 180.0;
        double lonFromRad = fromLongitude * Math.PI / 180.0;
        double lonToRad = toLongitude * Math.PI / 180.0;

        double x = Math.cos(latFromRad) * Math.sin(latToRad) - Math.sin(latFromRad) * Math.cos(latToRad) * Math.cos(lonFromRad - lonToRad);
        double y = -Math.sin(lonFromRad - lonToRad) * Math.cos(latToRad);

        return -(Math.atan2(y, x) * 180.0 / Math.PI);*/
    }
}
