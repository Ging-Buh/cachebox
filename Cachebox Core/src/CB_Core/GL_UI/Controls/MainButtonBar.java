package CB_Core.GL_UI.Controls;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.NinePatch;

/**
 * Nimmt die CB Haupt Buttons auf. </br> Die Anzahl kann variieren. </br>Bei Phones = 5</br> Bei Tablets mehr
 * 
 * @author Longri
 */
public class MainButtonBar extends CB_View_Base
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

	@Override
	protected void Initial()
	{
		// ListButton
		mButtons.get(0).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("db"), 0, 0, 0, 0));
		mButtons.get(0).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("db_pressed"), 0, 0, 0, 0));

		// CacheButton
		mButtons.get(1).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("cache"), 0, 0, 0, 0));
		mButtons.get(1).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("cache_pressed"), 0, 0, 0, 0));

		// NavButton
		mButtons.get(2).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("Nav"), 0, 0, 0, 0));
		mButtons.get(2).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("Nav_pressed"), 0, 0, 0, 0));

		// ToolsButton
		mButtons.get(3).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("tool"), 0, 0, 0, 0));
		mButtons.get(3).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("tool_pressed"), 0, 0, 0, 0));

		// MiscButton
		mButtons.get(4).setninePatch(new NinePatch(SpriteCache.uiAtlas.findRegion("misc"), 0, 0, 0, 0));
		mButtons.get(4).setninePatchPressed(new NinePatch(SpriteCache.uiAtlas.findRegion("misc_pressed"), 0, 0, 0, 0));

	}

}
