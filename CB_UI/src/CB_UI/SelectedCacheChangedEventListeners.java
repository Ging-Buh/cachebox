package CB_UI;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import java.util.concurrent.CopyOnWriteArrayList;

public class SelectedCacheChangedEventListeners extends CopyOnWriteArrayList<SelectedCacheChangedEventListener> {
    private static SelectedCacheChangedEventListeners selectedCacheChangedEventListeners;
    private static Thread selectChangeThread;

    private SelectedCacheChangedEventListeners() {
    }

    public static SelectedCacheChangedEventListeners getInstance() {
        if (selectedCacheChangedEventListeners == null)
            selectedCacheChangedEventListeners = new SelectedCacheChangedEventListeners();
        return selectedCacheChangedEventListeners;
    }

    public boolean add(SelectedCacheChangedEventListener listener) {
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
                for (SelectedCacheChangedEventListener listener : this) {
                    listener.selectedCacheChanged(selectedCache, waypoint);
                }
            });
            selectChangeThread.start();
        }

    }
}
