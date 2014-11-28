package de.CB.Test.SVG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;



import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.Images.VectorDrawable;
import CB_UI_Base.graphics.SVG.SVG;
import CB_Utils.Plattform;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.CB.TestBase.Actions.TestCaseBase;

public class SVG_with_PNG extends TestCaseBase
{
	final String Name;
	VectorDrawable SVG_BMP_SCALE_1;
	Drawable SVG_BMP_SCALE_2;
	BitmapDrawable PNG;
	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	public SVG_with_PNG(String Name)
	{
		super("SVG-" + Name, "");
		this.Name = Name;
	}

	@Override
	public void work()
	{
		try
		{

			String path;
			String path2;

			if (Plattform.used == Plattform.Android)
			{
				path = "storage/extSdCard/freizeitkarte/svg/osm/" + Name + ".svg";
				path2 = "storage/extSdCard/freizeitkarte/svg/osm/" + Name + ".png";
			}
			else
			{

				path = "assets/svg/osm/" + Name + ".svg";
				path2 = "assets/svg/osm/" + Name + ".png";
			}

			InputStream stream = new FileInputStream(path);
			InputStream stream2 = new FileInputStream(path2);

			try
			{
				SVG_BMP_SCALE_1 = (VectorDrawable) SVG.createBmpFromSVG(GL_Factory, stream);
				PNG = new BitmapDrawable(stream2, path.hashCode(), 1f);
			}
			catch (CB_UI_Base.graphics.SVG.SVGParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void draw(Batch batch)
	{

		if (PNG != null)
		{
			// if (!PNG.isPrepared()) PNG.prepare();
			float w = PNG.getWidth();
			float h = PNG.getHeight();

			PNG.draw(batch, secondPoint.x + 10, secondPoint.y + 10, w, h);

			if (SVG_BMP_SCALE_1 != null) isReady = true;
		}

		if (SVG_BMP_SCALE_1 != null)
		{
			float w = SVG_BMP_SCALE_1.getWidth();
			float h = SVG_BMP_SCALE_1.getHeight();
			SVG_BMP_SCALE_1.draw(batch, firstPoint.x + 10, firstPoint.y + 10, w, h);
		}
	}

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
