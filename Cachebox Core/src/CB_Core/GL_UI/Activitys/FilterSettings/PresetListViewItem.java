package CB_Core.GL_UI.Activitys.FilterSettings;

import CB_Core.FilterProperties;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Activitys.FilterSettings.PresetListView.PresetEntry;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PresetListViewItem extends ListViewItemBackground
{
	private PresetEntry mPresetEntry;

	public PresetListViewItem(CB_RectF rec, int Index, PresetEntry fne)
	{
		super(rec, Index, fne.getName());
		this.mPresetEntry = fne;
	}

	BitmapFontCache EntryName;
	float left = 0;
	float top = 0;

	@Override
	protected void render(SpriteBatch batch)
	{
		if (EditFilterSettings.tmpFilterProps != null)
		{
			if (chkPresetFilter(mPresetEntry.getFilterProperties(), EditFilterSettings.tmpFilterProps))
			{
				if (!EditFilterSettings.tmpFilterProps.isExtendsFilter())
				{
					isSelected = true;
				}
				else
				{
					isSelected = false;
				}
			}
		}

		super.render(batch);

		if (isPressed)
		{
			isPressed = GL.getIsTouchDown();
		}

		// initial
		left = getLeftWidth();
		top = this.height - this.getTopHeight();

		drawIcon(batch);

		// draw Name
		if (EntryName == null)
		{
			EntryName = new BitmapFontCache(Fonts.getNormal());
			EntryName.setColor(Fonts.getFontColor());
			EntryName.setMultiLineText(name, left + 10, top);
		}
		EntryName.draw(batch);

	}

	private void drawIcon(SpriteBatch batch)
	{
		if (mPresetEntry.getIcon() != null)
		{
			float iconHeight = this.height * 0.8f;
			float iconWidth = iconHeight;
			mPresetEntry.getIcon().setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
			mPresetEntry.getIcon().draw(batch);
			left += iconWidth + UI_Size_Base.that.getMargin() + getLeftWidth();
		}
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
	public static boolean chkPresetFilter(FilterProperties presetFilter, FilterProperties filter)
	{
		return (presetFilter.equals(filter));
		//
		// // exclude Category filter, cut String after 85 comma
		// String probs = filterString;
		// String[] commaSplit = probs.split(",");
		// probs = "";
		// if (commaSplit.length < 85) return false;
		// for (int i = 0; i < 85; i++)
		// {
		// probs += commaSplit[i] + ",";
		// }
		//
		// probs += ",,,,";
		//
		// String preset = presetString;
		// commaSplit = preset.split(",");
		// preset = "";
		// if (commaSplit.length < 85) return false;
		// for (int i = 0; i < 85; i++)
		// {
		// preset += commaSplit[i] + ",";
		// }
		//
		// preset += ",,,,";
		//
		// return probs.equals(preset);
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public PresetEntry getEntry()
	{
		return mPresetEntry;
	}
}
