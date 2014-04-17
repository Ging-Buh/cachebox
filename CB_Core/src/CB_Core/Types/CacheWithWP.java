package CB_Core.Types;

/**
 * Class with Cache and WP as return Type
 * 
 * @author Longri
 */
public class CacheWithWP
{
	private CacheLite cache;
	private Waypoint waypoint;

	public CacheWithWP(CacheLite cacheLite, Waypoint waypoint)
	{
		this.cache = cacheLite;
		this.waypoint = waypoint;
	}

	public void dispose()
	{
		this.cache = null;
		this.waypoint = null;
	}

	public CacheLite getCache()
	{
		return this.cache;
	}

	public Waypoint getWaypoint()
	{
		return this.waypoint;
	}
}
