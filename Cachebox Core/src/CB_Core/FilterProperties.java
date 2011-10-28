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

    public int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

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

           
            for (int i = 0; i < attributesFilter.length; i++)
            {
                if (attributesFilter[i] != 0)
                {
                	long value = Attributes.GetAttributeIndex(Attributes.getAttributeEnumByGcComId(i));
                	
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