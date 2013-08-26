package CB_UI.GL_UI.Main.Actions;

import java.util.Date;

import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate.Type;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.TrackListView;
import CB_UI.GL_UI.Views.TrackListViewItem;
import CB_UI.Map.RouteOverlay;
import CB_UI.Map.RouteOverlay.Track;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_Utils.Log.Logger;
import CB_Utils.Math.TrackPoint;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackListView extends CB_Action_ShowView
{

	Color TrackColor;

	public CB_Action_ShowTrackListView()
	{
		super("Tracks", MenuID.AID_SHOW_TRACKLIST);
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
		return SpriteCacheBase.Icons.get(IconName.trackList_8.ordinal());
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackListView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		Menu cm = new Menu("TrackListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_GENERATE:
					showMenuCreate();
					return true;

				case MenuID.MI_RENAME:
					if (TrackListView.that != null)
					{
						final TrackListViewItem selectedTrackItem = TrackListView.that.getSelectedItem();

						StringInputBox.Show(WrapType.SINGLELINE, selectedTrackItem.getRoute().Name, Translation.Get("RenameTrack"),
								selectedTrackItem.getRoute().Name, new OnMsgBoxClickListener()
								{

									@Override
									public boolean onClick(int which, Object data)
									{
										String text = StringInputBox.editText.getText();
										// Behandle das ergebniss
										switch (which)
										{
										case 1: // ok Clicket
											selectedTrackItem.getRoute().Name = text;
											TrackListView.that.notifyDataSetChanged();
											break;
										case 2: // cancel clicket
											break;
										case 3:
											break;
										}

										return true;
									}
								});

						TrackListView.that.notifyDataSetChanged();
						return true;
					}
					return true;

				case MenuID.MI_LOAD:
					platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", Translation.Get("LoadTrack"),
							Translation.Get("load"), new IgetFileReturnListner()
							{
								@Override
								public void getFieleReturn(String Path)
								{
									if (Path != null)
									{
										TrackColor = RouteOverlay.getNextColor();

										RouteOverlay.MultiLoadRoute(Path, TrackColor);
										Logger.LogCat("Load Track :" + Path);
										if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
									}
								}
							});

					return true;

				case MenuID.MI_SAVE:
					platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", Translation.Get("SaveTrack"),
							Translation.Get("save"), new IgetFileReturnListner()
							{
								TrackListViewItem selectedTrackItem = TrackListView.that.getSelectedItem();

								@Override
								public void getFieleReturn(String Path)
								{
									if (Path != null)
									{
										RouteOverlay.SaveRoute(Path, selectedTrackItem.getRoute());
										Logger.LogCat("Load Track :" + Path);
										if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
									}
								}
							});

					return true;

				case MenuID.MI_DELETE_TRACK:
					if (TrackListView.that != null)
					{
						TrackListViewItem selectedTrackItem = TrackListView.that.getSelectedItem();

						if (selectedTrackItem == null)
						{
							GL_MsgBox.Show(Translation.Get("NoTrackSelected"), null, MessageBoxButtons.OK, MessageBoxIcon.Warning,
									new OnMsgBoxClickListener()
									{

										@Override
										public boolean onClick(int which, Object data)
										{
											// hier brauchen wir nichts machen!
											return true;
										}
									});
							return true;
						}

						if (selectedTrackItem.getRoute().IsActualTrack)
						{
							GL_MsgBox.Show(Translation.Get("IsActualTrack"), null, MessageBoxButtons.OK, MessageBoxIcon.Warning, null);
							return false;
						}

						RouteOverlay.remove(selectedTrackItem.getRoute());
						selectedTrackItem = null;
						TrackListView.that.notifyDataSetChanged();
						return true;
					}
				}
				return false;
			}
		});

		TrackListViewItem selectedTrackItem = TrackListView.that.getSelectedItem();
		cm.addItem(MenuID.MI_LOAD, "load");
		cm.addItem(MenuID.MI_GENERATE, "generate");
		// rename, save, delete darf nicht mit dem aktuellen Track gemacht werden....
		if (selectedTrackItem != null && !selectedTrackItem.getRoute().IsActualTrack)
		{
			cm.addItem(MenuID.MI_RENAME, "rename");
			cm.addItem(MenuID.MI_SAVE, "save");
			cm.addItem(MenuID.MI_DELETE_TRACK, "delete");
		}

		return cm;
	}

	private void showMenuCreate()
	{
		Menu cm2 = new Menu("TrackListCreateContextMenu");
		cm2.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_P2P:
					GenTrackP2P();
					return true;
				case MenuID.MI_PROJECT:
					GenTrackProjection();
					return true;
				case MenuID.MI_CIRCLE:
					GenTrackCircle();
					return true;
				}
				return false;
			}
		});
		cm2.addItem(MenuID.MI_P2P, "Point2Point");
		cm2.addItem(MenuID.MI_PROJECT, "Projection");
		cm2.addItem(MenuID.MI_CIRCLE, "Circle");

		cm2.Show();
	}

	private void GenTrackP2P()
	{
		Coordinate coord = GlobalCore.getSelectedCoord();

		if (coord == null) coord = Locator.getCoordinate();

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), Translation.Get("fromPoint"), coord,
				new CB_UI.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{

						if (targetCoord == null || startCoord == null) return;

						float[] dist = new float[4];
						TrackColor = RouteOverlay.getNextColor();
						Track route = new Track(null, TrackColor);

						route.Name = "Point 2 Point Route";
						route.Points.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(),
								startCoord.getLongitude(), dist);
						route.TrackLength = dist[0];

						route.ShowRoute = true;
						RouteOverlay.add(route);
						if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
					}
				}, Type.p2p);
		pC.show();

	}

	private void GenTrackProjection()
	{
		Coordinate coord = GlobalCore.getSelectedCoord();
		if (coord == null) coord = Locator.getCoordinate();

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), Translation.Get("Projection"), coord,
				new CB_UI.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{

						if (targetCoord == null || startCoord == null) return;

						float[] dist = new float[4];
						TrackColor = RouteOverlay.getNextColor();
						Track route = new Track(null, TrackColor);
						route.Name = "Projected Route";

						route.Points.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(),
								startCoord.getLongitude(), dist);
						route.TrackLength = dist[0];

						route.ShowRoute = true;
						RouteOverlay.add(route);
						if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
					}

				}, Type.projetion);

		pC.show();

	}

	private void GenTrackCircle()
	{
		Coordinate coord = GlobalCore.getSelectedCoord();
		if (coord == null) coord = Locator.getCoordinate();

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), Translation.Get("centerPoint"), coord,
				new CB_UI.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{

						if (targetCoord == null || startCoord == null) return;

						float[] dist = new float[4];
						TrackColor = RouteOverlay.getNextColor();
						Track route = new Track(null, TrackColor);
						route.Name = "Circle Route";

						route.ShowRoute = true;
						RouteOverlay.add(route);

						Coordinate Projektion = new Coordinate();
						Coordinate LastCoord = new Coordinate();

						for (int i = 0; i <= 360; i += 10) // Achtung der Kreis darf nicht mehr als 50 Punkte haben, sonst gibt es Probleme
															// mit dem Reduktionsalgorythmus
						{
							Projektion = Coordinate.Project(startCoord.getLatitude(), startCoord.getLongitude(), (double) i, distance);

							route.Points.add(new TrackPoint(Projektion.getLongitude(), Projektion.getLatitude(), 0, 0, new Date()));

							if (!LastCoord.isValid())
							{
								LastCoord = Projektion;
								LastCoord.setValid(true);
							}
							else
							{
								Coordinate.distanceBetween(Projektion.getLatitude(), Projektion.getLongitude(), LastCoord.getLatitude(),
										LastCoord.getLongitude(), dist);
								route.TrackLength += dist[0];
								LastCoord = Projektion;
								LastCoord.setValid(true);
							}

						}
						if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
					}

				}, Type.circle);

		pC.show();
	}

}