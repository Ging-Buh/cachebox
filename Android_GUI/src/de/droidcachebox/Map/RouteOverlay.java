package de.droidcachebox.Map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Map.Descriptor.PointD;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class RouteOverlay {

	public final static int projectionZoomLevel = 15;

	public static class Route
	{
		public Paint paint;
		public ArrayList<PointD> Points;
		public String Name;
		public String FileName;
		public boolean ShowRoute = false;
		
		public Route(Paint paint, String name)
		{
			this.paint = paint;
			Points = new ArrayList<PointD>();
			Name = name;
		}
		
	}

	public static ArrayList<Route> Routes = new ArrayList<Route>();

        // Read track from gpx file
        // attention it is possible that a gpx file contains more than 1 <trk> segments
        // in this case all segments was connectet to one track
        public static Route LoadRoute(String file, Paint paint, double minDistanceMeters)
        {
       	    BufferedReader reader;
           	try {
           		InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF8");
           		reader = new BufferedReader(isr);
                Route route = new Route(paint, null);
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
                	for (int i = 0; i< rline.length(); i++)
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
                                    inTrk = true;

                                continue;
                            }
                            else
                            {
                                // found <name>?
                                if (line.indexOf("<name>") > -1)
                                {
                                    ReadName = true;
                                    continue;
                                }
                            }


                            if (!inBody)
                            {
                                // Anfang der Trackpoints gefunden?
                                if (line.indexOf("<trkseg>") > -1)
                                    inBody = true;

                                continue;
                            }

                            // Ende gefunden?
                            if (line.indexOf("</trkseg>") > 0)
                                break;

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
                                if (lastAcceptedCoordinate != null)
                                    if (Datum.WGS84.Distance(lat, lon, lastAcceptedCoordinate.Latitude, lastAcceptedCoordinate.Longitude) < minDistanceMeters)
                                        continue;
*/
                                lastAcceptedCoordinate = new Coordinate(lat, lon);

                                PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, lon),
                                    Descriptor.LatitudeToTileY(projectionZoomLevel, lat));

                                route.Points.add(projectedPoint);
                            }
                        }
                	}
                }

                reader.close();
                if (route.Points.size() < 2)
                    route.Name = "no Route segment found";

                route.ShowRoute = true;

                return route;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
        	
        	
/*
            try
            {
                BinaryReader reader = new BinaryReader(File.Open(file, FileMode.Open));
                
                Route route = new Route(pen, null);
                route.FileName = Path.GetFileName(file);
                
                long length = reader.BaseStream.Length;
                
                String line = null;
                bool inBody = false;
                bool inTrk = false;
                bool ReadName = false;

                Coordinate lastAcceptedCoordinate = null;

                StringBuilder sb = new StringBuilder();
                while (reader.BaseStream.Position < length)
                {

                    char nextChar = reader.ReadChar();
                    sb.Append(nextChar);

                    if (nextChar == '>')
                    {
                        line = sb.ToString().Trim().ToLower();
                        sb = new StringBuilder();

                        // Read Routename form gpx file
                        // attention it is possible that a gpx file contains more than 1 <trk> segments
                        // In this case the first name was used
                        if (ReadName && (route.Name == null))
                        {
                            route.Name = line.Substring(0,line.IndexOf("</name>"));
                            ReadName = false;
                            continue;
                        }

                        if (!inTrk)
                        {
                            // Begin of the Track detected?
                            if (line.IndexOf("<trk>") > -1)
                                inTrk = true;

                            continue;
                        }
                        else
                        {
                            // found <name>?
                            if (line.IndexOf("<name>") > -1)
                            {
                                ReadName = true;
                                continue;
                            }
                        }


                        if (!inBody)
                        {
                            // Anfang der Trackpoints gefunden?
                            if (line.IndexOf("<trkseg>") > -1)
                                inBody = true;

                            continue;
                        }

                        // Ende gefunden?
                        if (line.IndexOf("</trkseg>") > 0)
                            break;

                        if (line.IndexOf("<trkpt") > -1)
                        {
                            // Trackpoint lesen
                            int lonIdx = line.IndexOf("lon=\"") + 5;
                            int latIdx = line.IndexOf("lat=\"") + 5;

                            int lonEndIdx = line.IndexOf("\"", lonIdx);
                            int latEndIdx = line.IndexOf("\"", latIdx);

                            String latStr = line.Substring(latIdx, latEndIdx - latIdx);
                            String lonStr = line.Substring(lonIdx, lonEndIdx - lonIdx);

                            double lat = double.Parse(latStr, NumberFormatInfo.InvariantInfo);
                            double lon = double.Parse(lonStr, NumberFormatInfo.InvariantInfo);

                            if (lastAcceptedCoordinate != null)
                                if (Datum.WGS84.Distance(lat, lon, lastAcceptedCoordinate.Latitude, lastAcceptedCoordinate.Longitude) < minDistanceMeters)
                                    continue;

                            lastAcceptedCoordinate = new Coordinate(lat, lon);

                            PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, lon),
                                Descriptor.LatitudeToTileY(projectionZoomLevel, lat));

                            route.Points.Add(projectedPoint);
                        }
                    }
                }

                reader.Close();
                if (route.Points.Count < 2)
                    route.Name = "no Route segment found";

                route.ShowRoute = true;

                return route;
            }

            catch (Exception exc)
            {
#if DEBUG
                Global.AddLog("RouteOverlay.LoadRoute: " + exc.ToString());
#endif
                MessageBox.Show(exc.ToString(), "Error", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, MessageBoxDefaultButton.Button1);
                return null;
            }
*/
        }

        public static void RenderRoute(Canvas canvas, Bitmap bitmap, Descriptor desc, float dpiScaleFactorX, float dpiScaleFactorY)
        {
            double tileX = desc.X * 256 * dpiScaleFactorX;
            double tileY = desc.Y * 256 * dpiScaleFactorY;
            ArrayList<Point> points = new ArrayList<Point>();

            for (int i = 0; i < Routes.size(); i++)
            {
                //int lastX = -999;
                //int lastY = -999;
                //bool lastIn = true;
                //bool aktIn = false;

                if (Routes.get(i).ShowRoute)
                {
                    Paint paint = Routes.get(i).paint;

                    double adjustmentX = Math.pow(2, desc.Zoom - projectionZoomLevel) * 256 * dpiScaleFactorX;
                    double adjustmentY = Math.pow(2, desc.Zoom - projectionZoomLevel) * 256 * dpiScaleFactorY;

                    int step = Routes.get(i).Points.size() / 600;
                    if (step < 1)
                        step = 1;

                    for (int j = 0; j < (Routes.get(i).Points.size()); j = j + step)
                    {
                        int x1 = (int)(Routes.get(i).Points.get(j).X * adjustmentX - tileX);
                        int y1 = (int)(Routes.get(i).Points.get(j).Y * adjustmentY - tileY);

                        //if (Routes[i].Points.Count > 360)
                        //{
                        //    aktIn = (x1 >= -bitmap.Width) && (x1 <= bitmap.Width * 2) && (y1 >= -bitmap.Height) && (y1 <= bitmap.Height * 2);

                        //    if (aktIn)
                        //    {
                        //        if (!lastIn)  // wenn letzter Punkt nicht innerhalb war -> trotzdem hinzuf?gen, damit die Linie vollst?ndig wird
                        //            points.Add(new Point((int)lastX, (int)lastY));
                        //        if ((x1 != lastX) || (y1 != lastY))
                        //            points.Add(new Point(x1, y1));
                        //    }
                        //    else
                        //    {
                        //        if (lastIn)
                        //        {
                        //            // wenn der letzte Punkt noch sichtbar war, aktuellen Punkt hinzuf?gen, obwohl er ausserhalb ist, damit die Linie abgeschlossen wird
                        //            points.Add(new Point(x1, y1));
                        //            // Linienzug zeichnen
                        //            graphics.DrawLines(pen, points.ToArray());
                        //            points.Clear();
                        //        }
                        //    }

                        //    lastX = x1;
                        //    lastY = y1;
                        //    lastIn = aktIn;
                        //}
                        //else
                        {
                            points.add(new Point(x1, y1));
                        }
                    }
                    // letzte Punkte bis zum aktuellen noch zeichnen
  /*                  float[] ppp = new float[points.size() * 2];
                    int i1 = 0;
                    for (Point pp : points)
                    {
                    	ppp[i1] = pp.x;
                    	i1++;
                    	ppp[i1] = pp.y;
                    	i1++;
                    }
                    canvas.drawLines(ppp, paint);
*/
                    for (int ii = 0; ii < points.size() - 1; ii++)
                    {
                    	Point pp = points.get(ii);
                    	Point ppp = points.get(ii+1);
                    	canvas.drawLine(pp.x, pp.y, ppp.x, ppp.y, paint);
                    }
                  
                    points.clear();
                }
            }
        }
