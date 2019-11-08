package de.droidcachebox.locator;

public interface PositionChangedEvent {
    /**
     * Position is changed! Get the new Location from Locator!!!
     */
    void positionChanged();

    /**
     * Orientation is changed! Get the new Orientation from Locator!!!
     */
    void orientationChanged();

    /**
     * Speed is changed! Get the new Speed from Locator!!!
     */
    void speedChanged();

    /**
     * Return the Name of this Receiver, for Debug
     *
     * @return the name of the listener, who receives the event
     */
    String getReceiverName();

    /**
     * Return the priority for this Receiver!</br> Events are sorted by priority!</br> !!!! If display is switched off, only Priority== high
     * will receive !!!!!
     *
     * @return Priority
     */
    Priority getPriority();

    enum Priority {
        Low, Normal, High
    }

}
