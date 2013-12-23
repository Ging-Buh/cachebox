package CB_UI.GL_UI.Main.Actions;

import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_UI.Config;
import CB_UI.TrackRecorder;
import CB_UI.GL_UI.Activitys.MapDownload;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Menu.OptionMenu;
import CB_Utils.Settings.SettingBool;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowMap extends CB_Action_ShowView
{

	public CB_Action_ShowMap()
	{
		super("Map", MenuID.AID_SHOW_MAP);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.mapView == null) && (tabMainView != null) && (tab != null)) TabMainView.mapView = new MapView(tab.getContentRec(),
				false, "MapView");

		if ((TabMainView.mapView != null) && (tab != null)) tab.ShowView(TabMainView.mapView);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.mapView;
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.map_5.ordinal());
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{

		Menu icm = new Menu("menu_mapviewgl");
		icm.addItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_LAYER, "Layer");

		mi = icm.addItem(MenuID.MI_ALIGN_TO_COMPSS, "AlignToCompass");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.GetAlignToCompass());

		mi = icm.addItem(MenuID.MI_CENTER_WP, "CenterWP");

		// mi = icm.addItem(MI_SMOOTH_SCROLLING, "SmoothScrolling");
		mi = icm.addItem(MenuID.MI_SETTINGS, "settings", SpriteCacheBase.Icons.get(IconName.settings_26.ordinal()));
		// mi = icm.addItem(MenuID.MI_SEARCH, "search", SpriteCache.Icons.get(27));
		mi = icm.addItem(MenuID.MI_MAPVIEW_VIEW, "view");
		// mi = icm.addItem(MenuID.MI_TREC_REC, "TrackRec");
		mi = icm.addItem(MenuID.MI_MAP_DOWNOAD, "MapDownload");
		return icm;
	}

	private void showMapLayerMenu()
	{
		Menu icm = new Menu("MapViewShowLayerContextMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				if (((MenuItem) v).getMenuItemId() == MenuID.MI_MAPVIEW_OVERLAY_VIEW)
				{
					showMapOverlayMenu();
					return true;
				}

				Layer layer = (Layer) ((MenuItem) v).getData();
				TabMainView.mapView.SetCurrentLayer(layer);
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_MAPVIEW_OVERLAY_VIEW, "overlays");

		int Index = 0;
		for (Layer layer : ManagerBase.Manager.getLayers())
		{
			if (!layer.isOverlay())
			{
				mi = icm.addItem(Index++, "", layer.Name);
				mi.setData(layer);
				mi.setCheckable(true);
				if (layer == MapView.mapTileLoader.CurrentLayer)
				{
					mi.setChecked(true);
				}
			}
		}

		icm.Show();
	}

	private void showMapOverlayMenu()
	{
		final OptionMenu icm = new OptionMenu("MapViewShowMapOverlayMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Layer layer = (Layer) ((MenuItem) v).getData();
				if (layer == MapView.mapTileLoader.CurrentOverlayLayer)
				{
					// switch off Overlay
					TabMainView.mapView.SetCurrentOverlayLayer(null);
				}
				else
				{
					TabMainView.mapView.SetCurrentOverlayLayer(layer);
				}
				// Refresh menu
				icm.close();
				showMapOverlayMenu();
				return true;
			}
		});
		MenuItem mi;

		int Index = 0;
		for (Layer layer : ManagerBase.Manager.getLayers())
		{
			if (layer.isOverlay())
			{
				mi = icm.addItem(Index++, "", layer.Name);
				mi.setData(layer);
				mi.setCheckable(true);
				if (layer == MapView.mapTileLoader.CurrentOverlayLayer)
				{
					mi.setChecked(true);
				}
			}
		}

		icm.Show();
	}

	private void showMapViewLayerMenu()
	{
		OptionMenu icm = new OptionMenu("MapViewShowLayerContextMenu");

		icm.addItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_HIDE_FINDS, "HideFinds");
		mi.setCheckable(true);
		mi.setChecked(Config.MapHideMyFinds.getValue());

		mi = icm.addItem(MenuID.MI_MAP_SHOW_COMPASS, "MapShowCompass");
		mi.setCheckable(true);
		mi.setChecked(Config.MapShowCompass.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_ALL_WAYPOINTS, "ShowAllWaypoints");
		mi.setCheckable(true);
		mi.setChecked(Config.ShowAllWaypoints.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_RATINGS, "ShowRatings");
		mi.setCheckable(true);
		mi.setChecked(Config.MapShowRating.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_DT, "ShowDT");
		mi.setCheckable(true);
		mi.setChecked(Config.MapShowDT.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_TITLE, "ShowTitle");
		mi.setCheckable(true);
		mi.setChecked(Config.MapShowTitles.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_DIRECT_LINE, "ShowDirectLine");
		mi.setCheckable(true);
		mi.setChecked(Config.ShowDirektLine.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_ACCURACY_CIRCLE, "ShowAccuracyCircle");
		mi.setCheckable(true);
		mi.setChecked(Config.ShowAccuracyCircle.getValue());

		mi = icm.addItem(MenuID.MI_SHOW_CENTERCROSS, "ShowCenterCross");
		mi.setCheckable(true);
		mi.setChecked(Config.ShowMapCenterCross.getValue());

		icm.Show();
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			switch (((MenuItem) v).getMenuItemId())
			{
			case MenuID.MI_LAYER:
				showMapLayerMenu();
				return true;

			case MenuID.MI_MAPVIEW_VIEW:
				showMapViewLayerMenu();
				return true;

			case MenuID.MI_ALIGN_TO_COMPSS:
				MapView.that.SetAlignToCompass(!MapView.that.GetAlignToCompass());
				return true;

			case MenuID.MI_HIDE_FINDS:
				toggleSetting(Config.MapHideMyFinds);

				return true;

			case MenuID.MI_SHOW_ALL_WAYPOINTS:
				toggleSetting(Config.ShowAllWaypoints);
				return true;

			case MenuID.MI_SHOW_RATINGS:
				toggleSetting(Config.MapShowRating);
				return true;

			case MenuID.MI_SHOW_DT:
				toggleSetting(Config.MapShowDT);
				return true;

			case MenuID.MI_SHOW_TITLE:
				toggleSetting(Config.MapShowTitles);
				return true;

			case MenuID.MI_SHOW_DIRECT_LINE:
				toggleSetting(Config.ShowDirektLine);
				return true;

			case MenuID.MI_SHOW_ACCURACY_CIRCLE:
				toggleSetting(Config.ShowAccuracyCircle);
				return true;

			case MenuID.MI_SHOW_CENTERCROSS:
				toggleSetting(Config.ShowMapCenterCross);
				return true;

			case MenuID.MI_MAP_SHOW_COMPASS:
				toggleSetting(Config.MapShowCompass);
				return true;

			case MenuID.MI_CENTER_WP:
				if (MapView.that != null)
				{
					MapView.that.createWaypointAtCenter();
				}
				return true;

			case MenuID.MI_SETTINGS:
				TabMainView.actionShowSettings.Execute();
				return true;

			case MenuID.MI_SEARCH:
				if (SearchDialog.that == null)
				{
					new SearchDialog();
				}

				SearchDialog.that.showNotCloseAutomaticly();
				return true;

			case MenuID.MI_TREC_REC:
				showMenuTrackRecording();
				return true;

			case MenuID.MI_MAP_DOWNOAD:
				MapDownload.INSTANCE.show();
				return true;

			default:

				return false;

			}

		}
	};

	private static final int START = 1;
	private static final int PAUSE = 2;
	private static final int STOP = 3;

	private void showMenuTrackRecording()
	{
		MenuItem mi;
		Menu cm2 = new Menu("TrackRecordContextMenu");
		cm2.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case START:
					TrackRecorder.StartRecording();
					return true;
				case PAUSE:
					TrackRecorder.PauseRecording();
					return true;
				case STOP:
					TrackRecorder.StopRecording();
					return true;
				}
				return false;
			}
		});
		mi = cm2.addItem(START, "start");
		mi.setEnabled(!TrackRecorder.recording);

		if (TrackRecorder.pauseRecording) mi = cm2.addItem(PAUSE, "continue");
		else
			mi = cm2.addItem(PAUSE, "pause");

		mi.setEnabled(TrackRecorder.recording);

		mi = cm2.addItem(STOP, "stop");
		mi.setEnabled(TrackRecorder.recording | TrackRecorder.pauseRecording);

		cm2.Show();
	}

	private void toggleSetting(SettingBool setting)
	{
		setting.setValue(!setting.getValue());
		Config.AcceptChanges();
		if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_SETTINGS_WITH_OUT_ZOOM);
	}

}
