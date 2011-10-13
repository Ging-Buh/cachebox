package CB_Core;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import CB_Core.Converter.Base64;

import CB_Core.Log.Logger;

public class Config
{

	public static String WorkPath = "";
	public static String ConfigName = "";

	public static void Initialize(String workPath, String configName)
	{
		WorkPath = workPath;
		ConfigName = configName;
	}

	public static String GetString(String key)
	{
		checkInitialization();

		String value = keyLookup.get(key);
		if (value == null) return "";
		else
			return value;
	}

	public static double GetDouble(String key)
	{
		checkInitialization();

		String value = keyLookup.get(key);
		if (value == null) return 0;
		else
			return Double.parseDouble(value);
	}

	public static float GetFloat(String key)
	{
		checkInitialization();

		String value = keyLookup.get(key);
		if (value == null) return 0;
		else
			return Float.parseFloat(value);
	}

	public static Boolean GetBool(String key)
	{
		checkInitialization();

		String value = keyLookup.get(key);
		if (value == null) return false;
		else
			return Boolean.parseBoolean(value);
	}

	public static int GetInt(String key)
	{
		checkInitialization();

		String value = keyLookup.get(key);
		if (value == null)
		{
			return -1;
		}
		else
		{
			try
			{
				return Integer.parseInt(value);
			}
			catch (Exception e)
			{
			}
			return -1;
		}
	}

	public static void changeDayNight()
	{
		Boolean value = Config.GetBool("nightMode");
		value = !value;
		Config.Set("nightMode", value);
		Config.AcceptChanges();
	}

	static HashMap<String, String> keyLookup = null;

	static boolean initialized = false;

	public static void readConfigFile()
	{
		initialized = false;
		checkInitialization();
	}

	static void checkInitialization()
	{
		if (initialized) return;

		initialized = true;

		keyLookup = new HashMap<String, String>();

		BufferedReader Filereader;

		try
		{
			Filereader = new BufferedReader(new FileReader(ConfigName));
			String line;

			while ((line = Filereader.readLine()) != null)
			{
				int idx = line.indexOf('=');
				if (idx < 0)
				{
					continue;
				}

				String key = line.substring(0, idx);
				String value = line.substring(idx + 1)/* .replace("//","/" ) */;
				keyLookup.put(key, value);
			}

			Filereader.close();
		}
		catch (IOException e)
		{
			Logger.Error("ReadConfig", "Error when accessing cachebox.config!", e);
			e.printStackTrace();
		}

		validateDefaultConfigFile();
	}

