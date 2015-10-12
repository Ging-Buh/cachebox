/* 
 * Copyright (C) 2014 team-cachebox.de
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
package CB_Core.DB;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.LoggerFactory;

import CB_Core.DAO.CategoryDAO;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Categories;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.SDBM_Hash;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core;
import de.cb.sqlite.Parameters;
import de.cb.sqlite.SQLite;

public class Database_Data extends Database_Core
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Database_Data.class);
	public static Database_Data Data;
	public CacheList Query;

	public int MasterDatabaseId;

	public Database_Data(SQLite database)
	{
		super(database);
		database.setLatestDatabaseChange(Database.LatestDatabaseChange);
		Query = new CacheList();
		Data = this;

	}

	@Override
	public boolean StartUp()
	{
		boolean result = this.db.StartUp();
		if (!result) return false;
		// create or load DatabaseId for each
		long DatabaseId = this.db.ReadConfigLong("DatabaseId");
		if (DatabaseId <= 0)
		{
			DatabaseId = new Date().getTime();
			this.db.WriteConfigLong("DatabaseId", DatabaseId);
		}
		// Read MasterDatabaseId. If MasterDatabaseId > 0 -> This database
		// is connected to the Replications Master of WinCB
		// In this case changes of Waypoints, Solvertext, Notes must be
		// noted in the Table Replication...
		MasterDatabaseId = (int) this.db.ReadConfigLong("MasterDatabaseId");

		return result;
	}

	public void AlterDatabase(int lastDatabaseSchemeVersion)
	{

	}

	// Methoden f�r Waypoint
	public void DeleteFromDatabase(Waypoint WP)
	{
		Replication.WaypointDelete(WP.CacheId, 0, 1, WP.getGcCode());
		try
		{
			this.db.delete("Waypoint", "GcCode='" + WP.getGcCode() + "'", null);
		}
		catch (Exception exc)
		{
			log.error("Waypoint.DeleteFromDataBase()", "", exc);
		}
	}

	public boolean WaypointExists(String gcCode)
	{
		CoreCursor c = this.db.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[]
			{ gcCode });
		{
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{

				try
				{
					c.close();
					return true;
				}
				catch (Exception e)
				{
					return false;
				}
			}
			c.close();

			return false;
		}
	}

	public String CreateFreeGcCode(String cacheGcCode) throws Exception
	{
		String suffix = cacheGcCode.substring(2);
		String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
		String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		for (int i = 0; i < firstCharCandidates.length(); i++)
			for (int j = 0; j < secondCharCandidates.length(); j++)
			{
				String gcCode = firstCharCandidates.substring(i, i + 1) + secondCharCandidates.substring(j, j + 1) + suffix;
				if (!WaypointExists(gcCode)) return gcCode;
			}
		throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
	}

	// Methodes f�r Cache
	public String GetNote(Cache cache)
	{
		String resultString = GetNote(cache.Id);
		cache.setNoteChecksum((int) SDBM_Hash.sdbm(resultString));
		return resultString;
	}

	public String GetNote(long cacheId)
	{
		String resultString = "";
		CoreCursor c = this.db.rawQuery("select Notes from Caches where Id=?", new String[]
			{ String.valueOf(cacheId) });
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			resultString = c.getString(0);
			break;
		}
		return resultString;
	}

	/**
	 * ge�nderte Note nur in die DB schreiben
	 * 
	 * @param cacheId
	 * @param value
	 */
	public void SetNote(long cacheId, String value)
	{
		Parameters args = new Parameters();
		args.put("Notes", value);
		args.put("HasUserData", true);
		this.db.update("Caches", args, "id=" + cacheId, null);
	}

	public void SetNote(Cache cache, String value)
	{
		int newNoteCheckSum = (int) SDBM_Hash.sdbm(value);

		Replication.NoteChanged(cache.Id, cache.getNoteChecksum(), newNoteCheckSum);
		if (newNoteCheckSum != cache.getNoteChecksum())
		{
			SetNote(cache.Id, value);
			cache.setNoteChecksum(newNoteCheckSum);
		}
	}

	public void SetFound(long cacheId, boolean value)
	{
		Parameters args = new Parameters();
		args.put("found", value);
		this.db.update("Caches", args, "id=" + cacheId, null);
	}

	public String GetSolver(Cache cache)
	{
		String resultString = GetSolver(cache.Id);
		cache.setSolverChecksum((int) SDBM_Hash.sdbm(resultString));
		return resultString;
	}

	public String GetSolver(long cacheId)
	{
		try
		{
			String resultString = "";
			CoreCursor c = this.db.rawQuery("select Solver from Caches where Id=?", new String[]
				{ String.valueOf(cacheId) });
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				resultString = c.getString(0);
				break;
			}
			return resultString;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/**
	 * ge�nderten Solver nur in die DB schreiben
	 * 
	 * @param cacheId
	 * @param value
	 */
	public void SetSolver(long cacheId, String value)
	{
		Parameters args = new Parameters();
		args.put("Solver", value);
		args.put("HasUserData", true);

		this.db.update("Caches", args, "id=" + cacheId, null);
	}

	public void SetSolver(Cache cache, String value)
	{
		int newSolverCheckSum = (int) SDBM_Hash.sdbm(value);

		Replication.SolverChanged(cache.Id, cache.getSolverChecksum(), newSolverCheckSum);
		if (newSolverCheckSum != cache.getSolverChecksum())
		{
			SetSolver(cache.Id, value);
			cache.setSolverChecksum(newSolverCheckSum);
		}
	}

	public CB_List<LogEntry> Logs(Cache cache)
	{
		CB_List<LogEntry> result = new CB_List<LogEntry>();
		if (cache == null) // if no cache is selected!
			return result;
		CoreCursor reader = this.db.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc", new String[]
			{ Long.toString(cache.Id) });

		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			LogEntry logent = getLogEntry(cache, reader, true);
			if (logent != null) result.add(logent);
			reader.moveToNext();
		}
		reader.close();

		return result;
	}

	private static LogEntry getLogEntry(Cache cache, CoreCursor reader, boolean filterBbCode)
	{
		int intLogType = reader.getInt(3);
		if (intLogType < 0 || intLogType > 13) return null;

		LogEntry retLogEntry = new LogEntry();

		retLogEntry.CacheId = reader.getLong(0);

		String sDate = reader.getString(1);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			retLogEntry.Timestamp = iso8601Format.parse(sDate);
		}
		catch (ParseException e)
		{
		}
		retLogEntry.Finder = reader.getString(2);
		retLogEntry.Type = CB_Core.Enums.LogTypes.values()[reader.getInt(3)];
		// retLogEntry.TypeIcon = reader.getInt(3);
		retLogEntry.Comment = reader.getString(4);
		retLogEntry.Id = reader.getLong(5);

		if (filterBbCode)
		{
			int lIndex;

			while ((lIndex = retLogEntry.Comment.indexOf('[')) >= 0)
			{
				int rIndex = retLogEntry.Comment.indexOf(']', lIndex);

				if (rIndex == -1) break;

				retLogEntry.Comment = retLogEntry.Comment.substring(0, lIndex) + retLogEntry.Comment.substring(rIndex + 1);
			}
		}

		return retLogEntry;
	}

	public String GetDescription(Cache cache)
	{
		String description = "";
		CoreCursor reader = this.db.rawQuery("select Description from Caches where Id=?", new String[]
			{ Long.toString(cache.Id) });
		if (reader == null) return "";
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			if (reader.getString(0) != null) description = reader.getString(0);
			reader.moveToNext();
		}
		reader.close();

		return description;
	}

	public String GetShortDescription(Cache cache)
	{
		String description = "";
		CoreCursor reader = this.db.rawQuery("select ShortDescription from Caches where Id=?", new String[]
			{ Long.toString(cache.Id) });
		if (reader == null) return "";
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			if (reader.getString(0) != null) description = reader.getString(0);
			reader.moveToNext();
		}
		reader.close();

		return description;
	}

	/**
	 * @return Set To GlobalCore.Categories
	 */
	public Categories GPXFilenameUpdateCacheCount()
	{
		// welche GPXFilenamen sind in der DB erfasst
		this.db.beginTransaction();
		try
		{
			CoreCursor reader = this.db.rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);
			reader.moveToFirst();

			while (reader.isAfterLast() == false)
			{
				long GPXFilename_ID = reader.getLong(0);
				long CacheCount = reader.getLong(1);

				Parameters val = new Parameters();
				val.put("CacheCount", CacheCount);
				this.db.update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);

				reader.moveToNext();
			}

			this.db.delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
			this.db.delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
			reader.close();
			this.db.setTransactionSuccessful();
		}
		catch (Exception e)
		{

		}
		finally
		{
			this.db.endTransaction();
		}

		CategoryDAO categoryDAO = new CategoryDAO();
		Categories categories = new Categories();
		categoryDAO.LoadCategoriesFromDatabase();
		return categories;
	}

	public static int getCacheCountInDB(SQLite db)
	{

		CoreCursor reader = null;
		int count = 0;
		boolean closeAfterChk = false;
		if (!db.isStarted())
		{
			db.StartUp();
			closeAfterChk = true;
		}

		try
		{
			reader = db.rawQuery("select count(*) from caches", null);
			reader.moveToFirst();
			count = reader.getInt(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (reader != null) reader.close();

		if (closeAfterChk) db.Close();

		return count;
	}

	/**
	 * @param minToKeep
	 *            Config.settings.LogMinCount.getValue()
	 * @param LogMaxMonthAge
	 *            Config.settings.LogMaxMonthAge.getValue()
	 */
	public void DeleteOldLogs(int minToKeep, int LogMaxMonthAge)
	{

		if (LogMaxMonthAge == 0)
		{
			// Setting are 'immediately'
			// Delete all Logs and return
			// TODO implement this
		}

		ArrayList<Long> oldLogCaches = new ArrayList<Long>();
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MONTH, -LogMaxMonthAge);
		String TimeStamp = (now.get(Calendar.YEAR)) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DATE);

		// ###################################################
		// Get CacheId's from Caches with to match older Logs
		// ###################################################
		{
			String command = "select cacheid from logs WHERE Timestamp < '" + TimeStamp + "' GROUP BY CacheId HAVING COUNT(Id) > " + String.valueOf(minToKeep);

			CoreCursor reader = this.db.rawQuery(command, null);
			reader.moveToFirst();
			while (reader.isAfterLast() == false)
			{
				long tmp = reader.getLong(0);
				if (!oldLogCaches.contains(tmp)) oldLogCaches.add(reader.getLong(0));
				reader.moveToNext();
			}
			reader.close();
		}

		// ###################################################
		// Get Logs
		// ###################################################
		{
			this.db.beginTransaction();
			try
			{
				for (long oldLogCache : oldLogCaches)
				{
					ArrayList<Long> minLogIds = new ArrayList<Long>();

					String command = "select id from logs where cacheid = " + String.valueOf(oldLogCache) + " order by Timestamp desc";

					int count = 0;
					CoreCursor reader = this.db.rawQuery(command, null);
					reader.moveToFirst();
					while (reader.isAfterLast() == false)
					{
						if (count == minToKeep) break;
						minLogIds.add(reader.getLong(0));
						reader.moveToNext();
						count++;
					}

					StringBuilder sb = new StringBuilder();
					for (long id : minLogIds)
						sb.append(id).append(",");

					// now delete all Logs out of Date without minLogIds
					String delCommand = "delete from Logs where Timestamp<'" + TimeStamp + "' and cacheid = " + String.valueOf(oldLogCache) + " and id not in (" + sb.toString().substring(0, sb.length() - 1) + ")";
					this.db.execSQL(delCommand);
				}
				this.db.setTransactionSuccessful();
			}
			catch (Exception ex)
			{
				log.error("Delete Old Logs", "", ex);
			}
			finally
			{
				this.db.endTransaction();
			}
		}
	}

	@Override
	public void Initialize()
	{
		db.Initialize();
	}

	@Override
	public void Reset()
	{

	}

	@Override
	public void Close()
	{

	}

}
