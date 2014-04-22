package CB_Core.Types;

import java.util.Arrays;
import java.util.Date;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Locator.Coordinate;
import CB_Utils.DB.CoreCursor;

public class Waypoint extends WaypointLite
{
	private static final long serialVersionUID = 3907350162829741488L;

	// / Kommentartext
	private byte[] Description;

	// / Lösung einer QTA
	private byte[] Clue;

	public int checkSum = 0; // for replication

	public Date time;

	public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue,
			String title)
	{
		setGcCode(gcCode);
		CacheId = cacheId;
		Pos = new Coordinate(latitude, longitude);
		setDescription(description);
		Type = type;
		IsSyncExcluded = true;
		IsUserWaypoint = true;
		setClue(clue);
		setTitle(title);
		IsStart = false;
	}

	public Waypoint(WaypointLite waypointLite)
	{
		CacheId = waypointLite.CacheId;
		GcCode = waypointLite.GcCode;
		Pos = waypointLite.Pos;
		Type = waypointLite.Type;
		IsUserWaypoint = waypointLite.IsUserWaypoint;
		IsSyncExcluded = waypointLite.IsSyncExcluded;
		IsStart = waypointLite.IsStart;

		// read missing values from DB
		CoreCursor reader = Database.Data.rawQuery("select Description, Clue, Title from Waypoint where GcCode = ?", new String[]
			{ getGcCode() });
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			this.setDescription(reader.getString(0));
			this.setClue(reader.getString(1));
			this.setTitle(reader.getString(2).trim());
			reader.moveToNext();

		}
		reader.close();

	}

	public Waypoint()
	{
		CacheId = -1;
		setGcCode("");
		Pos = new Coordinate();
		IsStart = false;
	}

	public String getDescription()
	{
		if (Description == null) return EMPTY_STRING;
		return new String(Description, UTF_8);
	}

	public void setDescription(String description)
	{
		if (description == null)
		{
			Description = null;
			return;
		}
		Description = description.getBytes(UTF_8);
	}

	public String getClue()
	{
		if (Clue == null) return EMPTY_STRING;
		return new String(Clue, UTF_8);
	}

	public void setClue(String clue)
	{
		if (clue == null)
		{
			Clue = null;
			return;
		}
		Clue = clue.getBytes(UTF_8);
	}

	public void clear()
	{
		CacheId = -1;
		setGcCode("");
		Pos = new Coordinate();
		setTitle("");
		setDescription("");
		Type = null;
		IsUserWaypoint = false;
		IsSyncExcluded = false;
		setClue("");
		checkSum = 0;
		time = null;
	}

	public WaypointLite copy()
	{
		return new Waypoint(getGcCode(), Type, getDescription(), Pos.getLatitude(), Pos.getLongitude(), CacheId, getClue(), getTitle());
	}

	@Override
	public String toString()
	{
		return "WP-Lite:" + getGcCode() + " " + Pos.toString();
	}

	public void dispose()
	{
		setGcCode(null);
		Pos = null;
		setTitle(null);
		setDescription(null);
		Type = null;
		setClue(null);
		time = null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o instanceof WaypointLite)
		{
			WaypointLite w = (WaypointLite) o;

			if (!Arrays.equals(GcCode, w.GcCode)) return false;
			if (!this.Pos.equals(w.Pos)) return false;
			return true;
		}
		else if (o instanceof Waypoint)
		{
			Waypoint w = (Waypoint) o;

			if (!Arrays.equals(GcCode, w.GcCode)) return false;
			if (!this.Pos.equals(w.Pos)) return false;
			return true;
		}

		return false;
	}

	/**
	 * Returns the WaypointLite Object from this waypoint!
	 * 
	 * @return
	 */
	public WaypointLite makeLite()
	{
		return new WaypointLite(CacheId, GcCode, Pos, Type, IsUserWaypoint, IsSyncExcluded, IsStart);
	}

}
