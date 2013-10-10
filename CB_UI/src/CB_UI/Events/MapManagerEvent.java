package CB_UI.Events;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.Layer;

public interface MapManagerEvent
{
	public byte[] GetMapTile(Layer CurrentLayer, Descriptor desc);
}
