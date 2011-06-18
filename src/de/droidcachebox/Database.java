package de.droidcachebox;

import java.io.File;

import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Views.Forms.SelectDB;
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
}
