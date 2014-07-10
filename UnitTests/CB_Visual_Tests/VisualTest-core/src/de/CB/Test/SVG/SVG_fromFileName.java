package de.CB.Test.SVG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import CB_Utils.Plattform;



public class SVG_fromFileName extends SVG_TestBase
{
	private final String Name;

	public SVG_fromFileName(String Name)
	{
		super("SVG-" + Name);
		this.Name = Name;
	}

	@Override
	public InputStream get_SVG_InputStream() throws FileNotFoundException
	{

		String path;

		if (Plattform.used == Plattform.Android)
		{
			path = "storage/extSdCard/freizeitkarte/svg/osm/" + Name + ".svg";
		}
		else
		{
			path = "assets/svg/osm/" + Name + ".svg";
		}

		InputStream stream = new FileInputStream(path);

		return stream;
	}

}
