package CB_Core.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import CB_Core.Tag;
import CB_Core.DB.Database;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.LogEntry;
import CB_Utils.DB.Database_Core.Parameters;

import com.badlogic.gdx.Gdx;

public class LogDAO
{
	public void WriteToDatabase(LogEntry log)
	{
		Parameters args = new Parameters();
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
			Database.Data.insertWithConflictReplace("Logs", args);
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "Write Log", exc);
		}

	}

	// static HashMap<String, String> LogLookup = null;

	public void WriteImports(Iterator<LogEntry> logIterator)
	{
		WriteImports(logIterator, 0, null);
	}

	public void WriteImports(Iterator<LogEntry> logIterator, int logCount, ImporterProgress ip)
	{

		if (ip != null) ip.setJobMax("WriteLogsToDB", logCount);
		while (logIterator.hasNext())
		{
			LogEntry log = logIterator.next();
			if (ip != null) ip.ProgressInkrement("WriteLogsToDB", String.valueOf(log.CacheId), false);
			try
			{
				WriteToDatabase(log);
			}
			catch (Exception e)
			{

				// Statt hier den Fehler abzufangen, sollte die LogTabelle
				// Indexiert werden
				// und nur die noch nicht vorhandenen Logs geschrieben werden.

				Gdx.app.error(Tag.TAG, "", e);
			}

		}

	}

	/**
	 * Delete all Logs without exist Cache
	 */
	public void ClearOrphanedLogs()
	{
		String SQL = "DELETE  FROM  Logs WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Logs.CacheId = c.Id)";
		Database.Data.execSQL(SQL);
	}

}
