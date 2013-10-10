package CB_UI.GL_UI.Views;

import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.MapScale;
import CB_Locator.Map.MapTileLoader;
import CB_Locator.Map.MapViewBase;
import CB_Locator.Map.ZoomScale;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.Events.WaypointListChangedEventList;
import CB_UI.GL_UI.Activitys.EditWaypoint;
import CB_UI.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_UI.GL_UI.Controls.InfoBubble;
import CB_UI.GL_UI.Controls.MapInfoPanel;
import CB_UI.GL_UI.Controls.MapInfoPanel.CoordType;
import CB_UI.GL_UI.Views.MapViewCacheList.MapViewCacheListUpdateData;
import CB_UI.GL_UI.Views.MapViewCacheList.WaypointRenderInfo;
import CB_UI.Map.RouteOverlay;
import CB_UI_Base.GL_UI.DrawUtils;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.Controls.ZoomButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class MapView extends MapViewBase implements SelectedCacheEvent, PositionChangedEvent
{
	boolean CompassMode = false;

	// ####### Enthaltene Controls ##########
	MultiToggleButton togBtn;
	MapInfoPanel info;
	InfoBubble infoBubble;
	protected SortedMap<Integer, Integer> DistanceZoomLevel;

	MapViewCacheList mapCacheList;
	int zoomCross = 16;

	// private GL_ZoomScale zoomScale;

	// Settings values
	boolean showRating;
	boolean showDT;
	boolean showTitles;
	boolean hideMyFinds;
	boolean showCompass;
	boolean showDirektLine;
	boolean showAllWaypoints;
	private int lastCompassMapZoom = -1;

	public static MapView that = null;

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

		Config.MapsforgeDayTheme.addChangedEventListner(themeChangedEventHandler);
		Config.MapsforgeNightTheme.addChangedEventListner(themeChangedEventHandler);
		registerSkinChangedEvent();
		setBackground(SpriteCacheBase.ListBack);
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
				"mapScale", this, Config.ImperialUnits.getValue());

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

		String currentLayerName = Config.CurrentMapLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			if (mapTileLoader.CurrentLayer == null)
			{
				mapTileLoader.CurrentLayer = ManagerBase.Manager.GetLayerByName(currentLayerName, currentLayerName, "");
			}
		}

		String currentOverlayLayerName = Config.CurrentMapOverlayLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			if (mapTileLoader.CurrentOverlayLayer == null && currentOverlayLayerName.length() > 0) mapTileLoader.CurrentOverlayLayer = ManagerBase.Manager
					.GetLayerByName(currentOverlayLayerName, currentOverlayLayerName, "");
		}

		mapIntWidth = (int) rec.getWidth();
		mapIntHeight = (int) rec.getHeight();
		drawingWidth = mapIntWidth;
		drawingHeight = mapIntHeight;

		iconFactor = Config.MapViewDPIFaktor.getValue();

		togBtn = new MultiToggleButton(GL_UISizes.Toggle, "toggle");

		togBtn.addState("Free", Color.GRAY);
		togBtn.addState("GPS", Color.GREEN);
		togBtn.addState("WP", Color.MAGENTA);
		togBtn.addState("Lock", Color.RED);
		togBtn.addState("Car", Color.YELLOW);
		togBtn.setLastStateWithLongClick(true);

		MapState last = MapState.values()[Config.LastMapToggleBtnState.getValue()];
		togBtn.setState(last.ordinal());
		setMapState(last);

		togBtn.setOnStateChangedListner(new OnStateChangeListener()
		{

			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				setMapState(MapState.values()[State]);
			}
		});
		togBtn.registerSkinChangedEvent();

		setMapState(CompassMode ? MapState.GPS : last);
		switch (Config.LastMapToggleBtnState.getValue())
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

		center.setLatitude(Config.MapInitLatitude.getValue());
		center.setLongitude(Config.MapInitLongitude.getValue());
		// Info aktualisieren
		info.setCoord(center);
		aktZoom = Config.lastZoomLevel.getValue();
		zoomBtn.setZoom(aktZoom);
		calcPixelsPerMeter();
		mapScale.zoomChanged();

		if ((center.getLatitude() == -1000) && (center.getLongitude() == -1000))
		{
			// not initialized
			center.setLatitude(48);
			center.setLongitude(12);
		}

		// Initial SettingsChanged Events
		MapView.that.SetNightMode(Config.nightMode.getValue());
		Config.nightMode.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				MapView.this.SetNightMode(Config.nightMode.getValue());
			}
		});

		MapView.that.SetNorthOriented(Config.MapNorthOriented.getValue());
		Config.MapNorthOriented.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				MapView.this.SetNorthOriented(Config.MapNorthOriented.getValue());
				MapView.this.PositionChanged();
			}
		});

	}

	protected void renderOverlay(SpriteBatch batch)
	{
		batch.setProjectionMatrix(myParentInfo.Matrix());

		// calculate icon size
		int iconSize = 0; // 8x8
		if ((aktZoom >= 13) && (aktZoom <= 14)) iconSize = 1; // 13x13
		else if (aktZoom > 14) iconSize = 2; // default Images

		if (!CompassMode) CB_UI.Map.RouteOverlay.RenderRoute(batch, aktZoom, ySpeedVersatz);
		renderWPs(GL_UISizes.WPSizes[iconSize], GL_UISizes.UnderlaySizes[iconSize], batch);
		renderPositionMarker(batch);
		RenderTargetArrow(batch);

		renderUI(batch);
	}

	void renderUI(SpriteBatch batch)
	{
		batch.setProjectionMatrix(myParentInfo.Matrix());

		renderDebugInfo(batch);

	}

	CB_RectF TargetArrowScreenRec;

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
				Sprite arrow = SpriteCacheBase.Arrows.get(4);
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
	CB_RectF TargetArrow = null;

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

	private void renderWPI(SpriteBatch batch, SizeF WpUnderlay, SizeF WpSize, WaypointRenderInfo wpi)
	{
		Vector2 screen = worldToScreen(new Vector2(wpi.MapX, wpi.MapY));

		screen.y -= ySpeedVersatz;

		if (myPointOnScreen != null && showDirektLine && (wpi.Selected) && (wpi.Waypoint == GlobalCore.getSelectedWaypoint()))
		{
			if (LineSprite == null || PointSprite == null)
			{
				LineSprite = SpriteCacheBase.Arrows.get(13);
				PointSprite = SpriteCacheBase.Arrows.get(14);
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
			Sprite cross = SpriteCacheBase.MapOverlay.get(3);
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
			Sprite rating = SpriteCacheBase.MapStars.get((int) Math.min(wpi.Cache.Rating * 2, 5 * 2));
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
			Sprite difficulty = SpriteCacheBase.MapStars.get((int) Math.min(wpi.Cache.Difficulty * 2, 5 * 2));
			difficulty.setBounds(screen.x - WpUnderlay.width - GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2),
					WpUnderlay.width, WpUnderlay.Height4_8);
			difficulty.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			difficulty.setRotation(90);
			difficulty.draw(batch);

			Sprite terrain = SpriteCacheBase.MapStars.get((int) Math.min(wpi.Cache.Terrain * 2, 5 * 2));
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

	public static int INITIAL_WP_LIST = 4;

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

		if (getCenterGps())
		{
			PositionChanged();
			return;
		}

		positionInitialized = true;

		if (getMapState() != MapState.WP) setMapState(MapState.FREE);

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

	@Override
	public void SpeedChanged()
	{
		if (info != null)
		{
			info.setSpeed(Locator.SpeedString());

			if (getMapState() == MapState.CAR && Config.dynamicZoom.getValue())
			{
				// calculate dynamic Zoom

				double maxSpeed = Config.MoveMapCenterMaxSpeed.getValue();
				int maxZoom = Config.dynamicZoomLevelMax.getValue();
				int minZoom = Config.dynamicZoomLevelMin.getValue();

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
		}, true, false);
		EdWp.show();

	}

	@Override
	public void dispose()
	{
		// remove eventHandler
		SelectedCacheEventList.Remove(this);
		super.dispose();
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		TargetArrowScreenRec = null;
	}

	@Override
	public void onHide()
	{
		CB_UI.Events.SelectedCacheEventList.Remove(this);
		super.onHide();
	}

	protected void loadTiles()
	{
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(
				mapIntWidth, mapIntHeight)), aktZoom, false);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);

		super.loadTiles();
	}

	public void setCenter(Coordinate value)
	{
		super.setCenter(value);
		info.setCoord(center);
	}

	@Override
	public void OrientationChanged()
	{
		super.OrientationChanged();
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
	}

	public void InitializeMap()
	{
		zoomCross = Config.ZoomCross.getValue();
		super.InitializeMap();
	}

	protected void setZoomScale(int zoom)
	{
		// Logger.LogCat("set zoom");
		if (!CarMode && !CompassMode) zoomScale.setZoom(zoom);
		if (!CompassMode) mapScale.zoomChanged();
	}

	protected void calcCenter()
	{
		super.calcCenter();
		info.setCoord(center);
	}

	public void requestLayout()
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

	protected OnClickListener onClickListner = new OnClickListener()
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

						// switch map state to WP
						togBtn.setState(2);
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

	@Override
	protected void SetMapStateFree()
	{
		// setMapState(MapState.FREE);
		// Go over ToggelButton
		togBtn.setState(0);
	}

	@Override
	public void setMapState(MapState state)
	{
		if (super.getMapState() == state) return;

		info.setCoordType(CoordType.Map);
		Config.LastMapToggleBtnState.setValue(state.ordinal());
		Config.AcceptChanges();

		boolean wasCarMode = CarMode;

		if (state == MapState.CAR)
		{
			if (!wasCarMode)
			{
				info.setCoordType(CoordType.GPS);
			}
		}
		else if (state == MapState.WP)
		{
			info.setCoordType(CoordType.Cache);
		}
		else if (state == MapState.LOCK)
		{
			info.setCoordType(CoordType.GPS);
		}
		else if (state == MapState.GPS)
		{
			info.setCoordType(CoordType.GPS);
		}

		super.setMapState(state);
	}

	@Override
	public void PositionChanged()
	{

		if (CarMode)
		{
			// im CarMode keine Netzwerk Koordinaten zulassen
			if (!Locator.isGPSprovided()) return;
		}

		super.PositionChanged();

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
	}

	@Override
	public boolean doubleClick(int x, int y, int pointer, int button)
	{
		if (CompassMode)
		{
			// Center map on CompassMode
			setMapState(MapState.GPS);
			return true;
		}
		else
		{
			return super.doubleClick(x, y, pointer, button);
		}
	}

	@Override
	protected void SkinIsChanged()
	{
		super.SkinIsChanged();
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(
				mapIntWidth, mapIntHeight)), aktZoom, true);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);
		if (infoBubble.isVisible())
		{
			infoBubble.setCache(infoBubble.getCache(), infoBubble.getWaypoint(), true);
		}
	}

	@Override
	public void onStop()
	{
		if (!CompassMode) // save last zoom and position only from Map, not from CompassMap
		{
			super.onStop();
		}
	}

	@Override
	public void onShow()
	{
		super.onShow();
		CB_UI.Events.SelectedCacheEventList.Add(this);
		this.NorthOriented = CompassMode ? false : Config.MapNorthOriented.getValue();
		SelectedCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	}

	@Override
	public void setNewSettings(int InitialFlags)
	{
		if ((InitialFlags & INITIAL_SETTINGS) != 0)
		{
			showRating = CompassMode ? false : Config.MapShowRating.getValue();
			showDT = CompassMode ? false : Config.MapShowDT.getValue();
			showTitles = CompassMode ? false : Config.MapShowTitles.getValue();
			hideMyFinds = Config.MapHideMyFinds.getValue();
			showCompass = CompassMode ? false : Config.MapShowCompass.getValue();
			showDirektLine = CompassMode ? false : Config.ShowDirektLine.getValue();
			showAllWaypoints = CompassMode ? false : Config.ShowAllWaypoints.getValue();
			showAccuracyCircle = CompassMode ? false : Config.ShowAccuracyCircle.getValue();
			showMapCenterCross = CompassMode ? false : Config.ShowMapCenterCross.getValue();

			if (info != null) info.setVisible(showCompass);

			if (InitialFlags == INITIAL_ALL)
			{
				iconFactor = (float) Config.MapViewDPIFaktor.getValue();

				int setAktZoom = CompassMode ? Config.lastZoomLevel.getValue() : Config.lastZoomLevel.getValue();
				int setMaxZoom = CompassMode ? Config.CompassMapMaxZommLevel.getValue() : Config.OsmMaxLevel.getValue();
				int setMinZoom = CompassMode ? Config.CompassMapMinZoomLevel.getValue() : Config.OsmMinLevel.getValue();

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
				if (Config.nightMode.getValue())
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

				// Entweder wir sind nicht im CarMode oder es wurde kein Passender Theme für den CarMode gefunden!
				if (Config.nightMode.getValue())
				{
					themePath = ifThemeExist(Config.MapsforgeNightTheme.getValue());
				}

				if (themePath == null)
				{
					if (Config.nightMode.getValue()) useInvertNightTheme = true;
					themePath = ifThemeExist(Config.MapsforgeDayTheme.getValue());

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
				ManagerBase.Manager.clearRenderTheme();
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

	@Override
	protected void setInitialLocation()
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

	@Override
	public void MapStateChangedToWP()
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
	}

	@Override
	public void SetAlignToCompass(boolean value)
	{
		super.SetAlignToCompass(value);
		Config.MapNorthOriented.setValue(!value);
	}

}
