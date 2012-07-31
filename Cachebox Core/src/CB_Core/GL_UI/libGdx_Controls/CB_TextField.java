package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.PopUps.CopiePastePopUp;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.OnscreenKeyboard;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class CB_TextField extends LibGdx_Host_Control
{

	private com.badlogic.gdx.scenes.scene2d.ui.TextField mTextField;
	private CopiePastePopUp popUp;

	private CB_TextField that;

	public CB_TextField(CB_RectF rec, CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField wrappedTextField, String Name)
	{
		super(rec, new CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField("", Style.getTextFieldStyle()), Name);
		that = this;

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Handle ClickEvent for vibrate feedback
				return true;
			}
		});

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

	public CB_TextField(CB_RectF rec, String Name)
	{

		super(rec, new com.badlogic.gdx.scenes.scene2d.ui.TextField("", Style.getTextFieldStyle()), Name);
		that = this;

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Handle ClickEvent for vibrate feedback
				return true;
			}
		});

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

	public void setOnscreenKeyboard(OnscreenKeyboard keyboard)
	{
		mTextField.setOnscreenKeyboard(keyboard);
	}

	@Override
	public void onShow()
	{

	}

	@Override
	public void onStop()
	{

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

	public int getCursorPosition()
	{
		return mTextField.getCursorPosition();
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

		// Logger.LogCat("Show CopyPaste PopUp");

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

	public float getMesuredWidth()
	{
		TextFieldStyle style = mTextField.getStyle();
		float back = style.background.getLeftWidth() + style.background.getRightWidth();
		float txtW = style.font.getBounds(mTextField.getText()).width;
		return back + txtW;
	}

	public void enable()
	{
		mTextField.setTouchable(Touchable.enabled);
	}

	public void disable()
	{
		mTextField.setTouchable(Touchable.disabled);
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
		// TODO Auto-generated method stub

	}

	boolean hasFocus = false;

	public void setFocus()
	{
		hasFocus = true;
		mTextField.hit(0, 0);
		setTextFieldStyle();
	}

	public void setFocus(boolean value)
	{
		hasFocus = value;
		if (value == true) if (mTextField.getStage() != null) mTextField.getStage().setKeyboardFocus(mTextField);
		setTextFieldStyle();
	}

	public void resetFocus()
	{
		hasFocus = false;
		if (mTextField.getStage() != null) mTextField.getStage().setKeyboardFocus(null);
		setTextFieldStyle();
	}

	private void setTextFieldStyle()
	{
		TextFieldStyle style = null;

		if (!hasFocus)
		{
			style = Style.getTextFieldStyle();
		}
		else
		{
			style = Style.getTextFieldStyleFocus();
		}

		mTextField.setStyle(style);
		this.setBackground(style.background);
	}

	@Override
	public void resize(float width, float height)
	{
		mTextField.setHeight(height);
		mTextField.setWidth(width);
	}
}
