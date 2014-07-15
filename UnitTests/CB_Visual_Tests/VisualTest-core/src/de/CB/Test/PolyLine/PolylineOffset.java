package de.CB.Test.PolyLine;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Cap;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.Style;

import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.GL_Path;
import CB_UI_Base.graphics.PolylineDrawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

public class PolylineOffset extends PolylineTestBase
{
	PolylineDrawable polyLineDrawable;

	public PolylineOffset()
	{
		super(" PolyLine Offset Test" + br + "Red / CAP.DEFAULT / StrokeWidth=20");
		paint = new GL_Paint();
		paint.setColor(Color.RED);
		paint.setCap(GL_Cap.DEFAULT);
		paint.setStrokeWidth(20);

	}

	void createLineOffset(Path path)
	{

	}

	@Override
	public void work()
	{

		Color color = paint.getHSV_Color();

		GraphicFactory factory = Mapsforge_Factory;
		bmp = factory.createBitmap(256, 256);
		Canvas canvas = factory.createCanvas();
		canvas.setBitmap(bmp);

		Path path = factory.createPath();
		GL_Path glPath = new GL_Path();

		// Flip Y (256-y)
		path.moveTo(vertices[0], 256 - vertices[1]);
		glPath.moveTo(vertices[0], 256 - vertices[1]);

		for (int i = 2; i < vertices.length - 1; i += 2)
		{
			path.lineTo(vertices[i], 256 - vertices[i + 1]);
			glPath.lineTo(vertices[i], 256 - vertices[i + 1]);
		}

		Paint MF_Paint = factory.createPaint();
		MF_Paint.setStrokeWidth(paint.getStrokeWidth());
		MF_Paint.setStyle(Style.STROKE);

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

		polyLineDrawable = new PolylineDrawable(glPath, paint, 256, 256);
	}

	@Override
	public void render(Batch batch)
	{
		if (polyLineDrawable != null)
		{
			polyLineDrawable.draw(batch, firstPoint.x, firstPoint.y, 256, 256, 0);
			polyLineDrawable.draw(batch, firstPoint.x + 200, firstPoint.y, 512, 512, 0);
		}

		if (tex != null)
		{
			batch.draw(tex, secondPoint.x, secondPoint.y, 256, 256);
			batch.draw(tex, secondPoint.x + 200, secondPoint.y, 512, 512);
		}
		isReady = true;
	}

}
