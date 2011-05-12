package de.droidcachebox;

import java.util.ArrayList;

import android.location.Location;

import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Descriptor.PointD;

public class TrackRecorder {

	ArrayList<String> waypointXml = new ArrayList<String>();

//    StreamWriter outStream = null;
    boolean pauseRecording = false;

    /// Letzte aufgezeichnete Position des Empfängers
    public static Coordinate LastRecordedPosition = new Coordinate();
/*
    private void StartRecording(ClickButton sender)
    { 
        String directory = Config.GetString("TrackFolder");
        if (!Directory.Exists(directory))
            Directory.CreateDirectory(directory);

        if (outStream == null)
        {
            outStream = new StreamWriter(directory + "\\" + generateTrackFileName());

            outStream.WriteLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            outStream.WriteLine("<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">");
            outStream.WriteLine("<time>" +
                String.Format(NumberFormatInfo.InvariantInfo, "{0:0000}-{1:00}-{2:00}T{3:00}:{4:00}:{5:00}Z", DateTime.Now.Year, DateTime.Now.Month, DateTime.Now.Day, DateTime.Now.Hour, DateTime.Now.Minute, DateTime.Now.Second) +
                "</time>");
            outStream.WriteLine("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>");
            
            outStream.WriteLine("<trk><trkseg>");
        }

        pauseRecording = false;

        updateRecorderButtonAccessibility();
    }

    private void AnnotateMedia(String friendlyName, String mediaPath, Coordinate coordinate, DateTime timestamp)
    {
        String xml = "<wpt lat=\"" + coordinate.Latitude.ToString(NumberFormatInfo.InvariantInfo) + "\" lon=\"" + coordinate.Longitude.ToString(NumberFormatInfo.InvariantInfo) + "\">\n" +
            "   <ele>" + String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00}", Global.Locator.LastValidPosition.Elevation) + "</ele>\n" +
            "   <time>" + String.Format(NumberFormatInfo.InvariantInfo, "{0:0000}-{1:00}-{2:00}T{3:00}:{4:00}:{5:00}Z", timestamp.Year, timestamp.Month, timestamp.Day, timestamp.Hour, timestamp.Minute, timestamp.Second) + "</time>\n" +
            "   <name>" + friendlyName + "</name>\n" +
            "   <link href=\"" + mediaPath + "\" />\n" +
            "</wpt>";

        waypointXml.Add(xml);
    }
*/
    public static void recordPosition()
    {    	
        PointD NewPoint;
/*  
        if (outStream == null || pauseRecording || (Global.Locator != null && !Global.Locator.Position.Valid))
            return;
*/
        // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
        float[] dist = new float[4];
        Location.distanceBetween(LastRecordedPosition.Latitude, LastRecordedPosition.Longitude, Global.LastValidPosition.Latitude, Global.LastValidPosition.Longitude, dist);
        float cachedDistance = dist[0];
        
//        if ((float)Datum.WGS84.Distance(LastRecordedPosition.Latitude, LastRecordedPosition.Longitude, Global.LastValidPosition.Latitude, Global.LastValidPosition.Longitude) > Global.TrackDistance)
        if (cachedDistance > Global.TrackDistance) 
        {
/*            outStream.WriteLine("<trkpt lat=\"" + Global.Locator.LastValidPosition.Latitude.ToString(NumberFormatInfo.InvariantInfo) + "\" lon=\"" + Global.Locator.LastValidPosition.Longitude.ToString(NumberFormatInfo.InvariantInfo) + "\">");
            outStream.WriteLine("   <ele>" + String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00}", Global.Locator.LastValidPosition.Elevation) + "</ele>");
            outStream.WriteLine("   <time>" +
                String.Format(NumberFormatInfo.InvariantInfo, "{0:0000}-{1:00}-{2:00}T{3:00}:{4:00}:{5:00}Z", DateTime.Now.Year, DateTime.Now.Month, DateTime.Now.Day, DateTime.Now.Hour, DateTime.Now.Minute, DateTime.Now.Second) +
                "</time>");
            outStream.WriteLine("   <course>" + Global.Locator.Heading.ToString("0.0", CultureInfo.InvariantCulture) + "</course>");
            outStream.WriteLine("   <speed>" + Global.Locator.SpeedOverGround.ToString("0.0", CultureInfo.InvariantCulture) + "</speed>");
            outStream.WriteLine("</trkpt>");
*/
            NewPoint = new PointD(Descriptor.LongitudeToTileX(15, Global.LastValidPosition.Longitude),
                                  Descriptor.LatitudeToTileY(15, Global.LastValidPosition.Latitude));
            Global.AktuelleRoute.Points.add(NewPoint);
            LastRecordedPosition = new Coordinate(Global.LastValidPosition);
        }
    }
/*
    private void PauseRecording(ClickButton sender)
    {
        if (!pauseRecording)
        {
            pauseRecording = true;
            updateRecorderButtonAccessibility();
        }
    }

    private void StopRecording(ClickButton sender)
    {
        if (outStream == null)
            return;

        outStream.WriteLine("</trkseg>");
        outStream.WriteLine("</trk>");

        foreach (String waypoint in waypointXml)
            outStream.WriteLine(waypoint);

        waypointXml = new List<string>();
        
        outStream.WriteLine("</gpx>");
        outStream.Flush();
        outStream.Close();
        outStream = null;

        pauseRecording = false;

        updateRecorderButtonAccessibility();
    }

    void updateRecorderButtonAccessibility()
    {
        recordButton.Enabled = outStream == null || pauseRecording;
        stopButton.Enabled = outStream != null;
        pauseButton.Enabled = !pauseRecording && outStream != null;
    }

    String generateTrackFileName()
    {
        return String.Format("Track_{0:0000}-{1:00}-{2:00}-{3:00}{4:00}{5:00}.gpx", DateTime.Now.Year, DateTime.Now.Month, DateTime.Now.Day, DateTime.Now.Hour, DateTime.Now.Minute, DateTime.Now.Second);
    }
*/
}
