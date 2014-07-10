package de.CB.Test.String;

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Main.CB_Button;
import CB_UI_Base.graphics.GL_Matrix;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.GL_Path;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.PolylineDrawable;
import CB_UI_Base.graphics.TextDrawable;
import CB_UI_Base.graphics.TextDrawableFlipped;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_Utils.MathUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.CB.TestBase.Actions.TestCaseBase;
import de.CB.TestBase.Views.MainView;

public class TextOnPathTest extends TestCaseBase
{

	GL_Paint PAINT;
	final String TEXT;

	TextDrawable bmpFontCache;
	GL_Path Path;
	PolylineDrawable ply;

	TextDrawableFlipped bmpFontCache2;
	GL_Path Path2;
	PolylineDrawable ply2;

	PolygonDrawable polyBack;

	private int Angle;

	public TextOnPathTest()
	{
		super("Draw text on Path", "");

		// TEXT = "I";
		// TEXT = "ABCWXYZ";
		// TEXT = "Das ist ein Test mit ganz langem Text der nicht auf den Pfad Passt";
		TEXT = "Das ist ein Test";

	}

	@Override
	public void Initial()
	{
		super.Initial();

		PAINT = new GL_Paint();
		GL_Paint paint = new GL_Paint();
		paint.setStrokeWidth(3);
		paint.setColor(Color.MAGENTA);

		Path = new GL_Path();
		Path.moveTo(80, 20);
		Path.cubicTo(180, 200, 400, 120, 500, 70);
		GL_Matrix m = new GL_Matrix();
		m.translate(firstPoint.x, firstPoint.y);
		m.mapPoints(Path);
		ply = new PolylineDrawable(Path, paint, 600, 600);

		Path2 = new GL_Path();
		Path2.moveTo(80, 20);
		Path2.cubicTo(180, 200, 400, 120, 500, 70);

		GL_Matrix m2 = new GL_Matrix();
		m2.translate(secondPoint.x, secondPoint.y);
		m2.mapPoints(Path2);
		ply2 = new PolylineDrawable(Path2, paint, 600, 600);

		Quadrangle qu = new Quadrangle(this);
		GL_Paint back = new GL_Paint();
		back.setColor(Color.BLACK);
		polyBack = new PolygonDrawable(qu.getVertices(), qu.getTriangles(), back, 600, 600);

		PAINT.setColor(Color.RED);
		PAINT.setTextSize(60);

		isInitial = true;

	}

	@Override
	public void work()
	{
		// Enable Rotate Buttons
		MainView.that.enableRotateButton(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				CB_Button b = (CB_Button) v;
				if (b.getText().equals("CW")) Angle -= 5;
				else
					Angle += 5;

				Angle = (int) MathUtils.LegalizeDegreese(Angle);

				return true;
			}
		});

		isReady = true;
	}

	@Override
	public void draw(Batch batch)
	{
		if (!isInitial) Initial();
		if (polyBack != null) polyBack.draw(batch, 0, -100, 600, 600, Angle);

		if (bmpFontCache == null)
		{

			GL_Paint stroke = new GL_Paint();
			stroke.setColor(Color.GREEN);
			stroke.setStrokeWidth(6);
			bmpFontCache = new TextDrawable(TEXT, Path, 600, 600, PAINT, stroke, true);

			Path.getAverageDirection();

		}
		else
		{

			ply.draw(batch, 0, 0, 600, 600, Angle);
			bmpFontCache.draw(batch, 0, 0, 600, 600, Angle);

		}

		if (bmpFontCache2 == null)
		{

			GL_Paint stroke = new GL_Paint();
			stroke.setColor(Color.GREEN);
			stroke.setStrokeWidth(6);
			bmpFontCache2 = new TextDrawableFlipped(TEXT, Path2, 600, 600, PAINT, stroke, true);

			Path2.getAverageDirection();
		}
		else
		{

			ply2.draw(batch, 0, 0, 600, 600, Angle);
			bmpFontCache2.draw(batch, 0, 0, 600, 600, Angle);

		}

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
