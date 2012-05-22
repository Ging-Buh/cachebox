package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Math.CB_RectF;

public class Toast extends Dialog
{
	public static final int LENGTH_SHORT = 1500;
	public static final int LENGTH_LONG = 3000;

	public TextField mTextField;

	public Toast(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mTextField = new TextField(rec, "Toast.TextField");
		mTextField.disable();
		super.removeChildsDirekt();
		super.addChildDirekt(mTextField);

		registerSkinChangedEvent();
	}

	@Override
	protected void Initial()
	{
		// tue nichts
	}

	public void setText(String txt)
	{
		mTextField.setText(txt);
	}

	public float getMesuredWidth()
	{
		return mTextField.getMesuredWidth();
	}

	@Override
	public void setWidth(float width)
	{
		super.setWidth(width);
		mTextField.setWidth(width);
	}

	@Override
	protected void SkinIsChanged()
	{
		mTextField = new TextField(this, "Toast.TextField");
		mTextField.disable();
		super.removeChildsDirekt();
		super.addChildDirekt(mTextField);

	}

}
