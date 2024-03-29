package de.droidcachebox.database;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;

/**
 * Class with Cache and WP as return Type
 *
 * @author Longri
 */
public class CacheWithWP {
    private final Cache cache;
    private final Waypoint waypoint;

    public CacheWithWP(Cache cache, Waypoint waypoint) {
        this.cache = cache;
        this.waypoint = waypoint;
    }

    public Cache getCache() {
        return cache;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }
}
