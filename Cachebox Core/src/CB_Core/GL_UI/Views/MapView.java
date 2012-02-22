package CB_Core.GL_UI.Views;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.DestroyFailedException;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.DB.Database;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.ZoomButtons;
import CB_Core.GL_UI.Views.MapViewCacheList.WaypointRenderInfo;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.Point;
import CB_Core.Map.TileGL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Locator;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class MapView extends GL_View_Base implements SelectedCacheEvent, PositionChangedEvent
{
	private final String Tag = "MAP_VIEW_GL";

	private Locator locator = null;
	protected SortedMap<Long, TileGL> loadedTiles = new TreeMap<Long, TileGL>();
	final Lock loadedTilesLock = new ReentrantLock();
	protected SortedMap<Long, Descriptor> queuedTiles = new TreeMap<Long, Descriptor>();
	private Lock queuedTilesLock = new ReentrantLock();
	private Thread queueProcessor = null;
	public boolean alignToCompass = false;
	// private boolean centerGps = false;
	private float mapHeading = 0;
	private float arrowHeading = 0;
	private MapViewCacheList mapCacheList;
	private Point lastMovement = new Point(0, 0);
	private int zoomCross = 16;
	private MultiToggleButton togBtn;
	private ZoomButtons zoomBtn;
	// private GL_ZoomScale zoomScale;

	// Settings values
	public boolean showRating;
	public boolean showDT;
	public boolean showTitles;
	public boolean hideMyFinds;
	private boolean showCompass;
	public boolean showDirektLine;
	private boolean nightMode;
	private int aktZoom;
	private float startCameraZoom;
	private float endCameraZoom;
	private float diffCameraZoom;

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
	final int maxMapZoom = 22;
	int frameRateIdle = 200;
	int frameRateAction = 30;

	int maxNumTiles = 100;
	float iconFactor = 1.5f;

	long posx = 8745;
	long posy = 5685;

	// screencenter in World Coordinates (Pixels in Zoom Level maxzoom
	Vector2 screenCenterW = new Vector2(0, 0);
	int width;
	int height;
	int drawingWidth;
	int drawingHeight;

	long pos20y = 363904;
	long size20 = 256;

	public Coordinate center = new Coordinate(48.0, 12.0);
	private boolean positionInitialized = false;
	// String CurrentLayer = "germany-0.2.4.map";
	public Layer CurrentLayer = null;

	public static SpriteBatch batch;
	Matrix4 textMatrix;

	OrthographicCamera camera;

	// Gdx2DPixmap circle;
	// Texture tcircle;

	long startTime;
	Timer myTimer;

	boolean useNewInput = true;

	public MapView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		if (queueProcessor == null)
		{
			queueProcessor = new queueProcessor();
			queueProcessor.start();
		}

		// initial Zoom Buttons
		zoomBtn = new ZoomButtons(300, 10, 150, 75, "Zoom");
		zoomBtn.setOnClickListenerDown(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				zoomBtn.ZoomAdd(-1);
				// xxx zoomScale.setZoom(zoomBtn.getZoom());
				// xxx zoomScale.resetFadeOut();
				inputState = InputState.Idle;
				// debugString = "State: " + inputState;
				// aktZoom = zoomBtn.getZoom();
				// camera.zoom = getMapTilePosFactor(aktZoom);
				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System
						.currentTimeMillis() + 1000);
				return true;
			}
		});
		zoomBtn.setOnClickListenerUp(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				zoomBtn.ZoomAdd(1);
				// xxx zoomScale.setZoom(zoomBtn.getZoom());
				// xxx zoomScale.resetFadeOut();
				inputState = InputState.Idle;
				// debugString = "State: " + inputState;
				// aktZoom = zoomBtn.getZoom();
				// camera.zoom = getMapTilePosFactor(aktZoom);
				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System
						.currentTimeMillis() + 1000);
				return true;
			}
		});
		this.addChild(zoomBtn);
		InitializeMap();

		// initial Zoom Scale
		// zoomScale = new GL_ZoomScale(6, 20, 13);

		mapCacheList = new MapViewCacheList(maxMapZoom);

		// from create

		String currentLayerName = Config.settings.CurrentMapLayer.getValue();
		CurrentLayer = ManagerBase.Manager.GetLayerByName((currentLayerName == "") ? "Mapnik" : currentLayerName, currentLayerName, "");

		width = (int) rec.getWidth();// Gdx.graphics.getWidth();
		height = (int) rec.getHeight(); // Gdx.graphics.getHeight();
		drawingWidth = width;
		drawingHeight = height;

		iconFactor = (float) Config.settings.MapViewDPIFaktor.getValue();

		textMatrix = new Matrix4().setToOrtho2D(0, 0, width, height);

		MultiToggleButton togBtn = new MultiToggleButton(400, 100, 100, 100, this, "toggle");

		togBtn.addState("Free", Color.GRAY);
		togBtn.addState("GPS", Color.GREEN);
		togBtn.addState("Lock", Color.RED);
		togBtn.addState("Car", Color.YELLOW);
		togBtn.setLastStateWithLongClick(true);
		togBtn.setOnStateChangedListner(new OnStateChangeListener()
		{

			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				// TODO Auto-generated method stub
			}
		});
		this.addChild(togBtn);

		// SelectedCacheEventList.Add(this);

		// PositionEventList.Add(this);

		resize(rec.getWidth(), rec.getHeight());

		if (batch == null) batch = new SpriteBatch();

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
		if (rec.getWidth() == this.width && rec.getHeight() == this.height)
		{
			// Ausser wenn Camera == null!
			if (camera != null) return;
		}

		this.width = (int) rec.getWidth();
		this.height = (int) rec.getHeight(); // Gdx.graphics.getHeight();
		this.drawingWidth = (int) rec.getWidth();
		this.drawingHeight = (int) rec.getHeight();

		camera = new OrthographicCamera(width, height);

		aktZoom = zoomBtn.getZoom();
		// zoomScale.setZoom(aktZoom);
		camera.zoom = getMapTilePosFactor(aktZoom);
		endCameraZoom = camera.zoom;
		diffCameraZoom = 0;
		camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);

		textMatrix.setToOrtho2D(0, 0, width, height);

		// GL_UISizes.initial(width, height);

		// setze Size als IniSize
		Config.settings.MapIniWidth.setValue(width);
		Config.settings.MapIniHeight.setValue(height);
		Config.AcceptChanges();

	}

	public void SetCurrentLayer(Layer newLayer)
	{
		Config.settings.CurrentMapLayer.setValue(newLayer.Name);
		Config.AcceptChanges();

		CurrentLayer = newLayer;

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
		boolean reduceFps = ((kineticZoom != null) || ((kineticPan != null) && (kineticPan.started)));
		if (kineticZoom != null)
		{
			camera.zoom = kineticZoom.getAktZoom();
			// debugString = "Kinetic: " + camera.zoom;

			int zoom = maxMapZoom;
			float tmpZoom = camera.zoom;
			float faktor = 1.5f;
			faktor = faktor - iconFactor + 1;
			while (tmpZoom > faktor)
			{
				tmpZoom /= 2;
				zoom--;
			}
			aktZoom = zoom;

			// zoomScale.setDiffCameraZoom(1 - (tmpZoom * 2), true);

			if (kineticZoom.getFertig())
			{
				// startTimer(frameRateIdle);
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
			camera.position.add(pan.x * faktor, pan.y * faktor, 0);
			screenCenterW.x = camera.position.x;
			screenCenterW.y = camera.position.y;
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
			// startTimer(frameRateIdle);
		}

		if (SpriteCache.MapIcons == null)
		{
			SpriteCache.LoadSprites();
		}

		loadTiles();

		// if (!renderForced())
		// {
		// if (screenCapture != null)
		// {
		// float width = screenCapture.getTexture().getWidth();
		// float height = screenCapture.getTexture().getHeight();
		// batch.begin();
		// batch.draw(screenCapture, 0, 0, width, height);
		// batch.end();
		//
		// return;
		// }
		// }
		// else
		// {
		// destroyScreenCapture();
		// }

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

		if (alignToCompass)
		{
			camera.up.x = 0;
			camera.up.y = 1;
			camera.up.z = 0;
			camera.rotate(-mapHeading, 0, 0, 1);
		}
		else
		{
			camera.up.x = 0;
			camera.up.y = 1;
			camera.up.z = 0;
		}

		camera.update();

		renderMapTiles();
		renderOverlay();
		renderUI();

		Gdx.gl.glFlush();
		Gdx.gl.glFinish();
	}

	private void renderOverlay()
	{
		batch.setProjectionMatrix(textMatrix);
		batch.begin();

		// calculate icon size
		int iconSize = 0; // 8x8
		if ((aktZoom >= 13) && (aktZoom <= 14)) iconSize = 1; // 13x13
		else if (aktZoom > 14) iconSize = 2; // default Images

		renderWPs(GL_UISizes.WPSizes[iconSize], GL_UISizes.UnderlaySizes[iconSize]);
		renderPositionMarker();
		RenderTargetArrow();
		Bubble.render(GL_UISizes.WPSizes[iconSize]);

		batch.end();
	}

	private void renderUI()
	{
		batch.setProjectionMatrix(textMatrix);
		batch.begin();
		if (showCompass) renderInfoPanel();

		// btnTrackPos.Render(batch, GL_UISizes.Toggle, Fonts.get18());

		// zoomBtn.Render(batch, GL_UISizes.ZoomBtn);
		// zoomScale.Render(batch, GL_UISizes.ZoomScale);

		renderDebugInfo();
		batch.end();
	}

	private void renderMapTiles()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

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
			Descriptor lo = screenToDescriptor(new Vector2(width / 2 - drawingWidth / 2, height / 2 - drawingHeight / 2), tmpzoom);
			Descriptor ru = screenToDescriptor(new Vector2(width / 2 + drawingWidth / 2, height / 2 + drawingHeight / 2), tmpzoom);
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
		for (TileGL tile : tilesToDraw.values())
		{
			tile.createTexture();
			if (tile.texture != null)
			{
				// Faktor, mit der dieses MapTile vergrößert gezeichnet
				// werden muß
				long posFactor = getMapTilePosFactor(tile.Descriptor.Zoom);

				float xPos = tile.Descriptor.X * posFactor * 256;
				float yPos = -(tile.Descriptor.Y + 1) * posFactor * 256;
				float xSize = tile.texture.getWidth() * posFactor;
				float ySize = tile.texture.getHeight() * posFactor;
				batch.draw(tile.texture, xPos, yPos, xSize, ySize);
			}
		}
		tilesToDraw.clear();

		batch.end();
	}

	private void renderDebugInfo()
	{
		str = debugString;
		Fonts.get18().draw(batch, str, 20, 120);

		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.get18().draw(batch, str, 20, 100);

		str = String.valueOf(aktZoom) + " - camzoom: " + Math.round(camera.zoom * 100) / 100;
		Fonts.get18().draw(batch, str, 20, 80);

		str = "lTiles: " + loadedTiles.size() + " - qTiles: " + queuedTiles.size();
		Fonts.get18().draw(batch, str, 20, 60);

		if (mapCacheList != null)
		{
			str = "listCalc: " + mapCacheList.anz + " - C: " + mapCacheList.list.size();
			Fonts.get18().draw(batch, str, 20, 40);
		}
		str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
		Fonts.get18().draw(batch, str, 20, 20);

	}

	private void renderInfoPanel()
	{
		// draw background
		Sprite sprite = SpriteCache.InfoBack;
		sprite.setPosition(GL_UISizes.Info.getX(), GL_UISizes.Info.getY());
		sprite.setSize(GL_UISizes.Info.getWidth(), GL_UISizes.Info.getHeight());
		sprite.draw(MapView.batch);

		// Position ist entweder GPS-Position oder die des Markers, wenn
		// dieser gesetzt wurde.
		Coordinate position = null;
		if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
		else if (GlobalCore.LastValidPosition != null) position = GlobalCore.LastValidPosition;
		else
			position = new Coordinate();

		// Gps empfang ?
		if (GlobalCore.SelectedCache() != null && position.Valid)
		{
			// Distanz einzeichnen
			float distance = 0;

			if (GlobalCore.SelectedWaypoint() == null) distance = position.Distance(GlobalCore.SelectedCache().Pos);
			else
				distance = position.Distance(GlobalCore.SelectedWaypoint().Pos);

			String text = UnitFormatter.DistanceString(distance);
			Fonts.get18().draw(batch, text, GL_UISizes.InfoLine1.x, GL_UISizes.InfoLine1.y);
			// canvas.drawText(text, leftString, bottom - 10, paint);

			// Kompassnadel zeichnen
			if (locator != null)
			{
				Coordinate cache = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos
						: GlobalCore.SelectedCache().Pos;
				double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude, cache.Longitude);
				double relativeBearing = bearing - locator.getHeading();
				// double relativeBearingRad = relativeBearing * Math.PI / 180.0;

				// draw compass
				Sprite compass = SpriteCache.MapArrows.get(0);
				compass.setRotation((float) -relativeBearing);
				compass.setBounds(GL_UISizes.Compass.getX(), GL_UISizes.Compass.getY(), GL_UISizes.Compass.getWidth(),
						GL_UISizes.Compass.getHeight());
				compass.setOrigin(GL_UISizes.halfCompass, GL_UISizes.halfCompass);
				compass.draw(MapView.batch);

			}

			// Koordinaten
			if (position.Valid)
			{
				String textLatitude = GlobalCore.FormatLatitudeDM(position.Latitude);
				String textLongitude = GlobalCore.FormatLongitudeDM(position.Longitude);

				Fonts.get18().draw(batch, textLatitude, GL_UISizes.InfoLine2.x, GL_UISizes.InfoLine1.y);
				Fonts.get18().draw(batch, textLongitude, GL_UISizes.InfoLine2.x, GL_UISizes.InfoLine2.y);

				if (locator != null)
				{
					Fonts.get18().draw(batch, locator.SpeedString(), GL_UISizes.InfoLine1.x, GL_UISizes.InfoLine2.y);
				}

			}
		}

	}

	private void renderPositionMarker()
	{
		if (locator != null)
		{
			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(maxMapZoom, GlobalCore.LastValidPosition.Longitude),
					Descriptor.LatitudeToTileY(maxMapZoom, GlobalCore.LastValidPosition.Latitude), maxMapZoom, maxMapZoom);

			Vector2 vPoint = new Vector2((float) point.X, -(float) point.Y);

			Vector2 screen = worldToScreen(vPoint);

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

			Sprite arrow = SpriteCache.MapArrows.get(arrowId);
			arrow.setRotation(-arrowHeading);
			arrow.setBounds(screen.x - GL_UISizes.halfPosMarkerSize, screen.y - GL_UISizes.halfPosMarkerSize, GL_UISizes.PosMarkerSize,
					GL_UISizes.PosMarkerSize);
			arrow.setOrigin(GL_UISizes.halfPosMarkerSize, GL_UISizes.halfPosMarkerSize);
			arrow.draw(batch);
		}
	}

	private void RenderTargetArrow()
	{

		if (GlobalCore.SelectedCache() == null) return;

		Coordinate coord = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos : GlobalCore.SelectedCache().Pos;

		float x = (float) (256.0 * Descriptor.LongitudeToTileX(maxMapZoom, coord.Longitude));
		float y = (float) (-256.0 * Descriptor.LatitudeToTileY(maxMapZoom, coord.Latitude));

		float halfHeight = height / 2;
		float halfWidth = width / 2;

		// create ScreenRec

		CB_RectF screenRec = new CB_RectF(0, 0, width, height);
		screenRec.ScaleCenter(0.9f);

		Vector2 ScreenCenter = new Vector2(halfWidth, halfHeight);

		Vector2 screen = worldToScreen(new Vector2(x, y));
		Vector2 target = new Vector2(screen.x, screen.y);

		Vector2 newTarget = null;

		// Zuerst abfragen, ob der Target Arrow an ein Control stößt.
		if (showCompass)
		{
			newTarget = GL_UISizes.Info.getIntersection(ScreenCenter, target, 1);
		}

		if (newTarget == null)
		{
			newTarget = GL_UISizes.Toggle.getIntersection(ScreenCenter, target, 1);
		}

		if (newTarget == null && zoomBtn.isVisible())
		{
			newTarget = GL_UISizes.ZoomBtn.getIntersection(ScreenCenter, target, 4);
		}

		// if (newTarget == null && zoomScale.isShown())
		// {
		// newTarget = GL_UISizes.ZoomScale.getIntersection(ScreenCenter, target, 3);
		// }

		if (newTarget == null)
		{
			newTarget = screenRec.getIntersection(ScreenCenter, target);
		}

		if (newTarget != null)
		{

			// Rotation berechnen

			float direction = get_angle(ScreenCenter.x, ScreenCenter.y, newTarget.x, newTarget.y);

			// direction -= 180;
			//
			// direction = 360 - direction;

			direction = 180 - direction;

			// draw sprite
			Sprite arrow = SpriteCache.MapArrows.get(4);
			arrow.setRotation(direction);

			arrow.setBounds(newTarget.x - GL_UISizes.TargetArrow.halfWidth, newTarget.y - GL_UISizes.TargetArrow.height,
					GL_UISizes.TargetArrow.width, GL_UISizes.TargetArrow.height);

			arrow.setOrigin(GL_UISizes.TargetArrow.halfWidth, GL_UISizes.TargetArrow.height);
			arrow.draw(batch);
		}

	}

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

	private void renderWPs(SizeF WpUnderlay, SizeF WpSize)
	{
		if (mapCacheList.list != null)
		{
			synchronized (mapCacheList.list)
			{
				for (WaypointRenderInfo wpi : mapCacheList.list)
				{
					Vector2 screen = worldToScreen(new Vector2(wpi.MapX, wpi.MapY));

					float NameYMovement = 0;

					if ((aktZoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == GlobalCore.SelectedWaypoint()))
					{
						// Draw Cross and move screen vector
						Sprite cross = SpriteCache.MapOverlay.get(3);
						cross.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width,
								WpUnderlay.height);
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
						rating.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight - WpUnderlay.Height4_8,
								WpUnderlay.width, WpUnderlay.Height4_8);
						rating.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
						rating.setRotation(0);
						rating.draw(batch);
						NameYMovement += WpUnderlay.Height4_8;
					}

					// Beschriftung
					if (showTitles && (aktZoom >= 15) && (!drawAsWaypoint))
					{
						float halfWidth = Fonts.get16_Out().getBounds(wpi.Cache.Name).width / 2;
						Fonts.get16_Out().draw(batch, wpi.Cache.Name, screen.x - halfWidth,
								screen.y - WpUnderlay.halfHeight - NameYMovement);
					}

					// Show D/T-Rating
					if (showDT && (!drawAsWaypoint) && (aktZoom >= 15))
					{
						Sprite difficulty = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Difficulty * 2, 5 * 2));
						difficulty.setBounds(screen.x - WpUnderlay.width - GL_UISizes.infoShadowHeight, screen.y
								- (WpUnderlay.Height4_8 / 2), WpUnderlay.width, WpUnderlay.Height4_8);
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

					if ((wpi.Cache.Id == Bubble.CacheId) && (wpi.Waypoint == Bubble.waypoint) && Bubble.isShow)
					{
						Bubble.Pos = screen;
					}

				}
			}
		}

	}

	private boolean renderBiggerTiles(SpriteBatch batch2, int i, int j, int zoom2)
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

	private void renderSmallerTiles(SpriteBatch batch2, int i, int j, int zoom2)
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
				else if ((zoomzoom <= aktZoom + 0) && (zoomzoom <= maxMapZoom))
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
		deleteUnusedTiles();
		// alle notwendigen Tiles zum Laden einstellen in die Queue
		// (queuedTiles)
		int extensionTop = width / 2;
		int extensionBottom = width / 2;
		int extensionLeft = width / 2;
		int extensionRight = width / 2;
		Descriptor lo = screenToDescriptor(new Vector2(width / 2 - drawingWidth / 2 - extensionLeft, height / 2 - drawingHeight / 2
				- extensionTop), aktZoom);
		Descriptor ru = screenToDescriptor(new Vector2(width / 2 + drawingWidth / 2 + extensionRight, height / 2 + drawingHeight / 2
				+ extensionBottom), aktZoom);

		mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), aktZoom, false);

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

	private void stateChanged()
	{
		// if (btnTrackPos.getState() > 0)
		{
			setCenter(new Coordinate(GlobalCore.LastValidPosition.Latitude, GlobalCore.LastValidPosition.Longitude));
		}
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
						if (Database.Data.Query != null)
						{
							if (Database.Data.Query.size() > 0)
							{
								// Koordinaten des ersten Caches der Datenbank
								// nehmen
								setCenter(new Coordinate(Database.Data.Query.get(0).Latitude(), Database.Data.Query.get(0).Longitude()));
								positionInitialized = true;
								// setLockPosition(0);
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
		nightMode = Config.settings.nightMode.getValue();
		iconFactor = (float) Config.settings.MapViewDPIFaktor.getValue();
		aktZoom = Config.settings.lastZoomLevel.getValue();
		zoomBtn.setMaxZoom(Config.settings.OsmMaxLevel.getValue());
		zoomBtn.setMinZoom(Config.settings.OsmMinLevel.getValue());
		zoomBtn.setZoom(aktZoom);

		// zoomScale.setMaxZoom(Config.settings.OsmMaxLevel.getValue());
		// zoomScale.setMinZoom(Config.settings.OsmMinLevel.getValue());
		// zoomScale.setZoom(aktZoom);
	}

	public void saveToSettings()
	{
		Config.settings.lastZoomLevel.setValue(aktZoom);

	}

	private void setScreenCenter(Vector2 newCenter)
	{
		screenCenterW.x = newCenter.x;
		screenCenterW.y = -newCenter.y;
		if (camera != null) camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);
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

			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(maxMapZoom, center.Longitude),
					Descriptor.LatitudeToTileY(maxMapZoom, center.Latitude), maxMapZoom, maxMapZoom);

			setScreenCenter(new Vector2((float) point.X, (float) point.Y));
		}
	}

	private long getMapTileSizeFactor(int zoom)
	{
		long result = 1;
		for (int z = maxMapZoom; z < zoom; z++)
		{
			result *= 2;
		}
		return result;
	}

	private long getMapTilePosFactor(int zoom)
	{
		long result = 1;
		for (int z = zoom; z < maxMapZoom; z++)
		{
			result *= 2;
		}
		return result;
	}

	/**
	 * liefert die World-Koordinate in Pixel relativ zur Map in der höchsten Auflösung
	 */
	private Vector2 screenToWorld(Vector2 point)
	{
		Vector2 result = new Vector2(0, 0);
		result.x = screenCenterW.x + (point.x - width / 2) * camera.zoom;
		result.y = -screenCenterW.y + (point.y - height / 2) * camera.zoom;
		return result;
	}

	private Vector2 worldToScreen(Vector2 point)
	{
		Vector2 result = new Vector2(0, 0);
		result.x = (point.x - screenCenterW.x) / camera.zoom + width / 2;
		result.y = -(-point.y + screenCenterW.y) / camera.zoom + height / 2;
		result.add(-width / 2, -height / 2);
		result.rotate(mapHeading);
		result.add(width / 2, height / 2);
		return result;
	}

	private Descriptor screenToDescriptor(Vector2 point, int zoom)
	{
		// World-Koordinaten in Pixel
		Vector2 world = screenToWorld(point);
		for (int i = maxMapZoom; i > zoom; i--)
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
		this.locator = locator;
		GlobalCore.LastValidPosition = new Coordinate(locator.Position.Latitude, locator.Position.Longitude);
		GlobalCore.LastValidPosition.Elevation = locator.getAlt();

		// xxx if (btnTrackPos.getState() > 0) setCenter(new Coordinate(locator.Position.Latitude, locator.Position.Longitude));
	}

	@Override
	public void OrientationChanged(float heading)
	{
		// // liefert die Richtung (abhängig von der Geschwindigkeit von
		// // Kompass oder GPS
		// if (!Global.Locator.UseCompass())
		// {
		// // GPS-Richtung soll verwendet werden!
		// heading = Global.Locator.getHeading();
		// }

		// getHeading() entscheidet schon ob das Heading von GPS oder Hardware kommt!
		heading = locator.getHeading();

		if (alignToCompass)
		{
			this.mapHeading = heading;
			this.arrowHeading = 0;

			// da die Map gedreht in die offScreenBmp gezeichnet werden soll,
			// muss der Bereich, der gezeichnet werden soll größer sein, wenn
			// gedreht wird.
			if (heading >= 180) heading -= 180;
			if (heading > 90) heading = 180 - heading;
			double alpha = heading / 180 * Math.PI;
			double beta = Math.atan((double) width / (double) height);
			double gammaW = Math.PI / 2 - alpha - beta;
			// halbe Länge der Diagonalen
			double diagonal = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)) / 2;
			drawingWidth = (int) (Math.cos(gammaW) * diagonal * 2);

			double gammaH = alpha - beta;
			drawingHeight = (int) (Math.cos(gammaH) * diagonal * 2);
		}
		else
		{
			this.mapHeading = 0;
			this.arrowHeading = heading;
			drawingWidth = width;
			drawingHeight = height;
		}

	}

	public void SetAlignToCompass(boolean value)
	{
		alignToCompass = value;
		if (!value)
		{
			drawingWidth = width;
			drawingHeight = height;
		}
	}

	public boolean GetCenterGps()
	{
		// xxx return (btnTrackPos.getState() > 0);
		return false;
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
						}
						catch (Exception ex1)
						{
							Logger.Error("MapViewGL.queueProcessor.doInBackground()", "1", ex1);
						}
					}
					else
					{
						Thread.sleep(100);
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
			bytes = ManagerBase.Manager.LoadLocalPixmap(CurrentLayer, desc);
		}
		// byte[] bytes = MapManagerEventPtr.OnGetMapTile(CurrentLayer, desc);
		// Texture texture = new Texture(new Pixmap(bytes, 0, bytes.length));
		if (bytes != null)
		{
			tileState = TileGL.TileState.Present;
			addLoadedTile(desc, bytes, tileState);
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
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		y = height - y;
		debugString = "touchDown " + x + " - " + y;
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

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		y = height - y;
		debugString = "touchDragged: " + x + " - " + y;
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
			// xxx startTimer(frameRateAction);
			// debugString = "";
			long faktor = getMapTilePosFactor(aktZoom);
			// debugString += faktor;
			Point lastPoint = (Point) fingerDown.values().toArray()[0];
			// debugString += " - " + (lastPoint.x - x) * faktor + " - " + (y - lastPoint.y) * faktor;

			camera.position.add((lastPoint.x - x) * faktor, (y - lastPoint.y) * faktor, 0);
			// debugString = camera.position.x + " - " + camera.position.y;
			screenCenterW.x = camera.position.x;
			screenCenterW.y = camera.position.y;
			calcCenter();
			if (kineticPan == null) kineticPan = new KineticPan();
			kineticPan.setLast(System.currentTimeMillis(), x, y);

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

			endCameraZoom = camera.zoom;

			System.out.println(camera.zoom);
			int zoom = maxMapZoom;
			float tmpZoom = camera.zoom;
			float faktor = 1.5f;
			faktor = faktor - iconFactor + 1;
			while (tmpZoom > faktor)
			{
				tmpZoom /= 2;
				zoom--;
			}
			zoomBtn.setZoom(zoom);
			// xxx zoomScale.resetFadeOut();
			// xxx zoomScale.setZoom(zoom);
			// xxx zoomScale.setDiffCameraZoom(1 - (tmpZoom * 2), true);
			aktZoom = zoom;

			// debugString = currentDistance + " - " + originalDistance;
			return false;
		}

		// debugString = "State: " + inputState;
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		y = height - y;
		debugString = "touchUp: " + x + " - " + y;
		// debugString = "touchUp " + inputState.toString();
		if (inputState == InputState.IdleDown)
		{
			// es wurde gedrückt, aber nich verschoben
			fingerDown.remove(pointer);
			inputState = InputState.Idle;
			// -> Buttons testen

			double minDist = Double.MAX_VALUE;
			WaypointRenderInfo minWpi = null;
			Vector2 clickedAt = new Vector2(x, height - y);

			// auf Button Clicks nur reagieren, wenn aktuell noch kein Finger gedrückt ist!!!
			if (kineticPan != null)
			// bei FingerKlick (wenn Idle) sofort das kinetische Scrollen stoppen
			kineticPan = null;

			// check ToggleBtn clicked
			// xxx if (btnTrackPos.hitTest(clickedAt))
			if (false)
			{
				// xxx main.vibrate();
				stateChanged();
				inputState = InputState.Idle;
				// debugString = "State: " + inputState;
				return false;
			}

			// check Zoom Button clicked
			if (zoomBtn.hitTest(clickedAt))
			{
				// xxx zoomScale.setZoom(zoomBtn.getZoom());
				// xxx zoomScale.resetFadeOut();
				inputState = InputState.Idle;
				// debugString = "State: " + inputState;
				// aktZoom = zoomBtn.getZoom();
				// camera.zoom = getMapTilePosFactor(aktZoom);
				kineticZoom = new KineticZoom(camera.zoom, getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(),
						System.currentTimeMillis() + 1000);
				// xxx startTimer(frameRateAction);

				return false;
			}

			synchronized (mapCacheList.list)
			{
				// Bubble gedrückt?
				// xxx if ((Bubble.cache != null) && Bubble.isShow && Bubble.DrawRec != null && Bubble.DrawRec.contains(clickedAt.x,
				// clickedAt.y))
				if (false)
				{
					// Click inside Bubble -> hide Bubble and select Cache
					// GlobalCore.SelectedWaypoint(Bubble.cache,
					// Bubble.waypoint);
					// ThreadSaveSetSelectedWP(Bubble.cache, Bubble.waypoint);
					// CacheDraw.ReleaseCacheBMP();
					Bubble.isShow = false;
					Bubble.CacheId = -1;
					Bubble.cache = null;
					Bubble.waypoint = null;
					mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), aktZoom, true);

					// Shutdown Autoresort
					// Global.autoResort = false;
					inputState = InputState.Idle;
					// debugString = "State: " + inputState;
					// do nothing else with this click
					return false;
				}
				else if (Bubble.isShow)
				{
					// Click outside Bubble -> hide Bubble
					Bubble.isShow = false;
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

				if (minWpi == null || minWpi.Cache == null) return false;

				if (minDist < 40)
				{

					if (minWpi.Waypoint != null)
					{
						if (GlobalCore.SelectedCache() != minWpi.Cache)
						{
							// Show Bubble
							Bubble.isShow = true;
							Bubble.CacheId = minWpi.Cache.Id;
							Bubble.cache = minWpi.Cache;
							Bubble.waypoint = minWpi.Waypoint;
							Bubble.disposeSprite();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), aktZoom, true);

						}
						else
						{
							// do not show Bubble because there will not be
							// selected
							// a
							// different cache but only a different waypoint
							// Wegpunktliste ausrichten
							// xxx ThreadSaveSetSelectedWP(minWpi.Cache, minWpi.Waypoint);
							// FormMain.WaypointListPanel.AlignSelected();
							// updateCacheList();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), aktZoom, true);
						}

					}
					else
					{
						if (GlobalCore.SelectedCache() != minWpi.Cache)
						{
							Bubble.isShow = true;
							Bubble.CacheId = minWpi.Cache.Id;
							Bubble.cache = minWpi.Cache;
							Bubble.disposeSprite();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), aktZoom, true);
						}
						else
						{
							Bubble.isShow = true;
							Bubble.CacheId = minWpi.Cache.Id;
							Bubble.cache = minWpi.Cache;
							Bubble.disposeSprite();
							// Cacheliste ausrichten
							// xxx ThreadSaveSetSelectedWP(minWpi.Cache);
							// updateCacheList();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), aktZoom, true);
						}
					}
					inputState = InputState.Idle;
					// debugString = "State: " + inputState;
					// return false;
				}
			}
			inputState = InputState.Idle;
			return false;
		}

		fingerDown.remove(pointer);
		if (fingerDown.size() == 1) inputState = InputState.Pan;
		else if (fingerDown.size() == 0)
		{
			inputState = InputState.Idle;
			// wieder langsam rendern
			// xxx ((GLSurfaceView) MapViewGL.ViewGl).requestRender();

			// xxx if ((kineticZoom == null) && (kineticPan == null)) startTimer(frameRateIdle);
			if (kineticPan != null) kineticPan.start();
		}

		// debugString = "State: " + inputState;

		return false;
	}

	private void calcCenter()
	{
		// berechnet anhand des ScreenCenterW die Center-Coordinaten
		PointD point = Descriptor.FromWorld(screenCenterW.x, screenCenterW.y, maxMapZoom, maxMapZoom);

		center = new Coordinate(Descriptor.TileYToLatitude(maxMapZoom, -point.Y), Descriptor.TileXToLongitude(maxMapZoom, point.X));
	}

	// @Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		// xxx if (Global.autoResort) return;

		if (cache == null) return;
		/*
		 * if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint }); return; }
		 */
		positionInitialized = true;

		// xxx btnTrackPos.setState(0, true);
		Coordinate target = (waypoint != null) ? new Coordinate(waypoint.Latitude(), waypoint.Longitude()) : new Coordinate(
				cache.Latitude(), cache.Longitude());

		setCenter(target);

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

}
