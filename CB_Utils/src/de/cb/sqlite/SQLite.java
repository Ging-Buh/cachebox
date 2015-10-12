/* 
 * Copyright (C) 2015 team-cachebox.de
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
package de.cb.sqlite;

import org.slf4j.LoggerFactory;

/**
 * TODO document
 * 
 * @author Hoepfner 2015
 */
public abstract class SQLite
{

	final static org.slf4j.Logger log = LoggerFactory.getLogger(SQLite.class);

	final protected String databasePath;
	protected boolean newDB = false;
	protected boolean isStartet = false;

	private final AlternateDatabase alterNate;

	public SQLite(String databasePath, AlternateDatabase alter)
	{
		super();
		this.databasePath = databasePath;
		this.alterNate = alter;
	}

	public abstract void Initialize();

	public abstract void Close();

	public abstract void Reset();

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

	public boolean isDbNew()
	{
		return newDB;
	}

	public String getDatabasePath()
	{
		return databasePath;
	}

	public boolean StartUp()
	{
		try
		{
			log.debug("DB Startup : " + databasePath);
		}
		catch (Exception e)
		{
			// gibt beim splash - Start: NPE in Translation.readMissingStringsFile
			// Nachfolgende Starts sollten aber protokolliert werden
		}

		Initialize();

		int databaseSchemeVersion = GetDatabaseSchemeVersion();
		if (databaseSchemeVersion < alterNate.databaseSchemeVersion())
		{
			alterNate.alternateDatabase(this, databaseSchemeVersion);
			SetDatabaseSchemeVersion();
		}
		isStartet = true;
		return true;
	}

	private int GetDatabaseSchemeVersion()
	{
		int result = -1;
		CoreCursor c = null;
		try
		{
			c = rawQuery("select Value from Config where [Key] like ?", new String[]
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
		val.put("Value", alterNate.databaseSchemeVersion());
		long anz = update("Config", val, "[Key] like 'DatabaseSchemeVersionWin'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersionWin");
			insert("Config", val);
		}
		// for Compatibility with WinCB
		val.put("Value", alterNate.databaseSchemeVersion());
		anz = update("Config", val, "[Key] like 'DatabaseSchemeVersion'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersion");
			insert("Config", val);
		}
	}

	public void WriteConfigString(String key, String value)
	{
		Parameters val = new Parameters();
		val.put("Value", value);
		long anz = update("Config", val, "[Key] like '" + key + "'", null);
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
		long anz = update("Config", val, "[Key] like '" + key + "'", null);
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
			c = rawQuery("select Value from Config where [Key] like ?", new String[]
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
			c = rawQuery("select LongString from Config where [Key] like ?", new String[]
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

	public boolean isStarted()
	{
		return isStartet;
	}

	public String toString()
	{
		return "SQLite DB [" + (isStartet ? "is started" : "not started") + "]:" + databasePath;
	}
}