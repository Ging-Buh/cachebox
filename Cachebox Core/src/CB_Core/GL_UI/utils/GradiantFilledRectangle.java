package CB_Core.GL_UI.utils;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

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
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

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

				double alpha = (gradiant.getDirection() * Math.PI / 180);

				float x1 = (float) (width * Math.cos(alpha));
				float x2 = (float) (height * Math.sin(alpha));

				float y1 = (float) (width * Math.sin(alpha));
				float y2 = (float) (height * Math.cos(alpha));

				drawW = x1 + x2;
				drawH = y1 + y2;

				drawCX = (drawW / 2);
				drawCY = (drawH / 2);

				drawX = -(drawCX - this.halfWidth);
				drawY = -(drawCY - this.halfHeight);

			}
		}

		if (tex != null) batch.draw(tex, drawX, drawY, drawCX, drawCY, drawW, drawH, 1f, 1f, gradiant.getDirection());

	}
}
