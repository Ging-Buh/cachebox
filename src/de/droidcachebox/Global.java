package de.droidcachebox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import de.droidcachebox.Events.SelectedCacheEventList;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Messenger;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.JokerList;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Locator.Locator;
import de.droidcachebox.Map.RouteOverlay;
import de.droidcachebox.TranslationEngine.LangStrings;
import de.droidcachebox.Views.MapView.SmoothScrollingTyp;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.MessageBox;


public class Global {
    public static final int CurrentRevision = 244;
    public static final String CurrentVersion = "0.0.";
    public static final String VersionPrefix = "alpha";
    public static final int LatestDatabaseChange = 1002;
    public static final int LatestDatabaseFieldNoteChange = 1001;
    
    
    public static final String br = String.format("%n");
    public static final String splashMsg =
    	"Team Cachebox (2011)" + br +
    	"www.team-cachebox.de" + br +
    	"Cache Icons Copyright 2009," + br +
    	"Groundspeak Inc.Used with permission" + br + br +
    	"Support under: www.geoclub.de/viewforum.php?f=114 ";
    
    public static final boolean Debug = true;
	public static JokerList Jokers = new JokerList();
	
    
    
    public static FilterProperties LastFilter = null;
	public static boolean autoResort;
	public static Bitmap EmptyBmp = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565); // kleinst mögliches Bmp 
    
    // for MapView
	public static SmoothScrollingTyp SmoothScrolling = SmoothScrollingTyp.normal;
	public static RouteOverlay.Route AktuelleRoute = null;
    public static int aktuelleRouteCount = 0;
    public static boolean ResortAtWork = false;
    
    public static long TrackDistance;
    
    //Sizes
    public static int scaledFontSize_normal;
    public static int CornerSize;
    
    public static LangStrings Translations = new LangStrings();
    
    public static Coordinate Marker = new Coordinate();

    // Icons
    /**
     * <b>Ein Array mit Icons als Drawable</b>
     * <br>
     * 			<br>Index 0 =<img src="doc-files/night_tb.png" width=32 height=32> <img src="doc-files/day_tb.png" width=32 height=32>
        		<br>Index 1 =<img src="doc-files/addwaypoint.png" width=32 height=32>
        		<br>Index 2 =<img src="doc-files/smilie_gross.gif" width=32 height=32>
        		<br>Index 3 =<img src="doc-files/download.png" width=32 height=32>
        		<br>Index 4 =<img src="doc-files/log1.png" width=32 height=32>
        		<br>Index 5 =<img src="doc-files/maintenance.png" width=32 height=32>
        		<br>Index 6 =<img src="doc-files/checkbox_checked.png" width=32 height=32>
        		<br>Index 7 =<img src="doc-files/checkbox_unchecked.png" width=32 height=32>
        		<br>Index 8 =<img src="doc-files/sonne.png" width=32 height=32>
        		<br>Index 9 =<img src="doc-files/mond.png" width=32 height=32>
        		<br>Index 10 =<img src="doc-files/travelbug.gif" width=32 height=32>
        		<br>Index 11 =<img src="doc-files/collapse.png" width=32 height=32>
        		<br>Index 12 =<img src="doc-files/expand.png" width=32 height=32>
        		<br>Index 13 =<img src="doc-files/enabled.png" width=32 height=32>
        		<br>Index 14 =<img src="doc-files/disabled.png" width=32 height=32>
        		<br>Index 15 =<img src="doc-files/retrieve_tb.png" width=32 height=32>
        		<br>Index 16 =<img src="doc-files/drop_tb.png" width=32 height=32>
        		<br>Index 17 =<img src="doc-files/star.png" width=32 height=32>
        		<br>Index 18 =<img src="doc-files/earth.png" width=32 height=32>
        		<br>Index 19 =<img src="doc-files/favorit.png" width=32 height=32>
        		<br>Index 20 =<img src="doc-files/file.png" width=32 height=32>
        		<br>Index 21 =<img src="doc-files/userdata.jpg" width=32 height=32>
        		<br>Index 22 =<img src="doc-files/delete.jpg" width=32 height=32>  
        		<br>Index 23 =<img src="doc-files/archiv.png" width=32 height=32> 
        		<br>Index 24 =<img src="doc-files/not_available.jpg" width=32 height=32> 
        		<br>Index 25 =<img src="doc-files/checkbox_crossed.png" width=32 height=32> 
        		<br>Index 26 =<img src="doc-files/map22.png" width=32 height=32> 
        		<br>Index 27 =<img src="doc-files/chk_icon.png" width=32 height=32> 
        		<br>Index 28 =<img src="doc-files/delete_icon.png" width=32 height=32> 
        		<br>Index 29 =<img src="doc-files/voice_rec_icon.png" width=32 height=32> 
        		<br>Index 30 =<img src="doc-files/satellite.png" width=32 height=32> 
        		<br>Index 31 =<img src="doc-files/close_icon.png" width=32 height=32> 
        		<br>Index 32 =<img src="doc-files/info_icon.png" width=32 height=32> 
        		<br>Index 33 =<img src="doc-files/warning_icon.png" width=32 height=32> 
        		<br>Index 34 =<img src="doc-files/help_icon.png" width=32 height=32> 
     */
    public static Drawable[] Icons = null;
    public static Drawable[] SmallStarIcons= null;
    public static Drawable[] StarIcons= null;
    public static Drawable[] SizeIcons= null;
    
    /**
     * <b>Ein Array mit CacheIcons als Drawable</b>
     * <br>
     * 
     *          <br>Index 0 =<img src="doc-files/big_0.gif" width=32 height=32>
        		<br>Index 1 =<img src="doc-files/big_1.gif" width=32 height=32>
        		<br>Index 2 =<img src="doc-files/big_2.gif" width=32 height=32>
        		<br>Index 3 =<img src="doc-files/big_3.gif" width=32 height=32>
        		<br>Index 4 =<img src="doc-files/big_4.gif" width=32 height=32>
        		<br>Index 5 =<img src="doc-files/big_5.gif" width=32 height=32>
        		<br>Index 6 =<img src="doc-files/big_6.gif" width=32 height=32>
        		<br>Index 7 =<img src="doc-files/big_7.png" width=32 height=32>
        		<br>Index 8 =<img src="doc-files/big_8.gif" width=32 height=32>
        		<br>Index 9 =<img src="doc-files/big_9.gif" width=32 height=32>
        		<br>Index 10 =<img src="doc-files/big_10.gif" width=32 height=32>
        		<br>Index 11 =<img src="doc-files/big_11.png" width=32 height=32>
        		<br>Index 12 =<img src="doc-files/big_12.png" width=32 height=32>
        		<br>Index 13 =<img src="doc-files/big_13.png" width=32 height=32>
        		<br>Index 14 =<img src="doc-files/big_14.png" width=32 height=32>
        		<br>Index 15 =<img src="doc-files/big_15.png" width=32 height=32>
        		<br>Index 16 =<img src="doc-files/big_16.png" width=32 height=32>
        		<br>Index 17 =<img src="doc-files/big_17.png" width=32 height=32>
        		<br>Index 18 =<img src="doc-files/big_18.png" width=32 height=32>
        		<br>Index 19 =<img src="doc-files/big_19.gif" width=32 height=32>
     */
    public static Drawable[] CacheIconsBig= null;
    public static Drawable[] BatteryIcons= null;
    
    /**
     * <b>Ein Array mit LogIcons als Drawable</b>
     * <br>
     * 			 <br>Index 0 =<img src="doc-files/log0.gif" width=32 height=32>
        		 <br>Index 1 =<img src="doc-files/log1.png" width=32 height=32>
        		 <br>Index 2 =<img src="doc-files/log2.png" width=32 height=32>
        		 <br>Index 3 =<img src="doc-files/log3.png" width=32 height=32>
        		 <br>Index 4 =<img src="doc-files/log4.png" width=32 height=32>
        		 <br>Index 5 =<img src="doc-files/log5.png" width=32 height=32>
        		 <br>Index 6 =<img src="doc-files/log6.png" width=32 height=32>
        		 <br>Index 7 =<img src="doc-files/log7.png" width=32 height=32>
        		 <br>Index 8 =<img src="doc-files/log8.png" width=32 height=32>
        		 <br>Index 9 =<img src="doc-files/log9.png" width=32 height=32>
        		 <br>Index 10 =<img src="doc-files/log10.png" width=32 height=32>
        		 <br>Index 11 =<img src="doc-files/log11.jpg" width=32 height=32>
        		 <br>Index 12 =<img src="doc-files/log12.jpg" width=32 height=32>
        		 <br>Index 13 =<img src="doc-files/log13.png" width=32 height=32>
     */
    public static Drawable[] LogIcons= null;
    public static Drawable[] Arrows= null;
    public static Drawable[] ChkIcons= null;
    public static Drawable[] BtnIcons= null;
    
    // New Map Icons
    public static ArrayList<ArrayList<Drawable>> NewMapIcons = null;
    public static ArrayList<ArrayList<Drawable>> NewMapOverlay = null;
    
    
    /// <summary>
    /// Letzte bekannte Position
    /// </summary>
    public static Coordinate LastValidPosition = new Coordinate();
    /// <summary>
    /// Instanz des GPS-Parsers
    /// </summary>
    public static Locator Locator = null;

    protected static Cache selectedCache = null;
    public static void SelectedCache(Cache cache)
    {
    	selectedCache = cache;
    	selectedWaypoint = null;
    	SelectedCacheEventList.Call(cache, null);
    }
    
    public static Cache SelectedCache()
    {
    	return selectedCache;
    }

    private static Cache nearestCache = null;
    public static Cache NearestCache()
    {
    	return nearestCache;
    }
    public static void NearestCache(Cache nearest)
    {
    	nearestCache = nearest;
    }
    
    protected static Waypoint selectedWaypoint = null;
    public static void SelectedWaypoint(Cache cache, Waypoint waypoint)
    {
    	selectedCache = cache;
    	selectedWaypoint = waypoint;
    	SelectedCacheEventList.Call(selectedCache, waypoint);
    }
    public static Waypoint SelectedWaypoint()
    {
    	return selectedWaypoint;
    }

    
    public static class Paints
    {
    	public static Paint mesurePaint;
    	public static Paint ListBackground;
    	public static class Day
    		 {
	    		
				public static Paint selectedBack;   
	 		    public static Paint ListBackground; 
	 		    public static Paint ListBackground_second;
	 		    
	 		   
    		 }
    		 
    	public static class Night
    		 {
    			
    			public static Paint selectedBack;   
     		    public static Paint ListBackground;
     		    public static Paint ListBackground_second;
     		    
     		   
    		 }
    		 
    	public static void init(Context context)
        {
        	Resources res = context.getResources();
        	
        	// calc sizes
        	scaledFontSize_normal = res.getDimensionPixelSize(R.dimen.TextSize_normal);
        	CornerSize = scaledFontSize_normal/2;
        	
        	mesurePaint = new Paint();
        	mesurePaint.setTextSize(scaledFontSize_normal);
        	
        	ListBackground=new Paint();
    		Night.ListBackground_second=new Paint();
    		
    		Night.ListBackground=new Paint();
    		Night.ListBackground.setColor(res.getColor(R.color.Night_ListBackground));
    		
    		Night.selectedBack=new Paint();
    		Night.selectedBack.setColor(res.getColor(R.color.Night_SelectedBackground));
    		
    		
    		
    		Day.ListBackground=new Paint();
    		Day.ListBackground.setColor(res.getColor(R.color.Day_ListBackground));
    		Day.ListBackground_second=new Paint();
    		
    		
    		Day.selectedBack=new Paint();
    		Day.selectedBack.setColor(res.getColor(R.color.Day_SelectedBackground));
    		
    		
    		
    	}
    		 
    }
    
 
    public static void setDebugMsg(String msg)
    {
    	((main) main.mainActivity).setDebugMsg(msg);
    }
    
    
    /// <summary>
    ///     SDBM-Hash algorithm for storing hash values into the database. This is neccessary to be compatible to the CacheBox@Home project. Because the
    ///     standard .net Hash algorithm differs from compact edition to the normal edition.
    /// </summary>
    /// <param name="str"></param>
    /// <returns></returns>
    public static long sdbm(String str)
    {
        long hash = 0;
        // set mask to 2^32!!!???!!!
        long mask = 42949672;
        mask = mask * 100 + 95;

        for (int i = 0; i < str.length(); i++)
        {
        	char c = str.charAt(i);
            hash = (c + (hash << 6) + (hash << 16) - hash) & mask;
        }

        return hash; 
    }
    
    /**
     * Überprüft ob ein File exestiert!
     * @param filename
     * @return true, wenn das File Exestiert, ansonsten false.
     */
    public static boolean FileExists(String filename)
    {
    	File file = new File(filename);
    	return file.exists();
    }

    /**
     * Überprüft ob ein Ordner exestiert und legt ihn an, wenn er nicht exestiert.
     * @param folder Pfad des Ordners
     * @return true wenn er exestiert oder Angelegt wurde. false wenn das Anlegen nicht Funktioniert hat.
     */
    public static boolean DirectoryExists(String folder)
    {
    	File f = new File(folder);
    	if (f.isDirectory())
    		return true;
    	else
    	{
    		// have the object build the directory structure, if needed.
    		return f.mkdirs();
    	}
    }
 
    public static String GetFileExtension(String filename)
    {
	    int dotposition= filename.lastIndexOf(".");
//	    String filename_Without_Ext = filename.substring(0, dotposition);
	    String ext = filename.substring(dotposition + 1, filename.length());
	    return ext;
    }
    
    public static String GetFileNameWithoutExtension(String filename)
    {
	    int dotposition= filename.lastIndexOf(".");
	    if (dotposition >= 0)
	    	filename = filename.substring(dotposition-1);
	    int slashposition = filename.lastIndexOf("/");
	    if (slashposition >= 0)
	    	filename = filename.substring(slashposition + 1, filename.length());
	    return filename;
    	
    }

    public static String GetFileName(String filename)
    {
	    int slashposition = filename.lastIndexOf("/");
	    if (slashposition >= 0)
	    	filename = filename.substring(slashposition + 1, filename.length());
	    return filename;
    	
    }

	public static String GetDirectoryName(String filename) 
	{
	    int slashposition = filename.lastIndexOf("/");
	    if (slashposition >= 0)
	    	filename = filename.substring(0, slashposition);
	    return filename;
	}

  
    public static String getRelativePath(String targetPath, String basePath, String pathSeparator) 
    {   //  We need the -1 argument to split to make sure we get a trailing      
    	//  "" token if the base ends in the path separator and is therefore     
    	//  a directory. We require directory paths to end in the path     
    	//  separator -- otherwise they are indistinguishable from files.     
    	String[] base = basePath.split(Pattern.quote(pathSeparator), -1);     
    	String[] target = targetPath.split(Pattern.quote(pathSeparator), 0);     
    	//  First get all the common elements. Store them as a string,    
    	//  and also count how many of them there are.      
    	String common = "";     
    	int commonIndex = 0;     
    	for (int i = 0; i < target.length && i < base.length; i++) 
    	{         
    		if (target[i].equals(base[i])) 
    		{             
    			common += target[i] + pathSeparator;             
    			commonIndex++;         
    		}         
    		else break;     
    	}     

        if (commonIndex == 0)     
        {         //  Whoops -- not even a single common path element. This most         
        	//  likely indicates differing drive letters, like C: and D:.          
        	//  These paths cannot be relativized. Return the target path.         
        	return targetPath;         
        	//  This should never happen when all absolute paths
        	//  begin with / as in *nix.      
        }     
        String relative = "";     
        if (base.length == commonIndex) 
        {         
        	//  Comment this out if you prefer that a relative path not start with ./         
        	//relative = "." + pathSeparator;     
        }     
        else 
        {         
        	int numDirsUp = base.length - commonIndex;        
        	//  The number of directories we have to backtrack is the length of         
        	//  the base path MINUS the number of common path elements, minus         
        	//  one because the last element in the path isn't a directory.         
        	for (int i = 1; i <= (numDirsUp); i++) 
        	{             
        		relative += ".." + pathSeparator;         
        	}     
        }     
        relative += targetPath.substring(common.length());     
        return relative; 
    }
    

    public static String RemoveInvalidFatChars(String str)
    {
        String[] invalidChars = new String[] { ":", "\\", "/", "<", ">", "?", "*", "|", "\"", ";" };
        
        for (int i = 0; i < invalidChars.length; i++)
            str = str.replace(invalidChars[i], "");

        return str;
    }
    

    public static String GetDateTimeString()
    {
        Date now = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = datFormat.format(now);
        datFormat = new SimpleDateFormat("HHmmss");
        sDate += " " + datFormat.format(now);
        return sDate;
    }

    
    public static String GetTrackDateTimeString()
    {
        Date now = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = datFormat.format(now);
        datFormat = new SimpleDateFormat("HH:mm:ss");
        sDate += "T" + datFormat.format(now) + "Z";
        return sDate;
    }

    
    // N = Nachtmodus! Wenn true werden andere Icons geladen!
    public static void InitIcons(Context context, Boolean N)
    {
    	Resources res = context.getResources();
    	if (NewMapIcons == null)
    	{
    		
	    	 NewMapIcons = new ArrayList<ArrayList<Drawable>>();
	    	 NewMapOverlay = new ArrayList<ArrayList<Drawable>>();
	    	    
	    	
	        // NewMapIcons[0] contains the 8x8 Bitmaps
	        NewMapIcons.add(new ArrayList<Drawable>());
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_green));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_yellow));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_red));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_white));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_blue));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_violet));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_found));
	        NewMapIcons.get(0).add(res.getDrawable(R.drawable.map_8x8_own));
	        
	        // NewMapIcons[1] contains the 13x13 Bitmaps
	        NewMapIcons.add(new ArrayList<Drawable>());
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_green));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_yellow));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_red));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_white));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_blue));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_violet));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_found));
	        NewMapIcons.get(1).add(res.getDrawable(R.drawable.map_13x13_own));
	        
	        // NewMapIcons[2] contains the normal 20x20 Bitmaps
	        NewMapIcons.add(new ArrayList<Drawable>());        
	    	NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_0));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_1));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_2));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_3));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_4));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_5));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_6));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_7));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_8));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_9));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_10));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_12));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_12));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_13));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_14));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_15));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_16));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_17));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_18));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_19));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_20));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_21));
	        NewMapIcons.get(2).add(res.getDrawable(R.drawable.map_20x20_22));
	
	        // Overlays for Icons
	        NewMapOverlay.add(new ArrayList<Drawable>());  // 8x8
	        NewMapOverlay.get(0).add(res.getDrawable(R.drawable.map_8x8_shaddowrect));
	        NewMapOverlay.get(0).add(res.getDrawable(R.drawable.map_8x8_shaddowround));
	        NewMapOverlay.get(0).add(res.getDrawable(R.drawable.map_8x8_shaddowstar));
	        NewMapOverlay.get(0).add(res.getDrawable(R.drawable.map_8x8_strikeout));
	
	        NewMapOverlay.add(new ArrayList<Drawable>());  // 13x13
	        NewMapOverlay.get(1).add(res.getDrawable(R.drawable.map_13x13_shaddowrect));
	        NewMapOverlay.get(1).add(res.getDrawable(R.drawable.map_13x13_shaddowround));
	        NewMapOverlay.get(1).add(res.getDrawable(R.drawable.map_13x13_shaddowstar));
	        NewMapOverlay.get(1).add(res.getDrawable(R.drawable.map_13x13_strikeout));
	
	        NewMapOverlay.add(new ArrayList<Drawable>());  // 20x20
	        NewMapOverlay.get(2).add(res.getDrawable(R.drawable.map_20x20_shaddowrect));
	        NewMapOverlay.get(2).add(res.getDrawable(R.drawable.map_20x20_selected));
	        NewMapOverlay.get(2).add(res.getDrawable(R.drawable.map_20x20_shaddowrect_deact));
	        NewMapOverlay.get(2).add(res.getDrawable(R.drawable.map_20x20_selected_deact));
    	}
    
    	if (Icons == null)
    	{
	        Icons = new Drawable[] { 
	        		res.getDrawable(N? R.drawable.night_tb : R.drawable.day_tb ),
	        		res.getDrawable(R.drawable.addwaypoint),
	        		res.getDrawable(R.drawable.smilie_gross),
	        		res.getDrawable(R.drawable.download),
	        		res.getDrawable(R.drawable.log1),
	        		res.getDrawable(R.drawable.maintenance),
	        		res.getDrawable(R.drawable.checkbox_checked),
	        		res.getDrawable(R.drawable.checkbox_unchecked),
	        		res.getDrawable(R.drawable.sonne),
	        		res.getDrawable(R.drawable.mond),
	        		res.getDrawable(R.drawable.travelbug),
	        		res.getDrawable(R.drawable.collapse),
	        		res.getDrawable(R.drawable.expand),
	        		res.getDrawable(R.drawable.enabled),
	        		res.getDrawable(R.drawable.disabled),
	        		res.getDrawable(R.drawable.retrieve_tb),
	        		res.getDrawable(R.drawable.drop_tb),
	        		res.getDrawable(R.drawable.star),
	        		res.getDrawable(R.drawable.earth),
	        		res.getDrawable(R.drawable.favorit),
	        		res.getDrawable(R.drawable.file),
	        		res.getDrawable(R.drawable.userdata),
	        		res.getDrawable(R.drawable.delete),  // 22
	        		res.getDrawable(R.drawable.archiv), // 23
	        		res.getDrawable(R.drawable.not_available),  // 24
	        		res.getDrawable(R.drawable.checkbox_crossed), // 25
	        		res.getDrawable(R.drawable.map22), // 26
	        		res.getDrawable(R.drawable.chk_icon), // 27
	        		res.getDrawable(R.drawable.delete_icon), // 28
	        		res.getDrawable(R.drawable.voice_rec_icon), // 29
	        		res.getDrawable(R.drawable.satellite), // 30
	        		res.getDrawable(R.drawable.close_icon), // 31
	        		res.getDrawable(R.drawable.info_icon), // 32
	        		res.getDrawable(R.drawable.warning_icon), // 33
	        		res.getDrawable(R.drawable.help_icon), // 34
	        };
    	}
        
    	if (Arrows == null)
    	{
	        Arrows = new Drawable[] { 
	        		res.getDrawable(R.drawable.arrow),
	        		res.getDrawable(R.drawable.arrow_small),
	        };        		

    	}

    	if (SmallStarIcons == null)
    	{
	        SmallStarIcons = new Drawable[] { 
	        		res.getDrawable(R.drawable.smallstars_0),
	        		res.getDrawable(R.drawable.smallstars_0_5),
	        		res.getDrawable(R.drawable.smallstars_1),
	        		res.getDrawable(R.drawable.smallstars_1_5),
	        		res.getDrawable(R.drawable.smallstars_2),
	        		res.getDrawable(R.drawable.smallstars_2_5),
	        		res.getDrawable(R.drawable.smallstars_3),
	        		res.getDrawable(R.drawable.smallstars_3_5),
	        		res.getDrawable(R.drawable.smallstars_4),
	        		res.getDrawable(R.drawable.smallstars_4_5),
	        		res.getDrawable(R.drawable.smallstars_5) };
    	}

    	if (StarIcons == null)
    	{
	        StarIcons = new Drawable[] { 
	        		res.getDrawable(R.drawable.stars0),
	        		res.getDrawable(R.drawable.stars0_5),
	        		res.getDrawable(R.drawable.stars1),
	        		res.getDrawable(R.drawable.stars1_5),
	        		res.getDrawable(R.drawable.stars2),
	        		res.getDrawable(R.drawable.stars2_5),
	        		res.getDrawable(R.drawable.stars3),
	        		res.getDrawable(R.drawable.stars3_5),
	        		res.getDrawable(R.drawable.stars4),
	        		res.getDrawable(R.drawable.stars4_5),
	        		res.getDrawable(R.drawable.stars5) };
    	}

    	if (SizeIcons == null)
    	{
	        SizeIcons = new Drawable[] 
	                { 
	        		res.getDrawable(R.drawable.other),
	        		res.getDrawable(R.drawable.micro),
	        		res.getDrawable(R.drawable.small),
	        		res.getDrawable(R.drawable.regular),
	        		res.getDrawable(R.drawable.large)
	        		};
    	}
       
    	if (BatteryIcons == null)
    	{
	        BatteryIcons = new Drawable[] { 
	        		res.getDrawable(R.drawable.bat0),
	        		res.getDrawable(R.drawable.bat1),
	        		res.getDrawable(R.drawable.bat2),
	        		res.getDrawable(R.drawable.bat3),
	        };
    	}
        		

    	if (CacheIconsBig == null)
    	{
	        CacheIconsBig = new Drawable[] { 
	        		res.getDrawable(R.drawable.big_0),
	        		res.getDrawable(R.drawable.big_1),
	        		res.getDrawable(R.drawable.big_2),
	        		res.getDrawable(R.drawable.big_3),
	        		res.getDrawable(R.drawable.big_4),
	        		res.getDrawable(R.drawable.big_5),
	        		res.getDrawable(R.drawable.big_6),
	        		res.getDrawable(R.drawable.big_7),
	        		res.getDrawable(R.drawable.big_8),
	        		res.getDrawable(R.drawable.big_9),
	        		res.getDrawable(R.drawable.big_10),
	        		res.getDrawable(R.drawable.big_11),
	        		res.getDrawable(R.drawable.big_12),
	        		res.getDrawable(R.drawable.big_13),
	        		res.getDrawable(R.drawable.big_14),
	        		res.getDrawable(R.drawable.big_15),
	        		res.getDrawable(R.drawable.big_16),
	        		res.getDrawable(R.drawable.big_17),
	        		res.getDrawable(R.drawable.big_18),
	        		res.getDrawable(R.drawable.big_19),
	        };
    	}
        

    	if (LogIcons == null)
    	{
	        LogIcons = new Drawable[] { 
	        		res.getDrawable(R.drawable.log0),
	        		res.getDrawable(R.drawable.log1),
	        		res.getDrawable(R.drawable.log2),
	        		res.getDrawable(R.drawable.log3),
	        		res.getDrawable(R.drawable.log4),
	        		res.getDrawable(R.drawable.log5),
	        		res.getDrawable(R.drawable.log6),
	        		res.getDrawable(R.drawable.log7),
	        		res.getDrawable(R.drawable.log8),
	        		res.getDrawable(R.drawable.log9),
	        		res.getDrawable(R.drawable.log10),
	        		res.getDrawable(R.drawable.log11),
	        		res.getDrawable(R.drawable.log12),
	        		res.getDrawable(R.drawable.log13),
	
	        };
    	}

    	if (ChkIcons == null)
    	{
	        ChkIcons = new Drawable[] 
	                { 
		        		res.getDrawable(N? R.drawable.night_btn_check_off : R.drawable.day_btn_check_off  ),
		        		res.getDrawable(N? R.drawable.night_btn_check_on : R.drawable.day_btn_check_on  ),
	        		};
    	}

    	if (BtnIcons == null)
    	{
	        BtnIcons = new Drawable[] 
	                                { 
	                	        		res.getDrawable(N? R.drawable.night_btn_default_normal : R.drawable.day_btn_default_normal ),
	                	        		
	                        		};
    	}
        
    }

    static String FormatDM(double coord, String positiveDirection, String negativeDirection)
    {
        int deg = (int)coord;
        double frac = coord - deg;
        double min = frac * 60;

        String result = Math.abs(deg) + "° " + String.format("%.3f", Math.abs(min));

        if (coord < 0)
            result += negativeDirection;
        else
            result += positiveDirection;

        return result;
    }


    public static String FormatLatitudeDM(double latitude) {
        return FormatDM(latitude, "N", "S");
	}
    
	public static String FormatLongitudeDM(double longitude) {
        return FormatDM(longitude, "E", "W");
	}

    public static String Rot13(String message)
    {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lookup = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM";

        String result = "";

        for (int i = 0; i < message.length(); i++)
        {
            String curChar = message.substring(i, i+1);
            int idx = alphabet.indexOf(curChar);

            if (idx < 0)
                result += curChar;
            else
                result += lookup.substring(idx, idx+1);
        }

        return result;
    }


    
    static TypedArray themeStyles;
    final private static int[] colorAttrs = new int[]
       {R.attr.ListBackground, R.attr.ListBackground_secend, 
    	R.attr.ListBackground_select,R.attr.myBackground,R.attr.ListSeparator,
    	R.attr.TextColor,R.attr.EmptyBackground,
    	R.attr.ToggleBtColor_off,R.attr.ToggleBtColor_on,R.attr.SlideDownColorFilter,
    	R.attr.SlideDownBackColor,R.attr.LinkLabelColor 
       };
    
    public static void initTheme(Context context)
    {
    	Theme t = context.getTheme();
    	Arrays.sort(colorAttrs);
    	themeStyles = t.obtainStyledAttributes(colorAttrs);
    }
    
    public static int getColor(int attrResid) 
    {
    	return (int)themeStyles.getColor(Arrays.binarySearch(colorAttrs,attrResid), 0);
    }
	

    static class LockClass { };
    static LockClass lockObject = new LockClass();
    
	
    /**
     * Schreibt einen Log Eintrag in die debug.txt, wenn Global.Debug == true!
     * @param line Meldung die gelogt werden soll.
     */
    public static void AddLog(String line)
    {
    	if (!Debug)
    		return;
        synchronized (lockObject)
        {
        	File file = new File(Config.WorkPath + "/debug.txt");
        	FileWriter writer;
        	try {
				writer = new FileWriter(file, true);
				writer.write(new Date().toLocaleString() + " - " + line + "\n");
	            writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    
    /**
     * Schreibt einen Log Eintrag in die debug.txt, wenn Global.Debug == true!
     * @param line Meldung die gelogt werden soll.
     * @param withMsgBox wenn true, wird die Meldung zusätzlich in einer MessageBox ausgegeben. 
     */
    public static void AddLog(String line, Boolean withMsgBox)
    {
    	if (!Debug)
    		return;
    	 AddLog(line);
    	 
    	 if(withMsgBox)
    		 MessageBox.Show(line, "ERROR", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
    }
    
    public static void PlaySound(String soundFile)
    {
    	if (!Config.GetBool("PlaySounds"))
    		return;
    	MediaPlayer mp = new MediaPlayer();
        mp.setOnPreparedListener(new OnPreparedListener() { 
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        try 
        {
			mp.setDataSource(Config.WorkPath + "/data/sound/" + soundFile);
            mp.prepare();
		} catch (Exception e) {
			Global.AddLog("Error PlaySound: " + Config.WorkPath + "/data/sound/" + soundFile + " - " + e.getMessage());
			e.printStackTrace();
		}    	
    }
    
    public static String getVersionString()
    {
    	final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  " 
    	+ (VersionPrefix.equals("")? "" : "(" + VersionPrefix + ")");
    	return ret;
    }

    
}