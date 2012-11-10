package CB_Core.GL_UI.Activitys.FilterSettings;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Activitys.FilterSettings.PresetListView.PresetEntry;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

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

		boolean PresetEquelsFilter = false;
		if (EditFilterSettings.tmpFilterProps != null)
		{
			if (chkPresetFilter(mPresetEntry.getPresetString(), EditFilterSettings.tmpFilterProps.ToString()))
			{
				if (!EditFilterSettings.tmpFilterProps.isExtendsFilter())
				{
					PresetEquelsFilter = true;
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
		left = LeftWidth;
		top = this.height - this.TopHeight;

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
			mPresetEntry.getIcon().setBounds(left, UiSizes.getMargin(), iconWidth, iconHeight);
			mPresetEntry.getIcon().draw(batch);
			left += iconWidth + UiSizes.getMargin() + LeftWidth;
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

		String preset = presetString;
		commaSplit = preset.split(",");
		preset = "";
		if (commaSplit.length < 85) return false;
		for (int i = 0; i < 85; i++)
		{
			preset += commaSplit[i] + ",";
		}

		preset += ",,,,";

		return probs.equals(preset);
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
