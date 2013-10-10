package CB_Locator.Map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;

import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Locator.LocatorSettings;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_UI_Base.Events.invalidateTextureEvent;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.DrawUtils;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.ZoomButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Logger;
import CB_Utils.Math.Point;
import CB_Utils.Math.PointD;
import CB_Utils.Math.PointL;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public abstract class MapViewBase extends CB_View_Base implements PositionChangedEvent, invalidateTextureEvent
{
	public static int INITIAL_SETTINGS = 1;
	public static int INITIAL_THEME = 2;
	public static int INITIAL_ALL = 7;
	public static int INITIAL_WITH_OUT_ZOOM = 8;
	public static int INITIAL_SETTINGS_WITH_OUT_ZOOM = 9;
	public static final boolean debug = false;

	// ######### theme Path ###############
	public static String PathDefault;
	public static String PathCustom;
	public static String PathDefaultNight;
	public static String PathCustomNight;

	// ######################################

	public enum MapState
	{
		FREE, GPS, WP, LOCK, CAR
	}

	protected class KineticPan
	{
		boolean started;
		private boolean fertig;
		// benutze den Abstand der letzten 5 Positions�nderungen
		final int anzPoints = 3;
		private final int[] x = new int[anzPoints];
		private final int[] y = new int[anzPoints];
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
		private final float startZoom;
		private final float endZoom;
		private final long startTime;
		private final long endTime;
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

	private MapState mapState = MapState.FREE;

	protected final int ZoomTime = 1000;
	protected ZoomButtons zoomBtn;
	protected ZoomScale zoomScale;
	protected MapScale mapScale;
	public static MapTileLoader mapTileLoader = new MapTileLoader();
	// protected boolean alignToCompass = false;
	private float mapHeading = 0;
	protected float arrowHeading = 0;
	private final Point lastMovement = new Point(0, 0);
	protected Vector2 myPointOnScreen;
	protected boolean showAccuracyCircle;
	public int aktZoom;
	protected KineticZoom kineticZoom = null;
	private KineticPan kineticPan = null;
	protected float maxTilesPerScreen = 0;
	long posx = 8745;
	long posy = 5685;
	public PointL screenCenterW = new PointL(0, 0);
	PointL screenCenterT = new PointL(0, 0);
	protected int mapIntWidth;
	protected int mapIntHeight;
	protected int drawingWidth;
	protected int drawingHeight;
	long pos20y = 363904;
	long size20 = 256;
	public Coordinate center = new Coordinate(48.0, 12.0);
	protected boolean positionInitialized = false;
	protected OrthographicCamera camera;
	long startTime;
	Timer myTimer;
	boolean useNewInput = true;
	protected float ySpeedVersatz = 200;
	protected boolean CarMode = false;
	boolean NightMode = false;
	protected boolean NorthOriented = true;

	protected iChanged themeChangedEventHandler = new iChanged()
	{

		@Override
		public void isChanged()
		{
			MapViewBase.this.invalidateTexture();
		}
	};
	protected SortedMap<Long, TileGL> tilesToDraw = new TreeMap<Long, TileGL>();
	protected SortedMap<Long, TileGL> overlayToDraw = new TreeMap<Long, TileGL>();
	int debugcount = 0;
	private Sprite AccuracySprite;
	private int actAccuracy = 0;
	private float actPixelsPerMeter = 0;
	protected float iconFactor = 1.5f;
	protected boolean showMapCenterCross;
	Sprite crossLine = null;
	String str = "";

	public boolean GetNightMode()
	{
		return this.NightMode;
	}

	public void SetNightMode(boolean NightMode)
	{
		this.NightMode = NightMode;
	}

	public boolean GetNorthOriented()
	{
		return this.NorthOriented;
	}

	public void SetNorthOriented(boolean NorthOriented)
	{
		this.NorthOriented = NorthOriented;
	}

	@Override
	public void onShow()
	{
		PositionChangedEventList.Add(this);
		PositionChanged();

		CarMode = (getMapState() == MapState.CAR);
		if (!CarMode)
		{
			drawingWidth = mapIntWidth;
			drawingHeight = mapIntHeight;
		}

		setVisible();

		int zoom = MapTileLoader.MAX_MAP_ZOOM;
		float tmpZoom = camera.zoom;
		float faktor = 1.5f;
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
		PositionChangedEventList.Remove(this);
		setInvisible();
		onStop();// save last zoom and position
	}

	@Override
	public void dispose()
	{
		// remove eventHandler
		PositionChangedEventList.Remove(this);
		invalidateTextureEventList.Remove(this);
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		// wenn sich die Gr��e nicht ge�ndert hat, brauchen wir nicht zu machen!
		if (rec.getWidth() == this.mapIntWidth && rec.getHeight() == this.mapIntHeight)
		{
			// Ausser wenn Camera == null!
			if (camera != null) return;
		}
		this.mapIntWidth = (int) rec.getWidth();
		this.mapIntHeight = (int) rec.getHeight(); // Gdx.graphics.getHeight();
		this.drawingWidth = (int) rec.getWidth();
		this.drawingHeight = (int) rec.getHeight();

		camera = new OrthographicCamera(MainViewBase.mainView.getWidth(), MainViewBase.mainView.getHeight());

		aktZoom = zoomBtn.getZoom();

		camera.zoom = mapTileLoader.getMapTilePosFactor(aktZoom);

		camera.position.set(0, 0, 0);

		// Logger.LogCat("MapView Size Changed MaxY=" + this.getMaxY());

		requestLayout();

	}

	@Override
	public void onStop()
	{
		LocatorSettings.MapInitLatitude.setValue(center.getLatitude());
		LocatorSettings.MapInitLongitude.setValue(center.getLongitude());
		LocatorSettings.lastZoomLevel.setValue(aktZoom);
		super.onStop();
	}

	public void SetCurrentLayer(Layer newLayer)
	{
		if (newLayer == null)
		{
			LocatorSettings.CurrentMapLayer.setValue("");
		}
		else
		{
			LocatorSettings.CurrentMapLayer.setValue(newLayer.Name);
		}

		mapTileLoader.CurrentLayer = newLayer;
		mapTileLoader.clearLoadedTiles();
	}

	public void SetCurrentOverlayLayer(Layer newLayer)
	{
		if (newLayer == null)
		{
			LocatorSettings.CurrentMapOverlayLayer.setValue("");
		}
		else
		{
			LocatorSettings.CurrentMapOverlayLayer.setValue(newLayer.Name);
		}

		mapTileLoader.CurrentOverlayLayer = newLayer;
		mapTileLoader.clearLoadedTiles();
	}

	protected float scale;
	protected int outScreenDraw = 0;
	public static int INITIAL_NEW_SETTINGS = 3;

	@Override
	protected void render(SpriteBatch batch)
	{

		if (LocatorSettings.MoveMapCenterWithSpeed.getValue() && CarMode && Locator.hasSpeed())
		{

			double maxSpeed = LocatorSettings.MoveMapCenterMaxSpeed.getValue();

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
			if (zoomScale != null) zoomScale.setZoom(mapTileLoader.convertCameraZommToFloat(camera));

		}

		if ((kineticPan != null) && (kineticPan.started))
		{
			long faktor = mapTileLoader.getMapTilePosFactor(aktZoom);
			Point pan = kineticPan.getAktPan();
			// debugString = pan.x + " - " + pan.y;
			// camera.position.add(pan.x * faktor, pan.y * faktor, 0);
			// screenCenterW.x = camera.position.x;
			// screenCenterW.y = camera.position.y;
			screenCenterT.x += pan.x * faktor;
			screenCenterT.y += pan.y * faktor;
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
	}

	protected abstract void renderOverlay(SpriteBatch batch);

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

		if (!this.NorthOriented || CarMode)
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
			// das Alter aller Tiles um 1 erh�hen
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
					Descriptor desc = new Descriptor(i, j, tmpzoom, this.NightMode);
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
						// f�r den richtigen aktuellen Zoom ist
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
						// f�r den aktuellen Zoom ist kein Tile vorhanden ->
						// kleinere Zoomfaktoren durchsuchen
						if (!renderBiggerTiles(batch, i, j, aktZoom))
						// gr��ere Zoomfaktoren noch durchsuchen, ob davon Tiles
						// vorhanden sind...
						// daf�r m�ssen aber pro fehlendem Tile mehrere kleine
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
				if (tile.canDraw())
				{
					// Faktor, mit der dieses MapTile vergr��ert gezeichnet
					// werden mu�
					long posFactor = mapTileLoader.getMapTilePosFactor(tile.Descriptor.Zoom);

					long xPos = tile.Descriptor.X * posFactor * 256 - screenCenterW.x;
					long yPos = -(tile.Descriptor.Y + 1) * posFactor * 256 - screenCenterW.y;
					float xSize = tile.getWidth() * posFactor;
					float ySize = tile.getHeight() * posFactor;
					tile.draw(batch, xPos, yPos, xSize, ySize);
				}
			}
		}
		tilesToDraw.clear();
		batch.enableBlending();
		synchronized (screenCenterW)
		{
			for (TileGL tile : overlayToDraw.values())
			{
				if (tile.canDraw())
				{
					// Faktor, mit der dieses MapTile vergr��ert gezeichnet
					// werden mu�
					long posFactor = mapTileLoader.getMapTilePosFactor(tile.Descriptor.Zoom);

					long xPos = tile.Descriptor.X * posFactor * 256 - screenCenterW.x;
					long yPos = -(tile.Descriptor.Y + 1) * posFactor * 256 - screenCenterW.y;
					float xSize = tile.getWidth() * posFactor;
					float ySize = tile.getHeight() * posFactor;
					tile.draw(batch, xPos, yPos, xSize, ySize);
				}
			}
		}
		overlayToDraw.clear();

	}

	@SuppressWarnings("unused")
	protected void renderDebugInfo(SpriteBatch batch)
	{

		if (showMapCenterCross)
		{
			if (getMapState() == MapState.FREE)
			{
				if (crossLine == null)
				{
					crossLine = SpriteCacheBase.getThemedSprite("pixel2x2");
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

	protected void renderPositionMarker(SpriteBatch batch)
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
				if (radius > 0 && radius < UI_Size_Base.that.getSmallestWidth())
				{

					try
					{
						int squaredR = radius * 2;

						if (squaredR > SpriteCacheBase.Accuracy[2].getWidth()) AccuracySprite = new Sprite(SpriteCacheBase.Accuracy[2]);
						else if (squaredR > SpriteCacheBase.Accuracy[1].getWidth()) AccuracySprite = new Sprite(SpriteCacheBase.Accuracy[1]);
						else
							AccuracySprite = new Sprite(SpriteCacheBase.Accuracy[0]);
						if (AccuracySprite != null) AccuracySprite.setSize(squaredR, squaredR);
					}
					catch (Exception e)
					{
						Logger.Error("MapView.renderPositionMarker()", "set AccuracySprite", e);
					}

				}

			}

			if (AccuracySprite != null && AccuracySprite.getWidth() > GL_UISizes.PosMarkerSize)
			{// nur wenn berechnet wurde und gr�sser als der PosMarker

				float center = AccuracySprite.getWidth() / 2;

				AccuracySprite.setPosition(myPointOnScreen.x - center, myPointOnScreen.y - center);
				AccuracySprite.draw(batch);
			}
		}

		boolean lastUsedCompass = Locator.UseMagneticCompass();
		boolean Transparency = LocatorSettings.PositionMarkerTransparent.getValue();

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

		Sprite arrow = SpriteCacheBase.Arrows.get(arrowId);
		arrow.setRotation(-arrowHeading);
		arrow.setBounds(myPointOnScreen.x - GL_UISizes.halfPosMarkerSize, myPointOnScreen.y - GL_UISizes.halfPosMarkerSize,
				GL_UISizes.PosMarkerSize, GL_UISizes.PosMarkerSize);
		arrow.setOrigin(GL_UISizes.halfPosMarkerSize, GL_UISizes.halfPosMarkerSize);
		arrow.draw(batch);

	}

	protected float get_angle(float x1, float y1, float x2, float y2)
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

	private boolean renderBiggerTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// f�r den aktuellen Zoom ist kein Tile vorhanden -> kleinere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// von dem gefundenen Tile mu� dann nur ein Ausschnitt gezeichnet werden
		int ii = i / 2;
		int jj = j / 2;
		int zoomzoom = zoom2 - 1;

		Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
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
			// f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
			// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
			// sind...
			// daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
			// gezeichnet werden (4 oder 16 oder 64...)
			// dieser Aufruf kann auch rekursiv sein...
			renderBiggerTiles(batch, ii, jj, zoomzoom);
		}
		return false;
	}

	private boolean renderBiggerOverlayTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// f�r den aktuellen Zoom ist kein Tile vorhanden -> kleinere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// von dem gefundenen Tile mu� dann nur ein Ausschnitt gezeichnet werden
		int ii = i / 2;
		int jj = j / 2;
		int zoomzoom = zoom2 - 1;

		Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
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
			// f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
			// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
			// sind...
			// daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
			// gezeichnet werden (4 oder 16 oder 64...)
			// dieser Aufruf kann auch rekursiv sein...
			renderBiggerOverlayTiles(batch, ii, jj, zoomzoom);
		}
		return false;
	}

	private void renderSmallerTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles gezeichnet
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
				Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
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
					// f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
					// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
					// sind...
					// daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
					// gezeichnet werden (4 oder 16 oder 64...)
					// dieser Aufruf kann auch rekursiv sein...
					renderSmallerTiles(batch, ii, jj, zoomzoom);
				}
			}
		}
	}

	private void renderSmallerOverlayTiles(SpriteBatch batch, int i, int j, int zoom2)
	{
		// f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
		// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden sind...
		// daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles gezeichnet
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
				Descriptor desc = new Descriptor(ii, jj, zoomzoom, this.NightMode);
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
					// f�r den aktuellen Zoom ist kein Tile vorhanden -> gr��ere
					// Zoomfaktoren noch durchsuchen, ob davon Tiles vorhanden
					// sind...
					// daf�r m�ssen aber pro fehlendem Tile mehrere kleine Tiles
					// gezeichnet werden (4 oder 16 oder 64...)
					// dieser Aufruf kann auch rekursiv sein...
					renderSmallerOverlayTiles(batch, ii, jj, zoomzoom);
				}
			}
		}
	}

	protected void loadTiles()
	{
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
		zoomBtn.setZoom(LocatorSettings.lastZoomLevel.getValue());
		// Bestimmung der ersten Position auf der Karte
		if (!positionInitialized)
		{
			double lat = LocatorSettings.MapInitLatitude.getValue();
			double lon = LocatorSettings.MapInitLongitude.getValue();

			// Initialisierungskoordinaten bekannt und k�nnen �bernommen werden
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
					setInitialLocation();
				}
			}
		}

		setNewSettings(INITIAL_ALL);

	}

	/**
	 * Set the Initial location now. If you wont.
	 */
	protected abstract void setInitialLocation();

	protected float lastDynamicZoom = -1;

	public enum InputState
	{
		Idle, IdleDown, Button, Pan, Zoom, PanAutomatic, ZoomAutomatic
	}

	protected InputState inputState = InputState.Idle;
	private final HashMap<Integer, Point> fingerDown = new LinkedHashMap<Integer, Point>();
	private static String debugString = "";

	public void setNewSettings(int InitialFlags)
	{
		if ((InitialFlags & INITIAL_SETTINGS) != 0)
		{
			showAccuracyCircle = LocatorSettings.ShowAccuracyCircle.getValue();
			showMapCenterCross = LocatorSettings.ShowMapCenterCross.getValue();

			if (InitialFlags == INITIAL_ALL)
			{
				iconFactor = LocatorSettings.MapViewDPIFaktor.getValue();

				int setAktZoom = LocatorSettings.lastZoomLevel.getValue();
				int setMaxZoom = LocatorSettings.OsmMaxLevel.getValue();
				int setMinZoom = LocatorSettings.OsmMinLevel.getValue();

				aktZoom = setAktZoom;
				zoomBtn.setMaxZoom(setMaxZoom);
				zoomBtn.setMinZoom(setMinZoom);
				zoomBtn.setZoom(aktZoom);

				if (zoomScale != null)
				{
					zoomScale.setMaxZoom(setMaxZoom);
					zoomScale.setMinZoom(setMinZoom);
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
				if (this.NightMode)
				{
					// zuerst schauen, ob ein Render Theme im Custom Skin Ordner Liegt
					themePath = ifCarThemeExist(PathCustomNight);

					if (themePath == null)
					{// wenn kein Night Custum skin vorhanden, dann nach einem Day CostumTheme suchen, welches dann Invertiert wird!
						themePath = ifCarThemeExist(PathCustom);
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
					themePath = ifCarThemeExist(PathCustom);

					if (themePath == null) themePath = ifCarThemeExist(PathDefault);
				}

				if (themePath == null)
				{
					themePath = ManagerBase.INTERNAL_CAR_THEME;
				}

			}

			if (themePath == null)
			{

				// Entweder wir sind nicht im CarMode oder es wurde kein Passender Theme f�r den CarMode gefunden!
				if (this.NightMode)
				{
					themePath = ifThemeExist(LocatorSettings.MapsforgeNightTheme.getValue());
				}

				if (themePath == null)
				{
					if (this.NightMode) useInvertNightTheme = true;
					themePath = ifThemeExist(LocatorSettings.MapsforgeDayTheme.getValue());

				}

			}

			if (themePath != null)
			{
				ManagerBase.Manager.setUseInvertedNightTheme(useInvertNightTheme);
				ManagerBase.Manager.setRenderTheme(themePath);
			}
			else
			{
				// set Theme to null
				ManagerBase.Manager.setUseInvertedNightTheme(useInvertNightTheme);
				ManagerBase.Manager.clearRenderTheme();
			}

		}
	}

	protected String ifThemeExist(String Path)
	{
		if (FileIO.FileExists(Path) && FileIO.GetFileExtension(Path).contains("xml")) return Path;
		return null;
	}

	protected String ifCarThemeExist(String Path)
	{
		Path = Path + "CarTheme.xml";
		return ifThemeExist(Path);
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
			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, center.getLongitude()),
					Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, center.getLatitude()), MapTileLoader.MAX_MAP_ZOOM,
					MapTileLoader.MAX_MAP_ZOOM);

			setScreenCenter(new Vector2((float) point.X, (float) point.Y));
		}
	}

	/**
	 * liefert die World-Koordinate in Pixel relativ zur Map in der h�chsten Aufl�sung
	 */
	protected Vector2 screenToWorld(Vector2 point)
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
			// wenn hier ein Fehler auftritt, dann geben wir einen Vector 0,0 zur�ck!
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
		Descriptor result = new Descriptor(x, y, zoom, NightMode);
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
		if (getCenterGps()) setCenter(Locator.getCoordinate());

		GL.that.renderOnce(MapViewBase.this.getName() + " Position Changed");
	}

	public float pixelsPerMeter = 0;

	@Override
	public void OrientationChanged()
	{

		float heading = Locator.getHeading();

		// im CarMode keine Richtungs �nderungen unter 20kmh
		if (CarMode && Locator.SpeedOverGround() < 20) heading = this.mapHeading;

		if (!this.NorthOriented || CarMode)
		{
			this.mapHeading = heading;
			this.arrowHeading = 0;

			// da die Map gedreht in die offScreenBmp gezeichnet werden soll,
			// muss der Bereich, der gezeichnet werden soll gr��er sein, wenn
			// gedreht wird.
			if (heading >= 180) heading -= 180;
			if (heading > 90) heading = 180 - heading;
			double alpha = heading / 180 * Math.PI;
			double mapHeightCalcBase = mapIntHeight + (ySpeedVersatz * 1.7);
			double mapWidthCalcBase = mapIntWidth + (ySpeedVersatz * 1.7);
			double beta = Math.atan(mapWidthCalcBase / mapHeightCalcBase);
			double gammaW = Math.PI / 2 - alpha - beta;
			// halbe L�nge der Diagonalen
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

		GL.that.renderOnce(MapViewBase.this.getName() + " OrientationChanged");
	}

	public void SetAlignToCompass(boolean value)
	{
		if (!value)
		{
			drawingWidth = mapIntWidth;
			drawingHeight = mapIntHeight;
		}
		this.NorthOriented = !value;
	}

	/**
	 * Returns True, if MapState <br>
	 * MapState.GPS<br>
	 * MapState.LOCK<br>
	 * MapState.CAR<br>
	 * 
	 * @return
	 */
	public boolean getCenterGps()
	{
		return mapState != MapState.FREE && mapState != MapState.WP;
	}

	public boolean GetAlignToCompass()
	{
		return !this.NorthOriented;
	}

	public MapViewBase(String Name)
	{
		super(Name);
	}

	public MapViewBase(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public MapViewBase(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height, Parent, Name);
	}

	public MapViewBase(CB_RectF rec, String Name)
	{
		super(rec, Name);
		invalidateTextureEventList.Add(this);
	}

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

			int maxZoom = LocatorSettings.OsmMaxLevel.getValue();
			int minZoom = LocatorSettings.OsmMinLevel.getValue();
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

				GL.that.addRenderView(MapViewBase.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce(MapViewBase.this.getName() + " ZoomButtonClick");
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
				// es wurde 1x gedr�ckt -> testen, ob ein gewisser Minimum Bereich verschoben wurde
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
				// wenn ein Button gedr�ckt war -> beim Verschieben nichts machen!!!
				return false;
			}

			if ((inputState == InputState.Pan) && (fingerDown.size() == 1))
			{

				if (getMapState() == MapState.CAR || getMapState() == MapState.LOCK)
				{
					// f�r verschieben gesperrt!
					return false;
				}
				else
				{
					// auf GPS oder WP ausgerichtet und wird jetzt auf Free gestellt
					SetMapStateFree();
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

					// Pan stufenlos anpassen an den aktuell g�ltigen Zoomfaktor
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

				if (!CarMode && zoomScale != null)
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

	protected void SetMapStateFree()
	{
		setMapState(MapState.FREE);
	}

	protected void setZoomScale(int zoom)
	{
		if (!CarMode && zoomScale != null) zoomScale.setZoom(zoom);
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
			// es wurde gedr�ckt, aber nich verschoben
			fingerDown.remove(pointer);
			inputState = InputState.Idle;
			// -> Buttons testen

			// auf Button Clicks nur reagieren, wenn aktuell noch kein Finger gedr�ckt ist!!!
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

	public MapViewBase(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec, Parent, Name);
	}

	protected void calcCenter()
	{
		// berechnet anhand des ScreenCenterW die Center-Coordinaten
		PointD point = Descriptor.FromWorld(screenCenterW.x, screenCenterW.y, MapTileLoader.MAX_MAP_ZOOM, MapTileLoader.MAX_MAP_ZOOM);

		center = new Coordinate(Descriptor.TileYToLatitude(MapTileLoader.MAX_MAP_ZOOM, -point.Y), Descriptor.TileXToLongitude(
				MapTileLoader.MAX_MAP_ZOOM, point.X));
	}

	public MapViewBase(SizeF size, String Name)
	{
		super(size, Name);
	}

	protected void calcPixelsPerMeter()
	{

		float calcZoom = mapTileLoader.convertCameraZommToFloat(camera);

		Coordinate dummy = Coordinate.Project(center.getLatitude(), center.getLongitude(), 90, 1000);
		double l1 = Descriptor.LongitudeToTileX(calcZoom, center.getLongitude());
		double l2 = Descriptor.LongitudeToTileX(calcZoom, dummy.getLongitude());
		double diff = Math.abs(l2 - l1);
		pixelsPerMeter = (float) ((diff * 256) / 1000);

	}

	protected float getPixelsPerMeter(int ZoomLevel)
	{
		Coordinate dummy = Coordinate.Project(center.getLatitude(), center.getLongitude(), 90, 1000);
		double l1 = Descriptor.LongitudeToTileX(ZoomLevel, center.getLongitude());
		double l2 = Descriptor.LongitudeToTileX(ZoomLevel, dummy.getLongitude());
		double diff = Math.abs(l2 - l1);
		return (float) ((diff * 256) / 1000);
	}

	public abstract void requestLayout();

	@Override
	protected void Initial()
	{

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
		setBackground(SpriteCacheBase.ListBack);
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

	@Override
	public Priority getPriority()
	{
		return Priority.Normal;
	}

	public MapState getMapState()
	{
		return mapState;
	}

	public void setMapState(MapState state)
	{
		if (mapState == state) return;

		mapState = state;

		boolean wasCarMode = CarMode;

		if (mapState == MapState.CAR)
		{
			if (wasCarMode) return; // Brauchen wir nicht noch einmal machen!

			// Car mode
			CarMode = true;
			invalidateTexture();
		}
		else if (mapState == MapState.WP)
		{
			MapStateChangedToWP();
		}
		else if (mapState == MapState.LOCK || mapState == MapState.GPS)
		{
			setCenter(Locator.getCoordinate());
		}

		if (mapState != MapState.CAR)
		{
			if (!wasCarMode) return; // brauchen wir nicht noch einmal machen
			CarMode = false;
			invalidateTexture();
		}

	}

	public abstract void MapStateChangedToWP();

	public void SetZoom(int newZoom)
	{

		if (zoomBtn != null)
		{
			if (zoomBtn.getZoom() != newZoom)
			{
				zoomBtn.setZoom(newZoom);
			}
		}

		setZoomScale(newZoom);
		if (zoomScale != null) zoomScale.resetFadeOut();
		inputState = InputState.Idle;

		lastDynamicZoom = newZoom;

		kineticZoom = new KineticZoom(camera.zoom, mapTileLoader.getMapTilePosFactor(newZoom), System.currentTimeMillis(),
				System.currentTimeMillis() + ZoomTime);
		GL.that.addRenderView(MapViewBase.this, GL.FRAME_RATE_ACTION);
		GL.that.renderOnce(MapViewBase.this.getName() + " ZoomButtonClick");
		calcPixelsPerMeter();
	}

}