package CB_Core.CB_Core.Map;

import junit.framework.TestCase;
import CB_Core.Types.MeasuredCoord;
import CB_Core.Types.MeasuredCoordList;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Map.Descriptor;
import CB_Utils.Lists.CB_List;
import CB_Utils.Math.PointD;

public class DescriptorTest extends TestCase {
	private MeasuredCoordList mMeasuredCoordList;
	private Descriptor mDescriptor;
	private final int projectionZoom = 40;
	private final int centerX = 100;
	private final int centerY = 100;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mDescriptor = new Descriptor(0, 0, projectionZoom, false);
		mMeasuredCoordList = new MeasuredCoordList();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		mDescriptor = null;
		mMeasuredCoordList = null;
	}

	public void testConstructor() {
		assertTrue("Objekt muss konstruierbar sein", mDescriptor != null);
	}

	public void testDescriptor() {
		Coordinate Referenz = new CoordinateGPS(49.427700, 6.204300);
		assertTrue("Objekt muss konstruierbar sein", Referenz != null);

		MeasuredCoord.Referenz = Referenz;

		// add Messwerte zur Liste
		mMeasuredCoordList.add(new MeasuredCoord(49.427800, 6.204300, 12.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.427810, 6.204300, 12.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.427700, 6.204400, 13.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.427700, 6.204410, 44.0f));

		mMeasuredCoordList.sort();

		// Gemittelter Punkt der GPS-Messungen
		double medianLat = MeasuredCoord.Referenz.getLatitude();
		double medianLon = MeasuredCoord.Referenz.getLongitude();

		double peakLat = Math.max(Math.abs(mMeasuredCoordList.get(0).getLatitude() - medianLat), Math.abs(mMeasuredCoordList.get(mMeasuredCoordList.size() - 1).getLatitude() - medianLat));
		double peakLon = Math.max(Math.abs(mMeasuredCoordList.get(0).getLongitude() - medianLon), Math.abs(mMeasuredCoordList.get(mMeasuredCoordList.size() - 1).getLongitude() - medianLon));

		// Umrechnung in XY
		double medianX = Descriptor.LongitudeToTileX(projectionZoom, medianLon);
		double medianY = Descriptor.LatitudeToTileY(projectionZoom, medianLat);

		double extremeX = Descriptor.LongitudeToTileX(projectionZoom, peakLon + medianLon);
		double extremeY = Descriptor.LatitudeToTileY(projectionZoom, peakLat + medianLat);

		double peakX = Math.abs(extremeX - medianX);
		double peakY = Math.abs(extremeY - medianY);

		double maxPeak = Math.max(peakX, peakY);
		int minPix = Math.min(200, 200);

		double factor = (maxPeak > 0) ? (double) minPix / maxPeak : 1;

		int sumX = 0;
		int sumY = 0;
		for (int i = 1; i < mMeasuredCoordList.size(); i++) {

			PointD lastDrawEntry = Descriptor.projectCoordinate(mMeasuredCoordList.get(i - 1).getLatitude(), mMeasuredCoordList.get(i - 1).getLongitude(), projectionZoom);

			int lastX = (int) (centerX + (lastDrawEntry.X - medianX) * factor);
			int lastY = (int) (centerY - (lastDrawEntry.Y - medianY) * factor);

			PointD thisDrawEntry = Descriptor.projectCoordinate(mMeasuredCoordList.get(i).getLatitude(), mMeasuredCoordList.get(i).getLongitude(), projectionZoom);

			int x = (int) (centerX + (thisDrawEntry.X - medianX) * factor);
			int y = (int) (centerY - (thisDrawEntry.Y - medianY) * factor);

			sumX += lastX + x;
			sumY += lastY + y;

		}

		assertTrue("Die Summe der X punkte sollte  sein", sumX == 842);
		assertTrue("Die Summe der Y punkte sollte  sein", sumY == 249);
	}

	public void testAdjustDescriptor() {
		Descriptor def = new Descriptor(4400, 2684, 13, true);
		Descriptor def12 = new Descriptor(2200, 1342, 12, true);
		CB_List<Descriptor> adjustDown = def.AdjustZoom(12);
		assertTrue("return must only one Descriptor", adjustDown.size() == 1);
		assertEquals("must Equals!", adjustDown.get(0), def12);

		CB_List<Descriptor> adjustUpList = new CB_List<Descriptor>();
		adjustUpList.add(new Descriptor(8800, 5368, 14, true));
		adjustUpList.add(new Descriptor(8800, 5369, 14, true));
		adjustUpList.add(new Descriptor(8801, 5368, 14, true));
		adjustUpList.add(new Descriptor(8801, 5369, 14, true));
		CB_List<Descriptor> adjustUp = def.AdjustZoom(14);
		assertTrue("return must only four Descriptor", adjustUp.size() == 4);
		for (int i = 0; i < 4; i++) {
			assertTrue("Descriptor must inside the list", adjustUp.contains(adjustUpList.get(i)));
		}

	}
}
