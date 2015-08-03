package de;

import java.awt.Frame;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.mapsforge.map.model.DisplayModel;
import org.slf4j.LoggerFactory;

import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Locator.Location.ProviderType;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.splash;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.Events.platformConector.ICallUrl;
import CB_UI_Base.Events.platformConector.IHardwarStateListner;
import CB_UI_Base.Events.platformConector.IQuit;
import CB_UI_Base.Events.platformConector.IgetFileListner;
import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.Events.platformConector.IgetFolderListner;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Listener_Interface;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.Math.devicesSizes;
import CB_Utils.Plattform;
import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.PlatformSettings.iPlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingString;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.iChanged;
import ch.fhnw.imvs.gpssimulator.SimulatorMain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.CB_Texturepacker.Desctop_Packer;
import de.Map.DesktopManager;

public class DesktopMain
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(DesktopMain.class);
	static GL CB_UI;
	static float compassheading = -1;
	// Retrieve the user preference node for the package com.mycompany
	static Preferences prefs = Preferences.userNodeForPackage(de.DesktopMain.class);

	@SuppressWarnings("unused")
	public static void start(devicesSizes ui, boolean debug, boolean scissor, final boolean simulate, final Frame frame)
	{
		Plattform.used = Plattform.Desktop;
		frame.setVisible(false);

		// Initial Desctop TexturePacker
		new Desctop_Packer();

		PlatformSettings.setPlatformSettings(new iPlatformSettings()
		{

			@Override
			public void Write(SettingBase<?> setting)
			{

				if (setting instanceof SettingBool)
				{
					prefs.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
				}

				else if (setting instanceof SettingString)
				{
					prefs.put(setting.getName(), ((SettingString) setting).getValue());
				}
				else if (setting instanceof SettingInt)
				{
					prefs.putInt(setting.getName(), ((SettingInt) setting).getValue());
				}

				// Commit the edits!
				try
				{
					prefs.flush();
				}
				catch (BackingStoreException e)
				{

					e.printStackTrace();
				}

			}

			@Override
			public SettingBase<?> Read(SettingBase<?> setting)
			{
				if (setting instanceof SettingString)
				{
					String value = prefs.get(setting.getName(), ((SettingString) setting).getDefaultValue());
					((SettingString) setting).setValue(value);
				}
				else if (setting instanceof SettingBool)
				{
					boolean value = prefs.getBoolean(setting.getName(), ((SettingBool) setting).getDefaultValue());
					((SettingBool) setting).setValue(value);
				}
				else if (setting instanceof SettingInt)
				{
					int value = prefs.getInt(setting.getName(), ((SettingInt) setting).getDefaultValue());
					((SettingInt) setting).setValue(value);
				}
				setting.clearDirty();
				return setting;
			}
		});

		InitalConfig();
		Config.settings.ReadFromDB();

		// create new splash
		splash sp = new splash(0, 0, ui.Window.width, ui.Window.height, "Splash");

		// create new mainView
		TabMainView ma = new TabMainView(0, 0, ui.Window.width, ui.Window.height, "mainView");

		CB_UI = new GL(ui.Window.width, ui.Window.height, sp, ma);

		GL_View_Base.debug = debug;
		GL_View_Base.disableScissor = scissor;

		if (Config.installRev.getValue() < GlobalCore.CurrentRevision)
		{

			Config.installRev.setValue(GlobalCore.CurrentRevision);
			Config.newInstall.setValue(true);
			Config.AcceptChanges();
		}
		else
		{
			Config.newInstall.setValue(false);
			Config.AcceptChanges();
		}

		DisplayModel model = new DisplayModel();
		new DesktopManager(model);

		int sw = ui.Window.height > ui.Window.width ? ui.Window.width : ui.Window.height;

		// chek if use small skin
		GlobalCore.useSmallSkin = sw < 360 ? true : false;

		sw /= ui.Density;

		// chek if tablet
		GlobalCore.isTab = sw > 400 ? true : false;

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
			lwjglAppCfg.width = ui.Window.width;
			lwjglAppCfg.height = ui.Window.height;
			lwjglAppCfg.title = "DCB Desctop Cachebox";
			lwjglAppCfg.samples = 16;

			final LwjglApplication App = new LwjglApplication(CB_UI, lwjglAppCfg);
			App.getGraphics().setContinuousRendering(false);

			GL.listenerInterface = new GL_Listener_Interface()
			{

				@Override
				public void RequestRender()
				{
					App.getGraphics().requestRendering();

				}

				@Override
				public void RenderDirty()
				{
					App.getGraphics().setContinuousRendering(false);
					isContinousRenderMode.set(false);
				}

				@Override
				public void RenderContinous()
				{
					App.getGraphics().setContinuousRendering(true);
					isContinousRenderMode.set(true);
				}

				AtomicBoolean isContinousRenderMode = new AtomicBoolean(true);

				@Override
				public boolean isContinous()
				{
					return isContinousRenderMode.get();
				}

			};
		}

		new UiSizes();
		UiSizes.that.initial(ui);
		initialLocatorBase();

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

			@Override
			public boolean isTorchAvailable()
			{
				return true; // Simulate
			}

			private boolean torchOn = false;

			@Override
			public boolean isTorchOn()
			{
				return torchOn;
			}

			@Override
			public void switchTorch()
			{
				System.out.print("Switch Torch to => " + (torchOn ? "on" : "off"));
				torchOn = !torchOn;
			}

			@Override
			public void switchToGpsMeasure()
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void switchtoGpsDefault()
			{
				// TODO Auto-generated method stub

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
				if (GlobalCore.ifCacheSelected())
				{
					// speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
					Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
					Config.AcceptChanges();
					log.debug("LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
				}
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
					java.net.URI uri = null;
					if (url.startsWith("file://"))
					{
						File f = new File(url.replace("file://", ""));
						uri = f.toURI();
					}
					else
					{
						uri = new java.net.URI(url);
					}

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
		// final simulateForm sim = new simulateForm("Simulate Form");
		// sim.setSize(400, 130);
		// sim.setVisible(true);

		JFrame f;
		try
		{
			f = SimulatorMain.createFrame();
			f.pack();
			f.setResizable(false);
			f.setVisible(true);

			// SimulatorMain.startListener();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Initialisiert die Config für die Tests! initialisiert wird die Config mit der unter Testdata abgelegten config.db3
	 */
	public static void InitalConfig()
	{
		String base = new File("").getAbsolutePath();
		String workPath = base + "/cachebox";

		new Config(workPath);

		if (Config.settings != null && Config.settings.isLoaded()) return;

		// Read Config

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
		double latitude = Config.MapInitLatitude.getValue();
		double longitude = Config.MapInitLongitude.getValue();
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
		CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
		Config.ImperialUnits.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
			}
		});

		// GPS update time?
		CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
		Config.gpsUpdateTime.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
			}
		});

		// Use magnetic Compass?
		CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
		Config.HardwareCompass.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
			}
		});

		// Magnetic compass level
		CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
		Config.HardwareCompassLevel.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
			}
		});
	}

}
