/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox;

import com.badlogic.gdx.graphics.Color;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.views.TrackListView;
import de.droidcachebox.locator.Location;
import de.droidcachebox.locator.Location.ProviderType;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.Locator.CompassType;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.Log;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TrackRecorder {
    private static final String log = "TrackRecorder";
    private final static ProviderType GPS = ProviderType.GPS;
    private final static CompassType _GPS = CompassType.GPS;
    public static boolean pauseRecording = false;
    public static boolean recording = false;
    public static int distanceForNextTrackpoint;
    private static double SaveAltitude = 0;
    private static Location LastRecordedPosition = Location.NULL_LOCATION;
    private static String mFriendlyName = "";
    private static String mMediaPath = "";
    private static Location mMediaCoord = null;
    private static String mTimestamp = "";
    private static File gpxfile = null;
    private static FileWriter writer = null;
    private static boolean writeAnnotateMedia = false;
    private static int insertPos = 24;
    private static boolean mustWriteMedia = false;
    private static boolean mustRecPos = false;
    private static boolean writePos = false;

    public static void startRecording() {
        distanceForNextTrackpoint = Config.TrackDistance.getValue();

        GlobalCore.aktuelleRoute = new Track(Translation.get("actualTrack"));
        GlobalCore.aktuelleRoute.setColor(Color.BLUE);
        GlobalCore.aktuelleRoute.setVisible(true);
        GlobalCore.aktuelleRoute.setActualTrack(true);
        GlobalCore.aktuelleRouteCount = 0;
        GlobalCore.aktuelleRoute.setTrackLength(0);
        GlobalCore.aktuelleRoute.setAltitudeDifference(0);

        String directory = CB_UI_Settings.TrackFolder.getValue();
        if (!FileIO.createDirectory(directory))
            return;

        if (gpxfile == null) {
            gpxfile = FileFactory.createFile(directory + "/" + generateTrackFileName());
            try {
                writer = gpxfile.getFileWriter();
                try {
                    writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                            .append("<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n")
                            .append("<time>").append(GetDateTimeString()).append("</time>\n")
                            // set real bounds or basecamp (mapsource) will not import this track
                            // writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");
                            .append("<trk><trkseg>\n");
                    writer.flush();
                } catch (IOException e) {
                    Log.err(log, "IOException", e);
                }
            } catch (IOException e1) {
                Log.err(log, "IOException", e1);
            }

            try {
                writer.append("</trkseg>\n");
                writer.append("</trk>\n");

                writer.append("</gpx>\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Log.err(log, "IOException", e);
            }
            writer = null;

        }

        pauseRecording = false;
        recording = true;

        // updateRecorderButtonAccessibility();
    }

    private static String GetDateTimeString() {
        Date timestamp = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp);
        datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sDate += "T" + datFormat.format(timestamp) + "Z";
        return sDate;
    }

    public static void AnnotateMedia(final String friendlyName, final String mediaPath, final Location location, final String timestamp) {
        if (location == null)
            return;
        writeAnnotateMedia = true;

        if (writePos) {
            mFriendlyName = friendlyName;
            mMediaPath = mediaPath;
            mMediaCoord = location;
            mTimestamp = timestamp;
            mustWriteMedia = true;
        }

        if (gpxfile == null)
            return;

        String xml = "<wpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">\n"
                + "   <ele>" + location.getAltitude() + "</ele>\n"
                + "   <time>" + timestamp + "</time>\n"
                + "   <name>" + friendlyName + "</name>\n"
                + "   <link href=\"" + mediaPath + "\" />\n"
                + "</wpt>\n";

        RandomAccessFile rand;
        try {
            rand = gpxfile.getRandomAccessFile("rw");

            int i = (int) rand.length();

            byte[] bEnde = new byte[8];

            rand.seek(i - 8); // Seek to start point of file

            for (int ct = 0; ct < 8; ct++) {
                bEnde[ct] = rand.readByte(); // read byte from the file
            }

            // insert point

            byte[] b = xml.getBytes();

            rand.setLength(i + b.length);
            rand.seek(i - 8);
            rand.write(b);
            rand.write(bEnde);
            rand.close();

            insertPos += b.length;

        } catch (FileNotFoundException e) {
            Log.err(log, "FileNotFoundException", e);
        } catch (IOException e) {
            Log.err(log, "IOException", e);
        }
        writeAnnotateMedia = false;
        if (mustRecPos) {
            mustRecPos = false;
        }
        recordPosition();
    }

    public static void recordPosition() {

        if (gpxfile == null || pauseRecording || !Locator.getInstance().isGPSprovided())
            return;

        if (writeAnnotateMedia) {
            mustRecPos = true;
        }

        if (LastRecordedPosition.getProviderType() == ProviderType.NULL) // Warte bis 2 gültige Koordinaten vorliegen
        {
            LastRecordedPosition = Locator.getInstance().getLocation(GPS).cpy();
            SaveAltitude = LastRecordedPosition.getAltitude();
        } else {
            writePos = true;
            TrackPoint NewPoint;
            double AltDiff;

            // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter
            // zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
            float[] dist = new float[1];

            MathUtils.computeDistanceAndBearing(CalculationType.FAST, LastRecordedPosition.getLatitude(), LastRecordedPosition.getLongitude(), Locator.getInstance().getLatitude(GPS), Locator.getInstance().getLongitude(GPS), dist);
            float cachedDistance = dist[0];

            if (cachedDistance > distanceForNextTrackpoint) {
                StringBuilder sb = new StringBuilder();

                sb.append("<trkpt lat=\"").append(Locator.getInstance().getLatitude(GPS)).append("\" lon=\"").append(Locator.getInstance().getLongitude(GPS)).append("\">\n")
                        .append("   <ele>").append(Locator.getInstance().getAlt()).append("</ele>\n")
                        .append("   <time>").append(GetDateTimeString()).append("</time>\n")
                        .append("   <course>").append(Locator.getInstance().getHeading(_GPS)).append("</course>\n")
                        .append("   <speed>").append(Locator.getInstance().SpeedOverGround()).append("</speed>\n")
                        .append("</trkpt>\n");

                RandomAccessFile rand;
                try {
                    rand = gpxfile.getRandomAccessFile("rw");

                    // suche letzte "</trk>"

                    int i = (int) rand.length();
                    byte[] bEnde = new byte[insertPos];
                    rand.seek(i - insertPos); // Seek to start point of file

                    for (int ct = 0; ct < insertPos; ct++) {
                        bEnde[ct] = rand.readByte(); // read byte from the file
                    }

                    // insert point
                    byte[] b = sb.toString().getBytes();
                    rand.setLength(i + b.length);
                    rand.seek(i - insertPos);
                    rand.write(b);
                    rand.write(bEnde);
                    rand.close();
                } catch (FileNotFoundException e) {
                    Log.err(log, "FileNotFoundException", e);
                } catch (IOException e) {
                    Log.err(log, "Trackrecorder", "IOException", e);
                }

                NewPoint = new TrackPoint(Locator.getInstance().getLongitude(GPS), Locator.getInstance().getLatitude(GPS), Locator.getInstance().getAlt(), Locator.getInstance().getHeading(_GPS), new Date());

                GlobalCore.aktuelleRoute.getTrackPoints().add(NewPoint);

                // notify TrackListView
                TrackListView.getInstance().getAktRouteItem().notifyTrackChanged();
                GL.that.renderOnce();

                RouteOverlay.getInstance().trackListChanged();
                LastRecordedPosition = Locator.getInstance().getLocation(GPS).cpy();
                GlobalCore.aktuelleRoute.setTrackLength(GlobalCore.aktuelleRoute.getTrackLength() + cachedDistance);

                AltDiff = Math.abs(SaveAltitude - Locator.getInstance().getAlt());
                if (AltDiff >= 25) {
                    GlobalCore.aktuelleRoute.setAltitudeDifference(GlobalCore.aktuelleRoute.getAltitudeDifference() + AltDiff);
                    SaveAltitude = Locator.getInstance().getAlt();
                }
                writePos = false;

                if (mustWriteMedia) {
                    mustWriteMedia = false;
                    AnnotateMedia(mFriendlyName, mMediaPath, mMediaCoord, mTimestamp);
                }
            }
        }
    }

    public static void pauseRecording() {
        pauseRecording = !pauseRecording;
    }

    public static void stopRecording() {
        if (GlobalCore.aktuelleRoute != null) {
            GlobalCore.aktuelleRoute.setActualTrack(false);
            GlobalCore.aktuelleRoute.setName(Translation.get("recordetTrack"));
        }
        pauseRecording = false;
        recording = false;
        gpxfile = null;
    }

    private static String generateTrackFileName() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US);
        String sDate = datFormat.format(new Date());
        return "Track_" + sDate + ".gpx";
    }

}
