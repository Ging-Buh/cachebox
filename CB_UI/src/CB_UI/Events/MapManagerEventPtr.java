package CB_UI.Events;

import CB_Core.Map.Descriptor;
import CB_UI.Map.Layer;

public class MapManagerEventPtr
{
	public static MapManagerEvent Ptr = null;

	public static byte[] OnGetMapTile(Layer layer, Descriptor descriptor)
	{
		if (Ptr != null)
		{
			return Ptr.GetMapTile(layer, descriptor);
		}
		else
			return null;
	}
}
