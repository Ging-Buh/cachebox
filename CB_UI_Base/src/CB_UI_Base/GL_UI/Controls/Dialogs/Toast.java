package CB_UI_Base.GL_UI.Controls.Dialogs;

import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.Math.CB_RectF;

public class Toast extends Dialog
{
	public static final int LENGTH_SHORT = 1500;
	public static final int LENGTH_LONG = 3000;

	protected Label mTextField;

	public Toast(CB_RectF rec, String Name)
	{
		super(rec, Name);

		mTextField = new Label(rec, "Toast.Label");
		mTextField.setHAlignment(HAlignment.CENTER);
		mTextField.setVAlignment(VAlignment.CENTER);

		mTextField.setZeroPos();

		super.RemoveChildsFromOverlay();
		super.addChildToOverlay(mTextField);

	}

	@Override
	protected void Initial()
	{
		// tue nichts
	}

	public void setWrappedText(String txt)
	{
		mTextField.setWrappedText(txt);
	}

	@Override
	public void setWidth(float width)
	{
		super.setWidth(width);
		mTextField.setWidth(width);
		mTextField.setZeroPos();
	}

	@Override
	public void setHeight(float height)
	{
		super.setHeight(height);
		mTextField.setHeight(height - this.topBorder - this.bottomBorder);
		mTextField.setZeroPos();
	}

	@Override
	protected void SkinIsChanged()
	{
	}

}
