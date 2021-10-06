package de.droidcachebox.database;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.utils.log.Log;

public class DraftsDatabase extends Database_Core {
        private static final String sKlasse = "DraftsDatabase";
        private static DraftsDatabase draftsDatabase;

        private DraftsDatabase() {
                super();
                latestDatabaseChange = DatabaseVersions.DraftsLatestVersion;
                sql = PlatformUIBase.getSQLInstance();
                draftsDatabase = this;
        }

        public static DraftsDatabase getInstance() {
                if (draftsDatabase == null) draftsDatabase = new DraftsDatabase();
                return draftsDatabase;
        }

        @Override
        protected void alterDatabase(int lastDatabaseSchemeVersion) {
                draftsDatabase.sql.beginTransaction();
                try {

                        if (lastDatabaseSchemeVersion <= 0) {
                                // First Initialization of the Database
                                // FieldNotes Table
                                draftsDatabase.sql.execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [Name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");

                                // Config Table
                                draftsDatabase.sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                                draftsDatabase.sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
                        }
                        if (lastDatabaseSchemeVersion < 1002) {
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [Uploaded] BOOLEAN DEFAULT 'false' NULL");
                        }
                        if (lastDatabaseSchemeVersion < 1003) {
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [GC_Vote] integer default 0");
                        }
                        if (lastDatabaseSchemeVersion < 1004) {
                                draftsDatabase.sql.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (15) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                                draftsDatabase.sql.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                                draftsDatabase.sql.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (15) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                                draftsDatabase.sql.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                                draftsDatabase.sql.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
                        }
                        if (lastDatabaseSchemeVersion < 1005) {
                                draftsDatabase.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [TypeName] ntext NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [LastVisit] datetime NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [Home] ntext NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [Trackable] ADD COLUMN [TravelDistance] integer default 0");
                        }
                        if (lastDatabaseSchemeVersion < 1006) {
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbFieldNote] BOOLEAN DEFAULT 'false' NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbName] nvarchar (255)  NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbIconUrl] nvarchar (255)  NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TravelBugCode] nvarchar (15)  NULL");
                                draftsDatabase.sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TrackingNumber] nvarchar (15)  NULL");
                        }
                        if (lastDatabaseSchemeVersion < 1007) {
                                sql.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [directLog] BOOLEAN DEFAULT 'false' NULL");
                        }
                        draftsDatabase.sql.setTransactionSuccessful();
                } catch (Exception exc) {
                        Log.err(sKlasse, "alterDatabase", "", exc);
                } finally {
                        draftsDatabase.sql.endTransaction();
                }
        }

        @Override
        public void close() {
                if (sql != null) sql.close();
                sql = null;
                draftsDatabase = null;
        }
}
