package de.droidcachebox.Map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;



import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Coordinate;
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
		public boolean IsActualTrack = false;
		
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

                if (route.Name == null) //Wenn GPX keinen Namen enthält den Filenamen verwenden
                {
                	int idx = file.lastIndexOf("/");
                	if (idx == -1)
                		route.Name = file;
                	else
                		route.Name = file.substring(idx+1);
                }
                
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
}



