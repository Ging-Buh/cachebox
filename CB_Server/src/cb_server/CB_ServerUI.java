package cb_server;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

import CB_Core.CB_Core_Settings;
import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Types.CacheListDAO;
import CB_Core.Types.CacheList;
import cb_server.Import.ImportScheduler;
import cb_server.Views.CB_ViewBase;
import cb_server.Views.CacheListView;
import cb_server.Views.DescriptionView;
import cb_server.Views.LogView;
import cb_server.Views.MapView;
import cb_server.Views.ProgressView;
import cb_server.Views.SolverView;
import cb_server.Views.WaypointView;
import de.steinwedel.messagebox.MessageBox;

@SuppressWarnings("serial")
@Theme("cb_server")
@PreserveOnRefresh
@Push
public class CB_ServerUI extends UI implements DetachListener {
	private Logger log;
	private final MyExecutor executor = new MyExecutor();
	private final CacheList cacheList = new CacheList();
	private final LinkedList<CB_ViewBase> views = new LinkedList<>();
	private FilterProperties lastFilter = null;
	private MenuBar mainMenu = null;
	private CacheListView clv;

	@Override
	protected void init(VaadinRequest request) {
		addDetachListener(this);

		log = LoggerFactory.getLogger(CB_ServerUI.class);
		log.info("Initialize CB_ServerUI");
		//this.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
		System.out.println("New Session: " + getSession().toString());
		// Force locale "English"
		//MessageBox.RESOURCE_FACTORY.setResourceLocale(Locale.ENGLISH);
		MessageBox.setDialogSessionLanguage(Locale.ENGLISH);

		// You can use MessageBox.RESOURCES_FACTORY.setResourceBundle(basename);
		// to localize to your language

		final com.vaadin.ui.TextField gcLogin = new TextField("GCLogin");
		gcLogin.setValue(Config.settings.GcLogin.getValue());

		final Button button = new Button("Caches: " + Database.Data.Query.size());
		button.setStyleName("test");
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Config.settings.GcLogin.setValue(gcLogin.getValue());
				Config.settings.WriteToDB();
				// MessageBox.showPlain(Icon.INFO, "Settings", "Gespeichert", ButtonId.OK);
				MessageBox.createInfo().withCaption("Settings").withMessage("Gespeichert").withOkButton().open();
			}
		});

		button.setImmediate(true);
		this.setImmediate(true);
		TimerTask action = new TimerTask() {
			@Override
			public void run() {
				try {
					//					changeValue(button);
					//					ProgresssChangedEventList.Call("Tick", 100);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		};

		Timer caretaker = new Timer();
		caretaker.schedule(action, 1000, 5000);

		button.setCaption("c1");

		mainMenu = new MenuBar();
		MenuItem miImport = mainMenu.addItem("Import", null, null);
		MenuItem miGroundspeak = miImport.addItem("From Groundspeak", null);
		MenuItem miImportPQ = miGroundspeak.addItem("PocketQuery", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {

			}
		});
		MenuItem miReloadImages = miImport.addItem("Reload all Images", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				de.cb.sqlite.Database_Core.Parameters val = new de.cb.sqlite.Database_Core.Parameters();
				val.put("DescriptionImagesUpdated", false);
				val.put("ImagesUpdated", false);
				Database.Data.update("Caches", val, "", null);
			}
		});

		final Button open = new Button("Open Settings-Window");
		open.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				SettingsWindow sub = SettingsWindow.getInstanz();

				if (!UI.getCurrent().getWindows().contains(sub))

					// Add it to the root component
					UI.getCurrent().addWindow(sub);
			}
		});

		final Button bImport = new Button("Import");
		bImport.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// run Import once
				ImportScheduler.importScheduler.startOnce();
			}
		});

		final Button close = new Button("Finish Server");
		close.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				log.info("Exit Server");
				System.exit(0);
			}
		});

		MapView mv = new MapView();
		DescriptionView dv = new DescriptionView();
		clv = new CacheListView();
		WaypointView wpv = new WaypointView();
		LogView lv = new LogView();
		SolverView sv = new SolverView();
		views.add(mv);
		//		views.add(dv);
		views.add(clv);
		//		views.add(wpv);
		views.add(lv);
		views.add(sv);
		// VerticalLayout f�r Header, Inhalt und Footer erstellen
		VerticalLayout vl = new VerticalLayout();
		this.setContent(vl);
		Panel header = new Panel(); // Header
		HorizontalSplitPanel content = new HorizontalSplitPanel(); // Inhalt
		VerticalSplitPanel contentSplit = new VerticalSplitPanel();

		content.setFirstComponent(contentSplit);
		Panel footer = new Panel(); // Footer

		vl.addComponent(header);
		vl.addComponent(content);
		vl.addComponent(footer);

		vl.setSizeFull();
		vl.setExpandRatio(content, 1); // Inhalt muss den gr��ten Bereich einnehmen

		// Inhalt vom Header

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addComponent(mainMenu);
		headerLayout.addComponent(close);
		headerLayout.addComponent(open);
		headerLayout.addComponent(bImport);
		header.setContent(headerLayout);

		// Inhalt vom Content
		content.setSizeFull();

		TabSheet tabLinks = new TabSheet();
		contentSplit.setFirstComponent(tabLinks);
		contentSplit.setSplitPosition(75, Sizeable.UNITS_PERCENTAGE);
		tabLinks.setSizeFull();

		TabSheet tabRechts = new TabSheet();
		content.setSecondComponent(tabRechts);
		tabRechts.setSizeFull();
		tabRechts.addTab(dv, "DescriptionView");
		tabLinks.addTab(clv, "CacheList");
		tabRechts.addTab(mv, "MapView");
		tabRechts.addTab(lv, "Logs");
		tabRechts.addTab(sv, "Solver");

		TabSheet tabLinksUnten = new TabSheet();
		contentSplit.setSecondComponent(tabLinksUnten);
		tabLinksUnten.setSizeFull();
		tabLinksUnten.addTab(wpv, "Waypoints");

		// Inhalt vom Footer
		ProgressView progressView = new ProgressView();
		footer.setContent(progressView);

		// CacheList laden
		lastFilter = FilterInstances.ALL;
		Thread loadCacheListThread = new Thread(new LoadCacheListThread());
		loadCacheListThread.start();
	}

	public void pushChangedContent() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				getSession().lock();
				try {
					//NOTE: Comment this line below and problem will go away
					//					pusher.push();
				} finally {
					getSession().unlock();
				}
			}
		});
	}

	class MyExecutor extends ThreadPoolExecutor {
		public MyExecutor() {
			super(5, 20, 20, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		}
	}

	private class LoadCacheListThread implements Runnable {

		@Override
		public void run() {
			log.info("Load CacheList!");
			String sqlWhere = lastFilter.getSqlWhere(CB_Core_Settings.GcLogin.getValue());

			CacheListDAO cacheListDAO = new CacheListDAO();
			cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, true, false);
			log.debug("CacheList loaded!");
			//			for (CB_ViewBase view : views) {
			//				view.cacheListChanged(cacheList);
			//			}
			CacheListChangedEventList.Call();
		}

	}

	@Override
	public void detach(DetachEvent event) {
		for (CB_ViewBase view : views) {
			view.removeFromListener();
		}
	}

	@Override
	public boolean removeWindow(Window window) {
		System.out.println("Close");
		return super.removeWindow(window);
	}
}
