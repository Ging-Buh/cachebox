package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Logger;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Scrollbar extends CB_View_Base
{
	private final IScrollbarParent ListView;
	private final CB_RectF SliderPuchRec = new CB_RectF();
	private final CB_RectF SliderRec = new CB_RectF();
	private Drawable SliderBack, Slider, SliderPushed;
	protected float mDraged = 0;
	protected int mLastTouch = 0;
	protected float mLastPos_onTouch = 0;

	// / <summary>
	// / Die Y Pos des Sliders
	// / </summary>
	private float mSliderPos = 0;

	// / <summary>
	// / Die Höhe des gezeigten Sliders
	// / </summary>
	private float mSliderSollHeight = 0;

	private float mSliderIstHight = 0;

	// / <summary>
	// / True wenn der Slider gedrückt ist
	// / </summary>
	private boolean mSliderPuched = false;

	public Scrollbar(IScrollbarParent Parent)
	{
		super(Parent.getView(), Parent.getView(), "ScrollBar-on-" + Parent.getView().toString());
		ListView = Parent;
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public void render(SpriteBatch batch)
	{
		// Wenn Liste länger als Clintbereich zeige Slider
		if (ListView.isDragable())
		{
			if (SliderBack == null || Slider == null || SliderPushed == null)
			{
				LoadSliderImagesNew();
			}
			// Draw SliderBackGround

			if (SliderBack != null) SliderBack.draw(batch, SliderPuchRec.getX(), SliderPuchRec.getY(), SliderPuchRec.getWidth(),
					SliderPuchRec.getHeight());

			// Draw Slider
			CalcSliderPos();

			// SliderPushed.draw(batch, SliderPuchRec.getX(), SliderPuchRec.getY(), SliderPuchRec.getWidth(), SliderPuchRec.getHeight());

			Slider.draw(batch, SliderRec.getX(), SliderRec.getY(), SliderRec.getWidth(), SliderRec.getHeight());

		}
	}

	private void LoadSliderImagesNew()
	{
		Slider = SpriteCacheBase.Slider;
		SliderBack = SpriteCacheBase.SliderBack;
		SliderPushed = SpriteCacheBase.SliderPushed;

		float minWidth = Slider.getMinWidth();

		SliderRec.setX(this.width - (minWidth / 1.35f));
		SliderRec.setWidth(minWidth);

		float sliderPushSize = UI_Size_Base.that.getButtonHeight() * 0.8f;

		SliderPuchRec.setX(SliderRec.getX() - sliderPushSize + Slider.getLeftWidth());
		SliderPuchRec.setY(mSliderPos);
		SliderPuchRec.setWidth(sliderPushSize);
		SliderPuchRec.setHeight(sliderPushSize);

	}

	private void CalcSliderPos()
	{
		CalcSliderHeight();

		float ScrollPos = ListView.getScrollPos();
		float ListAllHeigt = ListView.getAllListSize();

		float rest = (ListAllHeigt - this.height) + ScrollPos;
		mSliderPos = ((rest / ListAllHeigt) * this.height);

		// Logger.DEBUG("ScrollPos:" + ScrollPos);
		// Logger.DEBUG("ListAllHeigt:" + ListAllHeigt);
		// Logger.DEBUG("rest:" + rest);
		// Logger.DEBUG("mSliderPos:" + mSliderPos);
		//

		// float versatz = (mSliderHeight / 2) - SliderPuchRec.getHalfHeight();
		// SliderPuchRec.setY(mSliderPos + versatz);
		SliderRec.setY(mSliderPos);
	}

	private void CalcSliderHeight()
	{

		mSliderSollHeight = (this.height / ListView.getAllListSize()) * this.height;

		// if (mSliderSollHeight > this.halfHeight)
		// {
		// mSliderIstHight = this.halfHeight;
		// }
		// else if (mSliderSollHeight < UI_Size_Base.that.getButtonHeight())
		// {
		// mSliderIstHight = UI_Size_Base.that.getButtonHeight();
		// }
		// else
		// {
		mSliderIstHight = mSliderSollHeight;
		// }

		SliderRec.setHeight(mSliderIstHight);

	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (mSliderPuched)
		{
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
			mLastTouch = y;
			mLastPos_onTouch = ListView.getScrollPos();
			mSliderPuched = true;
			return true;
		}

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (mSliderPuched)
		{

			// verhindere Move bei kleine Bewegungen
			mDraged = y - mLastTouch;

			// Dragged auf ListenLänge umrechnen
			mDraged = ListView.getAllListSize() / (this.height - SliderRec.getHeight()) * mDraged;

			float sollPos = mLastPos_onTouch - mDraged;
			float toMuch = 0;
			if (sollPos - ListView.getFirstItemSize() > 0 || sollPos < ListView.getAllListSize())
			{
				if (sollPos - (ListView.getFirstItemSize() * 3) > 0
						|| sollPos + (ListView.getLasstItemSize() * 3) < ListView.getAllListSize())
				{
					if (KineticPan) GL.that.StopKinetic(x, y, pointer, true);
					return true;
				}

				if (sollPos - ListView.getFirstItemSize() > 0)
				{
					toMuch = 0 - sollPos + ListView.getFirstItemSize();
					toMuch /= 2;
				}
				else if (sollPos < ListView.getAllListSize())
				{
					toMuch = ListView.getAllListSize() - sollPos;
					toMuch /= 2;
				}
			}

			sollPos += toMuch;

			ListView.setListPos(sollPos);
			Logger.DEBUG("SliderScrollValue:" + sollPos + " toMuch:" + toMuch + " Draged:" + mDraged);
			return true;
		}

		return false;
	}

}
