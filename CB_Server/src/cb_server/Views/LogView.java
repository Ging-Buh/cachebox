package cb_server.Views;

import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;
import cb_server.Events.SelectedCacheChangedEventList;
import cb_server.Events.SelectedCacheChangedEventListner;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class LogView extends CB_ViewBase implements SelectedCacheChangedEventListner {

	private static final long serialVersionUID = 2314353959099189624L;
	private final VerticalLayout list;
	private final Panel panel;

	public LogView() {
		super();
		list = new VerticalLayout();
		panel = new Panel();
		panel.setContent(list);
		panel.setHeight("100%");
		this.setCompositionRoot(panel);

		Label label = new Label("<table border=\"1\" frame=\"void\" cellspacing=\"5\" cellpadding=\"5\"><tr><th>hallo <br> <i>Hubert</i> aslkfjalskd fklas jfülkasjlkfj asülkdf jasljf laüsjg lkadjglk asdjglkajdlkdgj slkgjla kjglaj glaj lgkajlk gjasdülkgj alsdkg jlüksdgj lüakdsjg lkajglkjdglkksjdlgkjsdlkfgj sdülkfg jsdülkgjüslkdjg sdgjs dlkgsdlkjg sdjgs jfglsjd glsdj glsjdl�g jsd�lfgj sld�kgjsldjgsld</th></td></table>", ContentMode.HTML);
		list.addComponent(label);

		label = new Label("<table border=\"1\" frame=\"void\" cellspacing=\"5\" cellpadding=\"5\"><tr><th>hallo <br> <i>Martina</i></th></td></table>", ContentMode.HTML);
		list.addComponent(label);

		this.setSizeFull();

		SelectedCacheChangedEventList.Add(this);

	}

	@Override
	public void SelectedCacheChangedEvent(Cache cache2, Waypoint waypoint, boolean cacheChanged, boolean waypointChanged) {
		list.removeAllComponents();

		CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
		cleanLogs = Database.Logs(cache2);// cache.Logs();

		for (int i = 0, n = cleanLogs.size(); i < n; i++) {
			LogEntry logEntry = cleanLogs.get(i);
			String log = "<table border=\"1\" frame=\"void\" cellspacing=\"0\" cellpadding=\"5\" style=\"margin: 5px;\">";
			log += "<tr><th>" + logEntry.Type + " - " + logEntry.Finder + " - " + logEntry.Timestamp.toLocaleString() + "</th></tr>";
			Label label = new Label(log + "<tr><td>" + logEntry.Comment + "</td></tr></table>", ContentMode.HTML);
			list.addComponent(label);
		}
	}

}
