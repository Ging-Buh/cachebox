package de.droidcachebox.Views.FilterSettings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.FilterSettings.PresetListView.PresetEntry;

public class PresetListViewItem extends View
{
	private PresetEntry mPresetEntry;

	private static int width;
	private static int height = 0;

	private boolean BackColorChanger = false;
	private StaticLayout layoutEntryName;

	private static TextPaint textPaint;

	public PresetListViewItem(Context context, PresetEntry fne, Boolean BackColorId)
	{
		super(context);

		this.mPresetEntry = fne;
		BackColorChanger = BackColorId;

		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setTextSize(Sizes.getScaledFontSize_normal());
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		width = PresetListView.windowW;

		height = Sizes.getIconSize() + (Sizes.getCornerSize() * 2);
		setMeasuredDimension(width, height);

	}

	// static Member

	private static Paint TextPaint;

	@Override
	protected void onDraw(Canvas canvas)
	{

		boolean PresetEquelsFilter = false;
		if (EditFilterSettings.tmpFilterProps != null)
		{
			if (chkPresetFilter(mPresetEntry.getPresetString(), EditFilterSettings.tmpFilterProps.ToString()))
			{
				if (!EditFilterSettings.tmpFilterProps.isExtendsFilter()) PresetEquelsFilter = true;
			}
		}
		// initial
		int left = Sizes.getCornerSize();
		int top = Sizes.getCornerSize();

		if (TextPaint == null)
		{
			TextPaint = new Paint();
			TextPaint.setAntiAlias(true);
			TextPaint.setFakeBoldText(true);
			TextPaint.setTextSize((float) (Sizes.getScaledFontSize_big()));
			TextPaint.setColor(Global.getColor(R.attr.TextColor));
		}

		if (layoutEntryName == null)
		{
			int innerWidth = width - (Sizes.getCornerSize() * 2) - Sizes.getIconSize();
			layoutEntryName = new StaticLayout(mPresetEntry.getName(), textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		}

		textPaint.setColor(Global.getColor(R.attr.TextColor));

		int BackgroundColor;
		if (BackColorChanger)
		{
			BackgroundColor = (PresetEquelsFilter) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
		}
		else
		{
			BackgroundColor = (PresetEquelsFilter) ? Global.getColor(R.attr.ListBackground_select) : Global
					.getColor(R.attr.ListBackground_secend);
		}

		ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(5, 5, width - 5, height - 5), 2, Global.getColor(R.attr.ListSeparator),
				BackgroundColor, Sizes.getCornerSize());

		// draw Icon
		left += ActivityUtils.PutImageTargetHeight(canvas, mPresetEntry.getIcone(), left, top, Sizes.getIconSize())
				+ Sizes.getHalfCornerSize();

		ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left, top);

	}

	/**
	 * Vergleicht einen PresetString mit einem FilterString, wobei die Category
	 * einstellungen im FilterString ignoriert werden.
	 * 
	 * @param presetString
	 *            Der Preset String, mit dem der Filter verglichen wird.
	 * @param filterString
	 *            Der Filter String, mit dem das Preset verglichen werden soll.
	 * @return true wenn gleichheit
	 */
	public static boolean chkPresetFilter(String presetString, String filterString)
	{
		// exclude Category filter, cut String after 85 comma
		String probs = filterString;
		String[] commaSplit = probs.split(",");
		probs = "";
		if (commaSplit.length < 85) return false;
		for (int i = 0; i < 85; i++)
		{
			probs += commaSplit[i] + ",";
		}

		probs += ",,,,";

		return probs.equals(presetString);
	}
}
