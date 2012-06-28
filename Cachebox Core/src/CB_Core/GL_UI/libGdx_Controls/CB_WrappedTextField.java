package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class CB_WrappedTextField extends CB_Core.GL_UI.libGdx_Controls.CB_TextField
{

	private CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField mTextField;

	public CB_WrappedTextField(CB_RectF rec, String Name)
	{

		super(rec, new CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField(Style.getTextFieldStyle()), Name);

		mTextField = (CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField) getActor();
		mTextField.setClipboard(GlobalCore.getDefaultClipboard());
	}

	@Override
	public void onShow()
	{
		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_IDLE);
	}

	@Override
	public void onStop()
	{
		GL_Listener.glListener.removeRenderView(this);
	}

	public void setText(String text)
	{
		mTextField.setText(text);
	}

	public void setSelection(int selectionStart, int selectionEnd)
	{
		mTextField.setSelection(selectionStart, selectionEnd);
	}

	public void setCursorPosition(int cursorPosition)
	{
		mTextField.setCursorPosition(cursorPosition);
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	public float getmesuredHeight()
	{
		return mTextField.getMesuredHeight();
	}

	public TextFieldStyle getStyle()
	{
		return mTextField.getStyle();
	}

	public void setTextChangedListner(TextFieldListener listner)
	{
		mTextField.setTextFieldListener(listner);
	}

	@Override
	public void resize(float width, float height)
	{
		mTextField.height = height;
		mTextField.width = width;
	}

}
