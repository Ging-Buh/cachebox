package CB_Utils.DB;

import java.util.HashMap;

import CB_Utils.Tag;

import com.badlogic.gdx.Gdx;

public abstract class Database_Core
{
	protected String databasePath;

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

	public String getDatabasePath()
	{
		return databasePath;
	}

	public long DatabaseId = 0; // for Database replication with WinCachebox
	public long MasterDatabaseId = 0;
	protected int latestDatabaseChange = 0;

	public Database_Core()
	{

	}

	public abstract void Initialize();;

	public abstract void Reset();;

	public boolean StartUp(String databasePath)
	{
		try
		{
			Gdx.app.debug(Tag.TAG, "DB Startup : " + databasePath);
		}
		catch (Exception e)
		{
			// gibt beim splash - Start: NPE in Translation.readMissingStringsFile
			// Nachfolgende Starts sollten aber protokolliert werden
		}

		this.databasePath = databasePath;

		Initialize();

		int databaseSchemeVersion = GetDatabaseSchemeVersion();
		if (databaseSchemeVersion < latestDatabaseChange)
		{
			AlterDatabase(databaseSchemeVersion);
			SetDatabaseSchemeVersion();
		}
		SetDatabaseSchemeVersion();
		return true;
	}

	protected void AlterDatabase(int lastDatabaseSchemeVersion)
	{
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
		val.put("Value", latestDatabaseChange);
		long anz = update("Config", val, "[Key] like 'DatabaseSchemeVersionWin'", null);
		if (anz <= 0)
		{
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersionWin");
			insert("Config", val);
		}
		// for Compatibility with WinCB
		val.put("Value", latestDatabaseChange);
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

	// Zur Parameter übergabe and die DB
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
