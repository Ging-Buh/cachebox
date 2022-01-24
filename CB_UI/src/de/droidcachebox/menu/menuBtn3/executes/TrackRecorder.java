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
package de.droidcachebox.menu.menuBtn3.executes;

import static de.droidcachebox.menu.Action.ShowTracks;

import com.badlogic.gdx.graphics.Color;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.droidcachebox.Platform;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.CBLocation.ProviderType;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.Locator.CompassType;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.menuBtn3.ShowTracks;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.Log;

public class TrackRecorder {
    private static final String log = "TrackRecorder";
    private final static ProviderType GPS = ProviderType.GPS;
    private final static CompassType _GPS = CompassType.GPS;
    private static TrackRecorder instance;
    public boolean pauseRecording = false;
    public boolean recording = false;
    public int distanceForNextTrackpoint;
    private double savedAltitude = 0;
    private CBLocation lastRecordedPosition = CBLocation.NULL_LOCATION;
    private String mFriendlyName = "";
    private String mMediaPath = "";
    private CBLocation mMediaCoord = null;
    private String mTimestamp = "";
    private AbstractFile gpxfile = null;
    private boolean writeAnnotateMedia = false;
    private int insertPos = 24;
    private boolean mustWriteMedia = false;
    private boolean mustRecPos = false;
    private boolean writePos = false;
    private int currentRouteCount = 0;

    private TrackRecorder() {

    }

    public static TrackRecorder getInstance() {
        if (instance == null) instance = new TrackRecorder();
        return instance;
    }

    public void startRecording() {
        if (Platform.request_getLocationIfInBackground()) {

            distanceForNextTrackpoint = Settings.trackDistance.getValue();
            Track currentRoute = new Track(Translation.get("actualTrack"));
            currentRoute.setColor(Color.BLUE);
            currentRoute.setVisible(true);
            currentRoute.setActualTrack(true);
            currentRouteCount = 0;
            currentRoute.setTrackLength(0);
            currentRoute.setAltitudeDifference(0);
            TrackList.getInstance().currentRoute = currentRoute;

            String directory = Settings.TrackFolder.getValue();
            if (!FileIO.createDirectory(directory))
                return;

            if (gpxfile == null) {
                gpxfile = FileFactory.createFile(directory + "/" + generateTrackFileName());
                FileWriter writer;
                try {
                    writer = gpxfile.getFileWriter();
                    writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                            .append("<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n")
                            .append("<time>").append(getDateTimeString()).append("</time>\n")
                            // set real bounds or basecamp (mapsource) will not import this track
                            // writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");
                            .append("<trk><trkseg>\n</trkseg>\n</trk>\n</gpx>\n");
                    writer.close();
                } catch (IOException e) {
                    Log.err(log, "IOException", e);
                }
            }

            pauseRecording = false;
            recording = true;

            ((ShowTracks) ShowTracks.action).notifyDataSetChanged();
        }
    }

    private String getDateTimeString() {
        Date timestamp = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp);
        datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sDate += "T" + datFormat.format(timestamp) + "Z";
        return sDate;
    }

    public void annotateMedia(final String friendlyName, final String mediaPath, final CBLocation location, final String timestamp) {
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

    public void recordPosition() {

        if (gpxfile == null || pauseRecording || !Locator.getInstance().isGPSprovided())
            return;

        if (writeAnnotateMedia) {
            mustRecPos = true;
        }

        if (lastRecordedPosition.getProviderType() == ProviderType.NULL) // Warte bis 2 gültige Koordinaten vorliegen
        {
            lastRecordedPosition = Locator.getInstance().getLocation(GPS).cpy();
            savedAltitude = lastRecordedPosition.getAltitude();
        } else {
            writePos = true;
            TrackPoint NewPoint;
            double AltDiff;

            // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter
            // zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
            float[] dist = new float[1];

            MathUtils.computeDistanceAndBearing(CalculationType.FAST, lastRecordedPosition.getLatitude(), lastRecordedPosition.getLongitude(), Locator.getInstance().getLatitude(GPS), Locator.getInstance().getLongitude(GPS), dist);
            float cachedDistance = dist[0];

            if (cachedDistance > distanceForNextTrackpoint) {
                StringBuilder sb = new StringBuilder();

                sb.append("<trkpt lat=\"").append(Locator.getInstance().getLatitude(GPS)).append("\" lon=\"").append(Locator.getInstance().getLongitude(GPS)).append("\">\n")
                        .append("   <ele>").append(Locator.getInstance().getAlt()).append("</ele>\n")
                        .append("   <time>").append(getDateTimeString()).append("</time>\n")
                        .append("   <course>").append(Locator.getInstance().getHeading(_GPS)).append("</course>\n")
                        .append("   <speed>").append(Locator.getInstance().speedOverGround()).append("</speed>\n")
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

                TrackList.getInstance().currentRoute.getTrackPoints().add(NewPoint);

                // notify TrackListView (if already created)
                ((ShowTracks) ShowTracks.action).notifyCurrentRouteChanged();

                TrackList.getInstance().trackListChanged();
                lastRecordedPosition = Locator.getInstance().getLocation(GPS).cpy();
                TrackList.getInstance().currentRoute.setTrackLength(TrackList.getInstance().currentRoute.getTrackLength() + cachedDistance);

                AltDiff = Math.abs(savedAltitude - Locator.getInstance().getAlt());
                if (AltDiff >= 25) {
                    TrackList.getInstance().currentRoute.setAltitudeDifference(TrackList.getInstance().currentRoute.getAltitudeDifference() + AltDiff);
                    savedAltitude = Locator.getInstance().getAlt();
                }
                writePos = false;

                if (mustWriteMedia) {
                    mustWriteMedia = false;
                    annotateMedia(mFriendlyName, mMediaPath, mMediaCoord, mTimestamp);
                }
            }
        }
    }

    public void pauseRecording() {
        pauseRecording = !pauseRecording;
    }

    public void stopRecording() {
        if (TrackList.getInstance().currentRoute != null) {
            TrackList.getInstance().currentRoute.setActualTrack(false);
            TrackList.getInstance().currentRoute.setName(Translation.get("recordetTrack"));
        }
        pauseRecording = false;
        recording = false;
        gpxfile = null;
    }

    private String generateTrackFileName() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US);
        String sDate = datFormat.format(new Date());
        return "Track_" + sDate + ".gpx";
    }

}
