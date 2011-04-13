package de.droidcachebox;

import java.io.File;
import java.util.ArrayList;

import de.droidcachebox.Events.SelectedCacheEventList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;


public class Global {
    public static final int CurrentRevision = 1;
    public static final int LatestDatabaseChange = 1002;
    public static final int LatestDatabaseFieldNoteChange = 1001;
    
    public static Coordinate Marker = new Coordinate(48.12425, 12.16460);

    // Icons
    public static Drawable[] Icons = null;
    public static Drawable[] SmallStarIcons;
    // New Map Icons
    public static ArrayList<ArrayList<Drawable>> NewMapIcons = new ArrayList<ArrayList<Drawable>>();
    public static ArrayList<ArrayList<Drawable>> NewMapOverlay = new ArrayList<ArrayList<Drawable>>();
    
    
    /// <summary>
    /// Letzte bekannte Position
    /// </summary>
    public static Coordinate LastValidPosition = new Coordinate();
    public static Location Location = null;

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

    // Colors
    public static int TitleBarColor = Color.parseColor("#8B9D5F");
    public static int TitleBarText = Color.BLACK;
    public static int ListBackground = Color.parseColor("#F4F7ED");
    public static int ListSeperator = Color.parseColor("#8B9D5F");
    public static int EmptyBackground = Color.parseColor("#CAD4B2");
    // Paints
    public static Paint[] ListItemBackgroundPaint = new Paint[2];
    public static Paint[] ListItemTextPaint = new Paint[2];
    public static void InitPaints()
    {
    	// Hintergrund für Listen
    	// nicht selected
    	ListItemBackgroundPaint[0] = new Paint();
    	ListItemBackgroundPaint[0].setColor(Color.parseColor("#FFFFFF"));
    	// selected
    	ListItemBackgroundPaint[1] = new Paint();
    	ListItemBackgroundPaint[1].setColor(Color.parseColor("#FFD240"));
    	// Text für Listen
    	// nicht selected
    	ListItemTextPaint[0] = new Paint();
    	ListItemTextPaint[0].setColor(Color.BLACK);
    	ListItemTextPaint[0].setAntiAlias(true);
    	ListItemTextPaint[0].setTextSize(24);
    	// selected
    	ListItemTextPaint[1] = new Paint();
    	ListItemTextPaint[1].setColor(Color.BLACK);
    	ListItemTextPaint[1].setAntiAlias(true);
    	ListItemTextPaint[1].setTextSize(24);
    	ListItemTextPaint[1].setFakeBoldText(true);
    	
    	
    }

    /// <summary>
    /// Zeichnet das Bild und skaliert es proportional so, dass es die
    /// übergebene füllt.
    /// </summary>
    /// <param name="graphics"></param>
    /// <param name="image"></param>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="height"></param>
    public static int PutImageTargetHeight(Canvas canvas, Drawable image, int x, int y, int height)
    {
        float scale = (float)height / (float)image.getBounds().height();
        int width = (int)Math.round(image.getBounds().width() * scale);

        Rect oldBounds = image.getBounds();
        image.setBounds(x, y, x + width, y + height);
        image.draw(canvas);
        image.setBounds(oldBounds);

        return width;
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
    
    public static boolean FileExists(String filename)
    {
    	File file = new File(filename);
    	return file.exists();
    }

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
    public static void InitIcons(Context context)
    {
    	Resources res = context.getResources();
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
    
        Icons = new Drawable[] { 
        		res.getDrawable(R.drawable.bug),
        		res.getDrawable(R.drawable.addwaypoint),
        		res.getDrawable(R.drawable.icon_smile),
        		res.getDrawable(R.drawable.download),
        		res.getDrawable(R.drawable.icon_sad),
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
};

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
}
