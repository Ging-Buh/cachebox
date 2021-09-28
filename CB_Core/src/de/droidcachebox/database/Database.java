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
package de.droidcachebox.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.SDBM_Hash;
import de.droidcachebox.utils.log.Log;

public abstract class Database extends Database_Core {
    private static final String log = "Database";
    public static Database Data;
    public static Database Drafts;
    public static Database Settings;
    private static CB_List<LogEntry> cacheLogs;
    private static String lastGeoCache;
    private final DatabaseType databaseType;
    public CacheList cacheList;

    public Database(DatabaseType databaseType) {
        super();
        this.databaseType = databaseType;

        switch (databaseType) {
            case CacheBox:
                latestDatabaseChange = DatabaseVersions.CachesDBLatestVersion;
                cacheList = new CacheList();
                cacheLogs = new CB_List<>();
                lastGeoCache = "";
                break;
            case Drafts:
                latestDatabaseChange = DatabaseVersions.DraftsLatestVersion;
                break;
            case Settings:
                latestDatabaseChange = DatabaseVersions.SettingsLatestVersion;
        }
    }

    // Methoden für Waypoint
    public static void deleteFromDatabase(Waypoint WP) {
        Replication.WaypointDelete(WP.geoCacheId, 0, 1, WP.getGcCode());
        try {
            Data.sql.delete("Waypoint", "GcCode='" + WP.getGcCode() + "'", null);
        } catch (Exception exc) {
            Log.err(log, "Waypoint.DeleteFromDataBase()", "", exc);
        }
    }

    // Methodes für Cache
    public static String getNote(Cache cache) {
        String resultString = getNote(cache.generatedId);
        cache.setNoteChecksum((int) SDBM_Hash.sdbm(resultString));
        return resultString;
    }

    public static String getNote(long cacheId) {
        String resultString = "";
        CoreCursor c = Database.Data.sql.rawQuery("select Notes from Caches where Id=?", new String[]{String.valueOf(cacheId)});
        c.moveToFirst();
        if (!c.isAfterLast()) {
            resultString = c.getString(0);
        }
        return resultString;
    }

    /**
     * geänderte Note nur in die DB schreiben
     *
     * @param cacheId ?
     * @param value   ?
     */
    public static void setNote(long cacheId, String value) {
        Parameters args = new Parameters();
        args.put("Notes", value);
        args.put("HasUserData", true);

        Database.Data.sql.update("Caches", args, "id=" + cacheId, null);
    }

    public static void setNote(Cache cache, String value) {
        int newNoteCheckSum = (int) SDBM_Hash.sdbm(value);

        Replication.NoteChanged(cache.generatedId, cache.getNoteChecksum(), newNoteCheckSum);
        if (newNoteCheckSum != cache.getNoteChecksum()) {
            setNote(cache.generatedId, value);
            cache.setNoteChecksum(newNoteCheckSum);
        }
    }

    public static String getSolver(Cache cache) {
        String resultString = getSolver(cache.generatedId);
        cache.setSolverChecksum((int) SDBM_Hash.sdbm(resultString));
        return resultString;
    }

    public static String getSolver(long cacheId) {
        try {
            String resultString = "";
            CoreCursor c = Database.Data.sql.rawQuery("select Solver from Caches where Id=?", new String[]{String.valueOf(cacheId)});
            c.moveToFirst();
            if (!c.isAfterLast()) {
                resultString = c.getString(0);
            }
            return resultString;
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * geänderten Solver nur in die DB schreiben
     */
    private static void setSolver(long cacheId, String value) {
        Parameters args = new Parameters();
        args.put("Solver", value);
        args.put("HasUserData", true);

        Database.Data.sql.update("Caches", args, "id=" + cacheId, null);
    }

    public static void setSolver(Cache cache, String value) {
        int newSolverCheckSum = (int) SDBM_Hash.sdbm(value);

        Replication.SolverChanged(cache.generatedId, cache.getSolverChecksum(), newSolverCheckSum);
        if (newSolverCheckSum != cache.getSolverChecksum()) {
            setSolver(cache.generatedId, value);
            cache.setSolverChecksum(newSolverCheckSum);
        }
    }

    public static void forceRereadingOfGeoCacheLogs() {
        lastGeoCache = "";
    }

    public static CB_List<LogEntry> getLogs(Cache cache) {
        if (cache == null || cache.isDisposed()) {
            cacheLogs.clear();
            return cacheLogs;
        }
        if (cache.getGeoCacheCode().equals(lastGeoCache)) return cacheLogs;
        lastGeoCache = cache.getGeoCacheCode();
        cacheLogs.clear();
        Log.info(log, "getLogs for cache: " + cache.getGeoCacheCode());
        CoreCursor reader = Database.Data.sql.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc", new String[]{Long.toString(cache.generatedId)});
        if (reader != null) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                LogEntry logent = getLogEntry(reader);
                if (logent != null)
                    cacheLogs.add(logent);
                reader.moveToNext();
            }
            reader.close();
        } else {
            lastGeoCache = "";
        }
        return cacheLogs;
    }

