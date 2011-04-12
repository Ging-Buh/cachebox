package de.droidcachebox.Views;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Cache.CacheTypes;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.MysterySolution;
import de.droidcachebox.Geocaching.Waypoint;
import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.Paint.Style;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Descriptor.PointD;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Map.Manager;
import de.droidcachebox.Map.Tile;

public class MapView extends SurfaceView implements PositionEvent, ViewOptionsMenu {
	private Button buttonZoomIn;
	private Button buttonZoomOut;
	private Timer zoomScaleTimer;
	private TimerTask zoomTimerTask;
    /**
	 * Constructor
	 */
	SurfaceHolder holder;
	public MapView(Context context, String text) {
		super(context);
	
		setWillNotDraw(false);
		holder = getHolder();
		
        this.buttonZoomIn = new Button(this.getContext());
        this.buttonZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	zoomIn();
            }
          });
        this.buttonZoomOut = new Button(this.getContext());
        this.buttonZoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	zoomOut();
            }
          });

        ArrayList<android.view.View> buttons = new ArrayList<android.view.View>();
        buttons.add(buttonZoomIn);
        buttons.add(buttonZoomOut);
        this.addTouchables(buttons);
        scale = getContext().getResources().getDisplayMetrics().density;
        
        font.setTextSize(9);
        font.setFakeBoldText(true);
        fontSmall.setTextSize(12 * scale);
        PositionEventList.Add(this);

        zoomScaleTimer = new Timer();
        
	}
	
	final float scale;
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		try
		{
			Render(false);
		} catch (Exception exc)
		{
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
//       scaleWidth = width - scaleLeft - (int)this.CreateGraphics().MeasureString("100km ", Font).Width + 1;
        scaleWidth = 50;
        

        offScreenBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(offScreenBmp);

        zoomChanged();
    }

    private String debugString1 = "";
    private String debugString2 = "";
   
    private boolean multiTouch = false;
    private double lastMultiTouchDist = 0;
    private double multiTouchFaktor = 1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eX = (int)event.getX(0);
        int eY = (int)event.getY(0);
        int eX2 = (int)event.getX(1);
        int eY2 = (int)event.getY(1);
        double multiTouchDist = Math.sqrt(Math.pow(event.getX(1) - event.getX(), 2) + Math.pow(event.getY(1) - event.getY(), 2));
//        debugString1 = eX + "-" + eY;
//        debugString2 = eX2 + "-" + eY2;
        if (event.getPointerCount() > 1)
        {
        	if (multiTouch)
        	{
        		if (lastMultiTouchDist > multiTouchDist * 1.5)
        		{
        			zoomOut(false);
        			lastMultiTouchDist /= 2; //multiTouchDist;
        		}
        		else if (lastMultiTouchDist < multiTouchDist * 0.75)
        		{
        			zoomIn(false);
        			lastMultiTouchDist *= 2; //multiTouchDist;
        		}
        	} else
    			lastMultiTouchDist = multiTouchDist;

        	
        	if (lastMultiTouchDist > 0)
        		multiTouchFaktor = multiTouchDist / lastMultiTouchDist;
        	else 
        		multiTouchFaktor = 1;
//        	debugString1 = "f: " + multiTouchFaktor;
        	
        	eX = (int)(event.getX(0) + (event.getX(1) - event.getX(0)) / 2);
        	eY = (int)(event.getY(0) + (event.getY(1) - event.getY(0)) / 2);
        	if (!multiTouch)
        	{
        		dragStartX = lastClickX = eX;
        		dragStartY = lastClickY = eY;
        	}
        	multiTouch = true;
        } else
        {
        	if(multiTouch)
        	{
        		dragStartX = lastClickX = eX;
        		dragStartY = lastClickY = eY;        		
        	}
        	multiTouch = false;
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	MapView_MouseDown(eX, eY);
//                touch_start(x, y);
//                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
            	MapView_MouseMove(eX, eY);
//                touch_move(x, y);
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            	multiTouchFaktor = 1;
            	MapView_MouseUp(eX, eY);
//                touch_up();
//                invalidate();
                break;
        }
        return true;
    }

    public static MapView View = null;
/*    public delegate void TileLoadedHandler(Bitmap bitmap, Descriptor desc);
    public event TileLoadedHandler OnTileLoaded = null;*/
    private int aktuelleRouteCount = 0;

    /// <summary>
    /// Aktuell betrachteter Layer
    /// </summary>
    public Layer CurrentLayer = null;
    public void SetCurrentLayer(Layer newLayer)
    {
      Config.Set("CurrentMapLayer", newLayer.Name);
      Config.AcceptChanges();

      CurrentLayer = newLayer;
      synchronized (loadedTiles)
      {
        ClearCachedTiles();
        Render(true);
      }
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

    /// <summary>
    /// Instanz des Loaders
    /// </summary>
    Thread loaderThread = null;

    /// <summary>
    /// Liste mit den darzustellenden Wegpunkten
    /// </summary>
    ArrayList<WaypointRenderInfo> wpToRender = new ArrayList<WaypointRenderInfo>();

    /// <summary>
    /// Speichert die Informationen für einen im Sichtfeld befindlichen
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
    /// true, falls bei Mysterys mit Lösung (Final Waypoint) der Cache ausgeblendet werden soll, wenn der Cache nicht selected ist.
    /// </summary>
    boolean hideCacheWithFinal = true;

    /// <summary>
    /// true, falls der kleine Kompass open angezeigt werden soll
    /// </summary>
    boolean showCompass = true;

    /// <summary>
    /// Spiegelung des Logins bei Gc, damit ich das nicht dauernd aus der
    /// Config lesen muss.
    /// </summary>
    String gcLogin = "";

    /// <summary>
    /// true, falls Center gültige Koordinaten enthält
    /// </summary>
    boolean positionInitialized = false;

    boolean hideMyFinds = false;

    protected Coordinate center = new Coordinate(48.124258, 12.164580);

    PointD centerOsmSpace = new PointD(0, 0);
/*
    ///// <summary>
    ///// Wegpunkt des Markers. Wird bei go to als Ziel gesetzt
    ///// </summary>
    //Waypoint markerWaypoint = new Waypoint("MARKER", CacheTypes.ReferencePoint, "Marker", 0, 0, 0);
*/
    /// <summary>
    /// Der Kartenmittelpunkt. Wird dieser Wert überschrieben wird die
    /// Liste sichtbarer Caches entsprechend aktualisiert.
    /// </summary>
    public Coordinate getCenter()
    {
        return new Coordinate(Descriptor.TileYToLatitude(Zoom, centerOsmSpace.Y / (256.0)), Descriptor.TileXToLongitude(Zoom, centerOsmSpace.X / (256.0)));
    }
    public void setCenter(Coordinate value)
    {
    	if (center == null)
    		center = new Coordinate(48.124258, 12.164580);
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
        updateCacheList();
    }

    protected PointD screenCenter = new PointD(0, 0);

    protected Canvas canvas = null;

    public int Zoom = 14;

    /// <summary>
    /// Hashtabelle mit geladenen Kacheln
    /// </summary>
    protected Hashtable<Long, Tile> loadedTiles = new Hashtable<Long, Tile>();

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
    /// Größe des Kachel-Caches
    /// </summary>
    final int numMaxTiles = 32;

    // Vorberechnete Werte
    protected int halfWidth = 0;
    protected int halfHeight = 0;
    protected int width = 0;
    protected int height = 0;
    protected int halfIconSize = 10;

    protected int lineHeight = 16;
    protected int smallLineHeight = 12;

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
      else
        Routes.Add(new Route(new Pen(Color.Purple, 4), "-empty- Routeoverlay"));

      Global.AktuelleRoute = new Route(new Pen(Color.Blue, 4), "actual Track");
      Global.AktuelleRoute.ShowRoute = true;
      Routes.Add(Global.AktuelleRoute);

      //Load Routes for Autoload
      if (System.IO.Directory.Exists(Config.GetString("TrackFolder") + "\\Autoload"))
      {
        String[] dummy = Directory.GetFiles(Config.GetString("TrackFolder") + "\\Autoload", "*.gpx");
        foreach (String gpx in dummy)
        {
          String gpxFile = gpx.ToLower();
          if (gpxFile.EndsWith(".gpx"))
          {
            Color[] ColorField = new Color[8] { Color.Red, Color.Yellow, Color.Black, Color.DarkOrange, Color.Green, Color.LightBlue, Color.MediumAquamarine, Color.Gray };
            Color TrackColor;
            TrackColor = ColorField[(Routes.Count - 2) % 8];
            Routes.Add(LoadRoute(gpxFile, new Pen(TrackColor, 4), Config.GetInt("TrackDistance")));
          }

        }
      }
      else
      {
        System.IO.Directory.CreateDirectory(Config.GetString("TrackFolder") + "\\Autoload");
      }
*/
    }
