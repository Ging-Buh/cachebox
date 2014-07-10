package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.Join;

import com.badlogic.gdx.graphics.Color;

public class PolyJoin4 extends PolylineTestBase
{
	public PolyJoin4()
	{
		super(" PolyLine Test" + br + "Red / Join.Mitter / StrokeWidth=30");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.BUTT);
		paint.setStrokeWidth(30);

		paint.setStrokeJoin(Join.MITER);

		vertices = new float[]
			{ 10, 10, 60, 60, 110, 10 };

	}

}
