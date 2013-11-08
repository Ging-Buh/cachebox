package CB_UI_Base.GL_UI.utils;

import CB_UI_Base.GL_UI.runOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PixmapDrawable extends EmptyDrawable
{
	private Texture tex;

	public PixmapDrawable(final Pixmap pixmap)
	{
		// must create on GL Thread
		GL.that.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				tex = new Texture(pixmap);
				tex.bind();
				pixmap.dispose();
			}
		});

	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{
		if (tex != null) batch.draw(tex, x, y, width, height);
	}

	public void dispose()
	{
		if (tex == null) return;
		tex.dispose();
		tex = null;
	}
}
