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
        Coordinate.Latitude = latitude;
        Coordinate.Longitude = longitude;
        Description = description;
        Type = type;
        IsSyncExcluded = true;
        IsUserWaypoint = true;
        Clue = clue;
        Title = title;
    }
/*
    public static bool GcCodeExists(String gcCode)
    {
        SqlCeCommand command = new SqlCeCommand("select count(GcCode) from Waypoint where GcCode=@gccode", Database.Data.Connection);
        command.Parameters.Add("@gccode", DbType.String).Value = gcCode;
        int count = int.Parse(command.ExecuteScalar().ToString());
        command.Dispose();

        return count > 0;
    }

    public static String CreateFreeGcCode(String cacheGcCode)
    {
        String suffix = cacheGcCode.Substring(2);
        String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
        String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < firstCharCandidates.Length; i++)
            for (int j = 0; j < secondCharCandidates.Length; j++)
            {
                String gcCode = firstCharCandidates.Substring(i, 1) + secondCharCandidates.Substring(j, 1) + suffix;
                if (!GcCodeExists(gcCode))
                    return gcCode;
            }
        throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
    }

    public void WriteToDatabase()
    {
        int newCheckSum = createCheckSum();
        Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
        SqlCeCommand command = new SqlCeCommand("insert into Waypoint(GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title) values (@gccode, @cacheid, @latitude, @longitude, @description, @type, @syncexclude, @userwaypoint, @clue, @title)", Database.Data.Connection);
        command.Parameters.Add("@gccode", DbType.String).Value = GcCode;
        command.Parameters.Add("@cacheid", DbType.Int64).Value = CacheId;
        command.Parameters.Add("@latitude", DbType.Double).Value = Latitude;
        command.Parameters.Add("@longitude", DbType.Double).Value = Longitude;
        command.Parameters.Add("@description", DbType.String).Value = Description;
        command.Parameters.Add("@type", DbType.Int16).Value = Type;
        command.Parameters.Add("@syncexclude", DbType.Boolean).Value = IsSyncExcluded;
        command.Parameters.Add("@userwaypoint", DbType.Boolean).Value = IsUserWaypoint;
        command.Parameters.Add("@clue", DbType.String).Value = Clue;
        command.Parameters.Add("@title", DbType.String).Value = Title;
        command.ExecuteNonQuery();

        SqlCeCommand commandUserData = new SqlCeCommand("update Caches set HasUserData=@hasUserData where Id=@id", Database.Data.Connection);
        commandUserData.Parameters.Add("@hasUserData", DbType.Boolean).Value = true;
        commandUserData.Parameters.Add("@id", DbType.Int64).Value = CacheId;
        commandUserData.ExecuteNonQuery();
        commandUserData.Dispose();
    }
*/

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
/*
    public void DeleteFromDatabase()
    {
        int newCheckSum = 0;
        Replication.WaypointDelete(CacheId, checkSum, newCheckSum, GcCode);
        SqlCeCommand command = new SqlCeCommand("delete from Waypoint where GcCode=@gccode", Database.Data.Connection);
        command.Parameters.Add("@gccode", DbType.String).Value = GcCode;
        command.ExecuteNonQuery();
        command.Dispose();
    }
*/
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
