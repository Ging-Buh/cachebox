package cb_server;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import CB_Translation_Base.TranslationEngine.Translation;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import CB_Core.CB_Core_Settings;
import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.Database.DatabaseType;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Types.Categories;
import CB_Utils.Plattform;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.ServerFileFactory;
import Rpc.RpcFunctionsServer;
import cb_rpc.Rpc_Server;
import cb_server.DB.CBServerDB;
import cb_server.Import.ImportScheduler;

public class CacheboxServer {
	public static Logger log;
	private static Rpc_Server rpcServer;
	private static Server server;
	private static String lastLoadedTranslation;
	private static final int LOG_NONE = 0;
	private static final int LOG_ERROR = 1;
	private static final int LOG_INFO = 2;
	private static final int LOG_DEBUG = 3;

	public static void main(String[] args) throws Exception {

		new ServerFileFactory();

		Plattform.used = Plattform.Server;
		initialGdxLogger();

		writeLockFile("cbserver.lock");
		log.debug(System.getProperty("sun.net.http.allowRestrictedHeaders"));
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

		if ((args.length > 0) && (args[0].equalsIgnoreCase("UI"))) {
			//Schedule a job for the event-dispatching thread:
			//creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					createAndShowGUI();
				}
			});
		} else {
			startServer(args);
		}
	}

	private static void initialGdxLogger() {
		Gdx.app = new GdxLogger();
		Gdx.app.setLogLevel(LOG_DEBUG);
		log = LoggerFactory.getLogger(CacheboxServer.class);
	}

	private static void startServer(String[] args) throws Exception {

		//copyWebContent();

		log.info("Hallo Jetty Vaadin Server");
		log.debug("Initialize Config");
		InitialConfig();
		InitialCacheDB();
		Config.settings.ReadFromDB();
		ImportScheduler.importScheduler.start();
		// Changed default Port to 8085
		int port = 8085;
		try {
			port = Integer.valueOf(args[0]);
		} catch (Exception ex) {
			// Default Port 80 einstellen
		}
		RpcFunctionsServer.jettyPort = port;

		rpcServer = new Rpc_Server(RpcFunctionsServer.class);

		// Server server = new Server(8085);
		server = new Server(port);

		// VAADIN Part
		WebAppContext webapp = new WebAppContext();
		webapp.setDescriptor("");
		webapp.setResourceBase("./WebContent");
		webapp.setContextPath("/cbserver");
		webapp.setParentLoaderPriority(true);

		// Images
		WebAppContext webappImages = new WebAppContext();
		webappImages.setDescriptor("");
		webappImages.setResourceBase(Config.mWorkPath + "/repository/images");
		webappImages.setContextPath("/images");
		webappImages.setParentLoaderPriority(true);

		// Spoiler
		WebAppContext webappSpoiler = new WebAppContext();
		webappSpoiler.setDescriptor("");
		webappSpoiler.setResourceBase(Config.mWorkPath + "/repository/spoilers");
		webappSpoiler.setContextPath("/spoilers");
		webappSpoiler.setParentLoaderPriority(true);

		// Map
		ServletContextHandler mapContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		mapContext.setContextPath("/map");
		mapContext.addServlet(new ServletHolder(new MapServlet()), "/*");

		// Icons
		ServletContextHandler iconsContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		iconsContext.setContextPath("/ics");
		iconsContext.addServlet(new ServletHolder(new IconServlet()), "/*");

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { webapp, webappImages, webappSpoiler, mapContext, iconsContext });

		server.setHandler(contexts);

		server.start();
		log.info("Vaadin Server started on port " + port);
		server.join();

	}

	private static void stopServer() {
		if (rpcServer != null) {

		}
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Database.Data.Close();
		Database.Settings.Close();
	}

	/**
	 * Copy the WebContentFolder if need
	 */
	@SuppressWarnings("unused")
	private static void copyWebContent() {

		File root = new File("./");
		File WebContentFolder = new File(root.getAbsolutePath() + "/Webcontent");
		if (WebContentFolder.exists() && WebContentFolder.isDirectory()) {
			log.info("WebContentFolder exist, NOP");
		} else {
			log.info("Missing WebContentFolder, copy from Jar");

			try {
				String destPath = root.getAbsolutePath() + "/WebContent/VAADIN";
				String JarFolder = "/VAADIN";
				CopyJarFolder(destPath, JarFolder);

				destPath = root.getAbsolutePath() + "/WebContent/WEB-INF";
				JarFolder = "/WEB-INF";
				CopyJarFolder(destPath, JarFolder);

				destPath = root.getAbsolutePath() + "/WebContent/META-INF";
				JarFolder = "/META-INF";
				CopyJarFolder(destPath, JarFolder);
			}

			catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private static void CopyJarFolder(String destPath, String JarFolder) throws URISyntaxException, FileNotFoundException, IOException {
		File dir = new File(destPath);
		dir.mkdirs();

		URL url = CacheboxServer.class.getClass().getResource(JarFolder);
		URI uri = new URI(url.toString());

		File resource = new File(uri);
		File[] listResource = resource.listFiles();
		String[] files = resource.list();
		for (int i = 0; i < files.length; i++) {
			if (listResource[i].isDirectory()) {
				//Recursive call
				String rcursiveJarFolder = JarFolder + "/" + listResource[i].getName();
				String recursiveDestPath = destPath + "/" + listResource[i].getName();
				CopyJarFolder(recursiveDestPath, rcursiveJarFolder);
				continue;
			}

			File dstfile1 = new File(dir, files[i]);
			FileInputStream is1 = new FileInputStream(listResource[i]);
			FileOutputStream fos1 = new FileOutputStream(dstfile1);
			int b1;
			while ((b1 = is1.read()) != -1) {
				fos1.write(b1);
			}
			fos1.close();
			is1.close();
		}
	}

	public static void writeLockFile(String filename) {
		String prePid = ManagementFactory.getRuntimeMXBean().getName();
		String pid = null;
		for (int i = 0; i < prePid.length(); i++)
			if (prePid.charAt(i) == '@')
				pid = prePid.substring(0, i);
		writeTextToFile(filename, pid);
	}

	public static void writeTextToFile(String filename, String text) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(text);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void InitialCacheDB() {
		Database.Data.StartUp(Config.mWorkPath + "/cachebox.db3");
		FilterProperties lastFilter = FilterInstances.ALL;

		String sqlWhere = lastFilter.getSqlWhere(CB_Core_Settings.GcLogin.getValue());
		CoreSettingsForward.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();

		synchronized (Database.Data.Query) {
			CacheListDAO cacheListDAO = new CacheListDAO();
			cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, false);
		}

	}

	public static void InitialConfig() {

		if (Config.settings != null && Config.settings.isLoaded())
			return;
		// make Setting for GC Api Key visible to input API Key by copy/paste
		CB_Core_Settings.GcAPI.changeSettingsModus(SettingModus.Normal);
		// Read Config
		String workPath = "cachebox";
		// nachschauen ob im aktuellen Ordner eine cachebox.db3 vorhanden ist und in diesem Fall den aktuellen Ordner als WorkPath verwenden
		File file = new File("cachebox.db3");
		if (file.exists()) {
			workPath = "";
		}
		File file2 = new File(workPath);
		workPath = file2.getAbsolutePath();
		log.info("WorkPath: " + workPath);

		Config.Initialize(workPath);

		// hier muss die Config Db initialisiert werden
		try {
			Database.Settings = new CBServerDB(DatabaseType.Settings);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Database.Settings.StartUp(Config.mWorkPath + "/User/Config.db3");

		Config.settings.ReadFromDB();

		try {
			Database.Data = new CBServerDB(DatabaseType.CacheBox);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Database.FieldNotes = new CBServerDB(DatabaseType.FieldNotes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
			return;
		Database.FieldNotes.StartUp(Config.mWorkPath + "/User/FieldNotes.db3");

		InitialTranslations(SettingsClass.Sel_LanguagePath.getValue());

	}

	public static void InitialTranslations(String lang) {

		//		Initial GDX.File

		Gdx.files = new LwjglFiles();

		if (Translation.that != null) {
			if (lastLoadedTranslation.equals(lang))
				return;
		}

		lastLoadedTranslation = lang;

		new Translation(Config.mWorkPath, FileType.Classpath);
		try {
			Translation.LoadTranslation(lang);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// UI
	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("HelloWorldSwing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add the ubiquitous "Hello World" label.
		JLabel label = new JLabel("Hello World");
		frame.getContentPane().add(label);

		//Display the window.
		frame.pack();
		frame.setVisible(false);

		if (SystemTray.isSupported()) {
			final PopupMenu popup = new PopupMenu();
			final TrayIcon trayIcon = new TrayIcon(createImage("/images/bulb.gif", "tray icon"));
			final SystemTray tray = SystemTray.getSystemTray();

			// Create a pop-up menu components
			MenuItem aboutItem = new MenuItem("About");
			CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
			CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
			Menu displayMenu = new Menu("Display");
			MenuItem errorItem = new MenuItem("Error");
			MenuItem warningItem = new MenuItem("Warning");
			MenuItem startItem = new MenuItem("Start CBServer");
			MenuItem stopItem = new MenuItem("Stop CBServer");
			MenuItem exitItem = new MenuItem("Exit");

			//Add components to pop-up menu
			popup.add(aboutItem);
			popup.addSeparator();
			popup.add(cb1);
			popup.add(cb2);
			popup.addSeparator();
			popup.add(displayMenu);
			displayMenu.add(errorItem);
			displayMenu.add(warningItem);
			displayMenu.add(startItem);
			displayMenu.add(stopItem);
			popup.add(exitItem);

			trayIcon.setPopupMenu(popup);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.out.println("TrayIcon could not be added.");
			}

			exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

				}
			});

			startItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								startServer(new String[] { "" });
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					thread.start();

				}
			});

			stopItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stopServer();
				}
			});
		}
	}

	//Obtain the image URL
	protected static Image createImage(String path, String description) {
		URL imageURL = CacheboxServer.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

}