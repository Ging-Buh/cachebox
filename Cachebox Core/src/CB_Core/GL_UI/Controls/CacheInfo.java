package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CacheInfo extends GL_View_Base
{

	private Cache mCache;
	private float mIconSize = 0;
	private float mLeft = 0;
	private float mTop = 0;
	private float mVoteWidth = 0;

	private Label lblName;

	public CacheInfo(SizeF size, CharSequence Name, Cache value)
	{
		super(size, Name);
		mCache = value;
		requestLayout();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		// Draw Icon
		if (mIconSize <= 0) mIconSize = GL_UISizes.PosMarkerSize;
		if (mCache.MysterySolved())
		{
			SpriteCache.MapIcons.get(19).setSize(mIconSize, mIconSize);
			SpriteCache.MapIcons.get(19).setPosition(mLeft, height - mTop - mIconSize);
			SpriteCache.MapIcons.get(19).draw(batch);
		}
		else
		{
			SpriteCache.MapIcons.get(mCache.Type.ordinal()).setSize(mIconSize, mIconSize);
			SpriteCache.MapIcons.get(mCache.Type.ordinal()).setPosition(mLeft, height - mTop - mIconSize);
			SpriteCache.MapIcons.get(mCache.Type.ordinal()).draw(batch);
		}

	}

	private void requestLayout()
	{
		float scaleFactor = width / UiSizes.RefWidth;
		mIconSize = (UiSizes.getScaledIconSize() / 1.5f) * scaleFactor;
		mLeft = 10 * scaleFactor;
		mTop = 10 * scaleFactor;

		if (lblName == null)
		{
			lblName = new Label(this, "CacheInfoName");
			lblName.setFont(Fonts.get16());
		}

		// BitmapFontCache font = new BitmapFontCache(Fonts.get16());
		// font.lblName.setSize(width - mIconSize, mIconSize);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

}
