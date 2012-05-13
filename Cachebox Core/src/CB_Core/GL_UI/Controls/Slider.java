package CB_Core.GL_UI.Controls;

import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;

public class Slider extends CB_View_Base implements SelectedCacheEvent
{
	private Label mLblCacheName;
	private Box mSlideBox;

	private float PosY = 0;

	public Slider(CB_RectF rec, String Name)
	{
		super(rec, Name);
		SelectedCacheEventList.Add(this);
		this.setClickable(true);
	}

	@Override
	protected void Initial()
	{
		mSlideBox = new Box(new CB_RectF(-15, 100, this.width + 30, UiSizes.getInfoSliderHeight() + (GL_UISizes.infoShadowHeight * 2.3f)),
				"SlideBox");
		mSlideBox.setBackground(new NinePatch(SpriteCache.ToggleBtn.get(0), 16, 16, 16, 16));
		mLblCacheName = new Label(new CB_RectF(20, 0, this.width - 30, mSlideBox.getHeight()), "CacheNameLbl");
		mLblCacheName.setPos(30, 0);
		mLblCacheName.setHAlignment(HAlignment.CENTER);
		mSlideBox.addChild(mLblCacheName);
		this.addChild(mSlideBox);
		setSliderPos(this.height - mSlideBox.getHeight() + (GL_UISizes.infoShadowHeight));
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		if (cache != null)
		{
			mLblCacheName.setText(cache.Name);
		}

	}

	private void setSliderPos(float value)
	{
		PosY = value;
		mSlideBox.setY(value);
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		float newY = this.height - touchY + y - mSlideBox.getHeight();
		setSliderPos(newY);
		return true;
	}

	private float touchY = 0;

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (mSlideBox.contains(x, y))
		{
			touchY = y;
			return true;
		}

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return true;
	}

}
