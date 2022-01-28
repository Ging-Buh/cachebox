package de.droidcachebox;

import java.util.ArrayList;

import de.droidcachebox.dataclasses.Cache;

public class WaypointListChangedEventList {
    public static ArrayList<WaypointListChangedEvent> list = new ArrayList<>();

    public static void Add(WaypointListChangedEvent event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void remove(WaypointListChangedEvent event) {
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
                            event.wayPointListChanged(cache);
                        }
                    }
                }
            });

            thread.run();
        }

    }

}
