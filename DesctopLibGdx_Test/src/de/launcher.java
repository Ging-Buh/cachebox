package de;

//program to demonstrate the construction of a Container and a Button
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;

import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Math.Size;
import CB_UI.Math.devicesSizes;

class Ex_1
{
	public static void main(String[] args)
	{

		DesktopMain.InitalConfig();
		Config.settings.ReadFromDB();

		File Dir = new File("./");
		final String[] files;

		files = Dir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				if (filename.contains("src")) return true;
				if (filename.contains("DCB") && filename.endsWith("jar")) return true;
				return false;
			}
		});

		if (files.length > 0 && Config.installRev.getValue() < GlobalCore.CurrentRevision)
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

		if (files.length > 0 && !files[0].contains("src"))
		{
			File workJar = new File(files[0]);
			if (workJar.exists())
			{
				// don't show Launcher
				final Gui screen = new Gui("Device Launcher");
				screen.setSize(250, 500);
				screen.setVisible(true);
				DesktopMain.start(Gui.iniPhone(), false, false, true, screen);
			}
		}
		else
		{
			final Gui screen = new Gui("Device Launcher");
			screen.setSize(250, 500);
			screen.setVisible(true);
		}

	}
} // class Ex_1

class Gui extends Frame implements ActionListener, WindowListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Checkbox debugChkBox;
	Checkbox scissorChkBox;
	Checkbox simulateChkBox;

	// constructor
	public Gui(String s)
	{
		super(s);
		setBackground(Color.LIGHT_GRAY);
		setLayout(new FlowLayout());
		addWindowListener(this); // listen for events on this Window

		Button pushButton6 = new Button("Desctop Full");
		// TODO Activate Full Screen=> add(pushButton6);
		pushButton6.addActionListener(this); // listen for Button press

		Button pushButton = new Button("Phone 480x800 HDPI");
		add(pushButton);
		pushButton.addActionListener(this); // listen for Button press

		Button pushButton4 = new Button("Phone 240x400 LDPI");
		add(pushButton4);
		pushButton4.addActionListener(this); // listen for Button press

		Button pushButton5 = new Button("Phone 720x1280 XHDPI");
		add(pushButton5);
		pushButton5.addActionListener(this); // listen for Button press

		Button pushButton2 = new Button("Tab 1280x752 MDPI");
		add(pushButton2);
		pushButton2.addActionListener(this); // listen for Button press

		Button pushButton3 = new Button("Tab 1024x768 MDPI");
		add(pushButton3);
		pushButton3.addActionListener(this); // listen for Button press

		Button pushButto4 = new Button("Tab Nexus7");
		add(pushButto4);
		pushButto4.addActionListener(this); // listen for Button press

		debugChkBox = new Checkbox("Enable Debug on Main", null, false);
		scissorChkBox = new Checkbox("Disable scissor on Main", null, false);
		simulateChkBox = new Checkbox("Simulate GPS from simulation.gpx", null, false);

		add(debugChkBox);
		add(scissorChkBox);
		add(simulateChkBox);

	}

	// define action for Button press
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Phone 480x800 HDPI"))
		{
			DesktopMain.start(iniPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
		else if (event.getActionCommand().equals("Tab 1280x752 MDPI"))
		{
			DesktopMain.start(iniTab(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
		else if (event.getActionCommand().equals("Tab 1024x768 MDPI"))
		{
			DesktopMain.start(iniPad10(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
		else if (event.getActionCommand().equals("Phone 240x400 LDPI"))
		{
			DesktopMain.start(iniLowPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
		else if (event.getActionCommand().equals("Phone 720x1280 XHDPI"))
		{
			DesktopMain.start(iniHighPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
		else if (event.getActionCommand().equals("Desctop Full"))
		{
			DesktopMain.start(iniDesktop(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
		else if (event.getActionCommand().equals("Tab Nexus7"))
		{
			DesktopMain.start(iniNexus7(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
	}

	// define methods in WindowListener interface
	public void windowClosing(WindowEvent event)
	{
		System.exit(0);
	}

	public void windowClosed(WindowEvent event)
	{
	} // do nothing for now

	public void windowDeiconified(WindowEvent event)
	{
	}

	public void windowIconified(WindowEvent event)
	{
	}

	public void windowActivated(WindowEvent event)
	{
	}

	public void windowDeactivated(WindowEvent event)
	{
	}

	public void windowOpened(WindowEvent event)
	{
	}

	public static devicesSizes iniDesktop()
	{

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		Size myInitialSize = new Size(dim.width, dim.height);
		devicesSizes ui = getLDPI(myInitialSize);

		return ui;

	}

	public static devicesSizes iniPhone()
	{
		Size myInitialSize = new Size(480, 772);
		devicesSizes ui = getHDPI(myInitialSize);

		return ui;

	}

	public static devicesSizes iniTab()
	{

		Size myInitialSize = new Size(1280, 752);
		devicesSizes ui = getMDPI(myInitialSize);

		return ui;

	}

	public static devicesSizes iniPad10()
	{

		Size myInitialSize = new Size(1024, 768);
		devicesSizes ui = getMDPI(myInitialSize);

		return ui;

	}

	public static devicesSizes iniLowPhone()
	{

		Size myInitialSize = new Size(240, 381);
		devicesSizes ui = getLDPI(myInitialSize);

		return ui;

	}

	public static devicesSizes iniHighPhone()
	{

		Size myInitialSize = new Size(720, 1230);
		devicesSizes ui = getXHDPI(myInitialSize);

		return ui;

	}

	public static devicesSizes iniNexus7()
	{

		Size myInitialSize = new Size(1280, 703);
		devicesSizes ui = getNexus7(myInitialSize);

		return ui;

	}

	public static devicesSizes getLDPI(Size myInitialSize)
	{
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 0.75f;
		ui.RefSize = 33;
		ui.TextSize_Normal = 27;
		ui.ButtonTextSize = 20;
		ui.IconSize = 8;
		ui.Margin = 4;
		ui.ArrowSizeList = 20;
		ui.ArrowSizeMap = 14;
		ui.TB_IconSize = 15;
		ui.isLandscape = false;
		return ui;
	}

	public static devicesSizes getMDPI(Size myInitialSize)
	{
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.0f;
		ui.RefSize = 54;
		ui.TextSize_Normal = 52;
		ui.ButtonTextSize = 50;
		ui.IconSize = 13;
		ui.Margin = 3;
		ui.ArrowSizeList = 20;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 12;
		ui.isLandscape = false;
		return ui;
	}

	public static devicesSizes getHDPI(Size myInitialSize)
	{
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.5f;
		ui.RefSize = 64;
		ui.TextSize_Normal = 52;
		ui.ButtonTextSize = 50;
		ui.IconSize = 13;
		ui.Margin = 4;
		ui.ArrowSizeList = 11;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 8;
		ui.isLandscape = false;
		return ui;
	}

	public static devicesSizes getXHDPI(Size myInitialSize)
	{
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 2f;
		ui.RefSize = 148;
		ui.TextSize_Normal = 124;
		ui.ButtonTextSize = 120;
		ui.IconSize = 24;
		ui.Margin = 6;
		ui.ArrowSizeList = 22;
		ui.ArrowSizeMap = 36;
		ui.TB_IconSize = 8;
		ui.isLandscape = false;
		return ui;
	}

	public static devicesSizes getNexus7(Size myInitialSize)
	{
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.3312501f;
		ui.RefSize = 85;
		ui.TextSize_Normal = 69;
		ui.ButtonTextSize = 67;
		ui.IconSize = 17;
		ui.Margin = 5;
		ui.ArrowSizeList = 15;
		ui.ArrowSizeMap = 24;
		ui.TB_IconSize = 11;
		ui.isLandscape = true;
		return ui;
	}
}
