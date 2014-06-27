package ___Test_Suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import CB_Core.Config_Crypt;
import CB_Core.FilterProperties_Test;
import CB_Core.HSV_Test;
import CB_Core.MeasuredCoordTest;
import CB_Core.CB_Core.Export.GPX_Export;
import CB_Core.CB_Core.Map.DescriptorTest;
import CB_Core.Converter.Base64_Test;
import CB_Locator.Coordinate_LatLong_Test;
import CB_Locator.Map.MapTileCache_Test;
import CB_Utils.computeDistanceAndBearing;
import Math.Cb_RectF_Test;
import Math.PolylineReduction_Test;
import Types.CacheTest;
import Types.MeasuredCoordListTest;

@RunWith(Suite.class)
@SuiteClasses(
	{ Config_Crypt.class, FilterProperties_Test.class, HSV_Test.class, MeasuredCoordTest.class, Import_tests.class, DescriptorTest.class,
			Base64_Test.class, MapTileCache_Test.class, computeDistanceAndBearing.class, Cb_RectF_Test.class, PolylineReduction_Test.class,
			CacheTest.class, MeasuredCoordListTest.class, GPX_Export.class, Coordinate_LatLong_Test.class })
public class Offline_tests
{

}
