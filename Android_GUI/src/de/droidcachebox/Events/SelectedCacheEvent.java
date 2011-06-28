package de.droidcachebox.Events;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event
import java.util.ArrayList;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;

public interface SelectedCacheEvent {
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint);
}
