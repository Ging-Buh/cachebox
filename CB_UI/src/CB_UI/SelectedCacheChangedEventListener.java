package CB_UI;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public interface SelectedCacheChangedEventListener {
    void selectedCacheChanged(Cache selectedCache, Waypoint waypoint);
}