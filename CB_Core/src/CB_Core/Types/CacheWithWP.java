package CB_Core.Types;

/**
 * Class with Cache and WP as return Type
 * 
 * @author Longri
 */
public class CacheWithWP
{
	private Cache cache;
	private Waypoint waypoint;

	public CacheWithWP(Cache cache, Waypoint waypoint)
	{
		this.cache = cache;
		this.waypoint = waypoint;
	}

	public void dispose()
	{
		this.cache = null;
		this.waypoint = null;
	}

	public Cache getCache()
	{
		return this.cache;
	}

	public Waypoint getWaypoint()
	{
		return this.waypoint;
	}
}