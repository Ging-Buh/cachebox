package de.droidcachebox;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;

import java.util.HashMap;


import de.droidcachebox.Events.SelectedLangChangedEventList;
import de.droidcachebox.Views.FilterSettings.PresetListView;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class Config {
	
	
    public static final String WorkPath = Environment.getExternalStorageDirectory() + "/cachebox";
	public static final String ConfigName = WorkPath + "/cachebox.config";

	public static String GetString(String key)
     {
         checkInitialization();

         String value = keyLookup.get(key);
         if (value == null)
             return "";
         else
             return value;
     }

    public static double GetDouble(String key)
    {
        checkInitialization();

        String value = keyLookup.get(key);
        if (value == null)
            return 0 ; 
        else
            return Double.parseDouble(value);
    }

    public static float GetFloat(String key)
    {
        checkInitialization();

        String value = keyLookup.get(key);
        if (value == null)
            return 0;
        else
            return Float.parseFloat(value);
    }

    public static Boolean GetBool(String key)
    {
        checkInitialization();

        String value = keyLookup.get(key);
        if (value == null)
            return false;
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
            } catch (Exception e) {}
            return -1;
        }
    }
	
    public static void changeDayNight()
    {
    	Boolean value = Config.GetBool("nightMode");
    	value = !value;
    	Config.Set("nightMode",value);
    	
    }
	
	 static HashMap<String, String> keyLookup = null;

     static boolean initialized = false;

     static AssetManager AssetMgr;
    public static void readConfigFile(AssetManager mgr) 
    {
    	AssetMgr = mgr;
    	initialized=false;
    	checkInitialization();
    }
    public static void readConfigFile() 
    {
    	initialized=false;
    	checkInitialization();
    }
    
     
	static void checkInitialization()
     {
         if (initialized)
             return;

         initialized = true;

         keyLookup =  new HashMap<String, String>();
         
                 // c# TextReader reader = new StreamReader(Global.AppPath + "\\cachebox.config");
                 BufferedReader Filereader;
                 
                 try {
                	 //Filereader = new BufferedReader(new InputStreamReader(AssetMgr.open(ConfigName)));
                	 
                	 String state = Environment.getExternalStorageState();                	 
                	 if (!Environment.MEDIA_MOUNTED.equals(state)) {
                		 // External Storage not mounted or not readable
                		 initialized = false;
                		 return;                		 
                	 }
                	 
                	 File fileex = Environment.getExternalStorageDirectory();

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
					     String value = line.substring(idx + 1).replace("//","/" );
					         keyLookup.put(key, value);
					 }
					
					
					  Filereader.close(); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
//        validateSetting("PocketQueryFolder", Global.AppPath + "\\PocketQuery");
        validateSetting("DescriptionImageFolder", WorkPath + "/repository/images");
        validateSetting("MapPackFolder", WorkPath + "/repository/maps");
        validateSetting("SpoilerFolder", WorkPath + "/repository/spoilers");
        validateSetting("UserImageFolder", WorkPath + "/User/Media");
        validateSetting("TrackFolder", WorkPath + "/User/Tracks");
//        validateSetting("FieldNotesHtmlPath", Global.AppPath + "\\User\\fieldnotes.html");
        validateSetting("FieldNotesGarminPath", WorkPath + "/User/geocache_visits.txt");
//        validateSetting("GPXExportPath", Global.AppPath + "\\User\\cachebox_export.gpx");
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
        validateSetting("Filter", PresetListView.presets[0].toString());
        validateSetting("ZoomCross", "16");
        validateSetting("GpsDriverMethod", "default");
//        validateSetting("TomTomExportFolder", Global.AppPath + "\\user");
        validateSetting("GCAutoSyncCachesFound", "true");
        validateSetting("GCAdditionalImageDownload", "false");
        validateSetting("GCRequestDelay", "10");

        validateSetting("Camera_Resolution_Width", "640");
        validateSetting("Camera_Resolution_Height", "480");

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

        validateSetting("LockH", "1");
        validateSetting("LockM", "0");
        
//        validateSetting("OtherRepositoriesFolder", Global.AppPath + "\\Repositories");

        AcceptChanges();
    }

    private static void validateSetting(String key, String value)
    {
    	 String Lookupvalue = keyLookup.get(key);
         if (Lookupvalue == null)
            keyLookup.put(key, value);
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
    	//file:///android_asset/
    	
    	
    	BufferedWriter myFilewriter;
    	 try 
    	 {
    		 //myFilewriter = AssetMgr.getLocales() 
    		 
    		 myFilewriter = new BufferedWriter(new FileWriter(ConfigName));
	
    	
    		 for(String key : keyLookup.keySet())
    		 {
			
				myFilewriter.write(key + "=" + keyLookup.get(key));
    		 	myFilewriter.newLine();
				
				
    		 }
    		 myFilewriter.close();
    	 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    	 }

        
    }

}





