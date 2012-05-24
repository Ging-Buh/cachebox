package CB_Core.DB;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import CB_Core.GlobalCore;
import CB_Core.DAO.CategoryDAO;
import CB_Core.Enums.LogTypes;
import CB_Core.Log.Logger;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public abstract class Database
{
	protected String databasePath;
	public static Database Data;
	public static Database FieldNotes;
	public static Database Settings;

	protected boolean newDB = false;

	/***
	 * Wenn die DB neu erstellt wurde ist der Return Wert bei der ersten Abfrage True
	 * 
	 * @return
	 */
	public boolean isDbNew()
	{
		return newDB;
	}

	public enum DatabaseType
	{
		CacheBox, FieldNotes, Settings
	}

	protected DatabaseType databaseType;
	public long DatabaseId = 0; // for Database replication with WinCachebox
	public long MasterDatabaseId = 0;
	protected int latestDatabaseChange = 0;
	public CacheList Query;

	public Database(DatabaseType databaseType)
	{
		this.databaseType = databaseType;

		switch (databaseType)
		{
		case CacheBox:
			latestDatabaseChange = GlobalCore.LatestDatabaseChange;
			Query = new CacheList();
			break;
		case FieldNotes:
			latestDatabaseChange = GlobalCore.LatestDatabaseFieldNoteChange;
			break;
		case Settings:
			latestDatabaseChange = GlobalCore.LatestDatabaseSettingsChange;
		}
	}

	public abstract void Initialize();;

	public abstract void Reset();;

	public boolean StartUp(String databasePath)
	{
		this.databasePath = databasePath;

		Initialize();

		int databaseSchemeVersion = GetDatabaseSchemeVersion();
		if (databaseSchemeVersion < latestDatabaseChange)
		{
			AlterDatabase(databaseSchemeVersion);
			SetDatabaseSchemeVersion();
		}
		SetDatabaseSchemeVersion();
		if (databaseType == DatabaseType.CacheBox)
		{ // create or load DatabaseId for each
			DatabaseId = ReadConfigLong("DatabaseId");
			if (DatabaseId <= 0)
			{
				DatabaseId = new Date().getTime();
				WriteConfigLong("DatabaseId", DatabaseId);
			}
			// Read MasterDatabaseId. If MasterDatabaseId > 0 -> This database
			// is connected to the Replications Master of WinCB
			// In this case changes of Waypoints, Solvertext, Notes must be
			// noted in the Table Replication...
			MasterDatabaseId = ReadConfigLong("MasterDatabaseId");
		}
		return true;
	}

	private void AlterDatabase(int lastDatabaseSchemeVersion)
	{
		switch (databaseType)
		{
		case CacheBox:

			beginTransaction();
			try
			{
				if (lastDatabaseSchemeVersion <= 0)
				{
					// First Initialization of the Database
					execSQL("CREATE TABLE [Caches] ([Id] bigint NOT NULL primary key,[GcCode] nvarchar (12) NULL,[GcId] nvarchar (255) NULL,[Latitude] float NULL,[Longitude] float NULL,[Name] nchar (255) NULL,[Size] int NULL,[Difficulty] smallint NULL,[Terrain] smallint NULL,[Archived] bit NULL,[Available] bit NULL,[Found] bit NULL,[Type] smallint NULL,[PlacedBy] nvarchar (255) NULL,[Owner] nvarchar (255) NULL,[DateHidden] datetime NULL,[Hint] ntext NULL,[Description] ntext NULL,[Url] nchar (255) NULL,[NumTravelbugs] smallint NULL,[Rating] smallint NULL,[Vote] smallint NULL,[VotePending] bit NULL,[Notes] ntext NULL,[Solver] ntext NULL,[Favorit] bit NULL,[AttributesPositive] bigint NULL,[AttributesNegative] bigint NULL,[TourName] nchar (255) NULL,[GPXFilename_Id] bigint NULL,[HasUserData] bit NULL,[ListingCheckSum] int NULL DEFAULT 0,[ListingChanged] bit NULL,[ImagesUpdated] bit NULL,[DescriptionImagesUpdated] bit NULL,[CorrectedCoordinates] bit NULL);");
					execSQL("CREATE INDEX [archived_idx] ON [Caches] ([Archived] ASC);");
					execSQL("CREATE INDEX [AttributesNegative_idx] ON [Caches] ([AttributesNegative] ASC);");
					execSQL("CREATE INDEX [AttributesPositive_idx] ON [Caches] ([AttributesPositive] ASC);");
					execSQL("CREATE INDEX [available_idx] ON [Caches] ([Available] ASC);");
					execSQL("CREATE INDEX [Difficulty_idx] ON [Caches] ([Difficulty] ASC);");
					execSQL("CREATE INDEX [Favorit_idx] ON [Caches] ([Favorit] ASC);");
					execSQL("CREATE INDEX [found_idx] ON [Caches] ([Found] ASC);");
					execSQL("CREATE INDEX [GPXFilename_Id_idx] ON [Caches] ([GPXFilename_Id] ASC);");
					execSQL("CREATE INDEX [HasUserData_idx] ON [Caches] ([HasUserData] ASC);");
					execSQL("CREATE INDEX [ListingChanged_idx] ON [Caches] ([ListingChanged] ASC);");
					execSQL("CREATE INDEX [NumTravelbugs_idx] ON [Caches] ([NumTravelbugs] ASC);");
					execSQL("CREATE INDEX [placedby_idx] ON [Caches] ([PlacedBy] ASC);");
					execSQL("CREATE INDEX [Rating_idx] ON [Caches] ([Rating] ASC);");
					execSQL("CREATE INDEX [Size_idx] ON [Caches] ([Size] ASC);");
					execSQL("CREATE INDEX [Terrain_idx] ON [Caches] ([Terrain] ASC);");
					execSQL("CREATE INDEX [Type_idx] ON [Caches] ([Type] ASC);");

					execSQL("CREATE TABLE [CelltowerLocation] ([CellId] nvarchar (20) NOT NULL primary key,[Latitude] float NULL,[Longitude] float NULL);");

					execSQL("CREATE TABLE [GPXFilenames] ([Id] integer not null primary key autoincrement,[GPXFilename] nvarchar (255) NULL,[Imported] datetime NULL, [Name] nvarchar (255) NULL,[CacheCount] int NULL);");

					execSQL("CREATE TABLE [Logs] ([Id] bigint NOT NULL primary key, [CacheId] bigint NULL,[Timestamp] datetime NULL,[Finder] nvarchar (128) NULL,[Type] smallint NULL,[Comment] ntext NULL);");
					execSQL("CREATE INDEX [log_idx] ON [Logs] ([CacheId] ASC);");
					execSQL("CREATE INDEX [timestamp_idx] ON [Logs] ([Timestamp] ASC);");

					execSQL("CREATE TABLE [PocketQueries] ([Id] integer not null primary key autoincrement,[PQName] nvarchar (255) NULL,[CreationTimeOfPQ] datetime NULL);");

					execSQL("CREATE TABLE [Waypoint] ([GcCode] nvarchar (12) NOT NULL primary key,[CacheId] bigint NULL,[Latitude] float NULL,[Longitude] float NULL,[Description] ntext NULL,[Clue] ntext NULL,[Type] smallint NULL,[SyncExclude] bit NULL,[UserWaypoint] bit NULL,[Title] ntext NULL);");
					execSQL("CREATE INDEX [UserWaypoint_idx] ON [Waypoint] ([UserWaypoint] ASC);");

					execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
					execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");

					execSQL("CREATE TABLE [Replication] ([Id] integer not null primary key autoincrement, [ChangeType] int NOT NULL, [CacheId] bigint NOT NULL, [WpGcCode] nvarchar (12) NULL, [SolverCheckSum] int NULL, [NotesCheckSum] int NULL, [WpCoordCheckSum] int NULL);");
					execSQL("CREATE INDEX [Replication_idx] ON [Replication] ([Id] ASC);");
					execSQL("CREATE INDEX [ReplicationCache_idx] ON [Replication] ([CacheId] ASC);");
				}

				if (lastDatabaseSchemeVersion < 1003)
				{
					execSQL("CREATE TABLE [Locations] ([Id] integer not null primary key autoincrement, [Name] nvarchar (255) NULL, [Latitude] float NULL, [Longitude] float NULL);");
					execSQL("CREATE INDEX [Locatioins_idx] ON [Locations] ([Id] ASC);");

					execSQL("CREATE TABLE [SdfExport] ([Id]  integer not null primary key autoincrement, [Description] nvarchar(255) NULL, [ExportPath] nvarchar(255) NULL, [MaxDistance] float NULL, [LocationID] Bigint NULL, [Filter] ntext NULL, [Update] bit NULL, [ExportImages] bit NULL, [ExportSpoilers] bit NULL, [ExportMaps] bit NULL, [OwnRepository] bit NULL, [ExportMapPacks] bit NULL, [MaxLogs] int NULL);");
					execSQL("CREATE INDEX [SdfExport_idx] ON [SdfExport] ([Id] ASC);");

					execSQL("ALTER TABLE [CACHES] ADD [FirstImported] datetime NULL;");

					execSQL("CREATE TABLE [Category] ([Id]  integer not null primary key autoincrement, [GpxFilename] nvarchar(255) NULL, [Pinned] bit NULL default 0, [CacheCount] int NULL);");
					execSQL("CREATE INDEX [Category_idx] ON [Category] ([Id] ASC);");

					execSQL("ALTER TABLE [GpxFilenames] ADD [CategoryId] bigint NULL;");

					execSQL("ALTER TABLE [Caches] add [state] nvarchar(50) NULL;");
					execSQL("ALTER TABLE [Caches] add [country] nvarchar(50) NULL;");
				}
				if (lastDatabaseSchemeVersion < 1015)
				{
					// GpxFilenames mit Kategorien verkn�pfen

					// alte Category Tabelle l�schen
					delete("Category", "", null);
					HashMap<Long, String> gpxFilenames = new HashMap<Long, String>();
					HashMap<String, Long> categories = new HashMap<String, Long>();

					CoreCursor reader = rawQuery("select ID, GPXFilename from GPXFilenames", null);
					reader.moveToFirst();
					while (reader.isAfterLast() == false)
					{
						long id = reader.getLong(0);
						String gpxFilename = reader.getString(1);
						gpxFilenames.put(id, gpxFilename);
						reader.moveToNext();
					}
					reader.close();
					for (Entry<Long, String> entry : gpxFilenames.entrySet())
					{
						if (!categories.containsKey(entry.getValue()))
						{
							// add new Category
							CategoryDAO categoryDAO = new CategoryDAO();
							Category category = categoryDAO.CreateNewCategory(entry.getValue());
							// and store
							categories.put(entry.getValue(), category.Id);
						}
						if (categories.containsKey(entry.getValue()))
						{
							// and store CategoryId in GPXFilenames
							Parameters args = new Parameters();
							args.put("CategoryId", categories.get(entry.getValue()));
							try
							{
								Database.Data.update("GpxFilenames", args, "Id=" + entry.getKey(), null);
							}
							catch (Exception exc)
							{
								Logger.Error("Database", "Update_CategoryId", exc);
							}
						}
					}

				}
				if (lastDatabaseSchemeVersion < 1016)
				{
					execSQL("ALTER TABLE [CACHES] ADD [ApiStatus] smallint NULL default 0;");
				}
				if (lastDatabaseSchemeVersion < 1017)
				{
					execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (12) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
					execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
					execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (12) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
					execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
					execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
				}
				if (lastDatabaseSchemeVersion < 1018)
				{
					execSQL("ALTER TABLE [SdfExport] ADD [MapPacks] nvarchar(512) NULL;");

				}
				if (lastDatabaseSchemeVersion < 1019)
				{
					// neue Felder f�r die erweiterten Attribute einf�gen
					execSQL("ALTER TABLE [CACHES] ADD [AttributesPositiveHigh] bigint NULL default 0");
					execSQL("ALTER TABLE [CACHES] ADD [AttributesNegativeHigh] bigint NULL default 0");

					// Die Nummerierung der Attribute stimmte nicht mit der von
					// Groundspeak �berein. Bei 16 und 45 wurde jeweils eine
					// Nummber �bersprungen
					CoreCursor reader = rawQuery("select Id, AttributesPositive, AttributesNegative from Caches", new String[] {});
					reader.moveToFirst();
					while (reader.isAfterLast() == false)
					{
						long id = reader.getLong(0);
						long attributesPositive = (long) reader.getLong(1);
						long attributesNegative = (long) reader.getLong(2);

						attributesPositive = convertAttribute(attributesPositive);
						attributesNegative = convertAttribute(attributesNegative);

						Parameters val = new Parameters();
						val.put("AttributesPositive", attributesPositive);
						val.put("AttributesNegative", attributesNegative);
						String whereClause = "[Id]=" + id;
						update("Caches", val, whereClause, null);
						reader.moveToNext();
					}
					reader.close();

				}
				if (lastDatabaseSchemeVersion < 1020)
				{
					// for long Settings
					execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");

				}
				if (lastDatabaseSchemeVersion < 1021)
				{
					// Image Table
					execSQL("CREATE TABLE [Images] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [Description] ntext, [Name] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [IsCacheImage] bit NULL);");
					execSQL("CREATE INDEX [images_cacheid_idx] ON [Images] ([CacheId] ASC);");
					execSQL("CREATE INDEX [images_gccode_idx] ON [Images] ([GcCode] ASC);");
					execSQL("CREATE INDEX [images_iscacheimage_idx] ON [Images] ([IsCacheImage] ASC);");
					execSQL("CREATE UNIQUE INDEX [images_imageurl_idx] ON [Images] ([ImageUrl] ASC);");
				}
				if (lastDatabaseSchemeVersion < 1022)
				{
					execSQL("ALTER TABLE [Caches] ALTER COLUMN [GcCode] nvarchar(15); ");

					execSQL("ALTER TABLE [Waypoint] DROP CONSTRAINT Waypoint_PK ");
					execSQL("ALTER TABLE [Waypoint] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
					execSQL("ALTER TABLE [Waypoint] ADD CONSTRAINT  [Waypoint_PK] PRIMARY KEY ([GcCode]); ");

					execSQL("ALTER TABLE [Replication] ALTER COLUMN [WpGcCode] nvarchar(15); ");
					execSQL("ALTER TABLE [Trackable] ALTER COLUMN [GcCode] nvarchar(15); ");
					execSQL("ALTER TABLE [TbLogs] ALTER COLUMN [GcCode] nvarchar(15); ");
					execSQL("ALTER TABLE [Images] ALTER COLUMN [GcCode] nvarchar(15); ");
				}

				setTransactionSuccessful();
			}
			catch (Exception exc)
			{
				Logger.Error("AlterDatabase", "", exc);
			}
			finally
			{
				endTransaction();
			}

			break;
		case FieldNotes:
			beginTransaction();
			try
			{

				if (lastDatabaseSchemeVersion <= 0)
				{
					// First Initialization of the Database
					// FieldNotes Table
					execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [Name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");

					// Config Table
					execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
					execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
				}
				setTransactionSuccessful();
			}
			catch (Exception exc)
			{
				Logger.Error("AlterDatabase", "", exc);
			}
			finally
			{
				endTransaction();
			}
			break;
		case Settings:
			beginTransaction();
			try
			{
				if (lastDatabaseSchemeVersion <= 0)
				{
					// First Initialization of the Database
					execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
					execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
				}
				if (lastDatabaseSchemeVersion <= 1002)
				{
					// Long Text Field for long Strings
					execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
				}
				setTransactionSuccessful();
			}
			catch (Exception exc)
			{
				Logger.Error("AlterDatabase", "", exc);
			}
			finally
			{
				endTransaction();
			}
			break;
		}
	}

	private long convertAttribute(long att)
	{
		// Die Nummerierung der Attribute stimmte nicht mit der von Groundspeak
		// �berein. Bei 16 und 45 wurde jeweils eine Nummber �bersprungen
		long result = 0;
		// Maske f�r die untersten 15 bit
		long mask = 0;
		for (int i = 0; i < 16; i++)
			mask += (long) 1 << i;
		// unterste 15 bit ohne Verschiebung kopieren
		result = att & mask;
		// Maske f�r die Bits 16-45
		mask = 0;
		for (int i = 16; i < 45; i++)
			mask += (long) 1 << i;
		long tmp = att & mask;
		// Bits 16-44 um eins verschieben
		tmp = tmp << 1;
		// und zum Result kopieren
		result += tmp;
		// Maske f�r die Bits 45-45
		mask = 0;
		for (int i = 45; i < 63; i++)
			mask += (long) 1 << i;
		tmp = att & mask;
		// Bits 45-63 um 2 verschieben
		tmp = tmp << 2;
		// und zum Result kopieren
		result += tmp;

		return result;
	}

	private int GetDatabaseSchemeVersion()
	{
		int result = -1;
		CoreCursor c = null;
		try
		{
			c = rawQuery("select Value from Config where [Key]=?", new String[]
				{ "DatabaseSchemeVersionWin" });
		}
		catch (Exception exc)
		{
			return -1;
		}
		try
		{
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				String databaseSchemeVersion = c.getString(0);
				result = Integer.parseInt(databaseSchemeVersion);
				c.moveToNext();
			}
		}
		catch (Exception exc)
		{
			result = -1;
		}
		if (c != null)
		{
			c.close();
		}

		return result;
	}

	private void SetDatabaseSchemeVersion()
	{
		Parameters val = new Parameters();
		val.put("Value", latestDatabaseChange);
		long anz = update("Config", val, "[Key]='DatabaseSchemeVersionWin'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersionWin");
			insert("Config", val);
		}
		// for Compatibility with WinCB
		val.put("Value", latestDatabaseChange);
		anz = update("Config", val, "[Key]='DatabaseSchemeVersion'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersion");
			insert("Config", val);
		}
	}

	// Methoden f�r Waypoint
	public static void DeleteFromDatabase(Waypoint WP)
	{
		int newCheckSum = 0;
		Replication.WaypointDelete(WP.CacheId, WP.checkSum, newCheckSum, WP.GcCode);
		try
		{
			Data.delete("Waypoint", "GcCode='" + WP.GcCode + "'", null);
		}
		catch (Exception exc)
		{
			Logger.Error("Waypoint.DeleteFromDataBase()", "", exc);
		}
	}

	public static boolean WaypointExists(String gcCode)
	{
		CoreCursor c = Database.Data.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[]
			{ gcCode });
		{
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{

				try
				{
					c.close();
					return true;
				}
				catch (Exception e)
				{
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
				String gcCode = firstCharCandidates.substring(i, i + 1) + secondCharCandidates.substring(j, j + 1) + suffix;
				if (!WaypointExists(gcCode)) return gcCode;
			}
		throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
	}

	// Methodes f�r Cache

	public static String GetNote(Cache cache)
	{
		String resultString = "";
		CoreCursor c = Database.Data.rawQuery("select Notes from Caches where Id=?", new String[]
			{ String.valueOf(cache.Id) });
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			resultString = c.getString(0);
			break;
		}
		;
		cache.noteCheckSum = (int) GlobalCore.sdbm(resultString);
		return resultString;
	}

	public static void SetNote(Cache cache, String value)
	{
		int newNoteCheckSum = (int) GlobalCore.sdbm(value);

		Replication.NoteChanged(cache.Id, cache.noteCheckSum, newNoteCheckSum);
		if (newNoteCheckSum != cache.noteCheckSum)
		{
			Parameters args = new Parameters();
			args.put("Notes", value);
			args.put("HasUserData", true);

			Database.Data.update("Caches", args, "id=" + cache.Id, null);
			cache.noteCheckSum = newNoteCheckSum;
		}
	}

	public static String GetSolver(Cache cache)
	{
		String resultString = "";
		CoreCursor c = Database.Data.rawQuery("select Solver from Caches where Id=?", new String[]
			{ String.valueOf(cache.Id) });
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			resultString = c.getString(0);
			break;
		}
		;
		cache.noteCheckSum = (int) GlobalCore.sdbm(resultString);
		return resultString;
	}

	public static void SetSolver(Cache cache, String value)
	{
		int newSolverCheckSum = (int) GlobalCore.sdbm(value);

		Replication.SolverChanged(cache.Id, cache.solverCheckSum, newSolverCheckSum);
		if (newSolverCheckSum != cache.solverCheckSum)
		{
			Parameters args = new Parameters();
			args.put("Solver", value);
			args.put("HasUserData", true);

			Database.Data.update("Caches", args, "id=" + cache.Id, null);
			cache.solverCheckSum = newSolverCheckSum;
		}
	}

	public static ArrayList<LogEntry> Logs(Cache cache)
	{
		ArrayList<LogEntry> result = new ArrayList<LogEntry>();

		CoreCursor reader = Database.Data.rawQuery(
				"select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc",
				new String[]
					{ Long.toString(cache.Id) });

		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			LogEntry logent = getLogEntry(cache, reader, true);
			if (logent != null) result.add(logent);
			reader.moveToNext();
		}
		reader.close();

		return result;
	}

	private static LogEntry getLogEntry(Cache cache, CoreCursor reader, boolean filterBbCode)
	{
		int intLogType = reader.getInt(3);
		if (intLogType < 0 || intLogType > 13) return null;

		LogEntry retLogEntry = new LogEntry();

		retLogEntry.CacheId = reader.getLong(0);

		String sDate = reader.getString(1);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			retLogEntry.Timestamp = iso8601Format.parse(sDate);
		}
		catch (ParseException e)
		{
		}
		retLogEntry.Finder = reader.getString(2);
		retLogEntry.Type = LogTypes.values()[reader.getInt(3)];
		// retLogEntry.TypeIcon = reader.getInt(3);
		retLogEntry.Comment = reader.getString(4);
		retLogEntry.Id = reader.getLong(5);

		if (filterBbCode)
		{
			int lIndex;

			while ((lIndex = retLogEntry.Comment.indexOf('[')) >= 0)
			{
				int rIndex = retLogEntry.Comment.indexOf(']', lIndex);

				if (rIndex == -1) break;

				retLogEntry.Comment = retLogEntry.Comment.substring(0, lIndex) + retLogEntry.Comment.substring(rIndex + 1);
			}
		}

		return retLogEntry;
	}

	public static String GetDescription(Cache cache)
	{
		String description = "";
		CoreCursor reader = Database.Data.rawQuery("select Description from Caches where Id=?", new String[]
			{ Long.toString(cache.Id) });
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			if (reader.getString(0) != null) description = reader.getString(0);
			reader.moveToNext();
		}
		reader.close();

		return description;
	}

	public static String Hint(Cache cache)
	{
		if (cache.hint.equals(""))
		{
			CoreCursor reader = Database.Data.rawQuery("select Hint from Caches where Id=?", new String[]
				{ Long.toString(cache.Id) });
			reader.moveToFirst();
			while (reader.isAfterLast() == false)
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
		// Coordinate fromPos = (Global.Marker.Valid) ? Global.Marker :
		// Global.LastValidPosition;
		Waypoint waypoint = cache.GetFinalWaypoint();
		// Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die
		// Diszanzberechnung vom Final aus gemacht werden
		// If a mystery has a final waypoint, the distance will be calculated to
		// the final not the the cache coordinates
		Coordinate toPos = cache.Pos;
		if (waypoint != null) toPos = new Coordinate(waypoint.Pos.Latitude, waypoint.Pos.Longitude);
		float[] dist = new float[4];
		Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, toPos.Latitude, toPos.Longitude, dist);
		return (float) dist[0];
	}

	public void GPXFilenameUpdateCacheCount()
	{
		// welche GPXFilenamen sind in der DB erfasst
		beginTransaction();
		try
		{
			CoreCursor reader = rawQuery(
					"select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID",
					null);
			reader.moveToFirst();

			while (reader.isAfterLast() == false)
			{
				long GPXFilename_ID = reader.getLong(0);
				long CacheCount = reader.getLong(1);

				Parameters val = new Parameters();
				val.put("CacheCount", CacheCount);
				update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);

				reader.moveToNext();
			}

			delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
			delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
			reader.close();
			setTransactionSuccessful();
		}
		finally
		{
			endTransaction();
		}

		CategoryDAO categoryDAO = new CategoryDAO();
		GlobalCore.Categories = new Categories();
		categoryDAO.LoadCategoriesFromDatabase(GlobalCore.Categories);
		// GlobalCore.Categories.ReadFromFilter(Global.LastFilter);
		// GlobalCore.Categories.DeleteEmptyCategories();
	}

	public void WriteConfigString(String key, String value)
	{
		Parameters val = new Parameters();
		val.put("Value", value);
		long anz = update("Config", val, "[Key]='" + key + "'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", key);
			insert("Config", val);
		}
	}

	public void WriteConfigLongString(String key, String value)
	{
		Parameters val = new Parameters();
		val.put("LongString", value);
		long anz = update("Config", val, "[Key]='" + key + "'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", key);
			insert("Config", val);
		}
	}

	public String ReadConfigString(String key) throws Exception
	{
		String result = "";
		CoreCursor c = null;
		boolean found = false;
		try
		{
			c = rawQuery("select Value from Config where [Key]=?", new String[]
				{ key });
		}
		catch (Exception exc)
		{
			throw new Exception("not in DB");
		}
		try
		{
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				result = c.getString(0);
				found = true;
				c.moveToNext();
			}
		}
		catch (Exception exc)
		{
			throw new Exception("not in DB");
		}
		finally
		{
			c.close();
		}

		if (!found) throw new Exception("not in DB");

		return result;
	}

	public String ReadConfigLongString(String key) throws Exception
	{
		String result = "";
		CoreCursor c = null;
		boolean found = false;
		try
		{
			c = rawQuery("select LongString from Config where [Key]=?", new String[]
				{ key });
		}
		catch (Exception exc)
		{
			throw new Exception("not in DB");
		}
		try
		{
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				result = c.getString(0);
				found = true;
				c.moveToNext();
			}
		}
		catch (Exception exc)
		{
			throw new Exception("not in DB");
		}
		c.close();

		if (!found) throw new Exception("not in DB");

		return result;
	}

	public void WriteConfigLong(String key, long value)
	{
		WriteConfigString(key, String.valueOf(value));
	}

	public long ReadConfigLong(String key)
	{
		try
		{
			String value = ReadConfigString(key);
			return Long.valueOf(value);
		}
		catch (Exception ex)
		{
			return 0;
		}
	}

	// Zur Parameter �bergabe and die DB
	public static class Parameters extends HashMap<String, Object>
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6506158947781669528L;
	}

	// DB Funktionen
	public abstract CoreCursor rawQuery(String sql, String[] args);

	public abstract void execSQL(String sql);

	public abstract long update(String tablename, Parameters val, String whereClause, String[] whereArgs);

	public abstract long insert(String tablename, Parameters val);

	public abstract long delete(String tablename, String whereClause, String[] whereArgs);

	public abstract void beginTransaction();

	public abstract void setTransactionSuccessful();

	public abstract void endTransaction();

	public abstract long insertWithConflictReplace(String tablename, Parameters val);

	public abstract long insertWithConflictIgnore(String tablename, Parameters val);

	public abstract void Close();

	public abstract int getCacheCountInDB(String filename);
}
