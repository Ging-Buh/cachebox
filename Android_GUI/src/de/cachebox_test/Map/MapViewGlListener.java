package de.cachebox_test.Map;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.DestroyFailedException;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import de.cachebox_test.Global;
import de.cachebox_test.UnitFormatter;
import de.cachebox_test.main;
import de.cachebox_test.Components.CacheDraw;
import de.cachebox_test.Custom_Controls.MultiToggleButton;
import de.cachebox_test.Events.PositionEvent;
import de.cachebox_test.Events.PositionEventList;
import de.cachebox_test.Map.MapCacheList.WaypointRenderInfo;
import de.cachebox_test.Views.MapView;
import de.cachebox_test.Views.Forms.ScreenLock;

public class MapViewGlListener implements ApplicationListener, PositionEvent
{
	protected SortedMap<Long, TileGL> loadedTiles = new TreeMap<Long, TileGL>();
	final Lock loadedTilesLock = new ReentrantLock();
	protected SortedMap<Long, Descriptor> queuedTiles = new TreeMap<Long, Descriptor>();
	private Lock queuedTilesLock = new ReentrantLock();
	private queueProcessor queueProcessor = null;
	private AtomicBoolean started = new AtomicBoolean(false);
	public boolean alignToCompass = false;
	// private boolean centerGps = false;
	private float mapHeading = 0;
	private float arrowHeading = 0;
	private MapCacheList mapCacheList;
	private Point lastMovement = new Point(0, 0);
	private int zoomCross = 16;
	private MultiToggleButton btnTrackPos;

	// Settings values
	public boolean showRating;
	public boolean showDT;
	public boolean showTitles;
	public boolean hideMyFinds;
	private boolean showCompass;
	public boolean showDirektLine;
	private boolean nightMode;

	// maxzoom wird 1:1 dargestellt
	int minzoom = 6;
	int maxzoom = 20;
	int zoom = 13;
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
	String CurrentLayer = "germany-0.2.4.map";

	long startTime;

	public MapViewGlListener()
	{
		super();
		if (queueProcessor == null)
		{
			try
			{
				queueProcessor = new queueProcessor();
			}
			catch (Exception ex)
			{
				String s = ex.getMessage();
			}
			queueProcessor.execute(0);
		}

		mapCacheList = new MapCacheList(maxzoom);

	}

	@Override
	public void create()
	{
		PositionEventList.Add(this);

		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		drawingWidth = width;
		drawingHeight = height;
		batch = new SpriteBatch();
		camera = new OrthographicCamera(width, height);

		controller = new CameraController();
		gestureDetector = new GestureDetector(20, 0.5f, 2, 0.15f, controller);
		Gdx.input.setInputProcessor(gestureDetector);

		iconFactor = (float) Config.settings.MapViewDPIFaktor.getValue();

		// setScreenCenter(new Descriptor((int) posx, (int) posy, 14));
		// setCenter(new Coordinate(48.0, 12.0));
		textMatrix = new Matrix4().setToOrtho2D(0, 0, width, height);

		font = new BitmapFont(Gdx.files.internal("data/ArialBold18.fnt"), Gdx.files.internal("data/ArialBold18.png"), false);
		font.setColor(0.0f, 0.2f, 0.0f, 1.0f);
		font.setScale(1.0f);

		fontName = new BitmapFont(Gdx.files.internal("data/ArialBold16outline.fnt"), Gdx.files.internal("data/ArialBold16outline.png"),
				false);
		fontName.setColor(1.0f, 0.2f, 0.0f, 1.0f);
		fontName.setScale(1.0f);

		circle = new Gdx2DPixmap(16, 16, Gdx2DPixmap.GDX2D_FORMAT_RGB565);
		circle.clear(Color.TRANSPARENT);
		// circle.fillRect(0, 0, 16, 16, Color.YELLOW);
		circle.drawCircle(8, 8, 8, Color.BLACK);

		tcircle = new Texture(new Pixmap(circle));
		camera.zoom = getMapTilePosFactor(zoom);
		camera.position.set((float) screenCenterW.x, (float) screenCenterW.y, 0);
		startTime = System.currentTimeMillis();

		// initial Toggle Button
		btnTrackPos = new MultiToggleButton();
		btnTrackPos.clearStates();
		btnTrackPos.addState("Free", Color.GRAY);
		btnTrackPos.addState("GPS", Color.GREEN);
		btnTrackPos.addState("Lock", Color.RED);
		btnTrackPos.addState("Car", Color.YELLOW);
		btnTrackPos.setState(0);

	}

