package CB_Core.GL_UI.Views;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.WaypointListChangedEventList;
import CB_Core.Events.invalidateTextureEvent;
import CB_Core.Events.invalidateTextureEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.EditWaypoint;
import CB_Core.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_Core.GL_UI.Controls.InfoBubble;
import CB_Core.GL_UI.Controls.MapInfoPanel;
import CB_Core.GL_UI.Controls.MapInfoPanel.CoordType;
import CB_Core.GL_UI.Controls.MapScale;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.ZoomButtons;
import CB_Core.GL_UI.Controls.ZoomScale;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.MainViewBase;
import CB_Core.GL_UI.Views.MapViewCacheList.MapViewCacheListUpdateData;
import CB_Core.GL_UI.Views.MapViewCacheList.WaypointRenderInfo;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.MapTileLoader;
import CB_Core.Map.Point;
import CB_Core.Map.PointL;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.TileGL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Core.Util.FileIO;
import CB_Core.Util.iChanged;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class MapView extends CB_View_Base implements SelectedCacheEvent, PositionChangedEvent, invalidateTextureEvent
{
	public static final boolean debug = false;

	public static MapView that = null; // für Zugriff aus Listeners heraus auf this
	private final int ZoomTime = 1000;

	private boolean CompassMode = false;

	// ####### Enthaltene Controls ##########
	private MultiToggleButton togBtn;
	private ZoomButtons zoomBtn;
	private MapInfoPanel info;
	private ZoomScale zoomScale;
	private InfoBubble infoBubble;
	private MapScale mapScale;
	// ########################################

	protected SortedMap<Integer, Integer> DistanceZoomLevel;

	public static MapTileLoader mapTileLoader = new MapTileLoader();
	private boolean alignToCompass = false;
	private boolean CarMode = false;

	private float mapHeading = 0;
	private float arrowHeading = 0;
	private MapViewCacheList mapCacheList;
	private Point lastMovement = new Point(0, 0);
	private int zoomCross = 16;
	private Vector2 myPointOnScreen;

	private float ySpeedVersatz = 200;

	// private GL_ZoomScale zoomScale;

	// Settings values
	private boolean showRating;
	private boolean showDT;
	private boolean showTitles;
	private boolean hideMyFinds;
	private boolean showCompass;
	private boolean showDirektLine;
	private boolean showAllWaypoints;
	private boolean showAccuracyCircle;
	private boolean showMapCenterCross;

	// private boolean nightMode;
	public int aktZoom;
	// private float startCameraZoom;
	// private float endCameraZoom;
	// private float diffCameraZoom;

	// für kinetischen Zoom und Pan
	private KineticZoom kineticZoom = null;
	private KineticPan kineticPan = null;
	// #################################################################
	//
	// Min, Max und Act Zoom Werte sind jetzt im "zoomBtn" gespeichert!
	//
	// maxzoom wird 1:1 dargestellt
	// int minzoom = 6;
	// int maxzoom = 20;
	// int zoom = 13;
	// #################################################################

	String str = "";

	float maxTilesPerScreen = 0;
	float iconFactor = 1.5f;

	long posx = 8745;
	long posy = 5685;

	// screencenter in World Coordinates (Pixels in Zoom Level maxzoom
	public PointL screenCenterW = new PointL(0, 0); // wird am Anfang von Render() gesetzt
	PointL screenCenterT = new PointL(0, 0); // speichert dauerhaft den Zustand
	int mapIntWidth;
	int mapIntHeight;
	int drawingWidth;
	int drawingHeight;

	long pos20y = 363904;
	long size20 = 256;

	public Coordinate center = new Coordinate(48.0, 12.0);

	private boolean positionInitialized = false;
	// String CurrentLayer = "germany-0.2.4.map";

	OrthographicCamera camera;

	// Gdx2DPixmap circle;
	// Texture tcircle;

	long startTime;
	Timer myTimer;

	boolean useNewInput = true;
	Cache lastSelectedCache = null;
	Waypoint lastSelectedWaypoint = null;

	public MapView(CB_RectF rec, boolean compassMode, String Name)
	{
		super(rec, Name);
		// statischen that nur setzen wenn die HauptMapView initialisiert wird
		if (!compassMode)
		{
			that = this;
		}
		else
		{
			this.setOnDoubleClickListener(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					// Center own position!
					setCenter(Locator.getCoordinate());
					return true;
				}
			});
		}

		CompassMode = compassMode;

		invalidateTextureEventList.Add(this);
		Config.settings.MapsforgeDayTheme.addChangedEventListner(themeChangedEventHandler);
		Config.settings.MapsforgeNightTheme.addChangedEventListner(themeChangedEventHandler);
		registerSkinChangedEvent();
		setBackground(SpriteCache.ListBack);
		int maxNumTiles = 0;
		// calculate max Map Tile cache
		try
		{
			int aTile = 256 * 256;
			maxTilesPerScreen = (int) ((rec.getWidth() * rec.getHeight()) / aTile + 0.5);

			if (maxTilesPerScreen < 10)
			{
				float a = maxTilesPerScreen - 10;
				maxNumTiles = (int) (-90.0 / 6561.0 * (a * a * a * a) + 108);
			}
			else
			{
				maxNumTiles = 150;
			}
		}
		catch (Exception e)
		{
			maxNumTiles = 100;
		}

		maxNumTiles = Math.min(maxNumTiles, 150);
		maxNumTiles = Math.max(maxNumTiles, 15);

		mapTileLoader.setMaxNumTiles(maxNumTiles);

		mapScale = new MapScale(new CB_RectF(GL_UISizes.margin, GL_UISizes.margin, this.halfWidth, GL_UISizes.ZoomBtn.getHalfWidth() / 4),
				"mapScale", this);

		if (!CompassMode)
		{
			this.addChild(mapScale);
		}
		else
		{
			mapScale.setInvisible();
		}

		// initial Zoom Buttons
		zoomBtn = new ZoomButtons(GL_UISizes.ZoomBtn, this, "ZoomButtons");
		zoomBtn.setOnClickListenerDown(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// bei einer Zoom Animation in negativer Richtung muss der setDiffCameraZoom gesetzt werden!
				// zoomScale.setDiffCameraZoom(-1.9f, true);
				// zoomScale.setZoom(zoomBtn.getZoom());
				zoomScale.resetFadeOut();
				inputState = InputState.Idle;

				lastDynamicZoom = zoomBtn.getZoom();

				kineticZoom = new KineticZoom(camera.zoom, mapTileLoader.getMapTilePosFactor(zoomBtn.getZoom()),
						System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce(MapView.this.getName() + " ZoomButtonClick");
				calcPixelsPerMeter();
				return true;
			}
		});
		zoomBtn.setOnClickListenerUp(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				setZoomScale(zoomBtn.getZoom());
				zoomScale.resetFadeOut();
				inputState = InputState.Idle;

				lastDynamicZoom = zoomBtn.getZoom();

				kineticZoom = new KineticZoom(camera.zoom, mapTileLoader.getMapTilePosFactor(zoomBtn.getZoom()),
						System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce(MapView.this.getName() + " ZoomButtonClick");
				calcPixelsPerMeter();
				return true;
			}
		});

		if (!CompassMode)
		{
			this.addChild(zoomBtn);
		}
		else
		{
			zoomBtn.setInvisible();
		}

		this.setOnClickListener(onClickListner);

		info = (MapInfoPanel) this.addChild(new MapInfoPanel(GL_UISizes.Info, "InfoPanel"));

		CB_RectF ZoomScaleRec = new CB_RectF();
		ZoomScaleRec.setSize((float) (44.6666667 * GL_UISizes.DPI),
				this.height - info.getHeight() - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());
		ZoomScaleRec.setPos(new Vector2(GL_UISizes.margin, zoomBtn.getMaxY() + GL_UISizes.margin));

		zoomScale = new ZoomScale(ZoomScaleRec, "zoomScale", 2, 21, 12);
		if (!CompassMode) this.addChild(zoomScale);

		InitializeMap();

		// initial Zoom Scale
		// zoomScale = new GL_ZoomScale(6, 20, 13);

		mapCacheList = new MapViewCacheList(MapTileLoader.MAX_MAP_ZOOM);

		// from create

		String currentLayerName = Config.settings.CurrentMapLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			if (mapTileLoader.CurrentLayer == null)
			{
				mapTileLoader.CurrentLayer = ManagerBase.Manager.GetLayerByName(currentLayerName, currentLayerName, "");
			}
		}

		String currentOverlayLayerName = Config.settings.CurrentMapOverlayLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			if (mapTileLoader.CurrentOverlayLayer == null && currentOverlayLayerName.length() > 0) mapTileLoader.CurrentOverlayLayer = ManagerBase.Manager
					.GetLayerByName(currentOverlayLayerName, currentOverlayLayerName, "");
		}

		mapIntWidth = (int) rec.getWidth();
		mapIntHeight = (int) rec.getHeight();
		drawingWidth = mapIntWidth;
		drawingHeight = mapIntHeight;

		iconFactor = (float) Config.settings.MapViewDPIFaktor.getValue();

		togBtn = new MultiToggleButton(GL_UISizes.Toggle, "toggle");

		togBtn.addState("Free", Color.GRAY);
		togBtn.addState("GPS", Color.GREEN);
		togBtn.addState("WP", Color.MAGENTA);
		togBtn.addState("Lock", Color.RED);
		togBtn.addState("Car", Color.YELLOW);
		togBtn.setLastStateWithLongClick(true);
		togBtn.setOnStateChangedListner(new OnStateChangeListener()
		{

			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				boolean wasCarMode = CarMode;
				info.setCoordType(CoordType.Map);

				Config.settings.LastMapToggleBtnState.setValue(State);
				Config.AcceptChanges();

				if (State == 4)
				{
					if (wasCarMode) return; // Brauchen wir nicht noch einmal machen!

					// Car mode
					CarMode = true;
					invalidateTexture();
					info.setCoordType(CoordType.GPS);
				}
				else if (State == 2)
				{
					if (GlobalCore.getSelectedCache() != null)
					{
						if (GlobalCore.getSelectedWaypoint() != null)
						{
							Coordinate tmp = GlobalCore.getSelectedWaypoint().Pos;
							setCenter(new Coordinate(tmp.getLatitude(), tmp.getLongitude()));
						}
						else
						{
							Coordinate tmp = GlobalCore.getSelectedCache().Pos;
							setCenter(new Coordinate(tmp.getLatitude(), tmp.getLongitude()));
						}
					}
					info.setCoordType(CoordType.Cache);
				}
				else if (State > 0)
				{
					setCenter(Locator.getCoordinate());
					info.setCoordType(CoordType.GPS);
				}

				if (State != 4)
				{
					if (!wasCarMode) return; // brauchen wir nicht noch einmal machen
					CarMode = false;
					invalidateTexture();
				}

			}
		});
		togBtn.registerSkinChangedEvent();
		togBtn.setState(CompassMode ? 1 : Config.settings.LastMapToggleBtnState.getValue(), true);
		switch (Config.settings.LastMapToggleBtnState.getValue())
		{
		case 0:
			info.setCoordType(CoordType.Map);
			break;
		case 1:
			info.setCoordType(CoordType.GPS);
			break;
		case 2:
			info.setCoordType(CoordType.Cache);
			break;
		case 3:
			info.setCoordType(CoordType.GPS);
			break;
		case 4:
			info.setCoordType(CoordType.GPS);
			break;
		}

		if (!CompassMode) this.addChild(togBtn);

		infoBubble = new InfoBubble(GL_UISizes.Bubble, "infoBubble");
		infoBubble.setInvisible();
		infoBubble.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (infoBubble.getWaypoint() == null)
				{
					// Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
					Waypoint waypoint = infoBubble.getCache().GetFinalWaypoint();
					// wenn ein Cache keine Final hat, aber einen StartWaypoint dann wird dieser gleich selektiert
					if (waypoint == null) waypoint = infoBubble.getCache().GetStartWaypoint();
					GlobalCore.setSelectedWaypoint(infoBubble.getCache(), waypoint);
				}
				else
				{
					GlobalCore.setSelectedWaypoint(infoBubble.getCache(), infoBubble.getWaypoint());
				}
				infoBubble.setInvisible();
				return true;
			}
		});
		if (!CompassMode) this.addChild(infoBubble);

		resize(rec.getWidth(), rec.getHeight());

		center.setLatitude(Config.settings.MapInitLatitude.getValue());
		center.setLongitude(Config.settings.MapInitLongitude.getValue());
		// Info aktualisieren
		info.setCoord(center);
		aktZoom = Config.settings.lastZoomLevel.getValue();
		zoomBtn.setZoom(aktZoom);
		calcPixelsPerMeter();
		mapScale.zoomChanged();

		if ((center.getLatitude() == -1000) && (center.getLongitude() == -1000))
		{
			// not initialized
			center.setLatitude(48);
			center.setLongitude(12);
		}

	}

	private iChanged themeChangedEventHandler = new iChanged()
	{

		@Override
		public void isChanged()
		{
			MapView.this.invalidateTexture();
		}
	};

	@Override
	public void onShow()
	{
		CB_Core.Events.SelectedCacheEventList.Add(this);
		PositionChangedEventList.Add(this);
		PositionChanged();

		alignToCompass = CompassMode ? true : !Config.settings.MapNorthOriented.getValue();

		CarMode = (togBtn.getState() == 4);
		if (!CarMode)
		{
			drawingWidth = mapIntWidth;
			drawingHeight = mapIntHeight;
		}
		SelectedCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
		setVisible();

		int zoom = MapTileLoader.MAX_MAP_ZOOM;
		float tmpZoom = camera.zoom;
		float faktor = 1.5f;
		// faktor = faktor - iconFactor + 1;
		faktor = faktor / iconFactor;
		while (tmpZoom > faktor)
		{
			tmpZoom /= 2;
			zoom--;
		}
		aktZoom = zoom;

		calcPixelsPerMeter();

	}

	@Override
	public void onHide()
	{
		CB_Core.Events.SelectedCacheEventList.Remove(this);
		PositionChangedEventList.Remove(this);
		setInvisible();
		onStop();// save last zoom and position
	}

	@Override
	public void dispose()
	{
		// remove eventHandler
		invalidateTextureEventList.Remove(this);
		PositionChangedEventList.Remove(this);
		SelectedCacheEventList.Remove(this);
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		// wenn sich die Größe nicht geändert hat, brauchen wir nicht zu machen!
		if (rec.getWidth() == this.mapIntWidth && rec.getHeight() == this.mapIntHeight)
		{
			// Ausser wenn Camera == null!
			if (camera != null) return;
		}
		TargetArrowScreenRec = null;
		this.mapIntWidth = (int) rec.getWidth();
		this.mapIntHeight = (int) rec.getHeight(); // Gdx.graphics.getHeight();
		this.drawingWidth = (int) rec.getWidth();
		this.drawingHeight = (int) rec.getHeight();

		camera = new OrthographicCamera(MainViewBase.mainView.getWidth(), MainViewBase.mainView.getHeight());

		aktZoom = zoomBtn.getZoom();
		// setZoomScale(aktZoom);
		camera.zoom = mapTileLoader.getMapTilePosFactor(aktZoom);
		// endCameraZoom = camera.zoom;
		// diffCameraZoom = 0;
		// camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);
		camera.position.set(0, 0, 0);

		// setze Size als IniSize
		Config.settings.MapIniWidth.setValue(mapIntWidth);
		Config.settings.MapIniHeight.setValue(mapIntHeight);
		Config.AcceptChanges();

		// Logger.LogCat("MapView Size Changed MaxY=" + this.getMaxY());

		requestLayout();

	}

	@Override
	public void onStop()
	{
		if (!CompassMode) // save last zoom and position only from Map, not from CompassMap
		{
			Config.settings.MapInitLatitude.setValue(center.getLatitude());
			Config.settings.MapInitLongitude.setValue(center.getLongitude());
			Config.settings.lastZoomLevel.setValue(aktZoom);
			Config.settings.WriteToDB();
		}

		super.onStop();
	}

	public void SetCurrentLayer(Layer newLayer)
	{
		if (newLayer == null)
		{
			Config.settings.CurrentMapLayer.setValue("");
		}
		else
		{
			Config.settings.CurrentMapLayer.setValue(newLayer.Name);
		}
		Config.AcceptChanges();
		mapTileLoader.CurrentLayer = newLayer;
		mapTileLoader.clearLoadedTiles();
	}

	public void SetCurrentOverlayLayer(Layer newLayer)
	{
		if (newLayer == null)
		{
			Config.settings.CurrentMapOverlayLayer.setValue("");
		}
		else
		{
			Config.settings.CurrentMapOverlayLayer.setValue(newLayer.Name);
		}
		Config.AcceptChanges();
		mapTileLoader.CurrentOverlayLayer = newLayer;
		mapTileLoader.clearLoadedTiles();
	}

	protected SortedMap<Long, TileGL> tilesToDraw = new TreeMap<Long, TileGL>();
	protected SortedMap<Long, TileGL> overlayToDraw = new TreeMap<Long, TileGL>();

	int debugcount = 0;

	@Override
	protected void render(SpriteBatch batch)
	{

		if (Config.settings.MoveMapCenterWithSpeed.getValue() && CarMode && Locator.hasSpeed())
		{

			double maxSpeed = Config.settings.MoveMapCenterMaxSpeed.getValue();

			double percent = Locator.SpeedOverGround() / maxSpeed;

			float diff = (float) ((height) / 3 * percent);
			if (diff > height / 3) diff = height / 3;

			ySpeedVersatz = diff;

		}
		else
			ySpeedVersatz = 0;

		boolean reduceFps = ((kineticZoom != null) || ((kineticPan != null) && (kineticPan.started)));
		if (kineticZoom != null)
		{
			camera.zoom = kineticZoom.getAktZoom();
			// float tmpZoom = mapTileLoader.convertCameraZommToFloat(camera);
			// aktZoom = (int) tmpZoom;

			int zoom = MapTileLoader.MAX_MAP_ZOOM;
			float tmpZoom = camera.zoom;
			float faktor = 1.5f;
			// faktor = faktor - iconFactor + 1;
			faktor = faktor / iconFactor;
			while (tmpZoom > faktor)
			{
				tmpZoom /= 2;
				zoom--;
			}
			aktZoom = zoom;

			if (kineticZoom.getFertig())
			{
				setZoomScale(zoomBtn.getZoom());
				GL.that.removeRenderView(this);
				kineticZoom = null;
			}
			else
				reduceFps = false;

			calcPixelsPerMeter();
			mapScale.ZoomChanged();
			zoomScale.setZoom(mapTileLoader.convertCameraZommToFloat(camera));

		}

		if ((kineticPan != null) && (kineticPan.started))
		{
			long faktor = mapTileLoader.getMapTilePosFactor(aktZoom);
			Point pan = kineticPan.getAktPan();
			// debugString = pan.x + " - " + pan.y;
			// camera.position.add(pan.x * faktor, pan.y * faktor, 0);
			// screenCenterW.x = camera.position.x;
			// screenCenterW.y = camera.position.y;
			screenCenterT.x += (long) pan.x * faktor;
			screenCenterT.y += (long) pan.y * faktor;
			calcCenter();

			if (kineticPan.getFertig())
			{
				kineticPan = null;
			}
			else
				reduceFps = false;
		}

		if (reduceFps)
		{
			GL.that.removeRenderView(this);
		}

		synchronized (screenCenterT)
		{
			screenCenterW.x = screenCenterT.x;
			screenCenterW.y = screenCenterT.y;
		}
		loadTiles();
		/*
		 * if (alignToCompass) { camera.up.x = 0; camera.up.y = 1; camera.up.z = 0; camera.rotate(-mapHeading, 0, 0, 1); } else {
		 * camera.up.x = 0; camera.up.y = 1; camera.up.z = 0; }
		 */
		camera.update();

		renderMapTiles(batch);
		renderOverlay(batch);
		renderUI(batch);

	}

	private void renderOverlay(SpriteBatch batch)
	{
		batch.setProjectionMatrix(myParentInfo.Matrix());

		// calculate icon size
		int iconSize = 0; // 8x8
		if ((aktZoom >= 13) && (aktZoom <= 14)) iconSize = 1; // 13x13
		else if (aktZoom > 14) iconSize = 2; // default Images

		if (!CompassMode) CB_Core.Map.RouteOverlay.RenderRoute(batch, aktZoom, ySpeedVersatz);
		renderWPs(GL_UISizes.WPSizes[iconSize], GL_UISizes.UnderlaySizes[iconSize], batch);
		renderPositionMarker(batch);
		RenderTargetArrow(batch);

	}

	private void renderUI(SpriteBatch batch)
	{
		batch.setProjectionMatrix(myParentInfo.Matrix());

		renderDebugInfo(batch);

	}

	private void renderMapTiles(SpriteBatch batch)
	{
		batch.disableBlending();

		float faktor = camera.zoom;
		float dx = this.ThisWorldRec.getCenterPos().x - MainViewBase.mainView.getCenterPos().x;
		float dy = this.ThisWorldRec.getCenterPos().y - MainViewBase.mainView.getCenterPos().y;

		dy -= ySpeedVersatz;

		camera.position.set(0, 0, 0);
		float dxr = dx;
		float dyr = dy;

		if (alignToCompass || CarMode)
		{
			camera.up.x = 0;
			camera.up.y = 1;
			camera.up.z = 0;
			camera.rotate(-mapHeading, 0, 0, 1);
			double angle = mapHeading * Math.PI / 180;
			dxr = (float) (Math.cos(angle) * dx + Math.sin(angle) * dy);
			dyr = (float) (-Math.sin(angle) * dx + Math.cos(angle) * dy);
		}
		else
		{
			camera.up.x = 0;
			camera.up.y = 1;
			camera.up.z = 0;
		}
		camera.translate(-dxr * faktor, -dyr * faktor, 0);

		camera.update();

		Matrix4 mat = camera.combined;
		/*
		 * float dx = this.ThisWorldRec.getCenterPos().x - MainView.mainView.getCenterPos().x; float dy = this.ThisWorldRec.getCenterPos().y
		 * - MainView.mainView.getCenterPos().y; mat = mat.translate(dx * faktor, dy * faktor, 0);
		 */

		batch.setProjectionMatrix(mat);

		try
		{
			// das Alter aller Tiles um 1 erhöhen
			mapTileLoader.increaseLoadedTilesAge();
		}
		catch (Exception e)
		{
			// LogCat announces a java.util.ConcurrentModificationException
		}
		// for (int tmpzoom = zoom; tmpzoom <= zoom; tmpzoom++)
		{
			int tmpzoom = aktZoom;

			int halfMapIntWidth = mapIntWidth / 2;
			int halfMapIntHeight = mapIntHeight / 2;

			int halfDrawingtWidth = drawingWidth / 2;
			int halfDrawingHeight = drawingHeight / 2;

			Descriptor lo = screenToDescriptor(new Vector2(halfMapIntWidth - halfDrawingtWidth, halfMapIntHeight - halfDrawingHeight
					- ySpeedVersatz), tmpzoom);
			Descriptor ru = screenToDescriptor(new Vector2(halfMapIntWidth + halfDrawingtWidth, halfMapIntHeight + halfDrawingHeight
					+ ySpeedVersatz), tmpzoom);
			for (int i = lo.X; i <= ru.X; i++)
			{
				for (int j = lo.Y; j <= ru.Y; j++)
				{
					Descriptor desc = new Descriptor(i, j, tmpzoom);
					TileGL tile = null;
					TileGL tileOverlay = null;
					try
					{
						tile = mapTileLoader.getLoadedTile(desc);
						tileOverlay = mapTileLoader.getLoadedOverlayTile(desc);
					}
					catch (Exception ex)
					{
					}
					if (tile != null)
					{
						// das Alter der benutzten Tiles auf 0 setzen wenn dies
						// für den richtigen aktuellen Zoom ist
						if (tmpzoom == aktZoom) tile.Age = 0;

						try
						{
							if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(),
									tile);
						}
						catch (Exception e)
						{
						}
					}
					else if (tmpzoom == aktZoom)
					{
						// für den aktuellen Zoom ist kein Tile vorhanden ->
						// kleinere Zoomfaktoren durchsuchen
						if (!renderBiggerTiles(batch, i, j, aktZoom))
						// größere Zoomfaktoren noch durchsuchen, ob davon Tiles
						// vorhanden sind...
						// dafür müssen aber pro fehlendem Tile mehrere kleine
						// Tiles gezeichnet werden (4 oder 16 oder 64...)
						// dieser Aufruf kann auch rekursiv sein...
						renderSmallerTiles(batch, i, j, aktZoom);
					}
					if (tileOverlay != null)
					{
						if (tmpzoom == aktZoom) tileOverlay.Age = 0;
						try
						{
							if (!overlayToDraw.containsKey(tileOverlay.Descriptor.GetHashCode())) overlayToDraw.put(
									tileOverlay.Descriptor.GetHashCode(), tileOverlay);
						}
						catch (Exception e)
						{
						}
					}
					else if (tmpzoom == aktZoom)
					{
						if (!renderBiggerOverlayTiles(batch, i, j, aktZoom)) renderSmallerOverlayTiles(batch, i, j, aktZoom);
					}
				}
			}
		}
		synchronized (screenCenterW)
		{
			for (TileGL tile : tilesToDraw.values())
			{
				tile.createTexture();
				if (tile.texture != null)
				{
					// Faktor, mit der dieses MapTile vergrößert gezeichnet
					// werden muß
					long posFactor = mapTileLoader.getMapTilePosFactor(tile.Descriptor.Zoom);

					long xPos = (long) tile.Descriptor.X * posFactor * 256 - screenCenterW.x;
					long yPos = -(tile.Descriptor.Y + 1) * posFactor * 256 - screenCenterW.y;
					float xSize = tile.texture.getWidth() * posFactor;
					float ySize = tile.texture.getHeight() * posFactor;
					batch.draw(tile.texture, (float) xPos, (float) yPos, xSize, ySize);

				}
			}
		}
		tilesToDraw.clear();
		batch.enableBlending();
		synchronized (screenCenterW)
		{
			for (TileGL tile : overlayToDraw.values())
			{
				tile.createTexture();
				if (tile.texture != null)
				{
					// Faktor, mit der dieses MapTile vergrößert gezeichnet
					// werden muß
					long posFactor = mapTileLoader.getMapTilePosFactor(tile.Descriptor.Zoom);

					long xPos = (long) tile.Descriptor.X * posFactor * 256 - screenCenterW.x;
					long yPos = -(tile.Descriptor.Y + 1) * posFactor * 256 - screenCenterW.y;
					float xSize = tile.texture.getWidth() * posFactor;
					float ySize = tile.texture.getHeight() * posFactor;
					batch.draw(tile.texture, (float) xPos, (float) yPos, xSize, ySize);
				}
			}
		}
		overlayToDraw.clear();

	}

	private Sprite crossLine = null;

	@SuppressWarnings("unused")
	private void renderDebugInfo(SpriteBatch batch)
	{

		if (showMapCenterCross)
		{
			if (togBtn.getState() == 0)
			{
				if (crossLine == null)
				{
					crossLine = SpriteCache.getThemedSprite("pixel2x2");
					crossLine.setScale(UI_Size_Base.that.getScale());
					crossLine.setColor(Fonts.getCrossColor());
				}
				scale = UI_Size_Base.that.getScale();

				int crossSize = Math.min(mapIntHeight / 3, mapIntWidth / 3) / 2;
				DrawUtils.drawSpriteLine(batch, crossLine, scale, mapIntWidth / 2 - crossSize, mapIntHeight / 2, mapIntWidth / 2
						+ crossSize, mapIntHeight / 2);
				DrawUtils.drawSpriteLine(batch, crossLine, scale, mapIntWidth / 2, mapIntHeight / 2 - crossSize, mapIntWidth / 2,
						mapIntHeight / 2 + crossSize);
			}
		}
		if (true) return;

		str = debugString;
		Fonts.getNormal().draw(batch, str, 20, 120);

		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.getNormal().draw(batch, str, 20, 100);

		str = String.valueOf(aktZoom) + " - camzoom: " + Math.round(camera.zoom * 100) / 100;
		Fonts.getNormal().draw(batch, str, 20, 80);

		str = "lTiles: " + mapTileLoader.LoadedTilesSize() + " - qTiles: " + mapTileLoader.QueuedTilesSize();
		Fonts.getNormal().draw(batch, str, 20, 60);

		str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
		Fonts.getNormal().draw(batch, str, 20, 20);

	}

	private void renderPositionMarker(SpriteBatch batch)
	{
		PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, Locator.getLongitude()),
				Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, Locator.getLatitude()), MapTileLoader.MAX_MAP_ZOOM,
				MapTileLoader.MAX_MAP_ZOOM);

		Vector2 vPoint = new Vector2((float) point.X, -(float) point.Y);

		myPointOnScreen = worldToScreen(vPoint);

		myPointOnScreen.y -= ySpeedVersatz;

		if (showAccuracyCircle)
		{

			if (actAccuracy != Locator.getCoordinate().getAccuracy() || actPixelsPerMeter != pixelsPerMeter)
			{
				actAccuracy = Locator.getCoordinate().getAccuracy();
				actPixelsPerMeter = pixelsPerMeter;

				int radius = (int) (pixelsPerMeter * Locator.getCoordinate().getAccuracy());
				// Logger.LogCat("Accuracy radius " + radius);
				// Logger.LogCat("pixelsPerMeter " + pixelsPerMeter);
				if (radius > 0 && radius < UI_Size_Base.that.getSmallestWidth())
				{

					try
					{
						int squaredR = radius * 2;

						if (squaredR > SpriteCache.Accuracy[2].getWidth()) AccuracySprite = new Sprite(SpriteCache.Accuracy[2]);
						else if (squaredR > SpriteCache.Accuracy[1].getWidth()) AccuracySprite = new Sprite(SpriteCache.Accuracy[1]);
						else
							AccuracySprite = new Sprite(SpriteCache.Accuracy[0]);
						if (AccuracySprite != null) AccuracySprite.setSize(squaredR, squaredR);
					}
					catch (Exception e)
					{
						Logger.Error("MapView.renderPositionMarker()", "set AccuracySprite", e);
					}

				}

			}

			if (AccuracySprite != null && AccuracySprite.getWidth() > GL_UISizes.PosMarkerSize)
			{// nur wenn berechnet wurde und grösser als der PosMarker

				float center = AccuracySprite.getWidth() / 2;

				AccuracySprite.setPosition(myPointOnScreen.x - center, myPointOnScreen.y - center);
				AccuracySprite.draw(batch);
			}
		}

		boolean lastUsedCompass = Locator.UseMagneticCompass();
		boolean Transparency = Config.settings.PositionMarkerTransparent.getValue();
		// int arrowId = lastUsedCompass ? (Transparency ? 2 : 0) :
		// (Transparency ? 3 : 1);
		int arrowId = 0;
		if (lastUsedCompass)
		{
			arrowId = Transparency ? 1 : 0;
		}
		else
		{
			arrowId = Transparency ? 3 : 2;
		}

		if (CarMode) arrowId = 15;

		Sprite arrow = SpriteCache.Arrows.get(arrowId);
		arrow.setRotation(-arrowHeading);
		arrow.setBounds(myPointOnScreen.x - GL_UISizes.halfPosMarkerSize, myPointOnScreen.y - GL_UISizes.halfPosMarkerSize,
				GL_UISizes.PosMarkerSize, GL_UISizes.PosMarkerSize);
		arrow.setOrigin(GL_UISizes.halfPosMarkerSize, GL_UISizes.halfPosMarkerSize);
		arrow.draw(batch);

	}

	private Sprite AccuracySprite;
	private int actAccuracy = 0;
	private float actPixelsPerMeter = 0;

	private CB_RectF TargetArrowScreenRec;

	private void RenderTargetArrow(SpriteBatch batch)
	{

		if (GlobalCore.getSelectedCache() == null) return;

		Coordinate coord = (GlobalCore.getSelectedWaypoint() != null) ? GlobalCore.getSelectedWaypoint().Pos : GlobalCore
				.getSelectedCache().Pos;

		float x = (float) (256.0 * Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, coord.getLongitude()));
		float y = (float) (-256.0 * Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, coord.getLatitude()));

		float halfHeight = (mapIntHeight / 2) - ySpeedVersatz;
		float halfWidth = mapIntWidth / 2;

		// create ScreenRec

		if (TargetArrowScreenRec == null)
		{
			TargetArrowScreenRec = new CB_RectF(0, 0, mapIntWidth, mapIntHeight);
			if (!CompassMode)
			{
				TargetArrowScreenRec.ScaleCenter(0.9f);
				TargetArrowScreenRec.setHeight(TargetArrowScreenRec.getHeight() - (TargetArrowScreenRec.getHeight() - info.getY())
						- zoomBtn.getHeight());
				TargetArrowScreenRec.setY(zoomBtn.getMaxY());
			}

		}

		Vector2 ScreenCenter = new Vector2(halfWidth, halfHeight);

		Vector2 screen = worldToScreen(new Vector2(x, y));
		Vector2 target = new Vector2(screen.x, screen.y);

		try
		{
			Vector2 newTarget = TargetArrowScreenRec.getIntersection(ScreenCenter, target);

			// Rotation berechnen
			if (newTarget != null)
			{

				float direction = get_angle(ScreenCenter.x, ScreenCenter.y, newTarget.x, newTarget.y);
				direction = 180 - direction;

				// draw sprite
				Sprite arrow = SpriteCache.Arrows.get(4);
				arrow.setRotation(direction);

				arrow.setBounds(newTarget.x - GL_UISizes.TargetArrow.halfWidth, newTarget.y - GL_UISizes.TargetArrow.height,
						GL_UISizes.TargetArrow.width, GL_UISizes.TargetArrow.height);

				arrow.setOrigin(GL_UISizes.TargetArrow.halfWidth, GL_UISizes.TargetArrow.height);
				arrow.draw(batch);

				Rectangle bound = arrow.getBoundingRectangle();

				TargetArrow = new CB_RectF(bound.x, bound.y, bound.width, bound.height);

			}
			else
			{
				TargetArrow = null;
			}
		}
		catch (Exception e)
		{
			TargetArrow = null;
		}
	}

	/**
	 * Rechteck vom Target-Pfeil zur onClick bestimmung
	 */
	private CB_RectF TargetArrow = null;

	float get_angle(float x1, float y1, float x2, float y2)
	{
		float opp;
		float adj;
		float ang1;

		// calculate vector differences
		opp = y1 - y2;
		adj = x1 - x2;

		if (x1 == x2 && y1 == y2) return (-1);

		// trig function to calculate angle
		if (adj == 0) // to catch vertical co-ord to prevent division by 0
		{
			if (opp >= 0)
			{
				return (0);
			}
			else
			{
				return (180);
			}
		}
		else
		{
			ang1 = (float) ((Math.atan(opp / adj)) * 180 / Math.PI);
			// the angle calculated will range from +90 degrees to -90 degrees
			// so the angle needs to be adjusted if point x1 is less or greater then x2
			if (x1 >= x2)
			{
				ang1 = 90 - ang1;
			}
			else
			{
				ang1 = 270 - ang1;
			}
		}
		return (ang1);
	}

	private void renderWPs(SizeF wpUnderlay, SizeF wpSize, SpriteBatch batch)
	{

		if (mapCacheList.list != null)
		{
			synchronized (mapCacheList.list)
			{
				for (WaypointRenderInfo wpi : mapCacheList.list)
				{
					if (wpi.Selected)
					{
						// wenn der Wp selectiert ist, dann immer in der größten Darstellung
						renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
					}
					else if (CarMode)
					{
						// wenn CarMode dann immer in der größten Darstellung
						renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
					}
					else
					{
						renderWPI(batch, wpUnderlay, wpSize, wpi);
					}
				}
			}
		}
		outScreenDraw = 0;
	}

	private Sprite LineSprite, PointSprite;
	private float scale;

	int outScreenDraw = 0;

	private void renderWPI(SpriteBatch batch, SizeF WpUnderlay, SizeF WpSize, WaypointRenderInfo wpi)
	{
		Vector2 screen = worldToScreen(new Vector2(wpi.MapX, wpi.MapY));

		screen.y -= ySpeedVersatz;

		if (myPointOnScreen != null && showDirektLine && (wpi.Selected) && (wpi.Waypoint == GlobalCore.getSelectedWaypoint()))
		{
			if (LineSprite == null || PointSprite == null)
			{
				LineSprite = SpriteCache.Arrows.get(13);
				PointSprite = SpriteCache.Arrows.get(14);
				scale = 0.8f * UI_Size_Base.that.getScale();
			}

			LineSprite.setColor(Color.RED);
			PointSprite.setColor(Color.RED);

			DrawUtils.drawSpriteLine(batch, LineSprite, PointSprite, scale, myPointOnScreen.x, myPointOnScreen.y, screen.x, screen.y);

		}

		// Don't render if outside of screen !!
		if (screen.x < 0 - WpSize.width || screen.x > this.width + WpSize.height) return;
		if (screen.y < 0 - WpSize.height || screen.y > this.height + WpSize.height) return;

		float NameYMovement = 0;

		if ((aktZoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == GlobalCore.getSelectedWaypoint()))
		{
			// Draw Cross and move screen vector
			Sprite cross = SpriteCache.MapOverlay.get(3);
			cross.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
			cross.draw(batch);

			screen.add(-WpUnderlay.width, WpUnderlay.height);
			NameYMovement = WpUnderlay.height;
		}

		if (wpi.UnderlayIcon != null)
		{
			wpi.UnderlayIcon.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width,
					WpUnderlay.height);
			wpi.UnderlayIcon.draw(batch);
		}
		if (wpi.Icon != null)
		{
			wpi.Icon.setBounds(screen.x - WpSize.halfWidth, screen.y - WpSize.halfHeight, WpSize.width, WpSize.height);
			wpi.Icon.draw(batch);
		}

		if (wpi.OverlayIcon != null)
		{
			wpi.OverlayIcon.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width,
					WpUnderlay.height);
			wpi.OverlayIcon.draw(batch);
		}

		boolean drawAsWaypoint = wpi.Waypoint != null;

		// Rating des Caches darstellen
		if (showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (aktZoom >= 15))
		{
			Sprite rating = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Rating * 2, 5 * 2));
			rating.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight - WpUnderlay.Height4_8, WpUnderlay.width,
					WpUnderlay.Height4_8);
			rating.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			rating.setRotation(0);
			rating.draw(batch);
			NameYMovement += WpUnderlay.Height4_8;
		}

		// Beschriftung
		if (showTitles && (aktZoom >= 15))
		{
			String Name = drawAsWaypoint ? wpi.Waypoint.Title : wpi.Cache.Name;

			float halfWidth = Fonts.getNormal().getBounds(Name).width / 2;
			Fonts.getNormal().draw(batch, Name, screen.x - halfWidth, screen.y - WpUnderlay.halfHeight - NameYMovement);
		}

		// Show D/T-Rating
		if (showDT && (!drawAsWaypoint) && (aktZoom >= 15))
		{
			Sprite difficulty = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Difficulty * 2, 5 * 2));
			difficulty.setBounds(screen.x - WpUnderlay.width - GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2),
					WpUnderlay.width, WpUnderlay.Height4_8);
			difficulty.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			difficulty.setRotation(90);
			difficulty.draw(batch);

			Sprite terrain = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Terrain * 2, 5 * 2));
			terrain.setBounds(screen.x + GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2), WpUnderlay.width,
					WpUnderlay.Height4_8);
			terrain.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			terrain.setRotation(90);
			terrain.draw(batch);

		}

		if ((wpi.Cache.Id == infoBubble.getCacheId()) && infoBubble.isVisible())
		{
			if (infoBubble.getWaypoint() == wpi.Waypoint)
			{
				Vector2 pos = new Vector2(screen.x - infoBubble.getHalfWidth(), screen.y);
				infoBubble.setPos(pos);
			}
		}
	}

	private boolean renderBiggerTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// für den aktuellen Zoom ist kein Tile vorhanden -> kleinere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// von dem gefundenen Tile muß dann nur ein Ausschnitt gezeichnet werden
		int ii = i / 2;
		int jj = j / 2;
		int zoomzoom = zoom2 - 1;

		Descriptor desc = new Descriptor(ii, jj, zoomzoom);
		TileGL tile = null;
		try
		{
			tile = mapTileLoader.getLoadedTile(desc);
		}
		catch (Exception ex)
		{
		}
		if (tile != null)
		{
			// das Alter der benutzten Tiles nicht auf 0 setzen, da dies
			// eigentlich nicht das richtige Tile ist!!!
			// tile.Age = 0;
			try
			{
				if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(), tile);
			}
			catch (Exception e)
			{
			}
			return true;
		}
		else if ((zoomzoom >= aktZoom - 3) && (zoomzoom >= zoomBtn.getMinZoom()))
		{
			// für den aktuellen Zoom ist kein Tile vorhanden -> größere
			// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
			// sind...
			// dafür müssen aber pro fehlendem Tile mehrere kleine Tiles
			// gezeichnet werden (4 oder 16 oder 64...)
			// dieser Aufruf kann auch rekursiv sein...
			renderBiggerTiles(batch, ii, jj, zoomzoom);
		}
		return false;
	}

	private boolean renderBiggerOverlayTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// für den aktuellen Zoom ist kein Tile vorhanden -> kleinere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// von dem gefundenen Tile muß dann nur ein Ausschnitt gezeichnet werden
		int ii = i / 2;
		int jj = j / 2;
		int zoomzoom = zoom2 - 1;

		Descriptor desc = new Descriptor(ii, jj, zoomzoom);
		TileGL tile = null;
		try
		{
			tile = mapTileLoader.getLoadedOverlayTile(desc);
		}
		catch (Exception ex)
		{
		}
		if (tile != null)
		{
			// das Alter der benutzten Tiles nicht auf 0 setzen, da dies
			// eigentlich nicht das richtige Tile ist!!!
			// tile.Age = 0;
			try
			{
				if (!overlayToDraw.containsKey(tile.Descriptor.GetHashCode())) overlayToDraw.put(tile.Descriptor.GetHashCode(), tile);
			}
			catch (Exception e)
			{
			}
			return true;
		}
		else if ((zoomzoom >= aktZoom - 3) && (zoomzoom >= zoomBtn.getMinZoom()))
		{
			// für den aktuellen Zoom ist kein Tile vorhanden -> größere
			// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
			// sind...
			// dafür müssen aber pro fehlendem Tile mehrere kleine Tiles
			// gezeichnet werden (4 oder 16 oder 64...)
			// dieser Aufruf kann auch rekursiv sein...
			renderBiggerOverlayTiles(batch, ii, jj, zoomzoom);
		}
		return false;
	}

	private void renderSmallerTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// für den aktuellen Zoom ist kein Tile vorhanden -> größere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// dafür müssen aber pro fehlendem Tile mehrere kleine Tiles gezeichnet
		// werden (4 oder 16 oder 64...)
		int i1 = i * 2;
		int i2 = i * 2 + 1;
		int j1 = j * 2;
		int j2 = j * 2 + 1;
		int zoomzoom = zoom2 + 1;
		for (int ii = i1; ii <= i2; ii++)
		{
			for (int jj = j1; jj <= j2; jj++)
			{
				Descriptor desc = new Descriptor(ii, jj, zoomzoom);
				TileGL tile = null;
				try
				{
					tile = mapTileLoader.getLoadedTile(desc);
				}
				catch (Exception ex)
				{
				}
				if (tile != null)
				{
					try
					{
						if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(), tile);
					}
					catch (Exception e)
					{
					}
					// das Alter der benutzten Tiles nicht auf 0 setzen, da dies
					// eigentlich nicht das richtige Tile ist!!!
					// tile.Age = 0;
				}
				else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MapTileLoader.MAX_MAP_ZOOM))
				{
					// für den aktuellen Zoom ist kein Tile vorhanden -> größere
					// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
					// sind...
					// dafür müssen aber pro fehlendem Tile mehrere kleine Tiles
					// gezeichnet werden (4 oder 16 oder 64...)
					// dieser Aufruf kann auch rekursiv sein...
					renderSmallerTiles(batch, ii, jj, zoomzoom);
				}
			}
		}
	}

	private void renderSmallerOverlayTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// für den aktuellen Zoom ist kein Tile vorhanden -> größere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// dafür müssen aber pro fehlendem Tile mehrere kleine Tiles gezeichnet
		// werden (4 oder 16 oder 64...)
		int i1 = i * 2;
		int i2 = i * 2 + 1;
		int j1 = j * 2;
		int j2 = j * 2 + 1;
		int zoomzoom = zoom2 + 1;
		for (int ii = i1; ii <= i2; ii++)
		{
			for (int jj = j1; jj <= j2; jj++)
			{
				Descriptor desc = new Descriptor(ii, jj, zoomzoom);
				TileGL tile = null;
				try
				{
					tile = mapTileLoader.getLoadedOverlayTile(desc);
				}
				catch (Exception ex)
				{
				}
				if (tile != null)
				{
					try
					{
						if (!overlayToDraw.containsKey(tile.Descriptor.GetHashCode())) overlayToDraw.put(tile.Descriptor.GetHashCode(),
								tile);
					}
					catch (Exception e)
					{
					}
					// das Alter der benutzten Tiles nicht auf 0 setzen, da dies
					// eigentlich nicht das richtige Tile ist!!!
					// tile.Age = 0;
				}
				else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MapTileLoader.MAX_MAP_ZOOM))
				{
					// für den aktuellen Zoom ist kein Tile vorhanden -> größere
					// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
					// sind...
					// dafür müssen aber pro fehlendem Tile mehrere kleine Tiles
					// gezeichnet werden (4 oder 16 oder 64...)
					// dieser Aufruf kann auch rekursiv sein...
					renderSmallerOverlayTiles(batch, ii, jj, zoomzoom);
				}
			}
		}
	}

	private void loadTiles()
	{
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(
				mapIntWidth, mapIntHeight)), aktZoom, false);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);

		// alle notwendigen Tiles zum Laden einstellen in die Queue
		// (queuedTiles)

		int halfMapIntWidth = mapIntWidth / 2;
		int halfMapIntHeight = mapIntHeight / 2;

		int extensionTop = (int) (halfMapIntHeight - ySpeedVersatz);
		int extensionBottom = (int) (halfMapIntHeight + ySpeedVersatz);
		int extensionLeft = halfMapIntWidth;
		int extensionRight = halfMapIntWidth;
		Descriptor lo = screenToDescriptor(new Vector2(halfMapIntWidth - drawingWidth / 2 - extensionLeft, halfMapIntHeight - drawingHeight
				/ 2 - extensionTop), aktZoom);
		Descriptor ru = screenToDescriptor(new Vector2(halfMapIntWidth + drawingWidth / 2 + extensionRight, halfMapIntHeight
				+ drawingHeight / 2 + extensionBottom), aktZoom);

		mapTileLoader.loadTiles(this, lo, ru, aktZoom);

	}

	public void InitializeMap()
	{
		zoomCross = Config.settings.ZoomCross.getValue();
		zoomBtn.setZoom(Config.settings.lastZoomLevel.getValue());
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
				// setLockPosition(0);
			}
			else
			{
				// GPS-Position bekannt?
				if (Locator.Valid())
				{
					setCenter(Locator.getCoordinate());
					positionInitialized = true;
				}
				else
				{
					try
					{
						if (Database.Data != null)
						{

							if (Database.Data.Query != null)
							{
								synchronized (Database.Data.Query)
								{
									if (Database.Data.Query.size() > 0)
									{
										// Koordinaten des ersten Caches der Datenbank
										// nehmen
										setCenter(new Coordinate(Database.Data.Query.get(0).Pos.getLatitude(),
												Database.Data.Query.get(0).Pos.getLongitude()));
										positionInitialized = true;
										// setLockPosition(0);
									}
									else
									{
										// Wenns auch den nicht gibt...)
										setCenter(new Coordinate(48.0, 12.0));
									}
								}
							}
							else
							{
								// Wenn Query == null
								setCenter(new Coordinate(48.0, 12.0));
							}
						}
						else
						{
							// Wenn Data == null
							setCenter(new Coordinate(48.0, 12.0));
						}
					}
					catch (Exception e)
					{
						setCenter(new Coordinate(48.0, 12.0));
					}

				}
			}
		}

		setNewSettings(INITIAL_ALL);

	}

	public static int INITIAL_NEW_SETTINGS = 3;
	public static int INITIAL_SETTINGS = 1;
	public static int INITIAL_THEME = 2;
	public static int INITIAL_WP_LIST = 4;
	public static int INITIAL_ALL = 7;
	public static int INITIAL_WITH_OUT_ZOOM = 8;
	public static int INITIAL_SETTINGS_WITH_OUT_ZOOM = 9;

	public void setNewSettings(int InitialFlags)
	{
		if ((InitialFlags & INITIAL_SETTINGS) != 0)
		{
			showRating = CompassMode ? false : Config.settings.MapShowRating.getValue();
			showDT = CompassMode ? false : Config.settings.MapShowDT.getValue();
			showTitles = CompassMode ? false : Config.settings.MapShowTitles.getValue();
			hideMyFinds = Config.settings.MapHideMyFinds.getValue();
			showCompass = CompassMode ? false : Config.settings.MapShowCompass.getValue();
			showDirektLine = CompassMode ? false : Config.settings.ShowDirektLine.getValue();
			showAllWaypoints = CompassMode ? false : Config.settings.ShowAllWaypoints.getValue();
			showAccuracyCircle = CompassMode ? false : Config.settings.ShowAccuracyCircle.getValue();
			showMapCenterCross = CompassMode ? false : Config.settings.ShowMapCenterCross.getValue();

			if (info != null) info.setVisible(showCompass);

			if (InitialFlags == INITIAL_ALL)
			{
				iconFactor = (float) Config.settings.MapViewDPIFaktor.getValue();

				int setAktZoom = CompassMode ? Config.settings.lastZoomLevel.getValue() : Config.settings.lastZoomLevel.getValue();
				int setMaxZoom = CompassMode ? Config.settings.CompassMapMaxZommLevel.getValue() : Config.settings.OsmMaxLevel.getValue();
				int setMinZoom = CompassMode ? Config.settings.CompassMapMinZoomLevel.getValue() : Config.settings.OsmMinLevel.getValue();

				aktZoom = setAktZoom;
				zoomBtn.setMaxZoom(setMaxZoom);
				zoomBtn.setMinZoom(setMinZoom);
				zoomBtn.setZoom(aktZoom);

				zoomScale.setMaxZoom(setMaxZoom);
				zoomScale.setMinZoom(setMinZoom);

				if (CompassMode)
				{
					// Berechne die darstellbare Entfernung für jedes ZoomLevel
					DistanceZoomLevel = new TreeMap<Integer, Integer>();

					int posiblePixel = (int) this.halfHeight;

					for (int i = setMaxZoom; i > setMinZoom; i--)
					{
						float PixelForZoomLevel = getPixelsPerMeter(i);
						DistanceZoomLevel.put(i, (int) (posiblePixel / PixelForZoomLevel));
					}
				}

			}
		}

		if ((InitialFlags & INITIAL_THEME) != 0)
		{
			String themePath = null;

			boolean useInvertNightTheme = false;

			// Zustand der Karte CarMode/NormalMode
			if (CarMode)
			{
				if (Config.settings.nightMode.getValue())
				{
					// zuerst schauen, ob ein Render Theme im Custom Skin Ordner Liegt
					themePath = ifCarThemeExist(GlobalCore.PathCustomNight);

					if (themePath == null)
					{// wenn kein Night Custum skin vorhanden, dann nach einem Day CostumTheme suchen, welches dann Invertiert wird!
						themePath = ifCarThemeExist(GlobalCore.PathCustom);
						if (themePath != null) useInvertNightTheme = true;
					}

					if (themePath == null)
					{// wenn kein Night Default skin vorhanden, dann nach einem Day DayTheme suchen, welches dann Invertiert wird!
						themePath = ManagerBase.INTERNAL_CAR_THEME;
						useInvertNightTheme = true;
					}

				}
				else
				{
					// zuerst schauen, ob ein Render Theme im Custom Skin Ordner Liegt
					themePath = ifCarThemeExist(GlobalCore.PathCustom);

					if (themePath == null) themePath = ifCarThemeExist(GlobalCore.PathDefault);
				}

				if (themePath == null)
				{
					themePath = ManagerBase.INTERNAL_CAR_THEME;
				}

			}

			if (themePath == null)
			{

				// Entweder wir sind nicht im CarMode oder es wurde kein Passender Theme für den CarMode gefunden!
				if (Config.settings.nightMode.getValue())
				{
					themePath = ifThemeExist(Config.settings.MapsforgeNightTheme.getValue());
				}

				if (themePath == null)
				{
					if (Config.settings.nightMode.getValue()) useInvertNightTheme = true;
					themePath = ifThemeExist(Config.settings.MapsforgeDayTheme.getValue());

				}

			}

			if (themePath != null)
			{
				if (CompassMode)
				{
					if (!ManagerBase.Manager.isRenderThemeSetted())
					{
						ManagerBase.Manager.setUseInvertedNightTheme(useInvertNightTheme);
						ManagerBase.Manager.setRenderTheme(themePath);
					}
				}
				else
				{
					ManagerBase.Manager.setUseInvertedNightTheme(useInvertNightTheme);
					ManagerBase.Manager.setRenderTheme(themePath);
				}

			}
			else
			{
				// set Theme to null
				ManagerBase.Manager.setUseInvertedNightTheme(useInvertNightTheme);
				ManagerBase.Manager.setRenderTheme(null);
			}

		}

		if ((InitialFlags & INITIAL_WP_LIST) != 0)
		{
			if (mapCacheList != null)
			{
				MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)),
						screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
				data.hideMyFinds = this.hideMyFinds;
				data.showAllWaypoints = this.showAllWaypoints;
				mapCacheList.update(data);
			}

		}

	}

	private String ifCarThemeExist(String Path)
	{
		Path = Path + "CarTheme.xml";
		return ifThemeExist(Path);
	}

	private String ifThemeExist(String Path)
	{
		if (FileIO.FileExists(Path) && FileIO.GetFileExtension(Path).contains("xml")) return Path;
		return null;
	}

	private void setScreenCenter(Vector2 newCenter)
	{
		synchronized (screenCenterT)
		{
			screenCenterT.x = (long) newCenter.x;
			screenCenterT.y = (long) (-newCenter.y);
		}
		// if (camera != null) camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);
		GL.that.renderOnce(this.getName() + " setScreenCenter");
	}

	public void setCenter(Coordinate value)
	{
		synchronized (screenCenterW)
		{

			if (center == null) center = new Coordinate(48.0, 12.0);
			positionInitialized = true;
			/*
			 * if (animationTimer != null) animationTimer.Enabled = false;
			 */
			if (center == value) return;

			center = value;
			info.setCoord(center);
			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, center.getLongitude()),
					Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, center.getLatitude()), MapTileLoader.MAX_MAP_ZOOM,
					MapTileLoader.MAX_MAP_ZOOM);

			setScreenCenter(new Vector2((float) point.X, (float) point.Y));
		}
	}

	/**
	 * liefert die World-Koordinate in Pixel relativ zur Map in der höchsten Auflösung
	 */
	private Vector2 screenToWorld(Vector2 point)
	{
		Vector2 result = new Vector2(0, 0);
		try
		{
			synchronized (screenCenterW)
			{
				result.x = screenCenterW.x + ((long) point.x - mapIntWidth / 2) * camera.zoom;
				result.y = -screenCenterW.y + ((long) point.y - mapIntHeight / 2) * camera.zoom;
			}
		}
		catch (Exception e)
		{
			// wenn hier ein Fehler auftritt, dann geben wir einen Vector 0,0 zurück!
		}
		return result;
	}

	public Vector2 worldToScreen(Vector2 point)
	{
		synchronized (screenCenterW)
		{
			Vector2 result = new Vector2(0, 0);
			result.x = ((long) point.x - screenCenterW.x) / camera.zoom + (float) mapIntWidth / 2;
			result.y = -(-(long) point.y + screenCenterW.y) / camera.zoom + (float) mapIntHeight / 2;
			result.add(-(float) mapIntWidth / 2, -(float) mapIntHeight / 2);
			result.rotate(mapHeading);
			result.add((float) mapIntWidth / 2, (float) mapIntHeight / 2);
			return result;
		}
	}

	private Descriptor screenToDescriptor(Vector2 point, int zoom)
	{
		// World-Koordinaten in Pixel
		Vector2 world = screenToWorld(point);
		for (int i = MapTileLoader.MAX_MAP_ZOOM; i > zoom; i--)
		{
			world.x /= 2;
			world.y /= 2;
		}
		world.x /= 256;
		world.y /= 256;
		int x = (int) world.x;
		int y = (int) world.y;
		Descriptor result = new Descriptor(x, y, zoom);
		return result;
	}

	@Override
	public void PositionChanged()
	{

		if (CarMode)
		{
			// im CarMode keine Netzwerk Koordinaten zulassen
			if (!Locator.isGPSprovided()) return;
		}

		if (info != null)
		{
			if (center != null)
			{
				info.setCoord(center);
			}

			if (GlobalCore.getSelectedCoord() != null)
			{
				info.setDistance(GlobalCore.getSelectedCoord().Distance());
			}
			// Logger.DEBUG("Map.SetDistance=" + GlobalCore.getSelectedCoord().Distance());
		}

		if (togBtn.getState() > 0 && togBtn.getState() != 2) setCenter(Locator.getCoordinate());

		if (CompassMode)
		{
			// Berechne den Zoom so, dass eigene Position und WP auf der Map zu sehen sind.
			Coordinate position = null;
			// if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
			position = Locator.getCoordinate();

			float distance = -1;
			if (GlobalCore.getSelectedCache() != null && position.isValid())
			{
				if (GlobalCore.getSelectedWaypoint() == null) distance = position.Distance(GlobalCore.getSelectedCache().Pos);
				else
					distance = position.Distance(GlobalCore.getSelectedWaypoint().Pos);
			}
			int setZoomTo = zoomBtn.getMinZoom();

			if (DistanceZoomLevel != null)
			{
				for (int i = zoomBtn.getMaxZoom(); i > zoomBtn.getMinZoom(); i--)
				{
					if (distance < DistanceZoomLevel.get(i))
					{
						setZoomTo = i;
						break;
					}
				}
			}

			if (setZoomTo != lastCompassMapZoom)
			{
				lastCompassMapZoom = setZoomTo;
				zoomBtn.setZoom(setZoomTo);
				inputState = InputState.Idle;

				kineticZoom = new KineticZoom(camera.zoom, mapTileLoader.getMapTilePosFactor(setZoomTo), System.currentTimeMillis(),
						System.currentTimeMillis() + ZoomTime);

				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce(MapView.this.getName() + " ZoomButtonClick");
				calcPixelsPerMeter();
			}

		}
		GL.that.renderOnce(MapView.this.getName() + " Position Changed");
	}

	private int lastCompassMapZoom = -1;
	private float lastDynamicZoom = -1;

	@Override
	public void OrientationChanged()
	{

		float heading = Locator.getHeading();

		// im CarMode keine Richtungs Änderungen unter 20kmh
		if (CarMode && Locator.SpeedOverGround() < 20) heading = this.mapHeading;

		if (alignToCompass || CarMode)
		{
			this.mapHeading = heading;
			this.arrowHeading = 0;

			// da die Map gedreht in die offScreenBmp gezeichnet werden soll,
			// muss der Bereich, der gezeichnet werden soll größer sein, wenn
			// gedreht wird.
			if (heading >= 180) heading -= 180;
			if (heading > 90) heading = 180 - heading;
			double alpha = heading / 180 * Math.PI;
			double mapHeightCalcBase = mapIntHeight + (ySpeedVersatz * 1.7);
			double mapWidthCalcBase = mapIntWidth + (ySpeedVersatz * 1.7);
			double beta = Math.atan(mapWidthCalcBase / mapHeightCalcBase);
			double gammaW = Math.PI / 2 - alpha - beta;
			// halbe Länge der Diagonalen
			double diagonal = Math.sqrt(Math.pow(mapWidthCalcBase, 2) + Math.pow(mapHeightCalcBase, 2)) / 2;
			drawingWidth = (int) (Math.cos(gammaW) * diagonal * 2);

			double gammaH = alpha - beta;
			drawingHeight = (int) (Math.cos(gammaH) * diagonal * 2);
		}
		else
		{
			this.mapHeading = 0;
			this.arrowHeading = heading;
			drawingWidth = mapIntWidth;
			drawingHeight = mapIntHeight;
		}

		if (info != null)
		{
			Coordinate position = null;
			// if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
			position = Locator.getCoordinate();

			if (GlobalCore.getSelectedCache() != null)
			{
				Coordinate cache = (GlobalCore.getSelectedWaypoint() != null) ? GlobalCore.getSelectedWaypoint().Pos : GlobalCore
						.getSelectedCache().Pos;
				double bearing = Coordinate.Bearing(position.getLatitude(), position.getLongitude(), cache.getLatitude(),
						cache.getLongitude());
				info.setBearing((float) (bearing - Locator.getHeading() - arrowHeading), Locator.getHeading());
			}
		}
		GL.that.renderOnce(MapView.this.getName() + " OrientationChanged");
	}

	public void SetAlignToCompass(boolean value)
	{
		alignToCompass = value;
		if (!value)
		{
			drawingWidth = mapIntWidth;
			drawingHeight = mapIntHeight;
		}

		Config.settings.MapNorthOriented.setValue(!alignToCompass);
		Config.AcceptChanges();

	}

	public boolean GetCenterGps()
	{
		return (togBtn.getState() > 0 && togBtn.getState() != 2);
	}

	public boolean GetAlignToCompass()
	{
		return alignToCompass;
	}

	// InputProcessor
	public enum InputState
	{
		Idle, IdleDown, Button, Pan, Zoom, PanAutomatic, ZoomAutomatic
	}

	private InputState inputState = InputState.Idle;
	// speicher, welche Finger-Pointer aktuell gedrückt sind
	private HashMap<Integer, Point> fingerDown = new LinkedHashMap<Integer, Point>();

	private static String debugString = "";

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN)
		{
			lastTouchPos = new Vector2(x, y);
			return true;
		}

		y = mapIntHeight - y;
		// debugString = "touchDown " + x + " - " + y;
		if (inputState == InputState.Idle)
		{
			fingerDown.clear();
			inputState = InputState.IdleDown;
			fingerDown.put(pointer, new Point(x, y));
		}
		else
		{
			fingerDown.put(pointer, new Point(x, y));
			if (fingerDown.size() == 2) inputState = InputState.Zoom;
		}

		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN)
		{
			// Mouse wheel scrolling => Zoom in/out

			if (lastDynamicZoom == -1) lastDynamicZoom = zoomBtn.getZoom();

			float div = lastTouchPos.x - x;

			float zoomValue = div / 100f;

			int maxZoom = Config.settings.OsmMaxLevel.getValue();
			int minZoom = Config.settings.OsmMinLevel.getValue();
			float dynZoom = (lastDynamicZoom - zoomValue);

			if (dynZoom > maxZoom) dynZoom = maxZoom;
			if (dynZoom < minZoom) dynZoom = minZoom;

			if (lastDynamicZoom != dynZoom)
			{

				// Logger.LogCat("Mouse Zoom:" + div + "/" + zoomValue + "/" + dynZoom);

				lastDynamicZoom = dynZoom;
				zoomBtn.setZoom((int) lastDynamicZoom);
				inputState = InputState.Idle;

				kineticZoom = new KineticZoom(camera.zoom, mapTileLoader.getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(),
						System.currentTimeMillis() + ZoomTime);

				// kineticZoom = new KineticZoom(camera.zoom, lastDynamicZoom, System.currentTimeMillis(), System.currentTimeMillis() +
				// 1000);

				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce(MapView.this.getName() + " ZoomButtonClick");
				calcPixelsPerMeter();
			}

			return true;
		}

		try
		{
			y = mapIntHeight - y;
			// debugString = "touchDragged: " + x + " - " + y;
			// debugString = "touchDragged " + inputState.toString();
			if (inputState == InputState.IdleDown)
			{
				// es wurde 1x gedrückt -> testen, ob ein gewisser Minimum Bereich verschoben wurde
				Point p = fingerDown.get(pointer);
				if (p != null)
				{
					// if ((Math.abs(p.x - x) > 10) || (Math.abs(p.y - y) > 10)) // this check is not necessary because this is already
					// checked in GL.java
					{
						inputState = InputState.Pan;
						// GL_Listener.glListener.addRenderView(this, frameRateAction);
						GL.that.renderOnce(this.getName() + " Dragged");
						// xxx startTimer(frameRateAction);
						// xxx ((GLSurfaceView) MapViewGL.ViewGl).requestRender();
					}
					return false;
				}
			}
			if (inputState == InputState.Button)
			{
				// wenn ein Button gedrückt war -> beim Verschieben nichts machen!!!
				return false;
			}

			if ((inputState == InputState.Pan) && (fingerDown.size() == 1))
			{

				if (togBtn.getState() > 1 && togBtn.getState() != 2)// für verschieben gesperrt!
				{
					return false;
				}
				else if (togBtn.getState() == 1 || togBtn.getState() == 2)// auf GPS oder WP ausgerichtet und wird jetzt auf Free gestellt
				{
					togBtn.setState(0);
				}

				// Fadein ZoomButtons!
				zoomBtn.resetFadeOut();

				// GL_Listener.glListener.addRenderView(this, frameRateAction);
				GL.that.renderOnce(this.getName() + " Pan");
				// debugString = "";
				long faktor = mapTileLoader.getMapTilePosFactor(aktZoom);
				// debugString += faktor;
				Point lastPoint = (Point) fingerDown.values().toArray()[0];
				// debugString += " - " + (lastPoint.x - x) * faktor + " - " + (y - lastPoint.y) * faktor;

				// camera.position.add((lastPoint.x - x) * faktor, (y - lastPoint.y) * faktor, 0);
				// screenCenterW.x = camera.position.x;
				// screenCenterW.y = camera.position.y;
				synchronized (screenCenterT)
				{
					double angle = mapHeading * Math.PI / 180;
					int dx = (lastPoint.x - x);
					int dy = (y - lastPoint.y);
					int dxr = (int) (Math.cos(angle) * dx + Math.sin(angle) * dy);
					int dyr = (int) (-Math.sin(angle) * dx + Math.cos(angle) * dy);
					debugString = dx + " - " + dy + " - " + dxr + " - " + dyr;

					// Pan stufenlos anpassen an den aktuell gültigen Zoomfaktor
					float tmpZoom = camera.zoom;
					float ffaktor = 1.5f;
					// ffaktor = ffaktor - iconFactor + 1;
					ffaktor = ffaktor / iconFactor;
					while (tmpZoom > ffaktor)
					{
						tmpZoom /= 2;
					}

					screenCenterT.x += (long) (dxr * faktor * tmpZoom);
					screenCenterT.y += (long) (dyr * faktor * tmpZoom);
				}
				calcCenter();

				// if (kineticPan == null) kineticPan = new KineticPan();
				// kineticPan.setLast(System.currentTimeMillis(), x, y);

				lastPoint.x = x;
				lastPoint.y = y;
			}
			else if ((inputState == InputState.Zoom) && (fingerDown.size() == 2))
			{
				Point p1 = (Point) fingerDown.values().toArray()[0];
				Point p2 = (Point) fingerDown.values().toArray()[1];
				float originalDistance = (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

				if (fingerDown.containsKey(pointer))
				{
					// neue Werte setzen
					fingerDown.get(pointer).x = x;
					fingerDown.get(pointer).y = y;
					p1 = (Point) fingerDown.values().toArray()[0];
					p2 = (Point) fingerDown.values().toArray()[1];
				}
				float currentDistance = (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
				float ratio = originalDistance / currentDistance;
				camera.zoom = camera.zoom * ratio;

				if (camera.zoom < mapTileLoader.getMapTilePosFactor(zoomBtn.getMaxZoom()))
				{
					camera.zoom = mapTileLoader.getMapTilePosFactor(zoomBtn.getMaxZoom());
				}
				if (camera.zoom > mapTileLoader.getMapTilePosFactor(zoomBtn.getMinZoom()))
				{
					camera.zoom = mapTileLoader.getMapTilePosFactor(zoomBtn.getMinZoom());
				}

				lastDynamicZoom = camera.zoom;

				int zoom = MapTileLoader.MAX_MAP_ZOOM;
				float tmpZoom = camera.zoom;
				float faktor = 1.5f;
				// faktor = faktor - iconFactor + 1;
				faktor = faktor / iconFactor;
				while (tmpZoom > faktor)
				{
					tmpZoom /= 2;
					zoom--;
				}
				aktZoom = zoom;

				calcPixelsPerMeter();
				mapScale.ZoomChanged();
				zoomBtn.setZoom(aktZoom);

				if (!CarMode && !CompassMode)
				{
					zoomScale.setZoom(mapTileLoader.convertCameraZommToFloat(camera));
					zoomScale.resetFadeOut();
				}

				return false;
			}

			// debugString = "State: " + inputState;
			return true;
		}
		catch (Exception ex)
		{
			Logger.Error("MapView", "-onTouchDragged Error", ex);
		}
		return false;
	}

	private void setZoomScale(int zoom)
	{
		// Logger.LogCat("set zoom");
		if (!CarMode && !CompassMode) zoomScale.setZoom(zoom);
		if (!CompassMode) mapScale.zoomChanged();
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN)
		{
			return true;
		}

		y = mapIntHeight - y;
		debugString = "touchUp: " + x + " - " + y;
		// debugString = "touchUp " + inputState.toString();
		if (inputState == InputState.IdleDown)
		{
			// es wurde gedrückt, aber nich verschoben
			fingerDown.remove(pointer);
			inputState = InputState.Idle;
			// -> Buttons testen

			// auf Button Clicks nur reagieren, wenn aktuell noch kein Finger gedrückt ist!!!
			if (kineticPan != null)
			// bei FingerKlick (wenn Idle) sofort das kinetische Scrollen stoppen
			kineticPan = null;

			inputState = InputState.Idle;
			return false;
		}

		fingerDown.remove(pointer);
		if (fingerDown.size() == 1) inputState = InputState.Pan;
		else if (fingerDown.size() == 0)
		{
			inputState = InputState.Idle;
			// wieder langsam rendern
			GL.that.renderOnce(this.getName() + " touchUp");

			if ((kineticZoom == null) && (kineticPan == null)) GL.that.removeRenderView(this);

			if (kineticPan != null) kineticPan.start();
		}

		// debugString = "State: " + inputState;

		return true;
	}

	private OnClickListener onClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			double minDist = Double.MAX_VALUE;
			WaypointRenderInfo minWpi = null;
			Vector2 clickedAt = new Vector2(x, y);

			if (TargetArrow != null && TargetArrow.contains(x, y))
			{
				if (GlobalCore.getSelectedCache() != null)
				{
					if (GlobalCore.getSelectedCache() != null)
					{
						if (GlobalCore.getSelectedWaypoint() != null)
						{
							Coordinate tmp = GlobalCore.getSelectedWaypoint().Pos;
							setCenter(new Coordinate(tmp.getLatitude(), tmp.getLongitude()));
						}
						else
						{
							Coordinate tmp = GlobalCore.getSelectedCache().Pos;
							setCenter(new Coordinate(tmp.getLatitude(), tmp.getLongitude()));
						}
					}
					return false;
				}
			}

			synchronized (mapCacheList.list)
			{
				if (infoBubble.isVisible())
				{
					// Click outside Bubble -> hide Bubble
					infoBubble.setInvisible();
				}

				for (WaypointRenderInfo wpi : mapCacheList.list)
				{
					Vector2 screen = worldToScreen(new Vector2(Math.round(wpi.MapX), Math.round(wpi.MapY)));
					if (clickedAt != null)
					{
						double aktDist = Math.sqrt(Math.pow(screen.x - clickedAt.x, 2) + Math.pow(screen.y - clickedAt.y, 2));
						if (aktDist < minDist)
						{
							minDist = aktDist;
							minWpi = wpi;
						}
					}
				}

				if (minWpi == null || minWpi.Cache == null) return true;
				// Vector2 screen = worldToScreen(new Vector2(Math.round(minWpi.MapX), Math.round(minWpi.MapY)));
				// Logger.LogCat("MapClick at:" + clickedAt + " minDistance: " + minDist + " screen:" + screen + " wpi:" + minWpi.Cache.Name
				// + "/ ");

				if (minDist < 40)
				{

					if (minWpi.Waypoint != null)
					{
						if (GlobalCore.getSelectedCache() != minWpi.Cache)
						{
							// Show Bubble at the location of the Waypoint!!!
							infoBubble.setCache(minWpi.Cache, minWpi.Waypoint);
							infoBubble.setVisible();
						}
						else
						{
							// do not show Bubble because there will not be
							// selected
							// a
							// different cache but only a different waypoint
							// Wegpunktliste ausrichten
							GlobalCore.setSelectedWaypoint(minWpi.Cache, minWpi.Waypoint);
							// FormMain.WaypointListPanel.AlignSelected();
							// updateCacheList();
							MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)),
									screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
							data.hideMyFinds = MapView.this.hideMyFinds;
							data.showAllWaypoints = MapView.this.showAllWaypoints;
							mapCacheList.update(data);
						}

					}
					else
					{
						// Show Bubble
						// unabhängig davon, ob der angeklickte Cache == der selectedCache ist
						infoBubble.setCache(minWpi.Cache, null);
						infoBubble.setVisible();
					}
					inputState = InputState.Idle;
					// debugString = "State: " + inputState;
					// return false;
				}
			}

			return false;
		}
	};

	private void calcCenter()
	{
		// berechnet anhand des ScreenCenterW die Center-Coordinaten
		PointD point = Descriptor.FromWorld(screenCenterW.x, screenCenterW.y, MapTileLoader.MAX_MAP_ZOOM, MapTileLoader.MAX_MAP_ZOOM);

		center = new Coordinate(Descriptor.TileYToLatitude(MapTileLoader.MAX_MAP_ZOOM, -point.Y), Descriptor.TileXToLongitude(
				MapTileLoader.MAX_MAP_ZOOM, point.X));
		info.setCoord(center);
	}

	public float pixelsPerMeter = 0;

	private void calcPixelsPerMeter()
	{

		float calcZoom = mapTileLoader.convertCameraZommToFloat(camera);

		Coordinate dummy = Coordinate.Project(center.getLatitude(), center.getLongitude(), 90, 1000);
		double l1 = Descriptor.LongitudeToTileX(calcZoom, center.getLongitude());
		double l2 = Descriptor.LongitudeToTileX(calcZoom, dummy.getLongitude());
		double diff = Math.abs(l2 - l1);
		pixelsPerMeter = (float) ((diff * 256) / 1000);

	}

	private float getPixelsPerMeter(int ZoomLevel)
	{
		Coordinate dummy = Coordinate.Project(center.getLatitude(), center.getLongitude(), 90, 1000);
		double l1 = Descriptor.LongitudeToTileX(ZoomLevel, center.getLongitude());
		double l2 = Descriptor.LongitudeToTileX(ZoomLevel, dummy.getLongitude());
		double diff = Math.abs(l2 - l1);
		return (float) ((diff * 256) / 1000);
	}

	// @Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		// xxx if (Global.autoResort) return;
		// Logger.DEBUG("Cache Changed Event");
		if (cache == null) return;
		if ((cache == lastSelectedCache) && (waypoint == lastSelectedWaypoint)) return;
		lastSelectedCache = cache;
		lastSelectedWaypoint = waypoint;
		/*
		 * if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint }); return; }
		 */

		// mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(
				mapIntWidth, mapIntHeight)), aktZoom, true);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);

		// Ich weiß nicht mehr warum dies so drin war, aber es hat verhindert, das am ende der Methode
		// PositionChanged(GlobalCore.Locator); Aufgerufen wurde. Damit hat sich beim wechsel des Caches die Distance nicht Aktualisiert!
		// Deshalb habe ich es erstmal auskommentiert! ( Longri)
		if (togBtn.getState() > 0 && togBtn.getState() != 2)
		{
			PositionChanged();
			return;
		}

		positionInitialized = true;

		if (togBtn.getState() != 2) togBtn.setState(0, true);

		Coordinate target = (waypoint != null) ? new Coordinate(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude()) : new Coordinate(
				cache.Pos.getLatitude(), cache.Pos.getLongitude());

		setCenter(target);

		GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);

		// für 2sec rendern lassen, bis Änderungen der WPI-list neu berechnet wurden
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				GL.that.removeRenderView(MapView.this);
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 2000);
		PositionChanged();
	}

	protected class KineticPan
	{
		private boolean started;
		private boolean fertig;
		// benutze den Abstand der letzten 5 Positionsänderungen
		final int anzPoints = 3;
		private int[] x = new int[anzPoints];
		private int[] y = new int[anzPoints];
		private int diffX;
		private int diffY;
		private long startTs;
		private long endTs;

		public KineticPan()
		{
			fertig = false;
			started = false;
			diffX = 0;
			diffY = 0;
			for (int i = 0; i < anzPoints; i++)
			{
				x[i] = 0;
				y[i] = 0;
			}
		}

		public void setLast(long aktTs, int aktX, int aktY)
		{
			for (int i = anzPoints - 2; i >= 0; i--)
			{
				x[i + 1] = x[i];
				y[i + 1] = y[i];
			}
			x[0] = aktX;
			y[0] = aktY;

			for (int i = 1; i < anzPoints; i++)
			{
				if (x[i] == 0) x[i] = x[i - 1];
				if (y[i] == 0) y[i] = y[i - 1];
			}
			diffX = x[anzPoints - 1] - aktX;
			diffY = aktY - y[anzPoints - 1];

			// debugString = x[2] + " - " + x[1] + " - " + x[0];
		}

		public boolean getFertig()
		{
			return fertig;
		}

		public boolean getStarted()
		{
			return started;
		}

		public void start()
		{
			startTs = System.currentTimeMillis();
			int abstand = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

			endTs = startTs + 2000 + abstand * 50 / anzPoints;
			started = true;
		}

		public Point getAktPan()
		{
			Point result = new Point(0, 0);

			long aktTs = System.currentTimeMillis();
			float faktor = (float) (aktTs - startTs) / (float) (endTs - startTs);
			faktor = com.badlogic.gdx.math.Interpolation.exp10Out.apply(faktor);
			if (faktor >= 1)
			{
				fertig = true;
				faktor = 1;
			}

			result.x = (int) (diffX / anzPoints * (1 - faktor));
			result.y = (int) (diffY / anzPoints * (1 - faktor));
			return result;
		}
	}

	protected class KineticZoom
	{
		private float startZoom;
		private float endZoom;
		private long startTime;
		private long endTime;
		private boolean fertig;

		public KineticZoom(float startZoom, float endZoom, long startTime, long endTime)
		{
			this.startTime = startTime;
			this.endTime = endTime;
			this.startZoom = startZoom;
			this.endZoom = endZoom;
			fertig = false;
		}

		public float getAktZoom()
		{
			long aktTime = System.currentTimeMillis();
			float faktor = (float) (aktTime - startTime) / (float) (endTime - startTime);
			faktor = com.badlogic.gdx.math.Interpolation.fade.apply(faktor);
			if (faktor >= 1)
			{
				fertig = true;
				faktor = 1;
			}
			return startZoom + (endZoom - startZoom) * faktor;
		}

		public boolean getFertig()
		{
			return fertig;
		}
	}

	private void requestLayout()
	{
		// Logger.LogCat("MapView clacLayout()");
		float margin = GL_UISizes.margin;
		info.setPos(new Vector2(margin, (float) (this.mapIntHeight - margin - info.getHeight())));
		info.setVisible(showCompass);
		togBtn.setPos(new Vector2((float) (this.mapIntWidth - margin - togBtn.getWidth()), this.mapIntHeight - margin - togBtn.getHeight()));

		zoomScale.setSize((float) (44.6666667 * GL_UISizes.DPI),
				this.height - info.getHeight() - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());

		GL.that.renderOnce(this.getName() + " requestLayout");
	}

	@Override
	protected void Initial()
	{

	}

	public void LoadTrack(String trackPath)
	{
		LoadTrack(trackPath, "");
	}

	public void LoadTrack(String trackPath, String file)
	{

		String absolutPath = "";
		if (file.equals(""))
		{
			absolutPath = trackPath;
		}
		else
		{
			absolutPath = trackPath + "/" + file;
		}
		RouteOverlay.MultiLoadRoute(absolutPath, RouteOverlay.getNextColor());
		RouteOverlay.RoutesChanged();
	}

	public int getAktZoom()
	{
		return aktZoom;
	}

	@Override
	public String getReceiverName()
	{
		return "Core.MapView";
	}

	@Override
	protected void SkinIsChanged()
	{
		setBackground(SpriteCache.ListBack);
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(
				mapIntWidth, mapIntHeight)), aktZoom, true);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);
		if (infoBubble.isVisible())
		{
			infoBubble.setCache(infoBubble.getCache(), infoBubble.getWaypoint(), true);
		}
		invalidateTexture();
	}

	@Override
	public void invalidateTexture()
	{
		setNewSettings(INITIAL_THEME);
		mapTileLoader.clearLoadedTiles();
		tilesToDraw.clear();
		mapScale.ZoomChanged();
		crossLine = null;
	}

	public boolean doubleClick(int x, int y, int pointer, int button)
	{
		if (CompassMode)
		{
			// Center map on CompassMode
			togBtn.setState(1);
			return true;
		}
		else
		{
			return super.doubleClick(x, y, pointer, button);
		}
	}

	@Override
	public Priority getPriority()
	{
		return Priority.Normal;
	}

	@Override
	public void SpeedChanged()
	{
		if (info != null)
		{
			info.setSpeed(Locator.SpeedString());

			if (togBtn.getState() == 4 && Config.settings.dynamicZoom.getValue())
			{
				// calculate dynamic Zoom

				double maxSpeed = Config.settings.MoveMapCenterMaxSpeed.getValue();
				int maxZoom = Config.settings.dynamicZoomLevelMax.getValue();
				int minZoom = Config.settings.dynamicZoomLevelMin.getValue();

				double percent = Locator.SpeedOverGround() / maxSpeed;

				float dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
				if (dynZoom > maxZoom) dynZoom = maxZoom;
				if (dynZoom < minZoom) dynZoom = minZoom;

				if (lastDynamicZoom != dynZoom)
				{
					lastDynamicZoom = dynZoom;
					zoomBtn.setZoom((int) lastDynamicZoom);
					inputState = InputState.Idle;

					kineticZoom = new KineticZoom(camera.zoom, mapTileLoader.getMapTilePosFactor(lastDynamicZoom),
							System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

					GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
					GL.that.renderOnce(MapView.this.getName() + " ZoomButtonClick");
					calcPixelsPerMeter();
				}
			}
		}

	}

	// Create new Waypoint at screen center
	public void createWaypointAtCenter()
	{
		String newGcCode = "";
		try
		{
			newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().GcCode);
		}
		catch (Exception e)
		{
			return;
		}
		Coordinate coord = center;
		if ((coord == null) || (!coord.isValid())) coord = Locator.getCoordinate();
		if ((coord == null) || (!coord.isValid())) return;
		Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(),
				GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));

		EditWaypoint EdWp = new EditWaypoint(newWP, new ReturnListner()
		{

			@Override
			public void returnedWP(Waypoint waypoint)
			{
				if (waypoint != null)
				{

					GlobalCore.getSelectedCache().waypoints.add(waypoint);
					GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
					if (waypoint.IsStart)
					{
						// Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
						// definiert
						// ist!!!
						WaypointDAO wpd = new WaypointDAO();
						wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), waypoint);
					}
					WaypointDAO waypointDAO = new WaypointDAO();
					waypointDAO.WriteToDatabase(waypoint);

					// informiere WaypointListView über Änderung
					WaypointListChangedEventList.Call(GlobalCore.getSelectedCache());
					GL.that.renderOnce("newWP_CenterMap");
				}
			}
		}, true);
		EdWp.show();

	}

	public int getState()
	{
		return togBtn.getState();
	}

}
