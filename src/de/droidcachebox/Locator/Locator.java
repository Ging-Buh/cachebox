package de.droidcachebox.Locator;

import android.location.Location;
import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Geocaching.Coordinate;

public class Locator {
    private Location location = null;
    public void setLocation(Location value)
    {
    	synchronized (this)
    	{
    		location = value;
    	}
    }
    public Location getLocation()
    {
    	synchronized (this)
    	{
    		return location;
    	}
    }
    /// <summary>
    /// Aktuelle Position des Empf�ngers
    /// </summary>
    public Coordinate Position = new Coordinate();

    /// <summary>
    /// Aktueller Winkel des mag. Kompass
    /// </summary>
    private float CompassHeading = -1;
    public void setCompassHeading(float value)
    {
    	synchronized (this)
    	{
    		CompassHeading = value;
    	}
    }
    public float getCompassHeading()
    {
    	synchronized (this)
    	{
    		return CompassHeading;
    	}
    }
    
    public float SpeedOverGround()
    {
    	if ((location != null) && (location.hasSpeed()))
    	{
    		return location.getSpeed() * 3600 / 1000;    			
    	} else
    		return 0;
    }
    public String SpeedString()
    {
    	if ((location != null) && (location.hasSpeed()))
    		return UnitFormatter.SpeedString(Global.Locator.SpeedOverGround());
    	else
    		return "-----";
    }
    
    public Locator()
    {
    	this.location = null;
    }
    
    public boolean UseCompass()
    {
    	synchronized (this)
    	{
    		if (!Config.GetBool("HtcCompass"))
    			return false;
    		if (CompassHeading < 0) 
    			return false;	// kein Kompass Wert -> Komapass nicht verwenden!
    		if ((location != null) && location.hasBearing() && (SpeedOverGround() > Config.GetInt("HtcLevel")))
    			return false;	// Geschwindigkeit > 5 km/h -> GPs Kompass verwenden

    		return true;    				
    	}    
    }

    public boolean LastUsedCompass = false;  // hier wird gespeichert, ob der zuletzt ausgegebene Winkel vom Kompass kam...
    public float getHeading()
    {
    	synchronized (this)
    	{
			LastUsedCompass = false;
    		if (UseCompass())
    		{
    			LastUsedCompass = true;
    			return CompassHeading;	// Compass Heading ausgeben, wenn Geschwindigkeit klein ist
    		} else if ((location != null) && (location.hasBearing()))
	    	{
	    		// GPS Heading ausgeben, wenn Geschwindigkeit gr��er ist	    		
	    		return location.getBearing();
	    	}
    	}
    	return 0;
    }
	public double getAlt() 
	{
		return location.getAltitude();
	}
}
