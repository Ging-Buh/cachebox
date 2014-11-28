package CB_Core.DAO;

import java.util.Iterator;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Import.ImporterProgress;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Utils.DB.CoreCursor;
import CB_Utils.DB.Database_Core.Parameters;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.SDBM_Hash;
import CB_Utils.Util.UnitFormatter;

public class WaypointDAO
{

	public static final String SQL_WP = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart from Waypoint";
	public static final String SQL_WP_FULL = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart, Description, Clue from Waypoint";

	public void WriteToDatabase(Waypoint WP)
	{
		int newCheckSum = createCheckSum(WP);
		Replication.WaypointNew(WP.CacheId, WP.getCheckSum(), newCheckSum, WP.getGcCode());
		Parameters args = new Parameters();
		args.put("gccode", WP.getGcCode());
		args.put("cacheid", WP.CacheId);
		args.put("latitude", WP.Pos.getLatitude());
		args.put("longitude", WP.Pos.getLongitude());
		args.put("description", WP.getDescription());
		args.put("type", WP.Type.ordinal());
		args.put("syncexclude", WP.IsSyncExcluded);
		args.put("userwaypoint", WP.IsUserWaypoint);
		if (WP.getClue() == null) WP.setClue("");
		args.put("clue", WP.getClue());
		args.put("title", WP.getTitle());
		args.put("isStart", WP.IsStart);

		try
		{
			Database.Data.insert("Waypoint", args);
			if (WP.IsUserWaypoint)
			{
				// HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist!!!
				args = new Parameters();
				args.put("hasUserData", true);
				Database.Data.update("Caches", args, "Id = ?", new String[]
					{ String.valueOf(WP.CacheId) });
			}
		}
		catch (Exception exc)
		{
			return;

		}
	}

	public boolean UpdateDatabase(Waypoint WP)
	{
		boolean result = false;
		int newCheckSum = createCheckSum(WP);
		Replication.WaypointChanged(WP.CacheId, WP.getCheckSum(), newCheckSum, WP.getGcCode());
		if (newCheckSum != WP.getCheckSum())
		{
			Parameters args = new Parameters();
			args.put("gccode", WP.getGcCode());
			args.put("cacheid", WP.CacheId);
			args.put("latitude", WP.Pos.getLatitude());
			args.put("longitude", WP.Pos.getLongitude());
			args.put("description", WP.getDescription());
			args.put("type", WP.Type.ordinal());
			args.put("syncexclude", WP.IsSyncExcluded);
			args.put("userwaypoint", WP.IsUserWaypoint);
			args.put("clue", WP.getClue());
			args.put("title", WP.getTitle());
			args.put("isStart", WP.IsStart);
			try
			{
				long count = Database.Data.update("Waypoint", args, "CacheId=" + WP.CacheId + " and GcCode=\"" + WP.getGcCode() + "\"",
						null);
				if (count > 0) result = true;
			}
			catch (Exception exc)
			{
				result = false;

			}

			if (WP.IsUserWaypoint)
			{
				// HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist (z.B. über API)
				args = new Parameters();
				args.put("hasUserData", true);
				try
				{
					Database.Data.update("Caches", args, "Id = ?", new String[]
						{ String.valueOf(WP.CacheId) });
				}
				catch (Exception exc)
				{
					return result;
				}
			}
			WP.setCheckSum(newCheckSum);
		}
		return result;
	}

	/**
	 * Create Waypoint Object from Reader.
	 * 
	 * @param reader
	 * @param full
	 *            Waypoints as FullWaypoints (true) or Waypoint (false)
	 * @return
	 */
	public Waypoint getWaypoint(CoreCursor reader, boolean full)
	{
		Waypoint WP = null;

		WP = new Waypoint(full);

		WP.setGcCode(reader.getString(0));
		WP.CacheId = reader.getLong(1);
		double latitude = reader.getDouble(2);
		double longitude = reader.getDouble(3);
		WP.Pos = new Coordinate(latitude, longitude);
		WP.Type = CacheTypes.values()[reader.getShort(4)];
		WP.IsSyncExcluded = reader.getInt(5) == 1;
		WP.IsUserWaypoint = reader.getInt(6) == 1;
		WP.setTitle(reader.getString(7).trim());
		WP.IsStart = reader.getInt(8) == 1;

		if (full)
		{
			WP.setClue(reader.getString(10));
			WP.setDescription(reader.getString(9));
			WP.setCheckSum(createCheckSum(WP));
		}
		return WP;
	}

