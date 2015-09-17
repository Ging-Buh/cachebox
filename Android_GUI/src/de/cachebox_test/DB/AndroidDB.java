package de.cachebox_test.DB;

import java.io.File;
import java.util.Map.Entry;

import CB_Core.DB.Database;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Log.LogLevel;
import CB_Utils.Util.FileIO;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AndroidDB extends Database
{
	private Activity activity;
	public SQLiteDatabase myDB = null;

	public AndroidDB(DatabaseType databaseType, Activity activity)
	{
		super(databaseType);
		this.activity = activity;

	}

	@Override
	public void Initialize()
	{

		if (myDB == null)
		{
			if (!FileIO.FileExists(databasePath))
			{
				Reset();
			}
			else
			{
				try
				{
					if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("open data base: " + databasePath);
					myDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
				}
				catch (Exception exc)
				{
					return;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Reset()
	{

		// if exists, delete old database file
		File file = new File(databasePath);
		if (file.exists())
		{
			if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("RESET DB, delete file: " + databasePath);
			file.delete();
		}

		try
		{
			if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("create data base: " + databasePath);
			myDB = activity.openOrCreateDatabase(getDatabasePath(databasePath).getAbsolutePath(), Context.MODE_WORLD_WRITEABLE, null);
			newDB = true;
		}
		catch (Exception exc)
		{
			log.error("createDB", exc);
		}
	}

	public File getDatabasePath(String dbfile)
	{

		File result = new File(dbfile);

		if (!result.getParentFile().exists())
		{
			result.getParentFile().mkdirs();
		}

		return result;
	}

	@Override
	public CoreCursor rawQuery(String sql, String[] args)
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG))
		{
			StringBuilder sb = new StringBuilder("RAW_QUERY :" + sql + " ARGs= ");
			if (args != null)
			{
				for (String arg : args)
					sb.append(arg + ", ");
			}
			else
				sb.append("NULL");
			log.debug(sb.toString());
		}
		if (myDB == null) return null;
		Cursor c = myDB.rawQuery(sql, args);
		return new AndroidCursor(c);
	}

	@Override
	public void execSQL(String sql)
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("execSQL : " + sql);
		try
		{
			myDB.execSQL(sql);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private ContentValues getContentValues(Parameters val)
	{
		ContentValues values = new ContentValues();
		for (Entry<String, Object> entry : val.entrySet())
		{
			Object o = entry.getValue();
			if (o instanceof Boolean) values.put(entry.getKey(), (Boolean) entry.getValue());
			else if (o instanceof Byte) values.put(entry.getKey(), (Byte) entry.getValue());
			else if (o instanceof byte[]) values.put(entry.getKey(), (byte[]) entry.getValue());
			else if (o instanceof Double) values.put(entry.getKey(), (Double) entry.getValue());
			else if (o instanceof Float) values.put(entry.getKey(), (Float) entry.getValue());
			else if (o instanceof Integer) values.put(entry.getKey(), (Integer) entry.getValue());
			else if (o instanceof Long) values.put(entry.getKey(), (Long) entry.getValue());
			else if (o instanceof Short) values.put(entry.getKey(), (Short) entry.getValue());
			else if (o instanceof String) values.put(entry.getKey(), (String) entry.getValue());
			else
				values.put(entry.getKey(), entry.getValue().toString());
		}
		return values;
	}

	@Override
	public long insert(String tablename, Parameters val)
	{

		ContentValues values = getContentValues(val);

		long ret = -1;
		try
		{
			if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("INSERT into: " + tablename + "values: " + values.toString());
			myDB.insert(tablename, null, values);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public long update(String tablename, Parameters val, String whereClause, String[] whereArgs)
	{

		if (LogLevel.isLogLevel(LogLevel.DEBUG))
		{
			StringBuilder sb = new StringBuilder("Update Table:" + tablename);
			sb.append("Parameters:" + val.toString());
			sb.append("WHERECLAUSE:" + whereClause);

			if (whereArgs != null)
			{
				for (String arg : whereArgs)
				{
					sb.append(arg + ", ");
				}
			}

			log.debug(sb.toString());
		}

		try
		{
			ContentValues values = getContentValues(val);
			return myDB.update(tablename, values, whereClause, whereArgs);
		}
		catch (Exception ex)
		{
			return 0;
		}
	}

	@Override
	public long delete(String tablename, String whereClause, String[] whereArgs)
	{

		if (LogLevel.isLogLevel(LogLevel.DEBUG))
		{
			StringBuilder sb = new StringBuilder("Delete@ Table:" + tablename);
			sb.append("WHERECLAUSE:" + whereClause);

			if (whereArgs != null)
			{
				for (String arg : whereArgs)
				{
					sb.append(arg + ", ");
				}
			}

			log.debug(sb.toString());
		}

		return myDB.delete(tablename, whereClause, whereArgs);
	}

	@Override
	public void beginTransaction()
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("begin transaction");
		if (myDB != null) myDB.beginTransaction();
	}

	@Override
	public void setTransactionSuccessful()
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("set Transaction Successful");
		if (myDB != null) myDB.setTransactionSuccessful();
	}

	@Override
	public void endTransaction()
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG)) log.debug("endTransaction");
		if (myDB != null) myDB.endTransaction();
	}

	@Override
	public long insertWithConflictReplace(String tablename, Parameters val)
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG))
		{
			log.debug("insertWithConflictReplace @Table:" + tablename + "Parameters: " + val.toString());
		}
		ContentValues values = getContentValues(val);
		return myDB.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	@Override
	public long insertWithConflictIgnore(String tablename, Parameters val)
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG))
		{
			log.debug("insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());
		}
		ContentValues values = getContentValues(val);
		return myDB.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}

	@Override
	public void Close()
	{
		if (LogLevel.isLogLevel(LogLevel.DEBUG))
		{
			log.debug("close DB:" + databasePath);
		}
		if (myDB != null) myDB.close();
		myDB = null;
	}

	@Override
	public int getCacheCountInDB(String filename)
	{
		try
		{
			SQLiteDatabase myDB = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
			Cursor c = myDB.rawQuery("select count(*) from caches", null);
			c.moveToFirst();
			int count = c.getInt(0);
			c.close();
			myDB.close();
			return count;
		}
		catch (Exception ex)
		{
		}
		return 0;
	}
}
