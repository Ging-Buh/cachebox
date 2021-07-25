package de.droidcachebox;

import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.utils.log.Log;

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
        }

        if (selectChangeThread != null) {
            while (selectChangeThread.getState() != Thread.State.TERMINATED){
                try {
                    Log.debug("Cache changed event", "still running. Won't change to cache!");
                    wait(1000);
                } catch (Exception ignored) {
                }
                // return;
            }
        }

        selectChangeThread = new Thread(() -> {
            for (CacheSelectionChangedListener listener : this) {
                try {
                    listener.handleCacheChanged(selectedCache, waypoint);
                    Log.debug("'Selected Cache changed' handled by ", listener.toString());
                } catch (Exception ex) {
                    Log.err(listener.toString(), selectedCache == null ? "Geocache = null" : ex.toString());
                }
            }
        });
        selectChangeThread.start();

    }

    public interface CacheSelectionChangedListener {
        void handleCacheChanged(Cache selectedCache, Waypoint waypoint);
    }
}
