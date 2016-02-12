package CB_UI.GL_UI.Activitys.settings;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Settings.SettingColor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SettingsItem_Color extends SettingsItemBase {
	private Sprite colorRec;
	private static CB_RectF Bounds;
	private SettingColor colorSetting;

	public SettingsItem_Color(CB_RectF rec, int Index, SettingColor colorSetting) {
		super(rec, Index, colorSetting.getName());
		this.colorSetting = colorSetting;
	}

	@Override
	public void render(Batch batch) {
		super.render(batch);
		if (Bounds == null) {
			Bounds = new CB_RectF(this.getWidth() - (getHeight() - 10), 5, getHeight() - 10, getHeight() - 10);
		}

		if (colorRec == null) {
			colorRec = SpriteCacheBase.getThemedSprite("text-field-back");
			colorRec.setBounds(Bounds.getX(), Bounds.getY(), Bounds.getWidth(), Bounds.getHeight());
			colorRec.setColor(colorSetting.getValue());
		}

		colorRec.draw(batch);

	}

}
