package __Static;

import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.badlogic.gdx.Files.FileType;

import CB_Core.CoreSettingsForward;
import CB_Core.DB.Database;
import CB_Core.Types.Categories;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.PlatformSettings.iPlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingString;
import de.cb.sqlite.DatabaseFactory;
import de.cb.sqlite.DesktopDatabaseFactory;

/**
 * Initialisiert die Config oder eine TestDB
 * 
 * @author Longri
 */
public class InitTestDBs {

    static Preferences prefs = Preferences.userNodeForPackage(InitTestDBs.class);

    /**
     * Initialisiert die Config f�r die Tests! initialisiert wird die Config mit der unter Testdata abgelegten config.db3
     */
    public static void InitalConfig() {

	if (Database.Settings != null)
	    return;

	// Initial database factory
	if (!DatabaseFactory.isInitial())
	    new DesktopDatabaseFactory();

	// Read Config
	String workPath = "./testdata";

	Config.Initialize(workPath, workPath + "/cachebox.config");
	if (!Database.Data.db.isStarted())
	    Database.initialDataDB("./testdata/test.db3");

	initialPlatformSettings();

	Config.settings.ReadFromDB();
    }

    private static void initialPlatformSettings() {
	PlatformSettings.setPlatformSettings(new iPlatformSettings() {

	    @Override
	    public void Write(SettingBase<?> setting) {

		if (setting instanceof SettingBool) {
		    prefs.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
		}

		else if (setting instanceof SettingString) {
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
    }

    /**
     * Initialisiert eine CacheBox DB f�r die Tests
     * 
     * @param database
     *            Pfad zur DB
     * @throws ClassNotFoundException
     */
    public static void InitTestDB(String path) throws ClassNotFoundException {

	// Initial database factory
	if (!DatabaseFactory.isInitial())
	    new DesktopDatabaseFactory();

	if (Database.Data != null && Database.Data.db.isStarted())
	    return;

	Database.initialDataDB(path);

	CoreSettingsForward.Categories = new Categories();
	Database.Data.GPXFilenameUpdateCacheCount();
    }

    private static String lastLoadedTranslation;

    public static void InitialTranslations(String lang) {
	if (Translation.that != null) {
	    if (lastLoadedTranslation.equals(lang))
		return;
	}

	lastLoadedTranslation = lang;

	InitalConfig();

	new Translation(Config.WorkPath, FileType.Absolute);
	try {
	    Translation.LoadTranslation(Config.WorkPath + "/lang/" + lang + "/strings.ini");
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
}
