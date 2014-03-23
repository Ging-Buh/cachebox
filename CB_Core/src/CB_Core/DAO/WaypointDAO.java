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
import CB_Utils.Util.SDBM_Hash;
import CB_Utils.Util.UnitFormatter;

public class WaypointDAO
{
	public void WriteToDatabase(Waypoint WP)
	{
		int newCheckSum = createCheckSum(WP);
		Replication.WaypointNew(WP.CacheId, WP.checkSum, newCheckSum, WP.GcCode);
		Parameters args = new Parameters();
		args.put("gccode", WP.GcCode);
		args.put("cacheid", WP.CacheId);
		args.put("latitude", WP.Pos.getLatitude());
		args.put("longitude", WP.Pos.getLongitude());
		args.put("description", WP.Description);
		args.put("type", WP.Type.ordinal());
		args.put("syncexclude", WP.IsSyncExcluded);
		args.put("userwaypoint", WP.IsUserWaypoint);
		if (WP.Clue == null) WP.Clue = "";
		args.put("clue", WP.Clue);
		args.put("title", WP.Title);
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
		Replication.WaypointChanged(WP.CacheId, WP.checkSum, newCheckSum, WP.GcCode);
		if (newCheckSum != WP.checkSum)
		{
			Parameters args = new Parameters();
			args.put("gccode", WP.GcCode);
			args.put("cacheid", WP.CacheId);
			args.put("latitude", WP.Pos.getLatitude());
			args.put("longitude", WP.Pos.getLongitude());
			args.put("description", WP.Description);
			args.put("type", WP.Type.ordinal());
			args.put("syncexclude", WP.IsSyncExcluded);
			args.put("userwaypoint", WP.IsUserWaypoint);
			args.put("clue", WP.Clue);
			args.put("title", WP.Title);
			args.put("isStart", WP.IsStart);
			try
			{
				long count = Database.Data.update("Waypoint", args, "CacheId=" + WP.CacheId + " and GcCode=\"" + WP.GcCode + "\"", null);
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
			WP.checkSum = newCheckSum;
		}
		return result;
	}

	public Waypoint getWaypoint(CoreCursor reader)
	{
		Waypoint WP = new Waypoint();
		WP.GcCode = reader.getString(0);
		WP.CacheId = reader.getLong(1);
		double latitude = reader.getDouble(2);
		double longitude = reader.getDouble(3);
		WP.Pos = new Coordinate(latitude, longitude);
		WP.Description = reader.getString(4);
		WP.Type = CacheTypes.values()[reader.getShort(5)];
		WP.IsSyncExcluded = reader.getInt(6) == 1;
		WP.IsUserWaypoint = reader.getInt(7) == 1;
		WP.Clue = reader.getString(8);
		if (WP.Clue != null) WP.Clue = WP.Clue.trim();
		WP.Title = reader.getString(9).trim();
		WP.IsStart = reader.getInt(10) == 1;
		WP.checkSum = createCheckSum(WP);
		return WP;
	}

	private int createCheckSum(Waypoint WP)
	{
		// for Replication
		String sCheckSum = WP.GcCode;
		sCheckSum += UnitFormatter.FormatLatitudeDM(WP.Pos.getLatitude());
		sCheckSum += UnitFormatter.FormatLongitudeDM(WP.Pos.getLongitude());
		sCheckSum += WP.Description;
		sCheckSum += WP.Type.ordinal();
		sCheckSum += WP.Clue;
		sCheckSum += WP.Title;
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
		args.put("gccode", WP.GcCode);
		args.put("cacheid", WP.CacheId);
		args.put("latitude", WP.Pos.getLatitude());
		args.put("longitude", WP.Pos.getLongitude());
		args.put("description", WP.Description);
		args.put("type", WP.Type.ordinal());
		args.put("syncexclude", WP.IsSyncExcluded);
		args.put("userwaypoint", WP.IsUserWaypoint);
		args.put("clue", WP.Clue);
		args.put("title", WP.Title);
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
				WaypointDAO waypointDAO = new WaypointDAO();
				waypointDAO.UpdateDatabase(wp);
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

}
