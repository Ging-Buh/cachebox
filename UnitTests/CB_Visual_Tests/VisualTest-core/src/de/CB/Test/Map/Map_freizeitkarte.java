package de.CB.Test.Map;

import org.mapsforge.core.model.Tile;

public class Map_freizeitkarte extends MapTileTestBase {

	private static byte zoom = 16;
	private static int x = 35207;
	private static int y = 21477;

	private static int count = 0;

	public Map_freizeitkarte(boolean deleteTheme) {
		super(new Tile(x--, y--, zoom), deleteTheme);
		if (count++ > 3) {
			count = 0;

			zoom = 16;
			x = 35207;
			y = 21477;

		}

		ThemeString = "freizeitkarte/freizeitkarte.xml";

		// ThemeString = "default/assets.xml";
		//
		// ThemeString = "default/assetssvg.xml";

		// ThemeString = "fre.xml";
	}
}
