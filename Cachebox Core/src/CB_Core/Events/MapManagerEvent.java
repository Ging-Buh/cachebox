package CB_Core.Events;

import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;

public interface MapManagerEvent
{
	public byte[] GetMapTile(Layer CurrentLayer, Descriptor desc);
}
