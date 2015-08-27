package CB_Core.Map.Route;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;

import CB_Locator.Coordinate;
import CB_Locator.Map.Track;
import CB_Locator.Map.TrackPoint;

import com.badlogic.gdx.graphics.Color;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

public class GraphhopperApi
{

	final static org.slf4j.Logger log = LoggerFactory.getLogger(GraphhopperApi.class);

	private GraphhopperApi()
	{
		// Static only
	}

	public Track getRoute(final Coordinate start, final Coordinate end, File map)
	{

		try
		{
			final GraphHopper hopper = new GraphHopper().forMobile();
			hopper.setInMemory();
			final File mapsforgeFile = map;
			hopper.setOSMFile(mapsforgeFile.getAbsolutePath());
			hopper.setGraphHopperLocation(mapsforgeFile.getParent());
			hopper.setEncodingManager(new EncodingManager("car"));
			hopper.importOrLoad();
			final GHRequest req = new GHRequest(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude()).setVehicle("car");
			final GHResponse rsp = hopper.route(req);
			if (rsp.hasErrors())
			{
				log.error("GHResponse contains errors!");
				List<Throwable> errors = rsp.getErrors();
				for (int i = 0; i < errors.size(); i++)
				{
					log.error("Graphhopper error #" + i, errors.get(i));
				}
				return null;
			}

			final ArrayList<TrackPoint> geoPoints = new ArrayList<TrackPoint>();
			final PointList points = rsp.getPoints();
			double lati, longi, alti;
			for (int i = 0; i < points.getSize(); i++)
			{
				lati = points.getLatitude(i);
				longi = points.getLongitude(i);
				alti = points.getElevation(i);
				geoPoints.add(new TrackPoint(lati, longi, alti, 0.0, new Date()));
			}
			return new Track("generate", Color.RED, geoPoints);

		}
		catch (OutOfMemoryError e)
		{
			log.error("Graphhoper OOM", e);
			return null;
		}
	}

}
