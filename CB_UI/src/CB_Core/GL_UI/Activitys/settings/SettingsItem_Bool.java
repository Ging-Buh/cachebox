package CB_Core.GL_UI.Activitys.settings;

import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.Math.CB_RectF;

public class SettingsItem_Bool extends SettingsItemBase
{

	chkBox Check;

	public SettingsItem_Bool(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);

		Check = new chkBox("");
		Check.setX(this.width - rightBorder - Check.getWidth());
		Check.setY(this.halfHeight - Check.getHalfHeight());

		lblName.setWidth(lblName.getWidth() - Check.getWidth() - rightBorder);

		lblDefault.setWidth(lblName.getWidth());

		this.addChild(Check);

	}

	public chkBox getCheckBox()
	{
		return Check;
	}

	@Override
	protected void layout()
	{
		super.layout();
		Check.setY(this.halfHeight - Check.getHalfHeight());
	}

}
