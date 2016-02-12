package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.Join;

import com.badlogic.gdx.graphics.Color;

public class PolyJoin2 extends PolylineTestBase {
	public PolyJoin2() {
		super(" PolyLine Test" + br + "Red / Join.Mitter / StrokeWidth=30");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		// paint.setAlpha(128);
		paint.setCap(GL_Cap.BUTT);
		paint.setStrokeWidth(30);

		paint.setStrokeJoin(Join.ROUND);

		vertices = new float[] { 30, 30, 100, 200, 170, 30, 30, 30 };

	}

}
