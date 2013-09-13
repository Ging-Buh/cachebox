package CB_UI_Base.GL_UI.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PixmapDrawable extends EmptyDrawable
{
	private Texture tex;

	public PixmapDrawable(Pixmap pixmap)
	{
		tex = new Texture(pixmap);
		pixmap.dispose();
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{
		batch.draw(tex, x, y, width, height);
	}

	public void dispose()
	{
		if (tex == null) return;
		tex.dispose();
		tex = null;
	}
}
