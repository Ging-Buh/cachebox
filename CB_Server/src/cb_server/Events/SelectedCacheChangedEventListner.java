package cb_server.Events;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event


public interface SelectedCacheChangedEventListner 
{
	// cacheChanged and waypointChanged are set when informations of cache or waypoint has changed
	public void SelectedCacheChangedEvent(Cache cache2, Waypoint waypoint, boolean cacheChanged, boolean waypointChanged);
}
