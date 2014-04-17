package CB_UI.Events;

// this is an interface for all Objects which should receive the selectedCacheChanged Event

import CB_Core.Types.CacheLite;
import CB_Core.Types.Waypoint;

public interface SelectedCacheEvent
{
	public void SelectedCacheChanged(CacheLite selectedCache, Waypoint waypoint);
}
