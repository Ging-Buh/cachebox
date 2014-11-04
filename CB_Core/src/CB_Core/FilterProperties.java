package CB_Core;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.Enums.CacheTypes;
import CB_Core.Types.DLong;

import com.badlogic.gdx.Gdx;

public class FilterProperties
{
	public static FilterProperties LastFilter = null;

	public static boolean isFilterSet()
	{
		return (LastFilter != null) && (!LastFilter.toString().equals("")) && (!FilterProperties.presets[0].equals(LastFilter)) && !LastFilter.isExtendsFilter();
	}

	public int Finds = 0;

	public int Own = 0;

	public int NotAvailable = 0;

	public int Archived = 0;

	public int ContainsTravelbugs = 0;

	public int Favorites = 0;

	public int ListingChanged = 0;

	public int WithManualWaypoint = 0;

	public int HasUserData;

	public float MinDifficulty = 0;

	public float MaxDifficulty = 5;

	public float MinTerrain = 0;

	public float MaxTerrain = 5;

	public float MinContainerSize = 0;

	public float MaxContainerSize = 4;

	public float MinRating = 0;

	public float MaxRating = 5;

	public static final FilterProperties[] presets = new FilterProperties[]
		{
				// All Caches 0
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}"),
				// "0,0,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// All Caches to find 1
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}"),
				// "-1,-1,-1,-1,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// Quick Cache 2
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,0.0,2.5,0.0,2.5,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,true,true,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}"),
				// "-1,-1,-1,-1,0,0,0,0,0,0.0,2.5,0.0,2.5,0.0,4.0,0.0,5.0,true,false,false,true,true,false,false,false,false,false,false,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// Fetch some Travelbugs 3
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"0,-1,-1,0,1,0,0,0,0,0.0,3.0,0.0,3.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}"),
				// "0,-1,-1,0,1,0,0,0,0,0.0,3.0,0.0,3.0,0.0,4.0,0.0,5.0,true,false,false,false,false,false,false,false,false,false,false,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// Drop off Travelbugs 4
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"0,-1,-1,0,0,0,0,0,0,0.0,3.0,0.0,3.0,2.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}"),
				// "-1,-1,0,0,0,0,0,0,0,0.0,3.0,0.0,3.0,2.0,4.0,0.0,5.0,true,false,false,false,false,false,false,false,false,false,false,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// Highlights 5
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,3.5,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}"),
				// "-1,-1,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,3.5,5.0,true,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// Favoriten
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,1,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}"),
				// "0,0,0,0,0,1,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// prepare to archive
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}"),
				// "0,0,-1,-1,0,-1,-1,-1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"),

				// Listing Changed
				new FilterProperties("{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}")
		// "0,0,0,0,0,0,0,1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,")

		};

	public boolean[] cacheTypes = new boolean[]
		{ true, true, true, true, true, true, true, true, true, true, true, true, true };

	public int[] attributesFilter = new int[]
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public ArrayList<Long> GPXFilenameIds = new ArrayList<Long>();

	public ArrayList<Long> Categories = new ArrayList<Long>();

	public String filterName = "";
	public String filterGcCode = "";
	public String filterOwner = "";

	private final String seperator = ",";
	private final String GPXseperator = "^";

	/**
	 * True, wenn diese FiletrPropertys eine Filterung nach Name, Gc-Code oder Owner enthält!
	 * 
	 * @return
	 */
	public boolean isExtendsFilter()
	{
		if (!filterName.equals("")) return true;

		if (!filterGcCode.equals("")) return true;

		if (!filterOwner.equals("")) return true;

		return false;
	}

	/**
	 * Gibt den SQL Where String dieses Filters zurück
	 * 
	 * @return
	 */
	@Override
	public String toString()
	{
		String result = "";

		try
		{

			result = String.valueOf(Finds) + seperator + String.valueOf(NotAvailable) + seperator + String.valueOf(Archived) + seperator + String.valueOf(Own) + seperator + String.valueOf(ContainsTravelbugs) + seperator + String.valueOf(Favorites) + seperator + String.valueOf(HasUserData) + seperator + String.valueOf(ListingChanged) + seperator + String.valueOf(WithManualWaypoint) + seperator + String.valueOf(MinDifficulty) + seperator + String.valueOf(MaxDifficulty) + seperator + String.valueOf(MinTerrain) + seperator + String.valueOf(MaxTerrain) + seperator + String.valueOf(MinContainerSize) + seperator + String.valueOf(MaxContainerSize) + seperator + String.valueOf(MinRating) + seperator + String.valueOf(MaxRating);

			for (int i = 0; i < cacheTypes.length; i++)
				result += seperator + String.valueOf(cacheTypes[i]);

			for (int i = 0; i < attributesFilter.length; i++)
				result += seperator + String.valueOf(attributesFilter[i]);

			String tempGPX = "";
			for (int i = 0; i <= GPXFilenameIds.size() - 1; i++)
			{
				tempGPX += GPXseperator + String.valueOf(GPXFilenameIds.get(i));
			}

			result += seperator + tempGPX;
			result += seperator + filterName;
			result += seperator + filterGcCode;
			result += seperator + filterOwner;

			String tempCategory = "";
			for (long i : Categories)
			{
				tempCategory += GPXseperator + i;
			}
			result += seperator + tempCategory;

			JSONObject json = new JSONObject();

			// add Cache properties
			json.put("caches", String.valueOf(Finds) + seperator + String.valueOf(NotAvailable) + seperator + String.valueOf(Archived) + seperator + String.valueOf(Own) + seperator + String.valueOf(ContainsTravelbugs) + seperator + String.valueOf(Favorites) + seperator + String.valueOf(HasUserData) + seperator + String.valueOf(ListingChanged) + seperator + String.valueOf(WithManualWaypoint) + seperator + String.valueOf(MinDifficulty) + seperator + String.valueOf(MaxDifficulty) + seperator + String.valueOf(MinTerrain) + seperator + String.valueOf(MaxTerrain) + seperator + String.valueOf(MinContainerSize) + seperator + String.valueOf(MaxContainerSize) + seperator + String.valueOf(MinRating) + seperator + String.valueOf(MaxRating));
			// add Cache Types
			String tmp = "";
			for (int i = 0; i < cacheTypes.length; i++)
			{
				if (tmp.length() > 0) tmp += seperator;
				tmp += String.valueOf(cacheTypes[i]);
			}
			json.put("types", tmp);
			// add Cache Attributes
			tmp = "";
			for (int i = 0; i < attributesFilter.length; i++)
			{
				if (tmp.length() > 0) tmp += seperator;
				tmp += String.valueOf(attributesFilter[i]);
			}
			json.put("attributes", tmp);
			// GPX Filenames
			tmp = "";
			for (int i = 0; i <= GPXFilenameIds.size() - 1; i++)
			{
				tmp += GPXseperator + String.valueOf(GPXFilenameIds.get(i));
			}
			json.put("gpxfilenameids", tmp);
			// Filter Name
			json.put("filtername", filterName);
			// Filter GCCode
			json.put("filtergc", filterGcCode);
			// Filter Owner
			json.put("filterowner", filterOwner);
			// Categories
			tmp = "";
			for (long i : Categories)
			{
				tmp += GPXseperator + i;
			}
			json.put("categories", tmp);

			result = json.toString();
		}
		catch (JSONException e)
		{

			Gdx.app.error(Tag.TAG, "", e);
		}
		return result;
	}

	public FilterProperties()
	{
	}

	public FilterProperties(String serialization)
	{
		// Try to parse as JSon
		JSONTokener tokener = new JSONTokener(serialization);
		try
		{
			JSONObject json = (JSONObject) tokener.nextValue();
			String caches = json.getString("caches");
			String[] parts = caches.split(seperator);
			int cnt = 0;
			Finds = Integer.parseInt(parts[cnt++]);
			NotAvailable = Integer.parseInt(parts[cnt++]);
			Archived = Integer.parseInt(parts[cnt++]);
			Own = Integer.parseInt(parts[cnt++]);
			ContainsTravelbugs = Integer.parseInt(parts[cnt++]);
			Favorites = Integer.parseInt(parts[cnt++]);
			HasUserData = Integer.parseInt(parts[cnt++]);
			ListingChanged = Integer.parseInt(parts[cnt++]);
			WithManualWaypoint = Integer.parseInt(parts[cnt++]);
			MinDifficulty = Float.parseFloat(parts[cnt++]);
			MaxDifficulty = Float.parseFloat(parts[cnt++]);
			MinTerrain = Float.parseFloat(parts[cnt++]);
			MaxTerrain = Float.parseFloat(parts[cnt++]);
			MinContainerSize = Float.parseFloat(parts[cnt++]);
			MaxContainerSize = Float.parseFloat(parts[cnt++]);
			MinRating = Float.parseFloat(parts[cnt++]);
			MaxRating = Float.parseFloat(parts[cnt++]);

			String types = json.getString("types");
			parts = types.split(seperator);
			cnt = 0;
			for (int i = 0; i < cacheTypes.length; i++)
				cacheTypes[i] = Boolean.parseBoolean(parts[cnt++]);

			String attributes = json.getString("attributes");
			parts = attributes.split(seperator);
			cnt = 0;
			for (int i = 0; i < attributesFilter.length; i++)
				attributesFilter[i] = Integer.parseInt(parts[cnt++]);

			String gpxfilenames = json.getString("gpxfilenameids");
			parts = gpxfilenames.split(seperator);
			cnt = 0;
			if (parts.length > cnt)
			{
				String tempGPX = parts[cnt++];
				String[] partsGPX = new String[] {};
				partsGPX = tempGPX.split(GPXseperator);
				for (int i = 1; i < partsGPX.length; i++)
				{
					GPXFilenameIds.add(Long.parseLong(partsGPX[i]));
				}
			}

			filterName = json.getString("filtername");

			filterGcCode = json.getString("filtergc");

			filterOwner = json.getString("filterowner");

			String filtercategories = json.getString("categories");
			if (filtercategories.length() > 0)
			{
				String tempGPX = filtercategories;
				String[] partsGPX = new String[] {};
				partsGPX = tempGPX.split(GPXseperator);
				for (int i = 1; i < partsGPX.length; i++)
				{
					Categories.add(Long.parseLong(partsGPX[i]));
				}
			}
			return;
		}
		catch (Exception e)
		{
			// Filter ist noch in alten Einstellungen gegeben...
		}

		try
		{
			String[] parts = serialization.split(seperator);
			int cnt = 0;
			Finds = Integer.parseInt(parts[cnt++]);
			NotAvailable = Integer.parseInt(parts[cnt++]);
			Archived = Integer.parseInt(parts[cnt++]);
			Own = Integer.parseInt(parts[cnt++]);
			ContainsTravelbugs = Integer.parseInt(parts[cnt++]);
			Favorites = Integer.parseInt(parts[cnt++]);
			HasUserData = Integer.parseInt(parts[cnt++]);
			ListingChanged = Integer.parseInt(parts[cnt++]);
			WithManualWaypoint = Integer.parseInt(parts[cnt++]);
			MinDifficulty = Float.parseFloat(parts[cnt++]);
			MaxDifficulty = Float.parseFloat(parts[cnt++]);
			MinTerrain = Float.parseFloat(parts[cnt++]);
			MaxTerrain = Float.parseFloat(parts[cnt++]);
			MinContainerSize = Float.parseFloat(parts[cnt++]);
			MaxContainerSize = Float.parseFloat(parts[cnt++]);
			MinRating = Float.parseFloat(parts[cnt++]);
			MaxRating = Float.parseFloat(parts[cnt++]);

			for (int i = 0; i < 11; i++)
				cacheTypes[i] = Boolean.parseBoolean(parts[cnt++]);

			for (int i = 0; i < attributesFilter.length; i++)
			{
				if (parts.length > cnt) attributesFilter[i] = Integer.parseInt(parts[cnt++]);
			}

			GPXFilenameIds.clear();

			if (parts.length > cnt)
			{
				String tempGPX = parts[cnt++];
				String[] partsGPX = new String[] {};
				partsGPX = tempGPX.split(GPXseperator);
				for (int i = 1; i < partsGPX.length; i++)
				{
					GPXFilenameIds.add(Long.parseLong(partsGPX[i]));
				}
			}
			if (parts.length > cnt) filterName = parts[cnt++];
			else
				filterName = "";
			if (parts.length > cnt) filterGcCode = parts[cnt++];
			else
				filterGcCode = "";
			if (parts.length > cnt) filterOwner = parts[cnt++];
			else
				filterOwner = "";

			if (parts.length > cnt)
			{
				String tempGPX = parts[cnt++];
				String[] partsGPX = new String[] {};
				partsGPX = tempGPX.split(GPXseperator);
				for (int i = 1; i < partsGPX.length; i++)
				{
					Categories.add(Long.parseLong(partsGPX[i]));
				}
			}
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "FilterProperties.FilterProperties()", exc);
		}
	}

	/**
	 * @param userName
	 *            Config.settings.GcLogin.getValue()
	 * @return
	 */
	public String getSqlWhere(String userName)
	{
		userName = userName.replace("'", "''");

		ArrayList<String> andParts = new ArrayList<String>();

		if (Finds == 1) andParts.add("Found=1");
		if (Finds == -1) andParts.add("(Found=0 or Found is null)");

		if (NotAvailable == 1) andParts.add("Available=0");
		if (NotAvailable == -1) andParts.add("Available=1");

		if (Archived == 1) andParts.add("Archived=1");
		if (Archived == -1) andParts.add("Archived=0");

		if (Own == 1) andParts.add("(Owner='" + userName + "')");
		if (Own == -1) andParts.add("(not Owner='" + userName + "')");

		if (ContainsTravelbugs == 1) andParts.add("NumTravelbugs > 0");
		if (ContainsTravelbugs == -1) andParts.add("NumTravelbugs = 0");

		if (Favorites == 1) andParts.add("Favorit=1");
		if (Favorites == -1) andParts.add("(Favorit=0 or Favorit is null)");

		if (HasUserData == 1) andParts.add("HasUserData=1");
		if (HasUserData == -1) andParts.add("(HasUserData = 0 or HasUserData is null)");

		if (ListingChanged == 1) andParts.add("ListingChanged=1");
		if (ListingChanged == -1) andParts.add("(ListingChanged=0 or ListingChanged is null)");

		if (WithManualWaypoint == 1) andParts.add(" ID in (select CacheId FROM Waypoint WHERE UserWaypoint = 1)");
		if (WithManualWaypoint == -1) andParts.add(" NOT ID in (select CacheId FROM Waypoint WHERE UserWaypoint = 1)");

		andParts.add("Difficulty >= " + String.valueOf(MinDifficulty * 2));
		andParts.add("Difficulty <= " + String.valueOf(MaxDifficulty * 2));
		andParts.add("Terrain >= " + String.valueOf(MinTerrain * 2));
		andParts.add("Terrain <= " + String.valueOf(MaxTerrain * 2));
		andParts.add("Size >= " + String.valueOf(MinContainerSize));
		andParts.add("Size <= " + String.valueOf(MaxContainerSize));
		andParts.add("Rating >= " + String.valueOf(MinRating * 100));
		andParts.add("Rating <= " + String.valueOf(MaxRating * 100));

		/*
		 * String availability = ""; if (AvailableCaches) availability += "Available=1"; if (ArchivedCaches) { if (availability.Length > 0)
		 * availability += " or "; availability += "Archived=1 or Available=0"; } if (availability.Length > 0) andParts.add("(" +
		 * availability + ")");
		 */

		String csvTypes = "";
		for (int i = 0; i < cacheTypes.length; i++)
		{
			String value;

			if (i == 11) value = "21";// Like Munzee
			else if (i == 12) value = "22";// Like Giga
			else
				value = String.valueOf(i);

			if (cacheTypes[i]) csvTypes += value + ",";
		}

		if (csvTypes.length() > 0)
		{
			csvTypes = csvTypes.substring(0, csvTypes.length() - 1);
			andParts.add("Type in (" + csvTypes + ")");
		}

		// Attributes test = Attributes.Offroad;

		for (int i = 0; i < attributesFilter.length; i++)
		{
			if (attributesFilter[i] != 0)
			{
				if (i < 62)
				{
					long shift = DLong.UL1 << (i + 1);

					if (attributesFilter[i] == 1) andParts.add("(AttributesPositive & " + shift + ") > 0");
					else
						andParts.add("(AttributesNegative &  " + shift + ") > 0");
				}
				else
				{
					long shift = DLong.UL1 << (i - 62);

					if (attributesFilter[i] == 1) andParts.add("(AttributesPositiveHigh &  " + shift + ") > 0");
					else
						andParts.add("(AttributesNegativeHigh & " + shift + ") > 0");
				}
			}
		}

		if (GPXFilenameIds.size() != 0)
		{
			String s = "";
			for (long id : GPXFilenameIds)
			{
				s += String.valueOf(id) + ",";
			}

			s += "-1";
			andParts.add("GPXFilename_Id not in (" + s + ")");
		}

		if (filterName != "")
		{
			andParts.add("Name like '%" + filterName + "%'");
		}
		if (filterGcCode != "")
		{
			andParts.add("GcCode like '%" + filterGcCode + "%'");
		}
		if (filterOwner != "")
		{
			andParts.add("( PlacedBy like '%" + filterOwner + "%' or Owner like '%" + filterOwner + "%' )");
		}

		return join(" and ", andParts);

	}

	public static String join(String separator, ArrayList<String> array)
	{
		String retString = "";

		int count = 0;
		for (String tmp : array)
		{
			retString += tmp;
			count++;
			if (count < array.size()) retString += separator;
		}
		return retString;
	}

	/**
	 * Filter miteinander vergleichen wobei Category Einstellungen ignoriert werden sollen
	 * 
	 * @param filter
	 * @return
	 */
	public boolean equals(FilterProperties filter)
	{
		if (Finds != filter.Finds) return false;
		if (NotAvailable != filter.NotAvailable) return false;
		if (Archived != filter.Archived) return false;
		if (Own != filter.Own) return false;
		if (ContainsTravelbugs != filter.ContainsTravelbugs) return false;
		if (Favorites != filter.Favorites) return false;
		if (HasUserData != filter.HasUserData) return false;
		if (ListingChanged != filter.ListingChanged) return false;
		if (WithManualWaypoint != filter.WithManualWaypoint) return false;
		if (MinDifficulty != filter.MinDifficulty) return false;
		if (MaxDifficulty != filter.MaxDifficulty) return false;
		if (MinTerrain != filter.MinTerrain) return false;
		if (MaxTerrain != filter.MaxTerrain) return false;
		if (MinContainerSize != filter.MinContainerSize) return false;
		if (MaxContainerSize != filter.MaxContainerSize) return false;
		if (MinRating != filter.MinRating) return false;
		if (MaxRating != filter.MaxRating) return false;

		for (int i = 0; i < cacheTypes.length; i++)
		{
			if (filter.cacheTypes.length <= i) break;
			if (filter.cacheTypes[i] != this.cacheTypes[i]) return false; // nicht gleich!!!
		}
		for (int i = 0; i < attributesFilter.length; i++)
		{
			if (filter.attributesFilter.length <= i) break;
			if (filter.attributesFilter[i] != this.attributesFilter[i]) return false; // nicht gleich!!!
		}
		if (GPXFilenameIds.size() != filter.GPXFilenameIds.size()) return false;
		for (Long gid : GPXFilenameIds)
		{
			if (!filter.GPXFilenameIds.contains(gid)) return false;
		}
		if (!filterOwner.equals(filter.filterOwner)) return false;
		if (!filterGcCode.equals(filter.filterGcCode)) return false;
		if (!filterName.equals(filter.filterName)) return false;

		return true;
	}

	public void setCachtypes(ArrayList<CacheTypes> types)
	{
		Arrays.fill(cacheTypes, false);
		for (CacheTypes type : types)
		{
			int TypeIndex = type.ordinal();
			if (type == CacheTypes.Munzee) TypeIndex = 11;
			if (type == CacheTypes.Giga) TypeIndex = 12;
			if (TypeIndex < 0 || TypeIndex > 12) continue;
			cacheTypes[TypeIndex] = true;
		}

	}
}