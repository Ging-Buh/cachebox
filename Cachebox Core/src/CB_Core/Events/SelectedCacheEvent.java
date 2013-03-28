package CB_Core.Events;

// this is an interface for all Objects which should receive the selectedCacheChanged Event

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public interface SelectedCacheEvent
{
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint);
}
