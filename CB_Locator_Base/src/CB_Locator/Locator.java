package CB_Locator;

import java.util.Date;

import CB_Locator.Location.ProviderType;
import CB_Locator.Events.GPS_FallBackEventList;
import CB_Locator.Events.PositionChangedEventList;

/**
 * @author Longri
 */
public class Locator
{
	/**
	 * @author Longri
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
		Magnetic,

		any
	};

	// #################################
	// Private Static Member
	// #################################

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
	private static boolean fix = false;
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
		if (initialLocation == null) initialLocation = Location.NULL_LOCATION;
		setNewLocation(initialLocation);
	}

	public static boolean isFixed()
	{
		return fix;
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
				that.mSaveLocation = location;
				that.hasSpeed = false;
				that.speed = 0;
				break;
			case Network:
				that.mNetworkLocation = location;
				// reset Speed only if last Speed value old
				long time = new Date().getTime();
				if (that.mTimeStampSpeed + (minGpsUpdateTime * 3) < time)
				{
					that.hasSpeed = false;
					that.speed = 0;
				}

				break;
			case GPS:

				that.mFineLocation = location;
				that.mLastSavedFineLocation = location;
				that.hasSpeed = location.getHasSpeed();
				that.speed = location.getSpeed();
				that.mTimeStampSpeed = (new Date()).getTime();
				if (location.getHasBearing())
				{
					setHeading(location.getBearing(), CompassType.GPS);
				}

				if (!fix && location != null)
				{
					fix = true;
					GPS_FallBackEventList.CallFix();
				}

				break;
			default:
				break;
			}

			PositionChangedEventList.SpeedChanged();
			PositionChangedEventList.PositionChanged();
			PositionChangedEventList.OrientationChanged();
		}
	}

	/**
	 * Returns the last saved fine location (from GPS) or null !
	 * 
	 * @return
	 */
	public static Location getLastSavedFineLocation()
	{
		synchronized (that)
		{
			return that.mLastSavedFineLocation;
		}
	}

	/**
	 * Returns the last Latitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return Latitude as double
	 */
	public static double getLatitude()
	{
		return getLatitude(ProviderType.any);
	}

	/**
	 * Returns the last Latitude from the last position of the given ProviderType
	 * 
	 * @param type
	 * @return
	 */
	public static double getLatitude(ProviderType type)
	{
		return getLocation(type).getLatitude();
	}

	/**
	 * Returns the last Longitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return Longitude as double
	 */
	public static double getLongitude()
	{
		return getLongitude(ProviderType.any);
	}

	/**
	 * Returns the last Longitude from the last position of the given ProviderType
	 * 
	 * @param type
	 * @return
	 */
	public static double getLongitude(ProviderType type)
	{
		return getLocation(type).getLongitude();
	}

	/**
	 * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return
	 */
	public static Location getLocation()
	{
		return getLocation(ProviderType.any);
	}

	/**
	 * Returns the last valid position of the given ProviderType
	 */
	public static Location getLocation(ProviderType type)
	{
		synchronized (that)
		{

			if (type == ProviderType.any)
			{
				if (that.mFineLocation != null) return that.mFineLocation;
				if (that.mNetworkLocation != null) return that.mNetworkLocation;
				if (that.mSaveLocation != null) return that.mSaveLocation;
				return Location.NULL_LOCATION;
			}
			else if (type == ProviderType.GPS)
			{
				return that.mLastSavedFineLocation;
			}
			else if (type == ProviderType.Network)
			{
				return that.mNetworkLocation;
			}
			else if (type == ProviderType.Saved)
			{
				return that.mSaveLocation;
			}
			return Location.NULL_LOCATION;
		}
	}

	/**
	 * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
	 * 
	 * @return
	 */
	public static Coordinate getCoordinate()
	{
		return getLocation(ProviderType.any).toCordinate();
	}

	/**
	 * Returns True if the saved Location != ProviderType.NULL
	 * 
	 * @return
	 */
	public static boolean Valid()
	{
		return getLocation().getProviderType() == ProviderType.GPS || getLocation().getProviderType() == ProviderType.Network;
	}

	/**
	 * Returns the last valid position of the given ProviderType
	 * 
	 * @param type
	 * @return
	 */
	public static Coordinate getCoordinate(ProviderType type)
	{
		Location loc = getLocation(type);
		if (loc == null) return null;
		return loc.toCordinate();
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

	static long lastFixLose = 0;

	/**
	 * Call this if GPS state changed to no sat have a fix
	 */
	public static void FallBack2Network()
	{
		synchronized (that)
		{
			lastFixLose = System.currentTimeMillis();
			fix = false;
			that.mFineLocation = null;
		}
		GPS_FallBackEventList.CallFallBack();
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
		return getHeading(CompassType.any);
	}

	/**
	 * Returns the last saved heading of the given ProviderType
	 * 
	 * @param type
	 * @return
	 */
	public static float getHeading(CompassType type)
	{
		synchronized (that)
		{

			if (type == CompassType.GPS || !mUseMagneticCompass) return that.mlastGPSHeading;
			if (type == CompassType.Magnetic) return that.mlastGPSHeading;

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

		// set last used compass Type

		if ((that.mlastGPSHeading > -1 && SpeedOverGround() > mMagneticCompassLevel) || !mUseMagneticCompass)
		{
			that.mLastUsedCompassType = CompassType.GPS;
		}
		else
		{
			that.mLastUsedCompassType = CompassType.Magnetic;
		}

		PositionChangedEventList.OrientationChanged();
	}

	// member are private for synchronized access
	private boolean hasSpeed = false;
	/**
	 * @uml.property name="mFineLocation"
	 * @uml.associationEnd
	 */
	private Location mFineLocation;

	private Location mLastSavedFineLocation;

	/**
	 * @uml.property name="mNetworkPosition"
	 * @uml.associationEnd
	 */
	private Location mNetworkLocation;

	private Location mSaveLocation;

	/**
	 * @uml.property name="speed"
	 */
	private float speed = 0;
	private float mlastMagneticHeading = 0;
	private float mlastGPSHeading = -1;
	private long mTimeStampSpeed = (new Date().getTime());
	/**
	 * @uml.property name="mLastUsedCompassType"
	 * @uml.associationEnd
	 */
	private CompassType mLastUsedCompassType = CompassType.any;

}