/*
    void MapView_MouseWheel(object sender, MouseEventArgs e)
    {
      if (e.Delta > 0)
        zoomIn();
      else
        zoomOut();
    }

    void MapView_OnTileLoaded(Bitmap bitmap, Descriptor desc)
    {
      // wegen dem Fehler mit den Indexed bitmap.
      Graphics graphics = Graphics.FromImage(bitmap);
      RenderRoute(graphics, bitmap, desc);

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

      graphics.Dispose();
    }

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
    	gcLogin = "Ging-Buh";
    	mapMaxCachesDisplay = 50;
    	mapMaxCachesDisplayLarge = 100;
    	zoomCross = 15;
/*      gcLogin = Config.GetString("GcLogin");
      mapMaxCachesDisplay = Config.GetInt("MapMaxCachesDisplay_config");
      mapMaxCachesDisplayLarge = Config.GetInt("mapMaxCachesDisplayLarge_config");
      zoomCross = Config.GetInt("ZoomCross");*/
    	

//      hideFindsButton.ButtonImage = (hideMyFinds) ? Global.Icons[6] : Global.Icons[7];

      // Bestimmung der ersten Position auf der Karte
      if (!positionInitialized)
      {
        double lat = Config.GetDouble("MapInitLatitude");
        double lon = Config.GetDouble("MapInitLongitude");

        // Initialisierungskoordinaten bekannt und können übernommen werden
        if (lat != -1000 && lon != -1000)
        {
          setCenter(new Coordinate(lat, lon));
          positionInitialized = true;
//          tabButtonTrackPosition.Down = false;
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
              setCenter(new Coordinate(Database.Data.Query.get(0).Latitude, Database.Data.Query.get(0).Longitude));
              positionInitialized = true;
//              tabButtonTrackPosition.Down = false;
            }
            else
            {
              // Wenns auch den nicht gibt, nehme Oldenburg :)
              setCenter(new Coordinate(48.124258, 12.164580));
            }
          }
        }

        // Größe des Maßstabes berechnen etc...
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
      }
      else
        dpiScaleFactorX = dpiScaleFactorY = 1;

      //redPen = new Pen(Color.Red, (int)(dpiScaleFactorX * 1.4f));

      // Falls DpiAwareRendering geändert wurde, müssen diese Werte ent-
      // sprechend angepasst werden.
      screenCenter.X = Math.round(centerOsmSpace.X * dpiScaleFactorX);
      screenCenter.Y = Math.round(centerOsmSpace.Y * dpiScaleFactorY);
/*
      halfIconSize = (int)((Global.NewMapIcons[2][0].Height * dpiScaleFactorX) / 2);
*/

      updateCacheList();
/* dieses Render hier nur zum testen!!!!!!!!!!!!!!!!!!!!!!!!*/      
//      Render(false);

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
    /// Läd eine Kachel und legt sie in loadedTiles ab. Implementiert den
    /// WaitCallback-Delegaten
    /// </summary>
    /// <param name="state">Descriptor der zu ladenen Kachel. Typlos, damit
    /// man es als WorkItem queuen kann!</param>
    protected void LoadTile(Object state)
    {
      Descriptor desc = (Descriptor) state;

      Bitmap bitmap = Manager.LoadLocalBitmap(CurrentLayer, desc);
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
/*        if (Config.GetBool("AllowInternetAccess"))
        {
          lock (wishlist)
          {
            wishlist.Add(desc);
            if (loaderThread == null)
            {
              loaderThread = new Thread(new ThreadStart(loaderThreadEntryPoint));
              loaderThread.Priority = ThreadPriority.BelowNormal;
              loaderThread.Start();
            }
          }
        }
*/
        // Upscale coarser map tile
        bitmap = loadBestFit(CurrentLayer, desc);
        tileState = Tile.TileState.LowResolution;
      }
      else
        tileState = Tile.TileState.Present;

      if (bitmap == null)
        return;

/*      if (Config.GetBool("OsmDpiAwareRendering") && (dpiScaleFactorX != 1 || dpiScaleFactorY != 1))
        scaleUpBitmap(ref bitmap);*/

      addLoadedTile(desc, bitmap, tileState);

/*      if (OnTileLoaded != null)
        OnTileLoaded(bitmap, desc);*/

      // Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
      // kann man die Karte ja gut mal neu rendern!
      tilesFinished = true;

