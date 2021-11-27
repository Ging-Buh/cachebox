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

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.utils.log.Log;

public class CBDB extends Database_Core {
    private static final String sClass = "CBDB";
    private static CBDB cbdb;
    public final CacheList cacheList;

    private CBDB() {
        super();
        latestDatabaseChange = DatabaseVersions.CachesDBLatestVersion;
        cacheList = new CacheList();
        sql = PlatformUIBase.createSQLInstance();
        cbdb = this;
    }

    public static CBDB getInstance() {
        if (cbdb == null) {
            cbdb = new CBDB();
        }
        return cbdb;
    }

    @Override
    public void startUp(String databasePath) {
        synchronized (cacheList) {
            super.startUp(databasePath);
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

        sql.beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 0) {
                // First Initialization of the Database
                cbdb.sql.execSQL("CREATE TABLE [Caches] ([Id] bigint NOT NULL primary key,[GcCode] nvarchar (12) NULL,[GcId] nvarchar (255) NULL,[Latitude] float NULL,[Longitude] float NULL,[Name] nchar (255) NULL,[Size] int NULL,[Difficulty] smallint NULL,[Terrain] smallint NULL,[Archived] bit NULL,[Available] bit NULL,[Found] bit NULL,[Type] smallint NULL,[PlacedBy] nvarchar (255) NULL,[Owner] nvarchar (255) NULL,[DateHidden] datetime NULL,[Hint] ntext NULL,[Description] ntext NULL,[Url] nchar (255) NULL,[NumTravelbugs] smallint NULL,[Rating] smallint NULL,[Vote] smallint NULL,[VotePending] bit NULL,[Notes] ntext NULL,[Solver] ntext NULL,[Favorit] bit NULL,[AttributesPositive] bigint NULL,[AttributesNegative] bigint NULL,[TourName] nchar (255) NULL,[GPXFilename_Id] bigint NULL,[HasUserData] bit NULL,[ListingCheckSum] int NULL DEFAULT 0,[ListingChanged] bit NULL,[ImagesUpdated] bit NULL,[DescriptionImagesUpdated] bit NULL,[CorrectedCoordinates] bit NULL);");
                cbdb.sql.execSQL("CREATE INDEX [archived_idx] ON [Caches] ([Archived] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [AttributesNegative_idx] ON [Caches] ([AttributesNegative] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [AttributesPositive_idx] ON [Caches] ([AttributesPositive] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [available_idx] ON [Caches] ([Available] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [Difficulty_idx] ON [Caches] ([Difficulty] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [Favorit_idx] ON [Caches] ([Favorit] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [found_idx] ON [Caches] ([Found] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [GPXFilename_Id_idx] ON [Caches] ([GPXFilename_Id] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [HasUserData_idx] ON [Caches] ([HasUserData] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [ListingChanged_idx] ON [Caches] ([ListingChanged] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [NumTravelbugs_idx] ON [Caches] ([NumTravelbugs] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [placedby_idx] ON [Caches] ([PlacedBy] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [Rating_idx] ON [Caches] ([Rating] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [Size_idx] ON [Caches] ([Size] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [Terrain_idx] ON [Caches] ([Terrain] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [Type_idx] ON [Caches] ([Type] ASC);");

                cbdb.sql.execSQL("CREATE TABLE [CelltowerLocation] ([CellId] nvarchar (20) NOT NULL primary key,[Latitude] float NULL,[Longitude] float NULL);");

                cbdb.sql.execSQL("CREATE TABLE [GPXFilenames] ([Id] integer not null primary key autoincrement,[GPXFilename] nvarchar (255) NULL,[Imported] datetime NULL, [Name] nvarchar (255) NULL,[CacheCount] int NULL);");

                cbdb.sql.execSQL("CREATE TABLE [Logs] ([Id] bigint NOT NULL primary key, [CacheId] bigint NULL,[Timestamp] datetime NULL,[Finder] nvarchar (128) NULL,[Type] smallint NULL,[Comment] ntext NULL);");
                cbdb.sql.execSQL("CREATE INDEX [log_idx] ON [Logs] ([CacheId] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [timestamp_idx] ON [Logs] ([Timestamp] ASC);");

                cbdb.sql.execSQL("CREATE TABLE [PocketQueries] ([Id] integer not null primary key autoincrement,[PQName] nvarchar (255) NULL,[CreationTimeOfPQ] datetime NULL);");

                cbdb.sql.execSQL("CREATE TABLE [Waypoint] ([GcCode] nvarchar (12) NOT NULL primary key,[CacheId] bigint NULL,[Latitude] float NULL,[Longitude] float NULL,[Description] ntext NULL,[Clue] ntext NULL,[Type] smallint NULL,[SyncExclude] bit NULL,[UserWaypoint] bit NULL,[Title] ntext NULL);");
                cbdb.sql.execSQL("CREATE INDEX [UserWaypoint_idx] ON [Waypoint] ([UserWaypoint] ASC);");

                cbdb.sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                cbdb.sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");

                cbdb.sql.execSQL("CREATE TABLE [Replication] ([Id] integer not null primary key autoincrement, [ChangeType] int NOT NULL, [CacheId] bigint NOT NULL, [WpGcCode] nvarchar (12) NULL, [SolverCheckSum] int NULL, [NotesCheckSum] int NULL, [WpCoordCheckSum] int NULL);");
                cbdb.sql.execSQL("CREATE INDEX [Replication_idx] ON [Replication] ([Id] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [ReplicationCache_idx] ON [Replication] ([CacheId] ASC);");
            }

            if (lastDatabaseSchemeVersion < 1003) {
                cbdb.sql.execSQL("CREATE TABLE [Locations] ([Id] integer not null primary key autoincrement, [Name] nvarchar (255) NULL, [Latitude] float NULL, [Longitude] float NULL);");
                cbdb.sql.execSQL("CREATE INDEX [Locatioins_idx] ON [Locations] ([Id] ASC);");

                cbdb.sql.execSQL("CREATE TABLE [SdfExport] ([Id]  integer not null primary key autoincrement, [Description] nvarchar(255) NULL, [ExportPath] nvarchar(255) NULL, [MaxDistance] float NULL, [LocationID] Bigint NULL, [Filter] ntext NULL, [Update] bit NULL, [ExportImages] bit NULL, [ExportSpoilers] bit NULL, [ExportMaps] bit NULL, [OwnRepository] bit NULL, [ExportMapPacks] bit NULL, [MaxLogs] int NULL);");
                cbdb.sql.execSQL("CREATE INDEX [SdfExport_idx] ON [SdfExport] ([Id] ASC);");

                cbdb.sql.execSQL("ALTER TABLE [CACHES] ADD [FirstImported] datetime NULL;");

                cbdb.sql.execSQL("CREATE TABLE [Category] ([Id]  integer not null primary key autoincrement, [GpxFilename] nvarchar(255) NULL, [Pinned] bit NULL default 0, [CacheCount] int NULL);");
                cbdb.sql.execSQL("CREATE INDEX [Category_idx] ON [Category] ([Id] ASC);");

                cbdb.sql.execSQL("ALTER TABLE [GpxFilenames] ADD [CategoryId] bigint NULL;");

                cbdb.sql.execSQL("ALTER TABLE [Caches] add [state] nvarchar(50) NULL;");
                cbdb.sql.execSQL("ALTER TABLE [Caches] add [country] nvarchar(50) NULL;");
            }
            if (lastDatabaseSchemeVersion < 1015) {
                // GpxFilenames mit Kategorien verknüpfen

                // alte Category Tabelle löschen
                sql.delete("Category", "", null);
                HashMap<Long, String> gpxFilenames = new HashMap<>();
                HashMap<String, Long> categories = new HashMap<>();

                CoreCursor reader = cbdb.sql.rawQuery("select ID, GPXFilename from GPXFilenames", null);
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
                            CBDB.cbdb.sql.update("GpxFilenames", args, "Id=" + entry.getKey(), null);
                        } catch (Exception exc) {
                            Log.err(sClass, "Database", "Update_CategoryId", exc);
                        }
                    }
                }

            }
            if (lastDatabaseSchemeVersion < 1016) {
                cbdb.sql.execSQL("ALTER TABLE [CACHES] ADD [ApiStatus] smallint NULL default 0;");
            }
            if (lastDatabaseSchemeVersion < 1017) {
                cbdb.sql.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (12) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                cbdb.sql.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                cbdb.sql.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (12) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                cbdb.sql.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1018) {
                cbdb.sql.execSQL("ALTER TABLE [SdfExport] ADD [MapPacks] nvarchar(512) NULL;");

            }
            if (lastDatabaseSchemeVersion < 1019) {
                // neue Felder für die erweiterten Attribute einfügen
                cbdb.sql.execSQL("ALTER TABLE [CACHES] ADD [AttributesPositiveHigh] bigint NULL default 0");
                cbdb.sql.execSQL("ALTER TABLE [CACHES] ADD [AttributesNegativeHigh] bigint NULL default 0");

                // Die Nummerierung der Attribute stimmte nicht mit der von
                // Groundspeak überein. Bei 16 und 45 wurde jeweils eine
                // Nummber übersprungen
                CoreCursor reader = cbdb.sql.rawQuery("select Id, AttributesPositive, AttributesNegative from Caches", new String[]{});
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
                    cbdb.sql.update("Caches", val, whereClause, null);
                    reader.moveToNext();
                }
                reader.close();

            }
            if (lastDatabaseSchemeVersion < 1020) {
                // for long Settings
                cbdb.sql.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");

            }
            if (lastDatabaseSchemeVersion < 1021) {
                // Image Table
                cbdb.sql.execSQL("CREATE TABLE [Images] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [Description] ntext, [Name] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [IsCacheImage] bit NULL);");
                cbdb.sql.execSQL("CREATE INDEX [images_cacheid_idx] ON [Images] ([CacheId] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [images_gccode_idx] ON [Images] ([GcCode] ASC);");
                cbdb.sql.execSQL("CREATE INDEX [images_iscacheimage_idx] ON [Images] ([IsCacheImage] ASC);");
                cbdb.sql.execSQL("CREATE UNIQUE INDEX [images_imageurl_idx] ON [Images] ([ImageUrl] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1022) {
                cbdb.sql.execSQL("ALTER TABLE [Caches] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");

                cbdb.sql.execSQL("ALTER TABLE [Waypoint] DROP CONSTRAINT Waypoint_PK ");
                cbdb.sql.execSQL("ALTER TABLE [Waypoint] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                cbdb.sql.execSQL("ALTER TABLE [Waypoint] ADD CONSTRAINT  [Waypoint_PK] PRIMARY KEY ([GcCode]); ");

                cbdb.sql.execSQL("ALTER TABLE [Replication] ALTER COLUMN [WpGcCode] nvarchar(15) NOT NULL; ");
                cbdb.sql.execSQL("ALTER TABLE [Trackable] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                cbdb.sql.execSQL("ALTER TABLE [TbLogs] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                cbdb.sql.execSQL("ALTER TABLE [Images] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
            }
            if (lastDatabaseSchemeVersion < 1024) {
                cbdb.sql.execSQL("ALTER TABLE [Waypoint] ADD COLUMN [IsStart] BOOLEAN DEFAULT 'false' NULL");
            }

            if (lastDatabaseSchemeVersion < 1026) {
                // add one column for short description
                // [ShortDescription] ntext NULL
                cbdb.sql.execSQL("ALTER TABLE [Caches] ADD [ShortDescription] ntext NULL;");
            }

            if (lastDatabaseSchemeVersion < 1027) {
                // add one column for Favorite Points
                // [FavPoints] SMALLINT 0
                cbdb.sql.execSQL("ALTER TABLE [CACHES] ADD [FavPoints] smallint NULL default 0;");

            }

            cbdb.sql.setTransactionSuccessful();
        } catch (Exception exc) {
            Log.err(sClass, "alterDatabase", "", exc);
        } finally {
            cbdb.sql.endTransaction();
        }
    }

    @Override
    public void close() {
        databasePath = "";
        if (sql != null) sql.close();
        sql = null;
        cbdb = null;
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
        CoreCursor reader = null;
        int count = 0;
        try {
            reader = CBDB.cbdb.sql.rawQuery("select count(*) from caches", null);
            if (reader != null) {
                reader.moveToFirst();
                count = reader.getInt(0);
            }
            else count = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reader != null)
            reader.close();

        return count;
    }

}
