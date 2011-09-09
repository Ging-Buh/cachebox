package CB_Core;

import java.util.ArrayList;
import java.util.HashMap;

import CB_Core.Config;
import CB_Core.Enums.Attributes;
import CB_Core.Log.Logger;



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

    public ArrayList<Long> GPXFilenameIds = new ArrayList<Long>();

    public ArrayList<Long> Categories = new ArrayList<Long>();

    public String filterName = "";
    public String filterGcCode = "";
    public String filterOwner = "";

    final String seperator = ",";
    final String GPXseperator = "^";

    
    /**
     * True, wenn diese FiletrPropertys eine Filterung nach
     * Name, Gc-Code oder Owner enthält!
     * @return
     */
    public boolean isExtendsFilter()
    {
    	if(!filterName.equals(""))
    		return true;
    	
    	if(!filterGcCode.equals(""))
    		return true;
    	
    	if(!filterOwner.equals(""))
    		return true;
    	
    	return false;
    }
    
    /**
     * Gibt den SQL Where String dieses Filters zurück
     * @return
     */
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

        String tempCategory = "";
        for (long i : Categories)
        {
            tempCategory += GPXseperator + i;
        }
        result += seperator + tempCategory;

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
	                GPXFilenameIds.add(Long.parseLong(partsGPX[i]));
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
            
            
            if (parts.length > cnt)
	        {
	            String tempGPX = parts[cnt++];
	            String[] partsGPX = new String[]{};
	            partsGPX= tempGPX.split(GPXseperator);
	            for (int i = 1; i < partsGPX.length; i++)
	            {
	                Categories.add(Long.parseLong(partsGPX[i]));
	            }
            }
        }
        catch (Exception exc)
        {
        	Logger.Error("FilterProperties.FilterProperties()","",exc);
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
            
            // Attributes test = Attributes.Offroad;

            HashMap<Integer, Attributes> attributeLookup;
            attributeLookup = new HashMap<Integer, Attributes>();
            attributeLookup.put(0, Attributes.Dogs);
            attributeLookup.put(1, Attributes.Bicycles);
            attributeLookup.put(2, Attributes.Motorcycles);
            attributeLookup.put(3, Attributes.Quads);
            attributeLookup.put(4, Attributes.Offroad);
            attributeLookup.put(5, Attributes.Snowmobiles);
            attributeLookup.put(6, Attributes.Horses);
            attributeLookup.put(7, Attributes.Campfires);
            attributeLookup.put(8, Attributes.TruckDriver);

            attributeLookup.put(9, Attributes.Fee);
            attributeLookup.put(10, Attributes.ClimbingGear);
            attributeLookup.put(11, Attributes.Boat);
            attributeLookup.put(12, Attributes.Scuba);
            attributeLookup.put(13, Attributes.Flashlight);
            attributeLookup.put(14, Attributes.UVLight);
            attributeLookup.put(15, Attributes.Snowshoes);
            attributeLookup.put(16, Attributes.CrossCountrySkiis);
            attributeLookup.put(17, Attributes.SpecialTool);

            attributeLookup.put(18, Attributes.Kids);
            attributeLookup.put(19, Attributes.TakesLess);
            attributeLookup.put(20, Attributes.ScenicView);
            attributeLookup.put(21, Attributes.SignificantHike);
            attributeLookup.put(22, Attributes.Climbing);
            attributeLookup.put(23, Attributes.Wading);
            attributeLookup.put(24, Attributes.Swimming);
            attributeLookup.put(25, Attributes.Anytime);
            attributeLookup.put(26, Attributes.Night);
            attributeLookup.put(27, Attributes.Winter);
            attributeLookup.put(28, Attributes.Stealth);
            attributeLookup.put(29, Attributes.NeedsMaintenance);
            attributeLookup.put(30, Attributes.Livestock);
            attributeLookup.put(31, Attributes.FieldPuzzle);
            attributeLookup.put(32, Attributes.NightCache);
            attributeLookup.put(33, Attributes.ParkAndGrab);
            attributeLookup.put(34, Attributes.AbandonedStructure);
            attributeLookup.put(35, Attributes.ShortHike);
            attributeLookup.put(36, Attributes.MediumHike);
            attributeLookup.put(37, Attributes.LongHike);

            attributeLookup.put(38, Attributes.PoisonPlants);
            attributeLookup.put(39, Attributes.Snakes);
            attributeLookup.put(40, Attributes.Ticks);
            attributeLookup.put(41, Attributes.AbandonedMines);
            attributeLookup.put(42, Attributes.Cliff);
            attributeLookup.put(43, Attributes.Hunting);
            attributeLookup.put(44, Attributes.Dangerous);
            attributeLookup.put(45, Attributes.Thorns);

            attributeLookup.put(46, Attributes.WheelchairAccessible);
            attributeLookup.put(47, Attributes.Parking);
            attributeLookup.put(48, Attributes.PublicTransportation);
            attributeLookup.put(49, Attributes.Drinking);
            attributeLookup.put(50, Attributes.Restrooms);
            attributeLookup.put(51, Attributes.Telephone);
            attributeLookup.put(52, Attributes.Picnic);
            attributeLookup.put(53, Attributes.Camping);
            attributeLookup.put(54, Attributes.Stroller);
            attributeLookup.put(55, Attributes.FuelNearby);
            attributeLookup.put(56, Attributes.FoodNearby);

            for (int i = 0; i < attributesFilter.length; i++)
            {
                if (attributesFilter[i] != 0)
                {
                	long value = Attributes.GetAttributeIndex(attributeLookup.get(i));
                	
                    if (attributesFilter[i] == 1)
                        andParts.add("AttributesPositive & " + value + " > 0");
                    else
                        andParts.add("AttributesNegative & " + value + " > 0");
                }
                
            }

            if (GPXFilenameIds.size() != 0)
            {
                String s = "";
                for(long id : GPXFilenameIds)
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