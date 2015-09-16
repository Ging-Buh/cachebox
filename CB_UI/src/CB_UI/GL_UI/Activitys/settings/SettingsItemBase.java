package CB_UI.GL_UI.Activitys.settings;

import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;

public class SettingsItemBase extends ListViewItemBackground
{
	protected Label lblName, lblDefault;
	protected boolean isDisabled = false;

	private static float MeasuredLabelHeight = -1;

	public SettingsItemBase(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);
		Initial();

		if (MeasuredLabelHeight == -1) MeasuredLabelHeight = Fonts.MeasureSmall("Tg").height;

		CB_RectF LblRec = new CB_RectF(leftBorder, 0, innerWidth, this.getHalfHeight());

		lblDefault = new Label(LblRec, "");
		lblDefault.setFont(Fonts.getSmall());
		this.addChild(lblDefault);

		LblRec.setY(MeasuredLabelHeight);
		LblRec.setHeight(this.getHeight() - MeasuredLabelHeight);

		lblName = new Label(LblRec, "");
		lblName.setFont(Fonts.getNormal());
		this.addChild(lblName);

		this.setLongClickable(true);

	}

	public void enable()
	{
		isDisabled = false;
		this.clearColorFilter();
	}

	public void disable()
	{
		isDisabled = true;
		this.setColorFilter(COLOR.getDisableFontColor());
	}

	public boolean isDisabled()
	{
		return isDisabled;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		// wenn Item disabled ein Behandelt zurück schicken,
		// damit keine weiteren Abfragen durchgereicht werden.
		// Auch wenn dieses Item ein OnClickListner hat.
		if (isDisabled) return true;
		else
			return super.click(x, y, pointer, button);
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
