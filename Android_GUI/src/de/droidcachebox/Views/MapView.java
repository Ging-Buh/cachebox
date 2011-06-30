package de.droidcachebox.Views;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import CB_Core.FileIO;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import CB_Core.Enums.CacheTypes;


import de.droidcachebox.Geocaching.MysterySolution;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;
import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.TrackRecorder;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.Events.CachListChangedEventList;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Descriptor.PointD;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Map.Manager;
import de.droidcachebox.Map.RouteOverlay;
import de.droidcachebox.Map.Tile;

public class MapView extends RelativeLayout implements SelectedCacheEvent, PositionEvent, ViewOptionsMenu, de.droidcachebox.Events.CacheListChangedEvent {
	private boolean isVisible;  // true, when MapView is visible
	private Timer zoomScaleTimer;
	private TimerTask zoomTimerTask;
	private SurfaceView surface;
	private ZoomControls zoomControls;
	private MultiToggleButton buttonTrackPosition;
	private String debugString1 = "";
	private String debugString2 = "";
	private ActivityManager activityManager;
	private long available_bytes;
	
	private float rangeFactorTiles = 1.0f;
	private float rangeFactorTrack = 1.0f;
	
	private Context myContext;
	AnimationThread animationThread;
	private Lock animationLock = new ReentrantLock();
	
	// 0 -> frei
	// 1 -> Position auf GPS gelockt
	// 2 -> Position auf GPS gelockt und Touch abgeschaltet
	int lockPosition;
	
	boolean useLockPosition;
	/**
	 * Constructor
	 */
	SurfaceHolder holder;
	public MapView(Context context, LayoutInflater inflater) {
		super(context);
		lockPosition = 0;
		useLockPosition = true;
		myContext = context;
		
		Global.SmoothScrolling = SmoothScrollingTyp.valueOf(Config.GetString("SmoothScrolling"));

		activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		available_bytes = activityManager.getMemoryClass();
		if (available_bytes > 31)
		{
			// Ger�te mit mindestens 32MB verf�gbar 
			rangeFactorTiles = 1.5f;
			numMaxTiles = 48;
			numMaxTrackTiles = 24;
		}
		if (available_bytes < 20)
		{
			// Ger�te mit nur 16MB verf�gbar
			// Minimalausstattung verwenden
			rangeFactorTiles = 1.0f;
			numMaxTiles = 12;
			numMaxTrackTiles = 12;
		}
		
		RelativeLayout mapviewLayout = (RelativeLayout)inflater.inflate(R.layout.mapview, null, false);
		this.addView(mapviewLayout);

		surface = (SurfaceView) findViewById(R.id.mapview_surface);

		setWillNotDraw(false);
		
		holder = surface.getHolder();

		animationThread = new AnimationThread();
//		animationThread.start();
		
		buttonTrackPosition = (MultiToggleButton) findViewById(R.id.mapview_trackposition);
		buttonTrackPosition.clearStates();
		buttonTrackPosition.addState("Free", Color.GRAY);
		buttonTrackPosition.addState("GPS", Color.GREEN);
		buttonTrackPosition.addState("FIX", Color.RED);
        this.buttonTrackPosition.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	buttonTrackPosition.onClick(v);
            	setLockPosition(buttonTrackPosition.getState());
            }
          });
		
		zoomControls = (ZoomControls) findViewById(R.id.mapview_zoom);
        zoomControls.setOnZoomInClickListener(new ZoomControls.OnClickListener() 
        { 
        	public void onClick(View v) {
        		zoomIn();
        	} 
        	
        }); 
        /* zoom controls out */ 
        zoomControls.setOnZoomOutClickListener(new ZoomControls.OnClickListener() 
        { 
        	public void onClick(View v) {
        		zoomOut();
        	} 
        	
        });		
        
        ArrayList<android.view.View> buttons = new ArrayList<android.view.View>();
        this.addTouchables(buttons);
        scale = getContext().getResources().getDisplayMetrics().density;
        
        font.setTextSize(14 * scale);
        font.setFakeBoldText(true);
        font.setAntiAlias(true);
        fontSmall.setTextSize(12 * scale);
        fontSmall.setFakeBoldText(true);
        fontSmall.setAntiAlias(true);
        PositionEventList.Add(this);
		CachListChangedEventList.Add(this);
        SelectedCacheEventList.Add(this);

        zoomScaleTimer = new Timer();
        
	}
	
	final float scale;

	private void setLockPosition(int value)
	{
		lockPosition = value;

		if (lockPosition > 0)
		{
    	    if (Global.Marker.Valid)
            {
    	    	startAnimation(new Coordinate(Global.Marker.Latitude, Global.Marker.Longitude));
    	    	return;
            }
            if (Global.LastValidPosition != null && Global.LastValidPosition.Valid)
            {
    	    	startAnimation(Global.LastValidPosition);
    	    	return;
            }		
		}
		else
		{
			buttonTrackPosition.setState(0);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		try
		{
			Render(true);
		} catch (Exception exc)
		{
			Logger.Error("MapView.onDraw", "", exc);
			return;
		}
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        halfWidth = w / 2;
        halfHeight = h / 2;

        
        scaleLeft = 0;// button2.Left + button2.Width + lineHeight;
        scaleWidth = width - scaleLeft - (int)font.measureText("100km ") + 1;
        

        offScreenBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        canvas = new Canvas(offScreenBmp);
        canvasOverlay = canvas;
        
        canvasHeading = 0;
        drawingWidth = width;
        drawingHeight = height;
//        canvas.rotate(45, width / 2, height / 2);

        zoomChanged();
    }

   
   
    private boolean multiTouch = false;
    private double lastMultiTouchDist = 0;
    private double multiTouchFaktor = 1;
    private Point mouseDownPos;
    private Point lastMousePos;
    private Point[] lastMouseDiff = new Point[5];
    private boolean mouseMoved;
    private long lastMouseMoveTick;
    private long[] lastMouseMoveTickDiff = new long[5];
    private int lastMouseMoveTickPos = 0;
    private int lastMouseMoveTickCount = 0;
    
    private Point rotate(Point p, float heading)
    {
    	float[] src = {p.x, p.y};
        float[] dist = new float[2];
        Matrix mat = new Matrix();
    	mat.setRotate(heading, width / 2, halfHeight);
    	mat.mapPoints(dist, src);
    	return new Point((int)dist[0], (int)dist[1]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (lockPosition == 2) 
    		return true;

    	int eX = 0;
    	int eY = 0;
    	try
    	{
	    	eX = (int)event.getX(0);
	    	eY = (int)event.getY(0);
	    	// bei gedrehter Map hier die Punkte drehen
	
	    	animationThread.stopMove();
	    	
	    	if (alignToCompass)
	    	{
		    	Point rot = rotate(new Point(eX, eY), canvasHeading);
		    	eX = rot.x;
		    	eY = rot.y;
	    	}
	    	
	        if (event.getPointerCount() > 1)
	        {
	        	int eX2 = (int)event.getX(1);
	        	int eY2 = (int)event.getY(1);
	        	if (alignToCompass)
	        	{
		        	Point rot = rotate(new Point(eX2, eY2), canvasHeading);
		        	eX2 = rot.x;
		        	eY2 = rot.y;
	        	}
	        	
	        	
	        	
	        	double multiTouchDist = Math.sqrt(Math.pow(eX2 - eX, 2) + Math.pow(eY2 - eY, 2));
	        	if (!multiTouch)
	        		lastMultiTouchDist = multiTouchDist;
	//        debugString1 = "" + multiTouchDist;
	//        debugString2 = "" + lastMultiTouchDist;
	        	if (Zoom >= maxZoom)
	        	{
	        		if (multiTouchDist > lastMultiTouchDist)
	        			multiTouchDist = lastMultiTouchDist;
	        	}
	        	if (Zoom <= minZoom)
	        	{
	        		if (multiTouchDist < lastMultiTouchDist)
	        			multiTouchDist = lastMultiTouchDist;
	        	
	        	}
	
	        	if (multiTouch)
	        	{
	        		if (lastMultiTouchDist > multiTouchDist * 1.5)
	        		{
	        			zoomOutDirect(false);
	       				lastMultiTouchDist /= 2; //multiTouchDist;
	        		}
	        		else if (lastMultiTouchDist < multiTouchDist * 0.75)
	        		{
	        			zoomInDirect(false);
	               		lastMultiTouchDist *= 2; //multiTouchDist;
	        		}
	        	} else
	    			lastMultiTouchDist = multiTouchDist;
	
	        	
	        	if (lastMultiTouchDist > 0)
	        		multiTouchFaktor = multiTouchDist / lastMultiTouchDist;
	        	else 
	        		multiTouchFaktor = 1;
	//        	debugString1 = "f: " + multiTouchFaktor;
	        	
	        	eX = (int)(eX + (eX2 - eX) / 2);
	        	eY = (int)(eY + (eY2 - eY) / 2);
	        	if (!multiTouch)
	        	{
	        		dragStartX = lastClickX = eX;
	        		dragStartY = lastClickY = eY;
	        	}
	        	multiTouch = true;
	        	mouseMoved = true;
	        } else
	        {
	        	if(multiTouch)
	        	{
	        		dragStartX = lastClickX = eX;
	        		dragStartY = lastClickY = eY;        		
	        	}
	        	multiTouch = false;
	        	if (renderZoomScaleActive)
	        		startZoomScaleTimer();
	        }
		} finally
		{
		}
	        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
		    	synchronized (screenCenter)
		    	{
	            	lastMouseMoveTickPos = 0;
	            	lastMouseMoveTick = SystemClock.uptimeMillis();
	            	lastMouseMoveTickDiff[lastMouseMoveTickPos] = 0;
	            	lastMousePos = new Point(eX, eY);
	            	lastMouseDiff[lastMouseMoveTickPos] = new Point(0, 0);
	            	lastMouseMoveTickCount = 0;
	            	mouseDownPos = new Point(eX, eY);
	            	mouseMoved = false;
	            	// Nachlauf stoppen, falls aktiv
	
	            	Coordinate coord = null;
            		coord = new Coordinate(Descriptor.TileYToLatitude(Zoom, screenCenter.Y / (256.0)), Descriptor.TileXToLongitude(Zoom, screenCenter.X / (256.0)));
	
	        		animationThread.moveTo(coord, smoothScrolling.AnimationSteps()*2, false);
	            	
	            	MapView_MouseDown(eX, eY);
		    	}
//                touch_start(x, y);
//                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
		    	synchronized (screenCenter)
		    	{
	            	lastMouseMoveTickDiff[lastMouseMoveTickPos] = SystemClock.uptimeMillis() - lastMouseMoveTick;
	            	lastMouseMoveTick = SystemClock.uptimeMillis();
	            	lastMouseDiff[lastMouseMoveTickPos] = new Point(eX - lastMousePos.x, eY - lastMousePos.y);
	            	lastMouseMoveTickPos++;
	            	lastMouseMoveTickCount++;
	            	if (lastMouseMoveTickPos > 4)
	            		lastMouseMoveTickPos = 0;
	            	lastMousePos = new Point(eX, eY);
	            	if (!mouseMoved)
	            	{
	            		Point akt = new Point(eX, eY);
	            		mouseMoved = ((Math.abs(akt.x - mouseDownPos.x) > 5) || Math.abs(akt.y - mouseDownPos.y) > 5);
	            	}
	            	if (mouseMoved)
	            		MapView_MouseMove(eX, eY);
	//                touch_move(x, y);
	//                invalidate();
		    	}
                break;
            case MotionEvent.ACTION_UP:
//            	multiTouchFaktor = 1;
            	if ((multiTouchFaktor < 0.99) || (multiTouchFaktor > 1.01))
            		animationThread.zoomTo(Zoom);
            	synchronized (screenCenter)
            	{
	            	if (mouseMoved)
	            	{
	//            		MapView_MouseUp(eX, eY);
	            		// Nachlauf der Map
	            		double dx = 0;
	            		double dy = 0;
	            		double dt = 0;
	            		int count = Math.min(5, lastMouseMoveTickCount);
	            		if (Global.SmoothScrolling != SmoothScrollingTyp.none)
	            		{
		            		int newPosFaktor = 5 * 50 / smoothScrolling.AnimationWait();
		            		for (int i = 0; i < count; i++)
		            		{
		            			dx += lastMouseDiff[i].x;
		            			dy += lastMouseDiff[i].y;
		            			dt += lastMouseMoveTickDiff[i];
		            		}
		            		dx /= count;
		            		dy /= count;
		            		dt /= count;
		            		PointD nachlauf = new PointD(0, 0);
		            		try
		            		{
		            			nachlauf = new PointD(screenCenter.X, screenCenter.Y);
		            		} finally
		            		{
		            		}
		            		nachlauf.X -= dx * newPosFaktor / dt * smoothScrolling.AnimationWait();
		            		nachlauf.Y -= dy * newPosFaktor / dt * smoothScrolling.AnimationWait();
		            		Coordinate coord = new Coordinate(Descriptor.TileYToLatitude(Zoom, nachlauf.Y / (256.0)), Descriptor.TileXToLongitude(Zoom, nachlauf.X / (256.0)));
		            		mouseMoved = false;
		            		animationThread.moveTo(coord, smoothScrolling.AnimationSteps()*2, false);
	            		} else
	                		MapView_MouseUp(eX, eY);
	
	            	}
	            	else
	            	{
	            		// click!!!!
	            		MapView_Click(eX, eY);
	                	mouseDownPos = null;
	            		return false;
	            	}
	            	mouseDownPos = null;
	//                touch_up();
	//                invalidate();
		    	}
                break;
        }
        return true;
    }

/*    public delegate void TileLoadedHandler(Bitmap bitmap, Descriptor desc);
    public event TileLoadedHandler OnTileLoaded = null;*/

    /// <summary>
    /// Aktuell betrachteter Layer
    /// </summary>
    public Layer CurrentLayer = null;
    public void SetCurrentLayer(Layer newLayer)
    {
      Config.Set("CurrentMapLayer", newLayer.Name);
      Config.AcceptChanges();

      CurrentLayer = newLayer;

      loadedTilesLock.lock();
      try
      {
        ClearCachedTiles();
      } finally
      {
    	  loadedTilesLock.unlock();
      }
      Render(true);
    }
    /// <summary>
    /// Tile Manager
    /// </summary>
    public static Manager Manager = new Manager();

    /// <summary>
    /// Wunschzettel. Diese Deskriptoren werden von loaderthread geladen
    /// und instanziiert
    /// </summary>
    ArrayList<Descriptor> wishlist = new ArrayList<Descriptor>();
    private Lock wishlistLock = new ReentrantLock();

    /// <summary>
    /// Instanz des Loaders
    /// </summary>
    loaderThread loaderThread = null;

    /// <summary>
    /// Liste mit den darzustellenden Wegpunkten
    /// </summary>
    ArrayList<WaypointRenderInfo> wpToRender = new ArrayList<WaypointRenderInfo>();

    /// <summary>
    /// Speichert die Informationen f�r einen im Sichtfeld befindlichen
    /// Waypoint
    /// </summary>
    class WaypointRenderInfo
    {
      public double MapX;
      public double MapY;
      public Drawable Icon;
      public Drawable OverlayIcon;
      public Drawable UnderlayIcon;
      public Cache Cache;
      public Waypoint Waypoint;
      public boolean Selected;
    };

    /// <summary>
    /// true, falls das Rating der Caches angezeigt werden soll
    /// </summary>
    boolean showRating = true;

    boolean showDT = true;

    /// <summary>
    /// true, falls die WP-Beschriftungen gezeigt werden sollen
    /// </summary>
    boolean showTitles = true;

    /// <summary>
    /// true, falls bei Mysterys mit L�sung (Final Waypoint) der Cache ausgeblendet werden soll, wenn der Cache nicht selected ist.
    /// </summary>
    boolean hideCacheWithFinal = true;

    /// <summary>
    /// true, falls der kleine Kompass open angezeigt werden soll
    /// </summary>
    boolean showCompass = true;

    /// <summary>
    /// true, falls die Map-Anzeige am Compass ausgerichtet werden soll
    /// </summary>
    boolean alignToCompass = false;
    
    /// <summary>
    /// Spiegelung des Logins bei Gc, damit ich das nicht dauernd aus der
    /// Config lesen muss.
    /// </summary>
    String gcLogin = "";

    /// <summary>
    /// true, falls Center g�ltige Koordinaten enth�lt
    /// </summary>
    boolean positionInitialized = false;

    boolean hideMyFinds = false;

    public Coordinate center = new Coordinate(48.0, 12.0);

    PointD centerOsmSpace = new PointD(0, 0);
/*
    ///// <summary>
    ///// Wegpunkt des Markers. Wird bei go to als Ziel gesetzt
    ///// </summary>
    //Waypoint markerWaypoint = new Waypoint("MARKER", CacheTypes.ReferencePoint, "Marker", 0, 0, 0);
*/
    /// <summary>
    /// Der Kartenmittelpunkt. Wird dieser Wert �berschrieben wird die
    /// Liste sichtbarer Caches entsprechend aktualisiert.
    /// </summary>
    public Coordinate getCenter()
    {
        return new Coordinate(Descriptor.TileYToLatitude(Zoom, centerOsmSpace.Y / (256.0)), Descriptor.TileXToLongitude(Zoom, centerOsmSpace.X / (256.0)));
    }
    public void setCenter(Coordinate value)
    {
    	synchronized (screenCenter)
    	{
	    	
	    	if (center == null)
	    		center = new Coordinate(48.0, 12.0);
	        positionInitialized = true;
	/*
	        if (animationTimer != null)
	          animationTimer.Enabled = false;
	*/
	        if (center == value)
	          return;
	
	        center = value;
	        centerOsmSpace = Descriptor.ToWorld(Descriptor.LongitudeToTileX(Zoom, center.Longitude), Descriptor.LatitudeToTileY(Zoom, center.Latitude), Zoom, Zoom);
	        screenCenter.X = Math.round(centerOsmSpace.X * dpiScaleFactorX);
	        screenCenter.Y = Math.round(centerOsmSpace.Y * dpiScaleFactorY);
	        if (animationThread != null)
	        {
	        	animationThread.toX = screenCenter.X;
	        	animationThread.toY = screenCenter.Y;
	        }
		}

        updateCacheList();
    }

    protected PointD screenCenter = new PointD(0, 0);

    protected Canvas canvas = null;
    protected Canvas canvasOverlay = null;
    protected float canvasHeading = 0;
    protected int drawingWidth = 0;
    protected int drawingHeight = 0;

    public int Zoom = 14;

    /// <summary>
    /// Hashtabelle mit geladenen Kacheln
    /// </summary>
    protected Hashtable<Long, Tile> loadedTiles = new Hashtable<Long, Tile>();
    final Lock loadedTilesLock = new ReentrantLock();
    

    /// <summary>
    /// Hashtabelle mit Kacheln der GPX Tracks zum Overlay �ber die MapTiles
    /// </summary>
    protected Hashtable<Long, Tile> trackTiles = new Hashtable<Long, Tile>();
    final Lock trackTilesLock = new ReentrantLock();

    /// <summary>
    /// Horizontaler Skalierungsfaktor bei DpiAwareRendering
    /// </summary>
    protected float dpiScaleFactorX = 1;

    /// <summary>
    /// Vertikaler Skalierungsfaktor bei DpiAwareRendering
    /// </summary>
    protected float dpiScaleFactorY = 1;

    // TODO: Dies schlau berechnen!
    /// <summary>
    /// Gr��e des Kachel-Caches
    /// </summary>
    private int numMaxTiles = 24;
    private int numMaxTrackTiles = 12;

    // Vorberechnete Werte
    protected int halfWidth = 0;
    protected int halfHeight = 0;
    protected int width = 0;
    protected int height = 0;
    protected int halfIconSize = 10;

    protected int lineHeight = 24;
    protected int smallLineHeight = 16;

    double adjustmentCurrentToCacheZoom = 1;
/*
    ClickContext mapMenu = null;
    ClickContext markerMenu = null;
    ClickContext layerMenu = null;
    ClickContext routeMenu = null;
    ClickContext viewMenu = null;

    ClickButton removeMarkerButton = null;
    ClickButton hideFindsButton = null;
    ClickButton showRatingButton = null;
    ClickButton showDTButton = null;
    ClickButton showTitlesButton = null;
    ClickButton showCompassButton = null;
    ClickButton nightmodeButton = null;

    List<ClickButton> layerButtons = new List<ClickButton>();
*/    
    int mapMaxCachesLabel = 12;
    int mapMaxCachesDisplay = 10000;
    int mapMaxCachesDisplayLarge = 75;
    int zoomCross = 16;

    boolean nightMode = false;
/*
    public MapView()
    {
      View = this;
      InitializeComponent();
    }
*/    
    public void Initialize()
    {
    	backBrush = new Paint();
    	backBrush.setColor(Color.argb(255, 201, 233, 203));
/*      MouseWheel += new MouseEventHandler(MapView_MouseWheel);

      mapMaxCachesLabel = Config.GetInt("MapMaxCachesLabel");

      showRating = Config.GetBool("MapShowRating");
      showDT = Config.GetBool("MapShowDT");
      showTitles = Config.GetBool("MapShowTitles");
      hideMyFinds = Config.GetBool("MapHideMyFinds");
      showCompass = Config.GetBool("MapShowCompass");
      nightMode = Config.GetBool("nightMode");

      Global.TargetChanged += new Global.TargetChangedHandler(OnTargetChanged);

      lineHeight = (int)this.CreateGraphics().MeasureString("M", Font).Height;
      smallLineHeight = (int)this.CreateGraphics().MeasureString("M", fontSmall).Height;

      mapMenu = new ClickContext(this);
      mapMenu.Add(new ClickButton("Layer", null, showLayerMenu, null, null), false);
      mapMenu.Add(new ClickButton("Center Point", null, showMarkerMenu, null, null), false);
      mapMenu.Add(new ClickButton("Route", null, showRouteMenu, null, null), false);
      mapMenu.Add(new ClickButton("View", null, showViewMenu, null, null), false);
      viewMenu = new ClickContext(this);
      viewMenu.Add(hideFindsButton = new ClickButton("Hide finds", (hideMyFinds) ? Global.Icons[6] : Global.Icons[7], hideFinds, null, null), false);
      viewMenu.Add(showRatingButton = new ClickButton("Show Rating", (showRating) ? Global.Icons[6] : Global.Icons[7], showRatingChanged, null, null), false);
      viewMenu.Add(showDTButton = new ClickButton("Show D/T", (showDT) ? Global.Icons[6] : Global.Icons[7], showDTChanged, null, null), false);
      viewMenu.Add(showTitlesButton = new ClickButton("Show Titles", (showTitles) ? Global.Icons[6] : Global.Icons[7], showTitlesChanged, null, null), false);
      viewMenu.Add(showCompassButton = new ClickButton("Show Compass", (showCompass) ? Global.Icons[6] : Global.Icons[7], showCompassChanged, null, null), false);
      viewMenu.Add(nightmodeButton = new ClickButton("Enable Nightmode", (nightMode) ? Global.Icons[6] : Global.Icons[7], enableNightmodeChanged, null, null), false);
      markerMenu = new ClickContext(this);
      markerMenu.Add(new ClickButton("Set", null, setMarker, null, null), false);
      markerMenu.Add(removeMarkerButton = new ClickButton("Remove", null, removeMarker, null, null, false), false);

      routeMenu = new ClickContext(this);
      routeMenu.Add(new ClickButton("Route to WP", null, showNavigationDialog, null, null), false);
      routeMenu.Add(new ClickButton("Reset Route", null, resetRoute, null, null), false);
      

      
      
      imageAttributes = new ImageAttributes();
      imageAttributes.SetColorKey(Global.SmallStarIcons[0].GetPixel(0, 0), Global.SmallStarIcons[0].GetPixel(0, 0));
      colorKey.SetColorKey(Global.Icons[19].GetPixel(0, 0), Global.Icons[19].GetPixel(0, 0));
*/
      String currentLayerName = Config.GetString("CurrentMapLayer");
      CurrentLayer = Manager.GetLayerByName((currentLayerName == "") ? "Mapnik" : currentLayerName, currentLayerName, "");

//      layerMenu = new ClickContext(this);
/*
      OnTileLoaded += new TileLoadedHandler(MapView_OnTileLoaded);

      if (Config.GetString("RouteOverlay").Length > 0 && File.Exists(Config.GetString("RouteOverlay")))
        Routes.Add(LoadRoute(Config.GetString("RouteOverlay"), new Pen(Color.Purple, 4), Config.GetInt("TrackDistance")));
      else*/

      RouteOverlay.Routes.clear();
/*      
      Paint paint = new Paint();
      paint.setColor(Color.BLUE);
      paint.setStrokeWidth(4);
      
      Global.AktuelleRoute = new RouteOverlay.Route(paint, "actual Track");
      Global.AktuelleRoute.ShowRoute = false;
      RouteOverlay.Routes.add(Global.AktuelleRoute);
*/
      //Load Routes for Autoload
/*
	        File dir = new File(Config.GetString("MapPackFolder"));
	        String[] files = dir.list();
	        if (!(files == null))
	        {
		        if (files.length>0)
		        {
			        for (String file : files)
				        {
				        	MapView.Manager.LoadMapPack(Config.GetString("MapPackFolder") + "/" + file);
				        }
		        }
	        }
      
 */
      String trackPath = Config.GetString("TrackFolder") + "/Autoload";
      if (FileIO.DirectoryExists(trackPath))
      {
    	  File dir = new File(trackPath);
    	  String[] files = dir.list();
    	  if (!(files == null))
    	  {
    		  if (files.length>0)
    		  {
    			  for (String file : files)
    			  {
    				  int[] ColorField = new int[8];
    				  ColorField[0] = Color.RED;
    				  ColorField[1] = Color.YELLOW;
    				  ColorField[2] = Color.BLACK;
    				  ColorField[3] = Color.LTGRAY;
    				  ColorField[4] =  Color.GREEN;
    				  ColorField[5] = Color.BLUE;
    				  ColorField[6] = Color.CYAN;
    				  ColorField[7] = Color.GRAY;
    				  int TrackColor;
    				  TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];
    				  
    				  Paint paint = new Paint();
    				  paint.setColor(TrackColor);
    				  paint.setStrokeWidth(4);
    				  RouteOverlay.Routes.add(RouteOverlay.LoadRoute(trackPath + "/" + file, paint, Config.GetInt("TrackDistance")));
    				  
    			  }
    		  }
    	  }
      }
      else
      {
    	  File sddir = new File(trackPath);
    	  sddir.mkdirs();
      }

    }
