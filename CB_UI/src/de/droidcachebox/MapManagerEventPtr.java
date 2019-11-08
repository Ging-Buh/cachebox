package de.droidcachebox;

import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.Layer;

public class MapManagerEventPtr {
    public static MapManagerEvent Ptr = null;

    public static byte[] OnGetMapTile(Layer layer, Descriptor descriptor) {
        if (Ptr != null) {
            return Ptr.GetMapTile(layer, descriptor);
        } else
            return null;
    }
}
