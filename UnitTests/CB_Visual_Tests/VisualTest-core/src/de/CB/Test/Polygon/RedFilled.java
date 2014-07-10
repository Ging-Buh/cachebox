package de.CB.Test.Polygon;

import CB_UI_Base.graphics.GL_Style;

import com.badlogic.gdx.graphics.Color;

public class RedFilled extends PolygonTestBase
{
	public RedFilled()
	{
		super(" Polygone Test Red filled" + br + "Mapsforge", "Cachebox");
		paint.setColor(Color.RED);
		paint.setStyle(GL_Style.FILL);
	}

}