/*
    void MapView_MouseWheel(object sender, MouseEventArgs e)
    {
      if (e.Delta > 0)
        zoomIn();
      else
        zoomOut();
    }
*/
    void OnTileLoaded(Bitmap bitmap, Descriptor desc)
    {
//        canvas = new Canvas(bitmap);
//        RouteOverlay.RenderRoute(canvas, bitmap, desc, dpiScaleFactorX, dpiScaleFactorY);
/*
      if (nightMode)
      { 
        unsafe
        {
          Rectangle bounds = new Rectangle(0, 0, bitmap.Size.Width, bitmap.Size.Height);
          BitmapData bitmapData = bitmap.LockBits(bounds, ImageLockMode.ReadWrite, PixelFormat.Format32bppRgb);

          byte* p = (byte*)bitmapData.Scan0.ToPointer();
          for (int i = 0; i < bitmapData.Height * bitmapData.Stride; i++)
          {

            p[i] = (byte)(255 - p[i]);
          };
          bitmap.UnlockBits(bitmapData);
        };
      };
*/

    }

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (Global.autoResort)
			return;
		
		if (cache == null)
			return;
/*
		if (InvokeRequired)
		{
			Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint });
			return;
		}*/
		positionInitialized = true;
		
		setLockPosition(0);
		Coordinate target = (waypoint != null) ? new Coordinate(waypoint.Latitude(), waypoint.Longitude()) : new Coordinate(cache.Latitude(), cache.Longitude());
		startAnimation(target);
		
	}

  /*
    delegate void targetChangedDelegate(Cache cache, Waypoint waypoint);
    void OnTargetChanged(Cache cache, Waypoint waypoint)
    {
      if (Global.autoResort)
        return;

      if (cache == null)
        return;

      if (InvokeRequired)
      {
        Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint });
        return;
      }
      positionInitialized = true;
//      tabButtonTrackPosition.Down = false;
      Coordinate target = (waypoint != null) ? new Coordinate(waypoint.Latitude, waypoint.Longitude) : new Coordinate(cache.Latitude, cache.Longitude);
      startAnimation(target);
    }

    public new void Dispose()
    {
      if (loaderThread != null)
        loaderThread.Abort();

      base.Dispose();
    }
*/
    public void OnShow()
    {	
    	gcLogin = Config.GetString("GcLogin").toLowerCase();
    	isVisible = true;
    	if (!animationThread.isAlive())
    		animationThread.start();
    	Zoom = Config.GetInt("lastZoomLevel");
    	animationThread.zoomTo(Zoom);
    }
    
    public void InitializeMap()
    {
    	gcLogin = Config.GetString("GcLogin").toLowerCase();
    	mapMaxCachesDisplay = 50;
    	mapMaxCachesDisplayLarge = 100;
    	zoomCross = 15;
/*      gcLogin = Config.GetString("GcLogin");
      mapMaxCachesDisplay = Config.GetInt("MapMaxCachesDisplay_config");
      mapMaxCachesDisplayLarge = Config.GetInt("mapMaxCachesDisplayLarge_config");*/
      zoomCross = Config.GetInt("ZoomCross");
    	

//      hideFindsButton.ButtonImage = (hideMyFinds) ? Global.Icons[6] : Global.Icons[7];

      // Bestimmung der ersten Position auf der Karte
      if (!positionInitialized)
      {
        double lat = Config.GetDouble("MapInitLatitude");
        double lon = Config.GetDouble("MapInitLongitude");

        // Initialisierungskoordinaten bekannt und k�nnen �bernommen werden
        if (lat != -1000 && lon != -1000)
        {
          setCenter(new Coordinate(lat, lon));
          positionInitialized = true;
          setLockPosition(0);
        }
        else
        {
          // GPS-Position bekannt?
          if (Global.LastValidPosition.Valid)
          {
            setCenter(new Coordinate(Global.LastValidPosition));
            positionInitialized = true;
          }
          else
          {
            if (Database.Data.Query.size() > 0)
            {
              // Koordinaten des ersten Caches der Datenbank nehmen
              setCenter(new Coordinate(Database.Data.Query.get(0).Latitude(), Database.Data.Query.get(0).Longitude()));
              positionInitialized = true;
              setLockPosition(0);
            }
            else
            {
              // Wenns auch den nicht gibt...)
              setCenter(new Coordinate(48.0, 12.0));
            }
          }
        }

        // Gr��e des Ma�stabes berechnen etc...
        zoomChanged();
      }

      renderZoomScaleActive = false;

      minZoom = Config.GetInt("OsmMinLevel");
      maxZoom = Config.GetInt("OsmMaxLevel");

      // Skalierungsfaktoren bestimmen
      if (Config.GetBool("OsmDpiAwareRendering"))
      {
//          dpiScaleFactorX = dpiScaleFactorY = 1;
/*        dpiScaleFactorX = this.AutoScaleDimensions.Width / 96.0f;
        dpiScaleFactorY = this.AutoScaleDimensions.Height / 96.0f;*/
          dpiScaleFactorX = dpiScaleFactorY = getContext().getResources().getDisplayMetrics().density;
          dpiScaleFactorX = dpiScaleFactorY = 1;
      }
      else
        dpiScaleFactorX = dpiScaleFactorY = 1;

      //redPen = new Pen(Color.Red, (int)(dpiScaleFactorX * 1.4f));

      // Falls DpiAwareRendering ge�ndert wurde, m�ssen diese Werte ent-
      // sprechend angepasst werden.
      synchronized (screenCenter)
      {
	      screenCenter.X = Math.round(centerOsmSpace.X * dpiScaleFactorX);
	      screenCenter.Y = Math.round(centerOsmSpace.Y * dpiScaleFactorY);
	      if (animationThread != null)
	      {
	    	  animationThread.toX = screenCenter.X;
	    	  animationThread.toY = screenCenter.Y;
	      }
      }
      
/*
      halfIconSize = (int)((Global.NewMapIcons[2][0].Height * dpiScaleFactorX) / 2);
*/

      updateCacheList();
    }
/*
    public void OnHide()
    {
      if (this.DesignMode)
        return;
      zoomScaleTimer.Enabled = false;

      if (offScreenBmp != null)
      {
        graphics.Dispose();
        offScreenBmp.Dispose();
      }

      offScreenBmp = null;
    }

*/
    /// <summary>
    /// L�d eine Kachel und legt sie in loadedTiles ab. Implementiert den
    /// WaitCallback-Delegaten
    /// </summary>
    /// <param name="state">Descriptor der zu ladenen Kachel. Typlos, damit
    /// man es als WorkItem queuen kann!</param>
    @SuppressWarnings("unchecked")
	protected void LoadTile(Object state)
    {
    	// damit die Anzahl der loadedTiles wirklich nicht viel gr��er ist als angegeben
    	preemptTile();
    	Descriptor desc = (Descriptor) state;

		Bitmap bitmap = Manager.LoadLocalBitmap(CurrentLayer, desc);
/*      Canvas canv = new Canvas(bitmap);
      RouteOverlay.RenderRoute(canv, bitmap, desc, dpiScaleFactorX, dpiScaleFactorY);*/
      
      
/*      if (bitmap != null)
      {
        // error while painting bitmaps with indexed format (png from Mapnik
        // -> create a copy of the bitmap
        if ((bitmap.PixelFormat == PixelFormat.Format1bppIndexed) || (bitmap.PixelFormat == PixelFormat.Format4bppIndexed) || (bitmap.PixelFormat == PixelFormat.Format8bppIndexed) || (bitmap.PixelFormat == PixelFormat.Indexed))
          bitmap = new Bitmap(bitmap);
      }*/
    	Tile.TileState tileState = Tile.TileState.Disposed;

    	if (bitmap == null)
    	{
    		if (Config.GetBool("AllowInternetAccess"))
    		{
    			wishlistLock.lock();
    			try
    			{
    				wishlist.add(desc);
    				if (loaderThread == null)
    				{
    					loaderThread = new loaderThread();
    					loaderThread.execute(wishlist);
/*
              loaderThread = new Thread(new ThreadStart(loaderThreadEntryPoint));
              loaderThread.Priority = ThreadPriority.BelowNormal;
              loaderThread.Start();*/
    				}
    	        } finally
    	        {
    	      	  wishlistLock.unlock();
    	        }
    		}

        // Upscale coarser map tile
    		bitmap = loadBestFit(CurrentLayer, desc, true);
    		tileState = Tile.TileState.LowResolution;
    	}
    	else
    		tileState = Tile.TileState.Present;
    	
    	if (bitmap == null)
    		return;

/*      if (Config.GetBool("OsmDpiAwareRendering") && (dpiScaleFactorX != 1 || dpiScaleFactorY != 1))
        scaleUpBitmap(ref bitmap);*/

    	addLoadedTile(desc, bitmap, tileState);

/*      if (OnTileLoaded != null)*/
//      OnTileLoaded(bitmap, desc);

      // Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
      // kann man die Karte ja gut mal neu rendern!
    	tilesFinished = true;

    	if (tileVisible(desc))
    		Render(true);
    }

    /// <summary>
    /// Zeichnet eine Kachel mit den Tracks und legt sie in trackTiles ab.
    /// </summary>
    /// <param name="state">Descriptor der zu ladenen Kachel. Typlos, damit
    /// man es als WorkItem queuen kann!</param>
	protected void LoadTrackTile(Descriptor desc)
    {
    	// damit die Anzahl der loadedTiles wirklich nicht viel gr��er ist als angegeben
    	preemptTrackTile();
      Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
      Paint ppp = new Paint();
      ppp.setColor(Color.argb(255, 0, 0, 0));
      ppp.setStyle(Style.FILL);
      
      Canvas canv = new Canvas(bitmap);
//      canvas.drawRect(0, 0, 255, 255, ppp);
      RouteOverlay.RenderRoute(canv, bitmap, desc, dpiScaleFactorX, dpiScaleFactorY);
      
      Tile.TileState tileState = Tile.TileState.Disposed;

      tileState = Tile.TileState.Present;

      if (bitmap == null)
        return;

      addTrackTile(desc, bitmap, tileState);

      // Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
      // kann man die Karte ja gut mal neu rendern!
      tilesFinished = true;

      if (tileVisible(desc))
        Render(true);
    }

    private Bitmap loadBestFit(Layer CurrentLayer, Descriptor desc, boolean loadFromManager)
    {
    	Descriptor available = new Descriptor(desc);

    	// Determine best available tile
    	Bitmap tile = null;
    	if (loadFromManager)
    	{
    		// load Bitmaps from Manager
			do
			{
				available.X /= 2;
				available.Y /= 2;
				available.Zoom--;
				
			} while (available.Zoom >= 0 && (tile = Manager.LoadLocalBitmap(CurrentLayer, available)) == null);
    	} else
    	{
    		// only search in loaded tiles
    		do
    		{
				available.X /= 2;
				available.Y /= 2;
				available.Zoom--;
				loadedTilesLock.lock();
				try
				{
	    			if (loadedTiles.containsKey(available.GetHashCode()))
	    			{
	    				Tile ttile = loadedTiles.get(available.GetHashCode());
	    				tile = ttile.Image;
	    			}
				} finally
				{
					loadedTilesLock.unlock();
				}
    		} while (available.Zoom >= 1 && (tile == null));
    	}
    	// No tile available. Use Background color (so that at least
    	// routes are painted!)
    	Bitmap result = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
    	Canvas graphics = new Canvas(result);
    	
    	if (tile == null)
    	{
    		graphics.drawRect(0, 0, result.getWidth(), result.getHeight(), backBrush);
    	}
    	else
    	{
    		// Load and interpolate
    		int scale = (int)Math.pow(2, desc.Zoom - available.Zoom);
    		int x = available.X * scale;
    		int y = available.Y * scale;
    		int width = 256 / scale;

    		int px = (desc.X - x) * width;
    		int py = (desc.Y - y) * width;
    		graphics.scale(scale, scale);
    		graphics.translate(-px, -py);
    		graphics.drawBitmap(tile, 0, 0, null);    		
    	}
    	
    	return result;
    }
