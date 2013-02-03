package CB_Core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Map.RouteOverlay;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.Color;

public class TrackRecorder
{

	// StreamWriter outStream = null;
	private static File gpxfile = null;
	private static FileWriter writer = null;
	public static boolean pauseRecording = false;
	public static boolean recording = false;

	// / Letzte aufgezeichnete Position des Empfängers
	public static Coordinate LastRecordedPosition = new Coordinate();

	public static void StartRecording()
	{

		GlobalCore.AktuelleRoute = new RouteOverlay.Track(Translation.Get("actualTrack"), Color.BLUE);
		GlobalCore.AktuelleRoute.ShowRoute = true;
		GlobalCore.AktuelleRoute.IsActualTrack = true;
		GlobalCore.aktuelleRouteCount = 0;
		GlobalCore.AktuelleRoute.TrackLength = 0;
		GlobalCore.AktuelleRoute.AltitudeDifference = 0;

		String directory = Config.settings.TrackFolder.getValue();
		if (!FileIO.DirectoryExists(directory)) return;

		if (gpxfile == null)
		{
			gpxfile = new File(directory + "/" + generateTrackFileName());
			try
			{
				writer = new FileWriter(gpxfile);
				try
				{
					writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					writer.append("<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
					writer.append("<time>" + GetDateTimeString() + "</time>\n");
					// set real bounds or basecamp (mapsource) will not import this track
					// writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");
					writer.append("<trk><trkseg>\n");
					writer.flush();
				}
				catch (IOException e)
				{
					CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e);
				}
			}
			catch (IOException e1)
			{
				CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e1);
			}

			try
			{
				writer.append("</trkseg>\n");
				writer.append("</trk>\n");

				writer.append("</gpx>\n");
				writer.flush();
				writer.close();
			}
			catch (IOException e)
			{
				CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e);
			}
			writer = null;

		}

		pauseRecording = false;
		recording = true;

		// updateRecorderButtonAccessibility();
	}

	private static String GetDateTimeString()
	{
		Date timestamp = new Date();
		SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
		datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String sDate = datFormat.format(timestamp);
		datFormat = new SimpleDateFormat("HH:mm:ss");
		datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		sDate += "T" + datFormat.format(timestamp) + "Z";
		return sDate;
	}

	private static boolean writeAnnotateMedia = false;

	private static int insertPos = 24;

	private static boolean mustWriteMedia = false;
	static String mFriendlyName = "";
	static String mMediaPath = "";
	static Coordinate mMediaCoord = null;
	static String mTimestamp = "";

	public static void AnnotateMedia(final String friendlyName, final String mediaPath, final Coordinate coordinate, final String timestamp)
	{
		writeAnnotateMedia = true;

		if (writePos)
		{
			mFriendlyName = friendlyName;
			mMediaPath = mediaPath;
			mMediaCoord = coordinate;
			mTimestamp = timestamp;
			mustWriteMedia = true;
		}

		if (gpxfile == null) return;

		String xml = "<wpt lat=\"" + String.valueOf(coordinate.getLatitude()) + "\" lon=\"" + String.valueOf(coordinate.getLongitude())
				+ "\">\n" + "   <ele>" + String.valueOf(GlobalCore.LastValidPosition.getElevation()) + "</ele>\n" + "   <time>" + timestamp
				+ "</time>\n" + "   <name>" + friendlyName + "</name>\n" + "   <link href=\"" + mediaPath + "\" />\n" + "</wpt>\n";

		RandomAccessFile rand;
		try
		{
			rand = new RandomAccessFile(gpxfile, "rw");

			int i = (int) rand.length();

			byte[] bEnde = new byte[8];

			rand.seek(i - 8); // Seek to start point of file

			for (int ct = 0; ct < 8; ct++)
			{
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

		}
		catch (FileNotFoundException e)
		{
			CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e);
		}
		catch (IOException e)
		{
			CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e);
		}
		writeAnnotateMedia = false;
		if (mustRecPos)
		{
			mustRecPos = false;
		}
		recordPosition();
	}

	private static boolean mustRecPos = false;
	private static boolean writePos = false;

	public static void recordPosition()
	{

		if (gpxfile == null || pauseRecording || (GlobalCore.Locator == null && !GlobalCore.LastValidPosition.Valid)
				|| !GlobalCore.Locator.isGPSprovided()) return;

		if (writeAnnotateMedia)
		{
			mustRecPos = true;
		}

		if (!LastRecordedPosition.Valid) // Warte bis 2 gültige Koordinaten vorliegen
		{
			LastRecordedPosition = GlobalCore.LastValidPosition;
		}
		else
		{
			writePos = true;
			TrackPoint NewPoint;
			double AltDiff = 0;

			// wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter
			// zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
			float[] dist = new float[4];

			Coordinate.distanceBetween(LastRecordedPosition.getLatitude(), LastRecordedPosition.getLongitude(),
					GlobalCore.LastValidPosition.getLatitude(), GlobalCore.LastValidPosition.getLongitude(), dist);
			float cachedDistance = dist[0];

			if (cachedDistance > GlobalCore.TrackDistance)
			{
				StringBuilder sb = new StringBuilder();

				sb.append("<trkpt lat=\"" + String.valueOf(GlobalCore.LastValidPosition.getLatitude()) + "\" lon=\""
						+ String.valueOf(GlobalCore.LastValidPosition.getLongitude()) + "\">\n");
				sb.append("   <ele>" + String.valueOf(GlobalCore.LastValidPosition.getElevation()) + "</ele>\n");
				sb.append("   <time>" + GetDateTimeString() + "</time>\n");
				sb.append("   <course>" + String.valueOf(GlobalCore.Locator.getHeading()) + "</course>\n");
				sb.append("   <speed>" + String.valueOf(GlobalCore.Locator.SpeedOverGround()) + "</speed>\n");
				sb.append("</trkpt>\n");
				AltDiff = Math.abs(LastRecordedPosition.getElevation() - GlobalCore.LastValidPosition.getElevation());
				RandomAccessFile rand;
				try
				{
					rand = new RandomAccessFile(gpxfile, "rw");

					// suche letzte "</trk>"

					int i = (int) rand.length();
					byte[] bEnde = new byte[insertPos];
					rand.seek(i - insertPos); // Seek to start point of file

					for (int ct = 0; ct < insertPos; ct++)
					{
						bEnde[ct] = rand.readByte(); // read byte from the file
					}

					// insert point
					byte[] b = sb.toString().getBytes();
					rand.setLength(i + b.length);
					rand.seek(i - insertPos);
					rand.write(b);
					rand.write(bEnde);
					rand.close();
				}
				catch (FileNotFoundException e)
				{
					CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e);
				}
				catch (IOException e)
				{
					CB_Core.Log.Logger.Error("Trackrecorder", "IOException", e);
				}

				NewPoint = new TrackPoint(GlobalCore.LastValidPosition.getLongitude(), GlobalCore.LastValidPosition.getLatitude(),
						GlobalCore.LastValidPosition.getElevation(), GlobalCore.Locator.getHeading(), new Date());

				GlobalCore.AktuelleRoute.Points.add(NewPoint);

				// notify TrackListView
				if (TrackListView.that != null) TrackListView.that.notifyActTrackChanged();

				RouteOverlay.RoutesChanged();
				LastRecordedPosition = GlobalCore.LastValidPosition;
				GlobalCore.AktuelleRoute.TrackLength += cachedDistance;
				GlobalCore.AktuelleRoute.AltitudeDifference += AltDiff;
				writePos = false;

				if (mustWriteMedia)
				{
					mustWriteMedia = false;
					AnnotateMedia(mFriendlyName, mMediaPath, mMediaCoord, mTimestamp);
				}
			}
		}
	}

	public static void PauseRecording()
	{
		if (!pauseRecording)
		{
			pauseRecording = true;
			// updateRecorderButtonAccessibility();
		}
	}

	public static void StopRecording()
	{
		if (GlobalCore.AktuelleRoute != null)
		{
			GlobalCore.AktuelleRoute.IsActualTrack = false;
			GlobalCore.AktuelleRoute.Name = Translation.Get("recordetTrack");
		}
		pauseRecording = false;
		recording = false;
		gpxfile = null;
	}

	private static String generateTrackFileName()
	{
		SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String sDate = datFormat.format(new Date());

		return "Track_" + sDate + ".gpx";
	}

}
