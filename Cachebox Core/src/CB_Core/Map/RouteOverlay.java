package CB_Core.Map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
	public static ArrayList<Trackable> Routes = new ArrayList<Trackable>();
	public static boolean mRoutesChanged = false;

	public static void RoutesChanged()
	{
		mRoutesChanged = true;
	}

	public static class Trackable
	{
		public ArrayList<TrackPoint> Points;
		public String Name;
		public String FileName;
		public boolean ShowRoute = false;
		public boolean IsActualTrack = false;
		public Color mColor;

		public Trackable(String name, Color color)
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
	// in this case all segments was connectet to one track
	public static Trackable LoadRoute(String file, Color color, double minDistanceMeters)
	{
		BufferedReader reader;
		try
		{
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF8");
			reader = new BufferedReader(isr);
			Trackable route = new Trackable(null, color);
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

				if (line.indexOf("<time>") > -1)
				{
					// Time lesen
					int timIdx = line.indexOf("<time>") + 6;
					int timEndIdx = line.indexOf("</time>", timIdx);

					String timStr = line.substring(timIdx, timEndIdx);

					lastAcceptedTime = parseDate(timStr);
				}

				if (line.indexOf("<course>") > -1)
				{
					// Time lesen
					int couIdx = line.indexOf("<course>") + 8;
					int couEndIdx = line.indexOf("</course>", couIdx);

					String couStr = line.substring(couIdx, couEndIdx);

					lastAcceptedDirection = Double.valueOf(couStr);

				}

				if (line.indexOf("</trkpt>") > -1)
				{
					// trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
					route.Points.add(new TrackPoint(lastAcceptedCoordinate.Longitude, lastAcceptedCoordinate.Latitude,
							lastAcceptedDirection, lastAcceptedTime));

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
			calendar.set(Calendar.MONTH, month);
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

			if (GlobalCore.AktuelleRoute != null)
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

	private static void addToDrawRoutes(double tolerance, Trackable track, int Zoom, boolean dontReduce)
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
}