	private int createCheckSum(Waypoint WP)
	{
		// for Replication
		String sCheckSum = WP.getGcCode();
		sCheckSum += UnitFormatter.FormatLatitudeDM(WP.Pos.getLatitude());
		sCheckSum += UnitFormatter.FormatLongitudeDM(WP.Pos.getLongitude());
		sCheckSum += WP.getDescription();
		sCheckSum += WP.Type.ordinal();
		sCheckSum += WP.getClue();
		sCheckSum += WP.getTitle();
		if (WP.IsStart) sCheckSum += "1";
		return (int) SDBM_Hash.sdbm(sCheckSum);
	}

	public void WriteImports(Iterator<Waypoint> waypointIterator, int waypointCount, ImporterProgress ip)
	{
		ip.setJobMax("WriteWaypointsToDB", waypointCount);
		while (waypointIterator.hasNext())
		{
			Waypoint waypoint = waypointIterator.next();
			ip.ProgressInkrement("WriteWaypointsToDB", String.valueOf(waypoint.CacheId), false);
			try
			{
				WriteImportToDatabase(waypoint);
			}
			catch (Exception e)
			{

				e.printStackTrace();
			}

		}

	}

	public void WriteImportToDatabase(Waypoint WP)
	{
		Parameters args = new Parameters();
		args.put("gccode", WP.getGcCode());
		args.put("cacheid", WP.CacheId);
		args.put("latitude", WP.Pos.getLatitude());
		args.put("longitude", WP.Pos.getLongitude());
		args.put("description", WP.getDescription());
		args.put("type", WP.Type.ordinal());
		args.put("syncexclude", WP.IsSyncExcluded);
		args.put("userwaypoint", WP.IsUserWaypoint);
		args.put("clue", WP.getClue());
		args.put("title", WP.getTitle());
		args.put("isStart", WP.IsStart);

		try
		{
			Database.Data.insertWithConflictReplace("Waypoint", args);

			args = new Parameters();
			args.put("hasUserData", true);
			Database.Data.update("Caches", args, "Id = ?", new String[]
				{ String.valueOf(WP.CacheId) });
		}
		catch (Exception exc)
		{
			return;

		}
	}

	// Hier wird überprüft, ob für diesen Cache ein Start-Waypoint existiert und dieser in diesem Fall zurückgesetzt
	// Damit kann bei der Definition eines neuen Start-Waypoints vorher der alte entfernt werden damit sichergestellt ist dass ein Cache nur
	// 1 Start-Waypoint hat
	public void ResetStartWaypoint(Cache cache, Waypoint except)
	{
		for (int i = 0, n = cache.waypoints.size(); i < n; i++)
		{
			Waypoint wp = cache.waypoints.get(i);
			if (except == wp) continue;
			if (wp.IsStart)
			{
				wp.IsStart = false;
				Parameters args = new Parameters();
				args.put("isStart", false);
				try
				{
					long count = Database.Data.update("Waypoint", args, "CacheId=" + wp.CacheId + " and GcCode=\"" + wp.getGcCode() + "\"",
							null);

				}
				catch (Exception exc)
				{

				}
			}
		}
	}

	/**
	 * Delete all Logs without exist Cache
	 */
	public void ClearOrphanedWaypoints()
	{
		String SQL = "DELETE  FROM  Waypoint WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Waypoint.CacheId = c.Id)";
		Database.Data.execSQL(SQL);
	}

	/**
	 * Returns a WaypointList from reading DB!
	 * 
	 * @param CacheID
	 *            ID of Cache
	 * @param Full
	 *            Waypoints as FullWaypoints (true) or Waypoint (false)
	 * @return
	 */
	public CB_List<Waypoint> getWaypointsFromCacheID(Long CacheID, boolean Full)
	{
		CB_List<Waypoint> wpList = new CB_List<Waypoint>();
		long aktCacheID = -1;

		StringBuilder sqlState = new StringBuilder(Full ? SQL_WP_FULL : SQL_WP);
		sqlState.append("  where CacheId = ?");

		CoreCursor reader = Database.Data.rawQuery(sqlState.toString(), new String[]
			{ String.valueOf(CacheID) });
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			Waypoint wp = getWaypoint(reader, Full);
			if (wp.CacheId != aktCacheID)
			{
				aktCacheID = wp.CacheId;
				wpList = new CB_List<Waypoint>();

			}
			wpList.add(wp);
			reader.moveToNext();

		}
		reader.close();

		return wpList;
	}

}
