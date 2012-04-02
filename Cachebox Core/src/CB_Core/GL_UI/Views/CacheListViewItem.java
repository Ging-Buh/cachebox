package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CacheListViewItem extends ListViewItemBase
{

	/**
	 * mit ausgeschaltener scissor berechnung
	 * 
	 * @author Longri
	 */
	private class extendedCacheInfo extends CacheInfo
	{

		public extendedCacheInfo(CB_RectF rec, String Name, Cache value)
		{
			super(rec, Name, value);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
		{
			if (!disableScissor) Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);

			batch.begin();
			if (hasBackground || hasNinePatchBackground)
			{

				if (hasNinePatchBackground)
				{
					nineBackground.draw(batch, 0, 0, width, height);
				}
				else
				{
					batch.draw(Background, 0, 0, width, height);
				}

			}

			this.render(batch);
			batch.end();

			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
		}
	}

	protected extendedCacheInfo info;
	protected boolean isPressed = false;

	// protected Label mDebugIndex;

	public CacheListViewItem(CB_RectF rec, int Index, Cache cache)
	{
		super(rec, Index, cache.Name);

		info = new extendedCacheInfo(UiSizes.getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.GcCode, cache);
		info.setZeroPos();
		setBackground();
		this.addChild(info);

		// mDebugIndex = new Label(150, 3, 50, 20, "DebugLabel");
		// mDebugIndex.setText("" + Index);
		// this.addChild(mDebugIndex);
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

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;

		return false;
	}

	private void setBackground()
	{

		Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);

		if (isSelected)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_selected"), 8, 8, 8, 8));
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
