package de.CB.Test.PolyLine;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;

import com.badlogic.gdx.graphics.Color;

public class Red20Square extends PolylineTestBase {
	public Red20Square() {
		super(" PolyLine Test" + br + "Red / CAP.SQUARE / StrokeWidth=20");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.SQUARE);
		paint.setStrokeWidth(20);
	}

}
