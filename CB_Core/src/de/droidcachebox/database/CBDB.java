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

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import de.droidcachebox.Platform;
import de.droidcachebox.dataclasses.CacheList;
import de.droidcachebox.dataclasses.Categories;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.utils.log.Log;

public class CBDB extends Database_Core {
    private static final String sClass = "CBDB";
    private static CBDB instance;
    public final static CacheList cacheList = new CacheList();

    private CBDB() {
        super();
        latestDatabaseChange = DatabaseVersions.CachesDBLatestVersion;
        sql = Platform.createSQLInstance();
        instance = this;
    }

    public static CBDB getInstance() {
        if (instance == null) {
            Log.info(sClass, "creator CBDB");
            instance = new CBDB();
        }
        return instance;
    }

    @Override
    public void startUp(String dbPathAndName) {
        synchronized (cacheList) {
            super.startUp(dbPathAndName);
            Log.info(sClass, "startUp " + dbPathAndName);
            cacheList.clear();
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
    }

    @Override
    protected void alterDatabase(int lastDatabaseSchemeVersion) {

        beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 0) {
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

            if (lastDatabaseSchemeVersion < 1003) {
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
            if (lastDatabaseSchemeVersion < 1015) {
                // GpxFilenames mit Kategorien verknüpfen

                // alte Category Tabelle löschen
                delete("Category", "", null);
                HashMap<Long, String> gpxFilenames = new HashMap<>();
                HashMap<String, Long> categories = new HashMap<>();

                CoreCursor c = rawQuery("select ID, GPXFilename from GPXFilenames", null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    long id = c.getLong(0);
                    String gpxFilename = c.getString(1);
                    gpxFilenames.put(id, gpxFilename);
                    c.moveToNext();
                }
                c.close();
                for (Entry<Long, String> entry : gpxFilenames.entrySet()) {
                    if (!categories.containsKey(entry.getValue())) {
                        // add new Category
                        Categories cs = new Categories();
                        Category category = cs.createNewCategory(entry.getValue());
                        // and store
                        categories.put(entry.getValue(), category.categoryId);
                    }
                    if (categories.containsKey(entry.getValue())) {
                        // and store CategoryId in GPXFilenames
                        Parameters args = new Parameters();
                        args.put("CategoryId", categories.get(entry.getValue()));
                        try {
                            update("GpxFilenames", args, "Id=" + entry.getKey(), null);
                        } catch (Exception exc) {
                            Log.err(sClass, "Database", "Update_CategoryId", exc);
                        }
                    }
                }

            }
            if (lastDatabaseSchemeVersion < 1016) {
                execSQL("ALTER TABLE [CACHES] ADD [ApiStatus] smallint NULL default 0;");
            }
            if (lastDatabaseSchemeVersion < 1017) {
                execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (12) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (12) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1018) {
                execSQL("ALTER TABLE [SdfExport] ADD [MapPacks] nvarchar(512) NULL;");

            }
            if (lastDatabaseSchemeVersion < 1019) {
                // neue Felder für die erweiterten Attribute einfügen
                execSQL("ALTER TABLE [CACHES] ADD [AttributesPositiveHigh] bigint NULL default 0");
                execSQL("ALTER TABLE [CACHES] ADD [AttributesNegativeHigh] bigint NULL default 0");

                // Die Nummerierung der Attribute stimmte nicht mit der von
                // Groundspeak überein. Bei 16 und 45 wurde jeweils eine
                // Nummber übersprungen
                CoreCursor c = rawQuery("select Id, AttributesPositive, AttributesNegative from Caches", new String[]{});
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    long id = c.getLong(0);
                    long attributesPositive = c.getLong(1);
                    long attributesNegative = c.getLong(2);

                    attributesPositive = convertAttribute(attributesPositive);
                    attributesNegative = convertAttribute(attributesNegative);

                    Parameters val = new Parameters();
                    val.put("AttributesPositive", attributesPositive);
                    val.put("AttributesNegative", attributesNegative);
                    String whereClause = "[Id]=" + id;
                    update("Caches", val, whereClause, null);
                    c.moveToNext();
                }
                c.close();

            }
            if (lastDatabaseSchemeVersion < 1020) {
                // for long Settings
                execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");

            }
            if (lastDatabaseSchemeVersion < 1021) {
                // Image Table
                execSQL("CREATE TABLE [Images] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [Description] ntext, [Name] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [IsCacheImage] bit NULL);");
                execSQL("CREATE INDEX [images_cacheid_idx] ON [Images] ([CacheId] ASC);");
                execSQL("CREATE INDEX [images_gccode_idx] ON [Images] ([GcCode] ASC);");
                execSQL("CREATE INDEX [images_iscacheimage_idx] ON [Images] ([IsCacheImage] ASC);");
                execSQL("CREATE UNIQUE INDEX [images_imageurl_idx] ON [Images] ([ImageUrl] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1022) {
                execSQL("ALTER TABLE [Caches] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");

                execSQL("ALTER TABLE [Waypoint] DROP CONSTRAINT Waypoint_PK ");
                execSQL("ALTER TABLE [Waypoint] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                execSQL("ALTER TABLE [Waypoint] ADD CONSTRAINT  [Waypoint_PK] PRIMARY KEY ([GcCode]); ");

                execSQL("ALTER TABLE [Replication] ALTER COLUMN [WpGcCode] nvarchar(15) NOT NULL; ");
                execSQL("ALTER TABLE [Trackable] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                execSQL("ALTER TABLE [TbLogs] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                execSQL("ALTER TABLE [Images] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
            }
            if (lastDatabaseSchemeVersion < 1024) {
                execSQL("ALTER TABLE [Waypoint] ADD COLUMN [IsStart] BOOLEAN DEFAULT 'false' NULL");
            }

            if (lastDatabaseSchemeVersion < 1026) {
                // add one column for short description
                // [ShortDescription] ntext NULL
                execSQL("ALTER TABLE [Caches] ADD [ShortDescription] ntext NULL;");
            }

            if (lastDatabaseSchemeVersion < 1027) {
                // add one column for Favorite Points
                // [FavPoints] SMALLINT 0
                execSQL("ALTER TABLE [CACHES] ADD [FavPoints] smallint NULL default 0;");

            }

            setTransactionSuccessful();
        } catch (Exception exc) {
            Log.err(sClass, "alterDatabase", "", exc);
        } finally {
            endTransaction();
        }
    }

    @Override
    public void close() {
        Log.info(sClass, "closing " + databasePath);
        databasePath = "";
        if (sql != null) sql.close();
        sql = null;
        instance = null;
        isOpen = false;
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

    public int getCacheCountInDB() {
        try {
            Log.info(sClass, "getCacheCountInDB");
            int count = 0;
            CoreCursor reader = rawQuery("select count(*) from caches", null);
            if (reader != null) {
                if (reader.getCount() > 0) {
                    reader.moveToFirst();
                    count = reader.getInt(0);
                }
                reader.close();
                return count;
            }
        } catch (Exception e) {
            Log.err(sClass, "getCacheCountInDB", e);
        }
        return 0;
    }

}
