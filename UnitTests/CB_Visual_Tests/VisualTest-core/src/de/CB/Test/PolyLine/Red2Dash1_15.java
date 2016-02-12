package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;

import com.badlogic.gdx.graphics.Color;

public class Red2Dash1_15 extends PolylineTestBase {
	public Red2Dash1_15() {
		super(" PolyLine Test" + br + "Red / DashArray 1,15 / StrokeWidth=2");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.DEFAULT);
		paint.setStrokeWidth(10);
		paint.setDashPathEffect(new float[] { 1, 15 });
	}

}
