package de.droidcachebox.Geocaching;

import java.io.Serializable;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.Geocaching.Cache.CacheTypes;

public class Waypoint implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 67610567646416L;

	/// Id des dazugehörigen Caches in der Datenbank von geocaching.com
    public long CacheId;

    /// Waypoint Code
    public String GcCode;

    public Coordinate Coordinate;
    /// Breitengrad
    public double Latitude() { return Coordinate.Latitude; } 

    /// Längengrad
    public double Longitude() { return Coordinate.Longitude; }

    /// Titel des Wegpunktes
    public String Title;

    /// Kommentartext
    public String Description;

    /// Art des Wegpunkts
    public CacheTypes Type;

    /// true, falls der Wegpunkt vom Benutzer erstellt wurde
    public boolean IsUserWaypoint;

    /// true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
    public boolean IsSyncExcluded;

    /// Lösung einer QTA
    public String Clue;

    
    public Waypoint()
    {
        CacheId = -1;
        GcCode = "";
        Coordinate = new Coordinate();
        Description = "";
    }
    
    private int checkSum = 0;   // for replication

    public Waypoint(Cursor reader)
    {
        GcCode = reader.getString(0);
        CacheId = reader.getLong(1);
        double latitude = reader.getDouble(2);
        double longitude = reader.getDouble(3);
    	Coordinate = new Coordinate(latitude, longitude);
        Description = reader.getString(4);
        Type = CacheTypes.values()[reader.getShort(5)];
        IsSyncExcluded = reader.getInt(6) == 1;
        IsUserWaypoint = reader.getInt(7) == 1;
        Clue = reader.getString(8);
        if (Clue != null) Clue = Clue.trim();
        Title = reader.getString(9).trim();
        checkSum = createCheckSum();
    }

    private int createCheckSum()
    {
        // for Replication
        String sCheckSum = GcCode;
        sCheckSum += Global.FormatLatitudeDM(Latitude());
        sCheckSum += Global.FormatLongitudeDM(Longitude());
        sCheckSum += Description;
        sCheckSum += Type.ordinal();
        sCheckSum += Clue;
        sCheckSum += Title;
        return (int)Global.sdbm(sCheckSum);
    }

    public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue, String title)
    {
        GcCode = gcCode;
        CacheId = cacheId;
        Coordinate = new Coordinate(latitude, longitude);
        Description = description;
        Type = type;
        IsSyncExcluded = true;
        IsUserWaypoint = true;
        Clue = clue;
        Title = title;
    }

    public static boolean GcCodeExists(String gcCode)
    {
        Cursor c = Database.Data.myDB.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[] { gcCode });
        try
        {
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
            	c.close();
            	return true;
            };
        }
        catch (Exception exc)
        {
            return false;
        }
        c.close();

        return false;
    }

    public static String CreateFreeGcCode(String cacheGcCode) throws Exception
    {
        String suffix = cacheGcCode.substring(2);
        String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
        String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < firstCharCandidates.length(); i++)
            for (int j = 0; j < secondCharCandidates.length(); j++)
            {
                String gcCode = firstCharCandidates.substring(i, i+1) + secondCharCandidates.substring(j, j+1) + suffix;
                if (!GcCodeExists(gcCode))
                    return gcCode;
            }
        throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
    }

    public void WriteToDatabase()
    {
        int newCheckSum = createCheckSum();
//        Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
        ContentValues args = new ContentValues();
        args.put("gccode", GcCode);
        args.put("cacheid", CacheId);
        args.put("latitude", Latitude());
        args.put("longitude", Longitude());
        args.put("description", Description);
        args.put("type", Type.ordinal());
        args.put("syncexclude", IsSyncExcluded);
        args.put("userwaypoint", IsUserWaypoint);
        args.put("clue", Clue);
        args.put("title", Title);

        try
        {
        	Database.Data.myDB.insert("Waypoint", null, args);
        	
            args = new ContentValues();
            args.put("hasUserData", true);
        	Database.Data.myDB.update("Caches", args, "Id=" + CacheId, null);
        } catch (Exception exc)
        {
        	return;
        
        }
    }


    public void UpdateDatabase()
    {
        int newCheckSum = createCheckSum();
//        Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
        if (newCheckSum != checkSum)
        {
            ContentValues args = new ContentValues();
            args.put("gccode", GcCode);
            args.put("cacheid", CacheId);
            args.put("latitude", Latitude());
            args.put("longitude", Longitude());
            args.put("description", Description);
            args.put("type", Type.ordinal());
            args.put("syncexclude", IsSyncExcluded);
            args.put("userwaypoint", IsUserWaypoint);
            args.put("clue", Clue);
            args.put("title", Title);
            try
            {
            	Database.Data.myDB.update("Waypoint", args, "CacheId=" + CacheId + " and GcCode=\"" + GcCode + "\"", null);
            } catch (Exception exc)
            {
            	return;
            
            }

            args = new ContentValues();
            args.put("hasUserData", true);
            try
            {
            Database.Data.myDB.update("Caches", args, "Id=" + CacheId, null);
            } catch (Exception exc)
            {
            	return;
            }

            checkSum = newCheckSum;
        }
    }

    public void DeleteFromDatabase()
    {
        int newCheckSum = 0;
//        Replication.WaypointDelete(CacheId, checkSum, newCheckSum, GcCode);
        try
        {
        	Database.Data.myDB.delete("Caches", "Id=" + CacheId, null);
        } catch (Exception exc)
        {
        	return;
        }
    }

    /// <summary>
    /// Entfernung von der letzten gültigen Position
    /// </summary>
    public float Distance()
    {
        Coordinate fromPos = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;

        float[] dist = new float[4];
        Location.distanceBetween(fromPos.Latitude, fromPos.Longitude, Coordinate.Latitude, Coordinate.Longitude, dist);
        return dist[0];
    }

/*
    public override int GetHashCode()
    {
        return GcCode.GetHashCode();
    }

    public override bool Equals(object obj)
    {
        if (obj.GetType() != this.GetType())
            return false;

        return ((Waypoint)obj).GcCode == this.GcCode;
    }
*/

}
