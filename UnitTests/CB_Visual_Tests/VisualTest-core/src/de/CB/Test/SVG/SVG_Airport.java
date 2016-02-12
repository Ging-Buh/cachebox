package de.CB.Test.SVG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import CB_Utils.Plattform;

public class SVG_Airport extends SVG_TestBase {

	public SVG_Airport() {
		super("SVG-Airport");
	}

	@Override
	public InputStream get_SVG_InputStream() throws FileNotFoundException {

		String path = "assets/svg/osm/museum.svg";

		if (Plattform.used == Plattform.Android) {
			path = "storage/extSdCard/freizeitkarte/svg/osm/airport.svg";
		} else {
			path = "assets/svg/osm/airport.svg";
		}

		InputStream stream = new FileInputStream(path);

		return stream;
	}

}
