package CB_Core.GL_UI.Main.Actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

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
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.Dialogs.RouteDialog;
import CB_Core.GL_UI.Controls.Dialogs.RouteDialog.returnListner;
import CB_Core.GL_UI.Controls.Dialogs.StringInputBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
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
	Color[] ColorField = new Color[13];
	{
		ColorField[0] = Color.RED;
		ColorField[1] = Color.YELLOW;
		ColorField[2] = Color.BLACK;
		ColorField[3] = Color.LIGHT_GRAY;
		ColorField[4] = Color.GREEN;
		ColorField[5] = Color.BLUE;
		ColorField[6] = Color.CYAN;
		ColorField[7] = Color.GRAY;
		ColorField[8] = Color.MAGENTA;
		ColorField[9] = Color.ORANGE;
		ColorField[10] = Color.DARK_GRAY;
		ColorField[11] = Color.PINK;
		ColorField[12] = Color.WHITE;
	}
	Color TrackColor;

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
	private static final int OPENROUTE = 9;

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
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

				case LOAD:
					platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", GlobalCore.Translations.Get("LoadTrack"),
							GlobalCore.Translations.Get("load"), new IgetFileReturnListner()
							{
								@Override
								public void getFieleReturn(String Path)
								{
									if (Path != null)
									{
										TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];

										RouteOverlay.MultiLoadRoute(Path, TrackColor);
										Logger.LogCat("Load Track :" + Path);
										if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
									}
								}
							});

					return true;

				case SAVE:
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

				case DELETE:
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

						RouteOverlay.Routes.remove(selectedTrackItem.getRoute());
						selectedTrackItem = null;
						TrackListView.that.notifyDataSetChanged();
						return true;
					}
				}
				return false;
			}
		});

		TrackListViewItem selectedTrackItem = TrackListView.that.getSelectedItem();
		cm.addItem(LOAD, "load");
		cm.addItem(GENERATE, "generate");
		// rename, save, delete darf nicht mit dem aktuellen Track gemacht werden....
		if (selectedTrackItem != null && !selectedTrackItem.getRoute().IsActualTrack)
		{
			cm.addItem(RENAME, "rename");
			cm.addItem(SAVE, "save");
			cm.addItem(DELETE, "delete");
		}

		return cm;
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
				case OPENROUTE:

					Thread tread = new Thread(new Runnable()
					{

						@Override
						public void run()
						{
							GenOpenRoute();
						}
					});
					tread.start();

					return true;
				}
				return false;
			}
		});
		cm2.addItem(P2P, "Point2Point");
		cm2.addItem(PROJECT, "Projection");
		cm2.addItem(CIRCLE, "Circle");
		cm2.addItem(OPENROUTE, "OpenRoute");

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
						TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
						Track route = new Track(null, TrackColor);

						route.Name = "Point 2 Point Route";
						route.Points.add(new TrackPoint(targetCoord.Longitude, targetCoord.Latitude, 0, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.Longitude, startCoord.Latitude, 0, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.Latitude, targetCoord.Longitude, startCoord.Latitude, startCoord.Longitude,
								dist);
						route.TrackLength = dist[0];

						route.ShowRoute = true;
						RouteOverlay.Routes.add(route);
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
						TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
						Track route = new Track(null, TrackColor);
						route.Name = "Projected Route";

						route.Points.add(new TrackPoint(targetCoord.Longitude, targetCoord.Latitude, 0, 0, new Date()));
						route.Points.add(new TrackPoint(startCoord.Longitude, startCoord.Latitude, 0, 0, new Date()));

						Coordinate.distanceBetween(targetCoord.Latitude, targetCoord.Longitude, startCoord.Latitude, startCoord.Longitude,
								dist);
						route.TrackLength = dist[0];

						route.ShowRoute = true;
						RouteOverlay.Routes.add(route);
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
						TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
						Track route = new Track(null, TrackColor);
						route.Name = "Circle Route";

						route.ShowRoute = true;
						RouteOverlay.Routes.add(route);

						Coordinate Projektion = new Coordinate();
						Coordinate LastCoord = new Coordinate();

						for (int i = 0; i <= 360; i += 10) // Achtung der Kreis darf nicht mehr als 50 Punkte haben, sonst gibt es Probleme
															// mit dem Reduktionsalgorythmus
						{
							Projektion = Coordinate.Project(startCoord.Latitude, startCoord.Longitude, (double) i, distance);

							route.Points.add(new TrackPoint(Projektion.Longitude, Projektion.Latitude, 0, 0, new Date()));

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
						if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
					}

				}, Type.circle);

		pC.show();
	}

	private static Coordinate start = new Coordinate();
	private static Coordinate target = new Coordinate();

	private void GenOpenRoute()
	{

		if ((GlobalCore.Locator == null && !GlobalCore.LastValidPosition.Valid) || !GlobalCore.Locator.isGPSprovided())
		{
			GL_MsgBox.Show("GPS ungültig", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		}
		else
		{
			start = GlobalCore.LastValidPosition;
		}

		if (GlobalCore.SelectedWaypoint() != null)
		{
			target = GlobalCore.SelectedWaypoint().Pos;
		}
		else if (GlobalCore.SelectedCache() != null)
		{
			target = GlobalCore.SelectedCache().Pos;
		}
		else
		{
			GL_MsgBox.Show("Cache / WP ungültig", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		}

		RouteDialog routeDia = new RouteDialog(new returnListner()
		{

			@Override
			public void returnFromRoute_Dialog(final boolean canceld, final boolean Motoway, final boolean CycleWay, final boolean FootWay,
					boolean UseTmc)
			{

				CancelWaitDialog wd = CancelWaitDialog.ShowWait("generateRoute", new IcancelListner()
				{

					@Override
					public void isCanceld()
					{
						// TODO Handle Cancel Clicket

					}
				}, new Runnable()
				{

					@Override
					public void run()
					{
						Coordinate lastAcceptedCoordinate = null;
						float[] dist = new float[4];
						double Distance = 0;
						Coordinate FromPosition = new Coordinate();
						String routepref = "Fastest";

						if (canceld) return;
						if (Motoway) routepref = "Fastest";
						if (CycleWay) routepref = "Bicycle";
						if (FootWay) routepref = "Pedestrain";

						String Url = Config.settings.NavigationProvider.getValue();
						// String Url = "http://openrouteservice.org/php/OpenLSRS_DetermineRoute.php";

						HttpClient httpclient = new DefaultHttpClient();
						HttpPost httppost = new HttpPost(Url);

						httppost.setHeader("User-Agent", "cachebox rev " + String.valueOf(GlobalCore.CurrentRevision));
						httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

						// Create a local instance of cookie store
						CookieStore cookieStore = new BasicCookieStore();

						((AbstractHttpClient) httpclient).setCookieStore(cookieStore);

						// Create local HTTP context
						HttpContext localContext = new BasicHttpContext();
						// Bind custom cookie store to the local context
						localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

						// Execute HTTP Post Request
						try
						{
							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(12);
							nameValuePairs.add(new BasicNameValuePair("Start", String.valueOf(start.Longitude) + ","
									+ String.valueOf(start.Latitude)));
							nameValuePairs.add(new BasicNameValuePair("End", String.valueOf(target.Longitude) + ","
									+ String.valueOf(target.Latitude)));

							nameValuePairs.add(new BasicNameValuePair("Via", ""));
							nameValuePairs.add(new BasicNameValuePair("lang", "de"));
							nameValuePairs.add(new BasicNameValuePair("distunit", "KM"));
							nameValuePairs.add(new BasicNameValuePair("routepref", routepref));
							nameValuePairs.add(new BasicNameValuePair("avoidAreas", ""));
							nameValuePairs.add(new BasicNameValuePair("useTMC", "false"));
							nameValuePairs.add(new BasicNameValuePair("noMotorways", "false"));
							nameValuePairs.add(new BasicNameValuePair("noTollways", "false"));
							nameValuePairs.add(new BasicNameValuePair("instructions", "false"));
							nameValuePairs.add(new BasicNameValuePair("_", ""));
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

							// Execute HTTP Post Request
							HttpResponse response = httpclient.execute(httppost, localContext);

							BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							StringBuilder builder = new StringBuilder();
							String line = "";
							TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
							Track route = new Track(null, TrackColor);
							route.Name = "OpenRouteService";
							route.ShowRoute = true;
							boolean RouteGeometryBlockFound = false;
							boolean IsRoute = false;

							try
							{
								while ((line = reader.readLine()) != null)
								{
									builder.append(line).append("\n");
									if (line.indexOf("<xls:Error ") >= 0)
									{
										int errorIdx = line.indexOf("message=\"");
										int endIdx = line.indexOf("\"", errorIdx + 9);
										String errorMessage = line.substring(errorIdx + 9, endIdx);
										GL_MsgBox.Show(errorMessage, "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
										return;
									}

									if (line.indexOf("<xls:RouteGeometry>") >= 0) // suche <xls:RouteGeometry> Block
									{
										RouteGeometryBlockFound = true;
									}

									int idx;
									if (((idx = line.indexOf("<gml:pos>")) > 0) & RouteGeometryBlockFound)
									{
										int seperator = line.indexOf(" ", idx + 1);
										int endIdx = line.indexOf("</gml:pos>", seperator + 1);

										String lonStr = line.substring(idx + 9, seperator);
										String latStr = line.substring(seperator + 1, endIdx);

										double lat = Double.valueOf(latStr);
										double lon = Double.valueOf(lonStr);

										lastAcceptedCoordinate = new Coordinate(lat, lon);

										route.Points.add(new TrackPoint(lastAcceptedCoordinate.Longitude, lastAcceptedCoordinate.Latitude,
												0, 0, null));

										// Calculate the length of a Track
										if (!FromPosition.Valid)
										{
											FromPosition.Longitude = lastAcceptedCoordinate.Longitude;
											FromPosition.Latitude = lastAcceptedCoordinate.Latitude;
											FromPosition.Valid = true;
										}
										else
										{
											Coordinate.distanceBetween(FromPosition.Latitude, FromPosition.Longitude,
													lastAcceptedCoordinate.Latitude, lastAcceptedCoordinate.Longitude, dist);
											Distance += dist[0];
											FromPosition.Longitude = lastAcceptedCoordinate.Longitude;
											FromPosition.Latitude = lastAcceptedCoordinate.Latitude;
											IsRoute = true; // min. 2 Punkte, damit es eine gültige Route ist
										}
									}
								}

								if (IsRoute)
								{
									route.TrackLength = Distance;
									RouteOverlay.Routes.add(route);
									if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();
								}
								else
								{
									GL_MsgBox.Show("OpenRouteService", "no route found", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
									return;
								}

							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// String page = builder.toString(); //page enthält komplette zurückgelieferte Web-Seite
						}
						catch (ClientProtocolException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						RouteOverlay.RoutesChanged();

					}
				});

			}

		});

		GL.that.showDialog(routeDia, true);

	}

}