package de.droidcachebox.Events;

// this is an interface for all Objects which sould receive the selectedCacheChanged Event


public interface ProgressChangedEvent {
		public void ProgressChangedEvent(String Message, String ProgressMessage, int Progress);
}
