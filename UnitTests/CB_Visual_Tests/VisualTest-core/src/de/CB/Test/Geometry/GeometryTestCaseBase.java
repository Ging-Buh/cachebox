package de.CB.Test.Geometry;

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.CB.TestBase.Actions.TestCaseBase;

public abstract class GeometryTestCaseBase extends TestCaseBase {
	public GeometryTestCaseBase(String TestMsg) {
		super(TestMsg, TestMsg + br + "Scale 1:2");

	}

	PolygonDrawable polygon;
	protected float[] vertices;
	protected short[] triangles;
	protected GL_Paint paint;
	protected boolean isReady = false;

	@Override
	public boolean getIsReady() {
		return isReady;
	}

	@Override
	public void work() {
		paint = new GL_Paint();
		paint.setColor(new Color(0.8f, 0.1f, 0.1f, 1f));
		polygon = new PolygonDrawable(vertices, triangles, paint, 256, 256);

	}

	@Override
	public void draw(Batch batch) {
		if (polygon != null) {
			polygon.draw(batch, firstPoint.x, firstPoint.y, 256, 256, 0);
			polygon.draw(batch, secondPoint.x, secondPoint.y, 512, 512, 0);
			isReady = true;
		}
	}

	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void dispose() {
		synchronized (isDisposed) {
			if (isDisposed.get())
				return;
			// TODO Dispose
			isDisposed.set(true);
		}
	}
}
