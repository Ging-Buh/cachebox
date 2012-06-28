package CB_Core.Locator;

import CB_Core.Config;
import CB_Core.UnitFormatter;
import CB_Core.Types.Coordinate;

public class Locator
{
	private boolean hasSpeed = false;
	private float speed = 0;
	private boolean hasBearing = false;
	private float bearing = 0;
	private float altitude = 0;

	private String ProviderString = "?";

	public void setLocation(double latitude, double longitude, float accuracy, boolean hasSpeed, float speed, boolean hasBearing,
			float bearing, double altitude, String providerString)
	{
		synchronized (this)
		{
			Position = new Coordinate(latitude, longitude, (int) accuracy);
			this.hasSpeed = hasSpeed;
			this.speed = speed;
			this.hasBearing = hasBearing;
			this.bearing = bearing;
			this.altitude = (float) altitude;
			this.ProviderString = providerString;
		}
	}

	// / <summary>
	// / Aktuelle Position des Empfängers
	// / </summary>
	private Coordinate Position = new Coordinate();

	public String ProviderString()
	{
		return ProviderString;
	}

	public Coordinate getLocation()
	{
		return Position;
	}

	public boolean isGPSprovided()
	{
		return (ProviderString.equalsIgnoreCase("GPS"));
	}

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
		if (hasSpeed)
		{
			return speed * 3600 / 1000;
		}
		else
			return 0;
	}

	public String SpeedString()
	{
		if (hasSpeed) return UnitFormatter.SpeedString(SpeedOverGround());
		else
			return "-----";
	}

	public Locator()
	{
		Position = null;
	}

	public boolean UseCompass()
	{
		synchronized (this)
		{
			if (!Config.settings.HtcCompass.getValue()) return false;
			if (CompassHeading < 0) return false; // kein Kompass Wert -> Komapass nicht verwenden!

			// Geschwindigkeit > 5 km/h -> GPs Kompass verwenden
			if (hasBearing && speed > Config.settings.HtcLevel.getValue()) return false;

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
			else if (hasBearing)
			{
				// GPS Heading ausgeben, wenn Geschwindigkeit größer ist
				return bearing;
			}
		}
		return 0;
	}

	public double altCorrection = 0;

	public double getAlt()
	{
		return altitude - altCorrection;
	}

	public String getAltString()
	{
		String result = String.format("%.0f", getAlt()) + " m";
		if (altCorrection > 0) result += " (+" + String.format("%.0f", altCorrection) + " m)";
		else if (altCorrection < 0) result += " (" + String.format("%.0f", altCorrection) + " m)";
		return result;
	}

}
