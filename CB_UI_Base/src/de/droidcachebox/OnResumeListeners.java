package de.droidcachebox;

import de.droidcachebox.utils.log.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class OnResumeListeners extends CopyOnWriteArrayList<OnResumeListeners.OnResumeListener> {
    private static OnResumeListeners onResumeListeners;

    public static OnResumeListeners getInstance() {
        if (onResumeListeners == null)
            onResumeListeners = new OnResumeListeners();
        return onResumeListeners;
    }

    public boolean addListener(OnResumeListener listener) {
        if (!contains(listener))
            return super.add(listener);
        else
            return false;
    }

    public void onResume() {
        for (OnResumeListener listener : this) {
            Log.info("Fire OnResume for: ", listener.getClass().getName());
            listener.onResume();
        }
    }

    public interface OnResumeListener {
        void onResume();
    }
}