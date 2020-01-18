package de;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.PlatformUIBase.IgetFileReturnListener;
import de.droidcachebox.PlatformUIBase.IgetFolderReturnListener;
import de.droidcachebox.PlatformUIBase.Methods;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Database.DatabaseType;
import de.droidcachebox.database.DesktopDB;
import de.droidcachebox.database.SQLiteClass;
import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Listener_Interface;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.texturepacker.DesktopTexturePacker;
import de.droidcachebox.gdx.utils.DesktopClipboard;
import de.droidcachebox.locator.DesktopLocatorBaseMethods;
import de.droidcachebox.locator.Location;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.LocatorBasePlatFormMethods;
import de.droidcachebox.menu.MainViewInit;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.settings.SettingInt;
import de.droidcachebox.settings.SettingString;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.log.Log;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

//import ch.fhnw.imvs.gpssimulator.SimulatorMain;

public class DesktopMain {
    private static final String log = "DesktopMain";
    static float compassheading = -1;
    // Retrieve the user preference node for the package com.mycompany
    static Preferences prefs = Preferences.userNodeForPackage(de.DesktopMain.class);
    private static GL CB_UI;
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static void start(DevicesSizes ui, boolean debug, boolean scissor, final boolean simulate, final Frame frame) {

        if (isWindows()) {
            Plattform.used = Plattform.DesktopWin;
        } else if (isMac()) {
            Plattform.used = Plattform.DesktopMac;
        } else if (isUnix()) {
            Plattform.used = Plattform.DesktopLinux;
        }


        frame.setVisible(false);

        // Initial Desctop TexturePacker
        new DesktopTexturePacker();

        // has been done by launcher
        // InitialConfig();
        // Config.settings.ReadFromDB();

        CB_RectF rec = new CB_RectF(0, 0, ui.Window.width, ui.Window.height);
        CB_UI = new GL(ui.Window.width, ui.Window.height, new MainViewInit(rec), new ViewManager(rec));

        GL_View_Base.debug = debug;
        GL_View_Base.disableScissor = scissor;

        if (Config.installedRev.getValue() < GlobalCore.getInstance().getCurrentRevision()) {

            Config.installedRev.setValue(GlobalCore.getInstance().getCurrentRevision());
            Config.newInstall.setValue(true);
            Config.AcceptChanges();
        } else {
            Config.newInstall.setValue(false);
            Config.AcceptChanges();
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

            GL.that.setGL_Listener_Interface(new GL_Listener_Interface() {

                AtomicBoolean isContinousRenderMode = new AtomicBoolean(true);

                @Override
                public void RequestRender() {
                    App.getGraphics().requestRendering();

                }

                @Override
                public void RenderDirty() {
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

        PlatformUIBase.setMethods(new Methods() {

            private boolean torchOn = false;

            @Override
            public void writePlatformSetting(SettingBase<?> setting) {

                if (setting instanceof SettingBool) {
                    prefs.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
                } else if (setting instanceof SettingString) {
                    prefs.put(setting.getName(), ((SettingString) setting).getValue());
                } else if (setting instanceof SettingInt) {
                    prefs.putInt(setting.getName(), ((SettingInt) setting).getValue());
                }

                // Commit the edits!
                try {
                    prefs.flush();
                } catch (BackingStoreException e) {

                    e.printStackTrace();
                }

            }

            @Override
            public SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
                if (setting instanceof SettingString) {
                    String value = prefs.get(setting.getName(), ((SettingString) setting).getDefaultValue());
                    ((SettingString) setting).setValue(value);
                } else if (setting instanceof SettingBool) {
                    boolean value = prefs.getBoolean(setting.getName(), ((SettingBool) setting).getDefaultValue());
                    ((SettingBool) setting).setValue(value);
                } else if (setting instanceof SettingInt) {
                    int value = prefs.getInt(setting.getName(), ((SettingInt) setting).getDefaultValue());
                    ((SettingInt) setting).setValue(value);
                }
                setting.clearDirty();
                return setting;
            }

            @Override
            public boolean isOnline() {
                return true;
            }

            @Override
            public boolean isGPSon() {
                return true;
            }

            @Override
            public void vibrate() {

            }

            @Override
            public boolean isTorchAvailable() {
                return true; // Simulate
            }

            @Override
            public boolean isTorchOn() {
                return torchOn;
            }

            @Override
            public void switchTorch() {
                System.out.print("Switch Torch to => " + (torchOn ? "on" : "off"));
                torchOn = !torchOn;
            }

            @Override
            public void switchToGpsMeasure() {
            }

            @Override
            public void switchtoGpsDefault() {
            }

            @Override
            public void callUrl(String url) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {

                    System.err.println("Desktop doesn't support the browse action (fatal)");
                    System.exit(1);
                }

                try {
                    java.net.URI uri = null;
                    if (url.startsWith("file://")) {
                        File f = new File(url.replace("file://", ""));
                        uri = f.toURI();
                    } else {
                        uri = new java.net.URI(url);
                    }

                    desktop.browse(uri);

                } catch (Exception e) {

                    System.err.println(e.getMessage());
                }
            }

            @Override
            public void startPictureApp(String file) {
            }

            @Override
            public SQLiteInterface getSQLInstance() {
                return new SQLiteClass();
            }

            @Override
            public void freeSQLInstance(SQLiteInterface sqlInstance) {
                sqlInstance = null;
            }

            @Override
            public void getApiKey() {
                // Android : GetApiAuth();
                (new GcApiLogin()).RunRequest();
            }

            @Override
            public void getFile(String initialPath, final String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {

                final String ext = extension.replace("*", "");

                JFileChooser chooser = new JFileChooser();

                chooser.setCurrentDirectory(new java.io.File(initialPath));
                chooser.setDialogTitle(TitleText);

                FileFilter filter = new FileFilter() {

                    @Override
                    public String getDescription() {

                        return extension;
                    }

                    @Override
                    public boolean accept(File f) {
                        if (f.getAbsolutePath().endsWith(ext))
                            return true;
                        return false;
                    }
                };

                chooser.setFileFilter(filter);

                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (returnListener != null)
                        returnListener.returnFile(chooser.getSelectedFile().getAbsolutePath());
                    System.out.println("getFile:" + "You chose to open this file: " + chooser.getSelectedFile().getName());
                }

            }

            @Override
            public void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {

                JFileChooser chooser = new JFileChooser();

                chooser.setCurrentDirectory(new java.io.File(initialPath));
                chooser.setDialogTitle(TitleText);

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (returnListener != null)
                        returnListener.returnFolder(chooser.getSelectedFile().getAbsolutePath());
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
                }
            }

            @Override
            public void quit() {
                if (GlobalCore.isSetSelectedCache()) {
                    // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
                    Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
                    Config.AcceptChanges();
                    Log.debug(log, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGeoCacheCode());
                }
                System.exit(0);
            }

            @Override
            public void handleExternalRequest() {

            }

            @Override
            public String removeHtmlEntyties(String text) {
                // todo Jsoup.parse(s).text();
                return text.replaceAll("\\<[^>]*>","");
            }

        });
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
     * Initialisiert die Config für die Tests! initialisiert wird die Config mit der unter Testdata abgelegten config.db3
     */
    public static void InitialConfig() {
        File forWorkPathTest = new File("C:/Daten/_WCB");
        if (!forWorkPathTest.exists())
            forWorkPathTest = new File("");
        String workPath = forWorkPathTest.getAbsolutePath();
        new Config(workPath);
        if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
            return;
        Database.Settings = new DesktopDB(DatabaseType.Settings);
        Database.Settings.startUp(Config.mWorkPath + "/User/Config.db3");
        Database.Data = new DesktopDB(DatabaseType.CacheBox);
        Database.Drafts = new DesktopDB(DatabaseType.Drafts);
        Database.Drafts.startUp(Config.mWorkPath + "/User/FieldNotes.db3");
    }

    /**
     * Initial all Locator functions
     */
    private static void initialLocatorBase() {
        // ##########################################################
        // initial Locator with saved Location
        // ##########################################################
        double latitude = Config.MapInitLatitude.getValue();
        double longitude = Config.MapInitLongitude.getValue();
        Location.ProviderType provider = (latitude == -1000) ? Location.ProviderType.NULL : Location.ProviderType.Saved;

        Location initialLocation;

        if (provider == Location.ProviderType.Saved) {
            initialLocation = new Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
        } else {
            initialLocation = Location.NULL_LOCATION;
        }

        Locator.getInstance().setNewLocation(initialLocation);

        // ##########################################################
        // initial settings changed handling
        // ##########################################################

        // Use Imperial units?
        Locator.getInstance().setUseImperialUnits(Config.ImperialUnits.getValue());
        Config.ImperialUnits.addSettingChangedListener(() -> Locator.getInstance().setUseImperialUnits(Config.ImperialUnits.getValue()));

        // GPS update time?
        Locator.getInstance().setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
        Config.gpsUpdateTime.addSettingChangedListener(() -> Locator.getInstance().setMinUpdateTime((long) Config.gpsUpdateTime.getValue()));

        // Use magnetic Compass?
        Locator.getInstance().setUseHardwareCompass(Config.HardwareCompass.getValue());
        Config.HardwareCompass.addSettingChangedListener(() -> Locator.getInstance().setUseHardwareCompass(Config.HardwareCompass.getValue()));

        // Magnetic compass level
        Locator.getInstance().setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
        Config.HardwareCompassLevel.addSettingChangedListener(() -> Locator.getInstance().setHardwareCompassLevel(Config.HardwareCompassLevel.getValue()));
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
