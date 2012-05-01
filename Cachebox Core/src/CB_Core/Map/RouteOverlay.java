package CB_Core.Map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor.PointD;
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
		public ArrayList<PointD> Points;
		public String Name;
		public String FileName;
		public boolean ShowRoute = false;
		public boolean IsActualTrack = false;
		public Color mColor;

		public Trackable(String name, Color color)
		{
			Points = new ArrayList<PointD>();
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
							/*
							 * if (lastAcceptedCoordinate != null) if (Datum.WGS84.Distance(lat, lon, lastAcceptedCoordinate.Latitude,
							 * lastAcceptedCoordinate.Longitude) < minDistanceMeters) continue;
							 */
							lastAcceptedCoordinate = new Coordinate(lat, lon);

							// PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, lon),
							// Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, lat));
							//
							// route.Points.add(projectedPoint);

							route.Points.add(new PointD(lon, lat));

						}
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

	public static int AllTrackPoints = 0;
	public static int ReduceTrackPoints = 0;
	public static int DrawedLineCount = 0;
	public static double Tolleranz = 0;

	public static int aktCalcedZoomLevel = -1;

	public class Route
	{
		private Color mColor;
		protected ArrayList<PointD> Points;

		public Route(Color color)
		{
			mColor = color;
			Points = new ArrayList<PointD>();
		}
	}

	private static ArrayList<Route> DrawRoutes;

	public static void RenderRoute(SpriteBatch batch, int Zoom) // , Descriptor desc, float dpiScaleFactorX, float dpiScaleFactorY)
	{

		if (aktCalcedZoomLevel != Zoom || mRoutesChanged)
		{// Zoom or Routes changed => calculate new Sprites Points

			Logger.LogCat("Zoom Changed => Calc Track Points");

			mRoutesChanged = false;
			aktCalcedZoomLevel = Zoom;
			DrawRoutes = new ArrayList<RouteOverlay.Route>();
			double tolerance = 0.01 * Math.exp(-1 * (Zoom - 11));
			Tolleranz = tolerance;

			for (int i = 0; i < Routes.size(); i++)
			{

				if (Routes.get(i) != null && Routes.get(i).ShowRoute)
				{
					addToDrawRoutes(tolerance, Routes.get(i));
				}
			}

			if (GlobalCore.AktuelleRoute != null)
			{
				addToDrawRoutes(tolerance, GlobalCore.AktuelleRoute);
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
					if (chkRec.contains(screen1.x, screen1.y) || chkRec.contains(screen1.x, screen1.y))
					{
						DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, 0.7f, screen1.x, screen1.y, screen2.x, screen2.y);
						DrawedLineCount++;
					}
					else
					{// chk if intersection
						if (chkRec.getIntersection(screen1, screen2, 2) != null)
						{
							DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, 0.7f, screen1.x, screen1.y, screen2.x, screen2.y);
							DrawedLineCount++;
						}

						// the line is not on the screen
					}

				}

			}
		}
	}

	private static void addToDrawRoutes(double tolerance, Trackable track)
	{
		ArrayList<PointD> reducedPoints = PolylineReduction.DouglasPeuckerReduction(track.Points, tolerance);

		AllTrackPoints = track.Points.size();
		ReduceTrackPoints = reducedPoints.size();

		Route tmp = (new RouteOverlay()).new Route(track.mColor);
		tmp.Points = reducedPoints;

		DrawRoutes.add(tmp);
	}
}
