package CB_UI.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import CB_Locator.Coordinate;
import CB_Utils.MathUtils;

public class Astronomy
{
	// Wandelt den übergebenen Zeitpunkt ins Julianische Datum um
	// nach http://www.jgiesen.de/elevaz/basics/meeus.htm#jd
	public static double UtcToJulianDate(Date date)
	{

		Calendar time = Calendar.getInstance();
		time.setTime(date);

		int monat = time.get(Calendar.MONTH);
		int jahr = time.get(Calendar.YEAR);
		int day = time.get(Calendar.DAY_OF_MONTH);
		int hour = time.get(Calendar.HOUR_OF_DAY);
		int minute = time.get(Calendar.MINUTE);
		int second = time.get(Calendar.SECOND);

		if (monat < 2)
		{
			jahr--;
			monat += 12;
		}

		return Math.floor(365.25 * jahr) + Math.floor(30.6001 * (monat + 1.0)) - 15 + 1720996.5 + day + (double) hour / 24.0
				+ (double) minute / 1440 + (double) second / 86400.0;
	}

	public static long getUtcTime(long time)
	{
		System.out.println("Time=" + time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date dbefore = new Date(time);
		System.out.println("Date before conversion=" + format.format(dbefore));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		TimeZone timezone = c.getTimeZone();
		int offset = timezone.getRawOffset();
		if (timezone.inDaylightTime(new Date()))
		{
			offset = offset + timezone.getDSTSavings();
		}
		int offsetHrs = offset / 1000 / 60 / 60;
		int offsetMins = offset / 1000 / 60 % 60;

		System.out.println("offset: " + offsetHrs);
		System.out.println("offset: " + offsetMins);

		c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
		c.add(Calendar.MINUTE, (-offsetMins));

		System.out.println("Date after conversion: " + format.format(c.getTime()));
		System.out.println("Time converted=" + c.getTime().getTime());
		return c.getTime().getTime();

	}

	public static Coordinate EclipticToEquatorial(Coordinate eclipticCoordinate, double julianDate)
	{
		double T = (julianDate - 2451545.0) / 36525.0; // Epoch 2000 January 1.5

		double eps = (23.0 + (26 + 21.45 / 60) / 60 + T * (-46.815 + T * (-0.0006 + T * 0.00181)) / 3600) / 180 * Math.PI; // schiefe der
																															// Ekliptik

		double coseps = Math.cos(eps);
		double sineps = Math.sin(eps);

		double sinlon = Math.sin(eclipticCoordinate.getLongitude() / 180.0 * Math.PI);
		Coordinate equatorial = new Coordinate();
		equatorial.setLongitude((MathUtils.RAD_DEG * Math.atan2(
				(sinlon * coseps - Math.tan(eclipticCoordinate.getLatitude() / 180.0 * Math.PI) * sineps),
				Math.cos(eclipticCoordinate.getLongitude() / 180.0 * Math.PI))) % 360); // rektaszension (alpha)
		equatorial.setLatitude(180.0
				/ Math.PI
				* Math.asin(Math.sin(eclipticCoordinate.getLatitude() / 180.0 * Math.PI) * coseps
						+ Math.cos(eclipticCoordinate.getLatitude() / 180.0 * Math.PI) * sineps * sinlon)); // deklination (delta)

		return equatorial;
	}

	public static Coordinate EquatorialToAzymuth(Coordinate observerPosition, double julianDate, Coordinate equatorialPosition)
	{
		double stunde = ((julianDate + 0.5) % 1) * 24.0;
		double jd0 = julianDate - stunde / 24; // julian date at UTC 0:00
		double t0 = (jd0 - 2451545.0) / 36525.0; // schon in t0 bzw jd0 richtig berechnet?
		double thetaHG = 6.697376 + 2400.05134 * t0 + 1.002738 * stunde; // + (double)minute/60.);
		double theta = thetaHG * 15.0 + observerPosition.getLongitude();
		double tau = (theta - equatorialPosition.getLongitude()) / 180.0 * Math.PI;
		double phi = observerPosition.getLatitude() / 180.0 * Math.PI;
		double azimutNenner = Math.cos(tau) * Math.sin(phi) - Math.tan(equatorialPosition.getLatitude() / 180.0 * Math.PI)
				* Math.cos(observerPosition.getLatitude() / 180.0 * Math.PI);
		float azimut = (float) Math.atan(Math.sin((theta - equatorialPosition.getLongitude()) / 180.0 * Math.PI) / azimutNenner);
		azimut = (float) (azimut * MathUtils.RAD_DEG);
		if (azimutNenner < 0) azimut += 180.0f;
		double h = MathUtils.RAD_DEG
				* Math.asin(Math.cos(equatorialPosition.getLatitude() / 180.0 * Math.PI) * Math.cos(tau) * Math.cos(phi)
						+ Math.sin(equatorialPosition.getLatitude() / 180.0 * Math.PI) * Math.sin(phi));
		// null = Sueden auf Null = Norden umrechnen
		azimut += 180.0f;
		if (azimut > 360.0f) azimut -= 360.0f;
		return new Coordinate(h, azimut);
	}

	public static Coordinate EclipticCoordinatesSun(double julianDate)
	{
		double T = (julianDate - 2451545) / 36525.0;
		double k = MathUtils.DEG_RAD;

		// mean anomaly, degree
		double M = 357.52910 + 35999.05030 * T - 0.0001559 * T * T - 0.00000048 * T * T * T;

		// mean longitude, degree
		double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T * T;
		double DL = (1.914600 - 0.004817 * T - 0.000014 * T * T) * Math.sin(k * M) + (0.019993 - 0.000101 * T) * Math.sin(k * 2 * M)
				+ 0.000290 * Math.sin(k * 3 * M);

		// true longitude, degree
		double L = L0 + DL;

		return new Coordinate(0, L);
	}

	// // the following code is adopted from http://lexikon.astronomie.info/java/sunmoon/sunmoon.html
	// // ignores the time difference between juliandate and TDT, which is something like 1 minute
	public static Coordinate EclipticCoordinatesMoon(double julianDate)
	{
		// final double DEG = MathUtils.DEG_RAD;
		// final double RAD = MathUtils.RAD_DEG;

		double sunAnomalyMean = 360.0 * MathUtils.DEG_RAD / 365.242191 * (julianDate - 2447891.5) + 279.403303 * MathUtils.DEG_RAD
				- 282.768422 * MathUtils.DEG_RAD;
		double D = julianDate - 2447891.5;

		// Mean Moon orbit elements as of 1990
		double l0 = 318.351648 * MathUtils.DEG_RAD;
		double P0 = 36.340410 * MathUtils.DEG_RAD;
		double N0 = 318.510107 * MathUtils.DEG_RAD;
		double i = 5.145396 * MathUtils.DEG_RAD;

		double l = 13.1763966 * MathUtils.DEG_RAD * D + l0;
		double MMoon = l - 0.1114041 * MathUtils.DEG_RAD * D - P0; // Moon's mean anomaly M
		double N = N0 - 0.0529539 * MathUtils.DEG_RAD * D; // Moon's mean ascending node longitude

		double sunlon = EclipticCoordinatesSun(julianDate).getLongitude();
		double C = l - sunlon;
		double Ev = 1.2739 * MathUtils.DEG_RAD * Math.sin(2 * C - MMoon);
		double Ae = 0.1858 * MathUtils.DEG_RAD * Math.sin(sunAnomalyMean);
		double A3 = 0.37 * MathUtils.DEG_RAD * Math.sin(sunAnomalyMean);

		double MMoon2 = MMoon + Ev - Ae - A3; // corrected Moon anomaly
		double Ec = 6.2886 * MathUtils.DEG_RAD * Math.sin(MMoon2); // equation of centre
		double A4 = 0.214 * MathUtils.DEG_RAD * Math.sin(2 * MMoon2);
		double l2 = l + Ev + Ec - Ae + A4; // corrected Moon's longitude
		double V = 0.6583 * MathUtils.DEG_RAD * Math.sin(2 * (l2 - sunlon));

		double l3 = l2 + V; // true orbital longitude;
		double N2 = N - 0.16 * MathUtils.DEG_RAD * Math.sin(sunAnomalyMean);

		Coordinate result = new Coordinate();
		result.setLongitude(((N2 + Math.atan2(Math.sin(l3 - N2) * Math.cos(i), Math.cos(l3 - N2))) * MathUtils.RAD_DEG));
		result.setLatitude(Math.asin(Math.sin(l3 - N2) * Math.sin(i)) * MathUtils.RAD_DEG);
		return result;

		// moonCoor.lonDec = ((N2 + Math.atan2(Math.sin(l3 - N2) * Math.cos(i), Math.cos(l3 - N2))) * RAD) % 360;
		// moonCoor.latDec = Math.asin(Math.sin(l3 - N2) * Math.sin(i)) * RAD;
	}
}
