package CB_Core.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Views.MapView;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TileGL implements Destroyable
{
	public enum TileState
	{
		Scheduled, Present, LowResolution, Disposed
	};

	public Descriptor Descriptor = null;

	public TileState State;
	// zum speichern beliebiger Zusatzinfos
	public Object data;

	// / <summary>
	// / Textur der Kachel
	// / </summary>
	public Texture texture = null;
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

	public void createTexture()
	{
		if (texture != null) return;
		try
		{
			pixmap = new Pixmap(bytes, 0, bytes.length);

			if (MapView.debug)
			{
				int h = pixmap.getHeight();
				int w = pixmap.getWidth();
				pixmap.setColor(Color.RED);
				pixmap.drawRectangle(0, 0, w, h);
			}

			texture = new Texture(pixmap);
		}
		catch (Exception ex)
		{
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

}
