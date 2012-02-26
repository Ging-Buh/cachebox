package de;

import java.util.Timer;
import java.util.TimerTask;
import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.ViewID.UI_Pos;
import CB_Core.GL_UI.ViewID.UI_Type;
import CB_Core.Math.Size;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class main {

	static GL_Listener CB_UI;
	public static final ViewID TEST_VIEW = new ViewID(ViewID.TEST_VIEW,
			UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID CREDITS_VIEW = new ViewID(ViewID.CREDITS_VIEW,
			UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID GL_MAP_VIEW = new ViewID(ViewID.GL_MAP_VIEW,
			UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);

	private final static Size myInitialSize = new Size(480, 800);

	public static void main(String[] args) {
		DesktopLogger iLogger = new DesktopLogger();

		InitalConfig();
		Config.settings.MapViewDPIFaktor.setValue(1);

		CB_UI = new Desktop_GL_Listner(myInitialSize.width,
				myInitialSize.height);

		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.0f;
		ui.ButtonSize = new Size(65, 65);
		ui.RefSize = 64;
		ui.TextSize_Normal = 52;
		ui.ButtonTextSize = 50;
		ui.IconSize = 13;
		ui.Margin = 4;
		ui.ArrowSizeList = 11;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 8;
		ui.isLandscape = false;

		UiSizes.initial(ui);
		new LwjglApplication(CB_UI, "Game", myInitialSize.width,
				myInitialSize.height, false);

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Run();
			}
		};
		timer.schedule(task, 1000);
	}

	private static void Run() {
		CB_UI.onStart();
		CB_UI.setGLViewID(TEST_VIEW);
//		 CB_UI.setGLViewID(CREDITS_VIEW);
		// CB_UI.setGLViewID(GL_MAP_VIEW);

		Gdx.input.setInputProcessor((InputProcessor) CB_UI);

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
	}

}
