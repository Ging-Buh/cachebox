package CB_Core.Import;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import CB_Core.Types.Cache;

public class ImportHandler implements IImportHandler {

	List<Cache> caches = new LinkedList<Cache>();
	
	public void handleCache(Cache cache) {
		caches.add( cache );
	}
	
	public Iterator<Cache> getCacheIterator() {
		return caches.iterator();
	}

}