/*
    private void scaleUpBitmap(ref Bitmap bitmap)
    {
      try
      {
        Bitmap dummyBitmap = new Bitmap((int)(256.0f * dpiScaleFactorX), (int)(256.0f * dpiScaleFactorY));
        Graphics dummy = Graphics.FromImage(dummyBitmap);

        dummy.DrawImage(bitmap, new Rectangle(0, 0, dummyBitmap.Width, dummyBitmap.Height), new Rectangle(0, 0, bitmap.Width, bitmap.Height), GraphicsUnit.Pixel);
        bitmap.Dispose();
        bitmap = dummyBitmap;
        dummy.Dispose();
      }
      catch (OutOfMemoryException)
      {
      }
    }
*/
    void addLoadedTile(Descriptor desc, Bitmap bitmap, Tile.TileState state)
    {
    	loadedTilesLock.lock();
    	try
    	{
    		if (loadedTiles.containsKey(desc.GetHashCode()))
    		{
    			// 	Wenn die Kachel schon geladen wurde und die neu zu registrierende Kachel
    			// weniger aktuell ist, behalten wir besser die alte!
    			if (loadedTiles.get(desc.GetHashCode()).State == Tile.TileState.Present && state != Tile.TileState.Present)
    				return;
    			
    			Tile tile = loadedTiles.get(desc.GetHashCode()); 
    			if (tile.Image != null)
    			{
    				tile.Image.recycle();
    				tile.Image = null;
    			}
    			
    			tile.State = state; // (bitmap != null) ? Tile.TileState.Present : Tile.TileState.Disposed;
    			tile.Image = bitmap;
    		}
    		else
    		{
    			Tile tile = new Tile(desc, bitmap, state);
    			loadedTiles.put(desc.GetHashCode(), tile);
    		}
    	} finally
    	{
    		loadedTilesLock.unlock();
    	}
    }

    void addTrackTile(Descriptor desc, Bitmap bitmap, Tile.TileState state)
    {
    	trackTilesLock.lock();
    	try
    	{
    		if (trackTiles.containsKey(desc.GetHashCode()))
    		{
    			// Wenn die Kachel schon geladen wurde und die neu zu registrierende Kachel
    			// weniger aktuell ist, behalten wir besser die alte!
    			if (trackTiles.get(desc.GetHashCode()).State == Tile.TileState.Present && state != Tile.TileState.Present)
    				return;
    			
    			Tile tile = trackTiles.get(desc.GetHashCode());
    			if (tile.Image != null)
    			{
    				tile.Image.recycle();
    				tile.Image = null;
    			}
    			
    			tile.State = state; // (bitmap != null) ? Tile.TileState.Present : Tile.TileState.Disposed;
    			tile.Image = bitmap;
    		}
    		else
        	{
    			Tile tile = new Tile(desc, bitmap, state);
    			trackTiles.put(desc.GetHashCode(), tile);
        	}
        } finally
        {
      	  trackTilesLock.unlock();
        }
    }

    private class loaderThread extends AsyncTask<ArrayList<Descriptor>, Integer, Integer> {

		@Override
		protected Integer doInBackground(ArrayList<Descriptor>... params) {
			try
		    {
				Descriptor desc = null;

		        while (true)
		        {
		        	wishlistLock.lock();
		        	try
		        	{
		        		if (wishlist.size() == 0)
		        			break;

		        		// Den raussuchen, der im Augenblick am meisten sichtbar ist
		        		int minDist = Integer.MAX_VALUE;

		        		// Alle beantragten Kacheln die nicht der
		        		// aktuellen Zoomstufe entsprechen, rausschmeissen
		        		for (int i = 0; i < wishlist.size(); i++)
		        		{
		        			if (wishlist.get(i).Zoom != Zoom)
		        			{
		        				loadedTilesLock.lock();
		        				try
		        				{
			        				Tile tile = loadedTiles.get(wishlist.get(i).GetHashCode());
			        				loadedTiles.remove(wishlist.get(i).GetHashCode());
			        				tile.destroy();
			        				wishlist.remove(i);
			        				i = -1;
		        				} finally
		        				{
		        					loadedTilesLock.unlock();
		        				}
		        			}
		        		}

		        		for (Descriptor candidate : wishlist)
		        		{
		        			int dist = Integer.MAX_VALUE;
		        			if (candidate.Zoom == Zoom)
		        			{
		        				Point p1 = ToScreen(candidate.X, candidate.Y, candidate.Zoom);
		        				p1.x += (int)(128 * dpiScaleFactorX);
		        				p1.y += (int)(128 * dpiScaleFactorY);
		        				
		        				dist = (p1.x - halfWidth) * (p1.x - halfWidth) + (p1.y - halfHeight) * (p1.y - halfHeight);
		        			}

		        			if (dist < minDist)
		        			{
		        				desc = candidate;
		        				minDist = dist;
		        			}
		        		}

		        		wishlist.remove(desc);
		            } finally
		            {
		          	  wishlistLock.unlock();
		            }
		        	try
		        	{
		        		if (Manager.CacheTile(CurrentLayer, desc))
		        		{
		        			Bitmap bitmap = MapView.Manager.LoadLocalBitmap(CurrentLayer, desc);
		        			
		        			if (bitmap == null)
		        			{
		        				// Laden der Kachel fehlgeschlagen! Tile wieder aus loadedTiles
		        				// entfernen
		        				loadedTilesLock.lock();
		        				try
		        				{
		        					if (loadedTiles.containsKey(desc.GetHashCode()))
		        						loadedTiles.remove(desc.GetHashCode());
		        				} finally
		        				{
		        					loadedTilesLock.unlock();
		        				}
		        				
		        				continue;
		        			}
/*
		        			if (OnTileLoaded != null)
		        				OnTileLoaded(bitmap, desc);
*/
/*
		        			if (Config.GetBool("OsmDpiAwareRendering") && (dpiScaleFactorX != 1 || dpiScaleFactorY != 1))
		        				scaleUpBitmap(ref bitmap);
*/		        				



		        			addLoadedTile(desc, bitmap, Tile.TileState.Present);

		        			// Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
		        			// kann man die Karte ja gut mal neu rendern!
		        			tilesFinished = true;

		        			if (tileVisible(desc))
		        				Render(true);

		        		}
		        		else
		        		{
		        			// Kachel nicht geladen, noch ein Versuch!
		        			//lock (loadedTiles)
		        			//    if (loadedTiles.ContainsKey(desc))
		        			//        loadedTiles.Remove(desc);

		        			continue;
		        		}
/*
		        		boolean LowMemory = Global.GetAvailableDiscSpace(Config.GetString("TileCacheFolder")) < ((long)1024 * 1024);
		        		if (LowMemory)
		        		{
		        			MessageBox.Show("Device is running low on memory! Internet access will shut down now. Please free some memory e.g. by deleting unused tiles!", "Warning!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1);
		#if DEBUG
		              Global.AddLog("MapView.loaderThreadEntryPoint: device is low on memory");
		#endif
							Config.Set("AllowInternetAccess", false);
		              		Config.AcceptChanges();
		              		break;
		        		}*/
		        	}
		        	catch (Exception exc)
		        	{
		        		// Fehler aufgetreten! Kachel nochmal laden!
		        		if (desc != null)
		        		{
		        			loadedTilesLock.lock();
		        			try
		        			{
		        				if (loadedTiles.containsKey(desc.GetHashCode()))
		        				{
		        					Tile tile = loadedTiles.get(desc.GetHashCode());		        			
		        					loadedTiles.remove(desc.GetHashCode());
		        					tile.destroy();
		        				}
		        		      } finally
		        		      {
		        		    	  loadedTilesLock.unlock();
		        		      }
		        		}
		        		Logger.Error("MapView.loaderThreadEntryPoint", "exception caught", exc);
		        	}

		        }

		        loaderThread = null;
		    }
		    catch (Exception ex) 
		    {
		    	Logger.Error("MapView.doInBackground()","", ex);	    	
		    }
		    return null;
		}

	     protected void onPostExecute(Integer result) {
	    	 loaderThread = null;
		     Render(true);
	     }    	
    }

    /*
    void loaderThreadEntryPoint()
    {
      try
      {
        Descriptor desc = null;

        while (true)
        {
          lock (wishlist)
          {
            if (wishlist.Count == 0)
              break;

            // Den raussuchen, der im Augenblick am meisten sichtbar ist
            int minDist = int.MaxValue;

            // Alle beantragten Kacheln die nicht der
            // aktuellen Zoomstufe entsprechen, rausschmeissen
            for (int i = 0; i < wishlist.Count; i++)
              if (wishlist[i].Zoom != Zoom)
              {
                loadedTiles.Remove(wishlist[i]);
                wishlist.RemoveAt(i);
                i = -1;
              }

            foreach (Descriptor candidate in wishlist)
            {
              int dist = int.MaxValue;
              if (candidate.Zoom == Zoom)
              {
                Point p1 = ToScreen(candidate.X, candidate.Y, candidate.Zoom);
                p1.X += (int)(128 * dpiScaleFactorX);
                p1.Y += (int)(128 * dpiScaleFactorY);

                dist = (p1.X - halfWidth) * (p1.X - halfWidth) + (p1.Y - halfHeight) * (p1.Y - halfHeight);
              }

              if (dist < minDist)
              {
                desc = candidate;
                minDist = dist;
              }
            }

            wishlist.Remove(desc);
          }
          try
          {
            if (Manager.CacheTile(CurrentLayer, desc))
            {
              Bitmap bitmap = MapView.Manager.LoadLocalBitmap(CurrentLayer, desc);

              if (bitmap == null)
               {
                // Laden der Kachel fehlgeschlagen! Tile wieder aus loadedTiles
                // entfernen
                loadedTiles.Remove(desc);
                continue;
              }

              if (OnTileLoaded != null)
                OnTileLoaded(bitmap, desc);

              if (Config.GetBool("OsmDpiAwareRendering") && (dpiScaleFactorX != 1 || dpiScaleFactorY != 1))
                scaleUpBitmap(ref bitmap);



              addLoadedTile(desc, bitmap, Tile.TileState.Present);

              // Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
              // kann man die Karte ja gut mal neu rendern!
              tilesFinished = true;

              //if (tileVisible(loadedTiles[desc]))
              Render(true);

            }
            else
            {
              // Kachel nicht geladen, noch ein Versuch!
              //lock (loadedTiles)
              //    if (loadedTiles.ContainsKey(desc))
              //        loadedTiles.Remove(desc);

              continue;
            }

            bool LowMemory = Global.GetAvailableDiscSpace(Config.GetString("TileCacheFolder")) < ((long)1024 * 1024);
            if (LowMemory)
            {
              MessageBox.Show("Device is running low on memory! Internet access will shut down now. Please free some memory e.g. by deleting unused tiles!", "Warning!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1);
#if DEBUG
              Global.AddLog("MapView.loaderThreadEntryPoint: device is low on memory");
#endif
              Config.Set("AllowInternetAccess", false);
              Config.AcceptChanges();
              break;
            }
          }
          catch (Exception exc)
          {
            // Fehler aufgetreten! Kachel nochmal laden!
            if (desc != null)
              lock (loadedTiles)
                if (loadedTiles.ContainsKey(desc))
                  loadedTiles.Remove(desc);

#if DEBUG
            Global.AddLog("MapView.loaderThreadEntryPoint: exception caught: " + exc.ToString());
#endif
          }

        }

        lock (wishlist)
          loaderThread = null;
      }
      catch (ThreadAbortException) { }
      finally
      {
        loaderThread = null;
        Render(true);
      }
    }

*/
    private Drawable getUnderlayIcon(Cache cache, Waypoint waypoint)
    {
        if (waypoint == null)
        {
	        if ((cache == null) || (cache == Global.SelectedCache()))
	        {
	            if (cache.Archived || !cache.Available)
	                return Global.NewMapOverlay.get(2).get(3);
	            else
	                return Global.NewMapOverlay.get(2).get(1);
	        }
	        else
	        {
	            if (cache.Archived || !cache.Available)
	                return Global.NewMapOverlay.get(2).get(2);
	            else
	                return Global.NewMapOverlay.get(2).get(0);
	        }
        } else
        {
            if (waypoint == Global.SelectedWaypoint())
            {
                return Global.NewMapOverlay.get(2).get(1);
            }
            else
            {
                return Global.NewMapOverlay.get(2).get(0);
            }        	
        }
    }

    public void UpdateCacheList()
    {
      updateCacheList();
    }
    /// <summary>
    /// Sucht aus dem aktuellen Query die Caches raus, die dargestellt
    /// werden sollen und aktualisiert wpToRender entsprechend.
    /// </summary>
    void updateCacheList()
    {
      if (Database.Data.Query == null)
        return;
      synchronized (screenCenter)
      {
	
	      int iconSize = 0; // 8x8
	      if ((Zoom >= 12) && (Zoom <= 13))
	          iconSize = 1; // 13x13
	      else if (Zoom > 13)
	          iconSize = 2;  // default Images
	
	      int xFrom = -halfIconSize - drawingWidth / 2;
	      int yFrom = -halfIconSize - drawingHeight / 2;
	      int xTo = drawingWidth + halfIconSize + drawingWidth / 2;
	      int yTo = drawingHeight + halfIconSize + drawingWidth / 2;
	
	      ArrayList<WaypointRenderInfo> result = new ArrayList<WaypointRenderInfo>();
	
	      // Wegpunkte in Zeichenliste eintragen, unabh�ngig davon, wo
	      // sie auf dem Bildschirm sind
	      if (Global.SelectedCache() != null)
	      {
	        if (!(hideMyFinds && Global.SelectedCache().Found))
	        {
	          ArrayList<Waypoint> wps = Global.SelectedCache().waypoints;
	
	          for (Waypoint wp : wps)
	          {
	            WaypointRenderInfo wpi = new WaypointRenderInfo();
	            wpi.MapX = 256 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, wp.Pos.Longitude);
	            wpi.MapY = 256 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, wp.Pos.Latitude);
	            wpi.Icon = Global.NewMapIcons.get(2).get((int)wp.Type.ordinal());
	            wpi.UnderlayIcon = Global.NewMapOverlay.get(2).get(0);
	            wpi.Cache = Global.SelectedCache();
	            wpi.Waypoint = wp;
	            wpi.Selected = (Global.SelectedWaypoint() == wp);
	            wpi.UnderlayIcon = getUnderlayIcon(wpi.Cache, wpi.Waypoint);
	
	           	int x = (int)(wpi.MapX * dpiScaleFactorX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
	           	int y = (int)(wpi.MapY * dpiScaleFactorY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;
	
	            if ((x < xFrom || y < yFrom || x > xTo || y > yTo))
	              continue;
	
	            result.add(wpi);
	          }
	        }
	      }
	
	      // Und Caches auch. Diese allerdings als zweites, da sie WPs �berzeichnen
	      // sollen
	      for (Cache cache : Database.Data.Query)
	      {
	        if (hideMyFinds && cache.Found)
	          continue;
	
	        int x = 0;
	        int y = 0;
	        try
	        {
	        	x = (int)(cache.MapX * dpiScaleFactorX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
	        	y = (int)(cache.MapY * dpiScaleFactorY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;
			} finally
			{
			}
	        
	
	        if ((x < xFrom || y < yFrom || x > xTo || y > yTo) && cache != Global.SelectedCache())
	          continue;
	
	        if ((hideCacheWithFinal) && (cache.Type == CacheTypes.Mystery) && cache.MysterySolved() && cache.HasFinalWaypoint())
	        {
	          // Wenn ein Mystery-Cache einen Final-Waypoint hat, hier die Koordinaten des Caches nicht zeichnen.
	          // Der Final-Waypoint wird sp�ter mit allen notwendigen Informationen gezeichnet. 
	          // Die Koordinaten des Caches sind in den allermeisten F�llen irrelevant.
	          // Damit wird von einem gel�sten Mystery nur noch eine Koordinate in der Map gezeichnet, wenn der Cache nicht Selected ist.
	          // Sobald der Cache Selected ist, werden der Cache und alle seine Waypoints gezeichnet.
	          if (cache != Global.SelectedCache())
	            continue;
	        }
	
	        WaypointRenderInfo wpi = new WaypointRenderInfo();
	        wpi.UnderlayIcon = null;
	        wpi.OverlayIcon = null;
	        wpi.MapX = cache.MapX;
	        wpi.MapY = cache.MapY;
	        wpi.Icon = (cache.Owner.toLowerCase().equals(gcLogin) && (gcLogin.length() > 0)) ? Global.NewMapIcons.get(2).get(20) : (cache.Found) ? Global.NewMapIcons.get(2).get(19) : (cache.MysterySolved() && (cache.Type == CacheTypes.Mystery)) ? Global.NewMapIcons.get(2).get(21) : Global.NewMapIcons.get(2).get((int)cache.Type.ordinal());
	        wpi.Icon = Global.NewMapIcons.get(2).get(cache.GetMapIconId(gcLogin));
	        wpi.UnderlayIcon = getUnderlayIcon(cache, wpi.Waypoint);
	          
	        if ((iconSize < 2) && (cache != Global.SelectedCache()))  // der SelectedCache wird immer mit den gro�en Symbolen dargestellt!
	        {
	            int iconId = 0;
	            wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(0);  // rectangular shaddow
	            if (cache.Archived || !cache.Available)
	                wpi.OverlayIcon = Global.NewMapOverlay.get(iconSize).get(3); 
	            switch (cache.Type)
	            {
	                case Traditional: iconId = 0; break;
	                case Letterbox: iconId = 0; break;
	                case Multi: iconId = 1; break;
	                case Event: iconId = 2; break;
	                case MegaEvent: iconId = 2; break;
	                case Virtual: iconId = 3; break;
	                case Camera: iconId = 3; break;
	                case Earth: iconId = 3; break;
	                case Mystery:
	                    {
	                        if (cache.HasFinalWaypoint())
	                            iconId = 5;
	                        else
	                            iconId = 4;
	                        break;
	                    }
	                case Wherigo: iconId = 4; break;
	            }
	            if (cache.Found)
	            {
	                iconId = 6;
	                wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(1);  // round shaddow
	            }
	            if (cache.Owner.toLowerCase().equals(gcLogin) && (gcLogin.length() > 0))
	            {
	                iconId = 7;
	                wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(2);  // star shaddow
	            }
	            wpi.Icon = Global.NewMapIcons.get(iconSize).get(iconId);
	        }
	        
	        wpi.Cache = cache;
	        wpi.Waypoint = null;
	        wpi.Selected = (Global.SelectedCache() == cache);
	
	        result.add(wpi);
	      }
	
	      // Final-Waypoints von Mysteries einzeichnen
	      for (MysterySolution solution : Database.Data.Query.MysterySolutions)
	      {
	          // bei allen Caches ausser den Mysterys sollen die Finals nicht gezeichnet werden, wenn der Zoom klein ist
	          if ((Zoom < 14) && (solution.Cache.Type != CacheTypes.Mystery))
	              continue;
	          
	          if (Global.SelectedCache() == solution.Cache)
	              continue;   // is already in list
	
	          if (hideMyFinds && solution.Cache.Found)
	              continue;
	
	          double mapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, solution.Longitude);
	          double mapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, solution.Latitude);
	
	          int x = 0;
	          int y = 0;
	          try
	          {
	        	  x = (int)(mapX * dpiScaleFactorX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
	        	  y = (int)(mapY * dpiScaleFactorY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;
	          } finally
	          {
	          }
	
	          if ((x < xFrom || y < yFrom || x > xTo || y > yTo))
	              continue;
	
	          WaypointRenderInfo wpiF = new WaypointRenderInfo();
	          wpiF.MapX = mapX;
	          wpiF.MapY = mapY;
	
	          if (iconSize == 2)
	          {
	              wpiF.Icon = (solution.Cache.Type == CacheTypes.Mystery) ? Global.NewMapIcons.get(2).get(21) : Global.NewMapIcons.get(2).get(18);
	              wpiF.UnderlayIcon = getUnderlayIcon(solution.Cache, solution.Waypoint);
	              if ((hideCacheWithFinal) && (solution.Cache.Type == CacheTypes.Mystery) && solution.Cache.MysterySolved() && solution.Cache.HasFinalWaypoint())
	              {
	                  if (Global.SelectedCache() != solution.Cache)
	                  {
	                      // die Icons aller geloesten Mysterys evtl. aendern, wenn der Cache gefunden oder ein Eigener ist.
	                      // change the icon of solved mysterys if necessary when the cache is found or own
	                      if (solution.Cache.Found)
	                          wpiF.Icon = Global.NewMapIcons.get(2).get(19);
	                      if (solution.Cache.Owner.toLowerCase().equals(gcLogin) && (gcLogin.length() > 0))
	                          wpiF.Icon = Global.NewMapIcons.get(2).get(20);
	                  }
	                  else
	                  {
	                      // das Icon des geloesten Mysterys als Final anzeigen, wenn dieser Selected ist
	                      // show the Icon of solved mysterys as final when cache is selected
	                      wpiF.Icon = Global.NewMapIcons.get(2).get((int)solution.Waypoint.Type.ordinal());
	                  }
	              }
	          }
	          else
	          {
	              int iconId = 0;
	              wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(0);  // rectangular shaddow
	              if (solution.Cache.Archived || !solution.Cache.Available)
	                wpiF.OverlayIcon = Global.NewMapOverlay.get(iconSize).get(3);
	              switch (solution.Cache.Type)
	              {
	                  case Traditional: iconId = 0; break;
	                  case Letterbox: iconId = 0; break;
	                  case Multi: iconId = 1; break;
	                  case Event: iconId = 2; break;
	                  case MegaEvent: iconId = 2; break;
	                  case Virtual: iconId = 3; break;
	                  case Camera: iconId = 3; break;
	                  case Earth: iconId = 3; break;
	                  case Mystery:
	                      {
	                          if (solution.Cache.HasFinalWaypoint())
	                              iconId = 5;
	                          else
	                              iconId = 4;
	                          break;
	                      }
	                  case Wherigo: iconId = 4; break;
	              }
	
	              if (solution.Cache.Found)
	              {
	                  iconId = 6;
	                  wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(1);  // round shaddow
	              }
	              if (solution.Cache.Owner.toLowerCase().equals(gcLogin) && (gcLogin.length() > 0))
	              {
	                  iconId = 7;
	                  wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(2);  // start shaddow
	              }
	              wpiF.Icon = Global.NewMapIcons.get(iconSize).get(iconId);
	          }
	          wpiF.Cache = solution.Cache;
	          wpiF.Waypoint = solution.Waypoint;
	          wpiF.Selected = (Global.SelectedWaypoint() == solution.Waypoint);
	          result.add(wpiF);
	      }
	
	      wpToRender = result;

      }
    }
/*
    Pen boldRedPen = new Pen(Color.Red, 4);
    Pen boldOrangePen = new Pen(Color.Orange, 4);
    Pen largeGreenPen = new Pen(Color.Green, 6);

    ImageAttributes colorKey = new ImageAttributes();
    ImageAttributes imageAttributes = null;

      
*/      
    void renderCaches()
    {

    	int smallStarHeight = (int)((double)Global.SmallStarIcons[1].getMinimumHeight() * dpiScaleFactorY);

		for (WaypointRenderInfo wpi : wpToRender)
		{
		  int halfIconWidth = (int)((wpi.Icon.getMinimumWidth()/* * dpiScaleFactorX*/) / 2);
		  int IconWidth = (int)(wpi.Icon.getMinimumWidth()/* * dpiScaleFactorX*/);
		  int halfOverlayWidth = halfIconWidth;
		  int OverlayWidth = IconWidth;
		  if (wpi.OverlayIcon != null)
		  {
		      halfOverlayWidth = (int)((wpi.OverlayIcon.getMinimumWidth()/* * dpiScaleFactorX*/) / 2);
		      OverlayWidth = (int)(wpi.OverlayIcon.getMinimumWidth()/* * dpiScaleFactorX*/);
		  }
		  int halfUnderlayWidth = halfIconWidth;
		  int UnderlayWidth = IconWidth;
		  if (wpi.UnderlayIcon != null)
		  {
		      halfUnderlayWidth = (int)((wpi.UnderlayIcon.getMinimumWidth()/* * dpiScaleFactorX*/) / 2);
		      UnderlayWidth = (int)(wpi.UnderlayIcon.getMinimumWidth()/* * dpiScaleFactorX*/);
		  }
		
		  int x = (int)((wpi.MapX * adjustmentCurrentToCacheZoom * dpiScaleFactorX - screenCenter.X)) + halfWidth;
		  int y = (int)((wpi.MapY * adjustmentCurrentToCacheZoom * dpiScaleFactorY - screenCenter.Y)) + halfHeight;
		
		  x = x - width / 2;
		  y = y - halfHeight;
		  x = (int)Math.round(x * multiTouchFaktor + width / 2);
		  y = (int)Math.round(y * multiTouchFaktor + halfHeight);
		  
		  // drehen
		  if (alignToCompass)
		  {
			  Point res = rotate(new Point(x, y), - canvasHeading);
			  x = res.x;
			  y = res.y;
		  }
		  
		  
		  int imageX = x;
		  int imageY = y;
		
		  if ((Zoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == Global.SelectedWaypoint()))
		  {
		    int size = (int)(halfIconWidth);
		
		    float lineWidth = 2.0f;
		
		    Paint CrossGreenPen = new Paint();
		    CrossGreenPen.setColor(Color.GREEN);
		    CrossGreenPen.setStrokeWidth(lineWidth * dpiScaleFactorX);
		    Paint CrossYellowPen = new Paint();
		    CrossYellowPen.setColor(Color.YELLOW);
		    CrossYellowPen.setStrokeWidth(lineWidth * dpiScaleFactorX);
		    Paint CrossMagentaPen = new Paint();
		    CrossMagentaPen.setColor(Color.MAGENTA);
		    CrossMagentaPen.setStrokeWidth(lineWidth * dpiScaleFactorX);
		    Paint CrossBluePen = new Paint();
		    CrossBluePen.setColor(Color.BLUE);
		    CrossBluePen.setStrokeWidth(lineWidth * dpiScaleFactorX);
		    Paint CrossRedPen = new Paint();
		    CrossRedPen.setColor(Color.RED);
		    CrossRedPen.setStrokeWidth(lineWidth * dpiScaleFactorX);
		
		    Paint cachePen = CrossMagentaPen;
		
		    if (wpi.Cache != null)
		    {
		      if (wpi.Cache.Type == CacheTypes.Traditional) cachePen = CrossGreenPen;
		      else if (wpi.Cache.Type == CacheTypes.Multi) cachePen = CrossYellowPen;
		      else if (wpi.Cache.Type == CacheTypes.Mystery) cachePen = CrossBluePen;
		
		      Paint selectedPen = wpi.Selected ? CrossRedPen : cachePen;
		      selectedPen.setStyle(Style.STROKE);
		
		      canvasOverlay.drawRect(x - size, y - size, x + size, y + size, selectedPen);
		      canvasOverlay.drawLine(x - size, y, x - size / 2, y, selectedPen);
		      canvasOverlay.drawLine(x + size / 2, y, x + size, y, selectedPen);
		      canvasOverlay.drawLine(x, y - size, x, y - size / 2, selectedPen);
		      canvasOverlay.drawLine(x, y + size / 2, x, y + size, selectedPen);
		      canvasOverlay.drawLine(x - 2 * size - 5, y - 2 * size - 5, x - size, y - size, selectedPen);
		
		      imageX = x - 2 * size - 5;
		      imageY = y - 2 * size - 5;
		    }
		  }
	
		  
		  if (wpi.UnderlayIcon != null)
		      drawImage(canvasOverlay, wpi.UnderlayIcon, imageX - halfUnderlayWidth, imageY - halfUnderlayWidth, UnderlayWidth, UnderlayWidth);
		  drawImage(canvasOverlay, wpi.Icon, imageX - halfIconWidth, imageY - halfIconWidth, IconWidth, IconWidth);
		  if (wpi.OverlayIcon != null)
		      drawImage(canvasOverlay, wpi.OverlayIcon, imageX - halfOverlayWidth, imageY - halfOverlayWidth, OverlayWidth, OverlayWidth);
		
		  if (wpi.Cache.Favorit)
		  {
			  ActivityUtils.PutImageTargetHeight(canvasOverlay, Global.Icons[19], imageX, imageY, (int)(14.0f * dpiScaleFactorY));
		  }
		
		  boolean drawAsWaypoint = wpi.Waypoint != null;
		  if ((Global.SelectedCache() != wpi.Cache) && hideCacheWithFinal && (wpi.Cache.Type == CacheTypes.Mystery))
		    // Waypoints (=final) of not selected caches should be drawn like the cache self because the cache is not drawn
		    drawAsWaypoint = false;
		
		  // Beschriftung
		  if (showTitles && (Zoom >= 14))
		  {
		    int yoffset = 0;
		    yoffset = (int)(fontSmall.getTextSize());
		
		    String wpName;                // draw Final Waypoint of not Selected Caches like the caches self because the cache will not be shown
		    if (drawAsWaypoint)
		    {  // Aktiver WP -> Titel oder GCCode
		    	wpName = (wpi.Waypoint.Title == "") ? wpi.Waypoint.GcCode : wpi.Waypoint.Title;
		    	fontSmall.setColor(Color.WHITE);
		    	canvasOverlay.drawText(wpName, x + halfIconWidth + 4, y, fontSmall);
		    	fontSmall.setColor(Color.BLACK);
		    	canvasOverlay.drawText(wpName, x + halfIconWidth + 5, y + 1, fontSmall);
		    }
		    else 
		    {  // Aktiver Cache -> Cachename
		    	wpName = wpi.Cache.Name;
		    	if (showRating)
		    		yoffset += 10 * dpiScaleFactorX;
		    	int fwidth = (int)(fontSmall.measureText(wpName) / 2);
		    	fontSmall.setColor(Color.WHITE);
		    	canvasOverlay.drawText(wpName, x - fwidth, y + halfIconWidth + yoffset, fontSmall);
		    	fontSmall.setColor(Color.BLACK);
		    	canvasOverlay.drawText(wpName, (x - fwidth) + 1, y + halfIconWidth + yoffset + 1, fontSmall);
		    }
		  }
		
		  // Rating des Caches darstellen
		  if (showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (Zoom >= 14))
		  {
		    Drawable img = Global.SmallStarIcons[(int)Math.min(wpi.Cache.Rating * 2, 5 * 2)];
		    Rect bounds = img.getBounds();
		    int halfSmallStarWidth = (int)(((double)img.getMinimumWidth() / 2.0) * dpiScaleFactorX);
		    int smallStarWidth = (int)((double)img.getMinimumWidth() * dpiScaleFactorX);
		    img.setBounds(x - halfSmallStarWidth, y + halfUnderlayWidth + 2, x - halfSmallStarWidth + smallStarWidth, y + halfUnderlayWidth + 2 + smallStarHeight);
		    img.draw(canvasOverlay);
		    img.setBounds(bounds);
		  }
		
		  // Show D/T-Rating
		  if (showDT && (!drawAsWaypoint) && (Zoom >= 14))
		  {
		    Drawable imgDx = Global.SmallStarIcons[(int)Math.min(wpi.Cache.Difficulty * 2, 5 * 2)];
		    Rect bounds = imgDx.getBounds();
		    int smallStarHeightD = (int)((double)imgDx.getMinimumWidth() * dpiScaleFactorY);
		    imgDx.setBounds(x - halfUnderlayWidth, y - halfUnderlayWidth - smallStarHeight - 2, x - halfUnderlayWidth + smallStarHeightD, y - halfUnderlayWidth - smallStarHeight - 2 + smallStarHeight);
		    
		    canvasOverlay.save();
		    canvasOverlay.rotate(270, x, y);
		    imgDx.draw(canvasOverlay);
		    canvasOverlay.restore();
		    imgDx.setBounds(bounds);

		    
		    imgDx = Global.SmallStarIcons[(int)Math.min(wpi.Cache.Terrain * 2, 5 * 2)];
		    bounds = imgDx.getBounds();
		    smallStarHeightD = (int)((double)imgDx.getMinimumWidth() * dpiScaleFactorY);
		    imgDx.setBounds(x - halfUnderlayWidth, y + halfUnderlayWidth + 2, x - halfUnderlayWidth + smallStarHeightD, y + halfUnderlayWidth + 2 + smallStarHeight);
		    canvasOverlay.save();
		    canvasOverlay.rotate(270, x, y);
		    imgDx.draw(canvasOverlay);
		    canvasOverlay.restore();
		    imgDx.setBounds(bounds);
		    /*		
		    Bitmap imgTx = Global.SmallStarIcons[(int)Math.Min(wpi.Cache.Terrain * 2, 5 * 2)];
		    Bitmap imgT = new Bitmap(imgTx.Height, imgTx.Width);
		    InternalRotateImage(270, imgTx, imgT);
		    int halfSmallStarHeightT = (int)(((double)imgT.Height / 2.0) * dpiScaleFactorY);
		    int smallStarHeightT = (int)((double)imgT.Height * dpiScaleFactorY);
		    graphics.DrawImage(imgT, new Rectangle(x + halfIconWidth + 4, y + halfIconWidth - smallStarHeightT, smallStarHeight, smallStarHeightT), 0, 0, imgT.Width, imgT.Height, GraphicsUnit.Pixel, imageAttributes);
*/		
		
		  }
		}
    }

    void drawImage(Canvas aCanvas, Bitmap image, int x, int y, int width, int height)
    {
        aCanvas.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new Rect(x, y, x + width, y + height), null);
//        graphics.DrawImage(image, new Rectangle(x, y, width, height), 0, 0, image.Width, image.Height, GraphicsUnit.Pixel);
    }
    void drawImage(Canvas aCanvas, Drawable image, int x, int y, int width, int height)
    {
    	image.setBounds(x, y, x+width, y+height);
    	image.draw(aCanvas);
    }

    /// <summary>
    /// �berpr�ft, ob die �bergebene Kachel im Darstellungsbereich des
    /// Controls liegt
    /// </summary>
    /// <param name="tile">Die zu pr�fende Kachel</param>
    /// <returns>true, wenn die Kachel sichtbar ist, sonst false</returns>
    boolean tileVisible(Descriptor tile)
    {
        Point p1 = ToScreen(tile.X, tile.Y, tile.Zoom);
        Point p2 = ToScreen(tile.X + 1, tile.Y + 1, tile.Zoom);

        // relativ zu Zentrum
        p1.x = p1.x - width / 2;
        p1.y = p1.y - (height - halfHeight);
        // skalieren
        p1.x = (int) Math.round(p1.x * multiTouchFaktor + drawingWidth/ 2);
        p1.y = (int) Math.round(p1.y * multiTouchFaktor + drawingHeight / 2);
        // relativ zu Zentrum
        p2.x = p2.x - width / 2;
        p1.y = p2.y - halfHeight;
        // skalieren
        p2.x = (int) Math.round(p2.x * multiTouchFaktor + drawingWidth / 2);
        p2.y = (int) Math.round(p2.y * multiTouchFaktor + drawingHeight / 2);

        return (p1.x < drawingWidth && p2.x >= 0 && p1.y < drawingHeight && p2.y >= 0);
    }

    /// <summary>
    /// Lagert die am l�ngsten nicht mehr verwendete Kachel aus
    /// </summary>
    protected void preemptTile()
    {
	  //List<Tile> tiles = new List<Tile>();
	  //tiles.AddRange(loadedTiles.Values);
	
	  // Ist Auslagerung �berhaupt n�tig?
	  if (numLoadedTiles() <= numMaxTiles)
	    return;
	
	  loadedTilesLock.lock();
	  try
	  {
		  do
		  {
			  // Kachel mit maximalem Alter suchen
			  int maxAge = Integer.MIN_VALUE;
			  Descriptor maxDesc = null;
			
			  for (Tile tile : loadedTiles.values())
			    if (tile.Image != null && tile.Age > maxAge)
			    {
			      maxAge = tile.Age;
			      maxDesc = tile.Descriptor;
			    }
			
			  // Instanz freigeben und Eintrag l�schen
			  if (maxDesc != null)
			  {
				  try
				  {
					  Tile tile = loadedTiles.get(maxDesc.GetHashCode());
					  loadedTiles.remove(maxDesc.GetHashCode());
					  tile.destroy();
				  } catch (Exception ex)
				  {
					  Logger.Error("MapView.preemptTile()","", ex);				  
				  }
			  }
		  } while (numLoadedTiles() > numMaxTiles);
      } finally
      {
    	  loadedTilesLock.unlock();
      }
    }
	
	
	  // das ganze noch f�r die TrackTiles
    protected void preemptTrackTile()
    {
    	// Ist Auslagerung �berhaupt n�tig?
    	if (numTrackTiles() <= numMaxTrackTiles)
    		return;
	
    	trackTilesLock.lock();
    	try
    	{
    		do
    		{
	    		// Kachel mit maximalem Alter suchen
	    		int maxAge = Integer.MIN_VALUE;
	    		Descriptor maxDesc = null;
	    		
	    		for (Tile tile : trackTiles.values())
	    			if (tile.Image != null && tile.Age > maxAge)
	    			{
	    				maxAge = tile.Age;
	    				maxDesc = tile.Descriptor;
	    			}
	    		
	    		// Instanz freigeben und Eintrag l�schen
	    		if (maxDesc != null)
	    		{
	    			try
	    			{
	    				Tile tile = trackTiles.get(maxDesc.GetHashCode());
	    				tile.destroy();
	    				trackTiles.remove(maxDesc.GetHashCode());
	    			} catch (Exception ex)
	    			{
	    				Logger.Error("MapView.preemptTrackTile()","",ex);				  
	    			}
	    		}
    		} while (numTrackTiles() > numMaxTrackTiles);
        } finally
        {
      	  trackTilesLock.unlock();
        }
    }

    boolean lastRenderZoomScale = false;
    boolean tilesFinished = false;
    int lastZoom = Integer.MIN_VALUE;
    Coordinate lastPosition = new Coordinate();
    float lastHeading = Float.MAX_VALUE;
    int lastWpCount = 0;
    PointD lastRenderedPosition = new PointD(Double.MAX_VALUE, Double.MAX_VALUE);

    public void Render(boolean overrideRepaintInteligence)
    {
    	if (canvas == null)
    		return;

//    	debugString1 = loadedTiles.size() + " / " + trackTiles.size() + " / " + numLoadedTiles();
//    	debugString2 = available_bytes * 1024 - Debug.getNativeHeapAllocatedSize() / 1024 + " kB";
    	try
    	{
	    	try
	    	{
		    	
		    	synchronized (screenCenter)
		     	{
		        	float tmpCanvasHeading = canvasHeading;
		    		if (Database.Data.Query == null)
		    		{
		    			return;
		    		}
		    		if (offScreenBmp == null)
		    		{
		    			return;
		    		}
			
			      // Aufruf ggf. im richtigen Thread starten
			/*      if (InvokeRequired)
			      {
			        Invoke(new EmptyDelegate(Render), overrideRepaintInteligence);
			        return;
			      }*/
			
			      // Wenn sich bei der Ansicht nichts getan hat braucht sie auch nicht gerendert werden.
		    		if (!overrideRepaintInteligence)
		    		{
		    			if (lastRenderZoomScale == renderZoomScaleActive && lastWpCount == wpToRender.size() && lastHeading == ((Global.Locator != null) && (Global.Locator.getLocation() != null) ? Global.Locator.getHeading() : 0) && lastPosition.Latitude == Global.LastValidPosition.Latitude && lastPosition.Longitude == Global.LastValidPosition.Longitude && lastZoom == Zoom && !tilesFinished && lastRenderedPosition.X == screenCenter.X && lastRenderedPosition.Y == screenCenter.Y)
		    			{
		    				return;
		    			}
		
		    			lastRenderZoomScale = renderZoomScaleActive;
		    			lastWpCount = wpToRender.size();
		    			tilesFinished = false;
		    			lastPosition.Latitude = Global.LastValidPosition.Latitude;
		    			lastPosition.Longitude = Global.LastValidPosition.Longitude;
		    			lastHeading = 0;
		    			/*        lastHeading = (Global.Locator != null) ? Global.Locator.Heading : 0;*/
		    			lastZoom = Zoom;
		    			lastRenderedPosition.X = screenCenter.X;
		    			lastRenderedPosition.Y = screenCenter.Y;
		    		}
	
		    		loadedTilesLock.lock();
		    		try
		    		{
		    			for (Tile tile : loadedTiles.values())
		    				tile.Age++;
		    		} finally
		    		{
		    			loadedTilesLock.unlock();
		    		}
		    		trackTilesLock.lock();
		    		try
		    		{
		    			for (Tile tile : trackTiles.values())
		    				tile.Age++;
		            } finally
		            {
		          	  trackTilesLock.unlock();
		            }
		    		
		    		int xFrom;
		    		int xTo;
		    		int yFrom;
		    		int yTo;
		    		Rect tmp = getTileRange(rangeFactorTiles);
		    		xFrom = tmp.left;
		    		xTo = tmp.right;
		    		yFrom = tmp.top;
		    		yTo = tmp.bottom;
	
		    		int xFromTrack;
		    		int xToTrack;
		    		int yFromTrack;
		    		int yToTrack;
		    		Rect tmpTrack = getTileRange(rangeFactorTrack);
		    		xFromTrack = tmpTrack.left;
		    		xToTrack = tmpTrack.right;
		    		yFromTrack = tmpTrack.top;
		    		yToTrack = tmpTrack.bottom;
			      
		    		canvas.save();
		    		canvas.rotate(-tmpCanvasHeading, width / 2, halfHeight);
	
		    		try
		    		{
			    		// 	Kacheln beantragen
			    		for (int x = xFrom; x <= xTo; x++)
			    		{
			    			for (int y = yFrom; y <= yTo; y++)
			    			{
			    				if (x < 0 || y < 0 || x >= Descriptor.TilesPerLine[Zoom] || y >= Descriptor.TilesPerColumn[Zoom])
			    					continue;
				
			    				Descriptor desc = new Descriptor(x, y, Zoom);
				
			    				Tile tile;
			    				Tile trackTile;
			    				
			    				loadedTilesLock.lock();
			    				try
			    				{
			    					if (!loadedTiles.containsKey(desc.GetHashCode()))
			    					{
			    						preemptTile();
			    						
			    						loadedTiles.put(desc.GetHashCode(), new Tile(desc, null, Tile.TileState.Disposed));
				
			    						queueTile(desc);
			    					}
			    					tile = loadedTiles.get(desc.GetHashCode());
			    				} finally
			    				{
			    					loadedTilesLock.unlock();
			    				}
			    				trackTilesLock.lock();
			    				try
			    				{			            
			    					if ((RouteOverlay.Routes.size() > 0) && ( x >= xFromTrack) && (x <= xToTrack) && (y >= yFromTrack) && (y <= yToTrack))
			    					{
			    						if (!trackTiles.containsKey(desc.GetHashCode()))
			    						{
			    							preemptTrackTile();
			    							
			    							trackTiles.put(desc.GetHashCode(), new Tile(desc, null, Tile.TileState.Disposed));
			    							
			    							queueTrackTile(desc);
			    						}
			    						trackTile = trackTiles.get(desc.GetHashCode());
			    					} else
			    						trackTile = null;
			    				} finally
			    				{
			    					trackTilesLock.unlock();
			    				}
			    				
			    				if ((tile != null) && (tileVisible(tile.Descriptor)))
			    				{
			    					renderTile(tile, true);
			    				}
			    				if ((trackTile != null) && (tileVisible(trackTile.Descriptor)))
			    				{
			    					renderTile(trackTile, false);
			    				}
			    			}
			    		}
		    		}
		    		catch (Exception ex)
		    		{
		    			Logger.Error("MapView.Render()","1",ex);
		    		}
		    		canvas.restore();
		    		
		    		canvasOverlay = canvas;
		    		renderCaches();
		
		    		canvas.save();
		    		canvas.rotate(-tmpCanvasHeading, width / 2, halfHeight);
		    		renderPositionAndMarker();
		    		canvas.restore();
			
		    		renderScale();
		    		
		    		RenderTargetArrow();
			
			
		    		if (renderZoomScaleActive)
		    			renderZoomScale();
			/*
			      if (loaderThread != null)
			        renderLoaderInfo();
			
		*/	
		    		if (showCompass)
		    			renderCompass();
			
		    		try
		    		{
		    			Canvas can = holder.lockCanvas(null);
		    			if (can != null)
		    			{
		    				can.drawBitmap(offScreenBmp, 0, 0, null);
		    				/*		     	      if (!debugString1.equals("") || !debugString2.equals(""))
			     	      	{
			     		      	Paint debugPaint = new Paint();
			     		      debugPaint.setTextSize(20);
			     		      debugPaint.setColor(Color.WHITE);
			     		      debugPaint.setStyle(Style.FILL);
			     		      can.drawRect(new Rect(50, 70, 300, 130), debugPaint);
			     		      debugPaint.setColor(Color.BLACK);
			     		      can.drawText(debugString1, 50, 100, debugPaint);
			     		      can.drawText(debugString2, 50, 130, debugPaint);
			     	      }*/
		    				holder.unlockCanvasAndPost(can);
		    			}
			    	  
			//        this.CreateGraphics().DrawImage(offScreenBmp, 0, 0);
		    		}
		    		catch (Exception ex)
		    		{
		    			Logger.Error("MapView.Render()","2",ex);
		    		}
		    		
		     	}
	    	} catch (Exception exc)
	    	{
	    		Logger.Error("MapView.Render()","3",exc);
	    	}
    	} finally
    	{
    	}
    }
    	
/*
    Brush orangeBrush = new SolidBrush(Color.Orange);
*/

    private void renderCompass()
    {
      // Position der Anzeigen berechnen
      int left = 10;
      int right = buttonTrackPosition.getLeft();
      int top = buttonTrackPosition.getTop();
      int compassCenter = buttonTrackPosition.getHeight() / 2;
      int bottom = buttonTrackPosition.getTop() + buttonTrackPosition.getHeight();
      int debugPos1 = 0;
      int debugPos2 = 0;
      int debugHeight = 0;
      if (!debugString1.equals(""))
      {
    	  debugHeight += 24;
    	  debugPos1 = bottom + debugHeight - 10;
      }
      if (!debugString2.equals(""))
      {
    	  debugHeight += 24;
    	  debugPos2 = bottom + debugHeight - 10;
      }

      int leftString = left + buttonTrackPosition.getHeight() + 10;

      Paint paint = new Paint();
      paint.setColor(myContext.getResources().getColor(R.color.Day_ColorCompassPanel));
      paint.setStyle(Style.FILL);
      canvasOverlay.drawRect(left, top, right, bottom + debugHeight, paint);

      // Position ist entweder GPS-Position oder die des Markers, wenn
      // dieser gesetzt wurde.
      Coordinate position = null;
      if ((Global.Marker != null) && (Global.Marker.Valid))
    	  position = Global.Marker;
      else if (Global.LastValidPosition != null)
    	  position = Global.LastValidPosition;
      else
    	  position = new Coordinate();
    	  
      // Koordinaten
      if (position.Valid)
      {
        String textLatitude = Global.FormatLatitudeDM(position.Latitude);
        String textLongitude = Global.FormatLongitudeDM(position.Longitude);

        paint = new Paint(font);
        paint.setTextAlign(Align.RIGHT);
        paint.setColor(myContext.getResources().getColor(R.color.Day_ColorCompassText));
        canvasOverlay.drawText(textLatitude, right - 5, top + compassCenter - 10, paint);
        canvasOverlay.drawText(textLongitude, right - 5, bottom - 10, paint);

        if (Global.Locator != null)
        {
        	paint.setTextAlign(Align.LEFT);
            canvasOverlay.drawText(Global.Locator.SpeedString(), leftString, top + compassCenter - 10, paint);
        }

        if (!debugString1.equals(""))
        {
        	canvasOverlay.drawText("D1: " + debugString1, leftString, debugPos1, paint);
        }
        if (!debugString2.equals(""))
        {
        	canvasOverlay.drawText("D2: " + debugString2, leftString, debugPos2, paint);
        }
      }

      // Gps empfang ?
      if (Global.SelectedCache() != null && position.Valid)
      {
        // Distanz einzeichnen
        float distance = 0;

        if (Global.SelectedWaypoint() == null)
          distance = position.Distance(Global.SelectedCache().Pos);
        else
          distance = position.Distance(Global.SelectedWaypoint().Pos);

        String text = UnitFormatter.DistanceString(distance);
        canvas.drawText(text, leftString, bottom - 10, paint);

        // Kompassnadel zeichnen
        if (Global.Locator != null)
        {
          Coordinate cache = (Global.SelectedWaypoint() != null) ? Global.SelectedWaypoint().Pos : Global.SelectedCache().Pos;
          double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude, cache.Longitude);
          double relativeBearing = bearing - Global.Locator.getHeading();
          double relativeBearingRad = relativeBearing * Math.PI / 180.0;

          int cs = canvas.save(123);
          int awidth = compassCenter * 2;
          int aheight = compassCenter * 2;
          Global.Arrows[1].setBounds(left, top, left + awidth, top + aheight);
          canvas.rotate((float)relativeBearing, (float)(left + compassCenter), (float)(top + compassCenter));
          Global.Arrows[1].draw(canvas);
          canvas.restoreToCount(cs);
//          Cachebox.Drawing.Arrow.FillArrow(graphics, Cachebox.Drawing.Arrow.HeadingArrow, blackPen, orangeBrush, left + compassCenter, top + compassCenter, compassCenter, relativeBearingRad);
        }
      }

    }
