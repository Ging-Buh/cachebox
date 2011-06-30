package de.droidcachebox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

import CB_Core.FileIO;
import CB_Core.Types.Coordinate;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;


import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Descriptor.PointD;
import de.droidcachebox.Map.RouteOverlay;

public class TrackRecorder {

	static ArrayList<String> waypointXml = new ArrayList<String>();

//    StreamWriter outStream = null;
    private static File gpxfile = null;
    private static FileWriter writer = null;
    public static boolean pauseRecording = false;
    public static boolean recording = false;

    /// Letzte aufgezeichnete Position des Empfängers
    public static Coordinate LastRecordedPosition = new Coordinate();

    public static void StartRecording()
    { 
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(4);
        
        Global.AktuelleRoute = new RouteOverlay.Route(paint, "actual Track");
        Global.AktuelleRoute.ShowRoute = true;
        Global.aktuelleRouteCount = 0;
        RouteOverlay.Routes.add(Global.AktuelleRoute);

        String directory = Config.GetString("TrackFolder");
        if (!FileIO.DirectoryExists(directory))
            return;

        
        if (writer == null)
        {
            gpxfile = new File(directory + "/" + generateTrackFileName());
			try {
				writer = new FileWriter(gpxfile);
	            try {
					writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		            writer.append("<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
		            Date now = new Date();
		            SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
		            String sDate = datFormat.format(now);
		            datFormat = new SimpleDateFormat("HH:mm:ss");
		            sDate += "T" + datFormat.format(now) + "Z";

		            writer.append("<time>" + sDate+ "</time>\n"); 
		                
		            writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");
		            
		            writer.append("<trk><trkseg>\n");
		            
		            writer.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

        }

        pauseRecording = false;
        recording = true;

//        updateRecorderButtonAccessibility();
    }

    public static void AnnotateMedia(String friendlyName, String mediaPath, Coordinate coordinate, String timestamp)
    {
        String xml = "<wpt lat=\"" + String.valueOf(coordinate.Latitude) + "\" lon=\"" + String.valueOf(coordinate.Longitude) + "\">\n" +
            "   <ele>" + String.valueOf(Global.LastValidPosition.Elevation) + "</ele>\n" +
            "   <time>" + timestamp + "</time>\n" +
            "   <name>" + friendlyName + "</name>\n" +
            "   <link href=\"" + mediaPath + "\" />\n" +
            "</wpt>";

        waypointXml.add(xml);
      
    }

    public static void recordPosition()
    {    	
        PointD NewPoint;
  
        if (writer == null || pauseRecording || (Global.Locator != null && !Global.LastValidPosition.Valid))
            return;

        // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
        float[] dist = new float[4];
        Location.distanceBetween(LastRecordedPosition.Latitude, LastRecordedPosition.Longitude, Global.LastValidPosition.Latitude, Global.LastValidPosition.Longitude, dist);
        float cachedDistance = dist[0];
        
//        if ((float)Datum.WGS84.Distance(LastRecordedPosition.Latitude, LastRecordedPosition.Longitude, Global.LastValidPosition.Latitude, Global.LastValidPosition.Longitude) > Global.TrackDistance)
        if (cachedDistance > Global.TrackDistance) 
        {
            try {
				writer.append("<trkpt lat=\"" + String.valueOf(Global.LastValidPosition.Latitude) + "\" lon=\"" + String.valueOf(Global.LastValidPosition.Longitude) + "\">\n");
	            writer.append("   <ele>" + String.valueOf(Global.LastValidPosition.Elevation) + "</ele>\n");
	            Date now = new Date();
	            SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
	            String sDate = datFormat.format(now);
	            datFormat = new SimpleDateFormat("HH:mm:ss");
	            sDate += "T" + datFormat.format(now) + "Z";
	            writer.append("   <time>" + sDate + "</time>\n");
	            writer.append("   <course>" + String.valueOf(Global.Locator.getHeading()) + "</course>\n");
	            writer.append("   <speed>" + String.valueOf(Global.Locator.SpeedOverGround())  + "</speed>\n");
	            writer.append("</trkpt>\n");
	            writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            NewPoint = new PointD(Descriptor.LongitudeToTileX(15, Global.LastValidPosition.Longitude),
                                  Descriptor.LatitudeToTileY(15, Global.LastValidPosition.Latitude));
            Global.AktuelleRoute.Points.add(NewPoint);
            LastRecordedPosition = new Coordinate(Global.LastValidPosition);
        }
    }

    public static void PauseRecording()
    {
        if (!pauseRecording)
        {
            pauseRecording = true;
  //          updateRecorderButtonAccessibility();
        }
    }

    public static void StopRecording()
    {
        if (writer == null)
            return;

        try {
			writer.append("</trkseg>\n");
	        writer.append("</trk>\n");

	        for (String item: waypointXml)
	        	writer.append(item);

	        waypointXml.clear();
        
	        writer.append("</gpx>\n");
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        writer = null;

        pauseRecording = false;
        recording = false;
//        updateRecorderButtonAccessibility();
    }
/*
    void updateRecorderButtonAccessibility()
    {
        recordButton.Enabled = outStream == null || pauseRecording;
        stopButton.Enabled = outStream != null;
        pauseButton.Enabled = !pauseRecording && outStream != null;
    }
*/
    private static String generateTrackFileName()
    {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String sDate = datFormat.format(new Date());
        	
        return "Track_" + sDate + ".gpx";
    }

}
