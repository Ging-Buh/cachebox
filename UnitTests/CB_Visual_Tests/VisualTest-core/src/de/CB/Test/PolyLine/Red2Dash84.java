package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;

import com.badlogic.gdx.graphics.Color;

public class Red2Dash84 extends PolylineTestBase
{
	public Red2Dash84()
	{
		super(" PolyLine Test" + br + "Red / DashArray 8,4 / StrokeWidth=2");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.DEFAULT);
		paint.setStrokeWidth(2);
		paint.setDashPathEffect(new float[]
			{ 8, 4 });
	}

}
