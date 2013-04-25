package CB_Core.GL_UI.Main.Actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import CB_Core.UnitFormatter;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.Dialogs.RouteDialog;
import CB_Core.GL_UI.Controls.Dialogs.RouteDialog.returnListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.RouteOverlay.Track;
import CB_Core.TranslationEngine.Translation;
import CB_Locator.Coordinate;
import CB_Locator.Locator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_GenerateRoute extends CB_ActionCommand
{

	Color TrackColor;

	public CB_Action_GenerateRoute()
	{
		super("GenerateRoute", MenuID.AID_GENERATE_ROUTE);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.trackList_8.ordinal());
	}

	@Override
	public void Execute()
	{
		Thread tread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				GenOpenRoute();
			}
		});
		tread.start();
	}

	private Coordinate start = new Coordinate();
	private Coordinate target = new Coordinate();
	private RouteDialog routeDia;
	private CancelWaitDialog wd;

	private void GenOpenRoute()
	{

		if (!Locator.isGPSprovided())
		{
			GL_MsgBox.Show("GPS ungültig", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		}
		else
		{
			start = Locator.getCoordinate();
		}

		if (GlobalCore.getSelectedWaypoint() != null)
		{
			target = GlobalCore.getSelectedWaypoint().Pos;
		}
		else if (GlobalCore.getSelectedCache() != null)
		{
			target = GlobalCore.getSelectedCache().Pos;
		}
		else
		{
			GL_MsgBox.Show("Cache / WP ungültig", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		}

		routeDia = new RouteDialog(new returnListner()
		{

			@Override
			public void returnFromRoute_Dialog(final boolean canceld, final boolean Motoway, final boolean CycleWay, final boolean FootWay,
					boolean UseTmc)
			{
				routeDia.close();

				if (!canceld) GL.that.RunOnGL(new runOnGL()
				{

					@Override
					public void run()
					{
						wd = CancelWaitDialog.ShowWait("generateRoute", new IcancelListner()
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
								if (FootWay) routepref = "Pedestrian";

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
								// see http://wiki.openstreetmap.org/wiki/DE:OpenRouteService#ORS_.22API.22
								// for more information
								try
								{
									List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(12);
									nameValuePairs.add(new BasicNameValuePair("Start", String.valueOf(start.getLongitude()) + ","
											+ String.valueOf(start.getLatitude())));
									nameValuePairs.add(new BasicNameValuePair("End", String.valueOf(target.getLongitude()) + ","
											+ String.valueOf(target.getLatitude())));

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
									TrackColor = RouteOverlay.getNextColor();
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
												final String errorMessage = line.substring(errorIdx + 9, endIdx);
												wd.close();
												GL.that.RunOnGL(new runOnGL()
												{
													// wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
													// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
													@Override
													public void run()
													{
														GL_MsgBox.Show(errorMessage, "OpenRouteService", MessageBoxButtons.OK,
																MessageBoxIcon.Error, null);
													}
												});

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

												route.Points.add(new TrackPoint(lastAcceptedCoordinate.getLongitude(),
														lastAcceptedCoordinate.getLatitude(), 0, 0, null));

												// Calculate the length of a Track
												if (!FromPosition.isValid())
												{
													FromPosition.setLongitude(lastAcceptedCoordinate.getLongitude());
													FromPosition.setLatitude(lastAcceptedCoordinate.getLatitude());
													FromPosition.setValid(true);
												}
												else
												{
													Coordinate.distanceBetween(FromPosition.getLatitude(), FromPosition.getLongitude(),
															lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(),
															dist);
													Distance += dist[0];
													FromPosition.setLongitude(lastAcceptedCoordinate.getLongitude());
													FromPosition.setLatitude(lastAcceptedCoordinate.getLatitude());
													IsRoute = true; // min. 2 Punkte, damit es eine gültige Route ist
												}
											}
										}

										if (IsRoute)
										{
											final String sDistance = UnitFormatter.DistanceString((float) Distance);
											route.TrackLength = Distance;
											RouteOverlay.addOpenRoute(route);
											if (TrackListView.that != null) TrackListView.that.notifyDataSetChanged();

											wd.close();

											GL.that.RunOnGL(new runOnGL()
											{
												// wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
												// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
												@Override
												public void run()
												{
													String msg = Translation.Get("generateRouteLength") + sDistance;
													GL_MsgBox.Show(msg, "OpenRouteService", MessageBoxButtons.OK,
															MessageBoxIcon.Information, null);
												}
											});
										}
										else
										{
											wd.close();

											GL.that.RunOnGL(new runOnGL()
											{
												// wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
												// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
												@Override
												public void run()
												{
													GL_MsgBox.Show("OpenRouteService", "no route found", MessageBoxButtons.OK,
															MessageBoxIcon.Error, null);
												}
											});

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

			}

		});

		GL.that.showDialog(routeDia, true);

	}

}
