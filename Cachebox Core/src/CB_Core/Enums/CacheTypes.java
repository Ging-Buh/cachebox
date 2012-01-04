package CB_Core.Enums;

public enum CacheTypes
{
	Traditional, // = 0,
	Multi, // = 1,
	Mystery, // = 2,
	Camera, // = 3,
	Earth, // = 4,
	Event, // = 5,
	MegaEvent, // = 6,
	CITO, // = 7,
	Virtual, // = 8,
	Letterbox, // = 9,
	Wherigo, // = 10,
	ReferencePoint, // = 11,
	Wikipedia, // = 12,
	Undefined, // = 13,
	MultiStage, // = 14,
	MultiQuestion, // = 15,
	Trailhead, // = 16,
	ParkingArea, // = 17,
	Final, // = 18,
	Cache, // = 19,
	MyParking, // = 20
	;

	/**
	 * @param string
	 * @return
	 */
	public static CacheTypes parseString(String string)
	{
		// Remove trailing " cache" or " hybrid" fragments
		if (string.contains(" ")) string = string.substring(0, string.indexOf(" "));
		// Remove trailing "-cache" fragments
		if (string.contains("-")) string = string.substring(0, string.indexOf("-"));

		// Replace some opencaching.de / geotoad cache types
		if (string.toLowerCase().equals("unknown")) string = "Mystery";
		if (string.toLowerCase().equals("multicache")) string = "Multi";
		if (string.toLowerCase().equals("whereigo")) // note the additional "e"
		string = "Wherigo";
		if (string.toLowerCase().equals("other")) string = "Mystery";
		if (string.toLowerCase().equals("earthcache")) string = "Earth";
		if (string.toLowerCase().equals("webcam")) string = "Camera";
		if (string.toLowerCase().equals("question")) string = "MultiQuestion";
		if (string.toLowerCase().equals("reference")) string = "ReferencePoint";
		if (string.toLowerCase().equals("parking")) string = "ParkingArea";
		if (string.toLowerCase().equals("stages")) string = "MultiStage";

		// If no cache type is given, use "Unknown"
		if (string.length() == 0) string = "Unknown";

		try
		{
			return valueOf(string);
		}
		catch (Exception ex)
		{
			CacheTypes cacheType = CacheTypes.Undefined;
			Boolean blnCacheTypeFound = false;
			for (CacheTypes ct : CacheTypes.values())
			{
				if (ct.toString().toLowerCase().equals(string.toLowerCase()))
				{
					cacheType = ct;
					blnCacheTypeFound = true;
				}
			}
			if (!blnCacheTypeFound) System.out.println("Handle cache type: " + string);
			return cacheType;
		}
	}
}