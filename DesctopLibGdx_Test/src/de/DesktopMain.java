package de;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.IHardwarStateListner;
import CB_Core.Events.platformConector.IgetFileListner;
import CB_Core.Events.platformConector.IgetFileReturnListner;
import CB_Core.Events.platformConector.IgetFolderListner;
import CB_Core.Events.platformConector.IgetFolderReturnListner;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Locator.GpsStatus;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import de.Map.DesctopManager;

public class DesktopMain
{

	static GL_Listener CB_UI;

	public static void start(devicesSizes ui, boolean debug, boolean scissor, final boolean simulate, final Frame frame)
	{
		GlobalCore.platform = Plattform.Desktop;
		frame.setVisible(false);
		DesktopLogger iLogger = new DesktopLogger();

		InitalConfig();

		CB_UI = new Desktop_GL_Listner(ui.Window.width, ui.Window.height);

		GL_View_Base.debug = debug;
		GL_View_Base.disableScissor = scissor;

		new DesctopManager();

		int sw = ui.Window.height > ui.Window.width ? ui.Window.width : ui.Window.height;

		// chek if use small skin
		GlobalCore.useSmallSkin = sw < 360 ? true : false;

		sw /= ui.Density;

		// chek if tablet
		GlobalCore.isTab = sw > 400 ? true : false;

		UiSizes.initial(ui);
		new LwjglApplication(CB_UI, "Game", ui.Window.width, ui.Window.height, false);

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
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean isGPSon()
			{
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public void vibrate()
			{
				// TODO Auto-generated method stub

			}

			@Override
			public GpsStatus getGpsStatus()
			{
				// TODO Auto-generated method stub
				return null;
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

				// FileDialog filedia = new FileDialog(frame, ButtonText);
				// filedia.setDirectory(initialPath);
				// filedia.setFile(extension);
				// filedia.setTitle(TitleText);
				// filedia.show();
				// String filename = filedia.getDirectory() + filedia.getFile();
				// if (filename != null)
				// {
				// if (returnListner != null) returnListner.getFieleReturn(filename);
				// }
				// filedia.dispose();

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

		DesktopClipboard dcb = new DesktopClipboard();

		if (dcb != null) GlobalCore.setDefaultClipboard(dcb);

	}

	private static void Run(boolean simulate)
	{
		CB_UI.onStart();
		// // CB_UI.setGLViewID(ViewConst.MAP_CONTROL_TEST_VIEW);
		// CB_UI.setGLViewID(ViewConst.TEST_VIEW);
		// // CB_UI.setGLViewID(ViewConst.CREDITS_VIEW);
		// // CB_UI.setGLViewID(ViewConst.GL_MAP_VIEW);
		// // CB_UI.setGLViewID(ViewConst.ViewConst.ABOUT_VIEW);
		// CB_UI.setGLViewID(ViewConst.TEST_LIST_VIEW);

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
		String workPath = "./testdata";

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

		try
		{
			Database.Data = new TestDB(DatabaseType.CacheBox);
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			Database.FieldNotes = new TestDB(DatabaseType.FieldNotes);
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

		try
		{
			GlobalCore.Translations.ReadTranslationsFile(Config.settings.Sel_LanguagePath.getValue());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
