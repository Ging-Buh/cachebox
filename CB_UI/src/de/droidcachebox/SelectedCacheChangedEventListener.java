package de.droidcachebox;

import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;

public interface SelectedCacheChangedEventListener {
    void selectedCacheChanged(Cache selectedCache, Waypoint waypoint);
}
