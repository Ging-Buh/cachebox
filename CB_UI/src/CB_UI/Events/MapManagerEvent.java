package CB_UI.Events;

import CB_Core.Map.Descriptor;
import CB_UI.Map.Layer;

public interface MapManagerEvent
{
	public byte[] GetMapTile(Layer CurrentLayer, Descriptor desc);
}
