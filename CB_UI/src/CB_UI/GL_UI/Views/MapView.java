/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_UI.GL_UI.Views;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.LiveMapQue;
import CB_Core.Api.SearchGC;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.MapScale;
import CB_Locator.Map.MapTileLoader;
import CB_Locator.Map.MapViewBase;
import CB_Locator.Map.ZoomScale;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.CB_UI_Settings;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.RouteOverlay;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI.WaypointListChangedEventList;
import CB_UI.GL_UI.Activitys.EditWaypoint;
import CB_UI.GL_UI.Activitys.EditWaypoint.IReturnListener;
import CB_UI.GL_UI.Controls.InfoBubble;
import CB_UI.GL_UI.Controls.LiveButton;
import CB_UI.GL_UI.Controls.MapInfoPanel;
import CB_UI.GL_UI.Controls.MapInfoPanel.CoordType;
import CB_UI.GL_UI.Views.MapViewCacheList.MapViewCacheListUpdateData;
import CB_UI.GL_UI.Views.MapViewCacheList.WaypointRenderInfo;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.Controls.ZoomButtons;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.KineticZoom;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Geometry.GeometryList;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.HSV_Color;
import CB_Utils.Util.IChanged;

/**
 * @author ging-buh
 * @author Longri
 */
public class MapView extends MapViewBase implements SelectedCacheEvent, PositionChangedEvent {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(MapView.class);

	public enum MapMode {
		Normal, Compass, Track
	}

	MapMode Mode = MapMode.Normal;

	// ####### Enthaltene Controls ##########
	LiveButton liveButton;
	MultiToggleButton togBtn;
	MapInfoPanel info;
	InfoBubble infoBubble;
	protected SortedMap<Integer, Integer> DistanceZoomLevel;
	CancelWaitDialog wd = null;

	private MapViewCacheList mapCacheList;
	int zoomCross = 16;

	// private GL_ZoomScale zoomScale;

	// Settings values
	boolean showRating;
	boolean showDT;
	boolean showTitles;
	boolean hideMyFinds;
	boolean showCompass;
	boolean showDirectLine;
	boolean showAllWaypoints;
	private int lastCompassMapZoom = -1;
	GL_Paint paint;
	public static MapView that = null;

	Cache lastSelectedCache = null;
	Waypoint lastSelectedWaypoint = null;
	GlyphLayout layout = null;

	public MapView(CB_RectF rec, MapMode Mode, String Name) {
		super(rec, Name);
		// statischen that nur setzen wenn die HauptMapView initialisiert wird
		if (Mode == MapMode.Normal) {
			that = this;
		} else {
			this.setOnDoubleClickListener(new OnClickListener() {

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
					// Center own position!
					setCenter(Locator.getCoordinate());
					return true;
				}
			});
		}

		this.Mode = Mode;

		Config.MapsforgeDayTheme.addChangedEventListener(themeChangedEventHandler);
		Config.MapsforgeNightTheme.addChangedEventListener(themeChangedEventHandler);

		registerSkinChangedEvent();
		setBackground(Sprites.ListBack);
		int maxNumTiles = 0;
		// calculate max Map Tile cache
		try {
			int aTile = 256 * 256;
			maxTilesPerScreen = (int) ((rec.getWidth() * rec.getHeight()) / aTile + 0.5);

			maxNumTiles = (int) (maxTilesPerScreen * 6);// 6 times as much as necessary

		} catch (Exception e) {
			maxNumTiles = 60;
		}

		maxNumTiles = Math.min(maxNumTiles, 60);
		maxNumTiles = Math.max(maxNumTiles, 20);

		mapTileLoader.setMaxNumTiles(maxNumTiles);

		mapScale = new MapScale(new CB_RectF(GL_UISizes.margin, GL_UISizes.margin, this.getHalfWidth(), GL_UISizes.ZoomBtn.getHalfWidth() / 4), "mapScale", this, Config.ImperialUnits.getValue());

		if (Mode == MapMode.Normal) {
			this.addChild(mapScale);
		} else {
			mapScale.setInvisible();
		}

		// initial Zoom Buttons
		zoomBtn = new ZoomButtons(GL_UISizes.ZoomBtn, this, "ZoomButtons");

		zoomBtn.setX(this.getWidth() - (zoomBtn.getWidth() + UI_Size_Base.that.getMargin()));

