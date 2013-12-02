package CB_UI_Base.GL_UI.utils;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.MathUtils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GradiantFilledRectangle extends CB_View_Base
{

	private GradiantFill gradiant;
	private TextureRegion tex;

	private float drawW = 0;
	private float drawH = 0;
	private float drawCX = 0;
	private float drawCY = 0;
	private float drawX = 0;
	private float drawY = 0;

	public GradiantFilledRectangle(CB_RectF rec, GradiantFill fill)
	{
		super(rec, "");
		gradiant = fill;
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setGradiant(GradiantFill fill)
	{
		gradiant = fill;
		tex = null;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (gradiant.getTexture() != null)
		{

			if (tex == null || tex != gradiant.getTexture())
			{
				tex = gradiant.getTexture();

				// TODO handle angle over 90°

				double alpha = (gradiant.getDirection() * MathUtils.DEG_RAD);

				float x1 = (float) (getWidth() * Math.cos(alpha));
				float x2 = (float) (getHeight() * Math.sin(alpha));

				float y1 = (float) (getWidth() * Math.sin(alpha));
				float y2 = (float) (getHeight() * Math.cos(alpha));

				drawW = x1 + x2;
				drawH = y1 + y2;

				drawCX = (drawW / 2);
				drawCY = (drawH / 2);

				drawX = -(drawCX - this.getHalfWidth());
				drawY = -(drawCY - this.getHalfHeight());

			}
		}

		if (tex != null) batch.draw(tex, drawX, drawY, drawCX, drawCY, drawW, drawH, 1f, 1f, gradiant.getDirection());

	}
}
