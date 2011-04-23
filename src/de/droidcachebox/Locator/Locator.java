package de.droidcachebox.Locator;

import android.location.Location;
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
    /// Letzte gültige Position des Empfängers
    /// </summary>
    public Coordinate LastValidPosition = new Coordinate();

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
    
    public Locator()
    {
    	this.Location = null;
    }
    
    public boolean UseCompass()
    {
    	synchronized (this)
    	{
    		if (CompassHeading < 0) 
    			return false;	// kein Kompass Wert -> Komapass nicht verwenden!
    		if ((Location != null) && Location.hasBearing() && Location.hasSpeed() && (Location.getSpeed() > 5 * 1000 / 3600))
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
