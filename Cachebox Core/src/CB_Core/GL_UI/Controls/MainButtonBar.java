package CB_Core.GL_UI.Controls;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Nimmt die CB Haupt Buttons auf. </br> Die Anzahl kann variieren. </br>Bei Phones = 5</br> Bei Tablets mehr
 * 
 * @author Longri
 */
public class MainButtonBar extends GL_View_Base
{

	ArrayList<MainButton> mButtons;

	public MainButtonBar(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);

		this.isClickable = true;

		CB_RectF btnRec = new CB_RectF(0, 0, UiSizes.RefWidth / 5, rec.getHeight());
		mButtons = new ArrayList<MainButton>();
		mButtons.add(new MainButton(btnRec, "mainListBtn"));
		mButtons.add(new MainButton(btnRec, "mainCacheBtn"));
		mButtons.add(new MainButton(btnRec, "mainNavBtn"));
		mButtons.add(new MainButton(btnRec, "mainToolBtn"));
		mButtons.add(new MainButton(btnRec, "mainSettingBtn"));

		// Tab Layout

		mButtons.add(new MainButton(btnRec, "mainTabBtn1"));
		mButtons.add(new MainButton(btnRec, "mainTabBtn2"));
		mButtons.add(new MainButton(btnRec, "mainTabBtn3"));
		mButtons.add(new MainButton(btnRec, "mainTabBtn4"));
		mButtons.add(new MainButton(btnRec, "mainTabBtn5"));

		int xPos = 0;
		for (Iterator<MainButton> iterator = mButtons.iterator(); iterator.hasNext();)
		{
			MainButton tmp = iterator.next();
			tmp.setX(xPos);
			tmp.setOnClickListener(mClickListner);
			this.addChild(tmp);
			xPos += tmp.getWidth();
		}

		setBtnImages();

		requestLayout();
	}

	private OnClickListener mClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			Logger.LogCat("MainButtonBar " + v.getName() + " Clicked");
			return false;
		}
	};

	private void setBtnImages()
	{
		// ListButton
		mButtons.get(0).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("db"), 16, 16, 16, 16));
		mButtons.get(0).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("db_pressed"), 16, 16, 16, 16));

		// CacheButton
		mButtons.get(1).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("cache"), 16, 16, 16, 16));
		mButtons.get(1).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("cache_pressed"), 16, 16, 16, 16));

		// NavButton
		mButtons.get(2).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("Nav"), 16, 16, 16, 16));
		mButtons.get(2).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("Nav_pressed"), 16, 16, 16, 16));

		// ToolsButton
		mButtons.get(3).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("tool"), 16, 16, 16, 16));
		mButtons.get(3).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("tool_pressed"), 16, 16, 16, 16));

		// MiscButton
		mButtons.get(4).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("misc"), 16, 16, 16, 16));
		mButtons.get(4).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("misc_pressed"), 16, 16, 16, 16));

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		// nichts zu tun

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

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

	private void requestLayout()
	{

	}

}
