package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;

import com.badlogic.gdx.graphics.Color;

public class Red20Round extends PolylineTestBase
{
	public Red20Round()
	{
		super(" PolyLine Test" + br + "Red / CAP.ROUND / StrokeWidth=20");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.ROUND);
		paint.setStrokeWidth(20);

	}

}
