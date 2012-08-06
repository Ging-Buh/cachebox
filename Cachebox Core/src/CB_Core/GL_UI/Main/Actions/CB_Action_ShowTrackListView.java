package CB_Core.GL_UI.Main.Actions;

import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.IgetFileReturnListner;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.ProjectionCoordinate;
import CB_Core.GL_UI.Activitys.ProjectionCoordinate.Type;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.RouteOverlay.Track;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackListView extends CB_Action_ShowView
{

	public CB_Action_ShowTrackListView()
	{
		super("Tracks", AID_SHOW_TRACKLIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.trackListView == null) && (tabMainView != null) && (tab != null)) TabMainView.trackListView = new TrackListView(
				tab.getContentRec(), "TrackListView");

		if ((TabMainView.trackListView != null) && (tab != null)) tab.ShowView(TabMainView.trackListView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(8);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackListView;
	}

	private static final int GENERATE = 1;
	private static final int RENAME = 2;
	private static final int LOAD = 3;
	private static final int SAVE = 4;
	private static final int DELETE = 5;
	private static final int P2P = 6;
	private static final int PROJECT = 7;
	private static final int CIRCLE = 8;

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public boolean ShowContextMenu()
	{
		Menu cm = new Menu("TrackListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case GENERATE:
					showMenuCreate();
					return true;
				case RENAME:
					// ;
					return true;
				case LOAD:
					platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", new IgetFileReturnListner()
					{
						@Override
						public void getFieleReturn(String Path)
						{
							if (Path != null)
							{
								Color[] ColorField = new Color[8];
								ColorField[0] = Color.RED;
								ColorField[1] = Color.YELLOW;
								ColorField[2] = Color.BLACK;
								ColorField[3] = Color.LIGHT_GRAY;
								ColorField[4] = Color.GREEN;
								ColorField[5] = Color.BLUE;
								ColorField[6] = Color.CYAN;
								ColorField[7] = Color.GRAY;
								Color TrackColor;
								TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];

								RouteOverlay.Routes.add(RouteOverlay.LoadRoute(Path, TrackColor, Config.settings.TrackDistance.getValue()));
								Logger.LogCat("Load Track :" + Path);
							}
						}
					});

					return true;
				case SAVE:
					return true;
				case DELETE:
					// platformConector.menuItemClicked(MenuItemConst.TRACK_LIST_DELETE);
					int selectedTrackItem = ((ListViewItemBase) v).getIndex();
					// if (selectedTrackItem == null)
					// {
					// GL_MsgBox.Show(GlobalCore.Translations.Get("NoTrackSelected"), null,
					// MessageBoxButtons.OK, MessageBoxIcon.Warning, new OnMsgBoxClickListener()
					// {
					//
					// @Override
					// public boolean onClick(int which)
					// {
					// that.show();
					// return true;
					// }
					// });
					// return true;
					// }
					//
					// if (selectedItem.getRoute().IsActualTrack)
					// {
					// MessageBox.Show(GlobalCore.Translations.Get("IsActualTrack"));
					// return;
					// }
					//
					// RouteOverlay.Routes.remove(selectedItem.getRoute());
					// selectedItem = null;
					// lvAdapter.notifyDataSetChanged();
					return true;

				}
				return false;
			}
		});

		cm.addItem(GENERATE, "generate");
		cm.addItem(RENAME, "rename");
		cm.addItem(LOAD, "load");
		cm.addItem(SAVE, "save");
		cm.addItem(DELETE, "delete");

		cm.show();

		return true;
	}

	private void showMenuCreate()
	{
		Menu cm2 = new Menu("TrackListCreateContextMenu");
		cm2.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case P2P:
					GenTrackP2P();
					return true;
				case PROJECT:
					GenTrackProjection();
					return true;
				case CIRCLE:
					GenTrackCircle();
					return true;
				}
				return false;
			}
		});
		cm2.addItem(P2P, "Point2Point");
		cm2.addItem(PROJECT, "Projection");
		cm2.addItem(CIRCLE, "Circle");

		cm2.show();
	}

	private void GenTrackP2P()
	{
		final Coordinate coord = GlobalCore.LastValidPosition;

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), GlobalCore.Translations.Get("fromPoint"), coord,
				new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{
						float[] dist = new float[4];
						Color[] ColorField = new Color[8];
						ColorField[0] = Color.RED;
						ColorField[1] = Color.YELLOW;
						ColorField[2] = Color.BLACK;
						ColorField[3] = Color.LIGHT_GRAY;
						ColorField[4] = Color.GREEN;
						ColorField[5] = Color.BLUE;
						ColorField[6] = Color.CYAN;
						ColorField[7] = Color.GRAY;
						Color TrackColor;
						TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];
						Track route = new Track(null, TrackColor);

						route.Name = "Point 2 Point Route";
						route.Points.add(new TrackPoint(targetCoord.Longitude, targetCoord.Latitude, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.Longitude, startCoord.Latitude, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.Latitude, targetCoord.Longitude, startCoord.Latitude, startCoord.Longitude,
								dist);
						route.TrackLength = dist[0];

						route.ShowRoute = true;
						RouteOverlay.Routes.add(route);
					}
				}, Type.p2p);
		pC.show();

	}

	private void GenTrackProjection()
	{
		final Coordinate coord = GlobalCore.LastValidPosition;

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), GlobalCore.Translations.Get("Projection"), coord,
				new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{
						float[] dist = new float[4];
						Color[] ColorField = new Color[8];
						ColorField[0] = Color.RED;
						ColorField[1] = Color.YELLOW;
						ColorField[2] = Color.BLACK;
						ColorField[3] = Color.LIGHT_GRAY;
						ColorField[4] = Color.GREEN;
						ColorField[5] = Color.BLUE;
						ColorField[6] = Color.CYAN;
						ColorField[7] = Color.GRAY;
						Color TrackColor;
						TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];
						Track route = new Track(null, TrackColor);
						route.Name = "Projected Route";

						route.Points.add(new TrackPoint(targetCoord.Longitude, targetCoord.Latitude, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.Longitude, startCoord.Latitude, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.Latitude, targetCoord.Longitude, startCoord.Latitude, startCoord.Longitude,
								dist);
						route.TrackLength = dist[0];

						route.ShowRoute = true;
						RouteOverlay.Routes.add(route);

					}

				}, Type.projetion);

		pC.show();

	}

	private void GenTrackCircle()
	{
		final Coordinate coord = GlobalCore.LastValidPosition;

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), GlobalCore.Translations.Get("centerPoint"), coord,
				new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{
						float[] dist = new float[4];
						Color[] ColorField = new Color[8];
						ColorField[0] = Color.RED;
						ColorField[1] = Color.YELLOW;
						ColorField[2] = Color.BLACK;
						ColorField[3] = Color.LIGHT_GRAY;
						ColorField[4] = Color.GREEN;
						ColorField[5] = Color.BLUE;
						ColorField[6] = Color.CYAN;
						ColorField[7] = Color.GRAY;
						Color TrackColor;
						TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];
						Track route = new Track(null, TrackColor);
						route.Name = "Circle Route";

						route.ShowRoute = true;
						RouteOverlay.Routes.add(route);

						Coordinate Projektion = new Coordinate();
						Coordinate LastCoord = new Coordinate();

						for (int i = 0; i <= 360; i++)
						{
							Projektion = Coordinate.Project(startCoord.Latitude, startCoord.Longitude, (double) i, distance);

							route.Points.add(new TrackPoint(Projektion.Longitude, Projektion.Latitude, 0, new Date()));

							if (!LastCoord.Valid)
							{
								LastCoord = Projektion;
								LastCoord.Valid = true;
							}
							else
							{
								Coordinate.distanceBetween(Projektion.Latitude, Projektion.Longitude, LastCoord.Latitude,
										LastCoord.Longitude, dist);
								route.TrackLength += dist[0];
								LastCoord = Projektion;
								LastCoord.Valid = true;
							}

						}
					}

				}, Type.circle);

		pC.show();
	}

}
