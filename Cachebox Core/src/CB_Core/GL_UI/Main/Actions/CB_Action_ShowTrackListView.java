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
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Dialogs.StringInputBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.GL_UI.Views.TrackListViewItem;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.RouteOverlay.Track;
import CB_Core.Types.Coordinate;

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
		return SpriteCache.Icons.get(8);
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

						StringInputBox.Show(TextFieldType.SingleLine, selectedTrackItem.getRoute().Name,
								GlobalCore.Translations.Get("RenameTrack"), selectedTrackItem.getRoute().Name, new OnMsgBoxClickListener()
								{

									@Override
									public boolean onClick(int which)
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
					platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", GlobalCore.Translations.Get("LoadTrack"),
							GlobalCore.Translations.Get("load"), new IgetFileReturnListner()
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
					platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", GlobalCore.Translations.Get("SaveTrack"),
							GlobalCore.Translations.Get("save"), new IgetFileReturnListner()
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
							GL_MsgBox.Show(GlobalCore.Translations.Get("NoTrackSelected"), null, MessageBoxButtons.OK,
									MessageBoxIcon.Warning, new OnMsgBoxClickListener()
									{

										@Override
										public boolean onClick(int which)
										{
											// hier brauchen wir nichts machen!
											return true;
										}
									});
							return true;
						}

						if (selectedTrackItem.getRoute().IsActualTrack)
						{
							GL_MsgBox.Show(GlobalCore.Translations.Get("IsActualTrack"), null, MessageBoxButtons.OK,
									MessageBoxIcon.Warning, null);
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
				case MenuID.MI_OPENROUTE:

					Thread tread = new Thread(new Runnable()
					{

						@Override
						public void run()
						{
							TabMainView.actionGenerateRoute.Execute();
						}
					});
					tread.start();

					return true;
				}
				return false;
			}
		});
		cm2.addItem(MenuID.MI_P2P, "Point2Point");
		cm2.addItem(MenuID.MI_PROJECT, "Projection");
		cm2.addItem(MenuID.MI_CIRCLE, "Circle");
		cm2.addItem(MenuID.MI_OPENROUTE, "OpenRoute");

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

						if (targetCoord == null || startCoord == null) return;

						float[] dist = new float[4];
						TrackColor = RouteOverlay.getNextColor();
						Track route = new Track(null, TrackColor);

						route.Name = "Point 2 Point Route";
						route.Points.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(),
								dist);
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
		final Coordinate coord = GlobalCore.LastValidPosition;

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), GlobalCore.Translations.Get("Projection"), coord,
				new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
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

						Coordinate.distanceBetween(targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(),
								dist);
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
		final Coordinate coord = GlobalCore.LastValidPosition;

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), GlobalCore.Translations.Get("centerPoint"), coord,
				new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
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

							if (!LastCoord.Valid)
							{
								LastCoord = Projektion;
								LastCoord.Valid = true;
							}
							else
							{
								Coordinate.distanceBetween(Projektion.getLatitude(), Projektion.getLongitude(), LastCoord.getLatitude(),
										LastCoord.getLongitude(), dist);
								route.TrackLength += dist[0];
								LastCoord = Projektion;
								LastCoord.Valid = true;
							}

						}
						if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
					}

				}, Type.circle);

		pC.show();
	}

}