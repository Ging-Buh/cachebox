package de.cachebox_test.Views.FilterSettings;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Views.FilterSettings.FilterSetListView.FilterSetEntry;

public class FilterSetListViewItem extends View
{
	private FilterSetEntry mFilterSetEntry;

	private static int width;
	private static int height = 0;
	private Context mContext;
	private boolean BackColorChanger = false;
	private StaticLayout layoutEntryName;
	private Resources mRes;
	private ArrayList<FilterSetListViewItem> mChildList = new ArrayList<FilterSetListViewItem>();

	private static TextPaint textPaint;

	public FilterSetListViewItem(Context context, FilterSetEntry fne, Boolean BackColorId)
	{
		super(context);
		mContext = context;
		mRes = mContext.getResources();
		this.mFilterSetEntry = fne;
		BackColorChanger = BackColorId;

		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setTextSize(UiSizes.getScaledFontSize());
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

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
			int newState = (mChildList.get(0).getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;

			for (FilterSetListViewItem tmp : mChildList)
			{
				tmp.setVisibility(newState);
			}
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		width = PresetListView.windowW;

		height = UiSizes.getIconSize() + UiSizes.getCornerSize() * 2;
		setMeasuredDimension(width, height);

	}

	// Draw Methods

	// static Member
	private static Paint TextPaint;

	// private Member
	int left;
	int top;
	int BackgroundColor;

	@Override
	protected void onDraw(Canvas canvas)
	{

		// initial
		left = UiSizes.getCornerSize();
		top = UiSizes.getCornerSize();

		TextPaint = new Paint();
		TextPaint.setAntiAlias(true);
		TextPaint.setFakeBoldText(true);
		TextPaint.setTextSize((float) (UiSizes.getScaledFontSize_big()));
		TextPaint.setColor(Global.getColor(R.attr.TextColor));

		int innerWidth = width - (UiSizes.getCornerSize() * 4) - (UiSizes.getIconSize() * 2);
		layoutEntryName = new StaticLayout(mFilterSetEntry.getName(), textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

		textPaint.setColor(Global.getColor(R.attr.TextColor));

		boolean selected = false;
		if (this.mFilterSetEntry == FilterSetListView.aktFilterSetEntry) selected = true;

		if (BackColorChanger)
		{
			BackgroundColor = (selected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
		}
		else
		{
			BackgroundColor = (selected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground_secend);
		}

		if (this.mFilterSetEntry.getItemType() != FilterSetListView.COLLABSE_BUTTON_ITEM)
		{
			ActivityUtils.drawFillRoundRecWithBorder(canvas, new CB_Rect(5, 5, width - 10, height - 10), 2,
					Global.getColor(R.attr.ListSeparator), BackgroundColor, UiSizes.getCornerSize());
		}

		switch (this.mFilterSetEntry.getItemType())
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
		case FilterSetListView.NUMERICK_ITEM:
			drawNumerickItem(canvas);
			break;
		}
		// draw Name
		if (this.mFilterSetEntry.getItemType() == FilterSetListView.THREE_STATE_ITEM)
		{
			left += ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left - 20, top);
		}
		else
		{
			left += ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left + 10, top + 10);
		}

		if (this.mFilterSetEntry.getItemType() == FilterSetListView.NUMERICK_ITEM)
		{
			canvas.drawText(String.valueOf(this.mFilterSetEntry.getNumState()), (float) (width / 1.5), (float) (height / 1.8), TextPaint);
		}

	}

	private static Drawable btnBack;
	// private static Drawable btnBack_pressed;
	private static Drawable minusBtn;
	private static Drawable plusBtn;

	private void drawCollabseButtonItem(Canvas canvas)
	{

		btnBack = mRes.getDrawable(Config.settings.nightMode.getValue() ? R.drawable.night_btn_default_normal
				: R.drawable.day_btn_default_normal);

		Rect bounds = new Rect(3, 7, width - 3, height);
		btnBack.setBounds(bounds);

		btnBack.draw(canvas);

	}

