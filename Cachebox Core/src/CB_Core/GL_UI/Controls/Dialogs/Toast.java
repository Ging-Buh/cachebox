package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class Toast extends Dialog
{
	public static final int LENGTH_SHORT = 1500;
	public static final int LENGTH_LONG = 3000;

	public Label mTextField;

	public Toast(CB_RectF rec, String Name)
	{
		super(rec, Name);

		mTextField = new Label(rec, "Toast.Label");
		mTextField.setHAlignment(HAlignment.CENTER);
		mTextField.setVAlignment(VAlignment.CENTER);

		mTextField.setZeroPos();

		super.RemoveChildsFromOverlay();
		super.addChildToOverlay(mTextField);

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

	@Override
	public void setWidth(float width)
	{
		super.setWidth(width);
		mTextField.setWidth(width);
	}

	@Override
	protected void SkinIsChanged()
	{
		mTextField = new Label(this, "Toast.Label");
		mTextField.setHAlignment(HAlignment.CENTER);
		mTextField.setVAlignment(VAlignment.CENTER);

		mTextField.setZeroPos();

		super.RemoveChildsFromOverlay();
		super.addChildToOverlay(mTextField);

	}

}
