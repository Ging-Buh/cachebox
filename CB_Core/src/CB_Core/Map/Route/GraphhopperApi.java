package CB_Core.Map.Route;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;

import CB_Core.Settings.CB_Core_Settings;
import CB_Locator.Coordinate;
import CB_Locator.Map.Track;
import CB_Locator.Map.TrackPoint;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.CopyHelper.Copy;
import CB_Utils.Util.CopyHelper.CopyRule;

import com.badlogic.gdx.graphics.Color;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.PointList;

public class GraphhopperApi
{

	final static org.slf4j.Logger log = LoggerFactory.getLogger(GraphhopperApi.class);

	private GraphhopperApi()
	{
		// Static only
	}

	public final static String INITIAL_OK = "initializes";
	public final static String NO_DATA_FILE = "no_data_file";
	public final static String IO_EXEPTION = "io_exeption";

	private static boolean rootDataInitializes = false;
	private static String lastData = "";
	private static String workFilePath = "";

	public static String initalRootData()
	{
		if (rootDataInitializes)// check changes
		{
			if (lastData.equals(CB_Core_Settings.GraphhopperActFile)) return INITIAL_OK;
		}

		// check if RootDataFile exist
		if (!FileIO.FileExistsNotEmpty(CB_Core_Settings.GraphhopperFolder.getValue() + "/" + CB_Core_Settings.GraphhopperActFile.getValue()))
		{
			return NO_DATA_FILE;
		}

		// check if Temp folder exist

		String tempExtractedFolderPath = CB_Core_Settings.GraphhopperTemp.getValue() + "/" + FileIO.GetFileNameWithoutExtension(CB_Core_Settings.GraphhopperActFile.getValue());

		if (FileIO.DirectoryExists(tempExtractedFolderPath))
		{
			// delete folder, maybe the data file is new
			FileIO.deleteDir(new File(tempExtractedFolderPath));
		}

		// copy File
		String data = CB_Core_Settings.GraphhopperFolder.getValue() + "/" + CB_Core_Settings.GraphhopperActFile.getValue();

		CopyRule rule = new CopyRule(data, CB_Core_Settings.GraphhopperTemp.getValue());
		Copy copyHelper = new Copy(rule);
		try
		{
			copyHelper.Run();
		}
		catch (IOException e)
		{
			return IO_EXEPTION;
		}

		workFilePath = CB_Core_Settings.GraphhopperTemp.getValue() + "/" + FileIO.GetFileNameWithoutExtension(CB_Core_Settings.GraphhopperActFile.getValue());

		rootDataInitializes = true;
		return INITIAL_OK;
	}

	public static Track getRoute(final Coordinate start, final Coordinate end)
	{

		if (!rootDataInitializes) return null;

		try
		{
			final GraphHopper hopper = new GraphHopper().forMobile();
			hopper.setInMemory();

			// hopper.setOSMFile(mapsforgeFile.getAbsolutePath());
			hopper.setGraphHopperLocation(workFilePath);
			hopper.setEncodingManager(new EncodingManager("car"));
			hopper.init(new CmdArgs());
			hopper.setGraphHopperLocation(workFilePath);
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

			hopper.close();

			return new Track("generate", Color.RED, geoPoints);

		}
		catch (OutOfMemoryError e)
		{
			log.error("Graphhoper OOM", e);
			return null;
		}
	}

}
