package CB_UI.GL_UI.Activitys.FilterSettings;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;

import CB_Core.FilterProperties;
import CB_UI.GL_UI.Activitys.FilterSettings.PresetListView.PresetEntry;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

public class PresetListViewItem extends ListViewItemBackground {
	private final PresetEntry mPresetEntry;

	public PresetListViewItem(CB_RectF rec, int Index, PresetEntry fne) {
		super(rec, Index, fne.getName());
		this.mPresetEntry = fne;
	}

	BitmapFontCache EntryName;
	float left = 0;
	float top = 0;

	@Override
	protected void render(Batch batch) {
		if (this.isDisposed())
			return;

		if (EditFilterSettings.tmpFilterProps != null) {
			if (chkPresetFilter(mPresetEntry.getFilterProperties(), EditFilterSettings.tmpFilterProps)) {
				if (!EditFilterSettings.tmpFilterProps.isExtendedFilter()) {
					isSelected = true;
				} else {
					isSelected = false;
				}
			}
		}

		super.render(batch);

		if (isPressed) {
			isPressed = GL.getIsTouchDown();
		}

		// initial
		left = getLeftWidth();
		top = (this.getHeight() + Fonts.getNormal().getLineHeight()) / 2f; //this.getTopHeight();

		drawIcon(batch);

		// draw Name
		if (EntryName == null) {
			EntryName = new BitmapFontCache(Fonts.getNormal());
			EntryName.setColor(COLOR.getFontColor());
			EntryName.setText(name, left + 10, top);
		}
		EntryName.draw(batch);

	}

	private void drawIcon(Batch batch) {
		if (mPresetEntry.getIcon() != null) {
			float iconHeight = this.getHeight() * 0.8f;
			float iconWidth = iconHeight;
			float y = (this.getHeight() - iconHeight) / 2f; // UI_Size_Base.that.getMargin()
			mPresetEntry.getIcon().setBounds(left, y, iconWidth, iconHeight);
			mPresetEntry.getIcon().draw(batch);
			left = left + iconWidth + y + getLeftWidth();
		}
	}

	/**
	 * Vergleicht einen PresetString mit einem FilterString, wobei die Categoryeinstellungen im FilterString ignoriert werden.
	 * 
	 * @param presetString
	 *            Der Preset String, mit dem der Filter verglichen wird.
	 * @param filterString
	 *            Der Filter String, mit dem das Preset verglichen werden soll.
	 * @return true wenn gleichheit
	 */
	public static boolean chkPresetFilter(FilterProperties presetFilter, FilterProperties filter) {
		return (presetFilter.equals(filter));
	}

	public PresetEntry getEntry() {
		return mPresetEntry;
	}
}
