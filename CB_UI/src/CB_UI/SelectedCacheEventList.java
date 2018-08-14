package CB_UI;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import java.util.ArrayList;

public class SelectedCacheEventList {
    public static ArrayList<SelectedCacheEvent> list = new ArrayList<SelectedCacheEvent>();
    private static Cache lastSelectedCache;
    private static Waypoint lastSelectedWayPoint;
    private static Thread selectChangeThread;

    public static void Add(SelectedCacheEvent event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void Remove(SelectedCacheEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static void Call(final Cache selectedCache, final Waypoint waypoint) {
        boolean change = true;

        if (lastSelectedCache != null) {
            if (lastSelectedCache == selectedCache) {
                if (lastSelectedWayPoint != null) {
                    if (lastSelectedWayPoint == waypoint)
                        change = false;
                } else {
                    if (waypoint == null)
                        change = false;
                }
            }
        }

        if (change)
            GlobalLocationReceiver.resetApproach();

        if (selectChangeThread != null) {
            if (selectChangeThread.getState() != Thread.State.TERMINATED)
                return;
            else
                selectChangeThread = null;
        }

        if (selectedCache != null) {
            selectChangeThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized (list) {
                        for (SelectedCacheEvent event : list) {
                            event.SelectedCacheChanged(selectedCache, waypoint);
                        }

                        // save last selected Cache in to DB
                        // nur beim Verlassen des Programms und DB-Wechsel
                        // Config.settings.LastSelectedCache.setValue(cache.GcCode);
                        // Config.AcceptChanges();
                    }
                }
            });

            selectChangeThread.start();
        }

    }
}
