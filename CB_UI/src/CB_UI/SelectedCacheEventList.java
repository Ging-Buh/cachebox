package CB_UI;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import java.util.ArrayList;

public class SelectedCacheEventList {
    public static ArrayList<SelectedCacheEvent> list = new ArrayList<>();
    private static Thread selectChangeThread;

    public static void Add(SelectedCacheEvent listener) {
        synchronized (list) {
            if (!list.contains(listener))
                list.add(listener);
        }
    }

    public static void Remove(SelectedCacheEvent listener) {
        synchronized (list) {
            list.remove(listener);
        }
    }

    public static void Call(final Cache selectedCache, final Waypoint waypoint) {
        GlobalLocationReceiver.resetApproach();

        if (selectChangeThread != null) {
            if (selectChangeThread.getState() != Thread.State.TERMINATED)
                return;
            else
                selectChangeThread = null;
        }

        if (selectedCache != null) {
            selectChangeThread = new Thread(() -> {
                synchronized (list) {
                    for (SelectedCacheEvent listener : list) {
                        listener.SelectedCacheChanged(selectedCache, waypoint);
                    }
                }
            });

            selectChangeThread.start();
        }

    }
}
