package CB_Core.Solver;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.MysterySolution;
import CB_Core.Types.Waypoint;

public class CoordinateEntity extends Entity
{

	private String gcCode = "";

	public CoordinateEntity(int id, String gcCode)
	{
		super(id);
		this.gcCode = gcCode;
	}

	@Override
	public void GetAllEntities(ArrayList<Entity> list)
	{
	}

	@Override
	public void ReplaceTemp(Entity source, Entity dest)
	{
	}

	private Coordinate LoadFromDB(String sql)
	{
		CoreCursor reader = Database.Data.rawQuery(sql, null);
		try
		{
			reader.moveToFirst();
			while (!reader.isAfterLast())
			{
				String sGcCode = reader.getString(0).trim();
				if (sGcCode.equalsIgnoreCase(this.gcCode))
				{ // gefunden. Suche abbrechen
					return new Coordinate(reader.getDouble(1), reader.getDouble(2));
				}
				reader.moveToNext();
			}
		}
		finally
		{
			reader.close();
		}

		return null;
	}

	@Override
	public String Berechne()
	{
		Cache selCache = CB_Core.GlobalCore.getSelectedCache();
		Coordinate coord = null;
		if (selCache != null)
		// In 99,9% der Fälle dürfte der Wegpunkt zum aktuellen Cache gehören
		{
			if (selCache.GcCode.equalsIgnoreCase(gcCode)) coord = selCache.Pos;
			else
				for (Waypoint wp : selCache.waypoints)
					if (wp.GcCode.equalsIgnoreCase(gcCode))
					{
						coord = wp.Pos;
						break;
					}
		}
		if (coord == null)
		// gesuchten Waypoint nicht im aktuellen Cache gefunden, jetzt alle Caches mit den passenden GC/OC etc. Code suchen
		coord = LoadFromDB("select GcCode, Latitude, Longitude from Caches where GcCode = \"" + this.gcCode + "\"");
		if (coord == null)
		// gesuchter Waypoint ist kein Cache-Waypoint, jetzt in Waypoint-Tabelle danach suchen
		coord = LoadFromDB("select GcCode, Latitude, Longitude from Waypoint where GcCode = \"" + this.gcCode + "\"");
		if (coord == null) return GlobalCore.Translations.Get("CacheOrWaypointNotFound", gcCode);
		else
			return coord.FormatCoordinate();
	}

	public String SetCoordinate(String sCoord)
	{
		if (Solver.isError(sCoord)) return sCoord;
		Coordinate coord = new Coordinate(sCoord);
		if (!coord.Valid) return GlobalCore.Translations.Get("InvalidCoordinate", "SetCoordinate", sCoord);
		WaypointDAO waypointDAO = new WaypointDAO();
		Waypoint dbWaypoint = null;
		// Suchen, ob dieser Waypoint bereits vorhanden ist.
		CoreCursor reader = Database.Data.rawQuery(
				"select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint where GcCode = \""
						+ this.gcCode + "\"", null);
		try
		{
			reader.moveToFirst();
			if (reader.isAfterLast()) return GlobalCore.Translations.Get("CacheOrWaypointNotFound", this.gcCode);
			dbWaypoint = waypointDAO.getWaypoint(reader);
		}
		finally
		{
			reader.close();
		}
		if ((CB_Core.GlobalCore.getSelectedCache() == null) || (CB_Core.GlobalCore.getSelectedCache().Id != dbWaypoint.CacheId))
		{
			// Zuweisung soll an einen Waypoint eines anderen als dem aktuellen Cache gemacht werden.
			// Vermutlich Tippfehler daher Update verhindern. Modale Dialoge gehen in Android nicht
			CacheDAO cacheDAO = new CacheDAO();
			Cache cache = cacheDAO.getFromDbByCacheId(dbWaypoint.CacheId);
			// String sFmt = "Change Coordinates of a waypoint which does not belong to the actual Cache?\n";
			// sFmt += "Cache: [%s]\nWaypoint: [%s]\nCoordinates: [%s]";
			// String s = String.format(sFmt, cache.Name, waypoint.Title, coord.FormatCoordinate());
			// MessageBox(s, "Solver", MessageBoxButtons.YesNo, MessageBoxIcon.Question, DiffCac//heListener);
			return GlobalCore.Translations.Get("solverErrDiffCache", coord.FormatCoordinate(), dbWaypoint.Title, cache.Name);
		}
		dbWaypoint.Pos.setLatitude(coord.getLatitude());
		dbWaypoint.Pos.setLongitude(coord.getLongitude());
		waypointDAO.UpdateDatabase(dbWaypoint);

		// evtl. bereits geladenen Waypoint aktualisieren
		Cache cacheFromCacheList = Database.Data.Query.GetCacheById(dbWaypoint.CacheId);
		if (cacheFromCacheList != null)
		{
			for (Waypoint wp : cacheFromCacheList.waypoints)
			{
				if (wp.GcCode.equalsIgnoreCase(this.gcCode))
				{
					wp.Pos.setLatitude(coord.getLatitude());
					wp.Pos.setLongitude(coord.getLongitude());
					for (MysterySolution sol : Database.Data.Query.MysterySolutions)
					{
						if ((sol.Cache == cacheFromCacheList) && (sol.Waypoint == wp))
						{
							sol.Latitude = coord.getLatitude();
							sol.Longitude = coord.getLongitude();
						}
					}
					break;
				}
			}
			if (CB_Core.GlobalCore.getSelectedCache().Id == cacheFromCacheList.Id)

			// Views.WaypointView.View.Refresh();
			;

		}
		return gcCode + "=" + coord.FormatCoordinate();
	}

	@Override
	public String ToString()
	{
		return "Gc" + Id + ":(" + gcCode + ")";
	}

}