/*
    Global.BlendFunction blend = new Global.BlendFunction(100, 0);
    Bitmap blackPixelImage = new Bitmap(1, 1);
    bool alphaBlendingAvailable = true;
    Brush grayBrush = new SolidBrush(Color.DarkGray);
    private void transparentRectangle(int x, int y, int width, int height, byte alpha)
    {
      if (alphaBlendingAvailable)
      {
        try
        {
          blend.SourceConstantAlpha = alpha;

          Graphics graphicsPixel = Graphics.FromImage(blackPixelImage);
          IntPtr hdcPixel = graphicsPixel.GetHdc();
          IntPtr hdcGraphics = graphics.GetHdc();
          try
          {
            Global.AlphaBlend(hdcGraphics, x, y, width, height, hdcPixel, 0, 0, 1, 1, blend);
          }
          finally
          {
            graphics.ReleaseHdc(hdcGraphics);
            graphicsPixel.ReleaseHdc(hdcPixel);
          }

          graphicsPixel.Dispose();
          return;
        }
        catch (Exception)
        {
          alphaBlendingAvailable = false;
        }
      }

      graphics.FillRectangle(grayBrush, x, y, width, height);
    }
*/
    LinkedList<Descriptor> queuedTiles = new LinkedList<Descriptor>();
    private Lock queuedTilesLock = new ReentrantLock();
    LinkedList<Descriptor> queuedTrackTiles = new LinkedList<Descriptor>();
    private Lock queuedTrackTilesLock = new ReentrantLock();
    queueProcessor queueProcessor = null;