//      if (tileVisible(desc))
//        Render(true);
    }

    private Bitmap loadBestFit(Layer CurrentLayer, Descriptor desc)
    {
      Descriptor available = new Descriptor(desc);

      // Determine best available tile
      Bitmap tile = null;
      do
      {
        available.X /= 2;
        available.Y /= 2;
        available.Zoom--;

      } while (available.Zoom >= 0 && (tile = Manager.LoadLocalBitmap(CurrentLayer, available)) == null);

      // No tile available. Use Background color (so that at least
      // routes are painted!)
      Bitmap result = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
      Canvas graphics = new Canvas(result);

      if (tile == null)
      {
    	  
    	  graphics.drawRect(0, 0, result.getWidth(), result.getHeight(), backBrush);
//    	  graphics.restore();
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

        graphics.drawBitmap(tile, new Rect(px, py, px + width, py + width), new Rect(0, 0, result.getWidth(), result.getHeight()), backBrush);
//        graphics.DrawImage(tile, new Rectangle(0, 0, result.Width, result.Height), new Rectangle(px, py, width, width), GraphicsUnit.Pixel);
//        graphics.restore();
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
      synchronized (loadedTiles)
      {
        if (loadedTiles.containsKey(desc.GetHashCode()))
        {
          // Wenn die Kachel schon geladen wurde und die neu zu registrierende Kachel
          // weniger aktuell ist, behalten wir besser die alte!
          if (loadedTiles.get(desc.GetHashCode()).State == Tile.TileState.Present && state != Tile.TileState.Present)
            return;

          if (loadedTiles.get(desc.GetHashCode()).Image != null)
            loadedTiles.get(desc.GetHashCode()).Image.recycle();

          loadedTiles.get(desc.GetHashCode()).State = state; // (bitmap != null) ? Tile.TileState.Present : Tile.TileState.Disposed;
          loadedTiles.get(desc.GetHashCode()).Image = bitmap;
        }
        else
        {
          Tile tile = new Tile(desc, bitmap, state);
          loadedTiles.put(desc.GetHashCode(), tile);
        }
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

      int iconSize = 0; // 8x8
      if ((Zoom >= 12) && (Zoom <= 13))
          iconSize = 1; // 13x13
      else if (Zoom > 13)
          iconSize = 2;  // default Images

      int xFrom = -halfIconSize;
      int yFrom = -halfIconSize;
      int xTo = width + halfIconSize;
      int yTo = height + halfIconSize;

      ArrayList<WaypointRenderInfo> result = new ArrayList<WaypointRenderInfo>();

      // Wegpunkte in Zeichenliste eintragen, unabhängig davon, wo
      // sie auf dem Bildschirm sind
      if (Global.SelectedCache() != null)
      {
        if (!(hideMyFinds && Global.SelectedCache().Found()))
        {
          ArrayList<Waypoint> wps = Global.SelectedCache().waypoints;

          for (Waypoint wp : wps)
          {
            WaypointRenderInfo wpi = new WaypointRenderInfo();
            wpi.MapX = 256 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, wp.Longitude);
            wpi.MapY = 256 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, wp.Latitude);
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

      // Und Caches auch. Diese allerdings als zweites, da sie WPs überzeichnen
      // sollen
      for (Cache cache : Database.Data.Query)
      {
        if (hideMyFinds && cache.Found())
          continue;

        int x = (int)(cache.MapX * dpiScaleFactorX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
        int y = (int)(cache.MapY * dpiScaleFactorY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;

        if ((x < xFrom || y < yFrom || x > xTo || y > yTo) && cache != Global.SelectedCache())
          continue;

        if ((hideCacheWithFinal) && (cache.Type == CacheTypes.Mystery) && cache.MysterySolved() && cache.HasFinalWaypoint())
        {
          // Wenn ein Mystery-Cache einen Final-Waypoint hat, hier die Koordinaten des Caches nicht zeichnen.
          // Der Final-Waypoint wird später mit allen notwendigen Informationen gezeichnet. 
          // Die Koordinaten des Caches sind in den allermeisten Fällen irrelevant.
          // Damit wird von einem gelösten Mystery nur noch eine Koordinate in der Map gezeichnet, wenn der Cache nicht Selected ist.
          // Sobald der Cache Selected ist, werden der Cache und alle seine Waypoints gezeichnet.
          if (cache != Global.SelectedCache())
            continue;
        }

        WaypointRenderInfo wpi = new WaypointRenderInfo();
        wpi.UnderlayIcon = null;
        wpi.OverlayIcon = null;
        wpi.MapX = cache.MapX;
        wpi.MapY = cache.MapY;
        wpi.Icon = (cache.Owner.equalsIgnoreCase(gcLogin) && (gcLogin.length() > 0)) ? Global.NewMapIcons.get(2).get(20) : (cache.Found()) ? Global.NewMapIcons.get(2).get(19) : (cache.MysterySolved() && (cache.Type == CacheTypes.Mystery)) ? Global.NewMapIcons.get(2).get(21) : Global.NewMapIcons.get(2).get((int)cache.Type.ordinal());
        wpi.Icon = Global.NewMapIcons.get(2).get(cache.GetMapIconId(gcLogin));
        wpi.UnderlayIcon = getUnderlayIcon(cache, wpi.Waypoint);
          
        if ((iconSize < 2) && (cache != Global.SelectedCache()))  // der SelectedCache wird immer mit den großen Symbolen dargestellt!
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
            if (cache.Found())
            {
                iconId = 6;
                wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(1);  // round shaddow
            }
            if (cache.Owner.equalsIgnoreCase(gcLogin) && (gcLogin.length() > 0))
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

          if (hideMyFinds && solution.Cache.Found())
              continue;

          double mapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, solution.Longitude);
          double mapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, solution.Latitude);

          int x = (int)(mapX * dpiScaleFactorX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
          int y = (int)(mapY * dpiScaleFactorY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;

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
                      if (solution.Cache.Found())
                          wpiF.Icon = Global.NewMapIcons.get(2).get(19);
                      if (solution.Cache.Owner.equalsIgnoreCase(gcLogin) && (gcLogin.length() > 0))
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

              if (solution.Cache.Found())
              {
                  iconId = 6;
                  wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(1);  // round shaddow
              }
              if (solution.Cache.Owner.equalsIgnoreCase(gcLogin) && (gcLogin.length() > 0))
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
		  y = y - height / 2;
		  x = (int)Math.round(x * multiTouchFaktor + width / 2);
		  y = (int)Math.round(y * multiTouchFaktor + height / 2);
		  
		  int imageX = x;
		  int imageY = y;
		
		  if ((Zoom >= zoomCross) && (wpi.Selected))
		  {
		    int size = (int)(10 * dpiScaleFactorX);
		
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
		    CrossRedPen.setColor(Color.BLUE);
		    CrossRedPen.setStrokeWidth(lineWidth * dpiScaleFactorX);
		
		    Paint cachePen = CrossMagentaPen;
		
		    if (wpi.Cache != null)
		    {
		      if (wpi.Cache.Type == CacheTypes.Traditional) cachePen = CrossGreenPen;
		      else if (wpi.Cache.Type == CacheTypes.Multi) cachePen = CrossYellowPen;
		      else if (wpi.Cache.Type == CacheTypes.Mystery) cachePen = CrossBluePen;
		
		      Paint selectedPen = wpi.Selected ? CrossRedPen : cachePen;
		
		      canvas.drawRect(x - size, y - size, x + size, y + size, selectedPen);
		      canvas.drawLine(x - size, y, x - size / 2, y, selectedPen);
		      canvas.drawLine(x + size / 2, y, x + size, y, selectedPen);
		      canvas.drawLine(x, y - size, x, y - size / 2, selectedPen);
		      canvas.drawLine(x, y + size / 2, x, y + size, selectedPen);
		      canvas.drawLine(x - 2 * size - 5, y - 2 * size - 5, x - size, y - size, selectedPen);
		
		      imageX = x - 2 * size - 5;
		      imageY = y - 2 * size - 5;
		    }
		  }
		
		  if (wpi.UnderlayIcon != null)
		      drawImage(wpi.UnderlayIcon, imageX - halfUnderlayWidth, imageY - halfUnderlayWidth, UnderlayWidth, UnderlayWidth);
		  drawImage(wpi.Icon, imageX - halfIconWidth, imageY - halfIconWidth, IconWidth, IconWidth);
		  if (wpi.OverlayIcon != null)
		      drawImage(wpi.OverlayIcon, imageX - halfOverlayWidth, imageY - halfOverlayWidth, OverlayWidth, OverlayWidth);
		
		  if (wpi.Cache.Favorit())
		  {
			  Global.PutImageTargetHeight(canvas, Global.Icons[19], imageX, imageY, (int)(14.0f * dpiScaleFactorY));
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
		    	canvas.drawText(wpName, x + halfIconWidth + 4, y, fontSmall);
		    	fontSmall.setColor(Color.BLACK);
		    	canvas.drawText(wpName, x + halfIconWidth + 5, y + 1, fontSmall);
		    }
		    else 
		    {  // Aktiver Cache -> Cachename
		    	wpName = wpi.Cache.Name;
		    	if (showRating)
		    		yoffset += 10 * dpiScaleFactorX;
		    	float fwidth = fontSmall.measureText(wpName);
		    	fontSmall.setColor(Color.WHITE);
		    	canvas.drawText(wpName, x - fwidth / 2, y + halfIconWidth + yoffset, fontSmall);
		    	fontSmall.setColor(Color.BLACK);
		    	canvas.drawText(wpName, (x - fwidth / 2) + 1, y + halfIconWidth + yoffset + 1, fontSmall);
		    }
		  }
		
		  // Rating des Caches darstellen
		  if (showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (Zoom >= 14))
		  {
		    Drawable img = Global.SmallStarIcons[(int)Math.min(wpi.Cache.Rating * 2, 5 * 2)];
		    Rect bounds = img.getBounds();
		    int halfSmallStarWidth = (int)(((double)img.getMinimumWidth() / 2.0) * dpiScaleFactorX);
		    int smallStarWidth = (int)((double)img.getMinimumWidth() * dpiScaleFactorX);
		    img.setBounds(x - halfSmallStarWidth, y + halfIconWidth, smallStarWidth, smallStarHeight);
		    img.draw(canvas);
		    img.setBounds(bounds);
//		    canvas.DrawImage(img, new Rectangle(x - halfSmallStarWidth, y + halfIconWidth, smallStarWidth, smallStarHeight), 0, 0, img.Width, img.Height, GraphicsUnit.Pixel, imageAttributes);
		  }
		
		  // Show D/T-Rating
/*		  if (showDT && (wpToRender.Count <= mapMaxCachesLabel) && (!drawAsWaypoint) && (Zoom >= 14))
		  {
		    Bitmap imgDx = Global.SmallStarIcons[(int)Math.Min(wpi.Cache.Difficulty * 2, 5 * 2)];
		    Bitmap imgD = new Bitmap(imgDx.Height, imgDx.Width);
		    InternalRotateImage(270, imgDx, imgD);
		    int halfSmallStarHeightD = (int)(((double)imgD.Width / 2.0) * dpiScaleFactorY);
		    int smallStarHeightD = (int)((double)imgD.Height * dpiScaleFactorY);
		    graphics.DrawImage(imgD, new Rectangle(x - halfIconWidth - smallStarHeight - 4, y + halfIconWidth - smallStarHeightD, smallStarHeight, smallStarHeightD), 0, 0, imgD.Width, imgD.Height, GraphicsUnit.Pixel, imageAttributes);
		
		    Bitmap imgTx = Global.SmallStarIcons[(int)Math.Min(wpi.Cache.Terrain * 2, 5 * 2)];
		    Bitmap imgT = new Bitmap(imgTx.Height, imgTx.Width);
		    InternalRotateImage(270, imgTx, imgT);
		    int halfSmallStarHeightT = (int)(((double)imgT.Height / 2.0) * dpiScaleFactorY);
		    int smallStarHeightT = (int)((double)imgT.Height * dpiScaleFactorY);
		    graphics.DrawImage(imgT, new Rectangle(x + halfIconWidth + 4, y + halfIconWidth - smallStarHeightT, smallStarHeight, smallStarHeightT), 0, 0, imgT.Width, imgT.Height, GraphicsUnit.Pixel, imageAttributes);
		
		
		  }*/
		}
    }

    void drawImage(Bitmap image, int x, int y, int width, int height)
    {
        canvas.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new Rect(x, y, x + width, y + height), null);
//        graphics.DrawImage(image, new Rectangle(x, y, width, height), 0, 0, image.Width, image.Height, GraphicsUnit.Pixel);
    }
    void drawImage(Drawable image, int x, int y, int width, int height)
    {
    	image.setBounds(x, y, x+width, y+height);
    	image.draw(canvas);
    }

    /// <summary>
    /// Überprüft, ob die übergebene Kachel im Darstellungsbereich des
    /// Controls liegt
    /// </summary>
    /// <param name="tile">Die zu prüfende Kachel</param>
    /// <returns>true, wenn die Kachel sichtbar ist, sonst false</returns>
    boolean tileVisible(Descriptor tile)
    {
      Point p1 = ToScreen(tile.X, tile.Y, tile.Zoom);
      Point p2 = ToScreen(tile.X + 1, tile.Y + 1, tile.Zoom);

      return (p1.x < width && p2.x >= 0 && p1.y < height && p2.y >= 0);
    }

    /// <summary>
    /// Lagert die am längsten nicht mehr verwendete Kachel aus
    /// </summary>
    protected void preemptTile()
    {
      //List<Tile> tiles = new List<Tile>();
      //tiles.AddRange(loadedTiles.Values);

      // Ist Auslagerung überhaupt nötig?
      if (numLoadedTiles() <= numMaxTiles)
        return;

      // Kachel mit maximalem Alter suchen
      int maxAge = Integer.MIN_VALUE;
      Descriptor maxDesc = null;

      for (Tile tile : loadedTiles.values())
        if (tile.Image != null && tile.Age > maxAge)
        {
          maxAge = tile.Age;
          maxDesc = tile.Descriptor;
        }

      // Instanz freigeben und Eintrag löschen
      if (maxDesc != null)
      {
    	  try
    	  {
    		  loadedTiles.get(maxDesc.GetHashCode()).destroy();
    		  loadedTiles.remove(maxDesc.GetHashCode());
    	  } catch (Exception ex) { }
      }
    }
/*

    protected override void OnPaint(PaintEventArgs e)
    {
      Render(true);
    }

    delegate void EmptyDelegate(bool overrideRepaintIntelligence);
*/
    boolean lastRenderZoomScale = false;
    boolean tilesFinished = false;
    int lastZoom = Integer.MIN_VALUE;
    Coordinate lastPosition = new Coordinate();
    float lastHeading = Float.MAX_VALUE;
    int lastWpCount = 0;
    PointD lastRenderedPosition = new PointD(Double.MAX_VALUE, Double.MAX_VALUE);
    public void Render(boolean overrideRepaintInteligence)
    {
     	synchronized (this)
     	{
	      if (Database.Data.Query == null)
	        return;
	      if (offScreenBmp == null)
	        return;
	
	      // Aufruf ggf. im richtigen Thread starten
	/*      if (InvokeRequired)
	      {
	        Invoke(new EmptyDelegate(Render), overrideRepaintInteligence);
	        return;
	      }*/
	
	      // Wenn sich bei der Ansicht nichts getan hat braucht sie auch nicht gerendert werden.
	      if (!overrideRepaintInteligence)
	      {
	
	        if (lastRenderZoomScale == renderZoomScaleActive && lastWpCount == wpToRender.size() && lastHeading == ((Global.Location != null) ? Global.Location.getBearing() : 0) && lastPosition.Latitude == Global.LastValidPosition.Latitude && lastPosition.Longitude == Global.LastValidPosition.Longitude && lastZoom == Zoom && !tilesFinished && lastRenderedPosition.X == screenCenter.X && lastRenderedPosition.Y == screenCenter.Y)
	          return;
	
	
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
	
	      synchronized (loadedTiles)
	      {
	        for (Tile tile : loadedTiles.values())
	          tile.Age++;
	      }
	      
	      int xFrom;
	      int xTo;
	      int yFrom;
	      int yTo;
	      Rect tmp = getTileRange();
	      xFrom = tmp.left;
	      xTo = tmp.right;
	      yFrom = tmp.top;
	      yTo = tmp.bottom;
	
	      // Kacheln beantragen
	      for (int x = xFrom; x <= xTo; x++)
	        for (int y = yFrom; y <= yTo; y++)
	        {
	          if (x < 0 || y < 0 || x >= Descriptor.TilesPerLine[Zoom] || y >= Descriptor.TilesPerColumn[Zoom])
	            continue;
	
	          Descriptor desc = new Descriptor(x, y, Zoom);
	
	          Tile tile;
	
	          synchronized (loadedTiles)
	          {
	            if (!loadedTiles.containsKey(desc.GetHashCode()))
	            {
	              preemptTile();
	
	              loadedTiles.put(desc.GetHashCode(), new Tile(desc, null, Tile.TileState.Disposed));
	
	              queueTile(desc);
//	            	LoadTile(desc);
	            }
	            tile = loadedTiles.get(desc.GetHashCode());
	          }
	
	          if ((tile != null) && (tileVisible(tile.Descriptor)))
	          {
	            renderTile(tile);
	          }
	        }
	      renderCaches();
	
	      renderPositionAndMarker();
	
	      renderScale();
	      /*
	      RenderTargetArrow();
	*/
	
	      if (renderZoomScaleActive)
	        renderZoomScale();
	/*
	      if (loaderThread != null)
	        renderLoaderInfo();
	
	
	      if (showCompass)
	        renderCompass();
	*/
	      try
	      {
	    	  Canvas can = holder.lockCanvas(null);
	    	  if (can != null)
	    	  {
	     		  can.drawBitmap(offScreenBmp, 0, 0, null);
	     	      if (!debugString1.equals("") || !debugString2.equals(""))
	     	      {
	     		      Paint debugPaint = new Paint();
	     		      debugPaint.setTextSize(20);
	     		      debugPaint.setColor(Color.WHITE);
	     		      debugPaint.setStyle(Style.FILL);
	     		      can.drawRect(new Rect(50, 20, 300, 100), debugPaint);
	     		      debugPaint.setColor(Color.BLACK);
	     		      can.drawText(debugString1, 50, 50, debugPaint);
	     		      can.drawText(debugString2, 50, 80, debugPaint);
	     	      }
	    		  holder.unlockCanvasAndPost(can);
	    	  }
	    	  
	//        this.CreateGraphics().DrawImage(offScreenBmp, 0, 0);
	      }
	      catch (Exception ex)
	      {
	      }
	      
		}
    }
/*
    Brush orangeBrush = new SolidBrush(Color.Orange);

    private void renderCompass()
    {
      // Position der Anzeigen berechnen
      int left = button1.Left + button1.Width;
      int right = button3.Left;
      int top = button1.Top;
      int compassCenter = button1.Height / 2;
      int bottom = button1.Top + button1.Height;
      int topText = ((bottom - top) - (smallLineHeight * 2 + 5)) / 2;

      int leftString = left + button1.Width + 5;

      transparentRectangle(left, top, right - left, bottom - top, 100);

      // Position ist entweder GPS-Position oder die des Markers, wenn
      // dieser gesetzt wurde.
      Coordinate position = (Global.Marker != null && Global.Marker.Valid) ? Global.Marker : (Global.Locator != null) ? Global.Locator.LastValidPosition : new Coordinate();

      // Koordinaten
      if (position.Valid)
      {
        String textLatitude = Global.FormatLatitudeDM(position.Latitude);
        String textLongitude = Global.FormatLongitudeDM(position.Longitude);

        SizeF size = graphics.MeasureString(textLatitude, fontSmall);
        graphics.DrawString(textLatitude, fontSmall, whiteBrush, right - 5 - size.Width, top + topText);

        size = graphics.MeasureString(textLongitude, fontSmall);
        graphics.DrawString(textLongitude, fontSmall, whiteBrush, right - 5 - size.Width, top + topText + 5 + smallLineHeight);

        if (Global.Locator != null)
          graphics.DrawString(UnitFormatter.SpeedString(Global.Locator.SpeedOverGround), fontSmall, whiteBrush, leftString, top + topText + 5 + smallLineHeight);
      }

      // Gps empfang ?
      if (Global.SelectedCache != null && position.Valid)
      {
        // Distanz einzeichnen
        float distance = 0;

        if (Global.SelectedWaypoint == null)
          distance = Datum.WGS84.Distance(position.Latitude, position.Longitude, Global.SelectedCache.Latitude, Global.SelectedCache.Longitude);
        else
          distance = Datum.WGS84.Distance(position.Latitude, position.Longitude, Global.SelectedWaypoint.Latitude, Global.SelectedWaypoint.Longitude);

        String text = UnitFormatter.DistanceString(distance);
        graphics.DrawString(text, font, whiteBrush, leftString, top + topText);

        // Kompassnadel zeichnen
        if (Global.Locator != null)
        {
          Coordinate cache = (Global.SelectedWaypoint != null) ? new Coordinate(Global.SelectedWaypoint.Latitude, Global.SelectedWaypoint.Longitude) : new Coordinate(Global.SelectedCache.Latitude, Global.SelectedCache.Longitude);
          double bearing = -Coordinate.Bearing(Global.Locator.LastValidPosition.Latitude, Global.Locator.LastValidPosition.Longitude, cache.Latitude, cache.Longitude);
          double relativeBearing = bearing - Global.Locator.Heading;
          double relativeBearingRad = relativeBearing * Math.PI / 180.0;

          Cachebox.Drawing.Arrow.FillArrow(graphics, Cachebox.Drawing.Arrow.HeadingArrow, blackPen, orangeBrush, left + compassCenter, top + compassCenter, compassCenter, relativeBearingRad);
        }
      }
    }

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
    queueProcessor queueProcessor = null;
/*
    Thread queueProcessor = null;
*/
    private Descriptor threadDesc;
    @SuppressWarnings("unchecked")
	private void queueTile(Descriptor desc)
    {
      // Alternative Implementierung mit Threadpools...
      // ThreadPool.QueueUserWorkItem(new WaitCallback(LoadTile), new Descriptor(desc));
    	
      synchronized (queuedTiles)
      {
        if (queuedTiles.contains(desc.GetHashCode()))
          return;

        queuedTiles.add(desc);

        if (queueProcessor == null)
        {
	        queueProcessor = new queueProcessor();
	        queueProcessor.execute(queuedTiles);
        }
/*        if (queueProcessor == null)
        {
          queueProcessor = new Thread(new ThreadStart(queueProcessorEntryPoint));
          queueProcessor.Priority = ThreadPriority.BelowNormal;
          queueProcessor.IsBackground = true;
          queueProcessor.Start();
        }*/
      }
    }
    
    private class queueProcessor extends AsyncTask<LinkedList<Descriptor>, Integer, Integer> {

		@Override
		protected Integer doInBackground(LinkedList<Descriptor>... params) {
			// TODO Auto-generated method stub
			boolean queueEmpty = false;
			
			try
			{
			
				do
			    {
					Descriptor desc = null;
					synchronized (queuedTiles)
					{
						desc = queuedTiles.poll();
					}
			
					if (desc.Zoom == Zoom)
						LoadTile(desc);
					else
					{
						// Da das Image fur diesen Tile nicht geladen wurde, da der Zoom-Faktor des Tiles nicht gleich
						// dem aktuellen ist muss dieser Tile wieder aus loadedTile entfernt werden, da sonst bei
						// spterem Wechsel des Zoom-Faktors dieses Tile nicht angezeigt wird.
						// Dies passiert bei schnellem Wechsel des Zoom-Faktors, wenn noch nicht alle aktuellen Tiles geladen waren.
						if (loadedTiles.containsKey(desc.GetHashCode()))
							loadedTiles.remove(desc.GetHashCode());
					}
			
			
					synchronized (queuedTiles)
					{
						queueEmpty = queuedTiles.size() < 1;
					}
			
			    } while (!queueEmpty);
			}	
			catch (Exception exc)
			{
				String forDebug = exc.getMessage();
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
    Rect getTileRange()
    {
    	int x1;
    	int y1;
    	int x2;
    	int y2;
    	double x = screenCenter.X / (256 * dpiScaleFactorX);
    	double y = screenCenter.Y / (256 * dpiScaleFactorY);

    	x1 = (int)Math.floor(x - width/multiTouchFaktor / (256 * dpiScaleFactorX * 2));
    	x2 = (int)Math.floor(x + width/multiTouchFaktor / (256 * dpiScaleFactorX * 2));
    	y1 = (int)Math.floor(y - height/multiTouchFaktor / (256 * dpiScaleFactorY * 2));
    	y2 = (int)Math.floor(y + height/multiTouchFaktor / (256 * dpiScaleFactorY * 2));
    	return new Rect(x1, y1, x2, y2);
    }

    Paint backBrush;

    Rect tileRect = new Rect(0, 0, 256, 256);

    void renderTile(Tile tile)
    {
      tile.Age = 0;

      Point pt = ToScreen(tile.Descriptor.X, tile.Descriptor.Y, tile.Descriptor.Zoom);
      // relativ zu Zentrum
      pt.x = pt.x - width / 2;
      pt.y = pt.y - height / 2;
      // skalieren
      pt.x = (int) Math.round(pt.x * multiTouchFaktor + width / 2);
      pt.y = (int) Math.round(pt.y * multiTouchFaktor + height / 2);

      if (tile.State == Tile.TileState.Present || tile.State == Tile.TileState.LowResolution)
      {
        drawImage(tile.Image, pt.x, pt.y, (int)(256.0f * dpiScaleFactorX * multiTouchFaktor), (int)(256.0f * dpiScaleFactorY * multiTouchFaktor));
        return;
      }
      try
      {
    	  canvas.drawRect(pt.x, pt.y, (int)(256 * dpiScaleFactorX), (int)(256 * dpiScaleFactorY), backBrush);
    	  Paint paint = new Paint();
    	  paint.setColor(Color.WHITE);
    	  canvas.drawLine(0, 0, 100, 100, paint);
        //.FillRectangle(backBrush, pt.X, pt.Y, (int)(256 * dpiScaleFactorX), (int)(256 * dpiScaleFactorY));
      }
      catch (Exception ex)
      {
      }
    }

    Point ToScreen(double x, double y, int zoom)
    {
      double adjust = Math.pow(2, (Zoom - zoom));
      x = x * adjust * 256 * dpiScaleFactorX;
      y = y * adjust * 256 * dpiScaleFactorY;

      return new Point((int)(x - screenCenter.X) + halfWidth, (int)(y - screenCenter.Y) + halfHeight);
    }

    Point ToScreen(double x, double y, double zoom)
    {
      double adjust = Math.pow(2, (Zoom - zoom));
      x = x * adjust * 256 * dpiScaleFactorX;
      y = y * adjust * 256 * dpiScaleFactorY;

      return new Point((int)(x - screenCenter.X) + halfWidth, (int)(y - screenCenter.Y) + halfHeight);
    }

    /// <summary>
    /// an dieser x-Koordinate beginnt die Skala. Muss beim Resize neu gesetzt werden
    /// </summary>
    int scaleLeft;

    /// <summary>
    /// Breite des Maßstabs
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
/*
    bool arrowHitWhenDown = false;
*/
    private void MapView_MouseDown(int eX, int eY)
    {
      dragging = true;
/*      animationTimer.Enabled = false;*/
//      tabButtonTrackPosition.Down = false;

      lastClickX = dragStartX = eX;
      lastClickY = dragStartY = eY;

/*      arrowHitWhenDown = Math.Sqrt(((e.X - cacheArrowCenter.X) * (e.X - cacheArrowCenter.X) + (e.Y - cacheArrowCenter.Y) * (e.Y - cacheArrowCenter.Y))) < (lineHeight * 1.5f);*/
    }

    private void MapView_MouseUp(int eX, int eY)
    {
      dragging = false;
      updateCacheList();
      Render(false);

/*      if (arrowHitWhenDown && Math.Sqrt(((e.X - cacheArrowCenter.X) * (e.X - cacheArrowCenter.X) + (e.Y - cacheArrowCenter.Y) * (e.Y - cacheArrowCenter.Y))) < (lineHeight * 1.5f))
      {
        Coordinate target = (Global.SelectedWaypoint != null) ? new Coordinate(Global.SelectedWaypoint.Latitude, Global.SelectedWaypoint.Longitude) : new Coordinate(Global.SelectedCache.Latitude, Global.SelectedCache.Longitude);

        startAnimation(target);
        cacheArrowCenter.X = int.MinValue;
        cacheArrowCenter.Y = int.MinValue;
      }
      arrowHitWhenDown = false;*/
    }

    private Coordinate lastMouseCoordinate = null;

    private void MapView_MouseMove(int eX, int eY)
    {
      PointD point = new PointD(0, 0);
      point.X = screenCenter.X + (eX - this.width / 2) / dpiScaleFactorX;
      point.Y = screenCenter.Y + (eY - this.height / 2) / dpiScaleFactorY;;
      lastMouseCoordinate = new Coordinate(Descriptor.TileYToLatitude(Zoom, point.Y / (256.0)), Descriptor.TileXToLongitude(Zoom, point.X / (256.0)));

      if (dragging)
      {
        screenCenter.X += dragStartX - eX;
        screenCenter.Y += dragStartY - eY;
        centerOsmSpace.X = screenCenter.X / dpiScaleFactorX;
        centerOsmSpace.Y = screenCenter.Y / dpiScaleFactorY;

        dragStartX = eX;
        dragStartY = eY;

        Render(false);
      } 
    }

    private void zoomIn()
    {
    	zoomIn(true);    
    }
    private void zoomIn(boolean doRender)
    {
      if (Zoom < maxZoom)
      {
        zoomScaleTimer.cancel();

        centerOsmSpace.X *= 2;
        centerOsmSpace.Y *= 2;
        screenCenter.X *= 2;
        screenCenter.Y *= 2;

        Zoom++;
        buttonZoomOut.setEnabled(true);

        if (Zoom >= maxZoom)
        	buttonZoomIn.setEnabled(false);

        zoomChanged();
        updateCacheList();

        renderZoomScaleActive = true;
        if (doRender)
        	Render(false);
/*        zoomScaleTimer.Enabled = true;*/
        try
        {
        	startZoomScaleTimer();
        } catch (Exception ex)
        { 
        	return;
        }
      }
    }
    
    private void startZoomScaleTimer()
    {
        zoomTimerTask = new TimerTask() {
        	@Override
        	public void run()
        	{
        		renderZoomScaleActive = false;
        		try
        		{
        			Render(false);
        		} catch (Exception exc)
        		{
        			return;
        		}
        	}
        };
        zoomScaleTimer = new Timer();
        zoomScaleTimer.schedule(zoomTimerTask, 1000);    	
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

    	if (Global.Location != null)
    	{
	        // Position auf der Karte
	        Point pt = ToScreen(Descriptor.LongitudeToTileX(Zoom, Global.LastValidPosition.Longitude), Descriptor.LatitudeToTileY(Zoom, Global.LastValidPosition.Latitude), Zoom);
	
	        int size = lineHeight;
	
	        double courseRad = Global.Location.getBearing() * Math.PI / 180.0;
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
	        paint.setColor(Color.RED);
	        paint.setStyle(Style.FILL);
	        canvas.drawPath(path, paint);
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
      if (Zoom > minZoom)
      {
        zoomScaleTimer.cancel();

        screenCenter.X /= 2;
        screenCenter.Y /= 2;
        centerOsmSpace.X /= 2;
        centerOsmSpace.Y /= 2;

        Zoom--;
        buttonZoomIn.setEnabled(true);

        if (Zoom == minZoom)
          buttonZoomOut.setEnabled(false);

        zoomChanged();
        updateCacheList();
        renderZoomScaleActive = true;
        if (doRender)
        	Render(false);
        try
        {
        	startZoomScaleTimer();
        } catch (Exception ex)
        {
        	return;
        }
/*        zoomScaleTimer.Enabled = true;*/

      }
    }

    double pixelsPerMeter;

    /// <summary>
    /// Anzahl der Schritte auf dem Maßstab
    /// </summary>
    int scaleUnits = 10;

    /// <summary>
    /// Länge des Maßstabs in Metern
    /// </summary>
    double scaleLength = 1000;


    /// <summary>
    /// Nachdem Zoom verändert wurde müssen einige Werte neu berechnet werden
    /// </summary>
    private void zoomChanged()
    {
      int[] scaleNumUnits = new int[] { 4, 3, 4, 3, 4, 5, 3 };
      float[] scaleSteps = new float[] { 1, 1.5f, 2, 3, 4, 5, 7.5f };
/*
      if (animationTimer != null)
        animationTimer.Enabled = false;
*/
      adjustmentCurrentToCacheZoom = Math.pow(2, Zoom - Cache.MapZoomLevel);

      // Infos für den Maßstab neu berechnen
      Coordinate dummy = Coordinate.Project(center.Latitude, center.Longitude, 90, 1000);
      double l1 = Descriptor.LongitudeToTileX(Zoom, center.Longitude);
      double l2 = Descriptor.LongitudeToTileX(Zoom, dummy.Longitude);
      double diff = Math.abs(l2 - l1);
      pixelsPerMeter = (diff * 256 * dpiScaleFactorX) / 1000;

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
    }
/*
    Brush[] brushes = new Brush[] { new SolidBrush(Color.Black), new SolidBrush(Color.White) };
*/
    Paint font = new Paint();
    Paint fontSmall = new Paint();

    /// <summary>
    /// Zeichnet den Maßstab. pixelsPerKm muss durch zoomChanged
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
        Paint fillPaint = new Paint();
        
        canvas.drawRect(new Rect(start + scaleLeft, height - lineHeight / 2 - lineHeight / 4, pos + scaleLeft, height - lineHeight / 4), brushes[i % 2]);
//        graphics.FillRectangle(brushes[i % 2], start + scaleLeft, height - lineHeight / 2 - lineHeight / 4, pos - start, lineHeight / 2);
        start = pos;
      }

      Paint blackPen = new Paint();
      blackPen.setColor(Color.BLACK);
      canvas.drawRect(new Rect(scaleLeft - 1, height - lineHeight / 2 - lineHeight / 4 - 1, scaleLeft + pos, height - lineHeight / 4 + 1), blackPen);
//      graphics.DrawRectangle(blackPen, scaleLeft - 1, height - lineHeight / 2 - lineHeight / 4 - 1, pos + 1, lineHeight / 2 + 1);
/*
      String distanceString;
      if (UnitFormatter.ImperialUnits)
    	  distanceString = String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00}mi", scaleLength / 1609.3);
      else
        if (scaleLength <= 500)
          distanceString = String.Format("{0:0}m", scaleLength);
        else
        {
          double length = scaleLength / 1000;
          distanceString = length.ToString(NumberFormatInfo.InvariantInfo) + "km";
        }

      graphics.DrawString(distanceString, font, brushes[0], scaleLeft + pos + lineHeight / 2, height - lineHeight);
*/
    }
/*
    private void MapView_DoubleClick(object sender, EventArgs e)
    {

      WaypointRenderInfo minWpi = new WaypointRenderInfo();
      minWpi.Cache = null;

      int minDist = int.MaxValue;
      // Überprüfen, auf welchen Cache geklickt wurde
      for (int i = wpToRender.Count - 1; i >= 0; i--)
      {
        WaypointRenderInfo wpi = wpToRender[i];
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

      int legalWidth = (int)(minWpi.Icon.Width * dpiScaleFactorX * 1.5f);

      if (minDist > (legalWidth * legalWidth))
        return;

      if (minWpi.Waypoint != null)
      {
        // Wegpunktliste ausrichten
        Global.SelectedCache = minWpi.Cache;
        Global.SelectedWaypoint = minWpi.Waypoint;
        //FormMain.WaypointListPanel.AlignSelected();
      }
      else
      {
        // Cacheliste ausrichten
        Global.SelectedCache = minWpi.Cache;
        //                Global.SelectedWaypoint = null;
        //                FormMain.CacheListPanel.AlignSelected();
      }
      this.Focus();
    }

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


    /// <summary>
    /// Ausgangspunkt der Animation
    /// </summary>
    PointD animateFrom = new PointD();

    /// <summary>
    /// Zielpunkt der Animation
    /// </summary>
    PointD animateTo = new PointD();

    // Zeitpunkt des Startes der Animation
    long animationStart;

    /// <summary>
    /// Dauer der Animation in ms
    /// </summary>
    const int animationDuration = 500;

    void startAnimation(Coordinate target)
    {
      animationStart = Environment.TickCount;
      animateFrom.X = screenCenter.X;
      animateFrom.Y = screenCenter.Y;

      animateTo.X = dpiScaleFactorX * 256 * Descriptor.LongitudeToTileX(Zoom, target.Longitude);
      animateTo.Y = dpiScaleFactorY * 256 * Descriptor.LatitudeToTileY(Zoom, target.Latitude);

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
    }

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

      Paint paint = new Paint();
      paint.setColor(Color.BLACK);
      canvas.drawLine(centerColumn, topRow, centerColumn, bottomRow, paint);

      float numSteps = maxZoom - minZoom;
      for (int i = minZoom; i <= maxZoom; i++)
      {
        int y = (int)((1 - ((float)(i - minZoom)) / numSteps) * (bottomRow - topRow)) + topRow;

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
        	
        	String label = String.valueOf(Zoom);
        	int textWidth = (int)font.measureText(label);
        	int textHeight = 28;
        	Rect bounds = new Rect();
        	font.getTextBounds(label, 0, label.length(), bounds);
        	textWidth = bounds.width();
        	textHeight = (int)(bounds.height() * 1.5);
//          SizeF sizeF = graphics.MeasureString(label, font);
//          Size size = new Size((int)sizeF.Width, (int)sizeF.Height);
        	canvas.drawRect(new Rect(centerColumn - textWidth / 2 - lineHeight / 2, y - textHeight / 2, centerColumn - textWidth / 2 - lineHeight / 2 + textWidth + lineHeight, y - textHeight / 2 + textHeight), white);
        	canvas.drawRect(new Rect(centerColumn - textWidth / 2 - 1 - lineHeight / 2, y - textHeight / 2 - 1, centerColumn - textWidth / 2 - 1 - lineHeight / 2 + textWidth + lineHeight + 1, y - textHeight / 2 - 1 + textHeight + 1), black);
        	canvas.drawText(label, centerColumn - textWidth / 2, y + textHeight / 2, font);
        }
        else
          canvas.drawLine(centerColumn - halfWidth, y, centerColumn + halfWidth, y, black);
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
      synchronized (loadedTiles)
      {
        for (long hash : loadedTiles.keySet())
          if (loadedTiles.get(hash).Image != null)
            loadedTiles.get(hash).Image.recycle();

        loadedTiles.clear();
      }

    }
/*
    Point cacheArrowCenter = new Point(int.MinValue, int.MinValue);
    Font distanceFont = new Font(FontFamily.GenericSansSerif, 9, FontStyle.Regular);

    public void RenderTargetArrow()
    {
      cacheArrowCenter.X = int.MinValue;
      cacheArrowCenter.Y = int.MinValue;

      if (Global.SelectedCache == null)
        return;

      double lat = (Global.SelectedWaypoint != null) ? Global.SelectedWaypoint.Latitude : Global.SelectedCache.Latitude;
      double lon = (Global.SelectedWaypoint != null) ? Global.SelectedWaypoint.Longitude : Global.SelectedCache.Longitude;

      Coordinate center = Center;
      float distance = Datum.WGS84.Distance(center.Latitude, center.Longitude, lat, lon);

      double x = 256.0 * Map.Descriptor.LongitudeToTileX(Zoom, lon) * dpiScaleFactorX;
      double y = 256.0 * Map.Descriptor.LatitudeToTileY(Zoom, lat) * dpiScaleFactorY;

      int halfHeight = height / 2;
      int halfWidth = width / 2;

      double dirx = x - screenCenter.X;
      double diry = y - screenCenter.Y;

      //  if (!(Math.Abs(dirx) > (width / 2) || Math.Abs(diry) > (height / 2)))
      // Ziel sichtbar, Pfeil nicht rendern
      //     return;

      double cx = dirx;
      double cy = diry;

      int toprow = -halfHeight + ((showCompass) ? (button1.Height + button1.Top) : 0);

      if (cy > halfHeight - lineHeight)
      {
        cx = clip(0, 0, cy, cx, halfHeight - lineHeight);
        cy = halfHeight;
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

      clipLineCircle(-halfWidth, -halfHeight, button1.Width * 1.5, 0, 0, ref cx, ref cy);
      clipLineCircle(halfWidth, -halfHeight, button1.Width * 1.5, 0, 0, ref cx, ref cy);
      clipLineCircle(-halfWidth, halfHeight, button1.Width * 1.5, 0, 0, ref cx, ref cy);
      clipLineCircle(halfWidth, halfHeight, button1.Width * 1.5, 0, 0, ref cx, ref cy);

      // Position auf der Karte
      Point pt = new Point((int)(cx + halfWidth), (int)(cy + halfHeight));

      double length = Math.Sqrt(cx * cx + cy * cy);

      int size = lineHeight;

      float dirX = -(float)(cx / length);
      float dirY = -(float)(cy / length);

      float crossX = -dirY;
      float crossY = dirX;

      Point[] dir = new Point[3];
      dir[0].X = (int)(pt.X);
      dir[0].Y = (int)(pt.Y);

      // x/y -> -y/x
      dir[1].X = (int)(pt.X + dirX * 1.5f * size - crossX * size * 0.5f);
      dir[1].Y = (int)(pt.Y + dirY * 1.5f * size - crossY * size * 0.5f);

      dir[2].X = (int)(pt.X + dirX * 1.5f * size + crossX * size * 0.5f);
      dir[2].Y = (int)(pt.Y + dirY * 1.5f * size + crossY * size * 0.5f);

      if (Math.Abs(dirx) > (width / 2) || Math.Abs(diry) > (height / 2))
      {
        graphics.FillPolygon(redBrush, dir);
        graphics.DrawPolygon(blackPen, dir);
      }

      float fontCenterX = pt.X + dirX * 2.2f * size;
      float fontCenterY = pt.Y + dirY * 2.2f * size;

      // Anzeige Pfeile zum Ziel auf Karte mit Waypoint abfrage
      String text = UnitFormatter.DistanceString(distance);

      SizeF textSize = graphics.MeasureString(text, distanceFont);

      if (Math.Abs(dirx) > (width / 2) || Math.Abs(diry) > (height / 2))
        graphics.DrawString(text, distanceFont, blackBrush, fontCenterX - textSize.Width / 2, fontCenterY - textSize.Height / 2);

      cacheArrowCenter.X = (int)(pt.X + dirX * 1.5f * size);
      cacheArrowCenter.Y = (int)(pt.Y + dirY * 1.5f * size);
    }


    private void clipLineCircle(double cx, double cy, double r, double x1, double y1, ref double x2, ref double y2)
    {
      if (((cx - x2) * (cx - x2) + (cy - y2) * (cy - y2)) > r * r)
        return;

      double a = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
      double b = 2 * ((x2 - x1) * (x1 - cx) + (y2 - y1) * (y1 - cy));
      double c = cx * cx + cy * cy + x1 * x1 + y1 * y1 - 2 * (cx * x1 + cy * y1) - r * r;

      double u = (-b - Math.Sqrt(b * b - 4 * a * c)) / (2 * a);
      double behaviour = b * b - 4 * a * c;

      x2 = x1 + u * (x2 - x1);
      y2 = y1 + u * (y2 - y1);
    }

    double clip(double a, double b, double c, double d, double clip)
    {
      return b + ((clip - a) / (c - a)) * (d - b);
    }


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
        for (Tile tile : loadedTiles.values())
        	if (tile.Image != null)
        		cnt++;
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
        // Muss der aktive Track gezeichnet werden?
/*        if (Global.AktuelleRoute.ShowRoute)
        {
            // Map Tiles aktualisieren, wenn der AktiveTrack erweitert wurde!
            if ((Global.AktuelleRoute.Points.Count > aktuelleRouteCount) && (Global.AktuelleRoute.Points.Count > 1))
            {
                // Liste aller neuen Punkte erstellen incl. dem letzten.
                List<PointD> punkte = new List<PointD>();
                for (int i = aktuelleRouteCount - 1; i < Global.AktuelleRoute.Points.Count; i++)
                {
                    if (i < 0) continue;
                    punkte.Add(Global.AktuelleRoute.Points[i]);
                }
                aktuelleRouteCount = Global.AktuelleRoute.Points.Count;

                Pen pen = Global.AktuelleRoute.Pen;
                lock (loadedTiles)
                {
                    foreach (KeyValuePair<Descriptor, Tile> tile in loadedTiles)
                    {
                        if (tile.Value.Image == null) continue;
                        Graphics graphics = Graphics.FromImage(tile.Value.Image);

                        double tileX = tile.Key.X * 256 * dpiScaleFactorX;
                        double tileY = tile.Key.Y * 256 * dpiScaleFactorY;

                        double adjustmentX = Math.Pow(2, tile.Key.Zoom - projectionZoomLevel) * 256 * dpiScaleFactorX;
                        double adjustmentY = Math.Pow(2, tile.Key.Zoom - projectionZoomLevel) * 256 * dpiScaleFactorY;

                        for (int j = 0; j < (punkte.Count - 1); j++)
                        {
                            graphics.DrawLine(pen, (int)(punkte[j].X * adjustmentX - tileX),
                                (int)(punkte[j].Y * adjustmentY - tileY),
                                (int)(punkte[j + 1].X * adjustmentX - tileX),
                                (int)(punkte[j + 1].Y * adjustmentY - tileY));
                        }
                        if (punkte.Count > 0)
                        {
                            double x = (punkte[punkte.Count - 1].X * adjustmentX - tileX);
                            double y = (punkte[punkte.Count - 1].Y * adjustmentY - tileY);
                            //                            graphics.DrawString(x.ToString() + " - " + y.ToString(), fontSmall, new SolidBrush(Color.Black), 20, 20);
                        }
                    }
                }
            }
        }
*/
/*        if (tabButtonTrackPosition.Down && !animationTimer.Enabled)*/
            setCenter(new Coordinate(Global.LastValidPosition));

        Render(false);
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.plus: 
				zoomIn();
				return true;
			case R.id.minus:
				zoomOut();
				return true;
			case R.id.hubertmedia:
				SetCurrentLayer(MapView.Manager.GetLayerByName("Hubermedia Bavaria", "Hubermedia Bavaria", ""));
				return true;
			case R.id.googleearth:
				SetCurrentLayer(MapView.Manager.GetLayerByName("Google Earth", "Google Earth", ""));
				return true;
		}
		return false;
	}

}