	private void drawChkItem(Canvas canvas)
	{
		drawIcon(canvas);
		drawRightChkBox(canvas);
		if (this.mFilterSetEntry.getState() == 1)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[27], rChkBounds.ScaleCenter(0.8));
		}
		else
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[39], rChkBounds.ScaleCenter(0.8));
		}

	}

	private void drawThreeStateItem(Canvas canvas)
	{
		drawIcon(canvas);
		drawRightChkBox(canvas);
		if (this.mFilterSetEntry.getState() == 1)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[27], rChkBounds.ScaleCenter(0.8));
		}
		else if (this.mFilterSetEntry.getState() == -1)
		{
			ActivityUtils.drawIconBounds(canvas, Global.Icons[28], rChkBounds.ScaleCenter(0.9));
		}
	}

	private static TextPaint mTextPaint;
	private static StaticLayout layoutPlus;
	private static StaticLayout layoutMinus;
	private static CB_Rect lBounds;
	private static CB_Rect rBounds;
	private static CB_Rect rChkBounds;

	private void drawNumerickItem(Canvas canvas)
	{

		plusBtn = mRes.getDrawable(Config.settings.nightMode.getValue() ? R.drawable.night_btn_default_normal
				: R.drawable.day_btn_default_normal);
		minusBtn = mRes.getDrawable(Config.settings.nightMode.getValue() ? R.drawable.night_btn_default_normal
				: R.drawable.day_btn_default_normal);

		lBounds = new CB_Rect(7, 7, height - 7, height - 7);
		minusBtn.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());

		rBounds = new CB_Rect(width - height - 7, 7, height - 7, height - 7);
		plusBtn.setBounds(rBounds.getX(), rBounds.getY(), rBounds.getWidth(), rBounds.getHeight());

		mTextPaint = new TextPaint();
		mTextPaint.setTextSize(UiSizes.getScaledFontSize() * 3);
		mTextPaint.setColor(Global.getColor(R.attr.TextColor));
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);

		layoutMinus = new StaticLayout("-", mTextPaint, height - 7, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
		layoutPlus = new StaticLayout("+", mTextPaint, height - 7, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

		// draw [-] Button
		minusBtn.draw(canvas);

		// draw [+] Button
		plusBtn.draw(canvas);

		// draw +/- on Button
		ActivityUtils.drawStaticLayout(canvas, layoutMinus, 4, top - 3);
		ActivityUtils.drawStaticLayout(canvas, layoutPlus, width - 5 - height, top);

		left += minusBtn.getBounds().width() + minusBtn.getBounds().left;

		if (mFilterSetEntry.getIcon() != null)
		{
			ActivityUtils.PutImageTargetHeight(canvas, mFilterSetEntry.getIcon(), left, top, UiSizes.getIconSize() / 2);
			top += UiSizes.getIconSize() / 1.5;
		}

	}

	private void drawIcon(Canvas canvas)
	{
		if (mFilterSetEntry.getIcon() != null) left += ActivityUtils.PutImageTargetHeight(canvas, mFilterSetEntry.getIcon(), left, top,
				UiSizes.getIconSize()) + UiSizes.getIconSize() / 2;

	}

	private void drawRightChkBox(Canvas canvas)
	{
		if (rBounds == null || rChkBounds == null)
		{
			rBounds = new CB_Rect(width - height - 10, 5, height - 10, height - 10);// = right Button bounds

			rChkBounds = rBounds.ScaleCenter(0.8);
		}
		ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, Global.getColor(R.attr.ListSeparator), BackgroundColor,
				UiSizes.getCornerSize());
	}

	public void plusClick()
	{
		this.mFilterSetEntry.plusClick();
	}

	public void minusClick()
	{
		this.mFilterSetEntry.minusClick();
	}

	public void stateClick()
	{
		this.mFilterSetEntry.stateClick();
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

}
