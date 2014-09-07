package de.CB.TestBase.Views;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.reader.header.MapFileInfo;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.MapViewBase;
import CB_Locator.Map.ZoomScale;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.ZoomButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import de.CB.TestBase.Config;

public class MapView extends MapViewBase
{

	public static MapView that;

	public MapView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		that = this;

		this.setOnDoubleClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Center own position!
				MapFileInfo info = ManagerBase.Manager.getMapsforgeLodedMapFileInfo(mapTileLoader.getCurrentLayer());
				LatLong lalo = info.boundingBox.getCenterPoint();
				CoordinateGPS cor = new CoordinateGPS(lalo.getLatitude(), lalo.getLongitude());
				MainView.mapView.setCenter(cor);
				return true;
			}
		});

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

		// mapScale = new MapScale(new CB_RectF(GL_UISizes.margin, GL_UISizes.margin, this.getHalfWidth(),
		// GL_UISizes.ZoomBtn.getHalfWidth() / 4), "mapScale", this, Config.ImperialUnits.getValue());
		//
		// this.addChild(mapScale);

		float margin = GL_UISizes.margin;

		// initial Zoom Buttons
		zoomBtn = new ZoomButtons(GL_UISizes.ZoomBtn, this, "ZoomButtons");
		zoomBtn.setPortrait();
		zoomBtn.disableFadeOut();
		zoomBtn.setSize(GL_UISizes.ZoomBtn.getHeight(), GL_UISizes.ZoomBtn.getWidth());
		zoomBtn.setPos(this.getWidth() - margin - zoomBtn.getWidth(), this.getHeight() - margin - zoomBtn.getHeight());
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
				GL.that.renderOnce();
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
				GL.that.renderOnce();
				calcPixelsPerMeter();
				return true;
			}
		});

		this.addChild(zoomBtn);

		CB_RectF ZoomScaleRec = new CB_RectF();
		ZoomScaleRec.setSize((float) (44.6666667 * GL_UISizes.DPI), this.getHeight() - (GL_UISizes.margin * 4) - zoomBtn.getMaxY());
		ZoomScaleRec.setPos(new Vector2(GL_UISizes.margin, zoomBtn.getMaxY() + GL_UISizes.margin));

		zoomScale = new ZoomScale(ZoomScaleRec, "zoomScale", 2, 21, 12);
		this.addChild(zoomScale);

		InitializeMap();

		onResized(rec);

		// from create

		String currentLayerName = Config.CurrentMapLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			if (mapTileLoader.getCurrentLayer() == null)
			{
				mapTileLoader.setLayer(ManagerBase.Manager.GetLayerByName(currentLayerName, currentLayerName, ""));
			}
		}

		String currentOverlayLayerName = Config.CurrentMapOverlayLayer.getValue();
		if (ManagerBase.Manager != null)
		{
			if (mapTileLoader.getCurrentOverlayLayer() == null && currentOverlayLayerName.length() > 0) mapTileLoader
					.setOverlayLayer(ManagerBase.Manager.GetLayerByName(currentOverlayLayerName, currentOverlayLayerName, ""));
		}

		mapIntWidth = (int) rec.getWidth();
		mapIntHeight = (int) rec.getHeight();
		drawingWidth = mapIntWidth;
		drawingHeight = mapIntHeight;

		iconFactor = Config.MapViewDPIFaktor.getValue();

		// togBtn = new MultiToggleButton(GL_UISizes.Toggle, "toggle");
		//
		// togBtn.addState("Free", Color.GRAY);
		// togBtn.addState("GPS", Color.GREEN);
		// togBtn.addState("WP", Color.MAGENTA);
		// togBtn.addState("Lock", Color.RED);
		// togBtn.addState("Car", Color.YELLOW);
		// togBtn.setLastStateWithLongClick(true);
		//
		// MapState last = MapState.values()[Config.LastMapToggleBtnState.getValue()];
		// togBtn.setState(last.ordinal());
		// setMapState(last);
		//
		// togBtn.setOnStateChangedListner(new OnStateChangeListener()
		// {
		//
		// @Override
		// public void onStateChange(GL_View_Base v, int State)
		// {
		// setMapState(MapState.values()[State]);
		// }
		// });
		// togBtn.registerSkinChangedEvent();
		//
		// setMapState(CompassMode ? MapState.GPS : last);
		// switch (Config.LastMapToggleBtnState.getValue())
		// {
		// case 0:
		// info.setCoordType(CoordType.Map);
		// break;
		// case 1:
		// info.setCoordType(CoordType.GPS);
		// break;
		// case 2:
		// info.setCoordType(CoordType.Cache);
		// break;
		// case 3:
		// info.setCoordType(CoordType.GPS);
		// break;
		// case 4:
		// info.setCoordType(CoordType.GPS);
		// break;
		// }
		//
		// if (!CompassMode) this.addChild(togBtn);

		resize(rec.getWidth(), rec.getHeight());

		
		center=new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
		
		aktZoom = Config.lastZoomLevel.getValue();
		zoomBtn.setZoom(aktZoom);
		calcPixelsPerMeter();
		// mapScale.zoomChanged();

		if ((center.getLatitude() == -1000) && (center.getLongitude() == -1000))
		{
			// not initialized
			center = new CoordinateGPS(48, 12);
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

	@Override
	public void SpeedChanged()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void MapStateChangedToWP()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void renderSyncronOverlay(Batch arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void requestLayout()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void setInitialLocation()
	{
		setCenter(new CoordinateGPS(52.579, 13.382));

	}

	@Override
	public void invalidateTexture()
	{

		mapTileLoader.clearLoadedTiles();

	}

	@Override
	protected void renderNonSyncronOverlay(Batch batch) {
		// TODO Auto-generated method stub
		
	}

}
