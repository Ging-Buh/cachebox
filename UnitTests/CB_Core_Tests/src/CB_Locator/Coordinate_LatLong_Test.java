package CB_Locator;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;

public class Coordinate_LatLong_Test extends TestCase
{

	@Test
	public void test_Distance()
	{
		Coordinate coord1;
		Coordinate coord2;

		int Tolerance1m = 1;
		int Tolerance3m = 3;
		int Tolerance5m = 5;
		int Tolerance10m = 10;
		int Tolerance50m = 50;

		{// 100m
			coord1 = new Coordinate("52° 33,355N / 13° 24,873E");
			coord2 = new Coordinate("52° 33,301N / 13° 24,873E");

			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 100, Tolerance1m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 100, Tolerance1m);
		}

		{// 100m
			coord1 = new Coordinate("52° 33,301N / 13° 24,873E");
			coord2 = new Coordinate("52° 33,339N / 13° 24,810E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 100, Tolerance1m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 100, Tolerance1m);
		}

		{// 10m
			coord1 = new Coordinate("52° 15,852N / 13° 45,569E");
			coord2 = new Coordinate("52° 15,853N / 13° 45,578E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 10, Tolerance1m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 10, Tolerance1m);
		}

		coord1 = new Coordinate("180° 15,852N / 13° 45,569E");
		assertFalse("Coordinate must be invalid", coord1.Valid);

		{// 50m
			coord1 = new Coordinate("52° 15,852N / 13° 45,569E");
			coord2 = new Coordinate("52° 15,829N / 13° 45,547E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 50, Tolerance1m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 50, Tolerance1m);
		}

		{// 450m
			coord1 = new Coordinate("2° 15,852N / 14° 45,569E");
			coord2 = new Coordinate("2° 15,620N / 14° 45,498E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 450, Tolerance3m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 450, Tolerance5m);
		}

		{// 500m
			coord1 = new Coordinate("42° 15,852N / 42° 15,852E");
			coord2 = new Coordinate("42° 16,117N / 42° 15,915E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 500, Tolerance3m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 500, Tolerance5m);
		}

		{// 5000m
			coord1 = new Coordinate("42° 15,852N / 42° 15,852E");
			coord2 = new Coordinate("42° 17,075N / 42° 19,098E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 5000, Tolerance5m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 5000, Tolerance10m);
		}

		{// 10000m
			coord1 = new Coordinate("42° 15,852N / 42° 15,852E");
			coord2 = new Coordinate("42° 11,330N / 42° 19,814E");
			assertTrue("Coordinate must be Valid", coord1.Valid);
			assertTrue("Coordinate must be Valid", coord2.Valid);
			assertCoordDistance(coord1, coord2, CalculationType.ACCURATE, 10000, Tolerance10m);
			assertCoordDistance(coord1, coord2, CalculationType.FAST, 10000, Tolerance50m);
		}
	}

	private void assertCoordDistance(Coordinate coord1, Coordinate coord2, CalculationType calcType, int exspectedDistance, int tolerance)
	{
		float[] dist = new float[4];
		MathUtils.computeDistanceAndBearing(calcType, coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(),
				coord2.getLongitude(), dist);

		double distance1 = Math.round(coord1.Distance(coord2, calcType));
		double distance2 = Math.round(dist[0]);
		assertEquals("Two ways for calculation distance must be equal", distance1, distance2, tolerance / 1.3d);
		assertEquals("Distance must be " + exspectedDistance + "m. But was " + distance1 + "m", exspectedDistance, distance1, tolerance);

		if (calcType == CalculationType.FAST)
		{
			if ((dist[0] < 300 || dist[0] > 1000) && (dist[0] < 5000 || dist[0] < 10000))
			{
				float[] distAccurate = new float[4];
				MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, coord1.getLatitude(), coord1.getLongitude(),
						coord2.getLatitude(), coord2.getLongitude(), distAccurate);

				String value = UnitFormatter.DistanceStringMetric(distAccurate[0]);
				String valueAccurate = UnitFormatter.DistanceStringMetric(dist[0]);

				assertEquals("Distance Strings must be equals ", value, valueAccurate);
			}
		}

	}

}
