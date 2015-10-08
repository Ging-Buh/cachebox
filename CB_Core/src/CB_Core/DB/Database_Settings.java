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

public abstract class Database_Settings extends Database_Core
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Database_Fieldnotes.class);
	public static Database_Settings Settings;

	public Database_Settings(SQLite database)
	{
		super(database);
		database.setLatestDatabaseChange(Database.LatestDatabaseChange);
		Settings = this;
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
				this.db.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
				this.db.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
			}
			if (lastDatabaseSchemeVersion < 1002)
			{
				// Long Text Field for long Strings
				this.db.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
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
