package CB_UI.GL_UI.Activitys.settings;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.FloatControl;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;

public class SettingsItem_Audio extends SettingsItemBase
{

	chkBox Check;
	FloatControl volumeControl;

	/**
	 * @param rec
	 * @param Index
	 * @param Name
	 * @param full
	 *            boolean /show with Path-Settings
	 */
	public SettingsItem_Audio(CB_RectF rec, int Index, String Name, Boolean full, FloatControl.iValueChanged listner)
	{
		super(rec, Index, Name);

		// TODO add option for load SoundFile
		// if (full) this.setHeight(rec.getHeight() * 2);

		Check = new chkBox("");
		Check.setX(this.getWidth() - rightBorder - Check.getWidth());
		Check.setY(this.getHalfHeight() - Check.getHalfHeight());

		lblName.setWidth(lblName.getWidth() - Check.getWidth() - rightBorder);

		// remove default Label
		this.removeChild(lblDefault);
		lblDefault.dispose();
		lblDefault = null;

		// add volume control
		float ProgressHeight = (SpriteCacheBase.ProgressBack.getBottomHeight() + SpriteCacheBase.ProgressBack.getTopHeight()) * 1.35f;

		CB_RectF rec2 = rec.copy();

		float margin = UiSizes.that.getMargin();

		rec2.setHeight(ProgressHeight);
		rec2.setWidth(this.getWidth() - Check.getWidth() - (margin * 2));
		rec2.setX(margin);
		volumeControl = new FloatControl(rec2, "", listner);
		volumeControl.setProgress(50);

		this.addChild(Check);
		this.addChild(volumeControl);

	}

	public chkBox getCheckBox()
	{
		return Check;
	}

	@Override
	public void setDefault(String def)
	{
		layout();
	}

	@Override
	protected void layout()
	{
		// don't call super.layout(); DefaultLabel is NULL

		Check.setY(this.getHalfHeight() - Check.getHalfHeight());

		float asc = lblName.getFont().getDescent();

		lblName.setY(this.getHeight() - lblName.getHeight() + asc);
		lblName.setX(this.getLeftWidth());
	}

	public void setVolume(int value)
	{
		volumeControl.setProgress(value);
	}

	public void setMuteDisabeld(boolean checked)
	{
		// Set VolumeControl to disabled
		volumeControl.disable(checked);
	}

	public void disable()
	{
		volumeControl.disable(true);
		Check.disable();
	}

	public void enable()
	{
		if (!Check.isChecked()) volumeControl.disable(false);
		Check.enable();
	}
}
