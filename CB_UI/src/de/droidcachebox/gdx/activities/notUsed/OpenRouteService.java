package de.droidcachebox.gdx.activities.notUsed;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.IRunOnGL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.RouteDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.menuBtn3.executes.TrackList;
import de.droidcachebox.menu.menuBtn3.executes.TrackListView;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.RunAndReady;
import de.droidcachebox.utils.UnitFormatter;

public class OpenRouteService {

    private Coordinate start = new CoordinateGPS(0, 0);
    private Coordinate target = new CoordinateGPS(0, 0);
    private RouteDialog routeDia;
    private CancelWaitDialog wd;

    private void generateOpenRoute() {

        if (!Locator.getInstance().isGPSprovided()) {
            MsgBox.show("GPS ungültig", "Error", MsgBoxButton.OK, MsgBoxIcon.Error, null);
            return;
        } else {
            start = Locator.getInstance().getMyPosition();
        }

        if (GlobalCore.getSelectedWayPoint() != null) {
            target = GlobalCore.getSelectedWayPoint().getCoordinate();
        } else if (GlobalCore.isSetSelectedCache()) {
            target = GlobalCore.getSelectedCache().getCoordinate();
        } else {
            MsgBox.show("Cache / WP ungültig", "Error", MsgBoxButton.OK, MsgBoxIcon.Error, null);
            return;
        }

        routeDia = new RouteDialog((canceld, Motoway, CycleWay, FootWay, UseTmc) -> {
            routeDia.close();

            if (!canceld)
                GL.that.RunOnGL(() -> {
                    AtomicBoolean isCanceled = new AtomicBoolean(false);
                    wd = new CancelWaitDialog(Translation.get("generateRoute"), new DownloadAnimation(), new RunAndReady() {
                        @Override
                        public void ready(boolean isCanceled) {

                        }

                        @Override
                        public void run() {
                            Coordinate lastAcceptedCoordinate;
                            float[] dist = new float[4];
                            double distance = 0;
                            Coordinate FromPosition = new CoordinateGPS(0, 0);

                            if (canceld)
                                return;

                            try {

                                // todo
                            /*
                            HttpResponse response = requestRouteGet(canceld, Motoway, CycleWay, FootWay);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                             */

                                StringBuilder builder = new StringBuilder();
                                String line = "";
                                Track route = new Track("OpenRouteService");
                                route.setVisible(true);
                                boolean RouteGeometryBlockFound = false;
                                boolean IsRoute = false;

                                try {
                                    // todo
                                    // replace corresponding
                                    // while (builder.length() > 0) {
                                    // while ((line = reader.readLine()) != null) {
                                    while (builder.length() > 0) {
                                        builder.append(line).append("\n");
                                        if (line.indexOf("<xls:Error ") >= 0) {
                                            int errorIdx = line.indexOf("message=\"");
                                            int endIdx = line.indexOf("\"", errorIdx + 9);
                                            final String errorMessage = line.substring(errorIdx + 9, endIdx);
                                            wd.close();
                                            GL.that.RunOnGL(new IRunOnGL() {
                                                // wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
                                                // Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
                                                @Override
                                                public void run() {
                                                    MsgBox.show(errorMessage, "OpenRouteService", MsgBoxButton.OK, MsgBoxIcon.Error, null);
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

                                            route.getTrackPoints().add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), 0, 0, null));

                                            // Calculate the length of a Track
                                            if (!FromPosition.isValid()) {
                                                FromPosition = new Coordinate(lastAcceptedCoordinate);
                                                FromPosition.setValid(true);
                                            } else {
                                                MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, FromPosition.getLatitude(), FromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(),
                                                        lastAcceptedCoordinate.getLongitude(), dist);
                                                distance += dist[0];
                                                FromPosition = new Coordinate(lastAcceptedCoordinate);
                                                IsRoute = true; // min. 2 Punkte, damit es eine gültige Route ist
                                            }
                                        }
                                    }

                                    if (IsRoute) {
                                        final String sDistance = UnitFormatter.distanceString((float) distance);
                                        route.setTrackLength(distance);
                                        TrackList.getInstance().setRoutingTrack(route);
                                        TrackListView.getInstance().notifyDataSetChanged();

                                        wd.close();

                                        GL.that.RunOnGL(new IRunOnGL() {
                                            // wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
                                            // Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
                                            @Override
                                            public void run() {
                                                String msg = Translation.get("generateRouteLength") + sDistance;
                                                MsgBox.show(msg, "OpenRouteService", MsgBoxButton.OK, MsgBoxIcon.Information, null);
                                            }
                                        });
                                    } else {
                                        wd.close();

                                        GL.that.RunOnGL(new IRunOnGL() {
                                            // wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
                                            // Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
                                            @Override
                                            public void run() {
                                                MsgBox.show("no route found", "OpenRouteService", MsgBoxButton.OK, MsgBoxIcon.Error, null);
                                            }
                                        });

                                        return;
                                    }

                                } catch (Exception e) {
                                    wd.close();

                                    GL.that.RunOnGL(new IRunOnGL() {
                                        // wird in RunOnGL ausgeführt, da erst der WaitDialog geschlossen werden muss.
                                        // Die Anzeige der MsgBox erfollgt dann einen Rederdurchgang später.
                                        @Override
                                        public void run() {
                                            MsgBox.show("no route found", "OpenRouteService", MsgBoxButton.OK, MsgBoxIcon.Error, null);
                                        }
                                    });
                                }

                                // String page = builder.toString(); //page enthüllt komplette zurückgelieferte Web-Seite
                            } catch (Exception e) {
                                wd.close();
                                GL.that.RunOnGL(() -> MsgBox.show("no route found", "OpenRouteService", MsgBoxButton.OK, MsgBoxIcon.Error, null));
                            }
                            TrackList.getInstance().trackListChanged();
                        }

                        @Override
                        public void setIsCanceled() {
                            isCanceled.set(true);
                        }

                    });
                    wd.show();
                });

        });

        // routeDia.show();
        GL.that.showDialog(routeDia, true);

    }

    /*
     * new API
     * @param canceld
     * @param Motoway
     * @param CycleWay
     * @param FootWay
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ClientProtocolException
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
     */

}
