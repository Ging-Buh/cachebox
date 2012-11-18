package CB_Core.GL_UI.Activitys.settings;

import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

public class SettingsItemEnum extends SettingsItemBase
{

	Spinner spinner;

	public SettingsItemEnum(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);

		spinner = new Spinner(lblDefault, "", null, null);

		spinner.setHeight(UiSizes.getButtonHeight());

		this.addChild(spinner);

	}

	public Spinner getSpinner()
	{
		return spinner;
	}

	@Override
	protected void layout()
	{
		lblDefault.setHeight(spinner.getHeight());
		super.layout();
		spinner.setY(lblDefault.getY() + UiSizes.getMargin());

	}

}
