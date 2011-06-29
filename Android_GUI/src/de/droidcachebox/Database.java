package de.droidcachebox;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Views.Forms.SelectDB;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class Database {
	public Activity activity;
	public static Database Data;
	public static Database FieldNotes; 
	public enum DatabaseType { CacheBox, FieldNotes }
    protected DatabaseType databaseType;
    public String databasePath = "";
    public long DatabaseId = 0;  // for Database replication with WinCachebox
    public long MasterDatabaseId = 0;
    protected int latestDatabaseChange = 0;
    public SQLiteDatabase myDB = null;
    public CacheList Query;
        
	public Database(DatabaseType databaseType, Activity activity) {
		this.databaseType = databaseType;
		this.activity = activity;
		switch (databaseType) {
		case CacheBox:
			latestDatabaseChange = Global.LatestDatabaseChange;
			Query = new CacheList();
			break;
		case FieldNotes:
			latestDatabaseChange = Global.LatestDatabaseFieldNoteChange;
			break;
		}
	}

	public void Initialize()
	{
		if (myDB == null)
		{
			if (!Global.FileExists(databasePath))
				Reset();

			try
			{
				myDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
			} catch (Exception exc)
			{
				return;
			}
		} 
	}
	
	public void Reset()
	{
		// if exists, delete old database file
    	File file = new File(databasePath);
    	if (file.exists())
    		file.delete();

    	try
    	{
    		activity.openOrCreateDatabase(databasePath, Context.MODE_WORLD_WRITEABLE, null);
    	} catch (Exception exc)
    	{
    		String s = exc.getMessage();
    	}
	}
	
	public boolean StartUp(String databasePath) {
		this.databasePath = databasePath;
		
        Initialize();
		
		int databaseSchemeVersion = GetDatabaseSchemeVersion();
		if (databaseSchemeVersion < latestDatabaseChange) {
            AlterDatabase(databaseSchemeVersion);
            SetDatabaseSchemeVersion();
		}
		SetDatabaseSchemeVersion();
		
		return true;
	}

	private void AlterDatabase(int lastDatabaseSchemeVersion) {
		switch (databaseType)
		{
		case CacheBox:
			if (lastDatabaseSchemeVersion <= 0)
			{
				// First Initialization of the Database
				try
				{
					// Saarfuchs: SQL-Befehle aus create_SQLite.sql eingefügt
					myDB.execSQL("CREATE TABLE [Caches] ([Id] bigint NOT NULL primary key,[GcCode] nvarchar (12) NULL,[GcId] nvarchar (255) NULL,[Latitude] float NULL,[Longitude] float NULL,[Name] nchar (255) NULL,[Size] int NULL,[Difficulty] smallint NULL,[Terrain] smallint NULL,[Archived] bit NULL,[Available] bit NULL,[Found] bit NULL,[Type] smallint NULL,[PlacedBy] nvarchar (255) NULL,[Owner] nvarchar (255) NULL,[DateHidden] datetime NULL,[Hint] ntext NULL,[Description] ntext NULL,[Url] nchar (255) NULL,[NumTravelbugs] smallint NULL,[Rating] smallint NULL,[Vote] smallint NULL,[VotePending] bit NULL,[Notes] ntext NULL,[Solver] ntext NULL,[Favorit] bit NULL,[AttributesPositive] bigint NULL,[AttributesNegative] bigint NULL,[TourName] nchar (255) NULL,[GPXFilename_Id] bigint NULL,[HasUserData] bit NULL,[ListingCheckSum] int NULL DEFAULT 0,[ListingChanged] bit NULL,[ImagesUpdated] bit NULL,[DescriptionImagesUpdated] bit NULL,[CorrectedCoordinates] bit NULL);");
					myDB.execSQL("CREATE INDEX [archived_idx] ON [Caches] ([Archived] ASC);");
					myDB.execSQL("CREATE INDEX [AttributesNegative_idx] ON [Caches] ([AttributesNegative] ASC);");
					myDB.execSQL("CREATE INDEX [AttributesPositive_idx] ON [Caches] ([AttributesPositive] ASC);");
					myDB.execSQL("CREATE INDEX [available_idx] ON [Caches] ([Available] ASC);");
					myDB.execSQL("CREATE INDEX [Difficulty_idx] ON [Caches] ([Difficulty] ASC);");
					myDB.execSQL("CREATE INDEX [Favorit_idx] ON [Caches] ([Favorit] ASC);");
					myDB.execSQL("CREATE INDEX [found_idx] ON [Caches] ([Found] ASC);");
					myDB.execSQL("CREATE INDEX [GPXFilename_Id_idx] ON [Caches] ([GPXFilename_Id] ASC);");
					myDB.execSQL("CREATE INDEX [HasUserData_idx] ON [Caches] ([HasUserData] ASC);");
					myDB.execSQL("CREATE INDEX [ListingChanged_idx] ON [Caches] ([ListingChanged] ASC);");
					myDB.execSQL("CREATE INDEX [NumTravelbugs_idx] ON [Caches] ([NumTravelbugs] ASC);");
					myDB.execSQL("CREATE INDEX [placedby_idx] ON [Caches] ([PlacedBy] ASC);");
					myDB.execSQL("CREATE INDEX [Rating_idx] ON [Caches] ([Rating] ASC);");
					myDB.execSQL("CREATE INDEX [Size_idx] ON [Caches] ([Size] ASC);");
					myDB.execSQL("CREATE INDEX [Terrain_idx] ON [Caches] ([Terrain] ASC);");
					myDB.execSQL("CREATE INDEX [Type_idx] ON [Caches] ([Type] ASC);");

					myDB.execSQL("CREATE TABLE [CelltowerLocation] ([CellId] nvarchar (20) NOT NULL primary key,[Latitude] float NULL,[Longitude] float NULL);");

					myDB.execSQL("CREATE TABLE [GPXFilenames] ([Id] integer not null primary key autoincrement,[GPXFilename] nvarchar (255) NULL,[Imported] datetime NULL, [Name] nvarchar (255) NULL,[CacheCount] int NULL);");

					myDB.execSQL("CREATE TABLE [Logs] ([Id] bigint NOT NULL primary key, [CacheId] bigint NULL,[Timestamp] datetime NULL,[Finder] nvarchar (128) NULL,[Type] smallint NULL,[Comment] ntext NULL);");
					myDB.execSQL("CREATE INDEX [log_idx] ON [Logs] ([CacheId] ASC);");
					myDB.execSQL("CREATE INDEX [timestamp_idx] ON [Logs] ([Timestamp] ASC);");

					myDB.execSQL("CREATE TABLE [PocketQueries] ([Id] integer not null primary key autoincrement,[PQName] nvarchar (255) NULL,[CreationTimeOfPQ] datetime NULL);");

					myDB.execSQL("CREATE TABLE [Waypoint] ([GcCode] nvarchar (12) NOT NULL primary key,[CacheId] bigint NULL,[Latitude] float NULL,[Longitude] float NULL,[Description] ntext NULL,[Clue] ntext NULL,[Type] smallint NULL,[SyncExclude] bit NULL,[UserWaypoint] bit NULL,[Title] ntext NULL);");
					myDB.execSQL("CREATE INDEX [UserWaypoint_idx] ON [Waypoint] ([UserWaypoint] ASC);");

					myDB.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
					myDB.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");

					myDB.execSQL("CREATE TABLE [Replication] ([Id] integer not null primary key autoincrement, [ChangeType] int NOT NULL, [CacheId] bigint NOT NULL, [WpGcCode] nvarchar (12) NULL, [SolverCheckSum] int NULL, [NotesCheckSum] int NULL, [WpCoordCheckSum] int NULL);");
					myDB.execSQL("CREATE INDEX [Replication_idx] ON [Replication] ([Id] ASC);");
					myDB.execSQL("CREATE INDEX [ReplicationCache_idx] ON [Replication] ([CacheId] ASC);");
					
				} catch (Exception exc)
				{
					String ex = exc.getMessage();
				}
			}
			break;
		case FieldNotes:
			if (lastDatabaseSchemeVersion <= 0)
			{
				// First Initialization of the Database
				try
				{
					// FieldNotes Table
					myDB.execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [Name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");
					
					// Config Table
					myDB.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
					myDB.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
				} catch (Exception exc)
				{
					String ex = exc.getMessage();
				}
			}
			break;
		}
	}

	private int GetDatabaseSchemeVersion() {
        int result = -1;
        Cursor c = null;
        try
        {
        	c = myDB.rawQuery("select Value from Config where [Key]=?", new String[] { "DatabaseSchemeVersion" });
        } catch (Exception exc)
        { 
        	return -1;
        }
        try
        {
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
                String databaseSchemeVersion = c.getString(0);
                result = Integer.parseInt(databaseSchemeVersion);
                c.moveToNext();
            };
        }
        catch (Exception exc)
        {
            result = -1;
        }
        c.close();

        return result;
	}

	private void SetDatabaseSchemeVersion() {
		// TODO Auto-generated method stub
//		myDB.execSQL("Update Config set Value=" + latestDatabaseChange + " where [Key]='DatabaseSchemeVersionx'");
		ContentValues val = new ContentValues();
		val.put("Value", latestDatabaseChange);
		int anz = myDB.update("Config", val, "[Key]='DatabaseSchemeVersion'", null);
		if (anz <= 0) {
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersion");
			myDB.insert("Config", null, val);
		}
												           	
	}
	
	
	
	
	// Methoden für Waypoint
	 public static void DeleteFromDatabase(Waypoint WP)
	    {
	        int newCheckSum = 0;
//	        Replication.WaypointDelete(CacheId, checkSum, newCheckSum, GcCode);
	        try
	        {
	        	Database.Data.myDB.delete("Waypoint", "GcCode='" + WP.GcCode+"'", null);
	        } catch (Exception exc)
	        {
	        	Logger.Error("Waypoint.DeleteFromDataBase()","", exc);
	        }
	    }
	
	 public static void UpdateDatabase(Waypoint WP)
	    {
	        int newCheckSum = createCheckSum(WP);
//	        Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
	        if (newCheckSum != WP.checkSum)
	        {
	            ContentValues args = new ContentValues();
	            args.put("gccode", WP.GcCode);
	            args.put("cacheid", WP.CacheId);
	            args.put("latitude", WP.Latitude());
	            args.put("longitude", WP.Longitude());
	            args.put("description", WP.Description);
	            args.put("type", WP.Type.ordinal());
	            args.put("syncexclude", WP.IsSyncExcluded);
	            args.put("userwaypoint", WP.IsUserWaypoint);
	            args.put("clue", WP.Clue);
	            args.put("title", WP.Title);
	            try
	            {
	            	Database.Data.myDB.update("Waypoint", args, "CacheId=" + WP.CacheId + " and GcCode=\"" + WP.GcCode + "\"", null);
	            } catch (Exception exc)
	            {
	            	return;
	            
	            }

	            args = new ContentValues();
	            args.put("hasUserData", true);
	            try
	            {
	            Database.Data.myDB.update("Caches", args, "Id=" + WP.CacheId, null);
	            } catch (Exception exc)
	            {
	            	return;
	            }

	            WP.checkSum = newCheckSum;
	        }
	    }
	 
	 private static int createCheckSum(Waypoint WP)
	    {
	        // for Replication
	        String sCheckSum = WP.GcCode;
	        sCheckSum += Global.FormatLatitudeDM(WP.Latitude());
	        sCheckSum += Global.FormatLongitudeDM(WP.Longitude());
	        sCheckSum += WP.Description;
	        sCheckSum += WP.Type.ordinal();
	        sCheckSum += WP.Clue;
	        sCheckSum += WP.Title;
	        return (int)Global.sdbm(sCheckSum);
	    }
	 
	 public static Waypoint getWaypoint(Cursor reader)
	    {
		 	Waypoint WP = new Waypoint();
		 	WP.GcCode = reader.getString(0);
		 	WP.CacheId = reader.getLong(1);
	        double latitude = reader.getDouble(2);
	        double longitude = reader.getDouble(3);
	        WP.Coordinate = new Coordinate(latitude, longitude);
	        WP.Description = reader.getString(4);
	        WP.Type = CacheTypes.values()[reader.getShort(5)];
	        WP.IsSyncExcluded = reader.getInt(6) == 1;
	        WP.IsUserWaypoint = reader.getInt(7) == 1;
	        WP.Clue = reader.getString(8);
	        if (WP.Clue != null) WP.Clue = WP.Clue.trim();
	        WP.Title = reader.getString(9).trim();
	        WP.checkSum = createCheckSum(WP);
	        
	        return WP;
	    }
	 
	 public static boolean WaypointExists(String gcCode)
	    {
	        Cursor c = Database.Data.myDB.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[] { gcCode });
	       {
	            c.moveToFirst();
	            while(c.isAfterLast() == false)
	            {
	          
	           	try {
					c.close();
						return true;
				} catch (Exception e) {
					return false;
				}
	            }
	            c.close();

	        return false;
	       }
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
	                if (!WaypointExists(gcCode))
	                    return gcCode;
	            }
	        throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
	    }

	 public static void WriteToDatabase(Waypoint WP)
	    {
	        int newCheckSum = createCheckSum(WP);
//	        Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
	        ContentValues args = new ContentValues();
	        args.put("gccode", WP.GcCode);
	        args.put("cacheid", WP.CacheId);
	        args.put("latitude", WP.Latitude());
	        args.put("longitude", WP.Longitude());
	        args.put("description", WP.Description);
	        args.put("type", WP.Type.ordinal());
	        args.put("syncexclude", WP.IsSyncExcluded);
	        args.put("userwaypoint", WP.IsUserWaypoint);
	        args.put("clue", WP.Clue);
	        args.put("title", WP.Title);

	        try
	        {
	        	Database.Data.myDB.insert("Waypoint", null, args);
	        	
	            args = new ContentValues();
	            args.put("hasUserData", true);
	        	Database.Data.myDB.update("Caches", args, "Id=" + WP.CacheId, null);
	        } catch (Exception exc)
	        {
	        	return;
	        
	        }
	    }

	 
	    
	    
	    // Methodes für Cache
	    
	    public void Found(boolean value, Cache cache)
	    {
	    	cache.Found = value;
	        
	        ContentValues args = new ContentValues();
	        args.put("found", value);
	        Database.Data.myDB.update("Caches", args, "Id=" + cache.Id, null);

	    }
	    
	   
        public static String GetNote(Cache cache)
        {
        	String resultString = "";
            Cursor c = Database.Data.myDB.rawQuery("select Notes from Caches where Id=?", new String[] { String.valueOf(cache.Id) });
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
                resultString = c.getString(0);
                break;
            };
            cache.noteCheckSum = (int)Global.sdbm(resultString);
            return resultString;
        }
        
        public static void SetNote(Cache cache, String value)
        {
            int newNoteCheckSum = (int)Global.sdbm(value);
            
//        	Replication.NoteChanged(this.Id, noteCheckSum, newNoteCheckSum);
          if (newNoteCheckSum != cache.noteCheckSum)
          {
              ContentValues args = new ContentValues();
              args.put("Notes", value);
              args.put("HasUserData", true);
              
              Database.Data.myDB.update("Caches", args, "id=" + cache.Id, null);
              cache.noteCheckSum = newNoteCheckSum;
          }
        }

        
        public static String GetSolver(Cache cache)
        {
        	String resultString = "";
            Cursor c = Database.Data.myDB.rawQuery("select Solver from Caches where Id=?", new String[] { String.valueOf(cache.Id) });
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
                resultString = c.getString(0);
                break;
            };
            cache.noteCheckSum = (int)Global.sdbm(resultString);
            return resultString;
        }
        public static void SetSolver(Cache cache, String value)
        {
            int newSolverCheckSum = (int)Global.sdbm(value);
            
//            Replication.SolverChanged(this.Id, solverCheckSum, newSolverCheckSum);
            if (newSolverCheckSum != cache.solverCheckSum)
            {
                ContentValues args = new ContentValues();
                args.put("Solver", value);
                args.put("HasUserData", true);
                
                Database.Data.myDB.update("Caches", args, "id=" + cache.Id, null);
                cache.solverCheckSum = newSolverCheckSum;
            }
        }

        public static ArrayList<LogEntry> Logs(Cache cache)
        {
            ArrayList<LogEntry> result = new ArrayList<LogEntry>();

            Cursor reader = Database.Data.myDB.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc", new String[] { Long.toString(cache.Id) });

        	reader.moveToFirst();
            while(reader.isAfterLast() == false)
            {
            	LogEntry logent = getLogEntry(cache,reader, true); 
                result.add(logent);
                reader.moveToNext();
            }
            reader.close();
            
            return result;
        }
        
        private static LogEntry getLogEntry(Cache cache, Cursor reader, boolean filterBbCode)
        {
        	LogEntry retLogEntry = new LogEntry();
        	
        	retLogEntry.CacheId = reader.getLong(0);
        	      
        	      String sDate = reader.getString(1);
        	      DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	      try {
        	    	  retLogEntry.Timestamp = iso8601Format.parse(sDate);
        	      } catch (ParseException e) {
        	      }
        	      retLogEntry.Finder = reader.getString(2);
        	      retLogEntry.TypeIcon = reader.getInt(3);
        	      retLogEntry.Comment = reader.getString(4);
        	      cache.Id = reader.getLong(5);

        	      if (filterBbCode)
        	      {
        	        int lIndex;

        	        while ((lIndex = retLogEntry.Comment.indexOf('[')) >= 0)
        	        {
        	          int rIndex = retLogEntry.Comment.indexOf(']', lIndex);

        	          if (rIndex == -1)
        	            break;

        	          retLogEntry.Comment = retLogEntry.Comment.substring(0, lIndex) + retLogEntry.Comment.substring(rIndex + 1);
        	        }
      	      }
        	      
        	 return retLogEntry;
        }

        public static String GetDescription(Cache cache)
        {
        	String description = "";
            Cursor reader = Database.Data.myDB.rawQuery("select Description from Caches where Id=?", new String[] { Long.toString(cache.Id) } );
        	reader.moveToFirst();
            while(reader.isAfterLast() == false)
            {
            	description = reader.getString(0);
                reader.moveToNext();
            }
            reader.close();

            return description;
        }

        public static String Hint(Cache cache)
        {
        	if (cache.hint.equals(""))
        	{
                Cursor reader = Database.Data.myDB.rawQuery("select Hint from Caches where Id=?", new String[] { Long.toString(cache.Id) } );
            	reader.moveToFirst();
                while(reader.isAfterLast() == false)
                {
                	cache.hint = reader.getString(0);
                    reader.moveToNext();
                }
                reader.close();
        	}
        	return cache.hint;
        }
	    
        public float Distance(Cache cache, Coordinate fromPos)
        {
           // Coordinate fromPos = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
        	Waypoint waypoint = cache.GetFinalWaypoint();
            // Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die Diszanzberechnung vom Final aus gemacht werden
            // If a mystery has a final waypoint, the distance will be calculated to the final not the the cache coordinates
        	Coordinate toPos = cache.Pos;
            if (waypoint != null)
            	toPos = new Coordinate(waypoint.Coordinate.Latitude, waypoint.Coordinate.Longitude);
            float[] dist = new float[4];
            Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, toPos.Latitude, toPos.Longitude, dist);
            return (float)dist[0];
        }

        public static long AttributesPositive(Cache cache)
        {
        	if (cache.attributesPositive == 0)
        	{
                Cursor c = Database.Data.myDB.rawQuery("select AttributesPositive from Caches where Id=?", new String[] { String.valueOf(cache.Id) });
                c.moveToFirst();
                while(c.isAfterLast() == false)
                {
                	if (!c.isNull(0))
                		cache.attributesPositive = c.getLong(0);
                	else
                		cache.attributesPositive = 0;
                    break;
                };
                c.close();
        	}
        	return cache.attributesPositive;
        }
        
        public static long AttributesNegative(Cache cache)
        {
        	if (cache.attributesNegative == 0)
        	{
                Cursor c = Database.Data.myDB.rawQuery("select AttributesNegative from Caches where Id=?", new String[] { String.valueOf(cache.Id) });
                c.moveToFirst();
                while(c.isAfterLast() == false)
                {
                	if (!c.isNull(0))
                		cache.attributesNegative = c.getLong(0);
                	else
                		cache.attributesNegative = 0;
                    break;
                };
                c.close();
        	}
        	return cache.attributesNegative;
        }

        public static Cache getCache(Cursor reader)
        {
        	Cache cache = new Cache();
        	cache.Id = reader.getLong(0);
        	cache.GcCode = reader.getString(1).trim();
        	cache.Pos = new Coordinate(reader.getDouble(2), reader.getDouble(3));
        	cache.Name = reader.getString(4).trim();
        	cache.Size = reader.getInt(5);
        	cache.Difficulty = ((float)reader.getShort(6)) / 2;
        	cache.Terrain = ((float)reader.getShort(7)) / 2;
        	cache.Archived = reader.getInt(8) != 0;
        	cache.Available = reader.getInt(9) != 0;
            int ifound = reader.getInt(10);
            cache.Found = reader.getInt(10) != 0;
            cache.Type = CacheTypes.values()[reader.getShort(11)];
            cache.PlacedBy = reader.getString(12).trim();
            cache.Owner = reader.getString(13).trim();
            
            String sDate = reader.getString(14);
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
            	cache.DateHidden = iso8601Format.parse(sDate);
            } catch (ParseException e) {
    		}

            cache.Url = reader.getString(15).trim();
            cache.NumTravelbugs = reader.getInt(16);
            cache.GcId = reader.getString(17).trim();
            cache.Rating = ((float)reader.getShort(18)) / 100.0f;
            if (reader.getInt(19) > 0)
            	cache.Favorit = true;
            else
            	cache.Favorit = false;
            cache.TourName = reader.getString(20).trim();

            if (reader.getString(21) != "")
            	cache.GPXFilename_ID = reader.getInt(21);
            else
            	cache.GPXFilename_ID = -1;

            if (reader.getInt(22) > 0)
            	cache.hasUserData = true;
            else
            	cache.hasUserData = false;

            if (reader.getInt(23) > 0)
            	cache.listingChanged = true;
            else
            	cache.listingChanged = false;

            if (reader.getInt(24) > 0)
            	cache.CorrectedCoordinates = true;
            else
            	cache.CorrectedCoordinates = false;

            cache.MapX = 256.0 * Descriptor.LongitudeToTileX(cache.MapZoomLevel, cache.Longitude());
            cache.MapY = 256.0 * Descriptor.LatitudeToTileY(cache.MapZoomLevel, cache.Latitude());
            
            return cache;
        }
        
    	public static void WriteToDatabase(Cache cache) 
    	{
    	  // this muss jetzt irgendwie in die DB!!
    		
    	}
}
