package CB_Core.GL_UI.utils;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class rectangle extends CB_View_Base
{
	private ShapeRenderer renderer;

	public rectangle(CB_RectF rec, String Name)
	{
		super(rec, Name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		batch.end();

		if (renderer == null)
		{
			renderer = new ShapeRenderer();
		}

		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		renderer.setTransformMatrix(batch.getTransformMatrix());
		renderer.translate(getX(), getY(), 0);

		Color c1 = Color.BLACK;
		Color c2 = Color.BLACK;
		Color c3 = Color.GREEN;
		Color c4 = Color.WHITE;

		renderer.begin(ShapeType.FilledRectangle);
		renderer.filledRect(0.0f, 0.0f, getWidth(), getHeight(), c1, c2, c3, c4);
		renderer.end();

		batch.begin();
	}

}
