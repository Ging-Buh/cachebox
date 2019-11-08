package de.droidcachebox;

import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.Layer;

public interface MapManagerEvent {
    public byte[] GetMapTile(Layer CurrentLayer, Descriptor desc);
}