	public static void validateDefaultConfigFile()
	{
		validateSetting("LanguagePath", WorkPath + "/data/lang");
		validateSetting("Sel_LanguagePath", WorkPath + "/data/lang/en.lan");
		validateSetting("DatabasePath", WorkPath + "/cachebox.db3");
		validateSetting("TileCacheFolder", WorkPath + "/cache");
		validateSetting("PocketQueryFolder", WorkPath + "/PocketQuery");
		validateSetting("DescriptionImageFolder", WorkPath + "/repository/images");
		validateSetting("MapPackFolder", WorkPath + "/repository/maps");
		validateSetting("SpoilerFolder", WorkPath + "/repository/spoilers");
		validateSetting("UserImageFolder", WorkPath + "/User/Media");
		validateSetting("TrackFolder", WorkPath + "/User/Tracks");
		// validateSetting("FieldNotesHtmlPath", Global.AppPath +
		// "\\User\\fieldnotes.html");
		validateSetting("FieldNotesGarminPath", WorkPath + "/User/geocache_visits.txt");
		// validateSetting("GPXExportPath", Global.AppPath +
		// "\\User\\cachebox_export.gpx");
		validateSetting("SaveFieldNotesHtml", "true");

		validateSetting("Proxy", "");
		validateSetting("ProxyPort", "");
		validateSetting("DopMin", "0.2");
		validateSetting("DopWidth", "1");
		validateSetting("OsmDpiAwareRendering", "true");
		validateSetting("LogMaxMonthAge", "99999");
		validateSetting("LogMinCount", "99999");
		validateSetting("MapInitLatitude", "-1000");
		validateSetting("MapInitLongitude", "-1000");
		validateSetting("AllowInternetAccess", "true");
		validateSetting("AllowRouteInternet", "true");
		validateSetting("ImportGpx", "true");
		validateSetting("CacheMapData", "false");
		validateSetting("CacheImageData", "false");
		validateSetting("OsmMinLevel", "8");
		validateSetting("OsmMaxImportLevel", "16");
		validateSetting("OsmMaxLevel", "17");
		validateSetting("OsmCoverage", "1000");
		validateSetting("SuppressPowerSaving", "true");
		validateSetting("PlaySounds", "true");
		validateSetting("PopSkipOutdatedGpx", "true");
		validateSetting("MapHideMyFinds", "false");
		validateSetting("MapShowRating", "true");
		validateSetting("MapShowDT", "true");
		validateSetting("MapShowTitles", "true");
		validateSetting("ShowKeypad", "true");
		validateSetting("FoundOffset", "0");
		validateSetting("ImportLayerOsm", "true");
		validateSetting("CurrentMapLayer", "Mapnik");
		validateSetting("AutoUpdate", "http://www.getcachebox.net/latest-stable");
		validateSetting("NavigationProvider", "http://129.206.229.146/openrouteservice/php/OpenLSRS_DetermineRoute.php");
		validateSetting("TrackRecorderStartup", "false");
		validateSetting("MapShowCompass", "true");
		validateSetting("FoundTemplate", "<br>###finds##, ##time##, Found it with DroidCachebox!");
		validateSetting("DNFTemplate", "<br>##time##. Logged it with DroidCachebox!");
		validateSetting("NeedsMaintenanceTemplate", "Logged it with DroidCachebox!");
		validateSetting("AddNoteTemplate", "Logged it with DroidCachebox!");
		validateSetting("ResortRepaint", "false");
		validateSetting("TrackDistance", "3");
		validateSetting("MapMaxCachesLabel", "12");
		validateSetting("MapMaxCachesDisplay_config", "10000");
		validateSetting("SoundApproachDistance", "50");
		validateSetting("mapMaxCachesDisplayLarge_config", "75");
		// validateSetting("Filter", PresetListView.presets[0].toString());
		validateSetting("ZoomCross", "16");
		// validateSetting("TomTomExportFolder", Global.AppPath + "\\user");
		validateSetting("GCAutoSyncCachesFound", "true");
		validateSetting("GCAdditionalImageDownload", "false");
		validateSetting("GCRequestDelay", "10");

		validateSetting("MultiDBAsk", "true");
		validateSetting("MultiDBAutoStartTime", "0");
		validateSetting("FieldnotesUploadAll", "false");

		validateSetting("SpoilersDescriptionTags", "");
		validateSetting("AutoResort", "false");

		validateSetting("HtcCompass", "false");
		validateSetting("HtcLevel", "30");
		validateSetting("SmoothScrolling", "none");

		validateSetting("DebugShowPanel", "false");
		validateSetting("DebugMemory", "false");
		validateSetting("DebugShowMsg", "false");

		validateSetting("LockM", "1");
		validateSetting("LockSec", "0");
		validateSetting("AllowLandscape", "false");
		validateSetting("MoveMapCenterWithSpeed", "false");
		validateSetting("MoveMapCenterMaxSpeed", "20");
		validateSetting("lastZoomLevel", "14");
		validateSetting("quickButtonShow", "true");
		validateSetting("quickButtonList", "5,0,1,3,2");
		validateSetting("PremiumMember", "false");
		validateSetting("SearchWithoutFounds", "true");
		validateSetting("SearchWithoutOwns", "true");

		// validateSetting("OtherRepositoriesFolder", Global.AppPath +
		// "\\Repositories");

		AcceptChanges();
	}

	private static void validateSetting(String key, String value)
	{
		String Lookupvalue = keyLookup.get(key);
		if (Lookupvalue == null) keyLookup.put(key, value);
	}

	public static void Set(String key, String value)
	{
		checkInitialization();
		keyLookup.put(key, value);
	}

	public static void Set(String key, double value)
	{
		Set(key, String.valueOf(value));
	}

	public static void Set(String key, float value)
	{
		Set(key, String.valueOf(value));
	}

	public static void Set(String key, boolean value)
	{
		Set(key, String.valueOf(value));
	}

	public static void Set(String key, int value)
	{
		Set(key, String.valueOf(value));
	}

