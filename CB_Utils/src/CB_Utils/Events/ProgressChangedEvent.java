package CB_Utils.Events;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event

public interface ProgressChangedEvent {
    void ProgressChangedEventCalled(String Message, String ProgressMessage, int Progress);
}
