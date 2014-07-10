package de.CB.Test.SVG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import CB_Utils.Plattform;


public class SVG_Postbox extends SVG_TestBase
{

	public SVG_Postbox()
	{
		super("SVG-Airport");
	}

	@Override
	public InputStream get_SVG_InputStream() throws FileNotFoundException
	{

		String path = "assets/svg/osm/postbox.svg";

		if (Plattform.used == Plattform.Android)
		{
			path = "storage/extSdCard/freizeitkarte/svg/osm/postbox.svg";
		}
		else
		{
			path = "assets/svg/osm/postbox.svg";
		}

		File test = new File(path);
		InputStream stream = new FileInputStream(path);

		return stream;
	}

}
