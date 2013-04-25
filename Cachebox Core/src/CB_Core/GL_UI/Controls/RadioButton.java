package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RadioButton extends chkBox
{

	private RadioGroup group;
	private Image radioBack;
	private Image radioSet;

	public RadioButton(String Name)
	{
		super(Name);
		radioBack = new Image(new CB_RectF(UI_Size_Base.that.getChkBoxSize()), name);
		radioBack.setDrawable(SpriteCache.radioBack);
		this.addChild(radioBack);

		radioSet = new Image(new CB_RectF(UI_Size_Base.that.getChkBoxSize()), name);
		radioSet.setDrawable(SpriteCache.radioOn);
		this.addChild(radioSet);
	}

	public void setRadioGroup(RadioGroup Group)
	{
		group = Group;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (lblTxt != null && lblTxt.getX() < radioBack.getMaxX())
		{
			lblTxt.setX(radioBack.getMaxX() + UI_Size_Base.that.getMargin());
		}

		if (isChk && !radioSet.isVisible())
		{
			radioSet.setVisible();
		}
		else if (!isChk && radioSet.isVisible())
		{
			radioSet.setVisible(false);
		}

		super.render(batch);
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		if (!isDisabled)
		{
			if (!isChk || group == null)
			{
				isChk = !isChk;
				if (changeListner != null) changeListner.onCheckedChanged(this, isChk);
				if (group != null) group.aktivate(this);

			}
		}
		return true;
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}