    private static LogEntry getLogEntry(CoreCursor reader) {
        int intLogType = reader.getInt(3);
        if (intLogType < 0 || intLogType > 13)
            return null;

        LogEntry retLogEntry = new LogEntry();

        retLogEntry.cacheId = reader.getLong(0);

        String sDate = reader.getString(1);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            retLogEntry.logDate = iso8601Format.parse(sDate);
        } catch (ParseException ignored) {
        }
        retLogEntry.finder = reader.getString(2);
        retLogEntry.logType = LogType.values()[reader.getInt(3)];
        // retLogEntry.TypeIcon = reader.getInt(3);
        retLogEntry.logText = reader.getString(4);
        retLogEntry.logId = reader.getLong(5);

        int lIndex;

        while ((lIndex = retLogEntry.logText.indexOf('[')) >= 0) {
            int rIndex = retLogEntry.logText.indexOf(']', lIndex);

            if (rIndex == -1)
                break;

            retLogEntry.logText = retLogEntry.logText.substring(0, lIndex) + retLogEntry.logText.substring(rIndex + 1);
        }

        return retLogEntry;
    }

    public static String getDescription(Cache cache) {
        String description = "";
        CoreCursor reader = Database.Data.sql.rawQuery("select Description from Caches where Id=?", new String[]{Long.toString(cache.generatedId)});
        if (reader == null)
            return "";
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            if (reader.getString(0) != null)
                description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();

        return description;
    }

    public static String getShortDescription(Cache cache) {
        String description = "";
        CoreCursor reader = Database.Data.sql.rawQuery("select ShortDescription from Caches where Id=?", new String[]{Long.toString(cache.generatedId)});
        if (reader == null)
            return "";
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            if (reader.getString(0) != null)
                description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();

        return description;
    }

    private static boolean waypointExists(String gcCode) {
        CoreCursor c = Database.Data.sql.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[]{gcCode});
        {
            c.moveToFirst();
            if (!c.isAfterLast()) {
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

    public static String createFreeGcCode(String cacheGcCode) throws Exception {
        String suffix = cacheGcCode.substring(2);
        String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
        String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < firstCharCandidates.length(); i++)
            for (int j = 0; j < secondCharCandidates.length(); j++) {
                String gcCode = firstCharCandidates.charAt(i) + secondCharCandidates.substring(j, j + 1) + suffix;
                if (!Database.Data.waypointExists(gcCode))
                    return gcCode;
            }
        throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
    }

    @Override
    public boolean startUp(String databasePath) {
        boolean result = super.startUp(databasePath);
        if (!result)
            return false;

        if (databaseType == DatabaseType.CacheBox) { // create or load DatabaseId for each
            DatabaseId = readConfigLong("DatabaseId");
            if (DatabaseId <= 0) {
                DatabaseId = new Date().getTime();
                writeConfigLong("DatabaseId", DatabaseId);
            }
            // Read MasterDatabaseId.
            // If MasterDatabaseId > 0 -> This database is connected to the Replications Master of WinCB
            // In this case changes of Waypoints, Solvertext, Notes must be noted in the Table Replication...
            MasterDatabaseId = readConfigLong("MasterDatabaseId");
        }
        return true;
    }

    @Override
    protected void alterDatabase(int lastDatabaseSchemeVersion) {
        super.alterDatabase(lastDatabaseSchemeVersion);

        switch (databaseType) {
            case CacheBox:

                sql.beginTransaction();
                try {
                    if (lastDatabaseSchemeVersion <= 0) {
                        // First Initialization of the Database
                        Data.sql.execSQL("CREATE TABLE [Caches] ([Id] bigint NOT NULL primary key,[GcCode] nvarchar (12) NULL,[GcId] nvarchar (255) NULL,[Latitude] float NULL,[Longitude] float NULL,[Name] nchar (255) NULL,[Size] int NULL,[Difficulty] smallint NULL,[Terrain] smallint NULL,[Archived] bit NULL,[Available] bit NULL,[Found] bit NULL,[Type] smallint NULL,[PlacedBy] nvarchar (255) NULL,[Owner] nvarchar (255) NULL,[DateHidden] datetime NULL,[Hint] ntext NULL,[Description] ntext NULL,[Url] nchar (255) NULL,[NumTravelbugs] smallint NULL,[Rating] smallint NULL,[Vote] smallint NULL,[VotePending] bit NULL,[Notes] ntext NULL,[Solver] ntext NULL,[Favorit] bit NULL,[AttributesPositive] bigint NULL,[AttributesNegative] bigint NULL,[TourName] nchar (255) NULL,[GPXFilename_Id] bigint NULL,[HasUserData] bit NULL,[ListingCheckSum] int NULL DEFAULT 0,[ListingChanged] bit NULL,[ImagesUpdated] bit NULL,[DescriptionImagesUpdated] bit NULL,[CorrectedCoordinates] bit NULL);");
                        Data.sql.execSQL("CREATE INDEX [archived_idx] ON [Caches] ([Archived] ASC);");
                        Data.sql.execSQL("CREATE INDEX [AttributesNegative_idx] ON [Caches] ([AttributesNegative] ASC);");
                        Data.sql.execSQL("CREATE INDEX [AttributesPositive_idx] ON [Caches] ([AttributesPositive] ASC);");
                        Data.sql.execSQL("CREATE INDEX [available_idx] ON [Caches] ([Available] ASC);");
                        Data.sql.execSQL("CREATE INDEX [Difficulty_idx] ON [Caches] ([Difficulty] ASC);");
                        Data.sql.execSQL("CREATE INDEX [Favorit_idx] ON [Caches] ([Favorit] ASC);");
                        Data.sql.execSQL("CREATE INDEX [found_idx] ON [Caches] ([Found] ASC);");
                        Data.sql.execSQL("CREATE INDEX [GPXFilename_Id_idx] ON [Caches] ([GPXFilename_Id] ASC);");
                        Data.sql.execSQL("CREATE INDEX [HasUserData_idx] ON [Caches] ([HasUserData] ASC);");
                        Data.sql.execSQL("CREATE INDEX [ListingChanged_idx] ON [Caches] ([ListingChanged] ASC);");
                        Data.sql.execSQL("CREATE INDEX [NumTravelbugs_idx] ON [Caches] ([NumTravelbugs] ASC);");
                        Data.sql.execSQL("CREATE INDEX [placedby_idx] ON [Caches] ([PlacedBy] ASC);");
                        Data.sql.execSQL("CREATE INDEX [Rating_idx] ON [Caches] ([Rating] ASC);");
                        Data.sql.execSQL("CREATE INDEX [Size_idx] ON [Caches] ([Size] ASC);");
                        Data.sql.execSQL("CREATE INDEX [Terrain_idx] ON [Caches] ([Terrain] ASC);");
                        Data.sql.execSQL("CREATE INDEX [Type_idx] ON [Caches] ([Type] ASC);");

                        Data.sql.execSQL("CREATE TABLE [CelltowerLocation] ([CellId] nvarchar (20) NOT NULL primary key,[Latitude] float NULL,[Longitude] float NULL);");

                        Data.sql.execSQL("CREATE TABLE [GPXFilenames] ([Id] integer not null primary key autoincrement,[GPXFilename] nvarchar (255) NULL,[Imported] datetime NULL, [Name] nvarchar (255) NULL,[CacheCount] int NULL);");

                        Data.sql.execSQL("CREATE TABLE [Logs] ([Id] bigint NOT NULL primary key, [CacheId] bigint NULL,[Timestamp] datetime NULL,[Finder] nvarchar (128) NULL,[Type] smallint NULL,[Comment] ntext NULL);");
                        Data.sql.execSQL("CREATE INDEX [log_idx] ON [Logs] ([CacheId] ASC);");
                        Data.sql.execSQL("CREATE INDEX [timestamp_idx] ON [Logs] ([Timestamp] ASC);");

                        Data.sql.execSQL("CREATE TABLE [PocketQueries] ([Id] integer not null primary key autoincrement,[PQName] nvarchar (255) NULL,[CreationTimeOfPQ] datetime NULL);");

                        Data.sql.execSQL("CREATE TABLE [Waypoint] ([GcCode] nvarchar (12) NOT NULL primary key,[CacheId] bigint NULL,[Latitude] float NULL,[Longitude] float NULL,[Description] ntext NULL,[Clue] ntext NULL,[Type] smallint NULL,[SyncExclude] bit NULL,[UserWaypoint] bit NULL,[Title] ntext NULL);");
                        Data.sql.execSQL("CREATE INDEX [UserWaypoint_idx] ON [Waypoint] ([UserWaypoint] ASC);");

                        Data.sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                        Data.sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");

                        Data.sql.execSQL("CREATE TABLE [Replication] ([Id] integer not null primary key autoincrement, [ChangeType] int NOT NULL, [CacheId] bigint NOT NULL, [WpGcCode] nvarchar (12) NULL, [SolverCheckSum] int NULL, [NotesCheckSum] int NULL, [WpCoordCheckSum] int NULL);");
                        Data.sql.execSQL("CREATE INDEX [Replication_idx] ON [Replication] ([Id] ASC);");
                        Data.sql.execSQL("CREATE INDEX [ReplicationCache_idx] ON [Replication] ([CacheId] ASC);");
                    }

                    if (lastDatabaseSchemeVersion < 1003) {
                        Data.sql.execSQL("CREATE TABLE [Locations] ([Id] integer not null primary key autoincrement, [Name] nvarchar (255) NULL, [Latitude] float NULL, [Longitude] float NULL);");
                        Data.sql.execSQL("CREATE INDEX [Locatioins_idx] ON [Locations] ([Id] ASC);");

                        Data.sql.execSQL("CREATE TABLE [SdfExport] ([Id]  integer not null primary key autoincrement, [Description] nvarchar(255) NULL, [ExportPath] nvarchar(255) NULL, [MaxDistance] float NULL, [LocationID] Bigint NULL, [Filter] ntext NULL, [Update] bit NULL, [ExportImages] bit NULL, [ExportSpoilers] bit NULL, [ExportMaps] bit NULL, [OwnRepository] bit NULL, [ExportMapPacks] bit NULL, [MaxLogs] int NULL);");
                        Data.sql.execSQL("CREATE INDEX [SdfExport_idx] ON [SdfExport] ([Id] ASC);");

                        Data.sql.execSQL("ALTER TABLE [CACHES] ADD [FirstImported] datetime NULL;");

                        Data.sql.execSQL("CREATE TABLE [Category] ([Id]  integer not null primary key autoincrement, [GpxFilename] nvarchar(255) NULL, [Pinned] bit NULL default 0, [CacheCount] int NULL);");
                        Data.sql.execSQL("CREATE INDEX [Category_idx] ON [Category] ([Id] ASC);");

                        Data.sql.execSQL("ALTER TABLE [GpxFilenames] ADD [CategoryId] bigint NULL;");

                        Data.sql.execSQL("ALTER TABLE [Caches] add [state] nvarchar(50) NULL;");
                        Data.sql.execSQL("ALTER TABLE [Caches] add [country] nvarchar(50) NULL;");
                    }
                    if (lastDatabaseSchemeVersion < 1015) {
                        // GpxFilenames mit Kategorien verknüpfen

                        // alte Category Tabelle löschen
                        sql.delete("Category", "", null);
                        HashMap<Long, String> gpxFilenames = new HashMap<>();
                        HashMap<String, Long> categories = new HashMap<>();

                        CoreCursor reader = Data.sql.rawQuery("select ID, GPXFilename from GPXFilenames", null);
                        reader.moveToFirst();
                        while (!reader.isAfterLast()) {
                            long id = reader.getLong(0);
                            String gpxFilename = reader.getString(1);
                            gpxFilenames.put(id, gpxFilename);
                            reader.moveToNext();
                        }
                        reader.close();
                        for (Entry<Long, String> entry : gpxFilenames.entrySet()) {
                            if (!categories.containsKey(entry.getValue())) {
                                // add new Category
                                Categories cs = new Categories();
                                Category category = cs.createNewCategory(entry.getValue());
                                // and store
                                categories.put(entry.getValue(), category.Id);
                            }
                            if (categories.containsKey(entry.getValue())) {
                                // and store CategoryId in GPXFilenames
                                Parameters args = new Parameters();
                                args.put("CategoryId", categories.get(entry.getValue()));
                                try {
                                    Database.Data.sql.update("GpxFilenames", args, "Id=" + entry.getKey(), null);
                                } catch (Exception exc) {
                                    Log.err(log, "Database", "Update_CategoryId", exc);
                                }
                            }
                        }

                    }
                    if (lastDatabaseSchemeVersion < 1016) {
                        Data.sql.execSQL("ALTER TABLE [CACHES] ADD [ApiStatus] smallint NULL default 0;");
                    }
                    if (lastDatabaseSchemeVersion < 1017) {
                        Data.sql.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (12) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                        Data.sql.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                        Data.sql.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (12) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                        Data.sql.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                        Data.sql.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1018) {
                        Data.sql.execSQL("ALTER TABLE [SdfExport] ADD [MapPacks] nvarchar(512) NULL;");

                    }
                    if (lastDatabaseSchemeVersion < 1019) {
                        // neue Felder für die erweiterten Attribute einfügen
                        Data.sql.execSQL("ALTER TABLE [CACHES] ADD [AttributesPositiveHigh] bigint NULL default 0");
                        Data.sql.execSQL("ALTER TABLE [CACHES] ADD [AttributesNegativeHigh] bigint NULL default 0");

                        // Die Nummerierung der Attribute stimmte nicht mit der von
                        // Groundspeak überein. Bei 16 und 45 wurde jeweils eine
                        // Nummber übersprungen
                        CoreCursor reader = Data.sql.rawQuery("select Id, AttributesPositive, AttributesNegative from Caches", new String[]{});
                        reader.moveToFirst();
                        while (!reader.isAfterLast()) {
                            long id = reader.getLong(0);
                            long attributesPositive = reader.getLong(1);
                            long attributesNegative = reader.getLong(2);

                            attributesPositive = convertAttribute(attributesPositive);
                            attributesNegative = convertAttribute(attributesNegative);

                            Parameters val = new Parameters();
                            val.put("AttributesPositive", attributesPositive);
                            val.put("AttributesNegative", attributesNegative);
                            String whereClause = "[Id]=" + id;
                            Data.sql.update("Caches", val, whereClause, null);
                            reader.moveToNext();
                        }
                        reader.close();

                    }
                    if (lastDatabaseSchemeVersion < 1020) {
                        // for long Settings
                        Data.sql.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");

                    }
                    if (lastDatabaseSchemeVersion < 1021) {
                        // Image Table
                        Data.sql.execSQL("CREATE TABLE [Images] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [Description] ntext, [Name] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [IsCacheImage] bit NULL);");
                        Data.sql.execSQL("CREATE INDEX [images_cacheid_idx] ON [Images] ([CacheId] ASC);");
                        Data.sql.execSQL("CREATE INDEX [images_gccode_idx] ON [Images] ([GcCode] ASC);");
                        Data.sql.execSQL("CREATE INDEX [images_iscacheimage_idx] ON [Images] ([IsCacheImage] ASC);");
                        Data.sql.execSQL("CREATE UNIQUE INDEX [images_imageurl_idx] ON [Images] ([ImageUrl] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1022) {
                        Data.sql.execSQL("ALTER TABLE [Caches] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");

                        Data.sql.execSQL("ALTER TABLE [Waypoint] DROP CONSTRAINT Waypoint_PK ");
                        Data.sql.execSQL("ALTER TABLE [Waypoint] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                        Data.sql.execSQL("ALTER TABLE [Waypoint] ADD CONSTRAINT  [Waypoint_PK] PRIMARY KEY ([GcCode]); ");

                        Data.sql.execSQL("ALTER TABLE [Replication] ALTER COLUMN [WpGcCode] nvarchar(15) NOT NULL; ");
                        Data.sql.execSQL("ALTER TABLE [Trackable] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                        Data.sql.execSQL("ALTER TABLE [TbLogs] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                        Data.sql.execSQL("ALTER TABLE [Images] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                    }
                    if (lastDatabaseSchemeVersion < 1024) {
                        Data.sql.execSQL("ALTER TABLE [Waypoint] ADD COLUMN [IsStart] BOOLEAN DEFAULT 'false' NULL");
                    }

                    if (lastDatabaseSchemeVersion < 1026) {
                        // add one column for short description
                        // [ShortDescription] ntext NULL
                        Data.sql.execSQL("ALTER TABLE [Caches] ADD [ShortDescription] ntext NULL;");
                    }

                    if (lastDatabaseSchemeVersion < 1027) {
                        // add one column for Favorite Points
                        // [FavPoints] SMALLINT 0
                        Data.sql.execSQL("ALTER TABLE [CACHES] ADD [FavPoints] smallint NULL default 0;");

                    }

                    Data.sql.setTransactionSuccessful();
                } catch (Exception exc) {
                    Log.err(log, "alterDatabase", "", exc);
                } finally {
                    Data.sql.endTransaction();
                }

                break;
            case Drafts:
                Drafts.sql.beginTransaction();
                try {

                    if (lastDatabaseSchemeVersion <= 0) {
                        // First Initialization of the Database
                        // FieldNotes Table
                        Drafts.sql.execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [Name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");

                        // Config Table
                        Drafts.sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                        Drafts.sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1002) {
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [Uploaded] BOOLEAN DEFAULT 'false' NULL");
                    }
                    if (lastDatabaseSchemeVersion < 1003) {
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [GC_Vote] integer default 0");
                    }
                    if (lastDatabaseSchemeVersion < 1004) {
                        Drafts.sql.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (15) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                        Drafts.sql.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                        Drafts.sql.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (15) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                        Drafts.sql.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                        Drafts.sql.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1005) {
                        Drafts.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [TypeName] ntext NULL");
                        Drafts.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [LastVisit] datetime NULL");
                        Drafts.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [Home] ntext NULL");
                        Drafts.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [TravelDistance] integer default 0");
                    }
                    if (lastDatabaseSchemeVersion < 1006) {
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbFieldNote] BOOLEAN DEFAULT 'false' NULL");
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbName] nvarchar (255)  NULL");
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbIconUrl] nvarchar (255)  NULL");
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TravelBugCode] nvarchar (15)  NULL");
                        Drafts.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TrackingNumber] nvarchar (15)  NULL");
                    }
                    if (lastDatabaseSchemeVersion < 1007) {
                        sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [directLog] BOOLEAN DEFAULT 'false' NULL");
                    }
                    Drafts.sql.setTransactionSuccessful();
                } catch (Exception exc) {
                    Log.err(log, "alterDatabase", "", exc);
                } finally {
                    Drafts.sql.endTransaction();
                }
                break;
            case Settings:
                Settings.sql.beginTransaction();
                try {
                    if (lastDatabaseSchemeVersion <= 0) {
                        // First Initialization of the Database
                        Settings.sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                        Settings.sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1002) {
                        // Long Text Field for long Strings
                        Settings.sql.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
                    }
                    Settings.sql.setTransactionSuccessful();
                } catch (Exception exc) {
                    Log.err(log, "alterDatabase", "", exc);
                } finally {
                    Settings.sql.endTransaction();
                }
                break;
        }
    }

    private long convertAttribute(long att) {
        // Die Nummerierung der Attribute stimmte nicht mit der von Groundspeak
        // überein. Bei 16 und 45 wurde jeweils eine Nummber übersprungen
        long result;
        // Maske für die untersten 15 bit
        long mask = 0;
        for (int i = 0; i < 16; i++)
            mask += (long) 1 << i;
        // unterste 15 bit ohne Verschiebung kopieren
        result = att & mask;
        // Maske für die Bits 16-45
        mask = 0;
        for (int i = 16; i < 45; i++)
            mask += (long) 1 << i;
        long tmp = att & mask;
        // Bits 16-44 um eins verschieben
        tmp = tmp << 1;
        // und zum Result kopieren
        result += tmp;
        // Maske für die Bits 45-45
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

    /**
     *
     */
    public void updateCacheCountForGPXFilenames() {
        // welche GPXFilenamen sind in der DB erfasst
        sql.beginTransaction();
        try {
            CoreCursor reader = sql.rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);
            reader.moveToFirst();

            while (!reader.isAfterLast()) {
                long GPXFilename_ID = reader.getLong(0);
                long CacheCount = reader.getLong(1);

                Parameters val = new Parameters();
                val.put("CacheCount", CacheCount);
                sql.update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);

                reader.moveToNext();
            }

            sql.delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
            sql.delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
            reader.close();
            sql.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            sql.endTransaction();
        }

        CategoryDAO.getInstance().loadCategoriesFromDatabase();
    }

    public int getCacheCountInDB() {
        CoreCursor reader = null;
        int count = 0;
        try {
            reader = Database.Data.sql.rawQuery("select count(*) from caches", null);
            reader.moveToFirst();
            count = reader.getInt(0);
        } catch (Exception e) {

            e.printStackTrace();
        }
        if (reader != null)
            reader.close();

        return count;
    }

    /**
     * @param minToKeep      Config.settings.LogMinCount.getValue()
     * @param LogMaxMonthAge Config.settings.LogMaxMonthAge.getValue()
     */
    public void deleteOldLogs(int minToKeep, int LogMaxMonthAge) {

        Log.debug(log, "deleteOldLogs but keep " + minToKeep + " and not older than " + LogMaxMonthAge);

        ArrayList<Long> oldLogCaches = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, -LogMaxMonthAge);
        // hint:
        // months are numbered from 0 onwards in Calendar
        // and month and day have leading zeroes in logs Timestamp
        String TimeStamp = (now.get(Calendar.YEAR)) + "-" + String.format("%02d", (now.get(Calendar.MONTH) + 1)) + "-" + String.format("%02d", now.get(Calendar.DATE));

        // #############################################################################
        // Get CacheId's from Caches with older logs and having more logs than minToKeep
        // #############################################################################
        {
            try {
                String command = "SELECT cacheid FROM logs WHERE Timestamp < '" + TimeStamp + "' GROUP BY CacheId HAVING COUNT(Id) > " + minToKeep;
                Log.debug(log, command);
                CoreCursor reader = Database.Data.sql.rawQuery(command, null);
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    long tmp = reader.getLong(0);
                    if (!oldLogCaches.contains(tmp))
                        oldLogCaches.add(reader.getLong(0));
                    reader.moveToNext();
                }
                reader.close();
            } catch (Exception ex) {
                Log.err(log, "deleteOldLogs", ex);
            }
        }

        // ###################################################
        // Get Logs
        // ###################################################
        {
            try {
                sql.beginTransaction();
                for (long oldLogCache : oldLogCaches) {
                    ArrayList<Long> minLogIds = new ArrayList<>();
                    String command = "select id from logs where cacheid = " + oldLogCache + " order by Timestamp desc";
                    Log.debug(log, command);
                    int count = 0;
                    CoreCursor reader = Database.Data.sql.rawQuery(command, null);
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        if (count == minToKeep)
                            break;
                        minLogIds.add(reader.getLong(0));
                        reader.moveToNext();
                        count++;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (long id : minLogIds)
                        sb.append(id).append(",");
                    // now delete all Logs out of Date but keep the ones in minLogIds
                    String delCommand;
                    if (sb.length() > 0)
                        delCommand = "DELETE FROM Logs WHERE Timestamp<'" + TimeStamp + "' AND cacheid = " + oldLogCache + " AND id NOT IN (" + sb.substring(0, sb.length() - 1) + ")";
                    else
                        delCommand = "DELETE FROM Logs WHERE Timestamp<'" + TimeStamp + "' AND cacheid = " + oldLogCache;
                    Log.debug(log, delCommand);
                    Database.Data.sql.execSQL(delCommand);
                }
                sql.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.err(log, "deleteOldLogs", ex);
            } finally {
                sql.endTransaction();
            }
        }
    }

    public enum DatabaseType {
        CacheBox, Drafts, Settings
    }

}
