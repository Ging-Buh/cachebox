package CB_Core.DAO;

import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database.Parameters;
import CB_Core.Enums.CacheTypes;
import CB_Core.Import.ImporterProgress;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;

public class WaypointDAO
{
	public void WriteToDatabase(Waypoint WP)
	{
		int newCheckSum = createCheckSum(WP);
		Replication.WaypointChanged(WP.CacheId, WP.checkSum, newCheckSum, WP.GcCode);
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
			Database.Data.insert("Waypoint", args);

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

	public void UpdateDatabase(Waypoint WP)
	{
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
				Database.Data.update("Waypoint", args, "CacheId=" + WP.CacheId + " and GcCode=\"" + WP.GcCode + "\"", null);
			}
			catch (Exception exc)
			{
				return;

			}

			args = new Parameters();
			args.put("hasUserData", true);
			try
			{
				Database.Data.update("Caches", args, "Id = ?", new String[]
					{ String.valueOf(WP.CacheId) });
			}
			catch (Exception exc)
			{
				return;
			}

			WP.checkSum = newCheckSum;
		}
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
		sCheckSum += GlobalCore.FormatLatitudeDM(WP.Pos.getLatitude());
		sCheckSum += GlobalCore.FormatLongitudeDM(WP.Pos.getLongitude());
		sCheckSum += WP.Description;
		sCheckSum += WP.Type.ordinal();
		sCheckSum += WP.Clue;
		sCheckSum += WP.Title;
		if (WP.IsStart) sCheckSum += "1";
		return (int) GlobalCore.sdbm(sCheckSum);
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
		// TODO Auto-generated method stub
		for (Waypoint wp : cache.waypoints)
		{
			if (except == wp) continue;
			if (wp.IsStart)
			{
				wp.IsStart = false;
				WaypointDAO waypointDAO = new WaypointDAO();
				waypointDAO.UpdateDatabase(wp);
			}
		}
	}

}
