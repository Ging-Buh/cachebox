package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.PopUps.CopiePastePopUp;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

public class TextField extends LibGdx_Host_Control
{

	private com.badlogic.gdx.scenes.scene2d.ui.TextField mTextField;
	private CopiePastePopUp popUp;

	private TextField that;

	public TextField(CB_RectF rec, String Name)
	{

		super(rec, new com.badlogic.gdx.scenes.scene2d.ui.TextField(Style.getTextFieldStyle()), Name);
		that = this;

		mTextField = (com.badlogic.gdx.scenes.scene2d.ui.TextField) getActor();
		mTextField.setClipboard(GlobalCore.getDefaultClipboard());
		this.setClickable(true);

		this.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showPopUp(x, y);
				return true;
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

	public String getText()
	{
		return mTextField.getText();
	}

	public void setMsg(String msg)
	{
		mTextField.setMessageText(msg);
	}

	public void paste()
	{
		mTextField.paste();
	}

	private void showPopUp(int x, int y)
	{
		if (popUp == null)
		{
			popUp = new CopiePastePopUp(new CB_RectF(0, 0, UiSizes.getButtonWidth() * 1.2f, UiSizes.getButtonHeight()), "CopiePastePopUp=>"
					+ getName(), that);
		}

		float noseOffset = popUp.getHalfWidth() / 2;

		Logger.LogCat("Show CopyPaste PopUp");

		CB_RectF world = getWorldRec();

		// not enough place on Top?
		float windowH = UiSizes.getWindowHeight();
		float worldY = world.getY();

		if (popUp.getHeight() + worldY > windowH * 0.8f)
		{
			popUp.flipX();
			worldY -= popUp.getHeight() + (popUp.getHeight() * 0.2f);
		}

		x += world.getX() - noseOffset;
		y += worldY + (popUp.getHeight() * 0.2f);
		popUp.show(x, y);
	}
}
