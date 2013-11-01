/*
 * CB_Math.cpp
 *
 *  Created on: 01.11.2013
 *      Author: Longri
 */

#include <iostream>
#include <cstdlib>
#include <cmath>
#include <sys/timeb.h>
#include "CB_Math.h"



const float PI = 3.14159265358979323846;
const float ToDegrees = 180.0f / PI;
const float ToRadians = PI / 180.0f;

void computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2, float results[])
{
	// Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
			// using the "Inverse Formula" (section 4)

			int length = sizeof(results);

			int MAXITERS = 3;
			// Convert lat/long to radians
			lat1 *= ToRadians;
			lat2 *= ToRadians;
			lon1 *= ToRadians;
			lon2 *= ToRadians;

			double a = 6378137.0; // WGS84 major axis
			double b = 6356752.3142; // WGS84 semi-major axis
			double f = (a - b) / a;
			double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

			double L = lon2 - lon1;
			double A = 0.0;
			double U1 = atan((1.0 - f) * tan(lat1));
			double U2 = atan((1.0 - f) * tan(lat2));


			double cosU1,sinU1,cosU2,sinU2;
			SinCos(U1 ,sinU1,cosU1);
			SinCos(U2 ,sinU2,cosU2);

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

				SinCos(lambda,sinLambda,cosLambda);

				double t1 = cosU2 * sinLambda;
				double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
				double sinSqSigma = t1 * t1 + t2 * t2; // (14)
				sinSigma = rSqrt(sinSqSigma);
				cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
				sigma = atan2(sinSigma, cosSigma); // (16)
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
				if (abs(delta) < 1.0e-12)
				{
					break;
				}
			}

			float distance = (float) (b * A * (sigma - deltaSigma));
			results[0] = distance;
			if (length > 1)
			{
				float initialBearing = (float) atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
				initialBearing *= ToDegrees;
				results[1] = initialBearing;
				if (length > 2)
				{
					float finalBearing = (float) atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
					finalBearing *= ToDegrees;
					results[2] = finalBearing;
				}
			}
}

 void __fastcall SinCos(double angle,double & s,double & c)
{

	    asm ("fsincos" : "=t" (c), "=u" (s) : "0" (angle));
}


 double __fastcall Sin(double angle)
{
		double s=0;
		asm ("fsin" : "=t" (s) : "0" (angle));
		return s;
}

 double __fastcall Cos(double angle)
{
		double s=0;
		asm ("fcos" : "=t" (s) : "0" (angle));
		return s;
}

 double __fastcall rSqrt(double n)
{
	double r=0;

	asm ("fsqrt" : "=t" (r) : "0" (n));

  return r;
}


