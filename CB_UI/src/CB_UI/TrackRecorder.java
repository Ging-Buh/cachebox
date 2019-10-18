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
package CB_UI;

import CB_Locator.Location;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Locator.CompassType;
import CB_Locator.Map.Track;
import CB_Locator.Map.TrackPoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Views.TrackListView;
import CB_Utils.Log.Log;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.graphics.Color;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TrackRecorder {
    private static final String log = "TrackRecorder";
    private final static ProviderType GPS = ProviderType.GPS;
    private final static CompassType _GPS = CompassType.GPS;
    public static boolean pauseRecording = false;
    public static boolean recording = false;
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
    public static int distanceForNextTrackpoint;

    public static void StartRecording() {
        distanceForNextTrackpoint = Config.TrackDistance.getValue();

        GlobalCore.AktuelleRoute = new Track(Translation.get("actualTrack"), Color.BLUE);
        GlobalCore.AktuelleRoute.ShowRoute = true;
        GlobalCore.AktuelleRoute.IsActualTrack = true;
        GlobalCore.aktuelleRouteCount = 0;
        GlobalCore.AktuelleRoute.TrackLength = 0;
        GlobalCore.AktuelleRoute.AltitudeDifference = 0;

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
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp);
        datFormat = new SimpleDateFormat("HH:mm:ss");
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

                GlobalCore.AktuelleRoute.Points.add(NewPoint);

                // notify TrackListView
                TrackListView.getInstance().notifyActTrackChanged();

                RouteOverlay.RoutesChanged();
                LastRecordedPosition = Locator.getInstance().getLocation(GPS).cpy();
                GlobalCore.AktuelleRoute.TrackLength += cachedDistance;

                AltDiff = Math.abs(SaveAltitude - Locator.getInstance().getAlt());
                if (AltDiff >= 25) {
                    GlobalCore.AktuelleRoute.AltitudeDifference += AltDiff;
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

    public static void PauseRecording() {
        pauseRecording = !pauseRecording;
    }

    public static void StopRecording() {
        if (GlobalCore.AktuelleRoute != null) {
            GlobalCore.AktuelleRoute.IsActualTrack = false;
            GlobalCore.AktuelleRoute.Name = Translation.get("recordetTrack");
        }
        pauseRecording = false;
        recording = false;
        gpxfile = null;
    }

    private static String generateTrackFileName() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String sDate = datFormat.format(new Date());
        return "Track_" + sDate + ".gpx";
    }

}
