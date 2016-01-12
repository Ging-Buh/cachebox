package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

public class FloatControl extends CB_View_Base
{
	ProgressBar progressbar;
	Button slideButton;

	public interface iValueChanged
	{
		public void ValueChanged(int value);
	}

	iValueChanged changeListener;

	public FloatControl(CB_RectF rec, String Name, iValueChanged listener)
	{
		super(rec, Name);
		changeListener = listener;
		progressbar = new ProgressBar(rec, "");
		progressbar.setHeight(this.getHeight() * 0.75f);
		progressbar.setText("");
		progressbar.setZeroPos();
		progressbar.setY(getHalfHeight() - progressbar.getHalfHeight());
		this.addChild(progressbar);
		slideButton = new Button(rec, "");
		slideButton.setWidth(this.getHeight());
		slideButton.setZeroPos();
		slideButton.setDrageble();
		this.addChild(slideButton);
	}

	public void setProgress(int value)
	{
		float progressDrawWidth = progressbar.setProgress(value);
		float ButtonPos = progressDrawWidth - slideButton.getHalfWidth();
		if (ButtonPos < 0) ButtonPos = 0;
		if (ButtonPos > this.getWidth() - slideButton.getWidth()) ButtonPos = this.getWidth() - slideButton.getWidth();

		slideButton.setX(ButtonPos);
		GL.that.renderOnce();
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (slideButton.isDisabled()) return false;
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (slideButton.isDisabled()) return false;
		if (!KineticPan)
		{
			int progress = (int) (100 / (getWidth() / x));
			if (progress >= 0 && progress <= 100) this.setProgress(progress);
		}

		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (slideButton.isDisabled()) return false;
		if (changeListener != null) changeListener.ValueChanged(progressbar.getProgress());
		return true;
	}

	public void disable(boolean checked)
	{
		if (checked)
		{
			slideButton.disable();
			progressbar.disable();
		}
		else
		{
			slideButton.enable();
			progressbar.enable();
		}

	}

}
