package de.cachebox_test.Views.FilterSettings;

import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Views.FilterSettings.PresetListView.PresetEntry;

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
			textPaint.setTextSize(UiSizes.getScaledFontSize());
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		width = PresetListView.windowW;

		height = UiSizes.getIconSize() + (UiSizes.getCornerSize() * 2);
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
		int left = UiSizes.getCornerSize();
		int top = UiSizes.getCornerSize();

		if (TextPaint == null)
		{
			TextPaint = new Paint();
			TextPaint.setAntiAlias(true);
			TextPaint.setFakeBoldText(true);
			TextPaint.setTextSize((float) (UiSizes.getScaledFontSize_big()));
			TextPaint.setColor(Global.getColor(R.attr.TextColor));
		}

		if (layoutEntryName == null)
		{
			int innerWidth = width - (UiSizes.getCornerSize() * 2) - UiSizes.getIconSize();
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

		ActivityUtils.drawFillRoundRecWithBorder(canvas, new CB_Rect(5, 5, width - 10, height - 10), 2,
				Global.getColor(R.attr.ListSeparator), BackgroundColor, UiSizes.getCornerSize());

		// draw Icon
		left += ActivityUtils.PutImageTargetHeight(canvas, mPresetEntry.getIcone(), left, top, UiSizes.getIconSize())
				+ UiSizes.getHalfCornerSize();

		ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left, top);

	}

	/**
	 * Vergleicht einen PresetString mit einem FilterString, wobei die Category einstellungen im FilterString ignoriert werden.
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
