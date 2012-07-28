package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class CB_WrappedTextField extends CB_Core.GL_UI.libGdx_Controls.CB_TextField
{

	private CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField mTextField;

	private float left = 0;
	private float right = 0;
	private float top = 0;
	private float bottom = 0;

	public CB_WrappedTextField(CB_RectF rec, String Name)
	{

		super(rec, new CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField(Style.getTextFieldStyle()), Name);

		mTextField = (CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField) getActor();
		mTextField.setClipboard(GlobalCore.getDefaultClipboard());

		TextFieldStyle style = mTextField.getStyle();
		left = style.background.getLeftWidth();
		right = style.background.getRightWidth();
		top = style.background.getTopHeight();
		bottom = style.background.getBottomHeight();

		this.setBackground(style.background);

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
	protected void render(SpriteBatch batch)
	{

		if (hasNinePatchBackground)
		{
			nineBackground.draw(batch, 0, 0, width, height);
		}
		batch.end();

		CB_RectF innerScissorReg = intersectRec.copy();
		innerScissorReg.setHeight(intersectRec.getHeight() - top - bottom);
		innerScissorReg.setY(intersectRec.getY() + bottom);

		batch.begin();

		Gdx.gl.glScissor((int) innerScissorReg.getX(), (int) innerScissorReg.getY(), (int) innerScissorReg.getWidth() + 1,
				(int) innerScissorReg.getHeight() + 1);

		super.render(batch);
		// batch.end();
		// Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth() + 1,
		// (int) intersectRec.getHeight() + 1);
		// batch.begin();
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
