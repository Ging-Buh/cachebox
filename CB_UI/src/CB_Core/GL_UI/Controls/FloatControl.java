package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;

public class FloatControl extends CB_View_Base
{
	ProgressBar progressbar;
	Button slideButton;

	public interface iValueChanged
	{
		public void ValueChanged(int value);
	}

	iValueChanged changeListner;

	public FloatControl(CB_RectF rec, String Name, iValueChanged listner)
	{
		super(rec, Name);
		changeListner = listner;
		progressbar = new ProgressBar(rec, "");
		progressbar.setHeight(this.height * 0.75f);
		progressbar.setText("");
		progressbar.setZeroPos();
		progressbar.setY(halfHeight - progressbar.getHalfHeight());
		this.addChild(progressbar);
		slideButton = new Button(rec, "");
		slideButton.setWidth(this.height);
		slideButton.setZeroPos();
		slideButton.setDrageble();
		this.addChild(slideButton);
	}

	public void setProgress(int value)
	{
		float progressDrawWidth = progressbar.setProgress(value);
		float ButtonPos = progressDrawWidth - slideButton.getHalfWidth();
		if (ButtonPos < 0) ButtonPos = 0;
		if (ButtonPos > this.width - slideButton.getWidth()) ButtonPos = this.width - slideButton.getWidth();

		slideButton.setX(ButtonPos);
		GL.that.renderOnce("ProgressBar state changed");
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
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (!KineticPan)
		{
			int progress = (int) (100 / (width / x));
			if (progress >= 0 && progress <= 100) this.setProgress(progress);
		}

		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (changeListner != null) changeListner.ValueChanged(progressbar.getProgress());
		return true;
	}

}
