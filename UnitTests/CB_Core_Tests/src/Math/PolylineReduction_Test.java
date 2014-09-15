package Math;

import junit.framework.TestCase;
import CB_Locator.Map.PolylineReduction;
import CB_Utils.Math.PointD;

public class PolylineReduction_Test extends TestCase
{
	public void test_PerpendicularDistance()
	{

		PointD point1 = new PointD(0.0, 0.0);
		PointD point2 = new PointD(0.0, 0.0);
		PointD point3 = new PointD(0.0, 0.0);

		Double distance = 0.0;

		distance = PolylineReduction.PerpendicularDistance(point1, point2, point3);
		assertEquals(0.0, distance);

		point3 = new PointD(1.0, 0.0);
		distance = PolylineReduction.PerpendicularDistance(point1, point2, point3);
		assertEquals(0.0, distance);

		point2 = new PointD(1.0, 0.0);
		distance = PolylineReduction.PerpendicularDistance(point1, point2, point3);
		assertEquals(0.0, distance);

		point3 = new PointD(1.0, 0.0);
		distance = PolylineReduction.PerpendicularDistance(point1, point2, point3);
		assertEquals(0.0, distance);

	}
}
