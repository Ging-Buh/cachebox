package de.droidcachebox.gdx.views;

import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.TrackList;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TrackListView extends V_ListView {
    private final static String log = "TrackListView";
    private static CB_RectF itemRec;
    private static TrackListView trackListView;
    private TrackListViewItem currentRouteItem;

    private TrackListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackListView");
        itemRec = new CB_RectF(0, 0, getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f);
        setBackground(Sprites.ListBack);
        // specific initialize
        setEmptyMsgItem(Translation.get("EmptyTrackList"));
        setAdapter(new TrackListViewAdapter());
    }

    public static TrackListView getInstance() {
        if (trackListView == null) trackListView = new TrackListView();
        return trackListView;
    }

    @Override
    public void onShow() {
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.info(log, "Dataset changed");

    }

    public TrackListViewItem getAktRouteItem() {
        return currentRouteItem;
    }

    public void selectTrackFileReadAndAddToTracks() {
        new FileOrFolderPicker(CB_UI_Settings.TrackFolder.getValue(), "*.gpx", Translation.get("LoadTrack"), Translation.get("load"), abstractFile -> {
            if (abstractFile != null) {
                readFromGpxFile(abstractFile);
            }
        }).show();
    }

    /**
     * Going to assume date is always in the form:<br>
     * 2006-05-25T08:55:01Z<br>
     * 2006-05-25T08:56:35Z<br>
     * <br>
     * i.e.: yyyy-mm-ddThh-mm-ssZ <br>
     * code from Tommi Laukkanen http://www.substanceofcode.com
     *
     * @param dateString ?
     * @return ?
     */
    private Date parseDate(String dateString) {
        try {
            final int year = Integer.parseInt(dateString.substring(0, 4));
            final int month = Integer.parseInt(dateString.substring(5, 7));
            final int day = Integer.parseInt(dateString.substring(8, 10));

            final int hour = Integer.parseInt(dateString.substring(11, 13));
            final int minute = Integer.parseInt(dateString.substring(14, 16));
            final int second = Integer.parseInt(dateString.substring(17, 19));

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1); // Beware MONTH was counted for 0 to 11, so we have to subtract 1
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);

            return calendar.getTime();
        } catch (Exception ex) {
            Log.err(log, "Exception caught trying to parse date : ", ex);
        }
        return null;
    }

    public void readFromGpxFile(AbstractFile abstractFile) {
        // !!! it is possible that a gpx file contains more than 1 <trk> segments
        // they are all added to the tracks (Tracklist)
        ArrayList<Track> tracks = new ArrayList<>();
        float[] dist = new float[4];
        double distance = 0;
        double altitudeDifference = 0;
        double deltaAltitude;
        CoordinateGPS fromPosition = new CoordinateGPS(0, 0);
        BufferedReader reader;
        HSV_Color trackColor = null;

        try {
            InputStreamReader isr = new InputStreamReader(abstractFile.getFileInputStream(), StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            Track track = new Track("");

            String line;
            String tmpLine;
            String gpxName = null;
            boolean isSeg = false;
            boolean isTrk = false;
            boolean isRte = false;
            boolean isTrkptOrRtept = false;
            boolean readName = false;
            int anzSegments = 0;

            CoordinateGPS lastAcceptedCoordinate = null;
            double lastAcceptedDirection = -1;
            Date lastAcceptedTime = null;

            StringBuilder sb = new StringBuilder();
            String rline;
            while ((rline = reader.readLine()) != null) {
                for (int i = 0; i < rline.length(); i++) {
                    char nextChar = rline.charAt(i);
                    sb.append(nextChar);

                    if (nextChar == '>') {
                        line = sb.toString().trim().toLowerCase();
                        tmpLine = sb.toString();
                        sb = new StringBuilder();

                        if (!isTrk) // Begin of the Track detected?
                        {
                            if (line.contains("<trk>")) {
                                isTrk = true;
                                continue;
                            }
                        }

                        if (!isSeg) // Begin of the Track Segment detected?
                        {
                            if (line.contains("<trkseg>")) {
                                isSeg = true;
                                track = new Track("");
                                track.setFileName(abstractFile.getAbsolutePath());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(abstractFile.getName()); // FileIO.getFileName(file)
                                else {
                                    if (anzSegments <= 1)
                                        track.setName(gpxName);
                                    else
                                        track.setName(gpxName + anzSegments);
                                }
                                continue;
                            }
                        }

                        if (!isRte) // Begin of the Route detected?
                        {
                            if (line.contains("<rte>")) {
                                isRte = true;
                                track = new Track("");
                                track.setFileName(abstractFile.getAbsolutePath());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(abstractFile.getName()); // FileIO.getFileName(file)
                                else {
                                    if (anzSegments <= 1)
                                        track.setName(gpxName);
                                    else
                                        track.setName(gpxName + anzSegments);
                                }
                                continue;
                            }
                        }

                        if ((line.contains("<name>")) & !isTrkptOrRtept) // found <name>?
                        {
                            readName = true;
                            continue;
                        }

                        if (readName & !isTrkptOrRtept) {
                            int cdata_start;
                            int name_start = 0;
                            int name_end;

                            name_end = line.indexOf("</name>");

                            // Name contains cdata?
                            cdata_start = line.indexOf("[cdata[");
                            if (cdata_start > -1) {
                                name_start = cdata_start + 7;
                                name_end = line.indexOf("]");
                            }

                            if (name_end > name_start) {
                                // tmpLine, damit Groß-/Kleinschreibung beachtet wird
                                if (isSeg || isRte)
                                    track.setName(tmpLine.substring(name_start, name_end));
                                else
                                    gpxName = tmpLine.substring(name_start, name_end);
                            }

                            readName = false;
                            continue;
                        }

                        if (line.contains("</trkseg>")) // End of the Track Segment detected?
                        {
                            if (track.getTrackPoints().size() < 2)
                                track.setName("no Route segment found");
                            track.setVisible(true);
                            track.setTrackLength(distance);
                            track.setAltitudeDifference(altitudeDifference);
                            tracks.add(track);
                            isSeg = false;
                            break;
                        }

                        if (line.contains("</rte>")) // End of the Route detected?
                        {
                            if (track.getTrackPoints().size() < 2)
                                track.setName("no Route segment found");
                            track.setVisible(true);
                            track.setTrackLength(distance);
                            track.setAltitudeDifference(altitudeDifference);
                            tracks.add(track);
                            isRte = false;
                            break;
                        }

                        if ((line.contains("<trkpt")) || (line.contains("<rtept"))) {
                            isTrkptOrRtept = true;
                            // Trackpoint lesen
                            int lonIdx = line.indexOf("lon=\"") + 5;
                            int latIdx = line.indexOf("lat=\"") + 5;

                            int lonEndIdx = line.indexOf("\"", lonIdx);
                            int latEndIdx = line.indexOf("\"", latIdx);

                            String latStr = line.substring(latIdx, latEndIdx);
                            String lonStr = line.substring(lonIdx, lonEndIdx);

                            double lat = Double.parseDouble(latStr);
                            double lon = Double.parseDouble(lonStr);

                            lastAcceptedCoordinate = new CoordinateGPS(lat, lon);
                        }

                        if (line.contains("</time>")) {
                            // Time lesen
                            int timIdx = line.indexOf("<time>") + 6;
                            if (timIdx == 5)
                                timIdx = 0;
                            int timEndIdx = line.indexOf("</time>", timIdx);

                            String timStr = line.substring(timIdx, timEndIdx);

                            lastAcceptedTime = parseDate(timStr);
                        }

                        if (line.contains("</course>")) {
                            // Course lesen
                            int couIdx = line.indexOf("<course>") + 8;
                            if (couIdx == 7)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</course>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);

                            lastAcceptedDirection = Double.parseDouble(couStr);

                        }

                        if ((line.contains("</ele>")) & isTrkptOrRtept) {
                            // Elevation lesen
                            int couIdx = line.indexOf("<ele>") + 5;
                            if (couIdx == 4)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</ele>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);

                            lastAcceptedCoordinate.setElevation(Double.parseDouble(couStr));

                        }

                        if (line.contains("</gpxx:colorrgb>")) {
                            // Color lesen
                            int couIdx = line.indexOf("<gpxx:colorrgb>") + 15;
                            if (couIdx == 14)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</gpxx:colorrgb>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);
                            trackColor = new HSV_Color(couStr);
                            track.setColor(trackColor);
                        }

                        if ((line.contains("</trkpt>")) || (line.contains("</rtept>")) || ((line.contains("/>")) & isTrkptOrRtept)) {
                            // trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
                            isTrkptOrRtept = false;
                            if (lastAcceptedCoordinate != null) {
                                track.getTrackPoints().add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getElevation(), lastAcceptedDirection, lastAcceptedTime));

                                // Calculate the length of a Track
                                if (!fromPosition.isValid()) {
                                    fromPosition = new CoordinateGPS(lastAcceptedCoordinate);
                                    fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                    fromPosition.setValid(true);
                                } else {
                                    MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, fromPosition.getLatitude(), fromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(), dist);
                                    distance = distance + dist[0];
                                    deltaAltitude = Math.abs(fromPosition.getElevation() - lastAcceptedCoordinate.getElevation());
                                    fromPosition = new CoordinateGPS(lastAcceptedCoordinate);

                                    if (deltaAltitude >= 25.0) // nur aufaddieren wenn Höhenunterschied größer 10 Meter
                                    {
                                        fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                        altitudeDifference = altitudeDifference + deltaAltitude;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException ex) {
            Log.err(log, "readFromGpxFile", ex);
        }
        for (Track track : tracks) {
            if (trackColor != null) track.setColor(trackColor);
            TrackList.getInstance().addTrack(track);
        }
        notifyDataSetChanged();

    }

    public void loadTrack(String trackPath, String file) {
        // used by autoload
        String absolutPath;
        if (file.equals("")) {
            absolutPath = trackPath;
        } else {
            absolutPath = trackPath + "/" + file;
        }
        readFromGpxFile(FileFactory.createFile(absolutPath));
    }

    public class TrackListViewAdapter implements Adapter {
        // if tracking is activated, aktuelleRoute gets index 0 and the others get one more
        public TrackListViewAdapter() {
        }

        @Override
        public int getCount() {
            int size = TrackList.getInstance().getNumberOfTracks();
            if (GlobalCore.currentRoute != null)
                size++;
            return size;
        }

        @Override
        public ListViewItemBase getView(int viewPosition) {
            Log.info(log, "get track item number " + viewPosition + " (" + (GlobalCore.currentRoute != null ? "with " : "without ") + "tracking." + ")");
            int tracksIndex = viewPosition;
            if (GlobalCore.currentRoute != null) {
                if (viewPosition == 0) {
                    currentRouteItem = new TrackListViewItem(itemRec, viewPosition, GlobalCore.currentRoute);
                    return currentRouteItem;
                }
                tracksIndex--; // viewPosition - 1, if tracking is activated
            }
            return new TrackListViewItem(itemRec, viewPosition, TrackList.getInstance().getTrack(tracksIndex));
        }

        @Override
        public float getItemSize(int position) {
            if (GlobalCore.currentRoute != null && position == 1) {
                // so there is a distance between aktuelleRoute and the others
                return itemRec.getHeight() + itemRec.getHalfHeight();
            }
            return itemRec.getHeight();
        }

    }

}
