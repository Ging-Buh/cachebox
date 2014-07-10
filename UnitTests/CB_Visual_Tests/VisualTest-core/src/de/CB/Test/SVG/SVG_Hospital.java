package de.CB.Test.SVG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import CB_Utils.Plattform;



public class SVG_Hospital extends SVG_TestBase
{

	public SVG_Hospital()
	{
		super("SVG-Airport");
	}

	@Override
	public InputStream get_SVG_InputStream() throws FileNotFoundException
	{

		String path = "assets/svg/osm/museum.svg";

		if (Plattform.used == Plattform.Android)
		{
			path = "storage/extSdCard/GL_RENDER_TEST/themes/default/symbols/hospital.svg";
		}
		else
		{
			path = "assets/themes/default/symbols/hospital.svg";
		}

		InputStream stream = new FileInputStream(path);

		return stream;
	}

}
