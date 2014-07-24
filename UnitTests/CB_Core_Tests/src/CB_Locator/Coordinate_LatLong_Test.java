package CB_Locator;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Locator.Map.Descriptor;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;

public class Coordinate_LatLong_Test extends TestCase
{
	private static final double DELTA = 1e-6;

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

	public void testStringConstructors()
	{
		String[] strings = new String[]
			{ "52° 33,355N / 13° 24,873E", "N52° 33,355 / E13° 24,873", "N 52° 33,355 / E 13° 24,873", "N 52° 33.355 / E 13° 24.873",
					"52° 33,355n / 13° 24,873e", "n52° 33,355 / e13° 24,873", "n 52° 33,355 / e 13° 24,873", "n 52° 33.355 / e 13° 24.873",
					"52° 33,355N / 13° 024,873E", "N52° 33,355 / E013° 24,873", "N 52° 33,355 / E 013° 24,873",
					"S 52° 33.355 / W 013° 24.873", "52° 33,355S / 13° 24,873W", "S52° 33,355 / W13° 24,873",
					"S 52° 33,355 / W 13° 24,873", "S 52° 33.355 / W 13° 24.873", "52° 33,355S / 13° 24,873W", "S52° 33,355 / W13° 24,873",
					"S 52° 33,355 / W 13° 24,873", "S 52° 33.355 / W 13° 24.873", "52° 33,355S / 13° 024,873W",
					"S52° 33,355 / W013° 24,873", "S 52° 33,355 / W 013° 24,873", "S 52° 33.355 / W 013° 24.873" };

		// ExpectedCoords are Integer (microdegrees Double*1e6)
		Coordinate[] expectedCoord = new Coordinate[]
			{ new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550),
					new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550),
					new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550),
					new Coordinate(52555916, 13414550), new Coordinate(52555916, 13414550), new Coordinate(-52555916, -13414550),
					new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550),
					new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550),
					new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550),
					new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550), new Coordinate(-52555916, -13414550) };

		for (int i = 0; i < strings.length; i++)
		{
			Coordinate cor = new Coordinate(strings[i]);
			assertTrue("Coordinate {" + cor.toString() + "} must be valid", cor.isValid());
			assertEquals("Coordinate Parser error with String \"" + strings[i] + "\"", expectedCoord[i], cor);
		}

	}

	public void testProjection()
	{

		// ############ check projection Problem with South coordinates ########
		// Point A S01° 16.841' E036° 41.470' All notations
		// Angle 90°
		// Distance 1000 m
		// Projection P S01° 16.841' E036° 42.009'
		// #####################################################################

		Coordinate A = new Coordinate("S01° 16.841' E036° 41.470'");
		Coordinate P = new Coordinate("S01° 16.841' E036° 42.009'");

		Coordinate p = Coordinate.Project(A, 90, 1000);

		assertEquals("Projected Coordinate must be right", P, p);

		// #####################################################################
		// Point A N52° 33.355' E013° 24.873'
		// Angle 213°
		// Distance 580 m
		// Projection P N52° 33.093' E013° 24.594'

		A = new Coordinate("N52° 33.355' E013° 24.873'");
		P = new Coordinate("N52° 33.093' E013° 24.593'");

		p = Coordinate.Project(A, 213, 580);

		assertEquals("Projected Coordinate must be right", P, p);

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

	@Test
	public void testCoordinateEquals()
	{
		Coordinate corPankow = new Coordinate("52° 34,9815N / 13° 23,540E");
		Descriptor desc = new Descriptor(corPankow, 14);
		Coordinate coor = desc.getCenterCoordinate();

		double la = coor.getLatitude();
		double lo = coor.getLongitude();

		assertEquals("Latitude must equals", la, corPankow.getLatitude(), DELTA);
		assertEquals("Longitude must equals", lo, corPankow.getLongitude(), DELTA);
		assertEquals("Coords must equals", coor, corPankow);
	}

}