/*
    Thread queueProcessor = null;
*/
//    private Descriptor threadDesc;
    
    @SuppressWarnings("unchecked")
	private void queueTile(Descriptor desc)
    {
      // Alternative Implementierung mit Threadpools...
      // ThreadPool.QueueUserWorkItem(new WaitCallback(LoadTile), new Descriptor(desc));
    	
    	queuedTilesLock.lock();
    	try
    	{
    		if (queuedTiles.contains(desc.GetHashCode()))
    			return;
    		
    		queuedTiles.add(desc);
    		
    		if (queueProcessor == null)
    		{
    			queueProcessor = new queueProcessor();
    			queueProcessor.execute(queuedTiles);
    		}
    	} finally
    	{
    		queuedTilesLock.unlock();
    	}
    }

    @SuppressWarnings("unchecked")
	private void queueTrackTile(Descriptor desc)
    {
      // Alternative Implementierung mit Threadpools...
      // ThreadPool.QueueUserWorkItem(new WaitCallback(LoadTile), new Descriptor(desc));
    	
    	queuedTrackTilesLock.lock();
    	try
    	{
    		if (queuedTrackTiles.contains(desc.GetHashCode()))
    			return;
    		
    		queuedTrackTiles.add(desc);
    		
    		if (queueProcessor == null)
    		{
    			queueProcessor = new queueProcessor();
    			queueProcessor.execute(queuedTiles);
    		}
    	} finally
    	{
    		queuedTrackTilesLock.unlock();
    	}
    }

    private class queueProcessor extends AsyncTask<LinkedList<Descriptor>, Integer, Integer> {

		@Override
		protected Integer doInBackground(LinkedList<Descriptor>... params) {
			boolean queueEmpty = false;
			try
			{
				do
			    {
					Descriptor desc = null;
					if (queuedTiles.size() > 0)
					{
						try
						{
							queuedTilesLock.lock();
							try
							{
								desc = queuedTiles.poll();
					    	} finally
					    	{
					    		queuedTilesLock.unlock();
					    	}
					
							if (desc.Zoom == Zoom)
							{
								LoadTile(desc);
							}
							else
							{
								// Da das Image fur diesen Tile nicht geladen wurde, da der Zoom-Faktor des Tiles nicht gleich
								// dem aktuellen ist muss dieser Tile wieder aus loadedTile entfernt werden, da sonst bei
								// spterem Wechsel des Zoom-Faktors dieses Tile nicht angezeigt wird.
								// Dies passiert bei schnellem Wechsel des Zoom-Faktors, wenn noch nicht alle aktuellen Tiles geladen waren.
								loadedTilesLock.lock();
								try
								{
									if (loadedTiles.containsKey(desc.GetHashCode()))
									{
										Tile tile = loadedTiles.get(desc.GetHashCode());
										loadedTiles.remove(desc.GetHashCode());
										tile.destroy();
									}
								} finally
								{
									loadedTilesLock.unlock();
								}
							}
						} catch (Exception ex1)
						{
							Logger.Error("MapView.queueProcessor.doInBackground()","1",ex1);
						}
					} else if (queuedTrackTiles.size() > 0)
					{
						try
						{
							// wenn keine Tiles mehr geladen werden m�ssen, dann die TrackTiles erstellen
							desc = null;
							queuedTrackTilesLock.lock();
							try
							{
								desc = queuedTrackTiles.poll();
					    	} finally
					    	{
					    		queuedTrackTilesLock.unlock();
					    	}
					
							if (desc.Zoom == Zoom)
							{
								LoadTrackTile(desc);
							}
							else
							{
								// Da das Image fur diesen Tile nicht geladen wurde, da der Zoom-Faktor des Tiles nicht gleich
								// dem aktuellen ist muss dieser Tile wieder aus loadedTile entfernt werden, da sonst bei
								// spterem Wechsel des Zoom-Faktors dieses Tile nicht angezeigt wird.
								// Dies passiert bei schnellem Wechsel des Zoom-Faktors, wenn noch nicht alle aktuellen Tiles geladen waren.
								trackTilesLock.lock();
								try
								{
									if (trackTiles.containsKey(desc.GetHashCode()))
									{
										Tile tile = trackTiles.get(desc.GetHashCode());
										tile.destroy();
										trackTiles.remove(desc.GetHashCode());
									}
			    				} finally
			    				{
			    					trackTilesLock.unlock();
			    				}
							}			
						} catch (Exception ex2)
						{
							Logger.Error("MapView.queueProcessor.doInBackground()","2",ex2);
						}
					
					}
					queuedTilesLock.lock();
					try
					{
						queuedTrackTilesLock.lock();
						try
						{
							queueEmpty = (queuedTiles.size() < 1) && (queuedTrackTiles.size() < 1);
				    	} finally
				    	{
				    		queuedTrackTilesLock.unlock();
				    	}
			    	} finally
			    	{
			    		queuedTilesLock.unlock();
			    	}
			    } while (!queueEmpty);
			}	
			catch (Exception ex3)
			{
				Logger.Error("MapView.queueProcessor.doInBackground()","3",ex3);
			}
			finally
			{
			    // damit im Falle einer Exception der Thread neu gestartet wird
//			    queueProcessor = null;
			}
		return null;
	}

	     protected void onPostExecute(Integer result) {
	         queueProcessor = null;
	     }    	
    }
/*
    void queueProcessorEntryPoint()
    {
      bool queueEmpty = false;

      try
      {

        do
        {
          Descriptor desc = null;
          lock (queuedTiles)
            desc = queuedTiles.Dequeue();

          if (desc.Zoom == this.Zoom)
            LoadTile(desc);
          else
          {
            // Da das Image fur diesen Tile nicht geladen wurde, da der Zoom-Faktor des Tiles nicht gleich
            // dem aktuellen ist muss dieser Tile wieder aus loadedTile entfernt werden, da sonst bei
            // spterem Wechsel des Zoom-Faktors dieses Tile nicht angezeigt wird.
            // Dies passiert bei schnellem Wechsel des Zoom-Faktors, wenn noch nicht alle aktuellen Tiles geladen waren.
            if (loadedTiles.ContainsKey(desc))
              loadedTiles.Remove(desc);
          }


          lock (queuedTiles)
            queueEmpty = queuedTiles.Count < 1;

        } while (!queueEmpty);
      }
      catch (Exception exc)
      {
        string forDebug = exc.Message;
      }
      finally
      {
        // damit im Falle einer Exception der Thread neu gestartet wird
        queueProcessor = null;
      }
    }

*/
    Rect getTileRange(float rangeFactor)
    {
    	synchronized (screenCenter)
    	{
    		int x1;
    		int y1;
    		int x2;
    		int y2;
    		double x = screenCenter.X / (256 * dpiScaleFactorX);
    		double y = (screenCenter.Y - halfHeight + height / 2) / (256 * dpiScaleFactorY);
    		
    		// preload more Tiles than necessary to ensure more smooth scrolling
    		int dWidth = (int)(drawingWidth * rangeFactor);
    		int dHeight= (int)(drawingHeight * rangeFactor);
    		x1 = (int)Math.floor(x - dWidth/multiTouchFaktor / (256 * dpiScaleFactorX * 2));
    		x2 = (int)Math.floor(x + dWidth/multiTouchFaktor / (256 * dpiScaleFactorX * 2));
    		y1 = (int)Math.floor(y - dHeight/multiTouchFaktor / (256 * dpiScaleFactorY * 2));
    		y2 = (int)Math.floor(y + dHeight/multiTouchFaktor / (256 * dpiScaleFactorY * 2));
    		return new Rect(x1, y1, x2, y2);
    	}
    }

    Paint backBrush;

    Rect tileRect = new Rect(0, 0, 256, 256);

    void renderTile(Tile tile, boolean drawBestFit)
    {
      tile.Age = 0;

      Point pt = ToScreen(tile.Descriptor.X, tile.Descriptor.Y, tile.Descriptor.Zoom);
      // relativ zu Zentrum
      pt.x = pt.x - width / 2;
      pt.y = pt.y - halfHeight;
      // skalieren
      pt.x = (int) Math.round(pt.x * multiTouchFaktor + width / 2);
      pt.y = (int) Math.round(pt.y * multiTouchFaktor + halfHeight);

      if (tile.State == Tile.TileState.Present || tile.State == Tile.TileState.LowResolution)
      {
        drawImage(canvas, tile.Image, pt.x, pt.y, (int)(256.0f * dpiScaleFactorX * multiTouchFaktor), (int)(256.0f * dpiScaleFactorY * multiTouchFaktor));
 
        if (drawBestFit)
        {
	        // Draw Kachel marker
        	if(Config.GetBool("DebugShowMarker"))
        	{
		        Paint paintt = new Paint(backBrush);
		        paintt.setColor(Color.GREEN);
		        paintt.setStyle(Style.STROKE);
		        if (tile.State == Tile.TileState.LowResolution)
		        	paintt.setColor(Color.RED);
		        Rect brect = new Rect(pt.x+5, pt.y+5, pt.x + (int)(256 * dpiScaleFactorX * multiTouchFaktor)-5, pt.y + (int)(256 * dpiScaleFactorY * multiTouchFaktor)-5);
		        canvas.drawRect(brect, paintt);
		        canvas.drawLine(brect.left, brect.top, brect.right, brect.bottom, paintt);
		        canvas.drawLine(brect.right, brect.top, brect.left, brect.bottom, paintt);
        	}
        }
        return;
      }

      if (!drawBestFit) 
    	  return;
      try
      {
    	  Bitmap bit = loadBestFit(CurrentLayer, tile.Descriptor, false);
    	  if (bit != null)
    	  {
    		  // skaliere letztes Tile solange bis das Tile mit richtigem Zoom geladen ist.
    		  // um ohne Verzerrungen oder L�cken zu zoomen und scrollen
    		  tile.Image = bit;
    		  tile.State = Tile.TileState.LowResolution;
    	      drawImage(canvas, bit, pt.x, pt.y, (int)(256.0f * dpiScaleFactorX * multiTouchFaktor), (int)(256.0f * dpiScaleFactorY * multiTouchFaktor));
    	  } else
    		  canvas.drawRect(pt.x, pt.y, pt.x + (int)(256 * dpiScaleFactorX * multiTouchFaktor), pt.y + (int)(256 * dpiScaleFactorY * multiTouchFaktor), backBrush);
        //.FillRectangle(backBrush, pt.X, pt.Y, (int)(256 * dpiScaleFactorX), (int)(256 * dpiScaleFactorY));
          canvas.drawLine(pt.x, pt.y, pt.x + (int)(256 * dpiScaleFactorX * multiTouchFaktor), pt.y + (int)(256 * dpiScaleFactorY * multiTouchFaktor), fontSmall);
          canvas.drawLine(pt.x, pt.y + (int)(256 * dpiScaleFactorY * multiTouchFaktor), pt.x + (int)(256 * dpiScaleFactorX * multiTouchFaktor), pt.y, fontSmall);
      }
      catch (Exception ex)
      {
    	  Logger.Error("MapView.RenderTile", "",ex);
		  canvas.drawRect(pt.x, pt.y, pt.x + (int)(256 * dpiScaleFactorX * multiTouchFaktor), pt.y + (int)(256 * dpiScaleFactorY * multiTouchFaktor), backBrush);
      }
    }

    Point ToScreen(double x, double y, int zoom)
    {
    	synchronized (screenCenter)
    	{
    		double adjust = Math.pow(2, (Zoom - zoom));
    		x = x * adjust * 256 * dpiScaleFactorX;
    		y = y * adjust * 256 * dpiScaleFactorY;
    		
    		return new Point((int)(x - screenCenter.X) + halfWidth, (int)(y - screenCenter.Y) + halfHeight);
    	}
    }

    Point ToScreen(double x, double y, double zoom)
    {
    	synchronized (screenCenter)
    	{

    		double adjust = Math.pow(2, (Zoom - zoom));
    		x = x * adjust * 256 * dpiScaleFactorX;
    		y = y * adjust * 256 * dpiScaleFactorY;

    		return new Point((int)(x - screenCenter.X) + halfWidth, (int)(y - screenCenter.Y) + halfHeight);
    	}
    }

    /// <summary>
    /// an dieser x-Koordinate beginnt die Skala. Muss beim Resize neu gesetzt werden
    /// </summary>
    int scaleLeft;

    /// <summary>
    /// Breite des Ma�stabs
    /// </summary>
    int scaleWidth;
    Bitmap offScreenBmp = null;
    /*
    private void MapView_Resize(object sender, EventArgs e)
    {
      halfHeight = Height / 2;
      halfWidth = Width / 2;

      width = Width;
      height = Height;
        
      scaleLeft = 0;// button2.Left + button2.Width + lineHeight;
      scaleWidth = width - scaleLeft - (int)this.CreateGraphics().MeasureString("100km ", Font).Width + 1;
      zoomChanged();

      if (offScreenBmp != null)
      {
        graphics.Dispose();
        offScreenBmp.Dispose();
      }

      offScreenBmp = new Bitmap(Math.Max(Width, 1), Math.Max(Height, 1));
      graphics = Graphics.FromImage(offScreenBmp);
    }
*/
    boolean dragging = false;
    int dragStartX = 0;
    int dragStartY = 0;
    int lastClickX = 0;
    int lastClickY = 0;

    boolean arrowHitWhenDown = false;

    private void MapView_MouseDown(int eX, int eY)
    {
      dragging = true;
/*      animationTimer.Enabled = false;*/
      setLockPosition(0);

      lastClickX = dragStartX = eX;
      lastClickY = dragStartY = eY;

      arrowHitWhenDown = Math.sqrt(((eX - cacheArrowCenter.x) * (eX - cacheArrowCenter.x) + (eY - cacheArrowCenter.y) * (eY - cacheArrowCenter.y))) < (lineHeight * 1.5f);
    }

    private void MapView_MouseUp(int eX, int eY)
    {
      dragging = false;
      mouseMoved = false;
      updateCacheList();
      Render(true);
    }

    private Coordinate lastMouseCoordinate = null;

    private void MapView_MouseMove(int eX, int eY)
    {
    	boolean doRender = false;
    	try
    	{
    		PointD point = new PointD(0, 0);
    		point.X = screenCenter.X + (eX - this.width / 2) / dpiScaleFactorX;
    		point.Y = screenCenter.Y + (eY - this.halfHeight) / dpiScaleFactorY;;
    		lastMouseCoordinate = new Coordinate(Descriptor.TileYToLatitude(Zoom, point.Y / (256.0)), Descriptor.TileXToLongitude(Zoom, point.X / (256.0)));
    		
    		if (dragging)
    		{
    			screenCenter.X += dragStartX - eX;
    			screenCenter.Y += dragStartY - eY;
    			animationLock.lock();
    			try
    			{
    				animationThread.toX = screenCenter.X;
    				animationThread.toY = screenCenter.Y;
    			} finally
    			{
    				animationLock.unlock();
    			}
    			centerOsmSpace.X = screenCenter.X / dpiScaleFactorX;
    			centerOsmSpace.Y = screenCenter.Y / dpiScaleFactorY;
    			
    			dragStartX = eX;
    			dragStartY = eY;
    			
    			doRender = true;
    		} 
		} finally
		{
		}
		if (doRender)
			Render(false);
      
    }

    private void zoomIn()
    {
  		zoomIn(true);
    }
    private void zoomIn(boolean doRender)
    {
    	synchronized (screenCenter)
    	{
	    	try
	    	{
	    		animationThread.zoomTo(Zoom+1);
	    	} catch(Exception exc)
	    	{
	    		Logger.Error("MapView.zoomIn","",exc);
	    	}
    	}
    }
    private void zoomInDirect(boolean doRender)
    {
    	synchronized (screenCenter)
    	{
	      if (Zoom < maxZoom)
	      {
	    	  try
	    	  {
	    		  zoomScaleTimer.cancel();
			
	    		  centerOsmSpace.X *= 2;
	    		  centerOsmSpace.Y *= 2;
	    		  screenCenter.X *= 2;
	    		  screenCenter.Y *= 2;
	    		  animationThread.toX *= 2;
	    		  animationThread.toY *= 2;
			
	    		  Zoom++;
	    		  animationThread.toZoom = Zoom;
	    		  animationThread.toFaktor = 1;
	    		  zoomControls.setIsZoomOutEnabled(true); 
	    		  
	    		  if (Zoom >= maxZoom)
	    		  {
	    			  zoomControls.setIsZoomInEnabled(false); 
	    		  }
	    		  zoomChanged();
	    		  //  updateCacheList();
			
	    		  renderZoomScaleActive = true;
	    		  if (doRender)
	    		  {
	    			  Render(false);
	    			  startZoomScaleTimer();
	    		  }    
	      		} catch(Exception exc)
	      		{
	      			Logger.Error("MapView.zoomInDirect()","",exc);
	      		}
	      	}
    	}
    }
    	
    
    private void startZoomScaleTimer()
    {
    	if (zoomTimerTask != null)
    		return;
    	try
    	{
	        zoomTimerTask = new TimerTask() {
	        	@Override
	        	public void run()
	        	{
	        		renderZoomScaleActive = false;
	        		try
	        		{
	        			Render(false);
	        			zoomTimerTask = null;
	        		} catch (Exception exc)
	        		{
	        			Logger.Error("MapView.ZoomTimerTask","",exc);
						return;
	        		}
	        	}
	        };
	        zoomScaleTimer = new Timer();
	        zoomScaleTimer.schedule(zoomTimerTask, 1000);
    	} catch(Exception exc)
    	{
    		Logger.Error("MapView.startZoomScaleTimer()","",exc);
    	}
    }
