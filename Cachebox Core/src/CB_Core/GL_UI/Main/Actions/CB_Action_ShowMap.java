package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.TrackRecorder;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.SearchDialog;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowMap extends CB_Action_ShowView
{
	public final int MI_LAYER = 15;
	public final int MI_ALIGN_TO_COMPSS = 16;
	public final int MI_SMOOTH_SCROLLING = 17;
	public final int MI_SEARCH = 18;
	public final int MI_TREC_REC = 19;
	public final int MI_HIDE_FINDS = 20;
	public final int MI_SHOW_RATINGS = 21;
	public final int MI_SHOW_DT = 22;
	public final int MI_SHOW_TITLE = 23;
	public final int MI_SHOW_DIRECT_LINE = 24;
	public final int MI_MAPVIEW_VIEW = 25;
	public final int MI_SETTINGS = 3;
	public final int MI_ROTATE = 2;
	public final int MI_CENTER_WP = 1;

	public CB_Action_ShowMap()
	{
		super("Map", AID_SHOW_MAP);
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
		return SpriteCache.Icons.get(5);
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
		icm.setItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MI_LAYER, "Layer");
		mi = icm.addItem(MI_ALIGN_TO_COMPSS, "AlignToCompass");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.GetAlignToCompass());

		mi = icm.addItem(MI_CENTER_WP, "CenterWP");

		// mi = icm.addItem(MI_SMOOTH_SCROLLING, "SmoothScrolling");
		mi = icm.addItem(MI_SETTINGS, "settings", SpriteCache.Icons.get(26));
		mi = icm.addItem(MI_SEARCH, "search", SpriteCache.Icons.get(27));
		mi = icm.addItem(MI_MAPVIEW_VIEW, "view");
		mi = icm.addItem(MI_TREC_REC, "TrackRec");

		return icm;
	}

	public void showMapLayerMenu()
	{
		Menu icm = new Menu("MapViewShowLayerContextMenu");

		icm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Layer layer = (Layer) ((MenuItem) v).getData();
				TabMainView.mapView.SetCurrentLayer(layer);
				return true;
			}
		});
		MenuItem mi;

		int Index = 0;
		for (Layer layer : ManagerBase.Manager.Layers)
		{
			mi = icm.addItem(Index++, "", layer.Name);
			mi.setData(layer);
			mi.setCheckable(true);
			if (layer == MapView.mapTileLoader.CurrentLayer)
			{
				mi.setChecked(true);
			}
		}

		icm.show();
	}

	public void showMapViewLayerMenu()
	{
		Menu icm = new Menu("MapViewShowLayerContextMenu");

		icm.setItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MI_HIDE_FINDS, "HideFinds");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.hideMyFinds);

		mi = icm.addItem(MI_SHOW_RATINGS, "ShowRatings");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showRating);

		mi = icm.addItem(MI_SHOW_DT, "ShowDT");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showDT);

		mi = icm.addItem(MI_SHOW_TITLE, "ShowTitle");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showTitles);

		mi = icm.addItem(MI_SHOW_DIRECT_LINE, "ShowDirectLine");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showDirektLine);

		icm.show();
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			switch (((MenuItem) v).getMenuItemId())
			{
			case MI_LAYER:
				showMapLayerMenu();
				return true;

			case MI_MAPVIEW_VIEW:
				showMapViewLayerMenu();
				return true;

			case MI_ALIGN_TO_COMPSS:
				MapView.that.SetAlignToCompass(!MapView.that.GetAlignToCompass());
				return true;

			case MI_HIDE_FINDS:
				MapView.that.hideMyFinds = !MapView.that.hideMyFinds;
				return true;

			case MI_SHOW_RATINGS:
				MapView.that.showRating = !MapView.that.showRating;
				return true;

			case MI_SHOW_DT:
				MapView.that.showDT = !MapView.that.showDT;
				return true;

			case MI_SHOW_TITLE:
				MapView.that.showTitles = !MapView.that.showTitles;
				return true;

			case MI_SHOW_DIRECT_LINE:
				MapView.that.showDirektLine = !MapView.that.showDirektLine;
				return true;

			case MI_CENTER_WP:
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

				return true;

			case MI_SETTINGS:
				TabMainView.actionShowSettings.Execute();
				return true;

			case MI_SEARCH:
				if (SearchDialog.that == null)
				{
					new SearchDialog();
				}

				SearchDialog.that.showNotCloseAutomaticly();
				return true;

			case MI_TREC_REC:
				showMenuTrackRecording();
				return true;

			default:
				String br = System.getProperty("line.separator");

				String msgText = "Ein OnClick vom Menu Item kommt an" + br + "Item " + ((MenuItem) v).getMenuItemId() + br + br
						+ "geklickt";
				// String msgTitle = "CB_ALLContextMenuHandler";
				String msgTitle = "CB_AL";

				GL_MsgBox.Show(msgText, msgTitle, MessageBoxButtons.OK, MessageBoxIcon.Information, null);
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
		cm2.setItemClickListner(new OnClickListener()
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

		cm2.show();
	}
}
