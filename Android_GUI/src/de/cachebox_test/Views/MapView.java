package de.cachebox_test.Views;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.SmoothScrollingTyp;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Map.Layer;
import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.MysterySolution;
import CB_Core.Types.Waypoint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomButton;
import android.widget.ZoomControls;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.UnitFormatter;
import de.cachebox_test.main;
import de.cachebox_test.Components.CacheDraw;
import de.cachebox_test.Custom_Controls.MultiToggleButton;
import de.cachebox_test.Events.PositionEvent;
import de.cachebox_test.Events.PositionEventList;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Map.Manager;
import de.cachebox_test.Map.RouteOverlay;
import de.cachebox_test.Map.Tile;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AllContextMenuCallHandler;
import de.cachebox_test.Views.Forms.ScreenLock;

public class MapView extends RelativeLayout implements SelectedCacheEvent, PositionEvent, ViewOptionsMenu,
		CB_Core.Events.CacheListChangedEvent
{
	private boolean isVisible; // true, when MapView is visible
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

	public MapView(Context context, LayoutInflater inflater)
	{
		super(context);
		lockPosition = 0;
		useLockPosition = true;
		myContext = context;

		try
		{
			// GlobalCore.SmoothScrolling =
			// SmoothScrollingTyp.valueOf(Config.settings.SmoothScrolling.getValue());
			GlobalCore.SmoothScrolling = Config.settings.SmoothScrolling.getEnumValue();
		}
		catch (Exception ex)
		{
			GlobalCore.SmoothScrolling = SmoothScrollingTyp.normal;
		}

		activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		available_bytes = activityManager.getMemoryClass();
		if (available_bytes > 31)
		{
			// Geräte mit mindestens 32MB verfügbar
			rangeFactorTiles = 1.5f;
			numMaxTiles = 48;
			numMaxTrackTiles = 24;
		}
		if (available_bytes < 20)
		{
			// Geräte mit nur 16MB verfügbar
			// Minimalausstattung verwenden
			rangeFactorTiles = 1.0f;
			numMaxTiles = 12;
			numMaxTrackTiles = 12;
		}

		RelativeLayout mapviewLayout = (RelativeLayout) inflater.inflate(R.layout.mapview, null, false);
		this.addView(mapviewLayout);

		surface = (SurfaceView) findViewById(R.id.mapview_surface);

		setWillNotDraw(false);

		holder = surface.getHolder();

		// animationThread = new AnimationThread();
		// animationThread.start();

		buttonTrackPosition = (MultiToggleButton) findViewById(R.id.mapview_trackposition);
		buttonTrackPosition.clearStates();
		buttonTrackPosition.addState("Free", Color.GRAY);
		buttonTrackPosition.addState("GPS", Color.GREEN);
		buttonTrackPosition.addState("Lock", Color.RED);
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
			public void onClick(View v)
			{
				zoomIn();
			}

		});
		/* zoom controls out */
		zoomControls.setOnZoomOutClickListener(new ZoomControls.OnClickListener()
		{
			public void onClick(View v)
			{
				zoomOut();
			}

		});

		ArrayList<android.view.View> buttons = new ArrayList<android.view.View>();
		this.addTouchables(buttons);

		font.setTextSize(UiSizes.getScaledFontSize_big() * dpiScaleFactorX);
		font.setFakeBoldText(true);
		font.setAntiAlias(true);
		fontSmall.setTextSize(UiSizes.getScaledFontSize() * dpiScaleFactorX);
		fontSmall.setFakeBoldText(true);
		fontSmall.setAntiAlias(true);
		PositionEventList.Add(this);
		CachListChangedEventList.Add(this);
		SelectedCacheEventList.Add(this);

		zoomScaleTimer = new Timer();

	}

	public void setNewSettings()
	{
		// set Zoom Button Style
		try
		{
			((ZoomButton) zoomControls.getChildAt(0)).setBackgroundResource(main.N ? R.drawable.night_btn_zoom_down
					: R.drawable.day_btn_zoom_down);
			((ZoomButton) zoomControls.getChildAt(1)).setBackgroundResource(main.N ? R.drawable.night_btn_zoom_up
					: R.drawable.day_btn_zoom_up);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		showRating = Config.settings.MapShowRating.getValue();
		showDT = Config.settings.MapShowDT.getValue();
		showTitles = Config.settings.MapShowTitles.getValue();
		hideMyFinds = Config.settings.MapHideMyFinds.getValue();
		showCompass = Config.settings.MapShowCompass.getValue();
		showDirektLine = Config.settings.ShowDirektLine.getValue();
		nightMode = Config.settings.nightMode.getValue();

		// Skalierungsfaktoren bestimmen
		if (Config.settings.OsmDpiAwareRendering.getValue())
		{
			dpiScaleFactorX = dpiScaleFactorY = getContext().getResources().getDisplayMetrics().density;
		}
		else
		{
			dpiScaleFactorX = dpiScaleFactorY = 1;
		}

		font.setTextSize(UiSizes.getScaledFontSize_big() * dpiScaleFactorX);
		font.setFakeBoldText(true);
		font.setAntiAlias(true);
		fontSmall.setTextSize(UiSizes.getScaledFontSize() * dpiScaleFactorX);
		fontSmall.setFakeBoldText(true);
		fontSmall.setAntiAlias(true);
		ClearCachedTiles();
		Render(true);
	}

	private void setLockPosition(int value)
	{
		lockPosition = value;

		if (lockPosition > 0)
		{
			if (GlobalCore.Marker.Valid)
			{
				startAnimation(new Coordinate(GlobalCore.Marker.Latitude, GlobalCore.Marker.Longitude));
				return;
			}
			if (GlobalCore.LastValidPosition != null && GlobalCore.LastValidPosition.Valid)
			{
				startAnimation(GlobalCore.LastValidPosition);
				return;
			}
		}
		else
		{
			buttonTrackPosition.setState(0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		try
		{
			Render(true);
		}
		catch (Exception exc)
		{
			Logger.Error("MapView.onDraw", "", exc);
			return;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
		halfWidth = w / 2;
		halfHeight = h / 2;

		scaleLeft = 0;// button2.Left + button2.Width + lineHeight;
		scaleWidth = width - scaleLeft - (int) font.measureText("100km ") + 1;

		offScreenBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		canvas = new Canvas(offScreenBmp);
		canvasOverlay = canvas;

		canvasHeading = 0;
		drawingWidth = width;
		drawingHeight = height;
		// canvas.rotate(45, width / 2, height / 2);

		updateCacheList();
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
		float[] src =
			{ p.x, p.y };
		float[] dist = new float[2];
		Matrix mat = new Matrix();
		mat.setRotate(heading, width / 2, halfHeight);
		mat.mapPoints(dist, src);
		return new Point((int) dist[0], (int) dist[1]);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (lockPosition == 2) return true;

		int eX = 0;
		int eY = 0;
		try
		{
			eX = (int) event.getX(0);
			eY = (int) event.getY(0);
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
				int eX2 = (int) event.getX(1);
				int eY2 = (int) event.getY(1);
				if (alignToCompass)
				{
					Point rot = rotate(new Point(eX2, eY2), canvasHeading);
					eX2 = rot.x;
					eY2 = rot.y;
				}

				double multiTouchDist = Math.sqrt(Math.pow(eX2 - eX, 2) + Math.pow(eY2 - eY, 2));
				if (!multiTouch) lastMultiTouchDist = multiTouchDist;
				// debugString1 = "" + multiTouchDist;
				// debugString2 = "" + lastMultiTouchDist;
				if (Zoom >= maxZoom)
				{
					if (multiTouchDist > lastMultiTouchDist) multiTouchDist = lastMultiTouchDist;
				}
				if (Zoom <= minZoom)
				{
					if (multiTouchDist < lastMultiTouchDist) multiTouchDist = lastMultiTouchDist;

				}

				if (multiTouch)
				{
					if (lastMultiTouchDist > multiTouchDist * 1.5)
					{
						zoomOutDirect(false);
						lastMultiTouchDist /= 2; // multiTouchDist;
					}
					else if (lastMultiTouchDist < multiTouchDist * 0.75)
					{
						zoomInDirect(false);
						lastMultiTouchDist *= 2; // multiTouchDist;
					}
				}
				else
					lastMultiTouchDist = multiTouchDist;

				if (lastMultiTouchDist > 0) multiTouchFaktor = multiTouchDist / lastMultiTouchDist;
				else
					multiTouchFaktor = 1;
				// debugString1 = "f: " + multiTouchFaktor;

				eX = (int) (eX + (eX2 - eX) / 2);
				eY = (int) (eY + (eY2 - eY) / 2);
				if (!multiTouch)
				{
					dragStartX = lastClickX = eX;
					dragStartY = lastClickY = eY;
				}
				multiTouch = true;
				mouseMoved = true;
			}
			else
			{
				if (multiTouch)
				{
					dragStartX = lastClickX = eX;
					dragStartY = lastClickY = eY;
				}
				multiTouch = false;
				if (renderZoomScaleActive) startZoomScaleTimer();
			}
		}
		finally
		{
		}

		switch (event.getAction())
		{
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
				coord = new Coordinate(Descriptor.TileYToLatitude(Zoom, screenCenter.Y / (256.0)), Descriptor.TileXToLongitude(Zoom,
						screenCenter.X / (256.0)));

				animationThread.moveTo(coord, smoothScrolling.AnimationSteps() * 2, false);

				MapView_MouseDown(eX, eY);
			}
			// touch_start(x, y);
			// invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			synchronized (screenCenter)
			{
				lastMouseMoveTickDiff[lastMouseMoveTickPos] = SystemClock.uptimeMillis() - lastMouseMoveTick;
				lastMouseMoveTick = SystemClock.uptimeMillis();
				lastMouseDiff[lastMouseMoveTickPos] = new Point(eX - lastMousePos.x, eY - lastMousePos.y);
				lastMouseMoveTickPos++;
				lastMouseMoveTickCount++;
				if (lastMouseMoveTickPos > 4) lastMouseMoveTickPos = 0;
				lastMousePos = new Point(eX, eY);
				if (!mouseMoved)
				{
					Point akt = new Point(eX, eY);
					mouseMoved = ((Math.abs(akt.x - mouseDownPos.x) > 10) || Math.abs(akt.y - mouseDownPos.y) > 10);
				}
				if (mouseMoved) MapView_MouseMove(eX, eY);
				// touch_move(x, y);
				// invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			// multiTouchFaktor = 1;
			if ((multiTouchFaktor < 0.99) || (multiTouchFaktor > 1.01)) animationThread.zoomTo(Zoom);
			synchronized (screenCenter)
			{
				if (mouseMoved)
				{
					// MapView_MouseUp(eX, eY);
					// Nachlauf der Map
					double dx = 0;
					double dy = 0;
					double dt = 0;
					int count = Math.min(5, lastMouseMoveTickCount);
					if (GlobalCore.SmoothScrolling != SmoothScrollingTyp.none)
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
						}
						finally
						{
						}
						nachlauf.X -= dx * newPosFaktor / dt * smoothScrolling.AnimationWait();
						nachlauf.Y -= dy * newPosFaktor / dt * smoothScrolling.AnimationWait();
						Coordinate coord = new Coordinate(Descriptor.TileYToLatitude(Zoom, nachlauf.Y / (256.0)),
								Descriptor.TileXToLongitude(Zoom, nachlauf.X / (256.0)));
						mouseMoved = false;
						animationThread.moveTo(coord, smoothScrolling.AnimationSteps() * 2, false);
					}
					else
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
				// touch_up();
				// invalidate();
			}
			break;
		}
		return true;
	}

	/*
	 * public delegate void TileLoadedHandler(Bitmap bitmap, Descriptor desc); public event TileLoadedHandler OnTileLoaded = null;
	 */

	/**
	 * Aktuell betrachteter Layer
	 */
	public Layer CurrentLayer = null;

	public void SetCurrentLayer(Layer newLayer)
	{
		Config.settings.CurrentMapLayer.setValue(newLayer.Name);
		Config.AcceptChanges();

		CurrentLayer = newLayer;

		loadedTilesLock.lock();
		try
		{
			ClearCachedTiles();
		}
		finally
		{
			loadedTilesLock.unlock();
		}
		Render(true);
	}

	/**
	 * Tile Manager
	 */
	public static Manager Manager = new Manager();

	/**
	 * Wunschzettel. Diese Deskriptoren werden von loaderthread geladen und instanziiert
	 */
	ArrayList<Descriptor> wishlist = new ArrayList<Descriptor>();
	private Lock wishlistLock = new ReentrantLock();

	/**
	 * Instanz des Loaders
	 */
	loaderThread loaderThread = null;

	/**
	 * Liste mit den darzustellenden Wegpunkten
	 */
	ArrayList<WaypointRenderInfo> wpToRender = new ArrayList<WaypointRenderInfo>();

	/**
	 * Speichert die Informationen für einen im Sichtfeld befindlichen Waypoint
	 */
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

	/**
	 * true, falls das Rating der Caches angezeigt werden soll
	 */
	public boolean showRating = true;

	/**
	 * true, falls die D/T Werte der Caches angezeigt werden soll
	 */
	public boolean showDT = true;

	/*
	 * true, falls eine direkte Line zum Cache angezeigt werden soll
	 */
	public boolean showDirektLine = false;

	/**
	 * true, falls die WP-Beschriftungen gezeigt werden sollen
	 */
	public boolean showTitles = true;

	/**
	 * true, falls bei Mysterys mit Lösung (Final Waypoint) der Cache ausgeblendet werden soll, wenn der Cache nicht selected ist.
	 */
	boolean hideCacheWithFinal = true;

	/**
	 * true, falls der kleine Kompass open angezeigt werden soll
	 */
	boolean showCompass = true;

	/**
	 * true, falls die Map-Anzeige am Compass ausgerichtet werden soll
	 */
	public boolean alignToCompass = false;

	/**
	 * Spiegelung des Logins bei Gc, damit ich das nicht dauernd aus der Config lesen muss.
	 */
	// String gcLogin = "";

	/**
	 * true, falls Center gültige Koordinaten enthält
	 */
	boolean positionInitialized = false;

	public boolean hideMyFinds = false;

	public Coordinate center = new Coordinate(48.0, 12.0);

	PointD centerOsmSpace = new PointD(0, 0);

	/*
	 * /// /// Wegpunkt des Markers. Wird bei go to als Ziel gesetzt // //Waypoint markerWaypoint = new Waypoint("MARKER",
	 * CacheTypes.ReferencePoint, "Marker", 0, 0, 0);
	 */
	/**
	 * Der Kartenmittelpunkt. Wird dieser Wert überschrieben wird die Liste sichtbarer Caches entsprechend aktualisiert.
	 */
	public Coordinate getCenter()
	{
		return new Coordinate(Descriptor.TileYToLatitude(Zoom, centerOsmSpace.Y / (256.0)), Descriptor.TileXToLongitude(Zoom,
				centerOsmSpace.X / (256.0)));
	}

	public void setCenter(Coordinate value)
	{
		synchronized (screenCenter)
		{

			if (center == null) center = new Coordinate(48.0, 12.0);
			positionInitialized = true;
			/*
			 * if (animationTimer != null) animationTimer.Enabled = false;
			 */
			if (center == value) return;

			center = value;
			centerOsmSpace = Descriptor.ToWorld(Descriptor.LongitudeToTileX(Zoom, center.Longitude),
					Descriptor.LatitudeToTileY(Zoom, center.Latitude), Zoom, Zoom);
			screenCenter.X = Math.round(centerOsmSpace.X);
			screenCenter.Y = Math.round(centerOsmSpace.Y);
			if (animationThread != null)
			{
				animationThread.toX = screenCenter.X;
				animationThread.toY = screenCenter.Y;
			}
		}

		updateCacheList();
	}

	public PointD screenCenter = new PointD(0, 0);

	protected Canvas canvas = null;
	protected Canvas canvasOverlay = null;
	protected float canvasHeading = 0;
	protected int drawingWidth = 0;
	protected int drawingHeight = 0;

	public int Zoom = 14;

	/**
	 * Hashtabelle mit geladenen Kacheln
	 */
	protected Hashtable<Long, Tile> loadedTiles = new Hashtable<Long, Tile>();
	final Lock loadedTilesLock = new ReentrantLock();

	/**
	 * Hashtabelle mit Kacheln der GPX Tracks zum Overlay über die MapTiles
	 */
	protected Hashtable<Long, Tile> trackTiles = new Hashtable<Long, Tile>();
	final Lock trackTilesLock = new ReentrantLock();

	/**
	 * Horizontaler Skalierungsfaktor bei DpiAwareRendering
	 */
	public static float dpiScaleFactorX = 1;

	/**
	 * Vertikaler Skalierungsfaktor bei DpiAwareRendering
	 */
	public static float dpiScaleFactorY = 1;

	// TODO: Dies schlau berechnen!
	/**
	 * Größe des Kachel-Caches
	 */
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
	 * ClickContext mapMenu = null; ClickContext markerMenu = null; ClickContext layerMenu = null; ClickContext routeMenu = null;
	 * ClickContext viewMenu = null; ClickButton removeMarkerButton = null; ClickButton hideFindsButton = null; ClickButton showRatingButton
	 * = null; ClickButton showDTButton = null; ClickButton showTitlesButton = null; ClickButton showCompassButton = null; ClickButton
	 * nightmodeButton = null; List<ClickButton> layerButtons = new List<ClickButton>();
	 */
	int mapMaxCachesLabel = 12;
	int mapMaxCachesDisplay = 10000;
	int mapMaxCachesDisplayLarge = 75;
	int zoomCross = 16;

	boolean nightMode = false;

	/*
	 * public MapView() { View = this; InitializeComponent(); }
	 */
	public void Initialize()
	{
		backBrush = new Paint();
		backBrush.setColor(Color.argb(255, 201, 233, 203));
		/*
		 * MouseWheel += new MouseEventHandler(MapView_MouseWheel); mapMaxCachesLabel = Config.settings.MapMaxCachesLabel"); showRating =
		 * Config.settings.MapShowRating"); showDT = Config.settings.MapShowDT"); showTitles = Config.settings.MapShowTitles"); hideMyFinds
		 * = Config.settings.MapHideMyFinds"); showCompass = Config.settings.MapShowCompass"); nightMode = Config.settings.nightMode");
		 * Global.TargetChanged += new Global.TargetChangedHandler(OnTargetChanged); lineHeight =
		 * (int)this.CreateGraphics().MeasureString("M", Font).Height; smallLineHeight = (int)this.CreateGraphics().MeasureString("M",
		 * fontSmall).Height; mapMenu = new ClickContext(this); mapMenu.Add(new ClickButton("Layer", null, showLayerMenu, null, null),
		 * false); mapMenu.Add(new ClickButton("Center Point", null, showMarkerMenu, null, null), false); mapMenu.Add(new
		 * ClickButton("Route", null, showRouteMenu, null, null), false); mapMenu.Add(new ClickButton("View", null, showViewMenu, null,
		 * null), false); viewMenu = new ClickContext(this); viewMenu.Add(hideFindsButton = new ClickButton("Hide finds", (hideMyFinds) ?
		 * Global.Icons[6] : Global.Icons[7], hideFinds, null, null), false); viewMenu.Add(showRatingButton = new ClickButton("Show Rating",
		 * (showRating) ? Global.Icons[6] : Global.Icons[7], showRatingChanged, null, null), false); viewMenu.Add(showDTButton = new
		 * ClickButton("Show D/T", (showDT) ? Global.Icons[6] : Global.Icons[7], showDTChanged, null, null), false);
		 * viewMenu.Add(showTitlesButton = new ClickButton("Show Titles", (showTitles) ? Global.Icons[6] : Global.Icons[7],
		 * showTitlesChanged, null, null), false); viewMenu.Add(showCompassButton = new ClickButton("Show Compass", (showCompass) ?
		 * Global.Icons[6] : Global.Icons[7], showCompassChanged, null, null), false); viewMenu.Add(nightmodeButton = new
		 * ClickButton("Enable Nightmode", (nightMode) ? Global.Icons[6] : Global.Icons[7], enableNightmodeChanged, null, null), false);
		 * markerMenu = new ClickContext(this); markerMenu.Add(new ClickButton("Set", null, setMarker, null, null), false);
		 * markerMenu.Add(removeMarkerButton = new ClickButton("Remove", null, removeMarker, null, null, false), false); routeMenu = new
		 * ClickContext(this); routeMenu.Add(new ClickButton("Route to WP", null, showNavigationDialog, null, null), false);
		 * routeMenu.Add(new ClickButton("Reset Route", null, resetRoute, null, null), false); imageAttributes = new ImageAttributes();
		 * imageAttributes.SetColorKey(Global.SmallStarIcons[0].GetPixel(0, 0), Global.SmallStarIcons[0].GetPixel(0, 0));
		 * colorKey.SetColorKey(Global.Icons[19].GetPixel(0, 0), Global.Icons[19].GetPixel(0, 0));
		 */
		String currentLayerName = Config.settings.CurrentMapLayer.getValue();
		CurrentLayer = Manager.GetLayerByName((currentLayerName == "") ? "Mapnik" : currentLayerName, currentLayerName, "");

		// layerMenu = new ClickContext(this);
		/*
		 * OnTileLoaded += new TileLoadedHandler(MapView_OnTileLoaded); if (Config.settings.RouteOverlay").Length > 0 &&
		 * File.Exists(Config.settings.RouteOverlay"))) Routes.Add(LoadRoute(Config.settings.RouteOverlay"), new Pen(Color.Purple, 4),
		 * Config.settings.TrackDistance"))); else
		 */

		RouteOverlay.Routes.clear();
		/*
		 * Paint paint = new Paint(); paint.setColor(Color.BLUE); paint.setStrokeWidth(4); Global.AktuelleRoute = new
		 * RouteOverlay.Route(paint, "actual Track"); Global.AktuelleRoute.ShowRoute = false; RouteOverlay.Routes.add(Global.AktuelleRoute);
		 */
		// Load Routes for Autoload
		/*
		 * File dir = new File(Config.settings.MapPackFolder")); String[] files = dir.list(); if (!(files == null)) { if (files.length>0) {
		 * for (String file : files) { MapView.Manager.LoadMapPack(Config.settings.MapPackFolder") + "/" + file); } } }
		 */
		String trackPath = Config.settings.TrackFolder.getValue() + "/Autoload";
		if (FileIO.DirectoryExists(trackPath))
		{
			File dir = new File(trackPath);
			String[] files = dir.list();
			if (!(files == null))
			{
				if (files.length > 0)
				{
					for (String file : files)
					{
						LoadTrack(trackPath, file);

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

	public void LoadTrack(String trackPath)
	{
		LoadTrack(trackPath, "");
	}

	public void LoadTrack(String trackPath, String file)
	{
		int[] ColorField = new int[8];
		ColorField[0] = Color.RED;
		ColorField[1] = Color.YELLOW;
		ColorField[2] = Color.BLACK;
		ColorField[3] = Color.LTGRAY;
		ColorField[4] = Color.GREEN;
		ColorField[5] = Color.BLUE;
		ColorField[6] = Color.CYAN;
		ColorField[7] = Color.GRAY;
		int TrackColor;
		TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];

		Paint paint = new Paint();
		paint.setColor(TrackColor);
		paint.setStrokeWidth(4);
		String absolutPath = "";
		if (file.equals(""))
		{
			absolutPath = trackPath;
		}
		else
		{
			absolutPath = trackPath + "/" + file;
		}
		RouteOverlay.Routes.add(RouteOverlay.LoadRoute(absolutPath, paint, Config.settings.TrackDistance.getValue()));
	}

	/*
	 * void MapView_MouseWheel(object sender, MouseEventArgs e) { if (e.Delta > 0) zoomIn(); else zoomOut(); }
	 */
	void OnTileLoaded(Bitmap bitmap, Descriptor desc)
	{
		// canvas = new Canvas(bitmap);
		// RouteOverlay.RenderRoute(canvas, bitmap, desc, , );
		/*
		 * if (nightMode) { unsafe { Rectangle bounds = new Rectangle(0, 0, bitmap.Size.Width, bitmap.Size.Height); BitmapData bitmapData =
		 * bitmap.LockBits(bounds, ImageLockMode.ReadWrite, PixelFormat.Format32bppRgb); byte* p = (byte*)bitmapData.Scan0.ToPointer(); for
		 * (int i = 0; i < bitmapData.Height * bitmapData.Stride; i++) { p[i] = (byte)(255 - p[i]); }; bitmap.UnlockBits(bitmapData); }; };
		 */

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		if (GlobalCore.autoResort) return;

		if (cache == null) return;
		/*
		 * if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint }); return; }
		 */
		positionInitialized = true;

		setLockPosition(0);
		Coordinate target = (waypoint != null) ? new Coordinate(waypoint.Latitude(), waypoint.Longitude()) : new Coordinate(
				cache.Latitude(), cache.Longitude());
		// CacheListe vor dem verschieben aktualisieren, damit während dem
		// Verschieben der neue SelectedCache schon markiert ist.
		updateCacheList();
		startAnimation(target);

	}

	/*
	 * delegate void targetChangedDelegate(Cache cache, Waypoint waypoint); void OnTargetChanged(Cache cache, Waypoint waypoint) { if
	 * (GlobalCore.autoResort) return; if (cache == null) return; if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged),
	 * new object[] { cache, waypoint }); return; } positionInitialized = true; // tabButtonTrackPosition.Down = false; Coordinate target =
	 * (waypoint != null) ? new Coordinate(waypoint.Latitude, waypoint.Longitude) : new Coordinate(cache.Latitude, cache.Longitude);
	 * startAnimation(target); } public new void Dispose() { if (loaderThread != null) loaderThread.Abort(); base.Dispose(); }
	 */
	public void OnShow()
	{
		// gcLogin = Config.settings.GcLogin").toLowerCase();
		isVisible = true;
		animationThread = new AnimationThread();
		if (!animationThread.isAlive()) animationThread.start();
		setNewSettings();
	}

	public void InitializeMap()
	{
		Zoom = Config.settings.lastZoomLevel.getValue();
		// gcLogin = Config.settings.GcLogin").toLowerCase();
		mapMaxCachesDisplay = 50;
		mapMaxCachesDisplayLarge = 100;
		zoomCross = 15;
		/*
		 * gcLogin = Config.settings.GcLogin"); mapMaxCachesDisplay = Config.settings.MapMaxCachesDisplay_config"); mapMaxCachesDisplayLarge
		 * = Config.settings.mapMaxCachesDisplayLarge_config");
		 */
		zoomCross = Config.settings.ZoomCross.getValue();

		// hideFindsButton.ButtonImage = (hideMyFinds) ? Global.Icons[6] :
		// Global.Icons[7];

		// Bestimmung der ersten Position auf der Karte
		if (!positionInitialized)
		{
			double lat = Config.settings.MapInitLatitude.getValue();
			double lon = Config.settings.MapInitLongitude.getValue();

			// Initialisierungskoordinaten bekannt und können übernommen werden
			if (lat != -1000 && lon != -1000)
			{
				setCenter(new Coordinate(lat, lon));
				positionInitialized = true;
				setLockPosition(0);
			}
			else
			{
				// GPS-Position bekannt?
				if (GlobalCore.LastValidPosition.Valid)
				{
					setCenter(new Coordinate(GlobalCore.LastValidPosition));
					positionInitialized = true;
				}
				else
				{
					try
					{
						if (Database.Data.Query != null)
						{
							if (Database.Data.Query.size() > 0)
							{
								// Koordinaten des ersten Caches der Datenbank
								// nehmen
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
						else
						{
							// Wenn Query == null
							setCenter(new Coordinate(48.0, 12.0));
						}
					}
					catch (Exception e)
					{
						setCenter(new Coordinate(48.0, 12.0));
						e.printStackTrace();
					}

				}
			}

			// Größe des Maßstabes berechnen etc...
			zoomChanged();
		}

		renderZoomScaleActive = false;

		minZoom = Config.settings.OsmMinLevel.getValue();
		maxZoom = Config.settings.OsmMaxLevel.getValue();

		// Skalierungsfaktoren bestimmen
		if (Config.settings.OsmDpiAwareRendering.getValue())
		{

			dpiScaleFactorX = dpiScaleFactorY = getContext().getResources().getDisplayMetrics().density;

		}
		else
		{
			dpiScaleFactorX = dpiScaleFactorY = 1;
		}
		((main) main.mainActivity).setDebugMsg("dpi=" + String.valueOf(dpiScaleFactorX));
		// redPen = new Pen(Color.Red, (int)( * 1.4f));

		// Falls DpiAwareRendering geändert wurde, müssen diese Werte ent-
		// sprechend angepasst werden.
		synchronized (screenCenter)
		{
			screenCenter.X = Math.round(centerOsmSpace.X);
			screenCenter.Y = Math.round(centerOsmSpace.Y);
			if (animationThread != null)
			{
				animationThread.toX = screenCenter.X;
				animationThread.toY = screenCenter.Y;
			}
		}

		/*
		 * halfIconSize = (int)((Global.NewMapIcons[2][0].Height * ) / 2);
		 */

		updateCacheList();
	}

	/*
	 * public void OnHide() { if (this.DesignMode) return; zoomScaleTimer.Enabled = false; if (offScreenBmp != null) { graphics.Dispose();
	 * offScreenBmp.Dispose(); } offScreenBmp = null; }
	 */
	/**
	 * Läd eine Kachel und legt sie in loadedTiles ab. Implementiert den WaitCallback-Delegaten <param name="state">Descriptor der zu
	 * ladenen Kachel. Typlos, damit man es als WorkItem queuen kann!</param>
	 */
	@SuppressWarnings("unchecked")
	protected void LoadTile(Object state)
	{
		// damit die Anzahl der loadedTiles wirklich nicht viel größer ist als
		// angegeben
		preemptTile();
		Descriptor desc = (Descriptor) state;

		Bitmap bitmap = Manager.LoadLocalBitmap(CurrentLayer, desc);
		/*
		 * Canvas canv = new Canvas(bitmap); RouteOverlay.RenderRoute(canv, bitmap, desc, , );
		 */

		/*
		 * if (bitmap != null) { // error while painting bitmaps with indexed format (png from Mapnik // -> create a copy of the bitmap if
		 * ((bitmap.PixelFormat == PixelFormat.Format1bppIndexed) || (bitmap.PixelFormat == PixelFormat.Format4bppIndexed) ||
		 * (bitmap.PixelFormat == PixelFormat.Format8bppIndexed) || (bitmap.PixelFormat == PixelFormat.Indexed)) bitmap = new
		 * Bitmap(bitmap); }
		 */
		Tile.TileState tileState = Tile.TileState.Disposed;

		if (bitmap == null)
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
					 * loaderThread = new Thread(new ThreadStart(loaderThreadEntryPoint)); loaderThread.Priority =
					 * ThreadPriority.BelowNormal; loaderThread.Start();
					 */
				}
			}
			finally
			{
				wishlistLock.unlock();
			}

			// Upscale coarser map tile
			bitmap = loadBestFit(CurrentLayer, desc, true);
			tileState = Tile.TileState.LowResolution;
		}
		else
			tileState = Tile.TileState.Present;

		if (bitmap == null) return;

		/*
		 * if (Config.settings.OsmDpiAwareRendering") && ( != 1 || != 1)) scaleUpBitmap(bitmap);
		 */

		addLoadedTile(desc, bitmap, tileState);

		/* if (OnTileLoaded != null) */
		// OnTileLoaded(bitmap, desc);

		// Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
		// kann man die Karte ja gut mal neu rendern!
		tilesFinished = true;

		if (tileVisible(desc)) Render(true);
	}

	/**
	 * Zeichnet eine Kachel mit den Tracks und legt sie in trackTiles ab. <param name="state">Descriptor der zu ladenen Kachel. Typlos,
	 * damit man es als WorkItem queuen kann!</param>
	 */
	protected void LoadTrackTile(Descriptor desc)
	{
		// damit die Anzahl der loadedTiles wirklich nicht viel größer ist als
		// angegeben
		preemptTrackTile();
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		Paint ppp = new Paint();
		ppp.setColor(Color.argb(255, 0, 0, 0));
		ppp.setStyle(Style.FILL);

		Canvas canv = new Canvas(bitmap);
		// canvas.drawRect(0, 0, 255, 255, ppp);
		RouteOverlay.RenderRoute(canv, bitmap, desc, 1, 1);

		Tile.TileState tileState = Tile.TileState.Disposed;

		tileState = Tile.TileState.Present;

		if (bitmap == null) return;

		addTrackTile(desc, bitmap, tileState);

		// Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist
		// kann man die Karte ja gut mal neu rendern!
		tilesFinished = true;

		if (tileVisible(desc)) Render(true);
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

			}
			while (available.Zoom >= 0 && (tile = Manager.LoadLocalBitmap(CurrentLayer, available)) == null);
		}
		else
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
				}
				finally
				{
					loadedTilesLock.unlock();
				}
			}
			while (available.Zoom >= 1 && (tile == null));
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
			int scale = (int) Math.pow(2, desc.Zoom - available.Zoom);
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

	Paint CachedBitmapPaitnt;

	private void scaleUpBitmap(Bitmap bitmap)
	{
		try
		{

			if (CachedBitmapPaitnt == null)
			{
				CachedBitmapPaitnt = new Paint();
				CachedBitmapPaitnt.setAntiAlias(true);
				CachedBitmapPaitnt.setFilterBitmap(true);
				CachedBitmapPaitnt.setDither(true);
			}

			Bitmap dummyBitmap = Bitmap.createBitmap((int) (256.0f), (int) (256.0f), Bitmap.Config.RGB_565);
			Canvas dummy = new Canvas(dummyBitmap);

			dummy.save();
			dummy.scale(1, 1);

			dummy.drawBitmap(bitmap, 0, 0, CachedBitmapPaitnt);
			dummy.restore();

			bitmap.recycle();
			bitmap = dummyBitmap;

		}
		catch (Exception e)
		{
		}
	}

	void addLoadedTile(Descriptor desc, Bitmap bitmap, Tile.TileState state)
	{
		loadedTilesLock.lock();
		try
		{
			if (loadedTiles.containsKey(desc.GetHashCode()))
			{
				// Wenn die Kachel schon geladen wurde und die neu zu
				// registrierende Kachel
				// weniger aktuell ist, behalten wir besser die alte!
				if (loadedTiles.get(desc.GetHashCode()).State == Tile.TileState.Present && state != Tile.TileState.Present) return;

				Tile tile = loadedTiles.get(desc.GetHashCode());
				if (tile.Image != null)
				{
					tile.Image.recycle();
					tile.Image = null;
				}

				tile.State = state; // (bitmap != null) ? Tile.TileState.Present
									// : Tile.TileState.Disposed;
				tile.Image = bitmap;
			}
			else
			{
				Tile tile = new Tile(desc, bitmap, state);
				loadedTiles.put(desc.GetHashCode(), tile);
			}
		}
		finally
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
				// Wenn die Kachel schon geladen wurde und die neu zu
				// registrierende Kachel
				// weniger aktuell ist, behalten wir besser die alte!
				if (trackTiles.get(desc.GetHashCode()).State == Tile.TileState.Present && state != Tile.TileState.Present) return;

				Tile tile = trackTiles.get(desc.GetHashCode());
				if (tile.Image != null)
				{
					tile.Image.recycle();
					tile.Image = null;
				}

				tile.State = state; // (bitmap != null) ? Tile.TileState.Present
									// : Tile.TileState.Disposed;
				tile.Image = bitmap;
			}
			else
			{
				Tile tile = new Tile(desc, bitmap, state);
				trackTiles.put(desc.GetHashCode(), tile);
			}
		}
		finally
		{
			trackTilesLock.unlock();
		}
	}

	private class loaderThread extends AsyncTask<ArrayList<Descriptor>, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(ArrayList<Descriptor>... params)
		{
			try
			{
				Descriptor desc = null;

				while (true)
				{
					wishlistLock.lock();
					try
					{
						if (wishlist.size() == 0) break;

						// Den raussuchen, der im Augenblick am meisten sichtbar
						// ist
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
								}
								finally
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
								p1.x += (int) (128);
								p1.y += (int) (128);

								dist = (p1.x - halfWidth) * (p1.x - halfWidth) + (p1.y - halfHeight) * (p1.y - halfHeight);
							}

							if (dist < minDist)
							{
								desc = candidate;
								minDist = dist;
							}
						}

						wishlist.remove(desc);
					}
					finally
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
								// Laden der Kachel fehlgeschlagen! Tile wieder
								// aus loadedTiles
								// entfernen
								loadedTilesLock.lock();
								try
								{
									if (loadedTiles.containsKey(desc.GetHashCode())) loadedTiles.remove(desc.GetHashCode());
								}
								finally
								{
									loadedTilesLock.unlock();
								}

								continue;
							}
							/*
							 * if (OnTileLoaded != null) OnTileLoaded(bitmap, desc);
							 */
							/*
							 * if (Config.settings.OsmDpiAwareRendering") && ( != 1 || != 1)) scaleUpBitmap(ref bitmap);
							 */

							addLoadedTile(desc, bitmap, Tile.TileState.Present);

							// Kachel erfolgreich geladen. Wenn die Kachel
							// sichtbar ist
							// kann man die Karte ja gut mal neu rendern!
							tilesFinished = true;

							if (tileVisible(desc)) Render(true);

						}
						else
						{
							// Kachel nicht geladen, noch ein Versuch!
							// lock (loadedTiles)
							// if (loadedTiles.ContainsKey(desc))
							// loadedTiles.Remove(desc);

							continue;
						}
						/*
						 * boolean LowMemory = Global.GetAvailableDiscSpace(Config .GetString("TileCacheFolder")) < ((long)1024 * 1024); if
						 * (LowMemory) { MessageBox.Show(
						 * "Device is running low on memory! Internet access will shut down now. Please free some memory e.g. by deleting unused tiles!"
						 * , "Warning!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1); #if DEBUG
						 * Global.AddLog( "MapView.loaderThreadEntryPoint: device is low on memory" ); #endif
						 * Config.settings.AllowInternetAccess", false); Config.AcceptChanges(); break; }
						 */
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
							}
							finally
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
				Logger.Error("MapView.doInBackground()", "", ex);
			}
			return null;
		}

		protected void onPostExecute(Integer result)
		{
			loaderThread = null;
			Render(true);
		}
	}

	/*
	 * void loaderThreadEntryPoint() { try { Descriptor desc = null; while (true) { lock (wishlist) { if (wishlist.Count == 0) break; // Den
	 * raussuchen, der im Augenblick am meisten sichtbar ist int minDist = int.MaxValue; // Alle beantragten Kacheln die nicht der //
	 * aktuellen Zoomstufe entsprechen, rausschmeissen for (int i = 0; i < wishlist.Count; i++) if (wishlist[i].Zoom != Zoom) {
	 * loadedTiles.Remove(wishlist[i]); wishlist.RemoveAt(i); i = -1; } foreach (Descriptor candidate in wishlist) { int dist =
	 * int.MaxValue; if (candidate.Zoom == Zoom) { Point p1 = ToScreen(candidate.X, candidate.Y, candidate.Zoom); p1.X += (int)(128 * );
	 * p1.Y += (int)(128 * ); dist = (p1.X - halfWidth) * (p1.X - halfWidth) + (p1.Y - halfHeight) * (p1.Y - halfHeight); } if (dist <
	 * minDist) { desc = candidate; minDist = dist; } } wishlist.Remove(desc); } try { if (Manager.CacheTile(CurrentLayer, desc)) { Bitmap
	 * bitmap = MapView.Manager.LoadLocalBitmap(CurrentLayer, desc); if (bitmap == null) { // Laden der Kachel fehlgeschlagen! Tile wieder
	 * aus loadedTiles // entfernen loadedTiles.Remove(desc); continue; } if (OnTileLoaded != null) OnTileLoaded(bitmap, desc); if
	 * (Config.settings.OsmDpiAwareRendering") && ( != 1 || != 1)) scaleUpBitmap(ref bitmap); addLoadedTile(desc, bitmap,
	 * Tile.TileState.Present); // Kachel erfolgreich geladen. Wenn die Kachel sichtbar ist // kann man die Karte ja gut mal neu rendern!
	 * tilesFinished = true; //if (tileVisible(loadedTiles[desc])) Render(true); } else { // Kachel nicht geladen, noch ein Versuch! //lock
	 * (loadedTiles) // if (loadedTiles.ContainsKey(desc)) // loadedTiles.Remove(desc); continue; } bool LowMemory =
	 * Global.GetAvailableDiscSpace(Config.settings.TileCacheFolder")) < ((long)1024 * 1024); if (LowMemory) { MessageBox.Show(
	 * "Device is running low on memory! Internet access will shut down now. Please free some memory e.g. by deleting unused tiles!" ,
	 * "Warning!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1); #if DEBUG
	 * Global.AddLog("MapView.loaderThreadEntryPoint: device is low on memory"); #endif Config.settings.AllowInternetAccess", false);
	 * Config.AcceptChanges(); break; } } catch (Exception exc) { // Fehler aufgetreten! Kachel nochmal laden! if (desc != null) lock
	 * (loadedTiles) if (loadedTiles.ContainsKey(desc)) loadedTiles.Remove(desc); #if DEBUG
	 * Global.AddLog("MapView.loaderThreadEntryPoint: exception caught: " + exc.ToString()); #endif } } lock (wishlist) loaderThread = null;
	 * } catch (ThreadAbortException) { } finally { loaderThread = null; Render(true); } }
	 */
	private Drawable getUnderlayIcon(Cache cache, Waypoint waypoint)
	{
		if (waypoint == null)
		{
			if ((cache == null) || (cache == GlobalCore.SelectedCache()))
			{
				if (cache.Archived || !cache.Available) return Global.NewMapOverlay.get(2).get(3);
				else
					return Global.NewMapOverlay.get(2).get(1);
			}
			else
			{
				if (cache.Archived || !cache.Available) return Global.NewMapOverlay.get(2).get(2);
				else
					return Global.NewMapOverlay.get(2).get(0);
			}
		}
		else
		{
			if (waypoint == GlobalCore.SelectedWaypoint())
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

	/**
	 * Sucht aus dem aktuellen Query die Caches raus, die dargestellt werden sollen und aktualisiert wpToRender entsprechend.
	 */
	public void updateCacheList()
	{
		if (Database.Data.Query == null) return;
		synchronized (screenCenter)
		{

			int iconSize = 0; // 8x8
			if ((Zoom >= 12) && (Zoom <= 13)) iconSize = 1; // 13x13
			else if (Zoom > 13) iconSize = 2; // default Images

			int xFrom = -halfIconSize - drawingWidth / 2;
			int yFrom = -halfIconSize - drawingHeight / 2;
			int xTo = drawingWidth + halfIconSize + drawingWidth / 2;
			int yTo = drawingHeight + halfIconSize + drawingWidth / 2;

			ArrayList<WaypointRenderInfo> result = new ArrayList<WaypointRenderInfo>();

			// Wegpunkte in Zeichenliste eintragen, unabhängig davon, wo
			// sie auf dem Bildschirm sind
			if (GlobalCore.SelectedCache() != null)
			{
				if (!(hideMyFinds && GlobalCore.SelectedCache().Found))
				{
					ArrayList<Waypoint> wps = GlobalCore.SelectedCache().waypoints;

					for (Waypoint wp : wps)
					{
						WaypointRenderInfo wpi = new WaypointRenderInfo();
						wpi.MapX = 256 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, wp.Pos.Longitude);
						wpi.MapY = 256 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, wp.Pos.Latitude);
						wpi.Icon = Global.NewMapIcons.get(2).get((int) wp.Type.ordinal());
						wpi.UnderlayIcon = Global.NewMapOverlay.get(2).get(0);
						wpi.Cache = GlobalCore.SelectedCache();
						wpi.Waypoint = wp;
						wpi.Selected = (GlobalCore.SelectedWaypoint() == wp);
						wpi.UnderlayIcon = getUnderlayIcon(wpi.Cache, wpi.Waypoint);

						int x = (int) (wpi.MapX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
						int y = (int) (wpi.MapY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;

						if ((x < xFrom || y < yFrom || x > xTo || y > yTo)) continue;

						result.add(wpi);
					}
				}
			}

			// Und Caches auch. Diese allerdings als zweites, da sie WPs
			// überzeichnen
			// sollen
			for (Cache cache : Database.Data.Query)
			{
				if (hideMyFinds && cache.Found) continue;

				int x = 0;
				int y = 0;
				try
				{
					x = (int) (cache.MapX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
					y = (int) (cache.MapY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;
				}
				finally
				{
				}

				if ((x < xFrom || y < yFrom || x > xTo || y > yTo) && cache != GlobalCore.SelectedCache()) continue;

				if ((hideCacheWithFinal) && (cache.Type == CacheTypes.Mystery) && cache.MysterySolved() && cache.HasFinalWaypoint())
				{
					// Wenn ein Mystery-Cache einen Final-Waypoint hat, hier die
					// Koordinaten des Caches nicht zeichnen.
					// Der Final-Waypoint wird später mit allen notwendigen
					// Informationen gezeichnet.
					// Die Koordinaten des Caches sind in den allermeisten
					// Fällen irrelevant.
					// Damit wird von einem gelösten Mystery nur noch eine
					// Koordinate in der Map gezeichnet, wenn der Cache nicht
					// Selected ist.
					// Sobald der Cache Selected ist, werden der Cache und alle
					// seine Waypoints gezeichnet.
					if (cache != GlobalCore.SelectedCache()) continue;
				}

				WaypointRenderInfo wpi = new WaypointRenderInfo();
				wpi.UnderlayIcon = null;
				wpi.OverlayIcon = null;
				wpi.MapX = cache.MapX;
				wpi.MapY = cache.MapY;
				wpi.Icon = (cache.ImTheOwner()) ? Global.NewMapIcons.get(2).get(20) : (cache.Found) ? Global.NewMapIcons.get(2).get(19)
						: (cache.MysterySolved() && (cache.Type == CacheTypes.Mystery)) ? Global.NewMapIcons.get(2).get(21)
								: Global.NewMapIcons.get(2).get((int) cache.Type.ordinal());
				wpi.Icon = Global.NewMapIcons.get(2).get(cache.GetMapIconId());
				wpi.UnderlayIcon = getUnderlayIcon(cache, wpi.Waypoint);

				if ((iconSize < 2) && (cache != GlobalCore.SelectedCache())) // der
																				// SelectedCache
																				// wird
																				// immer
																				// mit
																				// den
																				// großen
																				// Symbolen
																				// dargestellt!
				{
					int iconId = 0;
					wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(0); // rectangular
																					// shaddow
					if (cache.Archived || !cache.Available) wpi.OverlayIcon = Global.NewMapOverlay.get(iconSize).get(3);
					switch (cache.Type)
					{
					case Traditional:
						iconId = 0;
						break;
					case Letterbox:
						iconId = 0;
						break;
					case Multi:
						iconId = 1;
						break;
					case Event:
						iconId = 2;
						break;
					case MegaEvent:
						iconId = 2;
						break;
					case Virtual:
						iconId = 3;
						break;
					case Camera:
						iconId = 3;
						break;
					case Earth:
						iconId = 3;
						break;
					case Mystery:
					{
						if (cache.HasFinalWaypoint()) iconId = 5;
						else
							iconId = 4;
						break;
					}
					case Wherigo:
						iconId = 4;
						break;
					}
					if (cache.Found)
					{
						iconId = 6;
						wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(1); // round
																						// shaddow
					}
					if (cache.ImTheOwner())
					{
						iconId = 7;
						wpi.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(2); // star
																						// shaddow
					}
					wpi.Icon = Global.NewMapIcons.get(iconSize).get(iconId);
				}

				wpi.Cache = cache;
				wpi.Waypoint = null;
				wpi.Selected = (GlobalCore.SelectedCache() == cache);

				result.add(wpi);
			}

			// Final-Waypoints von Mysteries einzeichnen
			for (MysterySolution solution : Database.Data.Query.MysterySolutions)
			{
				// bei allen Caches ausser den Mysterys sollen die Finals nicht
				// gezeichnet werden, wenn der Zoom klein ist
				if ((Zoom < 14) && (solution.Cache.Type != CacheTypes.Mystery)) continue;

				if (GlobalCore.SelectedCache() == solution.Cache) continue; // is
																			// already
																			// in
																			// list

				if (hideMyFinds && solution.Cache.Found) continue;

				double mapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, solution.Longitude);
				double mapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, solution.Latitude);

				int x = 0;
				int y = 0;
				try
				{
					x = (int) (mapX * adjustmentCurrentToCacheZoom - screenCenter.X) + halfWidth;
					y = (int) (mapY * adjustmentCurrentToCacheZoom - screenCenter.Y) + halfHeight;
				}
				finally
				{
				}

				if ((x < xFrom || y < yFrom || x > xTo || y > yTo)) continue;

				WaypointRenderInfo wpiF = new WaypointRenderInfo();
				wpiF.MapX = mapX;
				wpiF.MapY = mapY;

				if (iconSize == 2)
				{
					wpiF.Icon = (solution.Cache.Type == CacheTypes.Mystery) ? Global.NewMapIcons.get(2).get(21) : Global.NewMapIcons.get(2)
							.get(18);
					wpiF.UnderlayIcon = getUnderlayIcon(solution.Cache, solution.Waypoint);
					if ((hideCacheWithFinal) && (solution.Cache.Type == CacheTypes.Mystery) && solution.Cache.MysterySolved()
							&& solution.Cache.HasFinalWaypoint())
					{
						if (GlobalCore.SelectedCache() != solution.Cache)
						{
							// die Icons aller geloesten Mysterys evtl. aendern,
							// wenn der Cache gefunden oder ein Eigener ist.
							// change the icon of solved mysterys if necessary
							// when the cache is found or own
							if (solution.Cache.Found) wpiF.Icon = Global.NewMapIcons.get(2).get(19);
							if (solution.Cache.ImTheOwner()) wpiF.Icon = Global.NewMapIcons.get(2).get(20);
						}
						else
						{
							// das Icon des geloesten Mysterys als Final
							// anzeigen, wenn dieser Selected ist
							// show the Icon of solved mysterys as final when
							// cache is selected
							wpiF.Icon = Global.NewMapIcons.get(2).get((int) solution.Waypoint.Type.ordinal());
						}
					}
				}
				else
				{
					int iconId = 0;
					wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(0); // rectangular
																					// shaddow
					if (solution.Cache.Archived || !solution.Cache.Available) wpiF.OverlayIcon = Global.NewMapOverlay.get(iconSize).get(3);
					switch (solution.Cache.Type)
					{
					case Traditional:
						iconId = 0;
						break;
					case Letterbox:
						iconId = 0;
						break;
					case Multi:
						iconId = 1;
						break;
					case Event:
						iconId = 2;
						break;
					case MegaEvent:
						iconId = 2;
						break;
					case Virtual:
						iconId = 3;
						break;
					case Camera:
						iconId = 3;
						break;
					case Earth:
						iconId = 3;
						break;
					case Mystery:
					{
						if (solution.Cache.HasFinalWaypoint()) iconId = 5;
						else
							iconId = 4;
						break;
					}
					case Wherigo:
						iconId = 4;
						break;
					}

					if (solution.Cache.Found)
					{
						iconId = 6;
						wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(1); // round
																						// shaddow
					}
					if (solution.Cache.ImTheOwner())
					{
						iconId = 7;
						wpiF.UnderlayIcon = Global.NewMapOverlay.get(iconSize).get(2); // start
																						// shaddow
					}
					wpiF.Icon = Global.NewMapIcons.get(iconSize).get(iconId);
				}
				wpiF.Cache = solution.Cache;
				wpiF.Waypoint = solution.Waypoint;
				wpiF.Selected = (GlobalCore.SelectedWaypoint() == solution.Waypoint);
				result.add(wpiF);
			}

			wpToRender = result;

		}
	}

	/*
	 * Pen boldRedPen = new Pen(Color.Red, 4); Pen boldOrangePen = new Pen(Color.Orange, 4); Pen largeGreenPen = new Pen(Color.Green, 6);
	 * ImageAttributes colorKey = new ImageAttributes(); ImageAttributes imageAttributes = null;
	 */
	void renderCaches()
	{

		boolean N = Config.settings.nightMode.getValue();

		int smallStarHeight = (int) ((double) Global.SmallStarIcons[1].getMinimumHeight());

		int bubbleX = 0;
		int bubbleY = 0;

		int halfUnderlayWidth = 0;
		for (WaypointRenderInfo wpi : wpToRender)
		{

			// Hide my finds
			if (hideMyFinds && wpi.Cache.Found) continue;

			int halfIconWidth = (int) ((wpi.Icon.getMinimumWidth()) / 2 * dpiScaleFactorX);
			int IconWidth = (int) (wpi.Icon.getMinimumWidth() * dpiScaleFactorX);
			int halfOverlayWidth = halfIconWidth;
			int OverlayWidth = IconWidth;
			if (wpi.OverlayIcon != null)
			{
				halfOverlayWidth = (int) ((wpi.OverlayIcon.getMinimumWidth()) / 2 * dpiScaleFactorX);
				OverlayWidth = (int) (wpi.OverlayIcon.getMinimumWidth() * dpiScaleFactorX);
			}
			halfUnderlayWidth = halfIconWidth;
			int UnderlayWidth = IconWidth;
			if (wpi.UnderlayIcon != null)
			{
				halfUnderlayWidth = (int) ((wpi.UnderlayIcon.getMinimumWidth()) / 2 * dpiScaleFactorX);
				UnderlayWidth = (int) (wpi.UnderlayIcon.getMinimumWidth() * dpiScaleFactorX);
			}

			int x = (int) ((wpi.MapX * adjustmentCurrentToCacheZoom - screenCenter.X)) + halfWidth;
			int y = (int) ((wpi.MapY * adjustmentCurrentToCacheZoom - screenCenter.Y)) + halfHeight;

			x = x - width / 2;
			y = y - halfHeight;
			x = (int) Math.round(x * multiTouchFaktor + width / 2);
			y = (int) Math.round(y * multiTouchFaktor + halfHeight);

			// drehen
			if (alignToCompass)
			{
				Point res = rotate(new Point(x, y), -canvasHeading);
				x = res.x;
				y = res.y;
			}

			int imageX = x;
			int imageY = y;

			if ((Zoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == GlobalCore.SelectedWaypoint()))
			{
				int size = (int) (halfIconWidth);

				float lineWidth = 2.0f;

				Paint CrossGreenPen = new Paint();
				CrossGreenPen.setColor(Color.GREEN);
				CrossGreenPen.setStrokeWidth(lineWidth);
				Paint CrossYellowPen = new Paint();
				CrossYellowPen.setColor(Color.YELLOW);
				CrossYellowPen.setStrokeWidth(lineWidth);
				Paint CrossMagentaPen = new Paint();
				CrossMagentaPen.setColor(Color.MAGENTA);
				CrossMagentaPen.setStrokeWidth(lineWidth);
				Paint CrossBluePen = new Paint();
				CrossBluePen.setColor(Color.BLUE);
				CrossBluePen.setStrokeWidth(lineWidth);
				Paint CrossRedPen = new Paint();
				CrossRedPen.setColor(Color.RED);
				CrossRedPen.setStrokeWidth(lineWidth);

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

			if (wpi.UnderlayIcon != null) drawImage(canvasOverlay, wpi.UnderlayIcon, imageX - halfUnderlayWidth,
					imageY - halfUnderlayWidth, UnderlayWidth, UnderlayWidth);
			drawImage(canvasOverlay, wpi.Icon, imageX - halfIconWidth, imageY - halfIconWidth, IconWidth, IconWidth);
			if (wpi.OverlayIcon != null) drawImage(canvasOverlay, wpi.OverlayIcon, imageX - halfOverlayWidth, imageY - halfOverlayWidth,
					OverlayWidth, OverlayWidth);

			if (wpi.Cache.Favorit())
			{
				ActivityUtils.PutImageTargetHeight(canvasOverlay, Global.Icons[19], imageX, imageY, (int) (14.0f));
			}

			boolean drawAsWaypoint = wpi.Waypoint != null;
			if ((GlobalCore.SelectedCache() != wpi.Cache) && hideCacheWithFinal && (wpi.Cache.Type == CacheTypes.Mystery))
			// Waypoints (=final) of not selected caches should be drawn like
			// the cache self because the cache is not drawn
			drawAsWaypoint = false;

			// Beschriftung
			if (showTitles && (Zoom >= 15))
			{
				int yoffset = 0;
				yoffset = (int) (fontSmall.getTextSize());

				((main) main.mainActivity).setDebugMsg("FontSize=" + String.valueOf(yoffset));

				String wpName; // draw Final Waypoint of not Selected Caches
								// like the caches self because the cache will
								// not be shown
				if (drawAsWaypoint)
				{ // Aktiver WP -> Titel oder GCCode
					wpName = (wpi.Waypoint.Title == "") ? wpi.Waypoint.GcCode : wpi.Waypoint.Title;
					fontSmall.setColor(N ? Color.BLACK : Color.WHITE); // Shadow
					canvasOverlay.drawText(wpName, x + halfIconWidth + 4, y, fontSmall);
					fontSmall.setColor(N ? Global.getInvertMatrixBlack() : Color.BLACK);
					canvasOverlay.drawText(wpName, x + halfIconWidth + 5, y + 1, fontSmall);
				}
				else
				{ // Aktiver Cache -> Cachename
					wpName = wpi.Cache.Name;
					if (showRating) yoffset += 10;
					int fwidth = (int) (fontSmall.measureText(wpName) / 2);
					fontSmall.setColor(N ? Color.BLACK : Color.WHITE); // Shadow
					canvasOverlay.drawText(wpName, x - fwidth, y + halfIconWidth + yoffset, fontSmall);
					fontSmall.setColor(N ? Global.getInvertMatrixBlack() : Color.BLACK);
					canvasOverlay.drawText(wpName, (x - fwidth) + 1, y + halfIconWidth + yoffset + 1, fontSmall);
				}
			}

			// Rating des Caches darstellen
			if (showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (Zoom >= 14))
			{
				Drawable img = Global.SmallStarIcons[(int) Math.min(wpi.Cache.Rating * 2, 5 * 2)];
				Rect bounds = img.getBounds();
				int halfSmallStarWidth = (int) (((double) img.getMinimumWidth() / 2.0) * dpiScaleFactorX);
				int smallStarWidth = (int) ((double) img.getMinimumWidth() * dpiScaleFactorX);
				img.setBounds(x - halfSmallStarWidth, y + halfUnderlayWidth + 2, x - halfSmallStarWidth + smallStarWidth, y
						+ halfUnderlayWidth + 2 + smallStarHeight);

				img.draw(canvasOverlay);

				img.setBounds(bounds);
			}

			// Show D/T-Rating
			if (showDT && (!drawAsWaypoint) && (Zoom >= 14))
			{
				Drawable imgDx = Global.SmallStarIcons[(int) Math.min(wpi.Cache.Difficulty * 2, 5 * 2)];
				Rect bounds = imgDx.getBounds();
				int smallStarHeightD = (int) ((double) imgDx.getMinimumWidth() * dpiScaleFactorX);
				imgDx.setBounds(x - halfUnderlayWidth, y - halfUnderlayWidth - smallStarHeight - 2, x - halfUnderlayWidth
						+ smallStarHeightD, y - halfUnderlayWidth - smallStarHeight - 2 + smallStarHeight);

				canvasOverlay.save();
				canvasOverlay.rotate(270, x, y);
				imgDx.draw(canvasOverlay);
				canvasOverlay.restore();
				imgDx.setBounds(bounds);

				imgDx = Global.SmallStarIcons[(int) Math.min(wpi.Cache.Terrain * 2, 5 * 2)];
				bounds = imgDx.getBounds();
				smallStarHeightD = (int) ((double) imgDx.getMinimumWidth() * dpiScaleFactorX);
				imgDx.setBounds(x - halfUnderlayWidth, y + halfUnderlayWidth + 2, x - halfUnderlayWidth + smallStarHeightD, y
						+ halfUnderlayWidth + 2 + smallStarHeight);
				canvasOverlay.save();
				canvasOverlay.rotate(270, x, y);
				imgDx.draw(canvasOverlay);
				canvasOverlay.restore();
				imgDx.setBounds(bounds);
				/*
				 * Bitmap imgTx = Global.SmallStarIcons[(int)Math.Min(wpi.Cache.Terrain * 2, 5 * 2)]; Bitmap imgT = new Bitmap(imgTx.Height,
				 * imgTx.Width); InternalRotateImage(270, imgTx, imgT); int halfSmallStarHeightT = (int)(((double)imgT.Height / 2.0) * );
				 * int smallStarHeightT = (int)((double)imgT.Height * ); graphics.DrawImage(imgT, new Rectangle(x + halfIconWidth + 4, y +
				 * halfIconWidth - smallStarHeightT, smallStarHeight, smallStarHeightT), 0, 0, imgT.Width, imgT.Height, GraphicsUnit.Pixel,
				 * imageAttributes);
				 */
			}

			if (showDirektLine && (wpi.Selected) && (wpi.Waypoint == GlobalCore.SelectedWaypoint()))
			{
				if (ArrowPaint == null)
				{
					ArrowPaint = new Paint();
				}

				ArrowPaint.setStyle(Style.FILL);
				ArrowPaint.setColor(Color.RED);
				ArrowPaint.setStrokeWidth(2);
				// canvas.drawLine(dirX, dirY, cacheArrowCenter.x,
				// cacheArrowCenter.y, ArrowPaint);
				canvas.drawLine(myPointOnScreen.x, myPointOnScreen.y, (float) x, (float) y, ArrowPaint);
			}

			if ((wpi.Cache.Id == BubbleCacheId) && (wpi.Waypoint == BubbleWaypoint) && isBubbleShow)
			{
				bubbleX = x;
				bubbleY = y;
			}

		}
		// Draw Bubble
		if (isBubbleShow && (BubbleCache != null))
		{

			float scale = 0.7f;
			int BubbleWidth = 430;
			int BubbleHeight = 140;
			BubbleDrawRec = new CB_Rect(bubbleX, bubbleY, bubbleX + BubbleWidth, bubbleY + BubbleHeight);
			BubbleDrawRec.offset(-((int) ((BubbleWidth / 2) * scale)), -((int) ((8 + halfUnderlayWidth + BubbleHeight) * scale)));
			Boolean GlobalSelected = BubbleCache == GlobalCore.SelectedCache();
			int BackgroundColor = GlobalSelected ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
			BackgroundColor = Color.argb(200, Color.red(BackgroundColor), Color.green(BackgroundColor), Color.blue(BackgroundColor));
			CacheDraw.DrawInfo(BubbleCache, canvasOverlay, BubbleDrawRec, BackgroundColor, CacheDraw.DrawStyle.withOwnerAndName, scale);

		}
	}

	void drawImage(Canvas aCanvas, Bitmap image, int x, int y, int width, int height)
	{
		drawImage(aCanvas, image, x, y, width, height, null);
	}

	void drawImage(Canvas aCanvas, Bitmap image, int x, int y, int width, int height, Paint p)
	{
		aCanvas.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new Rect(x, y, x + width, y + height), p);
		// graphics.DrawImage(image, new Rectangle(x, y, width, height), 0, 0,
		// image.Width, image.Height, GraphicsUnit.Pixel);
	}

	void drawImage(Canvas aCanvas, Drawable image, int x, int y, int width, int height)
	{
		image.setBounds(x, y, x + width, y + height);
		image.draw(aCanvas);
	}

	/**
	 * Überprüft, ob die übergebene Kachel im Darstellungsbereich des Controls liegt <param name="tile">Die zu prüfende Kachel</param>
	 * <returns>true, wenn die Kachel sichtbar ist, sonst false</returns>
	 */
	boolean tileVisible(Descriptor tile)
	{
		Point p1 = ToScreen(tile.X, tile.Y, tile.Zoom);
		Point p2 = ToScreen(tile.X + 1, tile.Y + 1, tile.Zoom);

		// relativ zu Zentrum
		// p1.x = p1.x - width / 2;
		// p1.y = p1.y - height / 2;
		// double x = (screenCenter.X - Math.sin(canvasHeading / 180 * Math.PI)
		// * (height / 2 - halfHeight)) / (256 );
		// double y = (screenCenter.Y + Math.cos(canvasHeading / 180 * Math.PI)
		// * (height / 2 - halfHeight)) / (256);

		float diff = halfHeight - height / 2;

		p1.x = p1.x - width / 2 - (int) (Math.sin(canvasHeading / 180 * Math.PI) * diff);
		p1.y = p1.y - height / 2 - (int) diff + (int) (Math.cos(canvasHeading / 180 * Math.PI) * diff);

		// skalieren
		p1.x = (int) Math.round(p1.x * multiTouchFaktor + drawingWidth / 2);
		p1.y = (int) Math.round(p1.y * multiTouchFaktor + drawingHeight / 2);
		// relativ zu Zentrum
		// p2.x = p2.x - width / 2;
		// p1.y = p2.y - height / 2 + (int)diff;
		// skalieren
		p2.x = p2.x - width / 2 - (int) (Math.sin(canvasHeading / 180 * Math.PI) * diff);
		p2.y = p2.y - height / 2 - (int) diff + (int) (Math.cos(canvasHeading / 180 * Math.PI) * diff);
		p2.x = (int) Math.round(p2.x * multiTouchFaktor + drawingWidth / 2);
		p2.y = (int) Math.round(p2.y * multiTouchFaktor + drawingHeight / 2);

		return (p1.x < drawingWidth && p2.x >= 0 && p1.y < drawingHeight && p2.y >= 0);
	}

	/**
	 * Lagert die am längsten nicht mehr verwendete Kachel aus
	 */
	protected void preemptTile()
	{
		// List<Tile> tiles = new List<Tile>();
		// tiles.AddRange(loadedTiles.Values);

		// Ist Auslagerung überhaupt nötig?
		if (numLoadedTiles() <= numMaxTiles) return;

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

				// Instanz freigeben und Eintrag löschen
				if (maxDesc != null)
				{
					try
					{
						Tile tile = loadedTiles.get(maxDesc.GetHashCode());
						loadedTiles.remove(maxDesc.GetHashCode());
						tile.destroy();
					}
					catch (Exception ex)
					{
						Logger.Error("MapView.preemptTile()", "", ex);
					}
				}
			}
			while (numLoadedTiles() > numMaxTiles);
		}
		finally
		{
			loadedTilesLock.unlock();
		}
	}

	// das ganze noch für die TrackTiles
	protected void preemptTrackTile()
	{
		// Ist Auslagerung überhaupt nötig?
		if (numTrackTiles() <= numMaxTrackTiles) return;

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

				// Instanz freigeben und Eintrag löschen
				if (maxDesc != null)
				{
					try
					{
						Tile tile = trackTiles.get(maxDesc.GetHashCode());
						tile.destroy();
						trackTiles.remove(maxDesc.GetHashCode());
					}
					catch (Exception ex)
					{
						Logger.Error("MapView.preemptTrackTile()", "", ex);
					}
				}
			}
			while (numTrackTiles() > numMaxTrackTiles);
		}
		finally
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

	/**
	 * If Night Mode
	 */
	boolean N = false;
	boolean overrideRepaintInteligence;

	public void Render(boolean overrideRepaintInteligence1)
	{
		if (canvas == null) return;
		if (ScreenLock.SliderMoves) return;

		// debugString1 = loadedTiles.size() + " / " + trackTiles.size() + " / "
		// + numLoadedTiles();
		// debugString2 = available_bytes * 1024 -
		// Debug.getNativeHeapAllocatedSize() / 1024 + " kB";
		N = Config.settings.nightMode.getValue();
		overrideRepaintInteligence = overrideRepaintInteligence1;

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
					/*
					 * if (InvokeRequired) { Invoke(new EmptyDelegate(Render), overrideRepaintInteligence); return; }
					 */

					// Wenn sich bei der Ansicht nichts getan hat braucht sie
					// auch nicht gerendert werden.
					if (!overrideRepaintInteligence)
					{
						if (lastRenderZoomScale == renderZoomScaleActive
								&& lastWpCount == wpToRender.size()
								&& lastHeading == ((GlobalCore.Locator != null) && (GlobalCore.Locator.getLocation() != null) ? GlobalCore.Locator
										.getHeading() : 0) && lastPosition.Latitude == GlobalCore.LastValidPosition.Latitude
								&& lastPosition.Longitude == GlobalCore.LastValidPosition.Longitude && lastZoom == Zoom && !tilesFinished
								&& lastRenderedPosition.X == screenCenter.X && lastRenderedPosition.Y == screenCenter.Y)
						{
							return;
						}

						lastRenderZoomScale = renderZoomScaleActive;
						lastWpCount = wpToRender.size();
						tilesFinished = false;
						lastPosition.Latitude = GlobalCore.LastValidPosition.Latitude;
						lastPosition.Longitude = GlobalCore.LastValidPosition.Longitude;
						lastHeading = 0;
						/*
						 * lastHeading = (Global.Locator != null) ? Global.Locator.Heading : 0;
						 */
						lastZoom = Zoom;
						lastRenderedPosition.X = screenCenter.X;
						lastRenderedPosition.Y = screenCenter.Y;
					}

					loadedTilesLock.lock();
					try
					{
						for (Tile tile : loadedTiles.values())
							tile.Age++;
					}
					finally
					{
						loadedTilesLock.unlock();
					}
					trackTilesLock.lock();
					try
					{
						for (Tile tile : trackTiles.values())
							tile.Age++;
					}
					finally
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
						ArrayList<KachelOrder> kOrder = new ArrayList<KachelOrder>();
						for (int x = xFrom; x <= xTo; x++)
						{
							for (int y = yFrom; y <= yTo; y++)
							{
								if (x < 0 || y < 0 || x >= Descriptor.TilesPerLine[Zoom] || y >= Descriptor.TilesPerColumn[Zoom]) continue;
								Descriptor desc = new Descriptor(x, y, Zoom);

								int dist = (int) Math.sqrt(Math.pow(screenCenter.X - (desc.X * 256 + 128), 2)
										+ Math.pow(screenCenter.Y - (desc.Y * 256 + 128), 2));
								kOrder.add(new KachelOrder(x, y, dist));
							}
						}
						Collections.sort(kOrder);
						// Kacheln beantragen
						for (KachelOrder ko : kOrder)
						{
							int x = ko.x;
							int y = ko.y;

							if (x < 0 || y < 0 || x >= Descriptor.TilesPerLine[Zoom] || y >= Descriptor.TilesPerColumn[Zoom]) continue;

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
							}
							finally
							{
								loadedTilesLock.unlock();
							}
							trackTilesLock.lock();
							try
							{
								if ((RouteOverlay.Routes.size() > 0) && (x >= xFromTrack) && (x <= xToTrack) && (y >= yFromTrack)
										&& (y <= yToTrack))
								{
									if (!trackTiles.containsKey(desc.GetHashCode()))
									{
										preemptTrackTile();

										trackTiles.put(desc.GetHashCode(), new Tile(desc, null, Tile.TileState.Disposed));

										queueTrackTile(desc);
									}
									trackTile = trackTiles.get(desc.GetHashCode());
								}
								else
									trackTile = null;
							}
							finally
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
					catch (Exception ex)
					{
						Logger.Error("MapView.Render()", "1", ex);
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

					if (renderZoomScaleActive) renderZoomScale();
					/*
					 * if (loaderThread != null) renderLoaderInfo();
					 */
					if (showCompass) renderCompass();

					try
					{
						Canvas can = holder.lockCanvas(null);
						if (can != null)
						{
							can.drawBitmap(offScreenBmp, 0, 0, null);
							/*
							 * if (!debugString1.equals("") || !debugString2.equals("")) { Paint debugPaint = new Paint();
							 * debugPaint.setTextSize(20); debugPaint.setColor(Color.WHITE); debugPaint.setStyle(Style.FILL);
							 * can.drawRect(new Rect(50, 70, 300, 130), debugPaint); debugPaint.setColor(Color.BLACK);
							 * can.drawText(debugString1, 50, 100, debugPaint); can.drawText(debugString2, 50, 130, debugPaint); }
							 */
							holder.unlockCanvasAndPost(can);
						}

						// this.CreateGraphics().DrawImage(offScreenBmp, 0, 0);
					}
					catch (Exception ex)
					{
						Logger.Error("MapView.Render()", "2", ex);
					}

				}
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.Render()", "3", exc);
			}
		}
		finally
		{
		}

		// nach dem vielem Rendern und Tile beantragen
		// können wir jetzt auch mal aufräumen
		System.gc();

	}

	private class KachelOrder implements Comparable<KachelOrder>
	{
		int x;
		int y;
		int dist;

		private KachelOrder(int x, int y, int dist)
		{
			this.x = x;
			this.y = y;
			this.dist = dist;
		}

		@Override
		public int compareTo(KachelOrder arg0)
		{
			return (dist < arg0.dist ? -1 : (dist == arg0.dist ? 0 : 1));
		}

	}

	/*
	 * Brush orangeBrush = new SolidBrush(Color.Orange);
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
		paint.setColor(Global.getColor(R.attr.Map_ColorCompassPanel));
		paint.setStyle(Style.FILL);
		canvasOverlay.drawRect(left, top, right, bottom + debugHeight, paint);

		// Position ist entweder GPS-Position oder die des Markers, wenn
		// dieser gesetzt wurde.
		Coordinate position = null;
		if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
		else if (GlobalCore.LastValidPosition != null) position = GlobalCore.LastValidPosition;
		else
			position = new Coordinate();

		// Koordinaten
		if (position.Valid)
		{
			String textLatitude = GlobalCore.FormatLatitudeDM(position.Latitude);
			String textLongitude = GlobalCore.FormatLongitudeDM(position.Longitude);

			paint = new Paint(font);
			paint.setTextAlign(Align.RIGHT);
			paint.setColor(Global.getColor(R.attr.Map_Compass_TextColor));
			canvasOverlay.drawText(textLatitude, right - 5, top + compassCenter - 10, paint);
			canvasOverlay.drawText(textLongitude, right - 5, bottom - 10, paint);

			if (GlobalCore.Locator != null)
			{
				paint.setTextAlign(Align.LEFT);
				canvasOverlay.drawText(GlobalCore.Locator.SpeedString(), leftString, top + compassCenter - 10, paint);
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
		if (GlobalCore.SelectedCache() != null && position.Valid)
		{
			// Distanz einzeichnen
			float distance = 0;

			if (GlobalCore.SelectedWaypoint() == null) distance = position.Distance(GlobalCore.SelectedCache().Pos);
			else
				distance = position.Distance(GlobalCore.SelectedWaypoint().Pos);

			String text = UnitFormatter.DistanceString(distance);
			canvas.drawText(text, leftString, bottom - 10, paint);

			// Kompassnadel zeichnen
			if (GlobalCore.Locator != null)
			{
				Coordinate cache = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos
						: GlobalCore.SelectedCache().Pos;
				double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude, cache.Longitude);
				double relativeBearing = bearing - GlobalCore.Locator.getHeading();
				double relativeBearingRad = relativeBearing * Math.PI / 180.0;

				int cs = canvas.save(123);
				int awidth = compassCenter * 2;
				int aheight = compassCenter * 2;
				Global.Arrows[1].setBounds(left, top, left + awidth, top + aheight);
				canvas.rotate((float) relativeBearing, (float) (left + compassCenter), (float) (top + compassCenter));
				Global.Arrows[1].draw(canvas);
				canvas.restoreToCount(cs);
				// Cachebox.Drawing.Arrow.FillArrow(graphics,
				// Cachebox.Drawing.Arrow.HeadingArrow, blackPen, orangeBrush,
				// left + compassCenter, top + compassCenter, compassCenter,
				// relativeBearingRad);
			}
		}

	}

	/*
	 * Global.BlendFunction blend = new Global.BlendFunction(100, 0); Bitmap blackPixelImage = new Bitmap(1, 1); bool alphaBlendingAvailable
	 * = true; Brush grayBrush = new SolidBrush(Color.DarkGray); private void transparentRectangle(int x, int y, int width, int height, byte
	 * alpha) { if (alphaBlendingAvailable) { try { blend.SourceConstantAlpha = alpha; Graphics graphicsPixel =
	 * Graphics.FromImage(blackPixelImage); IntPtr hdcPixel = graphicsPixel.GetHdc(); IntPtr hdcGraphics = graphics.GetHdc(); try {
	 * Global.AlphaBlend(hdcGraphics, x, y, width, height, hdcPixel, 0, 0, 1, 1, blend); } finally { graphics.ReleaseHdc(hdcGraphics);
	 * graphicsPixel.ReleaseHdc(hdcPixel); } graphicsPixel.Dispose(); return; } catch (Exception) { alphaBlendingAvailable = false; } }
	 * graphics.FillRectangle(grayBrush, x, y, width, height); }
	 */
	LinkedList<Descriptor> queuedTiles = new LinkedList<Descriptor>();
	private Lock queuedTilesLock = new ReentrantLock();
	LinkedList<Descriptor> queuedTrackTiles = new LinkedList<Descriptor>();
	private Lock queuedTrackTilesLock = new ReentrantLock();
	queueProcessor queueProcessor = null;

	/*
	 * Thread queueProcessor = null;
	 */
	// private Descriptor threadDesc;

	@SuppressWarnings("unchecked")
	private void queueTile(Descriptor desc)
	{
		// Alternative Implementierung mit Threadpools...
		// ThreadPool.QueueUserWorkItem(new WaitCallback(LoadTile), new
		// Descriptor(desc));

		queuedTilesLock.lock();
		try
		{
			if (queuedTiles.contains(desc.GetHashCode())) return;

			queuedTiles.add(desc);

			if (queueProcessor == null)
			{
				queueProcessor = new queueProcessor();
				queueProcessor.execute(queuedTiles);
			}
		}
		finally
		{
			queuedTilesLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void queueTrackTile(Descriptor desc)
	{
		// Alternative Implementierung mit Threadpools...
		// ThreadPool.QueueUserWorkItem(new WaitCallback(LoadTile), new
		// Descriptor(desc));

		queuedTrackTilesLock.lock();
		try
		{
			if (queuedTrackTiles.contains(desc.GetHashCode())) return;

			queuedTrackTiles.add(desc);

			if (queueProcessor == null)
			{
				queueProcessor = new queueProcessor();
				queueProcessor.execute(queuedTiles);
			}
		}
		finally
		{
			queuedTrackTilesLock.unlock();
		}
	}

	private class queueProcessor extends AsyncTask<LinkedList<Descriptor>, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(LinkedList<Descriptor>... params)
		{
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
							}
							finally
							{
								queuedTilesLock.unlock();
							}

							if (desc.Zoom == Zoom)
							{
								LoadTile(desc);
							}
							else
							{
								// Da das Image fur diesen Tile nicht geladen
								// wurde, da der Zoom-Faktor des Tiles nicht
								// gleich
								// dem aktuellen ist muss dieser Tile wieder aus
								// loadedTile entfernt werden, da sonst bei
								// spterem Wechsel des Zoom-Faktors dieses Tile
								// nicht angezeigt wird.
								// Dies passiert bei schnellem Wechsel des
								// Zoom-Faktors, wenn noch nicht alle aktuellen
								// Tiles geladen waren.
								loadedTilesLock.lock();
								try
								{
									if (loadedTiles.containsKey(desc.GetHashCode()))
									{
										Tile tile = loadedTiles.get(desc.GetHashCode());
										loadedTiles.remove(desc.GetHashCode());
										tile.destroy();
									}
								}
								finally
								{
									loadedTilesLock.unlock();
								}
							}
						}
						catch (Exception ex1)
						{
							Logger.Error("MapView.queueProcessor.doInBackground()", "1", ex1);
						}
					}
					else if (queuedTrackTiles.size() > 0)
					{
						try
						{
							// wenn keine Tiles mehr geladen werden müssen, dann
							// die TrackTiles erstellen
							desc = null;
							queuedTrackTilesLock.lock();
							try
							{
								desc = queuedTrackTiles.poll();
							}
							finally
							{
								queuedTrackTilesLock.unlock();
							}

							if (desc.Zoom == Zoom)
							{
								LoadTrackTile(desc);
							}
							else
							{
								// Da das Image fur diesen Tile nicht geladen
								// wurde, da der Zoom-Faktor des Tiles nicht
								// gleich
								// dem aktuellen ist muss dieser Tile wieder aus
								// loadedTile entfernt werden, da sonst bei
								// spterem Wechsel des Zoom-Faktors dieses Tile
								// nicht angezeigt wird.
								// Dies passiert bei schnellem Wechsel des
								// Zoom-Faktors, wenn noch nicht alle aktuellen
								// Tiles geladen waren.
								trackTilesLock.lock();
								try
								{
									if (trackTiles.containsKey(desc.GetHashCode()))
									{
										Tile tile = trackTiles.get(desc.GetHashCode());
										tile.destroy();
										trackTiles.remove(desc.GetHashCode());
									}
								}
								finally
								{
									trackTilesLock.unlock();
								}
							}
						}
						catch (Exception ex2)
						{
							Logger.Error("MapView.queueProcessor.doInBackground()", "2", ex2);
						}

					}
					queuedTilesLock.lock();
					try
					{
						queuedTrackTilesLock.lock();
						try
						{
							queueEmpty = (queuedTiles.size() < 1) && (queuedTrackTiles.size() < 1);
						}
						finally
						{
							queuedTrackTilesLock.unlock();
						}
					}
					finally
					{
						queuedTilesLock.unlock();
					}
				}
				while (!queueEmpty);
			}
			catch (Exception ex3)
			{
				Logger.Error("MapView.queueProcessor.doInBackground()", "3", ex3);
			}
			finally
			{
				// damit im Falle einer Exception der Thread neu gestartet wird
				// queueProcessor = null;
			}
			return null;
		}

		protected void onPostExecute(Integer result)
		{
			queueProcessor = null;
		}
	}

	/*
	 * void queueProcessorEntryPoint() { bool queueEmpty = false; try { do { Descriptor desc = null; lock (queuedTiles) desc =
	 * queuedTiles.Dequeue(); if (desc.Zoom == this.Zoom) LoadTile(desc); else { // Da das Image fur diesen Tile nicht geladen wurde, da der
	 * Zoom-Faktor des Tiles nicht gleich // dem aktuellen ist muss dieser Tile wieder aus loadedTile entfernt werden, da sonst bei //
	 * spterem Wechsel des Zoom-Faktors dieses Tile nicht angezeigt wird. // Dies passiert bei schnellem Wechsel des Zoom-Faktors, wenn noch
	 * nicht alle aktuellen Tiles geladen waren. if (loadedTiles.ContainsKey(desc)) loadedTiles.Remove(desc); } lock (queuedTiles)
	 * queueEmpty = queuedTiles.Count < 1; } while (!queueEmpty); } catch (Exception exc) { string forDebug = exc.Message; } finally { //
	 * damit im Falle einer Exception der Thread neu gestartet wird queueProcessor = null; } }
	 */
	Rect getTileRange(float rangeFactor)
	{
		synchronized (screenCenter)
		{
			int x1;
			int y1;
			int x2;
			int y2;
			double x = (screenCenter.X - Math.sin(canvasHeading / 180 * Math.PI) * (height / 2 - halfHeight)) / (256);
			double y = (screenCenter.Y + Math.cos(canvasHeading / 180 * Math.PI) * (height / 2 - halfHeight)) / (256);

			// preload more Tiles than necessary to ensure more smooth scrolling
			int dWidth = (int) (drawingWidth * rangeFactor);
			int dHeight = (int) (drawingHeight * rangeFactor);
			x1 = (int) Math.floor(x - dWidth / multiTouchFaktor / (256 * 2));
			x2 = (int) Math.floor(x + dWidth / multiTouchFaktor / (256 * 2));
			y1 = (int) Math.floor(y - dHeight / multiTouchFaktor / (256 * 2));
			y2 = (int) Math.floor(y + dHeight / multiTouchFaktor / (256 * 2));
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
			if (N)
			{
				drawImage(canvas, tile.Image, pt.x, pt.y, (int) (256.0f * multiTouchFaktor), (int) (256.0f * multiTouchFaktor),
						Global.invertPaint);
			}
			else
			{
				drawImage(canvas, tile.Image, pt.x, pt.y, (int) (256.0f * multiTouchFaktor), (int) (256.0f * multiTouchFaktor));
			}

			if (drawBestFit)
			{
				// Draw Kachel marker
				if (Config.settings.DebugShowMarker.getValue())
				{
					Paint paintt = new Paint(backBrush);
					paintt.setColor(Color.GREEN);
					paintt.setStyle(Style.STROKE);
					if (tile.State == Tile.TileState.LowResolution) paintt.setColor(Color.RED);
					Rect brect = new Rect(pt.x + 5, pt.y + 5, pt.x + (int) (256 * multiTouchFaktor) - 5, pt.y
							+ (int) (256 * multiTouchFaktor) - 5);
					canvas.drawRect(brect, paintt);
					canvas.drawLine(brect.left, brect.top, brect.right, brect.bottom, paintt);
					canvas.drawLine(brect.right, brect.top, brect.left, brect.bottom, paintt);
				}
			}
			return;
		}

		if (!drawBestFit) return;
		try
		{
			Bitmap bit = loadBestFit(CurrentLayer, tile.Descriptor, false);
			if (bit != null)
			{
				// skaliere letztes Tile solange bis das Tile mit richtigem Zoom
				// geladen ist.
				// um ohne Verzerrungen oder Lücken zu zoomen und scrollen
				tile.Image = bit;
				tile.State = Tile.TileState.LowResolution;

				if (Config.settings.nightMode.getValue())
				{
					drawImage(canvas, bit, pt.x, pt.y, (int) (256.0f * multiTouchFaktor), (int) (256.0f * multiTouchFaktor),
							Global.invertPaint);
				}
				else
				{
					drawImage(canvas, bit, pt.x, pt.y, (int) (256.0f * multiTouchFaktor), (int) (256.0f * multiTouchFaktor));
				}

			}
			else
				canvas.drawRect(pt.x, pt.y, pt.x + (int) (256 * multiTouchFaktor), pt.y + (int) (256 * multiTouchFaktor), backBrush);
			// .FillRectangle(backBrush, pt.X, pt.Y, (int)(256 * ), (int)(256 *
			// ));
			canvas.drawLine(pt.x, pt.y, pt.x + (int) (256 * multiTouchFaktor), pt.y + (int) (256 * multiTouchFaktor), fontSmall);
			canvas.drawLine(pt.x, pt.y + (int) (256 * multiTouchFaktor), pt.x + (int) (256 * multiTouchFaktor), pt.y, fontSmall);
		}
		catch (Exception ex)
		{
			Logger.Error("MapView.RenderTile", "", ex);
			canvas.drawRect(pt.x, pt.y, pt.x + (int) (256 * multiTouchFaktor), pt.y + (int) (256 * multiTouchFaktor), backBrush);
		}
	}

	Point ToScreen(double x, double y, int zoom)
	{
		synchronized (screenCenter)
		{
			double adjust = Math.pow(2, (Zoom - zoom));
			x = x * adjust * 256;
			y = y * adjust * 256;

			return new Point((int) (x - screenCenter.X) + halfWidth, (int) (y - screenCenter.Y) + halfHeight);
		}
	}

	Point ToScreen(double x, double y, double zoom)
	{
		synchronized (screenCenter)
		{

			double adjust = Math.pow(2, (Zoom - zoom));
			x = x * adjust * 256;
			y = y * adjust * 256;

			return new Point((int) (x - screenCenter.X) + halfWidth, (int) (y - screenCenter.Y) + halfHeight);
		}
	}

	/**
	 * an dieser x-Koordinate beginnt die Skala. Muss beim Resize neu gesetzt werden
	 */
	int scaleLeft;

	/**
	 * Breite des Maßstabs
	 */
	int scaleWidth;
	Bitmap offScreenBmp = null;
	/*
	 * private void MapView_Resize(object sender, EventArgs e) { halfHeight = Height / 2; halfWidth = Width / 2; width = Width; height =
	 * Height; scaleLeft = 0;// button2.Left + button2.Width + lineHeight; scaleWidth = width - scaleLeft -
	 * (int)this.CreateGraphics().MeasureString("100km ", Font).Width + 1; zoomChanged(); if (offScreenBmp != null) { graphics.Dispose();
	 * offScreenBmp.Dispose(); } offScreenBmp = new Bitmap(Math.Max(Width, 1), Math.Max(Height, 1)); graphics =
	 * Graphics.FromImage(offScreenBmp); }
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
		/* animationTimer.Enabled = false; */
		setLockPosition(0);

		lastClickX = dragStartX = eX;
		lastClickY = dragStartY = eY;

		// zurückdrehen, da eX und eY schon anhand der Map gedreht sind, die
		// Bubbles aber nicht!
		int ebX = eX;
		int ebY = eY;
		if (alignToCompass)
		{
			Point res = rotate(new Point(eX, eY), -canvasHeading);
			ebX = res.x;
			ebY = res.y;
		}
		arrowHitWhenDown = Math.sqrt(((ebX - cacheArrowCenter.x) * (ebX - cacheArrowCenter.x) + (ebY - cacheArrowCenter.y)
				* (ebY - cacheArrowCenter.y))) < (lineHeight * 1.5f);
	}

	private void MapView_MouseUp(int eX, int eY)
	{
		dragging = false;
		mouseMoved = false;
		updateCacheList();
		Render(true);
	}

	public Coordinate lastMouseCoordinate = null;

	private void MapView_MouseMove(int eX, int eY)
	{
		boolean doRender = false;
		try
		{
			PointD point = new PointD(0, 0);
			point.X = screenCenter.X + (eX - this.width / 2);
			point.Y = screenCenter.Y + (eY - this.halfHeight);
			;
			lastMouseCoordinate = new Coordinate(Descriptor.TileYToLatitude(Zoom, point.Y / (256.0)), Descriptor.TileXToLongitude(Zoom,
					point.X / (256.0)));

			if (dragging)
			{
				screenCenter.X += dragStartX - eX;
				screenCenter.Y += dragStartY - eY;
				animationLock.lock();
				try
				{
					animationThread.toX = screenCenter.X;
					animationThread.toY = screenCenter.Y;
				}
				finally
				{
					animationLock.unlock();
				}
				centerOsmSpace.X = screenCenter.X;
				centerOsmSpace.Y = screenCenter.Y;

				dragStartX = eX;
				dragStartY = eY;

				doRender = true;
			}
		}
		finally
		{
		}
		if (doRender) Render(false);

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
				animationThread.zoomTo(Zoom + 1);
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.zoomIn", "", exc);
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
					// updateCacheList();

					renderZoomScaleActive = true;
					if (doRender)
					{
						Render(false);
						startZoomScaleTimer();
					}
				}
				catch (Exception exc)
				{
					Logger.Error("MapView.zoomInDirect()", "", exc);
				}
			}
		}
	}

	private void startZoomScaleTimer()
	{
		if (zoomTimerTask != null) return;
		try
		{
			zoomTimerTask = new TimerTask()
			{
				@Override
				public void run()
				{
					renderZoomScaleActive = false;
					try
					{
						Render(false);
						zoomTimerTask = null;
					}
					catch (Exception exc)
					{
						Logger.Error("MapView.ZoomTimerTask", "", exc);
						return;
					}
				}
			};
			zoomScaleTimer = new Timer();
			zoomScaleTimer.schedule(zoomTimerTask, 1000);
		}
		catch (Exception exc)
		{
			Logger.Error("MapView.startZoomScaleTimer()", "", exc);
		}
	}

	/*
	 * Brush redBrush = new SolidBrush(Color.Red); Pen blackPen = new Pen(Color.Black); Pen bluePen = new Pen(Color.Magenta); Pen redPen =
	 * new Pen(Color.Red, 2); Pen goldenrodPen = new Pen(Color.Goldenrod, 2); Brush blueBrush = new SolidBrush(Color.Blue); Brush
	 * goldenrodBrush = new SolidBrush(Color.Goldenrod); Pen lightBluePen = new Pen(Color.LightBlue);
	 */

	Point myPointOnScreen = new Point();

	void renderPositionAndMarker()
	{

		if (GlobalCore.Locator != null)
		{
			// Position auf der Karte
			myPointOnScreen = ToScreen(Descriptor.LongitudeToTileX(Zoom, GlobalCore.LastValidPosition.Longitude),
					Descriptor.LatitudeToTileY(Zoom, GlobalCore.LastValidPosition.Latitude), Zoom);

			/*
			 * debugString1 = String.valueOf(Global.Locator.getCompassHeading()); if (Global.Locator.getLocation() != null) debugString2 =
			 * Global.Locator.getLocation().getBearing() + " - " + Global.Locator.getLocation().getSpeed() * 3600 / 1000 + "kmh"; else
			 * debugString2 = "";
			 */

			double courseRad = GlobalCore.Locator.getHeading() * Math.PI / 180.0;
			boolean lastUsedCompass = GlobalCore.Locator.LastUsedCompass;
			float dirX = (float) Math.sin(courseRad);
			float dirY = (float) -Math.cos(courseRad);

			Paint paint = new Paint();
			int MyColor;
			MyColor = Color.RED;
			if (lastUsedCompass) MyColor = Color.BLUE; // bei magnet. Kompass
			// first triangle
			long size = Math.round(1.8 * UiSizes.getArrowScaleMap());
			Path path = triaglePath(myPointOnScreen, size, dirX, dirY);
			paint.setColor(MyColor); // bei magnet. Kompass
			if (Config.settings.PositionAtVertex.getValue() || Config.settings.PositionMarkerTransparent.getValue())
			{
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(3);
				canvas.drawPath(path, paint);
				if (!Config.settings.PositionAtVertex.getValue()) canvas.drawCircle(myPointOnScreen.x, myPointOnScreen.y, 5, paint);
			}
			else
			{
				paint.setStyle(Style.FILL);
				canvas.drawPath(path, paint);
				// second triangle
				size = Math.round(1.4 * lineHeight);
				path = triaglePath(myPointOnScreen, size, dirX, dirY);
				paint.setColor(Color.WHITE); // bei magnet. Kompass
				paint.setStyle(Style.FILL);
				canvas.drawPath(path, paint);
				// third triangle, a little bit smaller
				size = lineHeight;
				path = triaglePath(myPointOnScreen, size, dirX, dirY);
				paint.setColor(MyColor); // bei magnet. Kompass
				paint.setStyle(Style.FILL);
				canvas.drawPath(path, paint);
			}

			if ((GlobalCore.Locator.getLocation() != null) && (GlobalCore.Locator.getLocation().hasAccuracy()))
			{
				float radius = GlobalCore.Locator.getLocation().getAccuracy();
				// debugString1 = String.valueOf(radius) + "m";
				Paint circlePaint = new Paint();
				circlePaint.setColor(Color.argb(55, 0, 0, 0));
				circlePaint.setStrokeWidth(5);
				canvas.drawCircle(myPointOnScreen.x, myPointOnScreen.y, (float) (pixelsPerMeter * radius), circlePaint);
			}
		}
		/*
		 * // Marker rendern if (Global.Marker.Valid) { Point pt = ToScreen(Descriptor.LongitudeToTileX(Zoom, Global.Marker.Longitude),
		 * Descriptor.LatitudeToTileY(Zoom, Global.Marker.Latitude), Zoom); int width = lineHeight / 3; graphics.FillEllipse(redBrush, pt.X
		 * - width, pt.Y - width, width + width, width + width); graphics.DrawEllipse(blackPen, pt.X - width, pt.Y - width, width + width,
		 * width + width); }
		 */
	}

	private Path triaglePath(Point pt, long size, float dirX, float dirY)
	{
		Point[] dir = new Point[3];
		dir[0] = new Point();
		dir[1] = new Point();
		dir[2] = new Point();
		float dx = dirX * size;
		float dy = dirY * size;
		if (Config.settings.PositionAtVertex.getValue())
		{
			dir[0].x = (int) (pt.x);
			dir[0].y = (int) (pt.y);

			dir[1].x = (int) (pt.x - dy / 4.0f - dx);
			dir[1].y = (int) (pt.y + dx / 4.0f - dy);

			dir[2].x = (int) (pt.x + dy / 4.0f - dx);
			dir[2].y = (int) (pt.y - dx / 4.0f - dy);
		}
		else
		{
			dir[0].x = (int) (pt.x + dx * 0.75f);
			dir[0].y = (int) (pt.y + dy * 0.75f);

			// x/y -> -y/x
			dir[1].x = (int) (pt.x - dy / 3.0f - dx * 0.25f);
			dir[1].y = (int) (pt.y + dx / 3.0f - dy * 0.25f);

			dir[2].x = (int) (pt.x + dy / 3.0f - dx * 0.25f);
			dir[2].y = (int) (pt.y - dx / 3.0f - dy * 0.25f);
		}

		// float[] verts = new float[6];
		// for (int i = 0; i < 3; i++)
		// {
		// verts[i * 2] = dir[i].x;
		// verts[i * 2 + 1] = dir[i].y;
		// }

		Path path = new Path();
		path.moveTo(dir[0].x, dir[0].y);
		path.lineTo(dir[1].x, dir[1].y);
		path.lineTo(dir[2].x, dir[2].y);
		path.lineTo(dir[0].x, dir[0].y);
		return path;
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
				animationThread.zoomTo(Zoom - 1);
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.zoomOut()", "", exc);
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
					// updateCacheList();
					renderZoomScaleActive = true;
					if (doRender)
					{
						Render(false);
						startZoomScaleTimer();
					}
				}
				catch (Exception exc)
				{
					Logger.Error("MapView.zoomOutDirect()", "", exc);
				}

			}
		}
	}

	double pixelsPerMeter;

	/**
	 * Anzahl der Schritte auf dem Maßstab
	 */
	int scaleUnits = 10;

	/**
	 * Länge des Maßstabs in Metern
	 */
	double scaleLength = 1000;

	/**
	 * Nachdem Zoom verändert wurde müssen einige Werte neu berechnet werden
	 */
	private void zoomChanged()
	{
		try
		{
			int[] scaleNumUnits = new int[]
				{ 4, 3, 4, 3, 4, 5, 3 };
			float[] scaleSteps = new float[]
				{ 1, 1.5f, 2, 3, 4, 5, 7.5f };
			/*
			 * if (animationTimer != null) animationTimer.Enabled = false;
			 */
			adjustmentCurrentToCacheZoom = Math.pow(2, Zoom - Cache.MapZoomLevel);

			// Infos für den Maßstab neu berechnen
			Coordinate dummy = Coordinate.Project(center.Latitude, center.Longitude, 90, 1000);
			double l1 = Descriptor.LongitudeToTileX(Zoom, center.Longitude);
			double l2 = Descriptor.LongitudeToTileX(Zoom, dummy.Longitude);
			double diff = Math.abs(l2 - l1);
			pixelsPerMeter = (diff * 256 * multiTouchFaktor) / 1000;

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
		catch (Exception exc)
		{
			Logger.Error("MapView.zoomChanged()", "", exc);
		}
	}

	/*
	 * Brush[] brushes = new Brush[] { new SolidBrush(Color.Black), new SolidBrush(Color.White) };
	 */
	Paint font = new Paint();
	Paint fontSmall = new Paint();

	/**
	 * Zeichnet den Maßstab. pixelsPerKm muss durch zoomChanged initialisiert sein! und graphics auch!
	 */
	private void renderScale()
	{
		int pos = 0;
		int start = 0;
		Paint[] brushes = new Paint[2];
		brushes[0] = new Paint();
		brushes[0].setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		brushes[0].setStyle(Style.FILL);
		brushes[1] = new Paint();
		brushes[1].setColor(main.N ? Global.getInvertMatrixWhite() : Color.WHITE);
		brushes[1].setStyle(Style.FILL);

		for (int i = 1; i <= scaleUnits; i++)
		{
			pos = (int) (scaleLength * ((double) i / scaleUnits) * pixelsPerMeter);

			canvasOverlay.drawRect(new Rect(start + scaleLeft, height - lineHeight / 2 - lineHeight / 4, pos + scaleLeft, height
					- lineHeight / 4), brushes[i % 2]);
			start = pos;
		}

		Paint blackPen = new Paint();
		blackPen.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		blackPen.setStyle(Style.STROKE);
		canvasOverlay.drawRect(new Rect(scaleLeft - 1, height - lineHeight / 2 - lineHeight / 4, scaleLeft + pos, height - lineHeight / 4),
				blackPen);

		String distanceString;

		if (UnitFormatter.ImperialUnits)
		{
			// distanceString = String.format("{0:0.00}mi", scaleLength /
			// 1609.3);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			distanceString = nf.format(scaleLength / 1609.3) + "mi";
		}
		else if (scaleLength <= 500)
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
		// graphics.DrawString(distanceString, font, brushes[0], scaleLeft + pos
		// + lineHeight / 2, height - lineHeight);
	}

	/**
	 * true when a dobble click on showing bubble
	 */
	private Boolean isSelected = false;

	/**
	 * set true to show Bubble from Cache with BubleCacheId
	 */
	private boolean isBubbleShow;

	/**
	 * is true when click on showing bubble
	 */
	private Boolean isBubbleClick;

	/**
	 * CacheID of the Cache showing Bubble
	 */
	private long BubbleCacheId = -1;

	/**
	 * Cache showing Bubble
	 */
	private Cache BubbleCache = null;
	private Waypoint BubbleWaypoint = null;

	/**
	 * Rectangle to Draw Bubble or detect click inside
	 */
	private CB_Rect BubbleDrawRec;

	public void showBubleSelected()
	{
		BubbleCacheId = GlobalCore.SelectedCache().Id;
		BubbleCache = GlobalCore.SelectedCache();
		isBubbleShow = true;
	}

	private void MapView_Click(int eX, int eY)
	{

		// zurückdrehen, da eX und eY schon anhand der Map gedreht sind, die
		// Bubbles aber nicht!
		int ebX = eX;
		int ebY = eY;
		if (alignToCompass)
		{
			Point res = rotate(new Point(eX, eY), -canvasHeading);
			ebX = res.x;
			ebY = res.y;
		}
		if ((BubbleCache != null) && isBubbleShow && BubbleDrawRec != null && BubbleDrawRec.contains(ebX, ebY)) // Bubble
																												// gedrückt
		{
			// Click inside Bubble -> hide Bubble and select Cache
			GlobalCore.SelectedWaypoint(BubbleCache, BubbleWaypoint);
			CacheDraw.ReleaseCacheBMP();
			isBubbleShow = false;
			BubbleCacheId = -1;
			BubbleCache = null;
			BubbleWaypoint = null;
			// Shutdown Autoresort
			GlobalCore.autoResort = false;
			Render(true);
			// do nothing else with this click
			return;
		}
		else if (isBubbleShow)
		{
			// Click outside Bubble -> hide Bubble
			isBubbleShow = false;
			Render(true);
		}

		if (arrowHitWhenDown
				&& Math.sqrt(((ebX - cacheArrowCenter.x) * (ebX - cacheArrowCenter.x) + (ebY - cacheArrowCenter.y)
						* (ebY - cacheArrowCenter.y))) < (lineHeight * 1.5f))
		{
			Coordinate target = (GlobalCore.SelectedWaypoint() != null) ? new Coordinate(GlobalCore.SelectedWaypoint().Latitude(),
					GlobalCore.SelectedWaypoint().Longitude()) : new Coordinate(GlobalCore.SelectedCache().Latitude(), GlobalCore
					.SelectedCache().Longitude());

			startAnimation(target);
			cacheArrowCenter.x = Integer.MIN_VALUE;
			cacheArrowCenter.y = Integer.MIN_VALUE;
			arrowHitWhenDown = false;
			return;
		}

		WaypointRenderInfo minWpi = new WaypointRenderInfo();
		minWpi.Cache = null;

		int minDist = Integer.MAX_VALUE;
		// Überprüfen, auf welchen Cache geklickt wurde
		for (int i = wpToRender.size() - 1; i >= 0; i--)
		{
			WaypointRenderInfo wpi = wpToRender.get(i);
			if (wpi.Cache == null) continue;
			int x = (int) ((wpi.MapX * adjustmentCurrentToCacheZoom - screenCenter.X)) + halfWidth;
			int y = (int) ((wpi.MapY * adjustmentCurrentToCacheZoom - screenCenter.Y)) + halfHeight;

			// Difference between Icon Center and Klick Position
			int xd = lastClickX - x;
			int yd = lastClickY - y;
			// Distance in Screen Koords
			int dist = xd * xd + yd * yd;

			x = x - width / 2;
			y = y - halfHeight;
			x = (int) Math.round(x * multiTouchFaktor + width / 2);
			y = (int) Math.round(y * multiTouchFaktor + halfHeight);

			int ClickToleranz = (int) (30 * dpiScaleFactorX);
			Rect HitTestRec = new Rect(x - ClickToleranz, y - ClickToleranz, x + ClickToleranz, y + ClickToleranz);

			if (HitTestRec.contains(lastClickX, lastClickY))
			{
				if (dist < minDist)
				{
					minDist = dist;
					minWpi = wpi;
				}
			}

		}

		if (minWpi.Cache == null) return;

		if (minWpi.Waypoint != null)
		{
			if (GlobalCore.SelectedCache() != minWpi.Cache)
			{
				// Show Bubble
				isBubbleShow = true;
				BubbleCacheId = minWpi.Cache.Id;
				BubbleCache = minWpi.Cache;
				BubbleWaypoint = minWpi.Waypoint;

				Render(true);

			}
			else
			{
				// do not show Bubble because there will not be selected a
				// different cache but only a different waypoint
				// Wegpunktliste ausrichten
				GlobalCore.SelectedWaypoint(minWpi.Cache, minWpi.Waypoint);
				// FormMain.WaypointListPanel.AlignSelected();
				// updateCacheList();
				Render(true);
			}

		}
		else
		{
			if (GlobalCore.SelectedCache() != minWpi.Cache)
			{
				isBubbleShow = true;
				BubbleCacheId = minWpi.Cache.Id;
				BubbleCache = minWpi.Cache;

				Render(true);
			}
			else
			{
				isBubbleShow = true;
				BubbleCacheId = minWpi.Cache.Id;
				BubbleCache = minWpi.Cache;
				// Cacheliste ausrichten
				GlobalCore.SelectedCache(minWpi.Cache);
				// updateCacheList();
				Render(true);
			}
		}

		// this.Focus();
	}

	/*
	 * private void button3_Click(object sender, EventArgs e) { if (Global.Marker.Valid) { // tabButtonTrackPosition.Down = true;
	 * startAnimation(new Coordinate(Global.Marker.Latitude, Global.Marker.Longitude)); return; } if (Global.LastValidPosition != null &&
	 * Global.LastValidPosition.Valid) { // tabButtonTrackPosition.Down = true; startAnimation(Global.LastValidPosition); return; } }
	 */
	/**
	 * Ausgangspunkt der Animation
	 */
	PointD animateFrom = new PointD(0, 0);

	/**
	 * Zielpunkt der Animation
	 */
	PointD animateTo = new PointD(0, 0);

	/*
	 * // Zeitpunkt des Startes der Animation long animationStart; /// /// Dauer der Animation in ms /// const int animationDuration = 500;
	 */
	void startAnimation(Coordinate target)
	{
		if (animationThread == null)
		{
			// Screencenter direkt auf die gegebenen Koordinaten setzen
			animateTo.X = 256 * Descriptor.LongitudeToTileX(Zoom, target.Longitude);
			animateTo.Y = 256 * Descriptor.LatitudeToTileY(Zoom, target.Latitude);
			screenCenter.X = animateTo.X;
			screenCenter.Y = animateTo.Y;
			centerOsmSpace.X = screenCenter.X;
			centerOsmSpace.Y = screenCenter.Y;
			updateCacheList();
			return;
		}
		animationThread.moveTo(target);

		// animationStart = Environment.TickCount;
		/*
		 * animateFrom.X = screenCenter.X; animateFrom.Y = screenCenter.Y; animateTo.X = * 256 * Descriptor.LongitudeToTileX(Zoom,
		 * target.Longitude); animateTo.Y = * 256 * Descriptor.LatitudeToTileY(Zoom, target.Latitude); screenCenter.X = animateTo.X;
		 * screenCenter.Y = animateTo.Y; centerOsmSpace.X = screenCenter.X / ; centerOsmSpace.Y = screenCenter.Y / ; updateCacheList();
		 * Render(false);
		 */
		/*
		 * double xDiff = animateFrom.X - animateTo.X; double yDiff = animateFrom.Y - animateTo.Y; center = target; if (Math.Sqrt(xDiff *
		 * xDiff + yDiff * yDiff) < 2 * 256 * ) animationTimer.Enabled = true; else { // Zu weit! Wir gehen ohne Animation direkt zum Ziel!
		 * screenCenter.X = animateTo.X; screenCenter.Y = animateTo.Y; centerOsmSpace.X = screenCenter.X / ; centerOsmSpace.Y =
		 * screenCenter.Y / ; updateCacheList(); Render(false); }
		 */
	}

	/*
	 * private void animationTimer_Tick(object sender, EventArgs e) { double scale = Math.Min(1.0, ((double)Environment.TickCount -
	 * (double)animationStart) / (double)animationDuration); double x = animateFrom.X + (animateTo.X - animateFrom.X) * scale; double y =
	 * animateFrom.Y + (animateTo.Y - animateFrom.Y) * scale; screenCenter.X = Math.Round(x); screenCenter.Y = Math.Round(y);
	 * centerOsmSpace.X = screenCenter.X / ; centerOsmSpace.Y = screenCenter.Y / ; if (scale == 1.0) { animationTimer.Enabled = false;
	 * updateCacheList(); } Render(false); } Brush blackBrush = new SolidBrush(Color.Black); Brush whiteBrush = new SolidBrush(Color.White);
	 * Brush RedBrush = new SolidBrush(Color.Red);
	 */
	boolean renderZoomScaleActive = false;

	private void renderZoomScale()
	{
		// int topRow = bZoomIn.Top + bZoomIn.Height + bZoomIn.Height / 2;
		// int bottomRow = bZoomOut.Top - bZoomOut.Height / 2;
		// int centerColumn = bZoomIn.Left + bZoomIn.Width / 2;
		// int halfWidth = (bZoomIn.Width / 5);
		int topRow = buttonTrackPosition.getTop() + buttonTrackPosition.getHeight() + 10;
		int bottomRow = height - 50;
		int centerColumn = 50;
		int halfWidth = 20;
		float dist = 20;

		Paint paint = new Paint();
		paint.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		canvasOverlay.drawLine(centerColumn, topRow, centerColumn, bottomRow, paint);

		float numSteps = maxZoom - minZoom;
		for (int i = minZoom; i <= maxZoom; i++)
		{
			int y = (int) ((1 - ((float) (i - minZoom)) / numSteps) * (bottomRow - topRow)) + topRow;
			dist = (bottomRow - topRow) / numSteps;

			Paint font = new Paint();
			font.setTextSize(UiSizes.getScaledFontSize_big());
			font.setFakeBoldText(true);
			font.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
			Paint white = new Paint();
			white.setColor(main.N ? Global.getInvertMatrixWhite() : Color.WHITE);
			white.setStyle(Style.FILL);
			Paint black = new Paint();
			black.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
			black.setStyle(Style.STROKE);

			if (i == Zoom)
			{
				y += dist * (1 - multiTouchFaktor) * 1.5;
				String label = String.valueOf(Zoom);
				int textWidth = (int) font.measureText(label);
				int textHeight = 28;
				Rect bounds = new Rect();
				font.getTextBounds(label, 0, label.length(), bounds);
				textWidth = bounds.width();
				textHeight = (int) (bounds.height() * 1.5);
				canvasOverlay.drawRect(new Rect(centerColumn - textWidth / 2 - lineHeight / 2, y - textHeight / 2, centerColumn - textWidth
						/ 2 - lineHeight / 2 + textWidth + lineHeight, y - textHeight / 2 + textHeight), white);
				canvasOverlay.drawRect(new Rect(centerColumn - textWidth / 2 - 1 - lineHeight / 2, y - textHeight / 2 - 1, centerColumn
						- textWidth / 2 - 1 - lineHeight / 2 + textWidth + lineHeight + 1, y - textHeight / 2 - 1 + textHeight + 1), black);
				canvasOverlay.drawText(label, centerColumn - textWidth / 2, y + textHeight / 2, font);
			}
			else
				canvasOverlay.drawLine(centerColumn - halfWidth, y, centerColumn + halfWidth, y, black);
		}
	}

	/*
	 * private void zoomScaleTimer_Tick(object sender, EventArgs e) { zoomScaleTimer.Enabled = false; renderZoomScaleActive = false;
	 * Render(false); } void renderLoaderInfo() { int tilesToLoad; lock (wishlist) tilesToLoad = wishlist.Count + 1; String info =
	 * (tilesToLoad == 1) ? "1 Tile, " : tilesToLoad.ToString() + " Tiles, "; info += Global.GetLengthString(Global.TransferredBytes);
	 * Global.PutImageTargetHeight(graphics, Global.Icons[3], scaleLeft, height - lineHeight * 2, lineHeight); graphics.DrawString(info,
	 * font, blackBrush, scaleLeft + lineHeight + lineHeight / 3, height - lineHeight * 2); } private void button3_Click_1(object sender,
	 * EventArgs e) { mapMenu.Show(this, button3.Left + button3.Width, button3.Top, 0); } private void tsiRemoveCenter_Click(object sender,
	 * EventArgs e) { if (MessageBox.Show("Really remove marker?", "Remove marker", MessageBoxButtons.YesNo, MessageBoxIcon.Question,
	 * MessageBoxDefaultButton.Button1) == DialogResult.Yes) { Global.Marker.Valid = false; tsiRemoveCenter.Enabled = false; Render(true);
	 * CacheListView.View.RefreshDistances(); CacheListView.View.Refresh(); WaypointView.View.Refresh(); } } private void
	 * tsiSetCenter_Click(object sender, EventArgs e) { if (lastMouseCoordinate != null) Global.SetMarker(lastMouseCoordinate); else
	 * Global.SetMarker(Center); Global.LastValidPosition = Global.Marker; //markerWaypoint.Latitude = Global.Marker.Latitude;
	 * //markerWaypoint.Longitude = Global.Marker.Longitude; tsiRemoveCenter.Enabled = true; // Render(true); } private void
	 * hideFinds(ClickButton sender) { hideMyFinds = !hideMyFinds; hideFindsButton.ButtonImage = (hideMyFinds) ? Global.Icons[6] :
	 * Global.Icons[7]; Config.settings.MapHideMyFinds", hideMyFinds); Config.AcceptChanges(); updateCacheList(); Render(false); } void
	 * showRatingChanged(ClickButton sender) { showRating = !showRating; showRatingButton.ButtonImage = (showRating) ? Global.Icons[6] :
	 * Global.Icons[7]; Config.settings.MapShowRating", showRating); Config.AcceptChanges(); Render(false); } void showDTChanged(ClickButton
	 * sender) { showDT = !showDT; showDTButton.ButtonImage = (showDT) ? Global.Icons[6] : Global.Icons[7]; Config.settings.MapShowDT",
	 * showDT); Config.AcceptChanges(); Render(false); } void showCompassChanged(ClickButton sender) { showCompass = !showCompass;
	 * showCompassButton.ButtonImage = (showCompass) ? Global.Icons[6] : Global.Icons[7]; Config.settings.MapShowCompass", showCompass);
	 * Config.AcceptChanges(); Render(false); } void showTitlesChanged(ClickButton sender) { showTitles = !showTitles;
	 * showTitlesButton.ButtonImage = (showTitles) ? Global.Icons[6] : Global.Icons[7]; Config.settings.MapShowTitles", showTitles);
	 * Config.AcceptChanges(); Render(false); } void showMarkerMenu(ClickButton sender) { markerMenu.Show(this, button3.Left +
	 * button3.Width, button3.Top, 0); } void showRouteMenu(ClickButton sender) { routeMenu.Show(this, button3.Left + button3.Width,
	 * button3.Top, 0); } void showViewMenu(ClickButton sender) { viewMenu.Show(this, button3.Left + button3.Width, button3.Top, 0); } void
	 * showNavigationDialog(ClickButton sender) { if (Global.SelectedCache == null) {
	 * MessageBox.Show("Please select a waypoint and try again!", "Route generation failed!"); return; } if ((Global.Locator == null ||
	 * !Global.Locator.LastValidPosition.Valid) && !Global.Marker.Valid) { MessageBox.Show(
	 * "Routes are generated between your current position and the selected waypoint. Please wait for a GPS fix or set the marker!" ,
	 * "Route generation failed!"); return; } if (!Config.settings.AllowRouteInternet")) { MessageBox.Show(
	 * "Querying the Routing Service requires an internet connection! Please enable Route Calculation to continue!" ,
	 * "Route Calculation disabled!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1); return; }
	 * Cachebox.Views.Forms.NavigationForm navigationForm = new Cachebox.Views.Forms.NavigationForm(); if (navigationForm.ShowDialog() ==
	 * DialogResult.OK) { Cursor.Current = Cursors.WaitCursor; HttpWebRequest webRequest = null; HttpWebResponse webResponse = null; try {
	 * webRequest = ( HttpWebRequest)WebRequest.Create(Config.settings.NavigationProvider")); webRequest.UserAgent = "cachebox rev " +
	 * Global.CurrentRevision.ToString() + ((Global.RevisionSuffix.Length > 0) ? "(" + Global.RevisionSuffix + ")" : ""); webRequest.Timeout
	 * = 17000; webRequest.Method = "POST"; webRequest.ContentType = "application/x-www-form-urlencoded"; webRequest.Proxy = Global.Proxy;
	 * Coordinate start = (Global.Marker.Valid) ? new Coordinate(Global.Marker.Latitude, Global.Marker.Longitude) :
	 * Global.Locator.LastValidPosition; Coordinate end = (Global.SelectedWaypoint != null) ? new
	 * Coordinate(Global.SelectedWaypoint.Latitude, Global.SelectedWaypoint.Longitude) : new Coordinate(Global.SelectedCache.Latitude,
	 * Global.SelectedCache.Longitude); bool motorways = navigationForm.radioButtonCar.Checked; String pref = "Fastest"; if
	 * (navigationForm.radioButtonPedestrian.Checked) pref = "Pedestrian"; if (navigationForm.radioButtonBike.Checked) pref = "Bicycle";
	 * String parameters = "Start=" + start.Longitude.ToString(NumberFormatInfo.InvariantInfo) + "," +
	 * start.Latitude.ToString(NumberFormatInfo.InvariantInfo) + "&End=" + end.Longitude.ToString(NumberFormatInfo.InvariantInfo) + "," +
	 * end.Latitude.ToString(NumberFormatInfo.InvariantInfo) + "&Via=&lang=de&distunit=KM&routepref=" + pref + "&avoidAreas=&useTMC=" +
	 * (navigationForm.checkBoxTMC.Checked && navigationForm.radioButtonCar.Checked).ToString().ToLower() + "&noMotorways=" +
	 * (!motorways).ToString().ToLower() + "&noTollways=" + (!motorways).ToString().ToLower() + "&instructions=false&_=";
	 * webRequest.ContentLength = parameters.Length; Stream requestStream = webRequest.GetRequestStream(); StreamWriter stOut = new
	 * StreamWriter(requestStream, System.Text.Encoding.ASCII); stOut.Write(parameters); stOut.Close(); webResponse =
	 * (HttpWebResponse)webRequest.GetResponse(); if (!webRequest.HaveResponse) { MessageBox.Show("Cannot connect to navigation provider!",
	 * "Routing failed!", MessageBoxButtons.OK, MessageBoxIcon.Question, MessageBoxDefaultButton.Button1); return; } TextReader response =
	 * new StreamReader(webResponse.GetResponseStream()); String line = ""; Route route = new Route(new Pen(Color.Purple, 4),
	 * "RouteOverlay"); route.ShowRoute = true; int skip = 2; while ((line = response.ReadLine()) != null) { if (line.IndexOf("<xls:Error ")
	 * >= 0) { int errorIdx = line.IndexOf("message=\""); int endIdx = line.IndexOf("\"", errorIdx + 9); String errorMessage =
	 * line.Substring(errorIdx + 9, endIdx - errorIdx - 9); MessageBox.Show(errorMessage, "An error occured!", MessageBoxButtons.OK,
	 * MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1); return; } int idx; if ((idx = line.IndexOf("<gml:pos>")) > 0) { int
	 * seperator = line.IndexOf(" ", idx + 1); int endIdx = line.IndexOf("</gml:pos>", seperator + 1); String lonStr = line.Substring(idx +
	 * 9, seperator - idx - 9); String latStr = line.Substring(seperator + 1, endIdx - seperator - 1); double lat = double.Parse(latStr,
	 * NumberFormatInfo.InvariantInfo); double lon = double.Parse(lonStr, NumberFormatInfo.InvariantInfo); PointD projectedPoint = new
	 * PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, lon), Descriptor.LatitudeToTileY(projectionZoomLevel, lat)); if (skip <= 0)
	 * route.Points.Add(projectedPoint); skip--; } } response.Close(); Routes[0] = route; ClearTileCache(); Render(true); } catch
	 * (WebException exc) { #if DEBUG Global.AddLog(exc.ToString()); #endif MessageBox.Show(
	 * "The request to OpenRouteService timed out! Please try again later.", "Timeout!", MessageBoxButtons.OK, MessageBoxIcon.Exclamation,
	 * MessageBoxDefaultButton.Button1); return; } catch (Exception exc) { #if DEBUG Global.AddLog(exc.ToString()); #endif } finally {
	 * Cursor.Current = Cursors.Default; if (webResponse != null) webResponse.Close(); navigationForm.Dispose(); } } } void
	 * showLayerMenu(ClickButton sender) { layerMenu.Show(this, button3.Left + button3.Width, button3.Top, 0); } private void
	 * enableNightmodeChanged(ClickButton sender) { nightMode = !nightMode; nightmodeButton.ButtonImage = (nightMode) ? Global.Icons[6] :
	 * Global.Icons[7]; Config.settings.nightMode", nightMode); Config.AcceptChanges(); lock (loadedTiles) { loadedTiles.Clear();
	 * Render(true); } //this.Render(false); } public void layerClick(ClickButton sender) { CurrentLayer = (Layer)sender.Tag; foreach
	 * (ClickButton layerButton in layerButtons) layerButton.ButtonImage = (CurrentLayer == (Layer)layerButton.Tag) ? Global.Icons[6] :
	 * Global.Icons[7]; Config.settings.CurrentMapLayer", CurrentLayer.Name); Config.AcceptChanges(); lock (loadedTiles) {
	 * ClearCachedTiles(); Render(true); } }
	 */
	public void ClearCachedTiles()
	{
		loadedTilesLock.lock();
		try
		{
			for (long hash : loadedTiles.keySet())
				if (loadedTiles.get(hash).Image != null) loadedTiles.get(hash).Image.recycle();

			loadedTiles.clear();
		}
		finally
		{
			loadedTilesLock.unlock();
		}
	}

	public void ClearCachedTrackTiles()
	{
		trackTilesLock.lock();
		try
		{
			for (long hash : trackTiles.keySet())
				if (trackTiles.get(hash).Image != null) trackTiles.get(hash).Image.recycle();

			trackTiles.clear();
		}
		finally
		{
			trackTilesLock.unlock();
		}
	}

	Point cacheArrowCenter = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
	/*
	 * Font distanceFont = new Font(FontFamily.GenericSansSerif, 9, FontStyle.Regular);
	 */

	static Paint ArrowPaint = null;

	public void RenderTargetArrow()
	{
		cacheArrowCenter.x = Integer.MIN_VALUE;
		cacheArrowCenter.y = Integer.MAX_VALUE;

		if (GlobalCore.SelectedCache() == null) return;

		Coordinate coord = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos : GlobalCore.SelectedCache().Pos;

		// float distance = Datum.WGS84.Distance(center.Latitude,
		// center.Longitude, lat, lon);
		float distance = center.Distance(coord);

		double x = 256.0 * Descriptor.LongitudeToTileX(Zoom, coord.Longitude);
		double y = 256.0 * Descriptor.LatitudeToTileY(Zoom, coord.Latitude);

		int halfHeight = height / 2;
		int halfWidth = width / 2;

		double dirx = x - screenCenter.X;
		double diry = y - screenCenter.Y + this.halfHeight - this.height / 2;

		float[] poi =
			{ (float) dirx, (float) diry };
		float[] res = new float[2];
		Matrix mat = new Matrix();
		mat.preRotate(-canvasHeading);
		mat.mapPoints(res, poi);
		dirx = res[0];
		diry = res[1];

		// if (!(Math.Abs(dirx) > (width / 2) || Math.Abs(diry) > (height / 2)))
		// Ziel sichtbar, Pfeil nicht rendern
		// return;

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
		 * clipX2 = cx; clipY2 = cy; clipLineCircle(-halfWidth, -halfHeight, buttonTrackPosition.getWidth() * 1.5, 0, 0);
		 * clipLineCircle(halfWidth, -halfHeight, buttonTrackPosition.getWidth()* 1.5, 0, 0); clipLineCircle(-halfWidth, halfHeight,
		 * buttonTrackPosition.getWidth()* 1.5, 0, 0); clipLineCircle(halfWidth, halfHeight, buttonTrackPosition.getWidth()* 1.5, 0, 0); cx
		 * = clipX2; cy = clipY2;
		 */
		// Position auf der Karte
		Point pt = new Point((int) (cx + halfWidth), (int) (cy + halfHeight));

		double length = Math.sqrt(cx * cx + cy * cy);

		int size = lineHeight;

		float dirX = -(float) (cx / length);
		float dirY = -(float) (cy / length);

		float crossX = -dirY;
		float crossY = dirX;

		Point[] dir = new Point[3];
		dir[0] = new Point((int) (pt.x), (int) (pt.y));

		// x/y -> -y/x
		dir[1] = new Point();
		dir[1].x = (int) (pt.x + dirX * 1.5f * size - crossX * size * 0.5f);
		dir[1].y = (int) (pt.y + dirY * 1.5f * size - crossY * size * 0.5f);

		dir[2] = new Point();
		dir[2].x = (int) (pt.x + dirX * 1.5f * size + crossX * size * 0.5f);
		dir[2].y = (int) (pt.y + dirY * 1.5f * size + crossY * size * 0.5f);

		cacheArrowCenter.x = (int) (pt.x + dirX * 1.5f * size);
		cacheArrowCenter.y = (int) (pt.y + dirY * 1.5f * size);

		boolean DrawArrow = (Math.abs(dirx) > (width / 2) || Math.abs(diry) > (height / 2));

		if (ArrowPaint == null)
		{
			ArrowPaint = new Paint();
		}

		if (DrawArrow)
		{
			/*
			 * graphics.FillPolygon(redBrush, dir); graphics.DrawPolygon(blackPen, dir);
			 */

			ArrowPaint.setStyle(Style.FILL);
			ArrowPaint.setColor(Color.RED);
			Path path = new Path();
			path.moveTo(dir[0].x, dir[0].y);
			path.lineTo(dir[1].x, dir[1].y);
			path.lineTo(dir[2].x, dir[2].y);
			path.lineTo(dir[0].x, dir[0].y);
			canvas.drawPath(path, ArrowPaint);
			ArrowPaint.setStyle(Style.STROKE);
			ArrowPaint.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
			canvas.drawPath(path, ArrowPaint);
		}

		float fontCenterX = pt.x + dirX * 2.2f * size;
		float fontCenterY = pt.y + dirY * 2.2f * size;

		// Anzeige Pfeile zum Ziel auf Karte mit Waypoint abfrage
		String text = UnitFormatter.DistanceString(distance);
		/*
		 * SizeF textSize = graphics.MeasureString(text, distanceFont); if (Math.abs(dirx) > (width / 2) || Math.abs(diry) > (height / 2))
		 * graphics.DrawString(text, distanceFont, blackBrush, fontCenterX - textSize.Width / 2, fontCenterY - textSize.Height / 2);
		 */

	}

	private double clipX2;
	private double clipY2;

	private void clipLineCircle(double cx, double cy, double r, double x1, double y1)
	{
		if (((cx - clipX2) * (cx - clipX2) + (cy - clipY2) * (cy - clipY2)) > r * r) return;

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
	 * internal void UpdateLayerButtons() { float heightMultiplier = 2.5f; if (Manager.Layers.Count > 7) heightMultiplier = 7f /
	 * (float)(Manager.Layers.Count) * (float)2.5f; if (heightMultiplier < 1.2f) heightMultiplier = 1.2f; foreach (Layer layer in
	 * Manager.Layers) { ClickButton button = new ClickButton(layer.FriendlyName, (CurrentLayer == layer) ? Global.Icons[6] :
	 * Global.Icons[7], layerClick, null, null, heightMultiplier); button.Tag = layer; layerButtons.Add(button); layerMenu.Add(button,
	 * false); } } internal void ClearTileCache() { lock (loadedTiles) { foreach (Descriptor desc in loadedTiles.Keys)
	 * loadedTiles[desc].Dispose(); loadedTiles = new Dictionary<Descriptor, Tile>(); } } internal void resetRoute(ClickButton sender) {
	 * Routes[0].Points = new List<PointD>(); Routes[0].ShowRoute = false; Routes[0].Name = "-empty- RouteOverlay"; ClearTileCache();
	 * Render(false); }
	 */
	int numLoadedTiles()
	{
		int cnt = 0;
		loadedTilesLock.lock();
		try
		{
			for (Tile tile : loadedTiles.values())
				if (tile.Image != null) cnt++;
		}
		finally
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
				if (tile.Image != null) cnt++;
		}
		finally
		{
			trackTilesLock.unlock();
		}

		return cnt;
	}

	/*
	 * private static void InternalRotateImage(int rotationAngle, Bitmap originalBitmap, Bitmap rotatedBitmap) { // It should be faster to
	 * access values stored on the stack // compared to calling a method (in this case a property) to // retrieve a value. That's why we
	 * store the width and height // of the bitmaps here so that when we're traversing the pixels // we won't have to call more methods than
	 * necessary. int newWidth = rotatedBitmap.Width; int newHeight = rotatedBitmap.Height; int originalWidth = originalBitmap.Width; int
	 * originalHeight = originalBitmap.Height; // We're going to use the new width and height minus one a lot so lets // pre-calculate that
	 * once to save some more time int newWidthMinusOne = newWidth - 1; int newHeightMinusOne = newHeight - 1; // To grab the raw bitmap
	 * data into a BitmapData object we need to // "lock" the data (bits that make up the image) into system memory. // We lock the source
	 * image as ReadOnly and the destination image // as WriteOnly and hope that the .NET Framework can perform some // sort of optimization
	 * based on this. // Note that this piece of code relies on the PixelFormat of the // images to be 32 bpp (bits per pixel). We're not
	 * interested in // the order of the components (red, green, blue and alpha) as // we're going to copy the entire 32 bits as they are.
	 * BitmapData originalData = originalBitmap.LockBits( new Rectangle(0, 0, originalWidth, originalHeight), ImageLockMode.ReadOnly,
	 * PixelFormat.Format32bppRgb); BitmapData rotatedData = rotatedBitmap.LockBits( new Rectangle(0, 0, rotatedBitmap.Width,
	 * rotatedBitmap.Height), ImageLockMode.WriteOnly, PixelFormat.Format32bppRgb); // We're not allowed to use pointers in "safe" code so
	 * this // section has to be marked as "unsafe". Cool! unsafe { // Grab int pointers to the source image data and the // destination
	 * image data. We can think of this pointer // as a reference to the first pixel on the first row of the // image. It's actually a
	 * pointer to the piece of memory // holding the int pixel data and we're going to treat it as // an array of one dimension later on to
	 * address the pixels. int* originalPointer = (int*)originalData.Scan0.ToPointer(); int* rotatedPointer =
	 * (int*)rotatedData.Scan0.ToPointer(); // There are nested for-loops in all of these case statements // and one might argue that it
	 * would have been neater and more // tidy to have the switch statement inside the a single nested // set of for loops, doing it this
	 * way saves us up to three int // to int comparisons per pixel. // switch (rotationAngle) { case 90: for (int y = 0; y <
	 * originalHeight; ++y) { int destinationX = newWidthMinusOne - y; for (int x = 0; x < originalWidth; ++x) { int sourcePosition = (x + y
	 * * originalWidth); int destinationY = x; int destinationPosition = (destinationX + destinationY * newWidth);
	 * rotatedPointer[destinationPosition] = originalPointer[sourcePosition]; } } break; case 180: for (int y = 0; y < originalHeight; ++y)
	 * { int destinationY = (newHeightMinusOne - y) * newWidth; for (int x = 0; x < originalWidth; ++x) { int sourcePosition = (x + y *
	 * originalWidth); int destinationX = newWidthMinusOne - x; int destinationPosition = (destinationX + destinationY);
	 * rotatedPointer[destinationPosition] = originalPointer[sourcePosition]; } } break; case 270: for (int y = 0; y < originalHeight; ++y)
	 * { int destinationX = y; for (int x = 0; x < originalWidth; ++x) { int sourcePosition = (x + y * originalWidth); int destinationY =
	 * newHeightMinusOne - x; int destinationPosition = (destinationX + destinationY * newWidth); rotatedPointer[destinationPosition] =
	 * originalPointer[sourcePosition]; } } break; } // We have to remember to unlock the bits when we're done.
	 * originalBitmap.UnlockBits(originalData); rotatedBitmap.UnlockBits(rotatedData); } } private void bZoomIn_Click(object sender,
	 * EventArgs e) { zoomIn(); } private void bZoomOut_Click(object sender, EventArgs e) { zoomOut(); } private void MapView_Load(object
	 * sender, EventArgs e) { foreach (Layer layer in MapView.Manager.Layers) { ToolStripMenuItem tsmi = new ToolStripMenuItem(layer.Name);
	 * tsiLayer.DropDownItems.Add(tsmi); tsmi.Click += new EventHandler(tsi_Click); if ((MapView.View != null) && (MapView.View.CurrentLayer
	 * != null) && (MapView.View.CurrentLayer.Name == layer.Name)) tsmi.Checked = true; tsmi.Tag = layer; } } void tsi_Click(object sender,
	 * EventArgs e) { ToolStripMenuItem tsmi = sender as ToolStripMenuItem; if (tsmi != null) { Layer layer = tsmi.Tag as Layer; if (layer
	 * != null) { MapView.View.SetCurrentLayer(layer); foreach (ToolStripMenuItem tsmi2 in tsiLayer.DropDownItems) { if (tsmi2 != null)
	 * tsmi2.Checked = false; } tsmi.Checked = true; } } } private void contextMenuStrip1_Opening(object sender, CancelEventArgs e) { }
	 * private void loactionToolStripMenuItem_Click(object sender, EventArgs e) { if (lastMouseCoordinate != null) { Location location = new
	 * Geocaching.Location("", lastMouseCoordinate.Latitude, lastMouseCoordinate.Longitude); location.Edit(); } } private void
	 * copyCoordinatesToolStripMenuItem_Click(object sender, EventArgs e) { if (lastMouseCoordinate != null) {
	 * Clipboard.SetText(lastMouseCoordinate.FormatCoordinate()); } }
	 */

	@Override
	public void PositionChanged(Location location)
	{
		GlobalCore.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		GlobalCore.LastValidPosition.Elevation = location.getAltitude();
		// Muss der aktive Track gezeichnet werden?
		if ((Global.AktuelleRoute != null) && Global.AktuelleRoute.ShowRoute)
		{
			try
			{
				// Map Tiles aktualisieren, wenn der AktiveTrack erweitert
				// wurde!
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

							double tileX = tile.Descriptor.X * 256;
							double tileY = tile.Descriptor.Y * 256;

							double adjustmentX = Math.pow(2, tile.Descriptor.Zoom - RouteOverlay.projectionZoomLevel) * 256;
							double adjustmentY = Math.pow(2, tile.Descriptor.Zoom - RouteOverlay.projectionZoomLevel) * 256;

							for (int j = 0; j < (punkte.size() - 1); j++)
							{
								canvas.drawLine((int) (punkte.get(j).X * adjustmentX - tileX),
										(int) (punkte.get(j).Y * adjustmentY - tileY), (int) (punkte.get(j + 1).X * adjustmentX - tileX),
										(int) (punkte.get(j + 1).Y * adjustmentY - tileY), paint);
							}
							if (punkte.size() > 0)
							{
								double x = (punkte.get(punkte.size() - 1).X * adjustmentX - tileX);
								double y = (punkte.get(punkte.size() - 1).Y * adjustmentY - tileY);
								// graphics.DrawString(x.ToString() + " - " +
								// y.ToString(), fontSmall, new
								// SolidBrush(Color.Black), 20, 20);
							}
						}
					}
					finally
					{
						trackTilesLock.unlock();
					}
				}
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.PositionChanged()", "1", exc);
			}
		}

		if (Config.settings.MoveMapCenterWithSpeed.getValue() && alignToCompass && (lockPosition >= 1))
		{
			if (location.hasSpeed())
			{
				double maxSpeed = Config.settings.MoveMapCenterMaxSpeed.getValue();
				int diff = (int) ((double) (height) / 3 * location.getSpeed() / maxSpeed);
				if (diff > height / 3) diff = height / 3;

				halfHeight = height / 2 + diff;
			}
		}
		else
			halfHeight = height / 2;

		if (isVisible)
		{
			try
			{
				// draw Map only when MapView is visible
				if (lockPosition >= 1/* && !animationTimer.Enabled */) setCenter(new Coordinate(GlobalCore.LastValidPosition));
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.PositionChanged()", "2", exc);
			}
			Render(false);
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 0:
			SetCurrentLayer(MapView.Manager.GetLayerByName(item.getTitle().toString(), item.getTitle().toString(), ""));
			return true;
			/*
			 * case R.id.hubertmedia: SetCurrentLayer(MapView.Manager.GetLayerByName ("Hubermedia Bavaria", "Hubermedia Bavaria", ""));
			 * return true; case R.id.googleearth: SetCurrentLayer(MapView.Manager.GetLayerByName("Google Earth", "Google Earth", ""));
			 * return true; case R.id.mapnik: SetCurrentLayer(MapView.Manager.GetLayerByName("Mapnik", "Mapnik", "")); return true;
			 */

		case R.id.mi_Track:
			AllContextMenuCallHandler.showTrackContextMenu();
			return true;

		case R.id.mapview_smooth:
			AllContextMenuCallHandler.showMapSmoothMenu();
			return true;

		case R.id.layer:
			AllContextMenuCallHandler.showMapLayerMenu();
			return true;

		case R.id.mimapview_view:
			AllContextMenuCallHandler.showMapViewLayerMenu();
			return true;

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

		case R.id.mapview_go_settings:
			final Intent mainIntent = new Intent().setClass(main.mainActivity,
					de.cachebox_test.Views.AdvancedSettingsForms.SettingsScrollView.class);
			Bundle b = new Bundle();
			b.putSerializable("Show", 3); // Show Settings und setze ein
											// PerformClick auf den MapSettings
											// Button! (3)
			mainIntent.putExtras(b);
			main.mainActivity.startActivity(mainIntent);
			return true;
		case R.id.miMap_HideFinds:
			hideMyFinds = !hideMyFinds;
			Config.settings.MapHideMyFinds.setValue(hideMyFinds);
			Config.AcceptChanges();
			Render(true);
			return true;

		case R.id.miMap_ShowDT:
			showDT = !showDT;
			Config.settings.MapShowDT.setValue(showDT);
			Config.AcceptChanges();
			Render(true);
			return true;

		case R.id.miMap_ShowRatings:
			showRating = !showRating;
			Config.settings.MapShowRating.setValue(showRating);
			Config.AcceptChanges();
			Render(true);
			return true;

		case R.id.miMap_ShowTitles:
			showTitles = !showTitles;
			Config.settings.MapShowTitles.setValue(showTitles);
			Config.AcceptChanges();
			Render(true);
			return true;

		case R.id.miMap_ShowDirektLine:
			showDirektLine = !showDirektLine;
			Config.settings.ShowDirektLine.setValue(showDirektLine);
			Config.AcceptChanges();
			Render(true);
			return true;

			// case R.id.searchcaches_online:
			// ((main)main.mainActivity).searchOnline();
			// ((main)main.mainActivity).ListSearch();
			// break;
		}
		return false;
	}

	private void setSmotthScrolling(SmoothScrollingTyp typ)
	{
		GlobalCore.SmoothScrolling = typ;
		Config.settings.SmoothScrolling.setValue(typ.toString());
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
		AllContextMenuCallHandler.showMapViewContextMenu();
	}

	@Override
	public int GetMenuId()
	{
		return 0;// return R.menu.menu_mapview;
	}

	/*
	 * int anzCompassValues = 0; float compassValue = 0; long lastCompassTick = -99999;
	 */
	@Override
	public void OrientationChanged(float heading)
	{
		if (!isVisible) return;
		/*
		 * anzCompassValues++; compassValue += heading; long aktTick = SystemClock.uptimeMillis(); if (aktTick < lastCompassTick + 200) { //
		 * do not update view now, only every 200 millisec return; } if (anzCompassValues == 0) { lastCompassTick = aktTick; return; } //
		 * Durchschnitts Richtung berechnen heading = compassValue / anzCompassValues; anzCompassValues = 0; compassValue = 0;
		 */
		if (alignToCompass) changeOrientation(heading);

		// lastCompassTick = aktTick;
	}

	private void changeOrientation(float heading)
	{
		if (canvas == null) return;
		// canvas.rotate(canvasHeading - heading, offScreenBmp.getWidth() / 2,
		// offScreenBmp.getHeight() / 2);

		try
		{

			float newCanvasHeading = heading;
			// liefert die Richtung (abhängig von der Geschwindigkeit von
			// Kompass oder GPS
			if (!GlobalCore.Locator.UseCompass() && alignToCompass)
			{
				// GPS-Richtung soll verwendet werden!
				newCanvasHeading = GlobalCore.Locator.getHeading();
				heading = newCanvasHeading;
			}

			animationThread.rotateTo(newCanvasHeading);

			// da die Map gedreht in die offScreenBmp gezeichnet werden soll,
			// muss der Bereich, der gezeichnet werden soll größer sein, wenn
			// gedreht wird.
			double w = offScreenBmp.getWidth();
			double h = offScreenBmp.getHeight();
			if (heading >= 180) heading -= 180;
			if (heading > 90) heading = 180 - heading;
			double alpha = heading / 180 * Math.PI;
			double beta = Math.atan(w / h);
			double gammaW = Math.PI / 2 - alpha - beta;
			// halbe Länge der Diagonalen
			double diagonal = Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2)) / 2;
			drawingWidth = (int) (Math.cos(gammaW) * diagonal * 2);

			double gammaH = alpha - beta;
			drawingHeight = (int) (Math.cos(gammaH) * diagonal * 2);

			// debugString1 = Math.round(alpha / Math.PI * 180) + " - " +
			// Math.round(beta / Math.PI * 180) + " - " + Math.round(gammaW /
			// Math.PI * 180) + " - " + Math.round(gammaH / Math.PI * 180);
			// debugString2 = "h = " + drawingHeight + " - w = " + drawingWidth;
			Render(true);
		}
		catch (Exception exc)
		{
			Logger.Error("MapView.changeOrientation()", "", exc);
		}

	}

	@Override
	public void OnHide()
	{
		ClearCachedTiles();
		ClearCachedTrackTiles();
		isVisible = false;

		if ((animationThread != null) && (animationThread.isAlive()))
		{
			animationThread.beendeThread();
			animationThread = null;
		}

		// save zoom level
		if (Config.settings.lastZoomLevel.getValue() != Zoom)
		{
			Config.settings.lastZoomLevel.setValue(Zoom);
			Config.AcceptChanges();
		}
	}

	@Override
	public void OnFree()
	{
		if (animationThread != null)
		{
			animationThread.beendeThread();
			if (animationThread.isAlive()) animationThread.stop();
			animationThread = null;
		}
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{

	}

	private final Handler messageHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
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
								centerOsmSpace.X = screenCenter.X;
								centerOsmSpace.Y = screenCenter.Y;
							}
							boolean updateCacheList = bundle.getBoolean("updateCacheList");
							if (updateCacheList) updateCacheList();
							if (bundle.getBoolean("doRender"))
							{
								try
								{
									Render(true);
								}
								catch (Exception exc)
								{
									Logger.Error("MapView.messageHandler()", "Render", exc);
								}
							}
						}
						sendEmptyMessage(5); // im UI Thread ausführen
					}
					catch (Exception exc)
					{
						Logger.Error("MapView.messageHandler()", "4", exc);
					}
				}
				if (msg.what == 5)
				{
					try
					{
						zoomControls.setIsZoomOutEnabled(Zoom > minZoom);
						zoomControls.setIsZoomInEnabled(Zoom < maxZoom);
					}
					catch (Exception exc)
					{
						Logger.Error("MapView.messageHandler()", "5", exc);
					}

				}
			}
			super.handleMessage(msg);
		}
	};

	public class AnimationThread extends Thread
	{
		private Handler handler;

		public boolean posInitialized = false;
		public double toX = 0;
		public double toY = 0;
		public boolean posDirect = false;
		public double posFaktor = 5; // je höher, desto langsamer
		public boolean zoomInitialized = false;
		public double toZoom = 0;
		public double toFaktor = 0;
		public boolean headingInitialized = false;
		public float toHeading = 0;
		public boolean animationFertig = true;
		private int count = 0;

		public void run()
		{
			Looper.prepare();
			handler = new Handler()
			{
				public void handleMessage(Message msg)
				{
					if (msg.what == 4)
					{
						try
						{
							animationFertig = false;
							long nextTick = 0;
							boolean doUpdateCacheList = false;

							while (true)
							{
								// zur Rückgabe
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

										if (Math.abs(aktHeading - toHeading) < 0.3f) fertigHeading = true;
										else
											fertigHeading = false;
										br.putBoolean("ChangeHeading", true);
										br.putFloat("Heading", aktHeading);
									}
									else
										br.putBoolean("ChangeHeading", false);

									if (!mouseMoved)
									{
										// Zoom und Position nur dann ändern,
										// wenn nicht gerade ein MouseMove aktiv
										// ist
										int aktZoom = Zoom;
										double faktor = multiTouchFaktor;
										if (faktor < 1)
										{
											// runden auf 0.05;
											faktor = Math.rint(faktor * smoothScrolling.AnimationSteps() * 2)
													/ (smoothScrolling.AnimationSteps() * 2);
											if (faktor < 0.74) faktor = 0.75;
										}
										if (faktor > 1)
										{
											// runden auf 0.1;
											faktor = Math.rint(faktor * smoothScrolling.AnimationSteps() * 2)
													/ (smoothScrolling.AnimationSteps() * 2);
											if (faktor > 1.5) faktor = 1.5;
										}
										if (zoomInitialized && ((aktZoom != toZoom) || (Math.abs(faktor - toFaktor) >= 0.001)))
										{
											double diff;
											if (aktZoom + faktor > toZoom + toFaktor)
											{
												diff = -1;
												if (faktor <= 1.0001) diff /= 2;
											}
											else
											{
												diff = 1;
												if (faktor < 0.99999) diff /= 2;
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

											if ((aktZoom == toZoom) && (Math.abs(faktor - toFaktor) < 0.001)) fertigZoom = false;
											else
												fertigZoom = false;
											doUpdateCacheList = true;
											br.putBoolean("ChangeZoom", true);
											br.putInt("Zoom", aktZoom);
											br.putDouble("Faktor", faktor);
										}
										else
											br.putBoolean("ChangeZoom", false);

										if (posInitialized && ((Math.abs(aktX - toX) > 1.1) || Math.abs(aktY - toY) > 1.1))
										{
											fertigPos = false;
											doUpdateCacheList = true;

											double scale = 1 / posFaktor;

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
										}
										else
											br.putBoolean("ChangePos", false);
									}

								}
								finally
								{
									animationLock.unlock();
								}

								// Nachricht senden
								// Cache Liste nach dem Drehen nicht
								// aktualisieren, da der sichtbare Bereich sich
								// fast nicht ändert
								// und sowieso mit UpdateCacheList mehr Caches
								// geladen werden, wie angezeigt...
								br.putBoolean("updateCacheList", /*
																 * fertigHeading &&
																 */fertigZoom && fertigPos && doUpdateCacheList);
								ret.setData(br);
								ret.what = 4;

								long delay;
								if (nextTick == 0)
								{
									// ersten Durchlauf sofort zeichnen!
									delay = 0;
									nextTick = SystemClock.uptimeMillis();
								}
								else
								{
									delay = nextTick - SystemClock.uptimeMillis();
									if ((delay < 0) && (fertigHeading && fertigZoom && fertigPos)) delay = 0; // das
																												// letzte
																												// Rendern
																												// muss
																												// gemacht
																												// werden!!
								}

								br.putBoolean("doRender", delay >= 0);
								// nur rendern, wenn noch nicht zu viel
								// Zeitvergangen ist...
								// ansonsten überspringen
								// Pause abwarten
								// Pause
								try
								{
									if (delay > 0)
									{
										Thread.sleep(delay);
									}
								}
								catch (InterruptedException e)
								{
									Logger.Error("MapView.AnimationThread.handleMessage()", "TimerInterrupt", e);
								}

								messageHandler.handleMessage(ret); // im
																	// animationThread
																	// ausführen

								// nächstes Rendern bei:
								nextTick += smoothScrolling.AnimationWait();

								if (fertigHeading && fertigZoom && fertigPos) break;
							}
							animationFertig = true;
						}
						catch (Exception exc)
						{
							Logger.Error("MapView.AnimationThread.handleMessage()", "", exc);
						}

					}

				}
			};
			Looper.loop();
			System.gc();
		}

		private float correctHeading(float heading)
		{
			if (heading >= 360) heading = heading - 360;
			else if (heading < 0) heading = heading + 360;
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
				if (distance < 0) direction = -1;

				int steps = smoothScrolling.AnimationSteps();
				double faktor = Math.sqrt((double) steps);

				return (float) (Math.max(Math.abs(distance) / faktor, 0.5f) * direction);
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.AnimationThread.rotationDirection()", "", exc);
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
				animateTo.X = 256 * Descriptor.LongitudeToTileX(Zoom, target.Longitude);
				animateTo.Y = 256 * Descriptor.LatitudeToTileY(Zoom, target.Latitude);
				double xDiff = animateFrom.X - animateTo.X;
				double yDiff = animateFrom.Y - animateTo.Y;
				center = target;
				if ((Math.sqrt(xDiff * xDiff + yDiff * yDiff) < 2 * 256) || (!useDirect))
				{
					animationLock.lock();
					try
					{
						animationThread.posFaktor = newPosFaktor;
						animationThread.posInitialized = true;
						animationThread.toX = animateTo.X;
						animationThread.toY = animateTo.Y;
						animationThread.posDirect = false;
					}
					finally
					{
						animationLock.unlock();
					}
					handler.sendEmptyMessage(4);
				}
				else
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
					}
					finally
					{
						animationLock.unlock();
					}
					handler.sendEmptyMessage(4);
				}
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.AnimationThread.moveTo()", "", exc);
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
				}
				finally
				{
					animationLock.unlock();
				}
				handler.sendEmptyMessage(4);
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.AnimationThread.ZoomTo", "", exc);
			}
		}

		private float lastNewHeading = -999;

		public void rotateTo(float newHeading)
		{
			try
			{
				if (!alignToCompass) return;
				if (Math.abs(lastNewHeading - newHeading) < 1) return;
				lastNewHeading = newHeading;

				animationLock.lock();
				try
				{
					animationThread.headingInitialized = true;
					animationThread.toHeading = lastNewHeading;
				}
				finally
				{
					animationLock.unlock();
				}
				// handler.removeMessages(4);
				handler.sendEmptyMessage(4);
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.AnimationThread.rotateTo", "", exc);
			}
		}

		public void stopMove()
		{
			try
			{
				handler.sendEmptyMessage(1);
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.AnimationThread.stopMove", "", exc);
			}
		}

		public void beendeThread()
		{
			try
			{
				handler.getLooper().quit();
			}
			catch (Exception exc)
			{
				Logger.Error("MapView.AnimationThread.beendeThread", "", exc);
			}
		}
	}

	// 0 -> kein
	// 1 -> normal
	// 2 -> weich
	// 3 -> sehr weich
	// 4 -> Benutzerdefiniert
	public SmoothScrolling smoothScrolling = new SmoothScrolling();

	private class SmoothScrolling
	{
		private int[] animationSteps = new int[5]; // Schritte
		private int[] animationWait = new int[5]; // Abstand zwischen den
													// Schritten in
													// Millisekunden

		public SmoothScrolling()
		{
			animationSteps[0] = 1;
			animationWait[0] = 1;

			animationSteps[1] = 5;
			animationWait[1] = 100;

			animationSteps[2] = 10;
			animationWait[2] = 50;

			animationSteps[3] = 20;
			animationWait[3] = 25;

			animationSteps[4] = 1;
			animationWait[4] = 1;
		}

		public int AnimationSteps()
		{
			return animationSteps[GlobalCore.SmoothScrolling.ordinal()];
		}

		public int AnimationWait()
		{
			return animationWait[GlobalCore.SmoothScrolling.ordinal()];
		}
	}

	@Override
	public int GetContextMenuId()
	{

		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{

	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{

		return false;
	}

	public void CacheListChangedEvent()
	{
		if (!isVisible) return; // nur wenn sichtbar
		updateCacheList();
		Render(true);
	}
}