/*
    Brush redBrush = new SolidBrush(Color.Red);
    Pen blackPen = new Pen(Color.Black);
    Pen bluePen = new Pen(Color.Magenta);
    Pen redPen = new Pen(Color.Red, 2);
    Pen goldenrodPen = new Pen(Color.Goldenrod, 2);
    Brush blueBrush = new SolidBrush(Color.Blue);
    Brush goldenrodBrush = new SolidBrush(Color.Goldenrod);
    Pen lightBluePen = new Pen(Color.LightBlue);
*/
    void renderPositionAndMarker()
    {

    	if (Global.Locator != null)
    	{
	        // Position auf der Karte
	        Point pt = ToScreen(Descriptor.LongitudeToTileX(Zoom, Global.LastValidPosition.Longitude), Descriptor.LatitudeToTileY(Zoom, Global.LastValidPosition.Latitude), Zoom);
	
	        int size = lineHeight;
	
/*	        debugString1 = String.valueOf(Global.Locator.getCompassHeading());
	        if (Global.Locator.getLocation() != null)
	        	debugString2 = Global.Locator.getLocation().getBearing() + " - " + Global.Locator.getLocation().getSpeed() * 3600 / 1000 + "kmh";
	        else
	        	debugString2 = "";*/
	        
	        double courseRad = Global.Locator.getHeading() * Math.PI / 180.0;
	        boolean lastUsedCompass = Global.Locator.LastUsedCompass;
	        float dirX = (float)Math.sin(courseRad);
	        float dirY = (float)-Math.cos(courseRad);
	
	        Point[] dir = new Point[3];
	        dir[0] = new Point();
	        dir[0].x = (int)(pt.x + dirX * size * 0.75f);
	        dir[0].y = (int)(pt.y + dirY * size * 0.75f);
	
	        // x/y -> -y/x
	        dir[1] = new Point();
	        dir[1].x = (int)(pt.x - dirY * size / 3.0f - dirX * size * 0.25f);
	        dir[1].y = (int)(pt.y + dirX * size / 3.0f - dirY * size * 0.25f);
	
	        dir[2] = new Point();
	        dir[2].x = (int)(pt.x + dirY * size / 3.0f - dirX * size * 0.25f);
	        dir[2].y = (int)(pt.y - dirX * size / 3.0f - dirY * size * 0.25f);
	
	        float[] verts = new float[6];
	        for (int i = 0; i < 3; i++)
	        {
	        	verts[i*2] = dir[i].x;
	        	verts[i*2+1] = dir[i].y;	        
	        }

	        Path path = new Path();
	        path.moveTo(dir[0].x, dir[0].y);
	        path.lineTo(dir[1].x, dir[1].y);
	        path.lineTo(dir[2].x, dir[2].y);
	        path.lineTo(dir[0].x, dir[0].y);
	        Paint paint = new Paint();
	        if (lastUsedCompass)
	        	paint.setColor(Color.BLACK);	// bei magnet. Kompass
	        else
	        	paint.setColor(Color.RED);		// bei GPS Kompass
	        paint.setStyle(Style.FILL);
	        canvas.drawPath(path, paint);
	        
	        if ((Global.Locator.getLocation() != null) && (Global.Locator.getLocation().hasAccuracy()))
	        {
	        	float radius = Global.Locator.getLocation().getAccuracy();
//	        	debugString1 = String.valueOf(radius) + "m";
	        	Paint circlePaint = new Paint();
	        	circlePaint.setColor(Color.argb(55, 0, 0, 0));
	        	circlePaint.setStrokeWidth(5);
	        	canvas.drawCircle(pt.x, pt.y, (float) (pixelsPerMeter * radius), circlePaint);
	        }
    	}
/*	
	      // Marker rendern
	    if (Global.Marker.Valid)
	    {
	        Point pt = ToScreen(Descriptor.LongitudeToTileX(Zoom, Global.Marker.Longitude), Descriptor.LatitudeToTileY(Zoom, Global.Marker.Latitude), Zoom);
	        int width = lineHeight / 3;
	        graphics.FillEllipse(redBrush, pt.X - width, pt.Y - width, width + width, width + width);
	        graphics.DrawEllipse(blackPen, pt.X - width, pt.Y - width, width + width, width + width);
	    }
*/
    }

    int minZoom;
    int maxZoom;
    
    private void zoomOut()
    {
    	zoomOut(true);
    }
    private void zoomOut(boolean doRender)
    {
    	synchronized (screenCenter)
    	{
	    	try
	    	{
	    		animationThread.zoomTo(Zoom-1);
	    	} catch(Exception exc)
	    	{
	    		Logger.Error("MapView.zoomOut()","",exc);
	    	}
    	}
    }
    private void zoomOutDirect(boolean doRender)
    {
    	synchronized (screenCenter)
    	{
	    	if (Zoom > minZoom)
	    	{
	    		try
	    		{
	    			zoomScaleTimer.cancel();
	    			
	    			screenCenter.X /= 2;
	    			screenCenter.Y /= 2;
	    			centerOsmSpace.X /= 2;
	    			centerOsmSpace.Y /= 2;
	    			animationThread.toX /= 2;
	    			animationThread.toY /= 2;
	    			
	    			Zoom--;
	    			animationThread.toZoom = Zoom;
	    			animationThread.toFaktor = 1;
	    			zoomControls.setIsZoomInEnabled(true); 
	
	    			if (Zoom == minZoom)
	    			{
	    				zoomControls.setIsZoomOutEnabled(false);
	    			}
	    			
	    			zoomChanged();
	    			//        updateCacheList();
	    			renderZoomScaleActive = true;
	    			if (doRender)
	    			{
	    				Render(false);
	    				startZoomScaleTimer();
	    			}
	    		} catch(Exception exc)
	    		{
	    			Logger.Error("MapView.zoomOutDirect()","",exc);
	    		}
	    		
	    	}
    	}
    }

    double pixelsPerMeter;

    /// <summary>
    /// Anzahl der Schritte auf dem Ma�stab
    /// </summary>
    int scaleUnits = 10;

    /// <summary>
    /// L�nge des Ma�stabs in Metern
    /// </summary>
    double scaleLength = 1000;


    /// <summary>
    /// Nachdem Zoom ver�ndert wurde m�ssen einige Werte neu berechnet werden
    /// </summary>
    private void zoomChanged()
    {
    	try
    	{
    		int[] scaleNumUnits = new int[] { 4, 3, 4, 3, 4, 5, 3 };
    		float[] scaleSteps = new float[] { 1, 1.5f, 2, 3, 4, 5, 7.5f };
/*
      if (animationTimer != null)
        animationTimer.Enabled = false;
*/
    		adjustmentCurrentToCacheZoom = Math.pow(2, Zoom - Cache.MapZoomLevel);

    		// Infos f�r den Ma�stab neu berechnen
    		Coordinate dummy = Coordinate.Project(center.Latitude, center.Longitude, 90, 1000);
    		double l1 = Descriptor.LongitudeToTileX(Zoom, center.Longitude);
    		double l2 = Descriptor.LongitudeToTileX(Zoom, dummy.Longitude);
    		double diff = Math.abs(l2 - l1);
    		pixelsPerMeter = (diff * 256 * dpiScaleFactorX * multiTouchFaktor) / 1000;
    		
    		int multiplyer = 1;
    		double scaleSize = 0;
    		int idx = 0;
    		while (scaleSize < (scaleWidth * 0.45))
    		{
    			scaleLength = multiplyer * scaleSteps[idx] * ((UnitFormatter.ImperialUnits) ? 1.6093 : 1);
    			scaleUnits = scaleNumUnits[idx];
    			
    			scaleSize = pixelsPerMeter * scaleLength;
    			
    			idx++;
    			if (idx == scaleNumUnits.length)
    			{
    				idx = 0;
    				multiplyer *= 10;
    			}
    		}
    	} catch(Exception exc)
    	{
    		Logger.Error("MapView.zoomChanged()","",exc);
    	}
    }
  /*
    Brush[] brushes = new Brush[] { new SolidBrush(Color.Black), new SolidBrush(Color.White) };
*/
    Paint font = new Paint();
    Paint fontSmall = new Paint();

    /// <summary>
    /// Zeichnet den Ma�stab. pixelsPerKm muss durch zoomChanged
    /// initialisiert sein! und graphics auch!
    /// </summary>
    private void renderScale()
    {
      int pos = 0;
      int start = 0;
      Paint[] brushes = new Paint[2];
      brushes[0] = new Paint();
      brushes[0].setColor(Color.BLACK);
      brushes[0].setStyle(Style.FILL);
      brushes[1] = new Paint();
      brushes[1].setColor(Color.WHITE);
      brushes[1].setStyle(Style.FILL);
      
      for (int i = 1; i <= scaleUnits; i++)
      {
        pos = (int)(scaleLength * ((double)i / scaleUnits) * pixelsPerMeter);
        
        canvasOverlay.drawRect(new Rect(start + scaleLeft, height - lineHeight / 2 - lineHeight / 4, pos + scaleLeft, height - lineHeight / 4), brushes[i % 2]);
        start = pos;
      }

      Paint blackPen = new Paint();
      blackPen.setColor(Color.BLACK);
      blackPen.setStyle(Style.STROKE);
      canvasOverlay.drawRect(new Rect(scaleLeft - 1, height - lineHeight / 2 - lineHeight / 4, scaleLeft + pos, height - lineHeight / 4), blackPen);

      String distanceString;
      
      if (UnitFormatter.ImperialUnits)
      {
//    	  distanceString = String.format("{0:0.00}mi", scaleLength / 1609.3);
          NumberFormat nf = NumberFormat.getInstance();
          nf.setMaximumFractionDigits(2);
          distanceString = nf.format(scaleLength / 1609.3) + "mi";
      }
      else
        if (scaleLength <= 500)
        {
          NumberFormat nf = NumberFormat.getInstance();
          nf.setMaximumFractionDigits(0);
          distanceString = nf.format(scaleLength) + "m";
        }
        else
        {
          double length = scaleLength / 1000;
          NumberFormat nf = NumberFormat.getInstance();
          nf.setMaximumFractionDigits(0);
          distanceString = nf.format(length) + "km";
      }

      canvasOverlay.drawText(distanceString, scaleLeft + pos + lineHeight / 2, height, font);
      //graphics.DrawString(distanceString, font, brushes[0], scaleLeft + pos + lineHeight / 2, height - lineHeight);
    }

    private void MapView_Click(int eX, int eY)
    {
        if (arrowHitWhenDown && Math.sqrt(((eX - cacheArrowCenter.x) * (eX - cacheArrowCenter.x) + (eY - cacheArrowCenter.y) * (eY - cacheArrowCenter.y))) < (lineHeight * 1.5f))
        {
          Coordinate target = (Global.SelectedWaypoint() != null) ? new Coordinate(Global.SelectedWaypoint().Latitude(), Global.SelectedWaypoint().Longitude()) : new Coordinate(Global.SelectedCache().Latitude(), Global.SelectedCache().Longitude());

          startAnimation(target);
          cacheArrowCenter.x = Integer.MIN_VALUE;
          cacheArrowCenter.y = Integer.MIN_VALUE;
          arrowHitWhenDown = false;
          return;
        }


      WaypointRenderInfo minWpi = new WaypointRenderInfo();
      minWpi.Cache = null;

      int minDist = Integer.MAX_VALUE;
      // �berpr�fen, auf welchen Cache geklickt wurde
      for (int i = wpToRender.size() - 1; i >= 0; i--)
      {
        WaypointRenderInfo wpi = wpToRender.get(i);
        int x = (int)(wpi.MapX * adjustmentCurrentToCacheZoom * dpiScaleFactorX - screenCenter.X) + halfWidth;
        int y = (int)(wpi.MapY * adjustmentCurrentToCacheZoom * dpiScaleFactorY - screenCenter.Y) + halfHeight;

        int xd = lastClickX - x;
        int yd = lastClickY - y;

        int dist = xd * xd + yd * yd;
        if (dist < minDist)
        {
          minDist = dist;
          minWpi = wpi;
        }
      }

      if (minWpi.Cache == null)
        return;

      int legalWidth = (int)(minWpi.Icon.copyBounds().width() * dpiScaleFactorX * 1.5f);

      if (minDist > (legalWidth * legalWidth))
        return;

      if (minWpi.Waypoint != null)
      {
        // Wegpunktliste ausrichten
        Global.SelectedWaypoint(minWpi.Cache, minWpi.Waypoint);
        //FormMain.WaypointListPanel.AlignSelected();
        updateCacheList();
        Render(true);
      }
      else
      {
        // Cacheliste ausrichten
        Global.SelectedCache(minWpi.Cache);
        //                Global.SelectedWaypoint = null;
        //                FormMain.CacheListPanel.AlignSelected();
        updateCacheList();
        Render(true);
      }
      // Shutdown Autoresort
      Global.autoResort = false;
//      this.Focus();
    }
/*
    private void button3_Click(object sender, EventArgs e)
    {
      if (Global.Marker.Valid)
      {
//        tabButtonTrackPosition.Down = true;
        startAnimation(new Coordinate(Global.Marker.Latitude, Global.Marker.Longitude));
        return;
      }

      if (Global.LastValidPosition != null && Global.LastValidPosition.Valid)
      {
//        tabButtonTrackPosition.Down = true;
        startAnimation(Global.LastValidPosition);
        return;
      }
    }

*/
    /// <summary>
    /// Ausgangspunkt der Animation
    /// </summary>
    PointD animateFrom = new PointD(0, 0);

    /// <summary>
    /// Zielpunkt der Animation
    /// </summary>
    PointD animateTo = new PointD(0, 0);
/*
    // Zeitpunkt des Startes der Animation
    long animationStart;

    /// <summary>
    /// Dauer der Animation in ms
    /// </summary>
    const int animationDuration = 500;
*/
    void startAnimation(Coordinate target)
    {
    	if (animationThread == null)
    		return;
    	animationThread.moveTo(target);
    	
//    	animationStart = Environment.TickCount;
/*    	animateFrom.X = screenCenter.X;
    	animateFrom.Y = screenCenter.Y;

    	animateTo.X = dpiScaleFactorX * 256 * Descriptor.LongitudeToTileX(Zoom, target.Longitude);
    	animateTo.Y = dpiScaleFactorY * 256 * Descriptor.LatitudeToTileY(Zoom, target.Latitude);

    	screenCenter.X = animateTo.X;
        screenCenter.Y = animateTo.Y;

        centerOsmSpace.X = screenCenter.X / dpiScaleFactorX;
        centerOsmSpace.Y = screenCenter.Y / dpiScaleFactorY;
        updateCacheList();
        Render(false);
*/    	
    	/*
      double xDiff = animateFrom.X - animateTo.X;
      double yDiff = animateFrom.Y - animateTo.Y;

      center = target;

      if (Math.Sqrt(xDiff * xDiff + yDiff * yDiff) < 2 * 256 * dpiScaleFactorX)
        animationTimer.Enabled = true;
      else
      {
        // Zu weit! Wir gehen ohne Animation direkt zum Ziel!
        screenCenter.X = animateTo.X;
        screenCenter.Y = animateTo.Y;
        centerOsmSpace.X = screenCenter.X / dpiScaleFactorX;
        centerOsmSpace.Y = screenCenter.Y / dpiScaleFactorY;
        updateCacheList();
        Render(false);
      }
  */
    }
/*
    private void animationTimer_Tick(object sender, EventArgs e)
    {
      double scale = Math.Min(1.0, ((double)Environment.TickCount - (double)animationStart) / (double)animationDuration);

      double x = animateFrom.X + (animateTo.X - animateFrom.X) * scale;
      double y = animateFrom.Y + (animateTo.Y - animateFrom.Y) * scale;

      screenCenter.X = Math.Round(x);
      screenCenter.Y = Math.Round(y);

      centerOsmSpace.X = screenCenter.X / dpiScaleFactorX;
      centerOsmSpace.Y = screenCenter.Y / dpiScaleFactorY;

      if (scale == 1.0)
      {
        animationTimer.Enabled = false;
        updateCacheList();
      }

      Render(false);
    }

    Brush blackBrush = new SolidBrush(Color.Black);
    Brush whiteBrush = new SolidBrush(Color.White);
    Brush RedBrush = new SolidBrush(Color.Red);
*/
    boolean renderZoomScaleActive = false;

    private void renderZoomScale()
    {
//      int topRow = bZoomIn.Top + bZoomIn.Height + bZoomIn.Height / 2;
//      int bottomRow = bZoomOut.Top - bZoomOut.Height / 2;
//      int centerColumn = bZoomIn.Left + bZoomIn.Width / 2;
//      int halfWidth = (bZoomIn.Width / 5);
      int topRow = 50;
      int bottomRow = height - 50;
      int centerColumn = 50;
      int halfWidth = 20;
      float dist = 20;

      Paint paint = new Paint();
      paint.setColor(Color.BLACK);
      canvasOverlay.drawLine(centerColumn, topRow, centerColumn, bottomRow, paint);

      float numSteps = maxZoom - minZoom;
      for (int i = minZoom; i <= maxZoom; i++)
      {
        int y = (int)((1 - ((float)(i - minZoom)) / numSteps) * (bottomRow - topRow)) + topRow;
        dist = (bottomRow - topRow) / numSteps;
        
    	Paint font = new Paint();
    	font.setTextSize(24);
    	font.setFakeBoldText(true);
    	Paint white = new Paint();
    	white.setColor(Color.WHITE);
    	white.setStyle(Style.FILL);
    	Paint black = new Paint();
    	black.setColor(Color.BLACK);
    	black.setStyle(Style.STROKE);

        if (i == Zoom)
        {
        	y += dist*(1-multiTouchFaktor) * 1.5;
        	String label = String.valueOf(Zoom);
        	int textWidth = (int)font.measureText(label);
        	int textHeight = 28;
        	Rect bounds = new Rect();
        	font.getTextBounds(label, 0, label.length(), bounds);
        	textWidth = bounds.width();
        	textHeight = (int)(bounds.height() * 1.5);
        	canvasOverlay.drawRect(new Rect(centerColumn - textWidth / 2 - lineHeight / 2, y - textHeight / 2, centerColumn - textWidth / 2 - lineHeight / 2 + textWidth + lineHeight, y - textHeight / 2 + textHeight), white);
        	canvasOverlay.drawRect(new Rect(centerColumn - textWidth / 2 - 1 - lineHeight / 2, y - textHeight / 2 - 1, centerColumn - textWidth / 2 - 1 - lineHeight / 2 + textWidth + lineHeight + 1, y - textHeight / 2 - 1 + textHeight + 1), black);
        	canvasOverlay.drawText(label, centerColumn - textWidth / 2, y + textHeight / 2, font);
        }
        else
        	canvasOverlay.drawLine(centerColumn - halfWidth, y, centerColumn + halfWidth, y, black);
      }
    }
