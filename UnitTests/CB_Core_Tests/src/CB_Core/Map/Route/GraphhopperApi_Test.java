package CB_Core.Map.Route;

import junit.framework.TestCase;
import CB_Locator.Coordinate;
import CB_Locator.Map.Track;
import CB_UI.Config;
import __Static.InitTestDBs;

public class GraphhopperApi_Test extends TestCase {

    public void test_offline() {

	// Graphhopper must return initialization error

	assertEquals("must return initialisation error", GraphhopperApi.initalRootData(), GraphhopperApi.NO_DATA_FILE);

	InitTestDBs.InitalConfig();

	// set test path settings
	Config.GraphhopperFolder.setValue("./testdata/graphhopper");
	Config.GraphhopperTemp.setValue("./testdata/graphhopper_temp");
	Config.GraphhopperActFile.setValue("berlin.ghz");
	Config.AcceptChanges();

	assertEquals("must return initialisation OK", GraphhopperApi.initalRootData(), GraphhopperApi.INITIAL_OK);

	Coordinate START = new Coordinate(52.581337, 13.398803);
	Coordinate TARGET = new Coordinate(52.453303, 13.549619);

	Track track = GraphhopperApi.getRoute(START, TARGET);

	//Check Track
	System.out.print(track.toString());
	assertEquals("Track length must be 20990.663893699646 m", track.TrackLength, 20990.663893699646);

    }

}
