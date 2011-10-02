package de.droidcachebox.DB;

import java.io.File;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import CB_Core.FileIO;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;

public class AndroidDB extends Database
{
	private Activity activity;
    public SQLiteDatabase myDB = null;
	
	public AndroidDB(DatabaseType databaseType, Activity activity) {
		super(databaseType);
		this.activity = activity;
	}
	
	@Override
	public void Initialize()
	{
		if (myDB == null)
		{
			if (!FileIO.FileExists(databasePath))
				Reset();

			try
			{
				myDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
			} catch (Exception exc)
			{
				return;
			}
		} 
	}

	@Override
	public void Reset()
	{
		// if exists, delete old database file
    	File file = new File(databasePath);
    	if (file.exists())
    		file.delete();

    	try
    	{
    		activity.openOrCreateDatabase(databasePath, Context.MODE_WORLD_WRITEABLE, null);
    	} catch (Exception exc)
    	{
    		String s = exc.getMessage();
    	}
	}

	@Override
	public CoreCursor rawQuery(String sql, String[] args)
	{
		Cursor c = myDB.rawQuery(sql, args);
		return new AndroidCursor(c);
	}

	@Override
	public void execSQL(String sql) {
		myDB.execSQL(sql);
	}


	private ContentValues getContentValues(Parameters val) {
		ContentValues values = new ContentValues();
		for (Entry<String, Object> entry : val.entrySet()) {
			Object o = entry.getValue();
			if (o instanceof Boolean)
				values.put(entry.getKey(), (Boolean)entry.getValue());
			else if (o instanceof Byte)
				values.put(entry.getKey(), (Byte)entry.getValue());
			else if (o instanceof byte[])
				values.put(entry.getKey(), (byte[])entry.getValue());
			else if (o instanceof Double)
				values.put(entry.getKey(), (Double)entry.getValue());
			else if (o instanceof Float)
				values.put(entry.getKey(), (Float)entry.getValue());
			else if (o instanceof Integer)
				values.put(entry.getKey(), (Integer)entry.getValue());
			else if (o instanceof Long)
				values.put(entry.getKey(), (Long)entry.getValue());
			else if (o instanceof Short)
				values.put(entry.getKey(), (Short)entry.getValue());
			else if (o instanceof String)
				values.put(entry.getKey(), (String)entry.getValue());
			else
				values.put(entry.getKey(), entry.getValue().toString());
		}
		return values;
	}
	
	@Override
	public long insert(String tablename, Parameters val)
	{
		ContentValues values = getContentValues(val);
		return myDB.insert(tablename, null, values);
	}
	
	@Override
	public long update(String tablename, Parameters val, String whereClause,
			String[] whereArgs)
	{
		ContentValues values = getContentValues(val);
		return myDB.update(tablename, values, whereClause, whereArgs);
	}

	@Override
	public long delete(String tablename, String whereClause, String[] whereArgs) {
		return myDB.delete(tablename, whereClause, whereArgs);
	}
	
	@Override
	public void beginTransaction()
	{
		myDB.beginTransaction();
	}
	
	@Override
	public void setTransactionSuccessful()
	{
		myDB.setTransactionSuccessful();
	}
	
	@Override
	public void endTransaction()
	{
		myDB.endTransaction();
	}
	
	@Override
	public long insertWithConflictReplace(String tablename, Parameters val)
	{
		ContentValues values = getContentValues(val);
		return myDB.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}
}
