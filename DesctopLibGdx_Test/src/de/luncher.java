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
		Gui screen = new Gui("Device Luncher");
		screen.setSize(250, 500);
		screen.setVisible(true);
	}
} // class Ex_1

class Gui extends Frame implements ActionListener, WindowListener {
	// constructor
	public Gui(String s) {
		super(s);
		setBackground(Color.LIGHT_GRAY);
		setLayout(new FlowLayout());
		addWindowListener(this); // listen for events on this Window

		Button pushButton = new Button("Phone 480x800 HDPI");
		add(pushButton);
		pushButton.addActionListener(this); // listen for Button press

		Button pushButton2 = new Button("Tab 1280x752 MDPI");
		add(pushButton2);
		pushButton2.addActionListener(this); // listen for Button press

	}

	// define action for Button press
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("Phone 480x800 HDPI")) {
			DesktopMain.test(iniPhone());
		} else if (event.getActionCommand().equals("Tab 1280x752 MDPI")) {
			DesktopMain.test(iniTab());
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
		devicesSizes ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.5f;
		ui.ButtonSize = new Size(55,55);
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

	private static devicesSizes iniTab() {

		Size myInitialSize = new Size(1280, 752);
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

}
