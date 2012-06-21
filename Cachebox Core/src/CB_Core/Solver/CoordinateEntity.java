package CB_Core.Solver;

import java.util.ArrayList;

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
		Cache selCache = CB_Core.GlobalCore.SelectedCache();
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
		if (coord == null) return "Cache/Waypoint not found: " + gcCode;
		else
			return coord.FormatCoordinate();
	}

	private boolean AskUpdateDiffCache(Cache cache, Waypoint waypoint, Coordinate coord)
	{
		String sFmt = "Change Coordinates of a waypoint which does not belong to the actual Cache?\n";
		sFmt += "Cache: [%s]\nWaypoint: [%s]\nCoordinates: [%s]";
		String s = String.format(sFmt, cache.Name, waypoint.Title, coord.FormatCoordinate());

		// return MessageBox(s, "Change Coordinates", MessageBoxButtons.YesNo, MessageBoxIcon.Question, MessageBoxDefaultButton.Button2) ==
		// DialogResult.No;

		return true;

	}

	public String SetCoordinate(String sCoord)
	{
		Coordinate coord = new Coordinate(sCoord);
		if (!coord.Valid) return "Koordinate not valid";
		WaypointDAO waypointDAO = new WaypointDAO();
		Waypoint dbWaypoint = null;
		// Suchen, ob dieser Waypoint bereits vorhanden ist.
		CoreCursor reader = Database.Data.rawQuery(
				"select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint where GcCode = \""
						+ this.gcCode + "\"", null);
		try
		{
			reader.moveToFirst();
			if (reader.isAfterLast()) return this.gcCode + " does not exist!";
			dbWaypoint = waypointDAO.getWaypoint(reader);
		}
		finally
		{
			reader.close();
		}
		if ((CB_Core.GlobalCore.SelectedCache() == null) || (CB_Core.GlobalCore.SelectedCache().Id != dbWaypoint.CacheId))
		{
			// Zuweisung soll an einen Waypoint eines anderen als dem aktuellen Cache gemacht werden
			// Sicherheitsabfrage, ob diese Zuweisung richtig ist!
			CacheDAO cacheDAO = new CacheDAO();
			Cache cache = cacheDAO.getFromDbByCacheId(dbWaypoint.CacheId);
			if (!AskUpdateDiffCache(cache, dbWaypoint, coord)) return "Refused to change Coordinates of Waypoint [" + dbWaypoint.Title
					+ "] of Cache [" + cache.Name + "] with Coordinates [" + coord.FormatCoordinate() + "]";
		}
		dbWaypoint.Pos.Latitude = coord.Latitude;
		dbWaypoint.Pos.Longitude = coord.Longitude;
		// waypointDAO.UpdateDatabase(dbWaypoint);

		// evtl. bereits geladenen Waypoint aktualisieren
		Cache cacheFromCacheList = Database.Data.Query.GetCacheById(dbWaypoint.CacheId);
		if (cacheFromCacheList != null)
		{
			for (Waypoint wp : cacheFromCacheList.waypoints)
			{
				if (wp.GcCode.equalsIgnoreCase(this.gcCode))
				{
					wp.Pos.Latitude = coord.Latitude;
					wp.Pos.Longitude = coord.Longitude;
					for (MysterySolution sol : Database.Data.Query.MysterySolutions)
					{
						if ((sol.Cache == cacheFromCacheList) && (sol.Waypoint == wp))
						{
							sol.Latitude = coord.Latitude;
							sol.Longitude = coord.Longitude;
						}
					}
					break;
				}
			}
			if (CB_Core.GlobalCore.SelectedCache() == cacheFromCacheList)

			// Views.WaypointView.View.Refresh();
			;

		}
		return "Test Only - NOT UPDATED IN DATABASE: " + gcCode + "=" + coord.FormatCoordinate();
	}

	@Override
	public String ToString()
	{
		return "Gc" + Id + ":(" + gcCode + ")";
	}

}
