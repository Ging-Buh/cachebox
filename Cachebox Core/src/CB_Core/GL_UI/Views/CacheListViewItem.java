package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class CacheListViewItem extends ListViewItemBase
{

	protected CacheInfo info;
	protected boolean isPressed = false;

	public CacheListViewItem(CB_RectF rec, int Index, Cache cache)
	{
		super(rec, Index, cache.Name);

		info = new CacheInfo(UiSizes.getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.GcCode, cache);
		// info.setViewMode(CacheInfo.VIEW_MODE_BUBBLE);
		info.setZeroPos();
		setBackground();
		this.addChild(info);
	}

	@Override
	protected void Initial()
	{
		setBackground();
	}

	@Override
	public void dispose()
	{
		info.dispose();
		info = null;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isPressed = true;
		setBackground();
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;
		setBackground();
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;
		setBackground();
		return false;
	}

	private void setBackground()
	{

		Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);

		if (isSelected)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_selected"), 8, 8, 8, 8));
		}
		else if (isPressed)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_pressed"), 8, 8, 8, 8));
		}
		else if (BackGroundChanger)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_first"), 8, 8, 8, 8));
		}
		else
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_secend"), 8, 8, 8, 8));
		}

		GL_Listener.glListener.renderOnce(this);
	}

}
