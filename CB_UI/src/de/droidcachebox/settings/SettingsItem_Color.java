package de.droidcachebox.settings;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public class SettingsItem_Color extends SettingsItemBase {
    private static CB_RectF Bounds;
    private Sprite colorRec;
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
            colorRec = Sprites.getSprite("text-field-back");
            colorRec.setBounds(Bounds.getX(), Bounds.getY(), Bounds.getWidth(), Bounds.getHeight());
            colorRec.setColor(colorSetting.getValue());
        }

        colorRec.draw(batch);

    }

}
