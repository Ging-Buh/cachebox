package CB_UI.GL_UI.Activitys.FilterSettings;

import java.util.ArrayList;

import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView.FilterSetEntry;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class FilterSetListViewItem extends ListViewItemBackground
{
	private FilterSetEntry mFilterSetEntry;

	public Vector2 lastItemTouchPos;

	private ArrayList<FilterSetListViewItem> mChildList = new ArrayList<FilterSetListViewItem>();

	public FilterSetListViewItem(CB_RectF rec, int Index, FilterSetEntry fne)
	{
		super(rec, Index, fne.getName());

		this.mFilterSetEntry = fne;

	}

	public FilterSetEntry getFilterSetEntry()
	{
		return mFilterSetEntry;
	}

	public FilterSetListViewItem addChild(FilterSetListViewItem item)
	{
		mChildList.add(item);
		return item;
	}

	public void toggleChildeViewState()
	{
		if (mChildList != null && mChildList.size() > 0)
		{
			boolean newState = !mChildList.get(0).isVisible();

			for (FilterSetListViewItem tmp : mChildList)
			{
				tmp.setVisible(newState);
			}
		}

	}

	// private Member
	float left;
	float top;

	BitmapFontCache EntryName;

	@Override
	protected void render(SpriteBatch batch)
	{
		if (this.mFilterSetEntry.getItemType() != FilterSetListView.COLLAPSE_BUTTON_ITEM)
		{
			super.render(batch);
		}

		if (isPressed)
		{
			GL.that.renderOnce("");
			isPressed = GL.getIsTouchDown();
		}

		// initial
		left = getLeftWidth();
		top = this.height - this.getTopHeight();

		switch (this.mFilterSetEntry.getItemType())
		{
		case FilterSetListView.COLLAPSE_BUTTON_ITEM:
			drawCollapseButtonItem(batch);
			break;
		case FilterSetListView.CHECK_ITEM:
			drawChkItem(batch);
			break;
		case FilterSetListView.THREE_STATE_ITEM:
			drawThreeStateItem(batch);
			break;
		case FilterSetListView.NUMERICK_ITEM:
			drawNumerickItem(batch);
			break;
		}
		// draw Name
		if (EntryName == null)
		{
			EntryName = new BitmapFontCache(Fonts.getNormal());
			EntryName.setColor(Fonts.getFontColor());
			if (this.mFilterSetEntry.getItemType() == FilterSetListView.THREE_STATE_ITEM)
			{
				float TextWidth = width - (left + 20) - getRightWidth() - height;
				EntryName.setWrappedText(name, left + 20, top, TextWidth);
			}
			else
			{
				EntryName.setMultiLineText(name, left + 10, top);
			}
		}
		EntryName.draw(batch);

		if (this.mFilterSetEntry.getItemType() == FilterSetListView.NUMERICK_ITEM)
		{
			if (Value == null)
			{
				Value = new BitmapFontCache(Fonts.getBig());
				Value.setColor(Fonts.getFontColor());
				setValueFont = true;
			}
			if (setValueFont) Value.setText(String.valueOf(getValue()), (width / 1.5f), (height / 1.8f));
			setValueFont = false;

			Value.draw(batch);
		}

	}

	private static NinePatch btnBack;

	private static NinePatch btnBack_pressed;
	private static Sprite minusBtn;
	private static Sprite plusBtn;
	private boolean setValueFont = false;
	private static Sprite chkOff;
	private static Sprite chkOn;
	private static Sprite chkNo;

	private void drawCollapseButtonItem(SpriteBatch batch)
	{

		if (this.isPressed)
		{
			if (btnBack_pressed == null)
			{
				btnBack_pressed = new NinePatch(SpriteCacheBase.getThemedSprite("btn-pressed"), 16, 16, 16, 16);
			}

			btnBack_pressed.draw(batch, 0, 0, width, height);

		}
		else
		{
			if (btnBack == null)
			{
				btnBack = new NinePatch(SpriteCacheBase.getThemedSprite("btn-normal"), 16, 16, 16, 16);
			}

			btnBack.draw(batch, 0, 0, width, height);

		}

	}

	private void drawChkItem(SpriteBatch batch)
	{
		drawIcon(batch);
		drawRightChkBox(batch);
		if (this.mFilterSetEntry.getState() == 1)
		{
			if (chkOn == null)
			{
				chkOn = SpriteCacheBase.getThemedSprite("check-on");

				chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOn.draw(batch);
		}

		boolean rClick = false;
		if (this.lastItemTouchPos != null)
		{
			if (this.isPressed)
			{
				rClick = rBounds.contains(this.lastItemTouchPos);

				if (rClick) Clicked = true;
			}
			else
			{
				if (Clicked)
				{
					Clicked = false;
					rClick = rBounds.contains(this.lastItemTouchPos);
					if (rClick) stateClick();
				}
			}

		}

	}

	private void drawThreeStateItem(SpriteBatch batch)
	{
		drawIcon(batch);
		drawRightChkBox(batch);

		if (this.mFilterSetEntry.getState() == 1)
		{
			if (chkOn == null)
			{
				chkOn = SpriteCacheBase.getThemedSprite("check-on");

				chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOn.draw(batch);
		}
		else if (this.mFilterSetEntry.getState() == -1)
		{
			if (chkNo == null)
			{
				chkNo = SpriteCacheBase.getThemedSprite("delete-icon");

				chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkNo.draw(batch);
		}

		boolean rClick = false;
		if (this.lastItemTouchPos != null)
		{
			if (this.isPressed)
			{
				rClick = rBounds.contains(this.lastItemTouchPos);

				if (rClick) Clicked = true;
			}
			else
			{
				if (Clicked)
				{
					Clicked = false;
					rClick = rBounds.contains(this.lastItemTouchPos);
					if (rClick) stateClick();
				}
			}
		}

	}

	private static CB_RectF lBounds;
	private static CB_RectF rBounds;
	private static CB_RectF rChkBounds;

	private static BitmapFontCache Minus;
	private static BitmapFontCache Plus;
	private BitmapFontCache Value;
	private boolean Clicked = false;

	private void drawNumerickItem(SpriteBatch batch)
	{
		lBounds = new CB_RectF(0, 0, height, height);
		lBounds = lBounds.ScaleCenter(0.95f);

		rBounds = new CB_RectF(width - height, 0, height, height);
		rBounds = rBounds.ScaleCenter(0.95f);

		boolean rClick = false;
		boolean lClick = false;
		if (this.lastItemTouchPos != null)
		{
			if (this.isPressed)
			{
				lClick = lBounds.contains(this.lastItemTouchPos);
				rClick = rBounds.contains(this.lastItemTouchPos);

				if (lClick || rClick) Clicked = true;
			}
			else
			{
				if (Clicked)
				{
					Clicked = false;
					lClick = lBounds.contains(this.lastItemTouchPos);
					rClick = rBounds.contains(this.lastItemTouchPos);
					if (rClick) plusClick();
					if (lClick) minusClick();
				}
			}
		}

		plusBtn = rClick ? SpriteCacheBase.getThemedSprite("btn-pressed") : SpriteCacheBase.getThemedSprite("btn-normal");
		minusBtn = lClick ? SpriteCacheBase.getThemedSprite("btn-pressed") : SpriteCacheBase.getThemedSprite("btn-normal");

		minusBtn.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());

		plusBtn.setBounds(rBounds.getX(), rBounds.getY(), rBounds.getWidth(), rBounds.getHeight());

		if (Minus == null)
		{
			Minus = new BitmapFontCache(Fonts.getBig());
			Minus.setColor(Fonts.getFontColor());
			Minus.setText("-", 0, 0);
			Minus.setPosition(lBounds.getCenterPos().x - (Minus.getBounds().width / 2), lBounds.getCenterPos().y
					+ (Minus.getBounds().height / 2));
		}

		if (Plus == null)
		{
			Plus = new BitmapFontCache(Fonts.getBig());
			Plus.setColor(Fonts.getFontColor());
			Plus.setText("+", 0, 0);
			Plus.setPosition(rBounds.getCenterPos().x - (Plus.getBounds().width / 2), rBounds.getCenterPos().y
					+ (Plus.getBounds().height / 2));
		}

		// draw [-] Button
		minusBtn.draw(batch);

		// draw [+] Button
		plusBtn.draw(batch);

		// draw +/- on Button
		Minus.draw(batch);
		Plus.draw(batch);

		left += minusBtn.getWidth() + minusBtn.getX();

		if (mFilterSetEntry.getIcon() != null)
		{
			float iconHeight = this.halfHeight * 0.8f;
			float iconWidth = iconHeight * 5;
			mFilterSetEntry.getIcon().setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
			mFilterSetEntry.getIcon().draw(batch);
			// top += UiSizes.getIconSize() / 1.5;
		}

	}

	private void drawIcon(SpriteBatch batch)
	{
		if (mFilterSetEntry.getIcon() != null)
		{
			float iconHeight = this.height * 0.8f;
			float iconWidth = iconHeight;
			mFilterSetEntry.getIcon().setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
			mFilterSetEntry.getIcon().draw(batch);
			left += iconWidth + UI_Size_Base.that.getMargin() + getLeftWidth();
		}

	}

	private void drawRightChkBox(SpriteBatch batch)
	{
		if (rBounds == null || rChkBounds == null)
		{
			rBounds = new CB_RectF(width - height - 10, 5, height - 10, height - 10);// = right Button bounds

			rChkBounds = rBounds.ScaleCenter(0.8f);
		}

		if (chkOff == null)
		{
			chkOff = SpriteCacheBase.getThemedSprite("check-off");

			chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

		}

		chkOff.draw(batch);

	}

	public void plusClick()
	{
		this.mFilterSetEntry.plusClick();
		setValueFont = true;
		FilterSetListView.mustSaveFilter = true;
		this.isPressed = false;
		this.lastItemTouchPos = null;
		GL.that.renderOnce("");
	}

	public void minusClick()
	{
		this.mFilterSetEntry.minusClick();
		setValueFont = true;
		FilterSetListView.mustSaveFilter = true;
		this.isPressed = false;
		this.lastItemTouchPos = null;
		GL.that.renderOnce("");
	}

	public void stateClick()
	{
		this.mFilterSetEntry.stateClick();
		FilterSetListView.mustSaveFilter = true;
		this.isPressed = false;
		this.lastItemTouchPos = null;
		GL.that.renderOnce("");
	}

	public void setValue(int value)
	{

		this.mFilterSetEntry.setState(value);

	}

	public void setValue(float value)
	{
		this.mFilterSetEntry.setState(value);

	}

	public int getChecked()
	{
		return mFilterSetEntry.getState();
	}

	public float getValue()
	{
		return (float) mFilterSetEntry.getNumState();
	}

	public FilterSetListViewItem getChild(int i)
	{
		return mChildList.get(i);
	}

	public void setValue(boolean b)
	{
		this.mFilterSetEntry.setState(b ? 1 : 0);
	}

	public int getChildLength()
	{
		return mChildList.size();
	}

	public boolean getBoolean()
	{
		if (mFilterSetEntry.getState() == 0) return false;

		return true;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

}
