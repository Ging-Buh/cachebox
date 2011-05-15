package de.droidcachebox;

import java.io.File;

import de.droidcachebox.Geocaching.CacheList;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class Database {
	public Activity activity;
	public static Database Data;
	public static Database FieldNotes; 
	public enum DatabaseType { CacheBox, FieldNotes }
    protected DatabaseType databaseType;
    public String databasePath = "";
    public long DatabaseId = 0;  // for Database replication with WinCachebox
    public long MasterDatabaseId = 0;
    protected int latestDatabaseChange = 0;
    public SQLiteDatabase myDB = null;
    public CacheList Query;
        
	public Database(DatabaseType databaseType, Activity activity) {
		this.databaseType = databaseType;
		this.activity = activity;
		switch (databaseType) {
		case CacheBox:
			latestDatabaseChange = Global.LatestDatabaseChange;
			Query = new CacheList();
			break;
		case FieldNotes:
			latestDatabaseChange = Global.LatestDatabaseFieldNoteChange;
			break;
		}
	}

	public void Initialize()
	{
		if (myDB == null)
		{
			if (!Global.FileExists(databasePath))
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
	
	public void Reset()
	{
		// if exists, delete old database file
    	File file = new File(databasePath);
    	if (file.exists())
    		file.delete();

    	try
    	{
    		activity.openOrCreateDatabase(databasePath, activity.MODE_WORLD_WRITEABLE, null);
    	} catch (Exception exc)
    	{
    		String s = exc.getMessage();
    	}
	}
	
	public boolean StartUp(String databasePath) {
		this.databasePath = databasePath;
		
        Initialize();
		
		int databaseSchemeVersion = GetDatabaseSchemeVersion();
		if (databaseSchemeVersion < latestDatabaseChange) {
            AlterDatabase(databaseSchemeVersion);
            SetDatabaseSchemeVersion();
		}
		SetDatabaseSchemeVersion();
		
		return true;
	}

	private void AlterDatabase(int lastDatabaseSchemeVersion) {
		switch (databaseType)
		{
		case CacheBox:
			if (lastDatabaseSchemeVersion <= 0)
			{
				// First Initialization of the Database
			}
			break;
		case FieldNotes:
			if (lastDatabaseSchemeVersion <= 0)
			{
				// First Initialization of the Database
				try
				{
					// FieldNotes Table
					myDB.execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [Name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");
					
					// Config Table
					myDB.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
					myDB.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
				} catch (Exception exc)
				{
					String ex = exc.getMessage();
				}
			}
			break;
		}
	}

	private int GetDatabaseSchemeVersion() {
        int result = -1;
        Cursor c = null;
        try
        {
        	c = myDB.rawQuery("select Value from Config where [Key]=?", new String[] { "DatabaseSchemeVersion" });
        } catch (Exception exc)
        { 
        	return -1;
        }
        try
        {
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
                String databaseSchemeVersion = c.getString(0);
                result = Integer.parseInt(databaseSchemeVersion);
                c.moveToNext();
            };
        }
        catch (Exception exc)
        {
            result = -1;
        }
        c.close();

        return result;
	}

	private void SetDatabaseSchemeVersion() {
		// TODO Auto-generated method stub
//		myDB.execSQL("Update Config set Value=" + latestDatabaseChange + " where [Key]='DatabaseSchemeVersionx'");
		ContentValues val = new ContentValues();
		val.put("Value", latestDatabaseChange);
		int anz = myDB.update("Config", val, "[Key]='DatabaseSchemeVersion'", null);
		if (anz <= 0) {
			// Update not possible because Key does not exist
			val.put("Key", "DatabaseSchemeVersion");
			myDB.insert("Config", null, val);
		}
												           	
	}
}
