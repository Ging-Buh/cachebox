package de.cachebox_test.Views.FilterSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import CB_Core.Types.GpxFilename;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Views.FilterSettings.CategorieListView.CategorieEntry;

public class CategorieListViewItem extends View
{
	public CategorieEntry categorieEntry;

	private static int width;
	private static int height = 0;
	private Context mContext;
	private boolean BackColorChanger = false;
	private StaticLayout layoutEntryName;
	private StaticLayout layoutEntryDate;
	private StaticLayout layoutEntryCount;
	private Resources mRes;
	private ArrayList<CategorieListViewItem> mChildList = new ArrayList<CategorieListViewItem>();

	private static TextPaint textPaint;

	public CategorieListViewItem(Context context, CategorieEntry fne, Boolean BackColorId)
	{
		super(context);
		mContext = context;
		mRes = mContext.getResources();
		this.categorieEntry = fne;
		BackColorChanger = BackColorId;

		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setTextSize(UiSizes.getScaledFontSize());
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

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
			int newState = (mChildList.get(0).getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;

			for (CategorieListViewItem tmp : mChildList)
			{
				tmp.setVisibility(newState);
			}
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		width = PresetListView.windowW;

		height = UiSizes.getIconSize() + UiSizes.getCornerSize() * 4;

		setMeasuredDimension(width, height);

	}

	// Draw Methods

	// static Member

	private static final SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	// private Member
	int left;
	int top;
	int BackgroundColor;

	private static CB_Rect lPinBounds;
	private static CB_Rect rBounds;
	private static CB_Rect rChkBounds;
	private static int halfSize = 0;

	@Override
	protected void onDraw(Canvas canvas)
	{

		// initial
		left = UiSizes.getCornerSize();
		top = UiSizes.getCornerSize();

		if (rBounds == null || rChkBounds == null || lPinBounds == null)
		{
			rBounds = new CB_Rect(width - height - 10, 5, height - 10, height - 10);// =
			// right
			// Button
			// bounds
			halfSize = rBounds.getWidth() / 4;

			rChkBounds = rBounds.ScaleCenter(0.8);
			lPinBounds = new CB_Rect(rChkBounds);
			lPinBounds.offset(-(width - (halfSize * 2) - rChkBounds.getWidth()), 0);
		}

		if (layoutEntryName == null)
		{
			int innerWidth = (width - (UiSizes.getIconAddCorner() + rChkBounds.getWidth())) + halfSize;
			int innerWidthName = innerWidth - rBounds.getWidth();
			GpxFilename file = categorieEntry.getFile();

			String Name = "";
			String Date = "";
			String Count = "";
			int Collaps = 0;
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
				Collaps = UiSizes.getCornerSize();
			}
			innerWidth += Collaps;
			innerWidthName += Collaps;
			Count += " Caches";

			layoutEntryName = new StaticLayout(Name, textPaint, innerWidthName, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			// layoutEntryDate = new StaticLayout(Date, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			// layoutEntryCount = new StaticLayout(Count, textPaint, innerWidth, Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, false);

			layoutEntryDate = new StaticLayout(Date, textPaint, innerWidthName, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			layoutEntryCount = new StaticLayout(Count, textPaint, innerWidthName, Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, false);
		}

		textPaint.setColor(Global.getColor(R.attr.TextColor));

		boolean selected = false;
		if (this.categorieEntry == CategorieListView.aktCategorieEntry) selected = true;

		if (BackColorChanger)
		{
			BackgroundColor = (selected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
		}
		else
		{
			BackgroundColor = (selected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground_secend);
		}

		if (this.categorieEntry.getItemType() != FilterSetListView.COLLABSE_BUTTON_ITEM)
		{
			ActivityUtils.drawFillRoundRecWithBorder(canvas, new CB_Rect(5, 5, width - 10, height - 10), 2,
					Global.getColor(R.attr.ListSeparator), BackgroundColor, UiSizes.getCornerSize());
		}

		switch (this.categorieEntry.getItemType())
		{
		case FilterSetListView.COLLABSE_BUTTON_ITEM:
			drawCollabseButtonItem(canvas);
			break;
		case FilterSetListView.CHECK_ITEM:
			drawChkItem(canvas);
			break;
		case FilterSetListView.THREE_STATE_ITEM:
			drawThreeStateItem(canvas);
			break;

		}
		// draw Name
		ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left, top);

		// draw Count
		ActivityUtils.drawStaticLayout(canvas, layoutEntryCount, left, top);

		// draw Import Date
		top += 52;
		ActivityUtils.drawStaticLayout(canvas, layoutEntryDate, left, top);

	}

	private static Drawable btnBack;

	private void drawCollabseButtonItem(Canvas canvas)
	{

		btnBack = mRes.getDrawable(main.N ? R.drawable.night_btn_default_normal : R.drawable.day_btn_default_normal);

		Rect bounds = new Rect(3, 7, width - 3, height - 7);
		btnBack.setBounds(bounds);

		left += 70;

		btnBack.draw(canvas);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, Global.getColor(R.attr.ListSeparator), Color.TRANSPARENT,
				UiSizes.getCornerSize());

		int ChkState = this.categorieEntry.getCat().getChek();

		if (ChkState == 1) ActivityUtils.drawIconBounds(canvas, Global.Icons[27], rChkBounds.ScaleCenter(0.8));
		if (ChkState == -1) ActivityUtils.drawIconBounds(canvas, Global.Icons[39], rChkBounds.ScaleCenter(0.8));

		drawPin(canvas);
	}

	private void drawPin(Canvas canvas)
	{
		if (this.getCategorieEntry().getCat().pinned)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[37], lPinBounds.ScaleCenter(0.7).offset(-10, 0));
		}
		else
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[38], lPinBounds.ScaleCenter(0.7).offset(-10, 0));
		}

	}

	private void drawChkItem(Canvas canvas)
	{
		drawIcon(canvas);
		drawRightChkBox(canvas);
		if (this.categorieEntry.getState() == 1)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[27], rChkBounds.ScaleCenter(0.8));
		}

	}

	private void drawThreeStateItem(Canvas canvas)
	{
		drawIcon(canvas);
		drawRightChkBox(canvas);
		if (this.categorieEntry.getState() == 1)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[27], rChkBounds.ScaleCenter(0.8));
		}
		else if (this.categorieEntry.getState() == -1)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[28], rChkBounds.ScaleCenter(0.9));
		}
	}

	private void drawIcon(Canvas canvas)
	{
		if (categorieEntry.getIcon() != null) ActivityUtils.PutImageTargetHeight(canvas, categorieEntry.getIcon(), left, top,
				UiSizes.getIconSize());
		left += UiSizes.getIconAddCorner();

	}

	private void drawRightChkBox(Canvas canvas)
	{

		ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, Global.getColor(R.attr.ListSeparator), BackgroundColor,
				UiSizes.getCornerSize());
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

}
