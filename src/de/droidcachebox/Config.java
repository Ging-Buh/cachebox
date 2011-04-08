package de.droidcachebox;

public class Config {

	public static String GetString(String key)
	{
		if (key == "DescriptionImageFolder")
			return "/sdcard/program files/cachebox/repository/images";
		else if (key == "TileCacheFolder")
			return "/sdcard/program files/cachebox/cache";
		else if (key == "MapPackFolder")
			return "/sdcard/program files/cachebox/repository/maps";
		else if (key == "CurrentMapLayer")
			return "Hubermedia Bavaria";
		return "";			
	}
	
	public static double GetDouble(String key)
	{
		if (key == "MapInitLatitude")
			return 48.124258;
		else if (key == "MapInitLongitude")
			return 12.164580;
		return 0;
	}
	
	public static boolean GetBool(String key)
	{
		if (key == "ImperialUnits")
			return false;
		return false;
	}
	
	public static int GetInt(String key)
	{
		if (key == "OsmMinLevel")
			return 8;
		else if (key == "OsmMaxLevel")
			return 19;
		return 0;	  
	}
	public static void Set(String key, String value)
	{
	
	}
	
	public static void AcceptChanges()
	{
		
	}
}

