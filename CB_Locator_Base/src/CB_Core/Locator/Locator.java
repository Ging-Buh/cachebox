package CB_Core.Locator;

import CB_Core.Locator.Location.ProviderType;
import CB_Core.Locator.Events.GPS_FallBackEventList;

/**
 * @author Longri_2
 */
public class Locator
{
	/**
	 * @author Longri_2
	 */
	public enum CompassType
	{
		/**
		 * @uml.property name="gPS"
		 * @uml.associationEnd
		 */
		GPS, /**
		 * @uml.property name="magnetic"
		 * @uml.associationEnd
		 */
		Magnetic, /**
		 * @uml.property name="unknown"
		 * @uml.associationEnd
		 */
		unknown
	};

	// #################################
	// Private Static Member
	// #################################
	private static final int NETWORK_POSITION_TIME = 120000;

	/**
	 * @uml.property name="that"
	 * @uml.associationEnd
	 */
	private static Locator that;

	private static long minGpsUpdateTime = 125;
	private static double altCorrection = 0;
	private static boolean mUseImperialUnits = false;
	private static boolean mUseMagneticCompass = false;
	private static int mMagneticCompassLevel = 5;
	/**
	 * @uml.property name="displayOff"
	 */
	private static boolean DisplayOff = false;

	// #################################
	// Public Static Access
	// #################################

	/**
	 * Constructor </br> </br> Set initial location, maybe last saved position! </br> Or NULL for initial with 0,0 Coords!
	 * 
	 * @param initialLocation
	 *            as GPS_Location
	 */
	public Locator(Location initialLocation)
	{
		that = this;
	}

	/**
	 * Set Display Off. </br>Only events with priority High will fire!
	 */
	public static void setDisplayOff()
	{
		DisplayOff = true;
	}

	/**
	 * Set Display on. </br> All events will fire!
	 */
	public static void setDisplayOn()
	{
		DisplayOff = false;
	}

	/**
	 * Returns True if the flag for DisplayOff is True!
	 * 
	 * @return
	 * @uml.property name="displayOff"
	 */
	public static boolean isDisplayOff()
	{
		return DisplayOff;
	}

	/**
	 * Set the minimum update time for fire position changed event
	 * 
	 * @param value
	 *            as long
	 */
	public static void setMinUpdateTime(Long value)
	{
		minGpsUpdateTime = value;
	}

	/**
	 * Returns the minimum update time for firing position changed event
	 * 
	 * @return long
	 */
	public static long getMinUpdateTime()
	{
		return minGpsUpdateTime;
	}

	/**
	 * Set the speed level for using Hardware or GPS heading
	 * 
	 * @param value
	 */
	public static void setHardwareCompassLevel(int value)
	{
		mMagneticCompassLevel = value;
	}

	/**
	 * Set true if the Locator is use heading values from Hardware Compass
	 * 
	 * @param value
	 */
	public static void setUseHardwareCompass(boolean value)
	{
		mUseMagneticCompass = value;
	}

	/**
	 * Set a new location from GPS,Network or the last saved location!</br> For all given information is using the last best Location!</br>
	 * 1. Gps</br> 2. Network</br> 3. Saved</br> </br> If the last set of GPS Location older 2min, the saved FineLocation will be cleaned!
	 * (Fall back to Network or saved Location)
	 * 
	 * @param location
	 */
	public static void setNewLocation(Location location)
	{
		synchronized (that)
		{
			ProviderType type = location.getProviderType();
			switch (type)
			{
			case Saved:
				that.mLastNetworkPosition = location;
				that.hasSpeed = false;
				that.speed = 0;
				break;
			case Network:
				that.mLastNetworkPosition = location;

				// chk if last FineLocation older 2min?
				if (that.mLastFineLocation != null)
				{
					if ((java.lang.System.currentTimeMillis() - that.mLastFineLocation.getTimeStamp().getTime()) > NETWORK_POSITION_TIME)
					{
						// fall back to Network Position
						that.mLastFineLocation = null;
						that.hasSpeed = false;
						that.speed = 0;
						GPS_FallBackEventList.Call();
					}
				}
				break;
			case GPS:
				that.mLastFineLocation = location;
				that.hasSpeed = location.getHasSpeed();
				that.speed = location.getSpeed();
				if (location.getHasBearing())
				{
					setHeading(location.getBearing(), CompassType.GPS);
				}
				;
				break;
			}
		}
	}

	/**
	 * Returns the last Latitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return Latitude as double
	 */
	public static double getLatitude()
	{
		return getLocation().getLatitude();
	}

	/**
	 * Returns the last Longitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return Longitude as double
	 */
	public static double getLongitude()
	{
		return getLocation().getLongitude();
	}

