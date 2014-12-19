package de.CB.TestBase.Actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.model.DisplayModel;
import org.slf4j.LoggerFactory;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL_Bmp;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.graphics.GL_GraphicFactory;
import CB_UI_Base.graphics.PolylineDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;

import de.CB.TestBase.Config;

public abstract class TestCaseBase extends CB_View_Base
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(TestCaseBase.class);
	protected final String MSG_TOP;
	protected final String MSG_BOTOM;
	protected int TestIndex;
	

	public static DisplayModel dispModel = new DisplayModel();
	public static DisplayModel GL_dispModel = new DisplayModel();
	public static ext_GraphicFactory GL_Factory;
	public static GraphicFactory Mapsforge_Factory;

	public TestCaseBase(String TopTestMsg, String BotomTestMsg)
	{
		super("");

		MSG_TOP = TopTestMsg;
		MSG_BOTOM = BotomTestMsg;
		this.isVisible();
		Mapsforge_Factory = ManagerBase.Manager.getGraphicFactory(2);
		dispModel.setUserScaleFactor(2);
		GL_dispModel.setUserScaleFactor(2);
		GL_Factory = new GL_GraphicFactory(DisplayModel.getDeviceScaleFactor());
	}

	protected boolean isReady = false;
	protected Bitmap bmp;
	protected Texture tex;

	public static final String br = System.getProperty("line.separator");

	public void setTestIndex(int index){
		this.TestIndex=index;
	}
	
	public boolean getIsReady()
	{
		return isReady;
	}

	public abstract void work();

	public TileGL_Bmp Bitmap2Drawable(Bitmap bmp)
	{
		if (bmp == null) return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			bmp.compress(baos);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		byte[] bytes = new byte[baos.toByteArray().length];
		System.arraycopy(baos.toByteArray(), 0, bytes, 0, baos.toByteArray().length);

		try
		{
			baos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		TileGL_Bmp tile = new TileGL_Bmp(new Descriptor(0, 0, 16, false), bytes, TileGL.TileState.Present, Format.RGB565);

		return tile;
	}

	public Texture Bmp2Texture(Bitmap bmp)
	{
		byte[] byteArray = null;

		try
		{
			if (bmp != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bmp.compress(baos);

				byteArray = baos.toByteArray();

				try
				{
					baos.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Pixmap pixmap = new Pixmap(byteArray, 0, byteArray.length);
			Texture texture = new Texture(pixmap);
			pixmap.dispose();
			return texture;
		}
		catch (Exception ex)
		{
			log.error("[TileGL] can't create Pixmap or Texture: " , ex);
		}
		return null;
	}

	PolylineDrawable splitline;

	protected BitmapFontCache topMsg;
	protected BitmapFontCache botomMsg;

	public static PointF firstPoint;
	public static PointF secondPoint;

	public class PointF
	{
		public PointF(float X, float Y)
		{
			this.x = X;
			this.y = Y;
		}

		public float x;
		public float y;
	}

	@Override
	public void render(Batch batch)
	{
		if (firstPoint == null)
		{
			firstPoint = new PointF(5, 5);
			if (this.getWidth() > this.getHeight())
			{
				secondPoint = new PointF(this.getHalfWidth() + 5, 5);
				secondPoint = new PointF(20 + 5, 5);
			}
			else
			{
				secondPoint = new PointF(5, this.getHalfHeight() + 5);
			}

		}

		draw(batch);

		// Draw MSG
		if (topMsg == null)
		{

			topMsg = new BitmapFontCache(Fonts.getNormal());
			topMsg.setColor(COLOR.getFontColor());
			topMsg.setMultiLineText("("+ TestIndex+")  " +   MSG_TOP, 20, getHeight() - 20);

		}

		if (botomMsg == null)
		{

			botomMsg = new BitmapFontCache(Fonts.getNormal());
			botomMsg.setColor(COLOR.getFontColor());
			botomMsg.setMultiLineText(MSG_BOTOM, 20, getHalfHeight() - 20);

		}

		topMsg.draw(batch);
		botomMsg.draw(batch);

	}

	public abstract void draw(Batch batch);

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void onResized(CB_RectF rec)
	{

	}

	@Override
	public abstract void dispose();

}
