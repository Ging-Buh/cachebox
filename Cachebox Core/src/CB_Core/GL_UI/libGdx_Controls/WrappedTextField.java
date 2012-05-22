package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

public class WrappedTextField extends LibGdx_Host_Control
{

	private CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField mTextField;

	public WrappedTextField(CB_RectF rec, String Name)
	{

		super(rec, new CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField(Style.getWrappedTextFieldStyle()), Name);

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

}
