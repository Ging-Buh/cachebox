package CB_Locator.Map;

import javax.security.auth.DestroyFailedException;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class TileGL
{
	public enum TileState
	{
		Scheduled, Present, LowResolution, Disposed
	}

	private final int DEFAULT_TILE_SIZE = 256;

	public Descriptor Descriptor = null;

	public TileState State;
	// zum speichern beliebiger Zusatzinfos
	public Object data;

	// / <summary>
	// / Frames seit dem letzten Zugriff auf die Textur
	// / </summary>
	public long Age = 0;

	public abstract boolean canDraw();

	@Override
	public abstract String toString();

	public abstract void destroy() throws DestroyFailedException;

	public abstract boolean isDestroyed();

	public abstract void draw(SpriteBatch batch, float x, float y, float width, float height);

	public abstract long getWidth();

	public abstract long getHeight();

	public float getScaleFactor()
	{
		return getWidth() / DEFAULT_TILE_SIZE;
	}

}