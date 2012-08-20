package CB_Core.GL_UI.Views;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.DestroyFailedException;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.invalidateTextureEvent;
import CB_Core.Events.invalidateTextureEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.InfoBubble;
import CB_Core.GL_UI.Controls.MapInfoPanel;
import CB_Core.GL_UI.Controls.MapScale;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.ZoomButtons;
import CB_Core.GL_UI.Controls.ZoomScale;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Main.MainViewBase;
import CB_Core.GL_UI.Views.MapViewCacheList.WaypointRenderInfo;
import CB_Core.Locator.Locator;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.Point;
import CB_Core.Map.PointL;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.TileGL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class MapView extends CB_View_Base implements SelectedCacheEvent, PositionChangedEvent, invalidateTextureEvent
{
	public static MapView that = null; // für Zugriff aus Listeners heraus auf this

	// ####### Enthaltene Controls ##########
	private MultiToggleButton togBtn;
	private ZoomButtons zoomBtn;
	private MapInfoPanel info;
	private ZoomScale zoomScale;
	private InfoBubble infoBubble;
	private MapScale mapScale;
	// ########################################

	private Locator locator = null;
	protected SortedMap<Long, TileGL> loadedTiles = new TreeMap<Long, TileGL>();
	final Lock loadedTilesLock = new ReentrantLock();
	protected SortedMap<Long, Descriptor> queuedTiles = new TreeMap<Long, Descriptor>();
	private Lock queuedTilesLock = new ReentrantLock();
	private Thread queueProcessor = null;
	private boolean alignToCompass = false;
	private boolean alignToCompassCarMode = false;
	private boolean autoResortFromCarMode = false;
	// private boolean centerGps = false;
	private float mapHeading = 0;
	private float arrowHeading = 0;
	private MapViewCacheList mapCacheList;
	private Point lastMovement = new Point(0, 0);
	private int zoomCross = 16;
	private Vector2 myPointOnScreen;
	private Sprite directLineOverlay;
	private Texture directLineTexture;
	private Texture AccuracyTexture;

	private float ySpeedVersatz = 200;

	// private GL_ZoomScale zoomScale;

	// Settings values
	public boolean showRating;
	public boolean showDT;
	public boolean showTitles;
	public boolean hideMyFinds;
	private boolean showCompass;
	public boolean showDirektLine;
	// private boolean nightMode;
	private int aktZoom;
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
	public static final int MAX_MAP_ZOOM = 22;
	int frameRateIdle = 200;
	int frameRateAction = 30;

	int maxNumTiles = 100;
	float maxTilesPerScreen = 0;
	float iconFactor = 1.5f;

	long posx = 8745;
	long posy = 5685;

	// screencenter in World Coordinates (Pixels in Zoom Level maxzoom
	PointL screenCenterW = new PointL(0, 0); // wird am Anfang von Render() gesetzt
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
	public Layer CurrentLayer = null;

	OrthographicCamera camera;

	// Gdx2DPixmap circle;
	// Texture tcircle;

	long startTime;
	Timer myTimer;

	boolean useNewInput = true;

	public MapView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		invalidateTextureEventList.Add(this);
		registerSkinChangedEvent();
		setBackground(SpriteCache.ListBack);

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

		if (queueProcessor == null)
		{
			queueProcessor = new queueProcessor();
			queueProcessor.setPriority(Thread.MIN_PRIORITY);
			queueProcessor.start();
		}

		mapScale = new MapScale(new CB_RectF(GL_UISizes.margin, GL_UISizes.margin, this.halfWidth, GL_UISizes.ZoomBtn.getHalfWidth() / 4),
				"mapScale", this);

		this.addChild(mapScale);

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

				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System
						.currentTimeMillis() + 1000);
				GL_Listener.glListener.addRenderView(that, frameRateAction);
				GL_Listener.glListener.renderOnce(that.getName() + " ZoomButtonClick");
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

				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System
						.currentTimeMillis() + 1000);
				GL_Listener.glListener.addRenderView(that, frameRateAction);
				GL_Listener.glListener.renderOnce(that.getName() + " ZoomButtonClick");
				calcPixelsPerMeter();
				return true;
			}
		});
		this.addChild(zoomBtn);

		this.setOnClickListener(onClickListner);

		info = (MapInfoPanel) this.addChild(new MapInfoPanel(GL_UISizes.Info, "InfoPanel"));

		CB_RectF ZoomScaleRec = new CB_RectF();
		ZoomScaleRec.setSize((float) (44.6666667 * GL_UISizes.DPI),
				this.height - info.getHeight() - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());
		ZoomScaleRec.setPos(new Vector2(GL_UISizes.margin, zoomBtn.getMaxY() + GL_UISizes.margin));

		zoomScale = new ZoomScale(ZoomScaleRec, "zoomScale", 2, 21, 12);
		this.addChild(zoomScale);

		InitializeMap();

		// initial Zoom Scale
		// zoomScale = new GL_ZoomScale(6, 20, 13);

		mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);

		// from create

		String currentLayerName = Config.settings.CurrentMapLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			CurrentLayer = ManagerBase.Manager.GetLayerByName((currentLayerName == "") ? "Mapnik" : currentLayerName, currentLayerName, "");
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

				Config.settings.LastMapToggleBtnState.setValue(State);
				Config.AcceptChanges();

				if (State == 4)
				{
					// Car mode
					alignToCompassCarMode = true;
					if (!GlobalCore.autoResort)
					{
						GlobalCore.autoResort = true;
						Config.settings.AutoResort.setValue(GlobalCore.autoResort);
						if (GlobalCore.autoResort)
						{
							synchronized (Database.Data.Query)
							{
								Database.Data.Query.Resort();
							}
						}
						autoResortFromCarMode = true;
					}

				}

				else if (State == 2)
				{

					if (GlobalCore.SelectedCache() != null)
					{
						if (GlobalCore.SelectedWaypoint() != null)
						{
							GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
						}
						else
						{
							GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), null);
						}
					}
				}
				else if (State > 0)
				{
					setCenter(new Coordinate(GlobalCore.LastValidPosition.Latitude, GlobalCore.LastValidPosition.Longitude));
				}

				if (State != 4)
				{
					alignToCompassCarMode = false;

					if (autoResortFromCarMode)
					{
						autoResortFromCarMode = false;
						GlobalCore.autoResort = false;
						Config.settings.AutoResort.setValue(GlobalCore.autoResort);
					}
				}

			}
		});
		togBtn.registerSkinChangedEvent();
		togBtn.setState(Config.settings.LastMapToggleBtnState.getValue(), true);
		this.addChild(togBtn);

		infoBubble = new InfoBubble(GL_UISizes.Bubble, "infoBubble");
		infoBubble.setVisibility(GL_View_Base.INVISIBLE);
		infoBubble.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GlobalCore.SelectedWaypoint(infoBubble.getCache(), infoBubble.getWaypoint());
				infoBubble.setVisibility(INVISIBLE);
				return true;
			}
		});
		this.addChild(infoBubble);

		resize(rec.getWidth(), rec.getHeight());

		this.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// CB_AllContextMenuHandler.showMapViewGLContextMenu();
				return true;
			}
		});
		center.Latitude = Config.settings.MapInitLatitude.getValue();
		center.Longitude = Config.settings.MapInitLongitude.getValue();
		aktZoom = Config.settings.lastZoomLevel.getValue();
		zoomBtn.setZoom(aktZoom);
		calcPixelsPerMeter();
		mapScale.zoomChanged();

		if ((center.Latitude == -1000) && (center.Longitude == -1000))
		{
			// not initialized
			center.Latitude = 48;
			center.Longitude = 12;
		}

	}

	@Override
	public void onShow()
	{
		CB_Core.Events.SelectedCacheEventList.Add(this);
		CB_Core.Events.PositionChangedEventList.Add(this);
		PositionChanged(GlobalCore.Locator);

		alignToCompass = !Config.settings.MapNorthOriented.getValue();

		alignToCompassCarMode = (togBtn.getState() == 4);
		if (!alignToCompassCarMode)
		{
			drawingWidth = mapIntWidth;
			drawingHeight = mapIntHeight;
		}
	}

	@Override
	public void onHide()
	{
		CB_Core.Events.SelectedCacheEventList.Remove(this);
		CB_Core.Events.PositionChangedEventList.Remove(this);

	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onRezised(CB_RectF rec)
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
		setZoomScale(aktZoom);
		camera.zoom = getMapTilePosFactor(aktZoom);
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
		Config.settings.MapInitLatitude.setValue(center.Latitude);
		Config.settings.MapInitLongitude.setValue(center.Longitude);
		Config.settings.lastZoomLevel.setValue(aktZoom);
		Config.settings.WriteToDB();
		super.onStop();
	}

	public void SetCurrentLayer(Layer newLayer)
	{
		Config.settings.CurrentMapLayer.setValue(newLayer.Name);
		Config.AcceptChanges();

		CurrentLayer = newLayer;

		clearLoadedTiles();
	}

	private void clearLoadedTiles()
	{
		loadedTilesLock.lock();
		try
		{
			for (TileGL tile : loadedTiles.values())
			{
				try
				{
					tile.destroy();
				}
				catch (DestroyFailedException e)
				{
					e.printStackTrace();
				}
			}
			loadedTiles.clear();
		}
		finally
		{
			loadedTilesLock.unlock();
		}
	}

	protected SortedMap<Long, TileGL> tilesToDraw = new TreeMap<Long, TileGL>();

	@Override
	protected void render(SpriteBatch batch)
	{

		if (Config.settings.MoveMapCenterWithSpeed.getValue() && alignToCompassCarMode && this.locator != null && this.locator.hasSpeed())
		{

			double maxSpeed = Config.settings.MoveMapCenterMaxSpeed.getValue();

			double percent = this.locator.SpeedOverGround() / maxSpeed;

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

			int zoom = MAX_MAP_ZOOM;
			float tmpZoom = camera.zoom;
			float faktor = 1.5f;
			faktor = faktor - iconFactor + 1;
			while (tmpZoom > faktor)
			{
				tmpZoom /= 2;
				zoom--;
			}
			aktZoom = zoom;

			float diffZoom = 1 - (tmpZoom * 2);
			// Logger.LogCat("Kinetic: " + diffZoom);
			zoomScale.setDiffCameraZoom(diffZoom, true);

			if (kineticZoom.getFertig())
			{
				setZoomScale(zoomBtn.getZoom());
				GL_Listener.glListener.removeRenderView(this);
				kineticZoom = null;
			}
			else
				reduceFps = false;
		}

		if ((kineticPan != null) && (kineticPan.started))
		{
			long faktor = getMapTilePosFactor(aktZoom);
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
			GL_Listener.glListener.removeRenderView(this);
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

		CB_Core.Map.RouteOverlay.RenderRoute(batch, aktZoom, ySpeedVersatz);
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
		float faktor = camera.zoom;
		float dx = this.ThisWorldRec.getCenterPos().x - MainViewBase.mainView.getCenterPos().x;
		float dy = this.ThisWorldRec.getCenterPos().y - MainViewBase.mainView.getCenterPos().y;

		dy -= ySpeedVersatz;

		camera.position.set(0, 0, 0);
		float dxr = dx;
		float dyr = dy;

		if (alignToCompass || alignToCompassCarMode)
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
			for (TileGL tile : loadedTiles.values())
			{
				tile.Age++;
			}
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
					try
					{
						tile = loadedTiles.get(desc.GetHashCode());
					}
					catch (Exception ex)
					{
					}
					if (tile != null)
					{
						// das Alter der benutzten Tiles auf 0 setzen wenn dies
						// für den richtigen aktuellen Zoom ist
						if (tmpzoom == aktZoom) tile.Age = 0;

						if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(), tile);
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
					long posFactor = getMapTilePosFactor(tile.Descriptor.Zoom);

					long xPos = (long) tile.Descriptor.X * posFactor * 256 - screenCenterW.x;
					long yPos = -(tile.Descriptor.Y + 1) * posFactor * 256 - screenCenterW.y;
					float xSize = tile.texture.getWidth() * posFactor;
					float ySize = tile.texture.getHeight() * posFactor;
					batch.draw(tile.texture, (float) xPos, (float) yPos, xSize, ySize);
				}
			}
		}
		tilesToDraw.clear();

	}

	@SuppressWarnings("unused")
	private void renderDebugInfo(SpriteBatch batch)
	{
		if (true) return;

		str = debugString;
		Fonts.getNormal().draw(batch, str, 20, 120);

		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.getNormal().draw(batch, str, 20, 100);

		str = String.valueOf(aktZoom) + " - camzoom: " + Math.round(camera.zoom * 100) / 100;
		Fonts.getNormal().draw(batch, str, 20, 80);

		str = "lTiles: " + loadedTiles.size() + " - qTiles: " + queuedTiles.size();
		Fonts.getNormal().draw(batch, str, 20, 60);

		str = "TrackPoi: " + RouteOverlay.AllTrackPoints + " -  " + RouteOverlay.ReduceTrackPoints + " [" + RouteOverlay.DrawedLineCount
				+ "]";
		Fonts.getNormal().draw(batch, str, 20, 40);

		str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
		Fonts.getNormal().draw(batch, str, 20, 20);

	}

	private void renderPositionMarker(SpriteBatch batch)
	{
		if (locator != null)
		{
			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MAX_MAP_ZOOM, GlobalCore.LastValidPosition.Longitude),
					Descriptor.LatitudeToTileY(MAX_MAP_ZOOM, GlobalCore.LastValidPosition.Latitude), MAX_MAP_ZOOM, MAX_MAP_ZOOM);

			Vector2 vPoint = new Vector2((float) point.X, -(float) point.Y);

			myPointOnScreen = worldToScreen(vPoint);

			myPointOnScreen.y -= ySpeedVersatz;

			directLineOverlay = null;
			if (directLineTexture != null) directLineTexture.dispose();
			if (actAccuracy != locator.getLocation().Accuracy || actPixelsPerMeter != pixelsPerMeter)
			{
				if (AccuracyTexture != null) AccuracyTexture.dispose();
				actAccuracy = locator.getLocation().Accuracy;
				actPixelsPerMeter = pixelsPerMeter;

				int radius = (int) (pixelsPerMeter * locator.getLocation().Accuracy);
				// Logger.LogCat("Accuracy radius " + radius);
				// Logger.LogCat("pixelsPerMeter " + pixelsPerMeter);
				if (radius > 0 && radius < UiSizes.getSmallestWidth())
				{

					try
					{
						int squaredR = radius * 2;

						int w = getNextHighestPO2(squaredR);
						int h = getNextHighestPO2(squaredR);
						// Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
						Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA4444);
						Pixmap.setBlending(Blending.None);
						p.setColor(0f, 0.1f, 0.4f, 0.1f);

						p.fillCircle(radius, radius, radius);
						p.setColor(0f, 0f, 1f, 0.8f);
						p.drawCircle(radius, radius, radius);
						p.setColor(0.5f, 0.5f, 1f, 0.7f);
						p.drawCircle(radius, radius, radius - 1);
						p.drawCircle(radius, radius, radius + 1);

						AccuracyTexture = new Texture(p);

						AccuracySprite = new Sprite(AccuracyTexture, squaredR, squaredR);
						p.dispose();
						AccuracySprite.setSize(squaredR, squaredR);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}

			}

			if (AccuracySprite != null && AccuracySprite.getWidth() > GL_UISizes.PosMarkerSize)
			{// nur wenn berechnet wurde und grösser als der PosMarker

				float center = AccuracySprite.getWidth() / 2;

				AccuracySprite.setPosition(myPointOnScreen.x - center, myPointOnScreen.y - center);
				AccuracySprite.draw(batch);
			}

			boolean lastUsedCompass = locator.LastUsedCompass;
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

			Sprite arrow = SpriteCache.Arrows.get(arrowId);
			arrow.setRotation(-arrowHeading);
			arrow.setBounds(myPointOnScreen.x - GL_UISizes.halfPosMarkerSize, myPointOnScreen.y - GL_UISizes.halfPosMarkerSize,
					GL_UISizes.PosMarkerSize, GL_UISizes.PosMarkerSize);
			arrow.setOrigin(GL_UISizes.halfPosMarkerSize, GL_UISizes.halfPosMarkerSize);
			arrow.draw(batch);

		}
	}

	private Sprite AccuracySprite;
	private int actAccuracy = 0;
	private float actPixelsPerMeter = 0;

	private CB_RectF TargetArrowScreenRec;

	private void RenderTargetArrow(SpriteBatch batch)
	{

		if (GlobalCore.SelectedCache() == null) return;

		Coordinate coord = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos : GlobalCore.SelectedCache().Pos;

		float x = (float) (256.0 * Descriptor.LongitudeToTileX(MAX_MAP_ZOOM, coord.Longitude));
		float y = (float) (-256.0 * Descriptor.LatitudeToTileY(MAX_MAP_ZOOM, coord.Latitude));

		float halfHeight = (mapIntHeight / 2) - ySpeedVersatz;
		float halfWidth = mapIntWidth / 2;

		// create ScreenRec

		if (TargetArrowScreenRec == null)
		{
			TargetArrowScreenRec = new CB_RectF(0, 0, mapIntWidth, mapIntHeight);
			TargetArrowScreenRec.ScaleCenter(0.9f);
			TargetArrowScreenRec.setHeight(TargetArrowScreenRec.getHeight() - (TargetArrowScreenRec.getHeight() - info.getY())
					- zoomBtn.getHeight());
			TargetArrowScreenRec.setY(zoomBtn.getMaxY());
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
						renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
					}
					else
					{
						renderWPI(batch, wpUnderlay, wpSize, wpi);
					}
				}
			}
		}

	}

	private Sprite LineSprite, PointSprite;
	private float scale;

	private void renderWPI(SpriteBatch batch, SizeF WpUnderlay, SizeF WpSize, WaypointRenderInfo wpi)
	{
		Vector2 screen = worldToScreen(new Vector2(wpi.MapX, wpi.MapY));

		screen.y -= ySpeedVersatz;

		if (myPointOnScreen != null && showDirektLine && (wpi.Selected) && (wpi.Waypoint == GlobalCore.SelectedWaypoint()))
		{

			// if (directLineOverlay == null)
			// {
			// int w = getNextHighestPO2((int) mapIntWidth);
			// int h = getNextHighestPO2((int) mapIntHeight);
			// Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
			// p.setColor(1f, 0f, 0f, 1f);
			// p.drawLine((int) myPointOnScreen.x, (int) myPointOnScreen.y, (int) screen.x, (int) screen.y);
			//
			// directLineTexture = new Texture(p, Pixmap.Format.RGBA8888, false);
			//
			// directLineOverlay = new Sprite(directLineTexture, (int) mapIntWidth, (int) mapIntHeight);
			// directLineOverlay.setPosition(0, 0);
			// directLineOverlay.flip(false, true);
			// p.dispose();
			//
			// }
			//
			// directLineOverlay.draw(batch);

			if (LineSprite == null || PointSprite == null)
			{
				LineSprite = SpriteCache.Arrows.get(13);
				PointSprite = SpriteCache.Arrows.get(14);
				scale = 0.8f * UiSizes.getScale();
			}

			LineSprite.setColor(Color.RED);
			PointSprite.setColor(Color.RED);

			DrawUtils.drawSpriteLine(batch, LineSprite, PointSprite, scale, myPointOnScreen.x, myPointOnScreen.y, screen.x, screen.y);

		}

		float NameYMovement = 0;

		if ((aktZoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == GlobalCore.SelectedWaypoint()))
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
			String Name = drawAsWaypoint ? wpi.Waypoint.Description : wpi.Cache.Name;

			float halfWidth = Fonts.get16_Out().getBounds(wpi.Cache.Name).width / 2;
			Fonts.get16_Out().draw(batch, Name, screen.x - halfWidth, screen.y - WpUnderlay.halfHeight - NameYMovement);
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
			tile = loadedTiles.get(desc.GetHashCode());
		}
		catch (Exception ex)
		{
		}
		if (tile != null)
		{
			// das Alter der benutzten Tiles nicht auf 0 setzen, da dies
			// eigentlich nicht das richtige Tile ist!!!
			// tile.Age = 0;
			if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(), tile);
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
					tile = loadedTiles.get(desc.GetHashCode());
				}
				catch (Exception ex)
				{
				}
				if (tile != null)
				{
					if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(), tile);
					// das Alter der benutzten Tiles nicht auf 0 setzen, da dies
					// eigentlich nicht das richtige Tile ist!!!
					// tile.Age = 0;
				}
				else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= MAX_MAP_ZOOM))
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

	private void loadTiles()
	{

		mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, false);

		if (ManagerBase.Manager == null) return; // Kann nichts laden, wenn der Manager Null ist!

		deleteUnusedTiles();
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

		loadedTilesLock.lock();
		queuedTilesLock.lock();
		// Queue jedesmal löschen, damit die Tiles, die eigentlich
		// mal
		// gebraucht wurden aber trotzdem noch nicht geladen sind
		// auch nicht mehr geladen werden
		queuedTiles.clear();
		try
		{
			for (int i = lo.X; i <= ru.X; i++)
			{
				for (int j = lo.Y; j <= ru.Y; j++)
				{
					Descriptor desc = new Descriptor(i, j, aktZoom);

					try
					{
						if (loadedTiles.containsKey(desc.GetHashCode()))
						{
							continue; // Dieses
										// Tile
										// existiert
										// schon!
						}
						if (queuedTiles.containsKey(desc.GetHashCode())) continue;
						queueTile(desc);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		finally
		{
			queuedTilesLock.unlock();
			loadedTilesLock.unlock();
		}
	}

	private void deleteUnusedTiles()
	{
		// Ist Auslagerung überhaupt nötig?
		if (numLoadedTiles() <= maxNumTiles) return;
		// Wenn die Anzahl der maximal gleichzeitig geladenen Tiles
		// überschritten ist
		// die ältesten Tiles löschen
		do
		{
			loadedTilesLock.lock();
			try
			{
				// Kachel mit maximalem Alter suchen
				long maxAge = Integer.MIN_VALUE;
				Descriptor maxDesc = null;

				for (TileGL tile : loadedTiles.values())
					if (/* tile.texture != null && */tile.Age > maxAge)
					{
						maxAge = tile.Age;
						maxDesc = tile.Descriptor;
					}

				// Instanz freigeben und Eintrag löschen
				if (maxDesc != null)
				{
					try
					{
						TileGL tile = loadedTiles.get(maxDesc.GetHashCode());
						loadedTiles.remove(maxDesc.GetHashCode());
						tile.destroy();
					}
					catch (Exception ex)
					{
						Logger.Error("MapView.preemptTile()", "", ex);
					}
				}
			}
			finally
			{
				loadedTilesLock.unlock();
			}
		}
		while (numLoadedTiles() > maxNumTiles);
	}

	int numLoadedTiles()
	{
		return loadedTiles.size();
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
				if (GlobalCore.LastValidPosition.Valid)
				{
					setCenter(new Coordinate(GlobalCore.LastValidPosition));
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
										setCenter(new Coordinate(Database.Data.Query.get(0).Latitude(), Database.Data.Query.get(0)
												.Longitude()));
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

			// Größe des Maßstabes berechnen etc...
			// zoomChanged();
		}

		setNewSettings();
	}

	public void setNewSettings()
	{
		showRating = Config.settings.MapShowRating.getValue();
		showDT = Config.settings.MapShowDT.getValue();
		showTitles = Config.settings.MapShowTitles.getValue();
		hideMyFinds = Config.settings.MapHideMyFinds.getValue();
		showCompass = Config.settings.MapShowCompass.getValue();
		showDirektLine = Config.settings.ShowDirektLine.getValue();
		// nightMode = Config.settings.nightMode.getValue();
		iconFactor = (float) Config.settings.MapViewDPIFaktor.getValue();
		aktZoom = Config.settings.lastZoomLevel.getValue();
		zoomBtn.setMaxZoom(Config.settings.OsmMaxLevel.getValue());
		zoomBtn.setMinZoom(Config.settings.OsmMinLevel.getValue());
		zoomBtn.setZoom(aktZoom);

		zoomScale.setMaxZoom(Config.settings.OsmMaxLevel.getValue());
		zoomScale.setMinZoom(Config.settings.OsmMinLevel.getValue());
		setZoomScale(aktZoom);

	}

	public void saveToSettings()
	{
		Config.settings.lastZoomLevel.setValue(aktZoom);

	}

	private void setScreenCenter(Vector2 newCenter)
	{
		synchronized (screenCenterT)
		{
			screenCenterT.x = (long) newCenter.x;
			screenCenterT.y = (long) (-newCenter.y);
		}
		// if (camera != null) camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);
		GL_Listener.glListener.renderOnce(this.getName() + " setScreenCenter");
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

			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MAX_MAP_ZOOM, center.Longitude),
					Descriptor.LatitudeToTileY(MAX_MAP_ZOOM, center.Latitude), MAX_MAP_ZOOM, MAX_MAP_ZOOM);

			setScreenCenter(new Vector2((float) point.X, (float) point.Y));
		}
	}

	private long getMapTilePosFactor(float zoom)
	{
		long result = 1;

		// for (int z = zoom; z < MAX_MAP_ZOOM; z++)
		// {
		// result *= 2;
		// }

		result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);

		return result;
	}

	/**
	 * liefert die World-Koordinate in Pixel relativ zur Map in der höchsten Auflösung
	 */
	private Vector2 screenToWorld(Vector2 point)
	{
		Vector2 result = new Vector2(0, 0);
		synchronized (screenCenterW)
		{
			result.x = screenCenterW.x + ((long) point.x - mapIntWidth / 2) * camera.zoom;
			result.y = -screenCenterW.y + ((long) point.y - mapIntHeight / 2) * camera.zoom;
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
		for (int i = MAX_MAP_ZOOM; i > zoom; i--)
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
	public void PositionChanged(Locator locator)
	{
		if (locator == null) return;
		if (locator.getLocation() == null) return;

		this.locator = locator;
		GlobalCore.LastValidPosition = new Coordinate(locator.getLocation().Latitude, locator.getLocation().Longitude);
		GlobalCore.LastValidPosition.Elevation = locator.getAlt();

		if (info != null)
		{
			info.setCoord(GlobalCore.LastValidPosition);
			info.setSpeed(locator.SpeedString());

			Coordinate position = null;
			// if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
			if (GlobalCore.LastValidPosition != null) position = GlobalCore.LastValidPosition;
			else
				position = new Coordinate();

			float distance = -1;

			// Gps empfang ?
			if (GlobalCore.SelectedCache() != null && position.Valid)
			{
				if (GlobalCore.SelectedWaypoint() == null) distance = position.Distance(GlobalCore.SelectedCache().Pos);
				else
					distance = position.Distance(GlobalCore.SelectedWaypoint().Pos);
			}
			info.setDistance(distance);

		}

		if (togBtn.getState() > 0 && togBtn.getState() != 2) setCenter(new Coordinate(locator.getLocation().Latitude,
				locator.getLocation().Longitude));

		if (togBtn.getState() == 4 && Config.settings.dynamicZoom.getValue())
		{
			// calculate dynamic Zoom

			double maxSpeed = Config.settings.MoveMapCenterMaxSpeed.getValue();
			int maxZoom = Config.settings.dynamicZoomLevelMax.getValue();
			int minZoom = Config.settings.dynamicZoomLevelMin.getValue();

			double percent = this.locator.SpeedOverGround() / maxSpeed;

			float dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
			if (dynZoom > maxZoom) dynZoom = maxZoom;
			if (dynZoom < minZoom) dynZoom = minZoom;

			if (lastDynamicZoom != dynZoom)
			{
				lastDynamicZoom = dynZoom;
				zoomBtn.setZoom((int) lastDynamicZoom);
				inputState = InputState.Idle;

				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(),
						System.currentTimeMillis() + 1000);

				// kineticZoom = new KineticZoom(camera.zoom, lastDynamicZoom, System.currentTimeMillis(), System.currentTimeMillis() +
				// 1000);

				GL_Listener.glListener.addRenderView(that, frameRateAction);
				GL_Listener.glListener.renderOnce(that.getName() + " ZoomButtonClick");
				calcPixelsPerMeter();
			}

			// Logger.LogCat("SpeedVersatz = " + String.valueOf(ySpeedVersatz) + " / 1/3=" + String.valueOf(height / 3));
			// Logger.LogCat("MaxSpeed = " + String.valueOf(maxSpeed));
			// Logger.LogCat("Dyn Zoom = " + String.valueOf(dynZoom));

		}

	}

	private float lastDynamicZoom = -1;

	@Override
	public void OrientationChanged(float heading)
	{
		if (GlobalCore.Locator == null) return;

		if (info != null)
		{
			Coordinate position = null;
			// if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
			if (GlobalCore.LastValidPosition != null) position = GlobalCore.LastValidPosition;
			else
				position = new Coordinate();
			if (GlobalCore.SelectedCache() != null)
			{
				Coordinate cache = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos
						: GlobalCore.SelectedCache().Pos;
				double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude, cache.Longitude);
				info.setBearing((float) (bearing - GlobalCore.Locator.getHeading()));
			}
		}

		if (this.locator != null)
		{
			heading = this.locator.getHeading();
		}
		else
		{
			heading = GlobalCore.Locator.getHeading();
		}

		if (alignToCompass || alignToCompassCarMode)
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

	private void queueTile(Descriptor desc)
	{
		queuedTilesLock.lock();
		try
		{
			if (queuedTiles.containsKey(desc.GetHashCode())) return;

			queuedTiles.put(desc.GetHashCode(), desc);
		}
		finally
		{
			queuedTilesLock.unlock();
		}

	}

	private class queueProcessor extends Thread
	{
		@Override
		public void run()
		{
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
								// ArrayList<KachelOrder> kOrder = new
								// ArrayList<KachelOrder>();
								// long posFactor = getMapTilePosFactor(zoom);
								// for (int i = lo.X; i <= ru.X; i++)
								// {
								// for (int j = lo.Y; j <= ru.Y; j++)
								// {
								// Descriptor desc = new Descriptor(i, j, zoom);
								// double dist = Math.sqrt(Math.pow((double)
								// desc.X * posFactor * 256 +
								// 128 - screenCenterW.X, 2)
								// + Math.pow((double) desc.Y * posFactor * 256
								// - 128 + screenCenterW.Y,
								// 2));
								// kOrder.add(new KachelOrder(desc, dist));
								// }
								// }
								// Collections.sort(kOrder);

								Descriptor nearestDesc = null;
								double nearestDist = Double.MAX_VALUE;
								int nearestZoom = 0;
								for (Descriptor tmpDesc : queuedTiles.values())
								{
									long posFactor = getMapTilePosFactor(tmpDesc.Zoom);

									double dist = Math.sqrt(Math.pow((double) tmpDesc.X * posFactor * 256 + 128 * posFactor
											- screenCenterW.x, 2)
											+ Math.pow((double) tmpDesc.Y * posFactor * 256 + 128 * posFactor + screenCenterW.y, 2));

									if (Math.abs(aktZoom - nearestZoom) > Math.abs(aktZoom - tmpDesc.Zoom))
									{
										// der Zoomfaktor des bisher besten
										// Tiles ist weiter entfernt vom
										// aktuellen Zoom als der vom tmpDesc ->
										// tmpDesc verwenden
										nearestDist = dist;
										nearestDesc = tmpDesc;
										nearestZoom = tmpDesc.Zoom;
									}

									if (dist < nearestDist)
									{
										if (Math.abs(aktZoom - nearestZoom) < Math.abs(aktZoom - tmpDesc.Zoom))
										{
											// zuerst die Tiles, die dem
											// aktuellen Zoom Faktor am nächsten
											// sind.
											continue;
										}
										nearestDist = dist;
										nearestDesc = tmpDesc;
										nearestZoom = tmpDesc.Zoom;
									}
								}
								desc = nearestDesc;

							}
							finally
							{
								queuedTilesLock.unlock();
							}

							// if (desc.Zoom == zoom)
							{
								LoadTile(desc);
							}

							if (queuedTiles.size() < maxTilesPerScreen) Thread.sleep(100);

						}
						catch (Exception ex1)
						{
							Logger.Error("MapViewGL.queueProcessor.doInBackground()", "1", ex1);
						}
					}
					else
					{
						Thread.sleep(200);
					}
				}
				while (true);
			}
			catch (Exception ex3)
			{
				Logger.Error("MapViewGL.queueProcessor.doInBackground()", "3", ex3);
			}
			finally
			{
				// damit im Falle einer Exception der Thread neu gestartet wird
				// queueProcessor = null;
			}
			return;
		}
	}

	private void LoadTile(Descriptor desc)
	{
		TileGL.TileState tileState = TileGL.TileState.Disposed;

		byte[] bytes = null;
		if (ManagerBase.Manager != null)
		{
			bytes = ManagerBase.Manager.LoadInvertedPixmap(CurrentLayer, desc);
		}
		// byte[] bytes = MapManagerEventPtr.OnGetMapTile(CurrentLayer, desc);
		// Texture texture = new Texture(new Pixmap(bytes, 0, bytes.length));
		if (bytes != null && bytes.length > 0)
		{
			tileState = TileGL.TileState.Present;
			addLoadedTile(desc, bytes, tileState);
			// Redraw Map after a new Tile was loaded or generated
			GL_Listener.glListener.renderOnce(this.getName() + " loadTile");
		}
		else
		{
			ManagerBase.Manager.CacheTile(CurrentLayer, desc);
		}
	}

	private void addLoadedTile(Descriptor desc, byte[] bytes, TileGL.TileState state)
	{
		loadedTilesLock.lock();
		try
		{
			if (loadedTiles.containsKey(desc.GetHashCode()))
			{

			}
			else
			{
				TileGL tile = new TileGL(desc, bytes, state);
				loadedTiles.put(desc.GetHashCode(), tile);
			}

		}
		finally
		{
			loadedTilesLock.unlock();
		}

		queuedTilesLock.lock();
		try
		{
			if (queuedTiles.containsKey(desc.GetHashCode())) queuedTiles.remove(desc.GetHashCode());
		}
		finally
		{
			queuedTilesLock.unlock();
		}

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

				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(),
						System.currentTimeMillis() + 500);

				// kineticZoom = new KineticZoom(camera.zoom, lastDynamicZoom, System.currentTimeMillis(), System.currentTimeMillis() +
				// 1000);

				GL_Listener.glListener.addRenderView(that, frameRateAction);
				GL_Listener.glListener.renderOnce(that.getName() + " ZoomButtonClick");
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
					if ((Math.abs(p.x - x) > 10) || (Math.abs(p.y - y) > 10))
					{
						inputState = InputState.Pan;
						// GL_Listener.glListener.addRenderView(this, frameRateAction);
						GL_Listener.glListener.renderOnce(this.getName() + " Dragged");
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
				GL_Listener.glListener.renderOnce(this.getName() + " Pan");
				// debugString = "";
				long faktor = getMapTilePosFactor(aktZoom);
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
					screenCenterT.x += (long) (dxr * faktor);
					screenCenterT.y += (long) (dyr * faktor);
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

				if (camera.zoom < getMapTilePosFactor(zoomBtn.getMaxZoom()))
				{
					camera.zoom = getMapTilePosFactor(zoomBtn.getMaxZoom());
				}
				if (camera.zoom > getMapTilePosFactor(zoomBtn.getMinZoom()))
				{
					camera.zoom = getMapTilePosFactor(zoomBtn.getMinZoom());
				}

				// endCameraZoom = camera.zoom;

				System.out.println(camera.zoom);
				int zoom = MAX_MAP_ZOOM;

				lastDynamicZoom = camera.zoom;

				float tmpZoom = camera.zoom;
				float faktor = 1.5f;
				faktor = faktor - iconFactor + 1;
				while (tmpZoom > faktor)
				{
					tmpZoom /= 2;
					zoom--;
				}
				zoomBtn.setZoom(zoom);
				zoomScale.resetFadeOut();
				setZoomScale(zoom);
				zoomScale.setDiffCameraZoom(1 - (tmpZoom * 2), true);
				aktZoom = zoom;

				// debugString = currentDistance + " - " + originalDistance;
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
		zoomScale.setZoom(zoom);
		mapScale.zoomChanged();
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
			GL_Listener.glListener.renderOnce(this.getName() + " touchUp");

			if ((kineticZoom == null) && (kineticPan == null)) GL_Listener.glListener.removeRenderView(this);

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
				if (GlobalCore.SelectedCache() != null)
				{
					if (GlobalCore.SelectedWaypoint() != null)
					{
						GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
					}
					else
					{
						GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), null);
					}
					return false;
				}
			}

			synchronized (mapCacheList.list)
			{
				if (infoBubble.isVisible())
				{
					// Click outside Bubble -> hide Bubble
					infoBubble.setVisibility(INVISIBLE);
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
						if (GlobalCore.SelectedCache() != minWpi.Cache)
						{
							// Show Bubble at the location of the Waypoint!!!
							infoBubble.setCache(minWpi.Cache, minWpi.Waypoint);
							infoBubble.setVisibility(VISIBLE);
						}
						else
						{
							// do not show Bubble because there will not be
							// selected
							// a
							// different cache but only a different waypoint
							// Wegpunktliste ausrichten
							GlobalCore.SelectedWaypoint(minWpi.Cache, minWpi.Waypoint);
							// FormMain.WaypointListPanel.AlignSelected();
							// updateCacheList();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)),
									aktZoom, true);
						}

					}
					else
					{
						// Show Bubble
						// unabhängig davon, ob der angeklickte Cache == der selectedCache ist
						infoBubble.setCache(minWpi.Cache, null);
						infoBubble.setVisibility(VISIBLE);
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
		PointD point = Descriptor.FromWorld(screenCenterW.x, screenCenterW.y, MAX_MAP_ZOOM, MAX_MAP_ZOOM);

		center = new Coordinate(Descriptor.TileYToLatitude(MAX_MAP_ZOOM, -point.Y), Descriptor.TileXToLongitude(MAX_MAP_ZOOM, point.X));
	}

	public float pixelsPerMeter = 0;

	private void calcPixelsPerMeter()
	{
		Coordinate dummy = Coordinate.Project(center.Latitude, center.Longitude, 90, 1000);
		double l1 = Descriptor.LongitudeToTileX(zoomBtn.getZoom(), center.Longitude);
		double l2 = Descriptor.LongitudeToTileX(zoomBtn.getZoom(), dummy.Longitude);
		double diff = Math.abs(l2 - l1);
		pixelsPerMeter = (float) ((diff * 256) / 1000);
	}

	// @Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		// xxx if (Global.autoResort) return;

		if (cache == null) return;
		/*
		 * if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint }); return; }
		 */

		// mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);
		mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);

		if (togBtn.getState() > 0 && togBtn.getState() != 2) return;

		positionInitialized = true;

		if (togBtn.getState() != 2) togBtn.setState(0, true);

		Coordinate target = (waypoint != null) ? new Coordinate(waypoint.Latitude(), waypoint.Longitude()) : new Coordinate(
				cache.Latitude(), cache.Longitude());

		setCenter(target);

		GL_Listener.glListener.addRenderView(MapView.that, GL_Listener.FRAME_RATE_ACTION);

		// für 2sec rendern lassen, bis Änderungen der WPI-list neu berechnet wurden
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				GL_Listener.glListener.removeRenderView(MapView.that);
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 2000);
		PositionChanged(GlobalCore.Locator);
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
		info.setVisibility(showCompass ? GL_View_Base.VISIBLE : GL_View_Base.INVISIBLE);
		togBtn.setPos(new Vector2((float) (this.mapIntWidth - margin - togBtn.getWidth()), this.mapIntHeight - margin - togBtn.getHeight()));

		zoomScale.setSize((float) (44.6666667 * GL_UISizes.DPI),
				this.height - info.getHeight() - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());

		GL_Listener.glListener.renderOnce(this.getName() + " requestLayout");
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	public void LoadTrack(String trackPath)
	{
		LoadTrack(trackPath, "");
	}

	public void LoadTrack(String trackPath, String file)
	{
		Color[] ColorField = new Color[13];
		{
			ColorField[0] = Color.RED;
			ColorField[1] = Color.YELLOW;
			ColorField[2] = Color.BLACK;
			ColorField[3] = Color.LIGHT_GRAY;
			ColorField[4] = Color.GREEN;
			ColorField[5] = Color.BLUE;
			ColorField[6] = Color.CYAN;
			ColorField[7] = Color.GRAY;
			ColorField[8] = Color.MAGENTA;
			ColorField[9] = Color.ORANGE;
			ColorField[10] = Color.DARK_GRAY;
			ColorField[11] = Color.PINK;
			ColorField[12] = Color.WHITE;
		}
		Color TrackColor;
		TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];

		String absolutPath = "";
		if (file.equals(""))
		{
			absolutPath = trackPath;
		}
		else
		{
			absolutPath = trackPath + "/" + file;
		}
		RouteOverlay.MultiLoadRoute(absolutPath, TrackColor);
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
		mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
		if (infoBubble.isVisible())
		{
			infoBubble.setCache(infoBubble.getCache(), infoBubble.getWaypoint(), true);
		}
	}

	@Override
	public void invalidateTexture()
	{
		clearLoadedTiles();
		tilesToDraw.clear();
		if (directLineOverlay != null) directLineOverlay.getTexture().dispose();
		directLineOverlay = null;
		if (directLineTexture != null) directLineTexture.dispose();
		directLineTexture = null;
		if (AccuracyTexture != null) AccuracyTexture.dispose();
		AccuracyTexture = null;

	}
}
