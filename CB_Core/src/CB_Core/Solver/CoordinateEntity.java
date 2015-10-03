package CB_Core.Solver;

import java.util.ArrayList;

import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Translation_Base.TranslationEngine.Translation;
import de.cb.sqlite.CoreCursor;

public class CoordinateEntity extends Entity
{

	private String gcCode = "";

	public CoordinateEntity(Solver solver, int id, String gcCode)
	{
		super(solver, id);
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
					return new CoordinateGPS(reader.getDouble(1), reader.getDouble(2));
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
		// Cache selCache = CB_UI.GlobalCore.getSelectedCache();
		Cache selCache = null;
		if (Solver.solverCacheInterface != null)
		{
			selCache = Solver.solverCacheInterface.sciGetSelectedCache();
		}
		Coordinate coord = null;
		if (selCache != null)
		// In 99,9% der F�lle d�rfte der Wegpunkt zum aktuellen Cache geh�ren
		{
			if (selCache.getGcCode().equalsIgnoreCase(gcCode))
			{
				coord = selCache.Pos;
			}
			else
			{
				for (int i = 0, n = selCache.waypoints.size(); i < n; i++)
				{
					Waypoint wp = selCache.waypoints.get(i);
					if (wp.getGcCode().equalsIgnoreCase(gcCode))
					{
						coord = wp.Pos;
						break;
					}
				}
			}
		}
		if (coord == null)
		// gesuchten Waypoint nicht im aktuellen Cache gefunden, jetzt alle Caches mit den passenden GC/OC etc. Code suchen
		coord = LoadFromDB("select GcCode, Latitude, Longitude from Caches where GcCode = \"" + this.gcCode + "\"");
		if (coord == null)
		// gesuchter Waypoint ist kein Cache-Waypoint, jetzt in Waypoint-Tabelle danach suchen
		coord = LoadFromDB("select GcCode, Latitude, Longitude from Waypoint where GcCode = \"" + this.gcCode + "\"");
		if (coord == null) return Translation.Get("CacheOrWaypointNotFound".hashCode(), gcCode);
		else
			return coord.FormatCoordinate();
	}

	public String SetCoordinate(String sCoord)
	{
		if (Solver.isError(sCoord)) return sCoord;
		Coordinate coord = new CoordinateGPS(sCoord);
		if (!coord.isValid()) return Translation.Get("InvalidCoordinate".hashCode(), "SetCoordinate", sCoord);
		WaypointDAO waypointDAO = new WaypointDAO();
		Waypoint dbWaypoint = null;
		// Suchen, ob dieser Waypoint bereits vorhanden ist.
		CoreCursor reader = Database.Data.rawQuery(WaypointDAO.SQL_WP_FULL + " where GcCode = \"" + this.gcCode + "\"", null);
		try
		{
			reader.moveToFirst();
			if (reader.isAfterLast()) return Translation.Get("CacheOrWaypointNotFound".hashCode(), this.gcCode);
			dbWaypoint = (Waypoint) waypointDAO.getWaypoint(reader, true);
		}
		finally
		{
			reader.close();
		}
		// if ((CB_UI.GlobalCore.getSelectedCache() == null) || (CB_UI.GlobalCore.getSelectedCache().Id != dbWaypoint.CacheId))
		if (Solver.solverCacheInterface != null)
		{
			if ((Solver.solverCacheInterface.sciGetSelectedCache() == null)
					|| (Solver.solverCacheInterface.sciGetSelectedCache().Id != dbWaypoint.CacheId))
			{
				// Zuweisung soll an einen Waypoint eines anderen als dem aktuellen Cache gemacht werden.
				// Vermutlich Tippfehler daher Update verhindern. Modale Dialoge gehen in Android nicht
				CacheDAO cacheDAO = new CacheDAO();
				Cache cache = cacheDAO.getFromDbByCacheId(dbWaypoint.CacheId);
				// String sFmt = "Change Coordinates of a waypoint which does not belong to the actual Cache?\n";
				// sFmt += "Cache: [%s]\nWaypoint: [%s]\nCoordinates: [%s]";
				// String s = String.format(sFmt, cache.Name, waypoint.Title, coord.FormatCoordinate());
				// MessageBox(s, "Solver", MessageBoxButtons.YesNo, MessageBoxIcon.Question, DiffCac//heListener);
				return Translation.Get("solverErrDiffCache".hashCode(), coord.FormatCoordinate(), dbWaypoint.getTitle(), cache.getName());
			}
		}
		dbWaypoint.Pos = new Coordinate(coord);

		waypointDAO.UpdateDatabase(dbWaypoint);

		// evtl. bereits geladenen Waypoint aktualisieren
		Cache cacheFromCacheList;
		synchronized (Database.Data.Query)
		{
			cacheFromCacheList = Database.Data.Query.GetCacheById(dbWaypoint.CacheId);
		}
		cacheFromCacheList = Solver.solverCacheInterface.sciGetSelectedCache();
		if (cacheFromCacheList != null)
		{
			for (int i = 0, n = cacheFromCacheList.waypoints.size(); i < n; i++)
			{
				Waypoint wp = cacheFromCacheList.waypoints.get(i);
				if (wp.getGcCode().equalsIgnoreCase(this.gcCode))
				{
					wp.Pos = new Coordinate(coord);
					break;
				}
			}
			if (Solver.solverCacheInterface != null)
			{
				if (Solver.solverCacheInterface.sciGetSelectedCache().Id == cacheFromCacheList.Id)
				{
					if (Solver.solverCacheInterface.sciGetSelectedWaypoint() == null)
					{
						Solver.solverCacheInterface.sciSetSelectedCache(Solver.solverCacheInterface.sciGetSelectedCache());
					}
					else
					{
						Solver.solverCacheInterface.sciSetSelectedWaypoint(Solver.solverCacheInterface.sciGetSelectedCache(),
								Solver.solverCacheInterface.sciGetSelectedWaypoint());
					}
				}
			}
		}
		return gcCode + "=" + coord.FormatCoordinate();
	}

	@Override
	public String ToString()
	{
		return "Gc" + Id + ":(" + gcCode + ")";
	}

}
