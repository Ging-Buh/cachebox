package CB_UI.GL_UI.Controls;

import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.Math.CB_RectF;
import CB_UI.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
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
		radioBack.setDrawable(SpriteCacheBase.radioBack);
		this.addChild(radioBack);

		radioSet = new Image(new CB_RectF(UI_Size_Base.that.getChkBoxSize()), name);
		radioSet.setDrawable(SpriteCacheBase.radioOn);
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
	}

	@Override
	public void setText(String Text, Color color)
	{
		setText(Text, null, color, HAlignment.LEFT);
	}

	@Override
	public void setText(String Text)
	{
		setText(Text, null, null, HAlignment.LEFT);
	}

	public void setText(String Text, HAlignment alignment)
	{
		setText(Text, null, null, alignment);
	}
}
