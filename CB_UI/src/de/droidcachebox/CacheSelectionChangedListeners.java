package de.droidcachebox;

import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;

import java.util.concurrent.CopyOnWriteArrayList;

public class CacheSelectionChangedListeners extends CopyOnWriteArrayList<CacheSelectionChangedListeners.CacheSelectionChangedListener> {
    private static CacheSelectionChangedListeners cacheSelectionChangedListeners;
    private static Thread selectChangeThread;

    private CacheSelectionChangedListeners() {
    }

    public static CacheSelectionChangedListeners getInstance() {
        if (cacheSelectionChangedListeners == null)
            cacheSelectionChangedListeners = new CacheSelectionChangedListeners();
        return cacheSelectionChangedListeners;
    }

    public boolean addListener(CacheSelectionChangedListener listener) {
        if (!contains(listener))
            return super.add(listener);
        else
            return false;
    }

    public void fireEvent(final Cache selectedCache, final Waypoint waypoint) {
        if (selectedCache != null) {
            GlobalLocationReceiver.resetApproach();

            if (selectChangeThread != null) {
                if (selectChangeThread.getState() != Thread.State.TERMINATED)
                    return;
                else
                    selectChangeThread = null;
            }

            selectChangeThread = new Thread(() -> {
                for (CacheSelectionChangedListener listener : this) {
                    listener.handleCacheChanged(selectedCache, waypoint);
                }
            });
            selectChangeThread.start();
        }
    }

    public interface CacheSelectionChangedListener {
        void handleCacheChanged(Cache selectedCache, Waypoint waypoint);
    }
}
