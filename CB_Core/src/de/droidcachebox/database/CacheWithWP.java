package de.droidcachebox.database;

/**
 * Class with Cache and WP as return Type
 *
 * @author Longri
 */
public class CacheWithWP {
    private Cache cache;
    private Waypoint waypoint;

    public CacheWithWP(Cache cache, Waypoint waypoint) {
        this.cache = cache;
        this.waypoint = waypoint;
    }

    public void dispose() {
        cache = null;
        waypoint = null;
    }

    public Cache getCache() {
        return cache;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }
}
