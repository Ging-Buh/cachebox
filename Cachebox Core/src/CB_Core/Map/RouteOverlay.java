package CB_Core.Map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class RouteOverlay
{

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

	}

	public static ArrayList<Trackable> Routes = new ArrayList<Trackable>();

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

	public static void RenderRoute(SpriteBatch batch, int Zoom) // , Descriptor desc, float dpiScaleFactorX, float dpiScaleFactorY)
	{
		// double tileX = desc.X * 256 * dpiScaleFactorX;
		// double tileY = desc.Y * 256 * dpiScaleFactorY;
		ArrayList<PointD> points = new ArrayList<PointD>();

		for (int i = 0; i < Routes.size(); i++)
		{
			// int lastX = -999;
			// int lastY = -999;
			// bool lastIn = true;
			// bool aktIn = false;

			if (Routes.get(i).ShowRoute)
			{
				Sprite ArrowSprite = SpriteCache.Arrows.get(5);
				ArrowSprite.setColor(Routes.get(i).mColor);

				int step = Routes.get(i).Points.size() / 1200;
				if (step < 1) step = 1;

				step = 1;

				for (int j = 0; j < (Routes.get(i).Points.size()); j = j + step)
				{

					points.add(new PointD(Routes.get(i).Points.get(j).X, Routes.get(i).Points.get(j).Y));

				}

				for (int ii = 0; ii < points.size() - 1; ii++)
				{

					double MapX1 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, points.get(ii).X);
					double MapY1 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, points.get(ii).Y);

					double MapX2 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, points.get(ii + 1).X);
					double MapY2 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, points.get(ii + 1).Y);

					Vector2 screen1 = MapView.that.worldToScreen(new Vector2((float) MapX1, (float) MapY1));
					Vector2 screen2 = MapView.that.worldToScreen(new Vector2((float) MapX2, (float) MapY2));

					DrawUtils.drawSpriteLine(batch, ArrowSprite, screen1.x, screen1.y, screen2.x, screen2.y);
				}

				points.clear();
			}
		}
	}
}
