package CB_UI_Base.GL_UI.Controls.Animation;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.Math.CB_RectF;

public class DownloadAnimation extends FrameAnimation
{

	private final static DownloadAnimation mINSTANCE = new DownloadAnimation();
	private static final int ANIMATION_DURATION = 1000;

	public static DownloadAnimation GetINSTANCE()
	{
		return mINSTANCE;
	}

	public static DownloadAnimation GetINSTANCE(CB_RectF rec)
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

	private DownloadAnimation()
	{
		super(new CB_RectF(0, 0, 50, 50), "DownloadAnimation");

		addFrame(SpriteCacheBase.getThemedSprite("download-1"));
		addFrame(SpriteCacheBase.getThemedSprite("download-2"));
		addFrame(SpriteCacheBase.getThemedSprite("download-3"));
		addFrame(SpriteCacheBase.getThemedSprite("download-4"));
		addFrame(SpriteCacheBase.getThemedSprite("download-5"));
		play(ANIMATION_DURATION);
	}

	@Override
	public void dispose()
	{
		// do nothing, is FINAL GLOBAL animation
	}

	@Override
	public void play()
	{
		play(ANIMATION_DURATION);
	}
}
