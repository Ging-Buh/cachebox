package CB_Locator.Map;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class QueueData
{
	final LoadetSortedTiles loadedTiles = new LoadetSortedTiles();
	final LoadetSortedTiles loadedOverlayTiles = new LoadetSortedTiles();
	final Lock loadedTilesLock = new ReentrantLock();
	final Lock loadedOverlayTilesLock = new ReentrantLock();
	final SortedMap<Long, Descriptor> queuedTiles = new TreeMap<Long, Descriptor>();
	final SortedMap<Long, Descriptor> queuedOverlayTiles = new TreeMap<Long, Descriptor>();
	final Lock queuedTilesLock = new ReentrantLock();
	final Lock queuedOverlayTilesLock = new ReentrantLock();
	Layer CurrentLayer = null;
	Layer CurrentOverlayLayer = null;
}
