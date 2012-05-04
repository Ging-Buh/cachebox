package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.derived.WrapedTextField;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;

public class TextField extends LibGdx_Host_Control
{

	private WrapedTextField mTextField;

	public TextField(CB_RectF rec, String Name)
	{

		super(rec, new WrapedTextField(Style.getTextFieldStyle()), Name);

		mTextField = (WrapedTextField) getActor();

		mTextField.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char key)
			{
				// TODO Auto-generated method stub

			}
		});

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

}
