package de.CB.Test.Polygon;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Cap;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.Style;

import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.EarClippingTriangulator;

import de.CB.TestBase.Actions.TestCaseBase;

public abstract class PolygonTestBase extends TestCaseBase
{
	protected float[] vertices;
	protected GL_Paint paint = new GL_Paint();
	protected final static EarClippingTriangulator ECT = new EarClippingTriangulator();

	protected PolygonDrawable polygon;

	public PolygonTestBase(String TopTestMsg, String BotomTestMsg)
	{
		super(TopTestMsg, BotomTestMsg);
		vertices = new float[]
			{ 10, 10, 100, 50, 50, 100, 50, 150, 200, 200, 200, 10, 10, 10 };
		
		vertices = new float[]
				{0.0390625f, 0.0390625f, 0.390625f, 0.1953125f, 0.1953125f, 0.390625f, 0.1953125f, 0.5859375f, 0.78125f, 0.78125f, 0.78125f, 0.0390625f, 0.0390625f, 0.0390625f };
	}

	@Override
	public void work()
	{
		Color color = paint.getHSV_Color();

		polygon = new PolygonDrawable(vertices, ECT.computeTriangles(vertices).toArray(), paint, 256, 256);

		GraphicFactory factory = Mapsforge_Factory;
		bmp = factory.createBitmap(256, 256);
		Canvas canvas = factory.createCanvas();
		canvas.setBitmap(bmp);

		Path path = factory.createPath();

		// Flip Y (256-y)
		path.moveTo(vertices[0], 256 - vertices[1]);

		for (int i = 2; i < vertices.length - 1; i += 2)
		{
			path.lineTo(vertices[i], 256 - vertices[i + 1]);
		}

		Paint MF_Paint = factory.createPaint();
		MF_Paint.setStrokeWidth(paint.getStrokeWidth());

		switch (paint.getStyle())
		{
		case FILL:
			MF_Paint.setStyle(Style.FILL);
			break;
		case STROKE:
			MF_Paint.setStyle(Style.STROKE);
			break;
		default:
			break;

		}

		MF_Paint.setColor(factory.createColor((int) (color.a * 255), (int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255)));

		switch (paint.getCap())
		{
		case BUTT:
			MF_Paint.setStrokeCap(Cap.BUTT);
		case ROUND:
			MF_Paint.setStrokeCap(Cap.ROUND);
		case SQUARE:
			MF_Paint.setStrokeCap(Cap.SQUARE);
		default:
			break;
		}

		if (paint.getDashArray() != null)
		{
			MF_Paint.setDashPathEffect(paint.getDashArray());

		}

		canvas.drawPath(path, MF_Paint);

		tex = Bmp2Texture(bmp);
	}

	@Override
	public void draw(Batch batch)
	{
		boolean test = true;

		if (polygon != null)
		{
			polygon.draw(batch, firstPoint.x, firstPoint.y, 256, 256, 0);
			polygon.draw(batch, firstPoint.x + 200, firstPoint.y, 512, 512, 0);
		}
		else
			test = false;

		if (tex != null)
		{
			batch.draw(tex, secondPoint.x, secondPoint.y, 256, 256);
			batch.draw(tex, secondPoint.x + 200, secondPoint.y, 512, 512);
		}
		else
			test = false;

		isReady = test;
	}

	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	@Override
	public boolean isDisposed()
	{
		return isDisposed.get();
	}

	@Override
	public void dispose()
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;
			// TODO Dispose
			isDisposed.set(true);
		}
	}
}
