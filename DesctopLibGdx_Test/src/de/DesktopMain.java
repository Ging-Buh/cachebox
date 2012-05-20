package de;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Map.Layer;
import CB_Core.Map.RouteOverlay;
import CB_Core.Math.Size;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;
import CB_Core.Types.Categories;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Locator;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ViewConst;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;

import de.Map.Manager;

public class DesktopMain {

	static GL_Listener CB_UI;

	public static void test(devicesSizes ui, boolean debug, boolean scissor,
			final boolean simulate) {
		GlobalCore.platform = Plattform.Desktop;

		DesktopLogger iLogger = new DesktopLogger();

		InitalConfig();
		// Config.settings.MapViewDPIFaktor.setValue(1);
		// Config.settings.MapViewFontFaktor.setValue(1);
		//
		// Config.settings.OsmMinLevel.setValue(2);
//		 Config.settings.MapShowCompass.setValue(false);
		Config.settings.TrackRecorderStartup.setValue(true);

		CB_UI = new Desktop_GL_Listner(ui.Window.width, ui.Window.height);

		GL_View_Base.debug = debug;
		GL_View_Base.disableScissor = scissor;

		Manager manager = new Manager();
		// Map Packs laden
		File dir = new File(Config.settings.MapPackFolder.getValue());
		String[] files = dir.list();
		if (!(files == null)) {
			if (files.length > 0) {
				for (String file : files) {
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("pack"))
						manager.LoadMapPack(Config.settings.MapPackFolder
								.getValue() + "/" + file);
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("map")) {
						Layer layer = new Layer(file, file, "");
						layer.isMapsForge = true;
						manager.Layers.add(layer);
					}
				}
			}
		}
		Descriptor.Init();

		int sw = ui.Window.height > ui.Window.width ? ui.Window.width
				: ui.Window.height;
		sw /= ui.Density;

		// chek if tablet

		GlobalCore.isTab = sw > 400 ? true : false;

		UiSizes.initial(ui);
		new LwjglApplication(CB_UI, "Game", ui.Window.width, ui.Window.height,
				false);

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Run(simulate);
			}
		};
		timer.schedule(task, 600);

	}

	private static void Run(boolean simulate) {
		CB_UI.onStart();
		// CB_UI.setGLViewID(ViewConst.MAP_CONTROL_TEST_VIEW);
		CB_UI.setGLViewID(ViewConst.TEST_VIEW);
		// CB_UI.setGLViewID(ViewConst.CREDITS_VIEW);
		// CB_UI.setGLViewID(ViewConst.GL_MAP_VIEW);
		// CB_UI.setGLViewID(ViewConst.ViewConst.ABOUT_VIEW);
		CB_UI.setGLViewID(ViewConst.TEST_LIST_VIEW);

		Gdx.input.setInputProcessor((InputProcessor) CB_UI);

		if (!simulateGpsWithGpxFile(simulate)) {
			// set Debug Pos
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					// Coordinate pos = new
					// Coordinate("N 52 27.130  E 13 33.117");
					Coordinate pos = new Coordinate("N 48 00.00  E 12 00.000");
					Locator Loc = new Locator();

					Loc.setLocation(pos.Latitude, pos.Longitude, 100, true,
							175, true, 45, 95, "GPS");
					PositionChangedEventList.PositionChanged(Loc);
					setBearing();
				}
			};
			timer.schedule(task, 5000);
		}

	}

	private static RouteOverlay.Trackable simulationRoute = null;

	private static boolean simulateGpsWithGpxFile(boolean simulate) {
		if (simulate) {
			File dir = new File(Config.settings.TrackFolder.getValue());
			String[] files = dir.list();
			if (!(files == null)) {
				if (files.length > 0) {
					for (String file : files) {
						if (file.equalsIgnoreCase("simulation.gpx")) {
							// Simmulations GPX gefunden Punkte Laden
							
							file=Config.settings.TrackFolder.getValue()+"/"+file;
							
							simulationRoute = RouteOverlay.LoadRoute(file,
									Color.BLACK, 0);
						}
					}
				}
			}

			if (simulationRoute != null) {
				// Run simulation
				runSimulation();
				return true;
			}
		}
		return false;
	}

	private static int trackPointIndex = 0;
	private static int trackPointIndexEnd = 0;
	private static Date trackStartTime;

	private static void runSimulation() {
		trackPointIndexEnd = simulationRoute.Points.size() - 1;
		trackStartTime = simulationRoute.Points.get(trackPointIndex).TimeStamp;
		long nextTimeStamp = (simulationRoute.Points.get(trackPointIndex+1).TimeStamp.getTime()-simulationRoute.Points.get(trackPointIndex).TimeStamp.getTime());
		
		nextTimeStamp/=8; // ein wenig schneller ablaufen lassen
		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				TrackPoint trk= simulationRoute.Points.get(trackPointIndex);
				Coordinate pos = new Coordinate(trk.Y, trk.X);
				Locator Loc = new Locator();

				Loc.setLocation(pos.Latitude, pos.Longitude, 100, true,
						175, true, (float) trk.Direction, 95, "GPS");
				PositionChangedEventList.PositionChanged(Loc);
								
				if(trackPointIndex<trackPointIndexEnd-2)
				{
					trackPointIndex++;
					runSimulation();
				}
			}
		};
		timer.schedule(task, nextTimeStamp);
		
		
	}

	private static int Bearing = 45;

	private static void setBearing() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// Coordinate pos = new Coordinate("N 52 27.130  E 13 33.117");
				Coordinate pos = new Coordinate("N 48 00.00  E 12 00.000");
				Locator Loc = new Locator();
				Bearing += 5;
				Loc.setLocation(pos.Latitude, pos.Longitude, 100, true, 175,
						true, Bearing, 95, "GPS");
				PositionChangedEventList.PositionChanged(Loc);
				setBearing();
			}
		};
		timer.schedule(task, 1000);

	}

	/**
	 * Initialisiert die Config für die Tests! initialisiert wird die Config mit
	 * der unter Testdata abgelegten config.db3
	 */
	public static void InitalConfig() {

		if (Config.settings != null && Config.settings.isLoaded())
			return;

		// Read Config
		String workPath = "./testdata";

		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		try {
			Database.Settings = new TestDB(DatabaseType.Settings);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (!FileIO.DirectoryExists(Config.WorkPath))
			return;
		Database.Settings.StartUp(Config.WorkPath + "/Config.db3");
		Config.settings.ReadFromDB();
		Config.AcceptChanges();

		try {
			Database.Data = new TestDB(DatabaseType.CacheBox);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String database = Config.settings.DatabasePath.getValue();
		Database.Data.StartUp(database);

		Config.settings.ReadFromDB();

		String FilterString = Config.settings.Filter.getValue();
		GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(
				FilterProperties.presets[0]) : new FilterProperties(
				FilterString);
		String sqlWhere = GlobalCore.LastFilter.getSqlWhere();

		GlobalCore.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();

		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

		try {
			Database.FieldNotes = new TestDB(DatabaseType.FieldNotes);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User"))
			return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

		try {
			GlobalCore.Translations
					.ReadTranslationsFile(Config.settings.Sel_LanguagePath
							.getValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
