package de.droidcachebox.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.droidcachebox.Database;

import android.content.ContentValues;
import CB_Core.Log.Logger;
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

}
