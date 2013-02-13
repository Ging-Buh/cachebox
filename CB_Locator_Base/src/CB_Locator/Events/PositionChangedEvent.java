package CB_Locator.Events;

public interface PositionChangedEvent
{
	public enum Priority
	{
		Low, Normal, High
	}

	/**
	 * Position is changed! Get the new Location from Locator!!!
	 */
	public abstract void PositionChanged();

	/**
	 * Orientation is changed! Get the new Orientation from Locator!!!
	 */
	public abstract void OrientationChanged();

	/**
	 * Return the Name of this Receiver, for Debug
	 * 
	 * @return
	 */
	public abstract String getReceiverName();

	/**
	 * Return the priority for this Receiver!</br> Events are sorted by priority!</br> !!!! If display is switched off, only Priority== high
	 * will receive !!!!!
	 * 
	 * @return
	 */
	public abstract Priority getPriority();

}
