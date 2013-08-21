package CB_UI_Base.GL_UI.Controls.Animation;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public abstract class RotateAnimation extends AnimationBase
{
	private float mOriginX;
	private float mOriginY;
	private float mScale = 1f;
	private float animateRotateValue = 0;
	private Drawable mDrawable;

	protected static final int ANIMATION_DURATION = 2000;

	public RotateAnimation(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public RotateAnimation(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	/**
	 * Time for 360°
	 */
	protected void play(int duration)
	{
		this.mDuration = duration;
		mPlaying = true;
		mPause = false;
	}

	@Override
	public void stop()
	{
		mPlaying = false;
		mPause = false;
	}

	@Override
	public void pause()
	{
		mPause = true;
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	public void setOrigin(float x, float y)
	{
		mOriginX = x;
		mOriginY = y;
	}

	public void setScale(float scale)
	{
		mScale = scale;
	}

	public void setSprite(Sprite sprite)
	{
		mSpriteWidth = sprite.getWidth();
		mSpriteHeight = sprite.getHeight();
		mDrawable = new SpriteDrawable(sprite);
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (mDrawable == null) return;

		// ####################################################################
		// calc Rotation
		// ####################################################################

		animateRotateValue = (1 + ((int) (GL.that.getStateTime() * 1000) % this.mDuration) / (this.mDuration / 360));

		// ####################################################################
		// Set Rotation
		// ####################################################################
		Matrix4 matrix = new Matrix4();
		matrix.idt();
		matrix.translate(mOriginX, mOriginY, 0);
		matrix.rotate(0, 0, 1, animateRotateValue);
		matrix.scale(mScale, mScale, 1);
		matrix.translate(-mOriginX, -mOriginY, 0);
		batch.setTransformMatrix(matrix);

		// ####################################################################
		// Draw
		// ####################################################################

		float drawwidth = width;
		float drawHeight = height;
		float drawX = 0;
		float drawY = 0;

		if (mSpriteWidth > 0 && mSpriteHeight > 0)
		{
			float proportionWidth = width / mSpriteWidth;
			float proportionHeight = height / mSpriteHeight;

			float proportion = Math.min(proportionWidth, proportionHeight);

			drawwidth = mSpriteWidth * proportion;
			drawHeight = mSpriteHeight * proportion;
			drawX = (width - drawwidth) / 2;
			drawY = (height - drawHeight) / 2;
		}

		mDrawable.draw(batch, drawX, drawY, drawwidth, drawHeight);

		// ####################################################################
		// Reset Rotation
		// ####################################################################

		matrix = new Matrix4();
		matrix.idt();
		matrix.rotate(0, 0, 1, 0);
		matrix.scale(1, 1, 1);
		batch.setTransformMatrix(matrix);

		GL.that.renderOnce("RotateAnimation-" + name);

	}

}
