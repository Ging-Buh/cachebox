package de;

//program to demonstrate the construction of a Container and a Button
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import CB_Core.Math.Size;
import CB_Core.Math.devicesSizes;

class Ex_1 {
	public static void main(String[] args) {
	final Gui screen = new Gui("Device Launcher");
		screen.setSize(250, 500);
		screen.setVisible(true);
	}
} // class Ex_1

class Gui extends Frame implements ActionListener, WindowListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Checkbox debugChkBox;
	Checkbox scissorChkBox;
	Checkbox simulateChkBox;
	
	
	// constructor
	public Gui(String s) {
		super(s);
		setBackground(Color.LIGHT_GRAY);
		setLayout(new FlowLayout());
		addWindowListener(this); // listen for events on this Window

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
		
		 debugChkBox = new Checkbox("Enable Debug on Main", null, false);
		 scissorChkBox = new Checkbox("Disable scissor on Main", null, false);
		 simulateChkBox= new Checkbox("Simulate GPS from simulation.gpx", null, false);
		 
		 add(debugChkBox);
		 add(scissorChkBox);
		 add(simulateChkBox);

	}

	// define action for Button press
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("Phone 480x800 HDPI")) {
			DesktopMain.start(iniPhone(),debugChkBox.getState(),scissorChkBox.getState(),simulateChkBox.getState(),this);
		} else if (event.getActionCommand().equals("Tab 1280x752 MDPI")) {
			DesktopMain.start(iniTab(),debugChkBox.getState(),scissorChkBox.getState(),simulateChkBox.getState(),this);
		} else if (event.getActionCommand().equals("Tab 1024x768 MDPI")) {
			DesktopMain.start(iniPad10(),debugChkBox.getState(),scissorChkBox.getState(),simulateChkBox.getState(),this);
		}else if (event.getActionCommand().equals("Phone 240x400 LDPI")) {
			DesktopMain.start(iniLowPhone(),debugChkBox.getState(),scissorChkBox.getState(),simulateChkBox.getState(),this);
		}else if (event.getActionCommand().equals("Phone 720x1280 XHDPI")) {
			DesktopMain.start(iniHighPhone(),debugChkBox.getState(),scissorChkBox.getState(),simulateChkBox.getState(),this);
		}
//		System.exit(0);
	}

	// define methods in WindowListener interface
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}

	public void windowClosed(WindowEvent event) {
	} // do nothing for now

	public void windowDeiconified(WindowEvent event) {
	}

	public void windowIconified(WindowEvent event) {
	}

	public void windowActivated(WindowEvent event) {
	}

	public void windowDeactivated(WindowEvent event) {
	}

	public void windowOpened(WindowEvent event) {
	}

	private static devicesSizes iniPhone() {
		Size myInitialSize = new Size(480, 772);
		devicesSizes ui = getHDPI(myInitialSize);

		return ui;

	}

	private static devicesSizes iniTab() {

		Size myInitialSize = new Size(1280, 752);
		devicesSizes ui = getMDPI(myInitialSize);

		return ui;

	}

	private static devicesSizes iniPad10() {

		Size myInitialSize = new Size(1024, 768);
		devicesSizes ui = getMDPI(myInitialSize);

		return ui;

	}
	
	private static devicesSizes iniLowPhone() {

		Size myInitialSize = new Size(240, 400);
		devicesSizes ui = getLDPI(myInitialSize);

		return ui;

	}
	
	private static devicesSizes iniHighPhone() {

		Size myInitialSize = new Size(720, 1280);
		devicesSizes ui = getXHDPI(myInitialSize);

		return ui;

	}

	
	private static devicesSizes getLDPI(Size myInitialSize) {
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 0.75f;
		ui.ButtonSize = new Size(53, 53);
		ui.RefSize = 44;
		ui.TextSize_Normal = 36;
		ui.ButtonTextSize = 27;
		ui.IconSize = 11;
		ui.Margin = 3;
		ui.ArrowSizeList = 26;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 20;
		ui.isLandscape = false;
		return ui;
	}
	
	private static devicesSizes getMDPI(Size myInitialSize) {
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.0f;
		ui.ButtonSize = new Size(53, 53);
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
	
	private static devicesSizes getHDPI(Size myInitialSize) {
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.5f;
		ui.ButtonSize = new Size(65,65);
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
	
	private static devicesSizes getXHDPI(Size myInitialSize) {
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 2f;
		ui.ButtonSize = new Size(53,53);
		ui.RefSize = 74;
		ui.TextSize_Normal = 62;
		ui.ButtonTextSize = 60;
		ui.IconSize = 12;
		ui.Margin = 3;
		ui.ArrowSizeList = 11;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 4;
		ui.isLandscape = false;
		return ui;
	}
}
