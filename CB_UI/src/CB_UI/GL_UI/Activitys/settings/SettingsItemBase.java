package CB_UI.GL_UI.Activitys.settings;

import CB_UI.GL_UI.Fonts;
import CB_UI.GL_UI.Controls.Label;
import CB_UI.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI.Math.CB_RectF;

public class SettingsItemBase extends ListViewItemBackground
{
	protected Label lblName, lblDefault;

	private static float MeasuredLabelHeight = -1;

	public SettingsItemBase(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);
		Initial();

		if (MeasuredLabelHeight == -1) MeasuredLabelHeight = Fonts.MeasureSmall("Tg").height;

		CB_RectF LblRec = new CB_RectF(leftBorder, 0, innerWidth, this.halfHeight);

		lblDefault = new Label(LblRec, "");
		lblDefault.setFont(Fonts.getSmall());
		this.addChild(lblDefault);

		LblRec.setY(MeasuredLabelHeight);
		LblRec.setHeight(this.height - MeasuredLabelHeight);

		lblName = new Label(LblRec, "");
		lblName.setFont(Fonts.getNormal());
		this.addChild(lblName);

		this.setLongClickable(true);

	}

	@Override
	protected void SkinIsChanged()
	{
	}

	public void setName(String name)
	{
		lblName.setWrappedText(name);
		lblName.setHeight(lblName.getTextHeight());
		layout();
	}

	public void setDefault(String def)
	{
		lblDefault.setWrappedText(def);
		lblDefault.setHeight(lblDefault.getTextHeight());
		layout();
	}

	protected void layout()
	{
		float asc = lblDefault.getFont().getDescent() * 2;

		lblDefault.setY(this.getBottomHeight() + asc);

		asc = lblName.getFont().getDescent();

		float a = 0;

		if (lblName.getLineCount() == 1) a = asc *= 2;

		lblName.setY(lblDefault.getMaxY() - (asc * 2) + a);

		this.setHeight(this.getBottomHeight() + lblDefault.getHeight() + lblName.getHeight() - asc);

	}

}
