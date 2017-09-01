package cb_server.Views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.CustomComponent;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheListChangedEventListener;
import cb_server.Events.SelectedCacheChangedEventList;
import cb_server.Events.SelectedCacheChangedEventListner;

public abstract class CB_ViewBase extends CustomComponent implements SelectedCacheChangedEventListner, CacheListChangedEventListener {

	private static final long serialVersionUID = 9051645487161218696L;
	protected Logger log;

	//	protected CacheList cacheList = null;

	public CB_ViewBase() {
		super();
		log = LoggerFactory.getLogger(CB_ViewBase.class);
		SelectedCacheChangedEventList.Add(this);
		CacheListChangedEventList.Add(this);
	}

	@Override
	public void CacheListChangedEvent() {
		this.getUI().access(new Runnable() {
			@Override
			public void run() {
				cacheListChanged();
			}
		});
	}

	public void cacheListChanged() {
		log.debug("CacheListChanged");
	}

	public void removeFromListener() {
		SelectedCacheChangedEventList.Remove(this);
		CacheListChangedEventList.Remove(this);

	}

}