		zoomBtn.setOnClickListenerDown(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				// bei einer Zoom Animation in negativer Richtung muss der setDiffCameraZoom gesetzt werden!
				// zoomScale.setDiffCameraZoom(-1.9f, true);
				// zoomScale.setZoom(zoomBtn.getZoom());
				zoomScale.resetFadeOut();
				inputState = InputState.Idle;

				lastDynamicZoom = zoomBtn.getZoom();

				kineticZoom = new KineticZoom(camera.zoom, MapTileLoader.getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce();
				calcPixelsPerMeter();
				return true;
			}
		});
		zoomBtn.setOnClickListenerUp(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				setZoomScale(zoomBtn.getZoom());
				zoomScale.resetFadeOut();
				inputState = InputState.Idle;

				lastDynamicZoom = zoomBtn.getZoom();

				kineticZoom = new KineticZoom(camera.zoom, MapTileLoader.getMapTilePosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);
				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce();
				calcPixelsPerMeter();
				return true;
			}
		});

		if (Mode == MapMode.Compass) {
			zoomBtn.setInvisible();
		} else {
			this.addChild(zoomBtn);
			zoomBtn.setMinimumFadeValue(0.25f);
		}

		this.setOnClickListener(onClickListener);

		float InfoHeight = 0;
		if (Mode == MapMode.Normal) {
			info = (MapInfoPanel) this.addChild(new MapInfoPanel(GL_UISizes.Info, "InfoPanel", this));
			InfoHeight = info.getHeight();
		}

		CB_RectF ZoomScaleRec = new CB_RectF();
		ZoomScaleRec.setSize((float) (44.6666667 * GL_UISizes.DPI), this.getHeight() - InfoHeight - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());
		ZoomScaleRec.setPos(new Vector2(GL_UISizes.margin, zoomBtn.getMaxY() + GL_UISizes.margin));

		zoomScale = new ZoomScale(ZoomScaleRec, "zoomScale", 2, 21, 12);
		if (Mode == MapMode.Normal)
			this.addChild(zoomScale);

		mapIntWidth = (int) rec.getWidth();
		mapIntHeight = (int) rec.getHeight();
		drawingWidth = mapIntWidth;
		drawingHeight = mapIntHeight;

		InitializeMap();

		// initial Zoom Scale
		// zoomScale = new GL_ZoomScale(6, 20, 13);

		mapCacheList = new MapViewCacheList(MapTileLoader.MAX_MAP_ZOOM);

		// from create

		String currentLayerName = Config.CurrentMapLayer.getValue();
		if (ManagerBase.Manager != null) {
			if (mapTileLoader.getCurrentLayer() == null) {
				mapTileLoader.setCurrentLayer(ManagerBase.Manager.getOrAddLayer(currentLayerName, currentLayerName, ""));
			}
		}

		String currentOverlayLayerName = Config.CurrentMapOverlayLayer.getValue();
		if (ManagerBase.Manager != null) {
			if (mapTileLoader.getCurrentOverlayLayer() == null && currentOverlayLayerName.length() > 0)
				mapTileLoader.setCurrentOverlayLayer(ManagerBase.Manager.getOrAddLayer(currentOverlayLayerName, currentOverlayLayerName, ""));
		}

		iconFactor = Config.MapViewDPIFaktor.getValue();

		liveButton = new LiveButton();
		liveButton.setState(Config.LiveMapEnabeld.getDefaultValue());
		Config.DisableLiveMap.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				requestLayout();
			}
		});

		togBtn = new MultiToggleButton(GL_UISizes.Toggle, "toggle");

		togBtn.addState("Free", new HSV_Color(Color.GRAY));
		togBtn.addState("GPS", new HSV_Color(Color.GREEN));
		togBtn.addState("WP", new HSV_Color(Color.MAGENTA));
		togBtn.addState("Lock", new HSV_Color(Color.RED));
		togBtn.addState("Car", new HSV_Color(Color.YELLOW));
		togBtn.setLastStateWithLongClick(true);

		MapState last = MapState.values()[Config.LastMapToggleBtnState.getValue()];
		togBtn.setState(last.ordinal());

		togBtn.setOnStateChangedListener(new OnStateChangeListener() {

			@Override
			public void onStateChange(GL_View_Base v, int State) {
				setMapState(MapState.values()[State]);
			}
		});
		togBtn.registerSkinChangedEvent();

		switch (Mode) {
		case Compass:
			setMapState(MapState.GPS);
			break;
		case Normal:
			setMapState(last);
			break;
		case Track:
			setMapState(MapState.FREE);
			break;
		}

		if (Mode == MapMode.Normal) {
			switch (Config.LastMapToggleBtnState.getValue()) {
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
		}

		if (Mode == MapMode.Normal) {
			this.addChild(togBtn);
			if (Config.DisableLiveMap.getValue()) {
				liveButton.setState(false);
			}
			this.addChild(liveButton);
		}

		infoBubble = new InfoBubble(GL_UISizes.Bubble, "infoBubble");
		infoBubble.setInvisible();
		infoBubble.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				if (infoBubble.saveButtonClicked(x, y)) {
					wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {
						@Override
						public void isCanceld() {

						}
					}, new cancelRunnable() {

						@Override
						public void run() {
							String GcCode = infoBubble.getCache().getGcCode();

							SearchGC searchC = new SearchGC(GcCode);
							searchC.number = 1;
							searchC.available = false;

							CB_List<Cache> apiCaches = new CB_List<Cache>();
							ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
							ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

							try {
								CB_UI.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, infoBubble.getCache().getGPXFilename_ID(), this);
								Cache c = apiCaches.get(0);
								if (c.getGcCode() == GcCode) {
									c.setApiStatus(Cache.NOTLITE);
								}
								GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							// Reload result from DB
							synchronized (Database.Data.Query) {
								String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
								CacheListDAO cacheListDAO = new CacheListDAO();
								cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
							}

							CacheListChangedEventList.Call();
							Cache selCache = Database.Data.Query.GetCacheByGcCode(GcCode);
							GlobalCore.setSelectedCache(selCache);
							infoBubble.setCache(selCache, null, true);
							wd.close();
						}

						@Override
						public boolean cancel() {
							// TODO handle cancel
							return false;
						}
					});
				} else {
					if (infoBubble.getWaypoint() == null) {
						// Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
						Waypoint waypoint = infoBubble.getCache().GetFinalWaypoint();
						// wenn ein Cache keine Final hat, aber einen StartWaypointm, dann wird dieser gleich selektiert
						if (waypoint == null)
							waypoint = infoBubble.getCache().GetStartWaypoint();
						GlobalCore.setSelectedWaypoint(infoBubble.getCache(), waypoint);
					} else {
						GlobalCore.setSelectedWaypoint(infoBubble.getCache(), infoBubble.getWaypoint());
					}
				}

				infoBubble.setInvisible();
				return true;
			}
		});
		if (Mode == MapMode.Normal)
			this.addChild(infoBubble);

		resize(rec.getWidth(), rec.getHeight());

		center = new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());

		// Info aktualisieren
		if (Mode == MapMode.Normal)
			info.setCoord(center);
		aktZoom = Config.lastZoomLevel.getValue();
		zoomBtn.setZoom(aktZoom);
		calcPixelsPerMeter();
		mapScale.zoomChanged();

		if ((center.getLatitude() == -1000) && (center.getLongitude() == -1000)) {
			// not initialized
			center = new CoordinateGPS(48, 12);
		}

		// Initial SettingsChanged Events
		MapView.that.SetNightMode(Config.nightMode.getValue());
		Config.nightMode.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				MapView.this.SetNightMode(Config.nightMode.getValue());
			}
		});

		MapView.that.SetNorthOriented(Config.MapNorthOriented.getValue());
		Config.MapNorthOriented.addChangedEventListener(new IChanged() {

			@Override
			public void isChanged() {
				MapView.this.SetNorthOriented(Config.MapNorthOriented.getValue());
				MapView.this.PositionChanged();
			}
		});

	}

	@Override
	protected void renderSyncronOverlay(Batch batch) {
		batch.setProjectionMatrix(myParentInfo.Matrix());

		// calculate icon size
		int iconSize = 0; // 8x8
		if ((aktZoom >= 13) && (aktZoom <= 14))
			iconSize = 1; // 13x13
		else if (aktZoom > 14)
			iconSize = 2; // default Images

		if (Mode != MapMode.Compass)
			CB_UI.RouteOverlay.RenderRoute(batch, this);
		renderWPs(GL_UISizes.WPSizes[iconSize], GL_UISizes.UnderlaySizes[iconSize], batch);
		renderPositionMarker(batch);
		RenderTargetArrow(batch);
	}

	@Override
	protected void renderNonSyncronOverlay(Batch batch) {
		renderUI(batch);
	}

	void renderUI(Batch batch) {
		batch.setProjectionMatrix(myParentInfo.Matrix());

		if (showMapCenterCross) {
			if (getMapState() == MapState.FREE) {
				if (CrossLines == null) {
					int crossSize = Math.min(mapIntHeight / 3, mapIntWidth / 3) / 2;
					float strokeWidth = 2 * UI_Size_Base.that.getScale();

					GeometryList geomList = new GeometryList();
					Line l1 = new Line(mapIntWidth / 2 - crossSize, mapIntHeight / 2, mapIntWidth / 2 + crossSize, mapIntHeight / 2);
					Line l2 = new Line(mapIntWidth / 2, mapIntHeight / 2 - crossSize, mapIntWidth / 2, mapIntHeight / 2 + crossSize);
					Quadrangle q1 = new Quadrangle(l1, strokeWidth);
					Quadrangle q2 = new Quadrangle(l2, strokeWidth);

					geomList.add(q1);
					geomList.add(q2);

					GL_Paint paint = new GL_Paint();
					paint.setGLColor(COLOR.getCrossColor());
					CrossLines = new PolygonDrawable(geomList.getVertices(), geomList.getTriangles(), paint, mapIntWidth, mapIntHeight);

					geomList.dispose();
					l1.dispose();
					l2.dispose();
					q1.dispose();
					q2.dispose();

				}

				CrossLines.draw(batch, 0, 0, mapIntWidth, mapIntHeight, 0);
			}
		}
	}

	CB_RectF TargetArrowScreenRec;

	private void RenderTargetArrow(Batch batch) {

		if (GlobalCore.getSelectedCache() == null)
			return;

		Coordinate coord = (GlobalCore.getSelectedWaypoint() != null) ? GlobalCore.getSelectedWaypoint().Pos : GlobalCore.getSelectedCache().Pos;

		if (coord == null) {
			return;
		}
		float x = (float) (256.0 * Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, coord.getLongitude()));
		float y = (float) (-256.0 * Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, coord.getLatitude()));

		float halfHeight = (mapIntHeight / 2) - ySpeedVersatz;
		float halfWidth = mapIntWidth / 2;

		// create ScreenRec
		try {
			if (TargetArrowScreenRec == null) {
				TargetArrowScreenRec = new CB_RectF(0, 0, mapIntWidth, mapIntHeight);
				if (Mode != MapMode.Compass) {
					TargetArrowScreenRec.ScaleCenter(0.9f);

					if (Mode == MapMode.Normal) {
						TargetArrowScreenRec.setHeight(TargetArrowScreenRec.getHeight() - (TargetArrowScreenRec.getHeight() - info.getY()) - zoomBtn.getHeight());
						TargetArrowScreenRec.setY(zoomBtn.getMaxY());
					}
				}
			}

			Vector2 ScreenCenter = new Vector2(halfWidth, halfHeight);

			Vector2 screen = worldToScreen(new Vector2(x, y));
			Vector2 target = new Vector2(screen.x, screen.y);

			Vector2 newTarget = TargetArrowScreenRec.getIntersection(ScreenCenter, target);

			// Rotation berechnen
			if (newTarget != null) {

				float direction = get_angle(ScreenCenter.x, ScreenCenter.y, newTarget.x, newTarget.y);
				direction = 180 - direction;

				// draw sprite
				Sprite arrow = Sprites.Arrows.get(4);
				arrow.setRotation(direction);

				float boundsX = newTarget.x - GL_UISizes.TargetArrow.halfWidth;
				float boundsY = newTarget.y - GL_UISizes.TargetArrow.height;

				arrow.setBounds(boundsX, boundsY, GL_UISizes.TargetArrow.width, GL_UISizes.TargetArrow.height);

				arrow.setOrigin(GL_UISizes.TargetArrow.halfWidth, GL_UISizes.TargetArrow.height);
				arrow.draw(batch);

				// get real bounding box of TargetArrow
				float t[] = arrow.getVertices();
				float maxX = Math.max(Math.max(t[0], t[5]), Math.max(t[10], t[15]));
				float minX = Math.min(Math.min(t[0], t[5]), Math.min(t[10], t[15]));
				float maxY = Math.max(Math.max(t[1], t[6]), Math.max(t[11], t[16]));
				float minY = Math.min(Math.min(t[1], t[6]), Math.min(t[11], t[16]));
				TargetArrow.set(minX, minY, maxX - minX, maxY - minY);
			} else {
				TargetArrow.set(0, 0, 0, 0);
			}
		} catch (Exception e) {
			TargetArrow.set(0, 0, 0, 0);
		}
	}

	/**
	 * Rechteck vom Target-Pfeil zur onClick bestimmung
	 */
	private final CB_RectF TargetArrow = new CB_RectF();

	private void renderWPs(SizeF wpUnderlay, SizeF wpSize, Batch batch) {

		if (mapCacheList.list != null) {
			synchronized (mapCacheList.list) {

				for (int i = 0, n = mapCacheList.list.size(); i < n; i++) {
					WaypointRenderInfo wpi = mapCacheList.list.get(i);
					if (wpi.Selected) {
						// wenn der Wp selectiert ist, dann immer in der größten Darstellung
						renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
					} else if (CarMode) {
						// wenn CarMode dann immer in der größten Darstellung
						renderWPI(batch, GL_UISizes.WPSizes[2], GL_UISizes.UnderlaySizes[2], wpi);
					} else {
						renderWPI(batch, wpUnderlay, wpSize, wpi);
					}
				}
			}
		}
		outScreenDraw = 0;
	}

	public void renderWPI(Batch batch, SizeF WpUnderlay, SizeF WpSize, WaypointRenderInfo wpi) {
		Vector2 screen = worldToScreen(new Vector2(wpi.MapX, wpi.MapY));

		screen.y -= ySpeedVersatz;
		// FIXME create a LineDrawable class for create one times and set the Coordinates with calculated Triangles
		if (myPointOnScreen != null && showDirectLine && (wpi.Selected) && (wpi.Waypoint == GlobalCore.getSelectedWaypoint())) {
			// FIXME render only if visible on screen (intersect the screen rec)
			Quadrangle line = new Quadrangle(myPointOnScreen.x, myPointOnScreen.y, screen.x, screen.y, 3 * UI_Size_Base.that.getScale());
			if (paint == null) {
				paint = new GL_Paint();
				paint.setGLColor(Color.RED);
			}
			PolygonDrawable po = new PolygonDrawable(line.getVertices(), line.getTriangles(), paint, this.mapIntWidth, this.mapIntHeight);
			po.draw(batch, 0, 0, this.mapIntWidth, this.mapIntHeight, 0);
			po.dispose();
		}
		// Don't render if outside of screen !!
		if ((screen.x < 0 - WpSize.width || screen.x > this.getWidth() + WpSize.height) || (screen.y < 0 - WpSize.height || screen.y > this.getHeight() + WpSize.height)) {
			if (wpi.Cache != null && (wpi.Cache.Id == infoBubble.getCacheId()) && infoBubble.isVisible()) {
				// check if wp selected
				if (wpi.Waypoint != null && wpi.Waypoint.equals(infoBubble.getWaypoint()) || wpi.Waypoint == null && infoBubble.getWaypoint() == null)
					infoBubble.setInvisible();
			}
			return;
		}

		float NameYMovement = 0;

		if ((aktZoom >= zoomCross) && (wpi.Selected) && (wpi.Waypoint == GlobalCore.getSelectedWaypoint())) {
			// Draw Cross and move screen vector
			Sprite cross = Sprites.MapOverlay.get(3);
			cross.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
			cross.draw(batch);

			screen.add(-WpUnderlay.width, WpUnderlay.height);
			NameYMovement = WpUnderlay.height;
		}

		if (wpi.UnderlayIcon != null) {
			wpi.UnderlayIcon.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
			wpi.UnderlayIcon.draw(batch);
		}
		if (wpi.Icon != null) {
			wpi.Icon.setBounds(screen.x - WpSize.halfWidth, screen.y - WpSize.halfHeight, WpSize.width, WpSize.height);
			wpi.Icon.draw(batch);
		}

		// draw Favorite symbol
		if (wpi.Cache != null && wpi.Cache.isFavorite()) {
			batch.draw(Sprites.getSprite(IconName.favorit.name()), screen.x + (WpSize.halfWidth / 2), screen.y + (WpSize.halfHeight / 2), WpSize.width, WpSize.height);
		}

		if (wpi.OverlayIcon != null) {
			wpi.OverlayIcon.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight, WpUnderlay.width, WpUnderlay.height);
			wpi.OverlayIcon.draw(batch);
		}

		boolean drawAsWaypoint = wpi.Waypoint != null;

		// Rating des Caches darstellen
		if (wpi.Cache != null && showRating && (!drawAsWaypoint) && (wpi.Cache.Rating > 0) && (aktZoom >= 15)) {
			Sprite rating = Sprites.MapStars.get((int) Math.min(wpi.Cache.Rating * 2, 5 * 2));
			rating.setBounds(screen.x - WpUnderlay.halfWidth, screen.y - WpUnderlay.halfHeight - WpUnderlay.Height4_8, WpUnderlay.width, WpUnderlay.Height4_8);
			rating.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			rating.setRotation(0);
			rating.draw(batch);
			NameYMovement += WpUnderlay.Height4_8;
		}

		// Beschriftung
		if (wpi.Cache != null && showTitles && (aktZoom >= 15)) {
			try {
				String Name = drawAsWaypoint ? wpi.Waypoint.getTitle() : wpi.Cache.getName();

				if (layout == null)
					layout = new GlyphLayout(Fonts.getNormal(), Name);
				else
					layout.setText(Fonts.getNormal(), Name);

				float halfWidth = layout.width / 2;
				Fonts.getNormal().draw(batch, layout, screen.x - halfWidth, screen.y - WpUnderlay.halfHeight - NameYMovement);
			} catch (Exception e) {
			}
		}

		// Show D/T-Rating
		if (wpi.Cache != null && showDT && (!drawAsWaypoint) && (aktZoom >= 15)) {
			Sprite difficulty = Sprites.MapStars.get((int) Math.min(wpi.Cache.getDifficulty() * 2, 5 * 2));
			difficulty.setBounds(screen.x - WpUnderlay.width - GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2), WpUnderlay.width, WpUnderlay.Height4_8);
			difficulty.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			difficulty.setRotation(90);
			difficulty.draw(batch);

			Sprite terrain = Sprites.MapStars.get((int) Math.min(wpi.Cache.getTerrain() * 2, 5 * 2));
			terrain.setBounds(screen.x + GL_UISizes.infoShadowHeight, screen.y - (WpUnderlay.Height4_8 / 2), WpUnderlay.width, WpUnderlay.Height4_8);
			terrain.setOrigin(WpUnderlay.width / 2, WpUnderlay.Height4_8 / 2);
			terrain.setRotation(90);
			terrain.draw(batch);

		}

		if (wpi.Cache != null && (wpi.Cache.Id == infoBubble.getCacheId()) && infoBubble.isVisible()) {
			if (infoBubble.getWaypoint() == wpi.Waypoint) {
				Vector2 pos = new Vector2(screen.x - infoBubble.getHalfWidth(), screen.y);
				infoBubble.setPos(pos);
			}
		}
	}

	public static int INITIAL_WP_LIST = 4;

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (cache == null)
			return;
		try {
			if ((cache == lastSelectedCache) && (waypoint == lastSelectedWaypoint)) {
				return;
			}
		} catch (Exception e) {
			return;
		}

		lastSelectedCache = cache;
		lastSelectedWaypoint = waypoint;
		/*
		 * if (InvokeRequired) { Invoke(new targetChangedDelegate(OnTargetChanged), new object[] { cache, waypoint }); return; }
		 */

		// mapCacheList = new MapViewCacheList(MAX_MAP_ZOOM);
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);

		if (getCenterGps()) {
			PositionChanged();
			return;
		}

		positionInitialized = true;

		if (getMapState() != MapState.WP)
			setMapState(MapState.FREE);

		try {
			CoordinateGPS target = (waypoint != null) ? new CoordinateGPS(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude()) : new CoordinateGPS(cache.Pos.getLatitude(), cache.Pos.getLongitude());
			setCenter(target);
		} catch (Exception e) {
		}

		GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);

		// für 2sec rendern lassen, bis Änderungen der WPI-list neu berechnet wurden
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				GL.that.removeRenderView(MapView.this);
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 2000);
		PositionChanged();
	}

	public void LoadTrack(String trackPath) {
		LoadTrack(trackPath, "");
	}

	public void LoadTrack(String trackPath, String file) {

		String absolutPath = "";
		if (file.equals("")) {
			absolutPath = trackPath;
		} else {
			absolutPath = trackPath + "/" + file;
		}
		RouteOverlay.MultiLoadRoute(absolutPath, RouteOverlay.getNextColor());
		RouteOverlay.RoutesChanged();
	}

	@Override
	public void SpeedChanged() {
		if (info != null) {
			info.setSpeed(Locator.SpeedString());

			if (getMapState() == MapState.CAR && Config.dynamicZoom.getValue()) {
				// calculate dynamic Zoom

				double maxSpeed = Config.MoveMapCenterMaxSpeed.getValue();
				int maxZoom = Config.dynamicZoomLevelMax.getValue();
				int minZoom = Config.dynamicZoomLevelMin.getValue();

				double percent = Locator.SpeedOverGround() / maxSpeed;

				float dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
				if (dynZoom > maxZoom)
					dynZoom = maxZoom;
				if (dynZoom < minZoom)
					dynZoom = minZoom;

				if (lastDynamicZoom != dynZoom) {
					lastDynamicZoom = dynZoom;
					zoomBtn.setZoom((int) lastDynamicZoom);
					inputState = InputState.Idle;

					kineticZoom = new KineticZoom(camera.zoom, MapTileLoader.getMapTilePosFactor(lastDynamicZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

					GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
					GL.that.renderOnce();
					calcPixelsPerMeter();
				}
			}
		}

	}

	// Create new Waypoint at screen center
	public void createWaypointAtCenter() {
		String newGcCode = "";
		try {
			newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
		} catch (Exception e) {
			return;
		}
		Coordinate coord = center;
		if ((coord == null) || (!coord.isValid()))
			coord = Locator.getCoordinate();
		if ((coord == null) || (!coord.isValid()))
			return;
		//Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
		Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", newGcCode);

		EditWaypoint EdWp = new EditWaypoint(newWP, new IReturnListener() {

			@Override
			public void returnedWP(Waypoint waypoint) {
				if (waypoint != null) {

					GlobalCore.getSelectedCache().waypoints.add(waypoint);
					GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
					if (waypoint.IsStart) {
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
					GL.that.renderOnce();
				}
			}
		}, true, false);
		EdWp.show();

	}

	@Override
	public void dispose() {
		// remove eventHandler
		SelectedCacheEventList.Remove(this);
		super.dispose();
	}

	@Override
	public void onResized(CB_RectF rec) {
		super.onResized(rec);
		TargetArrowScreenRec = null;
	}

	@Override
	public void onHide() {
		CB_UI.SelectedCacheEventList.Remove(this);
		super.onHide();
	}

	@Override
	protected void loadTiles() {
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, false);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);

		super.loadTiles();

		if (CarMode && CB_UI_Settings.LiveMapEnabeld.getValue()) {

			LiveMapQue.setCenterDescriptor(center);

			// LiveMap queue complete screen
			lo.Data = center;
			LiveMapQue.queScreen(lo, ru);
		}
	}

	@Override
	public void setCenter(CoordinateGPS value) {
		if (Mode == MapMode.Normal)
			info.setCoord(value);
		super.setCenter(value);

	}

	@Override
	public void OrientationChanged() {
		super.OrientationChanged();
		if (info != null) {
			try {
				Coordinate position = Locator.getCoordinate();

				if (GlobalCore.isSetSelectedCache()) {
					Coordinate dest = (GlobalCore.getSelectedWaypoint() != null) ? GlobalCore.getSelectedWaypoint().Pos : GlobalCore.getSelectedCache().Pos;

					if (dest == null)
						return;

					float heading = Locator.getHeading();

					float result[] = new float[2];

					MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);

					float bearing = result[1];

					float relativeBearing = bearing - heading;
					info.setBearing(relativeBearing, heading);
				}
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void InitializeMap() {
		zoomCross = Config.ZoomCross.getValue();
		super.InitializeMap();
	}

	@Override
	protected void setZoomScale(int zoom) {
		// Log.debug(log, "set zoom");
		if (Mode == MapMode.Normal)
			zoomScale.setZoom(zoom);
		if (Mode == MapMode.Normal)
			mapScale.zoomChanged();
	}

	@Override
	protected void calcCenter() {
		super.calcCenter();
		if (Mode == MapMode.Normal) {
			info.setCoord(center);
		}
	}

	@Override
	public void requestLayout() {
		// Log.debug(log, "MapView clacLayout()");
		float margin = GL_UISizes.margin;

		float infoHeight = 0;
		if (Mode == MapMode.Normal) {
			info.setPos(new Vector2(margin, this.mapIntHeight - margin - info.getHeight()));
			info.setVisible(showCompass);
			infoHeight = info.getHeight();
		}
		togBtn.setPos(new Vector2(this.mapIntWidth - margin - togBtn.getWidth(), this.mapIntHeight - margin - togBtn.getHeight()));

		if (Config.DisableLiveMap.getValue()) {
			liveButton.setInvisible();
		} else {
			liveButton.setVisible();
		}

		liveButton.setRec(togBtn);
		liveButton.setY(togBtn.getY() - margin - liveButton.getHeight());

		zoomScale.setSize((float) (44.6666667 * GL_UISizes.DPI), this.getHeight() - infoHeight - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());

		GL.that.renderOnce();
	}

	protected OnClickListener onClickListener = new OnClickListener() {
		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			WaypointRenderInfo minWpi = null;
			Vector2 clickedAt = new Vector2(x, y);

			if (TargetArrow != null && TargetArrow.contains(x, y)) {
				if (GlobalCore.isSetSelectedCache()) {
					if (GlobalCore.isSetSelectedCache()) {
						if (GlobalCore.getSelectedWaypoint() != null) {
							Coordinate tmp = GlobalCore.getSelectedWaypoint().Pos;
							setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
						} else {
							Coordinate tmp = GlobalCore.getSelectedCache().Pos;
							setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
						}

						// switch map state to WP
						togBtn.setState(2);
					}
					return false;
				}
			}

			synchronized (mapCacheList.list) {

				double minDist = Double.MAX_VALUE;
				for (int i = 0, n = mapCacheList.list.size(); i < n; i++) {
					WaypointRenderInfo wpi = mapCacheList.list.get(i);
					Vector2 screen = worldToScreen(new Vector2(Math.round(wpi.MapX), Math.round(wpi.MapY)));
					if (clickedAt != null) {
						double aktDist = Math.sqrt(Math.pow(screen.x - clickedAt.x, 2) + Math.pow(screen.y - clickedAt.y, 2));
						if (aktDist < minDist) {
							minDist = aktDist;
							minWpi = wpi;
						}
					}
				}
				// empty mapCacheList
				if (minWpi == null || minWpi.Cache == null) {
					// Log.info(log, "empty click");
					return true;
				}
				// always hide the bubble
				if (infoBubble.isVisible()) {
					infoBubble.setInvisible();
				}
				// check for showing the bubble
				if (minDist < 40) {
					if (minWpi.Waypoint != null) {
						if (GlobalCore.getSelectedCache() != minWpi.Cache) {
							// Log.info(log, "Waypoint clicked: " + minWpi.Cache.getGcCode() + "/" + minWpi.Waypoint.getGcCode());
							// show bubble at the location of the waypoint!!!
							infoBubble.setCache(minWpi.Cache, minWpi.Waypoint);
							infoBubble.setVisible();
						} else {
							// if only waypoint changes, the bubble will not be shown. (why not ?)
							// so we must do the selection here
							GlobalCore.setSelectedWaypoint(minWpi.Cache, minWpi.Waypoint);
							MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
							data.hideMyFinds = MapView.this.hideMyFinds;
							data.showAllWaypoints = MapView.this.showAllWaypoints;
							mapCacheList.update(data);
						}
					} else {
						// Log.info(log, "Cache clicked: " + minWpi.Cache.getGcCode());
						// show bubble
						infoBubble.setCache(minWpi.Cache, null);
						infoBubble.setVisible();
					}
					inputState = InputState.Idle;
				} else {
					// Click outside Bubble -> hide Bubble
					// Log.info(log, "outside click (hidden bubble)");
				}
			}
			return false;
		}
	};

	@Override
	protected void SetMapStateFree() {
		// setMapState(MapState.FREE);
		// Go over ToggelButton
		togBtn.setState(0);
	}

	@Override
	public void setMapState(MapState state) {
		if (super.getMapState() == state)
			return;

		Config.LastMapToggleBtnState.setValue(state.ordinal());
		Config.AcceptChanges();

		boolean wasCarMode = CarMode;

		if (Mode == MapMode.Normal) {
			info.setCoordType(CoordType.Map);
			if (state == MapState.CAR) {
				if (!wasCarMode) {
					info.setCoordType(CoordType.GPS);
				}
			} else if (state == MapState.WP) {
				info.setCoordType(CoordType.Cache);
			} else if (state == MapState.LOCK) {
				info.setCoordType(CoordType.GPS);
			} else if (state == MapState.GPS) {
				info.setCoordType(CoordType.GPS);
			}
		}

		super.setMapState(state);
	}

	@Override
	public void PositionChanged() {

		if (CarMode) {
			// im CarMode keine Netzwerk Koordinaten zulassen
			if (!Locator.isGPSprovided())
				return;
		}

		super.PositionChanged();

		if (info != null) {
			if (center != null) {
				info.setCoord(center);
			}

			if (GlobalCore.getSelectedCoord() != null) {
				info.setDistance(GlobalCore.getSelectedCoord().Distance(CalculationType.ACCURATE));
			}
			OrientationChanged();
		}

		if (Mode == MapMode.Compass) {
			// Berechne den Zoom so, dass eigene Position und WP auf der Map zu sehen sind.
			Coordinate position = null;
			// if ((GlobalCore.Marker != null) && (GlobalCore.Marker.Valid)) position = GlobalCore.Marker;
			position = Locator.getCoordinate();

			float distance = -1;
			if (GlobalCore.isSetSelectedCache() && position.isValid()) {
				try {
					if (GlobalCore.getSelectedWaypoint() == null)
						distance = position.Distance(GlobalCore.getSelectedCache().Pos, CalculationType.ACCURATE);
					else
						distance = position.Distance(GlobalCore.getSelectedWaypoint().Pos, CalculationType.ACCURATE);
				} catch (Exception e) {
					distance = 10;
				}
			}
			int setZoomTo = zoomBtn.getMinZoom();

			if (DistanceZoomLevel != null) {
				for (int i = zoomBtn.getMaxZoom(); i > zoomBtn.getMinZoom(); i--) {
					if (distance < DistanceZoomLevel.get(i)) {
						setZoomTo = i;
						break;
					}
				}
			}

			if (setZoomTo != lastCompassMapZoom) {
				lastCompassMapZoom = setZoomTo;
				zoomBtn.setZoom(setZoomTo);
				inputState = InputState.Idle;

				kineticZoom = new KineticZoom(camera.zoom, MapTileLoader.getMapTilePosFactor(setZoomTo), System.currentTimeMillis(), System.currentTimeMillis() + ZoomTime);

				GL.that.addRenderView(MapView.this, GL.FRAME_RATE_ACTION);
				GL.that.renderOnce();
				calcPixelsPerMeter();
			}

		}
	}

	@Override
	public boolean doubleClick(int x, int y, int pointer, int button) {
		if (Mode != MapMode.Normal) {
			// Center map on CompassMode
			setMapState(MapState.GPS);
			if (Mode != MapMode.Normal) {
				// If TrackMode set MapState.Free for showing CenterCross
				setMapState(MapState.FREE);
			}

			return true;
		} else {
			return super.doubleClick(x, y, pointer, button);
		}
	}

	@Override
	protected void SkinIsChanged() {
		super.SkinIsChanged();
		MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
		data.hideMyFinds = this.hideMyFinds;
		data.showAllWaypoints = this.showAllWaypoints;
		mapCacheList.update(data);
		if (infoBubble.isVisible()) {
			infoBubble.setCache(infoBubble.getCache(), infoBubble.getWaypoint(), true);
		}
	}

	@Override
	public void onStop() {
		if (Mode == MapMode.Normal) // save last zoom and position only from Map, not from CompassMap
		{
			super.onStop();
		}
	}

	@Override
	public void onShow() {
		super.onShow();

		CB_UI.SelectedCacheEventList.Add(this);
		this.NorthOriented = Mode == MapMode.Normal ? Config.MapNorthOriented.getValue() : false;
		SelectedCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	}

	private static boolean MapTileLoderPreInitial = false;
	private static boolean MapTileLoderPreInitialAtWork = false;

	/**
	 * setNewSettings
	 * 
	 */
	@Override
	public void setNewSettings(int InitialFlags) {
		if ((InitialFlags & INITIAL_SETTINGS) != 0) {
			showRating = Mode == MapMode.Compass ? false : Config.MapShowRating.getValue();
			showDT = Mode == MapMode.Compass ? false : Config.MapShowDT.getValue();
			showTitles = Mode == MapMode.Compass ? false : Config.MapShowTitles.getValue();
			hideMyFinds = Config.MapHideMyFinds.getValue();
			showCompass = Mode == MapMode.Compass ? false : Config.MapShowCompass.getValue();
			showDirectLine = Mode == MapMode.Compass ? false : Config.ShowDirektLine.getValue();
			showAllWaypoints = Mode == MapMode.Compass ? false : Config.ShowAllWaypoints.getValue();
			showAccuracyCircle = Mode == MapMode.Compass ? false : Config.ShowAccuracyCircle.getValue();
			showMapCenterCross = Mode == MapMode.Compass ? false : Config.ShowMapCenterCross.getValue();

			if (Mode == MapMode.Track) {
				showMapCenterCross = true;
				setMapState(MapState.FREE);
			}

			if (info != null)
				info.setVisible(showCompass);

			if (InitialFlags == INITIAL_ALL) {
				iconFactor = Config.MapViewDPIFaktor.getValue();

				int setAktZoom = Mode == MapMode.Compass ? Config.lastZoomLevel.getValue() : Config.lastZoomLevel.getValue();
				int setMaxZoom = Mode == MapMode.Compass ? Config.CompassMapMaxZommLevel.getValue() : Config.OsmMaxLevel.getValue();
				int setMinZoom = Mode == MapMode.Compass ? Config.CompassMapMinZoomLevel.getValue() : Config.OsmMinLevel.getValue();

				aktZoom = setAktZoom;
				zoomBtn.setMaxZoom(setMaxZoom);
				zoomBtn.setMinZoom(setMinZoom);
				zoomBtn.setZoom(aktZoom);

				zoomScale.setMaxZoom(setMaxZoom);
				zoomScale.setMinZoom(setMinZoom);

				if (Mode == MapMode.Compass) {
					// Berechne die darstellbare Entfernung für jedes ZoomLevel
					DistanceZoomLevel = new TreeMap<Integer, Integer>();

					int posiblePixel = (int) this.getHalfHeight();

					for (int i = setMaxZoom; i > setMinZoom; i--) {
						float PixelForZoomLevel = getPixelsPerMeter(i);
						DistanceZoomLevel.put(i, (int) (posiblePixel / PixelForZoomLevel));
					}
				}

			}
		}

		if ((InitialFlags & INITIAL_THEME) != 0) {
			mapsForgeThemePath = null;
			boolean useInvertNightTheme = false;
			if (CarMode) {
				ManagerBase.Manager.textScale = ManagerBase.DEFAULT_TEXT_SCALE * 1.35f;
				if (Config.nightMode.getValue()) {
					if (!setTheme(Config.MapsforgeCarNightTheme.getValue())) {
						useInvertNightTheme = true;
						if (!setTheme(Config.MapsforgeCarDayTheme.getValue())) {
							mapsForgeThemePath = ManagerBase.INTERNAL_CAR_THEME;
						}
					}
				} else {
					if (!setTheme(Config.MapsforgeCarDayTheme.getValue())) {
						mapsForgeThemePath = ManagerBase.INTERNAL_CAR_THEME;
					}
				}
			} else {
				ManagerBase.Manager.textScale = ManagerBase.DEFAULT_TEXT_SCALE;
				if (Config.nightMode.getValue()) {
					if (!setTheme(Config.MapsforgeNightTheme.getValue())) {
						useInvertNightTheme = true;
						setTheme(Config.MapsforgeDayTheme.getValue());
						// else themePath = null : defaults to internal RenderTheme OSMARENDER
					}
				} else {
					setTheme(Config.MapsforgeDayTheme.getValue());
					// else themePath = null : defaults to internal RenderTheme OSMARENDER
				}
			}
			ManagerBase.Manager.setRenderTheme(mapsForgeThemePath, useInvertNightTheme);
		}

		if ((InitialFlags & INITIAL_WP_LIST) != 0) {
			if (mapCacheList != null) {
				MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(screenToWorld(new Vector2(0, 0)), screenToWorld(new Vector2(mapIntWidth, mapIntHeight)), aktZoom, true);
				this.hideMyFinds = Config.MapHideMyFinds.getValue();
				data.hideMyFinds = this.hideMyFinds;
				this.showAllWaypoints = Mode == MapMode.Compass ? false : Config.ShowAllWaypoints.getValue();
				data.showAllWaypoints = this.showAllWaypoints;
				mapCacheList.update(data);
			}

		}

		//preload only if Mapsforge layer selected
		if (mapTileLoader != null && mapTileLoader.getCurrentLayer() != null && mapTileLoader.getCurrentLayer().isMapsForge) {
			if (!MapTileLoderPreInitial) {
				MapTileLoderPreInitial = true;

				Thread preLoadThread = new Thread(new Runnable() {

					@Override
					public void run() {
						int halfMapIntWidth = mapIntWidth / 2;
						int halfMapIntHeight = mapIntHeight / 2;

						synchronized (screenCenterT) {
							screenCenterW.x = screenCenterT.x;
							screenCenterW.y = screenCenterT.y;
						}

						// preload only one Tile(the Tile on Center)
						loVector.set(halfMapIntWidth, halfMapIntHeight);
						lo.set(screenToDescriptor(loVector, aktZoom, lo));

						MapTileLoderPreInitialAtWork = true;
						mapTileLoader.loadTiles(MapView.this, lo, lo, aktZoom);
						Thread checkPreLoadReadyThread = new Thread(new Runnable() {

							@Override
							public void run() {
								while (true) {
									if (mapTileLoader.LoadedTilesSize() > 0) {
										break;
									}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {

									}
								}

								MapTileLoderPreInitialAtWork = false;
							}
						});
						checkPreLoadReadyThread.start();
					}
				});
				preLoadThread.start();
			}
		}
	}

	@Override
	protected void setInitialLocation() {
		try {
			if (Database.Data != null) {

				if (Database.Data.Query != null) {
					synchronized (Database.Data.Query) {
						if (Database.Data.Query.size() > 0) {
							// Koordinaten des ersten Caches der Datenbank
							// nehmen
							setCenter(new CoordinateGPS(Database.Data.Query.get(0).Pos.getLatitude(), Database.Data.Query.get(0).Pos.getLongitude()));
							positionInitialized = true;
							// setLockPosition(0);
						} else {
							// Wenns auch den nicht gibt...)
							setCenter(new CoordinateGPS(48.0, 12.0));
						}
					}
				} else {
					// Wenn Query == null
					setCenter(new CoordinateGPS(48.0, 12.0));
				}
			} else {
				// Wenn Data == null
				setCenter(new CoordinateGPS(48.0, 12.0));
			}
		} catch (Exception e) {
			setCenter(new CoordinateGPS(48.0, 12.0));
		}
	}

	@Override
	public void MapStateChangedToWP() {
		if (GlobalCore.isSetSelectedCache()) {
			if (GlobalCore.getSelectedWaypoint() != null) {
				Coordinate tmp = GlobalCore.getSelectedWaypoint().Pos;
				setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
			} else {
				Coordinate tmp = GlobalCore.getSelectedCache().Pos;
				setCenter(new CoordinateGPS(tmp.getLatitude(), tmp.getLongitude()));
			}
		}
	}

	@Override
	public void SetAlignToCompass(boolean value) {
		super.SetAlignToCompass(value);
		Config.MapNorthOriented.setValue(!value);
	}

}
