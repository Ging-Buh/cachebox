package CB_UI;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.Layer;

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
