package de.droidcachebox.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Types.LogEntry;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import de.droidcachebox.Database;

public class LogDAO {
	public void WriteToDatabase(LogEntry log)
	{
		ContentValues args = new ContentValues();
        args.put("Id", log.Id);
        args.put("Finder", log.Finder);
        args.put("Type", log.Type.ordinal());
        args.put("Comment", log.Comment);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stimestamp = iso8601Format.format(log.Timestamp);
        args.put("Timestamp", stimestamp);
        args.put("CacheId", log.CacheId);
        try
        {
        	Database.Data.myDB.insertWithOnConflict("Logs", null, args, SQLiteDatabase.CONFLICT_REPLACE);
        }
        catch (Exception exc)
        {
        	Logger.Error("Write Log", "", exc);
        }
		
	}
	
	
	static HashMap<String, String> LogLookup = null;
	
	public void WriteImports(Iterator<LogEntry> logIterator, int logCount,
			ImporterProgress ip) {
		
		
		
		ip.setJobMax("WriteLogsToDB", logCount);
		while (logIterator.hasNext())
		{
			LogEntry log = logIterator.next();
			ip.ProgressInkrement("WriteLogsToDB", String.valueOf(log.CacheId));
			try {
				WriteToDatabase(log);
			} catch (Exception e) {
				
//				Statt hier den Fehler abzufangen, sollte die LogTabelle Indexiert werden 
//				und nur die noch nicht vorhandenen Logs geschrieben werden.

				e.printStackTrace();
			}
			
		}
		
	}

}
