package CB_Locator.Events;

import CB_Locator.Events.PositionChangedEvent.Priority;
import CB_Locator.Locator;

import java.util.ArrayList;
import java.util.Collections;

public class PositionChangedEventList {
    private static final ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();
    public static long minPosEventTime = Long.MAX_VALUE;
    public static long minOrientationEventTime = Long.MAX_VALUE;
    public static long lastPosTime = 0;
    public static long lastOrientTime = 0;
    private static long lastPositionChanged = 0;
    private static long lastOrintationChangedEvent = 0;

    public static void Add(PositionChangedEvent event) {
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

    public static void Remove(PositionChangedEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static void PositionChanged() {
        minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
        lastPosTime = System.currentTimeMillis();

        if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Locator.getMinUpdateTime())
            return;
        lastPositionChanged = System.currentTimeMillis();

        synchronized (list) {
            try {
                for (PositionChangedEvent event : list) {
                    // If display is switched off fire only events with high priority!
                    if (Locator.isDisplayOff() && (event.getPriority() != Priority.High))
                        continue;
                    try {
                        event.PositionChanged();
                    } catch (Exception e) {
                        // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(location)",
                        // event.getReceiverName(),
                        // e);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void OrientationChanged() {

        if (Locator.isDisplayOff())
            return; // Hier braucht niemand ein OriantationChangedEvent

        minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
        lastOrientTime = System.currentTimeMillis();

        if (lastOrintationChangedEvent != 0 && lastOrintationChangedEvent > System.currentTimeMillis() - Locator.getMinUpdateTime())
            return;
        lastOrintationChangedEvent = System.currentTimeMillis();

        synchronized (list) {
            for (PositionChangedEvent event : list) {
                try {
                    event.OrientationChanged();
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void SpeedChanged() {

        if (Locator.isDisplayOff())
            return; // Hier braucht niemand ein SpeedChangedEvent

        synchronized (list) {
            for (PositionChangedEvent event : list) {
                try {
                    event.SpeedChanged();
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }

    }
}
