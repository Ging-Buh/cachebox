package de.droidcachebox.Events;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event
import java.util.ArrayList;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;



public interface SelectedCacheEvent {
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint);
}
