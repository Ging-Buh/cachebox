package CB_Utils;

import junit.framework.TestCase;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Utils.MathUtils.CalculationType;

public class computeDistanceAndBearing extends TestCase {
	Coordinate position = new CoordinateGPS("52� 31,386N / 13� 28,800E");
	Coordinate Final[] = new Coordinate[] { new CoordinateGPS("52� 34,218N / 13� 23,634E"), new CoordinateGPS("52� 32,958N / 13� 24,816E"), new CoordinateGPS("52� 35,472N / 13� 21,540E"), new CoordinateGPS("52� 31,398N / 13� 23,004E"),
			new CoordinateGPS("52� 31,236N / 13� 24,570E") };

	CalculationType ACCURATE = CalculationType.ACCURATE;
	CalculationType FAST = CalculationType.FAST;

	public void test_computeDistanceAndBearing() {
		for (Coordinate cor : Final) {
			compute(cor);
		}

	}

	void compute(Coordinate finalCoord) {
		float result[] = new float[4];
		MathUtils.computeDistanceAndBearing(ACCURATE, position.getLatitude(), position.getLongitude(), finalCoord.getLatitude(), finalCoord.getLongitude(), result);

		float result2[] = new float[4];
		MathUtils.computeDistanceAndBearing(FAST, position.getLatitude(), position.getLongitude(), finalCoord.getLatitude(), finalCoord.getLongitude(), result2);

		// 1% Abweichung
		double maxabweichung = result[0] / 100;

		double div = result[0] - result2[0];
		if (div < 0)
			div *= -1;
		assertFalse("Distance Abweichung zwichen ACCURATE und FAST zu Gro�", div > maxabweichung);

		double BearingDiv = result[1] - result2[1];
		if (BearingDiv < 0)
			BearingDiv *= -1;
		assertFalse("Bearing Abweichung zwichen ACCURATE und FAST zu Gro�", BearingDiv > 0.1f);

	}
}
