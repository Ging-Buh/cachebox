package de.droidcachebox;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Cache.Attributes;

public class FilterProperties
{
    public int Finds = 0;

    public int Own = 0;

    public int NotAvailable = 0;

    public int Archived = 0;

    public int ContainsTravelbugs = 0;

    public int Favorites = 0;

    public int ListingChanged = 0;

    public int WithManualWaypoint = 0;

    public int HasUserData;

    public float MinDifficulty = 1;
    
    public float MaxDifficulty = 5;
    
    public float MinTerrain = 1;
    
    public float MaxTerrain = 5;
    
    public float MinContainerSize = 0;

    public float MaxContainerSize = 4;

    public float MinRating = 0;

    public float MaxRating = 5;

    public boolean[] cacheTypes = new boolean[] { true, true, true, true, true, true, true, true, true, true, true };

    public int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public ArrayList<Integer> GPXFilenameIds = new ArrayList<Integer>();

    public String filterName = "";
    public String filterGcCode = "";
    public String filterOwner = "";

    final String seperator = ",";
    final String GPXseperator = "^";

    public String ToString()
    {
        String result = 
        	String.valueOf(Finds) + seperator +
        	String.valueOf(NotAvailable) + seperator +
        	String.valueOf(Archived) + seperator +
        	String.valueOf(Own) + seperator +
        	String.valueOf(ContainsTravelbugs) + seperator +
        	String.valueOf(Favorites) + seperator +                
        	String.valueOf(HasUserData) + seperator +
        	String.valueOf(ListingChanged) + seperator +
        	String.valueOf(WithManualWaypoint) + seperator +
        	String.valueOf(MinDifficulty) + seperator +
        	String.valueOf(MaxDifficulty) + seperator + String.valueOf(MinTerrain) + seperator + String.valueOf(MaxTerrain) + seperator +
        	String.valueOf(MinContainerSize) + seperator + String.valueOf(MaxContainerSize) + seperator +
        	String.valueOf(MinRating) + seperator +
        	String.valueOf(MaxRating);

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


        return result;
    }

    /*public override boolean Equals(object obj)
    {
        if (obj.GetType() != this.GetType())
            return false;

        return (obj as FilterProperties == this.ToString();
    }*/

    public FilterProperties() { }

