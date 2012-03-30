package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.math.Vector2;

public class CB_ButtonListItem extends ListViewItemBase
{
	CB_Button mCB_Button;

	public CB_ButtonListItem(CB_RectF rec, int Index, CharSequence Name)
	{
		super(rec, Index, Name);
		mCB_Button = new CB_Button(this, "Button");
		mCB_Button.setPos(new Vector2(0, 0));
		mCB_Button.setSize(GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);
		this.addChild(mCB_Button);
	}

	public CB_ButtonListItem(int Index, CB_Button Button, CharSequence Name)
	{
		super(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), Index, Name);
		mCB_Button = Button;
		mCB_Button.setPos(new Vector2(0, 0));
		mCB_Button.setSize(GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);
		this.addChild(mCB_Button);
	}

	@Override
	protected void Initial()
	{

	}

}