/*
    private void zoomScaleTimer_Tick(object sender, EventArgs e)
    {
      zoomScaleTimer.Enabled = false;
      renderZoomScaleActive = false;
      Render(false);
    }

    void renderLoaderInfo()
    {
      int tilesToLoad;
      lock (wishlist)
        tilesToLoad = wishlist.Count + 1;

      String info = (tilesToLoad == 1) ? "1 Tile, " : tilesToLoad.ToString() + " Tiles, ";
      info += Global.GetLengthString(Global.TransferredBytes);

      Global.PutImageTargetHeight(graphics, Global.Icons[3], scaleLeft, height - lineHeight * 2, lineHeight);
      graphics.DrawString(info, font, blackBrush, scaleLeft + lineHeight + lineHeight / 3, height - lineHeight * 2);
    }

    private void button3_Click_1(object sender, EventArgs e)
    {
      mapMenu.Show(this, button3.Left + button3.Width, button3.Top, 0);
    }

    private void tsiRemoveCenter_Click(object sender, EventArgs e)
    {
      if (MessageBox.Show("Really remove marker?", "Remove marker", MessageBoxButtons.YesNo, MessageBoxIcon.Question, MessageBoxDefaultButton.Button1) == DialogResult.Yes)
      {
        Global.Marker.Valid = false;
        tsiRemoveCenter.Enabled = false;
        Render(true);
        CacheListView.View.RefreshDistances();
        CacheListView.View.Refresh();
        WaypointView.View.Refresh();
      }
    }

    private void tsiSetCenter_Click(object sender, EventArgs e)
    {
      if (lastMouseCoordinate != null)
        Global.SetMarker(lastMouseCoordinate);
      else 
        Global.SetMarker(Center);
      Global.LastValidPosition = Global.Marker;
      //markerWaypoint.Latitude = Global.Marker.Latitude;
      //markerWaypoint.Longitude = Global.Marker.Longitude;
      tsiRemoveCenter.Enabled = true;
//      Render(true);
    }

    private void hideFinds(ClickButton sender)
    {
      hideMyFinds = !hideMyFinds;
      hideFindsButton.ButtonImage = (hideMyFinds) ? Global.Icons[6] : Global.Icons[7];
      Config.Set("MapHideMyFinds", hideMyFinds);
      Config.AcceptChanges();
      updateCacheList();
      Render(false);
    }

    void showRatingChanged(ClickButton sender)
    {
      showRating = !showRating;
      showRatingButton.ButtonImage = (showRating) ? Global.Icons[6] : Global.Icons[7];

      Config.Set("MapShowRating", showRating);
      Config.AcceptChanges();
      Render(false);
    }

    void showDTChanged(ClickButton sender)
    {
      showDT = !showDT;
      showDTButton.ButtonImage = (showDT) ? Global.Icons[6] : Global.Icons[7];

      Config.Set("MapShowDT", showDT);
      Config.AcceptChanges();
      Render(false);
    }

    void showCompassChanged(ClickButton sender)
    {
      showCompass = !showCompass;
      showCompassButton.ButtonImage = (showCompass) ? Global.Icons[6] : Global.Icons[7];

      Config.Set("MapShowCompass", showCompass);
      Config.AcceptChanges();
      Render(false);
    }

    void showTitlesChanged(ClickButton sender)
    {
      showTitles = !showTitles;
      showTitlesButton.ButtonImage = (showTitles) ? Global.Icons[6] : Global.Icons[7];

      Config.Set("MapShowTitles", showTitles);
      Config.AcceptChanges();
      Render(false);
    }

    void showMarkerMenu(ClickButton sender)
    {
      markerMenu.Show(this, button3.Left + button3.Width, button3.Top, 0);
    }

    void showRouteMenu(ClickButton sender)
    {
      routeMenu.Show(this, button3.Left + button3.Width, button3.Top, 0);
    }

    void showViewMenu(ClickButton sender)
    {
      viewMenu.Show(this, button3.Left + button3.Width, button3.Top, 0);
    }

    void showNavigationDialog(ClickButton sender)
    {
      if (Global.SelectedCache == null)
      {
        MessageBox.Show("Please select a waypoint and try again!", "Route generation failed!");
        return;
      }

      if ((Global.Locator == null || !Global.Locator.LastValidPosition.Valid) && !Global.Marker.Valid)
      {
        MessageBox.Show("Routes are generated between your current position and the selected waypoint. Please wait for a GPS fix or set the marker!", "Route generation failed!");
        return;
      }

      if (!Config.GetBool("AllowRouteInternet"))
      {
        MessageBox.Show("Querying the Routing Service requires an internet connection! Please enable Route Calculation to continue!", "Route Calculation disabled!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1);
        return;
      }

      Cachebox.Views.Forms.NavigationForm navigationForm = new Cachebox.Views.Forms.NavigationForm();
      if (navigationForm.ShowDialog() == DialogResult.OK)
      {
        Cursor.Current = Cursors.WaitCursor;
        HttpWebRequest webRequest = null;
        HttpWebResponse webResponse = null;
        try
        {
          webRequest = (HttpWebRequest)WebRequest.Create(Config.GetString("NavigationProvider"));

          webRequest.UserAgent = "cachebox rev " + Global.CurrentRevision.ToString() + ((Global.RevisionSuffix.Length > 0) ? "(" + Global.RevisionSuffix + ")" : "");
          webRequest.Timeout = 17000;
          webRequest.Method = "POST";
          webRequest.ContentType = "application/x-www-form-urlencoded";
          webRequest.Proxy = Global.Proxy;

          Coordinate start = (Global.Marker.Valid) ? new Coordinate(Global.Marker.Latitude, Global.Marker.Longitude) : Global.Locator.LastValidPosition;
          Coordinate end = (Global.SelectedWaypoint != null) ? new Coordinate(Global.SelectedWaypoint.Latitude, Global.SelectedWaypoint.Longitude) : new Coordinate(Global.SelectedCache.Latitude, Global.SelectedCache.Longitude);

          bool motorways = navigationForm.radioButtonCar.Checked;

          String pref = "Fastest";

          if (navigationForm.radioButtonPedestrian.Checked)
            pref = "Pedestrian";

          if (navigationForm.radioButtonBike.Checked)
            pref = "Bicycle";

          String parameters = "Start=" + start.Longitude.ToString(NumberFormatInfo.InvariantInfo) + "," + start.Latitude.ToString(NumberFormatInfo.InvariantInfo) +
              "&End=" + end.Longitude.ToString(NumberFormatInfo.InvariantInfo) + "," + end.Latitude.ToString(NumberFormatInfo.InvariantInfo) +
              "&Via=&lang=de&distunit=KM&routepref=" + pref +
              "&avoidAreas=&useTMC=" + (navigationForm.checkBoxTMC.Checked && navigationForm.radioButtonCar.Checked).ToString().ToLower() +
              "&noMotorways=" + (!motorways).ToString().ToLower() +
              "&noTollways=" + (!motorways).ToString().ToLower() +
              "&instructions=false&_=";

          webRequest.ContentLength = parameters.Length;

          Stream requestStream = webRequest.GetRequestStream();
          StreamWriter stOut = new StreamWriter(requestStream, System.Text.Encoding.ASCII);
          stOut.Write(parameters);
          stOut.Close();

          webResponse = (HttpWebResponse)webRequest.GetResponse();

          if (!webRequest.HaveResponse)
          {
            MessageBox.Show("Cannot connect to navigation provider!", "Routing failed!", MessageBoxButtons.OK, MessageBoxIcon.Question, MessageBoxDefaultButton.Button1);
            return;
          }

          TextReader response = new StreamReader(webResponse.GetResponseStream());

          String line = "";

          Route route = new Route(new Pen(Color.Purple, 4), "RouteOverlay");
          route.ShowRoute = true;

          int skip = 2;
          while ((line = response.ReadLine()) != null)
          {
            if (line.IndexOf("<xls:Error ") >= 0)
            {
              int errorIdx = line.IndexOf("message=\"");
              int endIdx = line.IndexOf("\"", errorIdx + 9);
              String errorMessage = line.Substring(errorIdx + 9, endIdx - errorIdx - 9);
              MessageBox.Show(errorMessage, "An error occured!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1);
              return;
            }

            int idx;
            if ((idx = line.IndexOf("<gml:pos>")) > 0)
            {
              int seperator = line.IndexOf(" ", idx + 1);
              int endIdx = line.IndexOf("</gml:pos>", seperator + 1);

              String lonStr = line.Substring(idx + 9, seperator - idx - 9);
              String latStr = line.Substring(seperator + 1, endIdx - seperator - 1);

              double lat = double.Parse(latStr, NumberFormatInfo.InvariantInfo);
              double lon = double.Parse(lonStr, NumberFormatInfo.InvariantInfo);

              PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, lon), Descriptor.LatitudeToTileY(projectionZoomLevel, lat));

              if (skip <= 0)
                route.Points.Add(projectedPoint);

              skip--;
            }
          }

          response.Close();

          Routes[0] = route;

          ClearTileCache();

          Render(true);
        }
        catch (WebException exc)
        {
#if DEBUG
          Global.AddLog(exc.ToString());
#endif
          MessageBox.Show("The request to OpenRouteService timed out! Please try again later.", "Timeout!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1);
          return;
        }
        catch (Exception exc)
        {
#if DEBUG
          Global.AddLog(exc.ToString());
#endif
        }
        finally
        {
          Cursor.Current = Cursors.Default;
          if (webResponse != null)
            webResponse.Close();

          navigationForm.Dispose();
        }
      }
    }

    void showLayerMenu(ClickButton sender)
    {
      layerMenu.Show(this, button3.Left + button3.Width, button3.Top, 0);
    }

    private void enableNightmodeChanged(ClickButton sender)
    {
      nightMode = !nightMode;
      nightmodeButton.ButtonImage = (nightMode) ? Global.Icons[6] : Global.Icons[7];

      Config.Set("nightMode", nightMode);
      Config.AcceptChanges();

      lock (loadedTiles)
      {
        loadedTiles.Clear();
        Render(true);
      }

      //this.Render(false);
    }

    public void layerClick(ClickButton sender)
    {
      CurrentLayer = (Layer)sender.Tag;

      foreach (ClickButton layerButton in layerButtons)
        layerButton.ButtonImage = (CurrentLayer == (Layer)layerButton.Tag) ? Global.Icons[6] : Global.Icons[7];

      Config.Set("CurrentMapLayer", CurrentLayer.Name);
      Config.AcceptChanges();

      lock (loadedTiles)
      {
        ClearCachedTiles();
        Render(true);
      }

    }
*/
    public void ClearCachedTiles()
    {
    	loadedTilesLock.lock();
    	try
    	{
    		for (long hash : loadedTiles.keySet())
    			if (loadedTiles.get(hash).Image != null)
    				loadedTiles.get(hash).Image.recycle();
    		
    		loadedTiles.clear();
        } finally
        {
      	  loadedTilesLock.unlock();
        }
    }
    Point cacheArrowCenter = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
    /*
    Font distanceFont = new Font(FontFamily.GenericSansSerif, 9, FontStyle.Regular);
*/
    public void RenderTargetArrow()
    {
      cacheArrowCenter.x = Integer.MIN_VALUE;
      cacheArrowCenter.y = Integer.MAX_VALUE;

      if (Global.SelectedCache() == null)
        return;

      Coordinate coord = (Global.SelectedWaypoint() != null) ? Global.SelectedWaypoint().Pos : Global.SelectedCache().Pos;
      
//      float distance = Datum.WGS84.Distance(center.Latitude, center.Longitude, lat, lon);
      float distance = center.Distance(coord);

      double x = 256.0 * Descriptor.LongitudeToTileX(Zoom, coord.Longitude) * dpiScaleFactorX;
      double y = 256.0 * Descriptor.LatitudeToTileY(Zoom, coord.Latitude) * dpiScaleFactorY;

      int halfHeight = height / 2;
      int halfWidth = width / 2;

      double dirx = x - screenCenter.X;
      double diry = y - screenCenter.Y + this.halfHeight - this.height / 2;

      float[] poi = {(float) dirx , (float) diry};
      float[] res = new float[2];
      Matrix mat = new Matrix();
      mat.preRotate(-canvasHeading);
      mat.mapPoints(res, poi);
      dirx = res[0];
      diry = res[1];
      

      //  if (!(Math.Abs(dirx) > (width / 2) || Math.Abs(diry) > (height / 2)))
      // Ziel sichtbar, Pfeil nicht rendern
      //     return;

      double cx = dirx;
      double cy = diry;

      int toprow = -halfHeight + ((showCompass) ? (buttonTrackPosition.getTop() + buttonTrackPosition.getHeight()) : 0);

      if (cy > halfHeight - lineHeight)
      {
        cx = clip(0, 0, cy, cx, halfHeight - lineHeight);
        cy = halfHeight - lineHeight;
      }

      if (cy < toprow)
      {
        cx = clip(0, 0, cy, cx, toprow);
        cy = toprow;
      }

      if (cx > halfWidth)
      {
        cy = clip(0, 0, cx, cy, halfWidth);
        cx = halfWidth;
      }

      if (cx < -halfWidth)
      {
        cy = clip(0, 0, cx, cy, -halfWidth);
        cx = -halfWidth;
      }
/*
      clipX2 = cx;
      clipY2 = cy;
      clipLineCircle(-halfWidth, -halfHeight, buttonTrackPosition.getWidth() * 1.5, 0, 0);
      clipLineCircle(halfWidth, -halfHeight, buttonTrackPosition.getWidth()* 1.5, 0, 0);
      clipLineCircle(-halfWidth, halfHeight, buttonTrackPosition.getWidth()* 1.5, 0, 0);
      clipLineCircle(halfWidth, halfHeight, buttonTrackPosition.getWidth()* 1.5, 0, 0);
      cx = clipX2;
      cy = clipY2;
*/
      // Position auf der Karte
      Point pt = new Point((int)(cx + halfWidth), (int)(cy + halfHeight));

      double length = Math.sqrt(cx * cx + cy * cy);
      
      int size = lineHeight;

      float dirX = -(float)(cx / length);
      float dirY = -(float)(cy / length);

      float crossX = -dirY;
      float crossY = dirX;

      Point[] dir = new Point[3];
      dir[0] = new Point((int)(pt.x), (int)(pt.y));

      // x/y -> -y/x
      dir[1] = new Point();
      dir[1].x = (int)(pt.x + dirX * 1.5f * size - crossX * size * 0.5f);
      dir[1].y = (int)(pt.y + dirY * 1.5f * size - crossY * size * 0.5f);

      dir[2] = new Point();
      dir[2].x = (int)(pt.x + dirX * 1.5f * size + crossX * size * 0.5f);
      dir[2].y = (int)(pt.y + dirY * 1.5f * size + crossY * size * 0.5f);

      if (Math.abs(dirx) > (width / 2) || Math.abs(diry) > (height / 2))
      {
/*        graphics.FillPolygon(redBrush, dir);
        graphics.DrawPolygon(blackPen, dir);*/
    	  Paint paint = new Paint();
    	  paint.setStyle(Style.FILL);
    	  paint.setColor(Color.RED);
    	  Path path = new Path();
    	  path.moveTo(dir[0].x, dir[0].y);
    	  path.lineTo(dir[1].x, dir[1].y);
    	  path.lineTo(dir[2].x, dir[2].y);
    	  path.lineTo(dir[0].x, dir[0].y);
    	  canvas.drawPath(path, paint);
    	  paint.setStyle(Style.STROKE);
    	  paint.setColor(Color.BLACK);
    	  canvas.drawPath(path, paint);    	  
      }

      float fontCenterX = pt.x + dirX * 2.2f * size;
      float fontCenterY = pt.y + dirY * 2.2f * size;

      // Anzeige Pfeile zum Ziel auf Karte mit Waypoint abfrage
      String text = UnitFormatter.DistanceString(distance);
/*
      SizeF textSize = graphics.MeasureString(text, distanceFont);

      if (Math.abs(dirx) > (width / 2) || Math.abs(diry) > (height / 2))
        graphics.DrawString(text, distanceFont, blackBrush, fontCenterX - textSize.Width / 2, fontCenterY - textSize.Height / 2);
*/
      cacheArrowCenter.x = (int)(pt.x + dirX * 1.5f * size);
      cacheArrowCenter.y = (int)(pt.y + dirY * 1.5f * size);
    }

    private double clipX2;
    private double clipY2;
    
    private void clipLineCircle(double cx, double cy, double r, double x1, double y1)
    {
      if (((cx - clipX2) * (cx - clipX2) + (cy - clipY2) * (cy - clipY2)) > r * r)
        return;

      double a = (clipX2 - x1) * (clipX2 - x1) + (clipY2 - y1) * (clipY2 - y1);
      double b = 2 * ((clipX2 - x1) * (x1 - cx) + (clipY2 - y1) * (y1 - cy));
      double c = cx * cx + cy * cy + x1 * x1 + y1 * y1 - 2 * (cx * x1 + cy * y1) - r * r;

      double u = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
      double behaviour = b * b - 4 * a * c;

      clipX2 = x1 + u * (clipX2 - x1);
      clipY2 = y1 + u * (clipY2 - y1);
    }

    double clip(double a, double b, double c, double d, double clip)
    {
      return b + ((clip - a) / (c - a)) * (d - b);
    }
/*

    internal void UpdateLayerButtons()
    {
      float heightMultiplier = 2.5f;

      if (Manager.Layers.Count > 7)
        heightMultiplier = 7f / (float)(Manager.Layers.Count) * (float)2.5f;

      if (heightMultiplier < 1.2f)
        heightMultiplier = 1.2f;

      foreach (Layer layer in Manager.Layers)
      {
        ClickButton button = new ClickButton(layer.FriendlyName, (CurrentLayer == layer) ? Global.Icons[6] : Global.Icons[7], layerClick, null, null, heightMultiplier);
        button.Tag = layer;
        layerButtons.Add(button);
        layerMenu.Add(button, false);
      }
    }

    internal void ClearTileCache()
    {
      lock (loadedTiles)
      {
        foreach (Descriptor desc in loadedTiles.Keys)
          loadedTiles[desc].Dispose();

        loadedTiles = new Dictionary<Descriptor, Tile>();
      }
    }

    internal void resetRoute(ClickButton sender)
    {
      Routes[0].Points = new List<PointD>();
      Routes[0].ShowRoute = false;
      Routes[0].Name = "-empty- RouteOverlay";

      ClearTileCache();
      Render(false);
    }
*/
    int numLoadedTiles()
    {
        int cnt = 0;
        loadedTilesLock.lock();
        try
        {
	        for (Tile tile : loadedTiles.values())
	        	if (tile.Image != null)
	        		cnt++;
		} finally
		{
			loadedTilesLock.unlock();
		}
        
        return cnt;
    }

    int numTrackTiles()
    {
        int cnt = 0;
        trackTilesLock.lock();
        try
        {
        	for (Tile tile : trackTiles.values())
        		if (tile.Image != null)
        			cnt++;
		} finally
		{
			trackTilesLock.unlock();
		}
        
        return cnt;
    }
/*

    private static void InternalRotateImage(int rotationAngle,
                                    Bitmap originalBitmap,
                                    Bitmap rotatedBitmap)
    {
      // It should be faster to access values stored on the stack
      // compared to calling a method (in this case a property) to 
      // retrieve a value. That's why we store the width and height
      // of the bitmaps here so that when we're traversing the pixels
      // we won't have to call more methods than necessary.

      int newWidth = rotatedBitmap.Width;
      int newHeight = rotatedBitmap.Height;

      int originalWidth = originalBitmap.Width;
      int originalHeight = originalBitmap.Height;

      // We're going to use the new width and height minus one a lot so lets 
      // pre-calculate that once to save some more time
      int newWidthMinusOne = newWidth - 1;
      int newHeightMinusOne = newHeight - 1;

      // To grab the raw bitmap data into a BitmapData object we need to
      // "lock" the data (bits that make up the image) into system memory.
      // We lock the source image as ReadOnly and the destination image
      // as WriteOnly and hope that the .NET Framework can perform some
      // sort of optimization based on this.
      // Note that this piece of code relies on the PixelFormat of the 
      // images to be 32 bpp (bits per pixel). We're not interested in 
      // the order of the components (red, green, blue and alpha) as 
      // we're going to copy the entire 32 bits as they are.
      BitmapData originalData = originalBitmap.LockBits(
          new Rectangle(0, 0, originalWidth, originalHeight),
          ImageLockMode.ReadOnly,
          PixelFormat.Format32bppRgb);
      BitmapData rotatedData = rotatedBitmap.LockBits(
          new Rectangle(0, 0, rotatedBitmap.Width, rotatedBitmap.Height),
          ImageLockMode.WriteOnly,
          PixelFormat.Format32bppRgb);

      // We're not allowed to use pointers in "safe" code so this
      // section has to be marked as "unsafe". Cool!
      unsafe
      {
        // Grab int pointers to the source image data and the 
        // destination image data. We can think of this pointer
        // as a reference to the first pixel on the first row of the 
        // image. It's actually a pointer to the piece of memory 
        // holding the int pixel data and we're going to treat it as
        // an array of one dimension later on to address the pixels.
        int* originalPointer = (int*)originalData.Scan0.ToPointer();
        int* rotatedPointer = (int*)rotatedData.Scan0.ToPointer();

        // There are nested for-loops in all of these case statements
        // and one might argue that it would have been neater and more
        // tidy to have the switch statement inside the a single nested
        // set of for loops, doing it this way saves us up to three int 
        // to int comparisons per pixel. 
        //
        switch (rotationAngle)
        {
          case 90:
            for (int y = 0; y < originalHeight; ++y)
            {
              int destinationX = newWidthMinusOne - y;
              for (int x = 0; x < originalWidth; ++x)
              {
                int sourcePosition = (x + y * originalWidth);
                int destinationY = x;
                int destinationPosition =
                        (destinationX + destinationY * newWidth);
                rotatedPointer[destinationPosition] =
                    originalPointer[sourcePosition];
              }
            }
            break;
          case 180:
            for (int y = 0; y < originalHeight; ++y)
            {
              int destinationY = (newHeightMinusOne - y) * newWidth;
              for (int x = 0; x < originalWidth; ++x)
              {
                int sourcePosition = (x + y * originalWidth);
                int destinationX = newWidthMinusOne - x;
                int destinationPosition = (destinationX + destinationY);
                rotatedPointer[destinationPosition] =
                    originalPointer[sourcePosition];
              }
            }
            break;
          case 270:
            for (int y = 0; y < originalHeight; ++y)
            {
              int destinationX = y;
              for (int x = 0; x < originalWidth; ++x)
              {
                int sourcePosition = (x + y * originalWidth);
                int destinationY = newHeightMinusOne - x;
                int destinationPosition =
                    (destinationX + destinationY * newWidth);
                rotatedPointer[destinationPosition] =
                    originalPointer[sourcePosition];
              }
            }
            break;
        }

        // We have to remember to unlock the bits when we're done.
        originalBitmap.UnlockBits(originalData);
        rotatedBitmap.UnlockBits(rotatedData);
      }
    }

    private void bZoomIn_Click(object sender, EventArgs e)
    {
      zoomIn();
    }

    private void bZoomOut_Click(object sender, EventArgs e)
    {
      zoomOut();
    }

    private void MapView_Load(object sender, EventArgs e)
    {
      foreach (Layer layer in MapView.Manager.Layers)
      {
        ToolStripMenuItem tsmi = new ToolStripMenuItem(layer.Name);
        tsiLayer.DropDownItems.Add(tsmi);
        tsmi.Click += new EventHandler(tsi_Click);
        if ((MapView.View != null) && (MapView.View.CurrentLayer != null) && (MapView.View.CurrentLayer.Name == layer.Name))
          tsmi.Checked = true;
        tsmi.Tag = layer;
      }
    }
  
    void tsi_Click(object sender, EventArgs e)
    {
      ToolStripMenuItem tsmi = sender as ToolStripMenuItem;
      if (tsmi != null)
      {
        Layer layer = tsmi.Tag as Layer;
        if (layer != null)
        {
          MapView.View.SetCurrentLayer(layer);
          foreach (ToolStripMenuItem tsmi2 in tsiLayer.DropDownItems)
          {
            if (tsmi2 != null)
              tsmi2.Checked = false;
          }
          tsmi.Checked = true;
        }
      }
    }

    private void contextMenuStrip1_Opening(object sender, CancelEventArgs e)
    {
     
    }

    private void loactionToolStripMenuItem_Click(object sender, EventArgs e)
    {
      if (lastMouseCoordinate != null)
      {
        Location location = new Geocaching.Location("", lastMouseCoordinate.Latitude, lastMouseCoordinate.Longitude);
        location.Edit();
      }
    }

    private void copyCoordinatesToolStripMenuItem_Click(object sender, EventArgs e)
    {
      if (lastMouseCoordinate != null)
      {
        Clipboard.SetText(lastMouseCoordinate.FormatCoordinate());
      }
    }



 */

	@Override
	public void PositionChanged(Location location) {
		Global.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		Global.LastValidPosition.Elevation = location.getAltitude();
        // Muss der aktive Track gezeichnet werden?
        if ((Global.AktuelleRoute != null) && Global.AktuelleRoute.ShowRoute)
        {
        	try
        	{
	            // Map Tiles aktualisieren, wenn der AktiveTrack erweitert wurde!
	            if ((Global.AktuelleRoute.Points.size() > Global.aktuelleRouteCount) && (Global.AktuelleRoute.Points.size() > 1))
	            {
	                // Liste aller neuen Punkte erstellen incl. dem letzten.
	                ArrayList<PointD> punkte = new ArrayList<PointD>();
	                for (int i = Global.aktuelleRouteCount - 1; i < Global.AktuelleRoute.Points.size(); i++)
	                {
	                    if (i < 0) continue;
	                    punkte.add(Global.AktuelleRoute.Points.get(i));
	                }
	                Global.aktuelleRouteCount = Global.AktuelleRoute.Points.size();
	
	                Paint paint = Global.AktuelleRoute.paint;
	                trackTilesLock.lock();
	                try
	                {
	                    for (long hash : trackTiles.keySet())
	                    {
	                    	Tile tile = trackTiles.get(hash);
	                        if (tile.Image == null) continue;
	                        Canvas canvas = new Canvas(tile.Image);
	
	                        double tileX = tile.Descriptor.X * 256 * dpiScaleFactorX;
	                        double tileY = tile.Descriptor.Y * 256 * dpiScaleFactorY;
	
	                        double adjustmentX = Math.pow(2, tile.Descriptor.Zoom - RouteOverlay.projectionZoomLevel) * 256 * dpiScaleFactorX;
	                        double adjustmentY = Math.pow(2, tile.Descriptor.Zoom - RouteOverlay.projectionZoomLevel) * 256 * dpiScaleFactorY;
	
	                        for (int j = 0; j < (punkte.size() - 1); j++)
	                        {
	                        	canvas.drawLine((int)(punkte.get(j).X * adjustmentX - tileX), 
	                        					(int)(punkte.get(j).Y * adjustmentY - tileY), 
	                        					(int)(punkte.get(j + 1).X * adjustmentX - tileX), 
	                        					(int)(punkte.get(j + 1).Y * adjustmentY - tileY), paint);
	                        }
	                        if (punkte.size() > 0)
	                        {
	                            double x = (punkte.get(punkte.size() - 1).X * adjustmentX - tileX);
	                            double y = (punkte.get(punkte.size() - 1).Y * adjustmentY - tileY);
	                            //                            graphics.DrawString(x.ToString() + " - " + y.ToString(), fontSmall, new SolidBrush(Color.Black), 20, 20);
	                        }
	                    }
	                } finally
	                {
	              	  trackTilesLock.unlock();
	                }
	            }
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.PositionChanged()","1",exc);
        	}
        }
        
        if (Config.GetBool("MoveMapCenterWithSpeed") && alignToCompass && (lockPosition >= 1))
        { 
	        if (location.hasSpeed())
	        {
	        	double maxSpeed = Config.GetInt("MoveMapCenterMaxSpeed");
	        	int diff = (int)((double)(height) / 3 * location.getSpeed() / maxSpeed);
	        	if (diff > height / 3)
	        		diff = height / 3;
	        	
	        	halfHeight = height / 2 + diff;
	        }
        } else
        	halfHeight = height / 2;

        if (isVisible)
        {
        	try
        	{
        	// draw Map only when MapView is visible
        		if (lockPosition >= 1/* && !animationTimer.Enabled*/)
        			setCenter(new Coordinate(Global.LastValidPosition));
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.PositionChanged()","2",exc);
        	}
	        Render(false);
        }
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case 0:
				SetCurrentLayer(MapView.Manager.GetLayerByName(item.getTitle().toString(), item.getTitle().toString(), ""));
				return true;
/*			case R.id.hubertmedia:
				SetCurrentLayer(MapView.Manager.GetLayerByName("Hubermedia Bavaria", "Hubermedia Bavaria", ""));
				return true;
			case R.id.googleearth:
				SetCurrentLayer(MapView.Manager.GetLayerByName("Google Earth", "Google Earth", ""));
				return true;
			case R.id.mapnik:
				SetCurrentLayer(MapView.Manager.GetLayerByName("Mapnik", "Mapnik", ""));
				return true;
	*/		
			case R.id.miAlignCompass:
				if (alignToCompass)
				{
					changeOrientation(0);				
					Render(true);
				}
				alignToCompass = !alignToCompass;
				return true;
			case R.id.mapview_smooth_none:
				setSmotthScrolling(SmoothScrollingTyp.none);
				return true;
			case R.id.mapview_smooth_normal:
				setSmotthScrolling(SmoothScrollingTyp.normal);
				return true;
			case R.id.mapview_smooth_fine:
				setSmotthScrolling(SmoothScrollingTyp.fine);
				return true;
			case R.id.mapview_smooth_superfine:
				setSmotthScrolling(SmoothScrollingTyp.superfine);
				return true;
			case R.id.mapview_startrecording:
				TrackRecorder.StartRecording();
				return true;
			case R.id.mapview_pauserecording:
				TrackRecorder.PauseRecording();
				return true;
			case R.id.mapview_stoprecording:
				TrackRecorder.StopRecording();
				return true;
				
			case R.id.mapview_go_settings:
				final Intent mainIntent = new Intent().setClass( main.mainActivity,de.droidcachebox.Views.Forms.Settings.class);
		   		Bundle b = new Bundle();
			        b.putSerializable("Show", 3); //Show Settings und setze ein PerformClick auf den MapSettings Button! (3)
			        mainIntent.putExtras(b);
		   		main.mainActivity.startActivity(mainIntent);
		   		return true;
		}
		return false;
	}

	private void setSmotthScrolling(SmoothScrollingTyp typ) 
	{
		Global.SmoothScrolling = typ;
		Config.Set("SmoothScrolling",typ.toString());
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		try
		{
			MenuItem mi = menu.findItem(R.id.layer);
			if (mi != null)
			{
				SubMenu subMenu = mi.getSubMenu();
				subMenu.clear();
				for (Layer layer : Manager.Layers)
				{
					MenuItem mi22 = subMenu.add(layer.Name);
					if (layer == CurrentLayer)
					{
						mi22.setCheckable(true);
						mi22.setChecked(true);
					}
				}
			}
			mi = menu.findItem(R.id.miAlignCompass);
			mi.setCheckable(alignToCompass);
			
			// smoothScrolling
			mi = menu.findItem(R.id.mapview_smooth);
			if (mi != null)
			{
				SubMenu subMenu = mi.getSubMenu();
				MenuItem mi2 = subMenu.findItem(R.id.mapview_smooth_none);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.none);
				mi2 = subMenu.findItem(R.id.mapview_smooth_normal);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.normal);
				mi2 = subMenu.findItem(R.id.mapview_smooth_fine);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.fine);
				mi2 = subMenu.findItem(R.id.mapview_smooth_superfine);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.superfine);
			}
			mi = menu.findItem(R.id.mapview_startrecording);
			if (mi != null)
				mi.setEnabled(!TrackRecorder.recording);
			mi = menu.findItem(R.id.mapview_pauserecording);
			if (mi != null)
				mi.setEnabled(TrackRecorder.recording);
			mi = menu.findItem(R.id.mapview_stoprecording);
			if (mi != null)
				mi.setEnabled(TrackRecorder.recording);
		} catch (Exception exc)
		{
			Logger.Error("MapView.BeforeShowMenu()","",exc);
			return;
		}
	}
	
	@Override
	public int GetMenuId()
	{
		return R.menu.menu_mapview;
	}

