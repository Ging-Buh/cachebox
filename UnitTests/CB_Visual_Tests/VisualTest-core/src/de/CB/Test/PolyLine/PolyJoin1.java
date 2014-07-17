package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.Join;

import com.badlogic.gdx.graphics.Color;

public class PolyJoin1 extends PolylineTestBase
{
	public PolyJoin1()
	{
		super(" PolyLine Test" + br + "Red / Join.Mitter / StrokeWidth=30");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.BUTT);
		paint.setStrokeWidth(70);

		paint.setStrokeJoin(Join.MITER);

		vertices = new float[]
			{ 30, 30, 140, 100, 220, 30 };

	}

}