package CB_UI.GL_UI.Controls.Animation;

import CB_UI.GL_UI.SpriteCache;
import CB_UI.GL_UI.SpriteCache.IconName;
import CB_UI.Math.CB_RectF;

public class WorkAnimation extends RotateAnimation
{
	protected static WorkAnimation mINSTANCE = new WorkAnimation();

	public static AnimationBase GetINSTANCE()
	{
		return mINSTANCE;
	}

	public static AnimationBase GetINSTANCE(CB_RectF rec)
	{
		mINSTANCE.setRec(rec);
		return mINSTANCE;
	}

	public AnimationBase INSTANCE()
	{
		return mINSTANCE;
	}

	public AnimationBase INSTANCE(CB_RectF rec)
	{
		mINSTANCE.setRec(rec);
		return mINSTANCE;
	}

	private WorkAnimation()
	{
		super(new CB_RectF(0, 0, 50, 50), "DownloadAnimation");

		setSprite(SpriteCache.Icons.get(IconName.settings_26.ordinal()));
		setOrigin(this.getHalfWidth(), this.getHalfHeight());
		play(ANIMATION_DURATION);
	}

	@Override
	public void resize(float width, float height)
	{
		super.resize(width, height);
		setOrigin(this.getHalfWidth(), this.getHalfHeight());
	}

	@Override
	public void play()
	{
		play(ANIMATION_DURATION);
	}

	@Override
	public void dispose()
	{
		// do nothing, is FINAL GLOBAL animation
	}

}
