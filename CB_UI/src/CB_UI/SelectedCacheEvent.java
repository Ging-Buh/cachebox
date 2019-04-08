package CB_UI;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public interface SelectedCacheEvent {
    void SelectedCacheChanged(Cache selectedCache, Waypoint waypoint);
}
