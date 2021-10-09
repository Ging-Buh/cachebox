package de;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import java.awt.Frame;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Listener_Interface;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.texturepacker.DesktopTexturePacker;
import de.droidcachebox.gdx.utils.DesktopClipboard;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.DesktopLocatorBaseMethods;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.LocatorBasePlatFormMethods;
import de.droidcachebox.menu.MainViewInit;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.Plattform;

//import ch.fhnw.imvs.gpssimulator.SimulatorMain;

public class DesktopMain {
    private static final String sClass = "DesktopMain";
    static float compassheading = -1;
    // Retrieve the user preference node for the package com.mycompany
    private static GL CB_UI;
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void start(DevicesSizes ui, boolean debug, boolean scissor, final boolean simulate, final Frame frame) {

        if (isWindows()) {
            Plattform.used = Plattform.DesktopWin;
        } else if (isMac()) {
            Plattform.used = Plattform.DesktopMac;
        } else if (isUnix()) {
            Plattform.used = Plattform.DesktopLinux;
        }


        frame.setVisible(false);

        // Initial Desktop TexturePacker
        new DesktopTexturePacker();

        CB_RectF rec = new CB_RectF(0, 0, ui.Window.width, ui.Window.height);
        CB_UI = new GL(ui.Window.width, ui.Window.height, new MainViewInit(rec), new ViewManager(rec));

        GL_View_Base.debug = debug;
        GL_View_Base.disableScissor = scissor;

        if (Settings.installedRev.getValue() < GlobalCore.getInstance().getCurrentRevision()) {

            Settings.installedRev.setValue(GlobalCore.getInstance().getCurrentRevision());
            Settings.newInstall.setValue(true);
            ViewManager.that.acceptChanges();
        } else {
            Settings.newInstall.setValue(false);
            ViewManager.that.acceptChanges();
        }

        int sw = ui.Window.height > ui.Window.width ? ui.Window.width : ui.Window.height;

        // chek if use small skin
        if (sw < 360) GlobalCore.displayType = DisplayType.Small;
        else GlobalCore.displayType = DisplayType.Normal;

        sw /= ui.Density;

        // TODO Activate Full Screen
        if (false) {
            LwjglApplicationConfiguration lwjglAppCfg = new LwjglApplicationConfiguration();
            DisplayMode dispMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
            lwjglAppCfg.setFromDisplayMode(dispMode);
            lwjglAppCfg.fullscreen = true;

            new LwjglApplication(CB_UI, lwjglAppCfg);
        } else {

            LwjglApplicationConfiguration lwjglAppCfg = new LwjglApplicationConfiguration();
            DisplayMode dispMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

            lwjglAppCfg.setFromDisplayMode(dispMode);
            lwjglAppCfg.fullscreen = false;
            lwjglAppCfg.resizable = false;
            lwjglAppCfg.width = ui.Window.width;
            lwjglAppCfg.height = ui.Window.height;
            lwjglAppCfg.title = "DCB Desctop Cachebox";
            lwjglAppCfg.samples = 3;

            final LwjglApplication App = new LwjglApplication(CB_UI, lwjglAppCfg);
            App.getGraphics().setContinuousRendering(false);

            GL.that.setGlListener(new GL_Listener_Interface() {

                final AtomicBoolean isContinousRenderMode = new AtomicBoolean(true);

                @Override
                public void requestRender() {
                    App.getGraphics().requestRendering();

                }

                @Override
                public void renderDirty() {
                    App.getGraphics().setContinuousRendering(false);
                    isContinousRenderMode.set(false);
                }

                @Override
                public void renderContinous() {
                    App.getGraphics().setContinuousRendering(true);
                    isContinousRenderMode.set(true);
                }

                @Override
                public boolean isContinous() {
                    return isContinousRenderMode.get();
                }

            });
        }

        UiSizes.getInstance().initialize(ui);
        initialLocatorBase();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Run(simulate);
            }
        };
        timer.schedule(task, 600);
        PlatformUIBase.setClipboard(new DesktopClipboard());
        LocatorBasePlatFormMethods.setMethods(new DesktopLocatorBaseMethods());

    }

    // ################## simulation#################

    private static void Run(boolean simulate) {
        CB_UI.onStart();

        // Gdx.input.setInputProcessor(CB_UI);
/*
        if (simulate) {
            showSimmulateForm();
        }
 */
    }

    /*
    private static void showSimmulateForm() {
        // final simulateForm sim = new simulateForm("Simulate Form");
        // sim.setSize(400, 130);
        // sim.setVisible(true);

        JFrame f;
        try {
            f = SimulatorMain.createFrame();
            f.pack();
            f.setResizable(false);
            f.setVisible(true);

            // SimulatorMain.startListener();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

     */

    /**
     * Initial all Locator functions
     */
    private static void initialLocatorBase() {
        // ##########################################################
        // initial Locator with saved Location
        // ##########################################################
        double latitude = Settings.mapInitLatitude.getValue();
        double longitude = Settings.mapInitLongitude.getValue();
        CBLocation.ProviderType provider = (latitude == -1000) ? CBLocation.ProviderType.NULL : CBLocation.ProviderType.Saved;

        CBLocation initialLocation;

        if (provider == CBLocation.ProviderType.Saved) {
            initialLocation = new CBLocation(latitude, longitude, 0, false, 0, false, 0, 0, provider);
        } else {
            initialLocation = CBLocation.NULL_LOCATION;
        }

        Locator.getInstance().setNewLocation(initialLocation);

        // ##########################################################
        // initial settings changed handling
        // ##########################################################

        // Use Imperial units?
        Locator.getInstance().setUseImperialUnits(Settings.ImperialUnits.getValue());
        Settings.ImperialUnits.addSettingChangedListener(() -> Locator.getInstance().setUseImperialUnits(Settings.ImperialUnits.getValue()));

        // GPS update time?
        Locator.getInstance().setMinUpdateTime((long) Settings.gpsUpdateTime.getValue());
        Settings.gpsUpdateTime.addSettingChangedListener(() -> Locator.getInstance().setMinUpdateTime((long) Settings.gpsUpdateTime.getValue()));

        // Use magnetic Compass?
        Locator.getInstance().setUseHardwareCompass(Settings.HardwareCompass.getValue());
        Settings.HardwareCompass.addSettingChangedListener(() -> Locator.getInstance().setUseHardwareCompass(Settings.HardwareCompass.getValue()));

        // Magnetic compass level
        Locator.getInstance().setHardwareCompassLevel(Settings.HardwareCompassLevel.getValue());
        Settings.HardwareCompassLevel.addSettingChangedListener(() -> Locator.getInstance().setHardwareCompassLevel(Settings.HardwareCompassLevel.getValue()));
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }
}
