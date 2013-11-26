package CB_UI.GL_UI.Activitys;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ImportAnimation extends Box
{

	public enum AnimationType
	{
		Work, Download
	}

	AnimationBase mAnimation;
	private Drawable back;

	public ImportAnimation(CB_RectF rec)
	{
		super(rec, "");
		setAnimationType(AnimationType.Work);
	}

	public void setAnimationType(final AnimationType Type)
	{
		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				float size = ImportAnimation.this.getHalfWidth() / 2;
				float halfSize = ImportAnimation.this.getHalfWidth() / 4;
				CB_RectF imageRec = new CB_RectF(ImportAnimation.this.halfWidth - halfSize, ImportAnimation.this.halfHeight - halfSize,
						size, size);

				ImportAnimation.this.removeChilds();

				switch (Type)
				{
				case Work:
					mAnimation = WorkAnimation.GetINSTANCE(imageRec);
					break;

				case Download:
					mAnimation = DownloadAnimation.GetINSTANCE(imageRec);
					break;
				}

				ImportAnimation.this.addChild(mAnimation);
			}
		});

	}

	public void render(SpriteBatch batch)
	{
		if (drawableBackground != null)
		{
			back = drawableBackground;
			drawableBackground = null;
		}

		if (back != null)
		{
			Color c = batch.getColor();

			float a = c.a;
			float r = c.r;
			float g = c.g;
			float b = c.b;

			Color trans = new Color(0, 0.3f, 0, 0.40f);
			batch.setColor(trans);
			back.draw(batch, 0, 0, this.width, this.height);

			batch.setColor(new Color(r, g, b, a));

		}
	}

	@Override
	public void onHide()
	{
		mAnimation.dispose();
	}

	// alle Touch events abfangen

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		return true;
	}
}
