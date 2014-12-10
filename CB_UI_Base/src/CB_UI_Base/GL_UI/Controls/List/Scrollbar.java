package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Animation.Fader;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Scrollbar extends CB_View_Base
{
	private final IScrollbarParent ListView;
	private final CB_RectF SliderPuchRec = new CB_RectF();
	private final CB_RectF SliderRec = new CB_RectF();
	private Drawable Slider, SliderPushed;

	protected int mLastTouch = 0;
	protected float mLastPos_onTouch = 0;

	private float mSliderPos = 0;
	private float mSliderSollHeight = 0;
	private float mSliderIstHight = 0;

	private boolean mSliderPuched = false;
	private float mPuchSliderPos = 0;
	private float mPuchSliderTouch = 0;
	private float mPuchSliderIstHight = 0;

	private final Fader mPushSliderFader = new Fader(this);
	private final Fader mSliderFader = new Fader(this);

	private float mPushSliderAlpha = 1f;
	private float mSliderAlpha = 1f;

	public Scrollbar(IScrollbarParent Parent)
	{
		super(Parent.getView(), Parent.getView(), "ScrollBar-on-" + Parent.getView().toString());
		ListView = Parent;
		mPushSliderFader.setTimeToFadeOut(4000);
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	public void onShow()
	{
		super.onShow();
		mPushSliderFader.beginnFadeout();
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public void render(Batch batch)
	{
		// Wenn Liste l�nger als Clintbereich zeige Slider
		if (ListView.isDragable())
		{
			if (mPushSliderFader.isVisible() || mSliderFader.isVisible())
			{
				Color color = batch.getColor();// get current Color, you can't modify directly
				float oldAlpha = color.a; // save its alpha

				if (Slider == null || SliderPushed == null)
				{
					LoadSliderImagesNew();
				}

				// Draw Slider
				CalcSliderPos();

				mPushSliderAlpha = mPushSliderFader.isVisible() ? mPushSliderFader.getValue() : 0;
				mSliderAlpha = mSliderFader.isVisible() ? mSliderFader.getValue() : 0;

				color.a = oldAlpha * mPushSliderAlpha;
				batch.setColor(color); // set it
				SliderPushed.draw(batch, SliderPuchRec.getX(), SliderPuchRec.getY(), SliderPuchRec.getWidth(), SliderPuchRec.getHeight());

				color.a = oldAlpha * mSliderAlpha;
				batch.setColor(color); // set it
				Slider.draw(batch, SliderRec.getX(), SliderRec.getY(), SliderRec.getWidth(), SliderRec.getHeight());

				// Set it back to orginial alpha when you're done with your alpha manipulation
				color.a = oldAlpha;
				batch.setColor(color);
			}
		}
	}

	private void LoadSliderImagesNew()
	{
		Slider = SpriteCacheBase.Slider;
		SliderPushed = SpriteCacheBase.SliderPushed;

		float minWidth = Slider.getMinWidth();

		SliderRec.setX(this.getWidth() - (minWidth / 1.35f));
		SliderRec.setWidth(minWidth);

		mPuchSliderIstHight = UI_Size_Base.that.getButtonHeight() * 0.8f;

		SliderPuchRec.setX(SliderRec.getX() - mPuchSliderIstHight + Slider.getLeftWidth());
		SliderPuchRec.setY(mSliderPos);
		SliderPuchRec.setWidth(mPuchSliderIstHight);
		SliderPuchRec.setHeight(mPuchSliderIstHight);

	}

	/**
	 * l= Visible height<br>
	 * s= Slider height<br>
	 * p= position of slider<br>
	 * <br>
	 * ll= complete height of all items<br>
	 * lp= position of List
	 */
	private void CalcSliderPos()
	{
		CalcSliderHeight();

		float lp = ListView.getScrollPos();
		float ll = ListView.getAllListSize();

		float s = mSliderIstHight;
		float l = this.getHeight();

		mSliderPos = this.getHeight() + ((lp / (ll - l)) * (l - s)) - s;
		SliderRec.setY(mSliderPos);

		s = mPuchSliderIstHight;

		mPuchSliderPos = this.getHeight() + ((lp / (ll - l)) * (l - s)) - s;
		SliderPuchRec.setY(mPuchSliderPos);

	}

	private void CalcSliderHeight()
	{

		mSliderSollHeight = (this.getHeight() / ListView.getAllListSize()) * this.getHeight();

		if (mSliderSollHeight > this.getHalfHeight())
		{
			mSliderIstHight = this.getHalfHeight();
		}
		else if (mSliderSollHeight < getMinSliderHeight())
		{
			mSliderIstHight = getMinSliderHeight();
		}
		else
		{
			mSliderIstHight = mSliderSollHeight;
		}

		SliderRec.setHeight(mSliderIstHight);

	}

	private float mMinSliderHeight = -1;

	private float getMinSliderHeight()
	{
		if (mMinSliderHeight > 0) return mMinSliderHeight;
		return mMinSliderHeight = UI_Size_Base.that.getButtonHeight() * 0.5f;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (mSliderPuched)
		{
			mPushSliderFader.resetFadeOut();
			mSliderPuched = false;
			ListView.chkSlideBack();
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		if (SliderPuchRec.contains(x, y))
		{
			mSliderFader.resetFadeOut();
			mPushSliderFader.resetFadeOut();
			mLastTouch = y;

			mPuchSliderTouch = SliderPuchRec.getY() - y;
			mLastPos_onTouch = ListView.getScrollPos();
			mSliderPuched = true;
			return true;
		}

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		if (KineticPan)
		{
			GL.that.StopKinetic(x, y, pointer, true);
			return onTouchUp(x, y, pointer, 0);
		}

		if (mSliderPuched)
		{
			mPushSliderFader.stopTimer();
			float ll = ListView.getAllListSize();
			float ls = this.getHeight();
			float p = -ls + y - mPuchSliderTouch;
			float s = mPuchSliderIstHight;
			float l = this.getHeight();

			float lp = (p / (l - s)) * (ll - ls);

			this.ListView.setListPos(lp);

			// log.debug("SliderScrollValue:" + " Draged:" + lp + " LastTouch:" + mLastTouch + " Y=" + y);
			return true;
		}

		return false;
	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		this.setWidth(rec.getWidth());
		this.setHeight(rec.getHeight());
	}

	public void ScrollPositionChanged()
	{
		mSliderFader.resetFadeOut();
	}

}
