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

import org.mapsforge.map.model.DisplayModel;

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

import de.droidcachebox.DesktopPlatformMethods;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.database.SettingsDatabase;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.DesktopFileFactory;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.log.CB_SLF4J;
import de.droidcachebox.utils.log.LogLevel;

class DCB {

    public static void main(String[] args) {

        DisplayModel.setDeviceScaleFactor(1f);
        Plattform.used = Plattform.DesktopWin;
        new DesktopFileFactory();
        File forWorkPathTest = new File("C:/Daten/_WCB");
        if (!forWorkPathTest.exists())
            forWorkPathTest = new File("");
        String workPath = forWorkPathTest.getAbsolutePath();
        if (!FileIO.createDirectory(GlobalCore.workPath + "/User"))
            return;
        // todo set firstSDCard and secondSDCard somehow
        GlobalCore.firstSDCard = "C:/";
        GlobalCore.secondSDCard = "D:/";
        GlobalCore.workPath = workPath;
        Platform.init(new DesktopPlatformMethods());
        SettingsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/Config.db3");
        DraftsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/FieldNotes.db3");

        Settings.getInstance().readFromDB();

        CB_SLF4J.getInstance(GlobalCore.workPath).setLogLevel((LogLevel) Settings.AktLogLevel.getEnumValue());
        Settings.AktLogLevel.addSettingChangedListener(() -> CB_SLF4J.getInstance(GlobalCore.workPath).setLogLevel((LogLevel) Settings.AktLogLevel.getEnumValue()));

        AbstractFile Dir = FileFactory.createFile("./");
        final String[] files;

        files = Dir.list((dir, filename) -> {
            if (filename.contains("src"))
                return true;
            return filename.contains("DCB") && filename.endsWith("jar");
        });

        if (files.length > 0 && Settings.installedRev.getValue() < GlobalCore.getInstance().getCurrentRevision()) {
            Settings.installedRev.setValue(GlobalCore.getInstance().getCurrentRevision());
            Settings.newInstall.setValue(true);
        } else {
            Settings.newInstall.setValue(false);
        }

        if (files.length > 0 && !files[0].contains("src")) {
            AbstractFile workJar = FileFactory.createFile(files[0]);
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

        Button pushButton6 = new Button("Desktop Full");
        // TODO Activate Full Screen=> add(pushButton6);
        pushButton6.addActionListener(this); // listen for Button press

        ((Button) add(new Button("Phone 480x800 HDPI"))).addActionListener(this);
        ((Button) add(new Button("Phone on MAC HDPI"))).addActionListener(this);
        ((Button) add(new Button("Phone 240x400 LDPI"))).addActionListener(this);
        ((Button) add(new Button("Phone 720x1280 XHDPI"))).addActionListener(this);
        ((Button) add(new Button("Tab 1280x752 MDPI"))).addActionListener(this);
        ((Button) add(new Button("Tab 1024x768 MDPI"))).addActionListener(this);
        ((Button) add(new Button("Tab Nexus7"))).addActionListener(this);

        debugChkBox = new Checkbox("Enable Debug on Main", null, false);
        scissorChkBox = new Checkbox("Disable scissor on Main", null, false);
        simulateChkBox = new Checkbox("Simulate GPS from simulation.gpx", null, false);

        add(debugChkBox);
        add(scissorChkBox);
        add(simulateChkBox);

    }

    public static DevicesSizes iniDesktop() {

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        Size myInitialSize = new Size(dim.width, dim.height);

        return getLDPI(myInitialSize);

    }

    public static DevicesSizes iniPhone() {
        Size myInitialSize = new Size(480, 772);
        return getXHDPI(myInitialSize);

    }

    public static DevicesSizes iniTab() {
        Size myInitialSize = new Size(1280, 752);
        return getMDPI(myInitialSize);

    }

    public static DevicesSizes iniPad10() {
        Size myInitialSize = new Size(1024, 768);
        return getMDPI(myInitialSize);

    }

    public static DevicesSizes iniLowPhone() {
        Size myInitialSize = new Size(240, 381);
        return getLDPI(myInitialSize);

    }

    public static DevicesSizes iniHighPhone() {
        Size myInitialSize = new Size(720, 1230);
        return getXHDPI(myInitialSize);
    }

    public static DevicesSizes iniNexus7() {
        Size myInitialSize = new Size(1280, 703);
        return getNexus7(myInitialSize);

    }

    public static DevicesSizes iniMacEmulator() {
        Size myInitialSize = new Size(420, 700);
        return getMac(myInitialSize);

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
        ui.Density = 1.3f;

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

    // define action for Button press
    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case "Phone 480x800 HDPI":
                DesktopMain.start(iniPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Tab 1280x752 MDPI":
                DesktopMain.start(iniTab(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Tab 1024x768 MDPI":
                DesktopMain.start(iniPad10(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Phone 240x400 LDPI":
                DesktopMain.start(iniLowPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Phone 720x1280 XHDPI":
                DesktopMain.start(iniHighPhone(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Desktop Full":
                DesktopMain.start(iniDesktop(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Tab Nexus7":
                DesktopMain.start(iniNexus7(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
            case "Phone on MAC HDPI":
                DesktopMain.start(iniMacEmulator(), debugChkBox.getState(), scissorChkBox.getState(), simulateChkBox.getState(), this);
                break;
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

}
