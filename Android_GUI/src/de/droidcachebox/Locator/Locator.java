package de.droidcachebox.Locator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.Types.Coordinate;
import android.location.Location;
import android.location.LocationManager;

public class Locator
{
	private Location location = null;

	public void setLocation(Location value)
	{
		synchronized (this)
		{
			location = value;
			Position = new Coordinate(value.getLatitude(), value.getLongitude(), ((int) value.getAccuracy()));
		}
	}

	public Location getLocation()
	{
		synchronized (this)
		{
			return location;
		}
	}

	// / <summary>
	// / Aktuelle Position des Empfängers
	// / </summary>
	public Coordinate Position = new Coordinate();

	// / <summary>
	// / Aktueller Winkel des mag. Kompass
	// / </summary>
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
		}
		else
			return 0;
	}

	public String SpeedString()
	{
		if ((location != null) && (location.hasSpeed())) return UnitFormatter.SpeedString(GlobalCore.Locator.SpeedOverGround());
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
			if (!Config.settings.HardwareCompass.getValue()) return false;
			if (CompassHeading < 0) return false; // kein Kompass Wert -> Komapass nicht verwenden!

			// Geschwindigkeit > 5 km/h -> GPs Kompass verwenden
			if ((location != null) && location.hasBearing() && (SpeedOverGround() > Config.settings.HardwareCompassLevel.getValue())) return false;

			return true;
		}
	}

	/**
	 * hier wird gespeichert, ob der zuletzt ausgegebene Winkel vom Kompass kam...
	 */
	public boolean LastUsedCompass = false;

	public float getHeading()
	{
		synchronized (this)
		{
			LastUsedCompass = false;
			if (UseCompass())
			{
				LastUsedCompass = true;
				return CompassHeading; // Compass Heading ausgeben, wenn
										// Geschwindigkeit klein ist
			}
			else if ((location != null) && (location.hasBearing()))
			{
				// GPS Heading ausgeben, wenn Geschwindigkeit größer ist
				return location.getBearing();
			}
		}
		return 0;
	}

	public double altCorrection = 0;

	public double getAlt()
	{
		return location.getAltitude() - altCorrection;
	}

	public String getAltString()
	{
		String result = String.format("%.0f", getAlt()) + " m";
		if (altCorrection > 0) result += " (+" + String.format("%.0f", altCorrection) + " m)";
		else if (altCorrection < 0) result += " (" + String.format("%.0f", altCorrection) + " m)";
		return result;
	}

	public boolean isGPSprovided()
	{
		return (location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER));
	}

	public String ProviderString()
	{
		if (isGPSprovided())
		{
			return "GPS";
		}
		else
		{
			return "Cell";
		}
	}
}
