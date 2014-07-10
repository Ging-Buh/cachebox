package de.CB.TestBase.Actions;

import java.util.ArrayList;

import de.CB.Test.SVG.SVG_fromFileName;
import de.CB.Test.SVG.SVG_with_PNG;

public class Multi_SVG_Test extends ArrayList<TestCaseBase>
{

	private static final long serialVersionUID = 1L;

	String[] NameListWithPNG = new String[]
		{"matrixTrans", "strokes", "pathArc", "transformations",  "cubicPath", "matrix", "gradients", "shapes" };

	String[] NameList = new String[]
		{ "atm", "cafe", "bakery", "airport", "alpine_hut", "bank", "bench", "bicycle_rental", "bus", "bus_sta", "campSite" };

	Multi_SVG_Test()
	{

		for (String s : NameListWithPNG)
		{
			this.add(new SVG_with_PNG(s));
		}

		for (String s : NameList)
		{
			this.add(new SVG_fromFileName(s));
		}
	}

}
