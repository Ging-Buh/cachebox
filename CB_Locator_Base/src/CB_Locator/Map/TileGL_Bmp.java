package CB_Locator.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Log.Logger;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TileGL_Bmp extends TileGL implements Destroyable
{

	private Texture texture = null;

	private byte[] bytes;

	public TileGL_Bmp(Descriptor desc, byte[] bytes, TileState state)
	{
		Descriptor = desc;
		this.texture = null;
		// this.texture = texture;
		this.bytes = bytes;
		State = state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#canDraw()
	 */
	@Override
	public boolean canDraw()
	{
		if (texture != null) return true;
		if (bytes == null) return false;
		createTexture();
		if (texture != null) return true;
		return false;
	}

	private void createTexture()
	{
		if (texture != null) return;
		if (bytes == null) return;
		try
		{
			Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
			texture = new Texture(pixmap);
			pixmap.dispose();
			pixmap = null;
		}
		catch (Exception ex)
		{
			Logger.DEBUG("[TileGL] can't create Pixmap or Texture: " + ex.getMessage());
		}
		bytes = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#ToString()
	 */
	@Override
	public String ToString()
	{
		return State.toString() + ", " + Descriptor.ToString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#destroy()
	 */
	@Override
	public void destroy() throws DestroyFailedException
	{

		// must run on GL thrad

		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{

				try
				{
					if (texture != null) texture.dispose();
				}
				catch (java.lang.NullPointerException e)
				{
					e.printStackTrace();
				}
				texture = null;
				bytes = null;

			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#isDestroyed()
	 */
	@Override
	public boolean isDestroyed()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#draw(com.badlogic.gdx.graphics.g2d.SpriteBatch, float, float, float, float)
	 */
	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{
		if (texture != null) batch.draw(texture, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#getWidth()
	 */
	@Override
	public long getWidth()
	{
		if (texture != null) return texture.getWidth();
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#getHeight()
	 */
	@Override
	public long getHeight()
	{
		if (texture != null) return texture.getHeight();
		return 0;
	}

}
