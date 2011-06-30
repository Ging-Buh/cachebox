package CB_Core.Events;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event


public interface ProgressChangedEvent {
		public void ProgressChangedEventCalled(String Message, String ProgressMessage, int Progress);
}
