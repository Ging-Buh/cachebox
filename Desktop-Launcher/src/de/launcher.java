/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de;

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

import org.mapsforge.map.model.DisplayModel;
import org.slf4j.LoggerFactory;

import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI_Base.Math.DevicesSizes;
import CB_UI_Base.Math.Size;
import CB_Utils.Log.CB_SLF4J;
import CB_Utils.Log.LogLevel;
import CB_Utils.Util.IChanged;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.fileProvider.FilenameFilter;
import de.CB_Utils.fileProvider.DesktopFileFactory;

class Ex_1 {

	final static org.slf4j.Logger log = LoggerFactory.getLogger(Ex_1.class);
	public static final String br = System.getProperty("line.separator");

	public static void main(String[] args) {

		DisplayModel.setDeviceScaleFactor(1.2f);

		new DesktopFileFactory();

		DesktopMain.InitalConfig();

		Config.settings.ReadFromDB();
		new CB_SLF4J(Config.mWorkPath);
		CB_SLF4J.setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());
		Config.AktLogLevel.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				CB_SLF4J.setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());
			}
		});

		File Dir = FileFactory.createFile("./");
		final String[] files;

		files = Dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains("src"))
					return true;
				if (filename.contains("DCB") && filename.endsWith("jar"))
					return true;
				return false;
			}
		});

		if (files.length > 0 && Config.installRev.getValue() < GlobalCore.CurrentRevision) {
			Config.installRev.setValue(GlobalCore.CurrentRevision);
			Config.newInstall.setValue(true);
			Config.AcceptChanges();
		} else {
			Config.newInstall.setValue(false);
			Config.AcceptChanges();
		}

		if (files.length > 0 && !files[0].contains("src")) {
			File workJar = FileFactory.createFile(files[0]);
			if (workJar.exists()) {
				// don't show Launcher
				final Gui screen = new Gui("Device Launcher");
				screen.setSize(250, 500);
				screen.setVisible(true);
				// DesktopMain.start(Gui.iniPhone(), false, false, true, screen);
			}
		} else {
			final Gui screen = new Gui("Device Launcher");
			screen.setSize(250, 500);
			screen.setVisible(true);
		}

	}

} // class Ex_1

class Gui extends Frame implements ActionListener, WindowListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static Checkbox debugChkBox;
	Checkbox scissorChkBox;
	Checkbox simulateChkBox;

	// constructor
	public Gui(String s) {
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

		Button pushButton1 = new Button("Phone on MAC HDPI");
		add(pushButton1);
		pushButton1.addActionListener(this); // listen for Button press

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
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("Phone 480x800 HDPI")) {
			DesktopMain.start(iniPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Tab 1280x752 MDPI")) {
			DesktopMain.start(iniTab(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Tab 1024x768 MDPI")) {
			DesktopMain.start(iniPad10(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Phone 240x400 LDPI")) {
			DesktopMain.start(iniLowPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Phone 720x1280 XHDPI")) {
			DesktopMain.start(iniHighPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Desctop Full")) {
			DesktopMain.start(iniDesktop(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Tab Nexus7")) {
			DesktopMain.start(iniNexus7(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		} else if (event.getActionCommand().equals("Phone on MAC HDPI")) {
			DesktopMain.start(iniMacEmulator(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
		}
	}

	// define methods in WindowListener interface
	@Override
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent event) {
	} // do nothing for now

	@Override
	public void windowDeiconified(WindowEvent event) {
	}

	@Override
	public void windowIconified(WindowEvent event) {
	}

	@Override
	public void windowActivated(WindowEvent event) {
	}

	@Override
	public void windowDeactivated(WindowEvent event) {
	}

	@Override
	public void windowOpened(WindowEvent event) {
	}

	public static DevicesSizes iniDesktop() {

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		Size myInitialSize = new Size(dim.width, dim.height);
		DevicesSizes ui = getLDPI(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniPhone() {
		Size myInitialSize = new Size(480, 772);
		DevicesSizes ui = getXHDPI(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniTab() {

		Size myInitialSize = new Size(1280, 752);
		DevicesSizes ui = getMDPI(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniPad10() {

		Size myInitialSize = new Size(1024, 768);
		DevicesSizes ui = getMDPI(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniLowPhone() {

		Size myInitialSize = new Size(240, 381);
		DevicesSizes ui = getLDPI(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniHighPhone() {

		Size myInitialSize = new Size(720, 1230);
		DevicesSizes ui = getXHDPI(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniNexus7() {

		Size myInitialSize = new Size(1280, 703);
		DevicesSizes ui = getNexus7(myInitialSize);

		return ui;

	}

	public static DevicesSizes iniMacEmulator() {

		Size myInitialSize = new Size(420, 700);
		DevicesSizes ui = getMac(myInitialSize);

		return ui;

	}

	public static DevicesSizes getMac(Size myInitialSize) {
		DevicesSizes ui = new DevicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.5f;

		ui.isLandscape = false;
		return ui;
	}

	public static DevicesSizes getLDPI(Size myInitialSize) {
		DevicesSizes ui = new DevicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 0.75f;

		ui.isLandscape = false;
		return ui;
	}

	public static DevicesSizes getMDPI(Size myInitialSize) {
		DevicesSizes ui = new DevicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.0f;

		ui.isLandscape = false;
		return ui;
	}

	public static DevicesSizes getHDPI(Size myInitialSize) {
		DevicesSizes ui = new DevicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.5f;

		ui.isLandscape = false;
		return ui;
	}

	public static DevicesSizes getXHDPI(Size myInitialSize) {
		DevicesSizes ui = new DevicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 2f;

		ui.isLandscape = false;
		return ui;
	}

	public static DevicesSizes getNexus7(Size myInitialSize) {
		DevicesSizes ui = new DevicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.3312501f;

		ui.isLandscape = true;
		return ui;
	}

}