    public FilterProperties(String serialization)
    {
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
                attributesFilter[i] = Integer.parseInt(parts[cnt++]);

            GPXFilenameIds.clear();
            
            if(parts.length > cnt)
	        {
	            String tempGPX = parts[cnt++];
	            String[] partsGPX = new String[]{};
	            partsGPX= tempGPX.split(GPXseperator);
	            for (int i = 1; i < partsGPX.length; i++)
	            {
	                GPXFilenameIds.add(Integer.parseInt(partsGPX[i]));
	            }
            }
            if (parts.length > cnt)
                filterName = parts[cnt++];
            else
                filterName = "";
            if (parts.length > cnt)
                filterGcCode = parts[cnt++];
            else
                filterGcCode = "";
            if (parts.length > cnt)
                filterOwner = parts[cnt++];
            else
                filterOwner = "";
        }
        catch (Exception exc)
        {
        	if(exc!=null)
        	{
        		Global.AddLog("FilterProperties Construct - " + exc.getMessage());
        	}
        		
        }
    }

    public String getSqlWhere()
    {
           ArrayList<String> andParts = new ArrayList<String>();

            if (Finds == 1)
                andParts.add("Found=1");
            if (Finds == -1)
                andParts.add("(Found=0 or Found is null)");

            if (NotAvailable == 1)
                andParts.add("Available=0");
            if (NotAvailable == -1)
                andParts.add("Available=1");

            if (Archived == 1)
                andParts.add("Archived=1");
            if (Archived == -1)
                andParts.add("Archived=0");

            if (Own == 1)
                andParts.add("(Owner='" + Config.GetString("GcLogin").replace("'", "''") + "')");
            if (Own == -1)
                andParts.add("(not Owner='" + Config.GetString("GcLogin").replace("'", "''") + "')");

            if (ContainsTravelbugs == 1)
                andParts.add("NumTravelbugs > 0");
            if (ContainsTravelbugs == -1)
                andParts.add("NumTravelbugs = 0");

            if (Favorites == 1)
                andParts.add("Favorit=1");
            if (Favorites == -1)
                andParts.add("(Favorit=0 or Favorit is null)");
            
            if (HasUserData == 1)
                andParts.add("HasUserData=1");
            if (HasUserData == -1)
                andParts.add("(HasUserData = 0 or HasUserData is null)");

            if (ListingChanged == 1)
                andParts.add("ListingChanged=1");
            if (ListingChanged == -1)
                andParts.add("(ListingChanged=0 or ListingChanged is null)");

            if (WithManualWaypoint == 1)
                andParts.add(String.format(" ID in ({0})", "select CacheId FROM Waypoint WHERE UserWaypoint = 1"));
            if (WithManualWaypoint == -1)
                andParts.add(String.format(" NOT ID in ({0})", "select CacheId FROM Waypoint WHERE UserWaypoint = 1"));

            andParts.add("Difficulty >= " + String.valueOf(MinDifficulty * 2));
            andParts.add("Difficulty <= " + String.valueOf(MaxDifficulty * 2));
            andParts.add("Terrain >= " + String.valueOf(MinTerrain * 2));
            andParts.add("Terrain <= " + String.valueOf(MaxTerrain * 2));
            andParts.add("Size >= " + String.valueOf(MinContainerSize));
            andParts.add("Size <= " + String.valueOf(MaxContainerSize));
            andParts.add("Rating >= " + String.valueOf(MinRating * 100));
            andParts.add("Rating <= " + String.valueOf(MaxRating * 100));

            /*
            String availability = "";
            if (AvailableCaches)
                availability += "Available=1";

            if (ArchivedCaches)
            {
                if (availability.Length > 0)
                    availability += " or ";
                availability += "Archived=1 or Available=0";
            }

            if (availability.Length > 0)
                andParts.add("(" + availability + ")");
            */

            String csvTypes = "";
            for (int i=0; i<11; i++)
                if (cacheTypes[i])
                    csvTypes += String.valueOf(i)+",";

            if (csvTypes.length() > 0)
            {
                csvTypes = csvTypes.substring(0, csvTypes.length() - 1);
                andParts.add("Type in (" + csvTypes + ")");
            }
            
            Attributes test = Cache.Attributes.Offroad;

            HashMap<Integer, Attributes> attributeLookup;
            attributeLookup = new HashMap<Integer, Attributes>();
            attributeLookup.put(0, Cache.Attributes.Dogs);
            attributeLookup.put(1, Cache.Attributes.Bicycles);
            attributeLookup.put(2, Cache.Attributes.Motorcycles);
            attributeLookup.put(3, Cache.Attributes.Quads);
            attributeLookup.put(4, Cache.Attributes.Offroad);
            attributeLookup.put(5, Cache.Attributes.Snowmobiles);
            attributeLookup.put(6, Cache.Attributes.Horses);
            attributeLookup.put(7, Cache.Attributes.Campfires);
            attributeLookup.put(8, Cache.Attributes.TruckDriver);

            attributeLookup.put(9, Cache.Attributes.Fee);
            attributeLookup.put(10, Cache.Attributes.ClimbingGear);
            attributeLookup.put(11, Cache.Attributes.Boat);
            attributeLookup.put(12, Cache.Attributes.Scuba);
            attributeLookup.put(13, Cache.Attributes.Flashlight);
            attributeLookup.put(14, Cache.Attributes.UVLight);
            attributeLookup.put(15, Cache.Attributes.Snowshoes);
            attributeLookup.put(16, Cache.Attributes.CrossCountrySkiis);
            attributeLookup.put(17, Cache.Attributes.SpecialTool);

            attributeLookup.put(18, Cache.Attributes.Kids);
            attributeLookup.put(19, Cache.Attributes.TakesLess);
            attributeLookup.put(20, Cache.Attributes.ScenicView);
            attributeLookup.put(21, Cache.Attributes.SignificantHike);
            attributeLookup.put(22, Cache.Attributes.Climbing);
            attributeLookup.put(23, Cache.Attributes.Wading);
            attributeLookup.put(24, Cache.Attributes.Swimming);
            attributeLookup.put(25, Cache.Attributes.Anytime);
            attributeLookup.put(26, Cache.Attributes.Night);
            attributeLookup.put(27, Cache.Attributes.Winter);
            attributeLookup.put(28, Cache.Attributes.Stealth);
            attributeLookup.put(29, Cache.Attributes.NeedsMaintenance);
            attributeLookup.put(30, Cache.Attributes.Livestock);
            attributeLookup.put(31, Cache.Attributes.FieldPuzzle);
            attributeLookup.put(32, Cache.Attributes.NightCache);
            attributeLookup.put(33, Cache.Attributes.ParkAndGrab);
            attributeLookup.put(34, Cache.Attributes.AbandonedStructure);
            attributeLookup.put(35, Cache.Attributes.ShortHike);
            attributeLookup.put(36, Cache.Attributes.MediumHike);
            attributeLookup.put(37, Cache.Attributes.LongHike);

            attributeLookup.put(38, Cache.Attributes.PoisonPlants);
            attributeLookup.put(39, Cache.Attributes.Snakes);
            attributeLookup.put(40, Cache.Attributes.Ticks);
            attributeLookup.put(41, Cache.Attributes.AbandonedMines);
            attributeLookup.put(42, Cache.Attributes.Cliff);
            attributeLookup.put(43, Cache.Attributes.Hunting);
            attributeLookup.put(44, Cache.Attributes.Dangerous);
            attributeLookup.put(45, Cache.Attributes.Thorns);

            attributeLookup.put(46, Cache.Attributes.WheelchairAccessible);
            attributeLookup.put(47, Cache.Attributes.Parking);
            attributeLookup.put(48, Cache.Attributes.PublicTransportation);
            attributeLookup.put(49, Cache.Attributes.Drinking);
            attributeLookup.put(50, Cache.Attributes.Restrooms);
            attributeLookup.put(51, Cache.Attributes.Telephone);
            attributeLookup.put(52, Cache.Attributes.Picnic);
            attributeLookup.put(53, Cache.Attributes.Camping);
            attributeLookup.put(54, Cache.Attributes.Stroller);
            attributeLookup.put(55, Cache.Attributes.FuelNearby);
            attributeLookup.put(56, Cache.Attributes.FoodNearby);

            for (int i = 0; i < attributesFilter.length; i++)
            {
                if (attributesFilter[i] != 0)
                {
                	long value = Cache.GetAttributeIndex(attributeLookup.get(i));
                	
                    if (attributesFilter[i] == 1)
                        andParts.add("AttributesPositive & " + value + " > 0");
                    else
                        andParts.add("AttributesNegative & " + value + " > 0");
                }
                
            }

            if (GPXFilenameIds.size() != 0)
            {
                String s = "";
                for(int id : GPXFilenameIds)
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

	public static String join(String separator , ArrayList<String> array) 
	{
		String retString="";
		
		int count =0;
		for (String tmp : array)
		{
			retString += tmp ;
			count++;
			if (count<array.size())retString +=separator;
		}
		return retString;
	}

   /* public override int GetHashCode()
    {
        return SqlWhere.GetHashCode();
    }*/
}