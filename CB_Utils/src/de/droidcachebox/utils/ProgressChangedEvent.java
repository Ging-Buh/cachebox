package de.droidcachebox.utils;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event

public interface ProgressChangedEvent {
    void progressChangedEventCalled(String Message, String ProgressMessage, int Progress);
}
