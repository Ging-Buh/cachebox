package CB_UI.GL_UI.Main.Actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Locator.Map.Track;
import CB_Locator.Map.TrackPoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.Dialogs.RouteDialog;
import CB_UI.GL_UI.Controls.Dialogs.RouteDialog.returnListner;
import CB_UI.GL_UI.Views.TrackListView;
import CB_UI.Map.RouteOverlay;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Util.UnitFormatter;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_GenerateRoute extends CB_ActionCommand {

	Color TrackColor;

	public CB_Action_GenerateRoute() {
		super("GenerateRoute", MenuID.AID_GENERATE_ROUTE);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return SpriteCacheBase.Icons.get(IconName.trackList_8.ordinal());
	}

	@Override
	public void Execute() {
		Thread tread = new Thread(new Runnable() {
			@Override
			public void run() {
				GenOpenRoute();
			}
		});
		tread.start();
	}

	private Coordinate start = new CoordinateGPS(0, 0);
	private Coordinate target = new CoordinateGPS(0, 0);
	private RouteDialog routeDia;
	private CancelWaitDialog wd;

	private void GenOpenRoute() {

		if (!Locator.isGPSprovided()) {
			GL_MsgBox.Show("GPS ung?ltig", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		} else {
			start = Locator.getCoordinate();
		}

		if (GlobalCore.getSelectedWaypoint() != null) {
			target = GlobalCore.getSelectedWaypoint().Pos;
		} else if (GlobalCore.ifCacheSelected()) {
			target = GlobalCore.getSelectedCache().Pos;
		} else {
			GL_MsgBox.Show("Cache / WP ung?ltig", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		}

		routeDia = new RouteDialog(new returnListner() {

			@Override
			public void returnFromRoute_Dialog(final boolean canceld, final boolean Motoway, final boolean CycleWay, final boolean FootWay, boolean UseTmc) {
				routeDia.close();

				if (!canceld)
					GL.that.RunOnGL(new IRunOnGL() {

						@Override
						public void run() {
							wd = CancelWaitDialog.ShowWait(Translation.Get("generateRoute"), DownloadAnimation.GetINSTANCE(), new IcancelListner() {

								@Override
								public void isCanceld() {
									// TODO Handle Cancel Clicket

								}
							}, new cancelRunnable() {

								@Override
								public void run() {
									Coordinate lastAcceptedCoordinate = null;
									float[] dist = new float[4];
									double Distance = 0;
									Coordinate FromPosition = new CoordinateGPS(0, 0);

									if (canceld)
										return;

									try {

										HttpResponse response = requestRouteGet(canceld, Motoway, CycleWay, FootWay);

										BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
										StringBuilder builder = new StringBuilder();
										String line = "";
										TrackColor = RouteOverlay.getNextColor();
										Track route = new Track(null, TrackColor);
										route.Name = "OpenRouteService";
										route.ShowRoute = true;
										boolean RouteGeometryBlockFound = false;
										boolean IsRoute = false;

										try {
											while ((line = reader.readLine()) != null) {
												builder.append(line).append("\n");
												if (line.indexOf("<xls:Error ") >= 0) {
													int errorIdx = line.indexOf("message=\"");
													int endIdx = line.indexOf("\"", errorIdx + 9);
													final String errorMessage = line.substring(errorIdx + 9, endIdx);
													wd.close();
													GL.that.RunOnGL(new IRunOnGL() {
														// wird in RunOnGL ausgef?hrt, da erst der WaitDialog geschlossen werden muss.
														// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang sp?ter.
														@Override
														public void run() {
															GL_MsgBox.Show(errorMessage, "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
														}
													});

													return;
												}

												if (line.indexOf("<xls:RouteGeometry>") >= 0) // suche <xls:RouteGeometry> Block
												{
													RouteGeometryBlockFound = true;
												}

												int idx;
												if (((idx = line.indexOf("<gml:pos>")) > 0) & RouteGeometryBlockFound) {
													int seperator = line.indexOf(" ", idx + 1);
													int endIdx = line.indexOf("</gml:pos>", seperator + 1);

													String lonStr = line.substring(idx + 9, seperator);
													String latStr = line.substring(seperator + 1, endIdx);

													double lat = Double.valueOf(latStr);
													double lon = Double.valueOf(lonStr);

													lastAcceptedCoordinate = new CoordinateGPS(lat, lon);

													route.Points.add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), 0, 0, null));

													// Calculate the length of a Track
													if (!FromPosition.isValid()) {
														FromPosition = new Coordinate(lastAcceptedCoordinate);
														FromPosition.setValid(true);
													} else {
														MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, FromPosition.getLatitude(), FromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(), dist);
														Distance += dist[0];
														FromPosition = new Coordinate(lastAcceptedCoordinate);
														IsRoute = true; // min. 2 Punkte, damit es eine g?ltige Route ist
													}
												}
											}

											if (IsRoute) {
												final String sDistance = UnitFormatter.DistanceString((float) Distance);
												route.TrackLength = Distance;
												RouteOverlay.addOpenRoute(route);
												if (TrackListView.that != null)
													TrackListView.that.notifyDataSetChanged();

												wd.close();

												GL.that.RunOnGL(new IRunOnGL() {
													// wird in RunOnGL ausgef?hrt, da erst der WaitDialog geschlossen werden muss.
													// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang sp?ter.
													@Override
													public void run() {
														String msg = Translation.Get("generateRouteLength") + sDistance;
														GL_MsgBox.Show(msg, "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Information, null);
													}
												});
											} else {
												wd.close();

												GL.that.RunOnGL(new IRunOnGL() {
													// wird in RunOnGL ausgef?hrt, da erst der WaitDialog geschlossen werden muss.
													// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang sp?ter.
													@Override
													public void run() {
														GL_MsgBox.Show("no route found", "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
													}
												});

												return;
											}

										} catch (Exception e) {
											wd.close();

											GL.that.RunOnGL(new IRunOnGL() {
												// wird in RunOnGL ausgef?hrt, da erst der WaitDialog geschlossen werden muss.
												// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang sp?ter.
												@Override
												public void run() {
													GL_MsgBox.Show("no route found", "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
												}
											});
										}

										// String page = builder.toString(); //page enth?lt komplette zur?ckgelieferte Web-Seite
									} catch (ClientProtocolException e) {
										wd.close();

										GL.that.RunOnGL(new IRunOnGL() {
											// wird in RunOnGL ausgef?hrt, da erst der WaitDialog geschlossen werden muss.
											// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang sp?ter.
											@Override
											public void run() {
												GL_MsgBox.Show("no route found", "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
											}
										});
									} catch (IOException e) {
										wd.close();

										GL.that.RunOnGL(new IRunOnGL() {
											// wird in RunOnGL ausgef?hrt, da erst der WaitDialog geschlossen werden muss.
											// Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang sp?ter.
											@Override
											public void run() {
												GL_MsgBox.Show("no route found", "OpenRouteService", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
											}
										});
									}
									RouteOverlay.RoutesChanged();

								}

								@Override
								public boolean cancel() {
									// TODO Auto-generated method stub
									return false;
								}
							});
						}
					});

			}

		});

		GL.that.showDialog(routeDia, true);

	}

	/**
	 * Outdate
	 * @param canceld
	 * @param Motoway
	 * @param CycleWay
	 * @param FootWay
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private HttpResponse requestRoute(final boolean canceld, final boolean Motoway, final boolean CycleWay, final boolean FootWay) throws UnsupportedEncodingException, IOException, ClientProtocolException {
		// Execute HTTP Post Request
		// see http://wiki.openstreetmap.org/wiki/DE:OpenRouteService#ORS_.22API.22
		// for more information

		String routepref = "Fastest";

		if (Motoway)
			routepref = "Fastest";
		if (CycleWay)
			routepref = "Bicycle";
		if (FootWay)
			routepref = "Pedestrian";

		String Url = Config.NavigationProvider.getValue();
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

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(12);
		nameValuePairs.add(new BasicNameValuePair("Start", String.valueOf(start.getLongitude()) + "," + String.valueOf(start.getLatitude())));
		nameValuePairs.add(new BasicNameValuePair("End", String.valueOf(target.getLongitude()) + "," + String.valueOf(target.getLatitude())));

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
		return response;
	}

	/**
	 * new API
	 * @param canceld
	 * @param Motoway
	 * @param CycleWay
	 * @param FootWay
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private HttpResponse requestRouteGet(final boolean canceld, final boolean Motoway, final boolean CycleWay, final boolean FootWay) throws UnsupportedEncodingException, IOException, ClientProtocolException {
		// Execute HTTP Post Request
		// see http://wiki.openstreetmap.org/wiki/DE:OpenRouteService#ORS_.22API.22
		// for more information

		String routepref = "Fastest";

		if (Motoway)
			routepref = "Fastest";
		if (CycleWay)
			routepref = "Bicycle";
		if (FootWay)
			routepref = "Pedestrian";

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(12);
		nameValuePairs.add(new BasicNameValuePair("Start", String.valueOf(start.getLongitude()) + "," + String.valueOf(start.getLatitude())));
		nameValuePairs.add(new BasicNameValuePair("End", String.valueOf(target.getLongitude()) + "," + String.valueOf(target.getLatitude())));

		nameValuePairs.add(new BasicNameValuePair("Via", ""));
		nameValuePairs.add(new BasicNameValuePair("lang", "de"));
		nameValuePairs.add(new BasicNameValuePair("distunit", "KM"));
		nameValuePairs.add(new BasicNameValuePair("routepref", routepref));
		nameValuePairs.add(new BasicNameValuePair("avoidAreas", ""));
		nameValuePairs.add(new BasicNameValuePair("useTMC", "false"));
		nameValuePairs.add(new BasicNameValuePair("noMotorways", "false"));
		nameValuePairs.add(new BasicNameValuePair("noTollways", "false"));
		nameValuePairs.add(new BasicNameValuePair("instructions", "false"));

		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (NameValuePair pair : nameValuePairs) {
			sb.append(pair.getName());
			sb.append("=");
			sb.append(pair.getValue());

			if (count++ < nameValuePairs.size())
				sb.append("&");

		}

		String Url = Config.NavigationProvider.getValue() + sb.toString();
		// String Url = "http://openrouteservice.org/php/OpenLSRS_DetermineRoute.php";

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(Url);

		httpGet.setHeader("User-Agent", "cachebox rev " + String.valueOf(GlobalCore.CurrentRevision));
		httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");

		// Create a local instance of cookie store
		CookieStore cookieStore = new BasicCookieStore();

		((AbstractHttpClient) httpclient).setCookieStore(cookieStore);

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();
		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httpGet, localContext);
		return response;
	}

}