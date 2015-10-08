/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Core.DB;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import CB_Core.DAO.CategoryDAO;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.SDBM_Hash;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core;
import de.cb.sqlite.Parameters;
import de.cb.sqlite.SQLite;

public class Database_Data extends Database_Core
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Database_Data.class);
	public static Database_Data Data;
	public CacheList Query;

	private Object MasterDatabaseId;

	public Database_Data(SQLite database)
	{
		super(database);
		database.setLatestDatabaseChange(Database.LatestDatabaseChange);
		Query = new CacheList();
		Data = this;

	}

	@Override
	public boolean StartUp()
	{
		boolean result = this.db.StartUp();
		if (!result) return false;
		// create or load DatabaseId for each
		long DatabaseId = this.db.ReadConfigLong("DatabaseId");
		if (DatabaseId <= 0)
		{
			DatabaseId = new Date().getTime();
			this.db.WriteConfigLong("DatabaseId", DatabaseId);
		}
		// Read MasterDatabaseId. If MasterDatabaseId > 0 -> This database
		// is connected to the Replications Master of WinCB
		// In this case changes of Waypoints, Solvertext, Notes must be
		// noted in the Table Replication...
		MasterDatabaseId = this.db.ReadConfigLong("MasterDatabaseId");

		return result;
	}

	@Override
	public void AlterDatabase(int lastDatabaseSchemeVersion)
	{
		this.db.AlterDatabase(lastDatabaseSchemeVersion);
		this.db.beginTransaction();
		try
		{
			if (lastDatabaseSchemeVersion <= 0)
			{
				// First Initialization of the Database
				this.db.execSQL(
						"CREATE TABLE [Caches] ([Id] bigint NOT NULL primary key,[GcCode] nvarchar (12) NULL,[GcId] nvarchar (255) NULL,[Latitude] float NULL,[Longitude] float NULL,[Name] nchar (255) NULL,[Size] int NULL,[Difficulty] smallint NULL,[Terrain] smallint NULL,[Archived] bit NULL,[Available] bit NULL,[Found] bit NULL,[Type] smallint NULL,[PlacedBy] nvarchar (255) NULL,[Owner] nvarchar (255) NULL,[DateHidden] datetime NULL,[Hint] ntext NULL,[Description] ntext NULL,[Url] nchar (255) NULL,[NumTravelbugs] smallint NULL,[Rating] smallint NULL,[Vote] smallint NULL,[VotePending] bit NULL,[Notes] ntext NULL,[Solver] ntext NULL,[Favorit] bit NULL,[AttributesPositive] bigint NULL,[AttributesNegative] bigint NULL,[TourName] nchar (255) NULL,[GPXFilename_Id] bigint NULL,[HasUserData] bit NULL,[ListingCheckSum] int NULL DEFAULT 0,[ListingChanged] bit NULL,[ImagesUpdated] bit NULL,[DescriptionImagesUpdated] bit NULL,[CorrectedCoordinates] bit NULL);");
				this.db.execSQL("CREATE INDEX [archived_idx] ON [Caches] ([Archived] ASC);");
				this.db.execSQL("CREATE INDEX [AttributesNegative_idx] ON [Caches] ([AttributesNegative] ASC);");
				this.db.execSQL("CREATE INDEX [AttributesPositive_idx] ON [Caches] ([AttributesPositive] ASC);");
				this.db.execSQL("CREATE INDEX [available_idx] ON [Caches] ([Available] ASC);");
				this.db.execSQL("CREATE INDEX [Difficulty_idx] ON [Caches] ([Difficulty] ASC);");
				this.db.execSQL("CREATE INDEX [Favorit_idx] ON [Caches] ([Favorit] ASC);");
				this.db.execSQL("CREATE INDEX [found_idx] ON [Caches] ([Found] ASC);");
				this.db.execSQL("CREATE INDEX [GPXFilename_Id_idx] ON [Caches] ([GPXFilename_Id] ASC);");
				this.db.execSQL("CREATE INDEX [HasUserData_idx] ON [Caches] ([HasUserData] ASC);");
				this.db.execSQL("CREATE INDEX [ListingChanged_idx] ON [Caches] ([ListingChanged] ASC);");
				this.db.execSQL("CREATE INDEX [NumTravelbugs_idx] ON [Caches] ([NumTravelbugs] ASC);");
				this.db.execSQL("CREATE INDEX [placedby_idx] ON [Caches] ([PlacedBy] ASC);");
				this.db.execSQL("CREATE INDEX [Rating_idx] ON [Caches] ([Rating] ASC);");
				this.db.execSQL("CREATE INDEX [Size_idx] ON [Caches] ([Size] ASC);");
				this.db.execSQL("CREATE INDEX [Terrain_idx] ON [Caches] ([Terrain] ASC);");
				this.db.execSQL("CREATE INDEX [Type_idx] ON [Caches] ([Type] ASC);");

				this.db.execSQL("CREATE TABLE [CelltowerLocation] ([CellId] nvarchar (20) NOT NULL primary key,[Latitude] float NULL,[Longitude] float NULL);");

				this.db.execSQL("CREATE TABLE [GPXFilenames] ([Id] integer not null primary key autoincrement,[GPXFilename] nvarchar (255) NULL,[Imported] datetime NULL, [Name] nvarchar (255) NULL,[CacheCount] int NULL);");

				this.db.execSQL("CREATE TABLE [Logs] ([Id] bigint NOT NULL primary key, [CacheId] bigint NULL,[Timestamp] datetime NULL,[Finder] nvarchar (128) NULL,[Type] smallint NULL,[Comment] ntext NULL);");
				this.db.execSQL("CREATE INDEX [log_idx] ON [Logs] ([CacheId] ASC);");
				this.db.execSQL("CREATE INDEX [timestamp_idx] ON [Logs] ([Timestamp] ASC);");

				this.db.execSQL("CREATE TABLE [PocketQueries] ([Id] integer not null primary key autoincrement,[PQName] nvarchar (255) NULL,[CreationTimeOfPQ] datetime NULL);");

				this.db.execSQL("CREATE TABLE [Waypoint] ([GcCode] nvarchar (12) NOT NULL primary key,[CacheId] bigint NULL,[Latitude] float NULL,[Longitude] float NULL,[Description] ntext NULL,[Clue] ntext NULL,[Type] smallint NULL,[SyncExclude] bit NULL,[UserWaypoint] bit NULL,[Title] ntext NULL);");
				this.db.execSQL("CREATE INDEX [UserWaypoint_idx] ON [Waypoint] ([UserWaypoint] ASC);");

				this.db.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
				this.db.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");

				this.db.execSQL("CREATE TABLE [Replication] ([Id] integer not null primary key autoincrement, [ChangeType] int NOT NULL, [CacheId] bigint NOT NULL, [WpGcCode] nvarchar (12) NULL, [SolverCheckSum] int NULL, [NotesCheckSum] int NULL, [WpCoordCheckSum] int NULL);");
				this.db.execSQL("CREATE INDEX [Replication_idx] ON [Replication] ([Id] ASC);");
				this.db.execSQL("CREATE INDEX [ReplicationCache_idx] ON [Replication] ([CacheId] ASC);");
			}

			if (lastDatabaseSchemeVersion < 1003)
			{
				this.db.execSQL("CREATE TABLE [Locations] ([Id] integer not null primary key autoincrement, [Name] nvarchar (255) NULL, [Latitude] float NULL, [Longitude] float NULL);");
				this.db.execSQL("CREATE INDEX [Locatioins_idx] ON [Locations] ([Id] ASC);");

				this.db.execSQL("CREATE TABLE [SdfExport] ([Id]  integer not null primary key autoincrement, [Description] nvarchar(255) NULL, [ExportPath] nvarchar(255) NULL, [MaxDistance] float NULL, [LocationID] Bigint NULL, [Filter] ntext NULL, [Update] bit NULL, [ExportImages] bit NULL, [ExportSpoilers] bit NULL, [ExportMaps] bit NULL, [OwnRepository] bit NULL, [ExportMapPacks] bit NULL, [MaxLogs] int NULL);");
				this.db.execSQL("CREATE INDEX [SdfExport_idx] ON [SdfExport] ([Id] ASC);");

				this.db.execSQL("ALTER TABLE [CACHES] ADD [FirstImported] datetime NULL;");

				this.db.execSQL("CREATE TABLE [Category] ([Id]  integer not null primary key autoincrement, [GpxFilename] nvarchar(255) NULL, [Pinned] bit NULL default 0, [CacheCount] int NULL);");
				this.db.execSQL("CREATE INDEX [Category_idx] ON [Category] ([Id] ASC);");

				this.db.execSQL("ALTER TABLE [GpxFilenames] ADD [CategoryId] bigint NULL;");

				this.db.execSQL("ALTER TABLE [Caches] add [state] nvarchar(50) NULL;");
				this.db.execSQL("ALTER TABLE [Caches] add [country] nvarchar(50) NULL;");
			}
			if (lastDatabaseSchemeVersion < 1015)
			{
				// GpxFilenames mit Kategorien verkn�pfen

				// alte Category Tabelle l�schen
				this.db.delete("Category", "", null);
				HashMap<Long, String> gpxFilenames = new HashMap<Long, String>();
				HashMap<String, Long> categories = new HashMap<String, Long>();

				CoreCursor reader = this.db.rawQuery("select ID, GPXFilename from GPXFilenames", null);
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
							this.db.update("GpxFilenames", args, "Id=" + entry.getKey(), null);
						}
						catch (Exception exc)
						{
							log.error("Database", "Update_CategoryId", exc);
						}
					}
				}

			}
			if (lastDatabaseSchemeVersion < 1016)
			{
				this.db.execSQL("ALTER TABLE [CACHES] ADD [ApiStatus] smallint NULL default 0;");
			}
			if (lastDatabaseSchemeVersion < 1017)
			{
				this.db.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (12) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
				this.db.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
				this.db.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (12) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
				this.db.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
				this.db.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
			}
			if (lastDatabaseSchemeVersion < 1018)
			{
				this.db.execSQL("ALTER TABLE [SdfExport] ADD [MapPacks] nvarchar(512) NULL;");

			}
			if (lastDatabaseSchemeVersion < 1019)
			{
				// neue Felder f�r die erweiterten Attribute einf�gen
				this.db.execSQL("ALTER TABLE [CACHES] ADD [AttributesPositiveHigh] bigint NULL default 0");
				this.db.execSQL("ALTER TABLE [CACHES] ADD [AttributesNegativeHigh] bigint NULL default 0");

				// Die Nummerierung der Attribute stimmte nicht mit der von
				// Groundspeak �berein. Bei 16 und 45 wurde jeweils eine
				// Nummber �bersprungen
				CoreCursor reader = this.db.rawQuery("select Id, AttributesPositive, AttributesNegative from Caches", new String[] {});
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
					this.db.update("Caches", val, whereClause, null);
					reader.moveToNext();
				}
				reader.close();

			}
			if (lastDatabaseSchemeVersion < 1020)
			{
				// for long Settings
				this.db.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");

			}
			if (lastDatabaseSchemeVersion < 1021)
			{
				// Image Table
				this.db.execSQL("CREATE TABLE [Images] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [Description] ntext, [Name] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [IsCacheImage] bit NULL);");
				this.db.execSQL("CREATE INDEX [images_cacheid_idx] ON [Images] ([CacheId] ASC);");
				this.db.execSQL("CREATE INDEX [images_gccode_idx] ON [Images] ([GcCode] ASC);");
				this.db.execSQL("CREATE INDEX [images_iscacheimage_idx] ON [Images] ([IsCacheImage] ASC);");
				this.db.execSQL("CREATE UNIQUE INDEX [images_imageurl_idx] ON [Images] ([ImageUrl] ASC);");
			}
			if (lastDatabaseSchemeVersion < 1022)
			{
				this.db.execSQL("ALTER TABLE [Caches] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");

				this.db.execSQL("ALTER TABLE [Waypoint] DROP CONSTRAINT Waypoint_PK ");
				this.db.execSQL("ALTER TABLE [Waypoint] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
				this.db.execSQL("ALTER TABLE [Waypoint] ADD CONSTRAINT  [Waypoint_PK] PRIMARY KEY ([GcCode]); ");

				this.db.execSQL("ALTER TABLE [Replication] ALTER COLUMN [WpGcCode] nvarchar(15) NOT NULL; ");
				this.db.execSQL("ALTER TABLE [Trackable] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
				this.db.execSQL("ALTER TABLE [TbLogs] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
				this.db.execSQL("ALTER TABLE [Images] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
			}
			if (lastDatabaseSchemeVersion < 1024)
			{
				this.db.execSQL("ALTER TABLE [Waypoint] ADD COLUMN [IsStart] BOOLEAN DEFAULT 'false' NULL");
			}
			if (lastDatabaseSchemeVersion < 1025)
			{
				// nicht mehr ben�tigt execSQL("ALTER TABLE [Waypoint] ADD COLUMN [UserNote] ntext NULL");
			}

			if (lastDatabaseSchemeVersion < 1026)
			{
				// add one column for short description
				// [ShortDescription] ntext NULL
				this.db.execSQL("ALTER TABLE [Caches] ADD [ShortDescription] ntext NULL;");
			}

			this.db.setTransactionSuccessful();
		}
		catch (Exception exc)
		{
			log.error("AlterDatabase", "", exc);
		}
		finally
		{
			this.db.endTransaction();
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

	// Methoden f�r Waypoint
	public void DeleteFromDatabase(Waypoint WP)
	{
		Replication.WaypointDelete(WP.CacheId, 0, 1, WP.getGcCode());
		try
		{
			this.db.delete("Waypoint", "GcCode='" + WP.getGcCode() + "'", null);
		}
		catch (Exception exc)
		{
			log.error("Waypoint.DeleteFromDataBase()", "", exc);
		}
	}

	public boolean WaypointExists(String gcCode)
	{
		CoreCursor c = this.db.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[]
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

	public String CreateFreeGcCode(String cacheGcCode) throws Exception
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
	public String GetNote(Cache cache)
	{
		String resultString = GetNote(cache.Id);
		cache.setNoteChecksum((int) SDBM_Hash.sdbm(resultString));
		return resultString;
	}

	public String GetNote(long cacheId)
	{
		String resultString = "";
		CoreCursor c = this.db.rawQuery("select Notes from Caches where Id=?", new String[]
			{ String.valueOf(cacheId) });
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			resultString = c.getString(0);
			break;
		}
		return resultString;
	}

	/**
	 * ge�nderte Note nur in die DB schreiben
	 * 
	 * @param cacheId
	 * @param value
	 */
	public void SetNote(long cacheId, String value)
	{
		Parameters args = new Parameters();
		args.put("Notes", value);
		args.put("HasUserData", true);
		this.db.update("Caches", args, "id=" + cacheId, null);
	}

	public void SetNote(Cache cache, String value)
	{
		int newNoteCheckSum = (int) SDBM_Hash.sdbm(value);

		Replication.NoteChanged(cache.Id, cache.getNoteChecksum(), newNoteCheckSum);
		if (newNoteCheckSum != cache.getNoteChecksum())
		{
			SetNote(cache.Id, value);
			cache.setNoteChecksum(newNoteCheckSum);
		}
	}

	public void SetFound(long cacheId, boolean value)
	{
		Parameters args = new Parameters();
		args.put("found", value);
		this.db.update("Caches", args, "id=" + cacheId, null);
	}

	public String GetSolver(Cache cache)
	{
		String resultString = GetSolver(cache.Id);
		cache.setSolverChecksum((int) SDBM_Hash.sdbm(resultString));
		return resultString;
	}

	public String GetSolver(long cacheId)
	{
		try
		{
			String resultString = "";
			CoreCursor c = this.db.rawQuery("select Solver from Caches where Id=?", new String[]
				{ String.valueOf(cacheId) });
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				resultString = c.getString(0);
				break;
			}
			return resultString;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/**
	 * ge�nderten Solver nur in die DB schreiben
	 * 
	 * @param cacheId
	 * @param value
	 */
	public void SetSolver(long cacheId, String value)
	{
		Parameters args = new Parameters();
		args.put("Solver", value);
		args.put("HasUserData", true);

		this.db.update("Caches", args, "id=" + cacheId, null);
	}

	public void SetSolver(Cache cache, String value)
	{
		int newSolverCheckSum = (int) SDBM_Hash.sdbm(value);

		Replication.SolverChanged(cache.Id, cache.getSolverChecksum(), newSolverCheckSum);
		if (newSolverCheckSum != cache.getSolverChecksum())
		{
			SetSolver(cache.Id, value);
			cache.setSolverChecksum(newSolverCheckSum);
		}
	}

	public CB_List<LogEntry> Logs(Cache cache)
	{
		CB_List<LogEntry> result = new CB_List<LogEntry>();
		if (cache == null) // if no cache is selected!
			return result;
		CoreCursor reader = this.db.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc", new String[]
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
		retLogEntry.Type = CB_Core.Enums.LogTypes.values()[reader.getInt(3)];
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

	public String GetDescription(Cache cache)
	{
		String description = "";
		CoreCursor reader = this.db.rawQuery("select Description from Caches where Id=?", new String[]
			{ Long.toString(cache.Id) });
		if (reader == null) return "";
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			if (reader.getString(0) != null) description = reader.getString(0);
			reader.moveToNext();
		}
		reader.close();

		return description;
	}

	public String GetShortDescription(Cache cache)
	{
		String description = "";
		CoreCursor reader = this.db.rawQuery("select ShortDescription from Caches where Id=?", new String[]
			{ Long.toString(cache.Id) });
		if (reader == null) return "";
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			if (reader.getString(0) != null) description = reader.getString(0);
			reader.moveToNext();
		}
		reader.close();

		return description;
	}

	/**
	 * @return Set To GlobalCore.Categories
	 */
	public Categories GPXFilenameUpdateCacheCount()
	{
		// welche GPXFilenamen sind in der DB erfasst
		this.db.beginTransaction();
		try
		{
			CoreCursor reader = this.db.rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);
			reader.moveToFirst();

			while (reader.isAfterLast() == false)
			{
				long GPXFilename_ID = reader.getLong(0);
				long CacheCount = reader.getLong(1);

				Parameters val = new Parameters();
				val.put("CacheCount", CacheCount);
				this.db.update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);

				reader.moveToNext();
			}

			this.db.delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
			this.db.delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
			reader.close();
			this.db.setTransactionSuccessful();
		}
		catch (Exception e)
		{

		}
		finally
		{
			this.db.endTransaction();
		}

		CategoryDAO categoryDAO = new CategoryDAO();
		Categories categories = new Categories();
		categoryDAO.LoadCategoriesFromDatabase();
		return categories;
	}

	public int getCacheCountInDB()
	{
		CoreCursor reader = null;
		int count = 0;
		try
		{
			reader = this.db.rawQuery("select count(*) from caches", null);
			reader.moveToFirst();
			count = reader.getInt(0);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (reader != null) reader.close();

		return count;
	}

	/**
	 * @param minToKeep
	 *            Config.settings.LogMinCount.getValue()
	 * @param LogMaxMonthAge
	 *            Config.settings.LogMaxMonthAge.getValue()
	 */
	public void DeleteOldLogs(int minToKeep, int LogMaxMonthAge)
	{

		if (LogMaxMonthAge == 0)
		{
			// Setting are 'immediately'
			// Delete all Logs and return
			// TODO implement this
		}

		ArrayList<Long> oldLogCaches = new ArrayList<Long>();
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MONTH, -LogMaxMonthAge);
		String TimeStamp = (now.get(Calendar.YEAR)) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DATE);

		// ###################################################
		// Get CacheId's from Caches with to match older Logs
		// ###################################################
		{
			String command = "select cacheid from logs WHERE Timestamp < '" + TimeStamp + "' GROUP BY CacheId HAVING COUNT(Id) > " + String.valueOf(minToKeep);

			CoreCursor reader = this.db.rawQuery(command, null);
			reader.moveToFirst();
			while (reader.isAfterLast() == false)
			{
				long tmp = reader.getLong(0);
				if (!oldLogCaches.contains(tmp)) oldLogCaches.add(reader.getLong(0));
				reader.moveToNext();
			}
			reader.close();
		}

		// ###################################################
		// Get Logs
		// ###################################################
		{
			this.db.beginTransaction();
			try
			{
				for (long oldLogCache : oldLogCaches)
				{
					ArrayList<Long> minLogIds = new ArrayList<Long>();

					String command = "select id from logs where cacheid = " + String.valueOf(oldLogCache) + " order by Timestamp desc";

					int count = 0;
					CoreCursor reader = this.db.rawQuery(command, null);
					reader.moveToFirst();
					while (reader.isAfterLast() == false)
					{
						if (count == minToKeep) break;
						minLogIds.add(reader.getLong(0));
						reader.moveToNext();
						count++;
					}

					StringBuilder sb = new StringBuilder();
					for (long id : minLogIds)
						sb.append(id).append(",");

					// now delete all Logs out of Date without minLogIds
					String delCommand = "delete from Logs where Timestamp<'" + TimeStamp + "' and cacheid = " + String.valueOf(oldLogCache) + " and id not in (" + sb.toString().substring(0, sb.length() - 1) + ")";
					this.db.execSQL(delCommand);
				}
				this.db.setTransactionSuccessful();
			}
			catch (Exception ex)
			{
				log.error("Delete Old Logs", "", ex);
			}
			finally
			{
				this.db.endTransaction();
			}
		}
	}

	@Override
	public void Initialize()
	{
		db.Initialize();
	}

	@Override
	public void Reset()
	{

	}

	@Override
	public void Close()
	{

	}

}
