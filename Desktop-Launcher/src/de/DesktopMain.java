package de;

import CB_Core.Database;
import CB_Core.Database.DatabaseType;
import CB_Locator.Location.ProviderType;
import CB_UI.Config;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MainViewInit;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.*;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Listener_Interface;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.DevicesSizes;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Log;
import CB_Utils.Plattform;
import CB_Utils.Settings.*;
import CB_Utils.Settings.PlatformSettings.IPlatformSettings;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.CB_Texturepacker.Desktop_Packer;
import de.Map.DesktopManager;
import de.cb.sqlite.DesktopDB;
import org.mapsforge.map.model.DisplayModel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class DesktopMain {
    private static final String log = "DesktopMain";
    private static GL CB_UI;
    static float compassheading = -1;
    // Retrieve the user preference node for the package com.mycompany
    static Preferences prefs = Preferences.userNodeForPackage(de.DesktopMain.class);

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
        new Desktop_Packer();

        PlatformSettings.setPlatformSettings(new IPlatformSettings() {

            @Override
            public void Write(SettingBase<?> setting) {

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
            public SettingBase<?> Read(SettingBase<?> setting) {
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
        });

        InitalConfig();
        Config.settings.ReadFromDB();

        CB_RectF rec = new CB_RectF(0, 0, ui.Window.width, ui.Window.height);
        CB_UI = new GL(ui.Window.width, ui.Window.height, new MainViewInit(rec), new TabMainView(rec));

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

        DisplayModel model = new DisplayModel();
        new DesktopManager(model);

        int sw = ui.Window.height > ui.Window.width ? ui.Window.width : ui.Window.height;

        // chek if use small skin
        GlobalCore.useSmallSkin = sw < 360 ? true : false;

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
                public void RenderContinous() {
                    App.getGraphics().setContinuousRendering(true);
                    isContinousRenderMode.set(true);
                }

                @Override
                public boolean isContinous() {
                    return isContinousRenderMode.get();
                }

            });
        }

        new UiSizes();
        UiSizes.that.initial(ui);
        initialLocatorBase();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Run(simulate);
            }
        };
        timer.schedule(task, 600);

        // ''''''''''''''''''''''
        PlatformConnector.setisOnlineListener(new IHardwarStateListener() {

            private boolean torchOn = false;

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

        });

        PlatformConnector.setGetFileListener(new IgetFileListener() {
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
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
                }

            }
        });

        PlatformConnector.setGetFolderListener(new IgetFolderListener() {

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
        });

        PlatformConnector.setQuitListener(new IQuit() {

            @Override
            public void Quit() {
                if (GlobalCore.isSetSelectedCache()) {
                    // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
                    Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
                    Config.AcceptChanges();
                    Log.debug(log, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
                }
                System.exit(0);

            }
        });

        DesktopClipboard dcb = new DesktopClipboard();

        if (dcb != null)
            GlobalCore.setDefaultClipboard(dcb);

        PlatformConnector.setCallUrlListener(new ICallUrl() {

            /**
             * call
             */
            @Override
            public void call(String url) {
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
        });

        PlatformConnector.setGetApiKeyListener(new IGetApiKey() {
            @Override
            public void getApiKey() {
                // Android : GetApiAuth();
                (new GcApiLogin()).RunRequest();
            }
        });

    }

    private static void Run(boolean simulate) {
        CB_UI.onStart();

        // Gdx.input.setInputProcessor(CB_UI);

        if (simulate) {
            showSimmulateForm();
        }

    }

    // ################## simulation#################

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

    /**
     * Initialisiert die Config für die Tests! initialisiert wird die Config mit der unter Testdata abgelegten config.db3
     */
    public static void InitalConfig() {
        String base = new File("").getAbsolutePath();
        String workPath = base + "/cachebox";
        workPath = "C:/Daten/_WCB";
        // not yet initialised Log.debug(log, "workPath=" + workPath);

        new Config(workPath);

        if (Config.settings != null && Config.settings.isLoaded())
            return;

        // Read Config

        Config.Initialize(workPath, workPath + "/cachebox.config");

        // hier muss die Config Db initialisiert werden
        try {
            Database.Settings = new DesktopDB(DatabaseType.Settings);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Database.Settings.StartUp(Config.mWorkPath + "/User/Config.db3");

        try {
            Database.Data = new DesktopDB(DatabaseType.CacheBox);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Database.FieldNotes = new DesktopDB(DatabaseType.FieldNotes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
            return;
        Database.FieldNotes.StartUp(Config.mWorkPath + "/User/FieldNotes.db3");
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
        ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;

        CB_Locator.Location initialLocation;

        if (provider == ProviderType.Saved) {
            initialLocation = new CB_Locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
        } else {
            initialLocation = CB_Locator.Location.NULL_LOCATION;
        }

        new CB_Locator.Locator(initialLocation);

        // ##########################################################
        // initial settings changed handling
        // ##########################################################

        // Use Imperial units?
        CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
        Config.ImperialUnits.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
            }
        });

        // GPS update time?
        CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
        Config.gpsUpdateTime.addSettingChangedListener(new IChanged() {

            @Override
            public void handleChange() {
                CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
            }
        });

        // Use magnetic Compass?
        CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
        Config.HardwareCompass.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
            }
        });

        // Magnetic compass level
        CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
        Config.HardwareCompassLevel.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
            }
        });
    }


    private static String OS = System.getProperty("os.name").toLowerCase();

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