/*
        public Route GenP2PRoute(double FromLat, double FromLon, double ToLat, double ToLon, Pen pen)
        {
            Route route = new Route(pen, null);

            route.Name = "Point 2 Point Route";

            PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, FromLon),
                Descriptor.LatitudeToTileY(projectionZoomLevel, FromLat));
            route.Points.Add(projectedPoint);
            projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, ToLon),
                Descriptor.LatitudeToTileY(projectionZoomLevel, ToLat));
            route.Points.Add(projectedPoint);

            route.ShowRoute = true;

            return route;
        }

        public Route GenCircleRoute(double FromLat, double FromLon, double Distance, Pen pen)
        {
            Route route = new Route(pen, null);

            route.Name = "Circle Route";

            Coordinate GEOPosition = new Coordinate();
            GEOPosition.Latitude = FromLat;
            GEOPosition.Longitude = FromLon;

            Coordinate Projektion = new Coordinate();

            for (int i = 0; i <= 360; i++)
            {
                Projektion = Coordinate.Project(GEOPosition.Latitude, GEOPosition.Longitude, (double)i, Distance);

                PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, Projektion.Longitude),
                                            Descriptor.LatitudeToTileY(projectionZoomLevel, Projektion.Latitude));
                route.Points.Add(projectedPoint);

            }

            route.ShowRoute = true;

            return route;
        }

        public Route GenProjectRoute(double FromLat, double FromLon, double Distance, double Bearing, Pen pen)
        {
            Route route = new Route(pen, null);

            route.Name = "Projected Route";

            Coordinate GEOPosition = new Coordinate();
            GEOPosition.Latitude = FromLat;
            GEOPosition.Longitude = FromLon;
            PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, GEOPosition.Longitude),
                                        Descriptor.LatitudeToTileY(projectionZoomLevel, GEOPosition.Latitude));
            route.Points.Add(projectedPoint);

            Coordinate Projektion = new Coordinate();

            Projektion = Coordinate.Project(GEOPosition.Latitude, GEOPosition.Longitude, Bearing, Distance);

            projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, Projektion.Longitude),
                                        Descriptor.LatitudeToTileY(projectionZoomLevel, Projektion.Latitude));
            route.Points.Add(projectedPoint);
            route.ShowRoute = true;

            return route;
        }
*/
}



