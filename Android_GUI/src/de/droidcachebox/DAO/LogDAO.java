package de.droidcachebox.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import de.droidcachebox.Database;

import android.content.ContentValues;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;

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
        	long anzahl = Database.Data.myDB.insert("Logs", null, args);
            String s = anzahl + "";
        	
            args = new ContentValues();
        } catch (Exception exc)
        {
        	Logger.Error("Write Log", "", exc);
        
        }	
		
	}

	public void WriteImports(Iterator<LogEntry> logIterator, int logCount,
			ImporterProgress ip) {
		
		ip.setJobMax("WriteLogsToDB", logCount);
		while (logIterator.hasNext())
		{
			LogEntry log = logIterator.next();
			ip.ProgressInkrement("WriteLogsToDB", String.valueOf(log.CacheId));
			WriteToDatabase(log);
			
		}
		
	}

}