	/**
	 * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return
	 */
	public static Location getLocation()
	{
		synchronized (that)
		{
			if (that.mLastFineLocation != null) return that.mLastFineLocation;
			if (that.mLastNetworkPosition != null) return that.mLastNetworkPosition;
			return Location.NULL_LOCATION;
		}
	}

	/**
	 * Set a flag, that all Units are formated as Imperial Units or not! </br> default are false
	 * 
	 * @param value
	 *            as boolean
	 */
	public static void setUseImperialUnits(boolean value)
	{
		mUseImperialUnits = value;
	}

	/**
	 * Returns the formated speed String!
	 * 
	 * @return
	 */
	public static String SpeedString()
	{
		synchronized (that)
		{
			if (that.hasSpeed) return Formatter.SpeedString(SpeedOverGround(), mUseImperialUnits);
			else
				return "-----";
		}
	}

	/**
	 * Returns the Speed as float
	 * 
	 * @return
	 */
	public static float SpeedOverGround()
	{
		synchronized (that)
		{
			if (that.hasSpeed)
			{
				return that.speed * 3600 / 1000;
			}
			else
				return 0;
		}
	}

	/**
	 * Return True if the last valid Location from Type GPS
	 * 
	 * @return
	 */
	public static boolean isGPSprovided()
	{
		return getLocation().getProviderType() == ProviderType.GPS;
	}

	/**
	 * Set the alt correction value
	 * 
	 * @param value
	 * @uml.property name="altCorrection"
	 */
	public static void setAltCorrection(double value)
	{
		altCorrection = value;
	}

	/**
	 * Returns True if the last valid Location have a speed value
	 * 
	 * @return
	 */
	public static boolean hasSpeed()
	{
		synchronized (that)
		{
			return that.hasSpeed;
		}
	}

	/**
	 * Returns the speed value of the last valid Location
	 * 
	 * @return
	 * @uml.property name="speed"
	 */
	public static double getSpeed()
	{
		synchronized (that)
		{
			return that.speed;
		}
	}

	/**
	 * Returns the altitude with correction from last valid Location
	 * 
	 * @return
	 */
	public static double getAlt()
	{
		return getLocation().getAltitude() - altCorrection;
	}

	/**
	 * Returns the formated string of last valid altitude with correction value
	 * 
	 * @return
	 */
	public static String getAltStringWithCorection()
	{
		// TODO ImperialUnits ?
		String result = getAltString();
		if (altCorrection > 0) result += " (+" + String.format("%.0f", altCorrection) + " m)";
		else if (altCorrection < 0) result += " (" + String.format("%.0f", altCorrection) + " m)";
		return result;
	}

	/**
	 * Returns the formated string of last valid altitude
	 * 
	 * @return
	 */
	public static String getAltString()
	{
		// TODO ImperialUnits ?
		String result = String.format("%.0f", getAlt()) + " m";
		return result;
	}

	/**
	 * Returns the ProviderType of the last Valid Location
	 * 
	 * @return
	 */
	public static ProviderType getProvider()
	{
		return getLocation().getProviderType();
	}

	/**
	 * Returns True if the used bearing value from magnetic compass. </br> Returns False if the bearing from GPS.
	 * 
	 * @return
	 */
	public static boolean UseMagneticCompass()
	{
		synchronized (that)
		{
			return that.mLastUsedCompassType == CompassType.Magnetic;
		}
	}

	/**
	 * Returns the last saved heading
	 * 
	 * @return
	 */
	public static float getHeading()
	{
		synchronized (that)
		{
			if (UseMagneticCompass())
			{
				return that.mlastMagneticHeading;
			}
			else
			{
				return that.mlastGPSHeading;
			}
		}
	}

	/**
	 * Set the heading from GPS or magnetic sensor
	 * 
	 * @param heading
	 * @param type
	 */
	public static void setHeading(float heading, CompassType type)
	{
		if (type == CompassType.GPS)
		{
			that.mlastGPSHeading = heading;
		}
		else
		{
			that.mlastMagneticHeading = heading;
		}
	}

	// member are private for synchronized access
	private boolean hasSpeed = false;
	/**
	 * @uml.property name="mLastFineLocation"
	 * @uml.associationEnd
	 */
	private Location mLastFineLocation;
	/**
	 * @uml.property name="mLastNetworkPosition"
	 * @uml.associationEnd
	 */
	private Location mLastNetworkPosition;
	/**
	 * @uml.property name="speed"
	 */
	private float speed = 0;
	private float mlastMagneticHeading = 0;
	private float mlastGPSHeading = 0;
	/**
	 * @uml.property name="mLastUsedCompassType"
	 * @uml.associationEnd
	 */
	private final CompassType mLastUsedCompassType = CompassType.unknown;

}
