package de.droidcachebox.maps;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.map.Track;

public interface Router {
    boolean open();
    void close();
    Track getTrack(final Coordinate start, final Coordinate dest);
}
