package CB_Core.Locator;

import CB_Core.Config;
import CB_Core.UnitFormatter;
import CB_Core.Events.platformConector;
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
			if (!Config.settings.HardwareCompass.getValue()) return false;
			if (platformConector.getCompassHeading() < 0) return false; // kein Kompass Wert -> Komapass nicht verwenden!

			// Geschwindigkeit > 5 km/h -> GPs Kompass verwenden
			if (hasBearing && speed > Config.settings.HardwareCompassLevel.getValue()) return false;

			return true;
		}
	}

	/**
	 * hier wird gespeichert, ob der zuletzt ausgegebene Winkel vom Kompass kam...
	 */
	private boolean LastUsedCompass = false;

	public enum CompassType
	{
		GPS, Magnetic
	};

	public boolean isLastUsedCompass(CompassType type)
	{
		if (type == CompassType.GPS)
		{
			if (LastUsedCompass) return false;
			else
				return true;
		}
		else
		{
			if (LastUsedCompass) return true;
			else
				return false;
		}
	}

	public float getHeading()
	{
		synchronized (this)
		{
			LastUsedCompass = false;
			if (UseCompass())
			{
				LastUsedCompass = true;
				return platformConector.getCompassHeading(); // Compass Heading ausgeben, wenn
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

	public String getAltStringWithCorection()
	{
		String result = getAltString();
		if (altCorrection > 0) result += " (+" + String.format("%.0f", altCorrection) + " m)";
		else if (altCorrection < 0) result += " (" + String.format("%.0f", altCorrection) + " m)";
		return result;
	}

	public String getAltString()
	{
		String result = String.format("%.0f", getAlt()) + " m";
		return result;
	}

	public void setHeading(float heading)
	{
		bearing = heading;
		hasBearing = true;

	}

	public boolean hasHeading()
	{
		return hasBearing;
	}

	public boolean hasSpeed()
	{
		return hasSpeed;
	}

	public double getSpeed()
	{
		return speed;
	}

	public String getProvider()
	{
		return ProviderString;
	}

	public void setProvider(String provider)
	{
		ProviderString = provider;
	}

}
