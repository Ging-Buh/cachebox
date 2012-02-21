package CB_Core.Events;

import CB_Core.Types.Locator;

public interface PositionChangedEvent
{
	public void PositionChanged(Locator locator);

	public void OrientationChanged(float heading);
}