	@Override
	public void resize(int width, int height)
	{
		// Log.d("MAPVIEW", "resize" + width + "/" + height);
		textMatrix.setToOrtho2D(0, 0, width, height);
		togglePos = new Vector2(380, height - 97);
		infoPos = new Vector2(10, height - 100);
	}

	protected SortedMap<Long, TileGL> tilesToDraw = new TreeMap<Long, TileGL>();

	@Override
	public void render()
	{

		long endTime = System.currentTimeMillis();
		long dt = endTime - startTime;
		if (dt < 33)
		{
			try
			{
				if (20 - dt > 0) Thread.sleep(20 - dt);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		startTime = System.currentTimeMillis();

		if (SpriteCache.MapIcons == null)
		{
			SpriteCache.LoadSprites();
		}

		if (!started.get()) return;

		loadTiles();

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		controller.update();

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
			int tmpzoom = zoom;
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
						if (tmpzoom == zoom) tile.Age = 0;

						if (!tilesToDraw.containsKey(tile.Descriptor.GetHashCode())) tilesToDraw.put(tile.Descriptor.GetHashCode(), tile);
					}
					else if (tmpzoom == zoom)
					{
						// für den aktuellen Zoom ist kein Tile vorhanden ->
						// kleinere Zoomfaktoren durchsuchen
						if (!renderBiggerTiles(batch, i, j, zoom))
						// größere Zoomfaktoren noch durchsuchen, ob davon Tiles
						// vorhanden sind...
						// dafür müssen aber pro fehlendem Tile mehrere kleine
						// Tiles gezeichnet werden (4 oder 16 oder 64...)
						// dieser Aufruf kann auch rekursiv sein...
						renderSmallerTiles(batch, i, j, zoom);
					}
				}
			}
		}
		for (TileGL tile : tilesToDraw.values())
		{
			tile.createTexture();
			// Faktor, mit der dieses MapTile vergrößert gezeichnet
			// werden muß
			long posFactor = getMapTilePosFactor(tile.Descriptor.Zoom);

			float xPos = tile.Descriptor.X * posFactor * 256;
			float yPos = -(tile.Descriptor.Y + 1) * posFactor * 256;
			float xSize = tile.texture.getWidth() * posFactor;
			float ySize = tile.texture.getHeight() * posFactor;
			batch.draw(tile.texture, xPos, yPos, xSize, ySize);
		}
		tilesToDraw.clear();

		batch.end();

		batch.setProjectionMatrix(textMatrix);
		batch.begin();

		// calculate icon size

		int iconSize = 0; // 8x8
		if ((zoom >= 13) && (zoom <= 14)) iconSize = 1; // 13x13
		else if (zoom > 14) iconSize = 2; // default Images

		float xPos = (float) screenCenterW.x;
		float yPos = (float) screenCenterW.y;
		float xSizeUnder = 32 * iconFactor;
		float ySizeUnder = 32 * iconFactor;
		float xSize = 21 * iconFactor;
		float ySize = 21 * iconFactor;
		switch (iconSize)
		{
		case 0:
			xSizeUnder = 13 * iconFactor;
			ySizeUnder = 13 * iconFactor;
			xSize = 13 * iconFactor;
			ySize = 13 * iconFactor;
			break;
		case 1:
			xSizeUnder = 20 * iconFactor;
			ySizeUnder = 20 * iconFactor;
			xSize = 14 * iconFactor;
			ySize = 14 * iconFactor;
			break;
		}

		renderWPs(xSizeUnder, ySizeUnder, xSize, ySize);
		renderPositionMarker();
		Bubble.render(xSizeUnder, ySizeUnder);

		renderInfoPanel();

		btnTrackPos.Render(batch, togglePos, toggleWidth, toggleHeight);

		renderDebugInfo();

		batch.end();

	}

	private void renderDebugInfo()
	{
		str = GlobalCore.FormatLatitudeDM(center.Latitude) + " - " + GlobalCore.FormatLongitudeDM(center.Longitude);
		font.draw(batch, str, 20, 120);

		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		font.draw(batch, str, 20, 100);
		str = String.valueOf(zoom) + " - camera.zoom: " + Math.round(camera.zoom * 100) / 100;
		font.draw(batch, str, 20, 80);
		str = "loaded Tiles: " + loadedTiles.size() + " - queuedTiles: " + queuedTiles.size();
		font.draw(batch, str, 20, 60);
		if (mapCacheList != null)
		{
			str = "AnzCachelistCalc: " + mapCacheList.anz + " - Caches: " + mapCacheList.list.size();
			font.draw(batch, str, 20, 40);
		}
		str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
		font.draw(batch, str, 20, 20);

		str = "W/H: " + width + "/" + height;
		font.draw(batch, str, 200, 100);

		str = "dW/dH: " + drawingWidth + "/" + drawingHeight;
		font.draw(batch, str, 200, 80);

	}

	public static Vector2 togglePos = new Vector2(380, 438);
	public static final float toggleWidth = 87;
	public static final float toggleHeight = 87;

	public static Vector2 infoPos = new Vector2(10, 435);
	public static final float infoWidth = 366;
	public static final float infoHeight = 87;

	public static final float compassWidth = infoHeight - 20;
	public static final float compassHeight = infoHeight - 20;

	private void renderInfoPanel()
	{
		// draw background
		Sprite sprite = SpriteCache.InfoBack;
		sprite.setPosition(infoPos.x, infoPos.y);
		sprite.setSize(infoWidth, infoHeight);
		sprite.draw(MapViewGlListener.batch);

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
			font.draw(batch, text, infoPos.x + 100, infoPos.y + 40);
			// canvas.drawText(text, leftString, bottom - 10, paint);

			// Kompassnadel zeichnen
			if (Global.Locator != null)
			{
				Coordinate cache = (GlobalCore.SelectedWaypoint() != null) ? GlobalCore.SelectedWaypoint().Pos
						: GlobalCore.SelectedCache().Pos;
				double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude, cache.Longitude);
				double relativeBearing = bearing - Global.Locator.getHeading();
				// double relativeBearingRad = relativeBearing * Math.PI / 180.0;

				// draw compass
				Sprite compass = SpriteCache.MapArrows.get(0);
				compass.setRotation((float) -relativeBearing);
				compass.setSize(compassWidth, compassHeight);
				compass.setPosition(infoPos.x + 15, infoPos.y + 16);

				compass.draw(MapViewGlListener.batch);

			}

			// Koordinaten
			if (position.Valid)
			{
				String textLatitude = GlobalCore.FormatLatitudeDM(position.Latitude);
				String textLongitude = GlobalCore.FormatLongitudeDM(position.Longitude);

				font.draw(batch, textLatitude, infoPos.x + 220, infoPos.y + 70);
				font.draw(batch, textLongitude, infoPos.x + 220, infoPos.y + 40);

				if (Global.Locator != null)
				{
					font.draw(batch, Global.Locator.SpeedString(), infoPos.x + 100, infoPos.y + 70);
				}

			}
		}

	}

	private void renderPositionMarker()
	{
		if (Global.Locator != null)
		{
			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(maxzoom, GlobalCore.LastValidPosition.Longitude),
					Descriptor.LatitudeToTileY(maxzoom, GlobalCore.LastValidPosition.Latitude), maxzoom, maxzoom);

			Vector2 vPoint = new Vector2((float) point.X, -(float) point.Y);

			Vector2 screen = worldToScreen(vPoint);

			boolean lastUsedCompass = Global.Locator.LastUsedCompass;
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

			arrow.setPosition(screen.x - 24, screen.y - 24);
			arrow.setRotation(-arrowHeading);
			arrow.draw(batch);
		}
	}

	private void renderWPs(float xSizeUnder, float ySizeUnder, float xSize, float ySize)
	{
		WaypointRenderInfo newSelectedCache = null;
		if (mapCacheList.list != null)
		{
			synchronized (mapCacheList.list)
			{
				for (WaypointRenderInfo wpi : mapCacheList.list)
				{
					Vector2 screen = worldToScreen(new Vector2(Math.round(wpi.MapX), Math.round(wpi.MapY)));

					float NameYMovement = 0;

					if ((zoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == GlobalCore.SelectedWaypoint()))
					{
						// Draw Cross and move screen vector
						Sprite cross = SpriteCache.MapOverlay.get(3);
						cross.setPosition((float) screen.x - xSizeUnder / 2, (float) screen.y - ySizeUnder / 2);
						cross.setSize(xSizeUnder, ySizeUnder);
						cross.draw(batch);

						screen.add(-xSizeUnder, ySizeUnder);
						NameYMovement = ySizeUnder;
					}

					if (wpi.UnderlayIcon != null)
					{
						wpi.UnderlayIcon.setPosition((float) screen.x - xSizeUnder / 2, (float) screen.y - ySizeUnder / 2);
						wpi.UnderlayIcon.setSize(xSizeUnder, ySizeUnder);
						wpi.UnderlayIcon.draw(batch);
					}
					if (wpi.Icon != null)
					{
						wpi.Icon.setPosition((float) screen.x - xSize / 2, (float) screen.y - ySize / 2);
						wpi.Icon.setSize(xSize, ySize);
						wpi.Icon.draw(batch);
					}

					if (wpi.OverlayIcon != null)
					{
						wpi.OverlayIcon.setPosition((float) screen.x - xSizeUnder / 2, (float) screen.y - ySizeUnder / 2);

						wpi.OverlayIcon.setSize(xSizeUnder, ySizeUnder);
						wpi.OverlayIcon.draw(batch);
					}

					boolean drawAsWaypoint = wpi.Waypoint != null;

					// Rating des Caches darstellen
					if (showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (zoom >= 15))
					{
						Sprite rating = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Rating * 2, 5 * 2));
						rating.setPosition((float) screen.x - xSizeUnder / 2, (float) screen.y - ySizeUnder / 2 - 8);
						rating.setSize(xSizeUnder, (float) (ySizeUnder / 4.8));
						rating.setRotation(0);
						rating.draw(batch);
						NameYMovement += 10;
					}

					// Beschriftung
					if (showTitles && (zoom >= 15) && (!drawAsWaypoint))
					{
						float halfWidth = fontName.getBounds(wpi.Cache.Name).width / 2;
						fontName.draw(batch, wpi.Cache.Name, screen.x - halfWidth, screen.y - ySizeUnder / 2 - NameYMovement);
					}

					// Show D/T-Rating
					if (showDT && (!drawAsWaypoint) && (zoom >= 15))
					{
						Sprite difficulty = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Difficulty * 2, 5 * 2));
						difficulty.setPosition((float) screen.x - xSizeUnder - 5, (float) screen.y);
						difficulty.setSize(xSizeUnder, (float) (ySizeUnder / 4.8));
						difficulty.setRotation(90);
						difficulty.draw(batch);

						Sprite terrain = SpriteCache.MapStars.get((int) Math.min(wpi.Cache.Terrain * 2, 5 * 2));
						terrain.setPosition((float) screen.x + 5, (float) screen.y);
						terrain.setSize(xSizeUnder, (float) (ySizeUnder / 4.8));
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
		else if ((zoomzoom >= zoom - 3) && (zoomzoom >= minzoom))
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
				else if ((zoomzoom <= zoom + 0) && (zoomzoom <= maxzoom))
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
				- extensionTop), zoom);
		Descriptor ru = screenToDescriptor(new Vector2(width / 2 + drawingWidth / 2 + extensionRight, height / 2 + drawingHeight / 2
				+ extensionBottom), zoom);

		mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), zoom, false);

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
					Descriptor desc = new Descriptor(i, j, zoom);

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
		// int cnt = 0;
		// loadedTilesLock.lock();
		// try
		// {
		// for (TileGL tile : loadedTiles.values())
		// if (tile.texture != null) cnt++;
		// }
		// finally
		// {
		// loadedTilesLock.unlock();
		// }
		//
		// return cnt;
	}

	@Override
	public void pause()
	{
		onStop();
	}

	@Override
	public void resume()
	{
		onStart();
	}

	@Override
	public void dispose()
	{
		SpriteCache.destroyCache();

	}

	public void onStart()
	{
		started.set(true);
	}

	public void onStop()
	{

		// TODO wenn der ScreenLock angezeigt wird, kommt es auch zu einem
		// onStop.
		// Es darf dann aber nicht gestoppt werden.
		// Die main als AndroidAplication stoppt hier in onPause() das rendern.
		// Abhilfe schafft hier nur das Ändern des gdx Codes!
		if (ScreenLock.isShown) return;

		started.set(false);
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

	public static SpriteBatch batch;
	Matrix4 textMatrix;
	BitmapFont font;
	BitmapFont fontName;
	CharSequence str = "Hello World!";
	OrthographicCamera camera;
	CameraController controller;
	GestureDetector gestureDetector;
	Gdx2DPixmap circle;
	Texture tcircle;

	private void stateChanged()
	{
		if (btnTrackPos.getState() > 0)
		{
			setCenter(new Coordinate(GlobalCore.LastValidPosition.Latitude, GlobalCore.LastValidPosition.Longitude));
		}
	}

	class CameraController implements GestureListener
	{
		float velX, velY;
		boolean flinging = false;
		float initialScale = 1;

		public boolean touchDown(int x, int y, int pointer)
		{
			flinging = false;
			initialScale = camera.zoom;
			return false;
		}

		@Override
		public boolean tap(int x, int y, int count)
		{
			int xSizeUnder = 30;
			int ySizeUnder = 30;
			double minDist = Double.MAX_VALUE;
			WaypointRenderInfo minWpi = null;
			Vector2 clickedAt = new Vector2(Gdx.input.getX(), height - Gdx.input.getY());

			// check ToggleBtn clicked
			if (btnTrackPos.hitTest(clickedAt))
			{
				main.vibrator.vibrate(50);
				stateChanged();
				return true;
			}

			synchronized (mapCacheList.list)
			{
				// Bubble gedrückt?
				if ((Bubble.cache != null) && Bubble.isShow && Bubble.DrawRec != null && Bubble.DrawRec.contains(clickedAt.x, clickedAt.y))
				{
					// Click inside Bubble -> hide Bubble and select Cache
					// GlobalCore.SelectedWaypoint(Bubble.cache,
					// Bubble.waypoint);
					ThreadSaveSetSelectedWP(Bubble.cache, Bubble.waypoint);
					CacheDraw.ReleaseCacheBMP();
					Bubble.isShow = false;
					Bubble.CacheId = -1;
					Bubble.cache = null;
					Bubble.waypoint = null;
					mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), zoom, true);

					// Shutdown Autoresort
					Global.autoResort = false;
					// Render(true);
					// do nothing else with this click
					return false;
				}
				else if (Bubble.isShow)
				{
					// Click outside Bubble -> hide Bubble
					Bubble.isShow = false;
					// Render(true);
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
				// if (minDist < 40)
				// {
				//
				// final Cache updateCache = minWpi.Cache;
				// final Waypoint updateWaypoint = minWpi.Waypoint;
				//
				// ThreadSaveSetSelectedWP(updateCache, updateWaypoint);
				//
				// // CacheListe auf jeden Fall neu berechnen
				// // könnte aber noch verbessert werden, indem nur der letzte
				// // selected und der neue selected geändert werden!
				// mapCacheList.update(screenToWorld(new Vector2(0, 0)),
				// screenToWorld(new Vector2(width, height)), zoom, true);
				//
				// }

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
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), zoom, true);

						}
						else
						{
							// do not show Bubble because there will not be
							// selected
							// a
							// different cache but only a different waypoint
							// Wegpunktliste ausrichten
							ThreadSaveSetSelectedWP(minWpi.Cache, minWpi.Waypoint);
							// FormMain.WaypointListPanel.AlignSelected();
							// updateCacheList();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), zoom, true);
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
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), zoom, true);
						}
						else
						{
							Bubble.isShow = true;
							Bubble.CacheId = minWpi.Cache.Id;
							Bubble.cache = minWpi.Cache;
							Bubble.disposeSprite();
							// Cacheliste ausrichten
							ThreadSaveSetSelectedWP(minWpi.Cache);
							// updateCacheList();
							mapCacheList.update(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(width, height)), zoom, true);
						}
					}

				}
			}
			return false;
		}

		/**
		 * Wählt Cache Thread sicher an.
		 * 
		 * @param cache
		 */
		private void ThreadSaveSetSelectedWP(final Cache cache)
		{
			ThreadSaveSetSelectedWP(cache, null);
		}

		/**
		 * Wählt Cache und Waypoint Thread sicher an.
		 * 
		 * @param cache
		 * @param waypoint
		 */
		private void ThreadSaveSetSelectedWP(final Cache cache, final Waypoint waypoint)
		{
			Thread t = new Thread()
			{
				public void run()
				{
					main.mainActivity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (waypoint == null)
							{
								GlobalCore.SelectedCache(cache);
							}
							else
							{
								GlobalCore.SelectedWaypoint(cache, waypoint);
							}
						}
					});
				}
			};

			t.start();
		}

		@Override
		public boolean longPress(int x, int y)
		{
			Gdx.app.log("GestureDetectorTest", "long press at " + x + ", " + y);
			return false;
		}

		@Override
		public boolean fling(float velocityX, float velocityY)
		{
			if (btnTrackPos.getState() > 1) return false;

			flinging = true;

			Vector2 richtung = new Vector2(velocityX, velocityY);
			richtung.rotate(mapHeading);

			velX = camera.zoom * richtung.x * 0.5f;
			velY = camera.zoom * richtung.y * 0.5f;
			return false;
		}

		@Override
		public boolean pan(int x, int y, int deltaX, int deltaY)
		{
			if (btnTrackPos.getState() > 1) return false;

			// Drehung der Karte berücksichtigen
			Vector2 richtung = new Vector2(deltaX, deltaY);
			richtung.rotate(mapHeading);
			camera.position.add(-richtung.x * camera.zoom, richtung.y * camera.zoom, 0);
			screenCenterW.x = camera.position.x;
			screenCenterW.y = camera.position.y;
			calcCenter();
			btnTrackPos.setState(0);
			return false;
		}

		private void calcCenter()
		{
			// berechnet anhand des ScreenCenterW die Center-Coordinaten
			PointD point = Descriptor.FromWorld(screenCenterW.x, screenCenterW.y, maxzoom, maxzoom);

			center = new Coordinate(Descriptor.TileYToLatitude(maxzoom, -point.Y), Descriptor.TileXToLongitude(maxzoom, point.X));
		}

		@Override
		public boolean zoom(float originalDistance, float currentDistance)
		{
			float ratio = originalDistance / currentDistance;
			camera.zoom = initialScale * ratio;
			System.out.println(camera.zoom);
			zoom = maxzoom;
			float tmpZoom = camera.zoom;
			float faktor = 1.5f;
			faktor = faktor - iconFactor + 1;
			while (tmpZoom > faktor)
			{
				tmpZoom /= 2;
				zoom--;
			}

			return false;
		}

		public void update()
		{
			if (flinging)
			{
				velX *= 0.98f;
				velY *= 0.98f;
				camera.position.add(-velX * Gdx.graphics.getDeltaTime(), velY * Gdx.graphics.getDeltaTime(), 0);
				if (Math.abs(velX) < 0.01f) velX = 0;
				if (Math.abs(velY) < 0.01f) velY = 0;
				screenCenterW.x = camera.position.x;
				screenCenterW.y = camera.position.y;
				calcCenter();
			}
		}
	}

	public void Initialize()
	{
		// minzoom = Config.settings.OsmMinLevel.getValue();
		// maxzoom = Config.settings.OsmMaxLevel.getValue();

	}

	public void InitializeMap()
	{
		zoomCross = Config.settings.ZoomCross.getValue();
		zoom = Config.settings.lastZoomLevel.getValue();
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

			PointD point = Descriptor.ToWorld(Descriptor.LongitudeToTileX(maxzoom, center.Longitude),
					Descriptor.LatitudeToTileY(maxzoom, center.Latitude), maxzoom, maxzoom);

			setScreenCenter(new Vector2((float) point.X, (float) point.Y));
		}
	}

	private long getMapTileSizeFactor(int zoom)
	{
		long result = 1;
		for (int z = minzoom; z < zoom; z++)
		{
			result *= 2;
		}
		return result;
	}

	private long getMapTilePosFactor(int zoom)
	{
		long result = 1;
		for (int z = zoom; z < maxzoom; z++)
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
		for (int i = maxzoom; i > zoom; i--)
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
	public void PositionChanged(Location location)
	{
		GlobalCore.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		GlobalCore.LastValidPosition.Elevation = location.getAltitude();

		if (btnTrackPos.getState() > 0) setCenter(new Coordinate(location.getLatitude(), location.getLongitude()));

	}

	@Override
	public void OrientationChanged(float heading)
	{
		// liefert die Richtung (abhängig von der Geschwindigkeit von
		// Kompass oder GPS
		if (!Global.Locator.UseCompass())
		{
			// GPS-Richtung soll verwendet werden!
			heading = Global.Locator.getHeading();
		}

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
		return (btnTrackPos.getState() > 0);
	}

	public boolean GetAlignToCompass()
	{
		return alignToCompass;
	}

	@SuppressWarnings("unchecked")
	private void queueTile(Descriptor desc)
	{
		// Alternative Implementierung mit Threadpools...
		// ThreadPool.QueueUserWorkItem(new WaitCallback(LoadTile), new
		// Descriptor(desc));

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

	private class queueProcessor extends AsyncTask<Integer, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(Integer... params)
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

									if (Math.abs(zoom - nearestZoom) > Math.abs(zoom - tmpDesc.Zoom))
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
										if (Math.abs(zoom - nearestZoom) < Math.abs(zoom - tmpDesc.Zoom))
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
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			queueProcessor = null;
		}
	}

	private void LoadTile(Descriptor desc)
	{
		TileGL.TileState tileState = TileGL.TileState.Disposed;

		byte[] bytes = MapView.Manager.LoadLocalPixmap(CurrentLayer, desc);
		// Texture texture = new Texture(new Pixmap(bytes, 0, bytes.length));
		tileState = TileGL.TileState.Present;
		addLoadedTile(desc, bytes, tileState);
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

	private class KachelOrder implements Comparable<KachelOrder>
	{
		Descriptor desc;
		double dist;

		private KachelOrder(Descriptor desc, double dist)
		{
			this.desc = desc;
			this.dist = dist;
		}

		@Override
		public int compareTo(KachelOrder arg0)
		{
			return (dist < arg0.dist ? -1 : (dist == arg0.dist ? 0 : 1));
		}
	}

}
