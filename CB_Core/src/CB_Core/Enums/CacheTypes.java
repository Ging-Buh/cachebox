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
	Munzee, // 21
	Giga, // 22
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
		if (string.toLowerCase().contains("unknown")) string = "Mystery";
		if (string.toLowerCase().contains("multicache")) string = "Multi";
		if (string.toLowerCase().contains("whereigo")) // note the additional "e"
		string = "Wherigo";
		if (string.toLowerCase().contains("other")) string = "Mystery";
		if (string.toLowerCase().contains("earthcache")) string = "Earth";
		if (string.toLowerCase().contains("webcam")) string = "Camera";
		if (string.toLowerCase().contains("question")) string = "MultiQuestion";
		if (string.toLowerCase().contains("reference")) string = "ReferencePoint";
		if (string.toLowerCase().contains("parking")) string = "ParkingArea";
		if (string.toLowerCase().contains("stages")) string = "MultiStage";
		if (string.toLowerCase().contains("munzee")) string = "Munzee";
		if (string.toLowerCase().contains("mega")) string = "MegaEvent";
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