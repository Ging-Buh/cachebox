package CB_UI.GL_UI.Controls.Animation;

import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.GL_View_Base;
import CB_UI.Math.CB_RectF;
import CB_UI.Math.SizeF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AnimationBase extends CB_View_Base
{

	protected boolean mPlaying = false;
	protected int mDuration = 1;
	protected float mSpriteWidth;
	protected float mSpriteHeight;
	protected boolean mPause = false;

	public AnimationBase(String Name)
	{
		super(Name);
	}

	public AnimationBase(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public AnimationBase(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height, Parent, Name);
	}

	public AnimationBase(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	public AnimationBase(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec, Parent, Name);
	}

	public AnimationBase(SizeF size, String Name)
	{
		super(size, Name);
	}

	protected abstract void render(SpriteBatch batch);

	public abstract void play();

	public abstract void stop();

	public abstract void pause();

	public abstract AnimationBase INSTANCE();

	public abstract AnimationBase INSTANCE(CB_RectF rec);

}