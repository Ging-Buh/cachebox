package CB_Core.GL_UI.Activitys.FilterSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.FilterSettings.CategorieListView.CategorieEntry;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.GpxFilename;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class CategorieListViewItem extends ListViewItemBackground
{
	public CategorieEntry categorieEntry;
	public Vector2 lastItemTouchPos;
	private boolean Clicked = false;

	private BitmapFontCache EntryName;
	private BitmapFontCache EntryDate;
	private BitmapFontCache EntryCount;
	private static Sprite chkOff;
	private static Sprite chkOn;
	private static Sprite chkNo;
	private static Sprite chkBox;

	private ArrayList<CategorieListViewItem> mChildList = new ArrayList<CategorieListViewItem>();

	public CategorieListViewItem(CB_RectF rec, int Index, CategorieEntry fne)
	{
		super(rec, Index, "");

		this.categorieEntry = fne;

	}

	public CategorieEntry getCategorieEntry()
	{
		return categorieEntry;
	}

	public CategorieListViewItem addChild(CategorieListViewItem item)
	{
		mChildList.add(item);
		return item;
	}

	public void toggleChildeViewState()
	{
		if (mChildList != null && mChildList.size() > 0)
		{
			int newState = (mChildList.get(0).getVisibility() == CB_View_Base.VISIBLE) ? CB_View_Base.INVISIBLE : CB_View_Base.VISIBLE;

			for (CategorieListViewItem tmp : mChildList)
			{
				tmp.setVisibility(newState);
			}
		}

	}

	// Draw Methods

	// static Member

	private static final SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy hh:mm ");
	// private Member
	float left;
	float top;

	private static CB_RectF lPinBounds;
	private static CB_RectF rBounds;
	private static CB_RectF rChkBounds;
	private static float halfSize = 0;

	@Override
	protected void render(SpriteBatch batch)
	{
		if (this.categorieEntry.getItemType() != FilterSetListView.COLLABSE_BUTTON_ITEM) super.render(batch);

		if (isPressed)
		{
			isPressed = GL_Listener.isTouchDown();
		}

		// initial
		left = LeftWidth;
		top = this.height - TopHight;

		if (rBounds == null || rChkBounds == null || lPinBounds == null)
		{
			rBounds = new CB_RectF(width - height - 10, 5, height - 10, height - 10);// =
			// right
			// Button
			// bounds
			halfSize = rBounds.getWidth() / 4;

			rChkBounds = rBounds.ScaleCenter(0.8f);
			lPinBounds = new CB_RectF(rChkBounds);
			lPinBounds.offset(-(width - (halfSize * 2) - rChkBounds.getWidth()), 0);
		}

		boolean selected = false;
		if (this.categorieEntry == CategorieListView.aktCategorieEntry) selected = true;

		switch (this.categorieEntry.getItemType())
		{
		case FilterSetListView.COLLABSE_BUTTON_ITEM:
			drawCollabseButtonItem(batch);
			break;
		case FilterSetListView.CHECK_ITEM:
			drawChkItem(batch);
			break;
		case FilterSetListView.THREE_STATE_ITEM:
			drawThreeStateItem(batch);
			break;

		}
		// draw Name
		if (EntryName == null)
		{

			GpxFilename file = categorieEntry.getFile();

			String Name = "";
			String Date = "";
			String Count = "";

			if (file != null)
			{
				Name = file.GpxFileName;
				Date = postFormater.format(file.Imported);
				Count = String.valueOf(file.CacheCount);
			}
			else
			{
				Name = categorieEntry.getCatName();
				Date = postFormater.format(categorieEntry.getCat().LastImported());
				Count = String.valueOf(categorieEntry.getCat().CacheCount());
			}

			Count += " Caches";

			EntryName = new BitmapFontCache(Fonts.getNormal());
			EntryName.setMultiLineText(Name, left + UiSizes.getMargin(), top);

			top = margin + margin + Fonts.MesureSmall(Count).height;

			EntryDate = new BitmapFontCache(Fonts.getSmall());
			EntryDate.setMultiLineText(Date, left + UiSizes.getMargin(), top);

			float mesure = Fonts.Mesure(Count).width;
			EntryCount = new BitmapFontCache(Fonts.getSmall());
			EntryCount.setMultiLineText(Count, rBounds.getX() - margin - mesure, top);

			// layoutEntryName = new StaticLayout(Name, textPaint, innerWidthName, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			//
			// layoutEntryDate = new StaticLayout(Date, textPaint, innerWidthName, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			// layoutEntryCount = new StaticLayout(Count, textPaint, innerWidthName, Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, false);
		}

		if (EntryName != null) EntryName.draw(batch);
		if (EntryCount != null) EntryCount.draw(batch);
		if (EntryDate != null) EntryDate.draw(batch);

		// draw Count
		// ActivityUtils.drawStaticLayout(batch, layoutEntryCount, left, top);

		// draw Import Date
		top += 52;
		// ActivityUtils.drawStaticLayout(batch, layoutEntryDate, left, top);

	}

	private static NinePatch btnBack;
	private static NinePatch btnBack_pressed;

	private void drawCollabseButtonItem(SpriteBatch batch)
	{

		if (this.isPressed)
		{
			if (btnBack_pressed == null)
			{
				btnBack_pressed = new NinePatch(SpriteCache.getThemedSprite("btn-pressed"), 16, 16, 16, 16);
			}

			btnBack_pressed.draw(batch, 0, 0, width, height);

		}
		else
		{
			if (btnBack == null)
			{
				btnBack = new NinePatch(SpriteCache.getThemedSprite("btn-normal"), 16, 16, 16, 16);
			}

			btnBack.draw(batch, 0, 0, width, height);

		}

		drawPin(batch);
		drawChkItem(batch);

	}

	private static Sprite sPinOn;
	private static Sprite sPinOff;
	private static float margin = 0;

	private void drawPin(SpriteBatch batch)
	{
		margin = UiSizes.getMargin();
		float iconHeight = this.height * 0.6f;
		float iconWidth = iconHeight;

		if (this.getCategorieEntry().getCat().pinned)
		{
			if (sPinOn == null)
			{
				sPinOn = SpriteCache.getThemedSprite("pin-icon");
				sPinOn.setBounds(left, UiSizes.getMargin(), iconWidth, iconHeight);
			}

			sPinOn.draw(batch);
		}
		else
		{
			if (sPinOff == null)
			{
				sPinOff = SpriteCache.getThemedSprite("pin-icon-disable");
				sPinOff.setBounds(left, UiSizes.getMargin(), iconWidth, iconHeight);
			}
			sPinOff.draw(batch);

		}

		left += iconWidth + UiSizes.getMargin();

	}

	private void drawChkItem(SpriteBatch batch)
	{
		if (this.categorieEntry == null) return;

		drawIcon(batch);
		drawRightChkBox(batch);
		int ChkState = 0;
		if (this.categorieEntry.getItemType() == FilterSetListView.COLLABSE_BUTTON_ITEM)
		{
			ChkState = this.categorieEntry.getCat().getChek();
		}
		else
		{
			ChkState = this.categorieEntry.getState();
		}

		if (ChkState == 1)
		{
			if (chkOn == null)
			{
				chkOn = SpriteCache.getThemedSprite("check-on");

				chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOn.draw(batch);
		}
		if (ChkState == -1)
		{
			if (chkOff == null)
			{
				chkOff = SpriteCache.getThemedSprite("check-disable");

				chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOff.draw(batch);
		}

		boolean rClick = false;
		if (this.isPressed)
		{
			rClick = rBounds.contains(this.lastItemTouchPos);

			if (rClick) Clicked = true;
		}
		else
		{
			if (Clicked && this.categorieEntry.getItemType() != FilterSetListView.COLLABSE_BUTTON_ITEM)
			{
				Clicked = false;
				rClick = rBounds.contains(this.lastItemTouchPos);
				if (rClick) stateClick();
			}
		}

	}

	private void drawThreeStateItem(SpriteBatch batch)
	{
		drawIcon(batch);
		drawRightChkBox(batch);

		if (this.categorieEntry.getCat().getChek() == 1)
		{
			if (chkOn == null)
			{
				chkOn = SpriteCache.getThemedSprite("check-on");

				chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkOn.draw(batch);
		}
		else if (this.categorieEntry.getCat().getChek() == 0)
		{
			if (chkNo == null)
			{
				chkNo = SpriteCache.getThemedSprite("delete-icon");

				chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

			}

			chkNo.draw(batch);
		}

		boolean rClick = false;
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

	private void drawIcon(SpriteBatch batch)
	{
		// if (categorieEntry.getIcon() != null) ActivityUtils.PutImageTargetHeight(batch, categorieEntry.getIcon(), left, top,
		// UiSizes.getIconSize());
		// left += UiSizes.getIconAddCorner();

	}

	private void drawRightChkBox(SpriteBatch batch)
	{

		if (rBounds == null || rChkBounds == null)
		{
			rBounds = new CB_RectF(width - height - margin, margin, height - margin, height - margin);// = right Button bounds

			rChkBounds = rBounds.ScaleCenter(0.8f);
		}

		if (chkBox == null)
		{
			chkBox = SpriteCache.getThemedSprite("check-off");

			chkBox.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

		}

		chkBox.draw(batch);

	}

	public void plusClick()
	{
		this.categorieEntry.plusClick();

	}

	public void minusClick()
	{
		this.categorieEntry.minusClick();
	}

	public void stateClick()
	{
		this.categorieEntry.stateClick();
	}

	public void setValue(int value)
	{

		this.categorieEntry.setState(value);

	}

	public void setValue(float value)
	{
		this.categorieEntry.setState(value);

	}

	public int getChecked()
	{
		return categorieEntry.getState();
	}

	public float getValue()
	{
		return (float) categorieEntry.getNumState();
	}

	public CategorieListViewItem getChild(int i)
	{
		return mChildList.get(i);
	}

	public void setValue(boolean b)
	{
		this.categorieEntry.setState(b ? 1 : 0);
	}

	public int getChildLength()
	{
		return mChildList.size();
	}

	public boolean getBoolean()
	{
		if (categorieEntry.getState() == 0) return false;

		return true;
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}
