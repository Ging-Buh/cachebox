package CB_Core.Types;

/**
 * Class with Cache and WP as return Type
 * 
 * @author Longri
 */
public class CacheWithWP
{
	private Cache c;
	private Waypoint wp;

	public CacheWithWP(Cache cache, Waypoint waypoint)
	{
		cache = c;
		wp = waypoint;
	}

	public void dispose()
	{
		c = null;
		wp = null;
	}

	public Cache getCache()
	{
		return c;
	}

	public Waypoint getWaypoint()
	{
		return wp;
	}
}
