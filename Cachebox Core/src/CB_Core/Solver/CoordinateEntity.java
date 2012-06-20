package CB_Core.Solver;

import java.util.ArrayList;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
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

	public String SetCoordinate(String sCoord)
	{
		Coordinate coord = new Coordinate(sCoord);
		return "SetCoordinate coming soon: " + coord.FormatCoordinate();
	}

	@Override
	public String ToString()
	{
		return "Gc" + Id + ":(" + gcCode + ")";
	}

}
