package de;

import java.awt.Frame;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.ICallUrl;
import CB_Core.Events.platformConector.IHardwarStateListner;
import CB_Core.Events.platformConector.IQuit;
import CB_Core.Events.platformConector.IgetFileListner;
import CB_Core.Events.platformConector.IgetFileReturnListner;
import CB_Core.Events.platformConector.IgetFolderListner;
import CB_Core.Events.platformConector.IgetFolderReturnListner;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.GL_Listener.GL_Listener_Interface;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.splash;
import CB_Core.Log.Logger;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;
import CB_Core.Util.FileIO;
import CB_Core.Util.iChanged;
import CB_Locator.Location.ProviderType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.CB_Texturepacker.Desctop_Packer;
import de.Map.DesctopManager;

public class DesktopMain
{

	static GL CB_UI;

	static float compassheading = -1;

	@SuppressWarnings("unused")
	public static void start(devicesSizes ui, boolean debug, boolean scissor, final boolean simulate, final Frame frame)
	{
		GlobalCore.platform = Plattform.Desktop;
		frame.setVisible(false);

		// Initial Desctop TexturePacker
		new Desctop_Packer();

		InitalConfig();
		Config.settings.ReadFromDB();

		new DesktopLogger();
		Logger.setDebugFilePath(Config.WorkPath + "/debug.txt");
		Logger.setDebug(true);

		// create new splash
		splash sp = new splash(0, 0, ui.Window.width, ui.Window.height, "Splash");

		// create new mainView
		TabMainView ma = new TabMainView(0, 0, ui.Window.width, ui.Window.height, "mainView");

		CB_UI = new GL(ui.Window.width, ui.Window.height, sp, ma);

		GL_View_Base.debug = debug;
		GL_View_Base.disableScissor = scissor;

		if (Config.settings.installRev.getValue() < GlobalCore.CurrentRevision)
		{

			Config.settings.installRev.setValue(GlobalCore.CurrentRevision);
			Config.settings.newInstall.setValue(true);
			Config.AcceptChanges();
		}
		else
		{
			Config.settings.newInstall.setValue(false);
			Config.AcceptChanges();
		}

		new DesctopManager();

		int sw = ui.Window.height > ui.Window.width ? ui.Window.width : ui.Window.height;

		// chek if use small skin
		GlobalCore.useSmallSkin = sw < 360 ? true : false;

		sw /= ui.Density;

		// chek if tablet
		GlobalCore.isTab = sw > 400 ? true : false;

		new UiSizes();
		UiSizes.that.initial(ui);

		initialLocatorBase();

		// TODO Activate Full Screen
		if (false)
		{
			LwjglApplicationConfiguration lwjglAppCfg = new LwjglApplicationConfiguration();
			DisplayMode dispMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
			lwjglAppCfg.setFromDisplayMode(dispMode);
			lwjglAppCfg.fullscreen = true;

			new LwjglApplication(CB_UI, lwjglAppCfg);
		}
		else
		{

			LwjglApplicationConfiguration lwjglAppCfg = new LwjglApplicationConfiguration();
			DisplayMode dispMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

			lwjglAppCfg.setFromDisplayMode(dispMode);
			lwjglAppCfg.fullscreen = false;
			lwjglAppCfg.resizable = false;
			lwjglAppCfg.useGL20 = true;
			lwjglAppCfg.width = ui.Window.width;
			lwjglAppCfg.height = ui.Window.height;
			lwjglAppCfg.title = "DCB Desctop Cachebox";

			final LwjglApplication App = new LwjglApplication(CB_UI, lwjglAppCfg);
			App.getGraphics().setContinuousRendering(false);

			GL.listenerInterface = new GL_Listener_Interface()
			{

				@Override
				public void RequestRender(String requestName)
				{
					App.getGraphics().requestRendering();

				}

				@Override
				public void RenderDirty()
				{
					App.getGraphics().setContinuousRendering(false);

				}

				@Override
				public void RenderContinous()
				{
					App.getGraphics().setContinuousRendering(true);

				}
			};
		}

		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				Run(simulate);
			}
		};
		timer.schedule(task, 600);

		// ''''''''''''''''''''''
		platformConector.setisOnlineListner(new IHardwarStateListner()
		{

			@Override
			public boolean isOnline()
			{

				return true;
			}

			@Override
			public boolean isGPSon()
			{

				return true;
			}

			@Override
			public void vibrate()
			{

			}

		});

		platformConector.setGetFileListner(new IgetFileListner()
		{
			@Override
			public void getFile(String initialPath, final String extension, String TitleText, String ButtonText,
					IgetFileReturnListner returnListner)
			{

				final String ext = extension.replace("*", "");

				JFileChooser chooser = new JFileChooser();

				chooser.setCurrentDirectory(new java.io.File(initialPath));
				chooser.setDialogTitle(TitleText);

				FileFilter filter = new FileFilter()
				{

					@Override
					public String getDescription()
					{

						return extension;
					}

					@Override
					public boolean accept(File f)
					{
						if (f.getAbsolutePath().endsWith(ext)) return true;
						return false;
					}
				};

				chooser.setFileFilter(filter);

				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					if (returnListner != null) returnListner.getFieleReturn(chooser.getSelectedFile().getAbsolutePath());
					System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
				}

			}
		});

		platformConector.setGetFolderListner(new IgetFolderListner()
		{

			@Override
			public void getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner)
			{

				JFileChooser chooser = new JFileChooser();

				chooser.setCurrentDirectory(new java.io.File(initialPath));
				chooser.setDialogTitle(TitleText);

				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					if (returnListner != null) returnListner.getFolderReturn(chooser.getSelectedFile().getAbsolutePath());
					System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
				}

			}
		});

		platformConector.setQuitListner(new IQuit()
		{

			@Override
			public void Quit()
			{
				System.exit(0);

			}
		});

		DesktopClipboard dcb = new DesktopClipboard();

		if (dcb != null) GlobalCore.setDefaultClipboard(dcb);

		platformConector.setCallUrlListner(new ICallUrl()
		{

			@Override
			public void call(String url)
			{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

				if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE))
				{

					System.err.println("Desktop doesn't support the browse action (fatal)");
					System.exit(1);
				}

				try
				{

					java.net.URI uri = new java.net.URI(url);
					desktop.browse(uri);
				}
				catch (Exception e)
				{

					System.err.println(e.getMessage());
				}

			}
		});

	}

	private static void Run(boolean simulate)
	{
		CB_UI.onStart();

		Gdx.input.setInputProcessor((InputProcessor) CB_UI);

		if (simulate)
		{
			showSimmulateForm();
		}

	}

	// ################## simulation#################

	private static void showSimmulateForm()
	{
		final simulateForm sim = new simulateForm("Simulate Form");
		sim.setSize(400, 130);
		sim.setVisible(true);
	}

	/**
	 * Initialisiert die Config für die Tests! initialisiert wird die Config mit der unter Testdata abgelegten config.db3
	 */
	public static void InitalConfig()
	{

		if (Config.settings != null && Config.settings.isLoaded()) return;

		// Read Config
		String workPath = "./cachebox";

		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		try
		{
			Database.Settings = new TestDB(DatabaseType.Settings);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");

		try
		{
			Database.Data = new TestDB(DatabaseType.CacheBox);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		try
		{
			Database.FieldNotes = new TestDB(DatabaseType.FieldNotes);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		if (!FileIO.createDirectory(Config.WorkPath + "/User")) return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");
	}

	/**
	 * Initial all Locator functions
	 */
	private static void initialLocatorBase()
	{
		// ##########################################################
		// initial Locator with saved Location
		// ##########################################################
		double latitude = Config.settings.MapInitLatitude.getValue();
		double longitude = Config.settings.MapInitLongitude.getValue();
		ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;

		CB_Locator.Location initialLocation;

		if (provider == ProviderType.Saved)
		{
			initialLocation = new CB_Locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
		}
		else
		{
			initialLocation = CB_Locator.Location.NULL_LOCATION;
		}

		new CB_Locator.Locator(initialLocation);

		// ##########################################################
		// initial settings changed handling
		// ##########################################################

		// Use Imperial units?
		CB_Locator.Locator.setUseImperialUnits(Config.settings.ImperialUnits.getValue());
		Config.settings.ImperialUnits.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseImperialUnits(Config.settings.ImperialUnits.getValue());
			}
		});

		// GPS update time?
		CB_Locator.Locator.setMinUpdateTime((long) Config.settings.gpsUpdateTime.getValue());
		Config.settings.gpsUpdateTime.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setMinUpdateTime((long) Config.settings.gpsUpdateTime.getValue());
			}
		});

		// Use magnetic Compass?
		CB_Locator.Locator.setUseHardwareCompass(Config.settings.HardwareCompass.getValue());
		Config.settings.HardwareCompass.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseHardwareCompass(Config.settings.HardwareCompass.getValue());
			}
		});

		// Magnetic compass level
		CB_Locator.Locator.setHardwareCompassLevel(Config.settings.HardwareCompassLevel.getValue());
		Config.settings.HardwareCompassLevel.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setHardwareCompassLevel(Config.settings.HardwareCompassLevel.getValue());
			}
		});
	}

}
