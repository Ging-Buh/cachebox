package de.droidcachebox;

import java.util.concurrent.CopyOnWriteArrayList;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.utils.log.Log;

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

    public void fire(final Cache selectedCache, final Waypoint waypoint) {
        if (selectedCache != null) {
            GlobalLocationReceiver.resetApproach();
        }

        if (selectChangeThread != null) {
            try {
                Log.debug("Cache changed event", "still running. Won't change to cache!");
                wait(1000);
            } catch (Exception ignored) {
            }
            return;
        }

        selectChangeThread = new Thread(() -> {
            for (CacheSelectionChangedListener listener : this) {
                try {
                    Log.debug("'Do selected Cache change' by ", listener.toString());
                    listener.handleCacheSelectionChanged(selectedCache, waypoint);
                } catch (Exception ex) {
                    Log.err(listener.toString(), selectedCache == null ? "Geocache = null" : ex.toString(), ex);
                }
            }
            Log.debug("CacheSelectionChangedListeners", "handle Cache changed for all listeners called.");
            selectChangeThread = null;
        });
        selectChangeThread.start();

    }

    public interface CacheSelectionChangedListener {
        void handleCacheSelectionChanged(Cache selectedCache, Waypoint waypoint);
    }
}
