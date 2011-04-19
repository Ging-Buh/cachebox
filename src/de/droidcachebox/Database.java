package de.droidcachebox;

import de.droidcachebox.Geocaching.CacheList;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class Database {
	public static Database Data;
	public static Database FieldNotes = new Database(DatabaseType.FieldNotes); 
	public enum DatabaseType { CacheBox, FieldNotes }
    protected DatabaseType databaseType;
    public String databasePath = "";
    public long DatabaseId = 0;  // for Database replication with WinCachebox
    public long MasterDatabaseId = 0;
    protected int latestDatabaseChange = 0;
    public SQLiteDatabase myDB = null;
    public CacheList Query;
        
	public Database(DatabaseType databaseType) {
		this.databaseType = databaseType;
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
	
	public boolean StartUp(String databasePath) {
		this.databasePath = databasePath;
		try
		{
			myDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (Exception exc)
		{
			return false;
		}
		
		int databaseSchemeVersion = GetDatabaseSchemeVersion()-1;
		if (databaseSchemeVersion < latestDatabaseChange) {
            AlterDatabase(databaseSchemeVersion);
            SetDatabaseSchemeVersion();
		}
		SetDatabaseSchemeVersion();
		
		return true;
	}

	private void AlterDatabase(int lastDatabaseSchemeVersion) {
			
	}

	private int GetDatabaseSchemeVersion() {
        int result = -1;
        Cursor c = myDB.rawQuery("select Value from Config where [Key]=?", new String[] { "DatabaseSchemeVersion" });
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
