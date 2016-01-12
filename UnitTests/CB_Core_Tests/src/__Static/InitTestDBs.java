package __Static;

import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.badlogic.gdx.Files.FileType;

import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.Database.DatabaseType;
import CB_Core.Types.Categories;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.PlatformSettings.IPlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingString;
import CB_Utils.Util.FileIO;

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

	// Read Config
	String workPath = "./testdata";

	Config.Initialize(workPath, workPath + "/cachebox.config");

	// hier muss die Config Db initialisiert werden
	try {
	    Database.Settings = new TestDB(DatabaseType.Settings);
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	if (!FileIO.createDirectory(Config.WorkPath))
	    return;
	Database.Settings.StartUp(Config.WorkPath + "/Config.db3");

	initialPlatformSettings();

	Config.settings.ReadFromDB();

	String database = "./testdata/test.db3";
	try {
	    InitTestDBs.InitTestDB(database);
	} catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private static void initialPlatformSettings() {
	PlatformSettings.setPlatformSettings(new IPlatformSettings() {

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
    public static void InitTestDB(String database) throws ClassNotFoundException {

	if (Database.Data != null)
	    return;
	Database.Data = new TestDB(DatabaseType.CacheBox);
	Database.Data.StartUp(database);
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