/*	int anzCompassValues = 0;
	float compassValue = 0;
	long lastCompassTick = -99999;*/
	@Override
	public void OrientationChanged(float heading) {
		if (!isVisible) return;
/*		
		anzCompassValues++;
		compassValue += heading;

		long aktTick = SystemClock.uptimeMillis();
		if (aktTick < lastCompassTick + 200)
		{
			// do not update view now, only every 200 millisec
			return;
		}
		if (anzCompassValues == 0)
		{
			lastCompassTick = aktTick;
			return;
		}
		// Durchschnitts Richtung berechnen
		heading = compassValue / anzCompassValues;
		anzCompassValues = 0;
		compassValue = 0;
*/
		if (alignToCompass)
			changeOrientation(heading);

//		lastCompassTick = aktTick;
	}
	
	private void changeOrientation(float heading)
	{
		if (canvas == null)
			return;
//		canvas.rotate(canvasHeading - heading, offScreenBmp.getWidth() / 2, offScreenBmp.getHeight() / 2);
		
		try
		{
			
			float newCanvasHeading = heading;
			// liefert die Richtung (abh�ngig von der Geschwindigkeit von Kompass oder GPS
			if (!Global.Locator.UseCompass() && alignToCompass)
			{
				// GPS-Richtung soll verwendet werden!
				newCanvasHeading = Global.Locator.getHeading();
				heading = newCanvasHeading;
			}
	
			animationThread.rotateTo(newCanvasHeading);
			
			// da die Map gedreht in die offScreenBmp gezeichnet werden soll, muss der Bereich, der gezeichnet werden soll gr��er sein, wenn gedreht wird.
			double w = offScreenBmp.getWidth();
			double h = offScreenBmp.getHeight();
			if (heading >= 180)
				heading -= 180;
			if (heading > 90)
				heading = 180 - heading;
			double alpha = heading / 180 * Math.PI;
			double beta = Math.atan(w / h);
			double gammaW = Math.PI / 2 - alpha - beta;
			// halbe L�nge der Diagonalen
			double diagonal = Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2)) / 2;
			drawingWidth = (int)(Math.cos(gammaW) * diagonal * 2);
			
			double gammaH = alpha - beta;
			drawingHeight = (int)(Math.cos(gammaH) * diagonal * 2);
	
	//		debugString1 = Math.round(alpha / Math.PI * 180) + " - " + Math.round(beta / Math.PI * 180)  + " - " + Math.round(gammaW / Math.PI * 180) + " - " + Math.round(gammaH / Math.PI * 180);
	//		debugString2 = "h = " + drawingHeight + " - w = " + drawingWidth;
			Render(true);
    	} catch(Exception exc)
    	{
    		Logger.Error("MapView.changeOrientation()","",exc);
    	}

	}

	@Override
	public void OnHide() {
		ClearCachedTiles();
		isVisible = false;
		
    	if ((animationThread != null) && (animationThread.isAlive()))
    		animationThread.stop();
    	
		// save zoom level
    	if (Config.GetInt("lastZoomLevel") != Zoom)
    	{
			Config.Set("lastZoomLevel", Zoom);
			Config.AcceptChanges();
    	}
	}

	@Override
	public void OnFree() {
		if (animationThread != null)
		{
			animationThread.beendeThread();
			if (animationThread.isAlive())
				animationThread.stop();
			animationThread = null;
		}
	}


	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	
	

	
	private final Handler messageHandler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle != null)
			{
				if (msg.what == 4)
				{
					try
					{
				    	synchronized (screenCenter)
						{
							boolean changeHeading = bundle.getBoolean("ChangeHeading");
							if (changeHeading)
							{
								float newHeading = bundle.getFloat("Heading");
								canvasHeading = newHeading;
							}
							boolean changeZoom = bundle.getBoolean("ChangeZoom");
							if (changeZoom)
							{
								int zoom = bundle.getInt("Zoom");
								double faktor = bundle.getDouble("Faktor");
								while (zoom > Zoom)
								{
									Zoom++;
							        centerOsmSpace.X *= 2;
							        centerOsmSpace.Y *= 2;
							        screenCenter.X *= 2;
							        screenCenter.Y *= 2;
								}
								while (zoom < Zoom)
								{
									Zoom--;
							        centerOsmSpace.X /= 2;
							        centerOsmSpace.Y /= 2;
							        screenCenter.X /= 2;
							        screenCenter.Y /= 2;
								}
								multiTouchFaktor = faktor;
						        renderZoomScaleActive = true;
						        startZoomScaleTimer();
						        zoomChanged();
							}
							boolean changePos = bundle.getBoolean("ChangePos");
							if (changePos)
							{
								double x = bundle.getDouble("x");
								double y = bundle.getDouble("y");
								screenCenter.X = x;
								screenCenter.Y = y;
					            centerOsmSpace.X = screenCenter.X / dpiScaleFactorX;
					            centerOsmSpace.Y = screenCenter.Y / dpiScaleFactorY;
							}
							boolean updateCacheList = bundle.getBoolean("updateCacheList");
							if (updateCacheList)
								updateCacheList();
							if (bundle.getBoolean("doRender"))
							{
								try
								{
									Render(true);								
								} catch (Exception exc)
								{
									Logger.Error("MapView.messageHandler()","Render",exc);
								}
							}
						}
						sendEmptyMessage(5);  // im UI Thread ausf�hren
		        	} catch(Exception exc)
		        	{
		        		Logger.Error("MapView.messageHandler()","4",exc);
		        	}
				}
				if (msg.what == 5)
				{
					try
					{
						zoomControls.setIsZoomOutEnabled(Zoom > minZoom); 
						zoomControls.setIsZoomInEnabled(Zoom < maxZoom);
		        	} catch(Exception exc)
		        	{
		        		Logger.Error("MapView.messageHandler()","5",exc);
		        	}
					
				}
			}
			super.handleMessage(msg);
		}
	};
	
	
	
	
	
	public class AnimationThread extends Thread {
		private Handler handler;
		
		public boolean posInitialized = false;
		public double toX = 0;
		public double toY = 0;
		public boolean posDirect = false;
		public double posFaktor = 5;  // je h�her, desto langsamer 
		public boolean zoomInitialized = false;
		public double toZoom = 0;
		public double toFaktor = 0;
		public boolean headingInitialized = false;
		public float toHeading = 0;
		public boolean animationFertig = true;
		private int count = 0;
		public void run() {
			Looper.prepare();
			handler = new Handler() {
				public void handleMessage(Message msg) {
					if (msg.what == 4)
					{
						try
						{
							animationFertig = false;
							long nextTick = 0;
				            boolean doUpdateCacheList = false;
							
							while (true)
							{
								// zur R�ckgabe
					            Message ret = new Message();
					            Bundle br = new Bundle();
	
					            boolean fertigHeading = true;  
					            boolean fertigZoom = true;
					            boolean fertigPos = true;

					            float aktHeading = correctHeading(canvasHeading);
					    		double aktX = 0;
					    		double aktY = 0;
						    	synchronized (screenCenter)
						    	{
						    		aktX = screenCenter.X;
						    		aktY = screenCenter.Y;
						    	}
					            
					            animationLock.lock();
					            try
					            {
									if (headingInitialized && (Math.abs(aktHeading - toHeading) > 0.3f))
									{
							            float step = rotationDirection(aktHeading, toHeading);
										aktHeading += step;
										aktHeading = correctHeading(aktHeading);
										
										if (Math.abs(aktHeading - toHeading) < 0.3f)
											fertigHeading = true;
										else
											fertigHeading = false;
										br.putBoolean("ChangeHeading", true);
							            br.putFloat("Heading", aktHeading);
									} else
										br.putBoolean("ChangeHeading", false);
		
									if (!mouseMoved)
									{
										// Zoom und Position nur dann �ndern, wenn nicht gerade ein MouseMove aktiv ist
							            int aktZoom = Zoom;
										double faktor = multiTouchFaktor;
										if (faktor < 1)
										{
											// runden auf 0.05;
											faktor = Math.rint(faktor * smoothScrolling.AnimationSteps()*2) / (smoothScrolling.AnimationSteps()*2);
											if (faktor < 0.74)
												faktor = 0.75;
										}
										if (faktor > 1)
										{
											// runden auf 0.1;
											faktor = Math.rint(faktor * smoothScrolling.AnimationSteps()*2) / (smoothScrolling.AnimationSteps()*2);
											if (faktor > 1.5)
												faktor = 1.5;
										}
										if (zoomInitialized && ((aktZoom != toZoom) || (Math.abs(faktor - toFaktor) >= 0.001)))
										{
											double diff;
											if (aktZoom + faktor > toZoom + toFaktor)
											{
												diff = -1;
												if (faktor <= 1.0001)
													diff /= 2;
											} else
											{
												diff = 1;
												if (faktor < 0.99999)
													diff /= 2;
											}
			
											faktor += diff / smoothScrolling.AnimationSteps();
											if (faktor > 1.5)
											{
												aktZoom++;
										        toX *= 2;
										        toY *= 2;
										        aktX *= 2;
										        aktY *= 2;
												faktor = faktor / 2;
											}
											if (faktor < 0.75)
											{
												aktZoom--;
										        toX /= 2;
										        toY /= 2;
										        aktX /= 2;
										        aktY /= 2;
												faktor = faktor * 2;
											}
											
											if ((aktZoom == toZoom) && (Math.abs(faktor - toFaktor) < 0.001))
												fertigZoom = false;
											else
												fertigZoom = false;
											doUpdateCacheList = true;
											br.putBoolean("ChangeZoom", true);
								            br.putInt("Zoom", aktZoom);
								            br.putDouble("Faktor", faktor);
										} else
											br.putBoolean("ChangeZoom", false);
			
										
							            if (posInitialized && ((Math.abs(aktX - toX) > 1.1) || Math.abs(aktY - toY) > 1.1))
							            {
							            	fertigPos = false;
							            	doUpdateCacheList = true;
			
								            double scale = 1/posFaktor;
							            	
								            double dx = (toX - aktX) * scale;
								            double dy = (toY - aktY) * scale;
								            
								            double x = aktX + dx;
								            double y = aktY + dy;
												
								            if (posDirect)
								            {
								            	x = toX;
								            	y = toY;					            	
								            }
								            br.putDouble("x", x);
								            br.putDouble("y", y);
							            	
							            	br.putBoolean("ChangePos", true);
							            } else
							            	br.putBoolean("ChangePos", false);
									}
								
								
						    	} finally
						    	{
						    		animationLock.unlock();
						    	}
																		            
					            // Nachricht senden
								// Cache Liste nach dem Drehen nicht aktualisieren, da der sichtbare Bereich sich fast nicht �ndert
					            // und sowieso mit UpdateCacheList mehr Caches geladen werden, wie angezeigt...
					            br.putBoolean("updateCacheList", /*fertigHeading && */fertigZoom && fertigPos && doUpdateCacheList);
					            ret.setData(br);
					            ret.what = 4;
					            
					            
				            	long delay;
				            	if(nextTick == 0)
				            	{
				            		// ersten Durchlauf sofort zeichnen!
				            		delay = 0;
									nextTick = SystemClock.uptimeMillis();			            		
				            	} else
				            	{
				            		delay = nextTick - SystemClock.uptimeMillis();
				            		if ((delay < 0) && (fertigHeading && fertigZoom && fertigPos))
				            			delay = 0;		// das letzte Rendern muss gemacht werden!!
				            	}
				            	
				            	br.putBoolean("doRender", delay >= 0);
				            	// nur rendern, wenn noch nicht zu viel Zeitvergangen ist...
				            	// ansonsten �berspringen
				            	// Pause abwarten
				            	// Pause
					            try 
					            {
					            	if (delay > 0)
					            	{
					            		Thread.sleep(delay);
					            	} 
								} catch (InterruptedException e) 
								{
									Logger.Error("MapView.AnimationThread.handleMessage()","TimerInterrupt",e);								
								}							
				            	
				            	messageHandler.handleMessage(ret);		// im animationThread ausf�hren
	
				            	// n�chstes Rendern bei:
					            nextTick += smoothScrolling.AnimationWait();
					            
					            if (fertigHeading && fertigZoom && fertigPos)
					            	break;
							}			
							animationFertig = true;
			        	} catch(Exception exc)
			        	{
			        		Logger.Error("MapView.AnimationThread.handleMessage()","",exc);
			        	}
						
					}
					
				}
			};
			Looper.loop();
			System.gc();
		}

	    private float correctHeading(float heading)
	    {
	    	if (heading >= 360)
	    		heading = heading - 360;
	    	else if (heading < 0)
	    		heading = heading + 360;
	    	return heading;
	    }
	    
	    private float rotationDirection(float fromHeading, float toHeading)
	    {
	    	try
	    	{
		    	float distance = toHeading - fromHeading;
		    	if (distance > 180)
		    	{
		    		distance = distance - 360;
		    	}
		    	if (distance <= -180)
		    	{
		    		distance = 360 + distance;
		    	}
		    	float direction = 1;
		    	if (distance < 0)
		    		direction = -1;
	
		    	int steps = smoothScrolling.AnimationSteps();
		    	double faktor = Math.sqrt((double)steps);
		    	
		    	return (float) (Math.max(Math.abs(distance) / faktor, 0.5f) * direction);
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.AnimationThread.rotationDirection()","",exc);
        		return 0;
        	}
	    }
	    	
	    

		public void moveTo(Coordinate target)
		{
			moveTo(target, smoothScrolling.AnimationSteps(), true);
		}
		public void moveTo(Coordinate target, int anzsteps, boolean useDirect)
		{
			try
			{

				double newPosFaktor = anzsteps / 5 + 1;
	
				PointD animateFrom = new PointD(0, 0);
				synchronized (screenCenter)
				{
					animateFrom = new PointD(screenCenter.X, screenCenter.Y);
				}
				PointD animateTo = new PointD(0, 0);
	            animateTo.X = dpiScaleFactorX * 256 * Descriptor.LongitudeToTileX(Zoom, target.Longitude);
	            animateTo.Y = dpiScaleFactorY * 256 * Descriptor.LatitudeToTileY(Zoom, target.Latitude);			
	            double xDiff = animateFrom.X - animateTo.X;
	            double yDiff = animateFrom.Y - animateTo.Y;
	            center = target;
	            if ((Math.sqrt(xDiff * xDiff + yDiff * yDiff) < 2 * 256 * dpiScaleFactorX) || (!useDirect))
	            {
	            	animationLock.lock();
	            	try
	            	{
	            		animationThread.posFaktor = newPosFaktor;
	            		animationThread.posInitialized = true;
	            		animationThread.toX = animateTo.X;
	            		animationThread.toY = animateTo.Y;
	            		animationThread.posDirect = false;
			    	} finally
			    	{
			    		animationLock.unlock();
			    	}
	            	handler.sendEmptyMessage(4);
	            } else
	            {
	                // Zu weit! Wir gehen ohne Animation direkt zum Ziel!
	            	animationLock.lock();
	            	try
	            	{
	            		animationThread.posFaktor = newPosFaktor;
	            		animationThread.posInitialized = true;
	            		animationThread.toX = animateTo.X;
	            		animationThread.toY = animateTo.Y;
	            		animationThread.posDirect = true;
			    	} finally
			    	{
			    		animationLock.unlock();
			    	}
	            	handler.sendEmptyMessage(4);
	            }
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.AnimationThread.moveTo()","",exc);
        	}
		}

		public void zoomTo(int newZoom)
		{

			try
			{
				animationLock.lock();
				try
				{
					animationThread.zoomInitialized = true;
					animationThread.toZoom = newZoom;
					animationThread.toFaktor = 1;
		    	} finally
		    	{
		    		animationLock.unlock();
		    	}
	        	handler.sendEmptyMessage(4);			
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.AnimationThread.ZoomTo","",exc);
        	}
		}

		private float lastNewHeading = -999;
		public void rotateTo(float newHeading)
		{
			try
			{
				if (!alignToCompass)
					return;
				if (Math.abs(lastNewHeading - newHeading) < 1)
					return;
				lastNewHeading = newHeading;
	
				animationLock.lock();
				try
				{
					animationThread.headingInitialized = true;
					animationThread.toHeading = lastNewHeading;
		    	} finally
		    	{
		    		animationLock.unlock();
		    	}
	//			handler.removeMessages(4);
	        	handler.sendEmptyMessage(4);
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.AnimationThread.rotateTo","",exc);
        	}
		}
		public void stopMove() {
			try
			{
				handler.sendEmptyMessage(1);
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.AnimationThread.stopMove","",exc);
        	}
		}
		
		public void beendeThread() {
			try
			{
				handler.getLooper().quit();
        	} catch(Exception exc)
        	{
        		Logger.Error("MapView.AnimationThread.beendeThread","",exc);
        	}			
		}
	}
	

	// 0 -> kein
	// 1 -> normal
	// 2 -> weich
	// 3 -> sehr weich
	// 4 -> Benutzerdefiniert
	public SmoothScrolling smoothScrolling = new SmoothScrolling();
	
	public enum SmoothScrollingTyp { none, normal, fine, superfine, userdefined }

	private class SmoothScrolling
	{
		private int[] animationSteps = new int[5];  // Schritte
		private int[] animationWait = new int[5];	// Abstand zwischen den Schritten in Millisekunden
		
		public SmoothScrolling()
		{
			animationSteps[0] = 1;
			animationWait[0] = 0;

			animationSteps[1] = 5;
			animationWait[1] = 100;

			animationSteps[2] = 10;
			animationWait[2] = 50;

			animationSteps[3] = 20;
			animationWait[3] = 25;

			animationSteps[4] = 1;
			animationWait[4] = 0;
		}
		
		public int AnimationSteps()
		{
			return animationSteps[Global.SmoothScrolling.ordinal()];		
		}

		public int AnimationWait()
		{
			return animationWait[Global.SmoothScrolling.ordinal()];		
		}
	}

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	public void CacheListChangedEvent()
	{
		if (!isVisible)
			return;	// nur wenn sichtbar
		updateCacheList();
    	Render(true);
	}
}
