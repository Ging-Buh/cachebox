package CB_UI.Map;

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

import CB_Locator.CoordinateGPS;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.MapTileLoader;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.GL_UI.DrawUtils;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.HSV_Color;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.PolylineReduction;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Log.Logger;
import CB_Utils.Math.TrackPoint;
import CB_Utils.Util.FileIO;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class RouteOverlay
{
	/**
	 * ist in Routes eine von openRouteService generierter Track enthalten, dann enthällt diese Vatiable diesen track.
	 */
	private static Track OpenRoute;

	private static ArrayList<Track> Routes = new ArrayList<Track>();
	public static boolean mRoutesChanged = false;

	public static Color getNextColor()
	{
		Color ret = ColorField[(Routes.size()) % ColorField.length];
		if (ret == null) initialColorField();
		return ColorField[(Routes.size()) % ColorField.length];
	}

	private static Color[] ColorField = new Color[13];

	private static void initialColorField()
	{
		ColorField[0] = Color.RED;
		ColorField[1] = Color.YELLOW;
		ColorField[2] = Color.BLACK;
		ColorField[3] = Color.LIGHT_GRAY;
		ColorField[4] = Color.GREEN;
		ColorField[5] = Color.BLUE;
		ColorField[6] = Color.CYAN;
		ColorField[7] = Color.GRAY;
		ColorField[8] = Color.MAGENTA;
		ColorField[9] = Color.ORANGE;
		ColorField[10] = Color.DARK_GRAY;
		ColorField[11] = Color.PINK;
		ColorField[12] = Color.WHITE;
	}

	public static void RoutesChanged()
	{
		mRoutesChanged = true;
		GL.that.renderOnce();
	}

	public static class Track
	{
		public ArrayList<TrackPoint> Points;
		public String Name;
		public String FileName;
		public boolean ShowRoute = false;
		public boolean IsActualTrack = false;
		private Color mColor;
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
	public static Track MultiLoadRoute(String file, Color color)
	{
		float[] dist = new float[4];
		double Distance = 0;
		double AltitudeDifference = 0;
		double DeltaAltitude = 0;
		CoordinateGPS FromPosition = new CoordinateGPS(0, 0);
		BufferedReader reader;

		try
		{
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF8");
			reader = new BufferedReader(isr);
			Track route = new Track(null, color);

			String line = null;
			String tmpLine = null;
			String GPXName = null;
			boolean isSeg = false;
			boolean isTrk = false;
			boolean isRte = false;
			boolean IStrkptORrtept = false;
			boolean ReadName = false;
			int AnzTracks = 0;

			CoordinateGPS lastAcceptedCoordinate = null;
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
						tmpLine = sb.toString();
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
								Distance = 0;
								AltitudeDifference = 0;
								AnzTracks++;
								if (GPXName == null) route.Name = FileIO.GetFileName(file);
								else
								{
									if (AnzTracks <= 1) route.Name = GPXName;
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
								Distance = 0;
								AltitudeDifference = 0;
								AnzTracks++;
								if (GPXName != null) route.Name = FileIO.GetFileName(file);
								else
								{
									if (AnzTracks <= 1) route.Name = GPXName;
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
							int cdata_start = 0;
							int name_start = 0;
							int name_end;

							name_end = line.indexOf("</name>");

							// Name contains cdata?
							cdata_start = line.indexOf("[cdata[");
							if (cdata_start > -1)
							{
								name_start = cdata_start + 7;
								name_end = line.indexOf("]");
							}

							if (name_end > name_start)
							{
								// tmpLine, damit Groß-/Kleinschreibung beachtet wird
								if (isSeg | isRte) route.Name = tmpLine.substring(name_start, name_end);
								else
									GPXName = tmpLine.substring(name_start, name_end);
							}

							ReadName = false;
							continue;
						}

						if (line.indexOf("</trkseg>") > -1) // End of the Track Segment detected?
						{
							if (route.Points.size() < 2) route.Name = "no Route segment found";
							route.ShowRoute = true;
							route.TrackLength = Distance;
							route.AltitudeDifference = AltitudeDifference;
							add(route);
							isSeg = false;
							break;
						}

						if (line.indexOf("</rte>") > -1) // End of the Route detected?
						{
							if (route.Points.size() < 2) route.Name = "no Route segment found";
							route.ShowRoute = true;
							route.TrackLength = Distance;
							route.AltitudeDifference = AltitudeDifference;
							add(route);
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

							lastAcceptedCoordinate = new CoordinateGPS(lat, lon);
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
							// Course lesen
							int couIdx = line.indexOf("<course>") + 8;
							if (couIdx == 7) couIdx = 0;
							int couEndIdx = line.indexOf("</course>", couIdx);

							String couStr = line.substring(couIdx, couEndIdx);

							lastAcceptedDirection = Double.valueOf(couStr);

						}

						if ((line.indexOf("</ele>") > -1) & IStrkptORrtept)
						{
							// Elevation lesen
							int couIdx = line.indexOf("<ele>") + 5;
							if (couIdx == 4) couIdx = 0;
							int couEndIdx = line.indexOf("</ele>", couIdx);

							String couStr = line.substring(couIdx, couEndIdx);

							lastAcceptedCoordinate.setElevation(Double.valueOf(couStr));

						}

						if (line.indexOf("</gpxx:colorrgb>") > -1)
						{
							// Color lesen
							int couIdx = line.indexOf("<gpxx:colorrgb>") + 15;
							if (couIdx == 14) couIdx = 0;
							int couEndIdx = line.indexOf("</gpxx:colorrgb>", couIdx);

							String couStr = line.substring(couIdx, couEndIdx);
							color = new HSV_Color(couStr);
							route.setColor(color);
						}

						if ((line.indexOf("</trkpt>") > -1) | (line.indexOf("</rtept>") > -1)
								| ((line.indexOf("/>") > -1) & IStrkptORrtept))
						{
							// trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
							IStrkptORrtept = false;
							route.Points.add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(),
									lastAcceptedCoordinate.getElevation(), lastAcceptedDirection, lastAcceptedTime));

							// Calculate the length of a Track
							if (!FromPosition.isValid())
							{
								FromPosition = new CoordinateGPS(lastAcceptedCoordinate);
								FromPosition.setElevation(lastAcceptedCoordinate.getElevation());
								FromPosition.setValid(true);
							}
							else
							{
								MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, FromPosition.getLatitude(),
										FromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(),
										lastAcceptedCoordinate.getLongitude(), dist);
								Distance += dist[0];
								DeltaAltitude = Math.abs(FromPosition.getElevation() - lastAcceptedCoordinate.getElevation());
								FromPosition = new CoordinateGPS(lastAcceptedCoordinate);

								if (DeltaAltitude >= 25.0) // nur aufaddieren wenn Höhenunterschied größer 10 Meter
								{
									FromPosition.setElevation(lastAcceptedCoordinate.getElevation());
									AltitudeDifference = AltitudeDifference + DeltaAltitude;
								}
							}
						}
					}
				}
			}
			reader.close();
			return route;
		}

		catch (FileNotFoundException e)
		{

			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{

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

	// Debug
	// public static int AllTrackPoints = 0;
	// public static int ReduceTrackPoints = 0;
	// public static int DrawedLineCount = 0;

	public static double Tolleranz = 0;
	public static int aktCalcedZoomLevel = -1;

	public class Route
	{
		private boolean mIsOpenRoute = false;
		private Color mColor;
		protected ArrayList<TrackPoint> Points;

		Sprite ArrowSprite;
		Sprite PointSprite;
		float overlap = 0.9f;

		public Route(Color color)
		{
			mColor = color;
			Points = new ArrayList<TrackPoint>();
			ArrowSprite = SpriteCacheBase.Arrows.get(5);
			PointSprite = SpriteCacheBase.Arrows.get(10);
			overlap = 0.9f;
		}

		public Route(Color color, boolean isOpenRoute)
		{
			mIsOpenRoute = isOpenRoute;
			if (isOpenRoute)
			{
				ArrowSprite = new Sprite(SpriteCacheBase.Arrows.get(5));
				PointSprite = new Sprite(SpriteCacheBase.Arrows.get(10));
				ArrowSprite.scale(1.6f);
				PointSprite.scale(0.2f);
				overlap = 1.9f;
			}
			else
			{
				ArrowSprite = SpriteCacheBase.Arrows.get(5);
				PointSprite = SpriteCacheBase.Arrows.get(10);
				overlap = 0.9f;
			}
			mColor = color;
			Points = new ArrayList<TrackPoint>();
		}

		public boolean isOpenRoute()
		{
			return mIsOpenRoute;
		}

	}

	private static ArrayList<Route> DrawRoutes;

	public static void RenderRoute(Batch batch, int Zoom, float yVersatz) // , Descriptor desc, float dpiScaleFactorX, float
																			// dpiScaleFactorY)
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

		// DrawedLineCount = 0;

		if (DrawRoutes != null && DrawRoutes.size() > 0)
		{
			for (Route rt : DrawRoutes)
			{

				Sprite ArrowSprite = rt.ArrowSprite;
				Sprite PointSprite = rt.PointSprite;
				float overlap = rt.overlap;
				ArrowSprite.setColor(rt.mColor);
				PointSprite.setColor(rt.mColor);
				float scale = UI_Size_Base.that.getScale();

				for (int ii = 0; ii < rt.Points.size() - 1; ii++)
				{

					double MapX1 = 256.0 * Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, rt.Points.get(ii).X);
					double MapY1 = -256.0 * Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, rt.Points.get(ii).Y);

					double MapX2 = 256.0 * Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, rt.Points.get(ii + 1).X);
					double MapY2 = -256.0 * Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, rt.Points.get(ii + 1).Y);

					Vector2 screen1 = MapView.that.worldToScreen(new Vector2((float) MapX1, (float) MapY1));
					Vector2 screen2 = MapView.that.worldToScreen(new Vector2((float) MapX2, (float) MapY2));

					screen1.y -= yVersatz;
					screen2.y -= yVersatz;

					CB_RectF chkRec = new CB_RectF(MapView.that);
					chkRec.setPos(0, 0);

					// chk if line on Screen
					if (chkRec.contains(screen1.x, screen1.y) || chkRec.contains(screen2.x, screen2.y))
					{
						DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, overlap * scale, screen1.x, screen1.y, screen2.x,
								screen2.y);
						// DrawedLineCount++;
					}
					else
					{// chk if intersection
						if (chkRec.getIntersection(screen1, screen2, 2) != null)
						{
							DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, overlap * scale, screen1.x, screen1.y, screen2.x,
									screen2.y);
							// DrawedLineCount++;
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

			// AllTrackPoints = track.Points.size();
			// ReduceTrackPoints = reducedPoints.size();

			boolean isOpenRoute = track == OpenRoute;

			Route tmp = (new RouteOverlay()).new Route(track.mColor, isOpenRoute);
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

				writer.append("<trk>\n");
				writer.append("<name>" + track.Name + "</name>\n");
				writer.append("<extensions>\n<gpxx:TrackExtension>\n");
				writer.append("<gpxx:ColorRGB>" + track.mColor.toString() + "</gpxx:ColorRGB>\n");
				writer.append("</gpxx:TrackExtension>\n</extensions>\n");
				writer.append("<trkseg>\n");
				writer.flush();
			}
			catch (IOException e)
			{
				CB_Utils.Log.Logger.Error("SaveTrack", "IOException", e);
			}
		}
		catch (IOException e1)
		{
			CB_Utils.Log.Logger.Error("SaveTrack", "IOException", e1);
		}

		try
		{
			for (int i = 0; i < track.Points.size(); i++)
			{
				writer.append("<trkpt lat=\"" + String.valueOf(track.Points.get(i).Y) + "\" lon=\"" + String.valueOf(track.Points.get(i).X)
						+ "\">\n");

				writer.append("   <ele>" + String.valueOf(String.valueOf(track.Points.get(i).Elevation)) + "</ele>\n");
				SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
				String sDate = datFormat.format(track.Points.get(i).TimeStamp);
				datFormat = new SimpleDateFormat("HH:mm:ss");
				sDate += "T" + datFormat.format(track.Points.get(i).TimeStamp) + "Z";
				writer.append("   <time>" + sDate + "</time>\n");
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
			CB_Utils.Log.Logger.Error("SaveTrack", "IOException", e);
		}
		writer = null;
	}

	public void LoadTrack(String trackPath)
	{
		LoadTrack(trackPath, "");
	}

	public static void LoadTrack(String trackPath, String file)
	{

		String absolutPath = "";
		if (file.equals(""))
		{
			absolutPath = trackPath;
		}
		else
		{
			absolutPath = trackPath + "/" + file;
		}
		MultiLoadRoute(absolutPath, getNextColor());
	}

	public static void remove(Track route)
	{
		if (route == OpenRoute)
		{
			OpenRoute = null;
		}
		Routes.remove(route);
		RoutesChanged();
	}

	/**
	 * Dont use this for OpenRoute Track!! Use addOpenRoute(Track route)
	 * 
	 * @param route
	 */
	public static void add(Track route)
	{
		Routes.add(route);
		RoutesChanged();
	}

	public static void addOpenRoute(Track route)
	{
		if (OpenRoute == null)
		{
			route.setColor(new Color(0.85f, 0.1f, 0.2f, 1f));
			Routes.add(0, route);
			OpenRoute = route;
		}
		else
		{
			// erst die alte route löschen
			Routes.remove(OpenRoute);
			route.setColor(OpenRoute.getColor());
			Routes.add(0, route);
			OpenRoute = route;
		}

		RoutesChanged();
	}

	public static int getRouteCount()
	{
		return Routes.size();
	}

	public static Track getRoute(int position)
	{
		return Routes.get(position);
	}

}