	public static void AcceptChanges()
	{

		BufferedWriter myFilewriter;
		try
		{
			myFilewriter = new BufferedWriter(new FileWriter(ConfigName));

			for (String key : keyLookup.keySet())
			{

				myFilewriter.write(key + "=" + keyLookup.get(key));
				myFilewriter.newLine();

			}
			myFilewriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//
		HtcCompass_ValueChanged = true;

	}

	public static String GetStringEncrypted(String key)
	{
		String s;
		boolean convert = false;
		if (ExistsKey(key + "Enc"))
		{
			s = GetString(key + "Enc");
			if (s != "")
			{
				// encrypted Key is found -> remove the old non encrypted
				if (ExistsKey(key))
				{
					keyLookup.remove(key);
					AcceptChanges();
				}
				s = decrypt(s);
			}
		}
		else
		{
			// no encrypted Key is found -> search for non encrypted
			s = GetString(key);
			if (s != "")
			{
				// remove the old non encrypted and insert a new encrypted
				keyLookup.remove(key);
				convert = true;
			}
		}

		if (convert)
		{
			SetEncrypted(key, s);
			AcceptChanges();
		}
		return s;
	}

	public static boolean ExistsKey(String key)
	{
		checkInitialization();
		return keyLookup.containsKey(key);
	}

	/*
	 * Nachfolgend die Getter von Einstellungen, welche sehr h�ufig abgerufen
	 * werden. Diese Einstellungen werden zwischen gespeichert und erst bei
	 * einer �nderung aktualisiert. Diese erspart das Parsen von Werten
	 */

	/**
	 * true wenn sich der Wert ge�ndert hat
	 */
	private static boolean HtcCompass_ValueChanged = true;

	private static boolean mHtcCompass;

	public static boolean getHtcCompass()
	{
		if (HtcCompass_ValueChanged)
		{
			mHtcCompass = GetBool("HtcCompass");
			HtcCompass_ValueChanged = false;
		}

		return mHtcCompass;
	}

	static int[] Key =
		{ 128, 56, 20, 78, 33, 225 };

	public static void RC4(int[] bytes, int[] key)
	{
		int[] s = new int[256];
		int[] k = new int[256];
		int temp;
		int i, j;

		for (i = 0; i < 256; i++)
		{
			s[i] = (int) i;
			k[i] = (int) key[i % key.length];
		}

		j = 0;
		for (i = 0; i < 256; i++)
		{
			j = (j + s[i] + k[i]) % 256;
			temp = s[i];
			s[i] = s[j];
			s[j] = temp;
		}

		i = j = 0;
		for (int x = 0; x < bytes.length; x++)
		{
			i = (i + 1) % 256;
			j = (j + s[i]) % 256;
			temp = s[i];
			s[i] = s[j];
			s[j] = temp;
			int t = (s[i] + s[j]) % 256;
			bytes[x] = (int) (bytes[x] ^ s[t]);
		}
	}

	public static void SetEncrypted(String key, String value)
	{
		String encrypted = encrypt(value);
		if (ExistsKey(key)) keyLookup.remove(key); // remove non decrypted key
													// if exists
		Set(key + "Enc", encrypted);
	}

	public static String encrypt(String value)
	{
		int[] b = byte2intArray(value.getBytes());
		RC4(b, Key);
		String encrypted = Base64.encodeBytes(int2byteArray(b));
		return encrypted;
	}

	public static String decrypt(String value)
	{
		int[] b = null;
		try
		{
			b = byte2intArray(Base64.decode(value));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RC4(b, Key);
		String decrypted="";
		
			char[] c = new char[b.length];
			for (int x = 0; x < b.length; x++)
			{
				c[x]= (char) b[x];
			}
			
			
			decrypted = String.copyValueOf(c);
		
		
		return decrypted;

	}

	private static int[] byte2intArray(byte[] b)
	{
		int[] i = new int[b.length];

		for (int x = 0; x < b.length; x++)
		{
			int t = b[x];
			if (t < 0)
			{
				t += 256;
			}
			i[x] = t;
		}

		return i;
	}

	private static byte[] int2byteArray(int[] i)
	{
		byte[] b = new byte[i.length];

		for (int x = 0; x < i.length; x++)
		{

			int t = i[x];
			if (t > 128)
			{
				t -= 256;
			}

			b[x] = (byte) t;
		}

		return b;
	}

	// Read the encrypted AccessToken from the config and check wheter it is correct for Andorid CB
    public static String GetAccessToken()
    {
        String act = GetStringEncrypted("GcAPI");
        // Pr�fen, ob das AccessToken f�r ACB ist!!!
        if (!(act.startsWith("A")))
            return "";
        String result = act.substring(1,act.length());
        return result;
    }

}
