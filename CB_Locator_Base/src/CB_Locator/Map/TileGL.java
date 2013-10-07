package CB_Locator.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import CB_UI_Base.GL_UI.runOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.EmptyDrawable;
import CB_Utils.Log.Logger;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TileGL extends EmptyDrawable implements Destroyable
{
	public enum TileState
	{
		Scheduled, Present, LowResolution, Disposed
	};

	public Descriptor Descriptor = null;

	public TileState State;
	// zum speichern beliebiger Zusatzinfos
	public Object data;

	// TODO MapsforgeGL change to Drawable
	private Texture texture = null;

	private Pixmap pixmap;
	private byte[] bytes;

	// / <summary>
	// / Frames seit dem letzten Zugriff auf die Textur
	// / </summary>
	public long Age = 0;

	public TileGL(Descriptor desc, byte[] bytes, TileState state)
	{
		Descriptor = desc;
		this.texture = null;
		// this.texture = texture;
		this.bytes = bytes;
		State = state;
	}

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
			pixmap = new Pixmap(bytes, 0, bytes.length);
			texture = new Texture(pixmap);
		}
		catch (Exception ex)
		{
			Logger.DEBUG("[TileGL] can't create Pixmap or Texture: " + ex.getMessage());
		}
		bytes = null;
	}

	public String ToString()
	{
		return State.toString() + ", " + Descriptor.ToString();
	}

	@Override
	public void destroy() throws DestroyFailedException
	{

		// must run on GL thrad

		GL.that.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				try
				{
					if (pixmap != null) pixmap.dispose();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}

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
				pixmap = null;
			}
		});

	}

	@Override
	public boolean isDestroyed()
	{
		return false;
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{
		if (texture != null) batch.draw(texture, x, y, width, height);
	}

	public long getWidth()
	{
		if (texture != null) return texture.getWidth();
		return 0;
	}

	public long getHeight()
	{
		if (texture != null) return texture.getHeight();
		return 0;
	}

}
