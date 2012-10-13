package CB_Core.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

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
			texture = new Texture(new Pixmap(bytes, 0, bytes.length));
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
		if (texture != null) texture.dispose();
		bytes = null;
	}

	@Override
	public boolean isDestroyed()
	{
		return false;
	}

}
