package de.droidcachebox.Geocaching;

import android.location.Location;

public class Coordinate {
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
