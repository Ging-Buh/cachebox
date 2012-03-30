package de;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
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


public class DesktopMain {

	
	static GL_Listener CB_UI;
		
	

	public static void test(devicesSizes ui) {
		DesktopLogger iLogger = new DesktopLogger();


		InitalConfig();
		Config.settings.MapViewDPIFaktor.setValue(1);
		Config.settings.MapViewFontFaktor.setValue(1);
		
		Config.settings.OsmMinLevel.setValue(2);
			

		CB_UI = new Desktop_GL_Listner(ui.Window.width,
				ui.Window.height);
		
	
//		GL_View_Base.debug = true;
//		GL_View_Base.disableScissor= true;
		
		
		int sw = ui.Window.height > ui.Window.width ? ui.Window.width
				: ui.Window.height;
		sw /= ui.Density;

		// chek if tablet

		GlobalCore.isTab = sw > 400 ? true : false;

		UiSizes.initial(ui);
		new LwjglApplication(CB_UI, "Game", ui.Window.width,
				ui.Window.height, false);

		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Run();
			}
		};
		timer.schedule(task, 600);

	}
	
	
	
	

	private static void Run() {
		CB_UI.onStart();
//		CB_UI.setGLViewID(ViewConst.MAP_CONTROL_TEST_VIEW);
		CB_UI.setGLViewID(ViewConst.TEST_VIEW);
		// CB_UI.setGLViewID(ViewConst.CREDITS_VIEW);
//		 CB_UI.setGLViewID(ViewConst.GL_MAP_VIEW);
//		CB_UI.setGLViewID(ViewConst.ViewConst.ABOUT_VIEW);
		CB_UI.setGLViewID(ViewConst.TEST_LIST_VIEW);

		Gdx.input.setInputProcessor((InputProcessor) CB_UI);
		
		
		// set Debug Pos
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Coordinate pos = new Coordinate("N 52 27.130  E 13 33.117");
				Locator Loc = new Locator();
				
				Loc.setLocation(pos.Latitude, pos.Longitude, 100, true, 175, true, 45, 95);
				PositionChangedEventList.PositionChanged(Loc);
				setBearing();
			}
		};
		timer.schedule(task, 5000);
		
	}
	
	
	private static int Bearing=45;
	private static void setBearing()
	{
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Coordinate pos = new Coordinate("N 52 27.130  E 13 33.117");
				Locator Loc = new Locator();
				Bearing+=5;
				Loc.setLocation(pos.Latitude, pos.Longitude, 100, true, 175, true, Bearing, 95);
				PositionChangedEventList.PositionChanged(Loc);
				setBearing();
			}
		};
		timer.schedule(task, 2000);
		
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
		GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0]) : new FilterProperties(
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
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");
		
		
		
		try {
			GlobalCore.Translations.ReadTranslationsFile(Config.settings.Sel_LanguagePath.getValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
