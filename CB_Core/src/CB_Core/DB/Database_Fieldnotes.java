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

import org.slf4j.LoggerFactory;

import de.cb.sqlite.Database_Core;
import de.cb.sqlite.SQLite;

public abstract class Database_Fieldnotes extends Database_Core
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Database_Fieldnotes.class);
	public static Database_Fieldnotes FieldNotes;

	public Database_Fieldnotes(SQLite database)
	{
		super(database);
		database.setLatestDatabaseChange(Database.LatestDatabaseChange);
		FieldNotes = this;
	}

	@Override
	public boolean StartUp()
	{
		boolean result = this.db.StartUp();
		if (!result) return false;
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
				// FieldNotes Table
				this.db.execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [Name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");

				// Config Table
				this.db.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
				this.db.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
			}
			if (lastDatabaseSchemeVersion < 1002)
			{
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [Uploaded] BOOLEAN DEFAULT 'false' NULL");
			}
			if (lastDatabaseSchemeVersion < 1003)
			{
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [GC_Vote] integer default 0");
			}
			if (lastDatabaseSchemeVersion < 1004)
			{
				this.db.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (15) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [name] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
				this.db.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
				this.db.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (15) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
				this.db.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
				this.db.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
			}
			if (lastDatabaseSchemeVersion < 1005)
			{
				this.db.execSQL("ALTER TABLE [Trackable] ADD COLUMN [TypeName] ntext NULL");
				this.db.execSQL("ALTER TABLE [Trackable] ADD COLUMN [LastVisit] datetime NULL");
				this.db.execSQL("ALTER TABLE [Trackable] ADD COLUMN [Home] ntext NULL");
				this.db.execSQL("ALTER TABLE [Trackable] ADD COLUMN [TravelDistance] integer default 0");
			}
			if (lastDatabaseSchemeVersion < 1006)
			{
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbFieldNote] BOOLEAN DEFAULT 'false' NULL");
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbName] nvarchar (255)  NULL");
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbIconUrl] nvarchar (255)  NULL");
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TravelBugCode] nvarchar (15)  NULL");
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TrackingNumber] nvarchar (15)  NULL");
			}
			if (lastDatabaseSchemeVersion < 1007)
			{
				this.db.execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [directLog] BOOLEAN DEFAULT 'false' NULL");
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
}
