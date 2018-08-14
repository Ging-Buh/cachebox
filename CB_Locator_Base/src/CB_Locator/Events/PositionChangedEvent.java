package CB_Locator.Events;

public interface PositionChangedEvent {
    /**
     * Position is changed! Get the new Location from Locator!!!
     */
    public abstract void PositionChanged();

    /**
     * Orientation is changed! Get the new Orientation from Locator!!!
     */
    public abstract void OrientationChanged();

    /**
     * Speed is changed! Get the new Speed from Locator!!!
     */
    public abstract void SpeedChanged();

    /**
     * Return the Name of this Receiver, for Debug
     *
     * @return the name of the listener, who receives the event
     */
    public abstract String getReceiverName();

    /**
     * Return the priority for this Receiver!</br> Events are sorted by priority!</br> !!!! If display is switched off, only Priority== high
     * will receive !!!!!
     *
     * @return
     */
    public abstract Priority getPriority();

    public enum Priority {
        Low, Normal, High
    }

}
