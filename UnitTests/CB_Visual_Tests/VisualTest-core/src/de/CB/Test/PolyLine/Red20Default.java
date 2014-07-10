package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;

import com.badlogic.gdx.graphics.Color;

public class Red20Default extends PolylineTestBase
{
	public Red20Default()
	{
		super(" PolyLine Test" + br + "Red / CAP.DEFAULT / StrokeWidth=20");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.DEFAULT);
		paint.setStrokeWidth(20);

	}

}
