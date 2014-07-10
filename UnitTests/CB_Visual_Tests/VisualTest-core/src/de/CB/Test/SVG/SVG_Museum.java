package de.CB.Test.SVG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import CB_Utils.Plattform;


public class SVG_Museum extends SVG_TestBase
{

	public SVG_Museum()
	{
		super("SVG-Museum");
	}

	@Override
	public InputStream get_SVG_InputStream() throws FileNotFoundException
	{

		String path = "assets/svg/osm/museum.svg";

		if (Plattform.used == Plattform.Android)
		{
			path = "storage/extSdCard/freizeitkarte/svg/osm/museum.svg";
		}
		else
		{
			path = "assets/svg/osm/museum.svg";
		}

		InputStream stream = new FileInputStream(path);

		return stream;
	}

}
