package de.droidcachebox.DAO;

import java.util.Iterator;

import CB_Core.Enums.CacheTypes;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.Replication.Replication;

public class WaypointDAO {
	public void WriteToDatabase(Waypoint WP) {
		int newCheckSum = createCheckSum(WP);
		Replication.WaypointChanged(WP.CacheId, WP.checkSum, newCheckSum, WP.GcCode);
		ContentValues args = new ContentValues();
		args.put("gccode", WP.GcCode);
		args.put("cacheid", WP.CacheId);
		args.put("latitude", WP.Latitude());
		args.put("longitude", WP.Longitude());
		args.put("description", WP.Description);
		args.put("type", WP.Type.ordinal());
		args.put("syncexclude", WP.IsSyncExcluded);
		args.put("userwaypoint", WP.IsUserWaypoint);
		args.put("clue", WP.Clue);
		args.put("title", WP.Title);

		try {
			Database.Data.myDB.insert("Waypoint", null, args);

			args = new ContentValues();
			args.put("hasUserData", true);
			Database.Data.myDB.update("Caches", args, "Id=" + WP.CacheId, null);
		} catch (Exception exc) {
			return;

		}
	}

	public void UpdateDatabase(Waypoint WP) {
		int newCheckSum = createCheckSum(WP);
		Replication.WaypointChanged(WP.CacheId, WP.checkSum, newCheckSum, WP.GcCode);
		if (newCheckSum != WP.checkSum) {
			ContentValues args = new ContentValues();
			args.put("gccode", WP.GcCode);
			args.put("cacheid", WP.CacheId);
			args.put("latitude", WP.Latitude());
			args.put("longitude", WP.Longitude());
			args.put("description", WP.Description);
			args.put("type", WP.Type.ordinal());
			args.put("syncexclude", WP.IsSyncExcluded);
			args.put("userwaypoint", WP.IsUserWaypoint);
			args.put("clue", WP.Clue);
			args.put("title", WP.Title);
			try {
				Database.Data.myDB.update("Waypoint", args, "CacheId="
						+ WP.CacheId + " and GcCode=\"" + WP.GcCode + "\"",
						null);
			} catch (Exception exc) {
				return;

			}

			args = new ContentValues();
			args.put("hasUserData", true);
			try {
				Database.Data.myDB.update("Caches", args, "Id=" + WP.CacheId,
						null);
			} catch (Exception exc) {
				return;
			}

			WP.checkSum = newCheckSum;
		}
	}

	public Waypoint getWaypoint(Cursor reader) {
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
		if (WP.Clue != null)
			WP.Clue = WP.Clue.trim();
		WP.Title = reader.getString(9).trim();
		WP.checkSum = createCheckSum(WP);

		return WP;
	}

	private int createCheckSum(Waypoint WP) {
		// for Replication
		String sCheckSum = WP.GcCode;
		sCheckSum += Global.FormatLatitudeDM(WP.Latitude());
		sCheckSum += Global.FormatLongitudeDM(WP.Longitude());
		sCheckSum += WP.Description;
		sCheckSum += WP.Type.ordinal();
		sCheckSum += WP.Clue;
		sCheckSum += WP.Title;
		return (int) Global.sdbm(sCheckSum);
	}

	public void WriteImports(Iterator<Waypoint> waypointIterator,
			int waypointCount, ImporterProgress ip) {
		ip.setJobMax("WriteWaypointsToDB", waypointCount);
		while (waypointIterator.hasNext()) {
			Waypoint waypoint = waypointIterator.next();
			ip.ProgressInkrement("WriteWaypointsToDB",
					String.valueOf(waypoint.CacheId));
			try {
				WriteImportToDatabase(waypoint);
			} catch (Exception e) {

				e.printStackTrace();
			}

		}

	}

	public void WriteImportToDatabase(Waypoint WP) {
		ContentValues args = new ContentValues();
		args.put("gccode", WP.GcCode);
		args.put("cacheid", WP.CacheId);
		args.put("latitude", WP.Latitude());
		args.put("longitude", WP.Longitude());
		args.put("description", WP.Description);
		args.put("type", WP.Type.ordinal());
		args.put("syncexclude", WP.IsSyncExcluded);
		args.put("userwaypoint", WP.IsUserWaypoint);
		args.put("clue", WP.Clue);
		args.put("title", WP.Title);

		try {
			Database.Data.myDB.insertWithOnConflict("Waypoint", null, args,
					SQLiteDatabase.CONFLICT_REPLACE);

			args = new ContentValues();
			args.put("hasUserData", true);
			Database.Data.myDB.update("Caches", args, "Id=" + WP.CacheId, null);
		} catch (Exception exc) {
			return;

		}
	}
}
