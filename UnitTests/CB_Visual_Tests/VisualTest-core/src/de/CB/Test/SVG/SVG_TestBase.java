package de.CB.Test.SVG;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.graphics.Images.VectorDrawable;
import CB_UI_Base.graphics.SVG.SVG;
import CB_UI_Base.graphics.SVG.SVGParseException;

import com.badlogic.gdx.graphics.g2d.Batch;

import de.CB.TestBase.Actions.TestCaseBase;

public abstract class SVG_TestBase extends TestCaseBase {
	VectorDrawable SVG_BMP_SCALE_1;
	VectorDrawable SVG_BMP_SCALE_10;
	VectorDrawable SVG_BMP_SCALE_20;
	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	public SVG_TestBase(String TopTestMsg) {
		super(TopTestMsg, "");

	}

	public abstract InputStream get_SVG_InputStream() throws FileNotFoundException;

	@Override
	public void work() {

		try {

			SVG_BMP_SCALE_1 = (VectorDrawable) SVG.createBmpFromSVG(GL_Factory, get_SVG_InputStream(), 1f);
			SVG_BMP_SCALE_10 = (VectorDrawable) SVG.createBmpFromSVG(GL_Factory, get_SVG_InputStream(), 10f);
			SVG_BMP_SCALE_20 = (VectorDrawable) SVG.createBmpFromSVG(GL_Factory, get_SVG_InputStream(), 20f);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SVGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void draw(Batch batch) {

		if (SVG_BMP_SCALE_1 != null) {

			float w = SVG_BMP_SCALE_1.getWidth();
			float h = SVG_BMP_SCALE_1.getHeight();

			// w = h = 768;

			SVG_BMP_SCALE_1.draw(batch, firstPoint.x + 10, firstPoint.y + 10, w, h);

			isReady = true;
		} else
			isReady = false;

		if (SVG_BMP_SCALE_10 != null) {

			float w = SVG_BMP_SCALE_10.getWidth();
			float h = SVG_BMP_SCALE_10.getHeight();

			// w = h = 768;

			SVG_BMP_SCALE_10.draw(batch, firstPoint.x + 60, firstPoint.y + 10, w, h);

			isReady &= true;
		} else
			isReady = false;

		if (SVG_BMP_SCALE_20 != null) {

			float w = SVG_BMP_SCALE_20.getWidth();
			float h = SVG_BMP_SCALE_20.getHeight();

			// w = h = 768;

			SVG_BMP_SCALE_20.draw(batch, firstPoint.x + 300, firstPoint.y + 10, w, h);

			isReady &= true;
		} else
			isReady = false;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void dispose() {
		synchronized (isDisposed) {
			if (isDisposed.get())
				return;

			isDisposed.set(true);
		}
	}
}
