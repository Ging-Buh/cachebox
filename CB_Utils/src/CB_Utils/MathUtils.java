package CB_Utils;

public class MathUtils
{
	/**
	 * precalculation of Math.PI / 180.0
	 */
	static public final double DEG_RAD = Math.PI / 180.0;

	/**
	 * precalculation of 180.0 / Math.PI
	 */
	static public final double RAD_DEG = 180.0 / Math.PI;

	/**
	 * precalculation of Math.PI * 2
	 */
	static public final double PI2 = Math.PI * 2;

	/**
	 * precalculation of Math.PI / 2
	 */
	static public final double HALF_PI = Math.PI / 2;

	/**
	 * WGS84 major axis = 6378137.0
	 */
	static public final double WGS84_MAJOR_AXIS = 6378137.0; // WGS84 major axis

	/**
	 * WGS84 semi-major axis = 6356752.3142
	 */
	static public final double WGS84_SEMI_MAJOR_AXIS = 6356752.3142; // WGS84 semi-major axis

	/*
	 * Calculation of Cos/Sin/Atan over LockUpTable
	 */

	// static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
	// static private final int SIN_MASK = ~(-1 << SIN_BITS);
	// static private final int SIN_COUNT = SIN_MASK + 1;
	//
	// static private final double degFull = 360;
	// static private final double radToIndex = SIN_COUNT / PI2;
	// static private final double degToIndex = SIN_COUNT / degFull;
	//
	// static private class Sin
	// {
	// static final double[] table = new double[SIN_COUNT];
	// static
	// {
	// for (int i = 0; i < SIN_COUNT; i++)
	// table[i] = Math.sin((i + 0.5f) / SIN_COUNT * PI2);
	// for (int i = 0; i < 360; i += 90)
	// table[(int) (i * degToIndex) & SIN_MASK] = Math.sin(i * DEG_RAD);
	// }
	// }
	//
	// /** Returns the sine in radians from a lookup table. */
	// static public final double TableSin(double radians)
	// {
	// return Sin.table[(int) (radians * radToIndex) & SIN_MASK];
	// }
	//
	// /** Returns the cosine in radians from a lookup table. */
	// static public final double TableCos(double radians)
	// {
	// return Sin.table[(int) ((radians + HALF_PI) * radToIndex) & SIN_MASK];
	// }
	//
	// static private final int ATAN2_BITS = 7; // Adjust for accuracy.
	// static private final int ATAN2_BITS2 = ATAN2_BITS << 1;
	// static private final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	// static private final int ATAN2_COUNT = ATAN2_MASK + 1;
	// static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
	// static private final double INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
	//
	// static private class Atan2
	// {
	// static final double[] table = new double[ATAN2_COUNT];
	// static
	// {
	// for (int i = 0; i < ATAN2_DIM; i++)
	// {
	// for (int j = 0; j < ATAN2_DIM; j++)
	// {
	// double x0 = i / ATAN2_DIM;
	// double y0 = j / ATAN2_DIM;
	// table[j * ATAN2_DIM + i] = Math.atan2(y0, x0);
	// }
	// }
	// }
	// }
	//
	// /** Returns atan2 in radians from a lookup table. */
	// static public final double TableAtan2(double d, double e)
	// {
	// double add, mul;
	// if (e < 0)
	// {
	// if (d < 0)
	// {
	// d = -d;
	// mul = 1;
	// }
	// else
	// mul = -1;
	// e = -e;
	// add = (float) -Math.PI;
	// }
	// else
	// {
	// if (d < 0)
	// {
	// d = -d;
	// mul = -1;
	// }
	// else
	// mul = 1;
	// add = 0;
	// }
	// double invDiv = (1 / ((e < d ? d : e) * INV_ATAN2_DIM_MINUS_1));
	//
	// if (invDiv == Float.POSITIVE_INFINITY) return (Math.atan2(d, e) + add) * mul;
	//
	// int xi = (int) (e * invDiv);
	// int yi = (int) (d * invDiv);
	// return (Atan2.table[yi * ATAN2_DIM + xi] + add) * mul;
	// }

	/**
	 * Computes the approximate distance in meters between two locations, and optionally the initial and final bearings of the shortest path
	 * between them. Distance and bearing are defined using the WGS84 ellipsoid.
	 * <p>
	 * The computed distance is stored in results[0]. If results has length 2 or greater, the initial bearing is stored in results[1]. If
	 * results has length 3 or greater, the final bearing is stored in results[2].
	 * 
	 * @param startLatitude
	 *            the starting latitude
	 * @param startLongitude
	 *            the starting longitude
	 * @param endLatitude
	 *            the ending latitude
	 * @param endLongitude
	 *            the ending longitude
	 * @param results
	 *            an array of floats to hold the results
	 * @throws IllegalArgumentException
	 *             if results is null or has length < 1
	 */
	public static void computeDistanceAndBearing(CalculationType type, double startLatitude, double startLongitude, double endLatitude,
			double endLongitude, float[] results)
	{
		if (results == null || results.length < 1)
		{
			throw new IllegalArgumentException("results is null or has length < 1");
		}

		switch (type)
		{
		case ACCURATE:
			computeDistanceAndBearingAccurate(startLatitude, startLongitude, endLatitude, endLongitude, results);
			break;
		case FAST:
			computeDistanceAndBearingFast(startLatitude, startLongitude, endLatitude, endLongitude, results);
			break;
		}
	}

