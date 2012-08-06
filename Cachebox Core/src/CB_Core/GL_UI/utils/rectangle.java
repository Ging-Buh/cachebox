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
	private GradiantFill gradiant;

	public rectangle(CB_RectF rec, GradiantFill fill)
	{
		super(rec, "");
		gradiant = fill;
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

		Color[] c = new Color[5];

		getFillColors(c);

		renderer.begin(ShapeType.FilledRectangle);
		renderer.filledRect(0.0f, 0.0f, getWidth(), getHeight(), c[1], c[2], c[3], c[4]);
		renderer.end();

		batch.begin();
	}

	private void getFillColors(Color[] c)
	{
		switch (gradiant.Direction)
		{
		case left2reight:
			c[1] = gradiant.Color1;
			c[2] = gradiant.Color2;
			c[3] = gradiant.Color1;
			c[4] = gradiant.Color2;
			break;
		case right2left:
			c[1] = gradiant.Color1;
			c[2] = gradiant.Color1;
			c[3] = gradiant.Color1;
			c[4] = gradiant.Color1;
			break;
		case top2bottom:
			c[1] = gradiant.Color2;
			c[2] = gradiant.Color2;
			c[3] = gradiant.Color1;
			c[4] = gradiant.Color1;
			break;
		case bottom2top:
			c[1] = gradiant.Color1;
			c[2] = gradiant.Color1;
			c[3] = gradiant.Color2;
			c[4] = gradiant.Color2;
			break;
		}

	}

}
