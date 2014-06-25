package CB_Locator.Map;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Locator.Coordinate;
import CB_Locator.TestCoordinate;

public class Test_TestCoordinate extends TestCase
{

	/**
	 * Maximum possible latitude coordinate.
	 */
	public static final double LATITUDE_MAX = 90;

	/**
	 * Minimum possible latitude coordinate.
	 */
	public static final double LATITUDE_MIN = -LATITUDE_MAX;

	/**
	 * Maximum possible longitude coordinate.
	 */
	public static final double LONGITUDE_MAX = 180;

	/**
	 * Minimum possible longitude coordinate.
	 */
	public static final double LONGITUDE_MIN = -LONGITUDE_MAX;
	private static final double DELTA = 1e-6;

	@Test
	public void testConvertIntToByteToInt()
	{
		// Test all Values are possible for Latitude
		for (double i = LATITUDE_MIN; i < LATITUDE_MAX; i += 0.000001)
		{
			// convert Latitude to Integer
			final int a = TestCoordinate.degreesToMicrodegrees(i);

			final byte[] b = TestCoordinate.getVariableByteSigned(a);
			final int c = TestCoordinate.getSignedInt(b);
			assertEquals("Integer must equals", a, c);

			final Double d = TestCoordinate.microdegreesToDegrees(c);

			assertEquals("Double must equals", i, d, DELTA);
		}

		// Test all Values are possible for Longitude

		for (double i = LONGITUDE_MIN; i < LONGITUDE_MAX; i += 0.000001)
		{
			// convert Latitude to Integer
			final int a = TestCoordinate.degreesToMicrodegrees(i);

			final byte[] b = TestCoordinate.getVariableByteSigned(a);
			final int c = TestCoordinate.getSignedInt(b);
			assertEquals("Integer must equals", a, c);

			final Double d = TestCoordinate.microdegreesToDegrees(c);

			assertEquals("Double must equals", i, d, DELTA);
		}

	}

	@Test
	public void testCoordinateStorrage()
	{
		Coordinate corPankow = new Coordinate("52° 34,982N / 13° 23,540E");
		TestCoordinate coor = new TestCoordinate(corPankow.getLatitude(), corPankow.getLongitude());

		double la = coor.getLatitude();
		double lo = coor.getLongitude();

		assertEquals("Latitude must equals", la, corPankow.getLatitude(), DELTA);
		assertEquals("Longitude must equals", lo, corPankow.getLongitude(), DELTA);
	}

	public static double test;

	@Test
	public void testPerformance()
	{
		long count = 0;

		long time1;
		long time2;

		long start = System.currentTimeMillis();

		double coordStep = 1.45;// 0.0001;

		// performance Coordinate
		for (double x = LATITUDE_MIN; x < LATITUDE_MAX; x += coordStep)
		{
			for (double y = LONGITUDE_MIN; y < LONGITUDE_MAX; y += coordStep)
			{
				// create the Coordinate
				Coordinate t = new Coordinate(x, y);

				// get Values
				double la = t.getLatitude();
				double lo = t.getLongitude();
				count++;
				test = la + lo;
			}
		}

		time1 = System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		// performance TestCoordinate
		for (double x = LATITUDE_MIN; x < LATITUDE_MAX; x += coordStep)
		{
			for (double y = LONGITUDE_MIN; y < LONGITUDE_MAX; y += coordStep)
			{
				// create the Coordinate
				TestCoordinate t = new TestCoordinate(x, y);

				// get Values
				double la = t.getLatitude();
				double lo = t.getLongitude();

				test = la + lo;
			}
		}
		time2 = System.currentTimeMillis() - start;

		System.out.print(count + " Coordinaten getestet");

		assertEquals(time1, time2);
	}

}
