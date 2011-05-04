package de.droidcachebox.Locator;

import android.location.Location;
import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Geocaching.Coordinate;

public class Locator {
    private Location Location = null;
    public void setLocation(Location value)
    {
    	synchronized (this)
    	{
    		Location = value;
    	}
    }
    public Location getLocation()
    {
    	synchronized (this)
    	{
    		return Location;
    	}
    }
    /// <summary>
    /// Aktuelle Position des Empfängers
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
    	if ((Location != null) && (Location.hasSpeed()))
    	{
    		return Location.getSpeed() * 3600 / 1000;    			
    	} else
    		return 0;
    }
    public String SpeedString()
    {
    	if ((Location != null) && (Location.hasSpeed()))
    		return UnitFormatter.SpeedString(Global.Locator.SpeedOverGround());
    	else
    		return "-----";
    }
    
    public Locator()
    {
    	this.Location = null;
    }
    
    public boolean UseCompass()
    {
    	synchronized (this)
    	{
    		if (!Config.GetBool("HtcCompass"))
    			return false;
    		if (CompassHeading < 0) 
    			return false;	// kein Kompass Wert -> Komapass nicht verwenden!
    		if ((Location != null) && Location.hasBearing() && (SpeedOverGround() > Config.GetInt("HtcLevel")))
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
    		} else if ((Location != null) && (Location.hasBearing()))
	    	{
	    		// GPS Heading ausgeben, wenn Geschwindigkeit größer ist	    		
	    		return Location.getBearing();
	    	}
    	}
    	return 0;
    }
}
