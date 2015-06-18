package de.CB.TestBase;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.mapsforge.map.model.DisplayModel;
import org.slf4j.LoggerFactory;

import CB_Locator.Location.ProviderType;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.Events.platformConector.ICallUrl;
import CB_UI_Base.Events.platformConector.IHardwarStateListner;
import CB_UI_Base.Events.platformConector.IQuit;
import CB_UI_Base.Events.platformConector.IgetFileListner;
import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.Events.platformConector.IgetFolderListner;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Listener_Interface;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.devicesSizes;
import CB_Utils.Config_Core;
import CB_Utils.Plattform;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.CB.TestBase.Map.DesktopManager;
import de.CB.TestBase.Views.MainView;
import de.CB.TestBase.Views.splash;

public class Main
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args)
	{
		Plattform.used = Plattform.Desktop;

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Graphics-Test";
		cfg.width = 1120; // Tablet 1200* 720 Desctop 720* 950
		cfg.height = 720;
		cfg.samples = 16;
		// Read Config
		String workPath = "./freizeitkarte";
		// Initial Config
		new Config(workPath);

		Config_Core.WorkPath = workPath;
		Config.Initialize(workPath, workPath + "/freizeitkarte.config");

		Settings.SkinFolder.setValue("default");

		devicesSizes ui = new devicesSizes();

		float DENSITY = 1f;

		ui.Window = new Size(cfg.width, cfg.height);
		ui.Density = 0.8f;
			ui.isLandscape = false;
		DisplayModel.setDeviceScaleFactor(DENSITY);
		new UiSizes();
		UiSizes.that.initial(ui);

		initialLocatorBase();

		DisplayModel model = new DisplayModel();

		new DesktopManager(model);

		// create new splash
		splash sp = new splash(0, 0, cfg.width, cfg.height, "Splash");

		// create new mainView
		MainView ma = new MainView(0, 0, cfg.width, cfg.height, "mainView");

		final Ex Game = new Ex(cfg.width, cfg.height, sp, ma);

		final LwjglApplication App = new LwjglApplication(Game, cfg);
		App.getGraphics().setContinuousRendering(false);

		GL.listenerInterface = new GL_Listener_Interface()
		{

			AtomicBoolean mIsContinous = new AtomicBoolean(true);

			@Override
			public void RequestRender()
			{
				App.getGraphics().requestRendering();
			}

			@Override
			public void RenderDirty()
			{
				mIsContinous.set(false);
				App.getGraphics().setContinuousRendering(false);
			}

			@Override
			public void RenderContinous()
			{
				mIsContinous.set(true);
				App.getGraphics().setContinuousRendering(true);
			}

			@Override
			public boolean isContinous()
			{
				return mIsContinous.get();
			}
		};

		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				Game.onStart();
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
			public boolean isTorchAvailable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isTorchOn() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void switchTorch() {
			
			}

			@Override
			public void switchToGpsMeasure() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void switchtoGpsDefault() {
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
				System.exit(0);

			}
		});

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
