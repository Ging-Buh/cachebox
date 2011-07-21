package CB_Core;

import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

public class GlobalCore {

    /// <summary>
    /// Letzte bekannte Position
    /// </summary>
    public static Coordinate LastValidPosition = new Coordinate();
    public static Coordinate Marker = new Coordinate();
    public static boolean ResortAtWork = false;
	
    private static Cache selectedCache = null;
	public static void SelectedCache(Cache cache)
	{
		selectedCache = cache;
		GlobalCore.selectedWaypoint = null;
		SelectedCacheEventList.Call(cache, null);
	}
	public static Cache SelectedCache()
	{
		return selectedCache;
	}
	
	private static Cache nearestCache = null;
	public static Cache NearestCache()
	{
		return nearestCache;
	}
	
	private static Waypoint selectedWaypoint = null;
	public static void SelectedWaypoint(Cache cache, Waypoint waypoint)
	{
		selectedCache = cache;
		selectedWaypoint = waypoint;
		SelectedCacheEventList.Call(selectedCache, waypoint);
	}
	
	public static void NearestCache(Cache nearest)
	{
		nearestCache = nearest;
	}
	public static Waypoint SelectedWaypoint()
	{
		return selectedWaypoint;
	}

    public static CB_Core.Types.Categories Categories = null;

}
