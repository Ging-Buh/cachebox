package de.droidcachebox;

import de.droidcachebox.database.Cache;

import java.util.ArrayList;

public class WaypointListChangedEventList {
    public static ArrayList<WaypointListChangedEvent> list = new ArrayList<WaypointListChangedEvent>();

    public static void Add(WaypointListChangedEvent event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void Remove(WaypointListChangedEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static void Call(final Cache cache) {
        // Aufruf aus in einen neuen Thread packen
        if (cache != null) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized (list) {
                        for (WaypointListChangedEvent event : list) {
                            event.WaypointListChanged(cache);
                        }
                    }
                }
            });

            thread.run();
        }

    }

}
