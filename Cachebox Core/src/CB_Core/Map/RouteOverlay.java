package CB_Core.Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.PolylineReduction;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class RouteOverlay
{
	public static ArrayList<Track> Routes = new ArrayList<Track>();
	public static boolean mRoutesChanged = false;

	public static void RoutesChanged()
	{
		mRoutesChanged = true;
	}

	public static class Track
	{
		public ArrayList<TrackPoint> Points;
		public String Name;
		public String FileName;
		public boolean ShowRoute = false;
		public boolean IsActualTrack = false;
		public Color mColor;
		public double TrackLength;
		public double AltitudeDifference;

		public Track(String name, Color color)
		{
			Points = new ArrayList<TrackPoint>();
			Name = name;
			mColor = color;
		}

		public Color getColor()
		{
			return mColor;
		}

		public void setColor(Color color)
		{
			mColor = color;
			RoutesChanged();
		}

	}

	// Read track from gpx file
	// attention it is possible that a gpx file contains more than 1 <trk> segments
	public static void MultiLoadRoute(String file, Color color)
	{
		float[] dist = new float[4];
		double Distance = 0;
		double AltitudeDifference = 0;
		Coordinate FromPosition = new Coordinate();
		BufferedReader reader;

		try
		{
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF8");
			reader = new BufferedReader(isr);
			Track route = new Track(null, color);

			String line = null;
			String GPXName = null;
			boolean isSeg = false;
			boolean isTrk = false;
			boolean isRte = false;
			boolean IStrkptORrtept = false;
			boolean ReadName = false;
			int AnzTracks = 0;

			Coordinate lastAcceptedCoordinate = null;
			double lastAcceptedDirection = -1;
			Date lastAcceptedTime = null;

			StringBuilder sb = new StringBuilder();
			String rline = null;
			while ((rline = reader.readLine()) != null)
			{
				for (int i = 0; i < rline.length(); i++)
				{
					char nextChar = rline.charAt(i);
					sb.append(nextChar);

					if (nextChar == '>')
					{
						line = sb.toString().trim().toLowerCase();
						sb = new StringBuilder();

						if (!isTrk) // Begin of the Track detected?
						{
							if (line.indexOf("<trk>") > -1)
							{
								isTrk = true;
								continue;
							}
						}

						if (!isSeg) // Begin of the Track Segment detected?
						{
							if (line.indexOf("<trkseg>") > -1)
							{
								isSeg = true;
								route = new Track(null, color);
								route.FileName = file;
								AnzTracks++;
								if (GPXName == null) route.Name = file;
								else
								{
									if (AnzTracks < 1) route.Name = GPXName;
									else
										route.Name = GPXName + AnzTracks;
								}
								continue;
							}
						}

						if (!isRte) // Begin of the Route detected?
						{
							if (line.indexOf("<rte>") > -1)
							{
								isRte = true;
								route = new Track(null, color);
								route.FileName = file;
								AnzTracks++;
								if (GPXName != null) route.Name = file;
								else
								{
									if (AnzTracks < 1) route.Name = GPXName;
									else
										route.Name = GPXName + AnzTracks;
								}
								continue;
							}
						}

						if ((line.indexOf("<name>") > -1) & !IStrkptORrtept) // found <name>?
						{
							ReadName = true;
							continue;
						}

						if (ReadName & !IStrkptORrtept)
						{
							if (isSeg | isRte) route.Name = line.substring(0, line.indexOf("</name>"));
							else
								GPXName = line.substring(0, line.indexOf("</name>"));

							ReadName = false;
							continue;
						}

						if (line.indexOf("</trkseg>") > -1) // End of the Track Segment detected?
						{
							if (route.Points.size() < 2) route.Name = "no Route segment found";
							route.ShowRoute = true;
							route.TrackLength = Distance;
							route.AltitudeDifference = AltitudeDifference;
							RouteOverlay.Routes.add(route);
							isSeg = false;
							break;
						}

						if (line.indexOf("</rte>") > -1) // End of the Route detected?
						{
							if (route.Points.size() < 2) route.Name = "no Route segment found";
							route.ShowRoute = true;
							route.TrackLength = Distance;
							route.AltitudeDifference = AltitudeDifference;
							RouteOverlay.Routes.add(route);
							isRte = false;
							break;
						}

						if ((line.indexOf("<trkpt") > -1) | (line.indexOf("<rtept") > -1))
						{
							IStrkptORrtept = true;
							// Trackpoint lesen
							int lonIdx = line.indexOf("lon=\"") + 5;
							int latIdx = line.indexOf("lat=\"") + 5;

							int lonEndIdx = line.indexOf("\"", lonIdx);
							int latEndIdx = line.indexOf("\"", latIdx);

							String latStr = line.substring(latIdx, latEndIdx);
							String lonStr = line.substring(lonIdx, lonEndIdx);

							double lat = Double.valueOf(latStr);
							double lon = Double.valueOf(lonStr);

							lastAcceptedCoordinate = new Coordinate(lat, lon);
						}

						if (line.indexOf("</time>") > -1)
						{
							// Time lesen
							int timIdx = line.indexOf("<time>") + 6;
							if (timIdx == 5) timIdx = 0;
							int timEndIdx = line.indexOf("</time>", timIdx);

							String timStr = line.substring(timIdx, timEndIdx);

							lastAcceptedTime = parseDate(timStr);
						}

						if (line.indexOf("</course>") > -1)
						{
							// Time lesen
							int couIdx = line.indexOf("<course>") + 8;
							if (couIdx == 7) couIdx = 0;
							int couEndIdx = line.indexOf("</course>", couIdx);

							String couStr = line.substring(couIdx, couEndIdx);

							lastAcceptedDirection = Double.valueOf(couStr);

						}

						if (line.indexOf("</ele>") > -1)
						{
							// Time lesen
							int couIdx = line.indexOf("<ele>") + 5;
							if (couIdx == 4) couIdx = 0;
							int couEndIdx = line.indexOf("</ele>", couIdx);

							String couStr = line.substring(couIdx, couEndIdx);

							lastAcceptedCoordinate.Elevation = Double.valueOf(couStr);

						}

						if ((line.indexOf("</trkpt>") > -1) | (line.indexOf("</rtept>") > -1))
						{
							// trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
							IStrkptORrtept = false;
							route.Points.add(new TrackPoint(lastAcceptedCoordinate.Longitude, lastAcceptedCoordinate.Latitude,
									lastAcceptedCoordinate.Elevation, lastAcceptedDirection, lastAcceptedTime));

							// Calculate the length of a Track
							if (!FromPosition.Valid)
							{
								FromPosition.Longitude = lastAcceptedCoordinate.Longitude;
								FromPosition.Latitude = lastAcceptedCoordinate.Latitude;
								FromPosition.Elevation = lastAcceptedCoordinate.Elevation;
								FromPosition.Valid = true;
							}
							else
							{
								Coordinate.distanceBetween(FromPosition.Latitude, FromPosition.Longitude, lastAcceptedCoordinate.Latitude,
										lastAcceptedCoordinate.Longitude, dist);
								Distance += dist[0];
								FromPosition.Longitude = lastAcceptedCoordinate.Longitude;
								FromPosition.Latitude = lastAcceptedCoordinate.Latitude;
								AltitudeDifference += Math.abs(FromPosition.Elevation - lastAcceptedCoordinate.Elevation);

								// Höhendifferenzen nur in 10m Schritten
								if (AltitudeDifference > 10) FromPosition.Elevation = lastAcceptedCoordinate.Elevation;
							}
						}
					}
				}
			}
			reader.close();
			return;
		}

		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	// -------------------------------------------------------------------------------
	// Funktion kann gelöscht werden, wenn es mit MultiLoadRoute keine Probleme gibt
	// und diese Funktion in simulateForm.java nicht mehr gebraucht wird
	// -------------------------------------------------------------------------------
	//
	public static Track LoadRoute(String file, Color color, double minDistanceMeters)
	{
		float[] dist = new float[4];
		double Distance = 0;
		double AltitudeDifference = 0;
		Coordinate FromPosition = new Coordinate();
		BufferedReader reader;

		try
		{
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF8");
			reader = new BufferedReader(isr);
			Track route = new Track(null, color);
			route.FileName = file;

			String line = null;
			boolean inBody = false;
			boolean inTrk = false;
			boolean ReadName = false;

			Coordinate lastAcceptedCoordinate = null;
			double lastAcceptedDirection = -1;
			Date lastAcceptedTime = null;

			StringBuilder sb = new StringBuilder();
			String rline = null;
			while ((rline = reader.readLine()) != null)
			{
				line = rline;
				for (int i = 0; i < rline.length(); i++)
				{
					char nextChar = rline.charAt(i);

					sb.append(nextChar);

					if (nextChar == '>')
					{
						line = sb.toString().trim().toLowerCase();
						sb = new StringBuilder();

						// Read Routename form gpx file
						// attention it is possible that a gpx file contains more than 1 <trk> segments
						// In this case the first name was used
						if (ReadName && (route.Name == null))
						{
							route.Name = line.substring(0, line.indexOf("</name>"));
							ReadName = false;
							continue;
						}

						if (!inTrk)
						{
							// Begin of the Track detected?
							if (line.indexOf("<trk>") > -1)
							{
								inTrk = true;
								continue;
							}

							// found <name>?
							if (line.indexOf("<name>") > -1)
							{
								ReadName = true;
								continue;
							}

						}
						else
						{
						}

						if (!inBody)
						{
							// Anfang der Trackpoints gefunden?
							if (line.indexOf("<trkseg>") > -1) inBody = true;

							continue;
						}

						// Ende gefunden?
						if (line.indexOf("</trkseg>") > 0) break;

						if (line.indexOf("<trkpt") > -1)
						{
							// Trackpoint lesen
							int lonIdx = line.indexOf("lon=\"") + 5;
							int latIdx = line.indexOf("lat=\"") + 5;

							int lonEndIdx = line.indexOf("\"", lonIdx);
							int latEndIdx = line.indexOf("\"", latIdx);

							String latStr = line.substring(latIdx, latEndIdx);
							String lonStr = line.substring(lonIdx, lonEndIdx);

							double lat = Double.valueOf(latStr);
							double lon = Double.valueOf(lonStr);

							lastAcceptedCoordinate = new Coordinate(lat, lon);

						}
					}
				}

				if (line.indexOf("</time>") > -1)
				{
					// Time lesen
					int timIdx = line.indexOf("<time>") + 6;
					if (timIdx == 5) timIdx = 0;
					int timEndIdx = line.indexOf("</time>", timIdx);

					String timStr = line.substring(timIdx, timEndIdx);

					lastAcceptedTime = parseDate(timStr);
				}

				if (line.indexOf("</course>") > -1)
				{
					// Time lesen
					int couIdx = line.indexOf("<course>") + 8;
					if (couIdx == 7) couIdx = 0;
					int couEndIdx = line.indexOf("</course>", couIdx);

					String couStr = line.substring(couIdx, couEndIdx);

					lastAcceptedDirection = Double.valueOf(couStr);

				}

				if (line.indexOf("</ele>") > -1)
				{
					// Time lesen
					int couIdx = line.indexOf("<ele>") + 5;
					if (couIdx == 4) couIdx = 0;
					int couEndIdx = line.indexOf("</ele>", couIdx);

					String couStr = line.substring(couIdx, couEndIdx);

					lastAcceptedCoordinate.Elevation = Double.valueOf(couStr);

				}

				if (line.indexOf("</trkpt>") > -1)
				{
					// trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
					route.Points.add(new TrackPoint(lastAcceptedCoordinate.Longitude, lastAcceptedCoordinate.Latitude,
							lastAcceptedCoordinate.Elevation, lastAcceptedDirection, lastAcceptedTime));

					// Calculate the length of a Track
					if (!FromPosition.Valid)
					{
						FromPosition.Longitude = lastAcceptedCoordinate.Longitude;
						FromPosition.Latitude = lastAcceptedCoordinate.Latitude;
						FromPosition.Elevation = lastAcceptedCoordinate.Elevation;
						FromPosition.Valid = true;
					}
					else
					{
						Coordinate.distanceBetween(FromPosition.Latitude, FromPosition.Longitude, lastAcceptedCoordinate.Latitude,
								lastAcceptedCoordinate.Longitude, dist);
						Distance += dist[0];
						FromPosition.Longitude = lastAcceptedCoordinate.Longitude;
						FromPosition.Latitude = lastAcceptedCoordinate.Latitude;
						AltitudeDifference += Math.abs(FromPosition.Elevation - lastAcceptedCoordinate.Elevation);

						// Höhendifferenzen nur in 10m Schritten
						if (AltitudeDifference > 10) FromPosition.Elevation = lastAcceptedCoordinate.Elevation;
					}
				}
			}

			reader.close();
			if (route.Points.size() < 2) route.Name = "no Route segment found";

			if (route.Name == null) // Wenn GPX keinen Namen enthält den Filenamen verwenden
			{
				int idx = file.lastIndexOf("/");
				if (idx == -1) route.Name = file;
				else
					route.Name = file.substring(idx + 1);
			}

			route.ShowRoute = true;
			route.TrackLength = Distance;
			route.AltitudeDifference = AltitudeDifference;

			return route;
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Going to assume date is always in the form:<br>
	 * 2006-05-25T08:55:01Z<br>
	 * 2006-05-25T08:56:35Z<br>
	 * <br>
	 * i.e.: yyyy-mm-ddThh-mm-ssZ <br>
	 * code from Tommi Laukkanen http://www.substanceofcode.com
	 * 
	 * @param dateString
	 * @return
	 */
	private static Date parseDate(String dateString)
	{
		try
		{
			final int year = Integer.parseInt(dateString.substring(0, 4));
			final int month = Integer.parseInt(dateString.substring(5, 7));
			final int day = Integer.parseInt(dateString.substring(8, 10));

			final int hour = Integer.parseInt(dateString.substring(11, 13));
			final int minute = Integer.parseInt(dateString.substring(14, 16));
			final int second = Integer.parseInt(dateString.substring(17, 19));

			final String reconstruct = year + "-" + (month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day + "T"
					+ (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second
					+ "Z";

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month - 1); // Beware MONTH was counted for 0 to 11, so we have to subtract 1
			calendar.set(Calendar.DAY_OF_MONTH, day);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, second);

			return calendar.getTime();
		}
		catch (Exception e)
		{
			Logger.Error("RouteOverlay", "Exception caught trying to parse date : ", e);
		}
		return null;
	}

	public static int AllTrackPoints = 0;
	public static int ReduceTrackPoints = 0;
	public static int DrawedLineCount = 0;
	public static double Tolleranz = 0;

	public static int aktCalcedZoomLevel = -1;

	public class Route
	{
		private Color mColor;
		protected ArrayList<TrackPoint> Points;

		public Route(Color color)
		{
			mColor = color;
			Points = new ArrayList<TrackPoint>();
		}
	}

	private static ArrayList<Route> DrawRoutes;

	public static void RenderRoute(SpriteBatch batch, int Zoom) // , Descriptor desc, float dpiScaleFactorX, float dpiScaleFactorY)
	{

		if (aktCalcedZoomLevel != Zoom || mRoutesChanged)
		{// Zoom or Routes changed => calculate new Sprite Points

			// Logger.LogCat("Zoom Changed => Calc Track Points");

			mRoutesChanged = false;
			aktCalcedZoomLevel = Zoom;
			if (DrawRoutes == null) DrawRoutes = new ArrayList<RouteOverlay.Route>();
			else
				DrawRoutes.clear();

			double tolerance = 0.01 * Math.exp(-1 * (Zoom - 11));
			Tolleranz = tolerance;

			for (int i = 0; i < Routes.size(); i++)
			{

				if (Routes.get(i) != null && Routes.get(i).ShowRoute)
				{
					addToDrawRoutes(tolerance, Routes.get(i), Zoom, false);
				}
			}

			if (GlobalCore.AktuelleRoute != null && GlobalCore.AktuelleRoute.ShowRoute)
			{
				addToDrawRoutes(tolerance, GlobalCore.AktuelleRoute, Zoom, false);
			}

		}

		DrawedLineCount = 0;

		if (DrawRoutes != null && DrawRoutes.size() > 0)
		{
			for (Route rt : DrawRoutes)
			{
				Sprite ArrowSprite = SpriteCache.Arrows.get(5);
				ArrowSprite.setColor(rt.mColor);

				Sprite PointSprite = SpriteCache.Arrows.get(10);
				PointSprite.setColor(rt.mColor);

				for (int ii = 0; ii < rt.Points.size() - 1; ii++)
				{

					double MapX1 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, rt.Points.get(ii).X);
					double MapY1 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, rt.Points.get(ii).Y);

					double MapX2 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, rt.Points.get(ii + 1).X);
					double MapY2 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, rt.Points.get(ii + 1).Y);

					Vector2 screen1 = MapView.that.worldToScreen(new Vector2((float) MapX1, (float) MapY1));
					Vector2 screen2 = MapView.that.worldToScreen(new Vector2((float) MapX2, (float) MapY2));

					CB_RectF chkRec = new CB_RectF(MapView.that);
					chkRec.setPos(0, 0);

					// chk if line on Screen
					if (chkRec.contains(screen1.x, screen1.y) || chkRec.contains(screen2.x, screen2.y))
					{
						DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, 0.8f, screen1.x, screen1.y, screen2.x, screen2.y);
						DrawedLineCount++;
					}
					else
					{// chk if intersection
						if (chkRec.getIntersection(screen1, screen2, 2) != null)
						{
							DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, 0.8f, screen1.x, screen1.y, screen2.x, screen2.y);
							DrawedLineCount++;
						}

						// the line is not on the screen
					}

				}

			}
		}
	}

	private static void addToDrawRoutes(double tolerance, Track track, int Zoom, boolean dontReduce)
	{

		synchronized (track.Points)
		{

			ArrayList<TrackPoint> reducedPoints;

			// ab zoom level 18 keine Punkte Reduzieren

			if (dontReduce || Zoom >= 18)
			{
				reducedPoints = track.Points;
			}
			else
			{
				reducedPoints = PolylineReduction.DouglasPeuckerReduction(track.Points, tolerance);
			}

			AllTrackPoints = track.Points.size();
			ReduceTrackPoints = reducedPoints.size();

			Route tmp = (new RouteOverlay()).new Route(track.mColor);
			tmp.Points = reducedPoints;

			DrawRoutes.add(tmp);

		}

	}

	public static void SaveRoute(String Path, Track track)
	{
		FileWriter writer = null;
		File gpxfile = new File(Path);
		try
		{
			writer = new FileWriter(gpxfile);
			try
			{
				writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				writer.append("<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
				Date now = new Date();
				SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
				String sDate = datFormat.format(now);
				datFormat = new SimpleDateFormat("HH:mm:ss");
				sDate += "T" + datFormat.format(now) + "Z";

				writer.append("<time>" + sDate + "</time>\n");

				writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");

				writer.append("<trk><trkseg>\n");

				writer.flush();
			}
			catch (IOException e)
			{
				CB_Core.Log.Logger.Error("SaveTrack", "IOException", e);
			}
		}
		catch (IOException e1)
		{
			CB_Core.Log.Logger.Error("SaveTrack", "IOException", e1);
		}

		try
		{
			for (int i = 0; i < track.Points.size(); i++)
			{
				writer.append("<trkpt lat=\"" + String.valueOf(track.Points.get(i).Y) + "\" lon=\"" + String.valueOf(track.Points.get(i).X)
						+ "\">\n");

				// writer.append("   <ele>" + String.valueOf(GlobalCore.LastValidPosition.Elevation) + "</ele>\n");
				// Date now = new Date();
				SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
				String sDate = datFormat.format(track.Points.get(i).TimeStamp);
				datFormat = new SimpleDateFormat("HH:mm:ss");
				sDate += "T" + datFormat.format(track.Points.get(i).TimeStamp) + "Z";
				writer.append("   <time>" + sDate + "</time>\n");
				// writer.append("   <course>" + String.valueOf(GlobalCore.Locator.getHeading()) + "</course>\n");
				// writer.append("   <speed>" + String.valueOf(GlobalCore.Locator.SpeedOverGround()) + "</speed>\n");
				writer.append("</trkpt>\n");
			}

			writer.append("</trkseg>\n");
			writer.append("</trk>\n");

			writer.append("</gpx>\n");
			writer.flush();
			writer.close();
		}

		catch (IOException e)
		{
			CB_Core.Log.Logger.Error("SaveTrack", "IOException", e);
		}
		writer = null;

	}

}
