package CB_Core.GL_UI.utils;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class rectangle extends CB_View_Base
{

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
		if (gradiant.getTexture() != null)
		{

			batch.draw(gradiant.getTexture(), 0, 0, this.halfWidth, this.halfHeight, this.width, this.height, 1f, 1f,
					gradiant.getDirection());

		}
	}
}
