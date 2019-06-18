package CB_UI.GL_UI.Activitys.settings;

import CB_UI_Base.GL_UI.Controls.CB_CheckBox;
import CB_UI_Base.Math.CB_RectF;

public class SettingsItem_Bool extends SettingsItemBase {

    CB_CheckBox Check;

    public SettingsItem_Bool(CB_RectF rec, int Index, String Name) {
        super(rec, Index, Name);

        Check = new CB_CheckBox("");
        Check.setX(this.getWidth() - rightBorder - Check.getWidth());
        Check.setY(this.getHalfHeight() - Check.getHalfHeight());

        lblName.setWidth(lblName.getWidth() - Check.getWidth() - rightBorder);

        lblDefault.setWidth(lblName.getWidth());

        this.addChild(Check);

    }

    public CB_CheckBox getCheckBox() {
        return Check;
    }

    @Override
    protected void layout() {
        super.layout();
        Check.setY(this.getHalfHeight() - Check.getHalfHeight());
    }

}
