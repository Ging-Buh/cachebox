//============================================================================
// Name        : CoordinateMathCPP.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>

#include <cmath>
#include <sys/timeb.h>
#include "CB_Math.h"
using namespace std;



int getMilliCount(){
	timeb tb;
	ftime(&tb);
	int nCount = tb.millitm + (tb.time & 0xfffff) * 1000;
	return nCount;
}

int getMilliSpan(int nTimeStart){
	int nSpan = getMilliCount() - nTimeStart;
	if(nSpan < 0)
		nSpan += 0x100000 * 1000;
	return nSpan;
}





int main() {
	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!

	double lat1=49.428333;
	double lon1=6.203333;
	double lat2=49.427700;
	double lon2=6.204300;
	float results[]={0.0f,0.0f,0.0f,0.0f};

	cout << "\nStarting timer..."<< endl;
		int start = getMilliCount();

		// CODE YOU WANT TO TIME
		for(int i = 0; i < 1000000; i++)
		{
			computeDistanceAndBearing(lat1, lon1, lat2, lon2, results);
		}

		int milliSecondsElapsed = getMilliSpan(start);

		cout <<"\nElapsed time =";
		cout << milliSecondsElapsed << endl;



	cout << results[0] << endl;
	cout << results[1] << endl;
	cout << results[2] << endl;
	cout << results[3] << endl;

	cout <<""<< endl;
	cout << "Sinus"<< endl;
	cout <<sin(17.123453)<< endl;
	cout << Sin(17.123453) << endl;
	double SIN = 0.0f;
	double COS = 0.0f;
	SinCos( 17.123453, SIN,COS);
	cout <<SIN<< endl;


	cout <<""<< endl;
	cout << "Cosinus"<< endl;
	cout << cos(17.123453) << endl;
	cout << Cos(17.123453) << endl;
	cout << COS << endl;



	cout <<""<< endl;
	cout << "Wurzel"<< endl;
	cout <<sqrt(17.123453)<< endl;
	cout <<rSqrt(17.123453)<< endl;

	return 0;
}







