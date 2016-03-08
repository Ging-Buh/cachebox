package CB_UI.GL_UI.Activitys.FilterSettings;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView.FilterSetEntry;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class FilterSetListViewItem extends ListViewItemBackground {
	private final FilterSetEntry mFilterSetEntry;

	public Vector2 lastItemTouchPos;

	private final ArrayList<FilterSetListViewItem> mChildList = new ArrayList<FilterSetListViewItem>();

	public FilterSetListViewItem(CB_RectF rec, int Index, FilterSetEntry fne) {
		super(rec, Index, fne.getName());

		this.mFilterSetEntry = fne;

	}

	public FilterSetEntry getFilterSetEntry() {
		return mFilterSetEntry;
	}

	public FilterSetListViewItem addChild(FilterSetListViewItem item) {
		mChildList.add(item);
		return item;
	}

	public void toggleChildeViewState() {
		if (mChildList != null && mChildList.size() > 0) {
			boolean newState = !mChildList.get(0).isVisible();

			for (FilterSetListViewItem tmp : mChildList) {
				tmp.setVisible(newState);
			}
		}

	}

	// private Member
	float left;
	float top;

	BitmapFontCache EntryName;

	@Override
	protected void render(Batch batch) {
		if (this.mFilterSetEntry.getItemType() != FilterSetListView.COLLAPSE_BUTTON_ITEM) {
			super.render(batch);
		}

		try {
			if (isPressed) {
				GL.that.renderOnce();
				isPressed = GL.getIsTouchDown();
			}

			// initial
			left = getLeftWidth();
			//top = this.getHeight() - this.getTopHeight();
			top = (this.getHeight() + Fonts.getNormal().getLineHeight()) / 2f; //this.getTopHeight();

			switch (this.mFilterSetEntry.getItemType()) {
			case FilterSetListView.COLLAPSE_BUTTON_ITEM:
				drawCollapseButtonItem(batch);
				break;
			case FilterSetListView.CHECK_ITEM:
				drawChkItem(batch);
				break;
			case FilterSetListView.THREE_STATE_ITEM:
				drawThreeStateItem(batch);
				break;
			case FilterSetListView.NUMERIC_ITEM:
				top = this.getHeight() - this.getTopHeight();
				drawNumericItem(batch);
				break;
			}
			// draw Name
			if (EntryName == null) {
				EntryName = new BitmapFontCache(Fonts.getNormal());
				EntryName.setColor(COLOR.getFontColor());
				if (this.mFilterSetEntry.getItemType() == FilterSetListView.THREE_STATE_ITEM) {
					float TextWidth = getWidth() - (left + 20) - getRightWidth() - getHeight();
					EntryName.setText(name, left + 20, top, TextWidth, Align.left, true);
				} else {
					EntryName.setText(name, left + 10, top);
				}
			}
			EntryName.draw(batch);

			if (this.mFilterSetEntry.getItemType() == FilterSetListView.NUMERIC_ITEM) {
				if (Value == null) {
					Value = new BitmapFontCache(Fonts.getBig());
					Value.setColor(COLOR.getFontColor());
					setValueFont = true;
				}
				if (setValueFont)
					Value.setText(String.valueOf(getValue()), (getWidth() / 1.5f), (getHeight() / 1.8f));
				setValueFont = false;

				Value.draw(batch);
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	private void drawCollapseButtonItem(Batch batch) {

		if (this.isPressed) {
			if (btnBack_pressed == null) {
				btnBack_pressed = new NinePatch(Sprites.getSprite("btn-pressed"), 16, 16, 16, 16);
			}

			btnBack_pressed.draw(batch, 0, 0, getWidth(), getHeight());

		} else {
			if (btnBack == null) {
				btnBack = new NinePatch(Sprites.getSprite(IconName.btnNormal.name()), 16, 16, 16, 16);
			}

			btnBack.draw(batch, 0, 0, getWidth(), getHeight());

		}

	}

	private void drawChkItem(Batch batch) {
		drawIcon(batch);
		drawRightChkBox(batch);
		if (this.mFilterSetEntry.getState() == 1) {
			if (chkOn == null) {
				chkOn = Sprites.getSprite("check-on");

				chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOn.draw(batch);
		}

		boolean rClick = false;
		if (this.lastItemTouchPos != null) {
			if (this.isPressed) {
				rClick = rBounds.contains(this.lastItemTouchPos);

				if (rClick)
					Clicked = true;
			} else {
				if (Clicked) {
					Clicked = false;
					rClick = rBounds.contains(this.lastItemTouchPos);
					if (rClick)
						stateClick();
				}
			}

		}

	}

	private void drawThreeStateItem(Batch batch) {
		drawIcon(batch);
		drawRightChkBox(batch);

		if (this.mFilterSetEntry.getState() == 1) {
			if (chkOn == null) {
				chkOn = Sprites.getSprite("check-on");

				chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOn.draw(batch);
		} else if (this.mFilterSetEntry.getState() == -1) {
			if (chkNo == null) {
				chkNo = Sprites.getSprite(IconName.DELETE.name());

				chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkNo.draw(batch);
		}

		boolean rClick = false;
		if (this.lastItemTouchPos != null) {
			if (this.isPressed) {
				rClick = rBounds.contains(this.lastItemTouchPos);

				if (rClick)
					Clicked = true;
			} else {
				if (Clicked) {
					Clicked = false;
					rClick = rBounds.contains(this.lastItemTouchPos);
					if (rClick)
						stateClick();
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

	private void drawNumericItem(Batch batch) {
		lBounds = new CB_RectF(0, 0, getHeight(), getHeight());
		lBounds = lBounds.ScaleCenter(0.95f);

		rBounds = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight());
		rBounds = rBounds.ScaleCenter(0.95f);

		boolean rClick = false;
		boolean lClick = false;
		if (this.lastItemTouchPos != null) {
			if (this.isPressed) {
				lClick = lBounds.contains(this.lastItemTouchPos);
				rClick = rBounds.contains(this.lastItemTouchPos);

				if (lClick || rClick)
					Clicked = true;
			} else {
				if (Clicked) {
					Clicked = false;
					lClick = lBounds.contains(this.lastItemTouchPos);
					rClick = rBounds.contains(this.lastItemTouchPos);
					if (rClick)
						plusClick();
					if (lClick)
						minusClick();
				}
			}
		}

		plusBtn = rClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());
		minusBtn = lClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());

		minusBtn.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());

		plusBtn.setBounds(rBounds.getX(), rBounds.getY(), rBounds.getWidth(), rBounds.getHeight());

		if (Minus == null) {
			Minus = new BitmapFontCache(Fonts.getBig());
			Minus.setColor(COLOR.getFontColor());
			Minus.setText("-", 0, 0);
			Minus.setPosition(lBounds.getCenterPosX() - (Minus.getLayouts().first().width / 2), lBounds.getCenterPosY() + (Minus.getLayouts().first().height / 2));
		}

		if (Plus == null) {
			Plus = new BitmapFontCache(Fonts.getBig());
			Plus.setColor(COLOR.getFontColor());
			Plus.setText("+", 0, 0);
			Plus.setPosition(rBounds.getCenterPosX() - (Plus.getLayouts().first().width / 2), rBounds.getCenterPosY() + (Plus.getLayouts().first().height / 2));
		}

		// draw [-] Button
		minusBtn.draw(batch);

		// draw [+] Button
		plusBtn.draw(batch);

		// draw +/- on Button
		Minus.draw(batch);
		Plus.draw(batch);

		left += minusBtn.getWidth() + minusBtn.getX();

		if (mFilterSetEntry.getIcon() != null) {
			float iconHeight = this.getHalfHeight() * 0.8f;
			float iconWidth = iconHeight * 5;
			mFilterSetEntry.getIcon().setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
			mFilterSetEntry.getIcon().draw(batch);
			// top += UiSizes.getIconSize() / 1.5;
		}

	}

	private void drawIcon(Batch batch) {
		if (mFilterSetEntry.getIcon() != null) {
			float iconHeight = this.getHeight() * 0.8f;
			float iconWidth = iconHeight;
			float y = (this.getHeight() - iconHeight) / 2f; // UI_Size_Base.that.getMargin()
			mFilterSetEntry.getIcon().setBounds(left, y, iconWidth, iconHeight);
			// mFilterSetEntry.getIcon().setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
			mFilterSetEntry.getIcon().draw(batch);
			left += iconWidth + UI_Size_Base.that.getMargin() + getLeftWidth();
		}

	}

	private void drawRightChkBox(Batch batch) {
		if (rBounds == null || rChkBounds == null) {
			rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// = right Button bounds

			rChkBounds = rBounds.ScaleCenter(0.8f);
		}

		if (chkOff == null) {
			chkOff = Sprites.getSprite("check-off");

			chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

		}

		chkOff.draw(batch);

	}

	public void plusClick() {
		this.mFilterSetEntry.plusClick();
		setValueFont = true;
		FilterSetListView.mustSaveFilter = true;
		this.isPressed = false;
		this.lastItemTouchPos = null;
		GL.that.renderOnce();
	}

	public void minusClick() {
		this.mFilterSetEntry.minusClick();
		setValueFont = true;
		FilterSetListView.mustSaveFilter = true;
		this.isPressed = false;
		this.lastItemTouchPos = null;
		GL.that.renderOnce();
	}

	public void stateClick() {
		this.mFilterSetEntry.stateClick();
		FilterSetListView.mustSaveFilter = true;
		this.isPressed = false;
		this.lastItemTouchPos = null;
		GL.that.renderOnce();
	}

	public void setValue(int value) {

		this.mFilterSetEntry.setState(value);

	}

	public void setValue(float value) {
		this.mFilterSetEntry.setState(value);

	}

	public int getChecked() {
		return mFilterSetEntry.getState();
	}

	public float getValue() {
		return (float) mFilterSetEntry.getNumState();
	}

	@Override
	public FilterSetListViewItem getChild(int i) {
		return mChildList.get(i);
	}

	public void setValue(boolean b) {
		this.mFilterSetEntry.setState(b ? 1 : 0);
	}

	public int getChildLength() {
		return mChildList.size();
	}

	public boolean getBoolean() {
		if (mFilterSetEntry.getState() == 0)
			return false;

		return true;
	}

	@Override
	protected void SkinIsChanged() {

	}

}
