package de.droidcachebox.locator;

import de.droidcachebox.locator.PositionChangedEvent.Priority;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;
import java.util.Collections;

public class PositionChangedListeners {
    private static final String sKlasse = "PositionChangedListeners";
    private static final ArrayList<PositionChangedEvent> list = new ArrayList<>();
    public static long minPosEventTime = Long.MAX_VALUE;
    public static long minOrientationEventTime = Long.MAX_VALUE;
    public static long lastPosTime = 0;
    public static long lastOrientTime = 0;
    private static long lastPositionChanged = 0;
    private static long lastOrintationChangedEvent = 0;

    public static void addListener(PositionChangedEvent event) {
        synchronized (list) {
            if (!list.contains(event)) {
                list.add(event);
                Collections.sort(list, (arg0, arg1) -> {
                    int o2 = arg0.getPriority().ordinal();
                    int o1 = arg1.getPriority().ordinal();
                    return (Integer.compare(o1, o2));
                });

            }
        }

    }

    public static void removeListener(PositionChangedEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static void positionChanged() {
        minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
        lastPosTime = System.currentTimeMillis();

        if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Locator.getInstance().getMinUpdateTime())
            return;
        lastPositionChanged = System.currentTimeMillis();

        synchronized (list) {
            try {
                for (PositionChangedEvent event : list) {
                    // If display is switched off fire only events with high priority!
                    if (Locator.getInstance().isDisplayOff() && (event.getPriority() != Priority.High))
                        continue;
                    try {
                        event.positionChanged();
                    } catch (Exception ex) {
                        Log.err(sKlasse, "positionChanged", ex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void orientationChanged() {

        if (Locator.getInstance().isDisplayOff())
            return; // Hier braucht niemand ein OriantationChangedEvent

        minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
        lastOrientTime = System.currentTimeMillis();

        if (lastOrintationChangedEvent != 0 && lastOrintationChangedEvent > System.currentTimeMillis() - Locator.getInstance().getMinUpdateTime())
            return;
        lastOrintationChangedEvent = System.currentTimeMillis();

        synchronized (list) {
            for (PositionChangedEvent event : list) {
                try {
                    event.orientationChanged();
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void speedChanged() {

        if (Locator.getInstance().isDisplayOff())
            return; // Hier braucht niemand ein SpeedChangedEvent

        synchronized (list) {
            for (PositionChangedEvent event : list) {
                try {
                    event.speedChanged();
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }

    }
}