	public enum CalculationType
	{
		FAST, ACCURATE
	}

	/**
	 * Fast calculation with Cos/Sin/Atan over LockUpTable
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param results
	 */
	private static void computeDistanceAndBearingFast(double lat1, double lon1, double lat2, double lon2, float[] results)
	{

		lat1 *= DEG_RAD;
		lon1 *= DEG_RAD;
		lat2 *= DEG_RAD;
		lon2 *= DEG_RAD;

		results[0] = (float) ((WGS84_MAJOR_AXIS) * Math.acos(Math.sin((float) lat1) * Math.sin((float) lat2) + Math.cos((float) lat1)
				* Math.cos((float) lat2) * Math.cos((float) (lon2 - lon1))));

		if (results.length > 1)
		{
			double dlat = deg2rad(lat1) - deg2rad(lat2);
			double dlon = deg2rad(lon1) - deg2rad(lon2);
			double y = Math.sin(dlon) * Math.cos(lat2);
			double x = Math.cos(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) - Math.sin(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
					* Math.cos(dlon);
			double direct = rad2deg(Math.atan2(y, x));
			if (direct < 0) direct = direct + 360;

			results[1] = (float) (direct);
			if (results.length > 2)
			{
				results[2] = results[1];
			}
		}
	}

	private static double deg2rad(double deg)
	{
		return (deg * DEG_RAD);
	}

	private static double rad2deg(double rad)
	{
		return (rad * RAD_DEG);
	}

	private static void computeDistanceAndBearingAccurate(double lat1, double lon1, double lat2, double lon2, float[] results)
	{
		// Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
		// using the "Inverse Formula" (section 4)

		int MAXITERS = 20;
		// Convert lat/long to radians
		lat1 *= MathUtils.DEG_RAD;
		lat2 *= MathUtils.DEG_RAD;
		lon1 *= MathUtils.DEG_RAD;
		lon2 *= MathUtils.DEG_RAD;

		double f = (WGS84_MAJOR_AXIS - WGS84_SEMI_MAJOR_AXIS) / WGS84_MAJOR_AXIS;
		double aSqMinusBSqOverBSq = (WGS84_MAJOR_AXIS * WGS84_MAJOR_AXIS - WGS84_SEMI_MAJOR_AXIS * WGS84_SEMI_MAJOR_AXIS)
				/ (WGS84_SEMI_MAJOR_AXIS * WGS84_SEMI_MAJOR_AXIS);

		double L = lon2 - lon1;
		double A = 0.0;
		double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
		double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

		double cosU1 = Math.cos(U1);
		double cosU2 = Math.cos(U2);
		double sinU1 = Math.sin(U1);
		double sinU2 = Math.sin(U2);
		double cosU1cosU2 = cosU1 * cosU2;
		double sinU1sinU2 = sinU1 * sinU2;

		double sigma = 0.0;
		double deltaSigma = 0.0;
		double cosSqAlpha = 0.0;
		double cos2SM = 0.0;
		double cosSigma = 0.0;
		double sinSigma = 0.0;
		double cosLambda = 0.0;
		double sinLambda = 0.0;

		double lambda = L; // initial guess
		for (int iter = 0; iter < MAXITERS; iter++)
		{
			double lambdaOrig = lambda;
			cosLambda = Math.cos(lambda);
			sinLambda = Math.sin(lambda);
			double t1 = cosU2 * sinLambda;
			double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
			double sinSqSigma = t1 * t1 + t2 * t2; // (14)
			sinSigma = Math.sqrt(sinSqSigma);
			cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
			sigma = Math.atan2(sinSigma, cosSigma); // (16)
			double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda / sinSigma; // (17)
			cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
			cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

			double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
			A = 1 + (uSquared / 16384.0) * // (3)
					(4096.0 + uSquared * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
			double B = (uSquared / 1024.0) * // (4)
					(256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
			double C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
			double cos2SMSq = cos2SM * cos2SM;
			deltaSigma = B
					* sinSigma
					* // (6)
					(cos2SM + (B / 4.0)
							* (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0) * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma)
									* (-3.0 + 4.0 * cos2SMSq)));

			lambda = L + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SM + C * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

			double delta = (lambda - lambdaOrig) / lambda;
			if (Math.abs(delta) < 1.0e-12)
			{
				break;
			}
		}

		float distance = (float) (WGS84_SEMI_MAJOR_AXIS * A * (sigma - deltaSigma));
		results[0] = distance;
		if (results.length > 1)
		{
			float initialBearing = (float) Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
			initialBearing *= MathUtils.RAD_DEG;
			results[1] = initialBearing;
			if (results.length > 2)
			{
				float finalBearing = (float) Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
				finalBearing *= MathUtils.RAD_DEG;
				results[2] = finalBearing;
			}
		}
	}

}
