package de.droidcachebox;

import com.badlogic.gdx.Gdx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.GcApiLogin;
import de.droidcachebox.database.SQLiteClass;
import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.locator.map.MapTileLoader;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.settings.SettingInt;
import de.droidcachebox.settings.SettingString;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.StringReturner;
import de.droidcachebox.utils.log.Log;

public class DesktopUIBaseMethods implements PlatformUIBase.UIBaseMethods {
    private static final String sClass = "DesktopUIBaseMethods";
    static Preferences prefs = Preferences.userNodeForPackage(de.DesktopMain.class);

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
    public void switchToGpsDefault() {
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
    public SQLiteInterface createSQLInstance() {
        return new SQLiteClass();
    }

    @Override
    public void getApiKey() {
        // Android : GetApiAuth();
        (new GcApiLogin()).runRequest();
    }

    @Override
    public void quit() {
        if (GlobalCore.isSetSelectedCache()) {
            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
            Settings.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
            Log.debug(sClass, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGeoCacheCode());
        }
        MapTileLoader.getInstance().stopQueueProzessors();
        Settings.getInstance().acceptChanges();
        Gdx.app.exit();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 2000);
    }

    @Override
    public void handleExternalRequest() {

    }

    @Override
    public String removeHtmlEntyties(String text) {
        // todo Jsoup.parse(s).text();
        return text.replaceAll("\\<[^>]*>", "");
    }

    @Override
    public String getFileProviderContentUrl(String localFile) {
        return localFile;
    }

    @Override
    public void getDirectoryAccess(String _DirectoryToAccess, StringReturner returnUri) {

    }

    @Override
    public void getDocumentAccess(String directory, StringReturner value) {

    }

    @Override
    public InputStream getInputStream(String absolutePath) throws FileNotFoundException {
        // will never be used ?
        return FileFactory.createFile(absolutePath).getFileInputStream();
    }

    @Override
    public OutputStream getOutputStream(String contentFile) throws FileNotFoundException {
        return FileFactory.createFile(contentFile).getFileOutputStream();
    }

    @Override
    public boolean request_getLocationIfInBackground() {
        return true;
    }

    @Override
    public int getCacheCountInDB(String absolutePath) {
        int count = 0;
        Connection myDB;
        try {
            myDB = DriverManager.getConnection("jdbc:sqlite:" + absolutePath);
            Statement statement = myDB.createStatement();
            ResultSet result = statement.executeQuery("select count(*) from caches");
            // result.first();
            count = result.getInt(1);
            result.close();
            myDB.close();
        } catch (SQLException ignored) {
            // String s = e.getMessage();
        }
        return count;
    }

}
