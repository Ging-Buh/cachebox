package CB_UI;

// this is an interface for all Objects which should receive the selectedCacheChanged Event

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public interface SelectedCacheEvent {
    public void SelectedCacheChanged(Cache selectedCache, Waypoint waypoint);
}
